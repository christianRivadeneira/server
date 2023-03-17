package api.bill.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.bill.model.BillProspect;
import api.bill.model.BillProspectService;
import api.bill.model.BillServiceType;
import api.sys.model.SysCrudLog;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import metadata.model.GridRequest;
import model.system.SessionLogin;
import utilities.MySQLQuery;

@Path("/billProspectService")
public class BillProspectServiceApi extends BaseAPI {

    private BigDecimal div(BigDecimal v) {
        return v.divide(new BigDecimal(100), RoundingMode.HALF_EVEN);
    }

    private void calc(BillProspectService obj, Connection conn) throws Exception {
        BillServiceType st = new BillServiceType().select(obj.typeId, conn);
        String accCode = st.accCode;
        if (accCode == null || accCode.isEmpty()) {
            throw new Exception("El servicio '" + st.name + "' no tiene código contable.");
        }
        obj.netVal = obj.comVal.multiply(BigDecimal.ONE.subtract(div(obj.dtoRate))).multiply(BigDecimal.ONE.subtract(div(obj.subsRate)));
        obj.tax = obj.netVal.multiply(div(obj.taxRate));
    }

    @POST
    public Response insert(BillProspectService obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            if (new BillProspect().select(obj.prospectId, conn).converted) {
                throw new Exception("El prospecto ya fue convertido");
            }
            calc(obj, conn);
            obj.insert(conn);
            useDefault(conn);
            SysCrudLog.created(this, obj, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(BillProspectService obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            if (new BillProspect().select(obj.prospectId, conn).converted) {
                throw new Exception("El prospecto ya fue convertido");
            }
            calc(obj, conn);
            BillProspectService old = new BillProspectService().select(obj.id, conn);

            if (old.taxRate.compareTo(obj.taxRate) != 0) {
                if (new MySQLQuery("SELECT COUNT(*)>0 FROM bill_prospect_payment WHERE service_id = ?1").setParam(1, obj.id).getAsBoolean(conn)) {
                    throw new Exception("No se puede cambiar el IVA cuando ya se han hecho abonos");
                }
            }
            obj.update(conn);
            useDefault(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillProspectService obj = new BillProspectService().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillProspectService s = new BillProspectService().select(id, conn);
            if (new BillProspect().select(s.prospectId, conn).converted) {
                throw new Exception("El prospecto ya fue convertido");
            }
            BillProspectService.delete(id, conn);
            useDefault(conn);
            SysCrudLog.deleted(this, BillProspectService.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/grid")
    public Response getGrid(GridRequest r) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            GridResult tbl = new GridResult();
            tbl.data = new MySQLQuery("SELECT  "
                    + "s.id, "
                    + "t.`name`, "
                    + "s.net_val, "
                    + "s.tax, "
                    + "s.net_val + s.tax, "
                    + "(SELECT SUM(total) FROM bill_prospect_payment WHERE service_id = s.id), "
                    + "s.payments, "
                    + "s.finan_rate "
                    + "FROM  "
                    + "bill_prospect_service s "
                    + "INNER JOIN bill_service_type t ON t.id = s.type_id "
                    + "WHERE s.prospect_id = ?1").setParam(1, r.ints.get(0)).getRecords(conn);
            tbl.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 220, "Tipo"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 180, "Valor Neto"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 180, "IVA"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 180, "Total"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 180, "Abonos"),
                new MySQLCol(MySQLCol.TYPE_INTEGER, 130, "Cuotas"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 130, "% Interés")
            };

            return createResponse(tbl);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
