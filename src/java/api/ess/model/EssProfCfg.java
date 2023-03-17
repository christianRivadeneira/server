package api.ess.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class EssProfCfg extends BaseModel<EssProfCfg> {
//inicio zona de reemplazo

    public int profId;
    public boolean pnlMain;
    public boolean pnlPqrs;
    public boolean isSuperAdmin;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "prof_id",
            "pnl_main",
            "pnl_pqrs",
            "is_super_admin"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, profId);
        q.setParam(2, pnlMain);
        q.setParam(3, pnlPqrs);
        q.setParam(4, isSuperAdmin);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        profId = MySQLQuery.getAsInteger(row[0]);
        pnlMain = MySQLQuery.getAsBoolean(row[1]);
        pnlPqrs = MySQLQuery.getAsBoolean(row[2]);
        isSuperAdmin = MySQLQuery.getAsBoolean(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ess_prof_cfg";
    }

    public static String getSelFlds(String alias) {
        return new EssProfCfg().getSelFldsForAlias(alias);
    }

    public static List<EssProfCfg> getList(MySQLQuery q, Connection conn) throws Exception {
        return new EssProfCfg().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new EssProfCfg().deleteById(id, conn);
    }

    public static List<EssProfCfg> getAll(Connection conn) throws Exception {
        return new EssProfCfg().getAllList(conn);
    }

//fin zona de reemplazo
}