package api.hlp.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class HlpSupEmp extends BaseModel<HlpSupEmp> {
//inicio zona de reemplazo

    public Integer empId;
    public Boolean active;
    public int typeId;
    public boolean createCase;
    public Integer employeeId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "emp_id",
            "active",
            "type_id",
            "create_case",
            "employee_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, empId);
        q.setParam(2, active);
        q.setParam(3, typeId);
        q.setParam(4, createCase);
        q.setParam(5, employeeId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        empId = MySQLQuery.getAsInteger(row[0]);
        active = MySQLQuery.getAsBoolean(row[1]);
        typeId = MySQLQuery.getAsInteger(row[2]);
        createCase = MySQLQuery.getAsBoolean(row[3]);
        employeeId = MySQLQuery.getAsInteger(row[4]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "hlp_sup_emp";
    }

    public static String getSelFlds(String alias) {
        return new HlpSupEmp().getSelFldsForAlias(alias);
    }

    public static List<HlpSupEmp> getList(MySQLQuery q, Connection conn) throws Exception {
        return new HlpSupEmp().getListFromQuery(q, conn);
    }

    public static List<HlpSupEmp> getList(Params p, Connection conn) throws Exception {
        return new HlpSupEmp().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new HlpSupEmp().deleteById(id, conn);
    }

    public static List<HlpSupEmp> getAll(Connection conn) throws Exception {
        return new HlpSupEmp().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<HlpSupEmp> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}