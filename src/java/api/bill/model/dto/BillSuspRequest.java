package api.bill.model.dto;

import java.math.BigDecimal;
import java.util.Date;

public class BillSuspRequest {

    public int id;
    public String clientName;
    public String clientSub;
    public String building;
    public Date suspOrderDate;
    public Date suspDate;
    public Integer suspTecId;
    public Date reconOrderDate;
    public Date reconDate;
    public Integer reconTecId;
    public String fieldNotes;
    public BigDecimal reading;
    public String suspType;
    public Integer buildingId;
    public BigDecimal lat;
    public BigDecimal lon;
    public String address;
    public String suspNotes;
    public Integer neighId;

}
