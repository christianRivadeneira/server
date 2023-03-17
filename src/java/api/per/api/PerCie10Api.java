package api.per.api;

import api.BaseAPI;
import api.per.model.PerCie10;
import api.sys.model.Employee;
import java.sql.Connection;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import utilities.MySQLQuery;

@Path("/perCie10")
public class PerCie10Api extends BaseAPI {

    @GET
    public Response getAll() {
        try(Connection con = getConnection()) {
            MySQLQuery q = new MySQLQuery("SELECT " +
                PerCie10.getSelFlds("pc") +
                "FROM per_cie10 pc " +
                "ORDER BY pc.cod");

            List<PerCie10> perCie10List = PerCie10.getList(q, con);

            return createResponse(perCie10List);
        } catch(Exception ex){
            ex.printStackTrace();
            return createResponse(ex);
        }
    }
}
