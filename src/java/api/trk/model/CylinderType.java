package api.trk.model;

import api.BaseModel;
import api.Params;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class CylinderType extends BaseModel<CylinderType> {
//inicio zona de reemplazo

    public String name;
    public BigDecimal capacity;
    public Integer kg;
    public Integer lb;
    public boolean pref;
    public String type;
    public BigDecimal tara;
    public int place;
    public String accCode;
    public String accCodeCyl;
    public String suiRptCode;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "capacity",
            "kg",
            "lb",
            "pref",
            "type",
            "tara",
            "place",
            "acc_code",
            "acc_code_cyl",
            "sui_rpt_code"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, capacity);
        q.setParam(3, kg);
        q.setParam(4, lb);
        q.setParam(5, pref);
        q.setParam(6, type);
        q.setParam(7, tara);
        q.setParam(8, place);
        q.setParam(9, accCode);
        q.setParam(10, accCodeCyl);
        q.setParam(11, suiRptCode);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        capacity = MySQLQuery.getAsBigDecimal(row[1], false);
        kg = MySQLQuery.getAsInteger(row[2]);
        lb = MySQLQuery.getAsInteger(row[3]);
        pref = MySQLQuery.getAsBoolean(row[4]);
        type = MySQLQuery.getAsString(row[5]);
        tara = MySQLQuery.getAsBigDecimal(row[6], false);
        place = MySQLQuery.getAsInteger(row[7]);
        accCode = MySQLQuery.getAsString(row[8]);
        accCodeCyl = MySQLQuery.getAsString(row[9]);
        suiRptCode = MySQLQuery.getAsString(row[10]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "cylinder_type";
    }

    public static String getSelFlds(String alias) {
        return new CylinderType().getSelFldsForAlias(alias);
    }

    public static List<CylinderType> getList(MySQLQuery q, Connection conn) throws Exception {
        return new CylinderType().getListFromQuery(q, conn);
    }

    public static List<CylinderType> getList(Params p, Connection conn) throws Exception {
        return new CylinderType().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new CylinderType().deleteById(id, conn);
    }

    public static List<CylinderType> getAll(Connection conn) throws Exception {
        return new CylinderType().getAllList(conn);
    }

//fin zona de reemplazo
    public static CylinderType find(List<CylinderType> types, int typeId) {
        for (int i = 0; i < types.size(); i++) {
            CylinderType ct = types.get(i);
            if (ct.id == typeId) {
                return ct;
            }
        }
        return null;
    }
}
