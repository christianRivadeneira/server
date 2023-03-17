package web.tanks;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import web.fileManager;
import static web.fileManager.copy;

@MultipartConfig
@WebServlet(name = "EvidenceSync", urlPatterns = {"/EvidenceSync"})
public class EvidenceSync extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            Map<String, String> pars = MySQLQuery.scapedParams(request);

            String poolName = MySQLQuery.getAsString(pars.get("poolName"));
            String tz = MySQLQuery.getAsString(pars.get("tz"));
            int employeeId = MySQLQuery.getAsInteger(pars.get("employee"));
            int fileCount = MySQLQuery.getAsInteger(pars.get("fileCount"));
            Connection con = MySQLCommon.getConnection(poolName, tz);

            fileManager.PathInfo pInfo = new fileManager.PathInfo(con);
            JsonArrayBuilder jar = Json.createArrayBuilder();
            for (int i = 0; i < fileCount; i++) {
                int ownerId = MySQLQuery.getAsInteger(pars.get("ownerId" + i));
                int ownerType = MySQLQuery.getAsInteger(pars.get("ownerType" + i));
                String desc = MySQLQuery.getAsString(pars.get("desc" + i));
                String fileName = MySQLQuery.getAsString(pars.get("fileName" + i));
                int idIn = MySQLQuery.getAsInteger(pars.get("id" + i));
                String idSig = MySQLQuery.getAsString(pars.get("sigmaId" + i));
                Integer sigmaId = (idSig.isEmpty() ? -1 : MySQLQuery.getAsInteger(idSig));
                JsonArrayBuilder idsJar = Json.createArrayBuilder();
                idsJar.add(idIn);

                if (sigmaId > 0) {
                    new MySQLQuery("UPDATE bfile SET "
                            + "file_name = '" + fileName + "' "
                            + ",description = '" + desc + "' "
                            + ",owner_id = " + ownerId + " "
                            + ",owner_type = " + ownerType + " "
                            + ",created_by = " + employeeId + " "
                            + ",updated_by = " + employeeId + " "
                            + ",created = NOW() "
                            + ",updated = NOW() "
                            + ",keywords = ''"
                            + "WHERE id = " + sigmaId).executeUpdate(con);
                    idsJar.add(sigmaId);
                } else {
                    int id = new MySQLQuery("INSERT INTO bfile SET "
                            + "file_name = '" + fileName + "' "
                            + ",description = '" + desc + "' "
                            + ",owner_id = " + ownerId + " "
                            + ",owner_type = " + ownerType + " "
                            + ",created_by = " + employeeId + " "
                            + ",updated_by = " + employeeId + " "
                            + ",created = NOW() "
                            + ",updated = NOW() "
                            + ",keywords = ''").executeInsert(con);
                    File file = pInfo.getNewFile(id);
                    copy(request.getPart("file" + i).getInputStream(), new BufferedOutputStream(new FileOutputStream(file)));
                    new MySQLQuery("UPDATE bfile SET size = " + file.length() + " WHERE id = " + id).executeUpdate(con);
                    idsJar.add(id);
                }
                jar.add(idsJar);
            }
            w.writeArray(jar.build());
        } catch (IOException ex) {
            Logger.getLogger(EvidenceSync.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(EvidenceSync.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }

}
