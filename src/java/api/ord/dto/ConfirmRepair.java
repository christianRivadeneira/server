package api.ord.dto;

import api.ord.model.OrdPoll;
import api.ord.model.OrdTextPoll;
import java.util.List;

public class ConfirmRepair {
    public OrdPoll poll;
    public int repairId;
    public List<OrdTextPoll> textPollList;
    public boolean confirmWithApp;
}
