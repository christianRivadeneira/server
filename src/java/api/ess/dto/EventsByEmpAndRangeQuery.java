package api.ess.dto;

import java.util.Date;

public class EventsByEmpAndRangeQuery {
    public int empId;	
    public Date start;
    public Date end;
    public boolean asAdmin;
}
