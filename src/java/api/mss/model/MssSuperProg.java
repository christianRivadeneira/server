package api.mss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class MssSuperProg extends BaseModel<MssSuperProg> {
//inicio zona de reemplazo

    public Date begDt;
    public Date endDt;
    public int superId;
    public Integer pathId;
    public int postId;
    public Date arrivalDt;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "beg_dt",
            "end_dt",
            "super_id",
            "path_id",
            "post_id",
            "arrival_dt"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, begDt);
        q.setParam(2, endDt);
        q.setParam(3, superId);
        q.setParam(4, pathId);
        q.setParam(5, postId);
        q.setParam(6, arrivalDt);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        begDt = MySQLQuery.getAsDate(row[0]);
        endDt = MySQLQuery.getAsDate(row[1]);
        superId = MySQLQuery.getAsInteger(row[2]);
        pathId = MySQLQuery.getAsInteger(row[3]);
        postId = MySQLQuery.getAsInteger(row[4]);
        arrivalDt = MySQLQuery.getAsDate(row[5]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_super_prog";
    }

    public static String getSelFlds(String alias) {
        return new MssSuperProg().getSelFldsForAlias(alias);
    }

    public static List<MssSuperProg> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssSuperProg().getListFromQuery(q, conn);
    }

    public static List<MssSuperProg> getList(Params p, Connection conn) throws Exception {
        return new MssSuperProg().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssSuperProg().deleteById(id, conn);
    }

    public static List<MssSuperProg> getAll(Connection conn) throws Exception {
        return new MssSuperProg().getAllList(conn);
    }

//fin zona de reemplazo
}
