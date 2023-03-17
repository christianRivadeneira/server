package api.com.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class ComMultiPromo extends BaseModel<ComMultiPromo> {
//inicio zona de reemplazo

    public String name;
    public String description;
    public Date startDate;
    public Date endDate;
    public boolean active;
    public int cylTypeId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "description",
            "start_date",
            "end_date",
            "active",
            "cyl_type_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, description);
        q.setParam(3, startDate);
        q.setParam(4, endDate);
        q.setParam(5, active);
        q.setParam(6, cylTypeId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        description = MySQLQuery.getAsString(row[1]);
        startDate = MySQLQuery.getAsDate(row[2]);
        endDate = MySQLQuery.getAsDate(row[3]);
        active = MySQLQuery.getAsBoolean(row[4]);
        cylTypeId = MySQLQuery.getAsInteger(row[5]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "com_multi_promo";
    }

    public static String getSelFlds(String alias) {
        return new ComMultiPromo().getSelFldsForAlias(alias);
    }

    public static List<ComMultiPromo> getList(MySQLQuery q, Connection conn) throws Exception {
        return new ComMultiPromo().getListFromQuery(q, conn);
    }

    public static List<ComMultiPromo> getList(Params p, Connection conn) throws Exception {
        return new ComMultiPromo().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new ComMultiPromo().deleteById(id, conn);
    }

    public static List<ComMultiPromo> getAll(Connection conn) throws Exception {
        return new ComMultiPromo().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<ComInstPromo> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}
