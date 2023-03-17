package api.prov.dto;

import java.math.BigDecimal;
import utilities.MySQLQuery;

public class ProvItem {

    public String name;
    public String notes;
    public String provU;
    public Integer amount;
    public BigDecimal vltotal;
    public BigDecimal vlunit;
    public BigDecimal base;
    public BigDecimal net;
    public BigDecimal dto;
    public BigDecimal iva;
    public int ivaRate;
    public int dtoRate;
    public BigDecimal reteFue;
    public BigDecimal reteIva;
    public BigDecimal reteIca;

    public static ProvItem[] getFromData(Object[][] data) {
        ProvItem[] rta = new ProvItem[data.length];
        for (int i = 0; i < data.length; i++) {
            rta[i] = getFromRow(data[i]);
        }
        return rta;
    }

    public static ProvItem getFromRow(Object[] row) {
        ProvItem item = new ProvItem();
        item.name = MySQLQuery.getAsString(row[0]);
        item.notes = MySQLQuery.getAsString(row[1]);
        item.provU = MySQLQuery.getAsString(row[2]);
        item.amount = MySQLQuery.getAsInteger(row[3]);
        item.vltotal = MySQLQuery.getAsBigDecimal(row[4], true);
        item.vlunit = MySQLQuery.getAsBigDecimal(row[5], true);
        item.base = MySQLQuery.getAsBigDecimal(row[6], true);
        item.net = MySQLQuery.getAsBigDecimal(row[7], true);
        item.dto = MySQLQuery.getAsBigDecimal(row[8], true);
        item.iva = MySQLQuery.getAsBigDecimal(row[9], true);
        item.ivaRate = MySQLQuery.getAsInteger(row[10] != null ? row[10] : 0);
        item.dtoRate = MySQLQuery.getAsInteger(row[11] != null ? row[11] : 0);
        return item;
    }

    public static String getQuery(int reqId) {
        return "SELECT "
                + "it.`name`, "//0
                + "it.`notes`, "//1
                + "u.short_name, "//2
                + "it.`amount`, "//3
                + "@fac:=ROUND(it.`value`), "//valor facturado 4 
                + "@fac / it.amount, "//valor unitario incluye iva 5 
                + "@base:=ROUND(@fac /(1 + (it.`iva` / 100))), "//base facturado ya sin iva 6 
                + "@net:=ROUND(@base /(1 - (it.dto / 100))), "//bruto valor total sin descuentos ni iva, formula cambiada 7  
                + "@net - @base, "//dto formula cambiada 8  
                + "@fac - @base, "//iva 9 
                + "it.iva, " //iva 10
                + "it.dto " //dto 11
                + "FROM prov_item AS it "
                + "INNER JOIN prov_m_unit u ON u.id = it.m_unit_id "
                + "WHERE it.request_id = " + reqId;
    }
}
