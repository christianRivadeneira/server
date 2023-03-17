package api.bill.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import utilities.Dates;
import utilities.MySQLQuery;

public class BillInstCheck extends BaseModel<BillInstCheck> {
//inicio zona de reemplazo

    public int clientId;
    public Date chkDate;
    public int typeId;
    public int inspectorId;
    public String notes;
    public Integer certNum;
    public String codOnac;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "client_id",
            "chk_date",
            "type_id",
            "inspector_id",
            "notes",
            "cert_num",
            "cod_onac"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, clientId);
        q.setParam(2, chkDate);
        q.setParam(3, typeId);
        q.setParam(4, inspectorId);
        q.setParam(5, notes);
        q.setParam(6, certNum);
        q.setParam(7, codOnac);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        clientId = MySQLQuery.getAsInteger(row[0]);
        chkDate = MySQLQuery.getAsDate(row[1]);
        typeId = MySQLQuery.getAsInteger(row[2]);
        inspectorId = MySQLQuery.getAsInteger(row[3]);
        notes = MySQLQuery.getAsString(row[4]);
        certNum = MySQLQuery.getAsInteger(row[5]);
        codOnac = MySQLQuery.getAsString(row[6]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_inst_check";
    }

    public static String getSelFlds(String alias) {
        return new BillInstCheck().getSelFldsForAlias(alias);
    }

    public static List<BillInstCheck> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillInstCheck().getListFromQuery(q, conn);
    }

    public static List<BillInstCheck> getList(Params p, Connection conn) throws Exception {
        return new BillInstCheck().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillInstCheck().deleteById(id, conn);
    }

    public static List<BillInstCheck> getAll(Connection conn) throws Exception {
        return new BillInstCheck().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<BillInstCheck> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/
    /**
     *
     * @param clientId
     * @param inst
     * @param d puede ser null si es hoy
     * @param conn
     * @return
     * @throws Exception
     */
    public static InstCheckInfo getNextDates(int clientId, BillInstance inst, Date d, Connection conn) throws Exception {
        InstCheckInfo rta = new InstCheckInfo();

        if (d != null) {
            rta.lastCheck = new MySQLQuery("SELECT MAX(chk_date) FROM bill_inst_check WHERE client_id = ?1 AND chk_date <= ?2").setParam(1, clientId).setParam(2, d).getAsDate(conn);
        } else {
            rta.lastCheck = new MySQLQuery("SELECT MAX(chk_date) FROM bill_inst_check WHERE client_id = ?1").setParam(1, clientId).getAsDate(conn);

        }

        if (rta.lastCheck == null) {
            rta.lastCheck = new MySQLQuery("SELECT end_date FROM bill_span s WHERE s.id = (SELECT MIN(r.span_id) FROM bill_reading_bk r WHERE r.client_tank_id = ?1)").setParam(1, clientId).getAsDate(conn);
        }
        
        if (rta.lastCheck == null) {
            rta.lastCheck = new MySQLQuery("SELECT end_date FROM bill_span s WHERE s.id = (SELECT MIN(r.span_id) FROM bill_reading r WHERE r.client_tank_id = ?1)").setParam(1, clientId).getAsDate(conn);
        }

        if (rta.lastCheck != null) {

            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(rta.lastCheck);
            //gc.set(GregorianCalendar.YEAR, 2013);
            //gc.set(GregorianCalendar.MONTH, 2);
            gc.set(GregorianCalendar.DAY_OF_MONTH, 1);
            gc.add(GregorianCalendar.MONTH, inst.instCheckMonths - 4);
            if (gc.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SUNDAY) {
                gc.add(GregorianCalendar.DAY_OF_MONTH, 1);
            }
            rta.minDate = Dates.trimDate(gc.getTime());

            gc.setTime(rta.lastCheck);
            gc.set(GregorianCalendar.DAY_OF_MONTH, gc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
            gc.add(GregorianCalendar.MONTH, inst.instCheckMonths);
            if (gc.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SUNDAY) {
                gc.add(GregorianCalendar.DAY_OF_MONTH, -1);
            }
            rta.maxDate = Dates.trimDate(gc.getTime());
        }
        return rta;
    }

    public static class InstCheckInfo {

        public Date lastCheck;
        public Date minDate;
        public Date maxDate;

        public boolean overlaps(BillSpan s) {
            if (minDate != null && maxDate != null) {
                long min1 = Dates.trimDate(minDate).getTime();
                long min2 = Dates.trimDate(s.beginDate).getTime();
                long max1 = Dates.trimDate(maxDate).getTime();
                long max2 = Dates.trimDate(s.endDate).getTime();
                return Math.min(max1, max2) - Math.max(min1, min2) >= 0;
            }
            return false;
        }
    }
}
