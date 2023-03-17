package api.mto.dto;

import java.sql.Connection;
import java.util.Arrays;
import java.util.List;
import utilities.MySQLQuery;

public class VehicleList {

    public int id;
    public String plate;
    public String internal;
    public String vhClass;
    public String type;
    public boolean contract;
    public String city;
    public String enterprise;
    public int agencyId;
    public boolean visible;

    public VehicleList() {
    }

    public VehicleList(Object[] row) {
        this.id = MySQLQuery.getAsInteger(row[0]);
        this.plate = MySQLQuery.getAsString(row[1]);
        this.internal = MySQLQuery.getAsString(row[2]);
        this.vhClass = MySQLQuery.getAsString(row[3]);
        this.type = MySQLQuery.getAsString(row[4]);
        this.contract = MySQLQuery.getAsBoolean(row[5]);
        this.city = MySQLQuery.getAsString(row[6]);
        this.enterprise = MySQLQuery.getAsString(row[7]);
        this.agencyId = MySQLQuery.getAsInteger(row[8]);
        this.visible = MySQLQuery.getAsBoolean(row[9]);
    }

    public static VehicleList getVehicleListByPlate(String plate, Connection ep) throws Exception {
        Object[] row = new MySQLQuery("SELECT "
                + "v.id, v.plate, v.internal, vc.name, vt.name, v.contract, "
                + "ct.name, et.name, ag.id, v.visible "
                + "FROM "
                + "vehicle v "
                + "LEFT JOIN fuel_type_vehicle ftv ON v.id = ftv.vehicle_id "
                + "LEFT JOIN fuel_type ft ON ft.id = ftv.fuel_type_id "
                + "LEFT JOIN vehicle_type vt ON v.vehicle_type_id = vt.id "
                + "LEFT JOIN vehicle_class vc ON vt.vehicle_class_id = vc.id "
                + "LEFT JOIN agency ag ON v.agency_id = ag.id "
                + "LEFT JOIN city ct ON ag.city_id = ct.id "
                + "LEFT JOIN enterprise et ON ag.enterprise_id = et.id "
                + "WHERE v.visible = 1 "
                + "AND v.plate = '" + plate + "' "
                + "GROUP BY v.id "
                + "ORDER BY v.id Asc,ft.name ASC").getRecord(ep);
        return (row != null ? new VehicleList(row) : null);
    }

    public static VehicleList[] findVehicleList(Connection ep) throws Exception {
        Object[][] data = new MySQLQuery("SELECT"
                + " v.id, v.plate, v.internal, vc.name, vt.name, v.contract, "
                + "ct.name, et.name, ag.id, v.visible "
                + "FROM "
                + "vehicle v "
                + "LEFT JOIN fuel_type_vehicle ftv ON v.id = ftv.vehicle_id "
                + "LEFT JOIN fuel_type ft ON ft.id = ftv.fuel_type_id "
                + "LEFT JOIN vehicle_type vt ON v.vehicle_type_id = vt.id "
                + "LEFT JOIN vehicle_class vc ON vt.vehicle_class_id = vc.id "
                + "LEFT JOIN agency ag ON v.agency_id = ag.id "
                + "LEFT JOIN city ct ON ag.city_id = ct.id "
                + "LEFT JOIN enterprise et ON ag.enterprise_id = et.id "
                + "WHERE v.visible = 1 "
                + "GROUP BY v.id "
                + "ORDER BY v.id Asc, ft.name Asc").getRecords(ep);
        VehicleList[] rta = new VehicleList[data.length];
        for (int i = 0; i < data.length; i++) {
            rta[i] = new VehicleList(data[i]);
        }
        return rta;
    }
    
    public static List<VehicleList> findVehicleList2(Connection ep) throws Exception {
        VehicleList[] data = findVehicleList(ep);
        return Arrays.asList(data);
    }

    public static VehicleList getVehicleListById(int vehicleId, Connection ep) throws Exception {
        return new VehicleList(new MySQLQuery("SELECT "
                + "v.id, "
                + "v.plate, "
                + "v.internal, "
                + "vc.name, "
                + "vt.name, "
                + "v.contract, "
                + "ct.name, "
                + "et.name, "
                + "ag.id, "
                + "v.visible "
                + "FROM vehicle v "
                + "LEFT JOIN fuel_type_vehicle ftv ON v.id = ftv.vehicle_id "
                + "LEFT JOIN fuel_type ft ON ft.id = ftv.fuel_type_id "
                + "LEFT JOIN vehicle_type vt ON v.vehicle_type_id = vt.id "
                + "LEFT JOIN vehicle_class vc ON vt.vehicle_class_id = vc.id "
                + "LEFT JOIN agency ag ON v.agency_id = ag.id "
                + "LEFT JOIN city ct ON ag.city_id = ct.id "
                + "LEFT JOIN enterprise et ON ag.enterprise_id = et.id "
                + "WHERE v.id = " + vehicleId + " "
                + "GROUP BY v.id "
                + "ORDER BY v.id ASC, ft.name ASC").getRecord(ep));
    }
}
