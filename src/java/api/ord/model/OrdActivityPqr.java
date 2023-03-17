package api.ord.model;

import api.BaseModel;
import api.sys.model.Employee;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;
import web.ShortException;

public class OrdActivityPqr extends BaseModel<OrdActivityPqr> {
//inicio zona de reemplazo

    public Date actDate;
    public String activity;
    public String actDeveloper;
    public String observation;
    public Integer pqrCylId;
    public Integer pqrTankId;
    public Integer pqrOtherId;
    public Integer comId;
    public Integer repairId;
    public int createId;
    public Date creationDate;
    public int modId;
    public Date modDate;
    public Integer pqrAfilId;
    public String radOrfeo;
    public boolean evidence;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "act_date",
            "activity",
            "act_developer",
            "observation",
            "pqr_cyl_id",
            "pqr_tank_id",
            "pqr_other_id",
            "com_id",
            "repair_id",
            "create_id",
            "creation_date",
            "mod_id",
            "mod_date",
            "pqr_afil_id",
            "rad_orfeo",
            "evidence"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, actDate);
        q.setParam(2, activity);
        q.setParam(3, actDeveloper);
        q.setParam(4, observation);
        q.setParam(5, pqrCylId);
        q.setParam(6, pqrTankId);
        q.setParam(7, pqrOtherId);
        q.setParam(8, comId);
        q.setParam(9, repairId);
        q.setParam(10, createId);
        q.setParam(11, creationDate);
        q.setParam(12, modId);
        q.setParam(13, modDate);
        q.setParam(14, pqrAfilId);
        q.setParam(15, radOrfeo);
        q.setParam(16, evidence);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        actDate = MySQLQuery.getAsDate(row[0]);
        activity = MySQLQuery.getAsString(row[1]);
        actDeveloper = MySQLQuery.getAsString(row[2]);
        observation = MySQLQuery.getAsString(row[3]);
        pqrCylId = MySQLQuery.getAsInteger(row[4]);
        pqrTankId = MySQLQuery.getAsInteger(row[5]);
        pqrOtherId = MySQLQuery.getAsInteger(row[6]);
        comId = MySQLQuery.getAsInteger(row[7]);
        repairId = MySQLQuery.getAsInteger(row[8]);
        createId = MySQLQuery.getAsInteger(row[9]);
        creationDate = MySQLQuery.getAsDate(row[10]);
        modId = MySQLQuery.getAsInteger(row[11]);
        modDate = MySQLQuery.getAsDate(row[12]);
        pqrAfilId = MySQLQuery.getAsInteger(row[13]);
        radOrfeo = MySQLQuery.getAsString(row[14]);
        evidence = MySQLQuery.getAsBoolean(row[15]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_activity_pqr";
    }

    public static String getSelFlds(String alias) {
        return new OrdActivityPqr().getSelFldsForAlias(alias);
    }

    public static List<OrdActivityPqr> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdActivityPqr().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdActivityPqr().deleteById(id, conn);
    }

    public static List<OrdActivityPqr> getAll(Connection conn) throws Exception {
        return new OrdActivityPqr().getAllList(conn);
    }

//fin zona de reemplazo
    public static List<OrdActivityPqr> getAllByCreateId(Connection conn, Integer pqrId, int type, Integer empId) throws Exception {

        String field = "";
        switch (type) {
            case 1:
                field = "pqr_cyl_id = ";
                break;
            case 2:
                field = "pqr_tank_id = ";
                break;
            case 3:
                field = "repair_id = ";
                break;
            default:
                throw new AssertionError();
        }

        MySQLQuery mq = new MySQLQuery("SELECT " + getSelFlds("") + " FROM ord_activity_pqr WHERE "
                + "create_id = " + empId + " AND " + field + pqrId);

        return OrdActivityPqr.getList(mq, conn);

    }

    private static OrdActivityPqr insertCloseAnyPqrActivity(Connection con, Integer pqrCylId,
            Integer pqrTankId, Integer pqrOtherId, Integer pqrRepairId, Employee emp) throws Exception {

        if ((pqrCylId == null || pqrCylId.equals(0))
                && (pqrTankId == null || pqrTankId.equals(0))
                && (pqrOtherId == null || pqrOtherId.equals(0))
                && (pqrRepairId == null || pqrRepairId.equals(0))) {
            throw new ShortException("La Actividad debe pertenecer a una PQR");
        }

        Date now = MySQLQuery.now(con);
        OrdActivityPqr activityPqr = new OrdActivityPqr();
        activityPqr.actDate = now;
        activityPqr.activity = "Cierre de la PQR";
        activityPqr.actDeveloper = emp.firstName + " " + emp.lastName;
        activityPqr.pqrCylId = null;
        activityPqr.pqrTankId = null;
        activityPqr.pqrOtherId = null;
        activityPqr.repairId = null;
        activityPqr.createId = emp.id;
        activityPqr.creationDate = now;
        activityPqr.modId = emp.id;
        activityPqr.modDate = now;

        if (pqrCylId != null) {
            activityPqr.pqrCylId = pqrCylId;
        } else if (pqrTankId != null) {
            activityPqr.pqrTankId = pqrTankId;
        } else if (pqrOtherId != null) {
            activityPqr.pqrOtherId = pqrOtherId;
        } else {
            activityPqr.repairId = pqrRepairId;
        }

        activityPqr.insert(con);
        return activityPqr;
    }

    public static OrdActivityPqr insertCloseCylPqrActivity(Connection con, OrdPqrCyl pqr, Employee emp) throws Exception {
        return insertCloseAnyPqrActivity(con, pqr.id, null, null, null, emp);
    }

    public static OrdActivityPqr insertCloseTankPqrActivity(Connection con, OrdPqrTank pqr, Employee emp) throws Exception {
        return insertCloseAnyPqrActivity(con, null, pqr.id, null, null, emp);
    }

    public static OrdActivityPqr insertClosePqrOtherActivity(Connection con, OrdPqrOther pqr, Employee emp) throws Exception {
        return insertCloseAnyPqrActivity(con, null, null, pqr.id, null, emp);
    }

    public static OrdActivityPqr insertClosePqrOtherActivity(Connection con, OrdPqrOther pqr, Employee emp, String notes) throws Exception {
        OrdActivityPqr activityPqr = insertCloseAnyPqrActivity(con, null, null, pqr.id, null, emp);
        activityPqr.observation = notes;
        activityPqr.update(con);
        return activityPqr;
    }

    public static OrdActivityPqr insertCloseRepairPqrActivity(Connection con, OrdRepairs pqr, Employee emp) throws Exception {
        OrdActivityPqr activityPqr = insertCloseAnyPqrActivity(con, null, null, null, pqr.id, emp);
        return activityPqr;
    }

    public static OrdActivityPqr insertAttendCylPqrActivity(Connection con, OrdPqrCyl pqr, Employee emp) throws Exception {
        return insertAttendPqrActivity(con, pqr.id, null, null, null, emp);
    }

        public static OrdActivityPqr insertAttendTankPqrActivity(Connection con, OrdPqrTank pqr, Employee emp) throws Exception {
        return insertAttendPqrActivity(con, null, pqr.id, null, null, emp);
    }

    public static OrdActivityPqr insertAttendRepairActivity(Connection con, OrdRepairs pqr, Employee emp) throws Exception {
        return insertAttendPqrActivity(con, null, null, null, pqr.id, emp);
    }

    private static OrdActivityPqr insertAttendPqrActivity(Connection con, Integer pqrCylId, Integer pqrTankId,
            Integer pqrOtherId, Integer pqrRepairId, Employee emp) throws Exception {

        if ((pqrCylId == null || pqrCylId.equals(0))
                && (pqrTankId == null || pqrTankId.equals(0))
                && (pqrOtherId == null || pqrOtherId.equals(0))
                && (pqrRepairId == null || pqrRepairId.equals(0))) {
            throw new ShortException("La Actividad debe pertenecer a un pqr");
        }

        Date now = new Date();
        OrdActivityPqr activity = new OrdActivityPqr();
        activity.actDate = now;
        activity.activity = "Encuesta diligenciada";
        activity.actDeveloper = emp.firstName + " " + emp.lastName;
        activity.createId = emp.id;
        activity.creationDate = now;
        activity.modId = emp.id;
        activity.modDate = now;

        if (pqrCylId != null) {
            activity.pqrCylId = pqrCylId;
        } else if (pqrTankId != null) {
            activity.pqrTankId = pqrTankId;
        } else if (pqrOtherId != null) {
            activity.pqrOtherId = pqrOtherId;
        } else {
            activity.repairId = pqrRepairId;
        }

        activity.insert(con);
        return activity;
    }

}
