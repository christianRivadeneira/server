package api.chl.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class ChlItem extends BaseModel<ChlItem> {
//inicio zona de reemplazo

    public int provId;
    public String refsys;
    public String refprov;
    public String brand;
    public String notes;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "prov_id",
            "refsys",
            "refprov",
            "brand",
            "notes"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, provId);
        q.setParam(2, refsys);
        q.setParam(3, refprov);
        q.setParam(4, brand);
        q.setParam(5, notes);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        provId = MySQLQuery.getAsInteger(row[0]);
        refsys = MySQLQuery.getAsString(row[1]);
        refprov = MySQLQuery.getAsString(row[2]);
        brand = MySQLQuery.getAsString(row[3]);
        notes = MySQLQuery.getAsString(row[4]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "chl_item";
    }

    public static String getSelFlds(String alias) {
        return new ChlItem().getSelFldsForAlias(alias);
    }

    public static List<ChlItem> getList(MySQLQuery q, Connection conn) throws Exception {
        return new ChlItem().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new ChlItem().deleteById(id, conn);
    }

    public static List<ChlItem> getAll(Connection conn) throws Exception {
        return new ChlItem().getAllList(conn);
    }

//fin zona de reemplazo

}
