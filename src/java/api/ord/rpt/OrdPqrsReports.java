package api.ord.rpt;

import api.bill.model.BillInstance;
import api.ord.model.OrdCfg;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import utilities.MySQLQuery;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.HeaderColumn;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.SummaryRow;
import utilities.mysqlReport.Table;
import utilities.mysqlReport.TableHeader;
import web.billing.BillingServlet;

public class OrdPqrsReports {

    public static final Integer POLL_TYPE_CYL = 3;//antiguas 
    public static final Integer POLL_TYPE_CYL_APP = 15;// tanto con app como sin app lo usa 

    public static final Integer POLL_TYPE_TANK = 5; // antiguas 
    public static final Integer POLL_TYPE_TANK_APP = 16; // tanto con app como sin app lo usa 

    public static final Integer POLL_TYPE_REPAIR = 11;//antiguas
    public static final Integer POLL_TYPE_REPAIR_APP = 14;// tanto con app como sin app lo usa 

    //CILINDROS
    public static MySQLReport getDetailsPqrsCylRep(Integer entId, Date beginDate, Date endDate, String typeCli, Integer oprId, int state, Integer officeId, Integer tecId, Integer channelId, Integer supreasonId, Connection conn) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        OrdCfg cfg = new OrdCfg().select(1, conn);

        MySQLReport rep = new MySQLReport("Detallado Pqrs Cilindros", "Período " + df.format(beginDate) + " - " + df.format(endDate), "pqrs_cyls", MySQLQuery.now(conn));

        rep.setVerticalFreeze(6);
        rep.setHorizontalFreeze(0);
        rep.setZoomFactor(80);

        //Subtitulos
        String enterpriseName = new MySQLQuery("SELECT name FROM enterprise WHERE id = " + entId).getAsString(conn);
        String officeDesc = new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(conn);
        rep.getSubTitles().add(("Empresa: " + (entId != null ? enterpriseName : "Todas")) + (" ,Oficina: " + (officeId != null ? officeDesc : "Todas las Oficinas")));

        //Formatos
        rep.getFormats().add(new utilities.mysqlReport.CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));//0

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.00"));//3
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "HH:mm:ss"));//4
        rep.getFormats().get(1).setWrap(true);

        //Columnas
        List<Column> cols = new ArrayList<>();
        int columns = 12;

        cols.add(new Column("Oficina", 11, 1));//0
        cols.add(new Column("Canal", 12, 1));//0
        cols.add(new Column("Núm.", 11, 0));//

        if (cfg.enterprise) {
            cols.add(new Column("Empresa", 10, 1));//2
            columns++;
        }

        if (cfg.supreason) {
            cols.add(new Column("Tipificación", 20, 1));//3
            columns++;
        }

        cols.add(new Column("Motivo", 20, 1));//4

        if (cfg.subreason) {
            cols.add(new Column("Dlle. Motivo", 20, 1));//5    
            columns++;
        }
        if (cfg.hasTechApp) {
            cols.add(new Column("Fecha", 12, 2));
            cols.add(new Column("Capturado", 13, 4));
            cols.add(new Column("Llegada", 13, 4));
            cols.add(new Column("Cierre", 13, 4));
        } else {
            cols.add(new Column("Capturado", 12, 2));//6
            cols.add(new Column("Hora Cap.", 13, 4));//7
        }
        cols.add(new Column("Capturó", 25, 1));//8
        cols.add(new Column("Técnico", 25, 1));//9
        if (!cfg.hasTechApp) {
            cols.add(new Column("Confirmado", 13, 2));//9
            cols.add(new Column("Hora Conf.", 13, 4));//10
        }
        cols.add(new Column("Tipo", 12, 1));//11
        cols.add(new Column("NIF Cilindro", 20, 1));//12
        if (cfg.showPqrNotes) {
            cols.add(new Column("Notas", 35, 1));
            columns++;
        }

        cols.add(new Column("Documento", 13, 1));//12
        cols.add(new Column("Cliente", 25, 1));//13
        cols.add(new Column("Teléfono", 15, 1));//14
        cols.add(new Column("Dirección", 35, 1));//15
        cols.add(new Column("Observaciones", 50, 1));//16
        cols.add(new Column("Sector", 15, 1));//17

        cols.add(new Column("Horas", 12, 4));//18
        cols.add(new Column("Oportuno", 12, 0));//19

        cols.add(new Column("Encuest.", 10, 0));//20
        cols.add(new Column("Cancel", 10, 0));//21
        cols.add(new Column("Actividad", 40, 1));
        cols.add(new Column("Asist Téc", 12, 0));

        Table tbl = new Table("Detallado Pqrs Cilindros");
        tbl.setColumns(cols);
        TableHeader header = new TableHeader();
        tbl.getHeaders().add(header);
        header.getColums().add(new HeaderColumn("Datos de la Pqr", columns, 1));
        header.getColums().add(new HeaderColumn("Datos del Cliente", 6, 1));
        header.getColums().add(new HeaderColumn("Tiempo Respuesta", 2, 1));
        header.getColums().add(new HeaderColumn("Estado", 2, 1));
        header.getColums().add(new HeaderColumn("Actividades", 1, 2));
        header.getColums().add(new HeaderColumn("Asist Téc", 1, 2));

        tbl.setSummaryRow(new SummaryRow("Totales", columns + 6));

        Object[][] colsToReport = new MySQLQuery("SELECT t.name, t.id "
                + "FROM ord_poll_version v "
                + "INNER JOIN ord_poll_question q ON q.ord_poll_version_id = v.id "
                + "INNER JOIN ord_poll_question_type t ON t.id = q.question_type_id "
                + "WHERE v.report AND last = 1 AND v.ord_poll_type_id IN (" + POLL_TYPE_CYL + ", " + POLL_TYPE_CYL_APP + " ) "
                + "GROUP BY t.id ").print().getRecords(conn);

        boolean hasExtraCols = colsToReport != null && colsToReport.length > 0;

        String head = "SELECT "
                + (hasExtraCols ? "pqr.id, " : " ")
                + "of.sname, "//0
                + "ch.name, "//0
                + "pqr.serial, " //1
                + (cfg.enterprise ? "e.`short_name`, " : "")//2+
                + (cfg.supreason ? " supr.description,  " : "")
                + "r.description, "//3
                + (cfg.subreason ? " subr.description, " : "")//4
                + (cfg.hasTechApp ? "pqr.`creation_date`, pqr.`regist_hour`, pqr.`arrival_date`, pqr.`attention_hour`, " : "pqr.`creation_date`, pqr.`regist_hour`, ")
                + "CONCAT(o.first_name, \" \" , o.last_name) AS oper, "//7
                + "CONCAT(t.first_name, \" \" , t.last_name) AS tec, "//8
                + (cfg.hasTechApp ? "" : "pqr.`attention_date`, pqr.`attention_hour`, ")
                + "IF(i.type = 'brand',\"Afiliado\", IF(i.type = 'univ',\"Provisional\", IF(i.type = 'app',\"App\", null))), "
                + "pqr.nif, "//12
                + (cfg.showPqrNotes ? "pqr.`notes`," : "")
                + "i.document, "//12
                + "CONCAT(i.first_name, \" \" , i.last_name) AS cli, "//12
                + "i.phones, "//13
                + "CONCAT(i.address, IF(n.`name` IS NOT NULL, CONCAT(' ', n.`name`), '')) AS addr, "//14
                + "poll.notes, "//15
                + "s.`name` AS sector, "//16
                //inf de pac o prom
                + "IF(pqr.creation_date = pqr.attention_date AND TIME(pqr.arrival_date) > pqr.regist_hour, TIMEDIFF(TIME(pqr.arrival_date), pqr.regist_hour),null), "//17
                + "IF(pqr.creation_date = pqr.attention_date,IF(TIME_TO_SEC(TIME(pqr.arrival_date)) - TIME_TO_SEC(pqr.regist_hour) <= " + (cfg.cylPqrLimitTime * 60) + ",1,0),0), " //18
                + "iF(pqr.satis_poll_id is NUll,0,1), "//19
                + "iF(pqr.pqr_anul_cause_id is NUll,0,1), "//20
                + "GROUP_CONCAT("
                + " CONCAT(oap.activity,' ',COALESCE(oap.observation,''),' ',oap.act_date,IF(bfile.id IS NOT NULL,'(Si)','(No)'),'\n')  "
                + "ORDER BY oap.id  SEPARATOR '\n' ), "// 21 
                + "repair.serial ";//21 

        String body = " FROM "
                + "ord_pqr_cyl AS pqr "
                + "LEFT JOIN employee AS o ON o.id = pqr.regist_by "
                + (cfg.enterprise ? "LEFT JOIN enterprise AS e ON e.id = pqr.enterprise_id  " : "")
                + "LEFT JOIN ord_contract_index as i ON pqr.index_id= i.id "
                + "LEFT JOIN neigh AS n ON n.id = i.neigh_id "
                + "LEFT JOIN sector AS s ON s.id = n.sector_id "
                + "LEFT JOIN ord_pqr_reason as r ON pqr.pqr_reason=r.id "
                + "LEFT JOIN ord_technician as t ON pqr.technician_id=t.id "
                + (cfg.subreason ? "LEFT JOIN ord_pqr_subreason subr ON subr.id = pqr.pqr_subreason " : "")
                + "LEFT JOIN ord_poll as poll ON pqr.pqr_poll_id = poll.id "
                + "LEFT JOIN ord_office AS of ON of.id = pqr.office_id "
                + "LEFT JOIN ord_channel AS ch ON ch.id = pqr.channel_id "
                + (cfg.supreason ? "LEFT JOIN ord_pqr_supreason supr ON supr.id = r.supreason_id  " : "")
                + "LEFT JOIN ord_activity_pqr oap ON oap.pqr_cyl_id = pqr.id  "
                + "LEFT JOIN bfile ON oap.id = bfile.owner_id AND bfile.owner_type = 20 "
                + "LEFT JOIN ord_repairs repair ON repair.pqr_cyl_id = pqr.id ";

        String where = " WHERE "
                + " pqr.creation_date BETWEEN ?2 AND ?3 "
                + (officeId != null ? "AND pqr.office_id = " + officeId + " " : " ")
                + (entId != null && cfg.enterprise ? " AND pqr.enterprise_id = " + entId + " " : "")
                + (oprId != null ? " AND o.id = " + oprId + " " : "")
                + (tecId != null ? " AND t.id = " + tecId + " " : "")
                + (channelId != null ? " AND ch.id = " + channelId : " ")
                + (supreasonId != null ? " AND supr.id = " + supreasonId : " ");
        if (typeCli != null) {
            where += " AND i.type = '" + typeCli + "' ";
        }
        switch (state) {
            case 1:
                //atendidos
                where += " And pqr.attention_date IS NOT NULL ";
                break;
            case 2:
                //no atendidos
                where += " And pqr.attention_date IS NULL "
                        + " And pqr.pqr_anul_cause_id IS NULL ";
                break;
            case 3:
                //cancelados
                where += " And pqr.pqr_anul_cause_id IS NOT NULL ";
                break;
            default:
                break;
        }

        MySQLQuery mq = new MySQLQuery(head + body + where + "GROUP BY pqr.id ");
        mq.setParam(2, beginDate);
        mq.setParam(3, endDate);
        Object[][] records = mq.getRecords(conn);// datos de la encuesta

        if (records != null && records.length > 0) {

            if (hasExtraCols) {
                header.getColums().add(new HeaderColumn("Encuesta", colsToReport.length, 1));
                for (Object[] obj : colsToReport) {
                    tbl.getColumns().add(new Column(MySQLQuery.getAsString(obj[0]), 18, 1));
                }

                MySQLQuery mqExtra = new MySQLQuery("SELECT  pqr.id, qt.id, txt.text " + body + " "
                        + "INNER JOIN ord_poll_question q ON  q.ord_poll_version_id = poll.poll_version_id "
                        + "INNER JOIN ord_poll_question_type qt ON qt.id = q.question_type_id "
                        + "INNER JOIN ord_text_poll txt ON txt.poll_id = poll.id AND txt.ordinal = q.ordinal " + where);

                mqExtra.setParam(2, beginDate);
                mqExtra.setParam(3, endDate);
                Object[][] valueData = mqExtra.getRecords(conn);

                Map<Integer, Map<Integer, String>> extraData = new HashMap<>();

                if (valueData != null && valueData.length > 0) {
                    int pqrId = 0;
                    Map<Integer, String> innerMap = null;
                    for (int i = 0; i < valueData.length; i++) {
                        Object[] obj = valueData[i];
                        int curPqrId = MySQLQuery.getAsInteger(obj[0]);

                        if (pqrId != curPqrId) {
                            pqrId = curPqrId;
                            innerMap = new HashMap<>();
                        }

                        if (pqrId == curPqrId) {
                            innerMap.put(MySQLQuery.getAsInteger(obj[1]), MySQLQuery.getAsString(obj[2]));
                        }

                        if (i == valueData.length - 1 || pqrId != MySQLQuery.getAsInteger(valueData[i + 1][0])) {
                            extraData.put(MySQLQuery.getAsInteger(obj[0]), innerMap);
                        }
                    }
                }

                int xLength = records[0].length - 1;//se resta el pqr_id
                Object[][] newData = new Object[records.length][xLength + colsToReport.length];

                for (int i = 0; i < newData.length; i++) {
                    int pqrId = MySQLQuery.getAsInteger(records[i][0]);
                    for (int j = 0; j < xLength; j++) {
                        newData[i][j] = records[i][j + 1];
                    }
                    for (int j = 0; j < colsToReport.length; j++) {
                        int typeId = MySQLQuery.getAsInteger(colsToReport[j][1]);
                        Map<Integer, String> colsData = extraData.get(pqrId);
                        if (colsData != null) {
                            newData[i][xLength + j] = colsData.get(typeId);
                        } else {
                            newData[i][xLength + j] = null;
                        }
                    }
                }

                tbl.setData(newData);
                rep.getTables().add(tbl);

            } else {
                tbl.setData(records);
                rep.getTables().add(tbl);
            }
        }
        return rep;
    }

    //RPTE CRITICA 
    public static MySQLReport criticRequest(int year, int month, int state, Connection conn) throws Exception {
        // listado de instancias     
        String where = "";
        switch (state) {
            case 1: //Asignado
                where = where + " AND s.`type` IS NOT NULL ";
                break;
            case 2: // cancelado
                where = where + " AND s.dt_cancel IS NOT NULL  ";
                break;
            case 3: // pendiente
                where = where + " AND s.`type` IS NULL AND s.dt_cancel IS NULL ";
                break;

        }
        List<BillInstance> insts = BillInstance.getAll(conn);
        List<Object[][]> res = new ArrayList<>();
        for (int i = 0; i < insts.size(); i++) {
            BillInstance inst = insts.get(i);
            String db = inst.db;
            BillingServlet.getInst(inst.id).useInstance(conn);
            Integer spanId = new MySQLQuery("SELECT id FROM " + db + ".bill_span WHERE MONTH(end_date) = ?1 AND YEAR(end_date)= ?2 ").setParam(2, year).setParam(1, month).getAsInteger(conn);
            if (spanId != null) {
                String str = "SELECT "
                        + "if(s.`type` IS NULL, 'Solicitud', if(s.`type` = 'tank', "//0
                        + "'PQR Fuga Estacionario', if(s.`type` = 'other', 'PQR Reclamante', 'Asistencia Técnica'))) AS tipo, "
                        + "ci.name, "//1 
                        + "s.creation_date, "//2
                        + "CONCAT(e.first_name, ' ', e.last_name), "//3
                        + "CONCAT(c.first_name,'',COALESCE(c.last_name,'')), "//4
                        + "c.phones, "//5
                        + "IF(s.dt_cancel IS NOT NULL, 'Cancelado', if(s.`type` IS NULL, 'Pendiente', 'Asignado')) AS estado, "//6
                        + "if(t.id IS NOT NULL, t.serial, if(ot.id IS NOT NULL, ot.serial, r.serial)) AS serial_as, "//7
                        + "if(t.id IS NOT NULL, t.regist_date, if(ot.id IS NOT NULL, ot.regist_date, r.regist_date)) AS fecha_as, "//8
                        + "if(t.id IS NOT NULL, t.attention_date, if(ot.id IS NOT NULL, ot.confirm_date, r.confirm_date)) AS fecha_aten, "//9
                        + "if(t.id IS NOT NULL, pollT.notes, if(ot.id IS NOT NULL, pollO.notes,if(r.id IS NOT NULL,pollR.notes,'') )) AS aten, "//10
                        + "CONCAT(ec.first_name, ' ', ec.last_name), "//11
                        + "s.cancel_note, "//12
                        + "cancel.name, "
                        + "CONCAT(eco.first_name, ' ', eco.last_name), "//13
                        + "if(s.notes IS NOT NULL, s.notes, if(t.id IS NOT NULL, t.notes, if(ot.id IS NOT NULL, '', r.notes))), "//14
                        + "s.num_meter, " //15
                        + "c.num_install, "//16
                        + "ROUND((last6.reading - last6.last_reading), 3), "//17     
                        + "ROUND((last5.reading - last5.last_reading), 3), "//18     
                        + "ROUND((last4.reading - last4.last_reading), 3), "//19    
                        + "ROUND((last3.reading - last3.last_reading), 3), "//20
                        + "ROUND((last2.reading - last2.last_reading), 3), "//21
                        + "ROUND((last1.reading - last1.last_reading), 3), "//22
                        + "ROUND((curre.reading - curre.last_reading), 3), "//23
                        + "if(ect.residential,'Si','No') "//24
                        + "FROM sigma.ord_pqr_request s "
                        + "INNER JOIN sigma.ord_pqr_client_tank c ON c.id = s.client_tank_id  "
                        + "INNER JOIN " + db + ".bill_client_tank AS cli ON cli.id= c.mirror_id "
                        + "INNER JOIN sigma.employee e ON s.created_id = e.id "
                        + "INNER JOIN sigma.city ci ON ci.id = c.city_id "
                        + "INNER JOIN sigma.ord_tank_client otc ON otc.id = c.build_ord_id "
                        + "INNER JOIN sigma.est_tank_category cat ON otc.categ_id = cat.id "
                        + "INNER JOIN sigma.est_categ_type ect ON cat.type_id = ect.id "
                        + "LEFT JOIN sigma.ord_pqr_req_cancel cancel ON cancel.id = s.cancel_id "
                        + "LEFT JOIN sigma.ord_pqr_tank t ON t.id = s.type_id AND s.`type` = 'tank' "
                        + "LEFT JOIN sigma.ord_pqr_other ot ON ot.id = s.type_id AND s.`type` = 'other' "
                        + "LEFT JOIN sigma.ord_repairs r ON r.id = s.type_id AND s.`type` = 'repair' "
                        + "LEFT JOIN sigma.ord_poll as pollT ON t.pqr_poll_id = pollT.id " // poll con tank 
                        + "LEFT JOIN sigma.ord_poll as pollO ON ot.pqr_poll_id = pollO.id " //reclamante 
                        + "LEFT JOIN sigma.ord_poll as pollR ON r.pqr_poll_id = pollR.id " //Asistencia 
                        + "LEFT JOIN sigma.employee ec ON ec.id = s.cancelled_by "
                        + "LEFT JOIN sigma.employee eco ON eco.id = s.converted_by "
                        + "LEFT JOIN " + db + ".bill_reading AS curre ON curre.client_tank_id = cli.id AND curre.span_id = " + (spanId - 0) + " "
                        + "LEFT JOIN " + db + ".bill_reading AS last1 ON last1.client_tank_id = cli.id AND last1.span_id = " + (spanId - 1) + " "
                        + "LEFT JOIN " + db + ".bill_reading AS last2 ON last2.client_tank_id = cli.id AND last2.span_id = " + (spanId - 2) + " "
                        + "LEFT JOIN " + db + ".bill_reading AS last3 ON last3.client_tank_id = cli.id AND last3.span_id = " + (spanId - 3) + " "
                        + "LEFT JOIN " + db + ".bill_reading AS last4 ON last4.client_tank_id = cli.id AND last4.span_id = " + (spanId - 4) + " "
                        + "LEFT JOIN " + db + ".bill_reading AS last5 ON last5.client_tank_id = cli.id AND last5.span_id = " + (spanId - 5) + " "
                        + "LEFT JOIN " + db + ".bill_reading AS last6 ON last6.client_tank_id = cli.id AND last6.span_id = " + (spanId - 6) + " "
                        + "WHERE s.span_id = ?1 "
                        + where
                        + " AND s.bill_req_type = 'reading' AND c.bill_instance_id = " + inst.id + " "
                        + "ORDER BY s.creation_date DESC ";

                MySQLQuery q = new MySQLQuery(str);
                q.setParam(1, spanId);
                res.add(q.getRecords(conn));
            }
        }

        MySQLReport rep = new MySQLReport("Crítica de Facturación", "", "critic_fac", MySQLQuery.now(conn));

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//1                      
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.000"));//3

        rep.setZoomFactor(80);
        rep.getFormats().get(0).setWrap(true);
        rep.setVerticalFreeze(5);

        Table tb = new Table("Crítica de Facturación");
        tb.getColumns().add(new Column("Tipo", 20, 0));
        tb.getColumns().add(new Column("Poblado", 15, 0));
        tb.getColumns().add(new Column("Solicitud", 12, 1));
        tb.getColumns().add(new Column("Capturado por", 25, 0));
        tb.getColumns().add(new Column("Cliente", 30, 0));
        tb.getColumns().add(new Column("Teléfono", 15, 0));
        tb.getColumns().add(new Column("Estado", 20, 0));
        tb.getColumns().add(new Column("No Caso", 11, 2));
        tb.getColumns().add(new Column("Fec Caso", 12, 1));//8
        tb.getColumns().add(new Column("Conf Caso", 12, 1));//9
        tb.getColumns().add(new Column("Aten Caso", 25, 0));
        tb.getColumns().add(new Column("Cancelado por", 25, 0));
        tb.getColumns().add(new Column("Cancelado Nota", 25, 0));
        tb.getColumns().add(new Column("Motivo", 20, 0));
        tb.getColumns().add(new Column("Convert por", 25, 0));
        tb.getColumns().add(new Column("Detalle", 30, 0));
        tb.getColumns().add(new Column("Medidor", 12, 0));
        tb.getColumns().add(new Column("No Instala", 12, 0));
        tb.getColumns().add(new Column("C6", 9, 3));
        tb.getColumns().add(new Column("C5", 9, 3));
        tb.getColumns().add(new Column("C4", 9, 3));
        tb.getColumns().add(new Column("C3", 9, 3));
        tb.getColumns().add(new Column("C2", 9, 3));
        tb.getColumns().add(new Column("C1", 9, 3));
        tb.getColumns().add(new Column("Actual", 15, 3));
        tb.getColumns().add(new Column("Res", 6, 0));

        List<Object[]> rows = new ArrayList<>();
        for (int i = 0; i < res.size(); i++) {
            Object[][] row = res.get(i);
            for (int j = 0; j < row.length; j++) {
                rows.add(row[j]);
            }
        }
        tb.setData(rows);
        if (tb.getData().length > 0) {
            rep.getTables().add(tb);
        }
        return rep;
    }

    // TANQUES
    public static MySQLReport getDetailedTankPqrReport(Date beginDate, Date endDate, Integer oprId, int state, Integer officeId, Integer tecId, Integer channelId, Integer supreasonId, Connection conn) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        OrdCfg cfg = new OrdCfg().select(1, conn);

        MySQLReport rep = new MySQLReport("Detallado Pqrs Estacionarios", "Período " + df.format(beginDate) + " - " + df.format(endDate), "pqrs_tanks", MySQLQuery.now(conn));

        rep.setVerticalFreeze(6);
        rep.setHorizontalFreeze(0);
        rep.setZoomFactor(80);

        //Subtitulos
        String officeDesc = new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(conn);
        rep.getSubTitles().add("Oficina: " + (officeId != null ? officeDesc : "Todas las oficinas"));

        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.00"));//3
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "HH:mm:ss"));//4
        rep.getFormats().get(1).setWrap(true);

        //Columnas
        List<Column> cols = new ArrayList<>();
        int columns = 10;

        cols.add(new Column("Oficina", 11, 1));
        cols.add(new Column("Canal", 12, 1));
        cols.add(new Column("Núm.", 11, 0));
        if (cfg.supreason) {
            cols.add(new Column("Tipificación", 20, 1));
            columns++;
        }
        cols.add(new Column("Motivo", 20, 1));
        if (cfg.subreason) {
            cols.add(new Column("Dlle. Motivo", 20, 1));
            columns++;
        }
        if (cfg.hasTechApp) {
            cols.add(new Column("Fecha", 12, 2));
            cols.add(new Column("Capturado", 13, 4));
            cols.add(new Column("Llegada", 13, 4));
            cols.add(new Column("Cierre", 13, 4));
        } else {
            cols.add(new Column("Capturado", 12, 2));
            cols.add(new Column("Hora Cap.", 13, 4));
        }
        cols.add(new Column("Capturó", 25, 1));
        cols.add(new Column("Técnico", 25, 1));
        if (!cfg.hasTechApp) {
            cols.add(new Column("Confirmado", 13, 2));
            cols.add(new Column("Hora Conf.", 13, 4));
        }
        cols.add(new Column("Tipo", 15, 1));
        if (cfg.showPqrNotes) {
            cols.add(new Column("Notas", 35, 1));
            columns++;
        }
        cols.add(new Column("Cliente", 25, 1));
        cols.add(new Column("Teléfono", 15, 1));
        cols.add(new Column("Dirección", 35, 1));
        cols.add(new Column("Observaciones", 50, 1));
        cols.add(new Column("Horas", 12, 4));
        cols.add(new Column("Oportuno", 12, 0));
        cols.add(new Column("Encuest.", 10, 0));
        cols.add(new Column("Cancel", 10, 0));
        cols.add(new Column("Actividad", 40, 1));
        cols.add(new Column("Asistencia", 12, 0));

        Table tbl = new Table("Detallado Pqrs Estacionarios");
        tbl.setColumns(cols);
        TableHeader header = new TableHeader();
        tbl.getHeaders().add(header);

        header.getColums().add(new HeaderColumn("Datos Pqr", columns, 1));
        header.getColums().add(new HeaderColumn("Datos Cliente", 5, 1));
        header.getColums().add(new HeaderColumn("Tiempo Respuesta", 2, 1));
        header.getColums().add(new HeaderColumn("Estado", 2, 1));
        header.getColums().add(new HeaderColumn("Actividades", 1, 2));
        header.getColums().add(new HeaderColumn("Asistencia", 1, 2));

        tbl.setSummaryRow(new SummaryRow("Totales", columns + 5));

        Object[][] colsToReport = new MySQLQuery("SELECT t.name, t.id "
                + "FROM ord_poll_version v "
                + "INNER JOIN ord_poll_question q ON q.ord_poll_version_id = v.id "
                + "INNER JOIN ord_poll_question_type t ON t.id = q.question_type_id "
                + "WHERE v.report AND last = 1 AND v.ord_poll_type_id IN (" + POLL_TYPE_TANK + ", " + POLL_TYPE_TANK_APP + " ) "
                + "GROUP BY t.id ").print().getRecords(conn);

        boolean hasExtraCols = colsToReport != null && colsToReport.length > 0;

        String head = "SELECT "
                + (hasExtraCols ? "pqr.id, " : " ")
                + "of.sname, "//0
                + "ch.name, "//1
                + "pqr.serial, " //2
                + (cfg.supreason ? " supr.description, " : "")//4
                + "r.description, "//3
                + (cfg.subreason ? " subr.description, " : "")//4
                + (cfg.hasTechApp ? "pqr.`regist_date`, pqr.`regist_hour`, pqr.`arrival_date`, pqr.`attention_hour`, " : "pqr.`regist_date`, pqr.`regist_hour`, ")
                + "CONCAT(o.first_name, \" \" , o.last_name) AS oper, "//7
                + "CONCAT(t.first_name, \" \" , t.last_name) AS tec, "//8
                + (cfg.hasTechApp ? "" : "pqr.`attention_date`, pqr.`attention_hour`, ")
                + "IF(client.id is not null, type.description , tp.description), "//11
                + (cfg.showPqrNotes ? " pqr.notes, " : "")
                + "IF(client.id is not null, CONCAT(client.first_name, \" \", COALESCE(client.last_name, '')), b.name), "//12
                + "IF(client.id is not null, client.phones , b.phones), "//13
                + "IF(client.build_ord_id is NOT NULL, "
                + "(IF(build.id is not null, CONCAT(build.address,\" \",build.name), b.address)),"//Si 
                + "CONCAT(n.name,' - ',client.address)" //No 
                + "), "//14
                + "poll.notes, "//15

                + "IF(pqr.regist_date = pqr.attention_date AND TIME(pqr.arrival_date) > pqr.regist_hour ,TIMEDIFF(TIME(pqr.arrival_date),pqr.regist_hour),null), " //16
                + "IF(pqr.regist_date = pqr.attention_date, IF(TIME_TO_SEC(TIME(pqr.arrival_date)) - TIME_TO_SEC(pqr.regist_hour) <= " + (cfg.tankPqrLimitTime * 60) + ",1,0),0), " //17
                + "iF(pqr.satis_poll_id is NUll,0,1), "//18
                + "iF(pqr.anul_cause_id is NUll,0,1), "//19
                + "GROUP_CONCAT("
                + " CONCAT(oap.activity,' ',COALESCE(oap.observation,''),' ',oap.act_date,IF(bfile.id IS NOT NULL,'(Si)','(No)'),'\n')  "
                + "ORDER BY oap.id  SEPARATOR '\n' ), "//20
                + "repair.serial ";//21 

        String body = " FROM "
                + "ord_pqr_tank AS pqr "
                + "LEFT JOIN employee AS o ON o.id = pqr.regist_by "
                + "LEFT JOIN ord_pqr_reason as r ON pqr.reason_id=r.id "
                + "LEFT JOIN ord_technician as t ON pqr.technician_id=t.id "
                + "LEFT JOIN ord_pqr_client_tank as client ON client.id=pqr.client_id  "
                + "LEFT JOIN ord_tank_client as build ON build.id=client.build_ord_id "
                + "LEFT JOIN est_tank_category as type ON type.id=build.categ_id "
                + "LEFT JOIN ord_tank_client as b ON b.id=pqr.build_id  "
                + "LEFT JOIN est_tank_category as tp ON tp.id=b.categ_id "
                + "LEFT JOIN ord_poll as poll ON pqr.pqr_poll_id = poll.id "
                + (cfg.subreason ? "LEFT JOIN ord_pqr_subreason subr ON subr.id = pqr.subreason_id " : " ")
                + (cfg.supreason ? "LEFT JOIN ord_pqr_supreason supr ON supr.id = r.supreason_id " : " ")
                + "LEFT JOIN ord_office AS of ON of.id = pqr.office_id "
                + "LEFT JOIN ord_channel AS ch ON ch.id = pqr.channel_id "
                + "LEFT JOIN ord_activity_pqr oap ON oap.pqr_tank_id = pqr.id  "
                + "LEFT JOIN neigh n ON n.id = client.neigh_id  "
                + "LEFT JOIN bfile ON oap.id = bfile.owner_id AND bfile.owner_type = 20 "
                + "LEFT JOIN ord_repairs repair ON repair.pqr_tank_id = pqr.id ";

        String where = " WHERE "
                + "pqr.regist_date BETWEEN ?2 AND ?3 "
                + (officeId != null ? "AND pqr.office_id = " + officeId + " " : " ")
                + (oprId != null ? " AND o.id = " + oprId + " " : " ")
                + (tecId != null ? " AND t.id = " + tecId + " " : " ")
                + (channelId != null ? " AND ch.id = " + channelId + " " : " ")
                + (supreasonId != null ? " AND supr.id = " + supreasonId + " " : " ");

        switch (state) {
            case 1:
                //atendidos
                where += " And pqr.attention_date IS NOT NULL ";
                break;
            case 2:
                //no atendidos
                where += " And pqr.attention_date IS NULL "
                        + " And pqr.anul_cause_id IS NULL ";
                break;
            case 3:
                //cancelados
                where += " And pqr.anul_cause_id IS NOT NULL ";
                break;
            default:
                break;
        }

        MySQLQuery mq = new MySQLQuery(head + body + where + "GROUP BY pqr.id ");
        mq.setParam(2, beginDate);
        mq.setParam(3, endDate);
        Object[][] records = mq.getRecords(conn);// datos de la encuesta

        if (records != null && records.length > 0) {

            if (hasExtraCols) {
                header.getColums().add(new HeaderColumn("Encuesta", colsToReport.length, 1));
                for (Object[] obj : colsToReport) {
                    tbl.getColumns().add(new Column(MySQLQuery.getAsString(obj[0]), 18, 1));
                }

                MySQLQuery mqExtra = new MySQLQuery("SELECT  pqr.id, qt.id, txt.text " + body + " "
                        + "INNER JOIN ord_poll_question q ON  q.ord_poll_version_id = poll.poll_version_id "
                        + "INNER JOIN ord_poll_question_type qt ON qt.id = q.question_type_id "
                        + "INNER JOIN ord_text_poll txt ON txt.poll_id = poll.id AND txt.ordinal = q.ordinal " + where);

                mqExtra.setParam(2, beginDate);
                mqExtra.setParam(3, endDate);
                Object[][] valueData = mqExtra.getRecords(conn);

                Map<Integer, Map<Integer, String>> extraData = new HashMap<>();

                if (valueData != null && valueData.length > 0) {
                    int pqrId = 0;
                    Map<Integer, String> innerMap = null;
                    for (int i = 0; i < valueData.length; i++) {
                        Object[] obj = valueData[i];
                        int curPqrId = MySQLQuery.getAsInteger(obj[0]);

                        if (pqrId != curPqrId) {
                            pqrId = curPqrId;
                            innerMap = new HashMap<>();
                        }

                        if (pqrId == curPqrId) {
                            innerMap.put(MySQLQuery.getAsInteger(obj[1]), MySQLQuery.getAsString(obj[2]));
                        }

                        if (i == valueData.length - 1 || pqrId != MySQLQuery.getAsInteger(valueData[i + 1][0])) {
                            extraData.put(MySQLQuery.getAsInteger(obj[0]), innerMap);
                        }
                    }
                }

                int xLength = records[0].length - 1;//se resta el pqr_id
                Object[][] newData = new Object[records.length][xLength + colsToReport.length];

                for (int i = 0; i < newData.length; i++) {
                    int pqrId = MySQLQuery.getAsInteger(records[i][0]);
                    for (int j = 0; j < xLength; j++) {
                        newData[i][j] = records[i][j + 1];
                    }
                    for (int j = 0; j < colsToReport.length; j++) {
                        int typeId = MySQLQuery.getAsInteger(colsToReport[j][1]);
                        Map<Integer, String> colsData = extraData.get(pqrId);
                        if (colsData != null) {
                            newData[i][xLength + j] = colsData.get(typeId);
                        } else {
                            newData[i][xLength + j] = null;
                        }
                    }
                }

                tbl.setData(newData);
                rep.getTables().add(tbl);

            } else {
                tbl.setData(records);
                rep.getTables().add(tbl);
            }
        }
        return rep;

    }

}
