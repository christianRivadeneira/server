package api.mss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class MssRoundProgTime extends BaseModel<MssRoundProgTime> {
//inicio zona de reemplazo

    public int progId;
    public Date begin;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "prog_id",
            "begin"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, progId);
        q.setParam(2, begin);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        progId = MySQLQuery.getAsInteger(row[0]);
        begin = MySQLQuery.getAsDate(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_round_prog_time";
    }

    public static String getSelFlds(String alias) {
        return new MssRoundProgTime().getSelFldsForAlias(alias);
    }

    public static List<MssRoundProgTime> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssRoundProgTime().getListFromQuery(q, conn);
    }

    public static List<MssRoundProgTime> getList(Params p, Connection conn) throws Exception {
        return new MssRoundProgTime().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssRoundProgTime().deleteById(id, conn);
    }

    public static List<MssRoundProgTime> getAll(Connection conn) throws Exception {
        return new MssRoundProgTime().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<MssRoundProgTime> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}