package api.bill.writers.bill;

import api.bill.model.BillClientTank;
import api.bill.model.BillMarketProdQuality;
import api.bill.writers.bill.netZebra.BillSiteTemplate;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import utilities.Reports;
import utilities.Strings;

public class BillWriterNetZPL extends BillWriter {

    private final SimpleDateFormat consMonthLongFormat = new SimpleDateFormat("MMMM yyyy");
    private final SimpleDateFormat consMonthFormat = new SimpleDateFormat("MMM yy");
    private final SimpleDateFormat shortDateFormat = new SimpleDateFormat("d MMM yyyy");
    private final DecimalFormat moneyFormat = new DecimalFormat("$###,###,###,##0.00");
    private final DecimalFormat readingFormat = new DecimalFormat("###,###,###,##0.00");
    private final DecimalFormat numberFormat = new DecimalFormat("###,###,###,##0.00");
    private final DecimalFormat shortFormat = new DecimalFormat("#.00");
    private final DecimalFormat factorFormat = new DecimalFormat("0.00");

    private String[] bankData;
    private BillForPrint bill;
    private String curBill;
    private String template;
    private List<BillMarketProdQuality> prodQualityInds;

    public BillWriterNetZPL() throws Exception {

    }

    protected String drawBillNet() throws Exception {
        curBill = template;
        putValue("[cycle]", inst.cycle);
        putValue("[client_name]", bill.clientName);
        putValue("[document]", bill.clieDoc);
        putValue("[client_address]", Strings.toTitleType(bill.address + " " + bill.buildingName));
        putValue("[phone]", bill.cliePhone);
        putValue("[sector]", BillClientTank.getSectorDescription(bill.sectorType));
        putValue("[stratum]", bill.stratum != null ? bill.stratum + "" : "");
        putValue("[city_name]", pobName);
        putValue("[code]", sysCfg.billUseCode ? bill.clieCode : bill.instNum);
        putValue("[meter_num]", Strings.toTitleType(bill.meter));
        putValue("[client_status]", bill.clientStatus);

        putValue("[total]", moneyFormat.format(bill.total));
        putValue("[bill_num]", bill.billNum);
        putValue("[bill_ref]", (sysCfg.billUseCode ? (bill.isTotal ? bill.clieCode : "No disponible") : bill.instNum));
        putValue("[bill_span]", Strings.toTitleType(shortDateFormat.format(span.beginDate) + " - " + shortDateFormat.format(span.endDate)));
        putValue("[susp_date]", bill.isTotal ? shortDateFormat.format(span.suspDate) : "No aplica");
        putValue("[created]", shortDateFormat.format(bill.creationDate));
        putValue("[limit_date]", bill.months >= inst.suspDebtMonths - 1 ? "Pago inmediato" : shortDateFormat.format(span.limitDate));
        if (bill.months >= inst.suspDebtMonths - 1) {
            putValue("[months]", bill.months + " - Aviso de Corte");
        } else {
            putValue("[months]", bill.months + "");
        }

        putValue("[lbl_consums]", "Consumos Históricos con Factor de Corrección " + factorFormat.format(bill.factor));

        putValue("[cur_read]", bill.readingFaultDescription != null && !bill.readingFaultDescription.isEmpty() ? "No disponible" : readingFormat.format(bill.currRead) + " m3");
        putValue("[last_read]", readingFormat.format(bill.lastRead) + " m3");
        putValue("[med_cons]", readingFormat.format(bill.currRead.subtract(bill.lastRead)) + " m3");
        if (bill.factor != null) {
            putValue("[corr_cons]", readingFormat.format(bill.currRead.subtract(bill.lastRead).multiply(bill.factor)) + " m3");
        } else {
            putValue("[corr_cons]", readingFormat.format(bill.currRead.subtract(bill.lastRead)) + " m3");
        }

        putValue("[fault_description]", bill.readingFaultDescription != null ? bill.readingFaultDescription : "Sin Novedad");
        drawBars();
        putValue("[short_span]", consMonthLongFormat.format(span.consMonth));

        String barCode = BillWriter.getStringBarcode(bill.billNum, bill.total, span.limitDate, billCfg.codEnt, true);

        putValue("bar_code", barCode);

        //CODIGO DE BARRAS
        putValue("cod_bar", barCode);

        drawTarifComponent();

        return curBill;
    }

    private void drawBars() throws Exception {
        //--- Consumos        
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(span.limitDate);

        Double total = 0d;
        Double maxCons = 0d;
        int zeros = 0;
        for (Double cons : bill.consumos) {
            maxCons = (maxCons < cons ? cons : maxCons);
            total = total + cons;
            if (sysCfg.skipZeros && cons == 0) {
                zeros++;
            }
        }
        int totalCons = bill.consumos.size();

        if (totalCons < 6) {
            for (int i = 0; i < (6 - totalCons); i++) {
                bill.consumos.add(null);
            }
        }

        Double avg = total / (totalCons - zeros);
        bill.consumos.add(0, avg);

        for (int i = 0; i < 7; i++) {
            if (bill.consumos.get(i) == null) {
                putValue("cons_" + (i + 1), "");
            } else if (bill.consumos.get(i).compareTo(0d) > 0) {
                putValue("cons_" + (i + 1), readingFormat.format(bill.consumos.get(i)));
            } else {
                putValue("cons_" + (i + 1), "0,00");
            }
            if (i == 0) {
                putValue("rank_" + (i + 1), "Prom");
            } else {
                gc.add(GregorianCalendar.MONTH, -1);
                putValue("rank_" + (i + 1), consMonthFormat.format(gc.getTime()));
            }
        }
    }

    private void drawBarsOld() throws Exception {
        //--- Consumos        
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(span.limitDate);

        String barZone = null;
        try {
            barZone = curBill.substring(curBill.indexOf("^FXSBAR^FS") + 10, curBill.indexOf("^FXEBAR^FS"));
        } catch (java.lang.StringIndexOutOfBoundsException e) {
            throw new Exception("Error en la configuracion de la factura, comuniquese con sistemas");
        }

        String[] bars = barZone.split("\\^FS");

        Double total = 0d;
        Double maxCons = 0d;
        int zeros = 0;
        for (Double cons : bill.consumos) {
            maxCons = (maxCons < cons ? cons : maxCons);
            total = total + cons;
            if (sysCfg.skipZeros && cons == 0) {
                zeros++;
            }
        }
        int totalCons = bill.consumos.size();

        if (totalCons < 6) {
            for (int i = 0; i < (6 - totalCons); i++) {
                bill.consumos.add(null);
            }
        }

        Double avg = total / (totalCons - zeros);
        bill.consumos.add(0, avg);

        Pattern pattern = Pattern.compile("GB(.*)");
        Matcher matcher;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bars.length; i++) {
            String bar;
            if (!bars[i].trim().isEmpty()) {
                if (bill.consumos.get(i) == null) {
                    bar = "";
                    putValue("cons_" + (i + 1), "");
                } else if (bill.consumos.get(i).compareTo(0d) > 0) {
                    bar = bars[i];
                    matcher = pattern.matcher(bar);
                    String[] parts = null;
                    String dataToReplace = "";
                    while (matcher.find()) {
                        dataToReplace = matcher.group(1);
                        parts = matcher.group(1).split(",");
                    }
                    double x = Integer.valueOf(parts[0]);
                    double y = Integer.valueOf(parts[1]);
                    double t;

                    if (i == 0) { //promedio
                        y = (int) Math.round(avg * y / maxCons);
                        t = 1;
                    } else {
                        y = (int) Math.round(bill.consumos.get(i) * y / maxCons);
                        t = (int) Math.round(Math.min(x, y) / 2);
                    }
                    bar = bar.replace(dataToReplace, (int) x + "," + (int) y + "," + (int) t + "^FS");
                    putValue("cons_" + (i + 1), readingFormat.format(bill.consumos.get(i)));
                } else {
                    bar = "";
                    putValue("cons_" + (i + 1), "0,00");
                }

                if (i == 0) {
                    putValue("rank_" + (i + 1), "Prom");
                } else {
                    gc.add(GregorianCalendar.MONTH, -1);
                    putValue("rank_" + (i + 1), consMonthFormat.format(gc.getTime()));
                }
                sb.append(bar).append(System.lineSeparator());
            }
        }
        putValue(barZone, sb.toString());
    }

    private void putValue(String varName, String value) {
        curBill = curBill.replace(varName, value);
    }

    private void drawTarifComponent() {
        List<BillParam> billTarifs = new ArrayList<>();
        if (inst.isTankInstance()) {
            billTarifs.add(new BillParam("Poder Calorífico", numberFormat.format(span.power) + " MJ/m3"));
            BigDecimal costCons = bill.currRead.subtract(bill.lastRead).multiply(bill.factor).multiply(bill.gplPrice).setScale(2, RoundingMode.HALF_UP);
            if (bill.consumos.size() > 0) {
                BigDecimal kwhGLP = bill.currRead.subtract(bill.lastRead).multiply(span.power).divide(span.divisor, RoundingMode.HALF_EVEN).setScale(2, RoundingMode.HALF_UP);
                billTarifs.add(new BillParam("kWH GLP", numberFormat.format(kwhGLP)));
                if (kwhGLP.compareTo(BigDecimal.ZERO) != 0) {
                    billTarifs.add(new BillParam("Costo 1 kWH Gas", numberFormat.format(costCons.divide(kwhGLP, RoundingMode.HALF_EVEN))));
                } else {
                    billTarifs.add(new BillParam("Costo 1 kWH Gas", numberFormat.format(BigDecimal.ZERO)));
                }
            }
        } else {
            billTarifs = BillWriterNetPdf.getBillParameters(bill, bill.cau, span, factorFormat, this);
        }
        String bodyBill = getBodyBill(bill, billTarifs);
        curBill = curBill.replace("@body", bodyBill);
    }

    private long addItems(StringBuilder sb, String title, long totalBodyHeight, List<LineForPrint> items) {
        int lineHeigh = 25;
        sb.append(String.format("^FT16,%d^A0N,23,23^FB765,1,6,C^FH\\^CI28^FD" + title + "^FS^CI27\n"
                + "^LRY^FO16,%d^GB755,0,29^FS^LRN", totalBodyHeight, totalBodyHeight - 23));

        totalBodyHeight += 5;

        String itemLine = "^FT16,%d^A0N,23,23^FH\\^CI28^FD%s^FS^CI27\n"
                + "^FT580,%d^A0N,23,25^FB183,1,6,R^FH\\^CI28^FD%s^FS^CI27\n";

        String boldItemLine = "^FT16,%d^A0N,28,28^FH\\^CI28^FD%s^FS^CI27\n"
                + "^FT580,%d^A0N,30,32^FB183,1,6,R^FH\\^CI28^FD%s^FS^CI27\n";

        for (LineForPrint item : items) {
            if (item.value != null && item.value.compareTo(BigDecimal.ZERO) != 0) {
                String line = (item.bold ? boldItemLine : itemLine);
                totalBodyHeight += (lineHeigh + (item.bold ? 5 : 0));
                sb.append(String.format(line, totalBodyHeight, Strings.toTitleType(item.label), totalBodyHeight, moneyFormat.format(item.value)));
            }
        }
        totalBodyHeight += 40;
        return totalBodyHeight;
    }

    private String getBodyBill(BillForPrint bill, List<BillParam> tarifs) {

        //^FTx,y posición
        //^A0N,23,25  Fuente 0, normal, cada letra será de 23x25 pts
        //^FH hexadecimal
        //^CI28 unicode
        //^FD empieza cadena
        //^FS fin del campo
        //^FB183,1,6,R bloque 183 es el ancho, 1 lineas, 6 espacio entre lineas, R alineación
        //^GB Graphic box, ancho, alto, borde
        DtoSrvToPrint[] srvs = bill.srvs;
        DtoRangeToPrint[] ranges = bill.ranges;

        int lineHeigh = 25;
        long totalBodyHeight = 25;

        //
        StringBuilder body = new StringBuilder();

        //INICIO COMPONENTES
        body.append(String.format("^FT16,%d^A0N,23,23^FB765,1,6,C^FH\\^CI28^FDComponentes de Costo de Prestación del Servicio^FS^CI27\n"
                + "^LRY^FO16,%d^GB755,0,29^FS^LRN", totalBodyHeight, totalBodyHeight - 23));
        totalBodyHeight += 35;

        int xps[] = new int[]{38, 159, 280, 401, 522, 643};
        String fontSize = "20,15";

        for (int i = 0; i < Math.ceil(tarifs.size() / 6d); i++) {
            for (int j = 0; j < 6; j++) {
                int k = j + (i * 6);
                if (k < tarifs.size()) {
                    BillParam t1 = tarifs.get(k);
                    body.append("^FT" + xps[j] + "," + totalBodyHeight + "^A0N," + fontSize + "^FH\\^CI28^FD" + t1.label + "^FS^CI27");
                    body.append("^FT" + xps[j] + "," + (totalBodyHeight + 20) + "^A0N," + fontSize + "^FH\\^CI28^FD" + t1.value + "^FS^CI27");
                }
            }
            totalBodyHeight += 45;
        }
        totalBodyHeight += 25;
        //FIN COMPONENTES

        //INICIO TARIFAS
        body.append(String.format("^FT16,%d^A0N,23,23^FB765,1,6,C^FH\\^CI28^FDTarifas y Consumos Facturados^FS^CI27\n"
                + "^LRY^FO16,%d^GB765,0,29^FS^LRN", totalBodyHeight, totalBodyHeight - 23));
        totalBodyHeight += 35;

        if (ranges.length > 0) {
            body.append(""
                    + "^FT16," + totalBodyHeight + "^A0N,22,22^FH\\^CI28^FDRango de Consumo^FS"
                    + "^FT235," + totalBodyHeight + "^A0N,22,22^FB183,1,6,R^FH\\^CI28^FDConsumo^FS"
                    + "^FT385," + totalBodyHeight + "^A0N,22,22^FB183,1,6,R^FH\\^CI28^FDVlr Unit x m3^FS"
                    + "^FT580," + totalBodyHeight + "^A0N,22,22^FB183,1,6,R^FH\\^CI28^FDVlr Liquidado^FS");
            totalBodyHeight += lineHeigh;
            for (DtoRangeToPrint r : ranges) {
                body.append(""
                        + "^FT16," + totalBodyHeight + "^A0N,22,22^FH\\^CI28^FD" + r.label + "^FS"
                        + "^FT235," + totalBodyHeight + "^A0N,22,22^FB183,1,6,R^FH\\^CI28^FD" + factorFormat.format(r.cons) + "m3^FS"
                        + "^FT385," + totalBodyHeight + "^A0N,22,22^FB183,1,6,R^FH\\^CI28^FD" + moneyFormat.format(r.vunit) + "^FS"
                        + "^FT580," + totalBodyHeight + "^A0N,22,22^FB183,1,6,R^FH\\^CI28^FD" + moneyFormat.format(r.total) + "^FS");
                totalBodyHeight += lineHeigh;

            }
        } else {
            body.append("^FT16,").append(totalBodyHeight).append("^A0N,23,25^FH\\^CI28^FDSin Consumo^FS");
            totalBodyHeight += lineHeigh;
        }
        totalBodyHeight += 30;
        //FIN TARIFAS

        //INICIO TARIFA
        if(bill.tarifaPlena){
            totalBodyHeight += 20;
            body.append(String.format("^FT16,%d^A0N,23,23^FB765,1,6,C^FH\\^CI28^FDAportes Públicos^FS^CI27\n"
                + "^LRY^FO16,%d^GB755,0,29^FS^LRN", totalBodyHeight, totalBodyHeight - 23));
            totalBodyHeight += 30;
            body.append("^FT16," + totalBodyHeight + "^A0N,17,17^FH\\^CI28^FDConvenio 65-625: $"+String.format("%,.2f", bill.resultFinal!=null?bill.resultFinal:BigDecimal.ZERO)+ "^FS");
            //+String.format("%,.2f", bill.resultFinal!=null?bill.resultFinal:0)
            totalBodyHeight += 40;
        }
        //FIN TARIFA
        
        //INICIO INDICADORES
        body.append(String.format("^FT16,%d^A0N,23,23^FB765,1,6,C^FH\\^CI28^FDIndicadores de Calidad^FS^CI27\n"
                + "^LRY^FO16,%d^GB755,0,29^FS^LRN", totalBodyHeight, totalBodyHeight - 23));
        totalBodyHeight += 40;

        body.append(""
                + "^FT16," + totalBodyHeight + "^A0N,23,23^FH\\^CI28^FDDES^FS"
                + "^FT221," + totalBodyHeight + "^A0N,23,23^FH\\^CI28^FDIPLI: " + bill.ipli + "^FS"
                + "^FT402," + totalBodyHeight + "^A0N,23,23^FH\\^CI28^FDIO: " + bill.io + "^FS"
                + "^FT583," + totalBodyHeight + "^A0N,23,23^FH\\^CI28^FDIRST^FS");

        totalBodyHeight += 50;
        //FIN INDICADORES

        //CALIDAD PRODUCTO
        if (prodQualityInds != null && !prodQualityInds.isEmpty()) {
            body.append(String.format("^FT16,%d^A0N,23,23^FB765,1,6,C^FH\\^CI28^FDCalidad del Producto^FS^CI27\n"
                    + "^LRY^FO16,%d^GB765,0,29^FS^LRN", totalBodyHeight, totalBodyHeight - 23));
            totalBodyHeight += 35;
            int xpsProd[] = new int[]{38, 159, 280, 401, 522, 643};
            String prodFontSize = "20,15";

            for (int i = 0; i < Math.ceil(prodQualityInds.size() / 6d); i++) {
                for (int j = 0; j < 6; j++) {
                    int k = j + (i * 6);
                    if (k < prodQualityInds.size()) {
                        BillMarketProdQuality t1 = prodQualityInds.get(k);
                        body.append("^FT" + xpsProd[j] + "," + totalBodyHeight + "^A0N," + prodFontSize + "^FH\\^CI28^FD" + t1.label + "^FS^CI27");
                        body.append("^FT" + xpsProd[j] + "," + (totalBodyHeight + 20) + "^A0N," + prodFontSize + "^FH\\^CI28^FD" + t1.value + "^FS^CI27");
                    }
                }
                totalBodyHeight += 45;
            }
            totalBodyHeight += 25;
        }
        //FIN CALIDAD PRODUCTO
        
        //INICIO DESCRIPCIÓN COBRO
        totalBodyHeight = addItems(body, "Detalle Liquidación del Servicio", totalBodyHeight, bill.glpLines);
        totalBodyHeight = addItems(body, "Facturación Otros Cargos de Servicio", totalBodyHeight, bill.noGlpLines);

        //FIN DESCRIPCIÓN COBRO
        //INICIO FINANCIACIONES
        body.append(String.format("^FT16,%d^A0N,23,23^FB765,1,6,C^FH\\^CI28^FDFacturación de Otros Servicios y Financiación^FS^CI27\n"
                + "^LRY^FO16,%d^GB755,0,29^FS^LRN", totalBodyHeight, totalBodyHeight - 23));
        totalBodyHeight += 40;

        String fsize = "23,15";

        int[] pos = new int[]{16, 120 - 15, 212 - 15, 304 - 15, 396 - 15, 488 - 15, 545 - 15, 595 - 15};

        if (srvs.length > 0) {
            body.append(""
                    + "^FT" + pos[0] + "," + totalBodyHeight + "^A0N," + fsize + "^FH\\^CI28^FDConcepto^FS"
                    + "^FT" + pos[1] + "," + totalBodyHeight + "^A0N," + fsize + "^FB183,1,6,R^FH\\^CI28^FDSaldo Ant.^FS"
                    + "^FT" + pos[2] + "," + totalBodyHeight + "^A0N," + fsize + "^FB183,1,6,R^FH\\^CI28^FDAbn Capital^FS"
                    + "^FT" + pos[3] + "," + totalBodyHeight + "^A0N," + fsize + "^FB183,1,6,R^FH\\^CI28^FDInt Finan^FS"
                    + "^FT" + pos[4] + "," + totalBodyHeight + "^A0N," + fsize + "^FB183,1,6,R^FH\\^CI28^FDVlr Cuota^FS"
                    + "^FT" + pos[5] + "," + totalBodyHeight + "^A0N," + fsize + "^FB183,1,6,R^FH\\^CI28^FDNuevo Saldo^FS"
                    + "^FT" + pos[6] + "," + totalBodyHeight + "^A0N," + fsize + "^FB183,1,6,R^FH\\^CI28^FDC Pend^FS"
                    + "^FT" + pos[7] + "," + totalBodyHeight + "^A0N," + fsize + "^FB183,1,6,R^FH\\^CI28^FD%Inter^FS");
            totalBodyHeight += lineHeigh;
            for (DtoSrvToPrint srv : srvs) {
                body.append(""
                        + "^FT" + pos[0] + "," + totalBodyHeight + "^A0N," + fsize + "^FH\\^CI28^FD" + srv.label + "^FS"
                        + "^FT" + pos[1] + "," + totalBodyHeight + "^A0N," + fsize + "^FB183,1,6,R^FH\\^CI28^FD" + shortFormat.format(srv.pendCapital.add(srv.feeCapital)) + "^FS"
                        + "^FT" + pos[2] + "," + totalBodyHeight + "^A0N," + fsize + "^FB183,1,6,R^FH\\^CI28^FD" + shortFormat.format(srv.feeCapital) + "^FS"
                        + "^FT" + pos[3] + "," + totalBodyHeight + "^A0N," + fsize + "^FB183,1,6,R^FH\\^CI28^FD" + shortFormat.format(srv.feeInterest) + "^FS"
                        + "^FT" + pos[4] + "," + totalBodyHeight + "^A0N," + fsize + "^FB183,1,6,R^FH\\^CI28^FD" + shortFormat.format(srv.feeCapital.add(srv.feeInterest)) + "^FS"
                        + "^FT" + pos[5] + "," + totalBodyHeight + "^A0N," + fsize + "^FB183,1,6,R^FH\\^CI28^FD" + shortFormat.format(srv.pendCapital) + "^FS"
                        + "^FT" + pos[6] + "," + totalBodyHeight + "^A0N," + fsize + "^FB183,1,6,R^FH\\^CI28^FD" + (srv.totalFees - srv.fee) + "^FS"
                        + "^FT" + pos[7] + "," + totalBodyHeight + "^A0N," + fsize + "^FB183,1,6,R^FH\\^CI28^FD" + shortFormat.format(srv.creditInter) + "^FS");
                totalBodyHeight += lineHeigh;

            }
        } else {
            body.append("^FT16," + totalBodyHeight + "^A0N,23,25^FH\\^CI28^FDNinguno^FS");
            totalBodyHeight += lineHeigh;
        }
        totalBodyHeight += 30;
        //FIN FINANCIACIONES

        totalBodyHeight = addItems(body, "Resumen de la Factura", totalBodyHeight, bill.detLines);

        //INICIO INFO PAGOS
        totalBodyHeight += 20;
        body.append(String.format("^FT16,%d^A0N,23,23^FB765,1,6,C^FH\\^CI28^FDInformación de Pagos^FS^CI27\n"
                + "^LRY^FO16,%d^GB755,0,29^FS^LRN", totalBodyHeight, totalBodyHeight - 23));
        totalBodyHeight += 40;

        int payLineHeight = 25;

        if (bill.lastPayBank != null && !bill.lastPayBank.isEmpty()) {
            body.append("^FT16," + totalBodyHeight + "^A0N,17,17^FH\\^CI28^FDValor Último Pago: " + moneyFormat.format(bill.lastPayValue) + "^FS");
            totalBodyHeight += payLineHeight;
            body.append("^FT16," + totalBodyHeight + "^A0N,17,17^FH\\^CI28^FDFecha Último Pago: " + bill.lastPayDate + "^FS");
            totalBodyHeight += payLineHeight;
            body.append("^FT16," + totalBodyHeight + "^A0N,17,17^FH\\^CI28^FDPunto de Pago: " + bill.lastPayBank + "^FS");
            totalBodyHeight += payLineHeight;
        } else {
            body.append("^FT16," + totalBodyHeight + "^A0N,17,17^FH\\^CI28^FDValor Último Pago: No Registra pagos aún^FS");
            totalBodyHeight += payLineHeight;
            body.append("^FT16," + totalBodyHeight + "^A0N,17,17^FH\\^CI28^FDFecha Último Pago: No Registra pagos aún^FS");
            totalBodyHeight += payLineHeight;
            body.append("^FT16," + totalBodyHeight + "^A0N,17,17^FH\\^CI28^FDPunto de Pago: No Registra pagos aún^FS");
            totalBodyHeight += payLineHeight;
        }

        body.append("^FT475," + totalBodyHeight + "^GFA,1113,3160,40,:Z64:eJylVk1r2zAYllSbBRVSFxx2NTkVDzp2K9tFgWZnF+Lb9l9EdjE5bH9BeBehQHodObSn/ZfgQ3/ADtn0ZSeypTSwh9aWkjePn/dD72sAXgF/zcCCnGeG2Hl2MPQFpqe2B0TuNg3x3RytUeZsHZDQF8eohBAVlagopysh6DIkr9ND9H88tEB1nQGQmQ1DkghRtGV+ui5cljfpG+DK3IyekU7rSGUXC5/E9veQ6M0gG1YWsHIoNh8iLXyosfu9o+uwsbJQZeVRTUGVShnLQQnG5HhXmCCW5eFT6RnIKbgwdGCaq2suA5gLMc1AH22tvLGy4uuegdVniwNfUfWEG3AbOBytkEj/EU/taEHIyssMDUM189K18nQyZCw9xaIqj1o1yGQUU29mD/Lie8t+7zdr5eXKbQTyZcDbtpQLE7WSEL8dFyoteKV1cf6ya7z64GxRKFlRvFBaYTkrvXSXq7XAmcwt/pUjVF88rzd+eZMoScA1JBNSyNDFxUPkM0O7P7wR8syyZsW4oI87/t2nL549zKM0LWZwPoHSVemut1fJShM52wo8Hm9zzIAkbgPqeBstiocZLJMkffthMQEQRoOzq3ArmmazeXzhTDw97xuV3ZGohnaLeTkvy/sSgnn89TeR2fV2vne1+Pm4XQGB34urT9+YrOep7n89TGAC5+r8J2n6+VoXnjd6f/dCvIwo5mz3Y//EqSxmLW5QMsTcZoCU5ZdyAYHfXdtYMvFRrGsZSRnCTC5UT3BgQw8LtYTq7PaPrgTeAWwWS6ZWYyrPbq2ZnPExIbYlWRdTJTdqlx3Qtkb1dIrsKVaNQHZm/YS856+SJyNoWGFiFA6HG13pmSF4VakJorDZygvvees6HanWFxGfRWbcNZtcRfPU3E3aGwzMXRsl6xxXxL5abuNGjlX6YPoosuRZN038sKyexLZw5Anq1XaAkSe7nreSgZkenYeyjkPynMoN1LEC09fGkt+FXllswApLHuQzcbtglhwHn3vMeiIbJm5joyr4PtXGzZ6EUOy6tGbmdndaXQsz2rxw3sguadDOYfCNSAO3ONAJTedh9N8MDsbsPLuwgz2+M+2C78c9BN+Pe/gHchzEbQ==:4E47");
        body.append("^FT545," + totalBodyHeight + "^A0N,20,17^FH\\^CI28^FDRepresentante Legal^FS");
        body.append("^FT16," + totalBodyHeight + "^A0N,20,17^FH\\^CI28^FDUltima Revisión de Instalaciones: " + shortDateFormat.format(bill.lastInstCheck) + "^FS");
        totalBodyHeight += payLineHeight;
        body.append("^FT16," + totalBodyHeight + "^A0N,20,17^FH\\^CI28^FDRecuerde Reportar su Certificado de Conformidad antes de " + shortDateFormat.format(bill.maxInstCheck) + "^FS");
        totalBodyHeight += payLineHeight;
        body.append("^FT16," + totalBodyHeight + "^A0N,20,17^FH\\^CI28^FDpara evitar suspenciones. Mayor información en la línea 3102578506^FS");

        body.append("^FT791," + totalBodyHeight + "^A0B,17,18^FH\\^CI28^FDEsta factura presta mérito ejecutivo art. 130 ley 142/94^FS^CI27");

        //FIN PAGOS
        return BillSiteTemplate.getBodyTemplate().replace("@content_body", body).replace("@height", totalBodyHeight + "");
    }

    @Override
    public void prepare(Connection conn) throws Exception {
        bankData = new String[rawBankData.length + 1];
        bankData[0] = "Páguese en:";
        for (int i = 0; i < rawBankData.length; i++) {
            bankData[i + 1] = rawBankData[i][0].toString();
        }

        StringBuilder sb = new StringBuilder();
        BillSiteTemplate.getFooterTemplateH(sb, bankData);
        template = sb.toString() + "\n@body\n" + BillSiteTemplate.getHeaderTemplate();
        prodQualityInds = BillMarketProdQuality.getByMarket(inst.marketId, conn);
    }

    @Override
    public File endDocument() throws Exception {
        try (FileOutputStream fos = new FileOutputStream(fout); ByteArrayInputStream bais = new ByteArrayInputStream(curBill.getBytes("UTF-8"))) {
            Reports.copy(bais, fos);
        }
        return fout;
    }

    @Override
    public void addBill(BillForPrint bill) throws Exception {
        super.addBill(bill);
        this.bill = bill;
        if (inst.isNetInstance()) {
            drawBillNet();
        } else {
            throw new Exception("Opción no disponible en facturación de tanques");
        }
    }
}
