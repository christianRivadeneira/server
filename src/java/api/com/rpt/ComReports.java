package api.com.rpt;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.HeaderColumn;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.SummaryRow;
import utilities.mysqlReport.Table;
import utilities.mysqlReport.TableHeader;

public class ComReports {

    public static List<MySQLReport> getPriceListBiable(Connection conn) throws Exception {
        Object[][] cylRefs = new MySQLQuery("SELECT name FROM cylinder_type ORDER BY name").getRecords(conn);
        Object[][] tercSucData = new MySQLQuery("SELECT "
                + "terc_code, "
                + "sucursal "
                + "FROM "
                + "bbl_report "
                + "GROUP BY terc_code, sucursal").getRecords(conn);
        Object[][] inactiveData = new MySQLQuery("SELECT i.document FROM inv_store i WHERE !i.active "
                + "UNION "
                + "SELECT o.document FROM ord_contract_index o WHERE !o.active").getRecords(conn);
        List<PriceList> lstPrices = new ArrayList<>();
        for (int i = 0; i < tercSucData.length; i++) {
            PriceList pl = new PriceList();
            pl.tercCode = MySQLQuery.getAsString(tercSucData[i][0]);
            pl.tercSuc = MySQLQuery.getAsString(tercSucData[i][1]);
            for (int j = 0; j < inactiveData.length; j++) {
                if (pl.tercCode.equals(MySQLQuery.getAsString(inactiveData[j]))) {
                    pl.active = "No";
                    break;
                }
            }
            lstPrices.add(pl);
        }

        for (int i = 0; i < lstPrices.size(); i++) {
            PriceList get = lstPrices.get(i);
            Object[][] data = new MySQLQuery("SELECT "
                    + "r.cyl_ref, "//0
                    + "r.price_lp, "//1
                    + "r.price_ld, "//2
                    + "idlipre, "//3
                    + "idlidesc "//4
                    + "FROM bbl_report r "
                    + "WHERE terc_code = '" + get.tercCode + "' "
                    + "AND r.sucursal = '" + get.tercSuc + "' "
                    + "ORDER BY cyl_ref").getRecords(conn);

            for (int k = 0; k < cylRefs.length; k++) {
                String cylRef = MySQLQuery.getAsString(cylRefs[k][0]);
                for (int l = 0; l < data.length; l++) {
                    Object[] obj = data[l];
                    if (get.lpName == null) {
                        get.lpName = MySQLQuery.getAsString(obj[3]);
                    }
                    if (get.ldName == null && obj[4] != null) {
                        get.ldName = MySQLQuery.getAsString(obj[4]);
                    }

                    if (cylRef.equals(MySQLQuery.getAsString(obj[0]))) {
                        CylRef ref = new CylRef();
                        ref.ref = cylRef;
                        ref.price = MySQLQuery.getAsInteger(obj[1]);
                        ref.discount = MySQLQuery.getAsInteger(obj[2]);
                        get.refs.add(ref);
                        break;
                    }
                }
            }
        }

        Object[][] data = new Object[lstPrices.size()][5 + (cylRefs.length * 3)];
        for (int i = 0; i < lstPrices.size(); i++) {
            PriceList terc = lstPrices.get(i);
            data[i][0] = terc.tercCode;
            data[i][1] = terc.tercSuc;
            data[i][2] = terc.lpName;
            data[i][3] = terc.ldName;
            data[i][4] = terc.active;

            //Forma la data de los precios
            for (int j = 0; j < cylRefs.length; j++) {
                Object[] cylRef = cylRefs[j];
                data[i][5 + j] = 0;
                for (int k = 0; k < terc.refs.size(); k++) {
                    CylRef ref = terc.refs.get(k);
                    if (MySQLQuery.getAsString(cylRef[0]).equals(ref.ref)) {
                        data[i][5 + j] = ref.price;
                        break;
                    }
                }
            }

            //Forma la data de los descuentos
            for (int j = 0; j < cylRefs.length; j++) {
                Object[] cylRef = cylRefs[j];
                data[i][5 + cylRefs.length + j] = 0;
                for (int k = 0; k < terc.refs.size(); k++) {
                    CylRef ref = terc.refs.get(k);
                    if (MySQLQuery.getAsString(cylRef[0]).equals(ref.ref)) {
                        data[i][5 + cylRefs.length + j] = (ref.discount != null ? ref.discount : 0);
                        break;
                    }
                }
            }

            //Forma la data de la diferencia
            for (int j = 0; j < cylRefs.length; j++) {
                Object[] cylRef = cylRefs[j];
                data[i][5 + (cylRefs.length * 2) + j] = 0;
                for (int k = 0; k < terc.refs.size(); k++) {
                    CylRef ref = terc.refs.get(k);
                    if (MySQLQuery.getAsString(cylRef[0]).equals(ref.ref)) {
                        data[i][5 + (cylRefs.length * 2) + j] = ref.price - (ref.discount != null ? ref.discount : 0);
                        break;
                    }
                }
            }

        }

        TableHeader header = new TableHeader();
        header.getColums().add(new HeaderColumn("Tercero", 1, 2));
        header.getColums().add(new HeaderColumn("Sucursal", 1, 2));
        header.getColums().add(new HeaderColumn("L. Precios", 1, 2));
        header.getColums().add(new HeaderColumn("L. Descuento", 1, 2));
        header.getColums().add(new HeaderColumn("Activo", 1, 2));

        for (int i = 0; i < cylRefs.length; i++) { // precios 
            header.getColums().add(new HeaderColumn(MySQLQuery.getAsString(cylRefs[i][0]), 1, 1));
        }
        for (int i = 0; i < cylRefs.length; i++) { // dto 
            header.getColums().add(new HeaderColumn(MySQLQuery.getAsString(cylRefs[i][0]), 1, 1));
        }
        for (int i = 0; i < cylRefs.length; i++) { // final 
            header.getColums().add(new HeaderColumn(MySQLQuery.getAsString(cylRefs[i][0]), 1, 1));
        }

        Double sheets = Math.ceil(new Double(data != null ? data.length : 0) / 60000d); // el numero de hojas 
        System.out.println("wwwwwwwwwwwwwww"+sheets);
        List<MySQLReport> arrayRpt = new ArrayList<>();// lo utlizo para varias hojas 
        Integer maxRows = sheets == 1 ? (data != null ? data.length : 0) : 60000;
        for (int cs = 0; cs < sheets; cs++) { //count sheets cs 

            MySQLReport rep = new MySQLReport("Reporte Listado de Precios", "", "lst_precios"+(cs+1), MySQLQuery.now(conn));
            rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
            rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));//1

            rep.getFormats().get(0).setWrap(true);

            rep.setShowNumbers(true);
            rep.setZoomFactor(85);
            rep.setVerticalFreeze(6);

            Table tb = new Table("Listado de Precios");
            tb.getHeaders().add(header);
            tb.getColumns().add(new Column("Tercero", 15, 0));
            tb.getColumns().add(new Column("Sucursal", 15, 0));
            tb.getColumns().add(new Column("L. Precios", 15, 0));
            tb.getColumns().add(new Column("L. Descuento", 15, 0));
            tb.getColumns().add(new Column("Activo", 10, 0));

            for (int i = 0; i < cylRefs.length; i++) { // Precio 
                tb.getColumns().add(new Column("Precio", 12, 1));
            }
            for (int i = 0; i < cylRefs.length; i++) { // Dto 
                tb.getColumns().add(new Column("Dto", 12, 1));
            }
            for (int i = 0; i < cylRefs.length; i++) { // Total 
                tb.getColumns().add(new Column("Final", 12, 1));
            }
            List<Object[]> lstRows = new ArrayList<>();
            int start = maxRows * cs;
            for (int iData = start, i = 0; iData < data.length && i < maxRows; iData++, i++) {
                lstRows.add(data[iData]);
            }
            Object[][] data1 = new Object[lstRows.size()][];
            for (int i = 0; i < lstRows.size(); i++) {
                data1[i] = lstRows.get(i);
            }

            tb.setData(data1);
            if (tb.getData().length > 0) {
                rep.getTables().add(tb);
                arrayRpt.add(rep);
            }
        }
        return arrayRpt;
    }

    private static class PriceList {

        public String tercCode;
        public String tercSuc;
        public String lpName;
        public String ldName;
        public String active = "Si";
        public List<CylRef> refs = new ArrayList<>();
    }

    private static class CylRef {

        public String ref;
        public Integer price;
        public Integer discount;
    }

    public static MySQLReport rptCylOrderStoreVsDeliv(Integer storeId, Date begDt, Date endDt, Connection conn) throws Exception {

        Object[][] dataOrders = new MySQLQuery("SELECT "
                + " o.id, "
                + " o.pv_sale_id, "
                + " ic.name, "//2
                + " ci.name, "//3 
                + " s.internal, "
                + " CONCAT(s.first_name, IFNULL(s.last_name,'')), "
                + " o.taken_dt, o.confirm_dt, "
                + " CONCAT(v.plate, IFNULL(CONCAT(' - ',v.internal),'')), "
                + " CONCAT(e.first_name, IFNULL(e.last_name,'')) "
                + " FROM com_store_order o "
                + " INNER JOIN inv_store s ON s.id = o.store_id  "
                + "LEFT JOIN city ci ON ci.id = city_id "
                + " INNER JOIN inv_center ic ON ic.id = s.center_id "
                + " INNER JOIN employee e ON e.id = o.taken_by_id "
                + " INNER JOIN vehicle v ON v.id = o.vh_id "
                + " WHERE "
                + " o.taken_dt BETWEEN ?1 AND ?2 "
                + (storeId != null ? " AND s.id = " + storeId : " ")
        ).setParam(1, begDt).setParam(2, endDt).getRecords(conn);
        Object[][] cyls = new MySQLQuery("SELECT id, name FROM cylinder_type ORDER BY place ").getRecords(conn);

        List<Column> cols = new ArrayList<>();

        cols.add(new Column("Centro", 20, 0));
        cols.add(new Column("Poblado", 20, 0));
        cols.add(new Column("Interno", 20, 0));
        cols.add(new Column("Almacén", 35, 0));
        cols.add(new Column("Pedido", 30, 1));
        cols.add(new Column("Atendido", 30, 1));
        cols.add(new Column("Vehículo", 20, 0));
        cols.add(new Column("Capturado por", 35, 0));

        int sizeData = cols.size();
        int sizeTotal = sizeData + (cyls.length * 2);

        Object[][] data = new Object[dataOrders.length][sizeTotal];

        for (int i = 0; i < dataOrders.length; i++) {
            Object[] ord = dataOrders[i];

            Object[][] cylsOrd = new MySQLQuery(" SELECT o.cyl_type_id, o.amount "
                    + " FROM com_store_order_inv o "
                    + " WHERE o.order_id = " + ord[0] + " "
                    + " ORDER BY o.cyl_type_id "
            ).getRecords(conn);

            Object[][] cylsDel = new MySQLQuery(" SELECT c.cyl_type_id, COUNT(*) "
                    + " FROM trk_pv_sale s  "
                    + " INNER JOIN trk_pv_cyls pc ON pc.pv_sale_id = s.id AND pc.`type` = 'del' "
                    + " INNER JOIN trk_cyl c ON c.id = pc.cyl_id "
                    + " WHERE s.id = " + ord[1] + " "
                    + " GROUP BY c.cyl_type_id   ORDER BY c.cyl_type_id "
            ).getRecords(conn);

            data[i][0] = ord[2];
            data[i][1] = ord[3];
            data[i][2] = ord[4];
            data[i][3] = ord[5];
            data[i][4] = MySQLQuery.getAsDate(ord[6]);
            data[i][5] = MySQLQuery.getAsDate(ord[7]);
            data[i][6] = ord[8];
            data[i][7] = ord[9];

            int[] completeCyls = completeCyls(cylsOrd, cyls);
            int cylIndex = 8;
            for (int j = 0; j < cyls.length; j++) {
                data[i][cylIndex] = completeCyls[j];
                cylIndex++;
            }
            completeCyls = completeCyls(cylsDel, cyls);
            for (int j = 0; j < cyls.length; j++) {
                data[i][cylIndex] = completeCyls[j];
                cylIndex++;
            }
        }

        int colum = sizeData - 1;
        int sizeCyls = cyls.length * 2;
        List<Integer> lstDelete = new ArrayList<>();
        for (int x = 0; x < sizeCyls; x++) {
            colum++;
            boolean isZero = true;
            for (int i = 0; i < data.length; i++) {
                Object[] dataRow = data[i];
                if (MySQLQuery.getAsInteger(dataRow[colum]) > 0) {
                    isZero = false;
                    break;
                }
            }
            if (isZero) {
                lstDelete.add(colum);
            }
        }

        if (lstDelete.size() > 0 && data.length > 0) {
            data = deleteColums(lstDelete, data);
        }

        int orders = 0;
        for (int i = 0; i < cyls.length; i++) {
            if (!checkDeletes(lstDelete, (sizeData + i))) {
                cols.add(new Column(MySQLQuery.getAsString(cyls[i][1]), 8, 2));
                orders++;
            }
        }

        int deliv = 0;
        for (int i = 0; i < cyls.length; i++) {
            if (!checkDeletes(lstDelete, (sizeData + cyls.length + i))) {
                cols.add(new Column(MySQLQuery.getAsString(cyls[i][1]), 8, 2));
                deliv++;
            }
        }

        MySQLReport rep = new MySQLReport("Pedidos de Cilindros vs Entregados", "", "rpt_pedido_cilindros", MySQLQuery.now(conn));
        rep.setShowNumbers(true);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.LEFT, "dd/MM/yyyy hh:mm a"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//2

        rep.setZoomFactor(80);
        rep.setVerticalFreeze(6);
        rep.setHorizontalFreeze(8);
        rep.setMultiRowTitles(true);

        Table tb = new Table("Pedidos de cilindros vs Entregados");
        tb.setColumns(cols);
        tb.setSummaryRow(new SummaryRow("Totales", sizeData));
        TableHeader header = new TableHeader();
        tb.getHeaders().add(header);
        header.getColums().add(new HeaderColumn("Centro", 1, 2));
        header.getColums().add(new HeaderColumn("Poblado", 1, 2));
        header.getColums().add(new HeaderColumn("Interno", 1, 2));
        header.getColums().add(new HeaderColumn("Almacén", 1, 2));
        header.getColums().add(new HeaderColumn("Pedido", 1, 2));
        header.getColums().add(new HeaderColumn("Atendido", 1, 2));
        header.getColums().add(new HeaderColumn("Vehículo", 1, 2));
        header.getColums().add(new HeaderColumn("Capturado por", 1, 2));
        header.getColums().add(new HeaderColumn("Pedido", orders, 1));
        header.getColums().add(new HeaderColumn("Entrega", deliv, 1));

        tb.setData(data);
        if (tb.getData().length > 0) {
            rep.getTables().add(tb);
        }

        return rep;
    }

    private static Object[][] deleteColums(List<Integer> lstDeletes, Object[][] info) {
        Object[][] newMatriz = new Object[info.length][info[0].length - lstDeletes.size()];

        for (int i = 0; i < info.length; i++) {
            int newColum = 0;
            for (int j = 0; j < info[0].length; j++) {
                if (!checkDeletes(lstDeletes, j)) {
                    newMatriz[i][newColum] = info[i][j];
                    newColum++;
                }
            }
        }

        return newMatriz;
    }

    private static boolean checkDeletes(List<Integer> lstDeletes, int j) {
        return lstDeletes.contains(j);
    }

    private static int[] completeCyls(Object[][] cylData, Object[][] cylTypes) {
        int[] fullRow = new int[cylTypes.length];
        for (int i = 0; i < fullRow.length; i++) {
            fullRow[i] = 0;
        }
        for (int i = 0; i < cylTypes.length; i++) {
            Integer typeId = MySQLQuery.getAsInteger(cylTypes[i][0]);
            for (Object[] obj : cylData) {
                if (typeId.equals(obj[0])) {
                    fullRow[i] = MySQLQuery.getAsInteger(obj[1]);
                }
            }
        }
        return fullRow;
    }

}
