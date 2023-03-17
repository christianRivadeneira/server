package api.ord.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class OrdPollType extends BaseModel<OrdPollType> {
//inicio zona de reemplazo

    public String name;
    public int editable;
    public int place;
    public String kind;

    public static final int TYPE_CYL = 1;
    public static final int TYPE_TANK = 2;
    public static final int TYPE_REPAIR = 3;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "editable",
            "place",
            "kind"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, editable);
        q.setParam(3, place);
        q.setParam(4, kind);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        editable = MySQLQuery.getAsInteger(row[1]);
        place = MySQLQuery.getAsInteger(row[2]);
        kind = MySQLQuery.getAsString(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_poll_type";
    }

    public static String getSelFlds(String alias) {
        return new OrdPollType().getSelFldsForAlias(alias);
    }

    public static List<OrdPollType> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdPollType().getListFromQuery(q, conn);
    }

    public static List<OrdPollType> getList(Params p, Connection conn) throws Exception {
        return new OrdPollType().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdPollType().deleteById(id, conn);
    }

    public static List<OrdPollType> getAll(Connection conn) throws Exception {
        return new OrdPollType().getAllList(conn);
    }

//fin zona de reemplazo
    public static String getTypeQueryByPqr(int pqrId, int pqrType) {
        String table = (pqrType == TYPE_REPAIR ? "ord_repairs" : pqrType == TYPE_CYL ? "ord_pqr_cyl" : "ord_pqr_tank");

        return " SELECT v.ord_poll_type_id FROM " + table + " r "
                + " INNER JOIN ord_poll p ON p.id = r.pqr_poll_id "
                + " INNER JOIN ord_poll_version v ON v.id = p.poll_version_id "
                + " WHERE r.id = " + pqrId;
    }

}
