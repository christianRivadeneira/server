package api.hlp.model;

import api.BaseModel;
import api.Params;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class HlpPrjBacklog extends BaseModel<HlpPrjBacklog> {
//inicio zona de reemplazo

    public int prjId;
    public Integer develId;
    public String name;
    public String description;
    public String priority;
    public String status;
    public String statusNotes;
    public BigDecimal aproxHrs;
    public int place;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "prj_id",
            "devel_id",
            "name",
            "description",
            "priority",
            "status",
            "status_notes",
            "aprox_hrs",
            "place"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, prjId);
        q.setParam(2, develId);
        q.setParam(3, name);
        q.setParam(4, description);
        q.setParam(5, priority);
        q.setParam(6, status);
        q.setParam(7, statusNotes);
        q.setParam(8, aproxHrs);
        q.setParam(9, place);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        prjId = MySQLQuery.getAsInteger(row[0]);
        develId = MySQLQuery.getAsInteger(row[1]);
        name = MySQLQuery.getAsString(row[2]);
        description = MySQLQuery.getAsString(row[3]);
        priority = MySQLQuery.getAsString(row[4]);
        status = MySQLQuery.getAsString(row[5]);
        statusNotes = MySQLQuery.getAsString(row[6]);
        aproxHrs = MySQLQuery.getAsBigDecimal(row[7], false);
        place = MySQLQuery.getAsInteger(row[8]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "hlp_prj_backlog";
    }

    public static String getSelFlds(String alias) {
        return new HlpPrjBacklog().getSelFldsForAlias(alias);
    }

    public static List<HlpPrjBacklog> getList(MySQLQuery q, Connection conn) throws Exception {
        return new HlpPrjBacklog().getListFromQuery(q, conn);
    }

    public static List<HlpPrjBacklog> getList(Params p, Connection conn) throws Exception {
        return new HlpPrjBacklog().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new HlpPrjBacklog().deleteById(id, conn);
    }

    public static List<HlpPrjBacklog> getAll(Connection conn) throws Exception {
        return new HlpPrjBacklog().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<HlpPrjBacklog> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}
