package web.marketing.cylSales;

import java.sql.Connection;
import java.util.Date;
import utilities.MySQLQuery;

public class Contract {

    public int indexId;
    public int contractId;
    public boolean brand;
    public Date dt;
    public String firstName;
    public String lastName;
    public String address;
    public String phones;
    public String email;

    public Contract(Object[] row) {
        indexId = MySQLQuery.getAsInteger(row[0]);
        contractId = MySQLQuery.getAsInteger(row[1]);
        brand = MySQLQuery.getAsBoolean(row[2]);
        dt = MySQLQuery.getAsDate(row[3]);
        firstName = MySQLQuery.getAsString(row[4]);
        lastName = MySQLQuery.getAsString(row[5]);
        address = MySQLQuery.getAsString(row[6]);
        phones = MySQLQuery.getAsString(row[7]);
        email = MySQLQuery.getAsString(row[8]);
    }

    public static Contract searchContract(Connection conn, String document) throws Exception {
        Object[][] recs = new MySQLQuery("SELECT "
                + "oci.id, " //0
                + "oci.contract_id, " //1
                + "oci.type = 'brand', " //2
                + "IF(oci.type = 'brand', c.sign_date, oc.created_date) AS fecha, " //3
                + "oci.first_name, " //4
                + "oci.last_name," //5
                + "oci.address, "//6
                + "oci.phones, "//7
                + "oci.email "//8
                + "FROM ord_contract_index AS oci "
                + "LEFT JOIN contract AS c ON c.id = oci.contract_id "
                + "LEFT JOIN ord_contract AS oc ON oc.id = oci.contract_id "
                + "WHERE oci.document = '" + document + "' "
                + "AND oci.active = 1 "
                + "AND oci.type <> 'app' "
                + "ORDER BY oci.id DESC "
                + "LIMIT 1").getRecords(conn);
        if (recs != null && recs.length > 0) {
            return new Contract(recs[0]);
        }
        return null;
    }

    public static Contract searchContract(Connection conn, int indexId) throws Exception {
        Object[][] recs = new MySQLQuery("SELECT "
                + "oci.id, " //0
                + "oci.contract_id, " //1
                + "oci.type = 'brand', " //2
                + "IF(oci.type = 'brand', c.sign_date, oc.created_date) AS fecha, " //3
                + "oci.first_name, " //4
                + "oci.last_name," //5
                + "oci.address, "//6
                + "oci.phones, "//7
                + "oci.email "//8
                + "FROM ord_contract_index AS oci "
                + "LEFT JOIN contract AS c ON c.id = oci.contract_id "
                + "LEFT JOIN ord_contract AS oc ON oc.id = oci.contract_id "
                + "WHERE oci.id = " + indexId + " "
                + "AND oci.active = 1 "
                + "AND oci.type <> 'app' "
                + "ORDER BY oci.id DESC "
                + "LIMIT 1").getRecords(conn);
        if (recs != null && recs.length > 0) {
            return new Contract(recs[0]);
        }
        return null;
    }

    public static Contract searchContractId(Connection conn, int id) throws Exception {
        Object[][] recs = new MySQLQuery("SELECT "
                + "oci.id, " //0
                + "oci.contract_id, " //1
                + "oci.type = 'brand', " //2
                + "IF(oci.type = 'brand', c.sign_date, oc.created_date) AS fecha, " //3
                + "oci.first_name, " //4
                + "oci.last_name," //5
                + "oci.address, "//6
                + "oci.phones, "//7
                + "oci.email "//8
                + "FROM ord_contract_index AS oci "
                + "LEFT JOIN contract AS c ON c.id = oci.contract_id "
                + "LEFT JOIN ord_contract AS oc ON oc.id = oci.contract_id "
                + "WHERE oci.contract_id = " + id + " "
                + "AND oci.active = 1 "
                + "AND oci.type <> 'app' "
                + "ORDER BY oci.id DESC "
                + "LIMIT 1").getRecords(conn);
        if (recs != null && recs.length > 0) {
            return new Contract(recs[0]);
        }
        return null;
    }

     public static String updateContractPhone(Connection conn, Contract ctr, String phone) throws Exception {
        new MySQLQuery("UPDATE ord_contract_index SET phones = '" + phone + "' WHERE id = " + ctr.indexId).executeUpdate(conn);
        if (ctr.brand) {
            new MySQLQuery("UPDATE contract SET phones = '" + phone + "' WHERE id = " + ctr.contractId).executeUpdate(conn);
        } else {
            new MySQLQuery("UPDATE ord_contract SET phones = '" + phone + "' WHERE id = " + ctr.contractId).executeUpdate(conn);
        }
        phone = new MySQLQuery("SELECT phones FROM ord_contract_index WHERE id = " + ctr.indexId).getAsString(conn);
        return phone;
    }
    
    public static void updateContract(Connection conn, Contract ctr, String phone, String email, String address, int neigh) throws Exception {
        String upd = (phone != null ? "phones = '" + phone + "'," : "phones = NULL,")
                + (email != null ? "email = '" + email + "'," : "email = NULL,")
                + (address != null ? "address = '" + address + "'" : "address = NULL")
                + ", neigh_id = " + neigh + " ";

        new MySQLQuery("UPDATE ord_contract_index SET " + upd + " WHERE id = " + ctr.indexId).executeUpdate(conn);
        if (ctr.brand) {
            new MySQLQuery("UPDATE contract SET " + upd + " WHERE id = " + ctr.contractId).executeUpdate(conn);
        } else {
            new MySQLQuery("UPDATE ord_contract SET " + upd + " WHERE id = " + ctr.contractId).executeUpdate(conn);
        }
    }
}
