package api.ord.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class OrdPqrClientTank extends BaseModel<OrdPqrClientTank> {
//inicio zona de reemplazo

    public int cityId;
    public Integer billInstanceId;
    public int mirrorId;
    public Integer buildOrdId;
    public String firstName;
    public String lastName;
    public String phones;
    public String numInstall;
    public String apartament;
    public String doc;
    public Integer channelId;
    public Integer neighId;
    public String address;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "city_id",
            "bill_instance_id",
            "mirror_id",
            "build_ord_id",
            "first_name",
            "last_name",
            "phones",
            "num_install",
            "apartament",
            "doc",
            "channel_id",
            "neigh_id",
            "address"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, cityId);
        q.setParam(2, billInstanceId);
        q.setParam(3, mirrorId);
        q.setParam(4, buildOrdId);
        q.setParam(5, firstName);
        q.setParam(6, lastName);
        q.setParam(7, phones);
        q.setParam(8, numInstall);
        q.setParam(9, apartament);
        q.setParam(10, doc);
        q.setParam(11, channelId);
        q.setParam(12, neighId);
        q.setParam(13, address);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        cityId = MySQLQuery.getAsInteger(row[0]);
        billInstanceId = MySQLQuery.getAsInteger(row[1]);
        mirrorId = MySQLQuery.getAsInteger(row[2]);
        buildOrdId = MySQLQuery.getAsInteger(row[3]);
        firstName = MySQLQuery.getAsString(row[4]);
        lastName = MySQLQuery.getAsString(row[5]);
        phones = MySQLQuery.getAsString(row[6]);
        numInstall = MySQLQuery.getAsString(row[7]);
        apartament = MySQLQuery.getAsString(row[8]);
        doc = MySQLQuery.getAsString(row[9]);
        channelId = MySQLQuery.getAsInteger(row[10]);
        neighId = MySQLQuery.getAsInteger(row[11]);
        address = MySQLQuery.getAsString(row[12]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_pqr_client_tank";
    }

    public static String getSelFlds(String alias) {
        return new OrdPqrClientTank().getSelFldsForAlias(alias);
    }

    public static List<OrdPqrClientTank> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdPqrClientTank().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdPqrClientTank().deleteById(id, conn);
    }

    public static List<OrdPqrClientTank> getAll(Connection conn) throws Exception {
        return new OrdPqrClientTank().getAllList(conn);
    }

//fin zona de reemplazo
    public static OrdPqrClientTank getByMirror(int mirrorId, int instanceId, Connection conn) throws Exception {
        return new OrdPqrClientTank().select(new MySQLQuery("SELECT " + getSelFlds("c") + " FROM ord_pqr_client_tank c WHERE c.bill_instance_id = " + instanceId + " AND c.mirror_id = " + mirrorId), conn);
    }

}
