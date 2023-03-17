package api.inv.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class InvProfCfg extends BaseModel<InvProfCfg> {

    public static final int MODULE_ID = 923;

//inicio zona de reemplazo
    public int profId;
    public boolean isSuperAdm;
    public boolean isAdm;
    public boolean isAgencyCheck;
    public boolean isReadOnly;
    public boolean appNif;
    public boolean appOutFactory;
    public boolean appQualityTest;
    public boolean appPv;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "prof_id",
            "is_super_adm",
            "is_adm",
            "is_agency_check",
            "is_read_only",
            "app_nif",
            "app_out_factory",
            "app_quality_test",
            "app_pv"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, profId);
        q.setParam(2, isSuperAdm);
        q.setParam(3, isAdm);
        q.setParam(4, isAgencyCheck);
        q.setParam(5, isReadOnly);
        q.setParam(6, appNif);
        q.setParam(7, appOutFactory);
        q.setParam(8, appQualityTest);
        q.setParam(9, appPv);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        profId = MySQLQuery.getAsInteger(row[0]);
        isSuperAdm = MySQLQuery.getAsBoolean(row[1]);
        isAdm = MySQLQuery.getAsBoolean(row[2]);
        isAgencyCheck = MySQLQuery.getAsBoolean(row[3]);
        isReadOnly = MySQLQuery.getAsBoolean(row[4]);
        appNif = MySQLQuery.getAsBoolean(row[5]);
        appOutFactory = MySQLQuery.getAsBoolean(row[6]);
        appQualityTest = MySQLQuery.getAsBoolean(row[7]);
        appPv = MySQLQuery.getAsBoolean(row[8]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "inv_prof_cfg";
    }

    public static String getSelFlds(String alias) {
        return new InvProfCfg().getSelFldsForAlias(alias);
    }

    public static List<InvProfCfg> getList(MySQLQuery q, Connection conn) throws Exception {
        return new InvProfCfg().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new InvProfCfg().deleteById(id, conn);
    }

    public static List<InvProfCfg> getAll(Connection conn) throws Exception {
        return new InvProfCfg().getAllList(conn);
    }

//fin zona de reemplazo
    public static InvProfCfg getFromRow(Object[] row) throws Exception {
        InvProfCfg obj = new InvProfCfg();
        obj.profId = MySQLQuery.getAsInteger(row[0]);
        obj.isSuperAdm = MySQLQuery.getAsBoolean(row[1]);
        obj.isAdm = MySQLQuery.getAsBoolean(row[2]);
        obj.isAgencyCheck = MySQLQuery.getAsBoolean(row[3]);
        obj.isReadOnly = MySQLQuery.getAsBoolean(row[4]);
        obj.appNif = MySQLQuery.getAsBoolean(row[5]);
        obj.appOutFactory = MySQLQuery.getAsBoolean(row[6]);
        obj.appQualityTest = MySQLQuery.getAsBoolean(row[7]);
        obj.appPv = MySQLQuery.getAsBoolean(row[8]);
        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

    public static String getFromProfileQuery(int profileId) throws Exception {
        return "SELECT " + getSelFlds("") + ", id FROM inv_prof_cfg WHERE prof_id = " + profileId;
    }
}
