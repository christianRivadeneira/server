package api.per.model;

import java.util.ArrayList;
import java.util.List;
import utilities.MySQLQuery;

public class PerBadFp {

    public int perEmployeeId;
    public String document;
    public String empName;

    public PerBadFp() {
    }

    public PerBadFp(Object[] row) {
        this.perEmployeeId = MySQLQuery.getAsInteger(row[0]);
        this.document = MySQLQuery.getAsString(row[1]);
        this.empName = MySQLQuery.getAsString(row[2]);
    }

    public static List<PerBadFp> getList(Object[][] data) {
        List<PerBadFp> lst = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            lst.add(new PerBadFp(data[i]));
        }
        return lst;
    }
}
