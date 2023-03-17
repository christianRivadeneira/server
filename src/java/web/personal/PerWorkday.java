package web.personal;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import utilities.MySQLQuery;

public class PerWorkday {
//inicio zona de reemplazo

    public int id;
    public Date payMonth;
    public int empId;
    public Date evDate;
    public Date begTime;
    public Date endTime;
    public int hourLoss;
    public Integer authorizedById;
    public int regById;
    public String notes;
    public BigDecimal exDiuSem;
    public BigDecimal exNocSem;
    public BigDecimal exDiuDom;
    public BigDecimal exNocDom;
    public BigDecimal recNocSem;
    public BigDecimal recDiuDom;
    public BigDecimal recNocDom;
    public boolean qlik;

    private static final String SEL_FLDS = "`pay_month`, "
            + "`emp_id`, "
            + "`ev_date`, "
            + "`beg_time`, "
            + "`end_time`, "
            + "`hour_loss`, "
            + "`authorized_by_id`, "
            + "`reg_by_id`, "
            + "`notes`, "
            + "`ex_diu_sem`, "
            + "`ex_noc_sem`, "
            + "`ex_diu_dom`, "
            + "`ex_noc_dom`, "
            + "`rec_noc_sem`, "
            + "`rec_diu_dom`, "
            + "`rec_noc_dom`, "
            + "`qlik`";

    private static final String SET_FLDS = "per_workday SET "
            + "`pay_month` = ?1, "
            + "`emp_id` = ?2, "
            + "`ev_date` = ?3, "
            + "`beg_time` = ?4, "
            + "`end_time` = ?5, "
            + "`hour_loss` = ?6, "
            + "`authorized_by_id` = ?7, "
            + "`reg_by_id` = ?8, "
            + "`notes` = ?9, "
            + "`ex_diu_sem` = ?10, "
            + "`ex_noc_sem` = ?11, "
            + "`ex_diu_dom` = ?12, "
            + "`ex_noc_dom` = ?13, "
            + "`rec_noc_sem` = ?14, "
            + "`rec_diu_dom` = ?15, "
            + "`rec_noc_dom` = ?16, "
            + "`qlik` = ?17";

    private static void setFields(PerWorkday obj, MySQLQuery q) {
        q.setParam(1, obj.payMonth);
        q.setParam(2, obj.empId);
        q.setParam(3, obj.evDate);
        q.setParam(4, obj.begTime);
        q.setParam(5, obj.endTime);
        q.setParam(6, obj.hourLoss);
        q.setParam(7, obj.authorizedById);
        q.setParam(8, obj.regById);
        q.setParam(9, obj.notes);
        q.setParam(10, obj.exDiuSem);
        q.setParam(11, obj.exNocSem);
        q.setParam(12, obj.exDiuDom);
        q.setParam(13, obj.exNocDom);
        q.setParam(14, obj.recNocSem);
        q.setParam(15, obj.recDiuDom);
        q.setParam(16, obj.recNocDom);
        q.setParam(17, obj.qlik);

    }

    public static PerWorkday getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        PerWorkday obj = new PerWorkday();
        obj.payMonth = MySQLQuery.getAsDate(row[0]);
        obj.empId = MySQLQuery.getAsInteger(row[1]);
        obj.evDate = MySQLQuery.getAsDate(row[2]);
        obj.begTime = MySQLQuery.getAsDate(row[3]);
        obj.endTime = MySQLQuery.getAsDate(row[4]);
        obj.hourLoss = MySQLQuery.getAsInteger(row[5]);
        obj.authorizedById = MySQLQuery.getAsInteger(row[6]);
        obj.regById = MySQLQuery.getAsInteger(row[7]);
        obj.notes = MySQLQuery.getAsString(row[8]);
        obj.exDiuSem = MySQLQuery.getAsBigDecimal(row[9], false);
        obj.exNocSem = MySQLQuery.getAsBigDecimal(row[10], false);
        obj.exDiuDom = MySQLQuery.getAsBigDecimal(row[11], false);
        obj.exNocDom = MySQLQuery.getAsBigDecimal(row[12], false);
        obj.recNocSem = MySQLQuery.getAsBigDecimal(row[13], false);
        obj.recDiuDom = MySQLQuery.getAsBigDecimal(row[14], false);
        obj.recNocDom = MySQLQuery.getAsBigDecimal(row[15], false);
        obj.qlik = MySQLQuery.getAsBoolean(row[16]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    public PerWorkday select(int id, Connection ep) throws Exception {
        return PerWorkday.getFromRow(new MySQLQuery(getSelectQuery(id)).getRecord(ep));
    }

    public int insert(PerWorkday pobj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + SET_FLDS);
        setFields(pobj, q);
        return q.executeInsert(ep);
    }

    public void update(PerWorkday pobj, Connection ep) throws Exception {
        new MySQLQuery(PerWorkday.getUpdateQuery(pobj)).executeUpdate(ep);
    }

    public static String getSelectQuery(int id) {
        return "SELECT " + SEL_FLDS + ", id FROM per_workday WHERE id = " + id;
    }

    public static String getInsertQuery(PerWorkday obj) {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + SET_FLDS);
        setFields(obj, q);
        return q.getQuery();
    }

    public static String getUpdateQuery(PerWorkday obj) {
        MySQLQuery q = new MySQLQuery("UPDATE " + SET_FLDS + " WHERE id = " + obj.id);
        setFields(obj, q);
        return q.getQuery();
    }

    public void delete(int id, Connection ep) throws Exception {
        new MySQLQuery("DELETE FROM per_workday WHERE id = " + id).executeDelete(ep);
    }

}
