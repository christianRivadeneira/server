package api.rpt.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class RptRpt extends BaseModel<RptRpt> {

    /* public List<RptRptFld> rows;
    public List<RptRptFld> cols;
    public List<RptRptFld> joins;
    public List<RptRptFld> filts;*/
    //  public boolean isSetup;
//inicio zona de reemplazo
    public int cubeId;
    public Integer dashId;
    public Integer drillOfId;
    public Date created;
    public String name;
    public Integer limitRows;
    public String type;
    public String color;
    public int drillLevel;
    public int lastTime;
    public int place;
    public boolean isSetup;
    public boolean hasPreview;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "cube_id",
            "dash_id",
            "drill_of_id",
            "created",
            "name",
            "limit_rows",
            "type",
            "color",
            "drill_level",
            "last_time",
            "place"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, cubeId);
        q.setParam(2, dashId);
        q.setParam(3, drillOfId);
        q.setParam(4, created);
        q.setParam(5, name);
        q.setParam(6, limitRows);
        q.setParam(7, type);
        q.setParam(8, color);
        q.setParam(9, drillLevel);
        q.setParam(10, lastTime);
        q.setParam(11, place);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        cubeId = MySQLQuery.getAsInteger(row[0]);
        dashId = MySQLQuery.getAsInteger(row[1]);
        drillOfId = MySQLQuery.getAsInteger(row[2]);
        created = MySQLQuery.getAsDate(row[3]);
        name = MySQLQuery.getAsString(row[4]);
        limitRows = MySQLQuery.getAsInteger(row[5]);
        type = MySQLQuery.getAsString(row[6]);
        color = MySQLQuery.getAsString(row[7]);
        drillLevel = MySQLQuery.getAsInteger(row[8]);
        lastTime = MySQLQuery.getAsInteger(row[9]);
        place = MySQLQuery.getAsInteger(row[10]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "rpt_rpt";
    }

    public static String getSelFlds(String alias) {
        return new RptRpt().getSelFldsForAlias(alias);
    }

    public static List<RptRpt> getList(MySQLQuery q, Connection conn) throws Exception {
        return new RptRpt().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new RptRpt().deleteById(id, conn);
    }

    public static List<RptRpt> getAll(Connection conn) throws Exception {
        return new RptRpt().getAllList(conn);
    }

//fin zona de reemplazo
    /*   @Override
    public void afterSelect(Connection con) throws Exception {
        rows = RptRptFldApi.getByRptAndType(id, "row", con);
        cols = RptRptFldApi.getByRptAndType(id, "col", con);
        joins = RptRptFldApi.getByRptAndType(id, "join", con);
        filts = RptRptFldApi.getByRptAndType(id, "filt", con);
    }*/
    @Override
    public void afterSelect(Connection con) throws Exception {

        Integer cols = new MySQLQuery("SELECT COUNT(*) FROM rpt_rpt_fld WHERE rpt_id = ?1 AND type = ?2").setParam(1, id).setParam(2, "col").getAsInteger(con);
        Integer rows = new MySQLQuery("SELECT COUNT(*) FROM rpt_rpt_fld WHERE rpt_id = ?1 AND type = ?2").setParam(1, id).setParam(2, "row").getAsInteger(con);
        Integer joins = new MySQLQuery("SELECT COUNT(*) FROM rpt_rpt_fld WHERE rpt_id = ?1 AND type = ?2").setParam(1, id).setParam(2, "join").getAsInteger(con);
        int dims = cols + rows;

        switch (type) {
            case "clustered":
            case "stacked":
            case "stacked100":
            case "line":
                isSetup = cols > 0 && joins > 0;
                break;
            case "pie":
            case "donnut":
                isSetup = rows > 0 && joins > 0;
                break;
            case "pivot":
                isSetup = dims > 0 && joins > 0;
                break;
            case "table":
                isSetup = rows > 0;
                break;
            default:
                throw new RuntimeException("Type " + type + " no recognized");
        }
        hasPreview = lastTime < 10;
    }
}
