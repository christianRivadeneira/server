package web.vicky.model;

import java.sql.Connection;
import utilities.MySQLQuery;

public class OrderInfo {

    public int orderId;
    public Double lat;
    public Double lon;
    public boolean clieCoords;
    public int sectorId;
    public int officeId;
    public Integer vhId;
    public int indexId;
    public String address;
    public String neigh;
    public String document;
    public String names;
    public Cyl[] cyls;
    public String landMark;
    public String sector;
    public Integer cityId;

    public static OrderInfo getInfo(int orderId, Connection conn) throws Exception {
        Object[] indexRow = new MySQLQuery("SELECT IFNULL(i.lat, n.lat), IFNULL(i.lon, n.lon), n.sector_id, o.office_id, o.vehicle_id, i.id, address, n.name, i.document, "
                + "trim(concat(i.first_name, ' ', ifnull(last_name, ''))), i.lat IS NOT NULL, s.name, i.city_id "
                + "FROM  "
                + "ord_contract_index i  "
                + "inner join ord_cyl_order o on o.index_id = i.id "
                + "inner join neigh n on i.neigh_id = n.id "
                + "inner join sector s on n.sector_id = s.id "
                + "where o.id = " + orderId).getRecord(conn);

        Object[][] cylsData = new MySQLQuery("select t.name, ot.amount from  "
                + "ord_cyl_type_order ot  "
                + "inner join cylinder_type t on ot.cylinder_type_id = t.id "
                + "where ot.order_id = ?1 "
                + "order by cast(t.name as signed) asc").setParam(1, orderId).getRecords(conn);

        Cyl[] cyls = new Cyl[cylsData.length];
        for (int i = 0; i < cyls.length; i++) {
            cyls[i] = new Cyl();
            cyls[i].name = MySQLQuery.getAsString(cylsData[i][0]);
            cyls[i].amount = MySQLQuery.getAsInteger(cylsData[i][1]);
        }

        OrderInfo rta = new OrderInfo();
        rta.orderId = orderId;
        rta.lat = MySQLQuery.getAsDouble(indexRow[0]);
        rta.lon = MySQLQuery.getAsDouble(indexRow[1]);
        rta.sectorId = MySQLQuery.getAsInteger(indexRow[2]);
        rta.officeId = MySQLQuery.getAsInteger(indexRow[3]);
        rta.vhId = MySQLQuery.getAsInteger(indexRow[4]);
        rta.indexId = MySQLQuery.getAsInteger(indexRow[5]);
        rta.address = MySQLQuery.getAsString(indexRow[6]);
        rta.neigh = MySQLQuery.getAsString(indexRow[7]);
        rta.document = MySQLQuery.getAsString(indexRow[8]);
        rta.names = MySQLQuery.getAsString(indexRow[9]);
        rta.clieCoords = MySQLQuery.getAsBoolean(indexRow[10]);
        rta.sector = MySQLQuery.getAsString(indexRow[11]);
        rta.cityId = MySQLQuery.getAsInteger(indexRow[12]);
        rta.landMark = new MySQLQuery("SELECT u.landmark FROM clie_usr u "
                    + "INNER JOIN ord_contract_index i ON u.id = i.contract_id AND i.`type` = 'app' "
                    + "WHERE i.id = " + rta.indexId).getAsString(conn);

        rta.cyls = cyls;
        return rta;
    }
}
