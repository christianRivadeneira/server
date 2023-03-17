package api.ord.dto;

import api.ord.model.OrdPoll;
import api.ord.model.OrdTextPoll;
import java.util.Date;
import java.util.List;

public class ClosePqrCyl {
    public OrdPoll poll;
    public List<OrdTextPoll> textPollList;
    public int ordPqrCylId;
    public Date pqrCylAttentionDate;
    public Date pqrCylAttentionHour;
    public int suiNotifyId;
    public int suiRtaId;
    public boolean confirmWithApp;
}
