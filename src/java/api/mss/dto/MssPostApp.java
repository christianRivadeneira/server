package api.mss.dto;

import java.math.BigDecimal;
import java.util.Date;

public class MssPostApp {

    public int postId;
    public String postName;
    public String clientName;
    public BigDecimal lat;
    public BigDecimal lon;
    public boolean isEventual;
    public int progId;
    public Date begDate;
    public Date endDate;    
    public Date arrivalDate;    
    public Boolean isComplete;
    public Integer done; 
    public Integer numGuards;

}
