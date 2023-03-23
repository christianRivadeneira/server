package api.bill.rpt.fssri;

import api.bill.model.BillBill;
import api.bill.model.BillCfg;
import api.bill.model.BillClieCau;
import api.bill.model.BillClientTank;
import api.bill.model.BillInstance;
import api.bill.model.BillMarket;
import api.bill.model.BillSpan;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import jxl.format.CellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import utilities.Dates;
import utilities.MySQLQuery;
import utilities.Reports;
import utilities.cast;

public class BillFSSRIReports {

    //los formatos se consiguen en:
    //https://www.minenergia.gov.co/fondo-de-solidaridad1
    //AABAABABABBV pass f1
    //AABABBBBBAAA pass f7
    public static int getColNum(String c) {
        switch (c.length()) {
            case 1:
                return getColNumSingle(c.toLowerCase());
            case 2:
                return ((getColNumSingle(c.substring(0, 1).toLowerCase()) + 1) * 26) + getColNumSingle(c.substring(1, 2).toLowerCase());
            default:
                throw new RuntimeException("There's no support for columns with more than 2 letters. Col " + c);
        }
    }

    private static int getColNumSingle(String c) {
        return c.charAt(0) - 97;
    }

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

    public static void replace(WritableSheet sh, int row, String col, Date n) throws Exception {
        int c = getColNum(col);
        int r = row - 1;
        CellFormat cf = sh.getCell(c, r).getCellFormat();
        if (n != null) {
            if (cf != null) {
                sh.addCell(new jxl.write.DateTime(c, r, n, cf));
            } else {
                sh.addCell(new jxl.write.DateTime(c, r, n));
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
        if (n != null) {
            if (cf != null) {
                sh.addCell(new jxl.write.Number(c, r, n, cf));
            } else {
                sh.addCell(new jxl.write.Number(c, r, n));
            }
        } else {
            if (cf != null) {
                sh.addCell(new jxl.write.Blank(c, r, cf));
            } else {
                sh.addCell(new jxl.write.Blank(c, r));
            }
        }
    }

    public static void replace(WritableSheet sh, int row, String col, BigDecimal n) throws Exception {
        int c = getColNum(col);
        int r = row - 1;
        CellFormat cf = sh.getCell(c, r).getCellFormat();
        if (n != null) {
            if (cf != null) {
                sh.addCell(new jxl.write.Number(c, r, n.doubleValue(), cf));
            } else {
                sh.addCell(new jxl.write.Number(c, r, n.doubleValue()));
            }
        } else {
            if (cf != null) {
                sh.addCell(new jxl.write.Blank(c, r, cf));
            } else {
                sh.addCell(new jxl.write.Blank(c, r));
            }
        }
    }

    private static Date[] getMonths(int year, int trimester) {
        Date m1, m2, m3;
        GregorianCalendar gc = new GregorianCalendar();
        gc.set(GregorianCalendar.YEAR, year);
        gc.set(GregorianCalendar.DAY_OF_MONTH, 1);
        switch (trimester) {
            case 1:
                gc.set(GregorianCalendar.MONTH, GregorianCalendar.JANUARY);
                m1 = Dates.trimDate(gc.getTime());
                gc.set(GregorianCalendar.MONTH, GregorianCalendar.FEBRUARY);
                m2 = Dates.trimDate(gc.getTime());
                gc.set(GregorianCalendar.MONTH, GregorianCalendar.MARCH);
                m3 = Dates.trimDate(gc.getTime());
                break;
            case 2:
                gc.set(GregorianCalendar.MONTH, GregorianCalendar.APRIL);
                m1 = Dates.trimDate(gc.getTime());
                gc.set(GregorianCalendar.MONTH, GregorianCalendar.MAY);
                m2 = Dates.trimDate(gc.getTime());
                gc.set(GregorianCalendar.MONTH, GregorianCalendar.JUNE);
                m3 = Dates.trimDate(gc.getTime());
                break;
            case 3:
                gc.set(GregorianCalendar.MONTH, GregorianCalendar.JULY);
                m1 = Dates.trimDate(gc.getTime());
                gc.set(GregorianCalendar.MONTH, GregorianCalendar.AUGUST);
                m2 = Dates.trimDate(gc.getTime());
                gc.set(GregorianCalendar.MONTH, GregorianCalendar.SEPTEMBER);
                m3 = Dates.trimDate(gc.getTime());
                break;
            case 4:
                gc.set(GregorianCalendar.MONTH, GregorianCalendar.OCTOBER);
                m1 = Dates.trimDate(gc.getTime());
                gc.set(GregorianCalendar.MONTH, GregorianCalendar.NOVEMBER);
                m2 = Dates.trimDate(gc.getTime());
                gc.set(GregorianCalendar.MONTH, GregorianCalendar.DECEMBER);
                m3 = Dates.trimDate(gc.getTime());
                break;
            default:
                throw new RuntimeException();
        }
        return new Date[]{m1, m2, m3};
    }

    private static boolean equals(BigDecimal n1, BigDecimal n2) {
        if (n1 == null && n2 == null) {
            return true;
        } else if (n1 != null && n2 == null) {
            return false;
        } else if (n1 == null && n2 != null) {
            return false;
        } else if (n1 != null && n2 != null) {
            return n1.compareTo(n2) == 0;
        }
        throw new RuntimeException();
    }

    private static void compareSpans(BillMarket mk, BillSpan s1, BillSpan s2, Date month) throws Exception {
        if (s1 == null || s2 == null) {
            throw new Exception("Todas las instancias del mercado deben estar en el mismo periodo");
        }

        if (!equals(s1.cEq1, s2.cEq1)) {
            throw new Exception(mk.name + " tiene diferencias en cEq 2 para " + new SimpleDateFormat("MMMM/yyyy").format(month));
        }
        if (!equals(s1.cEq2, s2.cEq2)) {
            throw new Exception(mk.name + " tiene diferencias en cEq 2 para " + new SimpleDateFormat("MMMM/yyyy").format(month));
        }
        if (!equals(s1.finalTarif1, s2.finalTarif1)) {
            throw new Exception(mk.name + " tiene diferencias en tarifa 1 para " + new SimpleDateFormat("MMMM/yyyy").format(month));
        }
        if (!equals(s1.finalTarif2, s2.finalTarif2)) {
            throw new Exception(mk.name + " tiene diferencias en tarifa 2 para " + new SimpleDateFormat("MMMM/yyyy").format(month));
        }
        if (!equals(s1.subPerc1, s2.subPerc1)) {
            throw new Exception(mk.name + " tiene diferencias en % subsidio 1 para " + new SimpleDateFormat("MMMM/yyyy").format(month));
        }
        if (!equals(s1.subPerc2, s2.subPerc2)) {
            throw new Exception(mk.name + " tiene diferencias en % subsidio 2 para " + new SimpleDateFormat("MMMM/yyyy").format(month));
        }
    }

    public static File getF1(int year, int trimester, boolean showOpenSpans, BillCfg cfg, Connection conn) throws Exception {
        File tmp = File.createTempFile("tmp", ".xls");
        WritableWorkbook writable = Reports.getWorkbook(tmp, BillFSSRIReports.class, "f1.xls");
        WritableSheet s1 = writable.getSheet(1);
        WritableSheet s2 = writable.getSheet(2);

        replace(s1, 4, "c", "Montagas S.A E.S.P");
        replace(s1, 10, "c", trimester);
        replace(s1, 11, "c", year);
        replace(s1, 5, "c", cfg.fssri);
        //replace(s1, 10, "f", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));

        BigDecimal[] gSubS1 = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
        BigDecimal[] gSubS2 = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};

        BigDecimal[] gContE5 = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
        BigDecimal[] gContE6 = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
        BigDecimal[] gContC = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
        BigDecimal[] gContI = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};

        BigDecimal[] gContNoRecE5 = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
        BigDecimal[] gContNoRecE6 = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
        BigDecimal[] gContNoRecC = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
        BigDecimal[] gContNoRecI = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};

        BigDecimal[] gContRecE5 = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
        BigDecimal[] gContRecE6 = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
        BigDecimal[] gContRecC = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};
        BigDecimal[] gContRecI = new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};

        new MySQLQuery("USE sigma;").executeUpdate(conn);

        int curS2Row = 5;

        List<BillMarket> mks = BillMarket.getAll(conn);
        for (int i = 0; i < mks.size(); i++) {
            BillMarket mk = mks.get(i);

            BigDecimal mSubsE1 = BigDecimal.ZERO;

            /*contadores de usuarios, versiones anteriores del reporte los pedian, se dejan comentados por si los vuelven a pedir 
            en el futuro
            int mSubsUsersE1 = 0;
            int mSubsUsersE2 = 0;
            int mContUsersCom = 0;
            int mContUsersInd = 0;
            int mContUsersE5 = 0;
            int mContUsersE6 = 0;*/
            BigDecimal mSubsE2 = BigDecimal.ZERO;
            BigDecimal mContribCom = BigDecimal.ZERO;
            BigDecimal mContribInd = BigDecimal.ZERO;
            BigDecimal mContribE5 = BigDecimal.ZERO;
            BigDecimal mContribE6 = BigDecimal.ZERO;

            //contribuciones no recaudadas 6 meses luego de cobradas por mercado
            BigDecimal mNoRecCom = BigDecimal.ZERO;
            BigDecimal mNoRecInd = BigDecimal.ZERO;
            BigDecimal mNoRecE5 = BigDecimal.ZERO;
            BigDecimal mNoRecE6 = BigDecimal.ZERO;

            //recaudadas luego de haber aparecido en el listado anterior
            BigDecimal mRecCom = BigDecimal.ZERO;
            BigDecimal mRecInd = BigDecimal.ZERO;
            BigDecimal mRecE5 = BigDecimal.ZERO;
            BigDecimal mRecE6 = BigDecimal.ZERO;

            Date[] months = getMonths(year, trimester);

            new MySQLQuery("USE sigma;").executeUpdate(conn);
            List<BillInstance> insts = BillInstance.getBillingByMarket(mk.id, conn);

            if (!insts.isEmpty()) {
                BillSpan mSp1 = BillSpan.getByMonth(insts.get(0).db, months[0], conn);
                BillSpan mSp2 = BillSpan.getByMonth(insts.get(0).db, months[1], conn);
                BillSpan mSp3 = BillSpan.getByMonth(insts.get(0).db, months[2], conn);

                for (int j = 0; j < insts.size(); j++) {
                    BillInstance inst = insts.get(j);
                    new MySQLQuery("USE " + inst.db + ";").executeUpdate(conn);
                    BillSpan sp1 = BillSpan.getByMonth(inst.db, months[0], conn);
                    BillSpan sp2 = BillSpan.getByMonth(inst.db, months[1], conn);
                    BillSpan sp3 = BillSpan.getByMonth(inst.db, months[2], conn);

                    List<BillSpan> spans = new ArrayList<>();
                    if (sp1 != null) {
                        compareSpans(mk, sp1, mSp1, months[0]);
                        spans.add(sp1);
                        if (sp2 != null) {
                            compareSpans(mk, sp2, mSp2, months[0]);
                            spans.add(sp2);
                            if (sp3 != null) {
                                compareSpans(mk, sp3, mSp3, months[0]);
                                spans.add(sp3);
                            }
                        }
                    }

                    /*contadores de usuarios, versiones anteriores del reporte los pedian, se dejan comentados por si los vuelven a pedir 
                    en el futuro            
                    String subsUsrsQ = "SELECT COUNT(*) FROM bill_clie_cau c "
                            + "WHERE "
                            + "c.stratum = ?1 AND c.span_id = ?2 AND c.sector = 'r' AND c.val_subs > 0";
                    String contribRUsrsQ = "SELECT COUNT(*) FROM bill_clie_cau c "
                            + "WHERE "
                            + "c.stratum = ?1 AND c.span_id = ?2 AND c.sector = 'r' AND val_contrib > 0";
                    String contribNrUsrsQ = "SELECT COUNT(*) FROM bill_clie_cau c "
                            + "WHERE "
                            + "c.sector = ?1 AND c.span_id = ?2 AND val_contrib > 0";*/
                    String subsValQ = "SELECT SUM(v) FROM ("
                            + "SELECT c.val_subs AS v FROM bill_clie_cau c "
                            + "WHERE "
                            + "c.stratum = ?1 AND c.span_id = ?2 AND c.sector = 'r' AND c.val_subs > 0 "
                            + "UNION ALL "
                            + "SELECT c.diff_val_subs AS v FROM bill_clie_rebill c "
                            + "WHERE "
                            + "c.new_stratum = ?1 AND c.rebill_span_id = ?2 AND c.new_sector = 'r' AND c.diff_val_subs IS NOT NULL AND c.active) AS l";

                    String contribRValQ = "SELECT SUM(v) FROM "
                            + "(SELECT val_contrib + ifnull(contrib_interest, 0) AS v FROM bill_clie_cau c "
                            + "WHERE "
                            + "c.stratum = ?1 AND c.span_id = ?2 AND c.sector = 'r' AND val_contrib > 0 "
                            + "UNION ALL "
                            + "SELECT diff_val_contrib AS v FROM bill_clie_rebill c "
                            + "WHERE "
                            + "c.new_stratum = ?1 AND c.rebill_span_id = ?2 AND c.new_sector = 'r' AND diff_val_contrib IS NOT NULL AND c.active) AS l";

                    String contribNrValQ = "SELECT SUM(v) FROM "
                            + "(SELECT val_contrib + ifnull(contrib_interest, 0) AS v FROM bill_clie_cau c "
                            + "WHERE "
                            + "c.sector = ?1 AND c.span_id = ?2 AND val_contrib > 0 "
                            + "UNION ALL "
                            + "SELECT diff_val_contrib AS v FROM bill_clie_rebill c "
                            + "WHERE "
                            + "c.new_sector = ?1 AND c.rebill_span_id = ?2 AND diff_val_contrib IS NOT NULL AND c.active) AS l";

                    for (int k = 0; k < spans.size(); k++) {
                        BillSpan span = spans.get(k);
                        if (!span.state.equals("cons") || showOpenSpans) {

                            ContribRecNoRec contrib = ContribRecNoRec.calc(span.id, conn);

                            gContRecE5[k] = gContRecE5[k].add(contrib.rec.get("5"));
                            gContRecE6[k] = gContRecE6[k].add(contrib.rec.get("6"));
                            gContRecI[k] = gContRecI[k].add(contrib.rec.get("i"));
                            gContRecC[k] = gContRecC[k].add(contrib.rec.get("c"));

                            gContNoRecE5[k] = gContNoRecE5[k].add(contrib.noRec.get("5"));
                            gContNoRecE6[k] = gContNoRecE6[k].add(contrib.noRec.get("6"));
                            gContNoRecI[k] = gContNoRecI[k].add(contrib.noRec.get("i"));
                            gContNoRecC[k] = gContNoRecC[k].add(contrib.noRec.get("c"));

                            //contribuciones no recaudadas 6 meses luego de cobradas por mercado
                            mNoRecCom = mNoRecCom.add(contrib.noRec.get("c"));
                            mNoRecInd = mNoRecInd.add(contrib.noRec.get("i"));
                            mNoRecE5 = mNoRecE5.add(contrib.noRec.get("5"));
                            mNoRecE6 = mNoRecE6.add(contrib.noRec.get("6"));

                            //recaudadas luego de haber aparecido en el listado anterior
                            mRecCom = mRecCom.add(contrib.rec.get("c"));
                            mRecInd = mRecInd.add(contrib.rec.get("i"));
                            mRecE5 = mRecE5.add(contrib.rec.get("5"));
                            mRecE6 = mRecE6.add(contrib.rec.get("6"));

                            BigDecimal subS1i = new MySQLQuery(subsValQ).setParam(1, 1).setParam(2, span.id).getAsBigDecimal(conn, true);
                            BigDecimal subS2i = new MySQLQuery(subsValQ).setParam(1, 2).setParam(2, span.id).getAsBigDecimal(conn, true);

                            BigDecimal contE5i = new MySQLQuery(contribRValQ).setParam(1, 5).setParam(2, span.id).getAsBigDecimal(conn, true);
                            BigDecimal contE6i = new MySQLQuery(contribRValQ).setParam(1, 6).setParam(2, span.id).getAsBigDecimal(conn, true);
                            BigDecimal contIi = new MySQLQuery(contribNrValQ).setParam(1, "i").setParam(2, span.id).getAsBigDecimal(conn, true);
                            BigDecimal contCi = new MySQLQuery(contribNrValQ).setParam(1, "c").setParam(2, span.id).getAsBigDecimal(conn, true);

                            /* contadores de usuarios, versiones anteriores del reporte los pedian, se dejan comentados por si los vuelven a pedir 
                    en el futuro            
                            mSubsUsersE1 += new MySQLQuery(subsUsrsQ).setParam(1, 1).setParam(2, span.id).getAsInteger(conn, true);
                            mSubsUsersE2 += new MySQLQuery(subsUsrsQ).setParam(1, 2).setParam(2, span.id).getAsInteger(conn, true);
                            mContUsersE5 += new MySQLQuery(contribRUsrsQ).setParam(1, 5).setParam(2, span.id).getAsInteger(conn, true);
                            mContUsersE6 += new MySQLQuery(contribRUsrsQ).setParam(1, 6).setParam(2, span.id).getAsInteger(conn, true);
                            mContUsersInd += new MySQLQuery(contribNrUsrsQ).setParam(1, "i").setParam(2, span.id).getAsInteger(conn, true);
                            mContUsersCom += new MySQLQuery(contribNrUsrsQ).setParam(1, "c").setParam(2, span.id).getAsInteger(conn, true);*/
                            gSubS1[k] = gSubS1[k].add(subS1i);
                            gSubS2[k] = gSubS2[k].add(subS2i);

                            gContE5[k] = gContE5[k].add(contE5i);
                            gContE6[k] = gContE6[k].add(contE6i);

                            gContI[k] = gContI[k].add(contIi);
                            gContC[k] = gContC[k].add(contCi);

                            mSubsE1 = mSubsE1.add(subS1i);
                            mSubsE2 = mSubsE2.add(subS2i);

                            mContribCom = mContribCom.add(contCi);
                            mContribInd = mContribInd.add(contIi);
                            mContribE5 = mContribE5.add(contE5i);
                            mContribE6 = mContribE6.add(contE6i);
                        }
                    }
                }

                addS2Row(s2, curS2Row++, "Res. Estrato 1 (Mercado " + mk.name + ")", mSubsE1, null, null, null);
                addS2Row(s2, curS2Row++, "Res. Estrato 2 (Mercado " + mk.name + ")", mSubsE2, null, null, null);
                addS2Row(s2, curS2Row++, "Res. Estrato 5 (Mercado " + mk.name + ")", null, mContribE5, mNoRecE5, mRecE5);
                addS2Row(s2, curS2Row++, "Res. Estrato 6 (Mercado " + mk.name + ")", null, mContribE6, mNoRecE6, mRecE6);
                addS2Row(s2, curS2Row++, "Comerciales (Mercado " + mk.name + ")", null, mContribCom, mNoRecCom, mRecCom);
                addS2Row(s2, curS2Row++, "Industriales (Mercado " + mk.name + ")", null, mContribInd, mNoRecInd, mRecInd);
            }

        }

        replace(s1, 15, "c", gSubS1[0]);
        replace(s1, 15, "d", gSubS1[1]);
        replace(s1, 15, "e", gSubS1[2]);

        replace(s1, 16, "c", gSubS2[0]);
        replace(s1, 16, "d", gSubS2[1]);
        replace(s1, 16, "e", gSubS2[2]);

        replace(s1, 22, "c", gContE5[0]);
        replace(s1, 22, "d", gContE5[1]);
        replace(s1, 22, "e", gContE5[2]);

        replace(s1, 23, "c", gContE6[0]);
        replace(s1, 23, "d", gContE6[1]);
        replace(s1, 23, "e", gContE6[2]);

        replace(s1, 24, "c", gContC[0]);
        replace(s1, 24, "d", gContC[1]);
        replace(s1, 24, "e", gContC[2]);

        replace(s1, 25, "c", gContI[0]);
        replace(s1, 25, "d", gContI[1]);
        replace(s1, 25, "e", gContI[2]);

        replace(s1, 29, "c", gContRecE5[0]);
        replace(s1, 29, "d", gContRecE5[1]);
        replace(s1, 29, "e", gContRecE5[2]);

        replace(s1, 30, "c", gContRecE6[0]);
        replace(s1, 30, "d", gContRecE6[1]);
        replace(s1, 30, "e", gContRecE6[2]);

        replace(s1, 31, "c", gContRecC[0]);
        replace(s1, 31, "d", gContRecC[1]);
        replace(s1, 31, "e", gContRecC[2]);

        replace(s1, 32, "c", gContRecI[0]);
        replace(s1, 32, "d", gContRecI[1]);
        replace(s1, 32, "e", gContRecI[2]);

        replace(s1, 36, "c", gContNoRecE5[0]);
        replace(s1, 36, "d", gContNoRecE5[1]);
        replace(s1, 36, "e", gContNoRecE5[2]);

        replace(s1, 37, "c", gContNoRecE6[0]);
        replace(s1, 37, "d", gContNoRecE6[1]);
        replace(s1, 37, "e", gContNoRecE6[2]);

        replace(s1, 38, "c", gContNoRecC[0]);
        replace(s1, 38, "d", gContNoRecC[1]);
        replace(s1, 38, "e", gContNoRecC[2]);

        replace(s1, 39, "c", gContNoRecI[0]);
        replace(s1, 39, "d", gContNoRecI[1]);
        replace(s1, 39, "e", gContNoRecI[2]);

        s1.getSettings().setSelected(true);

        writable.write();
        writable.close();
        return tmp;
    }

    private static void addS2Row(WritableSheet s2, int row, String label, BigDecimal subs, BigDecimal cont, BigDecimal rec, BigDecimal noRec) throws Exception {
        replace(s2, row, "a", label);
        replace(s2, row, "b", subs != null ? subs : BigDecimal.ZERO);
        replace(s2, row, "c", cont != null ? cont : BigDecimal.ZERO);
        replace(s2, row, "d", noRec != null ? noRec : BigDecimal.ZERO);
        replace(s2, row, "e", rec != null ? rec : BigDecimal.ZERO);
    }

    public static File getF2(int year, int trimester, boolean showOpenSpans, BillCfg cfg, Connection conn) throws Exception {
        File tmp = File.createTempFile("tmp", ".xls");
        WritableWorkbook writable = Reports.getWorkbook(tmp, BillFSSRIReports.class, "f2.xls");
        WritableSheet s = writable.getSheet(0);
        Date[] months = getMonths(year, trimester);

        new MySQLQuery("USE sigma;").executeUpdate(conn);
        List<BillMarket> mks = BillMarket.getAll(conn);
        int row = 2;
        for (int i = 0; i < mks.size(); i++) {
            BillMarket mk = mks.get(i);
            new MySQLQuery("USE sigma;").executeUpdate(conn);
            List<BillInstance> insts = BillInstance.getBillingByMarket(mk.id, conn);
            if (insts.size() > 0) {
                for (Date month : months) {
                    GregorianCalendar gm = new GregorianCalendar();
                    gm.setTime(month);
                    //se supone que los spans de las instancias del mismo mercado deben ser iguales así que se toma el primero como referencia, luego se compara con los demás
                    BillSpan mkSpan = BillSpan.getByMonth(gm.get(GregorianCalendar.YEAR), gm.get(GregorianCalendar.MONTH) + 1, insts.get(0), conn);
                    for (int stratum = 1; stratum <= 2; stratum++) {
                        for (int r = 1; r <= 2; r++) {

                            if (mkSpan != null && (!mkSpan.state.equals("cons") || showOpenSpans)) {
                                for (int k = 0; k < insts.size(); k++) {
                                    int usrs = 0;
                                    BillInstance inst = insts.get(k);
                                    BillSpan cons = BillSpan.getByMonth(gm.get(GregorianCalendar.YEAR), gm.get(GregorianCalendar.MONTH) + 1, inst, conn);
                                    if (cons == null) {
                                        throw new Exception(mk.name + " no tiene un periodo de facturación para " + new SimpleDateFormat("MMMM/yyyy").format(month));
                                    }
                                    compareSpans(mk, mkSpan, cons, month);

                                    BigDecimal m3cons;
                                    BigDecimal consValue;
                                    BigDecimal subsValue = BigDecimal.ZERO;

                                    if (r == 1) {
                                        subsValue = new MySQLQuery("SELECT SUM(val_subs) FROM " + inst.db + ".bill_clie_cau WHERE m3_subs > 0 AND span_id = ?1 AND stratum = ?2").setParam(1, cons.id).setParam(2, stratum).getAsBigDecimal(conn, true);
                                        consValue = new MySQLQuery("SELECT SUM(val_cons_subs) FROM " + inst.db + ".bill_clie_cau WHERE m3_subs > 0 AND span_id = ?1 AND stratum = ?2").setParam(1, cons.id).setParam(2, stratum).getAsBigDecimal(conn, true);
                                        m3cons = new MySQLQuery("SELECT SUM(m3_subs) FROM " + inst.db + ".bill_clie_cau WHERE m3_subs > 0 AND span_id = ?1 AND stratum = ?2").setParam(1, cons.id).setParam(2, stratum).getAsBigDecimal(conn, true);
                                        usrs += new MySQLQuery("SELECT COUNT(*) FROM " + inst.db + ".bill_clie_cau WHERE m3_subs > 0 AND m3_no_subs = 0 AND span_id = ?1 AND stratum = ?2").setParam(1, cons.id).setParam(2, stratum).getAsInteger(conn);
                                    } else {
                                        consValue = new MySQLQuery("SELECT SUM(val_cons_no_subs) FROM " + inst.db + ".bill_clie_cau WHERE m3_subs > 0 AND m3_no_subs > 0 AND span_id = ?1 AND stratum = ?2").setParam(1, cons.id).setParam(2, stratum).getAsBigDecimal(conn, true);
                                        m3cons = new MySQLQuery("SELECT SUM(m3_no_subs) FROM " + inst.db + ".bill_clie_cau WHERE m3_subs > 0 AND m3_no_subs > 0 AND span_id = ?1 AND stratum = ?2").setParam(1, cons.id).setParam(2, stratum).getAsBigDecimal(conn, true);
                                        usrs += new MySQLQuery("SELECT COUNT(*) FROM " + inst.db + ".bill_clie_cau WHERE m3_subs > 0 AND m3_no_subs > 0 AND span_id = ?1 AND stratum = ?2").setParam(1, cons.id).setParam(2, stratum).getAsInteger(conn);
                                    }

                                    replace(s, row, "A", cfg.fssri);
                                    replace(s, row, "B", gm.get(GregorianCalendar.YEAR));
                                    replace(s, row, "C", gm.get(GregorianCalendar.MONTH) + 1);
                                    replace(s, row, "D", mk.fssriCode);
                                    replace(s, row, "E", inst.pobId);
                                    replace(s, row, "F", gm.get(GregorianCalendar.YEAR));
                                    replace(s, row, "G", gm.get(GregorianCalendar.MONTH) + 1);
                                    replace(s, row, "H", stratum);
                                    replace(s, row, "I", r);

                                    if (r == 1) {
                                        replace(s, row, "J", stratum == 1 ? mkSpan.cEq1 : mkSpan.cEq2);
                                        replace(s, row, "K", stratum == 1 ? mkSpan.finalTarif1 : mkSpan.finalTarif2);
                                        replace(s, row, "L", stratum == 1 ? mkSpan.subPerc1 : mkSpan.subPerc2);
                                        replace(s, row, "O", subsValue);

                                        BigDecimal expCons = m3cons.multiply(stratum == 1 ? mkSpan.cEq1 : mkSpan.cEq2).setScale(4, RoundingMode.HALF_EVEN);
                                        if (!equalsWith2dec(expCons, consValue)) {
                                            throw new Exception("Se hallaron inconsistencias " + expCons + " " + consValue);
                                        }
                                    } else {
                                        BigDecimal expCons = m3cons.multiply(mkSpan.cuvR).setScale(4, RoundingMode.HALF_EVEN);
                                        if (!equalsWith2dec(expCons, consValue)) {
                                            throw new Exception("Se hallaron inconsistencias " + expCons + " " + consValue);
                                        }
                                        replace(s, row, "J", mkSpan.cuvR);
                                    }
                                    replace(s, row, "M", m3cons);
                                    replace(s, row, "N", usrs);
                                    replace(s, row, "Q", cfg.fssri);
                                    row++;
                                }//fin instancias
                            }
                        }

                        if (mkSpan != null && (!mkSpan.state.equals("cons") || showOpenSpans)) {
                            for (int k = 0; k < insts.size(); k++) {
                                BillInstance inst = insts.get(k);
                                BillSpan cons = BillSpan.getByMonth(gm.get(GregorianCalendar.YEAR), gm.get(GregorianCalendar.MONTH) + 1, inst, conn);
                                Object[][] dbRebillData = new MySQLQuery(""
                                        + "SELECT "
                                        + "cons_month, COUNT(*), SUM(r.diff_val_subs) "
                                        + "FROM "
                                        + inst.db + ".bill_clie_rebill r "
                                        + "INNER JOIN " + inst.db + ".bill_span s ON s.id = r.error_span_id "
                                        + "WHERE r.new_sector = 'r' AND r.new_stratum = ?2 AND r.diff_val_subs <> 0 AND r.rebill_span_id = ?1 AND r.active GROUP BY s.id").setParam(1, cons.id).setParam(2, stratum).getRecords(conn);

                                for (Object[] dbRebillRow : dbRebillData) {
                                    GregorianCalendar rebGc = new GregorianCalendar();
                                    rebGc.setTime(cast.asDate(dbRebillRow, 0));
                                    int rebUsrs = cast.asInt(dbRebillRow, 1);
                                    BigDecimal rebVal = cast.asBigDecimal(dbRebillRow, 2, true);
                                    addF1AdjRow(s, mk, inst, row++, cfg, rebGc, gm, stratum, rebUsrs, rebVal);
                                }
                            }
                        }
                    }
                }
            }
        }
        writable.write();
        writable.close();
        return tmp;
    }

    private static void addF1AdjRow(WritableSheet s, BillMarket mk, BillInstance inst, int row, BillCfg cfg, GregorianCalendar errorGc, GregorianCalendar billCg, int stratum, int usrs, BigDecimal adj) throws Exception {
        replace(s, row, "A", cfg.fssri);
        replace(s, row, "B", billCg.get(GregorianCalendar.YEAR));
        replace(s, row, "C", billCg.get(GregorianCalendar.MONTH) + 1);
        replace(s, row, "D", mk.fssriCode);
        replace(s, row, "E", inst.pobId);
        replace(s, row, "F", errorGc.get(GregorianCalendar.YEAR));
        replace(s, row, "G", errorGc.get(GregorianCalendar.MONTH) + 1);

        replace(s, row, "H", stratum);
        replace(s, row, "N", usrs);
        replace(s, row, "P", adj);
        replace(s, row, "Q", cfg.fssri);
    }

    private static boolean equalsWith2dec(BigDecimal n1, BigDecimal n2) {
        return n1.subtract(n2).abs().compareTo(new BigDecimal(5)) < 0;
    }

    //contribuciones
    public static File getF3(int year, int trimester, boolean showOpenSpans, BillCfg cfg, Connection conn) throws Exception {
        File tmp = File.createTempFile("tmp", ".xls");
        WritableWorkbook writable = Reports.getWorkbook(tmp, BillFSSRIReports.class, "f3.xls");
        WritableSheet s = writable.getSheet(0);
        Date[] months = getMonths(year, trimester);

        new MySQLQuery("USE sigma;").executeUpdate(conn);
        List<BillMarket> mks = BillMarket.getAll(conn);
        int row = 2;
        for (int i = 0; i < mks.size(); i++) {
            BillMarket mk = mks.get(i);
            new MySQLQuery("USE sigma;").executeUpdate(conn);
            List<BillInstance> insts = BillInstance.getBillingByMarket(mk.id, conn);
            if (insts.size() > 0) {
                for (Date month : months) {
                    GregorianCalendar gm = new GregorianCalendar();
                    gm.setTime(month);
                    //se supone que los spans de las instancias del mismo mercado deben ser iguales así que se toma el primero como referencia, luego se compara con los demás
                    BillSpan mkSpan = BillSpan.getByMonth(gm.get(GregorianCalendar.YEAR), gm.get(GregorianCalendar.MONTH) + 1, insts.get(0), conn);

                    for (int stratum = 5; stratum <= 8; stratum++) {
                        String filter;
                        String rebillFilter;
                        switch (stratum) {
                            case 5:
                                filter = "sector = 'r' AND stratum = 5";
                                rebillFilter = "r.new_sector = 'r' AND r.new_stratum = 5";
                                break;
                            case 6:
                                filter = "sector = 'r' AND stratum = 6";
                                rebillFilter = "r.new_sector = 'r' AND r.new_stratum = 6";
                                break;
                            case 7:
                                filter = "sector = 'c'";
                                rebillFilter = "r.new_sector = 'c'";
                                break;
                            case 8:
                                filter = "sector = 'i'";
                                rebillFilter = "r.new_sector = 'i'";
                                break;
                            default:
                                throw new RuntimeException();
                        }

                        if (mkSpan != null && (!mkSpan.state.equals("cons") || showOpenSpans)) {
                            for (int k = 0; k < insts.size(); k++) {
                                BillInstance inst = insts.get(k);
                                BillSpan cons = BillSpan.getByMonth(gm.get(GregorianCalendar.YEAR), gm.get(GregorianCalendar.MONTH) + 1, inst, conn);
                                if (cons == null) {
                                    throw new Exception(mk.name + " no tiene un periodo de facturación para " + new SimpleDateFormat("MMMM/yyyy").format(month));
                                }
                                compareSpans(mk, mkSpan, cons, month);

                                Object[] contribRow = new MySQLQuery("SELECT "
                                        + "SUM(val_contrib), "
                                        + "SUM(val_cons_no_subs), "
                                        + "SUM(m3_no_subs), "
                                        + "COUNT(*) "
                                        + "FROM "
                                        + inst.db + ".bill_clie_cau "
                                        + "WHERE " + filter + " AND span_id = ?1 AND val_contrib > 0"
                                ).setParam(1, cons.id).getRecord(conn);

                                BigDecimal valContrib = cast.asBigDecimal(contribRow, 0, true);
                                BigDecimal valCons = cast.asBigDecimal(contribRow, 1, true);
                                BigDecimal m3Cons = cast.asBigDecimal(contribRow, 2, true);
                                int contribUsrs = cast.asInt(contribRow, 3);

                                replace(s, row, "A", cfg.fssri);
                                replace(s, row, "B", gm.get(GregorianCalendar.YEAR));
                                replace(s, row, "C", gm.get(GregorianCalendar.MONTH) + 1);
                                replace(s, row, "D", mk.fssriCode);
                                replace(s, row, "E", inst.pobId);
                                replace(s, row, "F", "");
                                replace(s, row, "G", gm.get(GregorianCalendar.YEAR));
                                replace(s, row, "H", gm.get(GregorianCalendar.MONTH) + 1);
                                replace(s, row, "I", "R");
                                replace(s, row, "J", stratum);
                                replace(s, row, "K", stratum);
                                replace(s, row, "L", contribUsrs);
                                replace(s, row, "M", m3Cons);
                                replace(s, row, "N", 1);
                                replace(s, row, "O", mkSpan.cuf);
                                replace(s, row, "P", stratum == 5 || stratum == 6 ? mkSpan.cuvR : mkSpan.cuvNr);
                                replace(s, row, "Q", valCons);
                                replace(s, row, "R", (stratum == 5 || stratum == 6 ? mkSpan.contribR : mkSpan.contribNr));
                                replace(s, row, "S", valContrib);
                                replace(s, row, "W", cfg.fssri);

                                row++;

                                //intereses de mora de las contribuciones no pagadas
                                int inteUsrs = new MySQLQuery("SELECT COUNT(*) FROM " + inst.db + ".bill_clie_cau WHERE " + filter + " AND contrib_interest > 0 AND span_id = ?1").setParam(1, cons.id).setParam(2, stratum).getAsInteger(conn, true);
                                BigDecimal interValue = new MySQLQuery("SELECT SUM(contrib_interest) FROM " + inst.db + ".bill_clie_cau WHERE " + filter + " AND contrib_interest > 0 AND span_id = ?1").setParam(1, cons.id).getAsBigDecimal(conn, true);
                                if (interValue.compareTo(BigDecimal.ZERO) > 0) {
                                    GregorianCalendar lm = (GregorianCalendar) gm.clone();
                                    lm.add(GregorianCalendar.MONTH, -1);
                                    addF3adjRow(s, mk, inst, row++, cfg, lm, gm, stratum, inteUsrs, interValue, "Int. Contribuciones no pagadas - " + mk.name);
                                }

                                //refacturaciones
                                Object[][] dbRebillData = new MySQLQuery(""
                                        + "SELECT "
                                        + "cons_month, COUNT(*), SUM(r.diff_val_contrib) FROM "
                                        + inst.db + ".bill_clie_rebill r "
                                        + "INNER JOIN " + inst.db + ".bill_span s ON s.id = r.error_span_id "
                                        + "WHERE " + rebillFilter + " AND r.diff_val_contrib <> 0 AND r.rebill_span_id = ?1 AND r.active GROUP BY s.id").setParam(1, cons.id).getRecords(conn);

                                for (Object[] dbRebillRow : dbRebillData) {
                                    Date errorMonth = cast.asDate(dbRebillRow, 0);
                                    GregorianCalendar rebGc = new GregorianCalendar();
                                    rebGc.setTime(errorMonth);
                                    int rebUsrs = cast.asInt(dbRebillRow, 1);
                                    BigDecimal rebVal = cast.asBigDecimal(dbRebillRow, 2);
                                    addF3adjRow(s, mk, inst, row++, cfg, rebGc, gm, stratum, rebUsrs, rebVal, "Refacturación - " + mk.name);

                                }
                            }
                        }
                    }
                }
            }
        }
        writable.write();
        writable.close();
        return tmp;
    }

    private static void addF3adjRow(WritableSheet s, BillMarket mk, BillInstance inst, int row, BillCfg cfg, GregorianCalendar errorGc, GregorianCalendar spanGc, int stratum, int usrs, BigDecimal adj, String label) throws Exception {
        replace(s, row, "A", cfg.fssri);
        replace(s, row, "B", spanGc.get(GregorianCalendar.YEAR));
        replace(s, row, "C", spanGc.get(GregorianCalendar.MONTH) + 1);

        replace(s, row, "D", mk.fssriCode);
        replace(s, row, "E", inst.pobId);
        replace(s, row, "F", "");
        replace(s, row, "G", errorGc.get(GregorianCalendar.YEAR));
        replace(s, row, "H", errorGc.get(GregorianCalendar.MONTH) + 1);
        replace(s, row, "I", "R");
        replace(s, row, "J", stratum);
        replace(s, row, "K", stratum);
        replace(s, row, "L", usrs);
        replace(s, row, "T", adj);
        replace(s, row, "U", label);
        replace(s, row, "W", cfg.fssri);
    }

    public static File getF6(int year, int trimester, boolean showOpenSpans, BillCfg cfg, Connection conn,String nameForm) throws Exception {
        File tmp = File.createTempFile("tmp", ".xls");
        WritableWorkbook writable=null;
        if(!nameForm.equalsIgnoreCase("GRC8")){
            writable = Reports.getWorkbook(tmp, BillFSSRIReports.class, "f6.xls");
        }
        else{
            writable = Reports.getWorkbook(tmp, BillFSSRIReports.class, "grc8.xls");
        }
        
        WritableSheet s = writable.getSheet(0);
        Date[] months = getMonths(year, trimester);

        new MySQLQuery("USE sigma;").executeUpdate(conn);
        List<BillMarket> mks = BillMarket.getAll(conn);
        int row = 2;
        for (int i = 0; i < mks.size(); i++) {
            BillMarket mk = mks.get(i);
            new MySQLQuery("USE sigma;").executeUpdate(conn);
            List<BillInstance> insts = BillInstance.getBillingByMarket(mk.id, conn);
            if (insts.size() > 0) {
                GregorianCalendar gm = new GregorianCalendar();
                gm.setTime(months[0]);
                BillSpan mkSpan1 = BillSpan.getByMonth(gm.get(GregorianCalendar.YEAR), gm.get(GregorianCalendar.MONTH) + 1, insts.get(0), conn);
                gm.setTime(months[1]);
                BillSpan mkSpan2 = BillSpan.getByMonth(gm.get(GregorianCalendar.YEAR), gm.get(GregorianCalendar.MONTH) + 1, insts.get(0), conn);
                gm.setTime(months[2]);
                BillSpan mkSpan3 = BillSpan.getByMonth(gm.get(GregorianCalendar.YEAR), gm.get(GregorianCalendar.MONTH) + 1, insts.get(0), conn);

                for (int k = 0; k < insts.size(); k++) {
                    BillInstance inst = insts.get(k);
                    inst.useInstance(conn);

                    gm.setTime(months[0]);
                    BillSpan instSpan1 = BillSpan.getByMonth(gm.get(GregorianCalendar.YEAR), gm.get(GregorianCalendar.MONTH) + 1, inst, conn);
                    gm.setTime(months[1]);
                    BillSpan instSpan2 = BillSpan.getByMonth(gm.get(GregorianCalendar.YEAR), gm.get(GregorianCalendar.MONTH) + 1, inst, conn);
                    gm.setTime(months[2]);
                    BillSpan instSpan3 = BillSpan.getByMonth(gm.get(GregorianCalendar.YEAR), gm.get(GregorianCalendar.MONTH) + 1, inst, conn);

                    List<BillSpan> spans = new ArrayList<>();
                    if (instSpan1 != null && (!instSpan1.state.equals("cons") || showOpenSpans)) {
                        compareSpans(mk, mkSpan1, instSpan1, months[0]);
                        spans.add(instSpan1);
                        if (instSpan2 != null && (!instSpan2.state.equals("cons") || showOpenSpans)) {
                            compareSpans(mk, mkSpan2, instSpan2, months[1]);
                            spans.add(instSpan2);
                            if (instSpan3 != null && (!instSpan3.state.equals("cons") || showOpenSpans)) {
                                compareSpans(mk, mkSpan3, instSpan3, months[2]);
                                spans.add(instSpan3);
                            }
                        }
                    }
                    Object[][] billsData =null;
                    for (int j = 0; j < spans.size(); j++) {
                        BillSpan span = spans.get(j);
                         
                        if(nameForm.equalsIgnoreCase("format6")){
                            billsData = new MySQLQuery(""
                                + "SELECT DISTINCT "
                                + "concat(c.first_name, ' ', ifnull(c.last_name, '')),"
                                + "c.code, "
                                + "cc.sector, "
                                + "cc.stratum, "
                                + "cc.ciiu_code, "
                                + "COALESCE(act.description, cc.activity), "
                                + "cc.m3_no_subs, "
                                + "val_cons_no_subs "
                                + "FROM bill_clie_cau cc "
                                + "INNER JOIN bill_client_tank c ON c.id = cc.client_id "
                                + "LEFT JOIN sigma.prov_ciiu_activity act ON CAST(act.code AS SIGNED) = CAST(cc.ciiu_code AS SIGNED) "
                                + "WHERE cc.span_id = ?1 AND cc.val_exc_contrib > 0").setParam(1, span.id).getRecords(conn);
                            
                            gm.setTime(span.consMonth);
                            for (Object[] billsRow : billsData) {
                                String name = cast.asString(billsRow, 0);
                                String code = cast.asString(billsRow, 1);
                                String sector = cast.asString(billsRow, 2);
                                int stratum = cast.asInt(billsRow, 3);
                                String ciiuCode = cast.asString(billsRow, 4);
                                String ciiuName = cast.asString(billsRow, 5);
                                BigDecimal m3 = cast.asBigDecimal(billsRow, 6, true);
                                BigDecimal cons = cast.asBigDecimal(billsRow, 7, true);

                                if (sector.equals("c")) {
                                    stratum = 7;
                                } else if (sector.equals("i")) {
                                    stratum = 8;
                                }

                                replace(s, row, "A", cfg.fssri);
                                replace(s, row, "B", gm.get(GregorianCalendar.YEAR));
                                replace(s, row, "C", gm.get(GregorianCalendar.MONTH) + 1);
                                replace(s, row, "D", name);
                                replace(s, row, "E", code);
                                replace(s, row, "F", stratum);
                                replace(s, row, "G", ciiuCode);
                                replace(s, row, "H", ciiuName);
                                replace(s, row, "I", inst.pobId);
                                replace(s, row, "J", m3);
                                replace(s, row, "K", cons);
                                replace(s, row, "L", cfg.fssri);

                                row++;
                            }
                        }
                        if(nameForm.equalsIgnoreCase("GRC8")){
                            billsData = new MySQLQuery("SELECT c.code, "
                                + "c.doc, "
                                + "3, "
                                + "DATE_FORMAT(bs.begin_date,'%d-%m-%Y'), "
                                + "1, "
                                + "cc.ciiu_code "
                                + "FROM bill_clie_cau cc "
                                + "INNER JOIN bill_client_tank c ON c.id = cc.client_id "
                                + "LEFT JOIN sigma.prov_ciiu_activity act ON CAST(act.code AS SIGNED) = CAST(cc.ciiu_code AS SIGNED) "
                                    + "LEFT JOIN bill_span bs ON bs.id =cc.span_id "
                                + "WHERE cc.span_id = ?1 AND cc.val_exc_contrib > 0").setParam(1, span.id).getRecords(conn);
                            gm.setTime(span.consMonth);
                            for (Object[] billsRow : billsData) {
                                String code = cast.asString(billsRow, 0);
                                String doc = cast.asString(billsRow, 1);
                                String typeGas = cast.asString(billsRow, 2);
                                String fecha = cast.asString(billsRow, 3);
                                String typeNovedad = cast.asString(billsRow, 4);
                                String ciiu = cast.asString(billsRow, 5);

                                replace(s, row, "A", code);
                                replace(s, row, "B", doc);
                                replace(s, row, "C", typeGas);
                                replace(s, row, "D", fecha);
                                replace(s, row, "E", typeNovedad);
                                replace(s, row, "F", ciiu);

                                row++;
                            }
                        }
                    }
                }
            }
        }
        writable.write();
        writable.close();
        return tmp;
    }

  /*================= GRF1 =====================*/  
     public static File getF14(int year, int trimester, boolean showOpenSpans, BillCfg cfg, Connection conn,String nameForm) throws Exception {
        File tmp = File.createTempFile("tmp", ".xls");
        WritableWorkbook writable=null;
        
         //CADENA DE TEXTO CONECTADA A (grf2.xls)
        String  CodFSSRI_Exentos, AñoFactura_Exento, MesFactura_Exento, Nombre_usuario, NIU_usuario, Tipo_usuario, Codigo_CIIU_Actividad, Nombre_Actividad, CodDANE, Consumo, Vr_Facturacion, CodFSSRIIncumbente_ContGas;
        
        
        if(!nameForm.equalsIgnoreCase("GRF1")){
            writable = Reports.getWorkbook(tmp, BillFSSRIReports.class, "grf1.xls");
        }
        else{
            writable = Reports.getWorkbook(tmp, BillFSSRIReports.class, "grf1.xls");
        }
        
        WritableSheet s = writable.getSheet(0);
        Date[] months = getMonths(year, trimester);

        new MySQLQuery("USE sigma;").executeUpdate(conn);
        List<BillMarket> mks = BillMarket.getAll(conn);
        int row = 2;
        for (int i = 0; i < mks.size(); i++) {
            BillMarket mk = mks.get(i);
            new MySQLQuery("USE sigma;").executeUpdate(conn);
            List<BillInstance> insts = BillInstance.getBillingByMarket(mk.id, conn);
            if (insts.size() > 0) {
                GregorianCalendar gm = new GregorianCalendar();
                gm.setTime(months[0]);
                BillSpan mkSpan1 = BillSpan.getByMonth(gm.get(GregorianCalendar.YEAR), gm.get(GregorianCalendar.MONTH) + 1, insts.get(0), conn);
                gm.setTime(months[1]);
                BillSpan mkSpan2 = BillSpan.getByMonth(gm.get(GregorianCalendar.YEAR), gm.get(GregorianCalendar.MONTH) + 1, insts.get(0), conn);
                gm.setTime(months[2]);
                BillSpan mkSpan3 = BillSpan.getByMonth(gm.get(GregorianCalendar.YEAR), gm.get(GregorianCalendar.MONTH) + 1, insts.get(0), conn);

                for (int k = 0; k < insts.size(); k++) {
                    BillInstance inst = insts.get(k);
                    inst.useInstance(conn);

                    gm.setTime(months[0]);
                    BillSpan instSpan1 = BillSpan.getByMonth(gm.get(GregorianCalendar.YEAR), gm.get(GregorianCalendar.MONTH) + 1, inst, conn);
                    gm.setTime(months[1]);
                    BillSpan instSpan2 = BillSpan.getByMonth(gm.get(GregorianCalendar.YEAR), gm.get(GregorianCalendar.MONTH) + 1, inst, conn);
                    gm.setTime(months[2]);
                    BillSpan instSpan3 = BillSpan.getByMonth(gm.get(GregorianCalendar.YEAR), gm.get(GregorianCalendar.MONTH) + 1, inst, conn);

                    List<BillSpan> spans = new ArrayList<>();
                    if (instSpan1 != null && (!instSpan1.state.equals("cons") || showOpenSpans)) {
                        compareSpans(mk, mkSpan1, instSpan1, months[0]);
                        spans.add(instSpan1);
                        if (instSpan2 != null && (!instSpan2.state.equals("cons") || showOpenSpans)) {
                            compareSpans(mk, mkSpan2, instSpan2, months[1]);
                            spans.add(instSpan2);
                            if (instSpan3 != null && (!instSpan3.state.equals("cons") || showOpenSpans)) {
                                compareSpans(mk, mkSpan3, instSpan3, months[2]);
                                spans.add(instSpan3);
                            }
                        }
                    }
                    Object[][] billsData =null;
                    for (int j = 0; j < spans.size(); j++) {
                        BillSpan span = spans.get(j);
                           // Conexion a base de datos GRF1
                        if(nameForm.equalsIgnoreCase("format6")){
                            billsData = new MySQLQuery(null).setParam(1, span.id).getRecords(conn);
                            
                            gm.setTime(span.consMonth);
                            for (Object[] billsRow : billsData) {
                                String name = cast.asString(billsRow, 0);
                                String code = cast.asString(billsRow, 1);
                                String sector = cast.asString(billsRow, 2);
                                int stratum = cast.asInt(billsRow, 3);
                                String ciiuCode = cast.asString(billsRow, 4);
                                String ciiuName = cast.asString(billsRow, 5);
                                BigDecimal m3 = cast.asBigDecimal(billsRow, 6, true);
                                BigDecimal cons = cast.asBigDecimal(billsRow, 7, true);

                                if (sector.equals("c")) {
                                    stratum = 7;
                                } else if (sector.equals("i")) {
                                    stratum = 8;
                                }

                                replace(s, row, "A", cfg.fssri);
                                replace(s, row, "B", gm.get(GregorianCalendar.YEAR));
                                replace(s, row, "C", gm.get(GregorianCalendar.MONTH) + 1);
                                replace(s, row, "D", name);
                                replace(s, row, "E", code);
                                replace(s, row, "F", stratum);
                                replace(s, row, "G", ciiuCode);
                                replace(s, row, "H", ciiuName);
                                replace(s, row, "I", inst.pobId);
                                replace(s, row, "J", m3);
                                replace(s, row, "K", cons);
                                replace(s, row, "L", cfg.fssri);

                                row++;
                            }
                        }
                        // Conexion a base de datos GRF1
                        if(nameForm.equalsIgnoreCase("format6")){
                            billsData = new MySQLQuery(null).setParam(1, span.id).getRecords(conn);
                            
                            gm.setTime(span.consMonth);
                            for (Object[] billsRow : billsData) {
                                String name = cast.asString(billsRow, 0);
                                String code = cast.asString(billsRow, 1);
                                String sector = cast.asString(billsRow, 2);
                                int stratum = cast.asInt(billsRow, 3);
                                String ciiuCode = cast.asString(billsRow, 4);
                                String ciiuName = cast.asString(billsRow, 5);
                                BigDecimal m3 = cast.asBigDecimal(billsRow, 6, true);
                                BigDecimal cons = cast.asBigDecimal(billsRow, 7, true);

                                if (sector.equals("c")) {
                                    stratum = 7;
                                } else if (sector.equals("i")) {
                                    stratum = 8;
                                }

                                replace(s, row, "A", cfg.fssri);
                                replace(s, row, "B", gm.get(GregorianCalendar.YEAR));
                                replace(s, row, "C", gm.get(GregorianCalendar.MONTH) + 1);
                                replace(s, row, "D", name);
                                replace(s, row, "E", code);
                                replace(s, row, "F", stratum);
                                replace(s, row, "G", ciiuCode);
                                replace(s, row, "H", ciiuName);
                                replace(s, row, "I", inst.pobId);
                                replace(s, row, "J", m3);
                                replace(s, row, "K", cons);
                                replace(s, row, "L", cfg.fssri);

                                row++;
                            }
                        }
                    }
                }
            }
        }
        writable.write();
        writable.close();
        return tmp;
    }

    
  /*================== final GRF1 ====================*/
    
    public static File getF7Old(int year, int trimester, boolean showOpenSpans, BillCfg cfg, Connection conn) throws Exception {
        File tmp = File.createTempFile("tmp", ".xls");
        WritableWorkbook writable = Reports.getWorkbook(tmp, BillFSSRIReports.class, "f7Old.xls");
        WritableSheet s = writable.getSheet(1);

        replace(s, 4, "E", cfg.fssri);
        replace(s, 6, "E", trimester);
        replace(s, 6, "G", year);

        Date[] tmpMonths = getMonths(year, trimester);

        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(tmpMonths[0]);
        gc.add(GregorianCalendar.MONTH, -1);

        Date[] months = new Date[4];
        months[0] = gc.getTime();
        months[1] = tmpMonths[0];
        months[2] = tmpMonths[1];
        months[3] = tmpMonths[2];

        new MySQLQuery("USE sigma;").executeUpdate(conn);
        List<BillMarket> mks = BillMarket.getAll(conn);

        for (int j = 0; j < mks.size(); j++) {
            BillMarket mk = mks.get(j);
            new MySQLQuery("USE sigma;").executeUpdate(conn);
            List<BillInstance> insts = BillInstance.getBillingByMarket(mk.id, conn);
            if (insts.size() > 0) {
                for (int i = 0; i < months.length; i++) {
                    for (int stratum = 1; stratum <= 2; stratum++) {
                        Date month = months[i];

                        GregorianCalendar gm = new GregorianCalendar();
                        gm.setTime(month);
                        //se supone que los spans de las instancias del mismo mercado deben ser iguales así que se toma el primero como referencia, luego se compara con los demás
                        BillSpan mkSpan = BillSpan.getByMonth(gm.get(GregorianCalendar.YEAR), gm.get(GregorianCalendar.MONTH) + 1, insts.get(0), conn);

                        if (mkSpan != null && (!mkSpan.state.equals("cons") || showOpenSpans)) {
                            for (int k = 0; k < insts.size(); k++) {
                                BillInstance inst = insts.get(k);
                                BillSpan cons = BillSpan.getByMonth(gm.get(GregorianCalendar.YEAR), gm.get(GregorianCalendar.MONTH) + 1, inst, conn);
                                if (cons == null) {
                                    throw new Exception(mk.name + " no tiene un periodo de facturación para " + new SimpleDateFormat("MMMM/yyyy").format(month));
                                }
                                compareSpans(mk, mkSpan, cons, month);
                            }

                            if ((stratum == 1 ? mkSpan.subPerc1 : mkSpan.subPerc2) != null && mkSpan.paramsDone) {
                                int row = 14 + ((mk.fssriCode - 1) * 8) + i + ((stratum - 1) * 4);
                                replace(s, row, "E", stratum == 1 ? mkSpan.cEq1 : mkSpan.cEq2);
                                replace(s, row, "H", stratum == 1 ? mkSpan.rawTarif1 : mkSpan.rawTarif2);
                                replace(s, row, "I", (stratum == 1 ? mkSpan.subPerc1 : mkSpan.subPerc2).divide(new BigDecimal(100), 4, RoundingMode.HALF_EVEN));
                                replace(s, row, "J", cfg.fssri);
                            }
                        }
                    }
                }
            }
        }
        writable.write();
        writable.close();
        return tmp;
    }
    
    /*================= GRF2 =====================*/  
     public static File getF1143(int year, int trimester, boolean showOpenSpans, BillCfg cfg, Connection conn,String nameForm) throws Exception {
        File tmp = File.createTempFile("tmp", ".xls");
        WritableWorkbook writable=null;
        //CADENA DE TEXTO CONECTADA A (grf2.xls)
        String  CodFSSRI_Exentos, AñoFactura_Exento, MesFactura_Exento, Nombre_usuario, NIU_usuario, Tipo_usuario, Codigo_CIIU_Actividad, Nombre_Actividad, CodDANE, Consumo, Vr_Facturacion, CodFSSRIIncumbente_ContGas;
        
        if(!nameForm.equalsIgnoreCase("GRF2")){
            
            writable = Reports.getWorkbook(tmp, BillFSSRIReports.class, "grf2.xls");
        }
        else{
            writable = Reports.getWorkbook(tmp, BillFSSRIReports.class, "grf2.xls");
        }
        
        WritableSheet s = writable.getSheet(0);
        Date[] months = getMonths(year, trimester);

        new MySQLQuery("USE sigma;").executeUpdate(conn);
        List<BillMarket> mks = BillMarket.getAll(conn);
        int row = 2;
        for (int i = 0; i < mks.size(); i++) {
            BillMarket mk = mks.get(i);
            new MySQLQuery("USE sigma;").executeUpdate(conn);
            List<BillInstance> insts = BillInstance.getBillingByMarket(mk.id, conn);
            if (insts.size() > 0) {
                GregorianCalendar gm = new GregorianCalendar();
                gm.setTime(months[0]);
                BillSpan mkSpan1 = BillSpan.getByMonth(gm.get(GregorianCalendar.YEAR), gm.get(GregorianCalendar.MONTH) + 1, insts.get(0), conn);
                gm.setTime(months[1]);
                BillSpan mkSpan2 = BillSpan.getByMonth(gm.get(GregorianCalendar.YEAR), gm.get(GregorianCalendar.MONTH) + 1, insts.get(0), conn);
                gm.setTime(months[2]);
                BillSpan mkSpan3 = BillSpan.getByMonth(gm.get(GregorianCalendar.YEAR), gm.get(GregorianCalendar.MONTH) + 1, insts.get(0), conn);

                for (int k = 0; k < insts.size(); k++) {
                    BillInstance inst = insts.get(k);
                    inst.useInstance(conn);

                    gm.setTime(months[0]);
                    BillSpan instSpan1 = BillSpan.getByMonth(gm.get(GregorianCalendar.YEAR), gm.get(GregorianCalendar.MONTH) + 1, inst, conn);
                    gm.setTime(months[1]);
                    BillSpan instSpan2 = BillSpan.getByMonth(gm.get(GregorianCalendar.YEAR), gm.get(GregorianCalendar.MONTH) + 1, inst, conn);
                    gm.setTime(months[2]);
                    BillSpan instSpan3 = BillSpan.getByMonth(gm.get(GregorianCalendar.YEAR), gm.get(GregorianCalendar.MONTH) + 1, inst, conn);

                    List<BillSpan> spans = new ArrayList<>();
                    if (instSpan1 != null && (!instSpan1.state.equals("cons") || showOpenSpans)) {
                        compareSpans(mk, mkSpan1, instSpan1, months[0]);
                        spans.add(instSpan1);
                        if (instSpan2 != null && (!instSpan2.state.equals("cons") || showOpenSpans)) {
                            compareSpans(mk, mkSpan2, instSpan2, months[1]);
                            spans.add(instSpan2);
                            if (instSpan3 != null && (!instSpan3.state.equals("cons") || showOpenSpans)) {
                                compareSpans(mk, mkSpan3, instSpan3, months[2]);
                                spans.add(instSpan3);
                            }
                        }
                    }
                    Object[][] billsData =null;
                    for (int j = 0; j < spans.size(); j++) {
                        BillSpan span = spans.get(j);
                           // Conexion a base de datos GRF1
                        if(nameForm.equalsIgnoreCase("format6")){
                            billsData = new MySQLQuery(null).setParam(1, span.id).getRecords(conn);
                            
                            gm.setTime(span.consMonth);
                            for (Object[] billsRow : billsData) {
                                String name = cast.asString(billsRow, 0);
                                String code = cast.asString(billsRow, 1);
                                String sector = cast.asString(billsRow, 2);
                                int stratum = cast.asInt(billsRow, 3);
                                String ciiuCode = cast.asString(billsRow, 4);
                                String ciiuName = cast.asString(billsRow, 5);
                                BigDecimal m3 = cast.asBigDecimal(billsRow, 6, true);
                                BigDecimal cons = cast.asBigDecimal(billsRow, 7, true);

                                if (sector.equals("c")) {
                                    stratum = 7;
                                } else if (sector.equals("i")) {
                                    stratum = 8;
                                }

                                replace(s, row, "A", cfg.fssri);
                                replace(s, row, "B", gm.get(GregorianCalendar.YEAR));
                                replace(s, row, "C", gm.get(GregorianCalendar.MONTH) + 1);
                                replace(s, row, "D", name);
                                replace(s, row, "E", code);
                                replace(s, row, "F", stratum);
                                replace(s, row, "G", ciiuCode);
                                replace(s, row, "H", ciiuName);
                                replace(s, row, "I", inst.pobId);
                                replace(s, row, "J", m3);
                                replace(s, row, "K", cons);
                                replace(s, row, "L", cfg.fssri);

                                row++;
                            }
                        }
                        // Conexion a base de datos GRF1
                        if(nameForm.equalsIgnoreCase("format6")){
                            billsData = new MySQLQuery(null).setParam(1, span.id).getRecords(conn);
                            
                            gm.setTime(span.consMonth);
                            for (Object[] billsRow : billsData) {
                                String name = cast.asString(billsRow, 0);
                                String code = cast.asString(billsRow, 1);
                                String sector = cast.asString(billsRow, 2);
                                int stratum = cast.asInt(billsRow, 3);
                                String ciiuCode = cast.asString(billsRow, 4);
                                String ciiuName = cast.asString(billsRow, 5);
                                BigDecimal m3 = cast.asBigDecimal(billsRow, 6, true);
                                BigDecimal cons = cast.asBigDecimal(billsRow, 7, true);

                                if (sector.equals("c")) {
                                    stratum = 7;
                                } else if (sector.equals("i")) {
                                    stratum = 8;
                                }

                                replace(s, row, "A", cfg.fssri);
                                replace(s, row, "B", gm.get(GregorianCalendar.YEAR));
                                replace(s, row, "C", gm.get(GregorianCalendar.MONTH) + 1);
                                replace(s, row, "D", name);
                                replace(s, row, "E", code);
                                replace(s, row, "F", stratum);
                                replace(s, row, "G", ciiuCode);
                                replace(s, row, "H", ciiuName);
                                replace(s, row, "I", inst.pobId);
                                replace(s, row, "J", m3);
                                replace(s, row, "K", cons);
                                replace(s, row, "L", cfg.fssri);

                                row++;
                            }
                        }
                    }
                }
            }
        }
        writable.write();
        writable.close();
        return tmp;
    }
    
  /*================== final  GRF2 ====================*/

    public static File getF7(int year, int trimester, boolean showOpenSpans, BillCfg cfg, Connection conn) throws Exception {
        File tmp = File.createTempFile("tmp", ".xls");
        WritableWorkbook writable = Reports.getWorkbook(tmp, BillFSSRIReports.class, "f7.xls");
        WritableSheet s = writable.getSheet(0);

        new MySQLQuery("USE sigma;").executeUpdate(conn);
        int row = 1;

        List<BillMarket> mks = BillMarket.getAll(conn);
        for (int i = 0; i < mks.size(); i++) {
            BillMarket mk = mks.get(i);
            Date[] months = getMonths(year, trimester);

            new MySQLQuery("USE sigma;").executeUpdate(conn);
            List<BillInstance> insts = BillInstance.getBillingByMarket(mk.id, conn);

            if (!insts.isEmpty()) {
                BillSpan mSp1 = BillSpan.getByMonth(insts.get(0).db, months[0], conn);
                BillSpan mSp2 = BillSpan.getByMonth(insts.get(0).db, months[1], conn);
                BillSpan mSp3 = BillSpan.getByMonth(insts.get(0).db, months[2], conn);

                for (int j = 0; j < insts.size(); j++) {
                    BillInstance inst = insts.get(j);
                    new MySQLQuery("USE " + inst.db + ";").executeUpdate(conn);
                    BillSpan sp1 = BillSpan.getByMonth(inst.db, months[0], conn);
                    BillSpan sp2 = BillSpan.getByMonth(inst.db, months[1], conn);
                    BillSpan sp3 = BillSpan.getByMonth(inst.db, months[2], conn);

                    List<BillSpan> spans = new ArrayList<>();
                    if (sp1 != null) {
                        compareSpans(mk, sp1, mSp1, months[0]);
                        spans.add(sp1);
                        if (sp2 != null) {
                            compareSpans(mk, sp2, mSp2, months[0]);
                            spans.add(sp2);
                            if (sp3 != null) {
                                compareSpans(mk, sp3, mSp3, months[0]);
                                spans.add(sp3);
                            }
                        }
                    }

                    for (int k = 0; k < spans.size(); k++) {
                        BillSpan span = spans.get(k);
                        if (!span.state.equals("cons") || showOpenSpans) {
                            ContribRecNoRec contrib = ContribRecNoRec.calc(span.id, conn);
                            for (int l = 0; l < contrib.noRecDetail.size(); l++) {
                                ContribRecNoRec.Contrib c = contrib.noRecDetail.get(l);
                                BillClieCau cau = BillClieCau.getByClientSpan(c.clientId, c.spanid, conn);

                                int billId = new MySQLQuery("SELECT id FROM bill_bill b "
                                        + "WHERE b.client_tank_id = ?1 AND b.span_id = 2 "
                                        + "LIMIT 1 "
                                        + "ORDER BY b.creation_date DESC")
                                        .setParam(1, c.clientId)
                                        .setParam(2, c.spanid)
                                        .getAsInteger(conn);

                                BillBill bill = BillBill.getById(billId, conn);
                                BillClientTank clie = new BillClientTank().select(bill.clientTankId, conn);

                                replace(s, row, "A", cfg.fssri);
                                replace(s, row, "B", bill.billNum);
                                replace(s, row, "D", bill.creationDate);
                                replace(s, row, "D", clie.perType.equals("nat") ? clie.code : clie.doc);

                                switch (cau.sector) {
                                    case "r":
                                        replace(s, row, "E", cau.stratum);
                                        break;
                                    case "c":
                                        replace(s, row, "E", 7);
                                        break;
                                    case "i":
                                        replace(s, row, "E", 8);
                                        break;
                                    default:
                                        break;
                                }

                                replace(s, row, "F", inst.pobId);
                                replace(s, row, "G", mk.fssriCode);
                                replace(s, row, "H", cfg.fssri);
                                replace(s, row, "I", cau.m3NoSubs);
                                replace(s, row, "J", cau.fixedCharge);
                                replace(s, row, "K", cau.valConsNoSubs);
                                replace(s, row, "L", cau.fixedCharge.add(cau.valConsNoSubs));
                                replace(s, row, "M", cau.sector.equals("r") ? span.contribR : span.contribNr);
                                replace(s, row, "N", cau.valContrib);
                                row++;
                            }
                        }
                    }
                }
            }
        }

        s.getSettings().setSelected(true);

        writable.write();
        writable.close();
        return tmp;
    }

    public static File getF8(int year, int trimester, boolean showOpenSpans, BillCfg cfg, Connection conn) throws Exception {
        File tmp = File.createTempFile("tmp", ".xls");
        WritableWorkbook writable = Reports.getWorkbook(tmp, BillFSSRIReports.class, "f8.xls");
        WritableSheet s = writable.getSheet(0);

        new MySQLQuery("USE sigma;").executeUpdate(conn);
        int row = 1;

        List<BillMarket> mks = BillMarket.getAll(conn);
        for (int i = 0; i < mks.size(); i++) {
            BillMarket mk = mks.get(i);
            Date[] months = getMonths(year, trimester);

            new MySQLQuery("USE sigma;").executeUpdate(conn);
            List<BillInstance> insts = BillInstance.getBillingByMarket(mk.id, conn);

            if (!insts.isEmpty()) {
                BillSpan mSp1 = BillSpan.getByMonth(insts.get(0).db, months[0], conn);
                BillSpan mSp2 = BillSpan.getByMonth(insts.get(0).db, months[1], conn);
                BillSpan mSp3 = BillSpan.getByMonth(insts.get(0).db, months[2], conn);

                for (int j = 0; j < insts.size(); j++) {
                    BillInstance inst = insts.get(j);
                    new MySQLQuery("USE " + inst.db + ";").executeUpdate(conn);
                    BillSpan sp1 = BillSpan.getByMonth(inst.db, months[0], conn);
                    BillSpan sp2 = BillSpan.getByMonth(inst.db, months[1], conn);
                    BillSpan sp3 = BillSpan.getByMonth(inst.db, months[2], conn);

                    List<BillSpan> spans = new ArrayList<>();
                    if (sp1 != null) {
                        compareSpans(mk, sp1, mSp1, months[0]);
                        spans.add(sp1);
                        if (sp2 != null) {
                            compareSpans(mk, sp2, mSp2, months[0]);
                            spans.add(sp2);
                            if (sp3 != null) {
                                compareSpans(mk, sp3, mSp3, months[0]);
                                spans.add(sp3);
                            }
                        }
                    }

                    for (int k = 0; k < spans.size(); k++) {
                        BillSpan span = spans.get(k);
                        if (!span.state.equals("cons") || showOpenSpans) {
                            ContribRecNoRec contrib = ContribRecNoRec.calc(span.id, conn);
                            for (int l = 0; l < contrib.recDetail.size(); l++) {
                                ContribRecNoRec.Contrib c = contrib.noRecDetail.get(l);
                                BillClieCau cau = BillClieCau.getByClientSpan(c.clientId, c.spanid, conn);

                                int billId = new MySQLQuery("SELECT id FROM bill_bill b "
                                        + "WHERE b.client_tank_id = ?1 AND b.span_id = 2 "
                                        + "LIMIT 1 "
                                        + "ORDER BY b.creation_date DESC")
                                        .setParam(1, c.clientId)
                                        .setParam(2, c.spanid)
                                        .getAsInteger(conn);

                                BillBill bill = BillBill.getById(billId, conn);
                                BillClientTank clie = new BillClientTank().select(bill.clientTankId, conn);

                                replace(s, row, "A", cfg.fssri);
                                replace(s, row, "B", bill.billNum);
                                replace(s, row, "D", bill.creationDate);
                                replace(s, row, "D", clie.perType.equals("nat") ? clie.code : clie.doc);

                                switch (cau.sector) {
                                    case "r":
                                        replace(s, row, "E", cau.stratum);
                                        break;
                                    case "c":
                                        replace(s, row, "E", 7);
                                        break;
                                    case "i":
                                        replace(s, row, "E", 8);
                                        break;
                                    default:
                                        break;
                                }

                                replace(s, row, "F", inst.pobId);
                                replace(s, row, "G", mk.fssriCode);
                                replace(s, row, "H", cfg.fssri);
                                replace(s, row, "I", cau.m3NoSubs);
                                replace(s, row, "J", cau.fixedCharge);
                                replace(s, row, "K", cau.valConsNoSubs);
                                replace(s, row, "L", cau.fixedCharge.add(cau.valConsNoSubs));
                                replace(s, row, "M", cau.sector.equals("r") ? span.contribR : span.contribNr);
                                replace(s, row, "N", cau.valContrib);
                                row++;
                            }
                        }
                    }
                }
            }
        }

        s.getSettings().setSelected(true);

        writable.write();
        writable.close();
        return tmp;
    }
}
