package api.sys.api;

import api.BaseAPI;
import api.sys.dto.SysLocationRequest;
import java.sql.Connection;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import utilities.MySQLQuery;

@Path("/sysLocation")
public class SysLocationApi extends BaseAPI {

    @GET
    public Response get(@QueryParam("id") Integer id, @QueryParam("tableName") String tableName) {
        try (Connection conn = getConnection()) {
            //getSession(conn);

            if (MySQLQuery.isEmpty(tableName)) {
                throw new Exception("No se encontro información de la tabla");
            }
            if (id == null) {
                throw new Exception("No se encontro información del id del registro");
            }

            MySQLQuery mq = new MySQLQuery("SELECT lat, lon FROM " + tableName + " WHERE id = ?1");
            mq.setParam(1, id);
            Object[] data = mq.getRecord(conn);

            SysLocationRequest obj = new SysLocationRequest();
            obj.id = id;
            obj.tableName = tableName;
            if (data != null) {
                obj.lat = MySQLQuery.getAsBigDecimal(data[0], false);
                obj.lon = MySQLQuery.getAsBigDecimal(data[1], false);
            }

            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(SysLocationRequest obj) {
        try (Connection conn = getConnection()) {
            //getSession(conn);
            MySQLQuery mq = new MySQLQuery("UPDATE " + obj.tableName + " SET lat = ?1 , lon = ?2 WHERE id = ?3");
            mq.setParam(1, obj.lat);
            mq.setParam(2, obj.lon);
            mq.setParam(3, obj.id);
            mq.executeUpdate(conn);
            return Response.ok().build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
