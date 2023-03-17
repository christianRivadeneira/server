package api.ess.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class EssCfg extends BaseModel<EssCfg> {
//inicio zona de reemplazo

    public boolean show;
    public String contactPhone;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "show",
            "contact_phone"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, show);
        q.setParam(2, contactPhone);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        show = MySQLQuery.getAsBoolean(row[0]);
        contactPhone = MySQLQuery.getAsString(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ess_cfg";
    }

    public static String getSelFlds(String alias) {
        return new EssCfg().getSelFldsForAlias(alias);
    }

    public static List<EssCfg> getList(MySQLQuery q, Connection conn) throws Exception {
        return new EssCfg().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new EssCfg().deleteById(id, conn);
    }

    public static List<EssCfg> getAll(Connection conn) throws Exception {
        return new EssCfg().getAllList(conn);
    }

//fin zona de reemplazo
}