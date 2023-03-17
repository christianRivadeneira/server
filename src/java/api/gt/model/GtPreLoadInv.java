package api.gt.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class GtPreLoadInv extends BaseModel<GtPreLoadInv> {

    //Fuera de la zona de reemplazo
    public String capaName;
    public int scnAmount;
    //---------------------------------------------------

    //inicio zona de reemplazo

    public int preLoadId;
    public int capaId;
    public int amount;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "pre_load_id",
            "capa_id",
            "amount"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, preLoadId);
        q.setParam(2, capaId);
        q.setParam(3, amount);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        preLoadId = MySQLQuery.getAsInteger(row[0]);
        capaId = MySQLQuery.getAsInteger(row[1]);
        amount = MySQLQuery.getAsInteger(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }
    

    @Override
    protected String getTblName() {
        return "gt_pre_load_inv";
    }

    public static String getSelFlds(String alias) {
        return new GtPreLoadInv().getSelFldsForAlias(alias);
    }

    public static List<GtPreLoadInv> getList(MySQLQuery q, Connection conn) throws Exception {
        return new GtPreLoadInv().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new GtPreLoadInv().deleteById(id, conn);
    }

    public static List<GtPreLoadInv> getAll(Connection conn) throws Exception {
        return new GtPreLoadInv().getAllList(conn);
    }

    //fin zona de reemplazo
    
    public GtPreLoadInv(){}
    
    public GtPreLoadInv(Object[] row) {
        preLoadId = MySQLQuery.getAsInteger(row[0]);
        capaId = MySQLQuery.getAsInteger(row[1]);
        amount = MySQLQuery.getAsInteger(row[2]);
        id = MySQLQuery.getAsInteger(row[3]);
        capaName = MySQLQuery.getAsString(row[4]);
        scnAmount = MySQLQuery.getAsInteger(row[5]);
    }
    
    public static GtCylInv getInv(int preLoadId, int cylTypeId, Connection conn) throws Exception {
        return new GtCylInv().select(new MySQLQuery("SELECT "
                + GtCylInv.getSelFlds("i")
                + "FROM gt_pre_load_inv AS i "
                + "WHERE i.pre_load_id = " + preLoadId + " AND capa_id = " + cylTypeId + ""), conn);
    }
}