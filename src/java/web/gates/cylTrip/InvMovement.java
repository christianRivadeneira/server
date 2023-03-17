package web.gates.cylTrip;

import java.sql.Connection;
import java.util.Date;
import utilities.MySQLQuery;

public class InvMovement {

//inicio zona de reemplazo

    public Integer id;
    public Date mvDate;
    public Integer typeId;
    public Integer centerId;
    public Integer centerDesId;
    public Integer storeId;
    public Integer workshopId;
    public Integer factoryId;
    public Integer externalId;
    public Integer clientId;
    public Integer lostId;
    public String notes;
    public Boolean cancel;
    public String cancelNotes;
    public Boolean checked;
    public Integer vh;
    public Boolean toConciliate;
    public Integer gtCylTripId;

    private static final String selFlds = "`mv_date`, "
            + "`type_id`, "
            + "`center_id`, "
            + "`center_des_id`, "
            + "`store_id`, "
            + "`workshop_id`, "
            + "`factory_id`, "
            + "`external_id`, "
            + "`client_id`, "
            + "`lost_id`, "
            + "`notes`, "
            + "`cancel`, "
            + "`cancel_notes`, "
            + "`checked`, "
            + "`vh`,"
            + "`to_conciliate`, "
            + "`gt_cyl_trip_id`";

    private static final String setFlds = "inv_movement SET "
            + "`mv_date` = ?1, "
            + "`type_id` = ?2, "
            + "`center_id` = ?3, "
            + "`center_des_id` = ?4, "
            + "`store_id` = ?5, "
            + "`workshop_id` = ?6, "
            + "`factory_id` = ?7, "
            + "`external_id` = ?8, "
            + "`client_id` = ?9, "
            + "`lost_id` = ?10, "
            + "`notes` = ?11, "
            + "`cancel` = ?12, "
            + "`cancel_notes` = ?13, "
            + "`checked` = ?14, "
            + "`vh` = ?15,"
            + "`to_conciliate` = ?16, "
            + "`gt_cyl_trip_id` = ?17";

    private void setFields(InvMovement obj, MySQLQuery q) {
        q.setParam(1, obj.mvDate);
        q.setParam(2, obj.typeId);
        q.setParam(3, obj.centerId);
        q.setParam(4, obj.centerDesId);
        q.setParam(5, obj.storeId);
        q.setParam(6, obj.workshopId);
        q.setParam(7, obj.factoryId);
        q.setParam(8, obj.externalId);
        q.setParam(9, obj.clientId);
        q.setParam(10, obj.lostId);
        q.setParam(11, obj.notes);
        q.setParam(12, obj.cancel);
        q.setParam(13, obj.cancelNotes);
        q.setParam(14, obj.checked);
        q.setParam(15, obj.vh);
        q.setParam(16, obj.toConciliate);
        q.setParam(17, obj.gtCylTripId);
    }

    public InvMovement select(int id, Connection ep) throws Exception {
        InvMovement obj = new InvMovement();
        MySQLQuery q = new MySQLQuery("SELECT " + selFlds + " FROM inv_movement WHERE id = " + id);
        Object[] row = q.getRecord(ep);
        obj.mvDate = MySQLQuery.getAsDate(row[0]);
        obj.typeId = MySQLQuery.getAsInteger(row[1]);
        obj.centerId = MySQLQuery.getAsInteger(row[2]);
        obj.centerDesId = MySQLQuery.getAsInteger(row[3]);
        obj.storeId = MySQLQuery.getAsInteger(row[4]);
        obj.workshopId = MySQLQuery.getAsInteger(row[5]);
        obj.factoryId = MySQLQuery.getAsInteger(row[6]);
        obj.externalId = MySQLQuery.getAsInteger(row[7]);
        obj.clientId = MySQLQuery.getAsInteger(row[8]);
        obj.lostId = MySQLQuery.getAsInteger(row[9]);
        obj.notes = MySQLQuery.getAsString(row[10]);
        obj.cancel = MySQLQuery.getAsBoolean(row[11]);
        obj.cancelNotes = MySQLQuery.getAsString(row[12]);
        obj.checked = MySQLQuery.getAsBoolean(row[13]);
        obj.vh = MySQLQuery.getAsInteger(row[14]);
        obj.toConciliate = MySQLQuery.getAsBoolean(row[15]);
        obj.gtCylTripId = MySQLQuery.getAsInteger(row[16]);

        obj.id = id;
        return obj;
    }

//fin zona de reemplazo

    public int insert(InvMovement obj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + setFlds);
        setFields(obj, q);
        return q.executeInsert(ep);
    }

    public String getInsertQuery(InvMovement obj) throws Exception {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + setFlds);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public void update(InvMovement obj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("UPDATE " + setFlds + " WHERE id = " + obj.id);
        setFields(obj, q);
        q.executeUpdate(ep);
    }

    public String getUpdateQuery(InvMovement obj) throws Exception {
        MySQLQuery q = new MySQLQuery("UPDATE " + setFlds + " WHERE id = " + obj.id);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public void delete(int id, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("DELETE FROM inv_movement WHERE id = " + id);
        q.executeDelete(ep);
    }
    
}
