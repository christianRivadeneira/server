package api.bill.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import utilities.Dates;
import utilities.MySQLQuery;

public class BillMeterCheck extends BaseModel<BillMeterCheck> {
//inicio zona de reemplazo

    public int meterId;
    public Date chkDate;
    public String notes;
    public String novelty;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "meter_id",
            "chk_date",
            "notes",
            "novelty"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, meterId);
        q.setParam(2, chkDate);
        q.setParam(3, notes);
        q.setParam(4, novelty);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        meterId = MySQLQuery.getAsInteger(row[0]);
        chkDate = MySQLQuery.getAsDate(row[1]);
        notes = MySQLQuery.getAsString(row[2]);
        novelty = MySQLQuery.getAsString(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_meter_check";
    }

    public static String getSelFlds(String alias) {
        return new BillMeterCheck().getSelFldsForAlias(alias);
    }

    public static List<BillMeterCheck> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillMeterCheck().getListFromQuery(q, conn);
    }

    public static List<BillMeterCheck> getList(Params p, Connection conn) throws Exception {
        return new BillMeterCheck().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillMeterCheck().deleteById(id, conn);
    }

    public static List<BillMeterCheck> getAll(Connection conn) throws Exception {
        return new BillMeterCheck().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<BillMeterCheck> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/
    
    public static MeterCheckInfo getNextDate(int clientId, int meterId, BillInstance inst, Date d, Connection conn) throws Exception {
        MeterCheckInfo rta = new MeterCheckInfo();

        if (d != null) {
            rta.lastCheck = new MySQLQuery("SELECT MAX(chk_date) FROM bill_meter_check WHERE meter_id = ?1 AND chk_date <= ?2").setParam(1, meterId).setParam(2, d).getAsDate(conn);
        } else {
            rta.lastCheck = new MySQLQuery("SELECT MAX(chk_date) FROM bill_meter_check WHERE meter_id = ?1").setParam(1, meterId).getAsDate(conn);

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
            gc.add(GregorianCalendar.MONTH, inst.meterCheckMonths);            
            rta.nextDate = Dates.trimDate(gc.getTime());
        }
        return rta;
    }

    public static class MeterCheckInfo {

        public Date lastCheck;
        public Date nextDate;

        public boolean overlaps(BillSpan s) {
            if (nextDate != null) {
                long min = Dates.trimDate(s.beginDate).getTime();
                long d = Dates.trimDate(nextDate).getTime();
                long max = Dates.trimDate(s.endDate).getTime();
                return min <= d && d <= max;
            }
            return false;
        }
    }

}
