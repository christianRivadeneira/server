package api.ord.dto;

import java.util.Date;

public class DetailsPollRequest {
    public Date begin;
    public Date end;
    public boolean allOffices;
    public boolean enterprise;
    public boolean subreason;
    public boolean showPqrNotes;
    public String officeCond;
    public boolean pollOtherOperator;
    public boolean pollResp;
    public int empId;
    public String citiesList;
}
