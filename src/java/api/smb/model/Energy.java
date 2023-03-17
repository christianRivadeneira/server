package api.smb.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class Energy extends BaseModel<Energy> {
//inicio zona de reemplazo

    public String name;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "energy";
    }

    public static String getSelFlds(String alias) {
        return new Energy().getSelFldsForAlias(alias);
    }

    public static List<Energy> getList(MySQLQuery q, Connection conn) throws Exception {
        return new Energy().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new Energy().deleteById(id, conn);
    }

    public static List<Energy> getAll(Connection conn) throws Exception {
        return new Energy().getAllList(conn);
    }

//fin zona de reemplazo
}