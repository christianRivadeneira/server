package api.sys.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class Enterprise extends BaseModel<Enterprise> {
//inicio zona de reemplazo

    public String name;
    public String nit;
    public String shortName;
    public String accCode;
    public boolean alternative;
    public String address;
    public String phones;
    public boolean active;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "nit",
            "short_name",
            "acc_code",
            "alternative",
            "address",
            "phones",
            "active"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, nit);
        q.setParam(3, shortName);
        q.setParam(4, accCode);
        q.setParam(5, alternative);
        q.setParam(6, address);
        q.setParam(7, phones);
        q.setParam(8, active);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        nit = MySQLQuery.getAsString(row[1]);
        shortName = MySQLQuery.getAsString(row[2]);
        accCode = MySQLQuery.getAsString(row[3]);
        alternative = MySQLQuery.getAsBoolean(row[4]);
        address = MySQLQuery.getAsString(row[5]);
        phones = MySQLQuery.getAsString(row[6]);
        active = MySQLQuery.getAsBoolean(row[7]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "enterprise";
    }

    public static String getSelFlds(String alias) {
        return new Enterprise().getSelFldsForAlias(alias);
    }

    public static List<Enterprise> getList(MySQLQuery q, Connection conn) throws Exception {
        return new Enterprise().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new Enterprise().deleteById(id, conn);
    }

    public static List<Enterprise> getAll(Connection conn) throws Exception {
        return new Enterprise().getAllList(conn);
    }

//fin zona de reemplazo
    public static List<Enterprise> getEnterprises(Connection conn) throws Exception {
        MySQLQuery mq = new MySQLQuery("SELECT " + getSelFlds("") + " "
                + "FROM Enterprise as o ORDER BY o.name ASC");
        return getList(mq, conn);
    }

    public static List<Enterprise> getEnterprisesByCityId(Connection conn, int cityId) throws Exception {
        MySQLQuery mq = new MySQLQuery("SELECT DISTINCT " + getSelFlds("e") + " "
                + "FROM Enterprise as e, Agency as a "
                + "WHERE e.id = a.enterpriseId AND a.cityId = " + cityId + " ORDER BY e.name ASC");
        return getList(mq, conn);
    }
}
