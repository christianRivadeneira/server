package api.est.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class EstProgSede extends BaseModel<EstProgSede> {
//inicio zona de reemplazo

    public int tankClientId;
    public int progId;
    public int place;
    public Integer orderTankId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "tank_client_id",
            "prog_id",
            "place",
            "order_tank_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, tankClientId);
        q.setParam(2, progId);
        q.setParam(3, place);
        q.setParam(4, orderTankId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        tankClientId = MySQLQuery.getAsInteger(row[0]);
        progId = MySQLQuery.getAsInteger(row[1]);
        place = MySQLQuery.getAsInteger(row[2]);
        orderTankId = MySQLQuery.getAsInteger(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "est_prog_sede";
    }

    public static String getSelFlds(String alias) {
        return new EstProgSede().getSelFldsForAlias(alias);
    }

    public static List<EstProgSede> getList(MySQLQuery q, Connection conn) throws Exception {
        return new EstProgSede().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new EstProgSede().deleteById(id, conn);
    }

    public static List<EstProgSede> getAll(Connection conn) throws Exception {
        return new EstProgSede().getAllList(conn);
    }

//fin zona de reemplazo

}
