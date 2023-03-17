package api.dane.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class DaneDepartment extends BaseModel<DaneDepartment> {
//inicio zona de reemplazo

    public String code;
    public String name;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "code",
            "name"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, code);
        q.setParam(2, name);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        code = MySQLQuery.getAsString(row[0]);
        name = MySQLQuery.getAsString(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "dane_department";
    }

    public static String getSelFlds(String alias) {
        return new DaneDepartment().getSelFldsForAlias(alias);
    }

    public static List<DaneDepartment> getList(MySQLQuery q, Connection conn) throws Exception {
        return new DaneDepartment().getListFromQuery(q, conn);
    }

    public static List<DaneDepartment> getList(Params p, Connection conn) throws Exception {
        return new DaneDepartment().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new DaneDepartment().deleteById(id, conn);
    }

    public static List<DaneDepartment> getAll(Connection conn) throws Exception {
        return new DaneDepartment().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<DaneDepartment> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}