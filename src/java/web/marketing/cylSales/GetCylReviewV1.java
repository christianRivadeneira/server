package web.marketing.cylSales;

import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import service.MySQL.MySQLSelect;
import utilities.MySQLQuery;

/*
**************************
Servlet que se llama desde el app de ventas y operaciones, tener en cuenta para 
los cambios. 
**************************
 */
@MultipartConfig
@WebServlet(name = "GetCylReviewV1", urlPatterns = {"/GetCylReviewV1"})
public class GetCylReviewV1 extends HttpServlet {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;

        response.addHeader("Access-Control-Allow-Credentials", "true");
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");

        Map<String, String> req = MySQLQuery.scapedParams(request);
        String nif = req.get("nif");
        Integer empId = req.containsKey("empId") ? Integer.valueOf(req.get("empId")) : null;
        /**
         * Cuando es llamado desde la app de ventas no tiene empId, cuando se
         * llama desde traking si lo trae. Parámetro session se envía desde app
         * de ventas, para asignarle el responsable al cilindro.
         */
        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            conn = MySQLCommon.getConnection("sigmads", null);

            try {
                SessionLogin sl = null;
                if (req.containsKey("sessionId")) {
                    sl = SessionLogin.validate(req.get("sessionId"));
                }
                JsonObjectBuilder ob = Json.createObjectBuilder();
                if (nif.length() >= 9 && nif.matches("[0-9]+")) {
                    Integer year = Integer.valueOf(nif.substring(0, 2));
                    Integer factory = Integer.valueOf(nif.substring(2, nif.length() - 6));
                    Integer serial = Integer.valueOf(nif.substring(nif.length() - 6, nif.length()));
                    Integer cylId = new MySQLQuery("SELECT id FROM trk_cyl WHERE nif_y = " + year + " AND nif_f = " + factory + " AND nif_s = " + serial).getAsInteger(conn);
                    boolean hasTara = new MySQLQuery("SELECT has_tara FROM inv_cfg").getAsBoolean(conn);
                    if (cylId != null) {
                        if (new MySQLQuery("SELECT lock_cyl_sale FROM com_cfg WHERE id = 1").getAsBoolean(conn)) {
                            if (sl != null && new MySQLQuery("SELECT sa.package_name = 'com.glp.subsidiosonline' FROM system_app sa WHERE sa.id = " + sl.appId).getAsBoolean(conn)) {
                                Object[] row = new MySQLQuery("SELECT salable, resp_id FROM trk_cyl WHERE id = " + cylId).getRecord(conn);
                                Boolean salable = MySQLQuery.getAsBoolean(row[0]);
                                Integer respId = MySQLQuery.getAsInteger(row[1]);
                                if (!salable) {
                                    if (respId != null && sl.employeeId != respId) {
                                        Object[] respInf = new MySQLQuery("SELECT "
                                                + "CASE "
                                                + "WHEN drv.id IS NOT NULL THEN 'Vendedor' "
                                                + "WHEN dis.id IS NOT NULL THEN 'Distribuidor' "
                                                + "WHEN sto.id IS NOT NULL THEN 'Almacén' "
                                                + "WHEN ctr.id IS NOT NULL THEN 'Contratista' "
                                                + "END, "
                                                + "CONCAT(e.first_name, ' ', e.last_name), "
                                                + "dc.name "
                                                + "FROM employee e "
                                                + "LEFT JOIN dto_salesman drv ON drv.driver_id = e.id AND drv.active "
                                                + "LEFT JOIN dto_salesman dis ON dis.distributor_id = e.id AND dis.active "
                                                + "LEFT JOIN dto_salesman sto ON sto.store_id = e.store_id AND sto.active "
                                                + "LEFT JOIN dto_salesman ctr ON ctr.contractor_id = e.contractor_id AND ctr.active "
                                                + "INNER JOIN dto_center dc ON dc.id = COALESCE(drv.center_id, dis.center_id, sto.center_id, ctr.center_id) "
                                                + "WHERE e.id = " + respId).getRecord(conn);
                                        ob.add("errMsg", "El cilindro " + nif + " está reservado por:\n" + respInf[0] + ": " + respInf[1] + "\n" + respInf[2]);
                                        w.writeObject(ob.build());
                                        return;
                                    } else if (respId == null) {
                                        ob.add("errMsg", "El cilindro " + nif + " ya fue vendido");
                                        w.writeObject(ob.build());
                                        return;
                                    }
                                }
                            } else if (sl == null || new MySQLQuery("SELECT sa.package_name = 'com.qualisys.tracking' FROM system_app sa WHERE sa.id = " + sl.appId).getAsBoolean(conn)) {
                                new MySQLQuery("UPDATE trk_cyl SET salable = 1, resp_id = NULL WHERE id = " + cylId).executeUpdate(conn);
                                new MySQLQuery("DELETE FROM trk_no_rot_cyls WHERE cyl_id = " + cylId).executeDelete(conn);
                            }
                        }

                        Object[] row = new MySQLQuery("SELECT "
                                + "@dt := (SELECT DATE(date) FROM trk_sale WHERE !training AND cylinder_id = c.id AND sale_type = 'sub' ORDER BY id DESC LIMIT 1) as fecha, "
                                + "DATEDIFF(CURDATE(), @dt), "
                                + "ct.name, "
                                + "IF(ct.id = 7, 'N/A', IF ((SELECT COUNT(*) > 0 FROM dto_minas_cyl WHERE y = c.nif_y AND f = c.nif_f AND s = nif_s), 'Si', 'No')), "
                                + "c.last_verify, "
                                + "DATEDIFF(CURDATE(), c.last_verify), "
                                + "c.id, "
                                + "c.cyl_type_id "
                                + (hasTara ? ", IFNULL(c.tara, 0) " : "")//8
                                + "FROM trk_cyl c "
                                + "INNER JOIN cylinder_type ct ON c.cyl_type_id = ct.id "
                                + "WHERE c.id = " + cylId).getRecord(conn);

                        boolean wanted = new MySQLQuery("SELECT COUNT(*) > 0 FROM trk_cyl_wanted WHERE cyl_id = " + cylId + " AND aprov_dt IS NULL").getAsBoolean(conn);
                        if (empId != null) {
                            new MySQLQuery("UPDATE trk_cyl_wanted SET find_dt = NOW(), find_id = " + empId + " WHERE cyl_id = " + cylId + " AND aprov_dt IS NULL").executeUpdate(conn);
                        }
                        Date mtoDate = new MySQLQuery("SELECT `date` FROM trk_mto WHERE trk_cyl_id = " + cylId + " ORDER BY `date` DESC LIMIT 1").getAsDate(conn);
                        //boolean editDate = (mtoDate == null) && (year == 9 || year == 10);
                        boolean editDate = true;
                        Date chkDate = new MySQLQuery("SELECT DATE(dt) FROM trk_check WHERE trk_cyl_id = " + cylId + " AND DATE(dt) < CURDATE() ORDER BY dt DESC LIMIT 1").getAsDate(conn);

                        if (mtoDate == null) {
                            mtoDate = new MySQLQuery("SELECT fab_date FROM trk_cyl WHERE id = " + cylId).getAsDate(conn);
                        }
                        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        LocalDate dateM = LocalDate.parse(SDF.format(mtoDate), fmt);
                        LocalDate now = LocalDate.now();

                        if ("No".equals(MySQLQuery.getAsString(row[3]))) {
                            ob.add("errMsg", "El nif " + nif + " no se ha reportado al ministerio de minas");
                        }

                        Period period = Period.between(dateM, now);

                        ob.add("saleDate", (row[0] != null ? SDF.format(MySQLQuery.getAsDate(row[0])) : "Sin información"));
                        ob.add("days", (row[1] != null ? MySQLQuery.getAsString(row[1]) : ""));
                        ob.add("cylCap", MySQLQuery.getAsString(row[2]));
                        ob.add("minasRep", MySQLQuery.getAsString(row[3]));
                        ob.add("verifyDate", (row[4] != null ? SDF.format(MySQLQuery.getAsDate(row[4])) : "Sin información"));
                        ob.add("diff", (row[5] != null ? MySQLQuery.getAsInteger(row[5]) : 0));
                        ob.add("cylId", MySQLQuery.getAsInteger(row[6]));
                        ob.add("typeId", MySQLQuery.getAsInteger(row[7]));
                        if (hasTara) {
                            ob.add("tara", MySQLQuery.getAsDouble(row[8]).toString());
                        }
                        ob.add("wanted", wanted);
                        ob.add("dateMto", SDF.format(mtoDate));
                        ob.add("warning", period.getYears());
                        ob.add("editDate", editDate);
                        ob.add("hasChkDt", chkDate != null);
                        if (chkDate != null) {
                            ob.add("lastChkDt", SDF.format(MySQLQuery.getAsDate(chkDate)));
                        }
                    } else {
                        ob.add("errMsg", "El cilindro " + nif + " no está registrado en " + new MySQLQuery("SELECT `name` FROM enterprise WHERE !alternative").getAsString(conn) + ".");
                    }
                } else {
                    ob.add("errMsg", "La etiqueta no pertenece a " + new MySQLQuery("SELECT `name` FROM enterprise WHERE !alternative").getAsString(conn) + ".");
                }
                w.writeObject(ob.build());
            } catch (Exception ex) {
                throw new Exception(ex.getMessage());
            }
        } catch (Exception ex) {
            Logger.getLogger(GetCylReviewV1.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            MySQLSelect.tryClose(conn);
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
        return "Información post-plataformas";
    }
}
