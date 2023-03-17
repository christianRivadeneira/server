package web.billing.readings;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import web.billing.BillingServlet;

@WebServlet(name = "/readings/listBuildingsV5", urlPatterns = {"/readings/listBuildingsV5"})
public class listBuildingsV5 extends BillingServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String instanceId = request.getParameter("cityId");        

        try (Connection gralConn = getConnection(); Connection cityConn = getConnection(Integer.valueOf(instanceId))) {
            SessionLogin.validate(request.getParameter("sessionId"), gralConn, null);
            int spanId = new MySQLQuery("SELECT id FROM bill_span WHERE state = 'cons'").getAsInteger(cityConn);
            Object[][] buildData = new MySQLQuery("SELECT "
                    + "CAST(b.id AS CHAR), "//0
                    + "CONCAT(IF(LENGTH(CAST(b.old_id AS CHAR) < 3), LPAD(CAST(b.old_id AS CHAR), 3, '0'), CAST(b.old_id AS CHAR)) , ' - ' , b.`name`), "//1
                    + "b.address, "//2
                    + "c.lat, "//3
                    + "c.lon, "//4
                    + "IF(c.checked_coords=1,c.checked_coords,NULL), "//5
                    + "IFNULL(c.expected_users, 0), "//6
                    + "IFNULL(c.created_users, 0) "//7
                    + "FROM bill_building AS b "
                    + "LEFT JOIN sigma.ord_tank_client c ON c.mirror_id=b.id AND c.`type`='build' AND c.bill_instance_id=" + instanceId + " "
                    + "ORDER BY b.old_id ASC").getRecords(cityConn);

            //gralem
            MySQLQuery tanksQ = new MySQLQuery("SELECT GROUP_CONCAT(CONCAT(CAST(t.id AS CHAR),'@',t.serial,'@', IFNULL(t.id_code, '')) ORDER BY t.serial ASC SEPARATOR ',') FROM ord_tank_client AS c INNER JOIN est_tank AS t ON t.client_id = c.id WHERE c.mirror_id = ?1 AND c.bill_instance_id = " + instanceId + " GROUP BY c.id");
            MySQLQuery readsQ = new MySQLQuery("SELECT GROUP_CONCAT(CAST(r.percent AS CHAR)) FROM ord_tank_client AS c INNER JOIN est_tank AS t ON t.client_id = c.id LEFT JOIN est_tank_read AS r ON r.tank_id = t.id AND r.bill_span_id = " + spanId + " WHERE percent IS NOT NULL AND c.mirror_id = ?1 AND c.bill_instance_id = " + instanceId + " ORDER BY t.serial");
            MySQLQuery datesQ = new MySQLQuery("SELECT MAX(r.read_date) FROM ord_tank_client AS c INNER JOIN est_tank AS t ON t.client_id = c.id LEFT JOIN est_tank_read AS r ON r.tank_id = t.id AND r.bill_span_id = " + spanId + " WHERE c.mirror_id = ?1 AND c.bill_instance_id = " + instanceId);

            //radio permitido
            Double ratio = new MySQLQuery("SELECT building_radius FROM sys_cfg").getAsDouble(gralConn);

            //escribiendo la respuesta
            response.setContentType("application/octet-stream");

            try (GZIPOutputStream goz = new GZIPOutputStream(response.getOutputStream()); OutputStreamWriter osw = new OutputStreamWriter(goz, "UTF8"); PrintWriter w = new PrintWriter(osw, true)) {
                w.write(String.valueOf(buildData.length));
                w.write(9);
                w.write(String.valueOf(spanId));
                w.write(9);
                w.write(String.valueOf(ratio));
                SimpleDateFormat sf = new SimpleDateFormat("ddMMyy");
                for (Object[] row : buildData) {
                    tanksQ.setParam(1, row[0]);
                    readsQ.setParam(1, row[0]);
                    datesQ.setParam(1, row[0]);
                    Date readDate = datesQ.getAsDate(gralConn);
                    String tankInfo = tanksQ.getAsString(gralConn);
                    String tkReads = readsQ.getAsString(gralConn);

                    //inicio fila
                    w.write(13);
                    w.write(10);
                    w.write(row[0].toString());//id 0
                    w.write(9);
                    w.write(row[1].toString());// código + nombre ed 1
                    w.write(9);
                    w.write(row[2].toString());// dirección 2
                    w.write(9);
                    w.write(row[3] != null ? row[3].toString() : "0");//latitud 3
                    w.write(9);
                    w.write(row[4] != null ? row[4].toString() : "0");//longitud 4
                    w.write(9);
                    w.write(row[5] != null ? row[5].toString() : "");//coordenadas actualizadas
                    w.write(9);
                    w.write(tankInfo != null ? tankInfo : ""); //tanques 5
                    w.write(9);
                    w.write(tkReads != null ? tkReads : ""); //lecturas de tanques 6
                    w.write(9);
                    w.write(readDate != null ? sf.format(readDate) : " ");// fecha ultima lectura tanques 7, se envía un espacio en blanco para que todas las filas queden iguales
                    w.write(9);
                    w.write(row[6].toString());//8
                    w.write(9);
                    w.write(row[7].toString());//9
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(listBuildingsV5.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(500, ex.getMessage());
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
        return "";
    }
}
