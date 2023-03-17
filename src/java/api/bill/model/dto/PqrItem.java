package api.bill.model.dto;

import java.util.Date;

public class PqrItem {

    public int id;
    public int type;
    public String serial;
    public String client;
    public String address;
    public String phone;
    public String reason;
    public String enterprise;
    public Date date;
    public Date hour;
    public String contract;
    public String captured;
    public Date arrivHour;
    public String notes;
    public String mail;
    public boolean hasInformation = true;
    public boolean hasCheck = true;
    public boolean hasSummary = true;
    
}
