package api.sys.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class SysTutorial extends BaseModel<SysTutorial> {
//inicio zona de reemplazo

    public int modId;
    public Integer appId;
    public String name;
    public String notes;
    public String url;
    public String filename;
    public String filetype;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "mod_id",
            "app_id",
            "name",
            "notes",
            "url",
            "filename",
            "filetype"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, modId);
        q.setParam(2, appId);
        q.setParam(3, name);
        q.setParam(4, notes);
        q.setParam(5, url);
        q.setParam(6, filename);
        q.setParam(7, filetype);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        modId = MySQLQuery.getAsInteger(row[0]);
        appId = MySQLQuery.getAsInteger(row[1]);
        name = MySQLQuery.getAsString(row[2]);
        notes = MySQLQuery.getAsString(row[3]);
        url = MySQLQuery.getAsString(row[4]);
        filename = MySQLQuery.getAsString(row[5]);
        filetype = MySQLQuery.getAsString(row[6]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "sys_tutorial";
    }

    public static String getSelFlds(String alias) {
        return new SysTutorial().getSelFldsForAlias(alias);
    }

    public static List<SysTutorial> getList(MySQLQuery q, Connection conn) throws Exception {
        return new SysTutorial().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new SysTutorial().deleteById(id, conn);
    }

    public static List<SysTutorial> getAll(Connection conn) throws Exception {
        return new SysTutorial().getAllList(conn);
    }

//fin zona de reemplazo
}