package api.ess.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class EssPerson extends BaseModel<EssPerson> {
//inicio zona de reemplazo

    public Integer empId;
    public String firstName;
    public String lastName;
    public String document;
    public String phone;
    public String mail;
    public String fPrintCode;
    public boolean canAuth;
    public String recoveryPin;
    public Date recoveryExp;
    public Boolean recoveryAccepted;
    public boolean active;
    public Integer buildId;
    public Integer unitId;
    public String callPriotity;
    public Boolean notify;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "emp_id",
            "first_name",
            "last_name",
            "document",
            "phone",
            "mail",
            "f_print_code",
            "can_auth",
            "recovery_pin",
            "recovery_exp",
            "recovery_accepted",
            "active",
            "build_id",
            "unit_id",
            "call_priotity",
            "notify"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, empId);
        q.setParam(2, firstName);
        q.setParam(3, lastName);
        q.setParam(4, document);
        q.setParam(5, phone);
        q.setParam(6, mail);
        q.setParam(7, fPrintCode);
        q.setParam(8, canAuth);
        q.setParam(9, recoveryPin);
        q.setParam(10, recoveryExp);
        q.setParam(11, recoveryAccepted);
        q.setParam(12, active);
        q.setParam(13, buildId);
        q.setParam(14, unitId);
        q.setParam(15, callPriotity);
        q.setParam(16, notify);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        empId = MySQLQuery.getAsInteger(row[0]);
        firstName = MySQLQuery.getAsString(row[1]);
        lastName = MySQLQuery.getAsString(row[2]);
        document = MySQLQuery.getAsString(row[3]);
        phone = MySQLQuery.getAsString(row[4]);
        mail = MySQLQuery.getAsString(row[5]);
        fPrintCode = MySQLQuery.getAsString(row[6]);
        canAuth = MySQLQuery.getAsBoolean(row[7]);
        recoveryPin = MySQLQuery.getAsString(row[8]);
        recoveryExp = MySQLQuery.getAsDate(row[9]);
        recoveryAccepted = MySQLQuery.getAsBoolean(row[10]);
        active = MySQLQuery.getAsBoolean(row[11]);
        buildId = MySQLQuery.getAsInteger(row[12]);
        unitId = MySQLQuery.getAsInteger(row[13]);
        callPriotity = MySQLQuery.getAsString(row[14]);
        notify = MySQLQuery.getAsBoolean(row[15]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ess_person";
    }

    public static String getSelFlds(String alias) {
        return new EssPerson().getSelFldsForAlias(alias);
    }

    public static List<EssPerson> getList(MySQLQuery q, Connection conn) throws Exception {
        return new EssPerson().getListFromQuery(q, conn);
    }

    public static List<EssPerson> getList(Params p, Connection conn) throws Exception {
        return new EssPerson().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new EssPerson().deleteById(id, conn);
    }

    public static List<EssPerson> getAll(Connection conn) throws Exception {
        return new EssPerson().getAllList(conn);
    }

//fin zona de reemplazo
    public static EssPerson getFromMail(String mail, Connection conn) throws Exception {

        MySQLQuery mq = new MySQLQuery("SELECT " + getSelFlds("") + ", id "
                + "FROM ess_person "
                + "WHERE mail = ?1;").setParam(1, mail);
        EssPerson person = new EssPerson().select(mq, conn);

        if (person == null) {
            throw new Exception("E-mail no registrado o incorrecto\nVerifíquelo o regístrese");
        }
        return person;
    }
    
    public static EssPerson getByDoc(String document, Connection conn) throws Exception {
        MySQLQuery mq = new MySQLQuery("SELECT " + getSelFlds("") + " "
                + "FROM ess_person "
                + "WHERE document = ?1;").setParam(1, document);
        return new EssPerson().select(mq, conn);
    }
}
