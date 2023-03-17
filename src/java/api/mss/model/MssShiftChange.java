package api.mss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class MssShiftChange extends BaseModel<MssShiftChange> {
//inicio zona de reemplazo

    public Date regDt;
    public int shiftId;
    public int oldGuardId;
    public int newGuardId;
    public String notes;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "reg_dt",
            "shift_id",
            "old_guard_id",
            "new_guard_id",
            "notes"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, regDt);
        q.setParam(2, shiftId);
        q.setParam(3, oldGuardId);
        q.setParam(4, newGuardId);
        q.setParam(5, notes);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        regDt = MySQLQuery.getAsDate(row[0]);
        shiftId = MySQLQuery.getAsInteger(row[1]);
        oldGuardId = MySQLQuery.getAsInteger(row[2]);
        newGuardId = MySQLQuery.getAsInteger(row[3]);
        notes = MySQLQuery.getAsString(row[4]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_shift_change";
    }

    public static String getSelFlds(String alias) {
        return new MssShiftChange().getSelFldsForAlias(alias);
    }

    public static List<MssShiftChange> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssShiftChange().getListFromQuery(q, conn);
    }

    public static List<MssShiftChange> getList(Params p, Connection conn) throws Exception {
        return new MssShiftChange().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssShiftChange().deleteById(id, conn);
    }

    public static List<MssShiftChange> getAll(Connection conn) throws Exception {
        return new MssShiftChange().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<MssShiftChange> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}