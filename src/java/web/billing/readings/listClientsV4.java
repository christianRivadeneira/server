package web.billing.readings;

import api.bill.model.BillInstance;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import service.MySQL.MySQLSelect;
import utilities.MySQLQuery;
import web.billing.BillingServlet;

@WebServlet(name = "/readings/listClientsV4", urlPatterns = {"/readings/listClientsV4"})
public class listClientsV4 extends BillingServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        Connection conn = null;
        int instId = Integer.valueOf(request.getParameter("cityId"));

        try (Connection cityConn = getConnection(instId)) {
            conn = MySQLCommon.getConnection("sigmads", null);
            // SessionLogin.validate(request.getParameter("sessionId"));

            int spanId = new MySQLQuery("SELECT id FROM bill_span WHERE state = 'cons'").getAsInteger(cityConn);
            boolean showApartment = new MySQLQuery("SELECT show_apartment FROM sys_cfg WHERE id = 1").getAsBoolean(conn);

            BillInstance inst = BillingServlet.getInst(instId);
            if (inst.isNetInstance()) {

            }

            Object[][] clieData = new MySQLQuery("SELECT "
                    + "CAST(cli.id AS CHAR), "//0
                    + "CONCAT( " + (showApartment ? " cli.apartment " : " cli.num_install ") + ", IFNULL(CONCAT(' (',(SELECT `number` FROM bill_meter WHERE client_id = cli.id ORDER BY start_span_id DESC LIMIT 1),')'), '') ), "//1
                    + "CAST(ROUND((last1.reading - last1.last_reading), 1) AS CHAR), "//2
                    + "CAST(ROUND((last2.reading - last2.last_reading), 1) AS CHAR), "//3
                    + "CAST(ROUND((last3.reading - last3.last_reading), 1) AS CHAR), "//4
                    + "CAST(ROUND((last4.reading - last4.last_reading), 1) AS CHAR), "//5
                    + "CAST(COALESCE(curre.last_reading, m.start_reading, last1.reading, 0) AS CHAR), "//6 anterior
                    + "CAST(curre.reading AS CHAR), "//7 actual
                    + "CONCAT(cli.first_name,' ',COALESCE(last_name,'')), "//8
                    + "IFNULL((SELECT `number` FROM bill_meter WHERE client_id = cli.id ORDER BY start_span_id DESC LIMIT 1),''), "//9
                    + "cli.building_id "//10
                    + "FROM bill_client_tank AS cli "
                    + "LEFT JOIN bill_reading AS curre ON curre.client_tank_id = cli.id AND curre.span_id = " + (spanId - 0) + " "
                    + "LEFT JOIN bill_reading AS last1 ON last1.client_tank_id = cli.id AND last1.span_id = " + (spanId - 1) + " "
                    + "LEFT JOIN bill_reading AS last2 ON last2.client_tank_id = cli.id AND last2.span_id = " + (spanId - 2) + " "
                    + "LEFT JOIN bill_reading AS last3 ON last3.client_tank_id = cli.id AND last3.span_id = " + (spanId - 3) + " "
                    + "LEFT JOIN bill_reading AS last4 ON last4.client_tank_id = cli.id AND last4.span_id = " + (spanId - 4) + " "
                    + "LEFT JOIN bill_meter AS m ON m.client_id = cli.id AND m.start_span_id = " + spanId + " "
                    + "WHERE active = 1 "
                    + "ORDER BY cli.num_install ASC").getRecords(cityConn);

            ////enviando la respuesta
            response.setContentType("application/octet-stream");
            try (GZIPOutputStream goz = new GZIPOutputStream(response.getOutputStream()); OutputStreamWriter osw = new OutputStreamWriter(goz, "UTF8"); PrintWriter w = new PrintWriter(osw, true)) {
                w.write("0");
                //se comenta para obligar a que se actualicen a la versión con api
                /* w.write(String.valueOf(clieData.length));

                for (Object[] clieRow : clieData) {
                    w.write(13);
                    w.write(10);
                    //id y número de instalación
                    w.write(clieRow[0].toString());//id 0
                    w.write(9);
                    w.write(clieRow[1].toString());// apto 1
                    w.write(9);
                    w.write("0");// mora 2
                    w.write(9);
                    w.write(clieRow[2] != null ? clieRow[2].toString() : ""); // cons 1 - 3
                    w.write(9);
                    w.write(clieRow[3] != null ? clieRow[3].toString() : "");// cons 2 - 4
                    w.write(9);
                    w.write(clieRow[4] != null ? clieRow[4].toString() : "");// cons 3 - 5
                    w.write(9);
                    w.write(clieRow[5] != null ? clieRow[5].toString() : "");// cons 4 - 6
                    w.write(9);
                    w.write(clieRow[6] != null ? clieRow[6].toString() : "");// lec ant - 7
                    w.write(9);
                    w.write(clieRow[7] != null ? clieRow[7].toString() : "");// lec act - 8
                    w.write(9);
                    w.write(clieRow[8] != null ? clieRow[8].toString() : "");// nombre_clie - 9
                    w.write(9);
                    w.write(clieRow[9] != null ? clieRow[9].toString() : "");//10
                    w.write(9);
                    w.write(clieRow[10].toString());//11
                }*/
            }
        } catch (Exception ex) {
            Logger.getLogger(listClientsV4.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(500, ex.getMessage());
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
        return "";
    }
}
