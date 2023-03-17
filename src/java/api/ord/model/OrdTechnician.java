package api.ord.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class OrdTechnician extends BaseModel<OrdTechnician> {
//inicio zona de reemplazo

    public String document;
    public String firstName;
    public String lastName;
    public String phone;
    public boolean active;
    public boolean enterprise;
    public boolean cyl;
    public boolean tank;
    public boolean repair;
    public boolean other;
    public Integer perEmpId;
    public Integer employeeId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "document",
            "first_name",
            "last_name",
            "phone",
            "active",
            "enterprise",
            "cyl",
            "tank",
            "repair",
            "other",
            "per_emp_id",
            "employee_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, document);
        q.setParam(2, firstName);
        q.setParam(3, lastName);
        q.setParam(4, phone);
        q.setParam(5, active);
        q.setParam(6, enterprise);
        q.setParam(7, cyl);
        q.setParam(8, tank);
        q.setParam(9, repair);
        q.setParam(10, other);
        q.setParam(11, perEmpId);
        q.setParam(12, employeeId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        document = MySQLQuery.getAsString(row[0]);
        firstName = MySQLQuery.getAsString(row[1]);
        lastName = MySQLQuery.getAsString(row[2]);
        phone = MySQLQuery.getAsString(row[3]);
        active = MySQLQuery.getAsBoolean(row[4]);
        enterprise = MySQLQuery.getAsBoolean(row[5]);
        cyl = MySQLQuery.getAsBoolean(row[6]);
        tank = MySQLQuery.getAsBoolean(row[7]);
        repair = MySQLQuery.getAsBoolean(row[8]);
        other = MySQLQuery.getAsBoolean(row[9]);
        perEmpId = MySQLQuery.getAsInteger(row[10]);
        employeeId = MySQLQuery.getAsInteger(row[11]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_technician";
    }

    public static String getSelFlds(String alias) {
        return new OrdTechnician().getSelFldsForAlias(alias);
    }

    public static List<OrdTechnician> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdTechnician().getListFromQuery(q, conn);
    }

    public static List<OrdTechnician> getList(Params p, Connection conn) throws Exception {
        return new OrdTechnician().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdTechnician().deleteById(id, conn);
    }

    public static List<OrdTechnician> getAll(Connection conn) throws Exception {
        return new OrdTechnician().getAllList(conn);
    }

//fin zona de reemplazo
    public static String getTecnicianLabel(Connection conn, Integer technicianId) throws Exception {
        String technicianLabel = new MySQLQuery("SELECT CONCAT(t.document, ', ',t.first_name, ' ', t.last_name)"
                + " FROM ord_technician t WHERE t.id = ?1 ").setParam(1, technicianId).getAsString(conn);

        return MySQLQuery.isEmpty(technicianLabel) ? "" : technicianLabel;
    }
;

}
