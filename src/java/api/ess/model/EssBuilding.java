package api.ess.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class EssBuilding extends BaseModel<EssBuilding> {
//inicio zona de reemplazo

    public String name;
    public String address;
    public String phone;
    public int towers;
    public boolean active;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "address",
            "phone",
            "towers",
            "active"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, address);
        q.setParam(3, phone);
        q.setParam(4, towers);
        q.setParam(5, active);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        address = MySQLQuery.getAsString(row[1]);
        phone = MySQLQuery.getAsString(row[2]);
        towers = MySQLQuery.getAsInteger(row[3]);
        active = MySQLQuery.getAsBoolean(row[4]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ess_building";
    }

    public static String getSelFlds(String alias) {
        return new EssBuilding().getSelFldsForAlias(alias);
    }

    public static List<EssBuilding> getList(MySQLQuery q, Connection conn) throws Exception {
        return new EssBuilding().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new EssBuilding().deleteById(id, conn);
    }

    public static List<EssBuilding> getAll(Connection conn) throws Exception {
        return new EssBuilding().getAllList(conn);
    }

//fin zona de reemplazo
}
