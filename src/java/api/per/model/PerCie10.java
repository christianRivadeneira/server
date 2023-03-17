package api.per.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class PerCie10 extends BaseModel<PerCie10> {
//inicio zona de reemplazo

    public String cod;
    public String description;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "cod",
            "description"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, cod);
        q.setParam(2, description);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        cod = MySQLQuery.getAsString(row[0]);
        description = MySQLQuery.getAsString(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "per_cie10";
    }

    public static String getSelFlds(String alias) {
        return new PerCie10().getSelFldsForAlias(alias);
    }

    public static List<PerCie10> getList(MySQLQuery q, Connection conn) throws Exception {
        return new PerCie10().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new PerCie10().deleteById(id, conn);
    }

    public static List<PerCie10> getAll(Connection conn) throws Exception {
        return new PerCie10().getAllList(conn);
    }

//fin zona de reemplazo

}
