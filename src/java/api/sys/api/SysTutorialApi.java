package api.sys.api;

import api.BaseAPI;
import api.sys.model.SysTutorial;
import java.io.File;
import java.sql.Connection;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import utilities.MySQLQuery;

@Path("/SysTutorialApi")
public class SysTutorialApi extends BaseAPI {

    @GET
    public Response getData(@QueryParam("pack") String pack) {
        try (Connection conn = getConnection()) {
            return createResponse(SysTutorial.getList(new MySQLQuery("SELECT " + SysTutorial.getSelFlds("s") + " FROM sys_tutorial s INNER JOIN system_app a ON a.id = s.app_id WHERE a.package_name = '" + pack + "'"), conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/findTutorial")
    public Response findTutorial(@QueryParam("name") String name, @QueryParam("type") String type) {
        try (Connection conn = getConnection()) {

            File tmp;

            String baseDir = new MySQLQuery("SELECT tutos_base_dir FROM sys_cfg LIMIT 1").getAsString(conn);
            if (!baseDir.endsWith(File.separator)) {
                baseDir = (baseDir + File.separator);
            }
            tmp = new File(baseDir + name + "." + type);

            return createResponse(tmp, tmp.getName());
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
