package api.crm.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class CrmPollClient extends BaseModel<CrmPollClient> {
//inicio zona de reemplazo

    public Integer pollId;
    public Integer clientId;
    public Integer tankId;
    public Date createDate;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "poll_id",
            "client_id",
            "tank_id",
            "create_date"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, pollId);
        q.setParam(2, clientId);
        q.setParam(3, tankId);
        q.setParam(4, createDate);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        pollId = MySQLQuery.getAsInteger(row[0]);
        clientId = MySQLQuery.getAsInteger(row[1]);
        tankId = MySQLQuery.getAsInteger(row[2]);
        createDate = MySQLQuery.getAsDate(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "crm_poll_client";
    }

    public static String getSelFlds(String alias) {
        return new CrmPollClient().getSelFldsForAlias(alias);
    }

    public static List<CrmPollClient> getList(MySQLQuery q, Connection conn) throws Exception {
        return new CrmPollClient().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new CrmPollClient().deleteById(id, conn);
    }

    public static List<CrmPollClient> getAll(Connection conn) throws Exception {
        return new CrmPollClient().getAllList(conn);
    }

//fin zona de reemplazo
    
}
