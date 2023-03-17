package printout.basics;

import java.sql.Connection;
import java.util.Date;
import utilities.MySQLQuery;

public class MtoChkElement {
//inicio zona de reemplazo

    public Integer id;
    public Integer elemId;
    public Integer lstId;
    public Boolean checked;
    public Boolean needReview;
    public Date revDate;
    public Integer workOrderId;
    public String correction;
    public Date corrDate;
    public String corrProv;

    private static final String selFlds = "`elem_id`, "
            + "`lst_id`, "
            + "`checked`, "
            + "`need_review`, "
            + "`rev_date`, "
            + "`work_order_id`, "
            + "`correction`, "
            + "`corr_date`, "
            + "`corr_prov`";

    private static final String setFlds = "mto_chk_element SET "
            + "`elem_id` = ?1, "
            + "`lst_id` = ?2, "
            + "`checked` = ?3, "
            + "`need_review` = ?4, "
            + "`rev_date` = ?5, "
            + "`work_order_id` = ?6, "
            + "`correction` = ?7, "
            + "`corr_date` = ?8, "
            + "`corr_prov` = ?9";

    private void setFields(MtoChkElement obj, MySQLQuery q) {
        q.setParam(1, obj.elemId);
        q.setParam(2, obj.lstId);
        q.setParam(3, obj.checked);
        q.setParam(4, obj.needReview);
        q.setParam(5, obj.revDate);
        q.setParam(6, obj.workOrderId);
        q.setParam(7, obj.correction);
        q.setParam(8, obj.corrDate);
        q.setParam(9, obj.corrProv);

    }

    public MtoChkElement select(int id, Connection ep) throws Exception {
        MtoChkElement obj = new MtoChkElement();
        MySQLQuery q = new MySQLQuery("SELECT " + selFlds + " FROM mto_chk_element WHERE id = " + id);
        Object[] row = q.getRecord(ep);
        obj.elemId = (row[0] != null ? (Integer) row[0] : null);
        obj.lstId = (row[1] != null ? (Integer) row[1] : null);
        obj.checked = (row[2] != null ? (Boolean) row[2] : null);
        obj.needReview = (row[3] != null ? (Boolean) row[3] : null);
        obj.revDate = (row[4] != null ? (Date) row[4] : null);
        obj.workOrderId = (row[5] != null ? (Integer) row[5] : null);
        obj.correction = (row[6] != null ? row[6].toString() : null);
        obj.corrDate = (row[7] != null ? (Date) row[7] : null);
        obj.corrProv = (row[8] != null ? row[8].toString() : null);

        obj.id = id;
        return obj;
    }

//fin zona de reemplazo
    public int insert(MtoChkElement obj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + setFlds);
        setFields(obj, q);
        return q.executeInsert(ep);
    }

    public void update(MtoChkElement obj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("UPDATE " + setFlds + " WHERE id = " + obj.id);
        setFields(obj, q);
        q.executeUpdate(ep);
    }

    public void delete(int id, Connection ep) throws Exception {
    }

    public int elementTypeId;
    public String name;

    public static String getQueryRows(Integer chkListId) {
        return "SELECT ce.id, "
                + "e.description, "
                + "e.id, "
                + "ce.need_review, "
                + "ce.rev_date, "
                + "ce.checked,"
                + "`work_order_id`, "
                + "`correction`, "
                + "`corr_date`, "
                + "`corr_prov`, "
                + "`elem_id` "
                + "FROM mto_chk_element AS ce "
                + "INNER JOIN mto_element AS e ON ce.elem_id = e.id "
                + "WHERE ce.lst_id = " + chkListId + " "
                + "ORDER BY need_review, e.description";
    }

    public static MtoChkElement[] getAllData(Connection ep, Integer chkListId) throws Exception {
        return getByData(new MySQLQuery(getQueryRows(chkListId)).getRecords(ep));
    }

    public static MtoChkElement[] getByData(Object[][] data) throws Exception {
        MtoChkElement[] row = new MtoChkElement[data.length];
        for (int i = 0; i < data.length; i++) {
            row[i] = new MtoChkElement();
            row[i].id = MySQLQuery.getAsInteger(data[i][0]);
            row[i].name = MySQLQuery.getAsString(data[i][1]);
            row[i].elementTypeId = MySQLQuery.getAsInteger(data[i][2]);
            row[i].needReview = MySQLQuery.getAsBoolean(data[i][3]);
            row[i].revDate = MySQLQuery.getAsDate(data[i][4]);
            row[i].checked = MySQLQuery.getAsBoolean(data[i][5]);
            row[i].workOrderId = MySQLQuery.getAsInteger(data[i][6]);
            row[i].correction = MySQLQuery.getAsString(data[i][7]);
            row[i].corrDate = MySQLQuery.getAsDate(data[i][8]);
            row[i].corrProv = MySQLQuery.getAsString(data[i][9]);
            row[i].elemId = MySQLQuery.getAsInteger(data[i][10]);
        }
        return row;
    }
}
