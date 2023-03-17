package api.sys.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class Employee extends BaseModel<Employee> {

    public static final String[] HIDDEN = new String[]{"password", "login"};

//inicio zona de reemplazo

    public String document;
    public String firstName;
    public String lastName;
    public String mail;
    public String phone;
    public Integer perEmployeeId;
    public Integer perCandidateId;
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
    public Integer lastProfile;
    public boolean tripsManager;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "document",
            "first_name",
            "last_name",
            "mail",
            "phone",
            "per_employee_id",
            "per_candidate_id",
            "store_id",
            "contractor_id",
            "technician_id",
            "login",
            "password",
            "last_password_change",
            "guest",
            "beg_exp",
            "driver",
            "active",
            "job_id",
            "agency_id",
            "short_name",
            "uni_desktop_session",
            "uni_movil_session",
            "imei_movil_session",
            "distributor",
            "ev_sent",
            "virtual",
            "last_profile",
            "trips_manager"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, document);
        q.setParam(2, firstName);
        q.setParam(3, lastName);
        q.setParam(4, mail);
        q.setParam(5, phone);
        q.setParam(6, perEmployeeId);
        q.setParam(7, perCandidateId);
        q.setParam(8, storeId);
        q.setParam(9, contractorId);
        q.setParam(10, technicianId);
        q.setParam(11, login);
        q.setParam(12, password);
        q.setParam(13, lastPasswordChange);
        q.setParam(14, guest);
        q.setParam(15, begExp);
        q.setParam(16, driver);
        q.setParam(17, active);
        q.setParam(18, jobId);
        q.setParam(19, agencyId);
        q.setParam(20, shortName);
        q.setParam(21, uniDesktopSession);
        q.setParam(22, uniMovilSession);
        q.setParam(23, imeiMovilSession);
        q.setParam(24, distributor);
        q.setParam(25, evSent);
        q.setParam(26, virtual);
        q.setParam(27, lastProfile);
        q.setParam(28, tripsManager);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        document = MySQLQuery.getAsString(row[0]);
        firstName = MySQLQuery.getAsString(row[1]);
        lastName = MySQLQuery.getAsString(row[2]);
        mail = MySQLQuery.getAsString(row[3]);
        phone = MySQLQuery.getAsString(row[4]);
        perEmployeeId = MySQLQuery.getAsInteger(row[5]);
        perCandidateId = MySQLQuery.getAsInteger(row[6]);
        storeId = MySQLQuery.getAsInteger(row[7]);
        contractorId = MySQLQuery.getAsInteger(row[8]);
        technicianId = MySQLQuery.getAsInteger(row[9]);
        login = MySQLQuery.getAsString(row[10]);
        password = MySQLQuery.getAsString(row[11]);
        lastPasswordChange = MySQLQuery.getAsDate(row[12]);
        guest = MySQLQuery.getAsBoolean(row[13]);
        begExp = MySQLQuery.getAsDate(row[14]);
        driver = MySQLQuery.getAsBoolean(row[15]);
        active = MySQLQuery.getAsBoolean(row[16]);
        jobId = MySQLQuery.getAsInteger(row[17]);
        agencyId = MySQLQuery.getAsInteger(row[18]);
        shortName = MySQLQuery.getAsString(row[19]);
        uniDesktopSession = MySQLQuery.getAsBoolean(row[20]);
        uniMovilSession = MySQLQuery.getAsBoolean(row[21]);
        imeiMovilSession = MySQLQuery.getAsString(row[22]);
        distributor = MySQLQuery.getAsBoolean(row[23]);
        evSent = MySQLQuery.getAsBoolean(row[24]);
        virtual = MySQLQuery.getAsBoolean(row[25]);
        lastProfile = MySQLQuery.getAsInteger(row[26]);
        tripsManager = MySQLQuery.getAsBoolean(row[27]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "employee";
    }

    public static String getSelFlds(String alias) {
        return new Employee().getSelFldsForAlias(alias);
    }

    public static List<Employee> getList(MySQLQuery q, Connection conn) throws Exception {
        return new Employee().getListFromQuery(q, conn);
    }

    public static List<Employee> getList(Params p, Connection conn) throws Exception {
        return new Employee().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new Employee().deleteById(id, conn);
    }

    public static List<Employee> getAll(Connection conn) throws Exception {
        return new Employee().getAllList(conn);
    }

//fin zona de reemplazo
    public static int closeSessions(int empId, Connection conn) throws Exception {
        Integer sessions = new MySQLQuery("SELECT COUNT(*) FROM session_login s WHERE s.end_time IS NULL AND s.employee_id = " + empId).getAsInteger(conn);
        new MySQLQuery("UPDATE session_login s SET s.end_time = NOW() WHERE s.end_time IS NULL AND s.employee_id = " + empId).executeUpdate(conn);
        return sessions;
    }

    public static Employee getByDoc(String document, Connection conn) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT " + getSelFlds("") + " "
                + "FROM employee "
                + "WHERE document = ?1;").setParam(1, document);
        return new Employee().select(q, conn);
    }
    
}
