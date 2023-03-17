package api.bill.model;

import api.BaseModel;
import api.bill.Locking;
import api.bill.writers.bill.BillForPrint;
import api.bill.writers.bill.BillWriter;
import api.bill.writers.bill.GetBills;
import api.sys.model.SysCfg;
import controller.billing.BillImportAsoc2001;
import static controller.billing.BillImportAsoc2001.removeLeftZeroes;
import controller.billing.BillSpanController;
import controller.billing.BillTransactionController;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import model.billing.BillBank;
import model.billing.BillSusp;
import utilities.MySQLParametrizable;
import utilities.MySQLPreparedInsert;
import utilities.MySQLPreparedUpdate;
import utilities.MySQLQuery;

public class BillBill extends BaseModel<BillBill> {

    //debe ser el número de campos más 1
    private static final int ID_PARAM = 17;
//inicio zona de reemplazo
    public String billNum;
    public Date creationDate;
    public Date paymentDate;
    public Date registDate;
    public int creatorId;
    public Integer registrarId;
    public Integer bankId;
    public int billSpanId;
    public int clientTankId;
    public boolean active;
    public int months;
    public int cauNoteId;
    public boolean total;
    public Integer lastTransId;
    public Integer ticket;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "bill_num",
            "creation_date",
            "payment_date",
            "regist_date",
            "creator_id",
            "registrar_id",
            "bank_id",
            "bill_span_id",
            "client_tank_id",
            "active",
            "months",
            "cau_note_id",
            "total",
            "last_trans_id",
            "ticket"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, billNum);
        q.setParam(2, creationDate);
        q.setParam(3, paymentDate);
        q.setParam(4, registDate);
        q.setParam(5, creatorId);
        q.setParam(6, registrarId);
        q.setParam(7, bankId);
        q.setParam(8, billSpanId);
        q.setParam(9, clientTankId);
        q.setParam(10, active);
        q.setParam(11, months);
        q.setParam(12, cauNoteId);
        q.setParam(13, total);
        q.setParam(14, lastTransId);
        q.setParam(15, ticket);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        billNum = MySQLQuery.getAsString(row[0]);
        creationDate = MySQLQuery.getAsDate(row[1]);
        paymentDate = MySQLQuery.getAsDate(row[2]);
        registDate = MySQLQuery.getAsDate(row[3]);
        creatorId = MySQLQuery.getAsInteger(row[4]);
        registrarId = MySQLQuery.getAsInteger(row[5]);
        bankId = MySQLQuery.getAsInteger(row[6]);
        billSpanId = MySQLQuery.getAsInteger(row[7]);
        clientTankId = MySQLQuery.getAsInteger(row[8]);
        active = MySQLQuery.getAsBoolean(row[9]);
        months = MySQLQuery.getAsInteger(row[10]);
        cauNoteId = MySQLQuery.getAsInteger(row[11]);
        total = MySQLQuery.getAsBoolean(row[12]);
        lastTransId = MySQLQuery.getAsInteger(row[13]);
        ticket = MySQLQuery.getAsInteger(row[14]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_bill";
    }

    public static String getSelFlds(String alias) {
        return new BillBill().getSelFldsForAlias(alias);
    }

    public static List<BillBill> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillBill().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillBill().deleteById(id, conn);
    }

    public static List<BillBill> getAll(Connection conn) throws Exception {
        return new BillBill().getAllList(conn);
    }

//fin zona de reemplazo
    public String calculateSetFlds() throws Exception {
        StringBuilder sb = new StringBuilder("bill_bill SET ");
        String[] flds = getFlds();
        for (int i = 0; i < flds.length; i++) {
            String fld = flds[i];
            sb.append("`").append(fld).append("` = ?").append(i + 1).append("");
            if (i < flds.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    //se hace con base en el prepareQuery
    private static void setFields(BillBill obj, MySQLParametrizable q) throws SQLException {
        q.setParameter(1, obj.billNum);
        q.setParameter(2, obj.creationDate);
        q.setParameter(3, obj.paymentDate);
        q.setParameter(4, obj.registDate);
        q.setParameter(5, obj.creatorId);
        q.setParameter(6, obj.registrarId);
        q.setParameter(7, obj.bankId);
        q.setParameter(8, obj.billSpanId);
        q.setParameter(9, obj.clientTankId);
        q.setParameter(10, obj.active);
        q.setParameter(11, obj.months);
        q.setParameter(12, obj.cauNoteId);
        q.setParameter(13, obj.total);
        q.setParameter(14, obj.lastTransId);
        q.setParameter(15, obj.ticket);
    }

    public static BillBill getById(Integer id, Connection conn) throws Exception {
        return new BillBill().select(id, conn);
    }

    public static String getPaymentReference(int instId, BillBill bill) {
        String instCode = String.valueOf(instId);
        String ref = String.valueOf(instCode.length()) + instCode + String.valueOf(bill.id);
        ref = BillSpanController.zeroFill(ref, 10);
        return ref;
    }

    public static MySQLPreparedInsert getInsertQuery(Connection conn) throws Exception {
        String SET_FLDS = new BillBill().calculateSetFlds();
        return new MySQLPreparedInsert("INSERT INTO " + SET_FLDS, true, conn);
    }

    public static MySQLPreparedUpdate getUpdateQuery(Connection conn) throws Exception {
        String SET_FLDS = new BillBill().calculateSetFlds();
        return new MySQLPreparedUpdate("UPDATE " + SET_FLDS + " WHERE id = ?" + ID_PARAM, conn);
    }

    public static void insert(BillBill bill, MySQLPreparedInsert q) throws Exception {
        setFields(bill, q);
        q.addBatch();
    }

    public static void update(BillBill bill, MySQLPreparedUpdate q) throws Exception {
        setFields(bill, q);
        q.setParameter(ID_PARAM, bill.id);
        q.addBatch();
    }

    public static BillBill getByBillNum(String origRef, Connection conn) throws Exception {
        BillImportAsoc2001.RefInfo info = BillImportAsoc2001.RefInfo.getInfo(origRef);
        BillBill bill = new BillBill().select(info.id, conn);
        if (bill != null && !removeLeftZeroes(bill.billNum).equals(removeLeftZeroes(origRef))) {
            throw new Exception("No coincide " + bill.billNum + "!= " + origRef);
        }
        return bill;
    }

    public static void payBill(int billId, Date payDate, int bankId, int empId, BillInstance inst, Connection conn) throws Exception {
        //verificaciones
        try {
            Locking.lock(inst.id);
            BillBill bill = new BillBill().select(billId, conn);
            BillSpan reca = BillSpan.getByClient("reca", bill.clientTankId, inst, conn);
            if (bill.billSpanId != reca.id) {
                throw new Exception("El cupón pertenece a otro periodo de recaudo, no puede ingresar.");
            }
            if (bill.paymentDate != null) {
                BillBank bank = BillBank.select(bill.bankId, conn);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
                throw new Exception("El cupón ya ha sido pagado.\nBanco: " + bank.name + "\nRegistro: " + sdf.format(bill.registDate));
            }
            if (!bill.active) {
                throw new Exception("El cupón ha sido anulado por cambios en la cuenta del cliente.");
            }
            //parche para eviitar transacciones duplicadas
            //con una lista fija
            /*List<Integer> lista = new ArrayList<Integer>(Arrays.asList(1,14,6,7,28,29,17,18,13,12));
            //boolean canInsert=canInsertTransaction2(billId,lista,conn);*/
            
            Object[][] transacciones = new MySQLQuery("SELECT cli_tank_id, bill_span_id, SUM(value), account_deb_id, account_cred_id, trans_type_id, doc_type, doc_id, " + empId + ", " + empId + ", NOW(), NOW(), " + bankId + " FROM bill_plan p WHERE p.doc_id = " + billId + " AND p.doc_type = 'fac' group by account_cred_id").getRecords(conn);
            
            for(int i=0; i<= transacciones.length-1;i++){
                boolean canInsert=canInsertTransaction((int)transacciones[i][7],(int)transacciones[i][4],conn);
                    //insertar cada transaccion
                if(canInsert){
                    new MySQLQuery("INSERT INTO bill_transaction (cli_tank_id, bill_span_id, value, account_deb_id, account_cred_id, trans_type_id, doc_type, doc_id, cre_usu_id, mod_usu_id, created, modified, bill_bank_id) VALUES "
                    + "("+transacciones[i][0]+","+transacciones[i][1]+", "+transacciones[i][2]+", "+transacciones[i][3]+", "+transacciones[i][4]+", "+transacciones[i][5]+", "+"'"+transacciones[i][6]+"'"+", "+transacciones[i][7]+", "+transacciones[i][8]+", "+transacciones[i][9]+", "+"'"+transacciones[i][10]+"'"+", "+"'"+transacciones[i][11]+"'"+", "+transacciones[i][12]
                        +")").executeInsert(conn);
                    //new MySQLQuery(q1).executeUpdate(conn);
                }
                else{
                    System.out.println("No se puede insertar otro valor al mismo documento con el mismo id de acout credit");
                }
            }
            
            ///lastId
            bill.lastTransId = (BillTransactionController.getLastTrasactionIdByClient(bill.clientTankId, conn));
            bill.paymentDate = (payDate);
            bill.registDate = new Date();
            bill.registrarId = (empId);
            bill.bankId = (bankId);
            MySQLPreparedUpdate updateBillPs = BillBill.getUpdateQuery(conn);
            BillBill.update(bill, updateBillPs);
            updateBillPs.executeBatch();

            MySQLPreparedUpdate stopSuspQ = BillSusp.getStopSuspQ(conn);
            MySQLPreparedUpdate progReconQ = BillSusp.getProgReconQ(conn);
            BillSusp.setParams(stopSuspQ, bill);
            BillSusp.setParams(progReconQ, bill);
            stopSuspQ.executeBatch();
            progReconQ.executeBatch();
        } finally {
            Locking.unlock(inst.id);
        }
    }
    public static boolean canInsertTransaction(int docId, int idCuentCred, Connection conn) throws Exception{
        boolean canInsert=true;
        BigDecimal value= new MySQLQuery("SELECT value "
                + "FROM bill_transaction "
                + "WHERE doc_id ="+docId+" AND created BETWEEN CURDATE() AND CURDATE()+1 AND doc_type ='fac' AND account_cred_id ="+idCuentCred).getAsBigDecimal(conn, false);
        if(value!=null){
            canInsert=false;
        }
        return canInsert;
    }
    /*public static boolean canInsertTransaction2(int docId, List lista, Connection conn) throws Exception{
        boolean canInsert=true;
        //recorrido parcial
        int contador=0;
        boolean encontrado=false;
        while (encontrado==false && contador<=lista.size()-1){
            
            BigDecimal value= new MySQLQuery("SELECT value "
                + "FROM bill_transaction "
                + "WHERE doc_id ="+docId+" AND created BETWEEN CURDATE() AND CURDATE()+1 AND doc_type ='fac' AND account_cred_id ="+lista.get(contador)).getAsBigDecimal(conn, false);
            if(value!=null){
                encontrado=true;
            }
            if(encontrado){
                canInsert=false;
            }
            contador+=1;
        }
        
        return canInsert;
    }*/

    public static void anullActiveBills(int clientId, int recaId, Connection billConn) throws Exception {
        new MySQLQuery("UPDATE bill_bill SET active = 0 WHERE bill_span_id = " + recaId + " AND payment_date IS NULL AND client_tank_id = " + clientId).executeUpdate(billConn);
    }

    public static File reprint(int billId, BillInstance inst, Connection conn) throws Exception {
        inst.useDefault(conn);
        SysCfg sysCfg = SysCfg.select(conn);
        inst.useInstance(conn);
        BillCfg cfg = new BillCfg().select(1, conn);
        File f = File.createTempFile("bill", ".pdf");
        BillForPrint bill = GetBills.getById(billId, inst, conn);
        BillWriter writer = BillWriter.getCurrentPdfWriter(inst, sysCfg, cfg, conn, f, true);
        writer.addBill(bill);
        writer.endDocument();
        return f;
    }
}
