package api.bill.beans;

import api.bill.model.BillClientTank;
import api.bill.model.BillInstCheck;
import api.bill.model.BillInstCheckPoll;
import api.bill.model.BillInstance;
import api.bill.model.BillMeterCheck;
import api.bill.model.BillMeterCheckAlert;
import api.ord.model.OrdPqrRequest;
import api.sys.model.SysCfg;
import java.io.File;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import service.MySQL.MySQLCommon;
import utilities.Dates;
import utilities.MySQLQuery;
import utilities.SysTask;
import utilities.cast;
import web.ShortException;
import web.fileManager;
import web.marketing.smsClaro.ClaroSmsSender;
import web.quality.SendMail;

@Singleton
@Startup
public class BillTasks {

    public static final String POOL_NAME = "sigmads";
    public static final String TZ = "GMT-05:00";

    @Schedule(hour = "1", minute = "30")
    public void deleteOldCriticalPictures() {
        try (Connection conn = MySQLCommon.getConnection(POOL_NAME, TZ)) {

            Object[][] data = new MySQLQuery("SELECT f.id FROM "
                    + "ord_pqr_request r "
                    + "INNER JOIN bfile f ON r.id = f.owner_id AND f.owner_type = 143 "
                    + "where r.bill_req_type = 'reading' "
                    + "AND r.creation_date < DATE_SUB(CURDATE(),INTERVAL 45 DAY)").getRecords(conn);

            fileManager.PathInfo pi = new fileManager.PathInfo(conn);

            for (Object[] row : data) {
                Integer fid = cast.asInt(row, 0);
                File f = pi.getExistingFile(fid);
                if (f != null) {
                    f.delete();
                    new MySQLQuery("DELETE FROM bfile WHERE id = ?1").setParam(1, fid).executeUpdate(conn);
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(BillTasks.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Schedule(hour = "11", minute = "13")
    public void instCheck() {
        long today = Dates.trimDate(new Date()).getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MMM/yy");
        final ClaroSmsSender sender = new ClaroSmsSender();
        try (Connection conn = MySQLCommon.getConnection(POOL_NAME, TZ)) {
            SysTask t = new SysTask(BillTasks.class, 1, conn);
            try {
                SysCfg sysCfg = SysCfg.select(conn);
                List<BillInstance> insts = BillInstance.getAll(conn);
                for (int i = 0; i < insts.size(); i++) {
                    BillInstance inst = insts.get(i);
                    if (inst.id == 205) {
                        inst.useInstance(conn);
                        MySQLQuery q = new MySQLQuery("SELECT c.id, (SELECT bill_meter.id FROM bill_meter WHERE client_id = c.id ORDER BY start_span_id DESC LIMIT 1) "
                                + "FROM "
                                + "bill_client_tank c "
                                + "WHERE c.active");
                        Object[][] data = q.getRecords(conn);

                        for (Object[] row : data) {
                            int billClientId = cast.asInt(row, 0);
                            Integer meterId = cast.asInt(row, 1);

                            BillInstCheck.InstCheckInfo ic = BillInstCheck.getNextDates(billClientId, inst, null, conn);
                            BillMeterCheck.MeterCheckInfo mc = null;
                            if (meterId != null) {
                                mc = BillMeterCheck.getNextDate(billClientId, meterId, inst, null, conn);
                            }

                            GregorianCalendar gc = new GregorianCalendar();
                            if (ic.maxDate != null) {
                                gc.setTime(ic.maxDate);
                                gc.add(GregorianCalendar.DAY_OF_MONTH, -10);

                                if (today == ic.minDate.getTime() || today == Dates.trimDate(gc.getTime()).getTime()) {
                                    BillClientTank c = new BillClientTank().select(billClientId, conn);
                                    if (inst.sendSms && c.smsBill) {
                                        final String msg = "Recuerde reportar su certificado de conformidad antes de " + sdf.format(ic.maxDate) + " para evitar suspensiones. Mas informacion al 3102578506";
                                        final List<String> phones = c.getPhonesForSMS();
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                for (int j = 0; j < phones.size(); j++) {
                                                    sender.sendMsg(msg, "1", phones.get(j));
                                                }
                                            }
                                        }).start();
                                    }

                                    if (inst.sendMail && c.mailBill) {
                                        String mailMsg = "Señor usuario, Montagas S.A E.S.P aun no ha recibido el certificado de conformidad de la revisión periódica de la resolución CREG 059/2012, por lo tanto se informa que el plazo máximo para la entrega de éste documento es el próximo " + sdf.format(ic.maxDate) + ", de lo contrario se procederá a la suspensión del servicio. Para mayor información comuníquese a la línea 3102578506.";
                                        SendMail.sendBillMail(sysCfg, c.mail, "Recordatorio Importante", mailMsg, mailMsg, null, null, null, null);
                                    }
                                }

                                gc.setTime(ic.maxDate);
                                gc.add(GregorianCalendar.DAY_OF_MONTH, -5);
                                if (today >= Dates.trimDate(gc.getTime()).getTime()) {
                                    if (new MySQLQuery("SELECT COUNT(*) = 0 FROM bill_inst_check_poll WHERE client_id = ?1 AND limit_date = ?2").setParam(1, billClientId).setParam(2, ic.maxDate).getAsBoolean(conn)) {
                                        BillInstCheckPoll p = new BillInstCheckPoll();
                                        p.clientId = billClientId;
                                        p.limitDate = ic.maxDate;
                                        p.insert(conn);
                                    }
                                }

                                //orden de corte
                                gc.setTime(ic.maxDate);
                                gc.add(GregorianCalendar.DAY_OF_MONTH, 1);
                                if (today == Dates.trimDate(gc.getTime()).getTime()) {
                                    OrdPqrRequest rq = new OrdPqrRequest();
                                    rq.createdId = 1;
                                    rq.clientTankId = new MySQLQuery("SELECT c.id "
                                            + " FROM sigma.ord_pqr_client_tank c "
                                            + " WHERE c.mirror_id = ?1 AND c.bill_instance_id = ?2")
                                            .setParam(1, billClientId).setParam(2, inst.id).getAsInteger(conn);
                                    rq.creationDate = new Date();
                                    rq.notes = "No reporta certificado de conformidad";
                                    rq.spanId = null;
                                    rq.instanceId = inst.id;
                                    rq.billReqType = "srv_cut";
                                    rq.numMeter = null;
                                    new MySQLQuery("USE sigma").executeUpdate(conn);
                                    rq.insert(conn);
                                    inst.useInstance(conn);
                                }
                            }

                            if (mc != null && mc.nextDate != null) {
                                gc.setTime(mc.nextDate);
                                gc.add(GregorianCalendar.DAY_OF_MONTH, -1);
                                if (today >= Dates.trimDate(gc.getTime()).getTime()) {
                                    if (new MySQLQuery("SELECT COUNT(*) = 0 FROM bill_meter_check_alert WHERE meter_id = ?1 AND limit_date = ?2").setParam(1, meterId).setParam(2, mc.nextDate).getAsBoolean(conn)) {
                                        BillMeterCheckAlert p = new BillMeterCheckAlert();
                                        p.meterId = meterId;
                                        p.limitDate = mc.nextDate;
                                        p.done = false;
                                        p.insert(conn);
                                    }
                                }
                            }
                        }
                    }
                }
                new MySQLQuery("USE sigma;").executeUpdate(conn);
                t.success(conn);
            } catch (ShortException ex) {
                ex.simplePrint();
            } catch (Exception e) {
                Logger.getLogger(BillTasks.class.getName()).log(Level.SEVERE, null, e);
                new MySQLQuery("USE sigma;").executeUpdate(conn);
                t.error(e, conn);
            }
        } catch (Exception ex) {
            Logger.getLogger(BillTasks.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
