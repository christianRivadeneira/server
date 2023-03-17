package api.hlp.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class ClosedSpansRequest {
    public String requestSubject;
    public int requestId;
    public Date requestRegistrationDate;

    public static List<ClosedSpansRequest> getList(Object[][] records){
        List<ClosedSpansRequest> list = new ArrayList<>();
        for (Object[] record : records) {
            list.add(fromRecord(record));
        }
        return list;
    }

    public static ClosedSpansRequest fromRecord(Object[] record){
        ClosedSpansRequest closedSpansRequest = new ClosedSpansRequest();
        closedSpansRequest.requestId = MySQLQuery.getAsInteger(record[0]);
        closedSpansRequest.requestSubject = MySQLQuery.getAsString(record[1]);
        closedSpansRequest.requestRegistrationDate = MySQLQuery.getAsDate(record[2]);
        return closedSpansRequest;
    }
}
