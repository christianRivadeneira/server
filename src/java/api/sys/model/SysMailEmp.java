package api.sys.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class SysMailEmp extends BaseModel<SysMailEmp> {
//inicio zona de reemplazo

    public int processId;
    public int empId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "process_id",
            "emp_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, processId);
        q.setParam(2, empId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        processId = MySQLQuery.getAsInteger(row[0]);
        empId = MySQLQuery.getAsInteger(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "sys_mail_emp";
    }

    public static String getSelFlds(String alias) {
        return new SysMailEmp().getSelFldsForAlias(alias);
    }

    public static List<SysMailEmp> getList(MySQLQuery q, Connection conn) throws Exception {
        return new SysMailEmp().getListFromQuery(q, conn);
    }

    public static List<SysMailEmp> getList(Params p, Connection conn) throws Exception {
        return new SysMailEmp().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new SysMailEmp().deleteById(id, conn);
    }

    public static List<SysMailEmp> getAll(Connection conn) throws Exception {
        return new SysMailEmp().getAllList(conn);
    }

//fin zona de reemplazo
}
