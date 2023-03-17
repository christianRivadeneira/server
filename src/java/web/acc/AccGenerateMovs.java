/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web.acc;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

/**
 *
 * @author Mario
 */
@WebServlet(name = "AccGenerateMovs", urlPatterns = {"/AccGenerateMovs"})
public class AccGenerateMovs extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try (PrintWriter out = response.getWriter()) {
            
            Map<String, String> pars = MySQLQuery.scapedParams(request);
            
            String poolName = pars.get("poolName");
            String tz = pars.get("tz");
            String sessionId = pars.get("sessionId");
            Integer docId = Integer.valueOf(pars.get("docId"));

            try (Connection conn = MySQLCommon.getConnection(poolName, tz)) {
                
                SessionLogin.validate(sessionId, conn);
                
                Object[] docRow = new MySQLQuery("SELECT d.acc_id, t.type FROM acc_doc d INNER JOIN acc_doc_type t ON d.doc_type_id = t.id WHERE d.id = ?1").setParam(1, docId).getRecord(conn);
                int mainAccId = MySQLQuery.getAsInteger(docRow[0]);
                String type = MySQLQuery.getAsString(docRow[1]);

                Map<Integer, BigDecimal> baseByAcc = new HashMap<>();
                Object[][] itemsData = new MySQLQuery("select id, value * amount, acc_id from acc_item i where i.doc_id = ?1").setParam(1, docId).getRecords(conn);

                BigDecimal totalBase = BigDecimal.ZERO;
                BigDecimal totalIva = BigDecimal.ZERO;
                BigDecimal totalIco = BigDecimal.ZERO;

                BigDecimal totalRetIva = BigDecimal.ZERO;
                BigDecimal totalRetFue = BigDecimal.ZERO;
                BigDecimal totalRetIca = BigDecimal.ZERO;
                BigDecimal totalRetCree = BigDecimal.ZERO;

                for (Object[] itemRow : itemsData) {
                    int itemId = MySQLQuery.getAsInteger(itemRow[0]);
                    BigDecimal itemBase = MySQLQuery.getAsBigDecimal(itemRow[1], true);
                    int accId = MySQLQuery.getAsInteger(itemRow[2]);

                    totalBase = totalBase.add(itemBase);

                    if (!baseByAcc.containsKey(accId)) {
                        baseByAcc.put(accId, BigDecimal.ZERO);
                    }
                    baseByAcc.remove(accId);
                    baseByAcc.put(accId, baseByAcc.get(accId).add(itemBase));

                    BigDecimal impIvaRate = getImpRate(itemId, "iva", conn);
                    BigDecimal impIcoRate = getImpRate(itemId, "ico", conn);

                    BigDecimal impIva = itemBase.multiply(impIvaRate);
                    BigDecimal impIco = itemBase.multiply(impIcoRate);

                    totalIva = totalIva.add(impIva);
                    totalIco = totalIco.add(impIco);

                    BigDecimal retIvaRate = getRetRate(itemId, "iva", conn);
                    BigDecimal retFueRate = getRetRate(itemId, "fue", conn);
                    BigDecimal retIcaRate = getRetRate(itemId, "ica", conn);
                    BigDecimal retCreeRate = getRetRate(itemId, "cree", conn);

                    BigDecimal retIva = impIva.multiply(retIvaRate);
                    BigDecimal retFue = itemBase.multiply(retFueRate);
                    BigDecimal retIca = itemBase.multiply(retIcaRate);
                    BigDecimal retCree = itemBase.multiply(retCreeRate);

                    totalRetIva = totalRetIva.add(retIva);
                    totalRetFue = totalRetFue.add(retFue);
                    totalRetIca = totalRetIca.add(retIca);
                    totalRetCree = totalRetCree.add(retCree);
                }

                new MySQLQuery("DELETE FROM acc_mov WHERE doc_id = ?1").setParam(1, docId).executeUpdate(conn);

                /////////////////////
                Iterator<Map.Entry<Integer, BigDecimal>> it = baseByAcc.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Integer, BigDecimal> debitAcc = it.next();
                    int accId = debitAcc.getKey();
                    BigDecimal val = debitAcc.getValue();
                    AccMov.insert(new AccMov(docId, accId, "deb", val), conn);
                }

                //2408 iva
                AccMov.insert(new AccMov(docId, 2408, "deb", totalIva), conn);
                //impuesto al consumo ////////FALTA FALTA FALTA FALTA FALTA FALTA 
                //AccMov.insert(new AccMov(docId, 2408, "d", totalIco), conn);

                AccMov.insert(new AccMov(docId, 2367, "cred", totalRetIva), conn);
                AccMov.insert(new AccMov(docId, 236540, "cred", totalRetFue), conn);
                AccMov.insert(new AccMov(docId, 2368, "cred", totalRetIca), conn);
                //impuesto al consumo ////////FALTA FALTA FALTA FALTA FALTA FALTA 
                //out.println("retCree " + totalRetCree + "<br/>");

                BigDecimal credit = totalBase.add(totalIva).add(totalIco).subtract(totalRetIva).subtract(totalRetFue).subtract(totalRetIca).subtract(totalRetCree);
                AccMov.insert(new AccMov(docId, mainAccId, "cred", credit), conn);

                if (new MySQLQuery("SELECT sum(value) from acc_mov WHERE doc_id = ?1").setParam(1, docId).getAsBigDecimal(conn, true).compareTo(BigDecimal.ZERO) == 0) {
                    out.write("ok");
                } else {
                    out.write("unbalance");
                }
            } catch (Exception ex) {
                out.write("error");
                Logger.getLogger(AccGenerateMovs.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private BigDecimal getImpRate(int itemId, String imp, Connection conn) throws Exception {
        return new MySQLQuery("select rate from acc_item_con ic inner join acc_concept c on ic.conc_id = c.id where ic.item_id = ?1 and dest_imp = ?2").setParam(1, itemId).setParam(2, imp).getAsBigDecimal(conn, true).divide(new BigDecimal(100), RoundingMode.HALF_EVEN);
    }

    private BigDecimal getRetRate(int itemId, String ret, Connection conn) throws Exception {
        return new MySQLQuery("select rate from acc_item_con ic inner join acc_concept c on ic.conc_id = c.id where ic.item_id = ?1 and dest_ret = ?2").setParam(1, itemId).setParam(2, ret).getAsBigDecimal(conn, true).divide(new BigDecimal(100), RoundingMode.HALF_EVEN);
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
