package api.per.model;

import java.sql.Connection;
import utilities.MySQLQuery;

public class PerDoorman {

    //Fuera de la zona de reemplazo
    public String officeName;
    //Fuera de la zona de reemplazo

    public int id;
    public int employeeId;
    public int perOfficeId;

    private static final String SEL_FLDS = "pd.`employee_id`, "
            + "pd.`per_office_id`";

    public PerDoorman selectByEmpId(int empId, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT " + SEL_FLDS + ", o.name, pd.id "
                + "FROM per_doorman pd "
                + "INNER JOIN per_office o ON pd.per_office_id = o.id "
                + "WHERE employee_id = " + empId);
        Object[] row = q.getRecord(ep);
        if (row != null && row.length > 0) {
            PerDoorman obj = new PerDoorman();
            obj.employeeId = MySQLQuery.getAsInteger(row[0]);
            obj.perOfficeId = MySQLQuery.getAsInteger(row[1]);
            obj.officeName = MySQLQuery.getAsString(row[2]);
            obj.id = MySQLQuery.getAsInteger(row[3]);
            return obj;
        }
        return null;
    }

}
