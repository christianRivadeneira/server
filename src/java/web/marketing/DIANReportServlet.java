package web.marketing;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import utilities.Strings;

@WebServlet(name = "DIANReportServlet", urlPatterns = {"/DIANReportServlet"})
public class DIANReportServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        Map<String, String> pars = MySQLQuery.scapedParams(request);
        
        
        try (Connection con = MySQLCommon.getConnection("sigmads", null); Statement st = con.createStatement(); PrintWriter out = response.getWriter();) {
            String year = pars.get("year");
            String sessionId = pars.get("sessionId");
            SessionLogin.validate(sessionId, con);
            ResultSet rs = st.executeQuery("SELECT "
                    + "GROUP_CONCAT(DISTINCT c.name) "
                    + "FROM "
                    + "contract AS ct "
                    + "INNER JOIN neigh AS n ON ct.neigh_id = n.id "
                    + "INNER JOIN sector AS s ON n.sector_id = s.id "
                    + "INNER JOIN city AS c ON c.id = s.city_id "
                    + "INNER JOIN zone AS z ON c.zone_id = z.id "
                    + "WHERE "
                    + "(c.dane_code IS NULL OR c.dane_code = '') AND "
                    + "ct.sign_date BETWEEN '" + year + "-01-01' AND '" + year + "-12-31' "
                    + "ORDER BY c.`name` asc");

            if (rs.next()) {
                String cities = rs.getString(1);
                if (cities != null && !cities.trim().equals("")) {
                    out.write("Las siguientes ciudades no tiene código DANE:\n");
                    out.println(cities);
                }
            }

            String q = "SELECT "
                    + "IF(c.cli_type = 'nat', 'NIT', 'CC'), "//0
                    + "CAST(document AS CHAR), "//1
                    + "cli_type, "//2
                    + "last_name, "//3
                    + "first_name, "//4
                    + "est_name, "//5
                    + "CONCAT(address,' ', ne.name), "//6
                    + "ct.dane_code, "//7
                    + "ct.name, "//8
                    + "SUM(deposit) "//9
                    + "FROM contract AS c "
                    + "INNER JOIN neigh AS ne ON c.neigh_id = ne.id "
                    + "INNER JOIN sector AS sc ON ne.sector_id = sc.id "
                    + "INNER JOIN city AS ct ON ct.id = sc.city_id "
                    + "WHERE c.sign_date BETWEEN '" + year + "-01-01' AND '" + year + "-12-31' AND c.anull_cause_id IS NULL "
                    + "GROUP BY document ORDER BY document ASC ";

            rs = st.executeQuery(q);
            response.setHeader("Content-Disposition", "attachment;filename=DIAN.csv");
            response.setContentType("text/csv;charset=ISO-8859-1");

            out.write("TIPO DOCUMENTO;NUMERO IDENTIFICACION;D.V.;PRIMER APELLIDO;SEGUNDO APELLIDO;PRIMER NOMBRE;OTROS NOMBRES;RAZON SOCIAL;DIRECCION;DEPARTAMENTO;CÓD_MUNICIPIO;NOM_MUNICIPIO;VALOR DEPOSITO;VALOR RETEFUENTE;VALOR TOTAL");
            out.println();
            while (rs.next()) {
                writeVal(rs, 1, out);//td
                writeVal(rs, 2, out);//id
                if (!rs.getString(3).equals("nat")) {
                    out.write(getCheckDigit(rs.getString(2)) + ";");//tipo cliente
                } else {
                    out.write(";");//tipo cliente
                }
                writeName(rs, 4, out);//apes
                writeName(rs, 5, out);//nombres
                writeVal(rs, 6, out);//esta_name
                writeVal(rs, 7, out);//dirección
                String dane = rs.getString(8);
                if (dane != null) {
                    out.write(dane.substring(0, 2));
                    out.write(";");
                    out.write(dane.substring(0, 5));
                    out.write(";");
                } else {
                    out.write(";;");
                }
                writeVal(rs, 9, out);//ciudad
                BigDecimal val = rs.getBigDecimal(10);
                if (val != null) {
                    System.out.println(val);
                    out.write(val.multiply(new BigDecimal("0.99")).toString().replaceAll("[\\.]", ","));
                    out.write(";");
                    out.write(val.multiply(new BigDecimal("0.01")).toString().replaceAll("[\\.]", ","));
                    out.write(";");
                    out.write(val.multiply(new BigDecimal(1d)).toString().replaceAll("[\\.]", ","));
                    out.write(";");
                } else {
                    out.write(";;;");
                }
                out.println();
            }
        } catch (Exception ex) {
            response.sendError(500, (ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName()));
        }
    }

    public static int getCheckDigit(String nit) {
        int x, y, z, i, dv1;
        int[] vpri = new int[16];

        x = 0;
        z = nit.length();
        vpri[1] = 3;
        vpri[2] = 7;
        vpri[3] = 13;
        vpri[4] = 17;
        vpri[5] = 19;
        vpri[6] = 23;
        vpri[7] = 29;
        vpri[8] = 37;
        vpri[9] = 41;
        vpri[10] = 43;
        vpri[11] = 47;
        vpri[12] = 53;
        vpri[13] = 59;
        vpri[14] = 67;
        vpri[15] = 71;
        for (i = 0; i < z; i++) {
            y = Integer.valueOf(nit.substring(i, i + 1));
            x += (y * vpri[z - i]);
        }
        y = x % 11;
        if (y > 1) {
            dv1 = 11 - y;
        } else {
            dv1 = y;
        }
        return dv1;
    }

    private static void writeName(ResultSet rs, int pos, PrintWriter out) throws Exception {
        String val = rs.getString(pos);
        if (val != null) {
            val = new String(val.getBytes(), "ISO-8859-1");
            val = set(val);
            String[] parts = val.split(" ");
            switch (parts.length) {
                case 0:
                    out.write(";;");
                    break;
                case 1:
                    out.write(unSet(parts[0]) + ";;");
                    break;
                default:
                    out.write(unSet(parts[0]) + ";");
                    for (int i = 1; i < parts.length; i++) {
                        out.write(unSet(parts[i]) + " ");
                    }
                    out.write(";");
                    break;
            }
        } else {
            out.write(";;");
        }
    }
    private static String[][] kts = new String[][]{{"de la ", "@"}, {"de las ", "#"}, {"de los ", "$"}, {"del ", "+"}};

    private static String set(String str) {
        str = str.toLowerCase();
        for (int i = 0; i < kts.length; i++) {
            str = str.replace(kts[i][0], kts[i][1]);
        }
        return str;
    }

    private static String unSet(String str) {
        for (int i = 0; i < kts.length; i++) {
            str = str.replace(kts[i][1], kts[i][0]);
        }
        return Strings.toTitleType(str);
    }

    private static void writeVal(ResultSet rs, int pos, PrintWriter out) throws SQLException {
        String val = rs.getString(pos);
        if (val != null) {
            out.write(val);
        } else {
            out.write("");
        }
        out.write(";");
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
