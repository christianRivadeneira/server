package api.mss.model;

import api.BaseModel;
import api.Params;
import api.mss.dto.ReviewInfo;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import utilities.MySQLQuery;
import utilities.ServerNow;
import web.push.GCMUtils;

public class MssRound extends BaseModel<MssRound> {

    public String roundName;
//inicio zona de reemplazo

    public int guardId;
    public int roundProgId;
    public Date regDt;
    public Date begDt;
    public Date endDt;
    public String chkStatus;
    public Integer tolerance;
    public Date closeDt;
    public String closeNotes;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "guard_id",
            "round_prog_id",
            "reg_dt",
            "beg_dt",
            "end_dt",
            "chk_status",
            "tolerance",
            "close_dt",
            "close_notes"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, guardId);
        q.setParam(2, roundProgId);
        q.setParam(3, regDt);
        q.setParam(4, begDt);
        q.setParam(5, endDt);
        q.setParam(6, chkStatus);
        q.setParam(7, tolerance);
        q.setParam(8, closeDt);
        q.setParam(9, closeNotes);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        guardId = MySQLQuery.getAsInteger(row[0]);
        roundProgId = MySQLQuery.getAsInteger(row[1]);
        regDt = MySQLQuery.getAsDate(row[2]);
        begDt = MySQLQuery.getAsDate(row[3]);
        endDt = MySQLQuery.getAsDate(row[4]);
        chkStatus = MySQLQuery.getAsString(row[5]);
        tolerance = MySQLQuery.getAsInteger(row[6]);
        closeDt = MySQLQuery.getAsDate(row[7]);
        closeNotes = MySQLQuery.getAsString(row[8]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_round";
    }

    public static String getSelFlds(String alias) {
        return new MssRound().getSelFldsForAlias(alias);
    }

    public static List<MssRound> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssRound().getListFromQuery(q, conn);
    }

    public static List<MssRound> getList(Params p, Connection conn) throws Exception {
        return new MssRound().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssRound().deleteById(id, conn);
    }

    public static List<MssRound> getAll(Connection conn) throws Exception {
        return new MssRound().getAllList(conn);
    }

//fin zona de reemplazo
    public static ReviewInfo getPendingRound(Connection conn) throws Exception {

        MySQLQuery mq = new MySQLQuery("SELECT r.id,  CONCAT(g.first_name, ' ', g.last_name) , r.beg_dt, r.end_dt "
                + "FROM mss_round r "
                + "INNER JOIN mss_guard g ON g.id = r.guard_id "
                + "WHERE r.`status` = 'pending' "
                + "ORDER BY r.id DESC LIMIT 1");

        Object[] row = mq.getRecord(conn);
        if (row != null) {
            ReviewInfo info = new ReviewInfo();
            info.id = MySQLQuery.getAsInteger(row[0]);
            info.guardName = MySQLQuery.getAsString(row[1]);
            info.begDt = MySQLQuery.getAsDate(row[2]);
            info.endDt = MySQLQuery.getAsDate(row[3]);
            return info;
        }
        return null;
    }

    public static void sendRound(int progId, int guardId, int empId, int appId, Integer roundTolerance, Connection conn) throws Exception {
        MssRound r = new MssRound();
        r.guardId = guardId;
        r.regDt = new ServerNow();
        r.roundProgId = progId;
        r.chkStatus = (Math.random() < 0.3d ? "pending" : "skip");
        r.tolerance = roundTolerance;
        r.insert(conn);

        List<MssPointProg> pts = MssPointProg.getByProg(progId, conn);
        if (!MySQLQuery.isEmpty(pts)) {

            for (int i = 0; i < pts.size(); i++) {
                MssPointProg pt = pts.get(i);
                MssRoundPoint rpt = new MssRoundPoint();
                rpt.place = pt.place != null ? pt.place : 0;
                rpt.pointId = pt.pointId;
                rpt.roundId = r.id;
                rpt.insert(conn);
            }

            JsonObjectBuilder ob = Json.createObjectBuilder();
            ob.add("type", "round");
            ob.add("roundId", r.id);
            System.out.println("envio de ronda a: " + empId + " ronda " + r.id);
            GCMUtils.sendToApp(appId, ob.build(), empId + "", conn);
        }

    }

}
