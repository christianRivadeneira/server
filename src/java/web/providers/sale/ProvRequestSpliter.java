package web.providers.sale;

import api.ord.writers.FrmAttachments;
import api.sys.model.Bfile;
import api.sys.model.SysFlowReq;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import web.fileManager;
import web.fileManager.PathInfo;
import web.system.flow.SysFlowChk;
import web.system.flow.interfaces.SysFlowSplitter;

public class ProvRequestSpliter implements SysFlowSplitter {

    public static void main(String[] args) throws Exception {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost/sigma?user=root&password=root");
            split(76, conn);
        } finally {
            MySQLCommon.closeConnection(conn);
        }
    }

    @Override
    public int[] split(SysFlowReq req, SessionLogin sl, Connection conn) throws Exception {
        return split(req.id, conn);
    }

    private static int[] split(int reqId, Connection conn) throws Exception {
        Integer prId = new MySQLQuery("SELECT id "
                + "FROM prov_request r "
                + "WHERE r.sys_req_id = " + reqId + ";").getAsInteger(conn);
        Object[][] typesData = new MySQLQuery("SELECT "
                + "distinct i.art_type_id, i.kind, i.ctr_type "
                + "FROM "
                + "prov_item i where i.request_id = " + prId + ";").getRecords(conn);

        int[] ids = new int[typesData.length];
        for (int i = 0; i < typesData.length; i++) {
            Object[] typesRow = typesData[i];
            int artTypeId = MySQLQuery.getAsInteger(typesRow[0]);
            String kind = MySQLQuery.getAsString(typesRow[1]);
            int newSrId;
            int newPrId;
            if (typesData.length > 1) {
                newSrId = cloneSysReq(reqId, conn);
                newPrId = duplicateRow("prov_request", prId, conn);
            } else {
                newSrId = reqId;
                newPrId = prId;
            }
            new MySQLQuery("UPDATE prov_request "
                    + "SET sys_req_id = " + newSrId + ", art_type_id = " + artTypeId + ", kind = '" + kind + "' "
                    + "WHERE id = " + newPrId).executeUpdate(conn);

            if (typesData.length > 1) { // duplicar items y adjuntos 
                Object[][] items = new MySQLQuery("select i.id "
                        + "from prov_item i "
                        + "where i.request_id = " + prId + " AND art_type_id = " + artTypeId + " AND i.kind='" + kind + "' ; ").getRecords(conn);
                for (int j = 0; j < items.length; j++) {
                    Object[] item = items[j];
                    int newItemId = duplicateRowItem("prov_item", MySQLQuery.getAsInteger(item[0]), newPrId, conn);
                    new MySQLQuery("UPDATE bfile "
                            + "SET owner_id = " + newItemId + "  "
                            + "WHERE owner_type = " + FrmAttachments.PROV_ITEM + " AND owner_id = " + MySQLQuery.getAsInteger(item[0])).executeUpdate(conn);
                }

            }
            ids[i] = newSrId;
        }
        if (typesData.length > 1) {
            new MySQLQuery("UPDATE prov_request SET visible = 0 WHERE id = " + prId).executeUpdate(conn);
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

    public static int duplicateRows(String tbl, String fkName, int oldId, int newId, Connection conn) throws Exception {
        Object[][] flds = new MySQLQuery("SHOW FIELDS FROM " + tbl + ";").getRecords(conn);
        StringBuilder sb = new StringBuilder("INSERT INTO " + tbl + " (");
        for (Object[] fld : flds) {
            String fldName = MySQLQuery.getAsString(fld[0]);
            if (!fldName.equals("id")) {
                sb.append(fldName).append(",");
            }
        }
        sb.setLength(sb.length() - 1);
        sb.append(") (SELECT ");
        for (Object[] fld : flds) {
            String fldName = MySQLQuery.getAsString(fld[0]);
            if (!fldName.equals("id")) {
                if (!fldName.equals(fkName)) {
                    sb.append(fldName);
                } else {
                    sb.append(newId);
                }
                sb.append(",");
            }
        }
        sb.setLength(sb.length() - 1);
        sb.append(" FROM ").append(tbl).append(" WHERE ").append(fkName).append(" = ").append(oldId).append(")");
        return new MySQLQuery(sb.toString()).executeUpdate(conn);
    }

    public static void duplicateRowsFile(int origProvReqId, int newProvReqId, Connection conn) throws Exception {
        PathInfo pInfo = new fileManager.PathInfo(conn);

        MySQLQuery mq = new MySQLQuery("SELECT " + Bfile.getSelFlds("") + " FROM bfile WHERE owner_type = ?1 AND owner_id = ?2 ");
        mq.setParam(1, FrmAttachments.PROV_REQ_PPTAS);
        mq.setParam(2, origProvReqId);

        List<Bfile> listPptas = Bfile.getList(mq, conn);

        if (!MySQLQuery.isEmpty(listPptas)) {
            for (Bfile filePta : listPptas) {
                int sourceFileId = filePta.id;
                filePta.ownerId = newProvReqId;
                int destFileId = filePta.insert(conn);
                fileManager.copyFile(pInfo, sourceFileId, destFileId);
            }
        }

        mq = new MySQLQuery("SELECT " + Bfile.getSelFlds("") + " FROM bfile WHERE owner_type = ?1 AND owner_id = ?2 ");
        mq.setParam(1, FrmAttachments.PROV_REQ_CTRS);
        mq.setParam(2, origProvReqId);

        List<Bfile> listCtrs = Bfile.getList(mq, conn);

        if (!MySQLQuery.isEmpty(listCtrs)) {
            for (Bfile fileCtr : listCtrs) {
                int sourceFileId = fileCtr.id;
                fileCtr.ownerId = newProvReqId;
                int destFileId = fileCtr.insert(conn);
                fileManager.copyFile(pInfo, sourceFileId, destFileId);
            }
        }
    }

    public static int duplicateRow(String tbl, int id, Connection conn) throws Exception {
        Object[][] flds = new MySQLQuery("SHOW FIELDS FROM " + tbl + ";").getRecords(conn);
        StringBuilder sb = new StringBuilder("INSERT INTO " + tbl + " (");
        for (Object[] fld : flds) {
            String fldName = MySQLQuery.getAsString(fld[0]);
            if (!fldName.equals("id")) {
                sb.append(fldName).append(",");
            }
        }
        sb.setLength(sb.length() - 1);
        sb.append(") (SELECT ");
        for (Object[] fld : flds) {
            String fldName = MySQLQuery.getAsString(fld[0]);
            if (!fldName.equals("id")) {
                sb.append(fldName).append(",");
            }
        }
        sb.setLength(sb.length() - 1);
        sb.append(" FROM ").append(tbl).append(" WHERE id = ").append(id).append(")");
        return new MySQLQuery(sb.toString()).executeInsert(conn);
    }

    public static int duplicateRowItem(String tbl, int id, int newId, Connection conn) throws Exception {
        Object[][] flds = new MySQLQuery("SHOW FIELDS FROM " + tbl + ";").getRecords(conn);
        StringBuilder sb = new StringBuilder("INSERT INTO " + tbl + " (");
        for (Object[] fld : flds) {
            String fldName = MySQLQuery.getAsString(fld[0]);
            if (!fldName.equals("id")) {
                sb.append(fldName).append(",");
            }
        }
        sb.setLength(sb.length() - 1);
        sb.append(") (SELECT ");
        for (Object[] fld : flds) {
            String fldName = MySQLQuery.getAsString(fld[0]);
            if (!fldName.equals("id")) {
                if (!fldName.equals("request_id")) {
                    sb.append(fldName);
                } else {
                    sb.append(newId);
                }
                sb.append(",");
            }
        }
        sb.setLength(sb.length() - 1);
        sb.append(" FROM ").append(tbl).append(" WHERE id = ").append(id).append(")");
        return new MySQLQuery(sb.toString()).executeInsert(conn);
    }
}
