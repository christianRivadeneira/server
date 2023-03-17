package api.com.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class ComStoreOrder extends BaseModel<ComStoreOrder> {
    public List<PvOrderInv> load;  
    
//inicio zona de reemplazo

    public int storeId;
    public Date takenDt;
    public Integer takenById;
    public Date assignDt;
    public Date progDt;
    public Integer assignById;
    public Date confirmDt;
    public Integer confirmById;
    public Date cancelDt;
    public Integer cancelById;
    public Integer cancelId;
    public Integer pvSaleId;
    public Integer vhId;
    public String notes;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "store_id",
            "taken_dt",
            "taken_by_id",
            "assign_dt",
            "prog_dt",
            "assign_by_id",
            "confirm_dt",
            "confirm_by_id",
            "cancel_dt",
            "cancel_by_id",
            "cancel_id",
            "pv_sale_id",
            "vh_id",
            "notes"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, storeId);
        q.setParam(2, takenDt);
        q.setParam(3, takenById);
        q.setParam(4, assignDt);
        q.setParam(5, progDt);
        q.setParam(6, assignById);
        q.setParam(7, confirmDt);
        q.setParam(8, confirmById);
        q.setParam(9, cancelDt);
        q.setParam(10, cancelById);
        q.setParam(11, cancelId);
        q.setParam(12, pvSaleId);
        q.setParam(13, vhId);
        q.setParam(14, notes);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        storeId = MySQLQuery.getAsInteger(row[0]);
        takenDt = MySQLQuery.getAsDate(row[1]);
        takenById = MySQLQuery.getAsInteger(row[2]);
        assignDt = MySQLQuery.getAsDate(row[3]);
        progDt = MySQLQuery.getAsDate(row[4]);
        assignById = MySQLQuery.getAsInteger(row[5]);
        confirmDt = MySQLQuery.getAsDate(row[6]);
        confirmById = MySQLQuery.getAsInteger(row[7]);
        cancelDt = MySQLQuery.getAsDate(row[8]);
        cancelById = MySQLQuery.getAsInteger(row[9]);
        cancelId = MySQLQuery.getAsInteger(row[10]);
        pvSaleId = MySQLQuery.getAsInteger(row[11]);
        vhId = MySQLQuery.getAsInteger(row[12]);
        notes = MySQLQuery.getAsString(row[13]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "com_store_order";
    }

    public static String getSelFlds(String alias) {
        return new ComStoreOrder().getSelFldsForAlias(alias);
    }

    public static List<ComStoreOrder> getList(MySQLQuery q, Connection conn) throws Exception {
        return new ComStoreOrder().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new ComStoreOrder().deleteById(id, conn);
    }

    public static List<ComStoreOrder> getAll(Connection conn) throws Exception {
        return new ComStoreOrder().getAllList(conn);
    }

//fin zona de reemplazo

}
