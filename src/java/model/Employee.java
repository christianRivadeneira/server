package model;

import java.sql.Connection;
import java.util.Date;
import utilities.MySQLQuery;

public class Employee {
//MUY IMPORTANTE!!!!!!
//Si se cambia la clase no olvidar actualizar el método getFromRow, cuidado con el ID
//inicio zona de reemplazo

    public int id;
    public String document;
    public String firstName;
    public String lastName;
    public String mail;
    public String phone;
    public Integer perEmployeeId;
    public Integer storeId;
    public Integer contractorId;
    public Integer technicianId;
    public String login;
    public String password;
    public Date lastPasswordChange;
    public boolean guest;
    public Date begExp;
    public boolean driver;
    public boolean active;
    public Integer jobId;
    public Integer agencyId;
    public String shortName;
    public boolean uniDesktopSession;
    public boolean uniMovilSession;
    public String imeiMovilSession;
    public boolean distributor;
    public boolean evSent;
    public boolean virtual;

    private static final String SEL_FLDS = "`document`, "
            + "`first_name`, "
            + "`last_name`, "
            + "`mail`, "
            + "`phone`, "
            + "`per_employee_id`, "
            + "`store_id`, "
            + "`contractor_id`, "
            + "`technician_id`, "
            + "`login`, "
            + "`password`, "
            + "`last_password_change`, "
            + "`guest`, "
            + "`beg_exp`, "
            + "`driver`, "
            + "`active`, "
            + "`job_id`, "
            + "`agency_id`, "
            + "`short_name`, "
            + "`uni_desktop_session`, "
            + "`uni_movil_session`, "
            + "`imei_movil_session`, "
            + "`distributor`, "
            + "`ev_sent`, "
            + "`virtual`";

    private static final String SET_FLDS = "employee SET "
            + "`document` = ?1, "
            + "`first_name` = ?2, "
            + "`last_name` = ?3, "
            + "`mail` = ?4, "
            + "`phone` = ?5, "
            + "`per_employee_id` = ?6, "
            + "`store_id` = ?7, "
            + "`contractor_id` = ?8, "
            + "`technician_id` = ?9, "
            + "`login` = ?10, "
            + "`password` = ?11, "
            + "`last_password_change` = ?12, "
            + "`guest` = ?13, "
            + "`beg_exp` = ?14, "
            + "`driver` = ?15, "
            + "`active` = ?16, "
            + "`job_id` = ?17, "
            + "`agency_id` = ?18, "
            + "`short_name` = ?19, "
            + "`uni_desktop_session` = ?20, "
            + "`uni_movil_session` = ?21, "
            + "`imei_movil_session` = ?22, "
            + "`distributor` = ?23, "
            + "`ev_sent` = ?24, "
            + "`virtual` = ?25";

    private static void setFields(Employee obj, MySQLQuery q) {
        q.setParam(1, obj.document);
        q.setParam(2, obj.firstName);
        q.setParam(3, obj.lastName);
        q.setParam(4, obj.mail);
        q.setParam(5, obj.phone);
        q.setParam(6, obj.perEmployeeId);
        q.setParam(7, obj.storeId);
        q.setParam(8, obj.contractorId);
        q.setParam(9, obj.technicianId);
        q.setParam(10, obj.login);
        q.setParam(11, obj.password);
        q.setParam(12, obj.lastPasswordChange);
        q.setParam(13, obj.guest);
        q.setParam(14, obj.begExp);
        q.setParam(15, obj.driver);
        q.setParam(16, obj.active);
        q.setParam(17, obj.jobId);
        q.setParam(18, obj.agencyId);
        q.setParam(19, obj.shortName);
        q.setParam(20, obj.uniDesktopSession);
        q.setParam(21, obj.uniMovilSession);
        q.setParam(22, obj.imeiMovilSession);
        q.setParam(23, obj.distributor);
        q.setParam(24, obj.evSent);
        q.setParam(25, obj.virtual);

    }

    public static Employee getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        Employee obj = new Employee();
        obj.document = MySQLQuery.getAsString(row[0]);
        obj.firstName = MySQLQuery.getAsString(row[1]);
        obj.lastName = MySQLQuery.getAsString(row[2]);
        obj.mail = MySQLQuery.getAsString(row[3]);
        obj.phone = MySQLQuery.getAsString(row[4]);
        obj.perEmployeeId = MySQLQuery.getAsInteger(row[5]);
        obj.storeId = MySQLQuery.getAsInteger(row[6]);
        obj.contractorId = MySQLQuery.getAsInteger(row[7]);
        obj.technicianId = MySQLQuery.getAsInteger(row[8]);
        obj.login = MySQLQuery.getAsString(row[9]);
        obj.password = MySQLQuery.getAsString(row[10]);
        obj.lastPasswordChange = MySQLQuery.getAsDate(row[11]);
        obj.guest = MySQLQuery.getAsBoolean(row[12]);
        obj.begExp = MySQLQuery.getAsDate(row[13]);
        obj.driver = MySQLQuery.getAsBoolean(row[14]);
        obj.active = MySQLQuery.getAsBoolean(row[15]);
        obj.jobId = MySQLQuery.getAsInteger(row[16]);
        obj.agencyId = MySQLQuery.getAsInteger(row[17]);
        obj.shortName = MySQLQuery.getAsString(row[18]);
        obj.uniDesktopSession = MySQLQuery.getAsBoolean(row[19]);
        obj.uniMovilSession = MySQLQuery.getAsBoolean(row[20]);
        obj.imeiMovilSession = MySQLQuery.getAsString(row[21]);
        obj.distributor = MySQLQuery.getAsBoolean(row[22]);
        obj.evSent = MySQLQuery.getAsBoolean(row[23]);
        obj.virtual = MySQLQuery.getAsBoolean(row[24]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
//Si se cambia la clase no olvidar actualizar el método rowToEmployee
    private static final String toStrFlds = "CONCAT(first_name, ' ', last_name)";

    public Employee select(int id, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery(getSelectQuery(id));
        return getFromRow(q.getRecord(ep));
    }

    public Employee selectActive(int id, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery(getSelectActiveQuery(id));
        return getFromRow(q.getRecord(ep));
    }

    public static String getSelectQuery(int id) {
        return "SELECT " + SEL_FLDS + ", id FROM employee WHERE id = " + id;
    }

    public static String getSelectActiveQuery(int id) {
        return "SELECT " + SEL_FLDS + ", id FROM employee WHERE active = 1 AND id = " + id;
    }

    public static Employee getByPerEmployee(int perEmployeeId, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM employee WHERE per_employee_id = " + perEmployeeId + " LIMIT 1");
        return getFromRow(q.getRecord(ep));
    }

    public static Employee getByContractor(int contractorId, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM employee WHERE contractor_id = " + contractorId + " LIMIT 1");
        return getFromRow(q.getRecord(ep));
    }

    public static Employee getByStore(int storeId, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM employee WHERE contractor_id = " + storeId + " LIMIT 1");
        return getFromRow(q.getRecord(ep));
    }

    public int insert(Employee obj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + SET_FLDS);
        setFields(obj, q);
        return q.executeInsert(ep);
    }

    public String getInsertQuery(Employee obj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + SET_FLDS);
        setFields(obj, q);
        return q.getQuery();
    }

    public void update(Employee obj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("UPDATE " + SET_FLDS + " WHERE id = " + obj.id);
        setFields(obj, q);
        q.executeUpdate(ep);
        new MySQLQuery("UPDATE employee SET job_id = IF (driver, 2, 1);").executeUpdate(ep);
    }

    public void delete(int id, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("DELETE FROM employee WHERE id = " + id);
        q.executeDelete(ep);
    }
}
