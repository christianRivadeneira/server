package api.sys.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class SysAppProfileEmp extends BaseModel<SysAppProfileEmp> {
//inicio zona de reemplazo

    public int empId;
    public int appProfileId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "emp_id",
            "app_profile_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, empId);
        q.setParam(2, appProfileId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        empId = MySQLQuery.getAsInteger(row[0]);
        appProfileId = MySQLQuery.getAsInteger(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "sys_app_profile_emp";
    }

    public static String getSelFlds(String alias) {
        return new SysAppProfileEmp().getSelFldsForAlias(alias);
    }

    public static List<SysAppProfileEmp> getList(MySQLQuery q, Connection conn) throws Exception {
        return new SysAppProfileEmp().getListFromQuery(q, conn);
    }

    public static List<SysAppProfileEmp> getList(Params p, Connection conn) throws Exception {
        return new SysAppProfileEmp().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new SysAppProfileEmp().deleteById(id, conn);
    }

    public static List<SysAppProfileEmp> getAll(Connection conn) throws Exception {
        return new SysAppProfileEmp().getAllList(conn);
    }

//fin zona de reemplazo
    
    public static boolean hasProfileByEmp(int empId, int profileId, Connection conn) throws Exception {
        Params par = new Params();
        par.param("emp_id", empId);
        par.param("app_profile_id", profileId);
        SysAppProfileEmp obj = new SysAppProfileEmp().select(par, conn);
        return obj != null;
    }
    
}
