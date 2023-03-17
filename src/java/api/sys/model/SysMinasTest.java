package api.sys.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class SysMinasTest extends BaseModel<SysMinasTest> {
//inicio zona de reemplazo

    public Date dt;
    public Integer t;
    public String src;
    public String ex;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "dt",
            "t",
            "src",
            "ex"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, dt);
        q.setParam(2, t);
        q.setParam(3, src);
        q.setParam(4, ex);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        dt = MySQLQuery.getAsDate(row[0]);
        t = MySQLQuery.getAsInteger(row[1]);
        src = MySQLQuery.getAsString(row[2]);
        ex = MySQLQuery.getAsString(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "sys_minas_test";
    }

    public static String getSelFlds(String alias) {
        return new SysMinasTest().getSelFldsForAlias(alias);
    }

    public static List<SysMinasTest> getList(MySQLQuery q, Connection conn) throws Exception {
        return new SysMinasTest().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new SysMinasTest().deleteById(id, conn);
    }

    public static List<SysMinasTest> getAll(Connection conn) throws Exception {
        return new SysMinasTest().getAllList(conn);
    }

//fin zona de reemplazo
   
}
