package api.per.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class PerEmployeerType extends BaseModel<PerEmployeerType> {
//inicio zona de reemplazo

    public String name;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "per_employeer_type";
    }

    public static String getSelFlds(String alias) {
        return new PerEmployeerType().getSelFldsForAlias(alias);
    }

    public static List<PerEmployeerType> getList(MySQLQuery q, Connection conn) throws Exception {
        return new PerEmployeerType().getListFromQuery(q, conn);
    }

    public static List<PerEmployeerType> getList(Params p, Connection conn) throws Exception {
        return new PerEmployeerType().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new PerEmployeerType().deleteById(id, conn);
    }

    public static List<PerEmployeerType> getAll(Connection conn) throws Exception {
        return new PerEmployeerType().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<PerEmployeerType> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}