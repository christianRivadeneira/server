package api.sys.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class SysColorCfg extends BaseModel<SysColorCfg> {
//inicio zona de reemplazo

    public String element;
    public String value;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "element",
            "value"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, element);
        q.setParam(2, value);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        element = MySQLQuery.getAsString(row[0]);
        value = MySQLQuery.getAsString(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "sys_color_cfg";
    }

    public static String getSelFlds(String alias) {
        return new SysColorCfg().getSelFldsForAlias(alias);
    }

    public static List<SysColorCfg> getList(MySQLQuery q, Connection conn) throws Exception {
        return new SysColorCfg().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new SysColorCfg().deleteById(id, conn);
    }

    public static List<SysColorCfg> getAll(Connection conn) throws Exception {
        return new SysColorCfg().getAllList(conn);
    }

//fin zona de reemplazo
    
}