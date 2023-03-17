package api.per.api;

import api.BaseAPI;
import api.per.model.PerContract;
import java.sql.Connection;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import utilities.MySQLQuery;
import web.ShortException;

@Path("/perContract")
public class PerContractApi extends BaseAPI {

    @GET
    public Response getContractByEmployeeId(@QueryParam("empId") int empId){
        try(Connection con = getConnection()) {
            String query = "SELECT " +
                PerContract.getSelFlds("pc") +
                "FROM per_contract pc " +
                "WHERE pc.emp_id = ?1 " +
                "AND pc.last = 1 " +
                "AND pc.active = 1 " +
                "AND pc.leave_date IS NULL ";

            MySQLQuery q = new MySQLQuery(query).setParam(1, empId);
            PerContract contract = new PerContract().select(q, con);

            if(contract == null) 
                throw new ShortException("El empleado no tiene contratos vigentes");

            return createResponse(contract);
        } catch (Exception e) {
            return createResponse(e);
        }
    }
}
