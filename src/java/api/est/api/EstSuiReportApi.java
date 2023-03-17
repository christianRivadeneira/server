/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.est.api;

import api.BaseAPI;
import api.bill.model.BillInstance;
import api.bill.model.BillPriceSpan;
import api.bill.model.BillSpan;
import api.est.model.EstCfg;
import api.est.model.EstTankCategory;
import api.sys.model.City;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import api.sys.model.SysCfg;
import utilities.MySQLQuery;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.SummaryRow;
import utilities.mysqlReport.Table;
import web.billing.BillingServlet;

@Path("/estSuiReport")
public class EstSuiReportApi extends BaseAPI {

    public static final int ventas = 0;
    public static final int remisiones = 1;

    @POST
    public Response getReport(@QueryParam("type") int type, @QueryParam("year") int year, @QueryParam("month") int month,
            @QueryParam("catId") Integer catId, @QueryParam("cityId") Integer cityId, @QueryParam("instNum") boolean instNum) throws Exception {

        try (Connection gralConn = getConnection(); Connection instConn = getConnection()) {
            getSession(gralConn);

            //useBillInstance(conn)
            MySQLReport rep = new MySQLReport((instNum ? "Reporte Nro Instalación" : "Reporte SUI"), "", "rept_sui", now(gralConn));
            List<String> subt = new ArrayList<>();
            City city = cityId != null ? new City().select(cityId, gralConn) : null;
            EstTankCategory categ = (catId != null ? new EstTankCategory().select(catId, gralConn) : null);
            EstCfg modCfg = new EstCfg().select(1, gralConn);
            SysCfg sysCfg = SysCfg.select(gralConn);

            GregorianCalendar gc = new GregorianCalendar();
            gc.set(GregorianCalendar.YEAR, year);
            gc.set(GregorianCalendar.MONTH, month - 1);
            Date monthDate = gc.getTime();

            subt.add("Ciudad: " + (city != null ? city.name + "." : "Todas") + " Categoria: " + (categ != null ? categ.description + "." : "Todas"));

            rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
            rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$#,##0.000")); //1
            rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.000"));//2
            rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.RIGHT));//3
            rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//4
            rep.getFormats().get(0).setWrap(true);
            rep.setZoomFactor(85);

            switch (type) {
                case ventas:
                    subt.add("Tipo: Ventas. Mes: " + new SimpleDateFormat("MMMM/yyyy").format(monthDate));

                    Object[][] salesData = new MySQLQuery("SELECT "//ventas != 'build'
                            + (modCfg.unifiedCenter ? " "
                                    + "(SELECT IFNULL(sc.nie,0) "
                                    + "FROM sys_center sc "
                                    + "WHERE sc.id= c.sys_center_id LIMIT 1), "
                                    : " ct.nie, ")
                            + "IF(s.est_tank_id IS NOT NULL,(SELECT et.serial FROM est_tank et WHERE et.id=s.est_tank_id),(SELECT GROUP_CONCAT(t.serial ORDER BY t.serial SEPARATOR ', ') FROM est_tank AS t WHERE t.client_id = c.id)), "
                            + "IF(s.est_tank_id IS NOT NULL,(SELECT et.capacity FROM est_tank et WHERE et.id = s.est_tank_id),(SELECT GROUP_CONCAT(t.capacity ORDER BY t.serial SEPARATOR ', ') FROM est_tank AS t WHERE t.client_id = c.id)), "
                            + "ct.name,"
                            + (modCfg.unifiedCenter ? "ct.code, " : "ct.dane_code, ")
                            + "(SELECT cat.description FROM est_tank_category AS cat WHERE cat.id = c.categ_id), "
                            + "(SELECT ct.description FROM est_categ_type AS ct INNER JOIN est_tank_category AS etc ON etc.type_id = ct.id WHERE etc.id = c.categ_id), "
                            + "DATE_FORMAT(s.sale_date,'%d/%m/%Y'), "
                            + "s.bill_num, "
                            + "c.document, "
                            + "c.`name`, "
                            + "' ',"
                            + "s.kgs, "
                            + "s.total "
                            + "FROM est_sale AS s "
                            + "INNER JOIN ord_tank_client AS c ON s.client_id = c.id "
                            + (modCfg.unifiedCenter
                                    ? "INNER JOIN dane_poblado ct ON ct.code = c.dane_pob "
                                    : "INNER JOIN city ct ON c.city_id = ct.id ")
                            + "WHERE " //c.type != 'build' AND "
                            + "s.cancel = 0 "
                            + "AND MONTH(s.sale_date) = ?1 AND YEAR(s.sale_date) = ?2 "
                            + (cityId != null ? "AND c.city_id = " + cityId + " " : " ")
                            + (catId != null ? "AND c.categ_id = " + catId + " " : " ")
                            + "AND s.bill_type in ('cort','fac','cntr') "
                    ).setParam(1, month).setParam(2, year).getRecords(gralConn);

                    if (salesData.length > 0) {
                        Table tblSales = new Table("Ventas");
                        tblSales.getColumns().add(new Column("Nie", 5, 4));
                        tblSales.getColumns().add(new Column("Cit", 20, 0));
                        tblSales.getColumns().add(new Column("Cap. Gls", 20, 0));
                        tblSales.getColumns().add(new Column("Poblado", 15, 3));
                        tblSales.getColumns().add(new Column("Dane", 15, 3));
                        tblSales.getColumns().add(new Column("Categoria", 15, 0));
                        tblSales.getColumns().add(new Column("Tipo", 15, 0));
                        tblSales.getColumns().add(new Column("Fecha", 15, 3));
                        tblSales.getColumns().add(new Column("Factura", 15, 3));
                        tblSales.getColumns().add(new Column("Documento", 15, 3));
                        tblSales.getColumns().add(new Column("Cliente", 55, 0));
                        tblSales.getColumns().add(new Column("m3", 15, 0));
                        tblSales.getColumns().add(new Column("kgs", 15, 2));
                        tblSales.getColumns().add(new Column("Valor", 20, 1));
                        tblSales.setSummaryRow(new SummaryRow("Total", 10));
                        tblSales.setData(salesData);
                        rep.getTables().add(tblSales);
                    }

                    break;
                case remisiones:
                    subt.add("Tipo: Remisiones. Mes: " + new SimpleDateFormat("MMMM/yyyy").format(monthDate));
                    remisiones(year, month, cityId, catId, instNum, modCfg, sysCfg, rep, instConn, gralConn);
                    break;
                default:
                    throw new RuntimeException("Tipo no reconocido: " + type);
            }
            rep.setSubTitles(subt);
            useDefault(gralConn);
            return createResponse(rep.write(gralConn), "sui.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private static void remisiones(int year, int month, Integer cityId, Integer catId, boolean instNum, EstCfg modCfg, SysCfg sysCfg, MySQLReport rep, Connection instConn, Connection gralConn) throws Exception {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Object[][] biData = new MySQLQuery("SELECT DISTINCT bi.id "
                + "FROM ord_tank_client AS cl "
                + "INNER JOIN bill_instance AS bi ON bi.id = cl.bill_instance_id "
                + "WHERE cl.type = 'build' AND cl.active = 1 AND bi.db IS NOT NULL AND bi.db <> '' "
                + (cityId != null ? "AND bi.city_id = " + cityId : "")
        ).getRecords(gralConn);

        List<Object[]> tData = new ArrayList<>();

        for (Object[] biRow : biData) {
            int instId = MySQLQuery.getAsInteger(biRow[0]);
            BillInstance bi = BillingServlet.getInst(instId);
            bi.useInstance(instConn);
            BillSpan span = BillSpan.getByMonth(year, month, bi, instConn);

            if (span != null && !span.state.equals("cons")) {
                boolean hasPrices = new MySQLQuery(("SELECT COUNT(*) > 0 "
                        + "FROM bill_price_span AS ps "
                        + "WHERE ps.span_id = " + span.id)).getAsBoolean(instConn);

                if (hasPrices) {
                    Object[][] buildsData = new MySQLQuery("SELECT DISTINCT "//remisiones type = 'build'
                            + "c.id, "
                            + (modCfg.unifiedCenter ? " "
                                    + "(SELECT IFNULL(sc.nie,0) "
                                    + "FROM sys_center sc "
                                    + "WHERE sc.id= c.sys_center_id LIMIT 1), "
                                    : " ct.nie, ")
                            + "(SELECT GROUP_CONCAT(t.serial ORDER BY t.serial SEPARATOR ', ') FROM est_tank AS t WHERE t.client_id = c.id), "
                            + "(SELECT GROUP_CONCAT(t.capacity ORDER BY t.serial SEPARATOR ', ') FROM est_tank AS t WHERE t.client_id = c.id), "
                            + "ct.name,"
                            + "c.dane_pob, "
                            + "(SELECT cat.description FROM est_tank_category AS cat WHERE cat.id = c.categ_id), "
                            + "(SELECT ct.description FROM est_categ_type AS ct INNER JOIN est_tank_category AS etc ON etc.type_id = ct.id WHERE etc.id = c.categ_id), "
                            + "c.mirror_id, "
                            + "c.`name`,"
                            + "c.ref_client_id "
                            + "FROM ord_tank_client AS c "
                            + "INNER JOIN bill_instance AS bi ON bi.id = c.bill_instance_id "
                            + "INNER JOIN city ct ON ct.id = bi.city_id "
                            + "WHERE c.type='build' AND bi.id = " + bi.id + " "
                            + (cityId != null ? "AND bi.city_id = " + cityId + " " : " ")
                            + (catId != null ? "AND c.categ_id = " + catId + " " : " ")
                            + "ORDER BY bi.id"
                    ).getRecords(gralConn);

                    for (Object[] row : buildsData) {
                        Build build = new Build(
                                (Integer) row[0],//id
                                MySQLQuery.getAsInteger(row[1]),//nie
                                (String) row[2],//tanques
                                MySQLQuery.getAsString(row[3]),//capacidades
                                (String) row[4], //poblado
                                (Integer) row[5],//dane
                                (String) row[6],//categoriga
                                (String) row[7],//tipo
                                (Integer) row[8],//mirror_id
                                (String) row[9],//name edificio
                                MySQLQuery.getAsInteger(row[10])//refClient
                        );

                        build.factor = new MySQLQuery("SELECT b.factor FROM bill_build_factor AS b WHERE "
                                + "b.bill_span_id <= " + span.id + " AND b.build_id = " + build.mirrorId + " "
                                + "ORDER BY b.bill_span_id DESC LIMIT 1").getAsBigDecimal(instConn, true);
                        build.factor = (build.factor.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ONE : build.factor);

                        Map<Integer, BigDecimal> prices = BillPriceSpan.getPricesMap(instConn, span.id);

                        Object[][] aptosData = new MySQLQuery("SELECT "
                                + "c.id, "
                                + "TRIM(CONCAT(COALESCE(c.first_name, ''),' ',COALESCE(c.last_name, ''))), "//clientes
                                + "c.num_install, "
                                + "SUM(DISTINCT r.reading - r.last_reading) * " + build.factor + ", "//Consumo por Factor..
                                + "(SELECT bpc.`name` FROM bill_price_list bpc INNER JOIN bill_client_list bcl ON bpc.id = bcl.list_id WHERE bcl.client_id = c.id ORDER BY span_id DESC LIMIT 1) "
                                + "FROM bill_reading AS r "
                                + "INNER JOIN bill_client_tank c ON r.client_tank_id = c.id "
                                + "WHERE r.span_id = " + span.id + " AND c.building_id = " + build.mirrorId + " "
                                + "AND c.active GROUP BY c.id "
                                + "UNION "
                                + "SELECT "
                                + "c.id, "
                                + "CONCAT(c.first_name,' ',c.last_name), "//clientes
                                + "c.num_install, "
                                + "SUM(DISTINCT r.reading - r.last_reading) * " + build.factor + ", "//Consumo por Factor..
                                + "(SELECT bpc.`name` FROM bill_price_list bpc INNER JOIN bill_client_list bcl ON bpc.id = bcl.list_id WHERE bcl.client_id = c.id ORDER BY span_id DESC LIMIT 1) "
                                + "FROM bill_reading_bk AS r "
                                + "INNER JOIN bill_client_tank c ON r.client_tank_id = c.id "
                                + "WHERE r.span_id = " + span.id + " AND c.building_id = " + build.mirrorId + " "
                                + "AND c.active GROUP BY c.id ").getRecords(instConn);

                        for (Object[] aptoRow : aptosData) {
                            int aptoId = MySQLQuery.getAsInteger(aptoRow[0]);
                            Integer listId = new MySQLQuery("SELECT cl1.list_id "
                                    + "FROM bill_client_list cl1 "
                                    + "WHERE cl1.client_id = " + aptoId + " "
                                    + "AND cl1.span_id = (SELECT MAX(span_id) FROM bill_client_list cl WHERE cl.span_id <= " + span.id + " AND cl.client_id = " + aptoId + ")").getAsInteger(instConn);

                            Object[] bill = new MySQLQuery("SELECT b.creation_date, b.bill_num "
                                    + "FROM bill_bill AS b WHERE b.bill_span_id = " + span.id + " "
                                    + "AND b.client_tank_id = " + aptoId + " ORDER BY b.creation_date ASC "
                                    + "LIMIT 0,1").getRecord(instConn);

                            BigDecimal consum = MySQLQuery.getAsBigDecimal(aptoRow[3], true).setScale(3, RoundingMode.HALF_EVEN);
                            if (build.cit == null && build.refClientId != null) {
                                Object[] data = new MySQLQuery("SELECT GROUP_CONCAT(t.serial ORDER BY t.serial SEPARATOR ', '), GROUP_CONCAT(t.capacity ORDER BY t.serial SEPARATOR ', ') FROM est_tank AS t WHERE t.client_id = " + build.refClientId + "").getRecord(gralConn);
                                build.cit = MySQLQuery.getAsString(data[0]);
                                build.cap = MySQLQuery.getAsString(data[1]);
                            }

                            BigDecimal price = span.getConsVal(consum, BigDecimal.ONE, (listId != null && prices.get(listId) != null ? prices.get(listId) : BigDecimal.ZERO));
                            Object[] rowB = new Object[12 + (!instNum ? 3 : 0)];
                            if (!sysCfg.skipMinCons || price.compareTo(span.minConsValue) >= 0) {
                                rowB[0] = build.nie;
                                rowB[1] = aptoRow[4];
                                rowB[2] = build.cit;
                                rowB[3] = build.cap;
                                rowB[4] = build.poblado;
                                rowB[5] = build.dane;
                                rowB[6] = build.categoria;
                                rowB[7] = build.type;
                                if (bill != null) {
                                    rowB[8] = df.format((Date) bill[0]);
                                    rowB[9] = bill[1];
                                } else {
                                    rowB[8] = "";
                                    rowB[9] = "";
                                }
                                rowB[10] = aptoRow[1] + " (" + build.name + ") ";
                                rowB[11] = aptoRow[2];
                                if (!instNum) {
                                    rowB[12] = consum;//m3 * fac
                                    rowB[13] = consum.multiply(span.getM3ToGalKte()).multiply(span.galToKgKte);//kgs
                                    rowB[14] = price;
                                }
                                tData.add(rowB);
                            }
                        }
                    }
                }
            }
        }

        Table tblRem = new Table("Remisiones");
        tblRem.getColumns().add(new Column("Nie", 5, 4));
        tblRem.getColumns().add(new Column("Lista", 20, 0));
        tblRem.getColumns().add(new Column("Cit", 20, 0));
        tblRem.getColumns().add(new Column("Cap. Gls", 20, 0));
        tblRem.getColumns().add(new Column("Poblado", 15, 3));
        tblRem.getColumns().add(new Column("Dane", 15, 3));
        tblRem.getColumns().add(new Column("Categoria", 15, 0));
        tblRem.getColumns().add(new Column("Tipo", 15, 0));
        tblRem.getColumns().add(new Column("Fecha", 15, 3));
        tblRem.getColumns().add(new Column("Factura", 15, 3));
        tblRem.getColumns().add(new Column("Cliente", 55, 0));
        tblRem.getColumns().add(new Column("Instalación", 20, 0));
        if (!instNum) {
            tblRem.getColumns().add(new Column("m3 * Fac", 15, 2));
            tblRem.getColumns().add(new Column("kgs * Fac", 15, 2));
            tblRem.getColumns().add(new Column("Valor", 20, 1));
            tblRem.setSummaryRow(new SummaryRow("Total", 11));
        }
        tblRem.setData(tData);
        if (tblRem.getData().length > 0) {
            rep.getTables().add(tblRem);
        }
    }

    static class Build {

        Integer id;
        Integer nie;
        String cit;
        String cap;
        String poblado;
        Integer dane;
        String categoria;
        String type;
        Integer mirrorId;
        String name;
        Integer refClientId;
        BigDecimal factor;

        public Build(Integer id, Integer nie, String cit, String cap, String poblado, Integer dane, String categoria, String type, Integer mirrorId, String name, Integer refClientId) {
            this.id = id;
            this.nie = nie;
            this.cit = cit;
            this.cap = cap;
            this.poblado = poblado;
            this.dane = dane;
            this.categoria = categoria;
            this.type = type;
            this.mirrorId = mirrorId;
            this.name = name;
            this.refClientId = refClientId;
        }
    }
}
