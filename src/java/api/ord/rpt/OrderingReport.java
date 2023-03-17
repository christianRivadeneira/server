package api.ord.rpt;

import api.GridResult;
import api.MySQLCol;
import api.ord.model.OrdCfg;
import api.ord.model.OrdPollOption;
import api.ord.model.OrdPollQuestion;
import api.ord.model.OrdPollVersion;
import api.ord.model.OrdPqrAnulCause;
import api.sys.model.Enterprise;
import api.sys.model.Sector;
import api.trk.model.CylinderType;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import utilities.Dates;
import utilities.MySQLQuery;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.HeaderColumn;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.SummaryRow;
import utilities.mysqlReport.Table;
import utilities.mysqlReport.TableHeader;

public class OrderingReport {

    private static final int CYLS_WIDTH = 10;
    private static final int ORDS_WIDTH = 15;
    private static final int GALS_WIDTH = 15;

    public static int getWinner(int officeId, Date drawDate, Date begin, Date end, String type, Connection con) throws Exception {
        if (new MySQLQuery("SELECT count(*) FROM ord_office WHERE id = " + officeId + " AND draw = 1").getAsInteger(con) == 0) {
            throw new Exception("La opción de sorteo no está habilitada para la oficina.");
        }

        MySQLQuery q = new MySQLQuery("SELECT o.id "
                + "FROM "
                + (type.equals("c") ? "ord_cyl_order AS o " : "ord_tank_order AS o ")
                + "WHERE "
                + "o.office_id = " + officeId + " "
                + "AND o.cancelled_by IS NULL "
                + "AND o.cancel_cause_id IS NULL "
                + "AND o.confirmed_by_id IS NOT NULL "
                + "AND o.confirm_hour IS NOT NULL "
                + "AND o.day BETWEEN ?1 AND ?2 "
                + "ORDER BY RAND() LIMIT 1").setParam(1, begin).setParam(2, end).setParam(3, drawDate);

        Integer ordId = q.getAsInteger(con);

        if (ordId == null) {
            throw new Exception("No hay pedidos para sortear en el periodo escogido");
        }

        q = new MySQLQuery("INSERT INTO ord_winner SET " + (type.equals("c") ? "order_id = " : "tank_ord_id = ") + ordId + ", draw_date = ?1, office_id = " + officeId).setParam(1, drawDate);
        q.executeInsert(con);
        return ordId;
    }

    public static GridResult univIndexDocumentLookup(Integer cityId, String document, Connection con) throws Exception {
        GridResult tbl = new GridResult();
        if (document == null) {
            throw new IllegalArgumentException("Ingrese algún parámetro.");
        }
        String where = "i.document = \"" + document + "\"";
        tbl.sortColIndex = 1;
        tbl.sortType = GridResult.SORT_DESC;
        tbl.data = new MySQLQuery("SELECT i.id, "
                + "i.document, "
                + "CONCAT(i.first_name, \" \", i.last_name),"
                + (cityId != null ? "CONCAT(i.address, IF(n.`name` IS NOT NULL, CONCAT(' ', n.`name`), ''))," : "CONCAT(i.address, IF(n.`name` IS NOT NULL, CONCAT(' ', n.`name`), ''), ' [', c.name, ']'),")
                + "i.phones, "
                + "'Provisional' "
                + "FROM "
                + "ord_contract_index AS i "
                + "LEFT JOIN neigh AS n ON n.id = i.neigh_id "
                + "LEFT JOIN city c ON c.id = i.city_id  "
                + "WHERE " + where + " AND i.type = 'univ' AND i.active = 1 "
                + (cityId != null ? " AND i.city_id = " + cityId : " ")).getRecords(con);;
        tbl.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_KEY),
            new MySQLCol(MySQLCol.TYPE_TEXT, 75, "Documento"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 110, "Nombres"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 130, "Dirección"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 110, "Teléfonos"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 90, "Tipo")
        };
        return tbl;
    }

    //CILINDROS
    public static MySQLReport getPqrsCylReport(boolean punctual, int officeId, Date date, Integer entId, String typeCli, Connection em) throws Exception {
        OrdCfg cfg = new OrdCfg().select(1, em);

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat df1 = new SimpleDateFormat("MMMM yyyy");
        //calculos con fechas
        Date[][] dates = Dates.getDateList(date);
        Date curBeg = dates[0][0];
        Date curEnd = dates[0][1];
        Date lmBeg = dates[1][0];
        Date lmEnd = dates[1][1];
        Date lyBeg = dates[2][0];
        Date lyEnd = dates[2][1];
        double day = Dates.getDayOfMonth(date);
        double maxDay = Dates.getMaxDayOfMonth(date);
        //composición del filtro ppal
        String filt = "?2 <= pqr.creation_date AND ?3 >= pqr.creation_date AND pqr.office_id = " + officeId + " "
                + (entId != null ? " AND IF(pqr.enterprise_id IS NOT NULL, pqr.enterprise_id = " + entId + ",TRUE) " : "");
        if (typeCli != null) {
            filt += " AND i.type = '" + typeCli + "' ";
        }

        MySQLQuery rowsQ;
        String title;
        String colName;
        rowsQ = new MySQLQuery("SELECT DISTINCT "
                + "pqr.technician_id, CONCAT(t.first_name, \" \", t.last_name) "//0
                + "FROM "
                + "ord_pqr_cyl pqr "
                + "INNER JOIN ord_contract_index as i ON pqr.index_id=i.id "
                + "INNER JOIN ord_technician as t ON pqr.technician_id=t.id "
                + "WHERE " + filt);
        filt += " AND pqr.technician_id = ?1 ";
        title = "PQRs Fugas Cilindros";
        colName = "Técnicos";

        MySQLQuery timesQ = new MySQLQuery("SELECT "
                + "CONVERT(AVG(TIME_TO_SEC(attention_hour) - TIME_TO_SEC(regist_hour))/60, UNSIGNED) "//0
                + "FROM "
                + "ord_pqr_cyl as pqr "
                + "INNER JOIN ord_contract_index as i ON pqr.index_id=i.id "
                + "WHERE pqr.pqr_anul_cause_id IS NULL AND pqr.attention_hour IS NOT NULL AND "
                + "creation_date = attention_date AND " + filt);

        MySQLQuery pqrsQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_pqr_cyl pqr "
                + "INNER JOIN ord_contract_index as i ON pqr.index_id=i.id "
                + "WHERE pqr.pqr_anul_cause_id IS NULL AND pqr.attention_hour IS NOT NULL AND " + filt);

        MySQLQuery inTimeQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_pqr_cyl pqr "
                + "INNER JOIN ord_contract_index as i ON pqr.index_id=i.id "
                + "WHERE pqr.pqr_anul_cause_id IS NULL AND pqr.attention_hour IS NOT NULL "
                + "AND IF(creation_date = attention_date,(TIME_TO_SEC(pqr.attention_hour) - TIME_TO_SEC(pqr.regist_hour) <= " + (cfg.cylPqrLimitTime * 60) + "),true) AND "
                + filt);

        MySQLQuery unatQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_pqr_cyl pqr "
                + "INNER JOIN ord_contract_index as i ON pqr.index_id=i.id "
                + "WHERE pqr.pqr_anul_cause_id is NULL AND pqr.attention_hour IS NULL AND " + filt);

        MySQLQuery cancelQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_pqr_cyl pqr "
                + "INNER JOIN ord_contract_index as i ON pqr.index_id=i.id "
                + "WHERE pqr.pqr_anul_cause_id IS NOT NULL AND " + filt);

        MySQLQuery polledQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_pqr_cyl pqr "
                + "INNER JOIN ord_contract_index as i ON pqr.index_id=i.id "
                + "WHERE  pqr.pqr_anul_cause_id  IS NULL AND  pqr.attention_hour IS NOT NULL AND pqr.pqr_poll_id IS NOT NULL AND " + filt);

        rowsQ.setParam(2, punctual ? curEnd : curBeg);
        rowsQ.setParam(3, curEnd);

        Object[][] rows = rowsQ.getRecords(em);

        //REPORTE
        MySQLReport rep = new MySQLReport(title, (punctual ? "Solo de: " + df.format(date) : "Acumulado: " + df.format(curBeg) + " - " + df.format(curEnd)), "Hoja 1", MySQLQuery.now(em));
        rep.setVerticalFreeze(0);
        rep.setHorizontalFreeze(2);
        rep.setZoomFactor(80);
        //Subtitulos  
        rep.getSubTitles().add(("Empresa: " + (entId != null ? new Enterprise().select(entId, em).name : "Todas")) + "Oficina: " + new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(em));
        rep.getSubTitles().add("Tipo Contrato: " + (typeCli != null ? (typeCli.equals("brand") ? "Afiliado" : (typeCli.equals("app") ? "App" : "Provisional")) : "Todos"));

        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.00"));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        //Columnas

        List<Column> cols = new ArrayList<>();
        cols.add(new Column("", 30, 2));//name
        cols.add(new Column("", 9, 0));//time
        cols.add(new Column("Cancelados", 14, 0));
        cols.add(new Column("Encuestados", 14, 0));
        cols.add(new Column("No Atend.", 14, 0));
        cols.add(new Column("Atendidos", 14, 0));
        cols.add(new Column("A tiempo", 14, 0));
        cols.add(new Column("PQRs", 25, 0));
        cols.add(new Column("PQRs", 16, 0));
        cols.add(new Column("PQRs", 16, 0));
        Table tbl = new Table("PQRs Fugas Cilindros " + (punctual ? df.format(date) : df.format(curBeg) + " - " + df.format(curEnd)));
        tbl.setColumns(cols);
        //Cabecera
        TableHeader header = new TableHeader();
        tbl.getHeaders().add(header);
        header.getColums().add(new HeaderColumn(colName, 1, 2));
        header.getColums().add(new HeaderColumn("Tiempo ", 1, 2));
        header.getColums().add(new HeaderColumn("PQRs", 5, 1));
        header.getColums().add(new HeaderColumn("Proyec. " + df1.format(curBeg), 1, 1));
        header.getColums().add(new HeaderColumn(df1.format(lmBeg), 1, 1));
        header.getColums().add(new HeaderColumn(df1.format(lyBeg), 1, 1));
        //Totales
        tbl.setSummaryRow(new SummaryRow("Totales", 2));

        //fin config
        for (int i = 0; i < rows.length; i++) {
            Object[] row = new Object[10];
            tbl.addRow(row);
            int rowId = (Integer) rows[i][0];
            String rowName = rows[i][1].toString();
            pqrsQ.setParam(1, rowId);
            inTimeQ.setParam(1, rowId);
            cancelQ.setParam(1, rowId);
            polledQ.setParam(1, rowId);
            unatQ.setParam(1, rowId);
            timesQ.setParam(1, rowId);

            //PEDIDOS
            //tiempo de entrega
            timesQ.setParam(2, punctual ? curEnd : curBeg);
            timesQ.setParam(3, curEnd);
            Integer curTime = timesQ.getAsInteger(em);

            //cancelados actual
            cancelQ.setParam(2, punctual ? curEnd : curBeg);
            cancelQ.setParam(3, curEnd);
            Long curCancel = cancelQ.getAsLong(em);

            //encuestados actual
            polledQ.setParam(2, punctual ? curEnd : curBeg);
            polledQ.setParam(3, curEnd);
            Long curPolled = polledQ.getAsLong(em);

            //no atendidos mes actual
            unatQ.setParam(2, punctual ? curEnd : curBeg);
            unatQ.setParam(3, curEnd);
            Long curUnat = unatQ.getAsLong(em);

            //pqrs mes actual
            pqrsQ.setParam(2, punctual ? curEnd : curBeg);
            pqrsQ.setParam(3, curEnd);
            Long curPed = pqrsQ.getAsLong(em);

            //a tiempo
            inTimeQ.setParam(2, punctual ? curEnd : curBeg);
            inTimeQ.setParam(3, curEnd);
            Long inTime = inTimeQ.getAsLong(em);

            //pqrs mes anterior
            pqrsQ.setParam(2, lmBeg);
            pqrsQ.setParam(3, lmEnd);
            Long lmPed = pqrsQ.getAsLong(em);

            //pqrs año anterior
            pqrsQ.setParam(2, lyBeg);
            pqrsQ.setParam(3, lyEnd);
            Long lyPed = pqrsQ.getAsLong(em);

            //proyección
            pqrsQ.setParam(2, curBeg);
            pqrsQ.setParam(3, curEnd);
            Long proyPed = (long) ((pqrsQ.getAsLong(em) / day) * maxDay);

            //ASIGNACIÓN
            row[0] = rowName;
            row[1] = curTime;//tiempo;
            row[2] = curCancel;//cancelados
            row[3] = curPolled;//encuestados
            row[4] = curUnat;//no atendidos
            row[5] = curPed;//Atendidos
            row[6] = inTime;//a tipo
            row[7] = proyPed;//Pedidos;
            row[8] = lmPed;//mes pasado Pedidos;
            row[9] = lyPed;//año pasado Pedidos;
        }
        if (tbl.getData() != null && tbl.getData().length > 0) {
            rep.getTables().add(tbl);
        }
        return rep;
    }

    public static MySQLReport getCylPQRPollingReport(int pollVersionId, boolean punctual, int officeId, Date date, String typeCli, Connection em) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date[][] dates = Dates.getDateList(date);
        Date curBeg = dates[0][0];
        Date curEnd = dates[0][1];
        String sb = punctual ? "Solo de: " + df.format(date) : "Acumulado: " + df.format(curBeg) + " - " + df.format(curEnd);
        return getCylPQRPollingReport(pollVersionId, officeId, dates, typeCli, sb, em);
    }

    public static MySQLReport getCylPQRPollingMonthlyReport(int pollVersionId, int officeId, int begYear, int endYear, int begMonth, int endMonth, String typeCli, Connection em) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date[][] dates = Dates.getDateList(begYear, endYear, begMonth, endMonth);
        String dateRange = df.format(dates[0][0]) + " - " + df.format(dates[dates.length - 1][1]);
        return getCylPQRPollingReport(pollVersionId, officeId, dates, typeCli, dateRange, em);
    }

    private static MySQLReport getCylPQRPollingReport(int pollVersionId, int officeId, Date[][] dates, String typeCli, String subTitle, Connection em) throws Exception {
        String str = "SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_poll AS p "
                + "INNER JOIN ord_pqr_cyl AS pq ON pq.pqr_poll_id = p.id "
                + "INNER JOIN ord_contract_index AS ind ON pq.index_id = ind.id "
                + "INNER JOIN ord_office AS of ON of.id = pq.office_id "
                + "WHERE "
                + " SUBSTR(p.answer FROM ?1 FOR 1) = ?2 AND "
                + " p.poll_version_id = " + pollVersionId + " AND "
                + "?3 <= pq.`creation_date` AND ?4 >= pq.`creation_date` AND pq.office_id = " + officeId + " "
                + "AND pq.enterprise_id = ?5 ";
        if (typeCli != null) {
            str += " AND ind.type = '" + typeCli + "' ";
        }
        MySQLQuery q = new MySQLQuery(str);
        MySQLReport rep = new MySQLReport("PQRs Fugas Cilindros", subTitle, "Hoja 1", MySQLQuery.now(em));
        rep.getSubTitles().add("Oficina: " + new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(em));
        rep.getSubTitles().add("Tipo Contrato: " + (typeCli != null ? (typeCli.equals("brand") ? "Afiliado" : (typeCli.equals("app") ? "App" : "Provisional")) : "Todos"));
        polling(q, pollVersionId, rep, dates, em);
        return rep;
    }

    protected static void polling(MySQLQuery q, int pollVersionId, MySQLReport rep, Date date, Connection em) throws Exception {
        //calculos con fechas
        GregorianCalendar curEndGc = new GregorianCalendar();
        curEndGc.setTime(date);
        GregorianCalendar curBegGc = new GregorianCalendar();
        curBegGc.setTime(date);
        curBegGc.set(GregorianCalendar.DAY_OF_MONTH, 1);

        int year = curEndGc.get(GregorianCalendar.YEAR);
        int month = curEndGc.get(GregorianCalendar.MONTH);

        GregorianCalendar lmBegGc = new GregorianCalendar(month > 0 ? year : year - 1, month > 0 ? month - 1 : 11, 1);
        GregorianCalendar lmEndGc = new GregorianCalendar(month > 0 ? year : year - 1, month > 0 ? month - 1 : 11, lmBegGc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));

        GregorianCalendar lyBegGc = new GregorianCalendar(year - 1, month, 1);
        GregorianCalendar lyEndGc = new GregorianCalendar(year - 1, month, lyBegGc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));

        Date[][] dates = new Date[3][];
        dates[0] = new Date[2];
        dates[0][0] = Dates.trimDate(curBegGc.getTime());//curBeg
        dates[0][1] = Dates.trimDate(curEndGc.getTime());//curEnd
        dates[1] = new Date[2];
        dates[1][0] = Dates.trimDate(lmBegGc.getTime());//lmBegGc
        dates[1][1] = Dates.trimDate(lmEndGc.getTime());//lmEndGc
        dates[2] = new Date[2];
        dates[2][0] = Dates.trimDate(lyBegGc.getTime());//lyBegGc
        dates[2][1] = Dates.trimDate(lyEndGc.getTime());//lyEndGc
        polling(q, pollVersionId, rep, dates, em);
    }

    protected static void polling(MySQLQuery q, int pollVersionId, MySQLReport rep, Date begDate, Date endDate, Connection em) throws Exception {
        GregorianCalendar begGc = new GregorianCalendar();
        begGc.setTime(begDate);
        GregorianCalendar endGc = new GregorianCalendar();
        endGc.setTime(endDate);
        Date[][] dates = Dates.getDateList(begGc.get(GregorianCalendar.YEAR), endGc.get(GregorianCalendar.YEAR), begGc.get(GregorianCalendar.MONTH), endGc.get(GregorianCalendar.MONTH));
        polling(q, pollVersionId, rep, dates, em);
    }

    protected static void polling(MySQLQuery q, int pollVersionId, MySQLReport rep, Date[][] dates, Connection em) throws Exception {

        List<Enterprise> ents = Enterprise.getAll(em);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat df1 = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
        rep.setVerticalFreeze(0);
        rep.setHorizontalFreeze(0);
        rep.setZoomFactor(80);
        rep.getSubTitles().add("Versión: " + df.format(new OrdPollVersion().select(pollVersionId, em).since));

        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//1
        //Columnas
        List<Column> cols = new ArrayList<>();
        cols.add(new Column("Opción", 48, 0));
        for (Date[] date : dates) {
            for (Enterprise ent : ents) {
                cols.add(new Column(ent.shortName, 8, 1));
            }
            cols.add(new Column("Total", 10, 1));
        }

        TableHeader head = new TableHeader();
        head.getColums().add(new HeaderColumn("Opción", 1, 2));
        for (Date[] date : dates) {
            head.getColums().add(new HeaderColumn(df1.format(date[0]), ents.size() + 1, 1));
        }

        Table tblMod = new Table("");
        tblMod.setColumns(cols);
        tblMod.getHeaders().add(head);
        tblMod.setSummaryRow(new SummaryRow("Totales", 1));
        //Fin config
        int curPos = 0;//posición en la cadena de respuesta
        List<OrdPollQuestion> result1 = OrdPollQuestion.getPollQuestionsByVersion(pollVersionId, em);
        for (int i = 0; i < result1.size(); i++) {
            OrdPollQuestion ques = result1.get(i);
            List<OrdPollOption> resOpts = OrdPollOption.getListQuestionsId(ques.id + "", em);
            Table tbl = new Table(tblMod);
            tbl.setTitle((i + 1) + ". " + ques.text);

            if (!ques.multiple) {
                for (int j = 0; j < resOpts.size(); j++) {
                    OrdPollOption opt = resOpts.get(j);
                    List<Object> row = new ArrayList<>();
                    //row.add(j + 1);
                    row.add(opt.text);
                    q.setParam(1, curPos + 1);
                    q.setParam(2, j + 1);
                    for (Date[] date : dates) {
                        long total = 0;
                        q.setParam(3, date[0]);
                        q.setParam(4, date[1]);
                        for (Enterprise ent : ents) {
                            q.setParam(5, ent.id);
                            Long lc = q.getAsLong(em);
                            row.add(lc);
                            total += lc;
                        }
                        row.add(total);
                    }
                    tbl.addRow(row.toArray());
                }
                curPos++;
            } else {
                for (OrdPollOption opt : resOpts) {
                    q.setParam(1, curPos + 1);
                    q.setParam(2, 1);

                    List<Object> row = new ArrayList<>();
                    row.add(opt.text);
                    for (Date[] date : dates) {
                        q.setParam(3, date[0]);
                        q.setParam(4, date[1]);
                        long total = 0;
                        for (Enterprise ent : ents) {
                            q.setParam(5, ent.id);
                            Long lc = q.getAsLong(em);
                            row.add(lc);
                            total += lc;
                        }
                        row.add(total);
                    }
                    tbl.addRow(row.toArray());
                    curPos++;
                }
            }
            if (tbl.getData() != null && tbl.getData().length > 0) {
                rep.getTables().add(tbl);
            }
        }
    }

    public static MySQLReport getCylPQRSatisPollingReport(int pollVersionId, boolean punctual, int officeId, Date date, String typeCli, Connection em) throws Exception {
        String str = "SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_poll AS p "
                + "INNER JOIN ord_pqr_cyl AS pq ON pq.satis_poll_id = p.id "
                + "INNER JOIN ord_contract_index AS ind ON pq.index_id = ind.id "
                + "INNER JOIN ord_office AS of ON of.id = pq.office_id "
                + "WHERE "
                + " SUBSTR(p.answer FROM ?1 FOR 1) = ?2 AND "
                + " p.poll_version_id = " + pollVersionId + " AND "
                + "?3 <= pq.`creation_date` AND ?4 >= pq.`creation_date` AND pq.office_id = " + officeId + " "
                + "AND pq.enterprise_id = ?5 ";
        if (typeCli != null) {
            str += " AND ind.type = '" + typeCli + "' ";
        }

        MySQLQuery q = new MySQLQuery(str);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        //calculos con fechas
        Date[][] dates = Dates.getDateList(date);
        Date curBeg = dates[0][0];
        Date curEnd = dates[0][1];

        MySQLReport rep = new MySQLReport("Encuestas de Satisfacción PQRs Fugas Cilindros", (punctual ? "Solo de: " + df.format(date) : "Acumulado: " + df.format(curBeg) + " - " + df.format(curEnd)), "Hoja 1", MySQLQuery.now(em));
        rep.getSubTitles().add("Oficina: " + new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(em));
        rep.getSubTitles().add("Tipo Contrato: " + (typeCli != null ? (typeCli.equals("brand") ? "Afiliado" : (typeCli.equals("app") ? "App" : "Provisional")) : "Todos"));
        polling(q, pollVersionId, rep, date, em);
        return rep;
    }

    public static MySQLReport getCylPQRSatisOtherReport(int pollVersionId, boolean punctual, int officeId, Date date, String typeCli, Boolean cils, Connection em) throws Exception {
        String str = "SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_poll AS p "
                + "INNER JOIN ord_pqr_other AS pq ON pq.satis_poll_id = p.id "
                + "INNER JOIN ord_office AS of ON of.id = pq.office_id "
                + "LEFT JOIN ord_contract_index AS ind ON pq.index_id = ind.id "
                + "WHERE "
                + " SUBSTR(p.answer FROM ?1 FOR 1) = ?2 AND "
                + " p.poll_version_id = " + pollVersionId + " AND "
                + "?3 <= pq.`regist_date` AND ?4 >= pq.`regist_date`AND pq.office_id = " + officeId + " "
                + "AND pq.enterprise_id = ?5 ";
        if (typeCli != null) {
            str += " AND ind.type = '" + typeCli + "' ";
        }
        if (cils != null) {
            str += (cils ? " AND pq.index_id IS NOT NULL " : " AND pq.index_id IS NULL ");
        }
        MySQLQuery q = new MySQLQuery(str);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        //calculos con fechas
        Date[][] dates = Dates.getDateList(date);
        Date curBeg = dates[0][0];
        Date curEnd = dates[0][1];

        MySQLQuery qLabel = new MySQLQuery("SELECT l.plural FROM sys_label l WHERE l.name = '{other}' ");
        String lblPqrOther = qLabel.getAsString(em);
        lblPqrOther = (lblPqrOther != null ? lblPqrOther : "PQR Otros");

        MySQLReport rep = new MySQLReport("Encuestas de Satisfacción " + lblPqrOther, (punctual ? "Solo de: " + df.format(date) : "Acumulado: " + df.format(curBeg) + " - " + df.format(curEnd)), "Hoja 1", MySQLQuery.now(em));
        rep.getSubTitles().add("Oficina: " + new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(em));
        rep.getSubTitles().add("Tipo Contrato: " + (typeCli != null ? (typeCli.equals("brand") ? "Afiliado" : (typeCli.equals("app") ? "App" : "Provisional")) : "Todos"));
        polling(q, pollVersionId, rep, date, em);
        return rep;
    }

    public static MySQLReport getPQRComSatisReport(int pollVersionId, boolean punctual, int officeId, Date date, String typeCli, Boolean cils, Connection em) throws Exception {
        String str = "SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_poll AS p "
                + "INNER JOIN ord_pqr_com AS pq ON pq.satis_poll_id = p.id "
                + "INNER JOIN ord_office AS of ON of.id = pq.office_id "
                + "LEFT JOIN ord_contract_index AS ind ON pq.index_id = ind.id "
                + "WHERE "
                + " SUBSTR(p.answer FROM ?1 FOR 1) = ?2 AND "
                + " p.poll_version_id = " + pollVersionId + " AND "
                + "?3 <= pq.`regist_date` AND ?4 >= pq.`regist_date`AND pq.office_id = " + officeId + " "
                + "AND pq.enterprise_id = ?5 ";
        if (typeCli != null) {
            str += " AND ind.type = '" + typeCli + "' ";
        }
        if (cils != null) {
            str += (cils ? " AND pq.index_id IS NOT NULL " : " AND pq.index_id IS NULL ");
        }
        MySQLQuery q = new MySQLQuery(str);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        //calculos con fechas
        Date[][] dates = Dates.getDateList(date);
        Date curBeg = dates[0][0];
        Date curEnd = dates[0][1];

        MySQLQuery qLabel = new MySQLQuery("SELECT l.plural FROM sys_label l WHERE l.name = '{other}' ");
        String lblPqrOther = qLabel.getAsString(em);
        lblPqrOther = (lblPqrOther != null ? lblPqrOther : "PQR Otros");

        MySQLReport rep = new MySQLReport("Encuestas de Satisfacción " + lblPqrOther, (punctual ? "Solo de: " + df.format(date) : "Acumulado: " + df.format(curBeg) + " - " + df.format(curEnd)), "Hoja 1", MySQLQuery.now(em));
        rep.getSubTitles().add("Oficina: " + new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(em));
        rep.getSubTitles().add("Tipo Contrato: " + (typeCli != null ? (typeCli.equals("brand") ? "Afiliado" : (typeCli.equals("app") ? "App" : "Provisional")) : "Todos"));
        polling(q, pollVersionId, rep, date, em);
        return rep;
    }

    public static MySQLReport getCylPQRCancelReport(boolean punctual, int officeId, Date date, String typeCli, Connection em) throws Exception {
        String str = "SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_pqr_cyl AS pq "
                + "INNER JOIN ord_office AS of ON of.id = pq.office_id "
                + "INNER JOIN ord_contract_index AS ind ON ind.id=index_id "
                + "WHERE "
                + "pq.pqr_anul_cause_id = ?1 "
                + "AND ?2 <= pq.`creation_date` AND ?3 >= pq.`creation_date` AND pq.office_id = " + officeId + " "
                + "AND pq.enterprise_id = ?4 ";
        if (typeCli != null) {
            str += " AND ind.type = '" + typeCli + "' ";
        }

        MySQLQuery q = new MySQLQuery(str);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        //calculos con fechas
        Date[][] dates = Dates.getDateList(date);
        Date curBeg = dates[0][0];
        Date curEnd = dates[0][1];

        MySQLReport rep = new MySQLReport("PQRs Fugas Cilindros Canceladas ", (punctual ? "Solo de: " + df.format(date) : "Acumulado: " + df.format(curBeg) + " - " + df.format(curEnd)), "Hoja 1", MySQLQuery.now(em));
        rep.getSubTitles().add("Oficina: " + new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(em));
        rep.getSubTitles().add("Tipo Contrato: " + (typeCli != null ? (typeCli.equals("brand") ? "Afiliado" : (typeCli.equals("app") ? "App" : "Provisional")) : "Todos"));
        List<Enterprise> ents = Enterprise.getAll(em);

        SimpleDateFormat df1 = new SimpleDateFormat("MMMM yyyy");
        rep.setVerticalFreeze(0);
        rep.setHorizontalFreeze(0);
        rep.setZoomFactor(80);

        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//1
        //Columnas
        List<Column> cols = new ArrayList<>();
        cols.add(new Column("Opción", 48, 0));

        for (int j = 0; j < 3; j++) {
            for (Enterprise ent : ents) {
                cols.add(new Column(ent.shortName, 8, 1));
            }
            cols.add(new Column("Total", 10, 1));
        }

        TableHeader head = new TableHeader();
        head.getColums().add(new HeaderColumn("Opción", 1, 2));
        for (Date[] date1 : dates) {
            head.getColums().add(new HeaderColumn(df1.format(date1[0]), ents.size() + 1, 1));
        }

        Table tbl = new Table("");
        tbl.setColumns(cols);
        tbl.getHeaders().add(head);

        List<OrdPqrAnulCause> causes = OrdPqrAnulCause.getPqrAnulCauses("cyl", em);
        for (OrdPqrAnulCause cause : causes) {
            List<Object> row = new ArrayList<>();
            row.add(cause.description);
            q.setParam(1, cause.id);
            for (Date[] date1 : dates) {
                long total = 0;
                q.setParam(2, date1[0]);
                q.setParam(3, date1[1]);
                for (Enterprise ent : ents) {
                    q.setParam(4, ent.id);
                    Long lc = q.getAsLong(em);
                    row.add(lc);
                    total += lc;
                }
                row.add(total);
            }
            tbl.addRow(row.toArray());
        }

        if (tbl.getData() != null && tbl.getData().length > 0) {
            rep.getTables().add(tbl);
        }
        return rep;
    }

    // TANQUES
    public static MySQLReport getTankPqrReport(boolean punctual, int officeId, Date date, Integer entId, Connection em) throws Exception {
        OrdCfg cfg = new OrdCfg().select(1, em);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat df1 = new SimpleDateFormat("MMMM yyyy");
        //calculos con fechas
        Date[][] dates = Dates.getDateList(date);
        Date curBeg = dates[0][0];
        Date curEnd = dates[0][1];
        Date lmBeg = dates[1][0];
        Date lmEnd = dates[1][1];
        Date lyBeg = dates[2][0];
        Date lyEnd = dates[2][1];
        double day = Dates.getDayOfMonth(date);
        double maxDay = Dates.getMaxDayOfMonth(date);

        //composición del filtro ppal
        String filt = "?2 <= pqr.regist_date AND ?3 >= pqr.regist_date AND pqr.office_id = " + officeId + " "
                + (entId != null ? " AND IF(pqr.enterprise_id IS NOT NULL, pqr.enterprise_id = " + entId + ",TRUE) " : "");

        MySQLQuery rowsQ;
        String title;
        String colName;
        rowsQ = new MySQLQuery("SELECT DISTINCT "
                + "pqr.technician_id, CONCAT(t.first_name, \" \", t.last_name) "//0
                + "FROM "
                + "ord_pqr_tank pqr "
                + "INNER JOIN ord_technician as t ON pqr.technician_id = t.id "
                + "INNER JOIN ord_office AS of ON of.id = pqr.office_id "
                + "WHERE " + filt);
        filt += " AND pqr.technician_id = ?1 ";
        title = "PQRs Fugas Estacionarios";
        colName = "Técnicos";

        MySQLQuery timesQ = new MySQLQuery("SELECT "
                + "AVG(TIME_TO_SEC(attention_hour) - TIME_TO_SEC(regist_hour))/60 "//0
                + "FROM "
                + "ord_pqr_tank as pqr "
                + "INNER JOIN ord_office AS of ON of.id = pqr.office_id "
                + "WHERE pqr.anul_cause_id IS NULL AND pqr.attention_hour IS NOT NULL AND "
                + "regist_date = attention_date AND " + filt);

        MySQLQuery pqrsQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_pqr_tank pqr "
                + "INNER JOIN ord_office AS of ON of.id = pqr.office_id "
                + "WHERE pqr.anul_cause_id IS NULL AND pqr.attention_hour IS NOT NULL AND " + filt);

        MySQLQuery inTimeQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_pqr_tank pqr "
                + "INNER JOIN ord_office AS of ON of.id = pqr.office_id "
                + "WHERE pqr.anul_cause_id IS NULL AND pqr.attention_hour IS NOT NULL "
                + "AND IF(regist_date = attention_date,(TIME_TO_SEC(pqr.attention_hour) - TIME_TO_SEC(pqr.regist_hour) <= " + (cfg.cylPqrLimitTime * 60) + "),true) AND "
                + filt);

        MySQLQuery unatQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_pqr_tank pqr "
                + "INNER JOIN ord_office AS of ON of.id = pqr.office_id "
                + "WHERE pqr.anul_cause_id is NULL AND pqr.attention_hour IS NULL AND " + filt);

        MySQLQuery cancelQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_pqr_tank pqr "
                + "INNER JOIN ord_office AS of ON of.id = pqr.office_id "
                + "WHERE pqr.anul_cause_id IS NOT NULL AND " + filt);

        MySQLQuery polledQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_pqr_tank pqr "
                + "INNER JOIN ord_office AS of ON of.id = pqr.office_id "
                + "WHERE  pqr.anul_cause_id  IS NULL AND  pqr.attention_hour IS NOT NULL AND pqr.pqr_poll_id IS NOT NULL AND " + filt);

        rowsQ.setParam(2, punctual ? curEnd : curBeg);
        rowsQ.setParam(3, curEnd);

        Object[][] rows = rowsQ.getRecords(em);

        //REPORTE
        MySQLReport rep = new MySQLReport(title, (punctual ? "Solo de: " + df.format(date) : "Acumulado: " + df.format(curBeg) + " - " + df.format(curEnd)), "Hoja 1", MySQLQuery.now(em));
        rep.setVerticalFreeze(0);
        rep.setHorizontalFreeze(2);
        rep.setZoomFactor(80);
        //Subtitulos
        rep.getSubTitles().add(("Empresa: " + (entId != null ? new Enterprise().select(entId, em).name : "Todas")) + "Oficina: " + new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(em));

        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.00"));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        //Columnas

        List<Column> cols = new ArrayList<>();
        cols.add(new Column("", 30, 2));//name
        cols.add(new Column("", 9, 0));//time
        cols.add(new Column("Cancelados", 14, 0));
        cols.add(new Column("Encuestados", 14, 0));
        cols.add(new Column("No Atend.", 14, 0));
        cols.add(new Column("Atendidos", 14, 0));
        cols.add(new Column("A tiempo", 14, 0));
        cols.add(new Column("PQRs", 25, 0));
        cols.add(new Column("PQRs", 16, 0));
        cols.add(new Column("PQRs", 16, 0));
        Table tbl = new Table("PQRs Fugas Estacionarios " + (punctual ? df.format(date) : df.format(curBeg) + " - " + df.format(curEnd)));
        tbl.setColumns(cols);
        //Cabecera
        TableHeader header = new TableHeader();
        tbl.getHeaders().add(header);
        header.getColums().add(new HeaderColumn(colName, 1, 2));
        header.getColums().add(new HeaderColumn("Tiempo ", 1, 2));
        header.getColums().add(new HeaderColumn("PQRs", 5, 1));
        header.getColums().add(new HeaderColumn("Proyec. " + df1.format(curBeg), 1, 1));
        header.getColums().add(new HeaderColumn(df1.format(lmBeg), 1, 1));
        header.getColums().add(new HeaderColumn(df1.format(lyBeg), 1, 1));
        //Totales
        tbl.setSummaryRow(new SummaryRow("Totales", 2));

        //fin config
        for (int i = 0; i < rows.length; i++) {
            Object[] row = new Object[10];
            tbl.addRow(row);
            int rowId = (Integer) rows[i][0];
            String rowName = rows[i][1].toString();
            pqrsQ.setParam(1, rowId);
            inTimeQ.setParam(1, rowId);
            cancelQ.setParam(1, rowId);
            polledQ.setParam(1, rowId);
            unatQ.setParam(1, rowId);
            timesQ.setParam(1, rowId);

            //PEDIDOS
            //tiempo de entrega
            timesQ.setParam(2, punctual ? curEnd : curBeg);
            timesQ.setParam(3, curEnd);
            BigDecimal curTime = timesQ.getAsBigDecimal(em, true);

            //cancelados actual
            cancelQ.setParam(2, punctual ? curEnd : curBeg);
            cancelQ.setParam(3, curEnd);
            Long curCancel = cancelQ.getAsLong(em);

            //encuestados actual
            polledQ.setParam(2, punctual ? curEnd : curBeg);
            polledQ.setParam(3, curEnd);
            Long curPolled = polledQ.getAsLong(em);

            //no atendidos mes actual
            unatQ.setParam(2, punctual ? curEnd : curBeg);
            unatQ.setParam(3, curEnd);
            Long curUnat = unatQ.getAsLong(em);

            //pqrs mes actual
            pqrsQ.setParam(2, punctual ? curEnd : curBeg);
            pqrsQ.setParam(3, curEnd);
            Long curPed = pqrsQ.getAsLong(em);

            //a tiempo
            inTimeQ.setParam(2, punctual ? curEnd : curBeg);
            inTimeQ.setParam(3, curEnd);
            Long inTime = inTimeQ.getAsLong(em);

            //pqrs mes anterior
            pqrsQ.setParam(2, lmBeg);
            pqrsQ.setParam(3, lmEnd);
            Long lmPed = pqrsQ.getAsLong(em);

            //pqrs año anterior
            pqrsQ.setParam(2, lyBeg);
            pqrsQ.setParam(3, lyEnd);
            Long lyPed = pqrsQ.getAsLong(em);

            //proyección
            pqrsQ.setParam(2, curBeg);
            pqrsQ.setParam(3, curEnd);
            Long proyPed = (long) ((pqrsQ.getAsLong(em) / day) * maxDay);

            //ASIGNACIÓN
            row[0] = rowName;
            row[1] = curTime;//tiempo;
            row[2] = curCancel;//cancelados
            row[3] = curPolled;//encuestados
            row[4] = curUnat;//no atendidos
            row[5] = curPed;//Atendidos
            row[6] = inTime;//a tipo
            row[7] = proyPed;//Pedidos;
            row[8] = lmPed;//mes pasado Pedidos;
            row[9] = lyPed;//año pasado Pedidos;
        }
        if (tbl.getData() != null && tbl.getData().length > 0) {
            rep.getTables().add(tbl);
        }
        return rep;
    }

    public static MySQLReport getTankPQRPollingReport(int pollVersionId, boolean punctual, int officeId, Date date, Connection em) throws Exception {
        Date[][] dates = Dates.getDateList(date);
        Date curBeg = dates[0][0];
        Date curEnd = dates[0][1];
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String sub = punctual ? "Solo de: " + df.format(date) : "Acumulado: " + df.format(curBeg) + " - " + df.format(curEnd);
        return getTankPQRPollingReport(pollVersionId, officeId, dates, sub, em);
    }

    public static MySQLReport getTankPQRPollingMonthlyReport(int pollVersionId, int officeId, int begYear, int endYear, int begMonth, int endMonth, Connection em) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date[][] dates = Dates.getDateList(begYear, endYear, begMonth, endMonth);
        String dateRange = df.format(dates[0][0]) + " - " + df.format(dates[dates.length - 1][1]);
        return getTankPQRPollingReport(pollVersionId, officeId, dates, dateRange, em);
    }

    private static MySQLReport getTankPQRPollingReport(int pollVersionId, int officeId, Date[][] dates, String subTitle, Connection em) throws Exception {
        String str = "SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_poll AS p "
                + "INNER JOIN ord_pqr_tank AS pq ON pq.pqr_poll_id = p.id "
                + "INNER JOIN ord_office AS of ON of.id = pq.office_id "
                + "WHERE "
                + " SUBSTR(p.answer FROM ?1 FOR 1) = ?2 AND "
                + " p.poll_version_id = " + pollVersionId + " AND "
                + "?3 <= pq.`regist_date` AND ?4 >= pq.`regist_date` AND pq.office_id = " + officeId + " "
                + "AND pq.enterprise_id = ?5 ";

        MySQLQuery q = new MySQLQuery(str);
        MySQLReport rep = new MySQLReport("PQRs Fugas Estacionarios", subTitle, "Hoja 1", MySQLQuery.now(em));
        rep.getSubTitles().add("Oficina: " + new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(em));
        polling(q, pollVersionId, rep, dates, em);
        return rep;
    }

    public static MySQLReport getTankPQRSatisPollingReport(int pollVersionId, boolean punctual, int officeId, Date date, Connection em) throws Exception {
        String str = "SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_poll AS p "
                + "INNER JOIN ord_pqr_tank AS pq ON pq.satis_poll_id = p.id "
                + "INNER JOIN ord_office AS of ON of.id = pq.office_id "
                + "WHERE "
                + " SUBSTR(p.answer FROM ?1 FOR 1) = ?2 AND "
                + " p.poll_version_id = " + pollVersionId + " AND "
                + "?3 <= pq.`regist_date` AND ?4 >= pq.`regist_date` AND pq.office_id = " + officeId + " "
                + "AND pq.enterprise_id = ?5 ";

        MySQLQuery q = new MySQLQuery(str);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        //calculos con fechas
        Date[][] dates = Dates.getDateList(date);
        Date curBeg = dates[0][0];
        Date curEnd = dates[0][1];

        MySQLReport rep = new MySQLReport("Encuestas de Satisfacción PQRs Fugas Estacionarios", (punctual ? "Solo de: " + df.format(date) : "Acumulado: " + df.format(curBeg) + " - " + df.format(curEnd)), "Hoja 1", MySQLQuery.now(em));
        rep.getSubTitles().add("Oficina: " + new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(em));
        polling(q, pollVersionId, rep, date, em);
        return rep;
    }

    public static MySQLReport getTankPQRCancelReport(boolean punctual, int officeId, Date date, Connection em) throws Exception {
        String str = "SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_pqr_tank AS pq "
                + "INNER JOIN ord_office AS of ON of.id = pq.office_id "
                + "WHERE "
                + "pq.anul_cause_id = ?1 "
                + "AND ?2 <= pq.`regist_date` AND ?3 >= pq.`regist_date` AND pq.office_id = " + officeId + " "
                + "AND pq.enterprise_id = ?4 ";

        MySQLQuery q = new MySQLQuery(str);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        //calculos con fechas
        Date[][] dates = Dates.getDateList(date);
        Date curBeg = dates[0][0];
        Date curEnd = dates[0][1];

        MySQLReport rep = new MySQLReport("PQRs Fugas Estacionarios Canceladas ", (punctual ? "Solo de: " + df.format(date) : "Acumulado: " + df.format(curBeg) + " - " + df.format(curEnd)), "Hoja 1", MySQLQuery.now(em));
        rep.getSubTitles().add("Oficina: " + new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(em));
        List<Enterprise> ents = Enterprise.getAll(em);

        SimpleDateFormat df1 = new SimpleDateFormat("MMMM yyyy");
        rep.setVerticalFreeze(0);
        rep.setHorizontalFreeze(0);
        rep.setZoomFactor(80);

        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//1
        //Columnas
        List<Column> cols = new ArrayList<>();
        cols.add(new Column("Opción", 48, 0));

        for (int j = 0; j < 3; j++) {
            for (Enterprise ent : ents) {
                cols.add(new Column(ent.shortName, 8, 1));
            }
            cols.add(new Column("Total", 10, 1));
        }

        TableHeader head = new TableHeader();
        head.getColums().add(new HeaderColumn("Opción", 1, 2));
        for (Date[] date1 : dates) {
            head.getColums().add(new HeaderColumn(df1.format(date1[0]), ents.size() + 1, 1));
        }

        Table tblMod = new Table("");
        tblMod.setColumns(cols);
        tblMod.getHeaders().add(head);

        //Fin config
        Table tbl = new Table(tblMod);
        List<OrdPqrAnulCause> causes = OrdPqrAnulCause.getPqrAnulCauses("tank", em);
        for (OrdPqrAnulCause cause : causes) {
            List<Object> row = new ArrayList<>();
            row.add(cause.description);
            q.setParam(1, cause.id);
            for (Date[] date1 : dates) {
                long total = 0;
                q.setParam(2, date1[0]);
                q.setParam(3, date1[1]);
                for (Enterprise ent : ents) {
                    q.setParam(4, ent.id);
                    Long lc = q.getAsLong(em);
                    row.add(lc);
                    total += lc;
                }
                row.add(total);
            }
            tbl.addRow(row.toArray());
        }

        if (tbl.getData() != null && tbl.getData().length > 0) {
            rep.getTables().add(tbl);
        }
        return rep;
    }

    //RECLAMANTES
    public static MySQLReport getOthersPQRCancelReport(boolean punctual, int officeId, Date date, Connection em) throws Exception {
        String str = "SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_pqr_other AS pq "
                + "INNER JOIN ord_office AS of ON of.id = pq.office_id "
                + "WHERE "
                + "pq.anul_cause_id = ?1 "
                + "AND ?2 <= pq.`regist_date` AND ?3 >= pq.`regist_date` AND pq.office_id = " + officeId + " "
                + "AND pq.enterprise_id = ?4 ";

        MySQLQuery q = new MySQLQuery(str);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        Date[][] dates = Dates.getDateList(date);
        Date curBeg = dates[0][0];
        Date curEnd = dates[0][1];

        MySQLQuery qLabel = new MySQLQuery("SELECT l.plural FROM sys_label l WHERE l.name = '{other}' ");
        String lblPqrOther = qLabel.getAsString(em);
        lblPqrOther = (lblPqrOther != null ? lblPqrOther : "PQR Otros");

        MySQLReport rep = new MySQLReport("Cancelación de " + lblPqrOther, (punctual ? "Solo de: " + df.format(date) : "Acumulado: " + df.format(curBeg) + " - " + df.format(curEnd)), "Hoja 1", MySQLQuery.now(em));
        rep.getSubTitles().add("Oficina: " + new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(em));
        List<Enterprise> ents = Enterprise.getAll(em);

        SimpleDateFormat df1 = new SimpleDateFormat("MMMM yyyy");
        rep.setVerticalFreeze(0);
        rep.setHorizontalFreeze(0);
        rep.setZoomFactor(80);

        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//1
        //Columnas
        List<Column> cols = new ArrayList<>();
        cols.add(new Column("Opción", 48, 0));

        for (int j = 0; j < 3; j++) {
            for (Enterprise ent : ents) {
                cols.add(new Column(ent.shortName, 8, 1));
            }
            cols.add(new Column("Total", 10, 1));
        }

        TableHeader head = new TableHeader();
        head.getColums().add(new HeaderColumn("Opción", 1, 2));
        for (Date[] date1 : dates) {
            head.getColums().add(new HeaderColumn(df1.format(date1[0]), ents.size() + 1, 1));
        }

        Table tblMod = new Table("");
        tblMod.setColumns(cols);
        tblMod.getHeaders().add(head);

        //Fin config
        Table tbl = new Table(tblMod);
        List<OrdPqrAnulCause> causes = OrdPqrAnulCause.getPqrAnulCauses("other", em);
        for (OrdPqrAnulCause cause : causes) {
            List<Object> row = new ArrayList<>();
            //row.add(j + 1);
            row.add(cause.description);
            q.setParam(1, cause.id);
            for (Date[] date1 : dates) {
                long total = 0;
                q.setParam(2, date1[0]);
                q.setParam(3, date1[1]);
                for (Enterprise ent : ents) {
                    q.setParam(4, ent.id);
                    Long lc = q.getAsLong(em);
                    row.add(lc);
                    total += lc;
                }
                row.add(total);
            }
            tbl.addRow(row.toArray());
        }

        if (tbl.getData() != null && tbl.getData().length > 0) {
            rep.getTables().add(tbl);
        }
        return rep;
    }

    public static MySQLReport getPQRComCancelReport(boolean punctual, int officeId, Date date, Connection em) throws Exception {
        String str = "SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_pqr_com AS pq "
                + "INNER JOIN ord_office AS of ON of.id = pq.office_id "
                + "WHERE "
                + "pq.anul_cause_id = ?1 "
                + "AND ?2 <= pq.`regist_date` AND ?3 >= pq.`regist_date` AND pq.office_id = " + officeId + " "
                + "AND pq.enterprise_id = ?4 ";

        MySQLQuery q = new MySQLQuery(str);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        Date[][] dates = Dates.getDateList(date);
        Date curBeg = dates[0][0];
        Date curEnd = dates[0][1];

        MySQLReport rep = new MySQLReport("Cancelación de Eventos Comerciales", (punctual ? "Solo de: " + df.format(date) : "Acumulado: " + df.format(curBeg) + " - " + df.format(curEnd)), "Hoja 1", MySQLQuery.now(em));
        rep.getSubTitles().add("Oficina: " + new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(em));
        List<Enterprise> ents = Enterprise.getAll(em);

        SimpleDateFormat df1 = new SimpleDateFormat("MMMM yyyy");
        rep.setVerticalFreeze(0);
        rep.setHorizontalFreeze(0);
        rep.setZoomFactor(80);

        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//1
        //Columnas
        List<Column> cols = new ArrayList<>();
        cols.add(new Column("Opción", 48, 0));

        for (int j = 0; j < 3; j++) {
            for (Enterprise ent : ents) {
                cols.add(new Column(ent.shortName, 8, 1));
            }
            cols.add(new Column("Total", 10, 1));
        }

        TableHeader head = new TableHeader();
        head.getColums().add(new HeaderColumn("Opción", 1, 2));
        for (Date[] date1 : dates) {
            head.getColums().add(new HeaderColumn(df1.format(date1[0]), ents.size() + 1, 1));
        }

        Table tblMod = new Table("");
        tblMod.setColumns(cols);
        tblMod.getHeaders().add(head);

        //Fin config
        Table tbl = new Table(tblMod);
        List<OrdPqrAnulCause> causes = OrdPqrAnulCause.getPqrAnulCauses("com", em);
        for (OrdPqrAnulCause cause : causes) {
            List<Object> row = new ArrayList<>();
            //row.add(j + 1);
            row.add(cause.description);
            q.setParam(1, cause.id);
            for (Date[] date1 : dates) {
                long total = 0;
                q.setParam(2, date1[0]);
                q.setParam(3, date1[1]);
                for (Enterprise ent : ents) {
                    q.setParam(4, ent.id);
                    Long lc = q.getAsLong(em);
                    row.add(lc);
                    total += lc;
                }
                row.add(total);
            }
            tbl.addRow(row.toArray());
        }

        if (tbl.getData() != null && tbl.getData().length > 0) {
            rep.getTables().add(tbl);
        }
        return rep;
    }

    public static MySQLReport getOthersPQRPollingReport(int pollVersionId, boolean punctual, int officeId, Date date, Connection em) throws Exception {
        Date[][] dates = Dates.getDateList(date);
        Date curBeg = dates[0][0];
        Date curEnd = dates[0][1];
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String sub = punctual ? "Solo de: " + df.format(date) : "Acumulado: " + df.format(curBeg) + " - " + df.format(curEnd);
        return getOthersPQRPollingReport(pollVersionId, officeId, dates, sub, em);
    }

    public static MySQLReport getOthersPQRPollingMonthlyReport(int pollVersionId, int officeId, int begYear, int endYear, int begMonth, int endMonth, Connection em) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date[][] dates = Dates.getDateList(begYear, endYear, begMonth, endMonth);
        String dateRange = df.format(dates[0][0]) + " - " + df.format(dates[dates.length - 1][1]);
        return getOthersPQRPollingReport(pollVersionId, officeId, dates, dateRange, em);
    }

    private static MySQLReport getOthersPQRPollingReport(int pollVersionId, int officeId, Date[][] dates, String subTitle, Connection em) throws Exception {
        String str = "SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_poll AS p "
                + "INNER JOIN ord_pqr_other AS pq ON pq.pqr_poll_id = p.id "
                + "INNER JOIN ord_office AS of ON of.id = pq.office_id "
                + "WHERE "
                + " SUBSTR(p.answer FROM ?1 FOR 1) = ?2 AND "
                + " p.poll_version_id = " + pollVersionId + " AND "
                + "?3 <= pq.`regist_date` AND ?4 >= pq.`regist_date` AND pq.office_id = " + officeId + " "
                + "AND pq.enterprise_id = ?5 ";
        MySQLQuery q = new MySQLQuery(str);

        MySQLQuery qLabel = new MySQLQuery("SELECT l.plural FROM sys_label l WHERE l.name = '{other}' ");
        String lblPqrOther = qLabel.getAsString(em);
        lblPqrOther = (lblPqrOther != null ? lblPqrOther : "PQR Otros");

        MySQLReport rep = new MySQLReport(lblPqrOther, subTitle, "Hoja 1", MySQLQuery.now(em));
        rep.getSubTitles().add("Oficina: " + new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(em));
        polling(q, pollVersionId, rep, dates, em);
        return rep;
    }

    private static String getOfficeName(Integer officeId, Connection em) throws Exception {
        return new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(em);
    }

    private static String getEnterpriseById(Integer entId, Connection em) throws Exception {
        return new MySQLQuery("SELECT e.name FROM enterprise e WHERE e.id = ?1").setParam(1, entId).getAsString(em);
    }

    public static MySQLReport getDetailedOtherPqrReport(Integer entId, Date beginDate, Date endDate, Integer oprId, int state, Integer officeId, Integer channelId, Integer supreasonId, Connection em) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        MySQLQuery qLabel = new MySQLQuery("SELECT l.plural FROM sys_label l WHERE l.name = '{other}' ");
        String lblPqrOther = qLabel.getAsString(em);
        lblPqrOther = (lblPqrOther != null ? lblPqrOther : "PQR Otros");

        OrdCfg cfg = new OrdCfg().select(1, em);
        MySQLReport rep = new MySQLReport("Reporte Detallado de " + lblPqrOther, "Período " + df.format(beginDate) + " - " + df.format(endDate), "details_pqrs_other", MySQLQuery.now(em));
        rep.setVerticalFreeze(6);
        rep.setHorizontalFreeze(0);
        rep.setZoomFactor(80);

        //Subtitulos
        rep.getSubTitles().add(("Empresa: " + (entId != null ? new MySQLQuery("SELECT e.name FROM enterprise e WHERE e.id = ?1").setParam(1, entId).getAsString(em) : "Todas")) + (", Oficina: " + (officeId != null ? new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(em) : "Todas las Oficinas")));

        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.00"));//3
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "HH:mm:ss"));//
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));//5
        rep.getFormats().get(1).setWrap(true);

        //Columnas
        List<Column> cols = new ArrayList<>();
        int columns = 11;
        cols.add(new Column("Oficina", 9, 1));
        cols.add(new Column("Zona", 25, 1));
        cols.add(new Column("Canal", 20, 1));

        cols.add(new Column("Núm.", 11, 0));
        if (cfg.enterprise) {
            cols.add(new Column("Empresa", 10, 1));
            columns++;
        }

        if (cfg.supreason) {
            cols.add(new Column("Tipificación", 20, 1));
            columns++;
        }
        cols.add(new Column("Motivo", 30, 1));
        if (cfg.showSinister) {
            cols.add(new Column("Es Evento", 15, 1));
            columns++;
        }
        if (cfg.showSubject) {
            cols.add(new Column("Asunto", 30, 1));
            columns++;
        }
        if (cfg.subreason) {
            cols.add(new Column("Dlle. Motivo", 20, 1));
            columns++;
        }

        cols.add(new Column("Responsable", 20, 1));
        cols.add(new Column("Capturado", 12, 2));
        cols.add(new Column("Hora Cap.", 13, 4));
        cols.add(new Column("Confirmado", 13, 2));
        cols.add(new Column("Cancelado", 12, 2));
        cols.add(new Column("Operador", 25, 1));

        if (cfg.subAndDesc) {
            cols.add(new Column("Asunto", 20, 1));
            cols.add(new Column("Descripción", 50, 1));
            columns += 2;
        }

        cols.add(new Column("Tipo", 15, 1));
        cols.add(new Column("Documento", 13, 1));
        cols.add(new Column("Nombre", 25, 1));
        cols.add(new Column("Teléfono", 15, 1));
        cols.add(new Column("Dirección", 35, 1));
        cols.add(new Column("Observaciones", 50, 1));
        cols.add(new Column("Días", 12, 5));
        cols.add(new Column("Oportuno", 12, 0));
        cols.add(new Column("Confirmado", 13, 0));
        cols.add(new Column("Cancel", 10, 0));

        Table tbl = new Table("Reporte Detallado de " + lblPqrOther);
        tbl.setColumns(cols);
        TableHeader header = new TableHeader();
        tbl.getHeaders().add(header);
        header.getColums().add(new HeaderColumn("Datos Pqr", columns, 1));
        header.getColums().add(new HeaderColumn("Datos Cliente", 6, 1));
        header.getColums().add(new HeaderColumn("Tiempo Respuesta", 2, 1));
        header.getColums().add(new HeaderColumn("Estado", 2, 1));

        tbl.setSummaryRow(new SummaryRow("Totales", columns + 7));
        String str = "SELECT "
                + "of.sname, "//0
                + "z.name, "
                + "ch.name, "//0
                + "pqr.serial, " //0
                + (cfg.enterprise ? "e.`short_name`, " : "")//1
                + (cfg.supreason ? " supr.description, " : "")
                + "r.description, "//2
                + (cfg.showSinister ? " IF(pqr.is_sinister, 'Si', ''), " : "")
                + (cfg.showSubject ? " pqr.subject_reason, " : "")
                + (cfg.subreason ? " subr.description, " : "")
                + (cfg.useCmbResp ? "(SELECT CONCAT(first_name, ' ', last_name) FROM employee WHERE id = pqr.resp_id), " : "pqr.resp_name,")
                + "pqr.`regist_date`, "//3
                + "pqr.`regist_hour`, "//4
                + "pqr.`confirm_date`, "//
                + "pqr.`cancel_date`, "//
                + "CONCAT(o.first_name, \" \" , o.last_name) AS oper, "//5
                + (cfg.subAndDesc ? "pqr.subject, pqr.description, " : "")
                + "IF(pqr.client_id IS NOT NULL, \"Apartamentos\", "
                + "IF(pqr.index_id IS NOT NULL,\"Cilindros\", "
                + "IF(pqr.build_id IS NOT NULL,\"Estacionarios\", "
                + "IF(pqr.store_id IS NOT NULL,\"Puntos de Venta\",\"\")))), "//6

                + "IF(pqr.client_id IS NOT NULL, cli.doc, "
                + "IF(pqr.index_id IS NOT NULL,i.document, "
                + "IF(pqr.build_id IS NOT NULL,build.document, "
                + "IF(pqr.store_id IS NOT NULL,sto.document,\"\")))), " //7

                + "IF(pqr.client_id IS NOT NULL, CONCAT(cli.first_name, \" \", IFNULL(cli.last_name, '')), "
                + "IF(pqr.index_id IS NOT NULL,CONCAT(i.first_name, \" \", IFNULL(i.last_name, '')), "
                + "IF(pqr.build_id IS NOT NULL,build.represen_name, "
                + "IF(pqr.store_id IS NOT NULL,CONCAT(sto.first_name, \" \", IFNULL(sto.last_name, '')),\"\")))), " //8

                + "IF(pqr.client_id IS NOT NULL, cli.phones, "
                + "IF(pqr.index_id IS NOT NULL,i.phones, "
                + "IF(pqr.build_id IS NOT NULL,build.phones, "
                + "IF(pqr.store_id IS NOT NULL,sto.phones,\"\")))), " //9

                + "IF(pqr.client_id IS NOT NULL, " // if 1
                + "IF(cli.build_ord_id IS NOT NULL," //if 2 
                + "CONCAT(buildApto.address, IF(n.`name` IS NOT NULL, CONCAT(' ', n.`name`), ''))," // si if 2 
                + "CONCAT(n.name,' - ',cli.address)" // no if 2 
                + "), "//SI if1 
                + "IF(pqr.index_id IS NOT NULL,CONCAT(i.address, IF(n.`name` IS NOT NULL, CONCAT(' ', n.`name`), '')), "
                + "IF(pqr.build_id IS NOT NULL,CONCAT(build.address,\" \",build.name), "
                + "IF(pqr.store_id IS NOT NULL, CONCAT(sto.address, IF(n.`name` IS NOT NULL, CONCAT(' ', n.`name`), '')),\"\")))"
                + "), "//10

                + "poll.notes,"
                + "IF(DATEDIFF(pqr.confirm_date,pqr.regist_date)>=0,DATEDIFF(pqr.confirm_date,pqr.regist_date),null), "//11
                + "IF(DATEDIFF(pqr.confirm_date,pqr.regist_date)>=0,IF(DATEDIFF(pqr.confirm_date,pqr.regist_date) <= " + cfg.otherPqrLimitTime + ",1,0),null), " //12
                + "iF(pqr.pqr_poll_id IS NUll,0,1), "//13
                + "iF(pqr.anul_cause_id IS NUll,0,1) "//14
                + "FROM "
                + "ord_pqr_other AS pqr "
                + "INNER JOIN ord_office AS of ON pqr.office_id = of.id "
                + "INNER JOIN employee AS o ON o.id = pqr.regist_by "
                + (cfg.enterprise ? "LEFT JOIN enterprise AS e ON e.id = pqr.enterprise_id  " : " ")
                + "INNER JOIN ord_pqr_reason as r ON pqr.reason_id=r.id "
                + "LEFT JOIN ord_pqr_client_tank as cli ON cli.id=pqr.client_id  "
                + "LEFT JOIN ord_tank_client AS build ON build.id=pqr.build_id "
                + "LEFT JOIN ord_tank_client AS buildApto ON buildApto.id=cli.build_ord_id "
                + "LEFT JOIN est_tank_category as type ON type.id=build.categ_id "
                + "LEFT JOIN ord_contract_index as i ON pqr.index_id=i.id "
                + "LEFT JOIN inv_store AS sto ON pqr.store_id=sto.id "
                + "LEFT JOIN neigh AS n ON n.id = i.neigh_id "
                + "LEFT JOIN ord_poll as poll ON pqr.pqr_poll_id = poll.id "
                + "LEFT JOIN ord_channel AS ch ON ch.id = pqr.channel_id "
                + "LEFT JOIN zone z  ON z.id = pqr.zone_id "
                + (cfg.subreason ? "LEFT JOIN ord_pqr_subreason subr ON subr.id = pqr.subreason_id " : " ")
                + (cfg.supreason ? "LEFT JOIN ord_pqr_supreason supr ON supr.id = r.supreason_id " : " ")
                + "WHERE "
                + " ?2 <= pqr.regist_date AND "
                + " ?3 >= pqr.regist_date "
                + (officeId != null ? " AND of.id = " + officeId + " " : " ")
                + (entId != null && cfg.enterprise ? " AND pqr.enterprise_id = " + entId + " " : "")
                + (oprId != null ? " AND o.id = " + oprId + " " : "")
                + (channelId != null ? " AND ch.id = " + channelId : " ")
                + (supreasonId != null ? " AND supr.id = " + supreasonId : " ");

        switch (state) {
            case 1:
                //atendidos
                str += " And pqr.pqr_poll_id IS NOT NULL ";
                break;
            case 2:
                //no atendidos
                str += " And pqr.pqr_poll_id IS NULL "
                        + " And pqr.anul_cause_id IS NULL ";
                break;
            case 3:
                //cancelados
                str += " And pqr.anul_cause_id IS NOT NULL ";
                break;
            default:
                break;
        }
        MySQLQuery rowsQ = new MySQLQuery(str);
        rowsQ.setParam(2, beginDate);
        rowsQ.setParam(3, endDate);
        Object[][] rows = rowsQ.getRecords(em);
        if (rows != null && rows.length > 0) {
            tbl.setData(rows);
            rep.getTables().add(tbl);
        }
        return rep;
    }

    public static MySQLReport getDetailedPqrComReport(Integer entId, Date beginDate, Date endDate, Integer oprId, int state, Integer officeId, Integer channelId, Integer supreasonId, Connection em) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        OrdCfg cfg = new OrdCfg().select(1, em);
        MySQLReport rep = new MySQLReport("Detallado de Eventos Comerciales", "Período " + df.format(beginDate) + " - " + df.format(endDate), "detail_events", MySQLQuery.now(em));
        rep.setVerticalFreeze(6);
        rep.setHorizontalFreeze(0);
        rep.setZoomFactor(80);

        //Subtitulos
        rep.getSubTitles().add(("Empresa: " + (entId != null ? new MySQLQuery("SELECT e.name FROM enterprise e WHERE e.id = ?1").setParam(1, entId).getAsString(em) : "Todas")) + (", Oficina: " + (officeId != null ? new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(em) : "Todas las Oficinas")));

        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.00"));//3
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "HH:mm:ss"));//
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));//5
        rep.getFormats().get(1).setWrap(true);

        //Columnas
        List<Column> cols = new ArrayList<>();
        int columns = 11;
        cols.add(new Column("Oficina", 9, 1));
        cols.add(new Column("Zona", 20, 1));
        cols.add(new Column("Canal", 25, 1));
        cols.add(new Column("Núm.", 11, 0));
        if (cfg.enterprise) {
            cols.add(new Column("Empresa", 10, 1));
            columns++;
        }

        if (cfg.supreason) {
            cols.add(new Column("Tipificación", 20, 1));
            columns++;
        }
        cols.add(new Column("Motivo", 30, 1));
        if (cfg.showSubject) {
            cols.add(new Column("Asunto", 30, 1));
            columns++;
        }
        if (cfg.subreason) {
            cols.add(new Column("Dlle. Motivo", 20, 1));
            columns++;
        }

        cols.add(new Column("Responsable", 20, 1));
        cols.add(new Column("Capturado", 12, 2));
        cols.add(new Column("Hora Cap.", 13, 4));
        cols.add(new Column("Confirmado", 13, 2));
        cols.add(new Column("Cancelado", 12, 2));
        cols.add(new Column("Operador", 25, 1));

        if (cfg.subAndDesc) {
            cols.add(new Column("Asunto", 20, 1));
            cols.add(new Column("Descripción", 50, 1));
            columns += 2;
        }

        cols.add(new Column("Tipo", 15, 1));
        cols.add(new Column("Documento", 13, 1));
        cols.add(new Column("Nombre", 25, 1));
        cols.add(new Column("Teléfono", 15, 1));
        cols.add(new Column("Dirección", 35, 1));
        cols.add(new Column("Observaciones", 50, 1));
        cols.add(new Column("Días", 12, 5));
        cols.add(new Column("Oportuno", 12, 0));
        cols.add(new Column("Confirmado", 13, 0));
        cols.add(new Column("Cancel", 10, 0));

        Table tbl = new Table("Detallado de Eventos Comerciales");
        tbl.setColumns(cols);
        TableHeader header = new TableHeader();
        tbl.getHeaders().add(header);
        header.getColums().add(new HeaderColumn("Datos Evento", columns, 1));
        header.getColums().add(new HeaderColumn("Datos Cliente", 6, 1));
        header.getColums().add(new HeaderColumn("Tiempo Respuesta", 2, 1));
        header.getColums().add(new HeaderColumn("Estado", 2, 1));

        tbl.setSummaryRow(new SummaryRow("Totales", columns + 7));
        String str = "SELECT "
                + "of.sname, "//0
                + "z.name, "
                + "ch.name, "//0
                + "pqr.serial, " //0
                + (cfg.enterprise ? "e.`short_name`, " : "")//1
                + (cfg.supreason ? " supr.description, " : "")
                + "r.description, "//2
                + (cfg.showSubject ? " pqr.subject_reason, " : "")
                + (cfg.subreason ? " subr.description, " : "")
                + "pqr.resp_name,"
                + "pqr.`regist_date`, "//3
                + "pqr.`regist_hour`, "//4
                + "pqr.`confirm_date`, "//
                + "pqr.`cancel_date`, "//
                + "CONCAT(o.first_name, \" \" , o.last_name) AS oper, "//5
                + (cfg.subAndDesc ? "pqr.subject, pqr.description, " : "")
                + "IF(pqr.client_id IS NOT NULL, \"Apartamentos\", "
                + "IF(pqr.index_id IS NOT NULL,\"Cilindros\", "
                + "IF(pqr.build_id IS NOT NULL,\"Estacionarios\", "
                + "IF(pqr.store_id IS NOT NULL,\"Puntos de Venta\",\"\")))), "//6

                + "IF(pqr.client_id IS NOT NULL, cli.doc, "
                + "IF(pqr.index_id IS NOT NULL,i.document, "
                + "IF(pqr.build_id IS NOT NULL,build.document, "
                + "IF(pqr.store_id IS NOT NULL,sto.document,\"\")))), " //7

                + "IF(pqr.client_id IS NOT NULL, CONCAT(cli.first_name, \" \", IFNULL(cli.last_name, '')), "
                + "IF(pqr.index_id IS NOT NULL,CONCAT(i.first_name, \" \", IFNULL(i.last_name, '')), "
                + "IF(pqr.build_id IS NOT NULL,build.represen_name, "
                + "IF(pqr.store_id IS NOT NULL,CONCAT(sto.first_name, \" \", IFNULL(sto.last_name, '')),\"\")))), " //8

                + "IF(pqr.client_id IS NOT NULL, cli.phones, "
                + "IF(pqr.index_id IS NOT NULL,i.phones, "
                + "IF(pqr.build_id IS NOT NULL,build.phones, "
                + "IF(pqr.store_id IS NOT NULL,sto.phones,\"\")))), " //9

                + "IF(pqr.client_id IS NOT NULL, CONCAT(buildApto.address, IF(n.`name` IS NOT NULL, CONCAT(' ', n.`name`), '')), "
                + "IF(pqr.index_id IS NOT NULL,CONCAT(i.address, IF(n.`name` IS NOT NULL, CONCAT(' ', n.`name`), '')), "
                + "IF(pqr.build_id IS NOT NULL,CONCAT(build.address,\" \",build.name), "
                + "IF(pqr.store_id IS NOT NULL, CONCAT(sto.address, IF(n.`name` IS NOT NULL, CONCAT(' ', n.`name`), '')),\"\")))), "//10

                + "poll.notes,"
                + "IF(DATEDIFF(pqr.confirm_date,pqr.regist_date)>=0,DATEDIFF(pqr.confirm_date,pqr.regist_date),null), "//11
                + "IF(DATEDIFF(pqr.confirm_date,pqr.regist_date)>=0,IF(DATEDIFF(pqr.confirm_date,pqr.regist_date) <= " + cfg.pqrComLimitTime + ",1,0),null), " //12
                + "iF(pqr.pqr_poll_id IS NUll,0,1), "//13
                + "iF(pqr.anul_cause_id IS NUll,0,1) "//14
                + "FROM "
                + "ord_pqr_com AS pqr "
                + "INNER JOIN ord_office AS of ON pqr.office_id = of.id "
                + "INNER JOIN employee AS o ON o.id = pqr.regist_by "
                + (cfg.enterprise ? "LEFT JOIN enterprise AS e ON e.id = pqr.enterprise_id  " : " ")
                + "INNER JOIN ord_pqr_reason as r ON pqr.reason_id=r.id "
                + "LEFT JOIN ord_pqr_client_tank as cli ON cli.id=pqr.client_id  "
                + "LEFT JOIN ord_tank_client AS build ON build.id=pqr.build_id "
                + "LEFT JOIN ord_tank_client AS buildApto ON buildApto.id=cli.build_ord_id "
                + "LEFT JOIN est_tank_category as type ON type.id=build.categ_id "
                + "LEFT JOIN ord_contract_index as i ON pqr.index_id=i.id "
                + "LEFT JOIN inv_store AS sto ON pqr.store_id=sto.id "
                + "LEFT JOIN neigh AS n ON n.id = i.neigh_id "
                + "LEFT JOIN ord_poll as poll ON pqr.pqr_poll_id = poll.id "
                + "LEFT JOIN ord_channel AS ch ON ch.id = pqr.channel_id "
                + "LEFT JOIN zone z  ON z.id = pqr.zone_id "
                + (cfg.subreason ? "LEFT JOIN ord_pqr_subreason subr ON subr.id = pqr.subreason_id " : " ")
                + (cfg.supreason ? "LEFT JOIN ord_pqr_supreason supr ON supr.id = r.supreason_id " : " ")
                + "WHERE "
                + " ?2 <= pqr.regist_date AND "
                + " ?3 >= pqr.regist_date "
                + (officeId != null ? " AND of.id = " + officeId + " " : " ")
                + (entId != null && cfg.enterprise ? " AND pqr.enterprise_id = " + entId + " " : "")
                + (oprId != null ? " AND o.id = " + oprId + " " : "")
                + (channelId != null ? " AND ch.id = " + channelId : " ")
                + (supreasonId != null ? " AND supr.id = " + supreasonId : " ");

        switch (state) {
            case 1:
                //atendidos
                str += " And pqr.pqr_poll_id IS NOT NULL ";
                break;
            case 2:
                //no atendidos
                str += " And pqr.pqr_poll_id IS NULL "
                        + " And pqr.anul_cause_id IS NULL ";
                break;
            case 3:
                //cancelados
                str += " And pqr.anul_cause_id IS NOT NULL ";
                break;
            default:
                break;
        }
        MySQLQuery rowsQ = new MySQLQuery(str);
        rowsQ.setParam(2, beginDate);
        rowsQ.setParam(3, endDate);
        Object[][] rows = rowsQ.getRecords(em);
        if (rows != null && rows.length > 0) {
            tbl.setData(rows);
            rep.getTables().add(tbl);
        }
        return rep;
    }

    private static boolean isMonthly(Date beg, Date end) {
        GregorianCalendar gBeg = new GregorianCalendar();
        gBeg.setTime(beg);
        GregorianCalendar gEnd = new GregorianCalendar();
        gEnd.setTime(end);
        return gBeg.get(GregorianCalendar.YEAR) == gEnd.get(GregorianCalendar.YEAR) && gBeg.get(GregorianCalendar.MONTH) == gEnd.get(GregorianCalendar.MONTH) && gBeg.get(GregorianCalendar.DAY_OF_MONTH) == 1;
    }

    public static MySQLReport getCylOrdersReport(int repType, int officeId, Date beginDate, Date endDate, Integer entId, String typeClient, Connection em) throws Exception {
        OrdCfg cfg = new OrdCfg().select(1, em);

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat df1 = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));

        Date[][] dates = Dates.getDateList(endDate);
        Date curBeg = Dates.trimDate(beginDate);
        Date curEnd = Dates.trimDate(endDate);
        if (curBeg.compareTo(endDate) > 0) {
            throw new Exception("La fecha inicial debe ser menor o igual a la final.");
        }

        boolean monthly = isMonthly(curBeg, curEnd);

        Date lmBeg = dates[1][0];
        Date lmEnd = dates[1][1];
        Date lyBeg = dates[2][0];
        Date lyEnd = dates[2][1];
        double day = Dates.getDayOfMonth(endDate);
        double maxDay = Dates.getMaxDayOfMonth(endDate);

        //composición del filtro ppal
        String filt = "?2 <= o.`day` AND ?3 >= o.`day` AND o.office_id = " + officeId + " "
                + (entId != null ? " AND IF(o.enterprise_id IS NOT NULL, o.enterprise_id = " + entId + ", TRUE) " : "");
        if (typeClient != null) {
            filt += " AND i.type = '" + typeClient + "' ";
        }
        String filt1 = filt;//mismo filtro pero sin restricción por vehículo

        MySQLQuery rowsQ = null;
        String title = null;
        String colName = null;
        switch (repType) {
            case 5:
                //barrios
                rowsQ = new MySQLQuery("select n.id, concat(c.name, ' - ', s.name, ' - ', n.name) from "
                        + "ord_office_city oc "
                        + "inner join ord_office o on o.id = oc.office_id "
                        + "inner join city c on c.id = oc.city_id "
                        + "inner join sector s on s.city_id = c.id "
                        + "inner join neigh n on n.sector_id = s.id "
                        + "where o.id = " + officeId + " "
                        + "order by c.name, s.name, n.name");
                filt += "AND n.id = ?1  ";
                title = "Pedidos por Barrio";
                colName = "Barrio";
                break;
            case 1:
                //sectores
                rowsQ = new MySQLQuery("select s.id, concat(c.name, ' - ', s.name) from "
                        + "ord_office o "
                        + "inner join ord_office_city oc on oc.office_id = o.id "
                        + "inner join city c on c.id = oc.city_id "
                        + "inner join sector s on s.city_id = c.id "
                        + "where o.id = " + officeId + " "
                        + "order by c.name, s.name");
                filt += "AND n.sector_id = ?1  ";
                title = "Pedidos por Sector";
                colName = "Sector";
                break;
            case 2:
                //vehículos
                rowsQ = new MySQLQuery("SELECT DISTINCT "
                        + "o.vehicle_id, CONCAT(v.internal, \" - \", v.plate)  "//0
                        + "FROM "
                        + "ord_cyl_order o "
                        + "INNER JOIN ord_office of ON o.office_id = of.id "
                        + "INNER JOIN vehicle v ON v.id = o.vehicle_id "
                        + "WHERE " + filt1);
                filt += "AND o.vehicle_id = ?1 ";
                title = "Pedidos por Vehículos";
                colName = "Vehículo";
                break;
            case 3:
                //conductores
                rowsQ = new MySQLQuery("SELECT DISTINCT "
                        + "o.driver_id, CONCAT(e.first_name, \" \", e.last_name) "//0
                        + "FROM "
                        + "ord_cyl_order o "
                        + "INNER JOIN ord_office of ON o.office_id = of.id "
                        + "INNER JOIN employee e ON e.id = o.driver_id "
                        + "WHERE " + filt1);
                filt += "AND o.driver_id = ?1 ";
                title = "Pedidos por Conductor";
                colName = "Conductor";
                break;
            case 4:
                //operadores
                rowsQ = new MySQLQuery("SELECT DISTINCT "
                        + "o.taken_by_id, CONCAT(e.first_name, \" \", e.last_name) "//0
                        + "FROM "
                        + "ord_cyl_order o "
                        + "INNER JOIN ord_office of ON o.office_id = of.id "
                        + "INNER JOIN employee e ON e.id = o.taken_by_id "
                        + "WHERE " + filt1);
                filt += "AND o.taken_by_id = ?1 ";
                title = "Pedidos por Operador";
                colName = "Operador";
                break;
            default:
                break;
        }

        if (rowsQ == null) {
            throw new Exception("repType desconocido");
        }

        MySQLQuery ordersQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_cyl_order o "
                + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                + "INNER JOIN ord_office of ON o.office_id = of.id "
                + "INNER JOIN neigh n ON o.neigh_id = n.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.confirm_hour IS NOT NULL AND " + filt);

        MySQLQuery timesQ = new MySQLQuery("SELECT "
                + "AVG(TIME_TO_SEC(confirm_hour) - TIME_TO_SEC(taken_hour))/60 "//0
                + "FROM "
                + "ord_cyl_order o "
                + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                + "INNER JOIN ord_office of ON o.office_id = of.id "
                + "INNER JOIN neigh n ON o.neigh_id = n.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.confirm_hour IS NOT NULL AND " + filt);

        MySQLQuery inTimeQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_cyl_order o "
                + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                + "INNER JOIN ord_office of ON o.office_id = of.id "
                + "INNER JOIN neigh n ON o.neigh_id = n.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.confirm_hour IS NOT NULL "
                + "AND TIME_TO_SEC(confirm_hour) - TIME_TO_SEC(taken_hour) <= " + (cfg.limitTime * 60) + " AND "
                + filt);

        MySQLQuery justifQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_cyl_order o "
                + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                + "INNER JOIN ord_office of ON o.office_id = of.id "
                + "INNER JOIN neigh n ON o.neigh_id = n.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.justif IS NOT NULL AND " + filt);

        MySQLQuery complQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_cyl_order o "
                + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                + "INNER JOIN ord_office of ON o.office_id = of.id "
                + "INNER JOIN neigh n ON o.neigh_id = n.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.complain IS NOT NULL AND " + filt);

        MySQLQuery unatQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_cyl_order o "
                + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                + "INNER JOIN ord_office of ON o.office_id = of.id "
                + "INNER JOIN neigh n ON o.neigh_id = n.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.confirm_hour IS NULL AND " + filt);

        MySQLQuery cancelQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_cyl_order o "
                + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                + "INNER JOIN ord_office of ON o.office_id = of.id "
                + "INNER JOIN neigh n ON o.neigh_id = n.id "
                + "WHERE o.cancel_cause_id IS NOT NULL AND " + filt);

        MySQLQuery polledQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_cyl_order o "
                + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                + "INNER JOIN ord_office of ON o.office_id = of.id "
                + "INNER JOIN neigh n ON o.neigh_id = n.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.confirm_hour IS NOT NULL AND o.poll_id IS NOT NULL AND " + filt);

        MySQLQuery galsQ = new MySQLQuery("SELECT "
                + "sum(ct.capacity * cto.amount) "//0
                + "FROM "
                + "ord_cyl_order AS o "
                + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                + "INNER JOIN ord_office of ON o.office_id = of.id "
                + "INNER JOIN ord_cyl_type_order AS cto ON cto.order_id = o.id "
                + "INNER JOIN cylinder_type AS ct ON ct.id = cto.cylinder_type_id "
                + "INNER JOIN neigh n ON o.neigh_id = n.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.confirm_hour IS NOT NULL AND " + filt);

        MySQLQuery cylsQ = new MySQLQuery("SELECT "
                + "sum(cto.amount) "//0
                + "FROM "
                + "ord_cyl_order AS o "
                + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                + "INNER JOIN ord_office of ON o.office_id = of.id "
                + "INNER JOIN ord_cyl_type_order AS cto ON cto.order_id = o.id "
                + "INNER JOIN neigh n ON o.neigh_id = n.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.confirm_hour IS NOT NULL AND " + filt + " AND "
                + "cto.cylinder_type_id = ?4 ");

        rowsQ.setParam(2, curBeg);
        rowsQ.setParam(3, curEnd);

        Object[][] rows = rowsQ.getRecords(em);
        List<CylinderType> types = CylinderType.getAll(em);

        //REPORTE
        MySQLReport rep = new MySQLReport(title, "Rango: " + df.format(curBeg) + " - " + df.format(curEnd), "Hoja 1", MySQLQuery.now(em));
        rep.setVerticalFreeze(0);
        rep.setHorizontalFreeze(2);
        rep.setZoomFactor(80);
        //Subtitulos
        rep.getSubTitles().add("Empresa: " + (entId != null ? new MySQLQuery("SELECT e.name FROM enterprise e WHERE e.id = ?1").setParam(1, entId).getAsString(em) : "Todas") + (" ,Oficina: " + new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(em)));
        rep.getSubTitles().add("Tipo Contrato: " + (typeClient != null ? (typeClient.equals("brand") ? "Afiliado" : (typeClient.equals("app") ? "App" : "Provisional")) : "Todos"));
        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.00"));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        //Columnas
        List<Column> cols = new ArrayList<>();
        cols.add(new Column("", 30, 2));//name
        cols.add(new Column("", 9, 1));//time
        for (CylinderType type : types) {
            cols.add(new Column(type.name, CYLS_WIDTH, 0));
        }
        cols.add(new Column("Total", ORDS_WIDTH + 2, 0));

        cols.add(new Column("Cancelados", ORDS_WIDTH, 0));
        cols.add(new Column("Encuestados", ORDS_WIDTH, 0));
        cols.add(new Column("No Atend.", ORDS_WIDTH, 0));

        cols.add(new Column("Quejas", ORDS_WIDTH, 0));
        cols.add(new Column("Justif", ORDS_WIDTH, 0));
        cols.add(new Column("Atendidos", ORDS_WIDTH, 0));
        cols.add(new Column("A tiempo.", ORDS_WIDTH, 0));
        cols.add(new Column("Galones", GALS_WIDTH, 1));
        cols.add(new Column("Kilos", GALS_WIDTH, 1));
        if (monthly) {
            cols.add(new Column("Pedidos", ORDS_WIDTH, 0));
            cols.add(new Column("Galones", GALS_WIDTH, 1));
            cols.add(new Column("Kilos", GALS_WIDTH, 1));
            cols.add(new Column("Pedidos", ORDS_WIDTH, 0));
            cols.add(new Column("Galones", GALS_WIDTH, 1));
            cols.add(new Column("Kilos", GALS_WIDTH, 1));
            cols.add(new Column("Pedidos", ORDS_WIDTH, 0));
            cols.add(new Column("Galones", GALS_WIDTH, 1));
            cols.add(new Column("Kilos", GALS_WIDTH, 1));
        }
        Table tbl = new Table("Pedidos de Cilindros " + df.format(curBeg) + " - " + df.format(curEnd));
        tbl.setColumns(cols);
        //Cabecera
        TableHeader header = new TableHeader();
        tbl.getHeaders().add(header);
        header.getColums().add(new HeaderColumn(colName, 1, 2));
        header.getColums().add(new HeaderColumn("Tiempo ", 1, 2));
        header.getColums().add(new HeaderColumn("Cilindros", types.size() + 1, 1));
        header.getColums().add(new HeaderColumn("Pedidos", 9, 1));
        if (monthly) {
            header.getColums().add(new HeaderColumn("Proyec. " + df1.format(curBeg), 3, 1));
            header.getColums().add(new HeaderColumn(df1.format(lmBeg), 3, 1));
            header.getColums().add(new HeaderColumn(df1.format(lyBeg), 3, 1));
        }
        //Totales
        tbl.setSummaryRow(new SummaryRow("Totales", 2));
        //fin config

        int rowLen;
        if (monthly) {
            rowLen = 21 + types.size();
        } else {
            rowLen = 12 + types.size();
        }
        BigDecimal kte = new MySQLQuery("SELECT gal_to_kg_kte FROM sys_cfg").getAsBigDecimal(em, true);

        for (int i = 0; i < rows.length; i++) {
            Object[] row = new Object[rowLen];
            tbl.addRow(row);
            int rowId = (Integer) rows[i][0];
            String rowName = rows[i][1].toString();
            ordersQ.setParam(1, rowId);
            galsQ.setParam(1, rowId);
            cylsQ.setParam(1, rowId);
            justifQ.setParam(1, rowId);
            inTimeQ.setParam(1, rowId);
            complQ.setParam(1, rowId);
            cancelQ.setParam(1, rowId);
            polledQ.setParam(1, rowId);
            unatQ.setParam(1, rowId);
            timesQ.setParam(1, rowId);
            //PEDIDOS
            //tiempo de entrega
            timesQ.setParam(2, curBeg);
            timesQ.setParam(3, curEnd);
            BigDecimal curTime = timesQ.getAsBigDecimal(em, true);
            //cancelados actual
            cancelQ.setParam(2, curBeg);
            cancelQ.setParam(3, curEnd);
            Long curCancel = cancelQ.getAsLong(em);
            //encuestados actual
            polledQ.setParam(2, curBeg);
            polledQ.setParam(3, curEnd);
            Long curPolled = polledQ.getAsLong(em);
            //no atendidos mes actual
            unatQ.setParam(2, curBeg);
            unatQ.setParam(3, curEnd);
            Long curUnat = unatQ.getAsLong(em);
            //justificados
            justifQ.setParam(2, curBeg);
            justifQ.setParam(3, curEnd);
            Long curJustif = justifQ.getAsLong(em);
            //quejas
            complQ.setParam(2, curBeg);
            complQ.setParam(3, curEnd);
            Long curCompl = complQ.getAsLong(em);
            //pedidos mes actual
            ordersQ.setParam(2, curBeg);
            ordersQ.setParam(3, curEnd);
            Long curPed = ordersQ.getAsLong(em);

            //a tiempo
            inTimeQ.setParam(2, curBeg);
            inTimeQ.setParam(3, curEnd);
            Long inTime = inTimeQ.getAsLong(em);

            //GALONES
            //mes actual
            galsQ.setParam(2, curBeg);
            galsQ.setParam(3, curEnd);
            BigDecimal curGal = galsQ.getAsBigDecimal(em, true);

            //ASIGNACIÓN
            row[0] = rowName;
            row[1] = curTime;//tiempo;
            //CILINDROS
            cylsQ.setParam(2, curBeg);
            cylsQ.setParam(3, curEnd);
            long total = 0l;
            for (int j = 0; j < types.size(); j++) {
                cylsQ.setParam(4, types.get(j).id);
                long c = cylsQ.getAsBigDecimal(em, true).longValue();
                row[2 + j] = c;
                total += c;
            }

            row[2 + types.size()] = total;//Total;
            row[3 + types.size()] = curCancel;//cancelados
            row[4 + types.size()] = curPolled;//encuestados
            row[5 + types.size()] = curUnat;//no atendidos

            row[6 + types.size()] = curCompl;//quejas
            row[7 + types.size()] = curJustif;//justificaciones
            row[8 + types.size()] = curPed;//Atendidos
            row[9 + types.size()] = inTime;//a tipo
            row[10 + types.size()] = curGal;//Galones;
            row[11 + types.size()] = curGal.multiply(kte);//kilos

            if (monthly) {
                //proyección
                ordersQ.setParam(2, curBeg);
                ordersQ.setParam(3, curEnd);
                row[12 + types.size()] = (long) ((ordersQ.getAsLong(em) / day) * maxDay);
                //proyección
                galsQ.setParam(2, curBeg);
                galsQ.setParam(3, curEnd);
                row[13 + types.size()] = (galsQ.getAsBigDecimal(em, true).doubleValue() / day) * maxDay;
                row[14 + types.size()] = kte.multiply(new BigDecimal((Double) row[13 + types.size()]));
                //pedidos mes anterior
                ordersQ.setParam(2, lmBeg);
                ordersQ.setParam(3, lmEnd);
                row[15 + types.size()] = ordersQ.getAsLong(em);
                //mes anterior
                galsQ.setParam(2, lmBeg);
                galsQ.setParam(3, lmEnd);
                row[16 + types.size()] = galsQ.getAsBigDecimal(em, true);
                row[17 + types.size()] = kte.multiply((BigDecimal) row[16 + types.size()]);
                //pedidos año anterior
                ordersQ.setParam(2, lyBeg);
                ordersQ.setParam(3, lyEnd);
                row[18 + types.size()] = ordersQ.getAsLong(em);
                //año anterior
                galsQ.setParam(2, lyBeg);
                galsQ.setParam(3, lyEnd);
                row[19 + types.size()] = galsQ.getAsBigDecimal(em, true);
                row[20 + types.size()] = kte.multiply((BigDecimal) row[19 + types.size()]);
            }
        }
        if (tbl.getData() != null && tbl.getData().length > 0) {
            rep.getTables().add(tbl);
        }
        return rep;
    }

    public static MySQLReport getTankOrdersReport(int repType, int officeId, Date begDate, Date endDate, Connection em) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat df1 = new SimpleDateFormat("MMM yyy");
        //calculos con fechas
        Date[][] dates = Dates.getDateList(endDate);
        Date curBeg = Dates.trimDate(begDate);
        Date curEnd = Dates.trimDate(endDate);

        if (curBeg.compareTo(endDate) > 0) {
            throw new Exception("La fecha inicial debe ser menor o igual a la final.");
        }
        Date lmBeg = dates[1][0];
        Date lmEnd = dates[1][1];
        Date lyBeg = dates[2][0];
        Date lyEnd = dates[2][1];
        double day = Dates.getDayOfMonth(begDate);
        double maxDay = Dates.getMaxDayOfMonth(begDate);

        boolean monthly = isMonthly(curBeg, curEnd);

        //composición del filtro ppal
        String filt = "?2 <= o.`day` AND ?3 >= o.`day` AND o.office_id = " + officeId + " ";

        String filt1 = filt;//mismo filtro pero sin restricción por vehículo

        MySQLQuery rowsQ = null;
        String title = null;
        String colName = null;
        switch (repType) {
            case 1:
                //tipo de cliente
                rowsQ = new MySQLQuery("SELECT DISTINCT "
                        + "ct.id, ct.description  "//0
                        + "FROM "
                        + "ord_tank_order o "
                        + "INNER JOIN ord_office of ON o.office_id = of.id "
                        + "INNER JOIN ord_tank_client c ON o.tank_client_id = c.id "
                        + "INNER JOIN est_tank_category ct ON c.categ_id = ct.id "
                        + "WHERE " + filt1);
                filt += " AND c.categ_id = ?1  ";
                title = "Pedidos por Tipo de Cliente";
                colName = "Tipo";
                break;
            case 2:
                //vehículos
                rowsQ = new MySQLQuery("SELECT DISTINCT "
                        + "o.vehicle_id, CONCAT(v.internal, \" - \", v.plate)  "//0
                        + "FROM "
                        + "ord_tank_order o "
                        + "INNER JOIN ord_office of ON o.office_id = of.id "
                        + "INNER JOIN vehicle v ON v.id = o.vehicle_id "
                        + "WHERE " + filt1);
                filt += "AND o.vehicle_id = ?1 ";
                title = "Pedidos por Vehículos";
                colName = "Vehículo";
                break;
            case 3:
                //conductores
                rowsQ = new MySQLQuery("SELECT DISTINCT "
                        + "o.driver_id, CONCAT(e.first_name, \" \", e.last_name) "//0
                        + "FROM "
                        + "ord_tank_order o "
                        + "INNER JOIN ord_office of ON o.office_id = of.id "
                        + "INNER JOIN employee e ON e.id = o.driver_id "
                        + "WHERE " + filt1);
                filt += "AND o.driver_id = ?1 ";
                title = "Pedidos por Conductor";
                colName = "Conductor";
                break;
            case 4:
                //operadores
                rowsQ = new MySQLQuery("SELECT DISTINCT "
                        + "o.taken_by_id, CONCAT(e.first_name, \" \", e.last_name) "//0
                        + "FROM "
                        + "ord_tank_order o "
                        + "INNER JOIN ord_office of ON o.office_id = of.id "
                        + "INNER JOIN employee e ON e.id = o.taken_by_id "
                        + "WHERE " + filt1);
                filt += "AND o.taken_by_id = ?1 ";
                title = "Pedidos por Operador";
                colName = "Operador";
                break;
            default:
                break;
        }
        if (rowsQ == null) {
            throw new Exception("reqType desconocido");
        }

        //QUERIES DE COLUMNAS
        MySQLQuery ordersQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_tank_order o "
                + "INNER JOIN ord_office of ON o.office_id = of.id "
                + "INNER JOIN ord_tank_client c ON o.tank_client_id = c.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.confirm_hour IS NOT NULL AND " + filt);

        MySQLQuery timesQ = new MySQLQuery("SELECT "
                + "CONVERT(AVG(TIME_TO_SEC(confirm_hour) - TIME_TO_SEC(taken_hour))/60, UNSIGNED) "//0
                + "FROM "
                + "ord_tank_order o "
                + "INNER JOIN ord_office of ON o.office_id = of.id "
                + "INNER JOIN ord_tank_client c ON o.tank_client_id = c.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.confirm_hour IS NOT NULL AND " + filt);

        MySQLQuery justifQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_tank_order o "
                + "INNER JOIN ord_office of ON o.office_id = of.id "
                + "INNER JOIN ord_tank_client c ON o.tank_client_id = c.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.justif IS NOT NULL AND " + filt);

        MySQLQuery complQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_tank_order o "
                + "INNER JOIN ord_office of ON o.office_id = of.id "
                + "INNER JOIN ord_tank_client c ON o.tank_client_id = c.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.complain IS NOT NULL AND " + filt);

        MySQLQuery unatQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_tank_order o "
                + "INNER JOIN ord_office of ON o.office_id = of.id "
                + "INNER JOIN ord_tank_client c ON o.tank_client_id = c.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.confirm_hour IS NULL AND " + filt);

        MySQLQuery cancelQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_tank_order o "
                + "INNER JOIN ord_office of ON o.office_id = of.id "
                + "INNER JOIN ord_tank_client c ON o.tank_client_id = c.id "
                + "WHERE o.cancel_cause_id IS NOT NULL AND " + filt);

        MySQLQuery polledQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_tank_order o "
                + "INNER JOIN ord_office of ON o.office_id = of.id "
                + "INNER JOIN ord_tank_client c ON o.tank_client_id = c.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.confirm_hour IS NOT NULL AND o.poll_id IS NOT NULL AND " + filt);

        rowsQ.setParam(2, curBeg);
        rowsQ.setParam(3, curEnd);

        Object[][] rows = rowsQ.getRecords(em);

        //REPORTE
        MySQLReport rep = new MySQLReport(title, "Acumulado: " + df.format(curBeg) + " - " + df.format(curEnd), "Hoja 1", MySQLQuery.now(em));
        rep.setVerticalFreeze(0);
        rep.setHorizontalFreeze(2);
        rep.setZoomFactor(80);
        //Subtitulos
        rep.getSubTitles().add("Oficina: " + new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(em));
        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.00"));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        //Columnas
        List<Column> cols = new ArrayList<>();
        cols.add(new Column("", 30, 2));//name
        cols.add(new Column("", 9, 0));//time
        cols.add(new Column("Cancelados", 16, 0));
        cols.add(new Column("Encuestados", 16, 0));
        cols.add(new Column("No Atend.", 16, 0));
        cols.add(new Column("Quejas", 16, 0));
        cols.add(new Column("Justif", 16, 0));
        cols.add(new Column("Atendidos", 16, 0));
        if (monthly) {
            cols.add(new Column("Pedidos", 16, 0));
            cols.add(new Column("Pedidos", 16, 0));
            cols.add(new Column("Pedidos", 16, 0));
        }
        Table tbl = new Table("Pedidos de Estacionarios " + df.format(curBeg) + " - " + df.format(curEnd));
        tbl.setColumns(cols);
        //Cabecera
        TableHeader header = new TableHeader();
        tbl.getHeaders().add(header);
        header.getColums().add(new HeaderColumn(colName, 1, 2));
        header.getColums().add(new HeaderColumn("Tiempo  ", 1, 2));
        header.getColums().add(new HeaderColumn("Pedidos", 6, 1));
        if (monthly) {
            header.getColums().add(new HeaderColumn("Proyec. " + df1.format(curBeg), 1, 1));
            header.getColums().add(new HeaderColumn(df1.format(lmBeg), 1, 1));
            header.getColums().add(new HeaderColumn(df1.format(lyBeg), 1, 1));
        }
        //Totales
        tbl.setSummaryRow(new SummaryRow("Totales", 2));
        //fin config

        int rowLen;
        if (monthly) {
            rowLen = 11;
        } else {
            rowLen = 8;
        }

        for (int i = 0; i < rows.length; i++) {
            Object[] row = new Object[rowLen];
            tbl.addRow(row);
            int rowId = (Integer) rows[i][0];
            String rowName = rows[i][1].toString();
            ordersQ.setParam(1, rowId);
            justifQ.setParam(1, rowId);
            complQ.setParam(1, rowId);
            cancelQ.setParam(1, rowId);
            polledQ.setParam(1, rowId);
            unatQ.setParam(1, rowId);
            timesQ.setParam(1, rowId);
            //PEDIDOS
            //tiempo de entrega
            timesQ.setParam(2, curBeg);
            timesQ.setParam(3, curEnd);
            Integer curTime = timesQ.getAsInteger(em);
            //cancelados actual
            cancelQ.setParam(2, curBeg);
            cancelQ.setParam(3, curEnd);
            Long curCancel = cancelQ.getAsLong(em);
            //encuestados actual
            polledQ.setParam(2, curBeg);
            polledQ.setParam(3, curEnd);
            Long curPolled = polledQ.getAsLong(em);
            //no atendidos mes actual
            unatQ.setParam(2, curBeg);
            unatQ.setParam(3, curEnd);
            Long curUnat = unatQ.getAsLong(em);
            //justificados
            justifQ.setParam(2, curBeg);
            justifQ.setParam(3, curEnd);
            Long curJustif = justifQ.getAsLong(em);
            //quejas
            complQ.setParam(2, curBeg);
            complQ.setParam(3, curEnd);
            Long curCompl = complQ.getAsLong(em);
            //pedidos mes actual
            ordersQ.setParam(2, curBeg);
            ordersQ.setParam(3, curEnd);
            Long curPed = ordersQ.getAsLong(em);

            //ASIGNACIÓN
            row[0] = rowName;
            row[1] = curTime;//tiempo;
            row[2] = curCancel;//cancelados
            row[3] = curPolled;//encuestados
            row[4] = curUnat;//no atendidos
            row[5] = curCompl;//quejas2
            row[6] = curJustif;//justificaciones
            row[7] = curPed;//Atendidos
            if (monthly) {
                //proyección
                ordersQ.setParam(2, curBeg);
                ordersQ.setParam(3, curEnd);
                row[8] = (long) ((ordersQ.getAsLong(em) / day) * maxDay);//Pedidos proyección
                //pedidos mes anterior
                ordersQ.setParam(2, lmBeg);
                ordersQ.setParam(3, lmEnd);
                row[9] = ordersQ.getAsLong(em);//Pedidos mes pasado
                //pedidos año anterior
                ordersQ.setParam(2, lyBeg);
                ordersQ.setParam(3, lyEnd);
                row[10] = ordersQ.getAsLong(em);//Pedidos año pasado
            }

        }
        if (tbl.getData() != null && tbl.getData().length > 0) {
            rep.getTables().add(tbl);
        }
        return rep;
    }

    public static MySQLReport getCylVehicleReport(int repType, int officeId, Date begDate, Date endDate, Integer entId, String typeCli, Connection em) throws Exception {
        int days = (int) ((endDate.getTime() - begDate.getTime()) / 1000 / 60 / 60 / 24);
        if (days > 31) {
            throw new Exception("El reporte no debe incluir más de 31 días.");
        }
        OrdCfg cfg = new OrdCfg().select(1, em);

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat df1 = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
        //calculos con fechas
        Date[][] dates = Dates.getDateList(endDate);
        Date curBeg = Dates.trimDate(begDate);
        Date curEnd = Dates.trimDate(endDate);

        if (curBeg.compareTo(endDate) > 0) {
            throw new Exception("La fecha inicial debe ser menor o igual a la final.");
        }
        boolean monthly = isMonthly(curBeg, curEnd);
        Date lmBeg = dates[1][0];
        Date lmEnd = dates[1][1];
        Date lyBeg = dates[2][0];
        Date lyEnd = dates[2][1];
        double day = Dates.getDayOfMonth(endDate);
        double maxDay = Dates.getMaxDayOfMonth(endDate);

        //composición del filtro ppal
        String filt = "?2 <= o.`day` AND ?3 >= o.`day` AND o.office_id = " + officeId + " "
                + (entId != null ? " AND IF(o.enterprise_id IS NOT NULL, o.enterprise_id = " + entId + ",TRUE) " : "");

        if (typeCli != null) {
            filt += " AND i.type = '" + typeCli + "' ";
        }
        filt += "AND n.sector_id = ?4  ";

        //es para identificar las filas, por eso no debe llevar el filtro específico
        String filt1 = filt;//mismo filtro pero sin restricción por vehículo

        MySQLQuery rowsQ = null;
        String title = null;
        String colName = null;

        if (repType == 1) {//vehículos
            rowsQ = new MySQLQuery("SELECT DISTINCT "
                    + "o.vehicle_id, CONCAT(v.internal, \" - \", v.plate)  "//0
                    + "FROM "
                    + "ord_cyl_order o "
                    + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                    + "INNER JOIN vehicle v ON v.id = o.vehicle_id "
                    + "INNER JOIN neigh n ON o.neigh_id = n.id "
                    + "WHERE " + filt1);
            filt += "AND o.vehicle_id = ?1 ";
            title = "Pedidos por Vehículos";
            colName = "Vehículo";
        } else if (repType == 2) {//conductores
            rowsQ = new MySQLQuery("SELECT DISTINCT "
                    + "o.driver_id, CONCAT(e.first_name, \" \", e.last_name) "//0
                    + "FROM "
                    + "ord_cyl_order o "
                    + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                    + "INNER JOIN employee e ON e.id = o.driver_id "
                    + "INNER JOIN neigh n ON o.neigh_id = n.id "
                    + "WHERE " + filt1);
            filt += "AND o.driver_id = ?1 ";
            title = "Pedidos por Conductor";
            colName = "Conductor";
        }

        MySQLQuery ordersQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_cyl_order o "
                + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                + "INNER JOIN neigh n ON o.neigh_id = n.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.confirmed_by_id IS NOT NULL AND " + filt);

        MySQLQuery timesQ = new MySQLQuery("SELECT "
                + "CONVERT(AVG(TIME_TO_SEC(confirm_hour) - TIME_TO_SEC(taken_hour))/60, UNSIGNED) "//0
                + "FROM "
                + "ord_cyl_order o "
                + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                + "INNER JOIN neigh n ON o.neigh_id = n.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.confirmed_by_id IS NOT NULL AND " + filt);

        MySQLQuery inTimeQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_cyl_order o "
                + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                + "INNER JOIN neigh n ON o.neigh_id = n.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.confirmed_by_id IS NOT NULL "
                + "AND TIME_TO_SEC(confirm_hour) - TIME_TO_SEC(taken_hour) <= " + (cfg.limitTime * 60) + " AND "
                + filt);

        MySQLQuery justifQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_cyl_order o "
                + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                + "INNER JOIN neigh n ON o.neigh_id = n.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.justif IS NOT NULL AND " + filt);

        MySQLQuery complQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_cyl_order o "
                + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                + "INNER JOIN neigh n ON o.neigh_id = n.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.complain IS NOT NULL AND " + filt);

        MySQLQuery unatQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_cyl_order o "
                + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                + "INNER JOIN neigh n ON o.neigh_id = n.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.confirmed_by_id IS NULL AND " + filt);

        MySQLQuery cancelQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_cyl_order o "
                + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                + "INNER JOIN neigh n ON o.neigh_id = n.id "
                + "WHERE o.cancel_cause_id IS NOT NULL AND " + filt);

        MySQLQuery polledQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_cyl_order o "
                + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                + "INNER JOIN neigh n ON o.neigh_id = n.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.confirmed_by_id IS NOT NULL AND o.poll_id IS NOT NULL AND " + filt);

        MySQLQuery galsQ = new MySQLQuery("SELECT "
                + "sum(ct.capacity * cto.amount) "//0
                + "FROM "
                + "ord_cyl_order AS o "
                + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                + "INNER JOIN ord_cyl_type_order AS cto ON cto.order_id = o.id "
                + "INNER JOIN cylinder_type AS ct ON ct.id = cto.cylinder_type_id "
                + "INNER JOIN neigh n ON o.neigh_id = n.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.confirmed_by_id IS NOT NULL AND " + filt);

        MySQLQuery cylsQ = new MySQLQuery("SELECT "
                + "sum(cto.amount) "//0
                + "FROM "
                + "ord_cyl_order AS o "
                + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                + "INNER JOIN ord_cyl_type_order AS cto ON cto.order_id = o.id "
                + "INNER JOIN neigh n ON o.neigh_id = n.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.confirmed_by_id IS NOT NULL AND " + filt + " AND "
                + "cto.cylinder_type_id = ?5 ");

        List<CylinderType> types = CylinderType.getAll(em);

        //REPORTE
        MySQLReport rep = new MySQLReport(title, "Rango: " + df.format(curBeg) + " - " + df.format(curEnd), "Hoja 1", MySQLQuery.now(em));
        rep.setVerticalFreeze(0);
        rep.setHorizontalFreeze(2);
        rep.setZoomFactor(80);
        //Subtitulos
        rep.getSubTitles().add("Empresa: " + (entId != null ? new MySQLQuery("SELECT e.name FROM enterprise e WHERE e.id = ?1").setParam(1, entId).getAsString(em) : "Todas") + (" ,Oficina: " + new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(em)));
        rep.getSubTitles().add("Tipo Contrato: " + (typeCli != null ? (typeCli.equals("brand") ? "Afiliado" : (typeCli.equals("app") ? "App" : "Provisional")) : "Todos"));
        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.00"));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        //Columnas
        List<Column> cols = new ArrayList<>();
        cols.add(new Column("", 30, 2));//name
        cols.add(new Column("", 9, 0));//time
        for (CylinderType type : types) {
            cols.add(new Column(type.name, CYLS_WIDTH, 0));
        }
        cols.add(new Column("Total", ORDS_WIDTH + 2, 0));

        cols.add(new Column("Cancelados", ORDS_WIDTH, 0));
        cols.add(new Column("Encuestados", ORDS_WIDTH, 0));
        cols.add(new Column("No Atend.", ORDS_WIDTH, 0));

        cols.add(new Column("Quejas", ORDS_WIDTH, 0));
        cols.add(new Column("Justif", ORDS_WIDTH, 0));
        cols.add(new Column("Atendidos", ORDS_WIDTH, 0));
        cols.add(new Column("A Tiempo", ORDS_WIDTH, 0));
        cols.add(new Column("Galones", GALS_WIDTH, 1));
        cols.add(new Column("Kilos", GALS_WIDTH, 1));

        if (monthly) {
            cols.add(new Column("Pedidos", ORDS_WIDTH, 0));
            cols.add(new Column("Galones", GALS_WIDTH, 1));
            cols.add(new Column("Kilos", GALS_WIDTH, 1));
            cols.add(new Column("Pedidos", ORDS_WIDTH, 0));
            cols.add(new Column("Galones", GALS_WIDTH, 1));
            cols.add(new Column("Kilos", GALS_WIDTH, 1));
            cols.add(new Column("Pedidos", ORDS_WIDTH, 0));
            cols.add(new Column("Galones", GALS_WIDTH, 1));
            cols.add(new Column("Kilos", GALS_WIDTH, 1));
        }

        Table modTbl = new Table("Pedidos de Cilindros " + df.format(curBeg) + " - " + df.format(curEnd));
        modTbl.setColumns(cols);
        //Cabecera
        TableHeader header = new TableHeader();
        modTbl.getHeaders().add(header);
        header.getColums().add(new HeaderColumn(colName, 1, 2));
        header.getColums().add(new HeaderColumn("Tiempo ", 1, 2));
        header.getColums().add(new HeaderColumn("Cilindros", types.size() + 1, 1));
        header.getColums().add(new HeaderColumn("Pedidos", 9, 1));
        if (monthly) {
            header.getColums().add(new HeaderColumn("Proyec. " + df1.format(curBeg), 3, 1));
            header.getColums().add(new HeaderColumn(df1.format(lmBeg), 3, 1));
            header.getColums().add(new HeaderColumn(df1.format(lyBeg), 3, 1));
        }
        if (rowsQ == null) {
            throw new Exception("reqType desconocido");
        }
        //Totales
        modTbl.setSummaryRow(new SummaryRow("Totales", 2));
        //fin config
        List<Sector> sectors = Sector.getSectorsByOffice(officeId, em);
        rowsQ.setParam(2, curBeg);
        rowsQ.setParam(3, curEnd);

        int rowLen;
        if (monthly) {
            rowLen = 21;
        } else {
            rowLen = 12;
        }

        BigDecimal kte = new MySQLQuery("SELECT gal_to_kg_kte FROM sys_cfg").getAsBigDecimal(em, true);

        for (Sector sector : sectors) {
            rowsQ.setParam(4, sector.id);
            Object[][] rows = rowsQ.getRecords(em);
            Table tbl = new Table(modTbl);
            tbl.setTitle(tbl.getTitle() + " - " + sector.name);
            for (int i = 0; i < rows.length; i++) {
                Object[] row = new Object[rowLen + types.size()];
                tbl.addRow(row);
                int rowId = (Integer) rows[i][0];
                String rowName = rows[i][1].toString();
                //filtro fila
                ordersQ.setParam(1, rowId);
                galsQ.setParam(1, rowId);
                cylsQ.setParam(1, rowId);
                inTimeQ.setParam(1, rowId);
                justifQ.setParam(1, rowId);
                complQ.setParam(1, rowId);
                cancelQ.setParam(1, rowId);
                polledQ.setParam(1, rowId);
                unatQ.setParam(1, rowId);
                timesQ.setParam(1, rowId);
                //filtro sector
                ordersQ.setParam(4, sector.id);
                galsQ.setParam(4, sector.id);
                cylsQ.setParam(4, sector.id);
                inTimeQ.setParam(4, sector.id);
                justifQ.setParam(4, sector.id);
                complQ.setParam(4, sector.id);
                cancelQ.setParam(4, sector.id);
                polledQ.setParam(4, sector.id);
                unatQ.setParam(4, sector.id);
                timesQ.setParam(4, sector.id);
                //PEDIDOS
                //tiempo de entrega
                timesQ.setParam(2, curBeg);
                timesQ.setParam(3, curEnd);
                Integer curTime = timesQ.getAsInteger(em);
                //cancelados actual
                cancelQ.setParam(2, curBeg);
                cancelQ.setParam(3, curEnd);
                Long curCancel = cancelQ.getAsLong(em);
                //encuestados actual
                polledQ.setParam(2, curBeg);
                polledQ.setParam(3, curEnd);
                Long curPolled = polledQ.getAsLong(em);
                //no atendidos mes actual
                unatQ.setParam(2, curBeg);
                unatQ.setParam(3, curEnd);
                Long curUnat = unatQ.getAsLong(em);
                //justificados
                justifQ.setParam(2, curBeg);
                justifQ.setParam(3, curEnd);
                Long curJustif = justifQ.getAsLong(em);
                //quejas
                complQ.setParam(2, curBeg);
                complQ.setParam(3, curEnd);
                Long curCompl = complQ.getAsLong(em);
                //pedidos mes actual
                ordersQ.setParam(2, curBeg);
                ordersQ.setParam(3, curEnd);
                Long curPed = ordersQ.getAsLong(em);
                //a tiempo
                inTimeQ.setParam(2, curBeg);
                inTimeQ.setParam(3, curEnd);
                Long inTime = inTimeQ.getAsLong(em);

                //GALONES
                //mes actual
                galsQ.setParam(2, curBeg);
                galsQ.setParam(3, curEnd);
                BigDecimal curGal = galsQ.getAsBigDecimal(em, true);

                //ASIGNACIÓN
                row[0] = rowName;
                row[1] = curTime;//tiempo;
                //CILINDROS
                cylsQ.setParam(2, curBeg);
                cylsQ.setParam(3, curEnd);
                long total = 0l;
                for (int j = 0; j < types.size(); j++) {
                    cylsQ.setParam(5, types.get(j).id);
                    long c = cylsQ.getAsBigDecimal(em, true).longValue();
                    row[2 + j] = c;
                    total += c;
                }

                row[2 + types.size()] = total;//Total;
                row[3 + types.size()] = curCancel;//cancelados
                row[4 + types.size()] = curPolled;//encuestados
                row[5 + types.size()] = curUnat;//no atendidos

                row[6 + types.size()] = curCompl;//quejas2
                row[7 + types.size()] = curJustif;//justificaciones
                row[8 + types.size()] = curPed;//Atendidos
                row[9 + types.size()] = inTime;//a tiempo
                row[10 + types.size()] = curGal;//Galones;
                row[11 + types.size()] = curGal.multiply(kte);//Kilos;

                //proyección
                if (monthly) {
                    //proyección
                    ordersQ.setParam(2, curBeg);
                    ordersQ.setParam(3, curEnd);
                    row[12 + types.size()] = (long) ((ordersQ.getAsLong(em) / day) * maxDay);//Pedidos;

                    galsQ.setParam(2, curBeg);
                    galsQ.setParam(3, curEnd);
                    row[13 + types.size()] = (galsQ.getAsBigDecimal(em, true).doubleValue() / day) * maxDay;//Galones;
                    row[14 + types.size()] = kte.multiply(new BigDecimal((Double) row[13 + types.size()]));
                    //mes anterior
                    ordersQ.setParam(2, lmBeg);
                    ordersQ.setParam(3, lmEnd);
                    row[15 + types.size()] = ordersQ.getAsLong(em);//Pedidos;

                    galsQ.setParam(2, lmBeg);
                    galsQ.setParam(3, lmEnd);
                    row[16 + types.size()] = galsQ.getAsBigDecimal(em, true);//Galones;
                    row[17 + types.size()] = kte.multiply((BigDecimal) row[16 + types.size()]);

                    //año anterior
                    ordersQ.setParam(2, lyBeg);
                    ordersQ.setParam(3, lyEnd);
                    row[18 + types.size()] = ordersQ.getAsLong(em);//Pedidos;

                    galsQ.setParam(2, lyBeg);
                    galsQ.setParam(3, lyEnd);
                    row[19 + types.size()] = galsQ.getAsBigDecimal(em, true);//Galones;
                    row[20 + types.size()] = kte.multiply((BigDecimal) row[19 + types.size()]);

                }
            }
            if (tbl.getData() != null && tbl.getData().length > 0) {
                rep.getTables().add(tbl);
            }
        }
        return rep;
    }

    public static MySQLReport getOrdVehicleReport(int repType, int officeId, Date begDate, Date endDate, Integer entId, String typeCli, Connection em) throws Exception {
        int days = (int) ((endDate.getTime() - begDate.getTime()) / 1000 / 60 / 60 / 24);
        if (days > 370) {
            throw new Exception("El reporte no debe incluir más de 366 días.");
        }

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date curBeg = Dates.trimDate(begDate);
        Date curEnd = Dates.trimDate(endDate);
        if (curBeg.compareTo(endDate) > 0) {
            throw new Exception("La fecha inicial debe ser menor o igual a la final.");
        }

        //composición del filtro ppal
        String filt = "?2 <= o.`day` AND ?3 >= o.`day` AND o.office_id = " + officeId + " "
                + (entId != null ? " AND IF(o.enterprise_id IS NOT NULL, o.enterprise_id = " + entId + ",TRUE) " : "");

        if (typeCli != null) {
            filt += " AND i.type = '" + typeCli + "' ";
        }
        filt += "AND n.sector_id = ?4  ";

        //es para identificar las filas, por eso no debe llevar el filtro específico
        String filt1 = filt;//mismo filtro pero sin restricción por vehículo

        MySQLQuery rowsQ = null;
        String title = null;
        String colName = null;

        switch (repType) {
            case 1:
                //vehículos
                rowsQ = new MySQLQuery("SELECT DISTINCT "
                        + "o.vehicle_id, CONCAT(e.short_name, ' ', COALESCE(v.internal,''), ' - ', v.plate)  "//0
                        + "FROM "
                        + "ord_cyl_order o "
                        + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                        + "INNER JOIN ord_office of ON o.office_id = of.id "
                        + "INNER JOIN vehicle v ON v.id = o.vehicle_id "
                        + "INNER JOIN agency ag ON ag.id = v.agency_id "
                        + "INNER JOIN enterprise e ON ag.enterprise_id = e.id "
                        + "INNER JOIN neigh n ON o.neigh_id = n.id "
                        + "WHERE " + filt1);
                filt += "AND o.vehicle_id = ?1 ";
                title = "Pedidos por Vehículos";
                colName = "Vehículo";
                break;
            case 2:
                //conductores
                rowsQ = new MySQLQuery("SELECT DISTINCT "
                        + "o.driver_id, CONCAT(e.first_name, \" \", e.last_name) "//0
                        + "FROM "
                        + "ord_cyl_order o "
                        + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                        + "INNER JOIN ord_office of ON o.office_id = of.id "
                        + "INNER JOIN employee e ON e.id = o.driver_id "
                        + "INNER JOIN neigh n ON o.neigh_id = n.id "
                        + "WHERE " + filt1);
                filt += "AND o.driver_id = ?1 ";
                title = "Pedidos por Conductor";
                colName = "Conductor";
                break;
            default:
                throw new Exception("repType Desconocido");
        }

        MySQLQuery ordersQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_cyl_order o "
                + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                + "INNER JOIN neigh n ON o.neigh_id = n.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.confirmed_by_id IS NOT NULL AND " + filt);

        if (rowsQ == null) {
            throw new Exception("reqType desconocido");
        }
        //REPORTE
        MySQLReport rep = new MySQLReport(title, "Rango: " + df.format(curBeg) + " - " + df.format(curEnd), "Hoja 1", MySQLQuery.now(em));
        rep.setVerticalFreeze(0);
        rep.setZoomFactor(80);
        //Subtitulos
        rep.getSubTitles().add(("Empresa: " + (entId != null ? new MySQLQuery("SELECT e.name FROM enterprise e WHERE e.id = ?1").setParam(1, entId).getAsString(em) : "Todas")) + (" ,Oficina: " + new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(em)));
        rep.getSubTitles().add("Tipo Contrato: " + (typeCli != null ? (typeCli.equals("brand") ? "Afiliado" : (typeCli.equals("app") ? "App" : "Provisional")) : "Todos"));
        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.00"));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        //Columnas
        List<Column> cols = new ArrayList<>();
        cols.add(new Column(colName, 55, 2));//name
        cols.add(new Column("Pedidos", 20, 0));//time
        Table modTbl = new Table(df.format(curBeg) + " - " + df.format(curEnd));
        modTbl.setColumns(cols);
        modTbl.setSummaryRow(new SummaryRow("Totales", 1));

        List<Sector> sectors = Sector.getSectorsByOffice(officeId, em);
        rowsQ.setParam(2, curBeg);
        rowsQ.setParam(3, curEnd);

        for (Sector sector : sectors) {
            rowsQ.setParam(4, sector.id);
            Object[][] rows = rowsQ.getRecords(em);
            Table tbl = new Table(modTbl);
            tbl.setTitle(tbl.getTitle() + ". " + sector.name);
            for (int i = 0; i < rows.length; i++) {
                Object[] row = new Object[2];
                tbl.addRow(row);
                int rowId = (Integer) rows[i][0];
                String rowName = rows[i][1].toString();
                ordersQ.setParam(1, rowId);
                ordersQ.setParam(4, sector.id);
                ordersQ.setParam(2, curBeg);
                ordersQ.setParam(3, curEnd);
                Long curPed = ordersQ.getAsLong(em);

                //ASIGNACIÓN
                row[0] = rowName;
                row[1] = curPed;//tiempo;
            }
            if (tbl.getData() != null && tbl.getData().length > 0) {
                rep.getTables().add(tbl);
            }
        }
        return rep;
    }

    public static MySQLReport getDetailsCylinderReport(Integer entId, Date beginDate, Date endDate, String typeCli, Integer oprId, Integer vehId, Integer driverId, Boolean justif, int state, Integer officeId, boolean descriptions, Integer channelId, boolean logs, Connection em) throws Exception {
        List<CylinderType> types = CylinderType.getAll(em);

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        MySQLReport rep = new MySQLReport("Detallado Pedidos de Cilindros", "Período " + df.format(beginDate) + " - " + df.format(endDate), "det_pedidos_cils", MySQLQuery.now(em));

        rep.setVerticalFreeze(6);
        rep.setHorizontalFreeze(0);
        rep.setZoomFactor(80);
        //Subtitulos
        rep.getSubTitles().add(("Empresa: " + (entId != null ? new MySQLQuery("SELECT e.name FROM enterprise e WHERE e.id = ?1").setParam(1, entId).getAsString(em) : "Todas")) + (" ,Oficina: " + (officeId != null ? new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(em) : "")));
        rep.getSubTitles().add("Tipo Contrato: " + (typeCli != null ? (typeCli.equals("brand") ? "Afiliado" : (typeCli.equals("app") ? "App" : "Provisional")) : "Todos"));

        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.00"));//3
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "HH:mm:ss"));//4
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy HH:mm:ss"));//5

        rep.getFormats().get(1).setWrap(true);
        OrdCfg cfg = new OrdCfg().select(1, em);
        //Columnas
        List<Column> cols = new ArrayList<>();
        //datos del pedido
        cols.add(new Column("Oficina.", 9, 1)); //0
        cols.add(new Column("Empresa", 10, 1)); //1
        cols.add(new Column("Canal.", 12, 1)); //2
        cols.add(new Column("Núm.", 11, 0)); //3
        cols.add(new Column("Solicitud", 25, 5)); //4 
        cols.add(new Column("Entrega", 12, 2)); //5               
        cols.add(new Column("Capturado", 13, 4)); //6
        cols.add(new Column("Asignado", 13, 4));//7
        cols.add(new Column("Confirmado", 13, 4)); //8
        if (cfg.getCallDate) {
            cols.add(new Column("Contacto", 25, 5));
        }
        cols.add(new Column("Capturó", 25, 1));//9
        cols.add(new Column("Asignó", 25, 1)); //10
        cols.add(new Column("Confirmó", 25, 1)); //10
        cols.add(new Column("Vehículo", 15, 1));//11
        cols.add(new Column("Conductor", 25, 1));//12
        cols.add(new Column("Tipo", 12, 1));//13
        //datos del cliente
        cols.add(new Column("Documento", 13, 1)); //14
        cols.add(new Column("Cliente", 25, 1));//15
        cols.add(new Column("Teléfono", 15, 1)); //16
        cols.add(new Column("Dirección", 35, 1));//17
        cols.add(new Column("Sector", 15, 1));//18

        if (descriptions) {
            cols.add(new Column("Justificación", 25, 1));//27
            cols.add(new Column("Queja", 25, 1));//28
            cols.add(new Column("Cancelado", 25, 1));//29
            if (cfg.orderPrice) {
                cols.add(new Column("Precio Sugerido", 18, 1));//29
            }
        }
        //promesa de entrega
        cols.add(new Column("Minutos", 12, 4)); //19
        cols.add(new Column("Cumplió", 12, 0)); //20
        cols.add(new Column("Encuest.", 10, 0)); //21
        cols.add(new Column("Justif", 10, 0));//22
        cols.add(new Column("Queja", 10, 0)); //23
        cols.add(new Column("Cancel", 10, 0));//24
        for (CylinderType type : types) {
            cols.add(new Column(type.name, CYLS_WIDTH, 0));
        }
        cols.add(new Column("Total", ORDS_WIDTH, 0));//27 + typecyl + 1
        cols.add(new Column("Galones", GALS_WIDTH, 3));//27  + typecyl + 2
        cols.add(new Column("Kilos", GALS_WIDTH, 3));//27  + typecyl + 3        
        if (cfg.salesApp) {
            cols.add(new Column("App", 9, 0));
        }
        cols.add(new Column("Futuro", 9, 0));
        cols.add(new Column("Llamar", 9, 0));
        cols.add(new Column("llamó", 9, 0));
        if (logs) {
            cols.add(new Column("Observaciones", 40, 1));
        }

        String str = "SELECT "
                //Pedidos
                + "of.sname, "//0
                + "e.`short_name`, " //1
                + "ch.name, "//2
                + "ord.id, "//3
                + "IF(ord.orig_day is not null, STR_TO_DATE(CONCAT(ord.orig_day, ' ', ord.orig_time), '%Y-%m-%d %H:%i:%s'), STR_TO_DATE(CONCAT(ord.day, ' ', ord.taken_hour), '%Y-%m-%d %H:%i:%s')), " //4
                + "ord.`day` , "//5
                + "ord.taken_hour, " //6
                + "ord.assig_hour, " //7
                + "ord.confirm_hour, " //8 
                + (cfg.getCallDate ? "ord.call_dt, " : "")
                + "(SELECT CONCAT(IF(o.virtual, 'PV - ', ''), o.first_name, ' ' ,o.last_name) FROM employee o WHERE o.id  = ord.taken_by_id  ), "// 9 + callDate
                + "(SELECT CONCAT(desp.first_name, ' ' , desp.last_name) FROM employee desp WHERE desp.id  = ord.assig_by_id ), "//desp  10+ callDate 
                + "(SELECT CONCAT(comf.first_name, ' ' , comf.last_name) FROM employee comf WHERE comf.id  = ord.confirmed_by_id ), "//conf 11+ callDate 
                + "CONCAT(COALESCE(v.internal,''), ' - ', v.plate) AS veh, " //12+ callDate 
                + "CONCAT(d.first_name, ' ' , d.last_name) AS drv, "//13+ callDate 
                + "IF(i.type = 'brand', 'Afiliado', IF(i.type = 'univ', 'Provisional', IF(i.type = 'app', 'App', null))), " //14+ callDate 
                //Cliente
                + "i.document, " //15+ callDate 
                + "CONCAT(i.first_name, ' ' , i.last_name) AS cli, " //16+ callDate 
                + "i.phones, " //17+ callDate 
                + "CONCAT(i.address, IF(n.`name` IS NOT NULL, CONCAT(' ', n.`name`), '')) AS addr, " //18+ callDate 
                + "s.`name` AS sector, "// es el sector 19+ callDate 
                //Despues de las descripciones
                + "IF(ord.orig_day is null AND ord.confirm_hour > ord.taken_hour,TIMEDIFF(ord.confirm_hour,ord.taken_hour),null), "// 20+ callDate 
                + "IF(ord.orig_day is null,IF(ord.orig_day is null,IF(TIME_TO_SEC(confirm_hour) - TIME_TO_SEC(taken_hour) <= " + (cfg.limitTime * 60) + ",1,0),0),0), " //21+ callDate 
                + "IF(ord.cancel_cause_id is NUll,iF(ord.poll_id is NUll,0,1),0) , " //22+ callDate 
                + "IF(ord.justif is NUll,0,1), " //23+ callDate 
                + "IF(ord.complain is NUll,0,1), " //24+ callDate 
                + "IF(ord.cancel_cause_id is NUll,0,1) "; //25+ callDate 
        if (descriptions) {
            str += ", ord.justif, " // 26 + callDate 
                    + "ord.complain, "// 27 + callDate 
                    + "c.description "// 28 + callDate 
                    + (cfg.orderPrice ? ", ord.price_suggested " : " ");// 29 + callDate 
        }
        if (cfg.salesApp) {
            str += ", IF(ord.app_confirmed=1,1,NULL) "; //26+ callDate + colDesc
        }
        str += " , IF(ord.future_date,'1','0') "
                + ", IF(ord.to_poll,'0','1'), " //26+ callDate + colDesc + App 
                + " IF(ord.called ,'1','0') "; //27+ callDate + colDesc + App 
        if (logs) {
            str += ", (SELECT GROUP_CONCAT(CONCAT(COALESCE(CONCAT('[', e.first_name, ' ', e.last_name, '] '), ''), lo.notes) SEPARATOR '\\n') FROM ord_log lo LEFT JOIN employee e ON e.id = lo.employee_id WHERE lo.owner_type = 11 AND lo.owner_id = ord.id) ";// 28 + callDate + colDesc + App 
        }
        str += "FROM "
                + "ord_cyl_order AS ord "
                + "LEFT JOIN employee AS d ON d.id = ord.driver_id "
                + "LEFT JOIN enterprise AS e ON e.id = ord.enterprise_id "
                + "LEFT JOIN neigh AS n ON n.id = ord.neigh_id "
                + "INNER JOIN sector AS s ON s.id = n.sector_id "
                + "INNER JOIN ord_contract_index AS i ON i.id = ord.index_id "
                + "LEFT JOIN vehicle AS v ON v.id = ord.vehicle_id "
                + "INNER JOIN ord_office AS of ON of.id = ord.office_id "
                + "LEFT JOIN ord_channel AS ch ON ch.id = ord.channel_id ";

        if (descriptions) {
            str += "LEFT JOIN ord_cancel_cause AS c ON ord.cancel_cause_id = c.id ";
        }
        str += "WHERE "
                + " ?2 <= ord.`day` AND "
                + " ?3 >= ord.`day` "
                + (officeId != null ? " AND ord.office_id = " + officeId + " " : "")
                + (entId != null ? " AND ord.enterprise_id = " + entId + " " : "")
                + (oprId != null ? " AND ord.taken_by_id = " + oprId + " " : "")
                + (vehId != null ? " AND v.id = " + vehId + " " : "")
                + (driverId != null ? " AND d.id = " + driverId + " " : "")
                + (channelId != null ? " AND ch.id = " + channelId + " " : " ");
        if (justif) {
            str += " AND ord.justif IS NOT NULL ";
        }

        if (typeCli != null) {
            str += " AND i.type = '" + typeCli + "' ";
        }
        switch (state) {
            case 1:
                //atendidos
                str += "And ord.vehicle_id IS NOT NULL "
                        + " And ord.confirm_hour IS NOT NULL "
                        + " And ord.cancel_cause_id IS NULL ";
                break;
            case 2:
                //no atendidos
                str += " And ord.confirm_hour IS NULL "
                        + " And ord.cancel_cause_id IS NULL ";
                break;
            case 3:
                //cancelados
                str += " And ord.cancel_cause_id IS NOT NULL ";
                break;
            default:
                break;
        }

        String strGals = "SELECT "
                + "sum(ct.capacity * cto.amount) "//0
                + "FROM "
                + "ord_cyl_order AS o "
                + "INNER JOIN ord_cyl_type_order AS cto ON cto.order_id = o.id "
                + "INNER JOIN cylinder_type AS ct ON ct.id = cto.cylinder_type_id "
                + "WHERE "
                + "o.cancel_cause_id IS NULL AND ";
        if (state != 2) {
            strGals += "o.confirm_hour IS NOT NULL AND ";
        }
        strGals += "o.id = ?1 ";
        MySQLQuery galsQ = new MySQLQuery(strGals);

        String strCyls = "SELECT "
                + "sum(cto.amount) "//0
                + "FROM "
                + "ord_cyl_order AS o "
                + "INNER JOIN ord_cyl_type_order AS cto ON cto.order_id = o.id "
                + "WHERE "
                + "o.id = ?1 AND "
                + "cto.cylinder_type_id = ?2 AND "
                + "o.cancel_cause_id IS NULL";
        if (state != 2) {
            strCyls += " AND o.confirm_hour IS NOT NULL ";

        }

        MySQLQuery cylsQ = new MySQLQuery(strCyls);
        MySQLQuery rowsQ = new MySQLQuery(str);
        rowsQ.setParam(2, beginDate);
        rowsQ.setParam(3, endDate);
        Object[][] rows = rowsQ.getRecords(em);

        if (rows.length > 0) {
            int callDate = cfg.getCallDate ? 1 : 0;
            Table tbl = new Table("Detallado Pedidos de Cilindros");
            tbl.setColumns(cols);
            TableHeader header = new TableHeader();
            tbl.getHeaders().add(header);
            header.getColums().add(new HeaderColumn("Datos del Pedido", 14 + callDate, 1));
            header.getColums().add(new HeaderColumn("Datos del Cliente", 5, 1));
            if (descriptions) {
                header.getColums().add(new HeaderColumn("Descripciones", (cfg.orderPrice ? 4 : 3), 1));
            }
            header.getColums().add(new HeaderColumn("Promesa de Entrega", 2, 1));
            header.getColums().add(new HeaderColumn("Estado", 5, 1));
            header.getColums().add(new HeaderColumn("Cilindros", types.size() + (cfg.salesApp ? 4 : 3) + 3, 1));
            if (logs) {
                header.getColums().add(new HeaderColumn("Historial", 1, 1));
            }

            int colDesc = 0;
            if (descriptions) {
                colDesc = (cfg.orderPrice ? 4 : 3);
            }

            tbl.setSummaryRow(new SummaryRow("Totales", 21 + colDesc));
            BigDecimal kte = new MySQLQuery("SELECT gal_to_kg_kte FROM sys_cfg").getAsBigDecimal(em, true);

            for (int i = 0; i < rows.length; i++) {
                Object[] data = new Object[32 + colDesc + types.size() + callDate + (cfg.salesApp ? 1 : 0) + (logs ? 1 : 0)];
                Integer ordId = (Integer) rows[i][3];
                //GALONES
                galsQ.setParam(1, ordId);
                BigDecimal gal = galsQ.getAsBigDecimal(em, true);

                //ASIGNACIÓN
                data[0] = rows[i][0];
                data[1] = rows[i][1];
                data[2] = rows[i][2];
                data[3] = rows[i][3]; //
                data[4] = rows[i][4]; //solicito
                data[5] = rows[i][5];//empresa
                data[6] = rows[i][6];//
                data[7] = rows[i][7];//
                data[8] = rows[i][8];// hora confirmacion 
                if (cfg.getCallDate) {//fecha de contacto con el cliente
                    data[9] = rows[i][9];
                }
                data[9 + callDate] = rows[i][9 + callDate];//operador
                data[10 + callDate] = rows[i][10 + callDate];//
                data[11 + callDate] = rows[i][11 + callDate];//
                data[12 + callDate] = rows[i][12 + callDate];//
                data[13 + callDate] = rows[i][13 + callDate];//
                //Datos Cliente 
                data[14 + callDate] = rows[i][14 + callDate];//
                data[15 + callDate] = rows[i][15 + callDate];
                data[16 + callDate] = rows[i][16 + callDate];//
                data[17 + callDate] = rows[i][17 + callDate];//telf 
                data[18 + callDate] = rows[i][18 + callDate];//sector 
                data[19 + callDate] = rows[i][19 + callDate];//

                if (descriptions) {
                    data[20 + callDate] = rows[i][26 + callDate];
                    data[21 + callDate] = rows[i][27 + callDate];
                    data[22 + callDate] = rows[i][28 + callDate];
                    if (cfg.orderPrice) {
                        data[23 + callDate] = rows[i][29 + callDate];
                    }
                }

                data[20 + colDesc + callDate] = rows[i][20 + callDate];
                data[21 + colDesc + callDate] = rows[i][21 + callDate];
                data[22 + colDesc + callDate] = rows[i][22 + callDate];
                data[23 + colDesc + callDate] = rows[i][23 + callDate];
                data[24 + colDesc + callDate] = rows[i][24 + callDate];
                data[25 + colDesc + callDate] = rows[i][25 + callDate];

                //Cilindros
                cylsQ.setParam(1, ordId);
                long total = 0l;
                for (int j = 0; j < types.size(); j++) {
                    cylsQ.setParam(2, types.get(j).id);
                    long c = cylsQ.getAsBigDecimal(em, true).longValue();
                    data[26 + colDesc + j + callDate] = c;
                    total += c;
                }
                data[26 + colDesc + types.size() + callDate] = total;//Total;
                data[27 + colDesc + types.size() + callDate] = gal;//galones
                data[28 + colDesc + types.size() + callDate] = gal.multiply(kte);//kilos

                if (cfg.salesApp) {
                    data[29 + colDesc + types.size() + callDate] = rows[i][26 + callDate + colDesc];
                }
                data[29 + colDesc + types.size() + callDate + (cfg.salesApp ? 1 : 0)] = rows[i][(cfg.salesApp ? 1 : 0) + 26 + callDate + colDesc];
                data[30 + colDesc + types.size() + callDate + (cfg.salesApp ? 1 : 0)] = rows[i][(cfg.salesApp ? 1 : 0) + 27 + callDate + colDesc];
                data[31 + colDesc + types.size() + callDate + (cfg.salesApp ? 1 : 0)] = rows[i][(cfg.salesApp ? 1 : 0) + 28 + callDate + colDesc];
                if (logs) {
                    data[32 + colDesc + types.size() + callDate + (cfg.salesApp ? 1 : 0)] = rows[i][(cfg.salesApp ? 1 : 0) + 29 + callDate + colDesc];
                }
                tbl.addRow(data);
            }

            if (tbl.getData() != null && tbl.getData().length > 0) {
                rep.getTables().add(tbl);
            }
        }
        return rep;
    }

    public static MySQLReport getDetailsTanksReport(Date beginDate, Date endDate, Integer oprId, Integer vehId, Integer driverId, Boolean justif, int state, Integer officeId, boolean descriptions, Integer channelId, Connection em) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        MySQLReport rep = new MySQLReport("Reporte Detallado de Pedidos Estacionarios", "Período " + df.format(beginDate) + " - " + df.format(endDate), "Hoja 1", MySQLQuery.now(em));

        rep.setVerticalFreeze(6);
        rep.setHorizontalFreeze(0);
        rep.setZoomFactor(80);
        //Subtitulos
        rep.getSubTitles().add(("Oficina: " + (officeId != null ? new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(em) : "Todas las Oficinas")));

        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.00"));//3
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "HH:mm:ss"));//4
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy HH:mm:ss"));//5

        rep.getFormats().get(1).setWrap(true);

        boolean getCallDt = new MySQLQuery("SELECT get_call_date FROM ord_cfg WHERE id = 1").getAsBoolean(em);
        int offSet = getCallDt ? 1 : 0;
        //Columnas
        List<Column> cols = new ArrayList<>();
        cols.add(new Column("Oficina", 9, 1));
        cols.add(new Column("Canal", 12, 1));
        cols.add(new Column("Núm.", 11, 0));
        cols.add(new Column("Solicitó", 25, 5));
        cols.add(new Column("Entrega", 12, 2));
        cols.add(new Column("Capturado", 13, 4));
        cols.add(new Column("Asignado", 13, 4));
        cols.add(new Column("Confirmado", 13, 4));
        if (getCallDt) {
            cols.add(new Column("Contacto", 25, 5));
        }
        cols.add(new Column("Operador", 25, 1));
        cols.add(new Column("Despacho", 25, 1));
        cols.add(new Column("Vehículo", 15, 1));
        cols.add(new Column("Conductor", 30, 1));
        cols.add(new Column("Nombre", 35, 1));
        cols.add(new Column("Dirección", 30, 1));
        cols.add(new Column("Tipo", 13, 1));
        cols.add(new Column("Ciudad", 15, 1));
        cols.add(new Column("Teléfono", 15, 1));
        cols.add(new Column("Descripción", 20, 1));
        if (descriptions) {
            cols.add(new Column("Justif", 25, 1));
            cols.add(new Column("Queja", 25, 1));
            cols.add(new Column("Causa Cancelación", 25, 1));
        }
        cols.add(new Column("Encuest.", 10, 0));
        cols.add(new Column("Justif", 10, 0));
        cols.add(new Column("Queja", 10, 0));
        cols.add(new Column("Cancel", 10, 0));

        Table tbl = new Table("Reporte Detallado de Pedidos Estacionarios");
        tbl.setColumns(cols);
        TableHeader header = new TableHeader();
        tbl.getHeaders().add(header);
        header.getColums().add(new HeaderColumn("Datos Pedido", 12 + offSet, 1));
        header.getColums().add(new HeaderColumn("Datos Cliente Estacionario ", 6, 1));
        if (descriptions) {
            header.getColums().add(new HeaderColumn("Descripciones", 3, 1));
        }
        header.getColums().add(new HeaderColumn("Estado", 4, 1));

        //Totales
        int desc = 0;
        if (descriptions) {
            desc = 3;
        }

        tbl.setSummaryRow(new SummaryRow("Totales", 18 + desc));

        String str = "SELECT "
                + "of.sname, "//0
                + "ch.name, "//0
                + "ord.id, "//1
                + "IF(ord.orig_day is not null, STR_TO_DATE(CONCAT(ord.orig_day, ' ', COALESCE(ord.orig_time, ord.taken_hour)), '%Y-%m-%d %H:%i:%s'), STR_TO_DATE(CONCAT(ord.day, ' ', ord.taken_hour), '%Y-%m-%d %H:%i:%s')), "//2
                + "ord.`day`, "//3
                + "ord.taken_hour, "//4
                + "ord.assig_hour, "//5
                + "ord.confirm_hour, "//6
                + (getCallDt ? "ord.call_dt, " : "")
                + "(SELECT o.short_name FROM employee o WHERE o.id  = ord.taken_by_id  ), "// oper
                + "(SELECT desp.short_name FROM employee desp WHERE desp.id  = ord.assig_by_id  ), "// desp
                + "CONCAT(COALESCE(v.internal,\"\"), \" - \", v.plate) AS veh, "//9
                + "CONCAT(d.first_name, \" \" , d.last_name) AS drv, "//10
                + "oc.`name`, "//11
                + "oc.address, "//12
                + "t.`description`, "//13
                + "ci.`name`, "//14
                + "oc.phones, "//15
                + "oc.description, "
                + "IF(ord.cancel_cause_id is NUll,iF(ord.poll_id is NUll,0,1),0) , "//16
                + "iF(ord.justif is NUll,0,1), "//17
                + "iF(ord.complain is NUll,0,1), ";//18
        if (descriptions) {
            str += "iF(ord.cancel_cause_id is NUll,0,1), "//19
                    + "ord.justif, "//20
                    + "ord.complain, "//21
                    + "c.description ";//22
        } else {
            str += "iF(ord.cancel_cause_id is NUll,0,1) ";//19
        }
        str += "FROM "
                + "ord_tank_order AS ord "
                + "LEFT JOIN employee AS d ON d.id = ord.driver_id "
                + "LEFT JOIN vehicle AS v ON v.id = ord.vehicle_id "
                + "INNER JOIN ord_tank_client AS oc ON ord.tank_client_id = oc.id "
                + "INNER JOIN est_tank_category AS t ON oc.categ_id = t.id "
                + "INNER JOIN city AS ci ON oc.city_id = ci.id "
                + "INNER JOIN ord_office AS of ON of.id = ord.office_id "
                + "LEFT JOIN ord_channel AS ch ON ch.id = ord.channel_id ";

        if (descriptions) {
            str += "LEFT JOIN ord_cancel_cause AS c ON ord.cancel_cause_id = c.id ";
        }

        str += "WHERE "
                + " ?2 <= ord.`day` AND ?3 >= ord.`day` "
                + (officeId != null ? "AND ord.office_id = " + officeId + " " : "")
                + (oprId != null ? " AND o.taken_by_id = " + oprId + " " : "")
                + (vehId != null ? " AND v.id = " + vehId + " " : "")
                + (driverId != null ? " AND d.id = " + driverId + " " : "")
                + (channelId != null ? " AND ch.id = " + channelId + " " : " ");
        if (justif) {
            str += " AND ord.justif IS NOT NULL ";
        }

        switch (state) {
            case 1:
                str += "And ord.vehicle_id IS NOT NULL "
                        + " And ord.confirm_hour IS NOT NULL "
                        + " And ord.cancel_cause_id IS NULL ";
                break;
            case 2:
                str += " And ord.confirm_hour IS NULL "
                        + " And ord.cancel_cause_id IS NULL ";
                break;
            case 3:
                str += " And ord.cancel_cause_id IS NOT NULL ";
                break;
            default:
                break;
        }

        MySQLQuery rowsQ = new MySQLQuery(str);
        rowsQ.setParam(2, beginDate);
        rowsQ.setParam(3, endDate);
        Object[][] rows = rowsQ.getRecords(em);

        for (int i = 0; i < rows.length; i++) {
            Object[] row = new Object[22 + desc + offSet];
            tbl.addRow(row);
            int ordId = (Integer) rows[i][2];

            //ASIGNACIÓN
            row[0] = rows[i][0];//
            row[1] = rows[i][1];//
            row[2] = ordId;//
            row[3] = rows[i][3]; //fecha
            row[4] = rows[i][4]; //empresa
            row[5] = rows[i][5];//hora captura
            row[6] = rows[i][6];//hora asignacion
            row[7] = rows[i][7];//hora confirmacion
            if (getCallDt) {
                row[8] = rows[i][8];//hora contacto cliente
            }
            row[8 + offSet] = rows[i][8 + offSet];//operador
            row[9 + offSet] = rows[i][9 + offSet];//despachador
            row[10 + offSet] = rows[i][10 + offSet];//veh
            row[11 + offSet] = rows[i][11 + offSet];//Conductor
            row[12 + offSet] = rows[i][12 + offSet];//Nombre
            row[13 + offSet] = rows[i][13 + offSet];
            row[14 + offSet] = rows[i][14 + offSet];//Direccion
            row[15 + offSet] = rows[i][15 + offSet];//Tipo
            row[16 + offSet] = rows[i][16 + offSet];//Ciudad
            row[17 + offSet] = rows[i][17 + offSet];//Telefono

            if (descriptions) {
                row[18 + offSet] = rows[i][22 + offSet];
                row[19 + offSet] = rows[i][23 + offSet];
                row[20 + offSet] = rows[i][24 + offSet];
            }
            row[18 + desc + offSet] = rows[i][18 + offSet];//encuestado
            row[19 + desc + offSet] = rows[i][19 + offSet];//justificacion
            row[20 + desc + offSet] = rows[i][20 + offSet];//quejas
            row[21 + desc + offSet] = rows[i][21 + offSet];//cancelado
        }
        if (tbl.getData() != null && tbl.getData().length > 0) {
            rep.getTables().add(tbl);
        }
        return rep;
    }

    public static MySQLReport getChangedAddresses(Connection em) throws Exception {
        MySQLReport rep = new MySQLReport("Direcciones Modificadas", "", "Modificaciones", MySQLQuery.now(em));
        rep.setVerticalFreeze(0);
        rep.setHorizontalFreeze(0);
        rep.setZoomFactor(80);
        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        rep.getFormats().get(0).setWrap(true);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));
        rep.setVerticalFreeze(5);
        //Columnas
        List<Column> cols = new ArrayList<>();
        cols.add(new Column("Contrato", 12, 0));
        cols.add(new Column("Fecha Cto.", 12, 1));
        cols.add(new Column("Fecha Mod.", 12, 1));
        cols.add(new Column("Documento", 15, 0));
        cols.add(new Column("Nombre", 32, 0));
        cols.add(new Column("Dirección de Contrato", 35, 0));
        cols.add(new Column("Dirección de Entrega", 35, 0));
        Table tbl = new Table("Direcciones modificadas");
        tbl.setColumns(cols);
        //fin config

        String str = "SELECT "
                + "contract.contract_num, "//0
                + "contract.sign_date, "//1
                + "contract.mod_deliv, "//2
                + "contract.document, "//3
                + "CONCAT(contract.first_name, \" \", contract.last_name), "//4
                + "CONCAT(contract.address, \" \", n.`name`), "//5
                + "CONCAT(contract.deliv_address, \" \", nd.`name`) "
                + "FROM "
                + "contract "
                + "INNER JOIN neigh AS nd ON nd.id = contract.deliv_neigh_id "
                + "INNER JOIN neigh AS n ON n.id = contract.neigh_id "
                + "WHERE "
                + "contract.address <> contract.deliv_address OR "
                + "contract.deliv_neigh_id <> contract.neigh_id AND "
                + "contract.cancel_date IS NULL AND "
                + "contract.anull_cause_id IS NULL";

        MySQLQuery q = new MySQLQuery(str);
        Object[][] result = q.getRecords(em);
        for (int i = 0; i < result.length; i++) {
            tbl.addRow(result[i]);
        }
        if (tbl.getData() != null && tbl.getData().length > 0) {
            rep.getTables().add(tbl);
        }
        return rep;
    }

    public static MySQLReport getChangedPhones(Connection em) throws Exception {
        MySQLReport rep = new MySQLReport("Teléfonos Modificadas", "", "Modificaciones", MySQLQuery.now(em));
        rep.setVerticalFreeze(0);
        rep.setHorizontalFreeze(0);
        rep.setZoomFactor(80);
        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().get(0).setWrap(true);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//1
        rep.setVerticalFreeze(5);

        //Columnas
        List<Column> cols = new ArrayList<>();
        cols.add(new Column("Contrato", 12, 0));
        cols.add(new Column("Fecha Cto.", 12, 1));
        cols.add(new Column("Fecha Mod.", 12, 1));
        cols.add(new Column("Documento", 15, 0));
        cols.add(new Column("Nombre", 32, 0));
        cols.add(new Column("Teléfono de Contrato", 29, 0));
        cols.add(new Column("Teléfono de Entrega", 29, 0));
        Table tbl = new Table("Teléfonos modificadas");
        tbl.setColumns(cols);
        //fin config

        String str = "SELECT "
                + "contract.contract_num, "//0
                + "contract.sign_date, "//1
                + "contract.mod_deliv, "//2
                + "contract.document, "//3
                + "CONCAT(contract.first_name, \" \", contract.last_name), "//4
                + "contract.phones, "//5
                + "contract.deliv_phones "//6
                + "FROM "
                + "contract "
                + "WHERE "
                + "contract.phones <> contract.deliv_phones AND "
                + "contract.cancel_date IS NULL AND "
                + "contract.anull_cause_id IS NULL";

        MySQLQuery q = new MySQLQuery(str);
        Object[][] result = q.getRecords(em);
        for (int i = 0; i < result.length; i++) {
            tbl.addRow(result[i]);
        }
        if (tbl.getData() != null && tbl.getData().length > 0) {
            rep.getTables().add(tbl);
        }
        return rep;
    }

    public static MySQLReport getCylOrdersMonthlyReport(int repType, int officeId, int begYear, int endYear, int begMonth, int endMonth, Integer entId, String type, Connection em) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat df1 = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
        Date[][] dates = Dates.getDateList(begYear, endYear, begMonth, endMonth);

        //composición del filtro ppal
        String filt = "?2 <= o.`day` AND ?3 >= o.`day` AND o.office_id = " + officeId + " "
                + (entId != null ? " AND o.enterprise_id = " + entId + " " : "");
        if (type != null) {
            filt += " AND i.type = '" + type + "' ";
        }
        String filt1 = filt;//mismo filtro pero sin restricción por vehículo

        MySQLQuery rowsQ = null;
        String title = null;
        String colName = null;
        switch (repType) {
            case 1:
                //sectores
                rowsQ = new MySQLQuery("select s.id, concat(c.name, ' - ', s.name) from "
                        + "ord_office o "
                        + "inner join ord_office_city oc on oc.office_id = o.id "
                        + "inner join city c on c.id = oc.city_id "
                        + "inner join sector s on s.city_id = c.id "
                        + "where o.id = " + officeId + " "
                        + "order by c.name, s.name");
                filt += "AND n.sector_id = ?1  ";
                title = "Pedidos por Sector";
                colName = "Sector";
                break;
            case 2:
                //vehículos
                rowsQ = new MySQLQuery("SELECT DISTINCT "
                        + "o.vehicle_id, CONCAT(v.internal, \" - \", v.plate)  "//0
                        + "FROM "
                        + "ord_cyl_order o "
                        + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                        + "INNER JOIN ord_office of ON o.office_id = of.id "
                        + "INNER JOIN vehicle v ON v.id = o.vehicle_id "
                        + "WHERE o.cancel_cause_id IS NULL AND o.confirm_hour IS NOT NULL AND " + filt1);
                filt += "AND o.vehicle_id = ?1 ";
                title = "Pedidos por Vehículos";
                colName = "Vehículo";
                break;
            case 3:
                //conductores
                rowsQ = new MySQLQuery("SELECT DISTINCT "
                        + "o.driver_id, CONCAT(e.first_name, \" \", e.last_name) "//0
                        + "FROM "
                        + "ord_cyl_order o "
                        + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                        + "INNER JOIN ord_office of ON o.office_id = of.id "
                        + "INNER JOIN employee e ON e.id = o.driver_id "
                        + "WHERE o.cancel_cause_id IS NULL AND o.confirm_hour IS NOT NULL AND " + filt1);
                filt += "AND o.driver_id = ?1 ";
                title = "Pedidos por Conductor";
                colName = "Conductor";
                break;
            case 4:
                //operadores
                rowsQ = new MySQLQuery("SELECT DISTINCT "
                        + "o.taken_by_id, CONCAT(e.first_name, \" \", e.last_name) "//0
                        + "FROM "
                        + "ord_cyl_order o "
                        + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                        + "INNER JOIN ord_office of ON o.office_id = of.id "
                        + "INNER JOIN employee e ON e.id = o.taken_by_id "
                        + "WHERE o.cancel_cause_id IS NULL AND o.confirm_hour IS NOT NULL AND " + filt1);
                filt += "AND o.taken_by_id = ?1 ";
                title = "Pedidos por Operador";
                colName = "Operador";
                break;
            default:
                break;
        }

        MySQLQuery ordersQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_cyl_order o "
                + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                + "INNER JOIN ord_office of ON o.office_id = of.id "
                + "INNER JOIN neigh n ON o.neigh_id = n.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.confirm_hour IS NOT NULL AND " + filt);

        MySQLQuery galsQ = new MySQLQuery("SELECT "
                + "sum(ct.capacity * cto.amount) "//0
                + "FROM "
                + "ord_cyl_order AS o "
                + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                + "INNER JOIN ord_office of ON o.office_id = of.id "
                + "INNER JOIN ord_cyl_type_order AS cto ON cto.order_id = o.id "
                + "INNER JOIN cylinder_type AS ct ON ct.id = cto.cylinder_type_id "
                + "INNER JOIN neigh n ON o.neigh_id = n.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.confirm_hour IS NOT NULL AND " + filt);

        if (rowsQ == null) {
            throw new Exception("reqType desconocido");
        }

        rowsQ.setParam(2, dates[0][0]);
        rowsQ.setParam(3, dates[dates.length - 1][1]);

        Object[][] rows = rowsQ.getRecords(em);

        //REPORTE
        String dateRange = df.format(dates[0][0]) + " - " + df.format(dates[dates.length - 1][1]);
        MySQLReport rep = new MySQLReport(title, dateRange, "Hoja 1", MySQLQuery.now(em));
        rep.setVerticalFreeze(0);
        rep.setHorizontalFreeze(2);
        rep.setZoomFactor(80);
        //Subtitulos
        rep.getSubTitles().add(("Empresa: " + (entId != null ? new MySQLQuery("SELECT e.name FROM enterprise e WHERE e.id = ?1").setParam(1, entId).getAsString(em) : "Todas")) + (" ,Oficina: " + new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(em)));
        rep.getSubTitles().add("Tipo Contrato: " + (type != null ? (type.equals("brand") ? "Afiliado" : (type.equals("app") ? "App" : "Provisional")) : "Todos"));
        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.00"));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        //Columnas
        List<Column> cols = new ArrayList<>();
        cols.add(new Column("", 30, 2));//name

        Table tbl = new Table("Pedidos de Cilindros " + dateRange);
        tbl.setColumns(cols);
        //Cabecera
        TableHeader header = new TableHeader();
        tbl.getHeaders().add(header);
        header.getColums().add(new HeaderColumn(colName, 1, 2));
        //Totales
        tbl.setSummaryRow(new SummaryRow("Totales", 1));
        for (Date[] date : dates) {
            header.getColums().add(new HeaderColumn(df1.format(date[0]), 3, 1));
            cols.add(new Column("Pedidos", ORDS_WIDTH, 0));
            cols.add(new Column("Galones", GALS_WIDTH, 1));
            cols.add(new Column("Kilos", GALS_WIDTH, 1));
        }
        //fin config
        BigDecimal kte = new MySQLQuery("SELECT gal_to_kg_kte FROM sys_cfg").getAsBigDecimal(em, true);

        for (int i = 0; i < rows.length; i++) {
            Object[] row = new Object[(dates.length * 3) + 1];
            tbl.addRow(row);
            int rowId = (Integer) rows[i][0];
            String rowName = rows[i][1].toString();
            ordersQ.setParam(1, rowId);
            galsQ.setParam(1, rowId);
            row[0] = rowName;

            for (int j = 0; j < dates.length; j++) {
                Date[] dates1 = dates[j];
                //pedidos mes actual
                ordersQ.setParam(2, dates1[0]);
                ordersQ.setParam(3, dates1[1]);
                Long curPed = ordersQ.getAsLong(em);
                //GALONES mes actual
                galsQ.setParam(2, dates1[0]);
                galsQ.setParam(3, dates1[1]);
                BigDecimal curGal = galsQ.getAsBigDecimal(em, true);
                row[(j * 3) + 1] = curPed;//Atendidos
                row[(j * 3) + 2] = curGal;//Galones;
                row[(j * 3) + 3] = curGal.multiply(kte);//Galones;
            }
        }
        if (tbl.getData() != null && tbl.getData().length > 0) {
            rep.getTables().add(tbl);
        }
        return rep;
    }

    public static MySQLReport getTankOrdersMonthlyReport(int repType, int officeId, int begYear, int endYear, int begMonth, int endMonth, Integer entId, Connection em) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat df1 = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
        Date[][] dates = Dates.getDateList(begYear, endYear, begMonth, endMonth);

        //composición del filtro ppal
        String filt = "?2 <= o.`day` AND ?3 >= o.`day` AND o.office_id = " + officeId + " "
                + (entId != null ? " AND o.enterprise_id = " + entId + " " : "");

        String filt1 = filt;//mismo filtro pero sin restricción por vehículo

        MySQLQuery rowsQ = null;
        String title = null;
        String colName = null;

        switch (repType) {
            case 1:
                //tipo de cliente
                rowsQ = new MySQLQuery("SELECT DISTINCT "
                        + "ct.id, ct.description  "//0
                        + "FROM "
                        + "ord_tank_order o "
                        + "INNER JOIN ord_office of ON o.office_id = of.id "
                        + "INNER JOIN ord_tank_client c ON o.tank_client_id = c.id "
                        + "INNER JOIN est_tank_category ct ON c.categ_id = ct.id "
                        + "WHERE o.cancel_cause_id IS NULL AND o.confirm_hour IS NOT NULL AND " + filt1);
                filt += "AND c.categ_id = ?1  ";
                title = "Pedidos por Tipo de Cliente";
                colName = "Tipo";
                break;
            case 2:
                //vehículos
                rowsQ = new MySQLQuery("SELECT DISTINCT "
                        + "o.vehicle_id, CONCAT(v.internal, \" - \", v.plate)  "//0
                        + "FROM "
                        + "ord_tank_order o "
                        + "INNER JOIN ord_office of ON o.office_id = of.id "
                        + "INNER JOIN vehicle v ON v.id = o.vehicle_id "
                        + "WHERE o.cancel_cause_id IS NULL AND o.confirm_hour IS NOT NULL AND " + filt1);
                filt += "AND o.vehicle_id = ?1 ";
                title = "Pedidos por Vehículos";
                colName = "Vehículo";
                break;
            case 3:
                //conductores
                rowsQ = new MySQLQuery("SELECT DISTINCT "
                        + "o.driver_id, CONCAT(e.first_name, \" \", e.last_name) "//0
                        + "FROM "
                        + "ord_tank_order o "
                        + "INNER JOIN ord_office of ON o.office_id = of.id "
                        + "INNER JOIN employee e ON e.id = o.driver_id "
                        + "WHERE o.cancel_cause_id IS NULL AND o.confirm_hour IS NOT NULL AND " + filt1);
                filt += "AND o.driver_id = ?1 ";
                title = "Pedidos por Conductor";
                colName = "Conductor";
                break;
            case 4:
                //operadores
                rowsQ = new MySQLQuery("SELECT DISTINCT "
                        + "o.taken_by_id, CONCAT(e.first_name, \" \", e.last_name) "//0
                        + "FROM "
                        + "ord_tank_order o "
                        + "INNER JOIN ord_office of ON o.office_id = of.id "
                        + "INNER JOIN employee e ON e.id = o.taken_by_id "
                        + "WHERE o.cancel_cause_id IS NULL AND o.confirm_hour IS NOT NULL AND " + filt1);
                filt += "AND o.taken_by_id = ?1 ";
                title = "Pedidos por Operador";
                colName = "Operador";
                break;
            default:
                break;
        }

        MySQLQuery ordersQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_tank_order o "
                + "INNER JOIN ord_office of ON o.office_id = of.id "
                + "INNER JOIN ord_tank_client c ON o.tank_client_id = c.id "
                + "WHERE o.cancel_cause_id IS NULL AND o.confirm_hour IS NOT NULL AND " + filt);

        if (rowsQ == null) {
            throw new Exception("reqType desconocido");
        }
        rowsQ.setParam(2, dates[0][0]);
        rowsQ.setParam(3, dates[dates.length - 1][1]);

        Object[][] rows = rowsQ.getRecords(em);

        //REPORTE
        String dateRange = df.format(dates[0][0]) + " - " + df.format(dates[dates.length - 1][1]);
        MySQLReport rep = new MySQLReport(title, dateRange, "Hoja 1", MySQLQuery.now(em));
        rep.setVerticalFreeze(0);
        rep.setHorizontalFreeze(2);
        rep.setZoomFactor(80);
        //Subtitulos
        rep.getSubTitles().add(("Empresa: " + (entId != null ? new MySQLQuery("SELECT e.name FROM enterprise e WHERE e.id = ?1").setParam(1, entId).getAsString(em) : "Todas")) + (" ,Oficina: " + new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(em)));
        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.00"));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        //Columnas
        List<Column> cols = new ArrayList<>();
        cols.add(new Column(colName, 30, 2));//name

        Table tbl = new Table("Pedidos de Estacionarios " + dateRange);
        tbl.setColumns(cols);
        //Cabecera
        //
        //Totales
        tbl.setSummaryRow(new SummaryRow("Totales", 1));
        for (Date[] date : dates) {
            //header.getColums().add(new HeaderColumn(df1.format(dates.get(i)[0]), 1, 1));
            cols.add(new Column(df1.format(date[0]), 18, 0));
        }
        //fin config
        for (int i = 0; i < rows.length; i++) {
            Object[] row = new Object[dates.length + 1];
            tbl.addRow(row);
            int rowId = (Integer) rows[i][0];
            String rowName = rows[i][1].toString();
            ordersQ.setParam(1, rowId);
            row[0] = rowName;

            for (int j = 0; j < dates.length; j++) {
                Date[] dates1 = dates[j];
                ordersQ.setParam(2, dates1[0]);
                ordersQ.setParam(3, dates1[1]);
                row[j + 1] = ordersQ.getAsLong(em);
            }
        }
        if (tbl.getData() != null && tbl.getData().length > 0) {
            rep.getTables().add(tbl);
        }
        return rep;
    }

    public static MySQLReport getCylPollingReport(int pollVersionId, boolean punctual, int officeId, Date date, String typeCli, Connection em) throws Exception {
        String str = "SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_poll AS p "
                + "INNER JOIN ord_cyl_order AS o ON o.poll_id = p.id "
                + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                + "INNER JOIN ord_office AS of ON of.id = o.office_id "
                + "WHERE "
                + " SUBSTR(p.answer FROM ?1 FOR 1) = ?2 AND "
                + " p.poll_version_id = " + pollVersionId + " AND "
                + "?3 <= o.`day` AND ?4 >= o.`day` AND o.office_id = " + officeId + " "
                + "AND o.enterprise_id = ?5 ";
        if (typeCli != null) {
            str += " AND i.type = '" + typeCli + "' ";
        }
        MySQLQuery q = new MySQLQuery(str);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        //calculos con fechas
        GregorianCalendar curEndGc = new GregorianCalendar();
        curEndGc.setTime(date);
        GregorianCalendar curBegGc = new GregorianCalendar();
        curBegGc.setTime(date);
        curBegGc.set(GregorianCalendar.DAY_OF_MONTH, 1);

        Date curEnd = Dates.trimDate(curEndGc.getTime());
        Date curBeg = Dates.trimDate(curBegGc.getTime());

        MySQLReport rep = new MySQLReport("Encuestas de Satisfacción Pedidos Cilindros", (punctual ? "Solo de: " + df.format(date) : "Acumulado: " + df.format(curBeg) + " - " + df.format(curEnd)), "Hoja 1", MySQLQuery.now(em));
        //Subtitulos
        rep.getSubTitles().add("Oficina: " + new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(em));
        rep.getSubTitles().add("Tipo Contrato: " + (typeCli != null ? (typeCli.equals("brand") ? "Afiliado" : (typeCli.equals("app") ? "App" : "Provisional")) : "Todos"));
        polling(q, pollVersionId, rep, date, em);
        return rep;
    }

    //rpte encuestas derrotero
    public static MySQLReport getTankVisitPollReport(int pollVersionId, boolean punctual, Date date, Connection em) throws Exception {
        String str = "SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_tank_visit AS otv "
                + "INNER JOIN ord_poll AS poll ON poll.id = otv.poll_id "
                + "INNER JOIN ord_tank_client AS otc ON otc.id = otv.client_id "
                + "WHERE ?5 = 3 AND "
                + "SUBSTR(poll.answer FROM ?1 FOR 1) = ?2 AND "
                + "poll.poll_version_id = " + pollVersionId + " AND "
                + "?3 <= otc.last_poll_date AND ?4 >=otc.last_poll_date ";
        MySQLQuery q = new MySQLQuery(str);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        //calculos con fechas
        GregorianCalendar curEndGc = new GregorianCalendar();
        curEndGc.setTime(date);
        GregorianCalendar curBegGc = new GregorianCalendar();
        curBegGc.setTime(date);
        curBegGc.set(GregorianCalendar.DAY_OF_MONTH, 1);

        Date curEnd = Dates.trimDate(curEndGc.getTime());
        Date curBeg = Dates.trimDate(curBegGc.getTime());

        MySQLReport rep = new MySQLReport("Encuestas de Satisfacción Derrotero", (punctual ? "Solo de: " + df.format(date) : "Acumulado: " + df.format(curBeg) + " - " + df.format(curEnd)), "Hoja 1", MySQLQuery.now(em));
        polling(q, pollVersionId, rep, date, em);
        return rep;
    }

    public static MySQLReport getTankPollingReport(int pollVersionId, boolean punctual, int officeId, Date date, Connection em) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String str = "SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_poll AS p "
                + "INNER JOIN ord_tank_order AS o ON o.poll_id = p.id "
                + "INNER JOIN ord_office AS of ON of.id = o.office_id "
                + "WHERE "
                + " SUBSTR(p.answer FROM ?1 FOR 1) = ?2 AND "
                + " p.poll_version_id = " + pollVersionId + " AND "
                + "?3 <= o.`day` AND ?4 >= o.`day` AND o.office_id = " + officeId + " "
                + "AND o.enterprise_id = ?5 ";

        MySQLQuery q = new MySQLQuery(str);

        //calculos con fechas
        GregorianCalendar curEndGc = new GregorianCalendar();
        curEndGc.setTime(date);
        GregorianCalendar curBegGc = new GregorianCalendar();
        curBegGc.setTime(date);
        curBegGc.set(GregorianCalendar.DAY_OF_MONTH, 1);

        Date curEnd = Dates.trimDate(curEndGc.getTime());
        Date curBeg = Dates.trimDate(curBegGc.getTime());

        MySQLReport rep = new MySQLReport("Encuestas de Satisfacción Pedidos Estacionarios", (punctual ? "Solo de: " + df.format(date) : "Acumulado: " + df.format(curBeg) + " - " + df.format(curEnd)), "Hoja 1", MySQLQuery.now(em));
        rep.getSubTitles().add("Oficina: " + new MySQLQuery("SELECT description FROM ord_office WHERE id = " + officeId).getAsString(em));
        polling(q, pollVersionId, rep, date, em);
        return rep;
    }

    public static MySQLReport getCylOrderCancelReport(boolean punctual, int officeId, Date date, String typeCli, Connection em) throws Exception {
        String str = "SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_cyl_order AS ord "
                + "INNER JOIN ord_office AS of ON of.id = ord.office_id "
                + "INNER JOIN  ord_contract_index AS i ON i.id= ord.index_id "
                + "WHERE "
                + "ord.cancel_cause_id = ?1 "
                + (punctual ? "AND ?2 = ord.`day` " : "AND ?2 <= ord.`day` AND ?3 >= ord.`day` ")
                + "AND ord.office_id = " + officeId + " ";

        if (typeCli != null) {
            str += " AND i.type = '" + typeCli + "' ";
        }

        MySQLQuery q = new MySQLQuery(str);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        //calculos con fechas
        Date[][] dates = Dates.getDateList(date);
        Date curBeg = dates[0][0];
        Date curEnd = dates[0][1];

        MySQLReport rep = new MySQLReport("Cancelación Pedidos Cilindros", (punctual ? "Solo de: " + df.format(date) : "Acumulado: " + df.format(curBeg) + " - " + df.format(curEnd)), "Hoja 1", MySQLQuery.now(em));
        rep.getSubTitles().add("Oficina: " + getOfficeName(officeId, em));
        rep.getSubTitles().add("Tipo Contrato: " + (typeCli != null ? (typeCli.equals("brand") ? "Afiliado" : (typeCli.equals("app") ? "App" : "Provisional")) : "Todos"));

        SimpleDateFormat df1 = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
        rep.setVerticalFreeze(0);
        rep.setHorizontalFreeze(0);
        rep.setZoomFactor(80);

        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//1
        //Columnas
        List<Column> cols = new ArrayList<>();
        cols.add(new Column("Opción", 48, 0));

        if (punctual) {
            cols.add(new Column("Cantidad", 20, 1));
        } else {
            for (Date[] date1 : dates) {
                cols.add(new Column(df1.format(date1[0]), 20, 1));
            }
        }
        Table tblMod = new Table("");
        tblMod.setColumns(cols);

        //Fin config
        Table tbl = new Table(tblMod);

        Object[][] causesData = new MySQLQuery("select c.id, c.description  from ord_cancel_cause c order by c.description;").getRecords(em);
        for (int i = 0; i < causesData.length; i++) {
            Object[] causeRow = causesData[i];
            List<Object> row = new ArrayList<>();
            row.add(causeRow[1]);
            q.setParam(1, causeRow[0]);
            //actual
            long aux = 0;
            if (punctual) {
                q.setParam(2, dates[0][1]);
                aux = q.getAsLong(em);
                row.add(aux);
            } else {
                for (Date[] date1 : dates) {
                    q.setParam(2, date1[0]);
                    q.setParam(3, date1[1]);
                    Long lc = q.getAsLong(em);
                    row.add(lc);
                    aux = aux + lc;
                }
            }
            if (aux > 0) {
                tbl.addRow(row.toArray());
            }
        }

        if (tbl.getData() != null && tbl.getData().length > 0) {
            tbl.setSummaryRow(new SummaryRow(punctual ? "Total" : "Totales", 1));
            rep.getTables().add(tbl);
        }
        return rep;
    }

    public static MySQLReport getCylOrderCancelAppReport(Date begDate, Date endDate, int officeId, String typeCli, Connection em) throws Exception {
        String str = "SELECT "
                + "COUNT(*) "
                + "FROM ord_cyl_order o "
                + "INNER JOIN ord_contract_index AS i ON i.id= o.index_id "
                + "WHERE o.cancel_cause_id = ?1 "
                + "AND (o.`day` BETWEEN ?2 AND ?3) "
                + "AND o.office_id = " + officeId + " ";

        if (typeCli != null) {
            str += " AND i.type = '" + typeCli + "' ";
        }

        MySQLQuery q = new MySQLQuery(str);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        //calculos con fechas
        Date curBeg = Dates.trimDate(begDate);
        Date curEnd = Dates.trimDate(endDate);

        MySQLReport rep = new MySQLReport("Cancelación Pedidos App Clientes", " ", "Hoja 1", MySQLQuery.now(em));
        rep.getSubTitles().add("Oficina: " + getOfficeName(officeId, em));
        rep.getSubTitles().add("Tipo Contrato: " + (typeCli != null ? (typeCli.equals("brand") ? "Afiliado" : (typeCli.equals("app") ? "App" : "Provisional")) : "Todos"));
        rep.getSubTitles().add("Periodo: " + df.format(begDate) + " hasta " + df.format(endDate));

        SimpleDateFormat df1 = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
        rep.setVerticalFreeze(0);
        rep.setHorizontalFreeze(0);
        rep.setZoomFactor(80);

        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//1
        //Columnas
        List<Column> cols = new ArrayList<>();
        cols.add(new Column("Opción", 48, 0));
        cols.add(new Column("Cantidad", 20, 1));

        Table tblMod = new Table("");
        tblMod.setColumns(cols);

        //Fin config
        Table tbl = new Table(tblMod);

        Object[][] causesData = new MySQLQuery("select c.id, c.description  from ord_cancel_cause c where c.`type` = 'clie' order by c.description;").getRecords(em);
        for (int i = 0; i < causesData.length; i++) {
            Object[] causeRow = causesData[i];
            List<Object> row = new ArrayList<>();
            row.add(causeRow[1]);
            q.setParam(1, causeRow[0]);
            //actual
            q.setParam(2, curBeg);
            q.setParam(3, curEnd);
            Long lc = q.getAsLong(em);
            row.add(lc);
            tbl.addRow(row.toArray());

        }

        if (tbl.getData() != null && tbl.getData().length > 0) {
            tbl.setSummaryRow(new SummaryRow("Total", 1));
            rep.getTables().add(tbl);
        }
        return rep;
    }

    public static MySQLReport getTankOrderCancelReport(boolean punctual, int officeId, Date date, Connection em) throws Exception {
        String str = "SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_tank_order AS ord "
                + "INNER JOIN ord_office AS of ON of.id = ord.office_id "
                + "WHERE "
                + "ord.cancel_cause_id = ?1 "
                + (punctual ? "AND ?2 = ord.`day` " : "AND ?2 <= ord.`day` AND ?3 >= ord.`day` ")
                + "AND ord.office_id = " + officeId + " ";

        MySQLQuery q = new MySQLQuery(str);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        //calculos con fechas
        Date[][] dates = Dates.getDateList(date);
        Date curBeg = dates[0][0];
        Date curEnd = dates[0][1];

        MySQLReport rep = new MySQLReport("Cancelación Pedidos Estacionarios", (punctual ? "Solo de: " + df.format(date) : "Acumulado: " + df.format(curBeg) + " - " + df.format(curEnd)), "Hoja 1", MySQLQuery.now(em));
        rep.getSubTitles().add("Oficina: " + getOfficeName(officeId, em));

        SimpleDateFormat df1 = new SimpleDateFormat("MMMM yyyy", new Locale("es", "ES"));
        rep.setVerticalFreeze(0);
        rep.setHorizontalFreeze(0);
        rep.setZoomFactor(80);

        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//1
        //Columnas
        List<Column> cols = new ArrayList<>();
        cols.add(new Column("Opción", 48, 0));

        if (punctual) {
            cols.add(new Column("Cantidad", 20, 1));
        } else {
            for (Date[] date1 : dates) {
                cols.add(new Column(df1.format(date1[0]), 20, 1));
            }
        }
        Table tblMod = new Table("");
        tblMod.setColumns(cols);

        //Fin config
        Table tbl = new Table(tblMod);

        Object[][] causesData = new MySQLQuery("select c.id, c.description  from ord_cancel_cause c order by c.description;").getRecords(em);
        for (int i = 0; i < causesData.length; i++) {
            Object[] causeRow = causesData[i];
            List<Object> row = new ArrayList<>();
            //row.add(j + 1);
            row.add(causeRow[1]);
            q.setParam(1, causeRow[0]);
            //actual
            long aux = 0;
            if (punctual) {
                q.setParam(2, dates[0][1]);
                aux = q.getAsLong(em);
                row.add(aux);
            } else {
                for (Date[] date1 : dates) {
                    q.setParam(2, date1[0]);
                    q.setParam(3, date1[1]);
                    Long lc = q.getAsLong(em);
                    row.add(lc);
                    aux = aux + lc;
                }
            }
            if (aux > 0) {
                tbl.setSummaryRow(new SummaryRow(punctual ? "Total" : "Totales", 1));
                tbl.addRow(row.toArray());
            }
        }

        if (tbl.getData() != null && tbl.getData().length > 0) {
            rep.getTables().add(tbl);
        }
        return rep;
    }

    public static MySQLReport getRepurchaseReport(int officeId, int maxDesv, Date minDate, Connection em) throws Exception {
        MySQLReport rep = new MySQLReport("Recompra", "", "Hoja 1", MySQLQuery.now(em));
        rep.setVerticalFreeze(5);
        rep.setHorizontalFreeze(0);
        rep.setZoomFactor(85);
        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.00"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//2
        rep.getFormats().get(0).setWrap(true);
        if (maxDesv > 0) {
            //Columnas
            List<Column> cols = new ArrayList<>();
            cols.add(new Column("Documento", 15, 0));//0
            cols.add(new Column("Nombres", 25, 0));//1
            cols.add(new Column("Dirección", 25, 0));//2
            cols.add(new Column("Teléfono", 15, 0));//3
            cols.add(new Column("Promedio", 15, 1));//4
            cols.add(new Column("Desviación", 15, 1));//5
            cols.add(new Column("Fecha", 15, 2));//6
            Table tbl = new Table("Recompra");
            tbl.setColumns(cols);
            //fin config
            String str = "SELECT "
                    + "i.document, "//0
                    + "CONCAT(i.first_name,\" \",i.last_name), "//1
                    + "i.address, "//2
                    + "i.phones, "//3
                    + "AVG(o.dist), "//4
                    + "STDDEV_SAMP(o.dist), "//5
                    + "DATE_ADD(MAX(o.`day`), INTERVAL AVG(o.dist) DAY) "//6
                    + "FROM "
                    + "ord_cyl_order AS o "
                    + "INNER JOIN ord_contract_index AS i ON o.index_id = i.id "
                    + "INNER JOIN ord_office AS of ON of.id = o.office_id "
                    + "WHERE "
                    + "o.confirm_hour IS NOT NULL "
                    //+ "AND o.office_id = " + officeId + " "
                    + "AND o.`day` > ?1 "
                    + "AND o.dist > 0 "
                    + "AND o.office_id = " + officeId + " "
                    + "GROUP BY "
                    + "o.index_id "
                    + "HAVING STDDEV_POP(o.dist) <= " + maxDesv + " AND MAX(o.`day`)>= DATE(NOW()) ";
            MySQLQuery q = new MySQLQuery(str);
            q.setParam(1, minDate);
            Object[][] result = q.getRecords(em);
            for (int i = 0; i < result.length; i++) {
                tbl.addRow(result[i]);
            }
            if (tbl.getData() != null && tbl.getData().length > 0) {
                rep.getTables().add(tbl);
            }
        } else {
            //Columnas
            rep.getSubTitles().add("Clientes que comprarán el " + new SimpleDateFormat("dd/MM/yyyy").format(minDate));
            List<Column> cols = new ArrayList<>();
            cols.add(new Column("Documento", 15, 0));//0
            cols.add(new Column("Nombres", 30, 0));//1
            cols.add(new Column("Dirección", 25, 0));//2
            cols.add(new Column("Teléfono", 15, 0));//3
            cols.add(new Column("Promedio", 15, 1));//3
            Table tbl = new Table("Recompra");
            tbl.setColumns(cols);
            String str = "SELECT "
                    + "ind.document, CONCAT(ind.first_name, ' ', ind.last_name), ind.address, ind.phones, ind.ord_avg "
                    + "FROM ord_contract_index AS ind WHERE ind.next_order = ?1";
            MySQLQuery q = new MySQLQuery(str);
            q.setParam(1, Dates.trimDate(minDate));
            Object[][] result = q.getRecords(em);
            for (int i = 0; i < result.length; i++) {
                tbl.addRow(result[i]);
            }
            if (tbl.getData() != null && tbl.getData().length > 0) {
                rep.getTables().add(tbl);
            }
        }
        return rep;
    }

    public static MySQLReport getClientsUnivBrand(Connection em) throws Exception {
        String str = "SELECT "
                + "concat(i.first_name,\" \",i.last_name), "//0
                + "i.document, "//1
                + "c.`name`, "//2
                + "group_concat(address) "//3
                + " "//4
                + "FROM "
                + "ord_contract_index AS i "
                + "INNER JOIN city AS c ON i.city_id = c.id "
                + "Where "
                + "i.active=1 "
                + "GROUP BY "
                + "i.document "
                + "HAVING "
                + "count(*) <> sum(brand) AND "
                + "sum(brand) > 0";

        MySQLReport rep = new MySQLReport("Revisión Provisionales Duplicados ", "", "clts_univ_marc", MySQLQuery.now(em));

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        rep.getFormats().get(0).setWrap(true);
        rep.setVerticalFreeze(4);

        MySQLQuery q = new MySQLQuery(str);
        Object[][] clts = q.getRecords(em);

        if (clts.length > 0) {
            List<Column> cols = new ArrayList<>();
            cols.add(new Column("Nombre", 30, 0));
            cols.add(new Column("Documento", 12, 0));
            cols.add(new Column("Ciudad", 15, 0));
            cols.add(new Column("Direcciones", 45, 0));

            Table tbl = new Table("");
            tbl.setColumns(cols);
            int c = 0;
            for (int i = 0; i < clts.length; i++) {
                tbl.addRow(clts[i]);
                c++;
            }

            rep.getSubTitles().add(c + "clientes encontrados");
            rep.getTables().add(tbl);
        }
        return rep;
    }

    // Metodos para los reportes de Asistencia Técnica//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public static MySQLReport getDetailedRepairs(Integer entId, Date beginDate, Date endDate, Integer oprId, int state, Integer officeId, Integer tecId, Integer channelId, Connection em) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        MySQLReport rep = new MySQLReport("Detallado Asistencia Técnica", "Período " + df.format(beginDate) + " - " + df.format(endDate), "det_asit_tect", MySQLQuery.now(em));
        OrdCfg cfg = new OrdCfg().select(1, em);

        rep.setVerticalFreeze(6);
        rep.setHorizontalFreeze(0);
        rep.setZoomFactor(80);

        //Subtitulos
        rep.getSubTitles().add(("Empresa: " + (entId != null ? getEnterpriseById(entId, em) : "Todas")) + (" ,Oficina: " + (officeId != null ? getOfficeName(officeId, em) : "Todas las Oficinas")));

        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.00"));//3
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "HH:mm:ss"));//4
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));//5
        rep.getFormats().get(1).setWrap(true);

        //Columnas
        int columns = 11;
        List<Column> cols = new ArrayList<>();
        cols.add(new Column("Oficina", 11, 1));
        cols.add(new Column("Canal", 12, 1));
        cols.add(new Column("Solicitud", 11, 0));

        if (cfg.enterprise) {
            cols.add(new Column("Empresa", 10, 1));
            columns++;
        }
        cols.add(new Column("Motivo", 30, 1));
        if (cfg.subreason) {
            cols.add(new Column("Dlle. Motivo", 30, 1));
            columns++;
        }

        cols.add(new Column("PQR", 14, 1));
        cols.add(new Column("Capturado", 12, 2));
        cols.add(new Column("Hora Cap.", 13, 4));
        cols.add(new Column("Confirmado", 13, 2));
        cols.add(new Column("Cancelado", 12, 2));
        cols.add(new Column("Operador", 30, 1));
        cols.add(new Column("Técnico", 30, 1));
        cols.add(new Column("Tipo", 15, 1));
        if (cfg.showPqrNotes) {
            cols.add(new Column("Notas", 35, 1));
            columns++;
        }
        cols.add(new Column("Clase", 15, 1));
        cols.add(new Column("Documento", 13, 1));
        cols.add(new Column("Cliente", 25, 1));
        cols.add(new Column("Teléfono", 15, 1));
        cols.add(new Column("Dirección", 35, 1));
        cols.add(new Column("Observaciones", 35, 1));
        cols.add(new Column("Días", 12, 5));
        cols.add(new Column("Oportuno", 12, 0));
        cols.add(new Column("Confirmado", 13, 0));
        cols.add(new Column("Cancel", 10, 0));
        cols.add(new Column("Actividad", 40, 1));

        Table tbl = new Table("Detallado Asistencia Técnica");
        tbl.setColumns(cols);
        TableHeader header = new TableHeader();
        tbl.getHeaders().add(header);
        header.getColums().add(new HeaderColumn("Datos Solicitud", columns, 1));
        header.getColums().add(new HeaderColumn("Datos Cliente", 7, 1));
        header.getColums().add(new HeaderColumn("Tiempo Respuesta", 2, 1));
        header.getColums().add(new HeaderColumn("Estado", 2, 1));
        header.getColums().add(new HeaderColumn("Actividades", 1, 2));

        tbl.setSummaryRow(new SummaryRow("Totales", columns + 7));

        String repairs = "SELECT "
                + "of.sname, "//0
                + "ch.name, "//0
                + "pqr.serial, " //1
                + (cfg.enterprise ? "e.`short_name`, " : "")
                + "r.description, "//2
                + (cfg.subreason ? " subr.description, " : "")
                + "COALESCE(pcyl.serial, ptank.serial, ''), "
                + "pqr.`regist_date`, "//3
                + "pqr.`regist_hour`, "//4
                + "pqr.`confirm_date`, "//5
                + "pqr.`cancel_date`, "//6
                + "CONCAT(o.first_name, \" \" , o.last_name) AS oper, "//7
                + "CONCAT(t.first_name, \" \" , t.last_name) AS tec, "//8
                + "IF(client.id IS NOT NULL, type.description ,IF(i.type = 'brand','Afiliado', IF(i.type = 'univ', 'Provisional', IF(i.type = 'app', 'App', null)))), "//9
                + (cfg.showPqrNotes ? " pqr.notes, " : "")
                + " CASE " // tipo 
                + "WHEN pqr.index_id IS NOT NULL THEN 'Cilindros' "
                + "WHEN pqr.build_id IS NOT NULL THEN 'Estacionarios' "
                + "WHEN pqr.client_id IS NOT NULL AND client.build_ord_id IS NOT NULL THEN 'Facturación' "
                + "ELSE 'Redes' "
                + "END ,"
                + "IF(client.id IS NOT NULL, client.doc, IF(i.id IS NOT NULL,i.document, IF(build.id IS NOT NULL ,build.document,b.document) )), "//11
                + "IF(client.id IS NOT NULL, "
                + "CONCAT(client.first_name, \" \", COALESCE(client.last_name, '')), "
                + "IF(i.id IS NOT NULL,CONCAT(i.first_name, \" \" , i.last_name), IF(build.id IS NOT NULL ,build.name,b.name))), "//11
                + "IF(client.id IS NOT NULL, client.phones, IF(i.id IS NOT NULL,i.phones, IF(build.id IS NOT NULL ,build.phones,b.phones) )), "//11
                + " CASE "//direccion 
                + "WHEN pqr.index_id IS NOT NULL THEN CONCAT(i.address, IF(n.`name` IS NOT NULL, CONCAT(' ', n.`name`), ''), ' [', COALESCE(c.name,''),']') "
                + "WHEN pqr.build_id IS NOT NULL THEN CONCAT(b.address,' [', COALESCE(c.name,''),']') "
                + "WHEN pqr.client_id IS NOT NULL AND client.build_ord_id IS NOT NULL THEN CONCAT(build.address,' ',build.name,' [', COALESCE(c.name,''),']')  "
                + "ELSE CONCAT(ne.name,' ',client.address)  "
                + "END ,"
                + "poll.notes,"
                + "IF(DATEDIFF(pqr.confirm_date,pqr.regist_date)>=0,DATEDIFF(pqr.confirm_date,pqr.regist_date),null), "//15
                + "IF(DATEDIFF(pqr.confirm_date,pqr.regist_date)>=0,IF(DATEDIFF(pqr.confirm_date,pqr.regist_date) <= " + cfg.repairLimitTime + ",1,0),null), " //16
                + "iF(pqr.pqr_poll_id IS NUll,0,1), "//17
                + "iF(pqr.anul_cause_id IS NUll,0,1), "//18
                + "pqr.id "
                + "FROM "
                + "ord_repairs AS pqr "
                + "INNER JOIN ord_office AS of ON pqr.office_id = of.id "
                + "INNER JOIN ord_technician as t ON pqr.technician_id=t.id "
                + "INNER JOIN employee AS o ON o.id = pqr.regist_by "
                + (cfg.enterprise ? "LEFT JOIN enterprise AS e ON e.id = pqr.enterprise_id  " : "")
                + "INNER JOIN ord_pqr_reason as r ON pqr.reason_id=r.id "
                + "LEFT JOIN ord_pqr_client_tank as client ON client.id=pqr.client_id "//facturacion 
                + "LEFT JOIN ord_tank_client as build ON build.id=client.build_ord_id "
                + "LEFT JOIN ord_tank_client as b ON b.id = pqr.build_id "//pqr a nombre del edificio
                + "LEFT JOIN est_tank_category as type ON type.id=build.categ_id "
                + "LEFT JOIN ord_contract_index as i ON pqr.index_id=i.id " //contratos 
                + "LEFT JOIN neigh AS n ON n.id = i.neigh_id "//barrio cilindros 
                + "LEFT JOIN ord_poll as poll ON pqr.pqr_poll_id = poll.id "
                + (cfg.subreason ? "LEFT JOIN ord_pqr_subreason subr ON subr.id = pqr.subreason_id " : "")
                + "LEFT JOIN ord_channel AS ch ON ch.id = pqr.channel_id "
                + "LEFT JOIN city c ON c.id = IF(i.id IS NOT NULL, i.city_id, build.city_id )"
                + "LEFT JOIN neigh AS ne ON ne.id = client.neigh_id "//barrio redes 
                + "LEFT JOIN ord_pqr_cyl pcyl ON pcyl.id = pqr.pqr_cyl_id "
                + "LEFT JOIN ord_pqr_tank ptank ON ptank.id = pqr.pqr_tank_id "
                + "WHERE "
                + " ?2 <= pqr.regist_date AND "
                + " ?3 >= pqr.regist_date "
                + (officeId != null ? "AND of.id = " + officeId + " " : " ")
                + (entId != null && cfg.enterprise ? " AND pqr.enterprise_id = " + entId + " " : "")
                + (oprId != null ? " AND o.id = " + oprId + " " : "")
                + (channelId != null ? " AND ch.id = " + channelId + " " : " ");

        switch (state) {
            case 1:
                //atendidos
                repairs += " And pqr.pqr_poll_id IS NOT NULL  ";
                break;
            case 2:
                //no atendidos
                repairs += " And pqr.pqr_poll_id IS NULL "
                        + " And pqr.anul_cause_id IS NULL ";
                break;
            case 3:
                //cancelados
                repairs += " And pqr.anul_cause_id IS NOT NULL ";
                break;
            default:
                break;
        }
        repairs += " GROUP BY pqr.id ";
        MySQLQuery rowsQ = new MySQLQuery(repairs);

        rowsQ.setParam(2, beginDate);
        rowsQ.setParam(3, endDate);
        Object[][] rows = rowsQ.getRecords(em);

        if (rows != null && rows.length > 0) {
            for (Object[] row : rows) {
                int id = MySQLQuery.getAsInteger(row[24]);

                Object[][] novs = new MySQLQuery("SELECT DISTINCT a.activity, a.observation, a.act_date, IF(bfile.id IS NOT NULL,'(Si)','(No)') "
                        + "FROM ord_activity_pqr a "
                        + "LEFT JOIN bfile ON a.id = bfile.owner_id AND bfile.owner_type = 20 "
                        + "WHERE repair_id = ?1 ORDER BY a.act_date").setParam(1, id).getRecords(em);

                StringBuilder stringNov = new StringBuilder("");
                if (novs != null && novs.length > 0) {
                    for (Object[] nov : novs) {
                        stringNov.append(getString(nov[0])).append(" ").append(getString(nov[1])).append(" ").append(getString(nov[2])).append(" ").append(getString(nov[3])).append("\n");
                    }
                }
                row[24] = stringNov.toString();
            }

            tbl.setData(rows);
            rep.getTables().add(tbl);
        }
        return rep;
    }

    private static String getString(Object obj) {
        return obj != null && obj.toString().length() > 0 ? MySQLQuery.getAsString(obj) : "";
    }

    public static MySQLReport getRepairsCancel(boolean punctual, int officeId, Date date, Connection em) throws Exception {
        String str = "SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_repairs AS pq "
                + "INNER JOIN ord_office AS of ON of.id = pq.office_id "
                + "WHERE "
                + "pq.anul_cause_id = ?1 "
                + "AND ?2 <= pq.`regist_date` AND ?3 >= pq.`regist_date` AND pq.office_id = " + officeId + " "
                + "AND pq.enterprise_id = ?4 ";

        MySQLQuery q = new MySQLQuery(str);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        Date[][] dates = Dates.getDateList(date);
        Date curBeg = dates[0][0];
        Date curEnd = dates[0][1];

        MySQLReport rep = new MySQLReport("Solicitudes de Asistencia Técnica Canceladas", (punctual ? "Solo de: " + df.format(date) : "Acumulado: " + df.format(curBeg) + " - " + df.format(curEnd)), "Hoja 1", MySQLQuery.now(em));
        rep.getSubTitles().add("Oficina: " + getOfficeName(officeId, em));
        List<Enterprise> ents = Enterprise.getAll(em);

        SimpleDateFormat df1 = new SimpleDateFormat("MMMM yyyy");
        rep.setVerticalFreeze(0);
        rep.setHorizontalFreeze(0);
        rep.setZoomFactor(80);

        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//1
        //Columnas
        List<Column> cols = new ArrayList<>();
        cols.add(new Column("Opción", 48, 0));

        for (int j = 0; j < 3; j++) {
            for (Enterprise ent : ents) {
                cols.add(new Column(ent.shortName, 8, 1));
            }
            cols.add(new Column("Total", 10, 1));
        }

        TableHeader head = new TableHeader();
        head.getColums().add(new HeaderColumn("Opción", 1, 2));
        for (Date[] date1 : dates) {
            head.getColums().add(new HeaderColumn(df1.format(date1[0]), ents.size() + 1, 1));
        }

        Table tblMod = new Table("");
        tblMod.setColumns(cols);
        tblMod.getHeaders().add(head);

        //Fin config
        Table tbl = new Table(tblMod);
        List<OrdPqrAnulCause> causes = OrdPqrAnulCause.getPqrAnulCauses("repair", em);
        for (OrdPqrAnulCause cause : causes) {
            List<Object> row = new ArrayList<>();
            //row.add(j + 1);
            row.add(cause.description);
            q.setParam(1, cause.id);
            for (Date[] date1 : dates) {
                long total = 0;
                q.setParam(2, date1[0]);
                q.setParam(3, date1[1]);
                for (Enterprise ent : ents) {
                    q.setParam(4, ent.id);
                    Long lc = q.getAsLong(em);
                    row.add(lc);
                    total += lc;
                }
                row.add(total);
            }
            tbl.addRow(row.toArray());
        }

        if (tbl.getData() != null && tbl.getData().length > 0) {
            rep.getTables().add(tbl);
        }
        return rep;
    }

    public static MySQLReport getRepairsPollingReport(int pollVersionId, boolean punctual, int officeId, Date date, Connection em) throws Exception {
        Date[][] dates = Dates.getDateList(date);
        Date curBeg = dates[0][0];
        Date curEnd = dates[0][1];
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        String sub = punctual ? "Solo de: " + df.format(date) : "Acumulado: " + df.format(curBeg) + " - " + df.format(curEnd);
        return getRepairsPQRPollingReport(pollVersionId, officeId, dates, sub, em);
    }

    public static MySQLReport getRepairsPollingMonthlyReport(int pollVersionId, int officeId, int begYear, int endYear, int begMonth, int endMonth, Connection em) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date[][] dates = Dates.getDateList(begYear, endYear, begMonth, endMonth);
        String dateRange = df.format(dates[0][0]) + " - " + df.format(dates[dates.length - 1][1]);
        return getRepairsPQRPollingReport(pollVersionId, officeId, dates, dateRange, em);
    }

    private static MySQLReport getRepairsPQRPollingReport(int pollVersionId, int officeId, Date[][] dates, String subTitle, Connection em) throws Exception {
        String str = "SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_poll AS p "
                + "INNER JOIN ord_repairs AS pq ON pq.pqr_poll_id = p.id "
                + "INNER JOIN ord_office AS of ON of.id = pq.office_id "
                + "WHERE "
                + " SUBSTR(p.answer FROM ?1 FOR 1) = ?2 AND "
                + " p.poll_version_id = " + pollVersionId + " AND "
                + "?3 <= pq.`regist_date` AND ?4 >= pq.`regist_date` AND pq.office_id = " + officeId + " "
                + "AND pq.enterprise_id = ?5 ";
        MySQLQuery q = new MySQLQuery(str);
        MySQLReport rep = new MySQLReport("Solicitudes Asistencia Técnica", subTitle, "Hoja 1", MySQLQuery.now(em));
        rep.getSubTitles().add("Oficina: " + getOfficeName(officeId, em));
        polling(q, pollVersionId, rep, dates, em);
        return rep;
    }

    public static MySQLReport getRepairsByTech(boolean punctual, int officeId, Date date, Integer entId, Connection em) throws Exception {
        OrdCfg cfg = new OrdCfg().select(1, em);

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat df1 = new SimpleDateFormat("MMMM yyyy");
        //calculos con fechas
        Date[][] dates = Dates.getDateList(date);
        Date curBeg = dates[0][0];
        Date curEnd = dates[0][1];
        Date lmBeg = dates[1][0];
        Date lmEnd = dates[1][1];
        Date lyBeg = dates[2][0];
        Date lyEnd = dates[2][1];
        double day = Dates.getDayOfMonth(date);
        double maxDay = Dates.getMaxDayOfMonth(date);
        //composición del filtro ppal
        String filt = " ( pqr.regist_date BETWEEN ?2 AND ?3 ) AND pqr.office_id = " + officeId + " "
                + (entId != null ? " AND IF(pqr.enterprise_id IS NOT NULL, pqr.enterprise_id = " + entId + ",TRUE) " : "");

        MySQLQuery rowsQ;
        String colName;
        rowsQ = new MySQLQuery("SELECT DISTINCT "
                + "pqr.technician_id, "
                + "CONCAT(t.first_name, \" \", t.last_name) "//0
                + "FROM "
                + "ord_repairs pqr "
                + "INNER JOIN ord_office as o ON pqr.office_id = o.id "
                + "INNER JOIN ord_technician as t ON pqr.technician_id=t.id "
                + "WHERE " + filt);
        filt += " AND pqr.technician_id = ?1 ";
        colName = "Técnicos";

        MySQLQuery repairsQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_repairs pqr "
                + "INNER JOIN ord_office as o ON pqr.office_id = o.id "
                + "WHERE "
                + "pqr.anul_cause_id IS NULL AND "
                + "pqr.confirm_date IS NOT NULL AND " + filt);

        MySQLQuery inTimeQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_repairs pqr "
                + "INNER JOIN ord_office as o ON pqr.office_id = o.id "
                + "WHERE "
                + "pqr.anul_cause_id IS NULL AND "
                + "pqr.confirm_date IS NOT NULL "
                + "AND IF(DATEDIFF(pqr.confirm_date,pqr.regist_date) <= " + cfg.repairLimitTime + ",true,false) AND " //16
                + filt);

        MySQLQuery unatQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_repairs pqr "
                + "INNER JOIN ord_office as o ON pqr.office_id = o.id "
                + "WHERE "
                + "pqr.anul_cause_id is NULL AND "
                + "pqr.confirm_date IS NULL AND " + filt);

        MySQLQuery cancelQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_repairs pqr "
                + "INNER JOIN ord_office as o ON pqr.office_id = o.id "
                + "WHERE "
                + "pqr.anul_cause_id IS NOT NULL AND " + filt);

        MySQLQuery polledQ = new MySQLQuery("SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_repairs pqr "
                + "INNER JOIN ord_office as o ON pqr.office_id = o.id "
                + "WHERE  pqr.anul_cause_id  IS NULL AND  "
                + "pqr.confirm_date IS NOT NULL AND "
                + "pqr.pqr_poll_id IS NOT NULL AND " + filt);

        rowsQ.setParam(2, punctual ? curEnd : curBeg);
        rowsQ.setParam(3, curEnd);

        Object[][] rows = rowsQ.getRecords(em);

        //REPORTE
        MySQLReport rep = new MySQLReport("Solitudes Atendidas por Técnico", (punctual ? "Solo de: " + df.format(date) : "Acumulado: " + df.format(curBeg) + " - " + df.format(curEnd)), "Hoja 1", MySQLQuery.now(em));
        rep.setVerticalFreeze(0);
        rep.setHorizontalFreeze(2);
        rep.setZoomFactor(80);
        //Subtitulos
        rep.getSubTitles().add(("Empresa: " + (entId != null ? getEnterpriseById(entId, em) : "Todas")) + "Oficina: " + getOfficeName(officeId, em));

        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.00"));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        //Columnas

        List<Column> cols = new ArrayList<>();
        cols.add(new Column("", 30, 2));//name
        cols.add(new Column("Cancelados", 14, 0));
        cols.add(new Column("Encuestados", 14, 0));
        cols.add(new Column("No Atend.", 14, 0));
        cols.add(new Column("Atendidos", 14, 0));
        cols.add(new Column("A tiempo", 14, 0));
        cols.add(new Column("Solicitudes", 25, 0));
        cols.add(new Column("Solicitudes", 16, 0));
        cols.add(new Column("Solicitudes", 16, 0));
        Table tbl = new Table("Solicitudes de Asistencia Técnica " + (punctual ? df.format(date) : df.format(curBeg) + " - " + df.format(curEnd)));
        tbl.setColumns(cols);
        //Cabecera
        TableHeader header = new TableHeader();
        tbl.getHeaders().add(header);
        header.getColums().add(new HeaderColumn(colName, 1, 2));
        header.getColums().add(new HeaderColumn("Solicitudes", 5, 1));
        header.getColums().add(new HeaderColumn("Proyec. " + df1.format(curBeg), 1, 1));
        header.getColums().add(new HeaderColumn(df1.format(lmBeg), 1, 1));
        header.getColums().add(new HeaderColumn(df1.format(lyBeg), 1, 1));
        //Totales
        tbl.setSummaryRow(new SummaryRow("Totales", 2));

        //fin config
        for (int i = 0; i < rows.length; i++) {
            Object[] row = new Object[9];
            tbl.addRow(row);
            int rowId = (Integer) rows[i][0];
            String rowName = rows[i][1].toString();
            repairsQ.setParam(1, rowId);
            inTimeQ.setParam(1, rowId);
            cancelQ.setParam(1, rowId);
            polledQ.setParam(1, rowId);
            unatQ.setParam(1, rowId);

            //cancelados actual
            cancelQ.setParam(2, punctual ? curEnd : curBeg);
            cancelQ.setParam(3, curEnd);
            Long curCancel = cancelQ.getAsLong(em);

            //encuestados actual
            polledQ.setParam(2, punctual ? curEnd : curBeg);
            polledQ.setParam(3, curEnd);
            Long curPolled = polledQ.getAsLong(em);

            //no atendidos mes actual
            unatQ.setParam(2, punctual ? curEnd : curBeg);
            unatQ.setParam(3, curEnd);
            Long curUnat = unatQ.getAsLong(em);

            //pqrs mes actual
            repairsQ.setParam(2, punctual ? curEnd : curBeg);
            repairsQ.setParam(3, curEnd);
            Long curPed = repairsQ.getAsLong(em);

            //a tiempo
            inTimeQ.setParam(2, punctual ? curEnd : curBeg);
            inTimeQ.setParam(3, curEnd);
            Long inTime = inTimeQ.getAsLong(em);

            //pqrs mes anterior
            repairsQ.setParam(2, lmBeg);
            repairsQ.setParam(3, lmEnd);
            Long lmPed = repairsQ.getAsLong(em);

            //pqrs año anterior
            repairsQ.setParam(2, lyBeg);
            repairsQ.setParam(3, lyEnd);
            Long lyPed = repairsQ.getAsLong(em);

            //proyección
            repairsQ.setParam(2, curBeg);
            repairsQ.setParam(3, curEnd);
            Long proyPed = (long) ((repairsQ.getAsLong(em) / day) * maxDay);

            //ASIGNACIÓN
            row[0] = rowName;
            row[1] = curCancel;//cancelados
            row[2] = curPolled;//encuestados
            row[3] = curUnat;//no atendidos
            row[4] = curPed;//Atendidos
            row[5] = inTime;//a tipo
            row[6] = proyPed;//Pedidos;
            row[7] = lmPed;//mes pasado Pedidos;
            row[8] = lyPed;//año pasado Pedidos;
        }
        if (tbl.getData() != null && tbl.getData().length > 0) {
            rep.getTables().add(tbl);
        }
        return rep;
    }

    public static MySQLReport getRepairsSatisPollingReport(int pollVersionId, boolean punctual, int officeId, Date date, Connection em) throws Exception {
        String str = "SELECT "
                + "COUNT(*) "//0
                + "FROM "
                + "ord_poll AS p "
                + "INNER JOIN ord_repairs AS pq ON pq.satis_poll_id = p.id "
                + "INNER JOIN ord_office AS of ON of.id = pq.office_id "
                + "WHERE "
                + " SUBSTR(p.answer FROM ?1 FOR 1) = ?2 AND "
                + " p.poll_version_id = " + pollVersionId + " AND "
                + "pq.`regist_date` BETWEEN ?3 AND ?4 AND pq.office_id = " + officeId + " "
                + "AND pq.enterprise_id = ?5 ";

        MySQLQuery q = new MySQLQuery(str);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        //calculos con fechas
        Date[][] dates = Dates.getDateList(date);
        Date curBeg = dates[0][0];
        Date curEnd = dates[0][1];

        MySQLReport rep = new MySQLReport("Encuestas de Satisfacción Solicitudes de Asistencia Técnica", (punctual ? "Solo de: " + df.format(date) : "Acumulado: " + df.format(curBeg) + " - " + df.format(curEnd)), "Hoja 1", MySQLQuery.now(em));
        rep.getSubTitles().add("Oficina: " + getOfficeName(officeId, em));
        polling(q, pollVersionId, rep, date, em);
        return rep;
    }
}
