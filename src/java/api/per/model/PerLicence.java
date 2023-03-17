package api.per.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;
import api.MySQLCol;
import utilities.logs.LogUtils;

public class PerLicence extends BaseModel<PerLicence> {
//inicio zona de reemplazo

    public int empId;
    public Date begDate;
    public Date endDate;
    public int causeId;
    public String notes;
    public boolean active;
    public Integer groupLicenceId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "emp_id",
            "beg_date",
            "end_date",
            "cause_id",
            "notes",
            "active",
            "group_licence_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, empId);
        q.setParam(2, begDate);
        q.setParam(3, endDate);
        q.setParam(4, causeId);
        q.setParam(5, notes);
        q.setParam(6, active);
        q.setParam(7, groupLicenceId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        empId = MySQLQuery.getAsInteger(row[0]);
        begDate = MySQLQuery.getAsDate(row[1]);
        endDate = MySQLQuery.getAsDate(row[2]);
        causeId = MySQLQuery.getAsInteger(row[3]);
        notes = MySQLQuery.getAsString(row[4]);
        active = MySQLQuery.getAsBoolean(row[5]);
        groupLicenceId = MySQLQuery.getAsInteger(row[6]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "per_licence";
    }

    public static String getSelFlds(String alias) {
        return new PerLicence().getSelFldsForAlias(alias);
    }

    public static List<PerLicence> getList(MySQLQuery q, Connection conn) throws Exception {
        return new PerLicence().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new PerLicence().deleteById(id, conn);
    }

    public static List<PerLicence> getAll(Connection conn) throws Exception {
        return new PerLicence().getAllList(conn);
    }

//fin zona de reemplazo
   
    public String getLogs(PerLicence orig, PerLicence obj, Connection conn) throws Exception {
        StringBuilder sb = new StringBuilder();

        if (orig != null) {
            sb.append("Se editó el permiso:");
            int nov = 0;

            nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_DD_MM_YYYY_HH12_MM_A, "Inicio", orig.begDate, obj.begDate);
            nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_DD_MM_YYYY_HH12_MM_A, "Fin", orig.endDate, obj.endDate);
            nov += LogUtils.getLogLine(conn, sb, "Causal", orig.causeId, obj.causeId, "SELECT name FROM per_cause WHERE id = " + orig.causeId);
            nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_TEXT, "Observaciones", orig.notes, obj.notes);
            nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_BOOLEAN, "Activo", orig.active, obj.active);

            if (nov > 0) {
                return sb.toString();
            } else {
                return null;
            }
        } else {
            sb.append("Se adicionó permiso");
            return sb.toString();
        }
    }

}
