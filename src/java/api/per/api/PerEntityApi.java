package api.per.api;

import api.BaseAPI;
import api.per.model.PerEntity;
import java.sql.Connection;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import utilities.MySQLQuery;

@Path("/perEntity")
public class PerEntityApi extends BaseAPI {

    @GET	
    public Response filterByType(@QueryParam("type") String type){
        try (Connection con = getConnection()){
            MySQLQuery q = new MySQLQuery("SELECT " +
                    PerEntity.getSelFlds("pe") +
                    "FROM per_entity pe " +
                            "WHERE pe.`type` = ?1 " +
                            "ORDER BY pe.NAME;")
                    .setParam(1, type);


            List<PerEntity> perEntityList =PerEntity.getList(q, con);
            return createResponse(perEntityList);
        } catch(Exception ex){
            return createResponse(ex);
        }
    }
}
