package api.rpt.model;

import api.BaseModel;
import api.rpt.api.dataTypes.DataType;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import utilities.MySQLQuery;

public class RptCubeFld extends BaseModel<RptCubeFld> {

    public static String[] HIDDEN = new String[]{"tbls", "type"};

    private Integer[] tblIds = null;
    public RptCubeTbl tbls[];
    public DataType type;

//inicio zona de reemplazo
    public Integer tbl1Id;
    public Integer tbl2Id;
    public Integer tbl3Id;
    public Integer tbl4Id;
    public Integer tbl5Id;
    public int cubeId;
    public Integer drillToId;
    public String fldType;
    public String dataType;
    public String name;
    public String query;
    public String enumOpts;
    public String dspName;
    public boolean unique;
    public String notes;
    public int place;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "tbl1_id",
            "tbl2_id",
            "tbl3_id",
            "tbl4_id",
            "tbl5_id",
            "cube_id",
            "drill_to_id",
            "fld_type",
            "data_type",
            "name",
            "query",
            "enum_opts",
            "dsp_name",
            "unique",
            "notes",
            "place"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, tbl1Id);
        q.setParam(2, tbl2Id);
        q.setParam(3, tbl3Id);
        q.setParam(4, tbl4Id);
        q.setParam(5, tbl5Id);
        q.setParam(6, cubeId);
        q.setParam(7, drillToId);
        q.setParam(8, fldType);
        q.setParam(9, dataType);
        q.setParam(10, name);
        q.setParam(11, query);
        q.setParam(12, enumOpts);
        q.setParam(13, dspName);
        q.setParam(14, unique);
        q.setParam(15, notes);
        q.setParam(16, place);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        tbl1Id = MySQLQuery.getAsInteger(row[0]);
        tbl2Id = MySQLQuery.getAsInteger(row[1]);
        tbl3Id = MySQLQuery.getAsInteger(row[2]);
        tbl4Id = MySQLQuery.getAsInteger(row[3]);
        tbl5Id = MySQLQuery.getAsInteger(row[4]);
        cubeId = MySQLQuery.getAsInteger(row[5]);
        drillToId = MySQLQuery.getAsInteger(row[6]);
        fldType = MySQLQuery.getAsString(row[7]);
        dataType = MySQLQuery.getAsString(row[8]);
        name = MySQLQuery.getAsString(row[9]);
        query = MySQLQuery.getAsString(row[10]);
        enumOpts = MySQLQuery.getAsString(row[11]);
        dspName = MySQLQuery.getAsString(row[12]);
        unique = MySQLQuery.getAsBoolean(row[13]);
        notes = MySQLQuery.getAsString(row[14]);
        place = MySQLQuery.getAsInteger(row[15]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "rpt_cube_fld";
    }

    public static String getSelFlds(String alias) {
        return new RptCubeFld().getSelFldsForAlias(alias);
    }

    public static List<RptCubeFld> getList(MySQLQuery q, Connection conn) throws Exception {
        return new RptCubeFld().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new RptCubeFld().deleteById(id, conn);
    }

    public static List<RptCubeFld> getAll(Connection conn) throws Exception {
        return new RptCubeFld().getAllList(conn);
    }

//fin zona de reemplazo
   
    public static List<RptCubeFld> getByCubeQuery(int cubeId, Connection conn) throws Exception {
        return getList(new MySQLQuery("SELECT " + RptCubeFld.getSelFlds("") + " FROM rpt_cube_fld WHERE cube_id = " + cubeId + " ORDER BY place"), conn);
    }

    public static List<RptCubeFld> getByRptQuery(int rptId, Connection conn) throws Exception {
        return getList(new MySQLQuery("SELECT " + RptCubeFld.getSelFlds("") + " FROM rpt_cube_fld WHERE cube_id = (SELECT cube_id FROM rpt_rpt WHERE id = " + rptId + ") ORDER BY place"), conn);
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

    public void setTables(List<RptCubeTbl> allTbls) {
        if (dataType != null) {
            type = DataType.getType(dataType);
        }
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

    public static RptCubeFld find(int id, List<RptCubeFld> raw) {
        for (RptCubeFld tbl : raw) {
            if (tbl.id == id) {
                return tbl;
            }
        }
        return null;
    }
}
