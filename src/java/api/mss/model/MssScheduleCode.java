package api.mss.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class MssScheduleCode extends BaseModel<MssScheduleCode> {
//inicio zona de reemplazo

    public String code;
    public String notes;
    public Date begTime;
    public Date endTime;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "code",
            "notes",
            "beg_time",
            "end_time"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, code);
        q.setParam(2, notes);
        q.setParam(3, begTime);
        q.setParam(4, endTime);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        code = MySQLQuery.getAsString(row[0]);
        notes = MySQLQuery.getAsString(row[1]);
        begTime = MySQLQuery.getAsDate(row[2]);
        endTime = MySQLQuery.getAsDate(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_schedule_code";
    }

    public static String getSelFlds(String alias) {
        return new MssScheduleCode().getSelFldsForAlias(alias);
    }

    public static List<MssScheduleCode> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssScheduleCode().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssScheduleCode().deleteById(id, conn);
    }

    public static List<MssScheduleCode> getAll(Connection conn) throws Exception {
        return new MssScheduleCode().getAllList(conn);
    }

//fin zona de reemplazo
}
