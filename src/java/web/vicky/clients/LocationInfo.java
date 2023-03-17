package web.vicky.clients;

import api.sys.model.Sector;
import java.sql.Connection;

import utilities.MySQLQuery;

public class LocationInfo {

    public int cityId;
    public int sectorId;
    public int officeId;
    
    public LocationInfo(int cityId, int sectorId, int officeId) {
        this.cityId = cityId;
        this.sectorId = sectorId;
        this.officeId = officeId;
    }

    public static LocationInfo getInfo(double lat, double lon, Connection conn) throws Exception {
        Sector sector = GpsPolygon.hitSector(lat, lon);
        if (sector == null) {
            throw new Exception("El servicio no está disponible en su ubicación");
        }
        Object[] officeRow = new MySQLQuery("SELECT o.id, virtual_app FROM ord_office o "
                + "INNER JOIN ord_office_city oc ON oc.office_id = o.id "
                + "WHERE oc.city_id = " + sector.cityId).getRecord(conn);

        if (officeRow == null) {
            throw new Exception("No hay oficinas SAC en su ubicación");
        }

        int officeId = MySQLQuery.getAsInteger(officeRow[0]);
        boolean app = MySQLQuery.getAsBoolean(officeRow[1]);

        if (!app) {
            throw new Exception("No está disponible en su SAC");
        }
        return new LocationInfo(sector.cityId, sector.id, officeId);
    }

}
