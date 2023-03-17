package api.bill.writers.bill;

import api.bill.model.BillInstance;
import api.bill.model.BillSpan;
import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfReader;
import java.awt.Color;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import utilities.MySQLQuery;
import utilities.Reports;
import utilities.Strings;

public class BillWriterCLC extends BillWriterPDF {

    private final SimpleDateFormat consMonthFormat = new SimpleDateFormat("MMM yy");
    private final SimpleDateFormat shortDateFormat = new SimpleDateFormat("d MMM yy");
    private final DecimalFormat moneyFormat = new DecimalFormat("$###,###,###,##0.00");
    private final DecimalFormat readingFormat = new DecimalFormat("###,###,###,##0.000");
    private final DecimalFormat factorFormat = new DecimalFormat("0.0000");
    private final DecimalFormat fcFormat = new DecimalFormat("0.000000");
    private final DecimalFormat barFormat = new DecimalFormat("#,##0.0");

    private int monthsLimit;
    private final Map<Integer, String> buildCities = new HashMap<>();
    private final Map<Integer, String> stratums = new HashMap<>();

    @Override
    protected PdfReader getReader(boolean printLogo) throws Exception {
        if (printLogo) {
            return new PdfReader(Reports.getPatternStream("templateCLC.pdf", this));
        } else {
            return new PdfReader(Reports.getPatternStream("templateCLC.pdf", this));
        }
    }

    @Override
    protected void drawFact(BillInstance inst, BillForPrint bill, BillSpan span, int j) throws Exception {
        String stratum = null;
        if (!stratums.containsKey(bill.billBuildingId)) {
            stratum = new MySQLQuery("SELECT v.`data` "
                    + "FROM sys_frm_value v "
                    + "INNER JOIN sigma.sys_frm_field f ON v.field_id=f.id "
                    + "WHERE f.id=94 AND v.owner_id=" + bill.billBuildingId).getAsString(billConn);
            if (stratum != null) {
                NumberFormat df2 = new DecimalFormat("#");
                stratums.put(bill.billBuildingId, MySQLQuery.getAsString(df2.format(MySQLQuery.getAsBigDecimal(stratum, true))));
            } else {
                stratums.put(bill.billBuildingId, "N/A");
            }
        }

        double corrFac = span.fac1.doubleValue() * span.fac2.doubleValue();
        String cityName;
        if (buildCities.containsKey(bill.billBuildingId)) {
            cityName = buildCities.get(bill.billBuildingId);
        } else {
            cityName = new MySQLQuery("select p.name from "
                    + "ord_tank_client tc "
                    + "inner join city c on c.id = tc.city_id "
                    + "inner join dane_poblado p ON c.dane_code = p.code "
                    + "where tc.bill_instance_id = ?1 and tc.mirror_id = ?2").setParam(1, inst.id).setParam(2, bill.billBuildingId).getAsString(billConn);
            if (cityName != null) {
                buildCities.put(bill.billBuildingId, cityName);

            } else {

                throw new RuntimeException("No se encuentra la ciudad del edificio: " + bill.buildingName + " id = " + bill.billBuildingId);
            }
        }

        if (!bill.isTotal) {
            addWatermark("ABONO PARCIAL", j, "car1");
        } else if (bill.months >= monthsLimit - 1) {
            addWatermark("AVISO DE CORTE", j, "car1");
        }

        prepareNewBill(j);

        String facNum = bill.billNum;
        String address = bill.address;
        String names = bill.clientName;
        String meter = bill.meter;

        BigDecimal total = bill.total;
        BigDecimal curRead = bill.currRead;
        BigDecimal antRead = bill.lastRead;

        BigDecimal kg = curRead.subtract(antRead).multiply(span.getM3ToGalKte()).multiply(bill.factor != null ? bill.factor : BigDecimal.ONE).multiply(span.galToKgKte);

        BigDecimal costGLP = bill.gplPrice;
        Date beginDate = span.beginDate;
        Date endDate = span.endDate;
        Date expDate = bill.creationDate;
        Date limDate = span.limitDate;
        LineForPrint[] lines = bill.detLines.toArray(new LineForPrint[0]);
        String exped = shortDateFormat.format(expDate);
        String strTotal = moneyFormat.format(total);

        String limit;
        String maxPayDate;

        if (bill.months >= monthsLimit - 1) {
            limit = "Inmediato";
            maxPayDate = "Inmediato";
        } else {
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(limDate);
            gc.add(GregorianCalendar.DAY_OF_MONTH, -6);
            maxPayDate = shortDateFormat.format(gc.getTime());
            limit = shortDateFormat.format(limDate);
        }

        setFont(bfb, 10, Color.WHITE);

        addTextCenter("elecRef", sysCfg.billUseCode ? (bill.isTotal ? bill.clieCode : "No disponible") : bill.instNum, j);
        setFont(bf, 9, Color.BLACK);
        addText("billNum1", facNum, j);
        addText("codEmp", "5458", j);
        addText("exped1", exped, j);
        addText("names", Strings.toTitleType(names), j);
        addText("clieDoc", bill.clieDoc, j);
        addText("apto", bill.clieApto, j);
        addText("city", cityName, j);
        addText("building", Strings.toTitleType(bill.buildingName), j);
        addText("address", Strings.toTitleType(address), j);

        setFont(bfb, 11, Color.WHITE);
        addText("total1", strTotal, j);
        addText("limitDate1", maxPayDate, j);
        addText("limitDate", limit, j);

        setFont(bf, 9, Color.BLACK);
        addText("meter", meter, j);
        addText("lectAct", readingFormat.format(curRead) + " m3", j);
        addText("lectAnt", readingFormat.format(antRead) + " m3", j);
        addText("valGLP", moneyFormat.format(costGLP), j);
        addText("gals", readingFormat.format(kg), j);

        addText("gasType", "GLP", j);
        addText("span", Strings.toTitleType(shortDateFormat.format(beginDate) + " - " + shortDateFormat.format(endDate)), j);
        addText("readDates", Strings.toTitleType(shortDateFormat.format(beginDate) + " - " + shortDateFormat.format(endDate)), j);
        addText("readTypes", "Reales", j);
        //addText("activity", "Residencial", j);

        addText("stratum", stratums.get(bill.billBuildingId), j);
        addText("consm3", readingFormat.format(curRead.subtract(antRead).multiply(new BigDecimal(corrFac))), j);

        addText("fcTemp", span.fac1 != null ? fcFormat.format(span.fac1) : "No definido", j);
        addText("fcPresion", span.fac1 != null ? fcFormat.format(span.fac2) : "No definido", j);
        addText("fcVL", span.fac1 != null ? fcFormat.format(span.fac3) : "No definido", j);
        addText("use", "Residencial", j);

        setFont(bfb, 9, Color.WHITE);
        if (bill.months >= monthsLimit - 1) {
            addText("months", bill.months + "- Aviso de Corte", j);
        } else {
            addText("months", bill.months + "", j);
        }

        setFont(bfb, 9, Color.BLACK);
        if (bill.factor != null && !bill.factor.equals(BigDecimal.ONE)) {
            addTextCenter("txtConsumM3", "Histórico de Consumos en m3 con factor " + factorFormat.format(bill.factor), j);
        } else {
            addTextCenter("txtConsumM3", "Histórico de Consumos en m3", j);
        }

        List<Double> rawCons = bill.consumos;
        List<Double> corrCons = new ArrayList<>(rawCons.size());

        for (int i = 0; i < rawCons.size(); i++) {
            corrCons.add(rawCons.get(i) * corrFac);
        }

        drawBars(corrCons, j, consMonthFormat, barFormat, barFormat, span.limitDate, sysCfg.skipZeros, new Color(170, 170, 170), 45);
        drawMainBody(lines, j, moneyFormat, total, 10);

        setFont(bf, 7, Color.BLACK);
        addText("galToKg", "Equivalencias: Valor GLP en KW/h: 0,5432.    Factor de Conversión de gal a kg: " + factorFormat.format(span.galToKgKte), j);
    }

    @Override
    protected Document createDocument() {
        return new Document(new Rectangle(612, 792));
    }

    @Override
    public void prepare(Connection ep) throws Exception {
        monthsLimit = inst.suspDebtMonths;
    }
}
