package api.mss.model;

import api.BaseModel;
import api.GridResult;
import api.MySQLCol;
import api.Params;
import api.mss.dto.ReviewInfo;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import utilities.MySQLQuery;

public class MssShift extends BaseModel<MssShift> {
//inicio zona de reemplazo

    public int guardId;
    public Integer postId;
    public Date regBeg;
    public Date regEnd;
    public boolean active;
    public Date expBeg;
    public Date expEnd;
    public String scCode;
    public String chkStatus;
    public Date regChk;
    public Date revDt;
    public String revNotes;
    public Integer inTolerance;
    public Integer anticipation;
    public boolean notified;
    public Boolean makeRound;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "guard_id",
            "post_id",
            "reg_beg",
            "reg_end",
            "active",
            "exp_beg",
            "exp_end",
            "sc_code",
            "chk_status",
            "reg_chk",
            "rev_dt",
            "rev_notes",
            "in_tolerance",
            "anticipation",
            "notified",
            "make_round"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, guardId);
        q.setParam(2, postId);
        q.setParam(3, regBeg);
        q.setParam(4, regEnd);
        q.setParam(5, active);
        q.setParam(6, expBeg);
        q.setParam(7, expEnd);
        q.setParam(8, scCode);
        q.setParam(9, chkStatus);
        q.setParam(10, regChk);
        q.setParam(11, revDt);
        q.setParam(12, revNotes);
        q.setParam(13, inTolerance);
        q.setParam(14, anticipation);
        q.setParam(15, notified);
        q.setParam(16, makeRound);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        guardId = MySQLQuery.getAsInteger(row[0]);
        postId = MySQLQuery.getAsInteger(row[1]);
        regBeg = MySQLQuery.getAsDate(row[2]);
        regEnd = MySQLQuery.getAsDate(row[3]);
        active = MySQLQuery.getAsBoolean(row[4]);
        expBeg = MySQLQuery.getAsDate(row[5]);
        expEnd = MySQLQuery.getAsDate(row[6]);
        scCode = MySQLQuery.getAsString(row[7]);
        chkStatus = MySQLQuery.getAsString(row[8]);
        regChk = MySQLQuery.getAsDate(row[9]);
        revDt = MySQLQuery.getAsDate(row[10]);
        revNotes = MySQLQuery.getAsString(row[11]);
        inTolerance = MySQLQuery.getAsInteger(row[12]);
        anticipation = MySQLQuery.getAsInteger(row[13]);
        notified = MySQLQuery.getAsBoolean(row[14]);
        makeRound = MySQLQuery.getAsBoolean(row[15]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_shift";
    }

    public static String getSelFlds(String alias) {
        return new MssShift().getSelFldsForAlias(alias);
    }

    public static List<MssShift> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssShift().getListFromQuery(q, conn);
    }

    public static List<MssShift> getList(Params p, Connection conn) throws Exception {
        return new MssShift().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssShift().deleteById(id, conn);
    }

    public static List<MssShift> getAll(Connection conn) throws Exception {
        return new MssShift().getAllList(conn);
    }

//fin zona de reemplazo
    public static ReviewInfo getPendingShift(Connection conn) throws Exception {

        MySQLQuery mq = new MySQLQuery("SELECT s.id, CONCAT(g.first_name, ' ', g.last_name), s.exp_beg, s.exp_end, DATE_FORMAT(s.exp_beg, \"%d/%m/%Y %H:%i:%s\") AS strDate "
                + "FROM mss_shift s "
                + "INNER JOIN mss_post p ON p.id = s.post_id "
                + "INNER JOIN mss_guard g ON g.id = s.guard_id "
                + "WHERE "
                + "s.reg_beg IS NOT NULL AND s.chk_status = 'pending' AND s.active AND p.active AND p.begin_dt <= DATE(s.exp_beg) "
                + "ORDER BY s.id DESC LIMIT 1");

        Object[] row = mq.getRecord(conn);
        if (row != null) {
            ReviewInfo info = new ReviewInfo();
            info.id = MySQLQuery.getAsInteger(row[0]);
            info.guardName = MySQLQuery.getAsString(row[1]);
            info.begDt = MySQLQuery.getAsDate(row[2]);
            info.endDt = MySQLQuery.getAsDate(row[3]);
            info.strBegDt = MySQLQuery.getAsString(row[4]);
            return info;
        }
        return null;
    }

    public static boolean hasShiftByDates(int guardId, Date beg, Date end, Connection conn) throws Exception {
        return new MySQLQuery("SELECT COUNT(*)>0 "
                + " FROM mss_shift s "
                + " INNER JOIN mss_post p ON p.id = s.post_id "
                + " WHERE s.guard_id = ?3 AND s.active AND "
                + " AND p.begin_dt <= DATE(s.exp_beg) AND "
                + " (s.exp_beg BETWEEN ?1 AND ?2 OR "
                + " (s.exp_end BETWEEN ?1 AND ?2 )) ")
                .setParam(1, beg).setParam(2, end).setParam(3, guardId)
                .getAsBoolean(conn);
    }

    public static GridResult getLateCheckInReport(Connection conn) throws Exception {
        GridResult r = new GridResult();
        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_TEXT, 50, "Cedula"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 75, "Nombre"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 75, "Puesto"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 35, "Tipo"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 50, "Esperada"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 50, "Registrada")
        };

        r.data = new MySQLQuery(" SELECT * FROM ("
                + " SELECT "
                + " g.document, "
                + " CONCAT(g.first_name, ' ', g.last_name) AS empName, "
                + " p.name, "
                + " 'Entrada', "
                + " s.exp_beg, "
                + " s.reg_beg "
                + " FROM mss_shift s "
                + " INNER JOIN mss_post p ON p.id = s.post_id "
                + " INNER JOIN mss_guard g ON g.id = s.guard_id "
                + " WHERE "
                + " s.exp_beg BETWEEN SUBDATE(CURDATE(),1) AND ADDDATE(CURDATE(),1) "
                + " AND s.active AND p.begin_dt <= DATE(s.exp_beg) "
                + " AND s.reg_beg IS NOT NULL "
                + " AND (TO_SECONDS(s.reg_beg) NOT BETWEEN (TO_SECONDS(DATE_SUB( DATE_SUB(s.exp_beg, INTERVAL (s.anticipation *60) SECOND) , INTERVAL (s.in_tolerance * 60) SECOND))) AND  (TO_SECONDS(DATE_ADD( DATE_SUB(s.exp_beg, INTERVAL (s.anticipation *60) SECOND) , INTERVAL (s.in_tolerance * 60) SECOND)))) "
                + " AND s.notified = 0 "
                + " UNION ALL "
                + " SELECT "
                + " g.document, "
                + " CONCAT(g.first_name, ' ', g.last_name) AS empName, "
                + " p.name, "
                + " 'Salida', "
                + " s.exp_end, "
                + " s.reg_end "
                + " FROM mss_shift s "
                + " INNER JOIN mss_post p ON p.id = s.post_id "
                + " INNER JOIN mss_guard g ON g.id = s.guard_id "
                + " WHERE "
                + " s.exp_end BETWEEN SUBDATE(CURDATE(),1) AND ADDDATE(CURDATE(),1) "
                + " AND s.active AND p.begin_dt <= DATE(s.exp_end) "
                + " AND s.reg_end IS NOT NULL "
                + " AND (TO_SECONDS(s.reg_end) NOT BETWEEN (TO_SECONDS(DATE_SUB( DATE_SUB(s.exp_end, INTERVAL (s.anticipation *60) SECOND) , INTERVAL (s.in_tolerance * 60) SECOND))) AND  (TO_SECONDS(DATE_ADD( DATE_SUB(s.exp_end, INTERVAL (s.anticipation *60) SECOND) , INTERVAL (s.in_tolerance * 60) SECOND)))) "
                + " AND s.notified = 0 "
                + " ) AS t ORDER BY t.empName ")
                .getRecords(conn);

        return r;
    }

    public static void updateLateShiftNotified(Connection conn) throws Exception {
        new MySQLQuery("UPDATE mss_shift s "
                + "INNER JOIN mss_post p ON p.id = s.post_id "
                + "SET s.notified = 1 "
                + "WHERE s.active AND p.notified = 0 "
                + "s.exp_beg BETWEEN SUBDATE(CURDATE(),1) AND ADDDATE(CURDATE(),1) "
                + "AND s.active AND p.begin_dt <= DATE(s.exp_beg) "
                + "AND (TO_SECONDS(s.reg_beg) NOT BETWEEN (TO_SECONDS(DATE_SUB( DATE_SUB(s.exp_beg, INTERVAL (s.anticipation *60) SECOND) , INTERVAL (s.in_tolerance * 60) SECOND))) AND  (TO_SECONDS(DATE_ADD( DATE_SUB(s.exp_beg, INTERVAL (s.anticipation *60) SECOND) , INTERVAL (s.in_tolerance * 60) SECOND)))) ").executeUpdate(conn);
    }

    public static MssShift createInstantShift(Connection conn, int guardId, int postId, Date curDate, boolean makeRound, MssCfg cfg) throws Exception {
        MssShift obj = new MssShift();
        obj.active = true;
        obj.anticipation = null;

        BigDecimal val = BigDecimal.valueOf(Math.random() * 10 + 1).setScale(1, RoundingMode.HALF_EVEN);
        if (cfg.perReviewShift > 0) {
            obj.chkStatus = (val.compareTo(BigDecimal.ONE) >= 0 && val.compareTo(new BigDecimal(cfg.perReviewRound / 10)) <= 0 ? "skip" : "pending");
        } else {
            obj.chkStatus = "skip";
        }
        obj.expBeg = curDate;
        obj.inTolerance = 0;
        obj.makeRound = makeRound;
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(curDate);
        gc.add(GregorianCalendar.HOUR_OF_DAY, (cfg.maxWorkingHours > 0 ? cfg.maxWorkingHours : 12));
        obj.expEnd = gc.getTime();
        obj.notified = false;
        obj.postId = postId;
        obj.guardId = guardId;
        obj.insert(conn);
        return obj;
    }
}
