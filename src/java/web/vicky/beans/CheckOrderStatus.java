package web.vicky.beans;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.apache.commons.lang3.exception.ExceptionUtils;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import web.marketing.cylSales.TrkSale;
import web.push.FCMClient;
import web.push.GCMUtils;
import web.quality.SendMail;
import web.vicky.model.CliePromo;
import web.vicky.model.Cyl;
import web.vicky.model.OrdCylOrder;
import web.vicky.model.OrdCylOrderOffer;
import web.vicky.model.OrdCylOrderTimer;
import web.vicky.model.OrderInfo;
import web.vicky.model.Salesman;
import web.vicky.model.VickyCfg;
import web.vicky.servlets.ChangeOfferStatus;

@Stateless
public class CheckOrderStatus {

    @Resource
    TimerService timerService;

    public void scheduleCheck(int orderId) {
        try (Connection conn = MySQLCommon.getConnection("sigmads", null)) {
            VickyCfg cfg = VickyCfg.select(1, conn);
            if (new MySQLQuery("SELECT COUNT(*) > 0 FROM ord_cyl_order o WHERE o.driver_id IS NULL AND o.cancel_cause_id IS NULL AND o.id = " + orderId).getAsBoolean(conn)) {
                Timer t = timerService.createTimer(0, cfg.roundTimeSecs * 1000, new TaskInfo(orderId, TaskInfo.CHECK));
                OrdCylOrderTimer ot = new OrdCylOrderTimer();
                ot.orderId = orderId;
                ot.setHandle(t.getHandle());
                OrdCylOrderTimer.insert(ot, conn);
            }
        } catch (Exception ex) {
            Logger.getLogger(CheckOrderStatus.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Timeout
    public void timerTick(Timer timer) {
        try {
            TaskInfo info = (TaskInfo) timer.getInfo();
            switch (info.type) {
                case TaskInfo.CHECK:
                    check(info.orderId);
                    break;
                case TaskInfo.SCHEDULE:
                    scheduleCheck(info.orderId);
                    break;
                default:
                    throw new RuntimeException();
            }
        } catch (RuntimeException ex) {
            Logger.getLogger(CheckOrderStatus.class.getName()).log(Level.SEVERE, "message", ex);
        }
    }

    private void check(int orderId) {
        try (Connection conn = MySQLCommon.getConnection("sigmads", null)) {
            final VickyCfg cfg = VickyCfg.select(1, conn);
            OrdCylOrderOffer.cancelOffers(orderId, cfg, true, conn);
            final OrdCylOrderTimer ot = OrdCylOrderTimer.getActiveByOrderId(orderId, conn);
            if (ot != null) {
                final OrderInfo info = OrderInfo.getInfo(orderId, conn);
                if (cfg.editOrders && info.vhId != null) {
                    terminate(ot, false, cfg, conn);
                    return;
                }
                final List<Salesman> choosen = new ArrayList<>();
                if ((info.lat != null && info.lon != null) && cfg.maxGpsRounds > 0) {
                    if (ot.round >= cfg.maxGpsRounds) {
                        terminate(ot, true, cfg, conn);
                        return;
                    }
                    List<Salesman> salesmen = Salesman.getSalesmen(info.lat, info.lon, info.officeId, ot, cfg, conn);
                    GregorianCalendar gc = new GregorianCalendar();
                    gc.add(GregorianCalendar.SECOND, cfg.roundTimeSecs * -2);
                    Date offeredLim = gc.getTime();

                    do {
                        ot.lastRadius += cfg.radiousDeltaKm;
                        for (int i = 0; i < salesmen.size(); i++) {
                            Salesman sm = salesmen.get(i);
                            if ((sm.lastOffered == null || sm.lastOffered.compareTo(offeredLim) < 0) && sm.d < ot.lastRadius) {
                                choosen.add(sm);
                            }
                        }
                    } while (choosen.isEmpty() && ot.lastRadius <= cfg.maxRadiousKm);

                    if (choosen.isEmpty() || ot.lastRadius > cfg.maxRadiousKm) {
                        terminate(ot, true, cfg, conn);
                        return;
                    }
                } else {
                    if (ot.round >= cfg.maxSectorRounds) {
                        terminate(ot, true, cfg, conn);
                        return;
                    }
                    List<Salesman> salesmen;
                    //por solicitud de comercial se inabilitan las rondas por ciudad
                    salesmen = Salesman.getSalesmen(info.sectorId, ot, cfg, conn);
                    if (salesmen.isEmpty()) {
                        terminate(ot, true, cfg, conn);
                        return;
                    }
                    for (int i = 0; i < salesmen.size(); i++) {
                        choosen.add(salesmen.get(i));
                    }
                }

                final CountDownLatch latch = new CountDownLatch(choosen.size());
                for (int i = 0; i < choosen.size(); i++) {
                    final Salesman sm = choosen.get(i);
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                offerOrderToSalesman(info, sm.empId, sm.vhId, sm.lat, sm.lon, ot.round, cfg, conn);
                            } catch (Exception ex) {
                                Logger.getLogger(CheckOrderStatus.class.getName()).log(Level.INFO, "message", ex);
                            } finally {
                                latch.countDown();
                            }
                        }
                    });
                    t.start();
                }
                latch.await();
                ot.round++;
                OrdCylOrderTimer.update(ot, conn);
            }
        } catch (Exception ex) {
            Logger.getLogger(CheckOrderStatus.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static boolean offerOrderToSalesman(OrderInfo info, int empId, int vhId, Double smLat, Double smLon, int round, VickyCfg cfg, Connection conn) throws Exception {
        if (round >= 0) {
            OrdCylOrderOffer o = new OrdCylOrderOffer();
            o.orderId = info.orderId;
            o.empId = empId;
            o.vhId = vhId;
            o.round = round;
            o.lat = (smLat != null ? BigDecimal.valueOf(smLat) : null);
            o.lon = (smLon != null ? BigDecimal.valueOf(smLon) : null);
            OrdCylOrderOffer.insert(o, conn);

            JsonObjectBuilder ob = Json.createObjectBuilder();
            ob.add("type", "orderOffer");
            ob.add("offerId", o.id);
            ob.add("orderId", info.orderId);
            if (info.lat != null) {
                ob.add("lat", info.lat);
                ob.add("lon", info.lon);
                ob.add("clieCoords", info.clieCoords);
            }
            ob.add("indexId", info.indexId);
            ob.add("address", info.address);
            ob.add("neigh", info.neigh);
            ob.add("sector", info.sector);
            ob.add("document", info.document);
            ob.add("names", info.names);
            ob.add("duration", cfg.roundTimeSecs);
            ob.add("begin", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
            ob.add("round", o.round);
            ob.add("phone", new MySQLQuery("SELECT phones FROM ord_contract_index WHERE id = " + info.indexId).getAsString(conn));
            if (info.landMark != null && !info.landMark.isEmpty()) {
                ob.add("landMark", info.landMark);
            }
            JsonArrayBuilder cab = Json.createArrayBuilder();
            for (Cyl cyl : info.cyls) {
                JsonObjectBuilder cob = Json.createObjectBuilder();
                cob.add("name", cyl.name);
                cob.add("amount", cyl.amount);
                cab.add(cob);
            }
            ob.add("cyls", cab);
            JsonObject json = ob.build();
            GCMUtils.sendToApp(cfg.appId, json, String.valueOf(o.empId), conn);
            o.offerDt = new Date();
            OrdCylOrderOffer.update(o, conn);
            return true;
        }
        return false;
    }

    private synchronized static boolean claim(VickyCfg cfg, OrdCylOrderOffer o, Connection conn) throws Exception {
        if (new MySQLQuery("SELECT COUNT(*) = 1 FROM ord_cyl_order WHERE assig_by_id IS NULL AND id = ?1").setParam(1, o.orderId).getAsBoolean(conn)) {
            if (cfg.editOrders) {
                Integer entId = new MySQLQuery("SELECT en.id FROM "
                        + "vehicle v "
                        + "INNER JOIN agency a ON v.agency_id = a.id "
                        + "INNER JOIN enterprise en ON a.enterprise_id = en.id "
                        + "WHERE v.id = " + o.vhId).getAsInteger(conn);
                new MySQLQuery("UPDATE ord_cyl_order set assig_by_id = ?2, assig_hour = now(), driver_id = ?2, vehicle_id = ?3, enterprise_id = ?4 WHERE id = ?1").setParam(1, o.orderId).setParam(2, o.empId).setParam(3, o.vhId).setParam(4, entId).executeUpdate(conn);
            }
            return true;
        }
        return false;
    }

    public static Integer changeOfferStatus(int offerId, String status, String extra, String deviceId, VickyCfg cfg, TrkSale sale, Connection conn) throws Exception {
        try {
            OrdCylOrderOffer o = OrdCylOrderOffer.select(offerId, conn);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
            long t = System.currentTimeMillis();
            if (o != null) {
                o.log = o.log == null ? "" : o.log;
                o.log += ("@" + df.format(new Date()) + " " + status + " " + (extra != null ? extra : ""));
            }
            if (o == null) {
                return ChangeOfferStatus.NOT_FOUND;
            }

            if (cfg.testEmpId != null && cfg.testEmpId == o.empId) {
                cfg.editOrders = false;
            }
            Integer ret = null;
            switch (status) {
                case "ack":
                    if (o == null) {
                        throw new Exception("Offer is null: " + offerId);
                    }
                    if (o.ackDt == null && o.acceptDt == null && o.rejectDt == null && o.timeoutDt == null) {
                        o.ackDt = new Date();
                        o.ackType = extra;
                    }
                    o.log += (" SUCCESS");
                    ret = ChangeOfferStatus.SUCCESS;
                    break;
                case "accept":
                    if (o.timeoutDt != null || o.rejectDt != null) {
                        o.log += (" FAIL");
                        ret = ChangeOfferStatus.FAIL;
                    } else if (o.acceptDt != null) {
                        boolean rta = o.acceptDevice == null ? true : (Objects.equals(o.acceptDevice, deviceId != null ? Long.valueOf(deviceId) : null));
                        if (rta) {
                            o.log += (" SUCCESS");
                            ret = ChangeOfferStatus.SUCCESS;
                        } else {
                            o.log += (" FAIL");
                            ret = ChangeOfferStatus.FAIL;
                        }
                    } else { //PEDIDO LIBRE
                        boolean claimed = claim(cfg, o, conn);
                        if (claimed) {
                            o.acceptDevice = deviceId != null ? Long.valueOf(deviceId) : null;
                            o.acceptDt = new Date();
                            if (cfg.editOrders) {
                                OrdCylOrderOffer.cancelOffers(o.orderId, cfg, o.empId, true, conn);
                                sendToClient(o.orderId, o.empId, o.vhId, MySQLQuery.getAsDouble(o.lat), MySQLQuery.getAsDouble(o.lon), "orderAccepted", null, null, null, conn);
                                cancelTimer(o.orderId, conn);
                            }
                            o.log += (" SUCCESS");
                            ret = ChangeOfferStatus.SUCCESS;
                        } else {
                            o.timeoutDt = new Date();
                            o.log += (" FAIL");
                            ret = ChangeOfferStatus.FAIL;
                        }
                    }
                    break;
                case "arrive":
                    if (o.arriveDt == null) {
                        sendToClient(o.orderId, o.empId, o.vhId, null, null, "orderArrived", null, null, null, conn);
                    }
                    o.arriveDt = new Date();
                    o.log += (" SUCCESS");
                    ret = ChangeOfferStatus.SUCCESS;
                    break;
                case "reject":
                    if (o.acceptDt != null) {
                        if (o.acceptDevice != null && Objects.equals(o.acceptDevice, deviceId != null ? Long.valueOf(deviceId) : null)) {
                            o.rejectDt = new Date();
                            backoff(o, extra, cfg, conn);
                        }
                    } else if (o.timeoutDt == null) {
                        o.rejectDt = new Date();
                    }
                    o.log += (" SUCCESS");
                    ret = ChangeOfferStatus.SUCCESS;
                    break;
                case "timeout":
                    if (o.acceptDt != null) {
                        if (o.acceptDevice != null && Objects.equals(o.acceptDevice, deviceId != null ? Long.valueOf(deviceId) : null)) {
                            o.timeoutDt = new Date();
                            backoff(o, extra, cfg, conn);
                        }
                    } else if (o.rejectDt == null) {
                        o.timeoutDt = new Date();
                    }
                    o.log += (" SUCCESS");
                    ret = ChangeOfferStatus.SUCCESS;
                    break;
                case "confirm":
                    ///no se llama desde cliente, sólo desde cylsales
                    o.confirmDt = new Date();
                    if (cfg.editOrders) {
                        new MySQLQuery("INSERT INTO ord_ord_sale SET cyl_order_id = " + o.orderId + ", trk_sale_id = " + sale.id).executeInsert(conn);
                        new MySQLQuery("UPDATE ord_cyl_order set app_confirmed = 1,  confirmed_by_id = ?2, confirm_hour = now() WHERE id = ?1").setParam(1, o.orderId).setParam(2, o.empId).executeUpdate(conn);
                        //Marcación del pedido como to_poll
                        if (new Random().nextDouble() <= new MySQLQuery("SELECT cyl_poll_ratio/100 FROM ord_cfg WHERE id = 1").getAsDouble(conn)) {
                            new MySQLQuery("UPDATE ord_cyl_order SET to_poll = 1 WHERE id = " + o.orderId).executeUpdate(conn);
                        }
                        CliePromo.checkPromoTicket(o.orderId, sale, o.empId, conn);
                    }
                    o.log += (" SUCCESS");
                    ret = ChangeOfferStatus.SUCCESS;
                    break;
                case "cancel":
                    o.cancelDt = new Date();
                    if (cfg.editOrders) {
                        new MySQLQuery("UPDATE ord_cyl_order set cancel_cause_id = " + extra + ", cancelled_by = ?2 WHERE id = ?1").setParam(1, o.orderId).setParam(2, o.empId).executeUpdate(conn);
                    }
                    String cancelCause = new MySQLQuery("SELECT description FROM ord_cancel_cause WHERE id = " + extra).getAsString(conn);
                    sendToClient(o.orderId, o.empId, o.vhId, null, null, "cancelled", "Lo sentimos", "El vendedor no podrá completar el pedido", "Indicó como motivo: " + cancelCause + "\nPor favor inténtelo en unos momentos", conn);
                    o.log += (" SUCCESS");
                    ret = ChangeOfferStatus.SUCCESS;
                    break;
                case "backoff":
                    backoff(o, extra, cfg, conn);
                    String backOffCause = new MySQLQuery("SELECT name FROM ord_backoff_cause WHERE id = " + extra).getAsString(conn);
                    sendToClient(o.orderId, o.empId, o.vhId, null, null, "backedoff", "Lo sentimos", "El vendedor no podrá completar el pedido", "Indicó como motivo: " + backOffCause + "\nPor favor inténtelo en unos momentos", conn);
                    o.log += (" SUCCESS");
                    ret = ChangeOfferStatus.SUCCESS;
                    break;
                default:
                    throw new Exception("Unknown status: " + status);
            }
            o.log += (" took " + (System.currentTimeMillis() - t) + "ms");
            OrdCylOrderOffer.update(o, conn);
            return ret;
        } catch (Exception ex) {
            Logger.getLogger(CheckOrderStatus.class.getName()).log(Level.SEVERE, "message", ex);
            String stack = ExceptionUtils.getStackTrace(ex);
            try {
                SendMail.sendMail(conn, "karol.mendoza@montagas.com.co, qualisysapps@gmail.com", "Error Vicky", stack, stack);
            } catch (Exception ex1) {
                Logger.getLogger(CheckOrderStatus.class.getName()).log(Level.SEVERE, null, ex1);
            }
            throw ex;
        }
    }

    public static void sendToClient(int orderId, Integer empId, Integer vhId, Double lat, Double lon, String status, String msg1, String msg2, String msg3, Connection conn) throws Exception {
        Integer clieId = new MySQLQuery("SELECT i.contract_id FROM ord_cyl_order o INNER JOIN ord_contract_index i ON o.index_id = i.id  WHERE o.id = " + orderId + " AND i.`type` = 'app'").getAsInteger(conn);
        if (clieId != null) {
            Integer appId = new MySQLQuery("SELECT id FROM system_app WHERE package_name = 'qualisys.co.com.montagas'").getAsInteger(conn);

            JsonObjectBuilder ob = Json.createObjectBuilder();
            ob.add("type", status);
            ob.add("orderId", orderId);
            if (empId != null) {
                Integer photoId = new MySQLQuery("select b.id from employee e inner join per_employee pe on e.per_employee_id = pe.id inner join bfile b on b.owner_id = pe.id and b.owner_type = 10 where e.id = " + empId + ";").getAsInteger(conn);
                if (photoId != null) {
                    ob.add("photoId", photoId);
                }
                ob.add("driver", new MySQLQuery("SELECT CONCAT(first_name, ' ', last_name) FROM employee WHERE id = " + empId).getAsString(conn));
                ob.add("drvId", empId);
            }

            if (vhId != null) {
                ob.add("vh", new MySQLQuery("SELECT plate FROM vehicle WHERE id = " + vhId).getAsString(conn));
            }

            if (msg1 != null) {
                ob.add("msg1", msg1);
            }

            if (msg2 != null) {
                ob.add("msg2", msg2);
            }

            if (msg3 != null) {
                ob.add("msg3", msg3);
            }

            ob.add("timer", "5000");
            ob.add("mult", "1.5");
            if (lat != null && lon != null) {

                ob.add("vhLat", lat);
                ob.add("vhLon", lon);

            }
            Object[] usrCoords = new MySQLQuery("select i.lat, i.lon from ord_cyl_order o  inner join ord_contract_index i on o.index_id = i.id where o.id = " + orderId).getRecord(conn);
            ob.add("clLat", MySQLQuery.getAsDouble(usrCoords[0]));
            ob.add("clLon", MySQLQuery.getAsDouble(usrCoords[1]));

            FCMClient.sendToCliAppAsync(appId, ob.build(), clieId + "");
        }
    }

    public static void sendMsgToClient(int clieId, String title, String subtitle, String msg, Connection conn) throws Exception {
        Integer appId = new MySQLQuery("SELECT id FROM system_app WHERE package_name = 'qualisys.co.com.montagas'").getAsInteger(conn);
        JsonObjectBuilder ob = Json.createObjectBuilder();
        ob.add("type", "msg");

        if (title != null) {
            ob.add("msg1", title);
        }

        if (subtitle != null) {
            ob.add("msg2", subtitle);
        }

        if (msg != null) {
            ob.add("msg3", msg);
        }
        FCMClient.sendToCliApp(appId, ob.build(), clieId + "", conn);
    }

    private static void backoff(OrdCylOrderOffer o, String extra, VickyCfg cfg, Connection conn) throws Exception {
        o.backoffDt = new Date();
        if (extra != null) {
            o.backoffCauseId = Integer.valueOf(extra);
        }
        if (cfg.editOrders) {
            new MySQLQuery("UPDATE ord_cyl_order set enterprise_id = null, assig_by_id = null, assig_hour = null, driver_id = null, vehicle_id = null, wait_to_app = 0 WHERE id = ?1").setParam(1, o.orderId).executeUpdate(conn);
        }
    }

    private static void cancelTimer(int orderId, Connection conn) throws Exception {
        OrdCylOrderTimer t = OrdCylOrderTimer.getActiveByOrderId(orderId, conn);
        if (t != null) {
            t.cancelTimer(conn);
        }
    }

    public static synchronized void cancelOrderOffer(int orderId, Integer causeId, Integer empId, boolean offers, Connection conn) throws Exception {
        if (offers) {
            VickyCfg cfg = VickyCfg.select(1, conn);
            OrdCylOrderOffer.cancelOffers(orderId, cfg, false, conn);
            cancelTimer(orderId, conn);
        }
        if (causeId != null) {
            OrdCylOrder order = OrdCylOrder.select(orderId, conn);
            order.cancelCauseId = causeId;
            order.cancelledBy = empId;
            order.called = false;
            order.toPoll = false;
            order.delivered = false;
            if (order.pollId != null) {
                new MySQLQuery("DELETE FROM ord_poll WHERE id = " + order.pollId).executeDelete(conn);
                order.pollId = null;
            }
            order.update(order, conn);
        }
    }

    private void terminate(OrdCylOrderTimer t, boolean notifyClient, VickyCfg cfg, Connection conn) throws Exception {
        t.cancelTimer(conn);
        if (notifyClient) {
            Integer timers = new MySQLQuery("SELECT COUNT(*) FROM ord_cyl_order_timer WHERE order_id = " + t.orderId + " ").getAsInteger(conn);
            if (timers % 2 != 0) {
                timerService.createTimer(cfg.pauseTimeSecs * 1000, new TaskInfo(t.orderId, TaskInfo.SCHEDULE));
            } else {
                new MySQLQuery("UPDATE ord_cyl_order set wait_to_app = 0 WHERE id = ?1").setParam(1, t.orderId).executeDelete(conn);
                sendToClient(t.orderId, null, null, null, null, "noDriver", "Lo sentimos", "No hay vendedores disponibles", "Por favor inténtelo en unos momentos", conn);
            }
        }
    }

}
