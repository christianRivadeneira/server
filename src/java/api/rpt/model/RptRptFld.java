package api.rpt.model;

import api.rpt.api.BaseFilter;
import api.rpt.api.dataTypes.DataType;
import api.rpt.api.operations.Operation;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import utilities.MySQLQuery;

public class RptRptFld extends BaseFilter {
//inicio zona de reemplazo

    public Integer fldId;
    public int rptId;
    public String type;
    public String oper;
    public String sort;
    public String filtType;
    public String filtJson;
    public String filtDesc;
    public Integer dashFiltId;
    public String fx;
    public String fxName;
    public String kpiName;
    public Integer kpiL1Id;
    public Integer kpiL2Id;
    public Integer kpiValId;
    public BigDecimal kpiL1Kte;
    public BigDecimal kpiL2Kte;
    public String kpiType;
    public int place;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "fld_id",
            "rpt_id",
            "type",
            "oper",
            "sort",
            "filt_type",
            "filt_json",
            "filt_desc",
            "dash_filt_id",
            "fx",
            "fx_name",
            "kpi_name",
            "kpi_l1_id",
            "kpi_l2_id",
            "kpi_val_id",
            "kpi_l1_kte",
            "kpi_l2_kte",
            "kpi_type",
            "place"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, fldId);
        q.setParam(2, rptId);
        q.setParam(3, type);
        q.setParam(4, oper);
        q.setParam(5, sort);
        q.setParam(6, filtType);
        q.setParam(7, filtJson);
        q.setParam(8, filtDesc);
        q.setParam(9, dashFiltId);
        q.setParam(10, fx);
        q.setParam(11, fxName);
        q.setParam(12, kpiName);
        q.setParam(13, kpiL1Id);
        q.setParam(14, kpiL2Id);
        q.setParam(15, kpiValId);
        q.setParam(16, kpiL1Kte);
        q.setParam(17, kpiL2Kte);
        q.setParam(18, kpiType);
        q.setParam(19, place);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        fldId = MySQLQuery.getAsInteger(row[0]);
        rptId = MySQLQuery.getAsInteger(row[1]);
        type = MySQLQuery.getAsString(row[2]);
        oper = MySQLQuery.getAsString(row[3]);
        sort = MySQLQuery.getAsString(row[4]);
        filtType = MySQLQuery.getAsString(row[5]);
        filtJson = MySQLQuery.getAsString(row[6]);
        filtDesc = MySQLQuery.getAsString(row[7]);
        dashFiltId = MySQLQuery.getAsInteger(row[8]);
        fx = MySQLQuery.getAsString(row[9]);
        fxName = MySQLQuery.getAsString(row[10]);
        kpiName = MySQLQuery.getAsString(row[11]);
        kpiL1Id = MySQLQuery.getAsInteger(row[12]);
        kpiL2Id = MySQLQuery.getAsInteger(row[13]);
        kpiValId = MySQLQuery.getAsInteger(row[14]);
        kpiL1Kte = MySQLQuery.getAsBigDecimal(row[15], false);
        kpiL2Kte = MySQLQuery.getAsBigDecimal(row[16], false);
        kpiType = MySQLQuery.getAsString(row[17]);
        place = MySQLQuery.getAsInteger(row[18]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "rpt_rpt_fld";
    }

    public static String getSelFlds(String alias) {
        return new RptRptFld().getSelFldsForAlias(alias);
    }

    public static List<RptRptFld> getList(MySQLQuery q, Connection conn) throws Exception {
        return new RptRptFld().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new RptRptFld().deleteById(id, conn);
    }

    public static List<RptRptFld> getAll(Connection conn) throws Exception {
        return new RptRptFld().getAllList(conn);
    }

//fin zona de reemplazo
    
    public static List<RptRptFld> getByRptQuery(int rptId, Connection conn) throws Exception {
        return RptRptFld.getList(new MySQLQuery("SELECT " + RptRptFld.getSelFlds("") + " FROM rpt_rpt_fld WHERE rpt_id = ?1").setParam(1, rptId), conn);
    }

    public static List<RptRptFld> getByRptQuery(int rptId, String type, Connection conn) throws Exception {
        return RptRptFld.getList(new MySQLQuery("SELECT " + RptRptFld.getSelFlds("") + " FROM rpt_rpt_fld WHERE rpt_id = ?1 AND type = ?2").setParam(1, rptId).setParam(2, type), conn);
    }

    public static List<RptRptFld> findFlds(List<RptRptFld> flds, String type) {
        List<RptRptFld> rta = new ArrayList<>();
        for (RptRptFld fld : flds) {
            if (fld.type.equals(type)) {
                rta.add(fld);
            }
        }

        Collections.sort(rta, new Comparator<RptRptFld>() {
            @Override
            public int compare(RptRptFld o1, RptRptFld o2) {
                int c1 = o2.type.compareTo(o1.type);
                if (c1 == 0) {
                    return Integer.compare(o1.place, o2.place);
                } else {
                    return c1;
                }
            }

        });
        return rta;
    }


    @Override
    public String getFiltType() {
        return filtType;
    }

    @Override
    public void setFiltType(String filtType) {
        this.filtType = filtType;
    }

    @Override
    public String getFiltDesc() {
        return filtDesc;
    }

    @Override
    public void setFiltDesc(String filtDesc) {
        this.filtDesc = filtDesc;
    }

    @Override
    public int getId() {
        return id;
    }

    public static RptRptFld find(int id, List<RptRptFld> flds) {
        for (RptRptFld fld : flds) {
            if (fld.id == id) {
                return fld;
            }
        }
        return null;
    }

    public static String getDataType(RptRptFld join) {
        String oper = join.oper;
        String type = join.getCubeFld().dataType;
        return Operation.getOper(oper).getResultType(DataType.getType(type)).getName();
    }

    private static Map<String, String> operNames;

    static {
        operNames = new HashMap<>();
        for (Operation lOper : Operation.getAll()) {
            operNames.put(lOper.getName(), lOper.getLabel());
        }
    }

    public String getJoinDesc() {
        if (filtType == null) {
            if (fxName != null) {
                return fxName;
            } else if (kpiName != null) {
                return kpiName;
            } else {
                return operNames.get(oper) + " " + getCubeFld().dspName;
            }
        } else {
            //tiene un condicional
            return getCubeFld().dspName + " " + filtDesc;
        }
    }

    @Override
    public String getBinFilt() {
        return filtJson;
    }

    @Override
    public void setBinFilt(String bin) {
        this.filtJson = bin;
    }
}
