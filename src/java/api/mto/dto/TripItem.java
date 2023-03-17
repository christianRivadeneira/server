package api.mto.dto;

import java.util.Date;


/**
 * Para llenar las listas de viajes abiertos y cerrados en el app de flotas
 * @author alder
 */
public class TripItem {
    public int id;
    public String route;
    public String driver;
    public String plate;
    public Date start;
    public Date end;
    public PointItem lastPoint;
    
}
