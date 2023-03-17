package api.mss.model;

import api.BaseModel;
import api.Params;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class MssPoint extends BaseModel<MssPoint> {
//inicio zona de reemplazo

    public Integer postId;
    public String name;
    public BigDecimal lat;
    public BigDecimal lon;
    public String code;
    public boolean isCheck;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "post_id",
            "name",
            "lat",
            "lon",
            "code",
            "is_check"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, postId);
        q.setParam(2, name);
        q.setParam(3, lat);
        q.setParam(4, lon);
        q.setParam(5, code);
        q.setParam(6, isCheck);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        postId = MySQLQuery.getAsInteger(row[0]);
        name = MySQLQuery.getAsString(row[1]);
        lat = MySQLQuery.getAsBigDecimal(row[2], false);
        lon = MySQLQuery.getAsBigDecimal(row[3], false);
        code = MySQLQuery.getAsString(row[4]);
        isCheck = MySQLQuery.getAsBoolean(row[5]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_point";
    }

    public static String getSelFlds(String alias) {
        return new MssPoint().getSelFldsForAlias(alias);
    }

    public static List<MssPoint> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssPoint().getListFromQuery(q, conn);
    }

    public static List<MssPoint> getList(Params p, Connection conn) throws Exception {
        return new MssPoint().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssPoint().deleteById(id, conn);
    }

    public static List<MssPoint> getAll(Connection conn) throws Exception {
        return new MssPoint().getAllList(conn);
    }

//fin zona de reemplazo
    public static MssPoint getByQrCode(String qrCode, Connection conn) throws Exception {
        MySQLQuery mq = new MySQLQuery("SELECT " + MssPoint.getSelFlds("") + ",id FROM mss_point WHERE code = ?1 LIMIT 1").setParam(1, qrCode);
        return new MssPoint().select(mq, conn);
    }

    public static List<MssPoint> getPointsByQrCode(String qrCode, Connection conn) throws Exception {
        MySQLQuery mq = new MySQLQuery("SELECT " + MssPoint.getSelFlds("") + ",id FROM mss_point WHERE code = ?1 ").setParam(1, qrCode);
        return new MssPoint().getListFromQuery(mq, conn);
    }

    public static MssPoint getPointByQrCodeAndPost(String qrCode, int postId, Connection conn) throws Exception {
        MySQLQuery mq = new MySQLQuery("SELECT " + MssPoint.getSelFlds("") + ",id FROM mss_point WHERE code = ?1 and post_id = ?2")
                .setParam(1, qrCode)
                .setParam(2, postId);
        return new MssPoint().select(mq, conn);
    }

    public static boolean existPointByCodeAndPost(String qrCode, Integer pointId, Connection conn, int postId) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT " + getSelFlds("") + ",id "
                + "FROM mss_point "
                + "WHERE code = ?1 AND post_id = ?3 "
                + (pointId != null ? "AND id <> ?2 " : "")).setParam(1, qrCode).setParam(3, postId);
        if (pointId != null) {
            q.setParam(2, pointId);
        }
        return new MssPoint().select(q, conn) != null;
    }

    public static boolean hasCheckPoint(int postId, Connection conn) throws Exception {
        return new MySQLQuery("SELECT COUNT(*) = 1 FROM mss_point WHERE is_check AND post_id = ?1 ")
                .setParam(1, postId).getAsBoolean(conn);
    }

}
