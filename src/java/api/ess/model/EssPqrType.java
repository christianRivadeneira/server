package api.ess.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class EssPqrType extends BaseModel<EssPqrType> {
//inicio zona de reemplazo

    public String name;
    public boolean active;
    public int days;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "active",
            "days"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, active);
        q.setParam(3, days);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        active = MySQLQuery.getAsBoolean(row[1]);
        days = MySQLQuery.getAsInteger(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ess_pqr_type";
    }

    public static String getSelFlds(String alias) {
        return new EssPqrType().getSelFldsForAlias(alias);
    }

    public static List<EssPqrType> getList(MySQLQuery q, Connection conn) throws Exception {
        return new EssPqrType().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new EssPqrType().deleteById(id, conn);
    }

    public static List<EssPqrType> getAll(Connection conn) throws Exception {
        return new EssPqrType().getAllList(conn);
    }

//fin zona de reemplazo
}