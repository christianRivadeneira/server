package web.marketing;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import web.discount.ExportNif;

@MultipartConfig
@WebServlet(name = "ctrReportCsv", urlPatterns = {"/ctrReportCsv"})
public class GetCtrReportCsv extends HttpServlet {

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
        PrintWriter out = null;
        Statement st = null;
        try (Connection con = MySQLCommon.getConnection("sigmads", null);) {
            JsonObject req = MySQLQuery.scapeJsonObj(request);
            String sessionId = req.getString("sessionId");
            SessionLogin.validate(sessionId, con);
            String begin = req.getString("begin");
            String end = req.getString("end");
            Integer sectorId = req.isNull("sectorId") ? null : req.getInt("sectorId");
            Integer cityId = req.isNull("cityId") ? null : req.getInt("cityId");
            Integer zoneId = req.isNull("zoneId") ? null : req.getInt("zoneId");
            boolean afil = req.getBoolean("afil");
            boolean como = req.getBoolean("como");
            boolean deposit = req.getBoolean("deposit");
            boolean noDeposit = req.getBoolean("noDeposit");
            boolean cancel = req.getBoolean("cancel");
            boolean noCancel = req.getBoolean("noCancel");
            boolean anul = req.getBoolean("anul");
            boolean noAnul = req.getBoolean("noAnul");
            boolean ubiq = req.getBoolean("ubiq");
            boolean noUbiq = req.getBoolean("noUbiq");
            boolean aditional = req.getBoolean("ads");
            String types = req.getString("types").trim(); //tipos de cilindro
            String attachment = req.getString("attachment");

            String typeFilt = null;
            String typeTitle = null;
            if (afil && como) {
                typeFilt = "TRUE";
                typeTitle = "Afiliados y comodatos";
            } else if (!afil && como) {
                typeFilt = "contract.ctr_type = 'como'";
                typeTitle = "Solo comodatos";
            } else if (afil && !como) {
                typeFilt = "contract.ctr_type = 'afil'";
                typeTitle = "Solo afiliados";
            } else if (!afil && !como) {
                throw new Exception("Seleccione al menos un tipo.");
            }

            String anulFilt = null;
            String anulTitle = null;
            if (anul && noAnul) {
                anulFilt = "TRUE";
                anulTitle = "Anulados y sin anular";
            } else if (anul && !noAnul) {
                anulFilt = "anull_cause_id IS NOT NULL";
                anulTitle = "Solo anulados";
            } else if (!anul && noAnul) {
                anulFilt = "anull_cause_id IS NULL";
                anulTitle = "Solo sin anular";
            } else if (!anul && !noAnul) {
                throw new Exception("Seleccione al menos una opción de anulación.");
            }

            String cancelFilt = null;
            String cancelTitle = null;
            if (cancel && noCancel) {
                cancelFilt = "TRUE";
                cancelTitle = "Cancelados y sin cancelar";
            } else if (cancel && !noCancel) {
                cancelFilt = "cancel_cause_id IS NOT NULL";
                cancelTitle = "Solo cancelados";
            } else if (!cancel && noCancel) {
                cancelFilt = "cancel_cause_id IS NULL";
                cancelTitle = "Solo sin cancelar";
            } else if (!cancel && !noCancel) {
                throw new Exception("Seleccione al menos una opción de cancelación.");
            }

            String depositFilt = null;
            String depositTitle = null;
            if (deposit && noDeposit) {
                depositFilt = "TRUE";
                depositTitle = "Con y sin depósito";
            } else if (deposit && !noDeposit) {
                depositFilt = "deposit > 1";
                depositTitle = "Solo con depósito";
            } else if (!deposit && noDeposit) {
                depositFilt = "(deposit = 0 OR deposit = 1 OR deposit IS NULL) ";
                depositTitle = "Solo sin depósito";
            } else if (!deposit && !noDeposit) {
                throw new Exception("Seleccione al menos una opción de depósito.");
            }

            String ubiqFilt = null;
            String ubiqTitle = "";
            if (ubiq && noUbiq) {
                ubiqFilt = "TRUE";
                ubiqTitle = "Con y sin ubicación";
            } else if (ubiq && !noUbiq) {
                ubiqFilt = "neigh_id IS NOT NULL";
                ubiqTitle = "Solo con ubicación";
            } else if (!ubiq && noUbiq) {
                ubiqFilt = "neigh_id IS NULL";
                ubiqTitle = "Solo sin ubicación";
            } else if (!ubiq && !noUbiq) {
                throw new Exception("Seleccione al menos una opción de ubicación.");
            }

            String filt = typeFilt + " AND " + anulFilt + " AND " + cancelFilt + " AND " + depositFilt + " AND " + ubiqFilt;

            boolean smbEmail = new MySQLQuery("SELECT smb_email FROM smb_cfg").getAsBoolean(con);

            Object[][] cyls;
            if (types.isEmpty()) {
                cyls = new MySQLQuery("SELECT id, `name` FROM cylinder_type").getRecords(con);
            } else {
                cyls = new MySQLQuery("SELECT id, `name` FROM cylinder_type WHERE id IN (" + types + ") ").getRecords(con);
            }

            StringBuilder cylNames = new StringBuilder();
            StringBuilder str = new StringBuilder("SELECT ");
            str.append("contract.contract_num, ")//0
                    .append("contract.document, ")//1
                    .append("z.`name`, ")//2
                    .append("city.`name`, ")//2
                    .append("dp.code, ")//3
                    .append("contract.sign_date, ")//4
                    .append("(SELECT COUNT(*) > 0 FROM bfile b WHERE b.owner_type = 87 AND b.owner_id = contract.id) AS attach, ")//5
                    .append((aditional ? "neigh.name," : ""))//6
                    .append((aditional ? "CONCAT(first_name, ' ', last_name)," : ""))//7
                    .append((aditional ? "address," : ""))//8
                    .append((aditional ? "phones," : ""))//9
                    .append((smbEmail && aditional ? "email," : ""))//10
                    .append("contract.deposit, ");//11

            for (Object[] cyl : cyls) {
                str.append("(SELECT SUM(amount) FROM smb_ctr_cyl WHERE type_id = ").append(MySQLQuery.getAsInteger(cyl[0])).append(" AND action = 'd' AND  contract_id = contract.id) AS '").append(MySQLQuery.getAsString(cyl[1])).append("', ");
                cylNames.append(MySQLQuery.getAsString(cyl[1])).append(";");
            }

            str.append("(SELECT "
                    + "SUM(amount) "
                    + "FROM smb_ctr_cyl "
                    + "WHERE "
                    + (!types.isEmpty() ? "type_id IN (" + types + ") AND " : " ")
                    + "action = 'd' AND "
                    + "contract_id = contract.id) as 'Total' ")
                    .append("FROM ")
                    .append("contract ")
                    .append("LEFT JOIN neigh ON contract.neigh_id = neigh.id ")
                    .append("LEFT JOIN sector ON sector.id = neigh.sector_id ")
                    .append("LEFT JOIN city ON city.id = sector.city_id ")
                    .append("LEFT JOIN zone z ON z.id = city.zone_id ")
                    .append("LEFT JOIN dane_poblado dp ON dp.code = city.dane_code ")
                    .append("WHERE ");
            if (sectorId != null) {
                str.append("sector.id = ").append(sectorId).append(" AND ");
            }
            if (cityId != null) {
                str.append("city.id = ").append(cityId).append(" AND ");
            }
            if (zoneId != null) {
                str.append("city.zone_id = ").append(zoneId).append(" AND ");
            }
            str.append("sign_date BETWEEN '").append(begin).append("' AND '").append(end).append("' AND ")
                    .append(filt);

            if (attachment.equals("with")) {
                str.append(" HAVING attach");
            } else if (attachment.equals("without")) {
                str.append(" HAVING !attach");
            }

            response.setHeader("Content-Disposition", "attachment;filename=NIFS.csv");
            response.setContentType("text/plain;charset=ISO-8859-1");
            out = response.getWriter();
            out.write("BASE DE DATOS DE CONTRATOS Desde: " + begin + "  a " + end + System.lineSeparator());
            out.write(typeTitle + ". " + anulTitle + ". " + cancelTitle + ". " + System.lineSeparator());
            out.write(depositTitle + ". " + ubiqTitle + "." + (attachment.equals("with") ? "Con adjunto." : attachment.equals("without") ? "Sin adjunto." : "") + System.lineSeparator());
            out.write("" + System.lineSeparator());
            out.write("Contrato;Cédula;Zona;Poblado;DaneCod;Fecha;Adjuntos;" + (aditional ? "Barrio;Nombres;Dirección;Teléfono;" + (smbEmail && aditional ? "Correo;" : "") : "") + "Deposito;" + cylNames.toString() + "Total" + System.lineSeparator());
            long[] total = new long[9 + cyls.length + (aditional ? (smbEmail ? 7 : 6) : 0)];

            /**
             * Se hace por JDBC por optimización, no usar MySQLQuery, en este
             * caso gasta mucha memoria
             */
            st = con.createStatement();
            ResultSet rs = st.executeQuery(str.toString());
            int cols = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                String strRow = "";
                Integer tot = 0;
                for (int j = 0; j < cols; j++) {
                    Object cell = rs.getObject(j + 1);
                    if (j == 6) {
                        strRow += (MySQLQuery.getAsBoolean(cell) ? "Si" : "No");
                    } else if (j == 7 + (aditional ? (smbEmail ? 6 : 5) : 0)) {
                        strRow += (cell != null ? MySQLQuery.getAsBigDecimal(cell, true).intValue() + "" : "");
                    } else {
                        strRow += (cell != null ? cell.toString().replace(";", " ") : "");
                    }

                    strRow += ";";
                    if (j >= (7 + (aditional ? (smbEmail ? 6 : 5) : 0))) {
                        total[j] += (cell != null ? MySQLQuery.getAsInteger(cell) : 0);

                    }
                    if ((j + 1 == cols)) {
                        tot = MySQLQuery.getAsInteger(cell != null ? MySQLQuery.getAsInteger(cell) : 0);
                    }
                }
                strRow += (System.lineSeparator());
                if (types.isEmpty()) {
                    out.write(strRow);

                } else {
                    if (tot > 0) { // los clientes que estan en cero no sacar 
                        out.write(strRow);
                    }
                }
            }

            String totalRow = "";
            for (int j = 0; j < total.length; j++) {
                totalRow += (total[j] > 0 ? total[j] + "" : "");
                totalRow += ";";
            }
            totalRow += System.lineSeparator();
            out.write(totalRow);

        } catch (Exception ex) {
            Logger.getLogger(ExportNif.class.getName()).log(Level.SEVERE, null, ex);
            try {
                response.setStatus(500);
                response.getWriter().write(ex.getMessage());
            } catch (IOException ex1) {
                response.setStatus(500);
                Logger.getLogger(GetCtrReportCsv.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } finally {
            try {
                out.close();
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
        return "Reporte de Contratos";
    }
}
