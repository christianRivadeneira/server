package api.hlp.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class OpenRequestSpan {
    public String requestSubject;
    public int spanId;
    public Date spanRegistrationDate;

    public static List<OpenRequestSpan> getList(Object[][] records){
        List<OpenRequestSpan> list = new ArrayList<>();
        for(Object[] record : records){
            list.add(fromRecord(record));
        }
        return list;
    } 

    public static OpenRequestSpan fromRecord(Object[] record) {
        OpenRequestSpan openRequestSpan = new OpenRequestSpan();
        openRequestSpan.spanId = MySQLQuery.getAsInteger(record[0]);
        openRequestSpan.requestSubject = MySQLQuery.getAsString(record[1]);
        openRequestSpan.spanRegistrationDate = MySQLQuery.getAsDate(record[2]);
        return openRequestSpan;
    }
}
