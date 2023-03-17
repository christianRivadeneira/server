package api.rpt.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import utilities.MySQLQuery;

public class RptCubeCond extends BaseModel<RptCubeCond> {

    public static String[] HIDDEN = new String[]{"tbls"};

    private Integer[] tblIds = null;
    public RptCubeTbl tbls[];

//inicio zona de reemplazo
    public int cubeId;
    public String query;
    public Integer tbl1Id;
    public Integer tbl2Id;
    public Integer tbl3Id;
    public Integer tbl4Id;
    public Integer tbl5Id;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "cube_id",
            "query",
            "tbl1_id",
            "tbl2_id",
            "tbl3_id",
            "tbl4_id",
            "tbl5_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, cubeId);
        q.setParam(2, query);
        q.setParam(3, tbl1Id);
        q.setParam(4, tbl2Id);
        q.setParam(5, tbl3Id);
        q.setParam(6, tbl4Id);
        q.setParam(7, tbl5Id);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        cubeId = MySQLQuery.getAsInteger(row[0]);
        query = MySQLQuery.getAsString(row[1]);
        tbl1Id = MySQLQuery.getAsInteger(row[2]);
        tbl2Id = MySQLQuery.getAsInteger(row[3]);
        tbl3Id = MySQLQuery.getAsInteger(row[4]);
        tbl4Id = MySQLQuery.getAsInteger(row[5]);
        tbl5Id = MySQLQuery.getAsInteger(row[6]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "rpt_cube_cond";
    }

    public static String getSelFlds(String alias) {
        return new RptCubeCond().getSelFldsForAlias(alias);
    }

    public static List<RptCubeCond> getList(MySQLQuery q, Connection conn) throws Exception {
        return new RptCubeCond().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new RptCubeCond().deleteById(id, conn);
    }

    public static List<RptCubeCond> getAll(Connection conn) throws Exception {
        return new RptCubeCond().getAllList(conn);
    }

//fin zona de reemplazo
    
    public static List<RptCubeCond> getByCubeQuery(int cubeId, Connection conn) throws Exception {
        return getList(new MySQLQuery("SELECT " + RptCubeCond.getSelFlds("") + " FROM rpt_cube_cond WHERE cube_id = " + cubeId + ""), conn);
    }

    public static List<RptCubeCond> getByRptQuery(int rptId, Connection conn) throws Exception {
        return getList(new MySQLQuery("SELECT " + RptCubeCond.getSelFlds("") + " FROM rpt_cube_cond WHERE cube_id = (SELECT cube_id FROM rpt_rpt WHERE id = " + rptId + ")"), conn);
    }

    public void setTables(List<RptCubeTbl> allTbls) {
        Integer[] ids = getTblIds();
        tbls = new RptCubeTbl[ids.length];
        for (int i = 0; i < ids.length; i++) {
            Integer cId = ids[i];
            for (RptCubeTbl tbl : allTbls) {
                if (tbl.id == cId) {
                    tbls[i] = tbl;
                    break;
                }
            }
        }
    }

    public Integer[] getTblIds() {
        List<Integer> list = new ArrayList<>();
        if (tbl1Id != null) {
            list.add(tbl1Id);
        }
        if (tbl2Id != null) {
            list.add(tbl2Id);
        }
        if (tbl3Id != null) {
            list.add(tbl3Id);
        }
        if (tbl4Id != null) {
            list.add(tbl4Id);
        }
        if (tbl5Id != null) {
            list.add(tbl5Id);
        }
        tblIds = list.toArray(new Integer[list.size()]);
        return tblIds;
    }

}
