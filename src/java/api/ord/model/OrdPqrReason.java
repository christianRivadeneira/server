package api.ord.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class OrdPqrReason extends BaseModel<OrdPqrReason> {
//inicio zona de reemplazo

    public String type;
    public String description;
    public Integer causeSuiId;
    public Integer suiNetTypeId;
    public boolean saleData;
    public boolean active;
    public boolean poll;
    public Integer supreasonId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "type",
            "description",
            "cause_sui_id",
            "sui_net_type_id",
            "sale_data",
            "active",
            "poll",
            "supreason_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, type);
        q.setParam(2, description);
        q.setParam(3, causeSuiId);
        q.setParam(4, suiNetTypeId);
        q.setParam(5, saleData);
        q.setParam(6, active);
        q.setParam(7, poll);
        q.setParam(8, supreasonId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        type = MySQLQuery.getAsString(row[0]);
        description = MySQLQuery.getAsString(row[1]);
        causeSuiId = MySQLQuery.getAsInteger(row[2]);
        suiNetTypeId = MySQLQuery.getAsInteger(row[3]);
        saleData = MySQLQuery.getAsBoolean(row[4]);
        active = MySQLQuery.getAsBoolean(row[5]);
        poll = MySQLQuery.getAsBoolean(row[6]);
        supreasonId = MySQLQuery.getAsInteger(row[7]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_pqr_reason";
    }

    public static String getSelFlds(String alias) {
        return new OrdPqrReason().getSelFldsForAlias(alias);
    }

    public static List<OrdPqrReason> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdPqrReason().getListFromQuery(q, conn);
    }

    public static List<OrdPqrReason> getList(Params p, Connection conn) throws Exception {
        return new OrdPqrReason().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdPqrReason().deleteById(id, conn);
    }

    public static List<OrdPqrReason> getAll(Connection conn) throws Exception {
        return new OrdPqrReason().getAllList(conn);
    }

//fin zona de reemplazo

}
