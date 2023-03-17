package api.bill.model;

import api.sys.model.SysCfg;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import utilities.Dates;
import utilities.MySQLQuery;

public class BillBillExporter {

    public static final int FORMAT_PSE = 0;
    public static final int FORMAT_PSE_BC = 1;
    public static final int FORMAT_ASO_98 = 2;
    public static final int FORMAT_ASO_2001 = 3;
    public static final int FORMAT_ASO_BANCOLOMBIA=4;

    public static File exportBills(int spanId, SysCfg sysCfg, BillCfg billCfg, BillInstance inst, int format, Connection conn) throws Exception {
        System.out.println("REPORTE ASO");
        BillSpan span = new BillSpan().select(spanId, conn);
        if (inst.siteBilling) {
            if (span.state.equals("reca")) {
                if (new MySQLQuery("SELECT COUNT(*)>0 FROM bill_client_tank c WHERE c.active AND !c.span_closed").getAsBoolean(conn)) {
                    throw new Exception("El periodo está el proceso de cierre.");
                }
            }
        }
        List<BillPse> lines = getListPSE(spanId, conn);
        File f = File.createTempFile("fras", ".txt");
        DecimalFormat decf = new DecimalFormat("###");

        try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(f), Charset.forName("ISO-8859-1"))) {
            switch (format) {
                case FORMAT_PSE: {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    for (int i = 0; i < lines.size(); i++) {
                        BillPse line = lines.get(i);
                        out.write(line.billNum + ";");//Referencia
                        out.write(line.client + ";");//Nombre
                        out.write(decf.format(line.billValue) + ";");//Valor
                        out.write(df.format(Dates.trimDate(line.limitDate)) + ";");//Fecha Límite de Pago (si aplica).
                        out.write(sysCfg.billUseCode ? line.clientCode : line.numInstall);
                        out.write("\n");
                    }
                    break;
                }
                case FORMAT_PSE_BC: {
                    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
                    for (int i = 0; i < lines.size(); i++) {
                        BillPse line = lines.get(i);
                        out.write(line.clientCode + ";"); // Referencia unica de pago
                        out.write(line.billNum + ";");// factura
                        out.write("CC" + ";");//tipo de documento
                        out.write(line.clientDocument != null && !line.clientDocument.trim().isEmpty() ? line.clientDocument + ";" : line.billNum + ";");//documento
                        out.write(sysCfg.billPseLabel != null ? sysCfg.billPseLabel + ";" : ";");//Descripción del pago
                        out.write(decf.format(line.billValue) + ";");//Valor
                        out.write(df.format(Dates.trimDate(line.limitDate)) + ";");//Fecha Límite de Pago (si aplica).
                        out.write(line.numInstall + ";");
                        out.write("0");//primer valor impuesto
                        out.write("\n");
                    }
                    break;
                }
                case FORMAT_ASO_98: {
                    SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
                    if (billCfg.codEnt.length() != 13) {
                        throw new Exception("El código de la empresa debe tener 13 digitos.");
                    }
                    out.write("01");//tipo reg
                    out.write(leftPad(billCfg.codEnt, 13));//código empresa
                    out.write(df.format(span.limitDate));//primer vencimiento
                    out.write(df.format(span.limitDate));//segundo vencimiento
                    out.write(df.format(span.limitDate));//fecha facturación
                    out.write("000");//ciclo
                    out.write(fillEmpty(42));
                    out.write("\n");
                    BigDecimal total = BigDecimal.ZERO;
                    for (int i = 0; i < lines.size(); i++) {
                        BillPse line = lines.get(i);
                        out.write("02");//tipo reg
                        out.write(leftPad(sysCfg.billUseCode ? line.clientCode : line.numInstall.replaceAll("[^0-9]", ""), 25));//id usuario
                        out.write(leftPad(line.billNum, 12));//id fra
                        out.write("01");//periodos
                        out.write(leftPad(decf.format(line.billValue), 13));//valor serv ppal
                        out.write(leftPad("0", 13));//id empresa adicional
                        out.write(leftPad("0", 13));//valor adicional
                        out.write(fillEmpty(4));//relleno
                        out.write("\n");
                        total = total.add(line.billValue);
                    }
                    out.write("03");
                    out.write(leftPad(lines.size() + "", 9));
                    out.write(leftPad(decf.format(total), 18));
                    out.write(leftPad(decf.format(BigDecimal.ZERO), 18));
                    out.write(fillEmpty(37));
                    out.write("\n");
                    break;
                }
                case FORMAT_ASO_2001: {
                    SimpleDateFormat dfd = new SimpleDateFormat("yyyyMMdd");
                    SimpleDateFormat dfh = new SimpleDateFormat("HHmm");
                    if (billCfg.codEnt.length() != 13) {
                        throw new Exception("El código de la empresa debe tener 13 digitos.");
                    }       ///encabezado de archivo
                    out.write("01");//tipo reg
                    out.write(leftPad(billCfg.nit, 10));//NIT 1
                    out.write(leftPad("", 10));//NIT 2
                    out.write(leftPad("", 3));//Entidad Financiera
                    out.write(dfd.format(new Date()));//fecha creación archivo
                    out.write(dfh.format(new Date()));//hora creación archivo
                    out.write("A");//modificador
                    out.write(fillEmpty(182));
                    out.write("\n");
                    //encabezado de lote
                    out.write("05");//tipo reg
                    out.write(billCfg.codEnt);//EAN
                    out.write(leftPad("1", 4));//Lote
                    out.write(leftPad("Montagas", 15));//Descripción
                    out.write(fillEmpty(186));
                    out.write("\n");
                    //detalles
                    BigDecimal total = BigDecimal.ZERO;
                    for (int i = 0; i < lines.size(); i++) {
                        BillPse line = lines.get(i);
                        out.write("06");//tipo reg
                        out.write(leftPad(line.billNum, 48));//Fra
                        out.write(leftPad(sysCfg.billUseCode ? line.clientCode : line.numInstall.replaceAll("[^0-9]", ""), 30));//id usuario
                        out.write("01");//periodos
                        out.write("001");//ciclo
                        out.write(leftPad(decf.format(line.billValue), 12) + "00");//valor serv ppal
                        out.write(leftPad("0", 13));//servicio otra empresa
                        out.write(leftPad("0", 14));//valor otra empresa
                        out.write(dfd.format(line.limitDate));//Vencimiento
                        out.write(leftPad("0", 8));//Banco Cliente
                        out.write(leftPad("0", 17));//Cuenta Cliente receptor  (Domiciliación)
                        out.write(leftPad("0", 2));//Tipo cliente receptor  (Domiciliación)
                        out.write(leftPad("0", 10));//Nit Cliente receptor  (Domiciliación)
                        out.write(leftPad("0", 22));//Nombre Cliente receptor
                        out.write(fillEmpty(3));//Entidad financiera Originadora (Domiciliación)
                        out.write(fillEmpty(24));//relleno
                        out.write("\n");
                        total = total.add(line.billValue);
                    }       //Control de lote
                    out.write("08");
                    out.write(leftPad(lines.size() + "", 9));
                    out.write(leftPad(decf.format(total), 16) + "00");
                    out.write(leftPad(decf.format(BigDecimal.ZERO), 18));
                    out.write(leftPad("1", 4));//Lote
                    out.write(fillEmpty(169));
                    out.write("\n");
                    //Control de archivo
                    out.write("09");
                    out.write(leftPad(lines.size() + "", 9));
                    out.write(leftPad(decf.format(total), 16) + "00");
                    out.write(leftPad(decf.format(BigDecimal.ZERO), 18));
                    out.write(fillEmpty(173));
                    out.write("\n");
                    break;
                }
                case FORMAT_ASO_BANCOLOMBIA: {
                    SimpleDateFormat dfd = new SimpleDateFormat("yyyyMMdd");
                    SimpleDateFormat dfh = new SimpleDateFormat("HHmm");
                    if (billCfg.codEnt.length() != 13) {
                        throw new Exception("El código de la empresa debe tener 13 digitos.");
                    }       ///encabezado de archivo
                    out.write("01");//tipo reg
                    out.write(leftPad("891202203", 10));//NIT 1 (solicitud quitar digito de verificación. Izquierda agregar 0)
                    out.write(leftPad("", 10));//NIT 2
                    out.write(leftPad("7", 3));//Entidad Financiera =>Bancolombia 007
                    out.write(dfd.format(new Date()));//fecha creación archivo 8 caracteres
                    out.write(dfh.format(new Date()));//hora creación archivo 4 caracteres
                    out.write("A");//modificador 1 caracter
                    out.write(fillEmpty(182));//Reservado
                    out.write("\n");
                    //encabezado de lote
                    out.write("05");//tipo reg caracteres 2
                    out.write(billCfg.codEnt);//EAN caracteres 13
                    out.write(leftPad("1", 4));//Lote
                    out.write("Montagas");//Descripción
                    out.write(fillEmpty(7));//Descripción para completar 15 caracters junto con Montagas
                    out.write(fillEmpty(186));//Reservado
                    out.write("\n");
                    //detalles
                    BigDecimal total = BigDecimal.ZERO;
                    for (int i = 0; i < lines.size(); i++) {
                        BillPse line = lines.get(i);
                        out.write("06");//tipo reg
                        
                        out.write(leftPad(sysCfg.billUseCode ? line.clientCode : line.numInstall.replaceAll("[^0-9]", ""), 48));;//id usuario referencia principal
                        out.write(rightPad(line.billNum, 30));//Fra Referencia secundaria número de factura 
                        
                        out.write("01");//periodos
                        out.write("001");//ciclo
                        out.write(leftPad(decf.format(line.billValue), 12) + "00");//valor serv ppal
                        out.write(fillEmpty(13));//servicio otra empresa
                        out.write(fillEmpty(14));//valor otra empresa
                        out.write(dfd.format(line.limitDate));//Vencimiento
                        out.write(fillEmpty(8));//Banco Cliente
                        out.write(fillEmpty(17));//Cuenta Cliente receptor  (Domiciliación)
                        out.write(fillEmpty(2));//Tipo cliente receptor  (Domiciliación)
                        out.write(fillEmpty(10));//Nit Cliente receptor  (Domiciliación)
                        out.write(fillEmpty(22));//Nombre Cliente receptor
                        out.write(fillEmpty(3));//Entidad financiera Originadora (Domiciliación)
                        out.write(fillEmpty(24));//relleno
                        out.write("\n");
                        total = total.add(line.billValue);
                    }       //Control de lote
                    out.write("08");
                    out.write(leftPad(lines.size() + "", 9));
                    out.write(leftPad(decf.format(total), 16) + "00");
                    out.write(fillEmpty(18));
                    out.write(leftPad("1", 4));//Lote
                    out.write(fillEmpty(169));
                    out.write("\n");
                    //Control de archivo
                    out.write("09");
                    out.write(leftPad(lines.size() + "", 9));
                    out.write(leftPad(decf.format(total), 16) + "00");
                    out.write(fillEmpty(18));
                    out.write(fillEmpty(173));
                    out.write("\n");
                    break;
                }
                default:
                    throw new RuntimeException("Unrecognized format: " + format);
            }
        }
        return f;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(leftPad("0131000000", 12));
        System.out.println(leftPad("13999999", 12));

    }

    private static String leftPad(String str, int len) throws Exception {
        if (str.length() > len) {
            throw new Exception("El campo excede la longitud permitida.");
        }
        while (str.length() < len) {
            str = "0" + str;
        }
        return str;
    }

    private static String rightPad(String str, int len) throws Exception {
        if (str.length() > len) {
            throw new Exception("El campo excede la longitud permitida.");
        }
        while (str.length() < len) {
            str = str + " ";
        }
        return str;
    }
    
    private static String fillEmpty(int len) throws Exception {
        String str = "";
        while (str.length() < len) {
            str += " ";
        }
        return str;
    }

    private static List<BillPse> getListPSE(int spanId, Connection conn) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT "
                + "bill_bill.bill_num, "//0
                + "CONCAT(c.first_name,' ', COALESCE(c.last_name,'')), "//1
                + "(SELECT SUM(p.value) "
                + "FROM bill_plan p "
                + "WHERE p.account_deb_id = 15 "
                + "AND p.cli_tank_id = bill_bill.client_tank_id "
                + "AND p.doc_id = bill_bill.id AND p.doc_type = 'fac'), "//2
                + "(SELECT bill_span.limit_date FROM bill_span "
                + "WHERE id = " + spanId + "), "//3
                + "num_install, "//4
                + "code, "//5
                + "doc "//6                                   
                + "FROM bill_bill "
                + "INNER JOIN bill_client_tank c ON bill_bill.client_tank_id = c.id "
                + "WHERE  bill_bill.registrar_id IS NULL AND bill_bill.total = 1 AND bill_bill.active = 1 AND bill_span_id = " + spanId + " ORDER BY num_install");
        Object[][] data = q.getRecords(conn);
        List<BillPse> res = new ArrayList<>(data.length);

        for (Object[] row : data) {
            BillPse bill = new BillPse();
            bill.billNum = MySQLQuery.getAsString(row[0]);
            bill.client = MySQLQuery.getAsString(row[1]);
            bill.billValue = MySQLQuery.getAsBigDecimal(row[2], true);
            bill.limitDate = MySQLQuery.getAsDate(row[3]);
            bill.numInstall = MySQLQuery.getAsString(row[4]);
            bill.clientCode = MySQLQuery.getAsString(row[5]);
            bill.clientDocument = MySQLQuery.getAsString(row[6]);
            res.add(bill);
        }
        return res;
    }

    static class BillPse {

        public String billNum;
        public String client;
        public BigDecimal billValue;
        public Date limitDate;
        public String numInstall;
        public String clientCode;
        public String clientDocument;

    }
}
