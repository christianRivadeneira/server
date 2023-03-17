package api.bill.writers.bill;

import api.bill.model.BillClieCau;
import api.bill.model.BillClientTank;
import api.bill.model.BillInstance;
import api.bill.model.BillMarketProdQuality;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import utilities.Reports;
import utilities.Strings;

public class BillWriterNetPdf extends BillWriterPDF {

    private final SimpleDateFormat consMonthLongFormat = new SimpleDateFormat("MMMM yyyy");
    private final SimpleDateFormat consMonthFormat = new SimpleDateFormat("MMM yy");
    private final SimpleDateFormat shortDateFormat = new SimpleDateFormat("d MMM yyyy");
    private final SimpleDateFormat dateExpedition = new SimpleDateFormat("d MMM yy hh:mm a");
    private final DecimalFormat moneyFormat = new DecimalFormat("$,##0.00");
    private final DecimalFormat numberFormat = new DecimalFormat(",##0.00");
    private final DecimalFormat readingFormat = new DecimalFormat(",##0.00");
    private final DecimalFormat readingShortFormat = new DecimalFormat(",##0");
    private final DecimalFormat factorFormat = new DecimalFormat("0.00");
    private final DecimalFormat shortFormat = new DecimalFormat("#.00");
    private int monthsLimit;
    private List<BillMarketProdQuality> prodQualityInds;

    @Override
    protected PdfReader getReader(boolean printLogo) throws Exception {
        if (printLogo) {
            return new PdfReader(Reports.getPatternStream(ifNull(sysCfg.customBillTemplate, "billTemplateNet.pdf"), this));
        } else {
            return new PdfReader(Reports.getPatternStream(ifNull(sysCfg.customBillTemplateNoLogos, "billTemplateNet.pdf"), this));
        }
    }

    @Override
    protected void drawFact(BillInstance inst, BillForPrint bill, BillSpan span, int j) throws Exception {
        if (!bill.isTotal) {
            addWatermark("ABONO PARCIAL", j, "car1");
        } else if (bill.months >= monthsLimit - 1) {
            addWatermark("AVISO DE CORTE", j, "car1");
        }

        prepareNewBill(j);

        String facNum = bill.billNum;
        String names = bill.clientName;

        BigDecimal total = bill.total;
        BigDecimal curRead = bill.currRead;
        BigDecimal antRead = bill.lastRead;
        String elecRef = sysCfg.billUseCode ? (bill.isTotal ? bill.clieCode : "No disponible") : bill.instNum;

        //BigDecimal facComp = new BigDecimal(span.getFacComp());
        //double kwe = span.getKwhEner();
        Date beginDate = span.beginDate;
        Date endDate = span.endDate;
        Date expDate = bill.creationDate;
        Date limDate = span.limitDate;
        List<Double> cons = bill.consumos;
        LineForPrint[] lines = bill.detLines.toArray(new LineForPrint[0]);
        String limit = bill.months >= monthsLimit - 1 || !bill.isTotal ? "Pago Inmediato" : shortDateFormat.format(limDate);
        setFont(bfb, 9);

        addTextRight("cycle", inst.cycle, bf, 8, j);
        addTextRight("billNum1", bill.billNum, bf, 8, j);
        addTextRight("elecRef", elecRef, bf, 8, j);
        addTextRight("clie_code", bill.clieCode, bf, 8, j);
        addTextRight("span1", Strings.toTitleType(shortDateFormat.format(beginDate) + " - " + shortDateFormat.format(endDate)), bf, 7, j);
        if (bill.months >= monthsLimit - 1) {
            addTextRight("months", bill.months + " - Aviso de Corte", bf, 8, j);
        } else {
            addTextRight("months", bill.months + "", bf, 8, j);
        }
        addTextRight("exped1", dateExpedition.format(expDate), bf, 7, j);
        addTextRight("limitDate1", limit, bf, 8, j);
        addTextRight("phone", bill.cliePhone, bf, 8, j);
        addTextRight("stratum", bill.stratum != null ? bill.stratum + "" : "", bf, 8, j);
        if (bill.isTotal) {
            addTextRight("suspDate", shortDateFormat.format(span.suspDate), bf, 8, j);
        } else {
            addTextRight("suspDate", "No aplica", bf, 8, j);
        }

        setFont(bf, 8);
        
        addText("names", names, j);
        addText("address", Strings.toTitleType(bill.address + " " + bill.buildingName), j);
        addTextRight("document", bill.clieDoc, bf, 8, j);
        addTextRight("price_list", BillClientTank.getSectorDescription(bill.sectorType), bf, 8, j);
        addTextRight("status", bill.clientStatus, bf, 8, j);
        addTextRight("meter", bill.meter, bf, 8, j);
        addTextRight("city", pobName, bf, 8, j);

        if (bill.lastPayDate != null && !bill.lastPayDate.isEmpty()) {
            addText("lastCon1", bill.lastPayDate + " por " + moneyFormat.format(bill.lastPayValue), j);
            addText("lastCon2", bill.lastPayBank, j);
        } else {
            addText("lastCon1", "No registra", j);
            addText("lastCon2", "No registra", j);
        }
        if (bill.lastInstCheck != null) {
            addText("net_check1", "Última Rev. de Instalaciones: " + shortDateFormat.format(bill.lastInstCheck), j);
            addText("net_check2", "Recuerde Reportar su Certificado de Conformidad antes de " + shortDateFormat.format(bill.maxInstCheck), j);
            addText("net_check3", "para evitar suspensiones. Mayor información en la línea 3222222323", j);
            addText("net_check4", "Línea gratuita nal 018000914080 - #876", j);
        }
        System.out.println("Resultado del bool "+bill.tarifaPlena);
        /*if(bill.resultFinal!=(BigDecimal.ZERO) && bill.tarifaPlena){
            addText("mne","Convenio 65-6250: $"+String.format("%,.2f", bill.resultFinal!=null?bill.resultFinal:BigDecimal.ZERO),j);
            }
        else if (bill.tarifaPlena){
            addText("mne","Convenio 65-6250: $"+String.format("%,.2f", bill.resultFinal!=null?bill.resultFinal:BigDecimal.ZERO),j);
        }*/
        //aportes publicos
        if(bill.tarifaPlena){
            addText("mne","Convenio 65-6250: $"+String.format("%,.2f", bill.resultFinal!=null?bill.resultFinal:BigDecimal.ZERO),j);
        }
        setFont(bfb, 9, Color.WHITE);
        if (bill.factor != null && bill.factor.compareTo(BigDecimal.ONE) != 0 && bill.isTotal) {
            addTextCenter("txtConsumM3", "Consumos Históricos con Factor de Corrección " + factorFormat.format(bill.factor), j);
        } else {
            addTextCenter("txtConsumM3", "Consumos Históricos", j);
        }

        setFont(bfb, 10, Color.BLACK);
        addTextRight("total1", moneyFormat.format(total), bfb, 10, j);

        if (bill.isTotal) {
            setFont(bf, 8, Color.BLACK);
            addText("lectAct", bill.readingFaultDescription != null && !bill.readingFaultDescription.isEmpty() ? "No disponible" : readingFormat.format(curRead) + "m3", j);
            addText("lectAnt", readingFormat.format(antRead) + "m3", j);
            addText("cons", readingFormat.format(curRead.subtract(antRead)) + "m3", j);
            if (bill.factor != null) {
                addText("corrCons", readingFormat.format(curRead.subtract(antRead).multiply(bill.factor)) + "m3", j);
            } else {
                addText("corrCons", readingFormat.format(curRead.subtract(antRead)) + "m3", j);
            }
            setFont(bf, 7);
            addText("reading_fault_desc", bill.readingFaultDescription, j);
        }
        if (bill.isTotal) {
            drawBars(cons, j, consMonthFormat, readingFormat, readingShortFormat, span.limitDate, sysCfg.skipZeros, new Color(220, 220, 220), 83, false);
        }

        if (bill.isTotal) {
            setFont(bf, 7);
            if (bill.ranges.length > 0) {
                for (int i = 0; i < bill.ranges.length; i++) {
                    DtoRangeToPrint r = bill.ranges[i];
                    addText("tarif" + i + "_0", r.label, j);
                    addTextRight("tarif" + i + "_1", factorFormat.format(r.cons) + "m3", bf, 7, j);
                    addTextRight("tarif" + i + "_2", moneyFormat.format(r.vunit), bf, 7, j);
                    addTextRight("tarif" + i + "_3", moneyFormat.format(r.total), bf, 7, j);
                }
            } else {
                addText("tarif0_0", "Sin Consumo", j);
            }
        } else {
            setFont(bfb, 10);
            addTextCenter("tarif1_1", "Abono Parcial", j);
        }

        setFont(bf, 7);
        if (!bill.isTotal) {
            setFont(bfb, 10);
            addTextCenter("services", "Abono Parcial", j);
        } else if (bill.ranges.length > 0) {
            drawServices(bill.srvs, j, shortFormat, "services", 6, 0);
        } else {
            addText("finan0_0", "Ninguno", j);
        }

        setFont(bf, 7);

        if (bill.isTotal) {
            List<BillParam> billPriceFactor = getBillParameters(bill, bill.cau, span, factorFormat, this);
            drawParams(billPriceFactor, j, "tarifs", 7, 0);
        } else {
            setFont(bfb, 10);
            addTextCenter("tarifs", "Abono Parcial", j);
        }

        drawProductQuality(prodQualityInds, j, "prodQuality", 7, 0);

        Image barCode = getBarCode(facNum, total, limDate, 57);
        barCode.scalePercent(101f);
        addImage("barCode2", pos, barCode, j);

        setFont(bf, 7);

        setFont(bf, 9);

//        addText("valGLP", moneyFormat.format(costGLP), j);
//        addText("costCons", moneyFormat.format(costCons), j);
//        addText("poder", numberFormat.format(power) + " MJ/m3", j);
        //los clientes nuevos no tienen información de consumo.
        if (cons.size() > 0) {
            //BigDecimal kwhGLP = curRead.subtract(antRead).multiply(power).divide(divisor, RoundingMode.HALF_EVEN).setScale(2, RoundingMode.HALF_UP);
            ///addText("kwhglp", numberFormat.format(kwhGLP), j);
            //if (kwhGLP.compareTo(BigDecimal.ZERO) != 0) {
            //      addText("costoKWG", numberFormat.format(costCons.divide(kwhGLP, RoundingMode.HALF_EVEN)), j);
            //} else {
            //    addText("costoKWG", numberFormat.format(BigDecimal.ZERO), j);
            //}
        }

        if (bill.isTotal) {
            setFont(bf, 8);
            addText("ipli", bill.ipli, j);
            addText("io", bill.io, j);
        }

        setFont(bf, 7);
        drawBanks("banks", j);
        addTextRight("elecRef2", elecRef, bf, 8, j);
        addTextRight("billNum2", bill.billNum, bf, 8, j);
        addTextRight("total2", moneyFormat.format(total), bf, 8, j);
        addTextRight("limitDate2", limit, bf, 8, j);
        addTextRight("span2", consMonthLongFormat.format(span.consMonth), bf, 8, j);

        setFont(bfb, 9, Color.WHITE);
        addTextCenter("txtDetail", bill.isTotal ? "Resumen de la Factura" : "Detalle del Abono", j);
        setFont(bfb, 9, Color.BLACK);

        drawMainBody(lines, j, moneyFormat, "car1", 7, j);
        if (bill.isTotal) {
            drawMainBody(bill.glpLines.toArray(new LineForPrint[0]), j, moneyFormat, "glp", 7, j);
            drawMainBody(bill.noGlpLines.toArray(new LineForPrint[0]), j, moneyFormat, "no_glp", 7, j);
        } else {
            setFont(bfb, 10);
            addTextCenter("glp", "Abono Parcial", j);
            addTextCenter("no_glp", "Abono Parcial", j);
        }
    }

    @Override
    protected Document createDocument() {
        return new Document(new Rectangle(8.5f * 72f, 11f * 72f), 36f, 36f, 36f, 36f);
    }

    @Override
    public void prepare(Connection ep) throws Exception {
        monthsLimit = inst.suspDebtMonths;
        prodQualityInds = BillMarketProdQuality.getByMarket(inst.marketId, ep);
    }

    public static List<BillParam> getBillParameters(BillForPrint bill, BillClieCau cau, BillSpan span, DecimalFormat factorFormat, BillWriter w) {
        List<BillParam> params = new ArrayList<>();
        params.add(new BillParam("Gm $/kg", factorFormat.format(span.pms.divide(span.cglp, 4, RoundingMode.HALF_EVEN))));
        params.add(new BillParam("Tm $/kg", factorFormat.format(span.t.add(span.tv))));
        params.add(new BillParam("Fv kg/m3", factorFormat.format(span.fv)));

        if (bill.sectorType.equals("r")) {
            params.add(new BillParam("Dm $/m3", factorFormat.format(span.dAomR.add(span.dInvR))));
        } else {
            params.add(new BillParam("Dm $/m3", factorFormat.format(span.dAomNr.add(span.dInvR))));
        }

        params.add(new BillParam("Fpc", factorFormat.format(span.fpc)));
        params.add(new BillParam("Fact corr", factorFormat.format(cau.meterFactor.compareTo(BigDecimal.ONE) != 0 ? cau.meterFactor : span.fadj)));
        params.add(new BillParam("p %", factorFormat.format(span.p)));
        params.add(new BillParam("Cf $/fra", factorFormat.format(span.cuf)));

        if (bill.sectorType.equals("r") && (bill.stratum == 1 || bill.stratum == 2)) {
            if (bill.stratum == 1) {
                params.add(new BillParam("CEq e1 $/m3", factorFormat.format(span.cEq1)));
                params.add(new BillParam("Sub estr 1 %", factorFormat.format(span.subPerc1)));
                //params.add(new BillParam("Tarifa e1 $/m3", factorFormat.format(span.finalTarif1)));
            } else {
                params.add(new BillParam("CEq e2 $/m3", factorFormat.format(span.cEq2)));
                params.add(new BillParam("Sub estr 2 %", factorFormat.format(span.subPerc2)));
                //params.add(new BillParam("Tarifa e2 $/m3", factorFormat.format(span.finalTarif2)));
            }
        } else {
            params.add(new BillParam("Cuv $/m3", bill.sectorType.equals("r") ? factorFormat.format(span.cuvR) : factorFormat.format(span.cuvNr)));
            if (bill.cau.valContrib.compareTo(BigDecimal.ZERO) > 0) {
                if (bill.sectorType.equals("r")) {
                    params.add(new BillParam("Contrib res %", factorFormat.format(span.contribR)));
                } else {
                    params.add(new BillParam("Contr no res %", factorFormat.format(span.contribNr)));
                }
            }
        }

        BigDecimal consKwh = bill.currRead.subtract(bill.lastRead).multiply(bill.factor).multiply(span.power).multiply(new BigDecimal("0.0103"));
        double valKwh = bill.cau.valConsSubs.add(bill.cau.valConsNoSubs).subtract(bill.cau.valSubs).add(bill.cau.valContrib).doubleValue() / (consKwh.doubleValue());

        params.add(new BillParam("Consumo kwh", factorFormat.format(consKwh)));
        params.add(new BillParam("Valor kwh", factorFormat.format(valKwh)));
        return params;
    }

    private void drawServices(DtoSrvToPrint[] lines, int j, DecimalFormat outputFormat, String blockName, float fontSize, float border) throws Exception {
        float y1 = pos.get(blockName)[2] + border;
        float y2 = pos.get(blockName)[4] - border;
        float lineH = (y2 - y1) / lines.length;
        lineH = lineH > 10 ? 10 : lineH;
        float y = y2 - (fontSize / 2);
        setFont(bf, fontSize);
        for (DtoSrvToPrint line : lines) {
            addText(pos.get("finan0_0")[1], y, line.label, j);
            addTextRight(bf, fontSize, pos.get("finan0_1")[3], y, outputFormat.format(line.pendCapital.add(line.feeCapital)), j);
            addTextRight(bf, fontSize, pos.get("finan0_2")[3], y, outputFormat.format(line.feeCapital), j);
            addTextRight(bf, fontSize, pos.get("finan0_3")[3], y, outputFormat.format(line.feeInterest), j);
            addTextRight(bf, fontSize, pos.get("finan0_4")[3], y, outputFormat.format(line.feeCapital.add(line.feeInterest)), j);
            addTextRight(bf, fontSize, pos.get("finan0_5")[3], y, outputFormat.format(line.pendCapital), j);
            addTextRight(bf, fontSize, pos.get("finan0_6")[3], y, (line.totalFees - line.fee) + "", j);
            addTextRight(bf, fontSize, pos.get("finan0_7")[3], y, outputFormat.format(line.creditInter), j);
            y -= lineH;
        }
    }

    private void drawParams(List<BillParam> tarifs, int j, String blockName, float fontSize, float border) throws Exception {
        float y1 = pos.get(blockName)[2] + border;
        float y2 = pos.get(blockName)[4] - border;
        float lineH = (float) ((y2 - y1) / ((Math.ceil(tarifs.size() / 6d)) * 2.5));
        lineH = lineH > 12 ? 12 : lineH;
        float y = y2 - (fontSize / 2);
        setFont(bf, fontSize);
        float xps[] = new float[]{pos.get("tarifLbl0")[1], pos.get("tarifLbl1")[1], pos.get("tarifLbl2")[1], pos.get("tarifLbl3")[1], pos.get("tarifLbl4")[1], pos.get("tarifLbl5")[1]};

        for (int i = 0; i < Math.ceil(tarifs.size() / 6d); i++) {
            for (int m = 0; m < 6; m++) {
                int k = m + (i * 6);
                if (k < tarifs.size()) {
                    BillParam t1 = tarifs.get(k);
                    addText(xps[m], y, t1.label, j);
                    addText(xps[m], y - lineH, t1.value, j);
                }
            }
            y -= (lineH * 2.5);
        }
    }

    private void drawProductQuality(List<BillMarketProdQuality> pqInds, int j, String blockName, float fontSize, float border) throws Exception {
        float y1 = pos.get(blockName)[2] + border;
        float y2 = pos.get(blockName)[4] - border;
        float lineH = (float) ((y2 - y1) / ((Math.ceil(pqInds.size() / 6d)) * 2.5));
        lineH = lineH > 12 ? 12 : lineH;
        float y = y2 - (fontSize / 2);
        setFont(bf, fontSize);
        float xps[] = new float[]{pos.get("pqLbl0")[1], pos.get("pqLbl1")[1], pos.get("pqLbl2")[1], pos.get("pqLbl3")[1], pos.get("pqLbl4")[1], pos.get("pqLbl5")[1]};

        for (int i = 0; i < Math.ceil(pqInds.size() / 6d); i++) {
            for (int m = 0; m < 6; m++) {
                int k = m + (i * 6);
                if (k < pqInds.size()) {
                    BillMarketProdQuality t1 = pqInds.get(k);
                    addText(xps[m], y, t1.label, j);
                    addText(xps[m], y - lineH, t1.value, j);
                }
            }
            y -= (lineH * 2.5);
        }
    }

}
