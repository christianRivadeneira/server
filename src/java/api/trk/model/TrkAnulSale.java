package api.trk.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class TrkAnulSale extends BaseModel<TrkAnulSale> {
//inicio zona de reemplazo

    public Integer trkSaleId;
    public Integer dtoSaleId;
    public Date anulDt;
    public int empId;
    public String notes;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "trk_sale_id",
            "dto_sale_id",
            "anul_dt",
            "emp_id",
            "notes"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, trkSaleId);
        q.setParam(2, dtoSaleId);
        q.setParam(3, anulDt);
        q.setParam(4, empId);
        q.setParam(5, notes);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        trkSaleId = MySQLQuery.getAsInteger(row[0]);
        dtoSaleId = MySQLQuery.getAsInteger(row[1]);
        anulDt = MySQLQuery.getAsDate(row[2]);
        empId = MySQLQuery.getAsInteger(row[3]);
        notes = MySQLQuery.getAsString(row[4]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "trk_anul_sale";
    }

    public static String getSelFlds(String alias) {
        return new TrkAnulSale().getSelFldsForAlias(alias);
    }

    public static List<TrkAnulSale> getList(MySQLQuery q, Connection conn) throws Exception {
        return new TrkAnulSale().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new TrkAnulSale().deleteById(id, conn);
    }

    public static List<TrkAnulSale> getAll(Connection conn) throws Exception {
        return new TrkAnulSale().getAllList(conn);
    }

//fin zona de reemplazo
}