package api.rpt.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import utilities.MySQLQuery;

public class RptCubeTbl extends BaseModel<RptCubeTbl> {

    public static String[] HIDDEN = new String[]{"tbls"};

    private Integer[] tblIds = null;
    public RptCubeTbl tbls[];

//inicio zona de reemplazo
    public int cubeId;
    public Integer tbl1Id;
    public Integer tbl2Id;
    public Integer tbl3Id;
    public Integer tbl4Id;
    public Integer tbl5Id;
    public String cond;
    public String type;
    public String tbl;
    public String alias;
    public String dataClass;
    public int place;
    public Integer extTblId;
    public String ownKey;
    public String extKey;
    public String extraCond;
    public Integer oldId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "cube_id",
            "tbl1_id",
            "tbl2_id",
            "tbl3_id",
            "tbl4_id",
            "tbl5_id",
            "cond",
            "type",
            "tbl",
            "alias",
            "data_class",
            "place",
            "ext_tbl_id",
            "own_key",
            "ext_key",
            "extra_cond",
            "old_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, cubeId);
        q.setParam(2, tbl1Id);
        q.setParam(3, tbl2Id);
        q.setParam(4, tbl3Id);
        q.setParam(5, tbl4Id);
        q.setParam(6, tbl5Id);
        q.setParam(7, cond);
        q.setParam(8, type);
        q.setParam(9, tbl);
        q.setParam(10, alias);
        q.setParam(11, dataClass);
        q.setParam(12, place);
        q.setParam(13, extTblId);
        q.setParam(14, ownKey);
        q.setParam(15, extKey);
        q.setParam(16, extraCond);
        q.setParam(17, oldId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        cubeId = MySQLQuery.getAsInteger(row[0]);
        tbl1Id = MySQLQuery.getAsInteger(row[1]);
        tbl2Id = MySQLQuery.getAsInteger(row[2]);
        tbl3Id = MySQLQuery.getAsInteger(row[3]);
        tbl4Id = MySQLQuery.getAsInteger(row[4]);
        tbl5Id = MySQLQuery.getAsInteger(row[5]);
        cond = MySQLQuery.getAsString(row[6]);
        type = MySQLQuery.getAsString(row[7]);
        tbl = MySQLQuery.getAsString(row[8]);
        alias = MySQLQuery.getAsString(row[9]);
        dataClass = MySQLQuery.getAsString(row[10]);
        place = MySQLQuery.getAsInteger(row[11]);
        extTblId = MySQLQuery.getAsInteger(row[12]);
        ownKey = MySQLQuery.getAsString(row[13]);
        extKey = MySQLQuery.getAsString(row[14]);
        extraCond = MySQLQuery.getAsString(row[15]);
        oldId = MySQLQuery.getAsInteger(row[16]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "rpt_cube_tbl";
    }

    public static String getSelFlds(String alias) {
        return new RptCubeTbl().getSelFldsForAlias(alias);
    }

    public static List<RptCubeTbl> getList(MySQLQuery q, Connection conn) throws Exception {
        return new RptCubeTbl().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new RptCubeTbl().deleteById(id, conn);
    }

    public static List<RptCubeTbl> getAll(Connection conn) throws Exception {
        return new RptCubeTbl().getAllList(conn);
    }

//fin zona de reemplazo
    
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

    public void setTables(List<RptCubeTbl> allTbls) {
        Integer[] ids = getTblIds();
        tbls = new RptCubeTbl[ids.length];
        for (int i = 0; i < ids.length; i++) {
            Integer cId = ids[i];
            for (RptCubeTbl t : allTbls) {
                if (t.id == cId) {
                    tbls[i] = t;
                    break;
                }
            }
        }
    }

    public static List<RptCubeTbl> getByCubeQuery(int cubeId, Connection conn) throws Exception {
        return getList(new MySQLQuery("SELECT " + RptCubeTbl.getSelFlds("") + " FROM rpt_cube_tbl WHERE cube_id = " + cubeId + ""), conn);
    }

    public static List<RptCubeTbl> getByRptQuery(int rptId, Connection conn) throws Exception {
        return getList(new MySQLQuery("SELECT " + RptCubeTbl.getSelFlds("") + " FROM rpt_cube_tbl WHERE cube_id = (SELECT cube_id FROM rpt_rpt WHERE id = " + rptId + ")"), conn);
    }

    public static RptCubeTbl find(int id, List<RptCubeTbl> raw) {
        for (RptCubeTbl tbl : raw) {
            if (tbl.id == id) {
                return tbl;
            }
        }
        return null;
    }

}
