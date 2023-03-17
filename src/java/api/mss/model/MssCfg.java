package api.mss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class MssCfg extends BaseModel<MssCfg> {
//inicio zona de reemplazo

    public int challengeTolerance;
    public int pointRadiusTolerance;
    public int inTolerance;
    public int anticipation;
    public int perReviewShift;
    public int perReviewRound;
    public String adminMail;
    public Integer roundTolerance;
    public boolean allowEventualShift;
    public int maxWorkingHours;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "challenge_tolerance",
            "point_radius_tolerance",
            "in_tolerance",
            "anticipation",
            "per_review_shift",
            "per_review_round",
            "admin_mail",
            "round_tolerance",
            "allow_eventual_shift",
            "max_working_hours"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, challengeTolerance);
        q.setParam(2, pointRadiusTolerance);
        q.setParam(3, inTolerance);
        q.setParam(4, anticipation);
        q.setParam(5, perReviewShift);
        q.setParam(6, perReviewRound);
        q.setParam(7, adminMail);
        q.setParam(8, roundTolerance);
        q.setParam(9, allowEventualShift);
        q.setParam(10, maxWorkingHours);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        challengeTolerance = MySQLQuery.getAsInteger(row[0]);
        pointRadiusTolerance = MySQLQuery.getAsInteger(row[1]);
        inTolerance = MySQLQuery.getAsInteger(row[2]);
        anticipation = MySQLQuery.getAsInteger(row[3]);
        perReviewShift = MySQLQuery.getAsInteger(row[4]);
        perReviewRound = MySQLQuery.getAsInteger(row[5]);
        adminMail = MySQLQuery.getAsString(row[6]);
        roundTolerance = MySQLQuery.getAsInteger(row[7]);
        allowEventualShift = MySQLQuery.getAsBoolean(row[8]);
        maxWorkingHours = MySQLQuery.getAsInteger(row[9]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_cfg";
    }

    public static String getSelFlds(String alias) {
        return new MssCfg().getSelFldsForAlias(alias);
    }

    public static List<MssCfg> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssCfg().getListFromQuery(q, conn);
    }

    public static List<MssCfg> getList(Params p, Connection conn) throws Exception {
        return new MssCfg().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssCfg().deleteById(id, conn);
    }

    public static List<MssCfg> getAll(Connection conn) throws Exception {
        return new MssCfg().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<MssCfg> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}
