package //pack;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
//imports
public class //className extends BaseModel<//className> {
//inicio zona de reemplazo

//vars

    @Override
    protected String[] getFlds() {
        return new String[]{
//tblFlds
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
//setFlds
    }

    @Override
    public void setRow(Object[] row) throws Exception {
//readSelect
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "//tabName";
    }

    public static String getSelFlds(String alias) {
        return new //className().getSelFldsForAlias(alias);
    }

    public static List<//className> getList(MySQLQuery q, Connection conn) throws Exception {
        return new //className().getListFromQuery(q, conn);
    }

    public static List<//className> getList(Params p, Connection conn) throws Exception {
        return new //className().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new //className().deleteById(id, conn);
    }

    public static List<//className> getAll(Connection conn) throws Exception {
        return new //className().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<//className> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}