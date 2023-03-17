package api.mss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class MssGuard extends BaseModel<MssGuard> {
    
    public static String TYPE_GUARD = "guard";
    public static String TYPE_AGENT = "agent";
//inicio zona de reemplazo

    public int empId;
    public String firstName;
    public String lastName;
    public String document;
    public String email;
    public String phone;
    public String expDocument;
    public boolean supervisor;
    public String type;
    
    @Override
    protected String[] getFlds() {
        return new String[]{
            "emp_id",
            "first_name",
            "last_name",
            "document",
            "email",
            "phone",
            "exp_document",
            "supervisor",
            "type"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, empId);
        q.setParam(2, firstName);
        q.setParam(3, lastName);
        q.setParam(4, document);
        q.setParam(5, email);
        q.setParam(6, phone);
        q.setParam(7, expDocument);
        q.setParam(8, supervisor);
        q.setParam(9, type);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        empId = MySQLQuery.getAsInteger(row[0]);
        firstName = MySQLQuery.getAsString(row[1]);
        lastName = MySQLQuery.getAsString(row[2]);
        document = MySQLQuery.getAsString(row[3]);
        email = MySQLQuery.getAsString(row[4]);
        phone = MySQLQuery.getAsString(row[5]);
        expDocument = MySQLQuery.getAsString(row[6]);
        supervisor = MySQLQuery.getAsBoolean(row[7]);
        type = MySQLQuery.getAsString(row[8]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_guard";
    }

    public static String getSelFlds(String alias) {
        return new MssGuard().getSelFldsForAlias(alias);
    }

    public static List<MssGuard> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssGuard().getListFromQuery(q, conn);
    }

    public static List<MssGuard> getList(Params p, Connection conn) throws Exception {
        return new MssGuard().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssGuard().deleteById(id, conn);
    }

    public static List<MssGuard> getAll(Connection conn) throws Exception {
        return new MssGuard().getAllList(conn);
    }

//fin zona de reemplazo
    public static MssGuard getByDoc(String doc, String type, Connection conn) throws Exception {
        MySQLQuery mq = new MySQLQuery("SELECT " + getSelFlds("") + " "
                + "FROM mss_guard "
                + "WHERE document = ?1 AND type = ?2").setParam(1, doc).setParam(2, type);
        return new MssGuard().select(mq, conn);
    }
    
    public static MssGuard getAgent(Integer empId, Connection conn) throws Exception {
        MySQLQuery mq = new MySQLQuery("SELECT " + getSelFlds("") + " "
                + "FROM mss_guard "
                + "WHERE emp_id = ?1 AND type = 'agent'").setParam(1, empId);
        return new MssGuard().select(mq, conn);
    }


    public static Integer getFromEmployee(int empId, String type, Connection conn) throws Exception {
        Integer guardId = new MySQLQuery("SELECT id FROM mss_guard WHERE emp_id = ?1 AND supervisor = 0 "
                + "AND type = ?2").setParam(1, empId).setParam(2, type).getAsInteger(conn);
        return guardId;
    }

    public static Integer getSuperIdFromEmployee(int empId, Connection conn) throws Exception {
        Integer superId = new MySQLQuery("SELECT id FROM mss_guard WHERE emp_id = ?1 AND supervisor = 1 AND type = 'guard'").setParam(1, empId).getAsInteger(conn);
        return superId;
    }

}
