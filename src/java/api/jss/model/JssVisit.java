package api.jss.model;

import api.BaseModel;
import api.Params;
import api.jss.dto.PqrsVisitDto;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class JssVisit extends BaseModel<JssVisit> {
//inicio zona de reemplazo

    public int clientId;
    public int agentId;
    public Integer spanId;
    public Date begDt;
    public Date endDt;
    public String notes;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "client_id",
            "agent_id",
            "span_id",
            "beg_dt",
            "end_dt",
            "notes"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, clientId);
        q.setParam(2, agentId);
        q.setParam(3, spanId);
        q.setParam(4, begDt);
        q.setParam(5, endDt);
        q.setParam(6, notes);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        clientId = MySQLQuery.getAsInteger(row[0]);
        agentId = MySQLQuery.getAsInteger(row[1]);
        spanId = MySQLQuery.getAsInteger(row[2]);
        begDt = MySQLQuery.getAsDate(row[3]);
        endDt = MySQLQuery.getAsDate(row[4]);
        notes = MySQLQuery.getAsString(row[5]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "jss_visit";
    }

    public static String getSelFlds(String alias) {
        return new JssVisit().getSelFldsForAlias(alias);
    }

    public static List<JssVisit> getList(MySQLQuery q, Connection conn) throws Exception {
        return new JssVisit().getListFromQuery(q, conn);
    }

    public static List<JssVisit> getList(Params p, Connection conn) throws Exception {
        return new JssVisit().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new JssVisit().deleteById(id, conn);
    }

    public static List<JssVisit> getAll(Connection conn) throws Exception {
        return new JssVisit().getAllList(conn);
    }

//fin zona de reemplazo
    public static List<PqrsVisitDto> getPqrsVisits(Connection conn) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT c.acc, v.notes "
                + "FROM jss_visit v "
                + "INNER JOIN jss_alarm_client c ON c.id = v.client_id "
                + "WHERE v.notes IS NOT NULL");
        Object[][] data = q.getRecords(conn);
        List<PqrsVisitDto> lst = new ArrayList<>();

        if (data != null && data.length > 0) {
            for (Object[] row : data) {
                PqrsVisitDto rowDTO = new PqrsVisitDto();
                rowDTO.acc = MySQLQuery.getAsString(row[0]);
                rowDTO.notes = MySQLQuery.getAsString(row[1]);
                lst.add(rowDTO);
            }
        }
        return lst;
    }

}
