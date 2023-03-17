package api.emas.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class EmasCom extends BaseModel<EmasCom> {
//inicio zona de reemplazo

    public int empId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "emp_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, empId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        empId = MySQLQuery.getAsInteger(row[0]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "emas_com";
    }

    public static String getSelFlds(String alias) {
        return new EmasCom().getSelFldsForAlias(alias);
    }

    public static List<EmasCom> getList(MySQLQuery q, Connection conn) throws Exception {
        return new EmasCom().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new EmasCom().deleteById(id, conn);
    }

    public static List<EmasCom> getAll(Connection conn) throws Exception {
        return new EmasCom().getAllList(conn);
    }

//fin zona de reemplazo
}