package api.bill.model;

import api.BaseModel;
import api.bill.api.BillBillApi;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilities.MySQLQuery;
import web.ShortException;

public class BillPrintRequest extends BaseModel<BillPrintRequest> {

//inicio zona de reemplazo

    public int instId;
    public Integer bfileId;
    public int empId;
    public Date begDt;
    public Date endDt;
    public int requestedBills;
    public String msg;
    public String errorMsg;
    public String errorTrace;
    public String status;
    public Integer rate;
    public Integer fileLenKb;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "inst_id",
            "bfile_id",
            "emp_id",
            "beg_dt",
            "end_dt",
            "requested_bills",
            "msg",
            "error_msg",
            "error_trace",
            "status",
            "rate",
            "file_len_kb"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, instId);
        q.setParam(2, bfileId);
        q.setParam(3, empId);
        q.setParam(4, begDt);
        q.setParam(5, endDt);
        q.setParam(6, requestedBills);
        q.setParam(7, msg);
        q.setParam(8, errorMsg);
        q.setParam(9, errorTrace);
        q.setParam(10, status);
        q.setParam(11, rate);
        q.setParam(12, fileLenKb);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        instId = MySQLQuery.getAsInteger(row[0]);
        bfileId = MySQLQuery.getAsInteger(row[1]);
        empId = MySQLQuery.getAsInteger(row[2]);
        begDt = MySQLQuery.getAsDate(row[3]);
        endDt = MySQLQuery.getAsDate(row[4]);
        requestedBills = MySQLQuery.getAsInteger(row[5]);
        msg = MySQLQuery.getAsString(row[6]);
        errorMsg = MySQLQuery.getAsString(row[7]);
        errorTrace = MySQLQuery.getAsString(row[8]);
        status = MySQLQuery.getAsString(row[9]);
        rate = MySQLQuery.getAsInteger(row[10]);
        fileLenKb = MySQLQuery.getAsInteger(row[11]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_print_request";
    }

    public static String getSelFlds(String alias) {
        return new BillPrintRequest().getSelFldsForAlias(alias);
    }

    public static List<BillPrintRequest> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillPrintRequest().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillPrintRequest().deleteById(id, conn);
    }

    public static List<BillPrintRequest> getAll(Connection conn) throws Exception {
        return new BillPrintRequest().getAllList(conn);
    }

//fin zona de reemplazo
    private Connection conn;

    @Override
    public int insert(Connection conn) throws Exception {
        this.conn = conn;
        return super.insert(conn);
    }

    public void update() throws Exception {
        update(conn);
    }

    private int totalTicks;
    private int currentTick;
    private long tickingStart;

    public void tick() throws Exception {
        currentTick++;
        int newAvg = (int) (currentTick / (double) totalTicks * 100d);
        if (rate != newAvg) {
            rate = newAvg;
            int secs = (int) ((((System.currentTimeMillis() - tickingStart) / rate) * (100 - rate)) / 1000);
            msg = "Generando facturas, faltan " + toMin(secs);
            update();
        }
    }

    public static String toMin(int secs) {
        int m = (secs - (secs % 60)) / 60;
        int s = secs - (m * 60);
        return String.format("%02d:%02d:%02d", 0, m, s);
    }

    public void setTotalClicks(int totalTicks) {
        this.rate = 0;
        this.totalTicks = totalTicks;
        this.tickingStart = System.currentTimeMillis();
    }

    public void setMessage(String message) throws Exception {
        this.msg = message;
        this.update(conn);
    }

    public void setException(Exception ex) throws Exception {
        Logger.getLogger(BillBillApi.class.getName()).log(Level.SEVERE, null, ex);
        ShortException se = new ShortException(ex);
        errorMsg = se.getMessage();
        String stack = se.getSimpleStack();
        errorTrace = stack.substring(0, Math.min(4096, stack.length()));
        this.status = "fail";
        this.update(conn);
    }

    public void setStatus(String status, String message) throws Exception {
        this.status = status;
        this.msg = message;
        this.update(conn);
    }
}
