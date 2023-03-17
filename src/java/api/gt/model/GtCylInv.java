package api.gt.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class GtCylInv extends BaseModel<GtCylInv> {
//inicio zona de reemplazo

    public int tripId;
    public int capaId;
    public int typeId;
    public Integer amount;
    public String state;
    public String type;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "trip_id",
            "capa_id",
            "type_id",
            "amount",
            "state",
            "type"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, tripId);
        q.setParam(2, capaId);
        q.setParam(3, typeId);
        q.setParam(4, amount);
        q.setParam(5, state);
        q.setParam(6, type);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        tripId = MySQLQuery.getAsInteger(row[0]);
        capaId = MySQLQuery.getAsInteger(row[1]);
        typeId = MySQLQuery.getAsInteger(row[2]);
        amount = MySQLQuery.getAsInteger(row[3]);
        state = MySQLQuery.getAsString(row[4]);
        type = MySQLQuery.getAsString(row[5]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "gt_cyl_inv";
    }

    public static String getSelFlds(String alias) {
        return new GtCylInv().getSelFldsForAlias(alias);
    }

    public static List<GtCylInv> getList(MySQLQuery q, Connection conn) throws Exception {
        return new GtCylInv().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new GtCylInv().deleteById(id, conn);
    }

    public static List<GtCylInv> getAll(Connection conn) throws Exception {
        return new GtCylInv().getAllList(conn);
    }

//fin zona de reemplazo
    public static List<GtCylInv> getInv(int tripId, String type, Connection conn) throws Exception {
        return GtCylInv.getList(new MySQLQuery("SELECT "
                + GtCylInv.getSelFlds("i")
                + "FROM gt_cyl_inv AS i "
                + "WHERE i.trip_id = " + tripId + " AND i.type = ?1").setParam(1, type), conn);
    }

    public static GtCylInv getInv(int tripId, int cylTypeId, String type, Connection conn) throws Exception {
        return new GtCylInv().select(new MySQLQuery("SELECT "
                + GtCylInv.getSelFlds("i")
                + "FROM gt_cyl_inv AS i "
                + "WHERE i.trip_id = " + tripId + " "
                + "AND i.type = ?1 "
                + "AND capa_id = " + cylTypeId + "").setParam(1, type), conn);
    }

    public static GtCylInv getInv(int tripId, int cylTypeId, String type, String state, Connection conn) throws Exception {
        return new GtCylInv().select(new MySQLQuery("SELECT "
                + GtCylInv.getSelFlds("i")
                + "FROM gt_cyl_inv AS i "
                + "WHERE i.trip_id = " + tripId + " "
                + "AND i.type = ?1 "
                + "AND i.state = ?2 "
                + "AND capa_id = " + cylTypeId + "").setParam(1, type).setParam(2, state), conn);
    }

}
