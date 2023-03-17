package api.mss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class MssShiftIncident extends BaseModel<MssShiftIncident> {
//inicio zona de reemplazo

    public int shiftIncidentTypeId;
    public int shiftId;
    public Date regDt;
    public String notes;
    public Date closeDt;
    public String closeNotes;
    public String priority;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "shift_incident_type_id",
            "shift_id",
            "reg_dt",
            "notes",
            "close_dt",
            "close_notes",
            "priority"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, shiftIncidentTypeId);
        q.setParam(2, shiftId);
        q.setParam(3, regDt);
        q.setParam(4, notes);
        q.setParam(5, closeDt);
        q.setParam(6, closeNotes);
        q.setParam(7, priority);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        shiftIncidentTypeId = MySQLQuery.getAsInteger(row[0]);
        shiftId = MySQLQuery.getAsInteger(row[1]);
        regDt = MySQLQuery.getAsDate(row[2]);
        notes = MySQLQuery.getAsString(row[3]);
        closeDt = MySQLQuery.getAsDate(row[4]);
        closeNotes = MySQLQuery.getAsString(row[5]);
        priority = MySQLQuery.getAsString(row[6]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_shift_incident";
    }

    public static String getSelFlds(String alias) {
        return new MssShiftIncident().getSelFldsForAlias(alias);
    }

    public static List<MssShiftIncident> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssShiftIncident().getListFromQuery(q, conn);
    }

    public static List<MssShiftIncident> getList(Params p, Connection conn) throws Exception {
        return new MssShiftIncident().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssShiftIncident().deleteById(id, conn);
    }

    public static List<MssShiftIncident> getAll(Connection conn) throws Exception {
        return new MssShiftIncident().getAllList(conn);
    }

//fin zona de reemplazo
}
