package api.emas.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class EmasLog extends BaseModel<EmasLog> {
//inicio zona de reemplazo

    public Integer ownerId;
    public int ownerType;
    public int employeeId;
    public Date logDate;
    public String notes;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "owner_id",
            "owner_type",
            "employee_id",
            "log_date",
            "notes"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, ownerId);
        q.setParam(2, ownerType);
        q.setParam(3, employeeId);
        q.setParam(4, logDate);
        q.setParam(5, notes);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        ownerId = MySQLQuery.getAsInteger(row[0]);
        ownerType = MySQLQuery.getAsInteger(row[1]);
        employeeId = MySQLQuery.getAsInteger(row[2]);
        logDate = MySQLQuery.getAsDate(row[3]);
        notes = MySQLQuery.getAsString(row[4]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "emas_log";
    }

    public static String getSelFlds(String alias) {
        return new EmasLog().getSelFldsForAlias(alias);
    }

    public static List<EmasLog> getList(MySQLQuery q, Connection conn) throws Exception {
        return new EmasLog().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new EmasLog().deleteById(id, conn);
    }

    public static List<EmasLog> getAll(Connection conn) throws Exception {
        return new EmasLog().getAllList(conn);
    }

//fin zona de reemplazo
}