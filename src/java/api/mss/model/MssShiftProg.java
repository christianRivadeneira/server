package api.mss.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class MssShiftProg extends BaseModel<MssShiftProg> {
//inicio zona de reemplazo

    public String name;
    public int postId;
    public Date beg;
    public Date end;
    public boolean endNextDay;
    public boolean active;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "post_id",
            "beg",
            "end",
            "end_next_day",
            "active"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, postId);
        q.setParam(3, beg);
        q.setParam(4, end);
        q.setParam(5, endNextDay);
        q.setParam(6, active);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        postId = MySQLQuery.getAsInteger(row[1]);
        beg = MySQLQuery.getAsDate(row[2]);
        end = MySQLQuery.getAsDate(row[3]);
        endNextDay = MySQLQuery.getAsBoolean(row[4]);
        active = MySQLQuery.getAsBoolean(row[5]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_shift_prog";
    }

    public static String getSelFlds(String alias) {
        return new MssShiftProg().getSelFldsForAlias(alias);
    }

    public static List<MssShiftProg> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssShiftProg().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssShiftProg().deleteById(id, conn);
    }

    public static List<MssShiftProg> getAll(Connection conn) throws Exception {
        return new MssShiftProg().getAllList(conn);
    }

//fin zona de reemplazo
}
