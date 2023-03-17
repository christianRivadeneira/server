package api.ord.dto;

import api.ord.model.OrdPoll;
import api.ord.model.OrdTextPoll;
import java.util.Date;
import java.util.List;

public class ConfirmPqrTank {
    public OrdPoll poll;
    public List<OrdTextPoll> textPollList;
    public int pqrTankId;
    public Date pqrAttentionDate;
    public Date pqrAttentionHour;
    public int suiNotifyId;
    public int suiRtaId;
    public boolean confirmWithApp;
}
