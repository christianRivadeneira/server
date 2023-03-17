package api.bill.rpt.cguno;

import api.BaseAPI;
import api.bill.api.BillClientTankApi;
import api.bill.model.BillCfg;
import api.bill.model.BillInstance;
import static api.bill.rpt.fssri.BillFSSRIReports.getColNum;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jxl.format.CellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import utilities.MySQLQuery;
import utilities.NameSplitter;
import utilities.Reports;
import utilities.cast;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.Table;
import web.marketing.DIANReportServlet;

public class BillCgUnoReports {

    public static void replace(WritableSheet sh, int row, String col, String s) throws Exception {
        int c = getColNum(col);
        int r = row - 1;
        CellFormat cf = sh.getCell(c, r).getCellFormat();
        if (s != null) {
            if (cf != null) {
                sh.addCell(new jxl.write.Label(c, r, s, cf));
            } else {
                sh.addCell(new jxl.write.Label(c, r, s));
            }
        } else {
            if (cf != null) {
                sh.addCell(new jxl.write.Blank(c, r, cf));
            } else {
                sh.addCell(new jxl.write.Blank(c, r));
            }
        }
    }

    public static void replace(WritableSheet sh, int row, String col, Integer n) throws Exception {
        int c = getColNum(col);
        int r = row - 1;
        CellFormat cf = sh.getCell(c, r).getCellFormat();
        if (cf != null) {
            sh.addCell(new jxl.write.Number(c, r, n, cf));
        } else {
            sh.addCell(new jxl.write.Number(c, r, n));

        }
    }

    public static void replace(WritableSheet sh, int row, String col, BigDecimal n) throws Exception {
        int c = getColNum(col);
        int r = row - 1;
        CellFormat cf = sh.getCell(c, r).getCellFormat();
        if (n != null) {
            sh.addCell(new jxl.write.Number(c, r, n.doubleValue(), cf));
        } else {
            sh.addCell(new jxl.write.Blank(c, r, cf));
        }
    }

    public static String fillInt(Integer i, int len) throws Exception {
        return fillInt(i, len, null);
    }

    public static String fillStr(String str, int len) throws Exception {
        return fillStr(str, len, null);
    }

    public static String fillInt(Integer i, int len, String type) throws Exception {
        return String.format("%0" + len + "d", i);
    }

    public static String fillStr(String str, int len, String type) throws Exception {
        if (str.length() > len) {
            throw new Exception("Error de Configuración Batch:" + len + " : " + str.length() + (type != null ? "\n" + type : ""));
        }
        while (str.length() < len) {
            str += " ";
        }
        return str;
    }

    private static String getDocTypeCode(String docType) {
        if (null == docType) {
            throw new RuntimeException("Tipo de documento no soportado: " + docType);
        } else {
            switch (docType) {
                case "cc":
                    return "1";
                case "ce":
                    return "3";
                case "pa":
                    return "9";
                case "nit":
                    return "2";
                case "ti":
                    return "4";
                default:
                    throw new RuntimeException("Tipo de documento no soportado: " + docType);
            }
        }
    }

    public static File getTerc(Date date, BillCfg cfg, Connection conn) throws Exception {
        File tmp = File.createTempFile("tmp", ".xls");
        WritableWorkbook writable = Reports.getWorkbook(tmp, BillCgUnoReports.class, "terceros.xls");
        WritableSheet s1 = writable.getSheet(0);
        List<BillInstance> insts = BillInstance.getAllNet(conn);
        int count = 4;
        for (int j = 0; j < insts.size(); j++) {
            BillInstance inst = insts.get(j);
            inst.useInstance(conn);

            String deptoCode = inst.pobId + "";
            deptoCode = deptoCode.substring(0, deptoCode.length() - 6);

            String cityCode = inst.pobId + "";
            cityCode = cityCode.substring(2, cityCode.length() - 3);

            Object[][] data = new MySQLQuery("SELECT p.doc, p.doc_type, p.first_name, p.last_name, CONCAT(p.address,' ', n.name), p.phones, p.mail, p.ciiu, p.grand_contrib, p.per_type = 'jur', p.id "
                    + "FROM "
                    + "bill_prospect p "
                    + "INNER JOIN sigma.neigh n ON n.id = p.neigh_id "
                    + "WHERE p.creation_date >= ?1").setParam(1, date).getRecords(conn);

            for (Object[] row : data) {
                String doc = cast.asString(row, 0);
                String docType = cast.asString(row, 1);
                String firstName = cast.asString(row, 2);
                String lastName = cast.asString(row, 3);
                String address = removeLatin(cast.asString(row, 4)).toUpperCase();

                String phones = cast.asString(row, 5);
                String mail = cast.asString(row, 6);
                String ciiu = cast.asString(row, 7);
                Boolean grandContrib = cast.asBoolean(row, 8);
                Boolean jur = cast.asBoolean(row, 9);
                int prospectId = cast.asInt(row, 10);
                jur = jur != null ? jur : false;

                boolean nit = docType.equals("nit");

                replace(s1, count, "A", count - 3);
                replace(s1, count, "B", fillStr(doc, 13));
                replace(s1, count, "C", fillStr("00", 2));
                replace(s1, count, "D", fillStr(doc, 15));
                if (nit) {
                    replace(s1, count, "E", DIANReportServlet.getCheckDigit(doc));
                }
                replace(s1, count, "F", jur ? 1 : 0);
                replace(s1, count, "G", nit ? 1 : 0);
                if (jur) {
                    replace(s1, count, "H", fillStr(((lastName != null ? normalize(lastName) : "") + " " + normalize(firstName)), 50));
                } else {
                    if (lastName == null) {
                        replace(s1, count, "H", fillStr(normalize(firstName), 50));
                    } else {
                        NameSplitter split = NameSplitter.split(lastName);
                        split.cad1 = split.cad1 != null ? normalize(split.cad1) : null;
                        split.cad2 = split.cad2 != null ? normalize(split.cad2) : null;

                        if (split.cad1.length() > 15) {
                            throw new Exception("Los apellidos '" + split.cad1 + "' del cliente con documento " + doc + " tienen más de 15 letras.");
                        }
                        if (split.cad2.length() > 15) {
                            throw new Exception("Los apellidos '" + split.cad2 + "' del cliente con documento " + doc + " tienen más de 15 letras.");
                        }
                        if (firstName.length() > 20) {
                            throw new Exception("Los nombres '" + firstName + "' del cliente con documento " + doc + " tienen más de 20 letras.");
                        }
                        replace(s1, count, "H", fillStr(split.cad1, 15) + fillStr(split.cad2, 15) + fillStr(normalize(firstName), 20));
                    }
                }

                replace(s1, count, "j", Integer.valueOf(getDocTypeCode(docType)));
                replace(s1, count, "K", 1);
                replace(s1, count, "L", 0);
                replace(s1, count, "M", 0);
                replace(s1, count, "N", 0);
                replace(s1, count, "O", 1);
                replace(s1, count, "P", 0);
                replace(s1, count, "Q", 0);
                replace(s1, count, "R", 0);
                replace(s1, count, "S", 169);
                replace(s1, count, "T", deptoCode);
                replace(s1, count, "U", cityCode);
                replace(s1, count, "V", address);
                replace(s1, count, "Y", fillStr(phones.length() > 15 ? phones.substring(0, 15) : phones, 15));
                replace(s1, count, "AB", fillStr(mail, 50));
                replace(s1, count, "AE", fillStr(nit ? ciiu : "0010", 6));
                replace(s1, count, "AF", fillStr("0101", 6));

                replace(s1, count, "AG", inst.cgCoCode);
                replace(s1, count, "AH", inst.cgZoneCode);

                replace(s1, count, "AI", "A");
                replace(s1, count, "AM", jur ? (grandContrib ? "1" : "0") : "0");

                replace(s1, count, "AN", "0");//IND IMP
                replace(s1, count, "AO", "0");//IND RETEN

                replace(s1, count, "AX", "NIU: " + BillClientTankApi.getCode(inst.id, prospectId));//IND RETEN

                replace(s1, count, "BB", "1");//forma de pago
                replace(s1, count, "BC", "30");//condición pago
                replace(s1, count, "BD", "0");//postfechado

                replace(s1, count, "BF", "0");
                replace(s1, count, "BG", "0");
                replace(s1, count, "BH", "0");
                replace(s1, count, "BI", "0");
                replace(s1, count, "BJ", "0");

                count++;
            }
        }
        writable.write();
        writable.close();
        return tmp;
    }

    public static MySQLReport getConverted(Date date, BillCfg cfg, BaseAPI caller, Connection conn) throws Exception {
        List<BillInstance> insts = BillInstance.getAllNet(conn);

        MySQLReport rep = new MySQLReport("Prospectos Convertidos", "", "Hoja 1", caller.now(conn));
        rep.getFormats().add(new utilities.mysqlReport.CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new utilities.mysqlReport.CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$#,##0.0000"));//1
        rep.getFormats().add(new utilities.mysqlReport.CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//2
        rep.getFormats().add(new utilities.mysqlReport.CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "##0.00"));//3
        rep.setZoomFactor(85);
        rep.setVerticalFreeze(5);

        Table tbl = new Table("Prospectos Convertidos");

        tbl.getColumns().add(new Column("Instancia", 20, 0));//0
        tbl.getColumns().add(new Column("Documento", 20, 0));//1
        tbl.getColumns().add(new Column("Nombres", 20, 0));//2
        tbl.getColumns().add(new Column("Código Servicio", 20, 0));//3
        tbl.getColumns().add(new Column("Costo Sin Finan", 20, 1));//4
        tbl.getColumns().add(new Column("Abonos", 20, 1));//5
        tbl.getColumns().add(new Column("Costo a Financiar", 20, 1));//6
        tbl.getColumns().add(new Column("Cuotas", 20, 2));//7
        tbl.getColumns().add(new Column("% Interés", 20, 3));//8
        tbl.getColumns().add(new Column("% IVA Interés", 20, 3));//8
        tbl.getColumns().add(new Column("Costo Interés", 20, 1));//9
        tbl.getColumns().add(new Column("Costo IVA Interés", 20, 1));//10
        tbl.getColumns().add(new Column("Total a Facturar", 20, 1));//11
        tbl.getColumns().add(new Column("Saldo Cartera Inicial ", 20, 1));//12

        List<Object[]> ldata = new ArrayList<>();

        for (int j = 0; j < insts.size(); j++) {
            BillInstance inst = insts.get(j);
            inst.useInstance(conn);

            Object[][] idata = new MySQLQuery("SELECT "
                    + "NULL, "//0 INSTANCIA
                    + "c.doc, "//1
                    + "TRIM(CONCAT(c.first_name, ' ', IFNULL(c.last_name, ''))), "//2
                    + "st.acc_code, "//3
                    + "NULL, "//4 COSTO SIN FINANCIACIÓN  5 + 6
                    + "(SELECT SUM(total) FROM bill_prospect_payment pp WHERE pp.service_id = ps.id), "//5 TOTAL ABONOS
                    + "(SELECT SUM(value - ext_pay) FROM bill_user_service_fee f WHERE f.service_id = s.id), "//6 capital cuotas
                    + "(SELECT COUNT(*) FROM bill_user_service_fee f WHERE f.service_id = s.id), "//7 CUOTAS
                    + "s.credit_inter, "//8 % INTERES
                    + "s.inte_iva_rate, "//9 % IVA INTERES
                    + "(SELECT SUM(IFNULL(inter, 0) - IFNULL(ext_inter, 0)) FROM bill_user_service_fee f WHERE f.service_id = s.id), "//10 TOTAL INTERÉS
                    + "(SELECT SUM(IFNULL(inter_tax, 0) - IFNULL(ext_inter_tax, 0)) FROM bill_user_service_fee f WHERE f.service_id = s.id), "//11 TOTAL IVA INTERÉS
                    + "NULL,"//12
                    + "NULL "//13
                    + "FROM "
                    + "bill_client_tank c "
                    + "INNER JOIN bill_user_service s ON s.bill_client_tank_id = c.id "
                    + "INNER JOIN bill_service_type st ON st.id = s.type_id "
                    + "LEFT JOIN bill_prospect_service ps ON ps.prospect_id = c.prospect_id AND ps.type_id = s.type_id "
                    + "WHERE c.creation_date >= ?1").setParam(1, date).getRecords(conn);

            for (Object[] row : idata) {
                BigDecimal abonos = cast.asBigDecimal(row, 5, true);
                BigDecimal totalCapitalCuotas = cast.asBigDecimal(row, 6, true);
                BigDecimal totalInterCuotas = cast.asBigDecimal(row, 10, true);
                BigDecimal totalIvaInteres = cast.asBigDecimal(row, 11, true);

                BigDecimal costoSinFinan = totalCapitalCuotas.add(abonos);//capital que se pactó con el cliente a la firma de contrato
                BigDecimal totalFacturar = costoSinFinan.add(totalInterCuotas).add(totalIvaInteres); //todo lo que se va a facturar
                BigDecimal carteraInicial = totalFacturar.subtract(abonos);//todo lo que queda pendiente

                row[0] = inst.name;
                row[4] = costoSinFinan; 
                row[12] = totalFacturar;
                row[13] = carteraInicial;
                ldata.add(row);
            }
        }

        if (!ldata.isEmpty()) {
            tbl.setData(ldata);
            rep.getTables().add(tbl);
        }

        return rep;
    }

    private static String removeLatin(String s) {
        s = s.replaceAll("Á", "A");
        s = s.replaceAll("É", "E");
        s = s.replaceAll("Í", "I");
        s = s.replaceAll("Ó", "O");
        s = s.replaceAll("Ú", "U");
        s = s.replaceAll("Ñ", "N");
        return s;
    }

    private static String normalize(String s) {
        s = s.toUpperCase().trim();
        s = removeLatin(s);
        return s.replaceAll("[^A-Z ]", "");
    }
}
