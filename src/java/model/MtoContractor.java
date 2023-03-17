package model;

import java.sql.Connection;
import utilities.MySQLQuery;

public class MtoContractor {
//inicio zona de reemplazo   

    public Integer id;
    public String name;
    public String firstName;
    public String lastName;
    public String document;
    public Boolean active;
    public Integer perEmployeeId;
    public String notes;

    private static final String SEL_FLDS = "`name`, "
            + "`first_name`, "
            + "`last_name`, "
            + "`document`, "
            + "`active`,"
            + "per_employee_id,"
            + "notes ";
    private static final String SET_FLDS = "mto_contractor SET "
            + "`name` = ?1, "
            + "`first_name` = ?2, "
            + "`last_name` = ?3, "
            + "`document` = ?4, "
            + "`active` = ?5,"
            + "per_employee_id = ?6,"
            + "notes = ?7 ";

    private static void setFields(MtoContractor obj, MySQLQuery q) {
        q.setParam(1, obj.name);
        q.setParam(2, obj.firstName);
        q.setParam(3, obj.lastName);
        q.setParam(4, obj.document);
        q.setParam(5, obj.active);
        q.setParam(6, obj.perEmployeeId);
        q.setParam(7, obj.notes);
    }

//fin zona de reemplazo
    public MtoContractor select(int id, Connection ep) throws Exception {
        return getFromRow(new MySQLQuery(getSelectQuery(id, ep)).getRecord(ep));
    }

    public static String getSelectQuery(int id, Connection ep) throws Exception {
        return "SELECT " + SEL_FLDS + ", id FROM mto_contractor WHERE id = " + id;
    }

    public static MtoContractor getFromRow(Object[] row) {
        MtoContractor obj = new MtoContractor();
        obj.name = (row[0] != null ? row[0].toString() : null);
        obj.firstName = (row[1] != null ? row[1].toString() : null);
        obj.lastName = (row[2] != null ? row[2].toString() : null);
        obj.document = (row[3] != null ? row[3].toString() : null);
        obj.active = (row[4] != null ? (Boolean) row[4] : null);
        obj.perEmployeeId = (row[5] != null ? (Integer) row[5] : null);
        obj.notes = (row[6] != null ? (String) row[6] : null);
        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

    private static final String TO_STR_FLDS = "CONCAT(first_name, ' ', last_name)";

    public int insert(MtoContractor obj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + SET_FLDS);
        setFields(obj, q);
        return q.executeInsert(ep);
    }

    public void update(MtoContractor obj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("UPDATE " + SET_FLDS + " WHERE id = " + obj.id);
        setFields(obj, q);
        q.executeUpdate(ep);
    }

    public void delete(int id, Connection ep) throws Exception {
    }

    public static String getInsertQuery(MtoContractor obj){
        MySQLQuery q = new MySQLQuery("INSERT INTO " + SET_FLDS);
        setFields(obj, q);
        return q.getQuery();
    }

    public static String getUpdateQuery(MtoContractor obj){
        MySQLQuery q = new MySQLQuery("UPDATE " + SET_FLDS + " WHERE id = " + obj.id);
        setFields(obj, q);
        return q.getQuery();
    }
    
}
