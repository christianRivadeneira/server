package api.sys.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class SysCenterType extends BaseModel<SysCenterType> {
//inicio zona de reemplazo

    public String name;
    public String initials;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "initials"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, initials);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        initials = MySQLQuery.getAsString(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "sys_center_type";
    }

    public static String getSelFlds(String alias) {
        return new SysCenterType().getSelFldsForAlias(alias);
    }

    public static List<SysCenterType> getList(MySQLQuery q, Connection conn) throws Exception {
        return new SysCenterType().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new SysCenterType().deleteById(id, conn);
    }

    public static List<SysCenterType> getAll(Connection conn) throws Exception {
        return new SysCenterType().getAllList(conn);
    }

//fin zona de reemplazo
}