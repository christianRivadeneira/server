package service.MySQL;

import controller.system.LoginController;
import java.io.IOException;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.menu.Credential;
import utilities.JsonUtils;
import utilities.MySQLQuery;

//@MultipartConfig
@WebServlet(name = "MySQLLoginWeb", urlPatterns = {"/MySQLLoginWeb"})
public class MySQLWebLogin extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println(request);
        Connection conn = null;
        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            JsonObjectBuilder ob = Json.createObjectBuilder();
            conn = MySQLCommon.getConnection("sigmads", null);
            JsonObject req = MySQLQuery.scapeJsonObjStream(request);
            String header = req.getString("header");
            try {
                switch (header) {
                    case "login": {
                        String user = req.getString("user");
                        String pass = req.getString("pass");
                        String phone = req.getString("phone");
                        String type = req.getString("type");
                        String pack = req.getString("pack");
                        String poolName = req.getString("poolName");
                        
                        if(user.isEmpty() || pass.isEmpty() || pack.isEmpty()){
                            response.setStatus(400);
                            return;
                        }

                        Credential cred = LoginController.getByCredentials(getServletContext(),
                                user, pass, type,
                                "extras", phone, pack,
                                request, true, null,
                                poolName, "GMT-5", true);
                        cred.toJson(ob);
                        break;
                    }
                    case "logout": {
                        String sessionId = req.getString("sessionId");
                        String poolName = req.getString("poolName");
                        String tz = req.getString("tz");
                        LoginController.closeSession(sessionId, poolName, tz);
                        JsonUtils.addString(ob, "status", "OK");
                        break;
                    }
                    default:
                        throw new AssertionError();
                }

            } catch (Exception ex) {
                Logger.getLogger(MySQLWebLogin.class.getName()).log(Level.SEVERE, null, ex);
                String m = ex.getMessage();
                ob.add("result", "ERROR, " + m);
                if (m != null && !m.isEmpty()) {
                    ob.add("errorMsg", m);
                    response.setStatus(401);
                } else {
                    ob.add("errorMsg", "Error desconocido.");
                    response.setStatus(500);
                }
            } finally {
                w.writeObject(ob.build());
                MySQLCommon.closeConnection(conn);
            }
        } catch (Exception ex) {
            Logger.getLogger(MySQLWebLogin.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            MySQLSelect.tryClose(conn);
        }

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doDelete(req, resp); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPut(req, resp); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getServletInfo() {
        return "Login en web y mobil web";
    }

}
