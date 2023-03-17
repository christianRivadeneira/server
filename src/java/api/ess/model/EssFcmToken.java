package api.ess.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class EssFcmToken extends BaseModel<EssFcmToken> {
//inicio zona de reemplazo

    public int usrId;
    public String token;
    public String device;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "usr_id",
            "token",
            "device"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, usrId);
        q.setParam(2, token);
        q.setParam(3, device);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        usrId = MySQLQuery.getAsInteger(row[0]);
        token = MySQLQuery.getAsString(row[1]);
        device = MySQLQuery.getAsString(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ess_fcm_token";
    }

    public static String getSelFlds(String alias) {
        return new EssFcmToken().getSelFldsForAlias(alias);
    }

    public static List<EssFcmToken> getList(MySQLQuery q, Connection conn) throws Exception {
        return new EssFcmToken().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new EssFcmToken().deleteById(id, conn);
    }

    public static List<EssFcmToken> getAll(Connection conn) throws Exception {
        return new EssFcmToken().getAllList(conn);
    }

//fin zona de reemplazo
}
