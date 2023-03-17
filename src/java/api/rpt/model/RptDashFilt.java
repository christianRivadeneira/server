package api.rpt.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class RptDashFilt extends BaseModel<RptDashFilt> {
//inicio zona de reemplazo

    public int fldId;
    public int dashId;
    public String filtType;
    public String filtJson;
    public String filtDesc;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "fld_id",
            "dash_id",
            "filt_type",
            "filt_json",
            "filt_desc"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, fldId);
        q.setParam(2, dashId);
        q.setParam(3, filtType);
        q.setParam(4, filtJson);
        q.setParam(5, filtDesc);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        fldId = MySQLQuery.getAsInteger(row[0]);
        dashId = MySQLQuery.getAsInteger(row[1]);
        filtType = MySQLQuery.getAsString(row[2]);
        filtJson = MySQLQuery.getAsString(row[3]);
        filtDesc = MySQLQuery.getAsString(row[4]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "rpt_dash_filt";
    }

    public static String getSelFlds(String alias) {
        return new RptDashFilt().getSelFldsForAlias(alias);
    }

    public static List<RptDashFilt> getList(MySQLQuery q, Connection conn) throws Exception {
        return new RptDashFilt().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new RptDashFilt().deleteById(id, conn);
    }

    public static List<RptDashFilt> getAll(Connection conn) throws Exception {
        return new RptDashFilt().getAllList(conn);
    }

//fin zona de reemplazo
   
}
