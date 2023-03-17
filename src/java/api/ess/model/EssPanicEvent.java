package api.ess.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class EssPanicEvent extends BaseModel<EssPanicEvent> {
//inicio zona de reemplazo

    public int empId;
    public Date regDt;
    public Integer checkedById;
    public Date checkDt;
    public String notes;
    public Integer closeById;
    public Date closeDt;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "emp_id",
            "reg_dt",
            "checked_by_id",
            "check_dt",
            "notes",
            "close_by_id",
            "close_dt"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, empId);
        q.setParam(2, regDt);
        q.setParam(3, checkedById);
        q.setParam(4, checkDt);
        q.setParam(5, notes);
        q.setParam(6, closeById);
        q.setParam(7, closeDt);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        empId = MySQLQuery.getAsInteger(row[0]);
        regDt = MySQLQuery.getAsDate(row[1]);
        checkedById = MySQLQuery.getAsInteger(row[2]);
        checkDt = MySQLQuery.getAsDate(row[3]);
        notes = MySQLQuery.getAsString(row[4]);
        closeById = MySQLQuery.getAsInteger(row[5]);
        closeDt = MySQLQuery.getAsDate(row[6]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ess_panic_event";
    }

    public static String getSelFlds(String alias) {
        return new EssPanicEvent().getSelFldsForAlias(alias);
    }

    public static List<EssPanicEvent> getList(MySQLQuery q, Connection conn) throws Exception {
        return new EssPanicEvent().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new EssPanicEvent().deleteById(id, conn);
    }

    public static List<EssPanicEvent> getAll(Connection conn) throws Exception {
        return new EssPanicEvent().getAllList(conn);
    }

//fin zona de reemplazo
}
