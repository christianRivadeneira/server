package api.ord.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class OrdProfCfg extends BaseModel<OrdProfCfg> {
//inicio zona de reemplazo

    public int profId;
    public boolean asignTechPqr;
    public boolean allOffices;
    public boolean hasPollPerm;
    public boolean hasPnlTickets;
    public boolean hasChkList;
    public boolean hasPnlRepairs;
    public boolean hasPnlAfilPqrs;
    public boolean hasPnlPqrsOther;
    public boolean hasPnlPqrsOtherClc;
    public boolean hasPnlCom;
    public boolean hasPnlPqrsTank;
    public boolean hasPnlPqrsCyl;
    public boolean hasPnlOrdersTank;
    public boolean hasPnlOrdersCyl;
    public boolean cancel;
    public boolean editPqrsTanks;
    public boolean editPqrsCyl;
    public boolean editPqrsOther;
    public boolean editRepairs;
    public boolean editPqrsAfil;
    public boolean allowEdit;
    public boolean authAllOffs;
    public boolean hasPnlStore;
    public boolean hasPnlAlerts;
    public boolean clcProfileMode;
    public boolean clcProfileMode1;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "prof_id",
            "asign_tech_pqr",
            "all_offices",
            "has_poll_perm",
            "has_pnl_tickets",
            "has_chk_list",
            "has_pnl_repairs",
            "has_pnl_afil_pqrs",
            "has_pnl_pqrs_other",
            "has_pnl_pqrs_other_clc",
            "has_pnl_com",
            "has_pnl_pqrs_tank",
            "has_pnl_pqrs_cyl",
            "has_pnl_orders_tank",
            "has_pnl_orders_cyl",
            "cancel",
            "edit_pqrs_tanks",
            "edit_pqrs_cyl",
            "edit_pqrs_other",
            "edit_repairs",
            "edit_pqrs_afil",
            "allow_edit",
            "auth_all_offs",
            "has_pnl_store",
            "has_pnl_alerts",
            "clc_profile_mode",
            "clc_profile_mode_1"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, profId);
        q.setParam(2, asignTechPqr);
        q.setParam(3, allOffices);
        q.setParam(4, hasPollPerm);
        q.setParam(5, hasPnlTickets);
        q.setParam(6, hasChkList);
        q.setParam(7, hasPnlRepairs);
        q.setParam(8, hasPnlAfilPqrs);
        q.setParam(9, hasPnlPqrsOther);
        q.setParam(10, hasPnlPqrsOtherClc);
        q.setParam(11, hasPnlCom);
        q.setParam(12, hasPnlPqrsTank);
        q.setParam(13, hasPnlPqrsCyl);
        q.setParam(14, hasPnlOrdersTank);
        q.setParam(15, hasPnlOrdersCyl);
        q.setParam(16, cancel);
        q.setParam(17, editPqrsTanks);
        q.setParam(18, editPqrsCyl);
        q.setParam(19, editPqrsOther);
        q.setParam(20, editRepairs);
        q.setParam(21, editPqrsAfil);
        q.setParam(22, allowEdit);
        q.setParam(23, authAllOffs);
        q.setParam(24, hasPnlStore);
        q.setParam(25, hasPnlAlerts);
        q.setParam(26, clcProfileMode);
        q.setParam(27, clcProfileMode1);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        profId = MySQLQuery.getAsInteger(row[0]);
        asignTechPqr = MySQLQuery.getAsBoolean(row[1]);
        allOffices = MySQLQuery.getAsBoolean(row[2]);
        hasPollPerm = MySQLQuery.getAsBoolean(row[3]);
        hasPnlTickets = MySQLQuery.getAsBoolean(row[4]);
        hasChkList = MySQLQuery.getAsBoolean(row[5]);
        hasPnlRepairs = MySQLQuery.getAsBoolean(row[6]);
        hasPnlAfilPqrs = MySQLQuery.getAsBoolean(row[7]);
        hasPnlPqrsOther = MySQLQuery.getAsBoolean(row[8]);
        hasPnlPqrsOtherClc = MySQLQuery.getAsBoolean(row[9]);
        hasPnlCom = MySQLQuery.getAsBoolean(row[10]);
        hasPnlPqrsTank = MySQLQuery.getAsBoolean(row[11]);
        hasPnlPqrsCyl = MySQLQuery.getAsBoolean(row[12]);
        hasPnlOrdersTank = MySQLQuery.getAsBoolean(row[13]);
        hasPnlOrdersCyl = MySQLQuery.getAsBoolean(row[14]);
        cancel = MySQLQuery.getAsBoolean(row[15]);
        editPqrsTanks = MySQLQuery.getAsBoolean(row[16]);
        editPqrsCyl = MySQLQuery.getAsBoolean(row[17]);
        editPqrsOther = MySQLQuery.getAsBoolean(row[18]);
        editRepairs = MySQLQuery.getAsBoolean(row[19]);
        editPqrsAfil = MySQLQuery.getAsBoolean(row[20]);
        allowEdit = MySQLQuery.getAsBoolean(row[21]);
        authAllOffs = MySQLQuery.getAsBoolean(row[22]);
        hasPnlStore = MySQLQuery.getAsBoolean(row[23]);
        hasPnlAlerts = MySQLQuery.getAsBoolean(row[24]);
        clcProfileMode = MySQLQuery.getAsBoolean(row[25]);
        clcProfileMode1 = MySQLQuery.getAsBoolean(row[26]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_prof_cfg";
    }

    public static String getSelFlds(String alias) {
        return new OrdProfCfg().getSelFldsForAlias(alias);
    }

    public static List<OrdProfCfg> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdProfCfg().getListFromQuery(q, conn);
    }

    public static List<OrdProfCfg> getList(Params p, Connection conn) throws Exception {
        return new OrdProfCfg().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdProfCfg().deleteById(id, conn);
    }

    public static List<OrdProfCfg> getAll(Connection conn) throws Exception {
        return new OrdProfCfg().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<OrdProfCfg> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}