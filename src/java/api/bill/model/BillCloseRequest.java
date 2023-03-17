package api.bill.model;

import api.BaseModel;
import api.bill.api.BillBillApi;
import static api.bill.model.BillPrintRequest.toMin;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilities.MySQLQuery;
import web.ShortException;

public class BillCloseRequest extends BaseModel<BillCloseRequest> {
//inicio zona de reemplazo

    public int instId;
    public int empId;
    public Date begDt;
    public Date endDt;
    public String errorMsg;
    public String errorTrace;
    public String msg;
    public Integer rate;
    public String status;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "inst_id",
            "emp_id",
            "beg_dt",
            "end_dt",
            "error_msg",
            "error_trace",
            "msg",
            "rate",
            "status"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, instId);
        q.setParam(2, empId);
        q.setParam(3, begDt);
        q.setParam(4, endDt);
        q.setParam(5, errorMsg);
        q.setParam(6, errorTrace);
        q.setParam(7, msg);
        q.setParam(8, rate);
        q.setParam(9, status);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        instId = MySQLQuery.getAsInteger(row[0]);
        empId = MySQLQuery.getAsInteger(row[1]);
        begDt = MySQLQuery.getAsDate(row[2]);
        endDt = MySQLQuery.getAsDate(row[3]);
        errorMsg = MySQLQuery.getAsString(row[4]);
        errorTrace = MySQLQuery.getAsString(row[5]);
        msg = MySQLQuery.getAsString(row[6]);
        rate = MySQLQuery.getAsInteger(row[7]);
        status = MySQLQuery.getAsString(row[8]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_close_request";
    }

    public static String getSelFlds(String alias) {
        return new BillCloseRequest().getSelFldsForAlias(alias);
    }

    public static List<BillCloseRequest> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillCloseRequest().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillCloseRequest().deleteById(id, conn);
    }

    public static List<BillCloseRequest> getAll(Connection conn) throws Exception {
        return new BillCloseRequest().getAllList(conn);
    }

//fin zona de reemplazo
    private Connection conn;
    private int totalTicks;
    private int currentTick;
    private long tickingStart;

    @Override
    public int insert(Connection conn) throws Exception {
        this.conn = conn;
        return super.insert(conn);
    }

    public void tick() throws Exception {
        currentTick++;
        int newAvg = (int) (currentTick / (double) totalTicks * 100d);
        if (rate != newAvg) {
            rate = newAvg;
            int secs = (int) ((((System.currentTimeMillis() - tickingStart) / rate) * (100 - rate)) / 1000);
            msg = "Procesando los datos de los clientes, faltan " + toMin(secs);
            update(conn);
        }
    }

    public void setTotalClicks(int totalTicks) {
        this.rate = 0;
        this.totalTicks = totalTicks;
        this.tickingStart = System.currentTimeMillis();
    }

    public void setConnection(Connection conn) {
        this.conn = conn;
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

    public void setStaus(String status, String message) throws Exception {
        this.status = status;
        this.msg = message;
        this.update(conn);
    }
}
