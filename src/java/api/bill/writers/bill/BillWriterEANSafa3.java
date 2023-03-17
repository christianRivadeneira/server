package api.bill.writers.bill;

import api.bill.model.BillInstance;
import api.bill.model.BillSpan;
import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfReader;
import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import utilities.Dates;
import utilities.Reports;
import utilities.Strings;

public class BillWriterEANSafa3 extends BillWriterPDF {

    private final SimpleDateFormat consMonthFormat = new SimpleDateFormat("MMM yy");
    private final SimpleDateFormat shortDateFormat = new SimpleDateFormat("d MMM yy");
    private final SimpleDateFormat normalDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
    private final SimpleDateFormat dateExpedition = new SimpleDateFormat("d MMM yy hh:mm a");
    private final DecimalFormat moneyFormat = new DecimalFormat("$###,###,###,##0.00");
    private final DecimalFormat readingFormat = new DecimalFormat("###,###,###,##0.00");
    private final DecimalFormat readingShortFormat = new DecimalFormat("###,###,###,##0");
    private final DecimalFormat numberFormat = new DecimalFormat("###,###,###,##0.00");
    private final DecimalFormat factorFormat = new DecimalFormat("0.00");
    private int monthsLimit;
    private String cityName;

    @Override
    protected PdfReader getReader(boolean printLogo) throws Exception {
        if (printLogo) {
            return new PdfReader(Reports.getPatternStream(ifNull(sysCfg.customBillTemplate, "billTemplateEANSafa3.pdf"), this));
        } else {
            return new PdfReader(Reports.getPatternStream(ifNull(sysCfg.customBillTemplateNoLogos, "billTemplateEANSafa3_sin_logos.pdf"), this));
        }
    }

    @Override
    protected void drawFact(BillInstance inst, BillForPrint bill, BillSpan span, int j) throws Exception {
        if (!bill.isTotal) {
            addWatermark("ABONO PARCIAL", j, "car2");
        } else if (bill.months >= monthsLimit - 1) {
            addWatermark("AVISO DE CORTE", j, "car2");
        }

        prepareNewBill(j);

        String facNum = bill.billNum;
        String address = bill.address + " " + bill.buildingType + " " + bill.clieApto;
        String names = bill.clientName;
        String meter = bill.meter;
        String instNum = bill.instNum;
        String instNumShort = bill.instNum;

        BigDecimal total = bill.total;
        BigDecimal curRead = bill.currRead;
        BigDecimal antRead = bill.lastRead;
        BigDecimal costGLP = bill.gplPrice;
        BigDecimal costCons = bill.currRead.subtract(bill.lastRead).multiply(bill.factor).multiply(costGLP).setScale(2, RoundingMode.HALF_UP);
        BigDecimal power = span.power;
        BigDecimal divisor = span.divisor;
        //BigDecimal facComp = new BigDecimal(span.getFacComp());
        //double kwe = span.getKwhEner();
        Date beginDate = span.beginDate;
        Date endDate = span.endDate;
        Date expDate = bill.creationDate;
        Date limDate = span.limitDate;
        List<Double> cons = bill.consumos;
        LineForPrint[] lines = bill.detLines.toArray(new LineForPrint[0]);;

        /**
         * **INICIO UN SOLO CÓDIGO DE BARRAS *****************
         */
        Image barCode = getBarCode(facNum, total, limDate, 57);
        barCode.scalePercent(101f);
        addImage("barCode2", pos, barCode, j);
        /**
         * **FIN UN SOLO CÓDIGO DE BARRAS *****************
         */
//        Image barCode = getBarCode(facNum, total, limDate, 40);
//        addImage(cb, "barCode2", pos, barCode, j);
//        addImage(cb, "barCode3", pos, barCode, j);

        //addText("address", Strings.toTitleType(address), j);
        addText("address", Strings.toTitleType(address), j);
        addText("building", Strings.toTitleType(bill.buildingName), j);

        setFont(bfb, 10);
        addTextCenter("elecRef", sysCfg.billUseCode ? (bill.isTotal ? bill.clieCode : "No disponible") : bill.instNum, j);

        setFont(bf, 9);

        addText("price_list", (bill.priceList != null ? bill.priceList.toUpperCase() : ""), j);
        addText("city", cityName, j);

        if (bill.months >= monthsLimit - 1) {
            addText("months", bill.months + "- Aviso de Corte", j);
        } else {
            addText("months", bill.months + "", j);
        }
        addText("names", Strings.toTitleType(names), j);
        addText("span", Strings.toTitleType(shortDateFormat.format(beginDate) + " - " + shortDateFormat.format(endDate)), j);
        addText("meter", meter, j);
        addText("instNum1", instNum, j);
        addText("billNum1", bill.billNum, j);

        String exped = dateExpedition.format(expDate);
        String strTotal = moneyFormat.format(total);
        String limit = (bill.months >= monthsLimit - 1 ? "Pago inmediato" : shortDateFormat.format(limDate));

        addText("billNum2", (bill.isTotal ? bill.clieCode : "No disponible"), j);
//        addTextRot("exped2", exped, j);
        addText("instNum2", instNumShort, j);
        addText("total2", strTotal, j);
        addText("limitDate2", limit, j);
        setFont(bf, 7);

        addText("lastCon1", bill.lastPayDate, j);
        addText("lastCon2", bill.lastPayBank, j);
        addText("lastCon3", moneyFormat.format(bill.lastPayValue), j);

//        addTextRot("billNum3", bill.billNum, j);
//        addTextRot("exped3", exped, j);
//        addTextRot("instNum3", instNumShort, j);
//        addTextRot("total3", strTotal, j);
//        addTextRot("limitDate3", limit, j);
        setFont(bfb, 6);
        addText("exped1", exped, j);
        addText("limitDate1", limit, j);
        addText("total1", strTotal, j);

        if (bill.factor != null && !bill.factor.equals(BigDecimal.ONE)) {
            addTextCenter("txtConsumM3", "Consumos en m3 con factor " + factorFormat.format(bill.factor), j);
        } else {
            addTextCenter("txtConsumM3", "Consumos en m3", j);
        }
        setFont(bf, 9);

        addText("lectAct", readingFormat.format(curRead) + "m3", j);
        addText("lectAnt", readingFormat.format(antRead) + "m3", j);
        addText("valGLP", moneyFormat.format(costGLP), j);
//        addText("costCons", moneyFormat.format(costCons), j);

        addText("poder", numberFormat.format(power) + " MJ/m3", j);

        drawBars(cons, j, consMonthFormat, readingFormat, readingShortFormat, span.limitDate, sysCfg.skipZeros, new Color(220, 220, 220), 45);

        if (cons.size() > 0) {
            BigDecimal kwhGLP = curRead.subtract(antRead).multiply(power).divide(divisor, RoundingMode.HALF_EVEN).setScale(2, RoundingMode.HALF_UP);
            addText("kwhglp", numberFormat.format(kwhGLP), j);
            if (kwhGLP.compareTo(BigDecimal.ZERO) != 0) {
                addText("costoKWG", numberFormat.format(costCons.divide(kwhGLP, RoundingMode.HALF_EVEN)), j);
            } else {
                addText("costoKWG", numberFormat.format(BigDecimal.ZERO), j);
            }
        }

        setFont(bf, 7);
        drawBanks("banks", j);
        if(bill.c3 != null && bill.c4 != null && bill.c5 != null && bill.agua != null){
            addText("leg1", "Calidad del GLP", j);
            addText("leg2", "Propano: "+String.format("%,.2f", bill.c3!=null?bill.c3:BigDecimal.ZERO)+ "% Butano: " +
                String.format("%,.2f", bill.c4!=null?bill.c4:BigDecimal.ZERO) + "%", j);
            addText("leg3", "C5 + Pesados: "+String.format("%,.2f", bill.c5!=null?bill.c5:BigDecimal.ZERO)+ "% Agua: " +
                String.format("%,.0f", bill.agua!=null?bill.agua:BigDecimal.ZERO), j);
        }
        if(bill.review){
            if(bill.dateReview!=null){
                Date maxDate=getMaxDate(bill.dateReview);
                Date minDate=getMinDate(maxDate);
                Date suspDate=getDateSusp(maxDate);
                Date curDate=new Date();
                String statusReview="";
                if(curDate.before(minDate)){
                    statusReview="Vigente";
                }
                else if(curDate.after(minDate) && curDate.before(maxDate)|| curDate.equals(minDate)||curDate.equals(maxDate)){
                    statusReview="Próx. Vencimiento";
                }
                else if(curDate.after(maxDate)){
                    statusReview="Vencida";
                }
                drawTitleRevision("leg4", j,statusReview);
                drawRevision("leg5", j,bill.nameReview,normalDateFormat.format(bill.dateReview),statusReview,normalDateFormat.format(maxDate),normalDateFormat.format(suspDate));
            }
        }
        
        addText("leg1", billCfg.legend1 != null ? billCfg.legend1 : "", j);
        addText("leg2", billCfg.legend2 != null ? billCfg.legend2 : "", j);
        addText("leg3", billCfg.legend3 != null ? billCfg.legend3 : "", j);
        addText("leg4", billCfg.legend4 != null ? billCfg.legend4 : "", j);
        addText("leg5", billCfg.legend5 != null ? billCfg.legend5 : "", j);
        addText("leg6", billCfg.legend6 != null ? billCfg.legend6 : "", j);
        drawMainBody(lines, j, moneyFormat, total, 7);
    }
    public static Date getDateSusp(Date dateMax){
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(dateMax);
        gc.add(GregorianCalendar.MONTH, 1);
        gc.set(GregorianCalendar.DAY_OF_MONTH,gc.getActualMinimum(GregorianCalendar.DAY_OF_MONTH));
        Date suspDate = Dates.trimDate(gc.getTime());
        return suspDate;
    }
    public static Date getMaxDate(Date dateReview){
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(dateReview);
        gc.add(GregorianCalendar.YEAR, 5);
        gc.set(GregorianCalendar.DAY_OF_MONTH,gc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
        if (gc.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SUNDAY) {
                gc.add(GregorianCalendar.DAY_OF_MONTH, -1);
            }
        Date maxDate = Dates.trimDate(gc.getTime());
        return maxDate;
            }
    public static Date getMinDate(Date maxDate){
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(maxDate);
        gc.add(GregorianCalendar.MONTH, -5);
        Date minDate = Dates.trimDate(gc.getTime());
        return minDate;
    }

    @Override
    protected Document createDocument() {
        return new Document(new Rectangle(612, 936));
    }

    @Override
    public void prepare(Connection ep) throws Exception {
        monthsLimit = inst.suspDebtMonths;
        cityName = inst.name;
    }

    private static int getUTCDays(Date d) {
        return (int) ((d.getTime() + TimeZone.getDefault().getOffset(d.getTime())) / 86400000d);
    }

}
