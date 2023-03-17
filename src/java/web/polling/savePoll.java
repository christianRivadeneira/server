package web.polling;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@WebServlet(name = "savePoll", urlPatterns = {"/polling/savePoll"})
public class savePoll extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        out.println("<!DOCTYPE html><?php include 'connect.php'; include 'inc_menu.php'; ?>");
        out.println("<html>");
        out.println("<head>");
        out.println("<style>  .questionTittle{ font-size: 16px; font-weight: bold; margin-top: 30px; margin-left: 20px; } .gray{ background-color: #EFEFEE; }   td{ padding: 8px;  text-align: center;         } table{ border-collapse:collapse;  margin-left: 20px; margin-right: 20px; } .left{  text-align: left;         } body{ padding: 0px; margin: 0px; font-family: Arial, sans-serif; font-size: 13px; } </style>");
        out.println("<title>Servlet savePoll</title>");
        out.println("</head>");
        out.println("<body class=\"arial\">");
        out.println("<table style=\"margin:0px; width: 100%\"><tr style=\"background-color: #183D68; color:white; font-weight: bold\"><td>¡GRACIAS!</td></tr><tr style=\"background-color: #B3CDF3\"><td></td></tr></table>");
        out.println("<div class=\"questionTittle\">Su respuesta se ha enviado con éxito.</div><br>");
        String poolName = "";
        String tz = "";
        String pollId = "";

        Connection con = null;
        Statement st = null;

        try {
            Map<String, String> req = MySQLQuery.scapedParams(request);
            String[] parts = req.get("q").split("&");
            for (String part : parts) {
                String[] row = part.split("=");
                switch (row[0]) {
                    case "p":
                        poolName = row[1];
                        break;
                    case "t":
                        tz = (row.length > 1 ? row[1] : "");
                        break;
                    case "i":
                        pollId = row[1];
                        break;
                    default:
                        break;
                }
            }
            out.write(tz);

            con = MySQLCommon.getConnection(poolName, tz);
            st = con.createStatement();

            ResultSet rs = st.executeQuery("SELECT model_id FROM cal_poll WHERE id = " + pollId);
            rs.next();
            int modelId = rs.getInt(1);
            rs.close();

            /////////
            rs = st.executeQuery("SELECT id, type, show_other FROM cal_poll_question WHERE model_id = " + modelId + " ORDER BY place ASC");
            Statement stUpd = con.createStatement();
            stUpd.executeUpdate("UPDATE cal_poll SET filled = NOW() WHERE id = " + pollId);
            stUpd.executeUpdate("DELETE FROM cal_poll_answer WHERE poll_id = " + pollId);
            for (int i = 0; rs.next(); i++) {
                int qId = rs.getInt(1);
                String qType = rs.getString(2);
                boolean showOther = rs.getBoolean(3);
                Question[] rows = Question.getQuestions(con, "row", qId);
                Question[] cols = Question.getQuestions(con, "col", qId);
                String rta = "";
                switch (qType) {
                    case "eval":
                        for (int j = 0; j < rows.length; j++) {
                            String par = req.get((i + "_" + j));
                            rta += par != null ? par + ";" + cols[Integer.valueOf(par)].score : "-1;-1";
                            rta += (j < rows.length - 1 ? "," : "");
                        }   break;
                    case "opt_mat":
                        for (int j = 0; j < rows.length; j++) {
                            String par = req.get((i + "_" + j));
                            rta += (par != null ? par : "-1");
                            rta += (j < rows.length - 1 ? "," : "");
                        }   break;
                    case "chk_mat":
                        for (int j = 0; j < rows.length; j++) {
                            for (int k = 0; k < cols.length; k++) {
                                rta += (req.get(i + "_" + j + "_" + k) != null ? "1" : "0");
                                rta += (k < cols.length - 1 ? "," : "");
                            }
                            rta += (j < rows.length - 1 ? ";" : "");
                        }   break;
                    case "chk_list":
                        for (int j = 0; j < rows.length; j++) {
                            rta += (req.get(i + "_" + j) != null ? "1" : "0");
                            rta += (j < rows.length - 1 ? "," : "");
                        }   if (showOther) {
                            rta += (req.get(i + "_other") != null ? ",1" : ",0");
                            if (req.get(i + "_other") != null) {
                                rta += "," + req.get(i + "_txt_other");
                            }
                        }   break;
                    case "opt_list":
                        if (req.get(i + "") != null) {
                            rta = req.get(i + "");
                            if (rta.equals(rows.length + "")) {
                                rta += (req.get(i + "_txt_other") != null ? "," + req.get(i + "_txt_other") : "");
                            }
                        }   break;
                    case "num_list":
                        for (int j = 0; j < rows.length; j++) {
                            rta += req.get(i + "_" + j);
                            rta += (j < rows.length - 1 ? "," : "");
                        }   break;
                    case "txt_list":
                        for (int j = 0; j < rows.length; j++) {
                            rta += req.get(i + "_" + j);
                            rta += (j < rows.length - 1 ? "," : "");
                        }   break;
                    case "obs":
                        rta = req.get(i + "");
                        break;
                //para debug
                //out.write(qType);
                    default:
                        break;
                }
                //para debug
                //out.write((i + 1) + ". " + rta + "<br>");
                stUpd.executeUpdate("INSERT INTO cal_poll_answer SET poll_id = " + pollId + ", question_id = " + qId + ", data = '" + rta + "'");
            }
            stUpd.close();
            out.println("</body>");
            out.println("</html>");
        } catch (Exception ex) {
            Logger.getLogger(savePoll.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            MySQLCommon.closeConnection(con, st);
        }
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
