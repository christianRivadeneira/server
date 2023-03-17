package api.inv.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class InvProcArea extends BaseModel<InvProcArea> {
//inicio zona de reemplazo

    public String name;
    public String description;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "description"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, description);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        description = MySQLQuery.getAsString(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "inv_proc_area";
    }

    public static String getSelFlds(String alias) {
        return new InvProcArea().getSelFldsForAlias(alias);
    }

    public static List<InvProcArea> getList(MySQLQuery q, Connection conn) throws Exception {
        return new InvProcArea().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new InvProcArea().deleteById(id, conn);
    }

    public static List<InvProcArea> getAll(Connection conn) throws Exception {
        return new InvProcArea().getAllList(conn);
    }

//fin zona de reemplazo
}