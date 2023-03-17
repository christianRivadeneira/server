package api.mto.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class MtoNoveltyType extends BaseModel<MtoNoveltyType> {
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
        return "mto_novelty_type";
    }

    public static String getSelFlds(String alias) {
        return new MtoNoveltyType().getSelFldsForAlias(alias);
    }

    public static List<MtoNoveltyType> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MtoNoveltyType().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MtoNoveltyType().deleteById(id, conn);
    }

    public static List<MtoNoveltyType> getAll(Connection conn) throws Exception {
        return new MtoNoveltyType().getAllList(conn);
    }

//fin zona de reemplazo
}