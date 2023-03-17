package api.per.api;

import api.BaseAPI;
import api.per.model.PerAccident;
import api.per.model.PerCause;
import api.per.model.PerCfg;
import api.per.model.PerContract;
import api.per.model.PerContractHist;
import api.per.model.PerEmployee;
import api.per.model.PerFlowInsClaim;
import api.per.model.PerInsClaim;
import api.per.model.PerPayType;
import api.per.model.PerProfCfg;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import web.ShortException;

@Path("/perAccident")
public class PerAccidentApi extends BaseAPI {
    
    @POST
    public Response postPerAccident(PerAccident perAccident){
        try(Connection con = getConnection()) {
            validate(perAccident);
            SessionLogin sl = getSession(con);

            PerProfCfg profCfg = getPerProfCfg(con);
            PerEmployee perEmployee =getPerEmployee(con);

            if(perEmployee.id != perAccident.empId
                    && !profCfg.regAnyAttenNov){
                throw new ShortException("No tiene permisos para registrar este evento.");
            }

            PerCfg cfg = getPerCfg(con);
            PerContract contract = getContract(perAccident.empId, con);

            if(contract == null) {
                throw new ShortException("El empleado no tiene contratos vigentes");
            }

            perAccident.loadedDays = 0;
            perAccident.employeerId = contract.employeerId;
            perAccident.investigate = false;
            perAccident.active = true;

            perAccident.insert(con);

            if(cfg.hasInsClaim) {
                PerCause cause = getCause(perAccident.causeId, con);


                PerInsClaim claim = new PerInsClaim();
                claim.arlId = perAccident.entityId;
                claim.state = "req";
                claim.vlrRad = getValRad(cause, contract, con);
                claim.accId = perAccident.id;
                PerContractHist pch = getActualContractHistory(contract, con);
                claim.conHisId = pch.id;
                claim.insert(con);

                PerFlowInsClaim flow = new PerFlowInsClaim();
                flow.insClaimId = claim.id;
                flow.regDate = new Date();
                flow.notes = "Se creo el accidente";
                flow.state = "req";
                flow.insert(con);
            }

            return createResponse(perAccident);
        } catch (Exception e) {
            e.printStackTrace();
            return createResponse(e);
        }
    }


    private PerCfg getPerCfg(Connection con) throws Exception {
        return new PerCfg().select(1, con);
    }

    private PerProfCfg getPerProfCfg(Connection con) throws Exception {
        SessionLogin sl = getSession(con);
        MySQLQuery q = new MySQLQuery("SELECT " +
                PerProfCfg.getSelFlds("c") +
                "FROM login l " +
                "INNER JOIN profile p ON l.profile_id = p.id " +
                "INNER JOIN per_prof_cfg c ON p.id = c.prof_id " +
                "WHERE p.menu_id = 391 " +
                "AND l.employee_id = ?1")
            .setParam(1, sl.employeeId);

        return new PerProfCfg().select(q, con);
    }

    private PerEmployee getPerEmployee(Connection con) throws Exception {
        SessionLogin sl = getSession(con);
        MySQLQuery q = new MySQLQuery("SELECT " +
                PerEmployee.getSelFlds("pe") +
                "FROM per_employee pe " +
                        "WHERE pe.emp_id = ?1 ")
                .setParam(1, sl.employeeId);

        return new PerEmployee().select(q, con);
    }

    private PerContract getContract(int employeeId, Connection con) throws Exception{
        String query = "SELECT " +
            PerContract.getSelFlds("pc") +
            "FROM per_contract pc " +
            "WHERE pc.emp_id = ?1 " +
            "AND pc.last = 1 " +
            "AND pc.active = 1 " +
            "AND pc.leave_date IS NULL ";

        MySQLQuery q = new MySQLQuery(query)
            .setParam(1, employeeId);
        
        try {
            return new PerContract().select(q, con);
        } catch(Exception ex){
            return null;
        }
    }

    private PerCause getCause(int causeId, Connection con) throws Exception{
        return new PerCause()
                .select(causeId, con);
    } 

    private void validate(PerAccident perAccident) throws Exception {
        if(perAccident.empId == 0
                || perAccident.regDate == null
                || perAccident.causeId == 0
                || perAccident.days == 0
                || perAccident.entityId == 0)
            throw new ShortException("Datos incompletos");

    }

    private BigDecimal getValRad(PerCause cause, PerContract contract, Connection con) throws Exception {
        BigDecimal contractValue = getContractPayValue(contract, con);
        BigDecimal percent = new BigDecimal(cause.porcent)
                .divide(new BigDecimal(100));

        return contractValue.multiply(percent);
    }

    private PerContractHist getActualContractHistory(PerContract contract, Connection con) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT " +
                PerContractHist.getSelFlds("pch") +
                "FROM per_contract_hist pch " +
                "WHERE pch.contract_id = ?1 " +
                "AND pch.active = 1 " +
                "ORDER BY id DESC " +
                "LIMIT 1 ")
                .setParam(1, contract.id);

        PerContractHist pch = new PerContractHist().select(q, con);

        return pch; 
    }

    private BigDecimal getContractPayValue(PerContract contract, Connection con) throws Exception {
        return contract.payTypeId == null
                ? contract.payValue
                : new PerPayType().select(contract.payTypeId, con).value;
    }

    @GET
    @Path("/perEmployee/{perEmployeeId}")
    public Response getEmployeeAccidents(@PathParam("perEmployeeId") int perEmployeeId) {
        try(Connection con = getConnection()) {
            MySQLQuery q = new MySQLQuery("SELECT " +
                    "   pa.id, " +
                    "	pa.reg_date, " +
                    "	pc.NAME, " +
                    "	pa.days, " +
                    "   pic.state " +
                    "FROM per_accident pa " +
                    "JOIN per_cause pc ON pa.cause_id = pc.id " +
                    "JOIN per_ins_claim pic ON pic.acc_id = pa.id " +
                    "WHERE pa.emp_id = ?1 " +
                    "AND pa.active = 1 " +
                    "AND pic.state <> 'calcel' " +
                    "AND pic.state <> 'closed' " +
                    "ORDER BY pa.reg_date DESC "
                    )
                    .setParam(1, perEmployeeId);

            Object [][] records = q.getRecords(con);
            List<List<Object>> list = new ArrayList<>();
            for (Object[] record : records) {
                ArrayList row = new ArrayList();
                row.add(MySQLQuery.getAsInteger(record[0]));
                row.add(MySQLQuery.getAsDate(record[1]));
                row.add(MySQLQuery.getAsString(record[2]));
                row.add(MySQLQuery.getAsInteger(record[3]));
                row.add(MySQLQuery.getAsString(record[4]));
                list.add(row);
            }

            return createResponse(list);
        } catch(Exception ex){
            return createResponse(ex);
        }
    }

    public List<Object> arrayToList(Object[] array) {
        return Arrays.asList(array);
    }
}
