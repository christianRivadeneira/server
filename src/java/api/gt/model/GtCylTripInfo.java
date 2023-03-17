package api.gt.model;

import java.util.Date;
import utilities.MySQLQuery;

public class GtCylTripInfo {
    
    public String auth;
    public String tripType;
    public String tripDriver;
    public String plate;
    public String internal;
    public int id;
    public Date unloadDate;
    public Integer vhId;
    public Integer driverId;
    
    public GtCylTripInfo() {}
    
    public GtCylTripInfo(Object[] row) {
        this.auth = MySQLQuery.getAsString(row[0]);
        this.tripType = MySQLQuery.getAsString(row[1]);
        this.tripDriver = MySQLQuery.getAsString(row[2]);
        this.plate = MySQLQuery.getAsString(row[3]);
        this.internal = MySQLQuery.getAsString(row[4]);
        this.id = MySQLQuery.getAsInteger(row[5]);
        this.unloadDate = MySQLQuery.getAsDate(row[6]);
        this.vhId = MySQLQuery.getAsInteger(row[7]);
        this.driverId = MySQLQuery.getAsInteger(row[8]);
    }
    
}
