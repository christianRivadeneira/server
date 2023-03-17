package api.mss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class MssClient extends BaseModel<MssClient> {
//inicio zona de reemplazo

    public String name;
    public String nit;
    public String code;
    public String address;
    public String contactName;
    public String contactPhone;
    public String contactEmail;
    public boolean active;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "nit",
            "code",
            "address",
            "contact_name",
            "contact_phone",
            "contact_email",
            "active"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, nit);
        q.setParam(3, code);
        q.setParam(4, address);
        q.setParam(5, contactName);
        q.setParam(6, contactPhone);
        q.setParam(7, contactEmail);
        q.setParam(8, active);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        nit = MySQLQuery.getAsString(row[1]);
        code = MySQLQuery.getAsString(row[2]);
        address = MySQLQuery.getAsString(row[3]);
        contactName = MySQLQuery.getAsString(row[4]);
        contactPhone = MySQLQuery.getAsString(row[5]);
        contactEmail = MySQLQuery.getAsString(row[6]);
        active = MySQLQuery.getAsBoolean(row[7]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_client";
    }

    public static String getSelFlds(String alias) {
        return new MssClient().getSelFldsForAlias(alias);
    }

    public static List<MssClient> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssClient().getListFromQuery(q, conn);
    }

    public static List<MssClient> getList(Params p, Connection conn) throws Exception {
        return new MssClient().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssClient().deleteById(id, conn);
    }

    public static List<MssClient> getAll(Connection conn) throws Exception {
        return new MssClient().getAllList(conn);
    }

//fin zona de reemplazo
    public static MssClient getClientByCode(String cliCode, Connection conn) throws Exception {
        MySQLQuery mq = new MySQLQuery("SELECT " + getSelFlds("") + " FROM mss_client WHERE code = ?1").setParam(1, cliCode);
        return new MssClient().select(mq, conn);
    }
}
