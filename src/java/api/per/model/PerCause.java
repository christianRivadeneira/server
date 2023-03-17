package api.per.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class PerCause extends BaseModel<PerCause> {
//inicio zona de reemplazo

    public String name;
    public String type;
    public boolean cie10;
    public Integer porcent;
    public boolean active;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "type",
            "cie10",
            "porcent",
            "active"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, type);
        q.setParam(3, cie10);
        q.setParam(4, porcent);
        q.setParam(5, active);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        type = MySQLQuery.getAsString(row[1]);
        cie10 = MySQLQuery.getAsBoolean(row[2]);
        porcent = MySQLQuery.getAsInteger(row[3]);
        active = MySQLQuery.getAsBoolean(row[4]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "per_cause";
    }

    public static String getSelFlds(String alias) {
        return new PerCause().getSelFldsForAlias(alias);
    }

    public static List<PerCause> getList(MySQLQuery q, Connection conn) throws Exception {
        return new PerCause().getListFromQuery(q, conn);
    }

//fin zona de reemplazo
}