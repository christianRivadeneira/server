package api.bill.model;

import api.BaseModel;
import api.Params;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class BillStation extends BaseModel<BillStation> {
//inicio zona de reemplazo

    public int instId;
    public BigDecimal lat;
    public BigDecimal lon;
    public BigDecimal alt;
    public String type;
    public String code;
    public Date begDate;
    public Integer capacity;
    public String codCert;
    public Integer inspectorId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "inst_id",
            "lat",
            "lon",
            "alt",
            "type",
            "code",
            "beg_date",
            "capacity",
            "cod_cert",
            "inspector_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, instId);
        q.setParam(2, lat);
        q.setParam(3, lon);
        q.setParam(4, alt);
        q.setParam(5, type);
        q.setParam(6, code);
        q.setParam(7, begDate);
        q.setParam(8, capacity);
        q.setParam(9, codCert);
        q.setParam(10, inspectorId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        instId = MySQLQuery.getAsInteger(row[0]);
        lat = MySQLQuery.getAsBigDecimal(row[1], false);
        lon = MySQLQuery.getAsBigDecimal(row[2], false);
        alt = MySQLQuery.getAsBigDecimal(row[3], false);
        type = MySQLQuery.getAsString(row[4]);
        code = MySQLQuery.getAsString(row[5]);
        begDate = MySQLQuery.getAsDate(row[6]);
        capacity = MySQLQuery.getAsInteger(row[7]);
        codCert = MySQLQuery.getAsString(row[8]);
        inspectorId = MySQLQuery.getAsInteger(row[9]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_station";
    }

    public static String getSelFlds(String alias) {
        return new BillStation().getSelFldsForAlias(alias);
    }

    public static List<BillStation> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillStation().getListFromQuery(q, conn);
    }

    public static List<BillStation> getList(Params p, Connection conn) throws Exception {
        return new BillStation().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillStation().deleteById(id, conn);
    }

    public static List<BillStation> getAll(Connection conn) throws Exception {
        return new BillStation().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<BillStation> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}
