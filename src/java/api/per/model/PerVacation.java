package api.per.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class PerVacation extends BaseModel<PerVacation> {
//inicio zona de reemplazo

    public Date dateBeg;
    public Date dateEnd;
    public String observation;
    public int employeeId;
    public Integer groupVacationId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "date_beg",
            "date_end",
            "observation",
            "employee_id",
            "group_vacation_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, dateBeg);
        q.setParam(2, dateEnd);
        q.setParam(3, observation);
        q.setParam(4, employeeId);
        q.setParam(5, groupVacationId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        dateBeg = MySQLQuery.getAsDate(row[0]);
        dateEnd = MySQLQuery.getAsDate(row[1]);
        observation = MySQLQuery.getAsString(row[2]);
        employeeId = MySQLQuery.getAsInteger(row[3]);
        groupVacationId = MySQLQuery.getAsInteger(row[4]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "per_vacation";
    }

    public static String getSelFlds(String alias) {
        return new PerVacation().getSelFldsForAlias(alias);
    }

    public static List<PerVacation> getList(MySQLQuery q, Connection conn) throws Exception {
        return new PerVacation().getListFromQuery(q, conn);
    }

    public static List<PerVacation> getList(Params p, Connection conn) throws Exception {
        return new PerVacation().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new PerVacation().deleteById(id, conn);
    }

    public static List<PerVacation> getAll(Connection conn) throws Exception {
        return new PerVacation().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<PerVacation> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}