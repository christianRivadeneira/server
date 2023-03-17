package api.per.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class PerChild extends BaseModel<PerChild> {
//inicio zona de reemplazo

    public String firstName;
    public String lastName;
    public String gender;
    public Date birth;
    public int employeeId;
    public String notes;
    public String scLevel;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "first_name",
            "last_name",
            "gender",
            "birth",
            "employee_id",
            "notes",
            "sc_level"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, firstName);
        q.setParam(2, lastName);
        q.setParam(3, gender);
        q.setParam(4, birth);
        q.setParam(5, employeeId);
        q.setParam(6, notes);
        q.setParam(7, scLevel);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        firstName = MySQLQuery.getAsString(row[0]);
        lastName = MySQLQuery.getAsString(row[1]);
        gender = MySQLQuery.getAsString(row[2]);
        birth = MySQLQuery.getAsDate(row[3]);
        employeeId = MySQLQuery.getAsInteger(row[4]);
        notes = MySQLQuery.getAsString(row[5]);
        scLevel = MySQLQuery.getAsString(row[6]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "per_child";
    }

    public static String getSelFlds(String alias) {
        return new PerChild().getSelFldsForAlias(alias);
    }

    public static List<PerChild> getList(MySQLQuery q, Connection conn) throws Exception {
        return new PerChild().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new PerChild().deleteById(id, conn);
    }

    public static List<PerChild> getAll(Connection conn) throws Exception {
        return new PerChild().getAllList(conn);
    }

//fin zona de reemplazo

}
