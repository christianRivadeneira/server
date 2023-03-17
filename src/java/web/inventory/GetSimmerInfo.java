package web.inventory;

import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@WebServlet(name = "GetSimmerInfo", urlPatterns = {"/GetSimmerInfo"})
public class GetSimmerInfo extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            JsonObjectBuilder ob = Json.createObjectBuilder();
            try {
                response.addHeader("Access-Control-Allow-Credentials", "true");
                response.addHeader("Access-Control-Allow-Origin", "*");
                response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");

                /*http://192.168.1.2:1717/WsAddDato/adddato?
                id_operario=123456
                &id_bascula=a6
                &NIF=123456789032
                &peso_inicial=15.3
                &peso_final=28.5
                &estado=Exito
                &sucursal=Planta_espino
                &fecha=2019-02-22_11:43:38
                &key=A460170F68324D6ABD236F5A463B72BC*/
                Map<String, String> pars = MySQLQuery.scapedParams(request);
                String emp = pars.get("id_operario").replace("_", " ").replace("%20", " ");
                String basc = pars.get("id_bascula");
                String nif = pars.get("NIF").replace(" ", "").replace("%20", "").replace("-", "");

                int status = 200;//success Montagas
                if (!nif.equals("123456789012")) {
                    String initialWeigth = pars.get("peso_inicial");
                    String finalWeigth = pars.get("peso_final");
                    String state = pars.get("estado");
                    String sucursal = pars.get("sucursal");
                    Date date = MySQLQuery.getAsDate(pars.get("fecha").replace("_", " ").replace("%20", " "));
                    GregorianCalendar gc = new GregorianCalendar();
                    gc.setTime(date);
                    if (gc.get(GregorianCalendar.YEAR) < 2100) {
                        String key = pars.get("key");

                        try (Connection conn = MySQLCommon.getConnection("sigmads", null)) {
                            String simmerKey = new MySQLQuery("SELECT simer_key FROM inv_cfg").getAsString(conn);
                            if (simmerKey == null) {
                                throw new Exception("No hay clave de Simmer registrada en Sigma");
                            } else if (!simmerKey.toUpperCase().equals(key.toUpperCase())) {
                                throw new Exception("La llave no coincide con la registrada en Sigma");
                            }

                            String nifY = nif.substring(0, 2);
                            String nifF = nif.substring(2, nif.length() - 6);
                            String nifS = nif.substring(nif.length() - 6, nif.length());

                            Integer cylId = new MySQLQuery("SELECT id FROM trk_cyl WHERE nif_y = " + nifY + " AND nif_f = " + nifF + " AND nif_s = " + nifS).getAsInteger(conn);
                            if (cylId == null) {
                                throw new Exception("El cilindro " + nif + " no se encuentra registrado en Sigma");
                            }

                            Integer empId = new MySQLQuery("SELECT id FROM employee WHERE document = '" + emp + "' ORDER BY active DESC, id DESC LIMIT 1").getAsInteger(conn);
                            if (empId == null) {
                                throw new Exception("El empleado " + emp + " no se encuentra registrado en Sigma");
                            }

                            Integer centerId = new MySQLQuery("SELECT id FROM gt_center WHERE simmer_code = '" + sucursal + "'").getAsInteger(conn);
                            if (centerId == null) {
                                throw new Exception("El centro " + sucursal + " no se encuentra registrado en Sigma");
                            }

                            new MySQLQuery("INSERT INTO trk_cyl_fill SET "
                                    + "trk_cyl_id = " + cylId + ", "
                                    + "fill_date = '" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date) + "', "
                                    + "initial_weight = " + initialWeigth + ", "
                                    + "final_weight = " + finalWeigth + ", "
                                    + "center_id = " + centerId + ", "
                                    + "emp_id = " + empId + ", "
                                    + "simmer_state = '" + state + "', "
                                    + "basc_id = '" + basc + "'").executeInsert(conn);
                        }
                    } else {
                        throw new Exception("Fecha incorrecta: " + pars.get("fecha"));
                    }
                }
                ob.add("status", "OK");
                ob.add("code", status);
                ob.add("msg", "Exito");
            } catch (Exception ex) {
                Logger.getLogger(GetSimmerInfo.class.getName()).log(Level.SEVERE, null, ex);
                ob.add("status", "ERROR");
                ob.add("code", 500);
                ob.add("msg", (ex.getMessage() != null && !ex.getMessage().isEmpty()) ? ex.getMessage() : "Error desconocido.");
            } finally {
                w.writeObject(ob.build());
            }
        } catch (Exception ex) {
            Logger.getLogger(GetSimmerInfo.class.getName()).log(Level.SEVERE, null, ex);
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
    public String getServletInfo() {
        return "Short description";
    }

}
