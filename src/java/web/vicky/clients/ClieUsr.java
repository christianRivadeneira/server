package web.vicky.clients;

import java.sql.Connection;
import java.util.Date;
import utilities.MySQLQuery;

public class ClieUsr {
//inicio zona de reemplazo

    public int id;
    public String firstName;
    public String lastName;
    public String mail;
    public String phone;
    public String password;
    public Date registerDate;
    public String document;
    public String address;
    public String landmark;
    public String recoveryPin;
    public Date recoveryExp;
    public boolean recoveryAccepted;

    private static final String SEL_FLDS = "`first_name`, "
            + "`last_name`, "
            + "`mail`, "
            + "`phone`, "
            + "`password`, "
            + "`register_date`, "
            + "`document`, "
            + "`address`, "
            + "`landmark`, "
            + "`recovery_pin`, "
            + "`recovery_exp`, "
            + "`recovery_accepted`";

    private static final String SET_FLDS = "clie_usr SET "
            + "`first_name` = ?1, "
            + "`last_name` = ?2, "
            + "`mail` = ?3, "
            + "`phone` = ?4, "
            + "`password` = ?5, "
            + "`register_date` = ?6, "
            + "`document` = ?7, "
            + "`address` = ?8, "
            + "`landmark` = ?9, "
            + "`recovery_pin` = ?10, "
            + "`recovery_exp` = ?11, "
            + "`recovery_accepted` = ?12";

    private static void setFields(ClieUsr obj, MySQLQuery q) {
        q.setParam(1, obj.firstName);
        q.setParam(2, obj.lastName);
        q.setParam(3, obj.mail);
        q.setParam(4, obj.phone);
        q.setParam(5, obj.password);
        q.setParam(6, obj.registerDate);
        q.setParam(7, obj.document);
        q.setParam(8, obj.address);
        q.setParam(9, obj.landmark);
        q.setParam(10, obj.recoveryPin);
        q.setParam(11, obj.recoveryExp);
        q.setParam(12, obj.recoveryAccepted);

    }

    public static ClieUsr getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        ClieUsr obj = new ClieUsr();
        obj.firstName = MySQLQuery.getAsString(row[0]);
        obj.lastName = MySQLQuery.getAsString(row[1]);
        obj.mail = MySQLQuery.getAsString(row[2]);
        obj.phone = MySQLQuery.getAsString(row[3]);
        obj.password = MySQLQuery.getAsString(row[4]);
        obj.registerDate = MySQLQuery.getAsDate(row[5]);
        obj.document = MySQLQuery.getAsString(row[6]);
        obj.address = MySQLQuery.getAsString(row[7]);
        obj.landmark = MySQLQuery.getAsString(row[8]);
        obj.recoveryPin = MySQLQuery.getAsString(row[9]);
        obj.recoveryExp = MySQLQuery.getAsDate(row[10]);
        obj.recoveryAccepted = MySQLQuery.getAsBoolean(row[11]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    public static int insert(ClieUsr pobj, Connection ep) throws Exception {
        ClieUsr obj = (ClieUsr) pobj;
        MySQLQuery q = new MySQLQuery("INSERT INTO " + SET_FLDS);
        setFields(obj, q);
        return q.executeInsert(ep);
    }

    public static void update(ClieUsr pobj, Connection ep) throws Exception {
        ClieUsr obj = (ClieUsr) pobj;
        MySQLQuery q = new MySQLQuery("UPDATE " + SET_FLDS + " WHERE id = " + obj.id);
        setFields(obj, q);
        q.executeUpdate(ep);
    }

    public static void delete(int id, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("DELETE FROM clie_usr WHERE id = " + id);
        q.executeDelete(ep);
    }

    public static ClieUsr select(int id, Connection conn) throws Exception {
        return getFromRow(new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM clie_usr WHERE id = " + id).getRecord(conn));
    }

    public static ClieUsr getFromMail(String mail, Connection conn) throws Exception {
        Object[] row = new MySQLQuery("SELECT " + SEL_FLDS + ", id FROM clie_usr WHERE mail = ?1;").setParam(1, mail).getRecord(conn);
        if (row == null || row.length == 0) {
            throw new Exception("E-mail no registrado o incorrecto\nVerifíquelo o regístrese");
        }
        return getFromRow(row);
    }

}
