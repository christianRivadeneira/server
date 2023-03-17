package api.bill.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class BillNetBuildingType extends BaseModel<BillNetBuildingType> {
//inicio zona de reemplazo

    public String name;
    public boolean active;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "active"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, active);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        active = MySQLQuery.getAsBoolean(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_net_building_type";
    }

    public static String getSelFlds(String alias) {
        return new BillNetBuildingType().getSelFldsForAlias(alias);
    }

    public static List<BillNetBuildingType> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillNetBuildingType().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillNetBuildingType().deleteById(id, conn);
    }

    public static List<BillNetBuildingType> getAll(Connection conn) throws Exception {
        return new BillNetBuildingType().getAllList(conn);
    }

//fin zona de reemplazo
}