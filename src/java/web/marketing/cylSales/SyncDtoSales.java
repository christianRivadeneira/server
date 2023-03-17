package web.marketing.cylSales;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "syncDtoSale", urlPatterns = {"/syncDtoSale"})
public class SyncDtoSales extends HttpServlet {

    /**
     * Se hace por JDBC por optimización, no usar MySQLQuery, en este caso gasta
     * mucha memoria
     *
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        long l = System.currentTimeMillis();
        Map<String, String> req = MySQLQuery.scapedParams(request);
        int limit = req.containsKey("limit") ? Integer.valueOf(req.get("limit")) : 1000;
        response.setContentType("text/plain;charset=ISO-8859-1");
        PrintWriter out = response.getWriter();

        try (Connection con = MySQLCommon.getConnection("sigmads", null)) {
            int[] updated = syncDtoTrk(limit, con);
            for (int i = 0; i < updated.length; i++) {
                out.write(updated[i] + "");
            }
            out.write(System.lineSeparator());
            out.write("fin bloque de actualizacion " + (System.currentTimeMillis() - l) + "ms");
        } catch (Exception ex) {
            Logger.getLogger(SyncDtoSales.class.getName()).log(Level.SEVERE, null, ex);
            try {
                response.setStatus(500);
                response.getWriter().write(ex.getMessage());
            } catch (IOException ex1) {
                response.setStatus(500);
                Logger.getLogger(SyncDtoSales.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
            }
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
        return "sincronizacion de ventas y subsidios";
    }

    public static int[] syncDtoTrk(int limit, Connection con) throws Exception {

        //se crea la vista de la dto, solo lo que necesesitamos
        String view = "CREATE OR REPLACE VIEW view_dto_sale AS SELECT "
                + "d.id, "
                + "d.dt, "
                + "d.aprov_number, "
                + "d.bill, "
                + "d.trk_sale_id "
                + "FROM dto_sale d "
                + "WHERE d.trk_sale_id IS NULL AND d.dt > '2016-02-01 00:00:00';";
        Statement st = con.createStatement();
        st.execute(view);

        String str = " SELECT d.id ,s.id , COUNT(s.id) as num "
                + " FROM view_dto_sale d "
                + " INNER JOIN trk_sale s ON d.aprov_number = s.auth "
                + " GROUP BY d.id "
                + " HAVING num = 1 "
                + " ORDER BY d.id DESC "
                + " LIMIT " + (limit);

        st = con.createStatement();
        ResultSet rs = st.executeQuery(str);
        int cols = rs.getMetaData().getColumnCount();

        List<Object> objs = new ArrayList<>();
        while (rs.next()) {
            Object obj[] = new Object[cols];
            for (int j = 0; j < cols; j++) {
                obj[j] = rs.getObject(j + 1);
            }
            objs.add(obj);
        }

        Object[][] data = objs.toArray(new Object[0][0]);

        PreparedStatement stUpdate = con.prepareStatement("UPDATE view_dto_sale SET trk_sale_id = ? WHERE id = ? ");
        for (Object[] obj : data) {
            if (obj[0] != null && obj[1] != null) {
                stUpdate.setInt(1, (int) obj[1]);
                stUpdate.setInt(2, (int) obj[0]);
                stUpdate.addBatch();
            }
        }
        int[] updates = stUpdate.executeBatch();

        new MySQLQuery("UPDATE "
                + "trk_anul_sale s "
                + "INNER JOIN dto_sale ds ON ds.id = s.dto_sale_id AND ds.trk_sale_id IS NOT NULL "
                + "SET s.trk_sale_id = ds.trk_sale_id "
                + "WHERE s.trk_sale_id IS NULL;").executeUpdate(con);

        return updates;
    }
}
