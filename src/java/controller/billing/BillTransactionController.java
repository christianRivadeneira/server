package controller.billing;

import api.bill.model.BillTransaction;
import java.util.Date;
import java.sql.Connection;
import utilities.MySQLQuery;
import model.system.SessionLogin;
import utilities.MySQLPreparedInsert;
import web.billing.BillingServlet;

public class BillTransactionController {

    public static void create(BillTransaction billTrans, int cityId, String sessionId, String dbName) throws Exception {
        // No se le hara validacion porque estos datos siempres se van a crear por codigo
        try (Connection conn = BillingServlet.getConnection(cityId)) {
            SessionLogin.validate(sessionId, conn, dbName);
            billTrans.created = (new Date());
            billTrans.modified = (billTrans.created);
            MySQLPreparedInsert insertQuery = BillTransaction.getInsertQuery(true, conn);
            BillTransaction.insert(billTrans, insertQuery);
            billTrans.id = insertQuery.executeBatchWithKeys()[0];
        }
    }

    public static Integer getLastTrasactionIdByClient(int clienId, int cityId, String sessionId, String dbName) throws Exception {
        try (Connection conn = BillingServlet.getConnection(cityId)) {
            SessionLogin.validate(sessionId, conn, dbName);
            return getLastTrasactionIdByClient(clienId, conn);
        }
    }

    public static Integer getLastTrasactionIdByClient(int clienId, Connection conn) throws Exception {
        MySQLQuery q = new MySQLQuery("select MAX(id) from bill_transaction where cli_tank_id = ?1");
        q.setParam(1, clienId);
        return q.getAsInteger(conn);
    }

/*
    public static BillTransaction[] getTransactionsByDoc(int docId, String docType, int cityId, String sessionId, String dbName) throws Exception {
        try (Connection conn = BillingServlet.getConnection(cityId)) {
            SessionLogin.validate(sessionId, conn, dbName);
            return BillTransaction.getByDoc(docId, docType, conn);
        }
    }*/
}
