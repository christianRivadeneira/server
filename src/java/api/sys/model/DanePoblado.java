package api.sys.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.math.BigDecimal;

public class DanePoblado extends BaseModel<DanePoblado> {
//inicio zona de reemplazo

    public String code;
    public String name;
    public int munId;
    public String type;
    public BigDecimal lat;
    public BigDecimal lon;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "code",
            "name",
            "mun_id",
            "type",
            "lat",
            "lon"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, code);
        q.setParam(2, name);
        q.setParam(3, munId);
        q.setParam(4, type);
        q.setParam(5, lat);
        q.setParam(6, lon);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        code = MySQLQuery.getAsString(row[0]);
        name = MySQLQuery.getAsString(row[1]);
        munId = MySQLQuery.getAsInteger(row[2]);
        type = MySQLQuery.getAsString(row[3]);
        lat = MySQLQuery.getAsBigDecimal(row[4], false);
        lon = MySQLQuery.getAsBigDecimal(row[5], false);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "dane_poblado";
    }

    public static String getSelFlds(String alias) {
        return new DanePoblado().getSelFldsForAlias(alias);
    }

    public static List<DanePoblado> getList(MySQLQuery q, Connection conn) throws Exception {
        return new DanePoblado().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new DanePoblado().deleteById(id, conn);
    }

    public static List<DanePoblado> getAll(Connection conn) throws Exception {
        return new DanePoblado().getAllList(conn);
    }

//fin zona de reemplazo
}