package api.gt.dto;

import api.gt.model.GtCenter;
import api.gt.model.GtGlpTrip;
import api.sys.model.Employee;
import api.sys.model.Enterprise;
import web.gates.GtTripType;

public class GtTripDataTank {
    
    public GtTripType type;
    public Enterprise ent;
    public GtCenter cent;
    public Employee drv;
    public GtGlpTrip trip;
    
}
