package api.sys.model;

import api.BaseAPI;
import api.BaseModel;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import metadata.log.Diff;
import metadata.log.LogData;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import static utilities.MySQLQuery.now;
import utilities.json.JSONEncoder;

public class SysCrudLog extends BaseModel<SysCrudLog> {
//inicio zona de reemplazo

    public String table;
    public int ownerSerial;
    public Integer billInstId;
    public Date dt;
    public int employeeId;
    public String json;
    public Integer sessionId;
    public String type;
    public String txt;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "table",
            "owner_serial",
            "bill_inst_id",
            "dt",
            "employee_id",
            "json",
            "session_id",
            "type",
            "txt"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, table);
        q.setParam(2, ownerSerial);
        q.setParam(3, billInstId);
        q.setParam(4, dt);
        q.setParam(5, employeeId);
        q.setParam(6, json);
        q.setParam(7, sessionId);
        q.setParam(8, type);
        q.setParam(9, txt);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        table = MySQLQuery.getAsString(row[0]);
        ownerSerial = MySQLQuery.getAsInteger(row[1]);
        billInstId = MySQLQuery.getAsInteger(row[2]);
        dt = MySQLQuery.getAsDate(row[3]);
        employeeId = MySQLQuery.getAsInteger(row[4]);
        json = MySQLQuery.getAsString(row[5]);
        sessionId = MySQLQuery.getAsInteger(row[6]);
        type = MySQLQuery.getAsString(row[7]);
        txt = MySQLQuery.getAsString(row[8]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "sys_crud_log";
    }

    public static String getSelFlds(String alias) {
        return new SysCrudLog().getSelFldsForAlias(alias);
    }

    public static List<SysCrudLog> getList(MySQLQuery q, Connection conn) throws Exception {
        return new SysCrudLog().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new SysCrudLog().deleteById(id, conn);
    }

    public static List<SysCrudLog> getAll(Connection conn) throws Exception {
        return new SysCrudLog().getAllList(conn);
    }

//fin zona de reemplazo
    public static void created(BaseAPI caller, Object obj, String txt, Connection conn) throws Exception {
        custom(caller, obj, "crea", txt, conn);
    }

    public static void updated(BaseAPI caller, Object obj, String txt, Connection conn) throws Exception {
        custom(caller, obj, "upd", txt, conn);
    }

    private static void custom(BaseAPI caller, Object obj, String type, String txt, Connection conn) throws Exception {
        SysCrudLog l = new SysCrudLog();
        l.billInstId = caller.getBillInstId();
        l.dt = now(conn);
        l.employeeId = caller.getSession(conn).employeeId;
        l.sessionId = caller.getSession(conn).id;
        l.ownerSerial = ((BaseModel) obj).id;
        l.table = Diff.getTableName(obj);
        l.type = type;
        l.txt = txt;
        l.insert(conn);
    }

    public static void created(BaseAPI caller, Object obj, Connection conn) throws Exception {
        SysCrudLog l = new SysCrudLog();
        l.billInstId = caller.getBillInstId();
        l.dt = now(conn);
        SessionLogin session = caller.getSession(conn);
        l.employeeId = session.employeeId;
        l.sessionId = session.id;
        l.ownerSerial = ((BaseModel) obj).id;
        l.table = Diff.getTableName(obj);
        l.type = "crea";
        l.insert(conn);
    }

    public static void updated(BaseAPI caller, Object newObj, Object oldObj, Connection conn) throws Exception {
        SysCrudLog l = new SysCrudLog();
        l.billInstId = caller.getBillInstId();
        l.dt = now(conn);
        l.employeeId = caller.getSession(conn).employeeId;
        l.sessionId = caller.getSession(conn).id;
        boolean hasData;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            LogData data = LogData.getFromObjs(oldObj, newObj);
            hasData = !data.flds.isEmpty();
            JSONEncoder.encode(data, baos, true);
            l.json = new String(baos.toByteArray());
        }
        l.ownerSerial = ((BaseModel) oldObj).id;
        l.table = Diff.getTableName(newObj);
        l.type = "upd";
        if (hasData) {
            l.insert(conn);
        }
    }

    public static void deleted(BaseAPI caller, Class cs, int id, Connection conn) throws Exception {
        SysCrudLog l = new SysCrudLog();
        l.billInstId = caller.getBillInstId();
        l.dt = now(conn);
        l.employeeId = caller.getSession(conn).employeeId;
        l.sessionId = caller.getSession(conn).id;
        l.ownerSerial = id;
        l.table = Diff.toDbName(cs.getSimpleName());
        l.type = "del";
        l.insert(conn);
    }

}
