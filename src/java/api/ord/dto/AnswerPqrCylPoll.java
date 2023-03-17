package api.ord.dto;

import api.ord.model.OrdPoll;
import api.ord.model.OrdTextPoll;
import api.sys.model.Employee;
import java.util.List;

public class AnswerPqrCylPoll {
    public int employeeId;
    public OrdPoll poll;
    public List<OrdTextPoll> lstText;
    public int pqrId;
}
