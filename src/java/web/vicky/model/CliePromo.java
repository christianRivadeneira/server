package web.vicky.model;

import java.sql.Connection;
import java.util.Date;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import utilities.MySQLQuery;
import web.marketing.cylSales.TrkSale;
import web.push.GCMUtils;
import web.vicky.beans.CheckOrderStatus;

public class CliePromo {
//inicio zona de reemplazo

    public int id;
    public Date begDate;
    public Integer validity;
    public Integer price;
    public Integer amount;
    public boolean avail;
    public boolean chkMail;

    private static final String SEL_FLDS = "`beg_date`, "
            + "`validity`, "
            + "`price`, "
            + "`amount`, "
            + "`avail`, "
            + "`chk_mail`";

    private static final String SET_FLDS = "clie_promo SET "
            + "`beg_date` = ?1, "
            + "`validity` = ?2, "
            + "`price` = ?3, "
            + "`amount` = ?4, "
            + "`avail` = ?5, "
            + "`chk_mail` = ?6";

    private static void setFields(CliePromo obj, MySQLQuery q) {
        q.setParam(1, obj.begDate);
        q.setParam(2, obj.validity);
        q.setParam(3, obj.price);
        q.setParam(4, obj.amount);
        q.setParam(5, obj.avail);
        q.setParam(6, obj.chkMail);
    }

    public static CliePromo getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        CliePromo obj = new CliePromo();
        obj.begDate = MySQLQuery.getAsDate(row[0]);
        obj.validity = MySQLQuery.getAsInteger(row[1]);
        obj.price = MySQLQuery.getAsInteger(row[2]);
        obj.amount = MySQLQuery.getAsInteger(row[3]);
        obj.avail = MySQLQuery.getAsBoolean(row[4]);
        obj.chkMail = MySQLQuery.getAsBoolean(row[5]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    public CliePromo select(int id, Connection conn) throws Exception {
        return CliePromo.getFromRow(new MySQLQuery(getSelectQuery(id)).getRecord(conn));
    }

    public int insert(CliePromo obj, Connection conn) throws Exception {
        int nId = new MySQLQuery(CliePromo.getInsertQuery(obj)).executeInsert(conn);
        obj.id = nId;
        return nId;
    }

    public void update(CliePromo pobj, Connection conn) throws Exception {
        new MySQLQuery(CliePromo.getUpdateQuery((CliePromo) pobj)).executeUpdate(conn);
    }

    public static String getSelectQuery(int id) {
        return "SELECT " + SEL_FLDS + ", id FROM clie_promo WHERE id = " + id;
    }

    public static String getInsertQuery(CliePromo obj) {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + SET_FLDS);
        setFields(obj, q);
        return q.getQuery();
    }

    public static String getUpdateQuery(CliePromo obj) {
        MySQLQuery q = new MySQLQuery("UPDATE " + SET_FLDS + " WHERE id = " + obj.id);
        setFields(obj, q);
        return q.getQuery();
    }

    public void delete(int id, Connection conn) throws Exception {
        new MySQLQuery("DELETE FROM clie_promo WHERE id = " + id).executeDelete(conn);
    }

    public synchronized static void checkPromoTicket(int orderId, TrkSale sale, int empId, Connection conn) throws Exception {
        OrdCylOrder order = OrdCylOrder.select(orderId, conn);
        Object[] clieRow = new MySQLQuery("SELECT c.id, c.mail FROM "
                + "ord_contract_index i "
                + "INNER JOIN clie_usr c ON c.id = i.contract_id AND i.type = 'app' "
                + "WHERE i.id = " + order.indexId).getRecord(conn);

        //para trabajar los bonos con confirmación del cliente, 04/09/2017
        //if (clieRow != null && order.cancelCauseId == null && order.confirmedById != null && order.appConfirmed && order.clieConfirmed) {
        if (clieRow != null && order.cancelCauseId == null && order.confirmedById != null && order.appConfirmed) {
            Integer clieId = MySQLQuery.getAsInteger(clieRow[0]);
            String clieMail = MySQLQuery.getAsString(clieRow[1]);

            Object[] ticketRow = new MySQLQuery("SELECT t.id, p.price, s.date, p.id, s.credit, s.courtesy "
                    + "FROM clie_promo_ticket t "
                    + "INNER JOIN trk_sale s ON s.id = t.orig_sale "
                    + "INNER JOIN clie_promo p ON p.id = t.promo_id "
                    + "WHERE t.clie_id = " + clieId + " "
                    + "AND ?1 <= DATE_ADD(s.date, INTERVAL p.validity DAY)"
                    + "AND p.avail").setParam(1, order.day).getRecord(conn);

            if (ticketRow != null) {
                if (!MySQLQuery.getAsBoolean(ticketRow[4]) && !MySQLQuery.getAsBoolean(ticketRow[5])) {//no juegan ventas a crédito ni cortesias
                    Integer salePosition = new MySQLQuery("SELECT "
                            + "COUNT(*) "
                            + "FROM ord_cyl_order o "
                            + "INNER JOIN ord_cyl_order_offer of ON of.order_id = o.id "
                            + "INNER JOIN ord_contract_index i ON o.index_id = i.id "
                            + "WHERE i.contract_id = ?1 "
                            + "AND i.`type` = 'app' "
                            + "AND of.confirm_dt IS NOT NULL "
                            + "AND of.confirm_dt > ?2").setParam(1, clieId).setParam(2, MySQLQuery.getAsDate(ticketRow[2])).getAsInteger(conn);
                    //aquí había bug, AND of.offer_dt > ?2, hacía que no te de el bono en la 2 sino en la 3 compra, Tener en cuenta para evaluar posibles bonos no entregados

                    Object[][] claimerSales = new MySQLQuery("SELECT claimer_sale FROM clie_promo_condition WHERE promo_id = " + MySQLQuery.getAsInteger(ticketRow[3])).getRecords(conn);
                    for (int i = 0; i < claimerSales.length; i++) {
                        if (MySQLQuery.getAsInteger(claimerSales[i][0]).equals(salePosition + 1)) {
                            new MySQLQuery("INSERT INTO clie_promo_claim_sale SET "
                                    + "clie_id = " + clieId + ", "
                                    + "promo_id = " + MySQLQuery.getAsInteger(ticketRow[3]) + ", "
                                    + "claim_sale = " + sale.id).executeInsert(conn);
                            notifyPromoSman(MySQLQuery.getAsInteger(ticketRow[1]), order.id, empId, sale, conn);
                        }
                    }
                }
            } else {
                Object[] promoRow = new MySQLQuery("SELECT id, price, chk_mail FROM clie_promo WHERE beg_date <= ?1 AND avail").setParam(1, order.day).getRecord(conn);
                if (promoRow != null) {
                    int promoId = MySQLQuery.getAsInteger(promoRow[0]);
                    int price = MySQLQuery.getAsInteger(promoRow[1]);
                    boolean checkMail = MySQLQuery.getAsBoolean(promoRow[2]);
                    boolean choosen = true;
                    if (checkMail) {
                        choosen = new MySQLQuery("SELECT COUNT(*) > 0 FROM clie_promo_email WHERE email = ?1").setParam(1, clieMail).getAsBoolean(conn);
                    }

                    if (choosen && new MySQLQuery("SELECT COUNT(*) = 0 FROM clie_promo_ticket WHERE clie_id = " + clieId + " AND promo_id = " + promoId).getAsBoolean(conn)) {
                        Integer ticketId = new MySQLQuery("SELECT MIN(id) FROM clie_promo_ticket t WHERE t.clie_id IS NULL AND promo_id = " + promoId + "").getAsInteger(conn);
                        if (ticketId != null) {
                            new MySQLQuery("UPDATE clie_promo_ticket "
                                    + "SET clie_id = " + clieId + ", "
                                    + "orig_sale = " + sale.id + " "
                                    + "WHERE id = " + ticketId).executeUpdate(conn);

                            CheckOrderStatus.sendMsgToClient(clieId, "¡Ganador!", "Felicidades", "Recibió un bono por " + price + " pesos. Redimible en su próxima compra con Montagas App<br>Gracias por Preferirnos", conn);
                        } else {
                            new MySQLQuery("INSERT INTO clie_promo_ticket "
                                    + "SET clie_id = " + clieId + ", "
                                    + "orig_sale = " + sale.id + ", "
                                    + "promo_id = " + promoId).executeInsert(conn);
                        }
                    }
                }
            }
        }
    }

    public static void notifyPromoSman(int price, int orderId, int empId, TrkSale sale, Connection conn) throws Exception {
        /*
        * A partir de la versión 56 dejar solo la parte del else y quitar parámetros necesarios de la firma del método
         */

        Object[] appInfo = new MySQLQuery("SELECT id, version FROM system_app WHERE package_name = 'com.glp.subsidiosonline'").getRecord(conn);
        if (Double.valueOf(MySQLQuery.getAsString(appInfo[1])) < 56d) {
            JsonObjectBuilder ob = Json.createObjectBuilder();
            ob.add("price", price);
            ob.add("orderId", orderId);
            ob.add("type", "promo");
            JsonObject json = ob.build();
            GCMUtils.sendToApp(MySQLQuery.getAsInteger(appInfo[0]), json, String.valueOf(empId), conn);
        } else {
            sale.dtoPrice = String.valueOf(price);
        }
    }
}
