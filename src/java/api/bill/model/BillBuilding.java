package api.bill.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class BillBuilding extends BaseModel<BillBuilding> {
//inicio zona de reemplazo

    public String name;
    public String address;
    public Integer oldId;
    public int tankClientTypeId;
    public String phones;
    public boolean checkedCoords;
    public Integer buildTypeId;
    public boolean active;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "address",
            "old_id",
            "tank_client_type_id",
            "phones",
            "checked_coords",
            "build_type_id",
            "active"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, address);
        q.setParam(3, oldId);
        q.setParam(4, tankClientTypeId);
        q.setParam(5, phones);
        q.setParam(6, checkedCoords);
        q.setParam(7, buildTypeId);
        q.setParam(8, active);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        address = MySQLQuery.getAsString(row[1]);
        oldId = MySQLQuery.getAsInteger(row[2]);
        tankClientTypeId = MySQLQuery.getAsInteger(row[3]);
        phones = MySQLQuery.getAsString(row[4]);
        checkedCoords = MySQLQuery.getAsBoolean(row[5]);
        buildTypeId = MySQLQuery.getAsInteger(row[6]);
        active = MySQLQuery.getAsBoolean(row[7]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_building";
    }

    public static String getSelFlds(String alias) {
        return new BillBuilding().getSelFldsForAlias(alias);
    }

    public static List<BillBuilding> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillBuilding().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillBuilding().deleteById(id, conn);
    }

    public static List<BillBuilding> getAll(Connection conn) throws Exception {
        return new BillBuilding().getAllList(conn);
    }

//fin zona de reemplazo
    public static List<BillBuilding> getAllBuildings(Connection conn) throws Exception {
        MySQLQuery mq = new MySQLQuery("SELECT " + getSelFlds("") + ", id FROM bill_building ORDER BY old_id");
        return getList(mq, conn);
    }
}
