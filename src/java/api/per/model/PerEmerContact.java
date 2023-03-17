package api.per.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class PerEmerContact extends BaseModel<PerEmerContact> {
//inicio zona de reemplazo

    public String name;
    public String phone;
    public String relationship;
    public String address;
    public String city;
    public int perEmpId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "phone",
            "relationship",
            "address",
            "city",
            "per_emp_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, phone);
        q.setParam(3, relationship);
        q.setParam(4, address);
        q.setParam(5, city);
        q.setParam(6, perEmpId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        phone = MySQLQuery.getAsString(row[1]);
        relationship = MySQLQuery.getAsString(row[2]);
        address = MySQLQuery.getAsString(row[3]);
        city = MySQLQuery.getAsString(row[4]);
        perEmpId = MySQLQuery.getAsInteger(row[5]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "per_emer_contact";
    }

    public static String getSelFlds(String alias) {
        return new PerEmerContact().getSelFldsForAlias(alias);
    }

    public static List<PerEmerContact> getList(MySQLQuery q, Connection conn) throws Exception {
        return new PerEmerContact().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new PerEmerContact().deleteById(id, conn);
    }

    public static List<PerEmerContact> getAll(Connection conn) throws Exception {
        return new PerEmerContact().getAllList(conn);
    }

//fin zona de reemplazo
    public PerEmerContact getPerEmerContact(MySQLQuery q, Connection con) throws Exception {
        return new PerEmerContact().select(q, con);
    }
}
