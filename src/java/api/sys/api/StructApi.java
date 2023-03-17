package api.sys.api;

import api.BaseAPI;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.DBSettings;
import utilities.MySQLQuery;
import web.fileManager;

@Path("/struct")
public class StructApi extends BaseAPI {

    @POST
    public Response getStruct(@QueryParam("db") String dbName) throws Exception {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            if (sl.employeeId != 1) {
                throw new Exception("Unauthorized");
            }

            if (dbName == null) {
                dbName = new MySQLQuery("SELECT GROUP_CONCAT(SCHEMA_NAME SEPARATOR ' ') FROM information_schema.SCHEMATA WHERE SCHEMA_NAME NOT IN ('mysql','information_schema','performance_schema','sys');").getAsString(conn);
            }

            final DBSettings db = new DBSettings(conn);

            Process exec = Runtime.getRuntime().exec(
                    "mysqldump --no-autocommit --disable-keys --host=" + db.host + " --port=" + db.port + " "
                    + " --user=" + db.user + " --password=" + db.pass + " "
                    + " --skip-comments --no-data --databases " + dbName);
            File f = File.createTempFile("struct", ".sql");
            try (FileOutputStream fos = new FileOutputStream(f)) {
                fileManager.copy(exec.getInputStream(), fos);
            }

            exec.waitFor();
            if (exec.exitValue() != 0) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                fileManager.copy(exec.getErrorStream(), baos);
                byte[] bytes = baos.toByteArray();
                if (bytes.length > 0) {
                    throw new Exception(new String(bytes));
                } else {
                    throw new Exception("Error al generar el backup");
                }
            }
            return createResponse(f, "dump.sql", true);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
