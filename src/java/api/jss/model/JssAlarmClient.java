package api.jss.model;

import api.BaseModel;
import api.Params;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import utilities.MySQLQuery;

public class JssAlarmClient extends BaseModel<JssAlarmClient> {

    public static final String ENUM_COM = "com";

    public int numVisit;
    public int numVisitDay;
    public int numNoEnd;
    public int numGoal;
    public int visitColor;
//inicio zona de reemplazo

    public String document;
    public String name;
    public String acc;
    public String address;
    public String mail;
    public String type;
    public BigDecimal lat;
    public BigDecimal lon;
    public String code;
    public String notes;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "document",
            "name",
            "acc",
            "address",
            "mail",
            "type",
            "lat",
            "lon",
            "code",
            "notes"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, document);
        q.setParam(2, name);
        q.setParam(3, acc);
        q.setParam(4, address);
        q.setParam(5, mail);
        q.setParam(6, type);
        q.setParam(7, lat);
        q.setParam(8, lon);
        q.setParam(9, code);
        q.setParam(10, notes);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        document = MySQLQuery.getAsString(row[0]);
        name = MySQLQuery.getAsString(row[1]);
        acc = MySQLQuery.getAsString(row[2]);
        address = MySQLQuery.getAsString(row[3]);
        mail = MySQLQuery.getAsString(row[4]);
        type = MySQLQuery.getAsString(row[5]);
        lat = MySQLQuery.getAsBigDecimal(row[6], false);
        lon = MySQLQuery.getAsBigDecimal(row[7], false);
        code = MySQLQuery.getAsString(row[8]);
        notes = MySQLQuery.getAsString(row[9]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "jss_alarm_client";
    }

    public static String getSelFlds(String alias) {
        return new JssAlarmClient().getSelFldsForAlias(alias);
    }

    public static List<JssAlarmClient> getList(MySQLQuery q, Connection conn) throws Exception {
        return new JssAlarmClient().getListFromQuery(q, conn);
    }

    public static List<JssAlarmClient> getList(Params p, Connection conn) throws Exception {
        return new JssAlarmClient().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new JssAlarmClient().deleteById(id, conn);
    }

    public static List<JssAlarmClient> getAll(Connection conn) throws Exception {
        return new JssAlarmClient().getAllList(conn);
    }

//fin zona de reemplazo
    public static List<JssAlarmClient> getComClients(Integer empId, Connection conn) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT " + getSelFlds("c") + ", c.id FROM jss_alarm_client c "
                + "INNER JOIN jss_client_zone cz ON cz.client_id = c.id "
                + "INNER JOIN jss_agent_zone az ON az.zone_id = cz.zone_id "
                + "INNER JOIN mss_guard ag ON ag.id = az.agent_id "
                + "INNER JOIN jss_span s ON s.id = az.span_id "
                + "WHERE ag.emp_id = ?1 AND CURDATE() BETWEEN s.beg_dt AND s.end_dt "
                + "GROUP BY c.id");
        q.setParam(1, empId);
        return JssAlarmClient.getList(q, conn);
    }

    public static List<JssAlarmClient> getComClientsVisits(Integer empId, Connection conn) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT " + getSelFlds("c") + ", "
                + "(SELECT COUNT(*) FROM jss_visit v INNER JOIN jss_span s ON s.id = v.span_id WHERE NOW() BETWEEN s.beg_dt AND s.end_dt AND v.client_id = c.id) AS visits, "
                + "(SELECT COUNT(*) FROM jss_visit v WHERE CURDATE() = DATE(v.beg_dt) AND v.client_id = c.id) AS visitsDay, "
                + "(SELECT COUNT(*) FROM jss_visit v INNER JOIN jss_span s ON s.id = v.span_id WHERE NOW() BETWEEN s.beg_dt AND s.end_dt AND v.end_dt IS NULL AND v.client_id = c.id) AS noEnd, "
                + "z.goal, "
                + "c.id "
                + "FROM jss_alarm_client c "
                + "INNER JOIN jss_client_zone cz ON cz.client_id = c.id "
                + "INNER JOIN jss_zone z ON z.id = cz.zone_id "
                + "INNER JOIN jss_agent_zone az ON az.zone_id = cz.zone_id "
                + "INNER JOIN mss_guard ag ON ag.id = az.agent_id "
                + "INNER JOIN jss_span s ON s.id = az.span_id "
                + "WHERE ag.emp_id = ?1 AND c.code IS NOT NULL AND CURDATE() BETWEEN s.beg_dt AND s.end_dt "
                + "GROUP BY c.id");
        q.setParam(1, empId);

        List<JssAlarmClient> lst = new ArrayList<>();
        Object[][] data = q.getRecords(conn);
        if (data != null && data.length > 0) {
            for (Object[] row : data) {
                JssAlarmClient rowClient = new JssAlarmClient();
                rowClient.setRow(row);

                int numFlds = getNumFlds();
                rowClient.numVisit = MySQLQuery.getAsInteger(row[numFlds + 1]);
                rowClient.numVisitDay = MySQLQuery.getAsInteger(row[numFlds + 2]);
                rowClient.numNoEnd = MySQLQuery.getAsInteger(row[numFlds + 3]);
                rowClient.numGoal = MySQLQuery.getAsInteger(row[numFlds + 4]);

                if (rowClient.numVisit >= rowClient.numGoal) {
                    rowClient.visitColor = -16711936; //green
                } else {
                    rowClient.visitColor = -65536; //red
                }
                lst.add(rowClient);
            }
        }
        return lst;
    }

    public static JssAlarmClient selectAcc(String acc, Connection conn) throws Exception {

        MySQLQuery q = new MySQLQuery("SELECT " + getSelFlds("") + ", id FROM jss_alarm_client WHERE acc = ?1").setParam(1, acc);
        return new JssAlarmClient().select(q, conn);

    }

    public static List<JssAlarmClient> selectDocument(String doc, Connection conn) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT " + getSelFlds("") + ", id FROM jss_alarm_client WHERE document = ?1").setParam(1, doc);
        return new JssAlarmClient().getListFromQuery(q, conn);
    }
    
    public static JssAlarmClient selectCode(String code, Connection conn) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT " + getSelFlds("") + ", id FROM jss_alarm_client WHERE code = ?1").setParam(1, code);
        return new JssAlarmClient().select(q, conn);
    }

    public static int getNumFlds() {
        return new JssAlarmClient().getFlds().length;
    }
}
