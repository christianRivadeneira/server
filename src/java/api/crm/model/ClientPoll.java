package api.crm.model;

import java.util.List;

public class ClientPoll {

    public Integer id;
    public Integer clientId;
    public Integer pollVersionId;
    public String textPoll;
    public String answer;
    public String notes;
    public Integer empId;
    public List<CrmPollMisc> dataMisc;
    public Integer tankId;
    public Integer regPollId;

}
