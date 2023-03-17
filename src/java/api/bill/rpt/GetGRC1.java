package api.bill.rpt;

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
@WebServlet(name = "getGRC1", urlPatterns = {"/getGRC1"})
public class GetGRC1 extends HttpServlet {

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
        try (Connection con = MySQLCommon.getConnection("sigmads", null)) {
            JsonObject req = MySQLQuery.scapeJsonObj(request);
            String sessionId = req.getString("sessionId");
            int spanId = req.getInt("spanId");
            String instDb = req.getString("instDb");
            int pobId = req.getInt("pobId");
            String consMonth = req.getString("consMonth");
            int instId = req.getInt("instId");
            SessionLogin.validate(sessionId, con);
            response.setHeader("Content-Disposition", "attachment;filename=grc1.csv");
            response.setContentType("text/plain;charset=ISO-8859-1");
            out = response.getWriter();
            out.write("NIU,"//1
                    + "Tipo de Gas,"//2
                    + "ID Factura,"//3
                    + "Tipo de Factura,"//4
                    + "Fecha de Expedición de la Factura,"//5
                    + "Fecha de Inicio Periodo de Facturación,"//6
                    + "Fecha de Terminación del Periodo de Facturación,"//7
                    + "Predios en Condiciones Especiales," //8
                    + "Tipo de Lectura," //9
                    + "Factor De Poder Calorífico - Fpc," //10
                    + "Lectura Anterior," //11
                    + "Fecha Lectura Anterior, " //12
                    + "Lectura Actual," //13
                    + "Fecha Lectura Actual," //14
                    + "Número de Días Facturados,"//15
                    + "Factor de corrección utilizado,"//16
                    + "Consumo,"//17
                    + "Cuv Cargo Aplicado por Consumo,"//18
                    + "Facturación por Consumo,"//19
                    + "Facturación por Cargo Fijo,"//20
                    + "Valor por Mora Acumulado,"//21
                    + "Intereses por Mora Acumulado,"//22
                    + "Valor del Subsidio o Contribución,"//23
                    + "Porcentaje de Subsidio o Contribución Aplicado,"//24
                    + "Valor Cuota de Conexión,"//25
                    + "Intereses Financiación Conexión,"//26
                    + "Suspensión y Reconexión,"//27
                    + "Corte y Reinstalación,"//28
                    + "Tipo Revisión Instalación Interna,"//29
                    + "Fecha de la Revisión,"//30
                    + "Valor Otros Conceptos,"//31
                    + "Valor Intereses Otros Conceptos,"//32
                    + "Descripción Otros,"//33
                    + "Refacturación de Consumos,"//34
                    + "Valor Refacturación,"//35
                    + "Valor Refacturación Subsidio o Contribución,"//36
                    + "Fecha Límite de Pago,"//37
                    + "Fecha de Suspensión,"//38
                    + "Valor Total Facturado" //39
                    + System.lineSeparator());

            String str="SELECT "
                + "c.code AS 'NIU', "//1
                + "3 AS 'Tipo de Gas', "//2
                + "f.bill_num AS 'ID Factura', "//3
                + "(CASE WHEN (bcc.val_cons_no_subs + bcc.val_cons_subs)>= 0 AND (bcr.diff_val_cons_no_subs is null) THEN 1 WHEN (bcc.val_cons_no_subs + bcc.val_cons_subs)>=0 AND (bcr.diff_val_cons_no_subs is not null) THEN 3 WHEN (bcc.val_cons_no_subs + bcc.val_cons_subs) is null AND (bcr.diff_val_cons_no_subs is not null) THEN 2 ELSE 3 END) AS 'Tipo de Factura', "//4
                + "DATE_FORMAT(f.creation_date, '%d-%m-%Y') AS 'Fecha de Expedición de la Factura', "//5
                + "DATE_FORMAT(s.begin_date, '%d-%m-%Y') AS 'Fecha de Inicio Periodo de Facturación', "//6
                + "DATE_FORMAT(s.end_date, '%d-%m-%Y') AS 'Fecha de Terminación del Periodo de Facturación', "//7
                + "(CASE WHEN c.icfb_home = 1 THEN 1 WHEN c.priority_home = 1 THEN 2 WHEN c.asent_indigena = 1 THEN 3 ELSE 4 END) AS 'Predios en Condiciones Especiales', "//8
                + "(SELECT IF(br.critical_reading IS NOT NULL, '2', IF(cons_type IS NULL, '1', IF(cons_type = 'AVG', '2', '1'))) FROM "+instDb+".bill_client_tank c2 INNER JOIN sigma.neigh sn ON sn.id = c2.neigh_id INNER JOIN "+instDb+".bill_reading br ON br.client_tank_id = c2.id LEFT JOIN sigma.bill_reading_fault brf ON brf.id = br.fault_id LEFT JOIN "+instDb+".bill_clie_rebill rcb ON rcb.client_id = br.client_tank_id AND rcb.error_span_id = br.span_id AND rcb.active WHERE br.span_id = s.id AND c2.id = c.id) AS 'Tipo de Lectura', "//9
                + "s.power AS 'Factor De Poder Calorífico -Fpc', "//10
                + "IFNULL(ROUND(bcr2.orig_beg_read,0), ROUND(br4.last_reading,0)) AS 'Lectura Anterior', "//11
                + "(SELECT DATE_FORMAT(f2.creation_date,'%d-%m-%Y') FROM "+instDb+".bill_bill f2 WHERE f2.bill_span_id = s.id-1 AND f2.client_tank_id = c.id LIMIT 1) AS 'Fecha Lectura Anterior', "//12
                + "IFNULL(ROUND(bcr2.orig_end_read,0),ROUND(br4.reading,0)) AS 'Lectura Actual', "//13
                + "DATE_FORMAT(f.creation_date, '%d-%m-%Y') AS 'Fecha Lectura Actual', "//14
                + "DATEDIFF(s.end_date, s.begin_date)+1 AS 'Número de días facturados', "//15
                + "TRUNCATE(s.fadj,1) AS 'Factor de corrección utilizado', "//16
                + "ROUND(bcc.m3_subs+ bcc.m3_no_subs,0) AS 'Consumo', "//17
                + "s.cuv_r AS 'Cuv Cargo Aplicado por Consumo', "//18
                + "(bcc.val_cons_subs + bcc.val_cons_no_subs) AS 'Facturación por Consumo', "//19
                + "bcc.fixed_charge AS 'Facturación por Cargo Fijo', "//20
                + "(SELECT IFNULL(SUM(bp.value),0) FROM "+instDb+".bill_plan bp  WHERE bp.doc_id =f.id  AND bp.doc_type = 'fac' AND bp.account_cred_id IN (7,17,28,30,21,8)) AS 'Valor por Mora Acumulado', "//21
                + "(SELECT IFNULL(SUM(bp2.value),0) FROM "+instDb+".bill_plan bp2  WHERE bp2.doc_id = f.id  AND bp2.doc_type = 'fac' AND bp2.account_cred_id IN (6,18, 29,31,9)) AS 'Intereses por Mora Acumulado',"//22
                + "(bcc.val_subs+bcc.val_contrib) AS 'Valor del Subsidio o Contribución', "//23
                + "(CASE WHEN bcc.stratum = 1 THEN TRUNCATE((s.sub_perc_1/100),4) WHEN bcc.stratum = 2 THEN TRUNCATE((s.sub_perc_2/100),4) ELSE 0 END) AS 'Porcentaje de Subsidio o Contribución Aplicado', "//24
                + "(SELECT IFNULL(SUM(t.value),0) FROM "+instDb+".bill_transaction t WHERE t.cli_tank_id = c.id AND bill_span_id = s.id AND t.account_deb_id = 19 AND t.trans_type_id = 18) AS 'Valor Cuota de Conexión', "//25
                + "(SELECT IFNULL(SUM(t.value),0) FROM "+instDb+".bill_transaction t WHERE t.cli_tank_id = c.id AND bill_span_id = s.id AND t.account_deb_id = 20 AND t.trans_type_id = 21) AS 'Intereses Financiación Conexión', "//26
                + "(IFNULL((SELECT SUM(t.value) FROM "+instDb+".bill_transaction t WHERE t.cli_tank_id = c.id AND bill_span_id = s.id AND t.account_deb_id = 5),0)+(IFNULL((SELECT SUM(t.value) FROM "+instDb+".bill_transaction t WHERE t.cli_tank_id = c.id AND bill_span_id = s.id AND t.account_deb_id = 19 AND t.trans_type_id = 19),0))) AS 'Suspensión y Reconexión', "//27
                + "(SELECT IFNULL(SUM(t.value),0) FROM "+instDb+".bill_transaction t WHERE t.cli_tank_id = c.id AND bill_span_id = s.id AND t.account_deb_id = 19 AND t.trans_type_id = 26) AS 'Corte y Reinstalación', "//28
                + "(SELECT MAX(sbict.code) FROM "+instDb+".bill_inst_check bic INNER JOIN sigma.bill_inst_check_type sbict ON bic.type_id = sbict.id INNER JOIN sigma.bill_inst_inspector bii ON bic.inspector_id = bii.id WHERE bic.client_id = c.id LIMIT 1) AS 'Tipo Revisión Instalación Interna', "//29
                + "(SELECT DATE_FORMAT(bic2.chk_date, '%d-%m-%Y') FROM "+instDb+".bill_inst_check bic2 LEFT JOIN sigma.bill_inst_check_type bict2 on  bict2.id =bic2.type_id WHERE bic2.client_id =c.id and bict2.code=(SELECT MAX(sbict2.code) FROM "+instDb+".bill_inst_check bic3 INNER JOIN sigma.bill_inst_check_type sbict2 ON bic3.type_id = sbict2.id INNER JOIN sigma.bill_inst_inspector bii2 ON bic3.inspector_id = bii2.id WHERE bic3.client_id = c.id LIMIT 1) LIMIT 1) AS 'Fecha de la Revisión', "//30
                + "NULLIF(((IFNULL(CAST((SELECT (SUM(busf.value - busf.ext_pay)) FROM "+instDb+".bill_user_service us INNER JOIN "+instDb+".bill_service_type bst ON bst.id = us.type_id INNER JOIN "+instDb+".bill_user_service_fee busf ON us.id = busf.service_id WHERE busf.value - busf.ext_pay > 0 AND us.bill_client_tank_id = c.id AND us.bill_span_id + busf.place = s.id AND bst.trans_type <> 'conn')AS DECIMAL(40,4)),0))+(IFNULL((SELECT value FROM "+instDb+".bill_bill_pres WHERE bill_id =f.id and label LIKE 'Ajuste a la decena'),0)+(IFNULL((SELECT value FROM "+instDb+".bill_plan p  WHERE p.doc_type = 'fac' AND p.trans_type_id =8 AND bill_span_id =s.id AND account_cred_id =27 AND cli_tank_id =c.id LIMIT 1),0)))),0) AS 'Valor Otros Conceptos', "//31
                + "(SELECT IF (SUM(busf.inter - busf.ext_inter + IFNULL(busf.inter_tax, 0) - IFNULL(busf.ext_inter_tax, 0))IS NULL, 0, SUM(busf.inter - busf.ext_inter + IFNULL(busf.inter_tax, 0) - IFNULL(busf.ext_inter_tax, 0))) FROM "+instDb+".bill_user_service us INNER JOIN "+instDb+".bill_service_type bst ON bst.id = us.type_id INNER JOIN "+instDb+".bill_user_service_fee busf ON us.id = busf.service_id WHERE busf.value - busf.ext_pay > 0 AND us.bill_client_tank_id = c.id AND us.bill_span_id + busf.place = s.id AND bst.trans_type<>'conn') AS 'Valor Intereses Otros Conceptos', "//32
                + "CONCAT(IF((IFNULL((SELECT value FROM "+instDb+".bill_bill_pres WHERE bill_id =f.id and label LIKE 'Ajuste a la decena'),0))<>0,'Ajuste a la decena',''),"
                + "IF((IFNULL((SELECT value FROM "+instDb+".bill_plan p  WHERE p.doc_type = 'fac' AND p.trans_type_id =8 AND bill_span_id =s.id AND account_cred_id =27 AND cli_tank_id =c.id LIMIT 1),0))<>0,'-Financiación',''),"
                + "IF(((IFNULL(CAST((SELECT (SUM(busf.value - busf.ext_pay)) FROM "+instDb+".bill_user_service us INNER JOIN "+instDb+".bill_service_type bst ON bst.id = us.type_id INNER JOIN "+instDb+".bill_user_service_fee busf ON us.id = busf.service_id WHERE busf.value - busf.ext_pay > 0 AND us.bill_client_tank_id = c.id AND us.bill_span_id + busf.place = s.id AND bst.trans_type <> 'conn')AS DECIMAL(40,4)),0)))<>0,CONCAT('-',(SELECT GROUP_CONCAT(CAST(bst.name AS CHAR CHARACTER SET utf8) SEPARATOR '-') FROM "+instDb+".bill_user_service us INNER JOIN "+instDb+".bill_service_type bst ON bst.id = us.type_id INNER JOIN "+instDb+".bill_user_service_fee busf ON us.id = busf.service_id WHERE busf.value - busf.ext_pay > 0 AND us.bill_client_tank_id = c.id AND us.bill_span_id + busf.place = s.id AND bst.trans_type<>'conn')),'')) AS 'Descripción Otros',"//33
                + "(SELECT IF(SUM(diff_m3_subs+diff_m3_no_subs) IS NULL,0,diff_m3_subs+diff_m3_no_subs) FROM "+instDb+".bill_clie_rebill WHERE rebill_span_id = s.id AND active = 1 AND client_id = c.id) AS 'Refacturación de Consumos', "//34
                + "bcr.diff_val_cons_subs+bcr.diff_val_cons_no_subs AS 'Valor Refacturación', "//35
                + "-bcr.diff_val_subs AS 'Valor Refacturación Subsidio o Contribución', "//36
                + "DATE_FORMAT(s.limit_date,'%d-%m-%Y')  AS 'Fecha Límite de Pago', "//37
                + "DATE_FORMAT(s.susp_date, '%d-%m-%Y') AS 'Fecha de Suspensión', "//38
                + "(SELECT IFNULL(SUM(bp3.value),0) FROM "+instDb+".bill_plan bp3  WHERE bp3.doc_id = f.id  AND bp3.doc_type = 'fac' AND bp3.trans_type_id = 8) AS 'Valor Total Facturado' "//39
                + "FROM "+instDb+".bill_bill f "
                + "LEFT JOIN "+instDb+".bill_span s ON s.id = f.bill_span_id "
                + "LEFT JOIN "+instDb+".bill_client_tank c ON f.client_tank_id =c.id "
                + "LEFT JOIN "+instDb+".bill_clie_cau bcc ON bcc.client_id =f.client_tank_id AND bcc.span_id =f.bill_span_id "
                + "LEFT JOIN "+instDb+".bill_clie_rebill bcr ON bcr.client_id =f.client_tank_id AND bcr.rebill_span_id =f.bill_span_id AND bcr.active =1 "
                + "LEFT JOIN "+instDb+".bill_clie_rebill bcr2 ON bcr2.client_id =f.client_tank_id AND bcr2.error_span_id=f.bill_span_id AND bcr2.active =1 "
                + "LEFT JOIN "+instDb+".bill_reading br4 ON br4.client_tank_id =f.client_tank_id AND br4.span_id =f.bill_span_id "
                + "WHERE f.bill_span_id ="+spanId+" "
                + "AND f.creation_date =(SELECT MIN(f2.creation_date) FROM "+instDb+".bill_bill f2 WHERE f2.client_tank_id =c.id AND f2.bill_span_id=s.id)"
                + "ORDER BY f.bill_num";
            st = con.createStatement();
            ResultSet rs = st.executeQuery(str);
            int cols = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                String strRow = "";
                for (int j = 0; j < cols; j++) {
                    Object cell = rs.getObject(j + 1);
                    strRow += (cell != null ? cell.toString() : "");
                    if (j < cols - 1) {
                        strRow += ",";
                    }
                }
                strRow += (System.lineSeparator());
                out.write(strRow);
            }
        } catch (Exception ex) {
            Logger.getLogger(ExportNif.class.getName()).log(Level.SEVERE, null, ex);
            try {
                response.setStatus(500);
                response.getWriter().write(ex.getMessage());
            } catch (IOException ex1) {
                response.setStatus(500);
                Logger.getLogger(GetGRC1.class.getName()).log(Level.SEVERE, null, ex1);
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
        return "Reporte GRC1";
    }
}
