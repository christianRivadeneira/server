package web.providers.sale;

import api.ord.writers.FrmAttachments;
import api.sys.model.SysFlowReq;
import java.sql.Connection;
import java.sql.DriverManager;
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import static web.providers.sale.ProvRequestSpliter.duplicateRow;
import static web.providers.sale.ProvRequestSpliter.duplicateRowItem;
import static web.providers.sale.ProvRequestSpliter.duplicateRows;
import static web.providers.sale.ProvRequestSpliter.duplicateRowsFile;
import web.system.flow.SysFlowChk;
import web.system.flow.interfaces.SysFlowSplitter;

public class ProvOrderSpliter implements SysFlowSplitter {

    public static void main(String[] args) throws Exception {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/sigma?user=root&password=root");
            split(13, conn);
        } finally {
            MySQLCommon.closeConnection(conn);
        }
    }

    @Override
    public int[] split(SysFlowReq req, SessionLogin sl, Connection conn) throws Exception {
        return split(req.id, conn);
    }

    private static int[] split(int origSysFlowReqId, Connection conn) throws Exception {

        Integer empId = new MySQLQuery("SELECT emp_id "
                + "FROM sys_flow_chk "
                + "WHERE req_id = " + origSysFlowReqId + " "
                + "ORDER BY create_dt DESC LIMIT 1").getAsInteger(conn);

        Integer origRequestId = new MySQLQuery("SELECT id "
                + "FROM prov_request r "
                + "WHERE r.sys_req_id = " + origSysFlowReqId + ";").getAsInteger(conn);

        Object[][] provsData = new MySQLQuery("SELECT "
                + "distinct i.provider_id, "
                + "i.pay_method_id, i.ctr_type, "
                + "i.approv_by, i.quote_serial, i.coin_id, i.term_id "
                + "FROM prov_item i "
                + "WHERE "
                + "i.provider_id IS NOT NULL AND "
                + "i.request_id = " + origRequestId + ";").getRecords(conn);

        Integer cNull = new MySQLQuery("SELECT COUNT(*) "
                + "FROM prov_item i "
                + "WHERE "
                + "i.provider_id IS NULL AND "
                + "i.request_id = " + origRequestId + ";").getAsInteger(conn);

        int[] ids = new int[provsData.length];

        for (int i = 0; i < provsData.length; i++) {
            Object[] provsRow = provsData[i];
            int providerId = MySQLQuery.getAsInteger(provsRow[0]);
            int payMethodId = MySQLQuery.getAsInteger(provsRow[1]);
            String ctrType = MySQLQuery.getAsString(provsRow[2]);
            String approvBy = MySQLQuery.getAsString(provsRow[3]);
            String quoteSerial = MySQLQuery.getAsString(provsRow[4]);
            Integer coinId = MySQLQuery.getAsInteger(provsRow[5]);
            int termId = MySQLQuery.getAsInteger(provsRow[6]);

            int newSysFlowReqId;
            int newProvRequestId;
            if ((provsData.length + cNull) > 1) {
                newSysFlowReqId = cloneSysReq(origSysFlowReqId, conn);
                newProvRequestId = duplicateRow("prov_request", origRequestId, conn);
            } else {
                newSysFlowReqId = origSysFlowReqId;
                newProvRequestId = origRequestId;
            }

            int serial = new MySQLQuery("INSERT INTO prov_req_serial set serial = null;").executeInsert(conn);
            new MySQLQuery("UPDATE prov_request SET term_id = " + termId + ", coin_id = " + coinId + ", quote_serial = ?1 , "
                    + "purch_serial = '" + (serial + 1) + "', rev_1 = " + empId + ", "
                    + "sys_req_id = " + newSysFlowReqId + ", provider_id = " + providerId + ", "
                    + "pay_method_id = " + payMethodId + ", ctr_type = '" + ctrType + "', approv_by = '" + approvBy + "' "
                    + "WHERE id = " + newProvRequestId).setParam(1, quoteSerial).executeUpdate(conn);

            if ((provsData.length + cNull) > 1) { // duplicar items y adjuntos 
                Object[][] items = new MySQLQuery("SELECT i.id "
                        + "FROM prov_item i "
                        + "WHERE i.request_id = " + origRequestId + " AND "
                        + "provider_id = " + providerId + " AND "
                        + "i.pay_method_id=" + payMethodId + " AND "
                        + "i.ctr_type='" + ctrType + "' AND "
                        + "i.approv_by='" + approvBy + "' AND "
                        + "i.term_id=" + termId + "  "
                        + "; ").getRecords(conn);
                for (int j = 0; j < items.length; j++) {
                    Object[] item = items[j];
                    int newItemId = duplicateRowItem("prov_item", MySQLQuery.getAsInteger(item[0]), newProvRequestId, conn);

                    new MySQLQuery("UPDATE bfile "
                            + "SET owner_id = " + newItemId + "  "
                            + "WHERE owner_type = " + FrmAttachments.PROV_ITEM + " "
                            + "AND owner_id = " + MySQLQuery.getAsInteger(item[0])).executeUpdate(conn);
                    new MySQLQuery("DELETE FROM prov_item WHERE id = " + MySQLQuery.getAsInteger(item[0])).executeUpdate(conn);

                }

            }
            if (!origRequestId.equals(newProvRequestId)) {
                duplicateRowsFile(origRequestId, newProvRequestId, conn);
            }
            ids[i] = newSysFlowReqId;
        }

        if (provsData.length > 1 && cNull == 0) {
            new MySQLQuery("UPDATE prov_request SET visible = 0 WHERE id = " + origRequestId).executeUpdate(conn);
        }
        return ids;
    }

    private static int cloneSysReq(int oldSrId, Connection conn) throws Exception {
        int newSrId = duplicateRow("sys_flow_req", oldSrId, conn);
        duplicateRows("sys_flow_chk", "req_id", oldSrId, newSrId, conn);
        duplicateRows("sys_flow_subs", "req_id", oldSrId, newSrId, conn);

        SysFlowChk oldCheck = SysFlowChk.getCurByReqId(oldSrId, conn);
        Integer newCheckId = new MySQLQuery("select id from sys_flow_chk c where c.from_step_id = ?1 and c.create_dt = ?3 and c.req_id = ?4").setParam(1, oldCheck.fromStepId).setParam(3, oldCheck.createDt).setParam(4, newSrId).getAsInteger(conn);
        new MySQLQuery("update sys_flow_req SET cur_chk_id = " + newCheckId + " WHERE id = " + newSrId).executeUpdate(conn);
        return newSrId;
    }

}
