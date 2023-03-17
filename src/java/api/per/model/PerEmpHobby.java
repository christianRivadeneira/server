package api.per.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class PerEmpHobby extends BaseModel<PerEmpHobby> {
//inicio zona de reemplazo

    public int empId;
    public int hobbyId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "emp_id",
            "hobby_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, empId);
        q.setParam(2, hobbyId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        empId = MySQLQuery.getAsInteger(row[0]);
        hobbyId = MySQLQuery.getAsInteger(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "per_emp_hobby";
    }

    public static String getSelFlds(String alias) {
        return new PerEmpHobby().getSelFldsForAlias(alias);
    }

    public static List<PerEmpHobby> getList(MySQLQuery q, Connection conn) throws Exception {
        return new PerEmpHobby().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new PerEmpHobby().deleteById(id, conn);
    }

    public static List<PerEmpHobby> getAll(Connection conn) throws Exception {
        return new PerEmpHobby().getAllList(conn);
    }

//fin zona de reemplazo
}