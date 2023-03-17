package api.ess.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class EssEnterprise extends BaseModel<EssEnterprise> {
//inicio zona de reemplazo

    public String name;
    public String address;
    public String phones;
    public boolean active;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "address",
            "phones",
            "active"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, address);
        q.setParam(3, phones);
        q.setParam(4, active);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        address = MySQLQuery.getAsString(row[1]);
        phones = MySQLQuery.getAsString(row[2]);
        active = MySQLQuery.getAsBoolean(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ess_enterprise";
    }

    public static String getSelFlds(String alias) {
        return new EssEnterprise().getSelFldsForAlias(alias);
    }

    public static List<EssEnterprise> getList(MySQLQuery q, Connection conn) throws Exception {
        return new EssEnterprise().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new EssEnterprise().deleteById(id, conn);
    }

    public static List<EssEnterprise> getAll(Connection conn) throws Exception {
        return new EssEnterprise().getAllList(conn);
    }

//fin zona de reemplazo
}
