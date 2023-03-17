package api.sys.api;

import api.BaseAPI;
import java.sql.Connection;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.GregorianCalendar;
import utilities.MySQLQuery;

@Path("/sysMinasTest")
public class SysMinasTestApi extends BaseAPI {

    @GET
    @Path("/getStatus")
    public Response getStatus() {
        try (Connection conn = getConnection()) {
            GregorianCalendar gc = new GregorianCalendar();
            gc.add(GregorianCalendar.MINUTE, -15);

            Integer total = new MySQLQuery("SELECT COUNT(*) FROM sys_minas_test t WHERE t.dt > ?1").setParam(1, gc.getTime()).getAsInteger(conn);
            Integer error = new MySQLQuery("SELECT COUNT(*) FROM sys_minas_test t WHERE t.dt > ?1 AND t IS NULL").setParam(1, gc.getTime()).getAsInteger(conn);

            if (total == null || error == null) {
                return createResponse("Sin información");
            }

            double ratio = (((double) error) / total);
            Integer avgTime = new MySQLQuery("SELECT avg(t) FROM sys_minas_test t WHERE t.dt > ?1 AND t IS NOT NULL").setParam(1, gc.getTime()).getAsInteger(conn);

            if (avgTime == null) {
                return createResponse("Sin información");
            }
            
            
            boolean avgOK = avgTime < 500;

            if (ratio == 0) {
                if (avgOK) {
                    return createResponse("Operando Normalmente");
                } else {
                    return createResponse("Operando Parcialmente");
                }
            } else if (ratio <= 40) {
                return createResponse("Operando Parcialmente");
            } else if (ratio <= 80) {
                return createResponse("Con Fallas Graves");
            } else {
                return createResponse("¡Fuera de Servicio!");
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
