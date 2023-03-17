package api.bill.writers.bill;

import api.bill.model.BillCfg;
import api.bill.model.BillInstance;
import api.bill.model.BillSpan;
import api.sys.model.SysCfg;
import com.lowagie.text.pdf.Barcode128;
import controller.billing.BillSpanController;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import utilities.MySQLQuery;

public abstract class BillWriter {

    protected String pobName;
    protected SysCfg sysCfg;
    protected BillCfg billCfg;
    protected Connection billConn;
    protected Boolean printLogo;
    protected BillInstance inst;
    protected Object[][] rawBankData;
    protected BillSpan span;
    protected File fout;
    private static final SimpleDateFormat SDF_BARCODE = new SimpleDateFormat("yyyyMMdd");

    public static BillWriter getCurrentPdfWriter(BillInstance inst, SysCfg sysCfg, BillCfg cfg, Connection billConn, File fout, Boolean printLogo) throws Exception {
        BillWriter rta = (BillWriter) Class.forName(cfg.billWriterClass).newInstance();
        rta.begin(inst, sysCfg, cfg, billConn, printLogo, fout);
        return rta;
    }

    public static BillWriter getCurrentZplWriter(BillInstance inst, SysCfg sysCfg, BillCfg cfg, Connection billConn, File fout, Boolean printLogo) throws Exception {
        //BillWriter rta = (BillWriter) Class.forName(cfg.billWriterClass).newInstance();
        BillWriter w = new BillWriterNetZPL();
        w.begin(inst, sysCfg, cfg, billConn, printLogo, fout);
        return w;
    }

    public void begin(BillInstance inst, SysCfg sysCfg, BillCfg cfg, Connection billConn, Boolean printLogo, File fout) throws Exception {
        this.billConn = billConn;
        this.printLogo = printLogo;
        this.inst = inst;
        this.sysCfg = sysCfg;
        this.billCfg = cfg;
        this.fout = fout;
        rawBankData = new MySQLQuery("SELECT b.bill_label FROM bill_bank b WHERE b.bill_label IS NOT NULL ORDER BY b.bill_label").getRecords(billConn);
        pobName = new MySQLQuery("SELECT p.name FROM sigma.dane_poblado p WHERE p.id = ?1").setParam(1, inst.pobId).getAsString(billConn);
        prepare(billConn);
    }

    public abstract void prepare(Connection ep) throws Exception;

    public abstract File endDocument() throws Exception;

    public void addBill(BillForPrint bill) throws Exception{
        if (span == null) {
            span = new BillSpan().select(bill.spanId, billConn);
        } else {
            if (span.id != bill.spanId) {
                throw new Exception("No se pueden mezclar facturas de diferentes periodos.");
            }
        }
    }

    public String ifNull(String s1, String s2) {
        return (s1 != null && !s1.equals("") ? s1 : s2);
    }

    public static String getStringBarcode(String billNum, BigDecimal amount, Date date, String invoicing, boolean isZebra) throws Exception {        
        String strDate = SDF_BARCODE.format(date);
        char pdfFnc = Barcode128.FNC1;
        String zebraFnc = ">8";

        StringBuilder barCode = new StringBuilder();

        //INVOICING 415 EAN.UCC Global Location Number of the invoicing party n3+n13 Pay To
        barCode.append("415");
        if (invoicing.length() != 13) {
            throw new Exception("Invoicing party must be 13 characters long.");
        }
        barCode.append(invoicing);
        //REFERENCE 8020 Payment slip reference number variable, up to 25
        barCode.append("8020");
        billNum = BillSpanController.zeroFill(billNum, 10);
        if (billNum.length() > 25) {
            throw new Exception("Reference number must not be more than 25 characters long.");
        }
        barCode.append(billNum);
        if (billNum.length() < 25) {
            barCode.append(isZebra ? zebraFnc : pdfFnc);
        }
        //390y Amount payable (local currency)	variable, up to 15
        barCode.append("3900");
        //String strAmount = barCodeAmoutFormat.format(amount);
        //la norma dice que el número debe se par, pero el banco pide que se fije a 10
        String strAmount = String.format("%010d", amount.intValue());
        if (strAmount.length() > 15) {
            throw new Exception("Amount payable must not be more than 15 characters long.");
        }
        barCode.append(strAmount);
        barCode.append(isZebra ? zebraFnc : pdfFnc);

        //91-99	Internal Company Codes	variable, up to 30
        barCode.append("96");
        barCode.append(strDate);

        return barCode.toString();
    }
    
}
