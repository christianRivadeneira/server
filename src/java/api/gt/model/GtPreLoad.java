package api.gt.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class GtPreLoad extends BaseModel<GtPreLoad> {

//Fuera de la zona de reemplazo
    public Integer tripId;
    public List<GtPreLoadInv> load;
    public String vehicle;
//---------------------------------------------------    

//inicio zona de reemplazo

    public Date dt;
    public Integer vhId;
    public int creatorId;
    public int centerId;
    public boolean available;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "dt",
            "vh_id",
            "creator_id",
            "center_id",
            "available"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, dt);
        q.setParam(2, vhId);
        q.setParam(3, creatorId);
        q.setParam(4, centerId);
        q.setParam(5, available);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        dt = MySQLQuery.getAsDate(row[0]);
        vhId = MySQLQuery.getAsInteger(row[1]);
        creatorId = MySQLQuery.getAsInteger(row[2]);
        centerId = MySQLQuery.getAsInteger(row[3]);
        available = MySQLQuery.getAsBoolean(row[4]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "gt_pre_load";
    }

    public static String getSelFlds(String alias) {
        return new GtPreLoad().getSelFldsForAlias(alias);
    }

    public static List<GtPreLoad> getList(MySQLQuery q, Connection conn) throws Exception {
        return new GtPreLoad().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new GtPreLoad().deleteById(id, conn);
    }

    public static List<GtPreLoad> getAll(Connection conn) throws Exception {
        return new GtPreLoad().getAllList(conn);
    }

    //fin zona de reemplazo
    
    public GtPreLoad() {}
    
    public GtPreLoad(Object[] row) {
        dt = MySQLQuery.getAsDate(row[0]);
        vhId = MySQLQuery.getAsInteger(row[1]);
        creatorId = MySQLQuery.getAsInteger(row[2]);
        centerId = MySQLQuery.getAsInteger(row[3]);
        available = MySQLQuery.getAsBoolean(row[4]);
        id = MySQLQuery.getAsInteger(row[5]);
        vehicle = MySQLQuery.getAsString(row[6]);
    }
}