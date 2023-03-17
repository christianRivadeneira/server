package api.jss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class JssSpan extends BaseModel<JssSpan> {
//inicio zona de reemplazo

    public Date begDt;
    public Date endDt;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "beg_dt",
            "end_dt"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, begDt);
        q.setParam(2, endDt);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        begDt = MySQLQuery.getAsDate(row[0]);
        endDt = MySQLQuery.getAsDate(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "jss_span";
    }

    public static String getSelFlds(String alias) {
        return new JssSpan().getSelFldsForAlias(alias);
    }

    public static List<JssSpan> getList(MySQLQuery q, Connection conn) throws Exception {
        return new JssSpan().getListFromQuery(q, conn);
    }

    public static List<JssSpan> getList(Params p, Connection conn) throws Exception {
        return new JssSpan().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new JssSpan().deleteById(id, conn);
    }

    public static List<JssSpan> getAll(Connection conn) throws Exception {
        return new JssSpan().getAllList(conn);
    }

//fin zona de reemplazo
    public static JssSpan selectNow(Connection conn) throws Exception {
        Object[] row = new MySQLQuery("SELECT " + getSelFlds("") + ", id FROM jss_span WHERE NOW() BETWEEN beg_dt AND end_dt LIMIT 1").getRecord(conn);

        if (row != null && row.length > 0) {
            JssSpan obj = new JssSpan();
            obj.setRow(row);
            return obj;
        } else {
            return null;
        }
    }   

}
