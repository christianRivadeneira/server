package printout.basics;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.util.Date;
import utilities.MySQLQuery;
import web.fileManager;

//eliminar manualmente el blob de la firma
public class MtoChkLst {
//inicio zona de reemplazo

    public static final int MTO_SIGN_DRIVER = 108;//SI SE CAMBIA CAMBIAR EN EL MOVIL Y EN FrmAttachments EN CLIENTE
    public static final int MTO_SIGN_FORMATS = 109;
    public static final int SYS_ICONS = 29;

    public Integer id;
    public Integer vhId;
    public Integer creatorId;
    public Integer versionId;
    public Integer driverId;
    public Integer auxDriverId;
    public Date dt;
    public String notes;
    public Integer mileage;
    public Integer lastMileage;
    public Date nextDate;
    public Integer period;
    public Integer nextId;
    public Integer cdaId;
    public Boolean cdaRespon;
    public String responName;
    public String responJob;
    public String state;
    public Integer contractorId;
    public Integer agencyId;
    public Integer revId;
    public Integer hr;
    public Integer lastHr;

    private static final String selFlds = "`vh_id`, "
            + "`creator_id`, "
            + "`version_id`, "
            + "`driver_id`, "
            + "`aux_driver_id`, "
            + "`dt`, "
            + "`notes`, "
            + "`mileage`, "
            + "`last_mileage`, "
            + "`next_date`, "
            + "`period`, "
            + "`next_id`, "
            + "`cda_id`, "
            + "`cda_respon`, "
            + "`respon_name`, "
            + "`respon_job`, "
            + "`state`, "
            + "`contractor_id`,"
            + "`agency_id`,"
            + "`rev_id`, "
            + "`hr`, "
            + "`last_hr`";

    private static final String setFlds = "mto_chk_lst SET "
            + "`vh_id` = ?1, "
            + "`creator_id` = ?2, "
            + "`version_id` = ?3, "
            + "`driver_id` = ?4, "
            + "`aux_driver_id` = ?5, "
            + "`dt` = ?6, "
            + "`notes` = ?7, "
            + "`mileage` = ?8, "
            + "`last_mileage` = ?9, "
            + "`next_date` = ?10, "
            + "`period` = ?11, "
            + "`next_id` = ?12, "
            + "`cda_id` = ?13, "
            + "`cda_respon` = ?14, "
            + "`respon_name` = ?15, "
            + "`respon_job` = ?16, "
            + "`state` = ?17, "
            + "`contractor_id` = ?18,"
            + "`agency_id` = ?19,"
            + "`rev_id` = ?20, "
            + "`hr` = ?21, "
            + "`last_hr` = ?22";

    private void setFields(MtoChkLst obj, MySQLQuery q) {
        q.setParam(1, obj.vhId);
        q.setParam(2, obj.creatorId);
        q.setParam(3, obj.versionId);
        q.setParam(4, obj.driverId);
        q.setParam(5, obj.auxDriverId);
        q.setParam(6, obj.dt);
        q.setParam(7, obj.notes);
        q.setParam(8, obj.mileage);
        q.setParam(9, obj.lastMileage);
        q.setParam(10, obj.nextDate);
        q.setParam(11, obj.period);
        q.setParam(12, obj.nextId);
        q.setParam(13, obj.cdaId);
        q.setParam(14, obj.cdaRespon);
        q.setParam(15, obj.responName);
        q.setParam(16, obj.responJob);
        q.setParam(17, obj.state);
        q.setParam(18, obj.contractorId);
        q.setParam(19, obj.agencyId);
        q.setParam(20, obj.revId);
        q.setParam(21, obj.hr);
        q.setParam(22, obj.lastHr);
    }

    public MtoChkLst select(int id, Connection ep) throws Exception {
        MtoChkLst obj = new MtoChkLst();
        MySQLQuery q = new MySQLQuery("SELECT " + selFlds + " FROM mto_chk_lst WHERE id = " + id);
        Object[] row = q.getRecord(ep);
        obj.vhId = MySQLQuery.getAsInteger(row[0]);
        obj.creatorId = MySQLQuery.getAsInteger(row[1]);
        obj.versionId = MySQLQuery.getAsInteger(row[2]);
        obj.driverId = MySQLQuery.getAsInteger(row[3]);
        obj.auxDriverId = MySQLQuery.getAsInteger(row[4]);
        obj.dt = MySQLQuery.getAsDate(row[5]);
        obj.notes = MySQLQuery.getAsString(row[6]);
        obj.mileage = MySQLQuery.getAsInteger(row[7]);
        obj.lastMileage = MySQLQuery.getAsInteger(row[8]);
        obj.nextDate = MySQLQuery.getAsDate(row[9]);
        obj.period = MySQLQuery.getAsInteger(row[10]);
        obj.nextId = MySQLQuery.getAsInteger(row[11]);
        obj.cdaId = MySQLQuery.getAsInteger(row[12]);
        obj.cdaRespon = MySQLQuery.getAsBoolean(row[13]);
        obj.responName = MySQLQuery.getAsString(row[14]);
        obj.responJob = MySQLQuery.getAsString(row[15]);
        obj.state = MySQLQuery.getAsString(row[16]);
        obj.contractorId = MySQLQuery.getAsInteger(row[17]);
        obj.agencyId = MySQLQuery.getAsInteger(row[18]);
        obj.revId = MySQLQuery.getAsInteger(row[19]);
        obj.hr = MySQLQuery.getAsInteger(row[20]);
        obj.lastHr = MySQLQuery.getAsInteger(row[21]);

        obj.id = id;
        return obj;
    }

//fin zona de reemplazo
    public int insert(MtoChkLst pobj, Connection ep) throws Exception {
        return 0;//INSERT MANUAL EN EL FrmMtoChkLst.java
    }

    public void update(MtoChkLst obj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("UPDATE " + setFlds + " WHERE id = " + obj.id);
        setFields(obj, q);
        q.executeUpdate(ep);
    }

    public void delete(int id, Connection ep) throws Exception {
    }

    public static void createValsQuery(Connection ep, Integer chklistId) throws Exception {
        new MySQLQuery("INSERT INTO mto_chk_val (lst_id ,row_id , col_id, state, val) "
                + "(SELECT "
                + "lst.id, "
                + "`row`.id, "
                + "IF((SELECT COUNT(*)=1 FROM mto_chk_col c WHERE c.grp_id = grp.id),(SELECT c.id FROM mto_chk_col c WHERE c.grp_id = grp.id),IF(`row`.mandatory,	(SELECT c.id FROM mto_chk_col c WHERE c.grp_id = grp.id AND IF(`row`.flip, c.flip_state = 'ok', c.state = 'ok') ORDER BY c.place LIMIT 0, 1),NULL)), "
                + "IF(`row`.mandatory, 'ok', 'empty'),"
                + "null "
                + "FROM mto_chk_row AS `row` "
                + "INNER JOIN mto_chk_grp AS grp ON `row`.grp_id = grp.id "
                + "INNER JOIN mto_chk_lst AS lst ON lst.version_id = grp.version_id "
                + "WHERE lst.id = " + chklistId + " AND `row`.type = 'nor' )").executeInsert(ep);
        new MySQLQuery("INSERT INTO mto_chk_val (lst_id ,row_id , col_id, state, val) "
                + "(SELECT "
                + "lst.id, "
                + "`row`.id, "
                + "null, "
                + "'empty',"
                + "null "
                + "FROM mto_chk_row AS `row` "
                + "INNER JOIN mto_chk_grp AS grp ON `row`.grp_id = grp.id "
                + "INNER JOIN mto_chk_lst AS lst ON lst.version_id = grp.version_id "
                + "WHERE lst.id = " + chklistId + " AND `row`.type <> 'nor')").executeInsert(ep);
    }

    public static void checkStatus(Connection ep, int lstId, String driverName) throws Exception {
        new MySQLQuery("UPDATE mto_chk_lst SET state = "
                + "(SELECT state FROM "
                + "((SELECT state "
                + "FROM "
                + "mto_chk_val "
                + "INNER JOIN mto_chk_row ON mto_chk_val.row_id = mto_chk_row.id "
                + "WHERE mto_chk_val.lst_id = " + lstId + " "
                + "AND mto_chk_row.type <> 'tit' "
                + "AND (mto_chk_row.mandatory = 1 OR mto_chk_val.col_id IS NOT NULL)) "
                + "UNION ALL "
                + "(SELECT "
                + "IF(!checked, 'error', IF(review, IF(DATE(dt) <= DATE(rev_date), 'ok', 'error'), 'ok')) "
                + "FROM "
                + "mto_chk_element "
                + "INNER JOIN mto_element ON mto_chk_element.elem_id = mto_element.id "
                + "INNER JOIN mto_chk_lst ON mto_chk_element.lst_id = mto_chk_lst.id "
                + "WHERE "
                + "mto_chk_lst.id = " + lstId + ")) AS l "
                + "ORDER BY IF(state = 'empty', 1, if(state = 'error', 2, if(state = 'warn', 3, if(state = 'ok', 4, 4)))) "
                + "LIMIT 0,1) WHERE id = " + lstId).executeUpdate(ep);
        new MySQLQuery("UPDATE mto_chk_lst SET cda_respon = false, respon_name = '', respon_job = '' WHERE id = " + lstId + " AND state <> 'error'").executeUpdate(ep);
        new MySQLQuery("UPDATE mto_chk_lst SET respon_name = '" + driverName + "', respon_job = 'Conductor' WHERE id = " + lstId + " AND state = 'error' AND (respon_name = '' OR respon_name IS NULL)").executeUpdate(ep);
    }

    public static void deleteFormat(Connection ep, Integer lstId) throws Exception {
        String state = new MySQLQuery("SELECT state FROM mto_chk_lst WHERE id = " + lstId).getAsString(ep);
        boolean pendingValues = new MySQLQuery("SELECT COUNT(*)>0 "
                + "FROM mto_chk_val "
                + "INNER JOIN mto_chk_row ON mto_chk_row.id = mto_chk_val.row_id "
                + "WHERE state<>'ok' AND mto_chk_row.mandatory = 1 "
                + "AND work_order_id IS NOT NULL AND corr_date IS NULL AND lst_id = " + lstId).getAsBoolean(ep);
        boolean pendingElements = new MySQLQuery("(SELECT COUNT(*)>0 "
                + "FROM mto_chk_element AS ce "
                + "INNER JOIN mto_element AS e ON ce.elem_id = e.id "
                + "WHERE ce.lst_id = " + lstId + " "
                + "AND checked=0 AND work_order_id IS NULL AND corr_date IS NULL)").getAsBoolean(ep);
        boolean delete = false;
        if (state != null && !state.equals("ok")) {
            if (pendingValues || pendingElements) {
                throw new Exception("El formato tiene pendientes");
            } else {
                delete = true;
            }
        } else {
            delete = true;
        }
        if (delete) {
            new MySQLQuery("UPDATE mto_chk_lst SET next_id = NULL WHERE next_id = " + lstId).executeUpdate(ep);
            new MySQLQuery("DELETE FROM mto_chk_doc WHERE lst_id = " + lstId).executeDelete(ep);
            new MySQLQuery("DELETE FROM mto_chk_element WHERE lst_id = " + lstId).executeDelete(ep);
            new MySQLQuery("DELETE FROM mto_chk_val WHERE lst_id = " + lstId).executeDelete(ep);
            new MySQLQuery("DELETE FROM mto_chk_lst WHERE id = " + lstId).executeDelete(ep);
            new MySQLQuery("DELETE FROM mto_log WHERE owner_type = " + MtoLog.FORMATS.id + " AND owner_id = " + lstId).executeDelete(ep);
        }
    }

    public static Boolean canCreate(Connection ep, Integer vhId) throws Exception {
        Boolean b = new MySQLQuery("SELECT "
                + "(SELECT COUNT(*)=0 "
                + "FROM mto_chk_element AS ce "
                + "INNER JOIN mto_element AS e ON ce.elem_id = e.id "
                + "WHERE ce.lst_id = lst.id "
                + "AND IF(!ce.checked,(ce.work_order_id IS NULL AND ce.corr_date IS NULL),false)) "
                + "AND "
                + "(SELECT COUNT(*)=0 "
                + "FROM mto_chk_val AS val "
                + "INNER JOIN mto_chk_row AS r ON r.id = val.row_id "
                + "WHERE val.lst_id = lst.id "
                + "AND r.type <> 'tit' AND r.mandatory = 1 "
                + "AND IF((val.state = 'empty' OR val.state = 'error'), val.work_order_id IS NULL AND val.corr_date IS NULL,FALSE)) "
                + "FROM mto_chk_lst AS lst "
                + "WHERE lst.vh_id = " + vhId).getAsBoolean(ep);
        return (b == null ? true : b);
    }

    public static LastAndNext getLastAndNext(int vehId, Date dt, Connection ep) throws Exception {
        LastAndNext rta = new LastAndNext();;

        Object[][] lastData = new MySQLQuery("SELECT "
                + "`mileage`, "
                + "`dt`, "
                + "hr "
                + "FROM mto_chk_lst "
                + "WHERE `vh_id` = " + vehId + " AND DATE(`dt`) <= DATE(COALESCE(?2, NOW())) "
                + "ORDER BY `dt` DESC, `mileage` DESC LIMIT 0, 1").setParam(2, dt).getRecords(ep);

        if (lastData.length > 0) {
            rta.lastKms = MySQLQuery.getAsInteger(lastData[0][0]);
            rta.lastDate = MySQLQuery.getAsDate(lastData[0][1]);
            rta.lastHr = MySQLQuery.getAsInteger(lastData[0][2]);
        }

        Object[][] nextData = new MySQLQuery("SELECT "
                + "`mileage`, "
                + "`dt`, "
                + "hr "
                + "FROM mto_chk_lst "
                + "WHERE `vh_id` = " + vehId + " AND DATE(`dt`) > DATE(COALESCE(?2, NOW())) "
                + "ORDER BY `dt` ASC, `mileage` ASC LIMIT 0, 1").setParam(2, dt).getRecords(ep);
        if (nextData.length > 0) {
            rta.nextKms = MySQLQuery.getAsInteger(nextData[0][0]);
            rta.nextDate = MySQLQuery.getAsDate(nextData[0][1]);
            rta.nextHr = MySQLQuery.getAsInteger(nextData[0][2]);
        }
        return rta;
    }

    public static class LastAndNext {

        public Integer lastKms;
        public Integer lastHr;
        public Date lastDate;
        public Integer nextKms;
        public Integer nextHr;
        public Date nextDate;

    }

    public static String getQueryCheckStatusName() {
        return "IF("
                + "(SELECT COUNT(*)>0 "
                + "FROM mto_chk_val AS v "
                + "INNER JOIN mto_chk_row AS r ON r.id = v.row_id "
                + "WHERE (r.mandatory = 1 OR v.col_id IS NOT NULL) AND r.type <> 'tit' AND v.lst_id = lst.id "
                + "AND v.state <> 'ok' AND v.corr_date IS NULL AND v.work_order_id IS NULL) "
                + "OR "
                + "(SELECT COUNT(*)>0 FROM mto_chk_element AS e "
                + "INNER JOIN mto_element AS me ON me.id = e.elem_id "
                + "WHERE !e.checked AND e.corr_date IS NULL AND "
                + "e.work_order_id IS NULL AND e.lst_id = lst.id),'Pendiente','Corregido') ";
    }

    public static FileInputStream readFileDirect(int bFileId, Connection con) throws Exception {
        File f = new fileManager.PathInfo(con).getExistingFile(bFileId);
        return new FileInputStream(f);
    }
}
