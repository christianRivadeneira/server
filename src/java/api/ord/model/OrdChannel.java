package api.ord.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class OrdChannel extends BaseModel<OrdChannel> {
//inicio zona de reemplazo

    public String name;
    public boolean defOp;
    public boolean comApp;
    public String netSui;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "def_op",
            "com_app",
            "net_sui"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, defOp);
        q.setParam(3, comApp);
        q.setParam(4, netSui);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        defOp = MySQLQuery.getAsBoolean(row[1]);
        comApp = MySQLQuery.getAsBoolean(row[2]);
        netSui = MySQLQuery.getAsString(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_channel";
    }

    public static String getSelFlds(String alias) {
        return new OrdChannel().getSelFldsForAlias(alias);
    }

    public static List<OrdChannel> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdChannel().getListFromQuery(q, conn);
    }

    public static List<OrdChannel> getList(Params p, Connection conn) throws Exception {
        return new OrdChannel().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdChannel().deleteById(id, conn);
    }

    public static List<OrdChannel> getAll(Connection conn) throws Exception {
        return new OrdChannel().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<OrdChannel> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}