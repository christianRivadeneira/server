package web.marketing.cylSales;

import api.com.model.ComCfg;
import api.dto.model.DtoSale;
import api.ord.model.OrdContractIndex;
import api.sys.model.SystemApp;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import service.MySQL.MySQLSelect;
import utilities.Dates;
import utilities.JsonUtils;
import utilities.MySQLQuery;
import web.ShortException;
import web.marketing.cylSales.promo.ComPromoTarget;
import web.marketing.cylSales.promo.ComPromoTargetGrouped;
import web.marketing.pvs.TrkPVSaleWarning;
import web.marketing.smsClaro.ClaroSmsSender;
import web.vicky.beans.CheckOrderStatus;
import web.vicky.clients.GpsPolygon;
import web.vicky.model.VickyCfg;

@MultipartConfig
@WebServlet(name = "CylSales", urlPatterns = {"/CylSales"})
public class CylSales extends HttpServlet {

    private void confirmOffer(TrkSale sale, JsonObject req, Connection conn) throws Exception {
        if (req.containsKey("offerId")) {
            int offerId = req.getInt("offerId");
            CheckOrderStatus.changeOfferStatus(offerId, "confirm", null, null, VickyCfg.select(1, conn), sale, conn);
        }
    }

    /**
     * Unicamete online
     *
     * @param conn
     * @param document
     * @param subsidy
     * @return
     * @throws Exception
     */
    private Promo isInmediatlyPromoApplicant(Connection conn, String document, boolean subsidy, Integer indexId, Integer zoneId, Integer cityId, Integer sectorId, BigDecimal lat, BigDecimal lon, Integer cylinderId) throws Exception {

        Promo p = new Promo();
        int prizeValue = 0;
        int prizePercent = 0;

        Object[][] promos = new MySQLQuery("SELECT "
                + "id, "//0
                + "beg_date, "//1
                + "prize_value, "//2
                + "prize_percent, "//3
                + "days_restr, "//4
                + "target_type "//5
                + "FROM com_promo "
                + "WHERE active "
                + "AND cyl_promo "
                + "AND NOW() BETWEEN beg_date AND end_date "
                + "AND auth_date IS NOT NULL "
                + "AND inmediatly").getRecords(conn);

        for (Object[] promo : promos) {
            boolean applicant = false;
            int promoId = MySQLQuery.getAsInteger(promo[0]);
            String promoTarget = MySQLQuery.getAsString(promo[5]);

            PromoRestrictions rest = new PromoRestrictions();
            switch (promoTarget) {
                case "list":
                    List<ComPromoTargetGrouped> targLst = ComPromoTarget.getFromPromoGroupedCyls(promoId, conn);
                    if (targLst != null) {
                        for (int j = 0; j < targLst.size(); j++) {
                            ComPromoTargetGrouped item = targLst.get(j);
                            if (item.targType == ComPromoTargetGrouped.TYPE_CLIE) {
                                for (int k = 0; k < item.targLst.size(); k++) {
                                    ComPromoTarget target = item.targLst.get(k);
                                    if (target.clieDoc.equals(document)) {
                                        applicant = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    break;
                case "all_subs":
                    applicant = subsidy && applToPromo(promoId, indexId, null, zoneId, cityId, sectorId, cylinderId, lat, lon, rest, conn);
                    break;
                case "all_full":
                    applicant = !subsidy && applToPromo(promoId, indexId, null, zoneId, cityId, sectorId, cylinderId, lat, lon, rest, conn);
                    break;
                case "all":
                    applicant = true;
                    break;
                case "filt":
                    throw new Exception("El objetivo filt solo se usa para promociones SAC");
                default:
                    throw new Exception("Tipo de Objetivo desconocido: " + promoTarget);
            }

            if (applicant) {
                p.ids.add(promoId);
                boolean ableToSale = true;
                if (promo[4] != null) {
                    Date lastSaleDt = new MySQLQuery("SELECT last_sale_dt "
                            + "FROM com_promo_count "
                            + "WHERE promo_id = " + promoId + " "
                            + "AND clie_doc = '" + document + "'").getAsDate(conn);
                    if (promo[4] != null && lastSaleDt != null) {
                        GregorianCalendar c = new GregorianCalendar();
                        c.setTime(lastSaleDt);
                        c.add(GregorianCalendar.DAY_OF_MONTH, MySQLQuery.getAsInteger(promo[4]));
                        ableToSale = c.getTime().compareTo(new Date()) <= 0;
                    }
                }
                if (ableToSale) {
                    prizeValue += promo[2] != null ? MySQLQuery.getAsInteger(promo[2]) : 0;
                    prizePercent += promo[3] != null ? MySQLQuery.getAsInteger(promo[3]) : 0;
                }
            }
        }

        if (prizeValue == 0 && prizePercent == 0) {
            return null;
        } else if ((prizeValue != 0 && prizePercent == 0) || (prizeValue == 0 && prizePercent != 0)) {
            p.prizeValue = prizeValue > 0 ? prizeValue : null;
            p.prizePercent = prizePercent > 0 ? prizePercent : null;
            return p;
        } else {
            //si el cliente tiene derecho a más de una promoción y son de distintos tipos (valor o porcentaje), se prioriza el porcentaje.
            p.prizePercent = prizePercent;
            p.prizeValue = null;
            return p;
        }
    }

    /**
     * Solo para promociones inmediatas, se puede llamar online u offline
     *
     * @param ctrIndexId
     * @param saleDate
     * @param conn
     * @throws Exception
     */
    private void regPromoSale(Promo p, String clieDoc, Date saleDate, int saleId, int salePrice, Connection conn) throws Exception {
        //quitar cuando todos esten en la versión 70 o más        
        if (p != null && !p.ids.isEmpty()) {
            for (int i = 0; i < p.ids.size(); i++) {
                Integer promoId = p.ids.get(i);
                Integer cntId = new MySQLQuery("SELECT id "
                        + "FROM com_promo_count "
                        + "WHERE promo_id = " + promoId + " "
                        + "AND clie_doc = '" + clieDoc + "'").getAsInteger(conn);

                if (cntId == null) {
                    new MySQLQuery("INSERT INTO com_promo_count SET promo_id = " + promoId + ", clie_doc = '" + clieDoc + "', cnt = 0, last_sale_dt = ?1").setParam(1, saleDate).executeInsert(conn);
                } else {
                    //Cnt no se necesita incrementar. por que las promociones inmediatas no se validan por contador
                    new MySQLQuery("UPDATE com_promo_count SET last_sale_dt = ?1 WHERE id = " + cntId).setParam(1, saleDate).executeUpdate(conn);
                }

                if (new MySQLQuery("SELECT inmediatly FROM com_promo WHERE id = " + promoId).getAsBoolean(conn)) {
                    int prize = p.prizeValue != null ? p.prizeValue : MySQLQuery.getAsInteger(salePrice * (p.prizePercent / 100d));
                    new MySQLQuery("INSERT INTO com_inmediatly_promo_winner SET promo_id = " + promoId + ", trk_sale_id = " + saleId + ", prize = " + prize).executeInsert(conn);
                }
            }
        }
    }

    private void isPromoApplicant(TrkSale sale, Connection conn) throws Exception {
        Object[][] promos = new MySQLQuery("SELECT id, beg_date, sale_amount, prize_value, prize_percent, prize_id FROM com_promo WHERE active AND cyl_promo AND end_date >= NOW() AND beg_date <= NOW() AND auth_date IS NOT NULL AND !inmediatly").getRecords(conn);
        PromoRestrictions rest = new PromoRestrictions();
        for (Object[] promo : promos) {
            int promoId = MySQLQuery.getAsInteger(promo[0]);
            if (applToPromo(promoId, sale.indexId, sale.id, null, null, null, sale.cylinderId, sale.lat, sale.lon, rest, conn)) {
                Integer salesAmount = MySQLQuery.getAsInteger(promo[2]);
                DecimalFormat df = new DecimalFormat("#,##0");
                Object[] summRow = new MySQLQuery("SELECT id, cnt "
                        + "FROM com_promo_count "
                        + "WHERE promo_id = " + promoId + " "
                        + "AND zone_id " + (rest.zoneId == null ? " IS NULL" : " = " + rest.zoneId) + "  "
                        + "AND city_id " + (rest.cityId == null ? " IS NULL" : " = " + rest.cityId) + "  "
                        + "AND sector_id " + (rest.sectorId == null ? " IS NULL" : " = " + rest.sectorId) + "  "
                        + "AND cyl_type_id " + (rest.cylTypeId == null ? " IS NULL" : " = " + rest.cylTypeId) + " "
                        + "AND index_id " + (rest.indexId == null ? " IS NULL" : " = " + rest.indexId) + " "
                        + "AND estab_id " + (rest.estabId == null ? " IS NULL" : " = " + rest.estabId)).getRecord(conn);
                Integer summ;
                if (summRow != null) {
                    summ = MySQLQuery.getAsInteger(summRow[1]) + 1;
                    new MySQLQuery("UPDATE com_promo_count SET cnt = cnt + 1 WHERE id = " + MySQLQuery.getAsInteger(summRow[0])).executeUpdate(conn);
                } else {
                    summ = 1;
                    new MySQLQuery("INSERT INTO com_promo_count "
                            + "SET promo_id = " + promoId + ", "
                            + "cnt = 1, "
                            + "zone_id = " + (rest.zoneId == null ? "NULL" : rest.zoneId) + ", "
                            + "city_id = " + (rest.cityId == null ? "NULL" : rest.cityId) + ", "
                            + "sector_id = " + (rest.sectorId == null ? "NULL" : rest.sectorId) + ", "
                            + "cyl_type_id = " + (rest.cylTypeId == null ? "NULL" : rest.cylTypeId) + ", "
                            + "index_id = " + (rest.indexId == null ? "NULL" : rest.indexId) + ", "
                            + "estab_id = " + (rest.estabId == null ? "NULL" : rest.estabId)).executeInsert(conn);
                }
                if ((summ) % salesAmount == 0) {
                    String prizePrice = promo[3] != null ? "bono x " + df.format(MySQLQuery.getAsInteger(promo[3])) : (promo[4] != null ? "bono x " + df.format(sale.price * (MySQLQuery.getAsInteger(promo[4]) / 100d)) : null);
                    if (prizePrice == null) {
                        prizePrice = new MySQLQuery("SELECT prize FROM com_promo_prize WHERE id = " + MySQLQuery.getAsInteger(promo[5])).getAsString(conn);
                    }
                    sale.dtoPrice = prizePrice;
                    String cleanPhone = cleanPhone(sale.phones);
                    String promoCode = new MySQLQuery("SELECT HEX(RAND() * 1000000000000)").getAsString(conn);
                    String code = null;
                    if (cleanPhone != null) {
                        String msg = "Ud es ganador de " + prizePrice + ", acerquese a oficina de atencion al cliente Montagas con su cedula y el codigo " + promoCode + " visite www.montagas.com.co";
                        code = new ClaroSmsSender().sendMsg(msg, "1", cleanPhone);
                    }
                    new MySQLQuery("INSERT INTO com_promo_claim SET trk_sale_id = " + sale.id + ", promo_id = " + promoId + ", claim_code = '" + promoCode + "', send_sms_dt = NOW(), resp_sms_code = '" + (code != null ? code : "Imposible enviar mensaje al tlf: " + sale.phones) + "'").executeInsert(conn);
                }
                break;
            }
        }
    }

    private PromoMultiple isMultiPromoApplicant(int indexId, int saleCylTypeId, int price, int subsidy, int anotherPrize, String phone, boolean saleRegistering, ComCfg cfg, Connection conn) throws Exception {
        OrdContractIndex index = new OrdContractIndex().select(indexId, conn);
        boolean isApplicant = false;
        PromoMultiple mPromo = null;
        Integer superClientIndexId = null;
        Date promoStart = null;
        Integer promoCylTypeId = null;
        String promoCylName = null;
        Object[][] data = new MySQLQuery("SELECT id, start_date, cyl_type_id FROM com_multi_promo WHERE active AND CURDATE() BETWEEN start_date AND end_date").getRecords(conn);
        if (data.length > 0) {
            mPromo = new PromoMultiple();
        }
        for (int i = 0; i < data.length; i++) {
            Object[] promo = data[i];
            mPromo.promoId = MySQLQuery.getAsInteger(promo[0]);
            promoStart = MySQLQuery.getAsDate(promo[1]);
            promoCylTypeId = MySQLQuery.getAsInteger(promo[2]);
            Object[] row = new MySQLQuery("SELECT id, sale_count FROM com_multi_promo_target WHERE index_id = " + indexId + " AND promo_id = " + mPromo.promoId).getRecord(conn);
            isApplicant = row != null && row[0] != null;
            if (!isApplicant) {
                promoCylName = new MySQLQuery("SELECT name FROM cylinder_type WHERE id = " + promoCylTypeId).getAsString(conn);
                row = new MySQLQuery("SELECT "
                        + "t.id, "
                        + "t.sale_count, "
                        + "t.index_id "
                        + "FROM com_multi_promo_target t "
                        + "INNER JOIN ord_contract_index i ON t.index_id = i.id "
                        + "INNER JOIN com_multi_sede s ON s.index_id = i.id "
                        + "WHERE t.promo_id = " + mPromo.promoId + " "
                        + "AND s.document = '" + index.document + "'").getRecord(conn);
                isApplicant = row != null && row[0] != null;
                if (isApplicant) {
                    superClientIndexId = MySQLQuery.getAsInteger(row[2]);
                }
            }
            if (isApplicant) {
                break;
            }
        }

        if (isApplicant) {
            int salesCount = new MySQLQuery("SELECT "
                    + "COUNT(*) "
                    + "FROM trk_sale s "
                    + "INNER JOIN ord_contract_index i ON s.index_id = i.id "
                    + "WHERE i.document = ?1 "
                    + "AND s.date >= ?2 "
                    + "AND s.cube_cyl_type_id = ?3").setParam(1, index.document).setParam(2, promoStart).setParam(3, promoCylTypeId).getAsInteger(conn);

            if (superClientIndexId != null) {
                OrdContractIndex indexSuper = new OrdContractIndex().select(indexId, conn);
                int superSaleCount = new MySQLQuery("SELECT "
                        + "COUNT(*) "
                        + "FROM trk_sale s "
                        + "INNER JOIN ord_contract_index i ON s.index_id = i.id "
                        + "WHERE i.document = ?1 "
                        + "AND s.date >= ?2 "
                        + "AND s.cube_cyl_type_id = ?3").setParam(1, indexSuper.document).setParam(2, promoStart).setParam(3, promoCylTypeId).getAsInteger(conn);
                salesCount += superSaleCount;
            } else {
                superClientIndexId = indexId;
            }

            mPromo.superIndexId = superClientIndexId;
            Object[][] conds = new MySQLQuery("SELECT "
                    + "c.value "
                    + "FROM com_multi_promo_cond c "
                    + "WHERE c.promo_id = " + mPromo.promoId).getRecords(conn);

            mPromo.prizeValue = 0;
            mPromo.indexId = indexId;
            for (int i = 0; i < conds.length; i++) {
                Object[] cond = conds[i];
                if ((salesCount + 1) % MySQLQuery.getAsInteger(cond[0]) == 0 && saleRegistering) {
                    if (phone != null) {
                        String cleanPhone = cleanPhone(phone);
                        String msg = "Estimado cliente, su próxima compra de un cil. " + promoCylName + " tendrá un beneficio especial. Vigencia del beneficio " + cfg.multiPromoValidity + " días";
                        try {
                            new ClaroSmsSender().sendMsg(msg, "1", cleanPhone);
                        } catch (Exception e) {
                            Logger.getLogger(CylSales.class.getName()).log(Level.SEVERE, null, e);
                        }
                    }
                }

                if ((salesCount + 1) % MySQLQuery.getAsInteger(cond[0]) == 0 && !saleRegistering) {
                    if (saleCylTypeId == promoCylTypeId) {
                        mPromo.prizeValue = price;
                    } else {
                        boolean validity = new MySQLQuery("SELECT "
                                + "s.date > DATE_SUB(NOW(),INTERVAL " + cfg.multiPromoValidity + " DAY) "
                                + "FROM trk_sale s "
                                + "WHERE s.index_id = " + indexId + " AND s.cube_cyl_type_id = " + promoCylTypeId + " "
                                + "ORDER BY id DESC LIMIT 1").getAsBoolean(conn);

                        if (validity) {
                            int clieDanePobId = new MySQLQuery("SELECT "
                                    + "s.dane_pob_id  "
                                    + "FROM trk_sale s "
                                    + "WHERE s.index_id = " + indexId + " "
                                    + "ORDER BY id DESC LIMIT 1;").getAsInteger(conn);

                            mPromo.prizeValue = new MySQLQuery("SELECT "
                                    + "s.price "
                                    + "FROM trk_sale s "
                                    + "WHERE s.dane_pob_id = " + clieDanePobId + " "
                                    + "AND s.cube_cyl_type_id = " + promoCylTypeId + " "
                                    + "ORDER BY id DESC LIMIT 1").getAsInteger(conn);

                            mPromo.prizeValue = mPromo.prizeValue - subsidy - anotherPrize;
                        } else {
                            mPromo = null;
                            if (phone != null) {
                                String cleanPhone = cleanPhone(phone);
                                String msg = "Estimado cliente le informamos que el beneficio que ud tenía para esta compra está vencido. Excedió los " + cfg.multiPromoValidity + " días";
                                try {
                                    new ClaroSmsSender().sendMsg(msg, "1", cleanPhone);
                                } catch (Exception e) {
                                    Logger.getLogger(CylSales.class.getName()).log(Level.SEVERE, null, e);
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }

        return isApplicant ? mPromo : null;
    }
    
    public static void sendMessage(){
        System.out.println("##########################MÉTODO PARA ENVIAR MSG");
        //new ClaroSmsSender().sendMsg("Esto es una prueba ", "1", "3153040495");
    }
    public static Object getQualitySale(int id,Connection conn) throws Exception{
        Object[] quality=new MySQLQuery("SELECT q.c_3, q.c_4, q.c_5, q.agua "
                                    + "FROM trk_sale s "
                                    + "INNER JOIN trk_sale_quality q ON q.id_trk_sale =s.id "
                                    + "WHERE s.id="+id).getRecord(conn);
        return quality;
    }

    private void regMultiPromoSale(PromoMultiple p, TrkSale sale, Connection conn) throws Exception {
        new MySQLQuery("INSERT INTO com_multi_promo_winner "
                + "SET index_id = " + sale.indexId + ", "
                + "super_index_id = ?1, "
                + "promo_id = " + p.promoId + ", "
                + "sale_id = " + sale.id + ", "
                + "prize = " + p.prizeValue).setParam(1, p.superIndexId).executeInsert(conn);
    }

    private class PromoRestrictions {

        Integer zoneId = null;
        Integer cityId = null;
        Integer sectorId = null;
        Integer indexId = null;
        Integer cylTypeId = null;
        Integer estabId = null;
    }

    private String cleanPhone(String phone) {
        String clnPhone = null;
        if (phone.contains("-")) {
            String[] str = phone.split("-");
            for (String str1 : str) {
                if (str1.startsWith("3")) {
                    clnPhone = str1;
                    break;
                }
            }
        } else if (phone.startsWith("3")) {
            clnPhone = phone;
        }
        return clnPhone;
    }

    private boolean applToPromo(int promoId, int indexId, Integer saleId, Integer zoneId, Integer cityId, Integer sectorId, Integer cylinderId, BigDecimal lat, BigDecimal lon, PromoRestrictions rest, Connection conn) throws Exception {
        List<ComPromoTargetGrouped> targLst = ComPromoTarget.getFromPromoGroupedCyls(promoId, conn);
        Applicant applicant = new Applicant();
        if (targLst != null) {
            for (int j = 0; j < targLst.size(); j++) {
                ComPromoTargetGrouped item = targLst.get(j);
                if (item.targType == ComPromoTargetGrouped.TYPE_ZONE) {

                    Integer saleZoneId = zoneId != null ? zoneId : GpsPolygon.hit("SELECT id FROM zone", GpsPolygon.TYPE_ZONE, MySQLQuery.getAsDouble(lat), MySQLQuery.getAsDouble(lon), conn);
                    if (saleZoneId != null) {
                        for (int k = 0; k < item.targLst.size(); k++) {
                            ComPromoTarget target = item.targLst.get(k);
                            if (target.zoneId != null) {
                                applicant.applZone = Objects.equals(target.zoneId, saleZoneId);
                                if (applicant.applZone) {
                                    rest.zoneId = target.zoneId;
                                    break;
                                }
                            } else {
                                applicant.applZone = true;
                            }
                        }
                    } else {
                        applicant.applZone = false;
                    }
                }

                if (item.targType == ComPromoTargetGrouped.TYPE_CITY) {
                    Integer saleCityId = cityId != null ? cityId : GpsPolygon.hit("SELECT id FROM city", GpsPolygon.TYPE_CITY, MySQLQuery.getAsDouble(lat), MySQLQuery.getAsDouble(lon), conn);
                    if (saleCityId != null) {
                        for (int k = 0; k < item.targLst.size(); k++) {
                            ComPromoTarget target = item.targLst.get(k);
                            if (target.cityId != null) {
                                applicant.applCity = Objects.equals(target.cityId, saleCityId);
                                if (applicant.applCity) {
                                    rest.cityId = target.cityId;
                                    break;
                                }
                            } else {
                                applicant.applCity = true;
                            }
                        }
                    } else {
                        applicant.applCity = false;
                    }
                }

                if (item.targType == ComPromoTargetGrouped.TYPE_SECTOR) {
                    Integer saleSectorId = sectorId != null ? sectorId : GpsPolygon.hitSector(MySQLQuery.getAsDouble(lat), MySQLQuery.getAsDouble(lon)).id;
                    if (saleSectorId != null) {
                        for (int k = 0; k < item.targLst.size(); k++) {
                            ComPromoTarget target = item.targLst.get(k);
                            if (target.sectorId != null) {
                                applicant.applSector = Objects.equals(target.sectorId, saleSectorId);
                                if (applicant.applSector) {
                                    rest.sectorId = target.sectorId;
                                    break;
                                }
                            } else {
                                applicant.applSector = true;
                            }
                        }
                    } else {
                        applicant.applSector = false;
                    }
                }

                if (item.targType == ComPromoTargetGrouped.TYPE_CYL_TYPE) {
                    Object[][] cylTypeIds;

                    if (cylinderId != null) {
                        cylTypeIds = new MySQLQuery("SELECT c.cyl_type_id FROM trk_cyl c WHERE c.id = " + cylinderId).getRecords(conn);
                    } else if (saleId != null) {
                        cylTypeIds = new MySQLQuery("SELECT DISTINCT cyl_type_id FROM trk_multi_prices WHERE sale_id = " + saleId).getRecords(conn);
                    } else {
                        cylTypeIds = null;
                    }

                    if (cylTypeIds != null) {
                        for (int k = 0; k < item.targLst.size() && rest.cylTypeId == null; k++) {
                            ComPromoTarget target = item.targLst.get(k);
                            for (Object[] cylTypeRow : cylTypeIds) {
                                applicant.applCylType = target.cylTypeId.equals(MySQLQuery.getAsInteger(cylTypeRow[0]));
                                if (applicant.applCylType) {
                                    rest.cylTypeId = MySQLQuery.getAsInteger(cylTypeRow[0]);
                                    break;
                                }
                            }
                        }
                    }
                }

                if (item.targType == ComPromoTargetGrouped.TYPE_EST_TYPE) {
                    Object[] ctrInfo = new MySQLQuery("SELECT i.contract_id, i.`type` FROM ord_contract_index i WHERE i.id = " + indexId).getRecord(conn);
                    Integer estId = null;
                    if (!MySQLQuery.getAsString(ctrInfo[1]).equals("app")) {
                        estId = new MySQLQuery("SELECT establish_id FROM " + (MySQLQuery.getAsString(ctrInfo[1]).equals("brand") ? "contract" : "ord_contract") + " WHERE id = " + MySQLQuery.getAsInteger(ctrInfo[0])).getAsInteger(conn);
                    }
                    if (estId != null) {
                        for (int k = 0; k < item.targLst.size(); k++) {
                            ComPromoTarget target = item.targLst.get(k);
                            applicant.applEst = Objects.equals(estId, target.establishId);
                            if (applicant.applEst) {
                                rest.estabId = estId;
                                break;
                            }
                        }
                    } else {
                        applicant.applEst = false;
                    }
                }

                if (item.targType == ComPromoTargetGrouped.TYPE_CLIE) {
                    for (int k = 0; k < item.targLst.size(); k++) {
                        ComPromoTarget target = item.targLst.get(k);
                        String doc = new MySQLQuery("SELECT document FROM ord_contract_index WHERE id = ?1").setParam(1, indexId).getAsString(conn);
                        applicant.applIndex = target.clieDoc.equals(doc);
                        if (applicant.applIndex) {
                            rest.indexId = indexId;
                            break;
                        }
                    }
                }

                if (item.targType == ComPromoTargetGrouped.TYPE_PREF) {
                    for (int k = 0; k < item.targLst.size(); k++) {
                        ComPromoTarget target = item.targLst.get(k);
                        applicant.applPref = target.prefIndexId.equals(indexId);
                        if (applicant.applPref) {
                            rest.indexId = indexId;
                            break;
                        }
                    }
                }
            }
        }

//        System.out.println("app.applZone " + app.applZone);
//        System.out.println("app.applCity " + app.applCity);
//        System.out.println("app.applSector " + app.applSector);
//        System.out.println("app.applCylType " + app.applCylType);
//        System.out.println("app.applEst " + app.applEst);
//        System.out.println("app.applIndex " + app.applIndex);
//        System.out.println("app.applPref " + app.applPref);
        return (applicant.applZone == null || applicant.applZone)
                && (applicant.applCity == null || applicant.applCity)
                && (applicant.applSector == null || applicant.applSector)
                && (applicant.applCylType == null || applicant.applCylType)
                && (applicant.applEst == null || applicant.applEst)
                && (applicant.applIndex == null || applicant.applIndex)
                && (applicant.applPref == null || applicant.applPref);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;

        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            JsonObjectBuilder ob = Json.createObjectBuilder();
            conn = MySQLCommon.getConnection("sigmads", null);
            conn.setAutoCommit(false);
            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");
            JsonObject req = MySQLQuery.scapeJsonObj(request);

            String header = req.getString("header");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat sdfShort = new SimpleDateFormat("yyyy-MM-dd");
            try {
                boolean offLine = req.containsKey("offLine") ? req.getBoolean("offLine") : false;
                ComCfg cfg = new ComCfg().select(1, conn);
                SessionLogin sess;

                String sessionId = req.getString("sessionId");
                if (!offLine) {
                    sess = SessionLogin.validate(sessionId, conn, null);
                } else {
                    sess = SessionLogin.getFromRow(new MySQLQuery(SessionLogin.getSelectQueryBySessionId(sessionId)).getRecord(conn));

                    Object[] empRow = new MySQLQuery("SELECT document, first_name, last_name FROM employee WHERE id = " + sess.employeeId).getRecord(conn);
                    sess.document = MySQLQuery.getAsString(empRow[0]);
                    sess.firstName = MySQLQuery.getAsString(empRow[1]);
                    sess.lastName = MySQLQuery.getAsString(empRow[2]);
                }
                //validateSchedule(sess.employeeId, conn);

                if (req.containsKey("appVersion")) {
                    String clientVersion = req.getString("appVersion");
                    SystemApp app = new SystemApp().select(sess.appId, conn);
                    String profileVersion = new MySQLQuery("SELECT "
                            + "MAX(p.test_version) "
                            + "FROM system_app s "
                            + "LEFT JOIN sys_app_profile p ON p.app_id = s.id "
                            + "LEFT JOIN sys_app_profile_emp pe ON pe.app_profile_id = p.id AND pe.emp_id = " + sess.employeeId + " "
                            + "WHERE s.id = " + app.id).getAsString(conn);

                    if (profileVersion == null) {
                        if ((app.mandatory && !app.version.equals(clientVersion)) || (app.minVerAllow != null && Double.valueOf(clientVersion) < Double.valueOf(app.minVerAllow))) {
                            throw new ShortException("Inicie sesión nuevamente para instalar actualización.");
                        }
                    } else {
                        if (!clientVersion.equals(profileVersion)) {
                            throw new ShortException("Inicie sesión nuevamente para instalar actualización.");
                        }
                    }
                }

                switch (header) {
                    case "QS": {
                        //Consulta el subsidio
                        setTransaction(req, sess, ob, false, null, cfg.phantomNif, cfg, conn);
                        break;
                    }
                    case "QR": {
                        //2020-01-12 este parche se hizo para un usuario con problemas de sincronización, debe estar comentado
                        /*if (!req.containsKey("auth")) {
                            //PARCHE PROVISIONAL
                            if (req.containsKey("minasError")) {
                                if (req.containsKey("transId")) {
                                    TrkTransaction trans = new TrkTransaction().select(req.getInt("transId"), conn);
                                    Contract ctr = Contract.searchContract(conn, trans.indexId);
                                    ob.add("firstName", ctr.firstName != null ? ctr.firstName : "");
                                    ob.add("lastName", ctr.lastName != null ? ctr.lastName : "");
                                    ob.add("address", ctr.address != null ? ctr.address : "");
                                    ob.add("phones", ctr.phones != null ? ctr.phones : "");
                                }
                            }
                            break;
                        }*/

                        TrkTransaction trans;
                        List<String> warns = new ArrayList<>();
                        if (req.containsKey("transId")) {
                            trans = new TrkTransaction().select(req.getInt("transId"), conn);
                        } else {
                            trans = setTransaction(req, sess, ob, offLine, warns, cfg.phantomNif, cfg, conn);
                        }
                        trans.discount = req.containsKey("discountVal") ? req.getInt("discountVal") : null;
                        trans.bonusCode = req.containsKey("bonusCode") ? req.getString("bonusCode") : null;
                        //Se debe eliminar minasError y minasNetError
                        String minasError = get(req, "minasError") + get(req, "minasTokenError") + get(req, "minasNetError") + get(req, "minasRegError");
                        if (!minasError.isEmpty()) {
                            if (req.containsKey("bill")) {
                                trans.bill = req.getString("bill");
                            }
                            trans.error2 = minasError;
                            trans.update(trans, conn);

                            manageException(trans.error2, offLine, warns);
                        } else if (trans.auth != null) {
                            Contract ctr = Contract.searchContract(conn, trans.indexId);

                            String phone = (req.containsKey("phones") ? req.getString("phones") : null);
                            if (phone != null && !phone.isEmpty()) {
                                ctr.phones = Contract.updateContractPhone(conn, ctr, phone);
                            }

                            ob.add("firstName", ctr.firstName != null ? ctr.firstName : "");
                            ob.add("lastName", ctr.lastName != null ? ctr.lastName : "");
                            ob.add("address", ctr.address != null ? ctr.address : "");
                            ob.add("phones", ctr.phones != null ? ctr.phones : "");
                            break;
                        } else {
                            try {
                                boolean isNewContract = false;
                                trans.valSub = req.getInt("valSub");
                                new MySQLQuery("UPDATE trk_cyl SET used_date = CURDATE() WHERE id = " + trans.cylId).executeUpdate(conn);
                                if (trans.indexId == null) {
                                    String firstName = req.getString("firstName");
                                    String lastName = req.getString("lastName");
                                    String phone = req.getString("phones");
                                    int cityId = req.getInt("cityId");
                                    String address = req.getString("address");
                                    String neigh = req.getString("neigh");
                                    trans.indexId = insertContract(null, trans.document, firstName, lastName, phone, cityId, address, neigh, sess.employeeId, conn);
                                    isNewContract = true;
                                }
                                ZoneInfo zoneInfo = getZoneInfo(MySQLQuery.getAsBigDecimal(trans.lat, true), MySQLQuery.getAsBigDecimal(trans.lon, true), true, conn);
                                trans.auth = cleanAuth(req.getString("auth"));
                                if (new MySQLQuery("SELECT COUNT(*) = 0 FROM trk_sale WHERE cylinder_id = ?1 AND auth = ?2 AND sale_type = 'sub' FOR UPDATE").setParam(1, trans.cylId).setParam(2, trans.auth).getAsBoolean(conn)) {
                                    TrkSale s = registerSale(trans, sess.employeeId, (req.containsKey("stratum") ? req.getInt("stratum") : null), zoneInfo.zone, zoneInfo.danePobId, (req.containsKey("isSowing") ? req.getBoolean("isSowing") : false), (req.containsKey("training") ? req.getBoolean("training") : false), conn);
                                    if (cfg.phantomNif) {
                                        Object[] noRotRow = new MySQLQuery("SELECT id, no_rot_cyl FROM trk_no_rot_sold WHERE transaction_id = " + trans.id).getRecord(conn);
                                        if (noRotRow != null && noRotRow.length > 0) {
                                            new MySQLQuery("UPDATE trk_no_rot_sold SET sale_id = " + s.id + " WHERE id = " + MySQLQuery.getAsInteger(noRotRow[0])).executeUpdate(conn);
                                            new MySQLQuery("UPDATE trk_no_rot_cyls SET sale_date = '" + sdf.format(s.date) + "' WHERE cyl_id = " + MySQLQuery.getAsInteger(noRotRow[1])).executeUpdate(conn);
                                        }
                                    }

                                    if (!warns.isEmpty()) {
                                        s.lstWarns = warns;
                                        s.saveWarns(conn);
                                    }

                                    //metodo para guardar el viaje de la venta
                                    updateTripSale(s.empId, s.id, s.date, conn);

                                    lockCyl(cfg.lockCylSale, trans.cylId, conn);
                                    confirmOffer(s, req, conn);

                                    Contract ctr = Contract.searchContract(conn, trans.indexId);

                                    if (!isNewContract) {
                                        String phone = (req.containsKey("phones") ? req.getString("phones") : null);
                                        if (phone != null && !phone.isEmpty()) {
                                            ctr.phones = Contract.updateContractPhone(conn, ctr, phone);
                                        }
                                    }

                                    if (!offLine && s.discount == null) {
                                        s.phones = ctr.phones;
                                        isPromoApplicant(s, conn);
                                    }

                                    if (s.discount != null) {
                                        Promo p = isInmediatlyPromoApplicant(conn, trans.document, true, trans.indexId, null, null, null, s.lat, s.lon, s.cylinderId);
                                        if (p != null) {
                                            regPromoSale(p, trans.document, s.date, s.id, s.price, conn);
                                        }
                                        int anotherPrize = p != null ? p.prizeValue != null ? p.prizeValue : MySQLQuery.getAsInteger(trans.price * (p.prizePercent / 100d)) : 0;
                                        PromoMultiple pm = isMultiPromoApplicant(s.indexId, s.cubeCylTypeId, s.price, trans.valSub, anotherPrize, s.phones, true, cfg, conn);
                                        if (pm != null) {
                                            pm.prizeValue = s.discount;
                                            regMultiPromoSale(pm, s, conn);
                                        }
                                    }

                                    ob.add("firstName", ctr.firstName != null ? ctr.firstName : "");
                                    ob.add("lastName", ctr.lastName != null ? ctr.lastName : "");
                                    ob.add("address", ctr.address != null ? ctr.address : "");
                                    ob.add("phones", ctr.phones != null ? ctr.phones : "");
                                    //calidad glp
                                    Object[] qualitySale = (Object[]) getQualitySale(s.id, conn);
                                    if(qualitySale!=null){
                                        ob.add("qualitySale","1");
                                        ob.add("c3", String.format("%,.2f", qualitySale[0]));
                                        ob.add("c4", String.format("%,.2f", qualitySale[1]));
                                        ob.add("c5", String.format("%,.2f", qualitySale[2]));
                                        ob.add("agua", String.format("%,.0f", qualitySale[3]));
                                    }else{
                                        ob.add("qualitySale","0");
                                    }
                                    if (s.dtoPrice != null) {
                                        ob.add("dtoPrice", s.dtoPrice);
                                    }

                                    if (req.containsKey("negAnsIds")) {
                                        String ids[] = req.getString("negAnsIds").split(",");
                                        int neg_quest_id = new MySQLQuery("INSERT INTO com_app_neg_quest SET trk_sale_id = " + s.id).executeInsert(conn);
                                        for (String id : ids) {
                                            new MySQLQuery("INSERT INTO com_app_answer SET neg_quest_id = " + neg_quest_id + ", question_id = " + id).executeInsert(conn);
                                        }
                                    }

                                } else if (!offLine) {
                                    throw new ShortException("Ya hay una venta para el mismo cliente y factura");
                                }
                            } catch (Exception ex) {
                                trans.error2 = ex.getMessage();
                                throw ex;
                            } finally {
                                trans.update(trans, conn);
                            }
                        }
                        break;
                    }
                    case "QNI": {
                        //Puesto aquí para la versión 56 de la app de ventas. Cuando todos estén en la 56 se puede eliminar el servlet llamado FindContract (web.marketing)
                        String find = req.getString("find");
                        if (find != null) {
                            find = find.trim();
                            if (find.length() < 5) {
                                //A partir de la versión 62 se puede quitar ésta validación porque se hace en cliente
                                throw new ShortException("Escriba al menos 5 caractéres para buscar.");
                            }
                            String where;
                            if (find.substring(0, 1).matches("[0-9]")) {
                                find = find.replaceAll("[^0-9]", "");
                                where = "(oci.document LIKE '%" + find + "%' )";//BUSCA POR TELEFONO O POR DOCUMENTO
                            } else {
                                where = "(oci.address LIKE '%" + clearAddress(find) + "%')";//BUSCA POR DIRECCIÓN
                            }

                            Object[][] data = new MySQLQuery("SELECT "
                                    + "oci.contract_id AS id, "//0
                                    + "oci.document AS doc, "//1
                                    + "oci.first_name AS fn, "//2
                                    + "oci.last_name AS ln, "//3
                                    + "CONCAT(oci.address, IF(n.name IS NOT NULL, CONCAT(' ',n.name), ''))  AS ad, "//4
                                    + "oci.phones AS tel, "//5
                                    + "IF(oci.`type` = 'brand', 1, 0), "//6
                                    + "oci.city_id AS ct, "//7
                                    + "(SELECT capa.name FROM smb_ctr_cyl AS cyls INNER JOIN cylinder_type AS capa ON capa.id = cyls.type_id WHERE cyls.contract_id = oci.contract_id LIMIT 1) AS cyl, "
                                    + "oci.id AS ind, "//9
                                    + "n.sector_id, "//10
                                    + "city.zone_id "//11
                                    + "FROM ord_contract_index AS oci "
                                    + "LEFT JOIN neigh AS n ON n.id = oci.neigh_id "
                                    + "INNER JOIN city ON oci.city_id = city.id "
                                    + "WHERE oci.active = 1 AND " + where + " ORDER BY oci.`type` " + (find.contains("%") ? " LIMIT 100 " : " ")).getRecords(conn);

                            JsonArrayBuilder ab = Json.createArrayBuilder();
                            for (Object[] row : data) {
                                int indexId = MySQLQuery.getAsInteger(row[9]);
                                JsonObjectBuilder job = Json.createObjectBuilder()
                                        .add("id", MySQLQuery.getAsInteger(row[0]))
                                        .add("docs", (row[1] != null ? MySQLQuery.getAsString(row[1]) : ""))
                                        .add("first_name", (row[2] != null ? (MySQLQuery.getAsString(row[2]).equals("null") ? "Sin " : MySQLQuery.getAsString(row[2])) : ""))
                                        .add("last_name", (row[3] != null ? (MySQLQuery.getAsString(row[3]).equals("null") ? "Información" : MySQLQuery.getAsString(row[3])) : ""))
                                        .add("address", (row[4] != null ? (MySQLQuery.getAsString(row[4]).equals("null") ? "Sin Información" : MySQLQuery.getAsString(row[4])) : ""))
                                        .add("phones", (row[5] != null ? MySQLQuery.getAsString(row[5]) : ""))
                                        .add("brand", (row[6] != null ? MySQLQuery.getAsBoolean(row[6]) : false))
                                        .add("city_id", (row[7] != null ? MySQLQuery.getAsInteger(row[7]) : 0))
                                        .add("cyl", row[8] != null ? MySQLQuery.getAsString(row[8]) : "")
                                        .add("index_id", indexId);

                                Promo p = isInmediatlyPromoApplicant(conn, MySQLQuery.getAsString(row[1]), false, MySQLQuery.getAsInteger(row[9]), MySQLQuery.getAsInteger(row[11]), MySQLQuery.getAsInteger(row[7]), MySQLQuery.getAsInteger(row[10]), null, null, null);
                                if (p != null) {
                                    if (p.prizePercent != null) {
                                        job.add("prizePercent", p.prizePercent);
                                    } else if (p.prizeValue != null) {
                                        job.add("prizeValue", p.prizeValue);
                                    }
                                }
                                int anotherPrize = p != null ? p.prizeValue != null ? p.prizeValue : 0 : 0;
                                PromoMultiple pm = isMultiPromoApplicant(indexId, 69, 0, 0, anotherPrize, MySQLQuery.getAsString(row[5]), false, cfg, conn);
                                if (pm != null && pm.prizeValue != 0) {
                                    job.add("prizeValue", pm.prizeValue);
                                }

                                ab.add(job);
                            }
                            ob.add("ctrs", ab);
                        }
                        break;
                    }
                    case "QNN": {
                        //venta full
                        TrkSale s = new TrkSale();
                        String document = req.getString("clieDoc");
                        setLastSaleDays(s, document, conn);
                        Contract ctr = Contract.searchContract(conn, document);
                        if (ctr == null) {
                            String phone = (req.containsKey("phones") ? req.getString("phones") : null);
                            int indId = insertContract(null, document, req.getString("clieFName"), req.getString("clieLName"), phone, 3, req.getString("clieAddress"), "", 1, conn);
                            ctr = Contract.searchContract(conn, indId);
                        } else if (ctr.firstName.equals("null") || ctr.firstName.equals("Sin ")) {
                            String fName = req.getString("clieFName");
                            String lName = req.getString("clieLName");
                            String address = req.getString("clieAddress");
                            String phone = (req.containsKey("phones") ? req.getString("phones") : null);

                            new MySQLQuery("UPDATE ord_contract_index SET "
                                    + "first_name = '" + fName + "', last_name = '" + lName + "', address = '" + address + "' "
                                    + (phone != null ? ", phones = '" + phone + "' " : " ")
                                    + "WHERE id = " + ctr.indexId).executeUpdate(conn);
                            new MySQLQuery("UPDATE ord_contract SET "
                                    + "first_name = '" + fName + "', last_name = '" + lName + "', address = '" + address + "' "
                                    + (phone != null ? ", phones = '" + phone + "' " : " ")
                                    + "WHERE id = (SELECT contract_id FROM ord_contract_index WHERE id = " + ctr.indexId + ")").executeUpdate(conn);
                        } else {
                            String phone = (req.containsKey("phones") ? req.getString("phones") : null);
                            if (phone != null && !phone.isEmpty()) {
                                ctr.phones = Contract.updateContractPhone(conn, ctr, phone);
                            }
                        }
                        s.bonusCode = req.containsKey("bonusCode") ? req.getString("bonusCode") : null;
                        s.date = new Date();
                        if (offLine) {
                            s.lstWarns = new ArrayList<>();
                            if (req.containsKey("clientDate")) {
                                s.date = sdf.parse(req.getString("clientDate"));
                            }
                        }
                        s.indexId = ctr.indexId;
                        s.bill = req.getString("bill");
                        validarBillRepetido(s.bill, sess.employeeId, conn);
                        s.discount = (req.containsKey("discount") ? req.getInt("discount") : null);

                        Cylinder cyl = Cylinder.getCylinder(req.getString("nif"), true, false, offLine, s, sess.employeeId, conn);

                        s.cubeCylTypeId = cyl.cylTypeId;
                        s.cubeNifY = cyl.year;
                        s.cubeNifF = cyl.factory;
                        s.cubeNifS = cyl.serial;
                        System.out.println("EL VALOR DE IDCYL ES "+cyl.trkCylId);
                        if (new MySQLQuery("SELECT COUNT(*) = 0 FROM trk_sale WHERE index_id = " + s.indexId + " AND bill = '" + s.bill + "' AND DATE(date) = '" + sdfShort.format(s.date) + "' AND sale_type = 'full' FOR UPDATE").getAsBoolean(conn)) {
                            s.empId = sess.employeeId;
                            s.stratum = req.getInt("stratum");
                            s.cylinderId = cyl.trkCylId;
                            
                            s.credit = req.containsKey("credit") ? req.getBoolean("credit") : false;
                            s.price = req.getInt("price");
                            validatePrice(s.price, sess.document, cyl.cylTypeId, cyl.mgName, offLine, s, conn);

                            if (req.containsKey("nifReceived")) {
                                int recId = Cylinder.getCylinder(req.getString("nifReceived"), false, false, offLine, s, s.empId, conn).trkCylId;
                                s.cylReceivedId = recId != 0 ? recId : null;

                                //s.cylReceivedId = Cylinder.getCylinder(conn, , false, false, false, offLine, s, s.empId, lockCylSale).trkCylId;
                            }
                            s.lat = MySQLQuery.getAsBigDecimal(req.getString("lat"), true);
                            s.lon = MySQLQuery.getAsBigDecimal(req.getString("lon"), true);
                            s.saleType = req.getString("saleType");
                            s.courtesy = req.containsKey("courtesy") ? req.getBoolean("courtesy") : false;

                            s.isSowing = req.getBoolean("isSowing");
                            ZoneInfo zoneInfo = getZoneInfo(s.lat, s.lon, false, conn);
                            s.danePobId = zoneInfo.danePobId;
                            s.zone = zoneInfo.zone;

                            s.training = req.containsKey("training") ? req.getBoolean("training") : false;
                            s.phones = ctr.phones;
                            setVhAndManager(s, conn);
                            setDtoSalesman(s, conn);
                            s.id = s.insert(s, conn);

                            //metodo para guardar el viaje de la venta
                            updateTripSale(s.empId, s.id, s.date, conn);
                            //calidad glp
                            insertTrkSaleQuality(cyl.trkCylId, s.id,conn);
                            
                            //Aquí el metodo para enviar mensaje de texto
                            sendMessage();
                            
                            lockCyl(cfg.lockCylSale, s.cylinderId, conn);
                            confirmOffer(s, req, conn);
                            if (!offLine && s.discount == null) {
                                isPromoApplicant(s, conn);
                            }

                            if (s.discount != null) {
                                Promo p = isInmediatlyPromoApplicant(conn, document, false, s.indexId, null, null, null, s.lat, s.lon, s.cylinderId);
                                if (p != null) {
                                    regPromoSale(p, document, s.date, s.id, s.price, conn);
                                }
                                int anotherPrize = p != null ? p.prizeValue != null ? p.prizeValue : MySQLQuery.getAsInteger(s.price * (p.prizePercent / 100d)) : 0;
                                PromoMultiple pm = isMultiPromoApplicant(s.indexId, s.cubeCylTypeId, s.price, 0, anotherPrize, s.phones, true, cfg, conn);
                                if (pm != null) {
                                    pm.prizeValue = s.discount;
                                    regMultiPromoSale(pm, s, conn);
                                }
                            }
                            s.saveWarns(conn);

                            if (req.containsKey("negAnsIds")) {
                                String ids[] = req.getString("negAnsIds").split(",");
                                int neg_quest_id = new MySQLQuery("INSERT INTO com_app_neg_quest SET trk_sale_id = " + s.id).executeInsert(conn);
                                for (String id : ids) {
                                    new MySQLQuery("INSERT INTO com_app_answer SET neg_quest_id = " + neg_quest_id + ", question_id = " + id).executeInsert(conn);
                                }
                            }
                            
                            ob.add("address", (ctr.address != null ? ctr.address : "Sin información"));
                            ob.add("phones", (ctr.phones != null ? ctr.phones : "Sin información"));
                            ob.add("cylCap", cyl.mgName);
                            //calidad glp
                            Object[] qualitySale = (Object[]) getQualitySale(s.id, conn);
                            if(qualitySale!=null){
                                ob.add("qualitySale","1");
                                ob.add("c3", String.format("%,.2f", qualitySale[0]));
                                ob.add("c4", String.format("%,.2f", qualitySale[1]));
                                ob.add("c5", String.format("%,.2f", qualitySale[2]));
                                ob.add("agua", String.format("%,.0f", qualitySale[3]));
                            }else{
                                ob.add("qualitySale","0");
                            }
                            if (s.dtoPrice != null) {
                                ob.add("dtoPrice", s.dtoPrice);
                            }
                        } else if (!offLine) {
                            throw new ShortException("Ya hay una venta para el mismo cliente y factura en ésta fecha");
                        }

                        break;
                    }
                    case "QM": {
                        //Venta Múltiple
                        String document = req.getString("clientDoc");
                        Contract ctr = Contract.searchContract(conn, document);
                        if (ctr == null) {
                            String phone = (req.containsKey("phones") ? req.getString("phones") : "");
                            int indexId = insertContract(null, document, "Sin ", "Información", phone, 3, "Sin ", "Información", 1, conn);
                            ctr = Contract.searchContract(conn, indexId);
                        } else {
                            String phone = (req.containsKey("phones") ? req.getString("phones") : null);
                            if (phone != null && !phone.isEmpty()) {
                                ctr.phones = Contract.updateContractPhone(conn, ctr, phone);
                            }
                        }
                        TrkSale s = new TrkSale();
                        setLastSaleDays(s, document, conn);
                        s.date = new Date();
                        if (offLine) {
                            s.date = sdf.parse(req.getString("clientDate"));
                        }
                        s.indexId = ctr.indexId;
                        s.bill = req.getString("bill");
                        validarBillRepetido(s.bill, sess.employeeId, conn);
                        s.training = req.containsKey("training") ? req.getBoolean("training") : false;

                        if (new MySQLQuery("SELECT COUNT(*) = 0 FROM trk_sale WHERE index_id = " + s.indexId + " AND bill = '" + s.bill + "' AND DATE(date) = '" + sdfShort.format(s.date) + "' AND sale_type = 'mul' FOR UPDATE").getAsBoolean(conn)) {
                            s.empId = sess.employeeId;
                            s.lat = MySQLQuery.getAsBigDecimal(req.getString("lat"), true);
                            s.lon = MySQLQuery.getAsBigDecimal(req.getString("lon"), true);
                            s.saleType = req.getString("saleType");
                            s.stratum = (req.containsKey("stratum") ? req.getInt("stratum") : null);
                            s.isSowing = (req.containsKey("isSowing") ? req.getBoolean("isSowing") : false);
                            s.credit = req.containsKey("credit") ? req.getBoolean("credit") : false;
                            s.courtesy = req.containsKey("courtesy") ? req.getBoolean("courtesy") : false;
                            s.phones = ctr.phones;
                            s.bonusCode = req.containsKey("bonusCode") ? req.getString("bonusCode") : null;
                            s.discount = req.containsKey("discount") ? req.getInt("discount") : null;
                            setVhAndManager(s, conn);
                            setDtoSalesman(s, conn);

                            JsonArray pricesJA = req.getJsonArray("pricesJAB");
                            JsonArray nifDelJA = req.getJsonArray("nifDelJAB");
                            JsonArray nifRecJA = JsonUtils.getJsonArray(req, "nifRecJAB");

                            List<MultiPrices> lstPrices = new ArrayList<>();
                            for (int i = 0; i < pricesJA.size(); i++) {
                                MultiPrices price = new MultiPrices();
                                JsonObject aux = pricesJA.getJsonObject(i);
                                price.cylTypeId = aux.getInt("cylTypeId" + i);
                                price.amount = aux.getInt("cylTypeAmount" + i);
                                price.price = aux.getInt("cylTypePrice" + i);
                                price.cylName = aux.getString("cylName" + i);

                                if (aux.containsKey("priceListId" + i) && aux.containsKey("discListId" + i)) {
                                    price.priceListId = JsonUtils.getString(aux, "priceListId" + i);
                                    price.discListId = JsonUtils.getString(aux, "discListId" + i);
                                }

                                lstPrices.add(price);
                            }

                            if (offLine) {
                                s.lstWarns = new ArrayList<>();
                                if (req.containsKey("clientDate")) {
                                    s.date = sdf.parse(req.getString("clientDate"));
                                }
                            }
                            List<String> nifsDel = new ArrayList<>();
                            for (int i = 0; i < nifDelJA.size(); i++) {
                                nifsDel.add(nifDelJA.getJsonObject(i).getString("nifDel" + i));
                            }

                            List<Cylinder> cylsDel = Cylinder.getCylFromList(conn, nifsDel, offLine, true, s.empId, s);
                            amountsComparate(lstPrices, cylsDel, offLine, s, null);

                            if (!new MySQLQuery("SELECT get_from_biable FROM inv_cfg").getAsBoolean(conn)) {
                                for (int i = 0; i < lstPrices.size(); i++) {
                                    validatePrice(lstPrices.get(i).price, sess.document, lstPrices.get(i).cylTypeId, lstPrices.get(i).cylName, offLine, s, conn);
                                }
                            }

                            List<String> nifsRec = new ArrayList<>();
                            if (nifRecJA != null) {
                                for (int i = 0; i < nifRecJA.size(); i++) {
                                    nifsRec.add(nifRecJA.getJsonObject(i).getString("nifRec" + i));
                                }
                            }

                            List<Cylinder> cylsRec = Cylinder.getCylFromList(conn, nifsRec, offLine, false, s.empId, s);
                            ZoneInfo zoneInfo = getZoneInfo(s.lat, s.lon, false, conn);
                            s.danePobId = zoneInfo.danePobId;
                            s.zone = zoneInfo.zone;
                            s.id = s.insert(s, conn);
                            
                            //Enviar mensaje
                            sendMessage();

                            //metodo para guardar el viaje de la venta
                            updateTripSale(s.empId, s.id, s.date, conn);

                            for (int i = 0; i < cylsDel.size(); i++) {
                                int cylId = cylsDel.get(i).trkCylId;
                                new MySQLQuery("INSERT INTO trk_multi_cyls "
                                        + "SET sale_id = " + s.id + ", "
                                        + "cyl_id = " + cylId + ", "
                                        + "cube_cyl_type_id = " + cylsDel.get(i).cylTypeId + ", "
                                        + "cube_nif_y = " + cylsDel.get(i).year + ", "
                                        + "cube_nif_f = " + cylsDel.get(i).factory + ", "
                                        + "cube_nif_s = " + cylsDel.get(i).serial + ", "
                                        + "type = 'del'").executeInsert(conn);
                                lockCyl(cfg.lockCylSale, cylId, conn);
                            }

                            for (int i = 0; i < cylsRec.size(); i++) {
                                new MySQLQuery("INSERT INTO trk_multi_cyls "
                                        + "SET sale_id = " + s.id + ", "
                                        + "cyl_id = " + cylsRec.get(i).trkCylId + ", "
                                        + "cube_cyl_type_id = " + cylsRec.get(i).cylTypeId + ", "
                                        + "cube_nif_y = " + cylsRec.get(i).year + ", "
                                        + "cube_nif_f = " + cylsRec.get(i).factory + ", "
                                        + "cube_nif_s = " + cylsRec.get(i).serial + ", "
                                        + "type = 'rec'").executeInsert(conn);
                            }
                            for(int i=0; i<cylsDel.size();i++){
                                insertTrkSaleQuality(cylsDel.get(i).trkCylId,s.id,conn);
                            }

                            s.price = 0;
                            for (int i = 0; i < lstPrices.size(); i++) {
                                new MySQLQuery("INSERT INTO trk_multi_prices "
                                        + "SET cyl_type_id = " + lstPrices.get(i).cylTypeId + ", "
                                        + "sale_id = " + s.id + ", "
                                        + "cyl_type_price = " + lstPrices.get(i).price + ", "
                                        + "price_list_id = " + (lstPrices.get(i).priceListId == null ? "NULL, " : "BINARY('" + lstPrices.get(i).priceListId + "'), ")
                                        + "disc_list_id = " + (lstPrices.get(i).discListId == null ? "NULL" : "BINARY('" + lstPrices.get(i).discListId + "')")
                                ).executeInsert(conn);
                                s.price = s.price + lstPrices.get(i).price;
                            }

                            confirmOffer(s, req, conn);
                            isPromoApplicant(s, conn);
                            s.saveWarns(conn);

                            if (req.containsKey("negAnsIds")) {
                                String ids[] = req.getString("negAnsIds").split(",");
                                int neg_quest_id = new MySQLQuery("INSERT INTO com_app_neg_quest SET trk_sale_id = " + s.id).executeInsert(conn);
                                for (String id : ids) {
                                    new MySQLQuery("INSERT INTO com_app_answer SET neg_quest_id = " + neg_quest_id + ", question_id = " + id).executeInsert(conn);
                                }
                            }

                            if (s.dtoPrice != null) {
                                ob.add("dtoPrice", s.dtoPrice);
                            }
                        } else if (!offLine) {
                            throw new ShortException("Ya hay una venta para el mismo cliente y factura en ésta fecha");
                        }
                        break;
                    }
                    case "QPVI": {
                        //consulta info PV
                        String pvDoc = req.containsKey("pvDoc") ? req.getString("pvDoc") : null;
                        String internal = req.containsKey("pvInternal") ? req.getString("pvInternal") : null;
                        String pvName = req.containsKey("pvName") ? req.getString("pvName") : null;

                        Object[][] store = new MySQLQuery("SELECT s.id, s.address, s.phones, s.document, CONCAT(s.first_name, ' ', s.last_name), s.internal "
                                + "FROM inv_store s "
                                + "INNER JOIN inv_store_state t ON t.id = s.state_id "
                                + "WHERE s.active AND t.`type` IN ('nor','prov') "
                                + (internal != null ? "AND s.internal = '" + internal + "' " : "")
                                + (pvDoc != null ? " AND s.document = '" + pvDoc + "'" : "")
                                + (pvName != null ? " AND CONCAT(s.first_name, ' ', s.last_name) LIKE '%" + pvName.replace(" ", "%") + "%'" : "")).getRecords(conn);
                        if (store == null || store.length == 0) {
                            throw new ShortException("No se encontró el expendio.");
                        }

                        JsonArrayBuilder ab = Json.createArrayBuilder();
                        for (Object[] row : store) {
                            JsonObjectBuilder job = Json.createObjectBuilder();
                            job.add("pvId", MySQLQuery.getAsInteger(row[0]));
                            job.add("address", row[1] != null ? MySQLQuery.getAsString(row[1]) : "");
                            job.add("phones", row[2] != null ? MySQLQuery.getAsString(row[2]) : "");
                            job.add("document", row[3] != null ? MySQLQuery.getAsString(row[3]) : "");
                            job.add("name", row[4] != null ? MySQLQuery.getAsString(row[4]) : "");
                            job.add("internal", row[5] != null ? MySQLQuery.getAsString(row[5]) : "");
                            ab.add(job);
                        }
                        ob.add("pvs", ab);
                        break;
                    }
                    case "QPVR": {
                        //Venta Puntos de Venta
                        String document = req.getString("clientDoc");

                        TrkPvSale s = new TrkPvSale();
                        s.dt = new Date();
                        if (offLine) {
                            s.lstWarns = new ArrayList<>();
                            if (req.containsKey("clientDate")) {
                                s.dt = sdf.parse(req.getString("clientDate"));
                            }
                        }

                        String internal = (req.containsKey("internal") ? req.getString("internal") : null);

                        Object[][] store = new MySQLQuery("SELECT id, internal FROM inv_store WHERE document = '" + document + "' " + (internal != null ? " AND internal = '" + internal + "'" : "") + " AND active").getRecords(conn);

                        s.storeId = MySQLQuery.getAsInteger(store[0][0]);
                        s.bill = req.getInt("bill");
                        s.credit = req.getBoolean("credit");
                        if (new MySQLQuery("SELECT COUNT(*) = 0 FROM trk_pv_sale WHERE store_id = " + s.storeId + " AND bill = '" + s.bill + "' AND DATE(dt) = '" + sdfShort.format(s.dt) + "' FOR UPDATE").getAsBoolean(conn)) {
                            Integer storeEmpId = null;
                            if (cfg.lockCylSale) {
                                storeEmpId = new MySQLQuery("SELECT id FROM employee WHERE store_id = " + s.storeId + " AND active").getAsInteger(conn);
                                if (storeEmpId == null) {
                                    throw new ShortException("El expendio no está asociado con un usuario sigma. Comuníquese con Sistemas Montagas");
                                }
                            }

                            s.empId = sess.employeeId;
                            s.lat = MySQLQuery.getAsBigDecimal(req.getString("lat"), true);
                            s.lon = MySQLQuery.getAsBigDecimal(req.getString("lon"), true);

                            JsonArray pricesJA = req.getJsonArray("pricesJAB");
                            JsonArray nifDelJA = req.getJsonArray("nifDelJAB");
                            JsonArray nifRecJA = JsonUtils.getJsonArray(req, "nifRecJAB");

                            List<MultiPrices> lstPrices = new ArrayList<>();
                            for (int i = 0; i < pricesJA.size(); i++) {
                                MultiPrices price = new MultiPrices();
                                JsonObject aux = pricesJA.getJsonObject(i);
                                price.cylTypeId = aux.getInt("cylTypeId" + i);
                                price.amount = aux.getInt("cylTypeAmount" + i);
                                price.price = aux.getInt("cylTypePrice" + i);
                                price.cylName = aux.getString("cylName" + i);

                                if (aux.containsKey("priceListId" + i) && aux.containsKey("discListId" + i)) {
                                    price.priceListId = JsonUtils.getString(aux, "priceListId" + i);
                                    price.discListId = JsonUtils.getString(aux, "discListId" + i);
                                }

                                lstPrices.add(price);
                            }

                            List<String> nifsDel = new ArrayList<>();
                            for (int i = 0; i < nifDelJA.size(); i++) {
                                nifsDel.add(nifDelJA.getJsonObject(i).getString("nifDel" + i));
                            }

                            List<Cylinder> cylsDel = Cylinder.getCylFromList(conn, nifsDel, offLine, true, s.empId, s);
                            amountsComparate(lstPrices, cylsDel, offLine, null, s);

                            if (!new MySQLQuery("SELECT get_from_biable FROM inv_cfg").getAsBoolean(conn)) {
                                for (int i = 0; i < lstPrices.size(); i++) {
                                    validatePricePV(lstPrices.get(i).price, s.storeId, lstPrices.get(i).cylTypeId, lstPrices.get(i).cylName, offLine, s, conn);
                                }
                            } else {
                                s.sucursal = req.getString("sucursal");
                            }

                            List<String> nifsRec = new ArrayList<>();
                            if (nifRecJA != null) {
                                for (int i = 0; i < nifRecJA.size(); i++) {
                                    nifsRec.add(nifRecJA.getJsonObject(i).getString("nifRec" + i));
                                }
                            }
                            List<Cylinder> cylsRec = Cylinder.getCylFromList(conn, nifsRec, offLine, false, s.empId, s);

                            s.stratum = (req.containsKey("stratum") ? Integer.valueOf(req.getString("stratum")) : null);
                            ZoneInfo zoneInfo = getZoneInfo(s.lat, s.lon, false, conn);
                            s.zone = zoneInfo.zone;
                            s.danePobId = zoneInfo.danePobId;
                            s.id = s.insert(s, conn);

                            if (req.containsKey("orderId")) {
                                new MySQLQuery("UPDATE com_store_order SET pv_sale_id = " + s.id + ", confirm_by_id = " + s.empId + ", confirm_dt = '" + sdf.format(s.dt) + "' WHERE id = " + req.getInt("orderId")).executeUpdate(conn);
                            }

                            for (int i = 0; i < cylsDel.size(); i++) {
                                int trkCylId = cylsDel.get(i).trkCylId;
                                new MySQLQuery("INSERT INTO trk_pv_cyls "
                                        + "SET pv_sale_id = " + s.id + ", "
                                        + "cyl_id = " + trkCylId + ", "
                                        + "type = 'del'").executeInsert(conn);
                                if (cfg.lockCylSale) {
                                    new MySQLQuery("UPDATE trk_cyl SET salable = 0, resp_id = " + storeEmpId + " WHERE id = " + trkCylId).executeUpdate(conn);
                                }
                            }

                            for (int i = 0; i < cylsRec.size(); i++) {
                                new MySQLQuery("INSERT INTO trk_pv_cyls "
                                        + "SET pv_sale_id = " + s.id + ", "
                                        + "cyl_id = " + cylsRec.get(i).trkCylId + ", "
                                        + "type = 'rec'").executeInsert(conn);
                            }

                            for (int i = 0; i < lstPrices.size(); i++) {
                                new MySQLQuery("INSERT INTO trk_pv_prices "
                                        + "SET cyl_type_id = " + lstPrices.get(i).cylTypeId + ", "
                                        + "pv_sale_id = " + s.id + ", "
                                        + "cyl_type_price = " + lstPrices.get(i).price + ", "
                                        + "price_list_id = " + (lstPrices.get(i).priceListId == null ? "NULL, " : "BINARY('" + lstPrices.get(i).priceListId + "'), ")
                                        + "disc_list_id = " + (lstPrices.get(i).discListId == null ? "NULL" : "BINARY('" + lstPrices.get(i).discListId + "')")
                                ).executeInsert(conn);
                            }

                            if (s.lstWarns != null && s.lstWarns.size() > 0) {
                                for (int i = 0; i < s.lstWarns.size(); i++) {
                                    TrkPVSaleWarning warn = new TrkPVSaleWarning();
                                    warn.pvSaleId = s.id;
                                    warn.warning = s.lstWarns.get(i);
                                    warn.insert(warn, conn);
                                }
                            }
                        } else if (!offLine) {
                            throw new ShortException("Ya hay una venta para el mismo cliente y factura en ésta fecha");
                        }
                        break;
                    }

                    default:
                        break;
                }
                ob.add("result", "OK");
            } catch (Exception ex) {
                if (ex instanceof ShortException) {
                    ((ShortException) ex).simplePrint();
                } else {
                    Logger.getLogger(CylSales.class.getName()).log(Level.SEVERE, null, ex);
                }
                ob.add("result", "ERROR");
                String m = ex.getMessage();
                if (m != null && !m.isEmpty()) {
                    ob.add("errorMsg", m);
                } else {
                    ob.add("errorMsg", "Error desconocido.");
                }
            } finally {
                w.writeObject(ob.build());
            }
            conn.commit();
        } catch (Exception ex) {
            Logger.getLogger(CylSales.class.getName()).log(Level.SEVERE, null, ex);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex1) {
                    Logger.getLogger(CylSales.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        } finally {
            MySQLSelect.tryClose(conn);
        }
    }
    
    private static void validarBillRepetido(String nBill, int empleadoId, Connection conn) throws ShortException, Exception{
        //Existe una venta con el mismo no. bill
        Object[][] bills = new MySQLQuery("SELECT "
                                + "s.bill "
                                + "FROM trk_sale s "
                                + "WHERE s.bill = "+nBill+" AND s.emp_id ="+empleadoId).getRecords(conn);
        
        boolean existeBill=false;
        if(bills.length>0){
            existeBill=true;
        }
        if(existeBill){
            throw new ShortException("Ya hay una venta con el mismo No. de factura");
        }                             
    }
    
    private static void setLastSaleDays(TrkSale sale, String document, Connection conn) throws Exception {
        String ids = new MySQLQuery("SELECT GROUP_CONCAT(s.id) "
                + "FROM trk_sale s "
                + "INNER JOIN ord_contract_index i ON i.id = s.index_id "
                + "WHERE prom_last_sales <> 0 AND i.document = ?1").setParam(1, document).getAsString(conn);

        if (ids != null && ids.length() > 0) {
            new MySQLQuery("UPDATE trk_sale SET prom_last_sales = 0 WHERE id IN (" + ids + ")").executeUpdate(conn);
        }

        sale.promLastSales = 0;
        if (document != null) {
            Object[][] lastSalesData = new MySQLQuery("SELECT "
                    + "s.date "
                    + "FROM trk_sale s "
                    + "INNER JOIN ord_contract_index i ON s.index_id = i.id "
                    + "WHERE i.document = ?1 "
                    + "AND s.date > DATE_SUB(NOW(),INTERVAL 6 MONTH)").setParam(1, document).getRecords(conn);

            if (lastSalesData.length == 0) {
                sale.promLastSales = 0;
            } else if (lastSalesData.length == 1) {
                Date lastSale = MySQLQuery.getAsDate(lastSalesData[0][0]);
                Date now = new Date();

                long diff = now.getTime() - lastSale.getTime();
                sale.promLastSales = MySQLQuery.getAsInteger((diff / (1000 * 60 * 60 * 24)));
            } else {
                List<Integer> daysBetweenSales = new ArrayList<>();
                for (int i = 0; i < lastSalesData.length - 1; i++) {
                    Object[] firstRow = lastSalesData[i];
                    for (int j = 1; j < lastSalesData.length; j++) {
                        Object[] secondRow = lastSalesData[j];
                        Date lastSale = MySQLQuery.getAsDate(firstRow[0]);
                        Date now = MySQLQuery.getAsDate(secondRow[0]);

                        long diff = now.getTime() - lastSale.getTime();
                        daysBetweenSales.add(MySQLQuery.getAsInteger((diff / (1000 * 60 * 60 * 24))));
                    }
                }

                int sum = 0;
                for (int i = 0; i < daysBetweenSales.size(); i++) {
                    sum = sum + daysBetweenSales.get(i);
                }

                sale.promLastSales = MySQLQuery.getAsInteger(Math.ceil(sum / daysBetweenSales.size()));
            }
        }
    }

    public static void lockCyl(boolean lockCylSale, int cylId, Connection conn) throws Exception {
        if (lockCylSale) {
            new MySQLQuery("UPDATE trk_cyl SET salable = 0, resp_id = NULL WHERE id = " + cylId).executeUpdate(conn);
        }
    }

    private String get(JsonObject req, String key) {
        return req.containsKey(key) ? req.getString(key) : "";
    }

    private Boolean findMatch(Contract contract, Cylinder nifCyl, Connection conn) throws Exception {
        if ((contract == null || !contract.brand) || nifCyl == null) {
            return null;
        }
        int[] contractCylTypes = getCylTypeIdsByContract(conn, contract.contractId);
        for (int i = 0; i < contractCylTypes.length; i++) {
            if (contractCylTypes[i] == nifCyl.cylTypeId) {
                return true;
            }
        }
        return false;
    }

    private void amountsComparate(List<MultiPrices> lstPrices, List<Cylinder> cylsDel, boolean offLine, TrkSale s, TrkPvSale pvs) throws Exception {
        List<AmountComparator> lstCmp = new ArrayList<>();
        for (int i = 0; i < lstPrices.size(); i++) {
            AmountComparator cmp = new AmountComparator();
            cmp.cylTypeId = lstPrices.get(i).cylTypeId;
            cmp.cylName = lstPrices.get(i).cylName;
            cmp.writeAmount = lstPrices.get(i).amount;
            cmp.scnAmount = 0;
            lstCmp.add(cmp);
        }

        for (int i = 0; i < cylsDel.size(); i++) {
            boolean exist = false;
            for (int j = 0; j < lstCmp.size() && !exist; j++) {
                if (cylsDel.get(i).cylTypeId == lstCmp.get(j).cylTypeId) {
                    lstCmp.get(j).scnAmount++;
                    exist = true;
                }
            }

            if (!exist) {
                AmountComparator cmp = new AmountComparator();
                cmp.cylTypeId = cylsDel.get(i).cylTypeId;
                cmp.cylName = cylsDel.get(i).mgName;
                cmp.writeAmount = 0;
                cmp.scnAmount = 1;
                lstCmp.add(cmp);
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lstCmp.size(); i++) {
            if (lstCmp.get(i).writeAmount != lstCmp.get(i).scnAmount) {
                sb.append("Cap. ").append(lstCmp.get(i).cylName).append(" reporta: ").append(lstCmp.get(i).writeAmount).append(" escaneado: ").append(lstCmp.get(i).scnAmount);
                if (lstCmp.get(i).writeAmount < lstCmp.get(i).scnAmount) {
                    int diff = lstCmp.get(i).scnAmount - lstCmp.get(i).writeAmount;
                    int rep = 0;
                    for (int j = 0; j < cylsDel.size() && rep < diff; j++) {
                        if (cylsDel.get(j).cylTypeId == lstCmp.get(i).cylTypeId) {
                            sb.append(" nif: ").append(cylsDel.get(j).cylNif).append(",");
                            rep++;
                        }
                    }
                    sb.deleteCharAt(sb.length() - 1);
                }
                sb.append(System.lineSeparator());
            }
        }

        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
            if (s != null) {
                manageException("Existen inconsistencias en las cantidades de los cilindros." + System.lineSeparator() + sb.toString(), offLine, s);
            } else if (pvs != null) {
                manageException("Existen inconsistencias en las cantidades de los cilindros." + System.lineSeparator() + sb.toString(), offLine, pvs);
            } else {
                throw new Exception("Don't arrive sale or pvsale");
        }
    }
    }

    public static int insertContract(String cliType, String document, String firstName, String lastName, String phone, Integer cityId, String address, String neigh, int empId, Connection conn) throws Exception {
        return insertContract(cliType, document, firstName, lastName, phone, cityId, address, neigh, empId, conn, null);
    }

    public static int insertContract(String cliType, String document, String firstName, String lastName, String phone, Integer cityId, String address, String neigh, int empId, Connection conn, Integer neighId) throws Exception {
        int ctrId = new MySQLQuery("INSERT INTO "
                + "ord_contract SET "
                + "`address` = '" + address + " " + neigh + "', "
                + (phone != null ? "`phones` = '" + phone + "', " : "`phones` = NULL, ")
                + "`own` = 1, "
                + "`establish_id` = null, "
                + "`energy_id` = null, "
                + "`neigh_id` = " + (neighId != null ? neighId : "null") + ", "
                + "`people` = 0, "
                + "`notes` = null, "
                + "`state` = 'open', "
                + "`closed_pending_date` = null, "
                + "`closed_pending_notes` = null, "
                + "`city_id` = " + cityId + ", "
                + "`creator_id` = " + empId + ", "
                + "`created_date` = NOW(), "
                + "`created_from` = 'mvoff', "
                + "`document` = '" + document + "', "
                + "`first_name` = '" + firstName + "', "
                + "`last_name` = '" + lastName + "', "
                + "`cli_type` = '" + (cliType != null ? cliType : "nat") + "'").executeInsert(conn);

        return new MySQLQuery("INSERT INTO ord_contract_index SET "
                + "`contract_num` = null, "
                + "`ctr_type` = 'afil', "
                + "`cli_type` = '" + (cliType != null ? cliType : "nat") + "', "
                + "`document` = '" + document + "', "
                + "`address` = '" + address + " " + neigh + "', "
                + (phone != null ? "`phones` = '" + phone + "', " : "`phones` = NULL, ")
                + "`first_name` = '" + firstName + "', "
                + "`last_name` = '" + lastName + "', "
                + "`est_name` = null, "
                + "`contract_id` = " + ctrId + ", "
                + "`type` = 'univ', "
                + "`neigh_id` = " + (neighId != null ? neighId : "null") + ", "
                + "`city_id` = " + cityId + ", "
                + "`vehicle_id` = null, "
                + "`sower_id` = " + empId + ", "
                + "`active` = 1").executeInsert(conn);
    }

    public static String clearAddress(String dir) {
        if (dir == null) {
            return "";
        }
        dir = dir.toUpperCase();
        dir = Pattern.compile("[\\s]{2,}", Pattern.CASE_INSENSITIVE).matcher(dir).replaceAll(".").trim();
        Pattern p = Pattern.compile("[A-Z][0-9]|[0-9][A-Z]");
        Matcher mt = p.matcher(dir);
        while (mt.find()) {
            String p1 = dir.substring(0, mt.start() + 1);
            String p2 = dir.substring(mt.start() + 1);
            dir = p1 + " " + p2;
            mt = p.matcher(dir);
        }

        String[][] replacements = new String[][]{
            {"N0|#|NO|NUM|NUMERO|NRO", " "},
            {"CC", "CE"},
            /////////////////////////////////
            {"ADMINISTRACIÓN", "AD"},
            {"AEROPUERTO", "AE"},
            {"AGRUPACIÓN", "AG"},
            {"ALTILLO", "AL"},
            {"APARTAMENTO|APTO|APT", "AP"},//#
            {"AUTOPISTA", "AU"},
            {"AVENIDA|AVN", "AV"},//#
            //{"AVENIDA CALLE", "AC"},
            //{"AVENIDA CARRERA", "AK"},
            {"BARRIO|BRR", "BR"},
            {"BIS", "BIS"},
            {"BLOQUE|BL|BLQ|BQQ|BLO|BQO|BQQ", "BQ"},//#
            {"BODEGA", "BG"},
            //{"BULEVAR", "BL"},
            {"KRAA|KRA|KRRA|KRR|KRRA|KARRERA", "KR"},//#
            {"CR|CRA|CARRERA|CRRA|CRR|CRRA|CAR", "KR"},//#
            {"CARRETERA", "CT"},
            {"CALLE|CLE|CLLE|CAL|CLL", "CL"},//#
            {"CASA|CAS|CSA|CSS", "CS"},//#
            {"CELULA", "CU"},
            {"CENTRO COMERCIAL", "CE"},
            {"CIRCULAR", "CQ"},
            {"CIRCUNVALAR", "CV"},
            {"CIUDADELA", "CD"},
            {"CONJUNTO RESIDENCIAL", "CO"},
            {"CONSULTORIO", "CN"},
            {"CORREGIMIENTO|CORREG|CORREJIMIENTO", "CTO"},
            //{"CUENTAS CORRIDAS", "CC"},
            {"DEPOSITO", "DP"},
            //{"DEPOSITO SÓTANO", "DS"},
            {"DIAGONAL|DIAG|DGL|DGNAL|DGONAL|DGAL|DGNAL", "DG"},//#
            {"EDIFICIO", "ED"},
            {"ENTRADA", "EN"},
            {"ESQUINA", "EQ"},
            {"ESTACION", "ES"},
            {"ESTE", "ESTE"},
            {"ETAPA|ETP", "ET"},//#
            {"EXTERIOR", "EX"},
            {"FINCA", "FI"},
            {"GARAJE", "GA"},
            //{"GARAJE SÓTANO", "GS"},
            {"INTERIOR", "IN"},
            {"KILOMETRO", "KM"},
            {"LOCAL|LOCL", "LC"},
            //{"LOCAL MEZZANINE", "LM"},
            {"LOTE", "LT"},
            {"MANZANA|MZA|MAN|MANZ|MAZ", "MZ"},//#
            //{"MEZZANINE", "MN"},
            //{"MODULO", "MD"},
            {"NORTE", "NORTE"},
            {"OESTE", "OESTE"},
            {"OFICINA", "OF"},
            {"PARQUE", "PQ"},
            {"PARQUEADERO", "PA"},
            {"PASAJE", "PJ"},
            {"PASEO", "PS"},
            {"PEATONAL", "PT"},
            //{"PENT-HOUSE", "PN"},
            {"PISO", "PI"},
            {"PLANTA", "PL"},
            {"PORTERÍA", "PR"},
            {"PREDIO", "PD"},
            {"PUESTO", "PU"},
            //{"ROUND POINT (GLORIETA)", "RP"},
            {"SECTOR", "SC"},
            //{"SEMISÓTANO", "SS"},
            {"SOTANO", "SO"},
            //{"SUITE", "ST"},
            //{"SUPERMANZANA", "SM"},
            {"SUR", "SUR"},
            {"TERRAZA", "TZ"},
            {"TORRE|TORRES", "TO"},
            {"TRANSVERSAL|TRASVERSAL", "TV"},
            {"TRONCAL", "TC"},
            {"UNIDAD", "UN"},
            //{"UNIDAD RESIDENCIAL", "UL"},
            {"URBANIZACION|URB", "UR"},
            //{"VARIANTE", "VT"},
            {"VEREDA|VDA|VR|VD", "VD"},//#
            {"VIA", "VI"},
            {"ZONA", "ZN"},};
        dir = Pattern.compile("[^A-Z0-9Ñ]", Pattern.CASE_INSENSITIVE).matcher(dir).replaceAll(" ");
        for (String[] replacement : replacements) {
            dir = Pattern.compile("\\b(" + replacement[0] + ")\\b", Pattern.CASE_INSENSITIVE).matcher(dir).replaceAll(replacement[1]);
        }
        dir = Pattern.compile("[\\s]{2,}", Pattern.CASE_INSENSITIVE).matcher(dir).replaceAll(" ").trim();
        Matcher matcher = Pattern.compile("\\b[0]+(\\d{1,})\\b", Pattern.CASE_INSENSITIVE).matcher(dir);
        while (matcher.find()) {
            dir = matcher.replaceAll(matcher.group(1));
            matcher = Pattern.compile("\\b[0]+(\\d)\\b", Pattern.CASE_INSENSITIVE).matcher(dir);
        }
        return dir;
    }

    public static ZoneInfo getZoneInfo(BigDecimal lat, BigDecimal lon, boolean subsidy, Connection conn) throws Exception {
        ZoneInfo zInfo = new ZoneInfo();
        Object[] record;
        if (subsidy) {
            record = new MySQLQuery("SELECT IF(p.type = 'CM', 'ur', 'ru'), p.id FROM dane_poblado p "
                    + "INNER JOIN dto_sub_mun m ON p.mun_id = m.mun_id "
                    + "WHERE lat IS NOT NULL AND lon IS NOT NULL "
                    + "ORDER BY (POW(p.lat - (" + lat + "),2) + POW(p.lon - (" + lon + "),2)) * IF(p.type = 'CM', 0.2, 1) ASC LIMIT 1").getRecord(conn);
        } else {
            record = new MySQLQuery("SELECT IF(p.type = 'CM', 'ur', 'ru'), p.id FROM dane_poblado p "
                    + "WHERE lat IS NOT NULL AND lon IS NOT NULL "
                    + "ORDER BY (POW(p.lat - (" + lat + "),2) + POW(p.lon - (" + lon + "),2)) * IF(p.type = 'CM', 0.2, 1) ASC LIMIT 1").getRecord(conn);
        }
        zInfo.zone = MySQLQuery.getAsString(record[0]);
        zInfo.danePobId = MySQLQuery.getAsInteger(record[1]);
        return zInfo;
    }

    public static void setVhAndManager(TrkSale s, Connection conn) throws Exception {
        s.vehicleId = new MySQLQuery("SELECT dv.vehicle_id FROM driver_vehicle dv WHERE dv.driver_id = " + s.empId + " AND dv.`end` IS NULL").getAsInteger(conn);
        if (s.vehicleId != null) {
            s.manId = new MySQLQuery("SELECT man_id FROM com_man_veh WHERE veh_id = " + s.vehicleId).getAsInteger(conn);
        }
        if (s.manId == null) {
            s.manId = new MySQLQuery("SELECT s.man_id FROM "
                    + "employee e "
                    + "INNER JOIN com_man_store s ON s.store_id = e.store_id "
                    + "WHERE e.id = " + s.empId).getAsInteger(conn);
        }
    }

    private static void setDtoSalesman(TrkSale s, Connection conn) throws Exception {
        s.smanId = new MySQLQuery("SELECT id FROM dto_salesman WHERE active AND document = (SELECT document FROM employee WHERE id = " + s.empId + ")").getAsInteger(conn);
    }

    private static TrkSale registerSale(TrkTransaction trans, int empId, Integer stratum, String zone, Integer danePobId, Boolean isSowing, boolean training, Connection conn) throws Exception {
        TrkSale s = new TrkSale();
        s.auth = trans.auth;
        s.bill = trans.bill;
        s.cylinderId = trans.cylId;
        s.date = trans.dt;
        s.empId = empId;
        s.indexId = trans.indexId;
        s.lat = MySQLQuery.getAsBigDecimal(trans.lat, true);
        s.lon = MySQLQuery.getAsBigDecimal(trans.lon, true);
        s.price = trans.price;
        s.subsidy = trans.valSub;
        s.saleType = "sub";
        s.cylReceivedId = trans.cylReceivedId;
        s.stratum = stratum > 2 ? 2 : stratum;
        s.zone = zone;
        s.danePobId = danePobId;
        s.isSowing = isSowing;
        s.discount = trans.discount;
        s.bonusCode = trans.bonusCode;
        s.training = training;
        
        setVhAndManager(s, conn);
        setDtoSalesman(s, conn);

        Object[] cylRow = new MySQLQuery("SELECT nif_y, nif_f, nif_s, cyl_type_id FROM trk_cyl WHERE id = " + trans.cylId).getRecord(conn);
        s.cubeNifY = MySQLQuery.getAsInteger(cylRow[0]);
        s.cubeNifF = MySQLQuery.getAsInteger(cylRow[1]);
        s.cubeNifS = MySQLQuery.getAsInteger(cylRow[2]);
        s.cubeCylTypeId = MySQLQuery.getAsInteger(cylRow[3]);
        setLastSaleDays(s, trans.document, conn);

        s.id = s.insert(s, conn);
        //dto_sale
        DtoSale d=new DtoSale();
        d.dt=s.date;
        Integer documento=new MySQLQuery("SELECT c.document "
                + "FROM sigma.ord_contract_index c "
                + "WHERE c.id = "+s.indexId+" "
                        + "LIMIT 1").getAsInteger(conn);
        d.clieDoc=documento;
        Integer idCentro=new MySQLQuery("SELECT v.center_id "
                + "FROM sigma.dto_salesman v "
                + "WHERE v.id="+s.smanId+" "
                        + "LIMIT 1").getAsInteger(conn);
        d.centerId=idCentro;
        d.stratum=s.stratum;
        d.valueTotal=s.price;
        d.subsidy=s.subsidy;
        d.nif=s.cubeNifY.toString()+s.cubeNifF.toString()+s.cubeNifS.toString();
        d.cylTypeId=s.cubeCylTypeId;
        d.salesmanId=s.smanId;
        d.dtoLiqId=s.liqId;
        d.importNotes="Venta cargada desde app";
        d.state="ok";    
        d.aprovNumber=Integer.parseInt(s.auth);
        d.bill=new BigInteger(s.bill);
        d.trkSaleId=s.id;
        
        d.id= d.insert(conn);
        
        //Calidad glp
        insertTrkSaleQuality(trans.cylId,s.id, conn);
        return s;
    }

    private static String cleanAuth(String auth) {
        if (auth.equals("Capacitación")) {
            return auth;
        } else {
            Pattern pattern = Pattern.compile("[^0-9]+");
            Matcher matcher = pattern.matcher(auth);
            if (matcher.find()) {
                String res = auth.replaceAll("\\d*\\.\\d", "");
                return res.replaceAll("[^0-9]+", "");
            } else {
                return auth;
            }
        }
    }

    private int[] getCylTypeIdsByContract(Connection conn, int ctrId) throws Exception {
        Object[][] records = new MySQLQuery("SELECT "
                + "type_id "
                + "FROM "
                + "smb_ctr_cyl "
                + "WHERE contract_id = " + ctrId + " "
                + "GROUP BY type_id").getRecords(conn);
        if (records != null && records.length > 0) {
            int[] rta = new int[records.length];
            for (int i = 0; i < records.length; i++) {
                rta[i] = MySQLQuery.getAsInteger(records[i][0]);
            }
            return rta;
        } else {
            return new int[0];
        }
    }

    public static void validatePrice(int price, String salesmanDoc, Integer typeId, String mgName, boolean offLine, WarningList sale, Connection conn) throws Exception {
        Object[][] prices = new MySQLQuery("SELECT "
                + "price_from, "
                + "price_to "
                + "FROM dto_cyl_price "
                + "WHERE cylinder_type_id = " + typeId + " "
                + "AND center_id = (SELECT center_id "
                + "FROM dto_salesman "
                + "WHERE document = '" + salesmanDoc + "' AND active = 1)").getRecords(conn);

        if (prices == null || prices.length == 0) {
            manageException("No hay lista de precios " + mgName + "\nReporte al área encargada", offLine, sale);
        } else {
            int minPrice = MySQLQuery.getAsInteger(prices[0][0]);
            int maxPrice = MySQLQuery.getAsInteger(prices[0][1]);

            if (minPrice > price || price > maxPrice) {
                manageException("Precio " + price + " fuera de rango\npara cilindro de " + mgName + " lbs.", offLine, sale);
    }
        }
    }

    private void validatePricePV(int price, int storeId, Integer typeId, String mgName, boolean offLine, TrkPvSale sale, Connection conn) throws Exception {
        Object[][] prices = new MySQLQuery("SELECT "
                + "price_from, "
                + "price_to "
                + "FROM inv_cyl_price "
                + "WHERE cyl_type_id = " + typeId + " "
                + "AND center_id = (SELECT center_id "
                + "FROM inv_store "
                + "WHERE id = " + storeId + ")").getRecords(conn);

        if (prices == null || prices.length == 0) {
            manageException("No hay lista de precios para cilindros " + mgName + "\nReporte al área encargada", offLine, sale);
        } else {
            int minPrice = MySQLQuery.getAsInteger(prices[0][0]);
            int maxPrice = MySQLQuery.getAsInteger(prices[0][1]);

            if (minPrice > price || price > maxPrice) {
                manageException("Precio " + price + " fuera de rango\npara cilindro de " + mgName + " lbs.", offLine, sale);
            }
        }
    }

    private static void manageException(String ex, boolean offLine, WarningList s) throws Exception {
        if (!offLine) {
            throw new ShortException(ex);
        } else {
            s.addWarn(ex);
        }
    }

    private static void manageException(String ex, boolean offLine, List<String> warns) throws Exception {
        if (!offLine) {
            throw new ShortException(ex);
        } else {
            warns.add(ex);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Cyl Info";

    }

    public static void updateTripSale(int driverId, int saleId, Date date, Connection conn) throws Exception {
        //el tipo 141 hay que marcarlo de alguna manera por que solo se hará para viajes de ventas
        Object[][] tripId = new MySQLQuery("SELECT t.id FROM gt_cyl_trip t WHERE "
                + " t.driver_id = " + driverId + " "
                + " AND t.cancel = 0 "
                + " AND t.type_id = 141 "
                + " AND '" + Dates.getSQLDateTimeFormat().format(date) + "' "
                + " BETWEEN t.sdt AND IFNULL(t.edt, NOW()) ").getRecords(conn);

        if (tripId != null && tripId.length == 1 && tripId[0] != null) {
            new MySQLQuery("UPDATE trk_sale s SET s.gt_trip_id = " + tripId[0][0] + " WHERE s.id = " + saleId).executeUpdate(conn);
        }
    }

    public static void insertTrkSaleQuality(int trkCylId,int saleId, Connection conn)throws Exception{
        Object[] quality=new MySQLQuery("SELECT "
                                    + "t.c_3, "
                                    + "t.c_4, "
                                    + "t.c_5, "
                                    + "t.agua "
                                    + "FROM sigma.trk_cyl t "
                                    + "WHERE t.id="+trkCylId).getRecord(conn);
        if (quality!=null){
            new MySQLQuery("INSERT INTO sigma.trk_sale_quality SET id_trk_sale="+saleId+", c_3="+(BigDecimal)quality[0]+", c_4="+(BigDecimal)quality[1]+", c_5="+(BigDecimal)quality[2]+", agua="+(BigDecimal)quality[3]+", id_cyl="+trkCylId).executeInsert(conn);
        }
        
    }
    
    public static Contract getContract(String document, String firstName, String lastName, Connection conn) throws Exception {
        if (firstName == null || firstName.isEmpty()) {
            firstName = "Sin ";
            lastName = " Información";
        }

        Contract contract = Contract.searchContract(conn, document);
        if (contract == null) {
            int indexId = insertContract(null, document, firstName, lastName, null, 3, "Sin ", "Información ", 1, conn);
            contract = Contract.searchContract(conn, indexId);
        } else if (contract.firstName.equals("null") || contract.firstName.equals("Sin ")) {
            String fName = firstName;
            String lName = lastName;
            new MySQLQuery("UPDATE ord_contract_index SET "
                    + "first_name = '" + fName + "', last_name = '" + lName + "', address = 'Sin Información' "
                    + "WHERE id = " + contract.indexId).executeUpdate(conn);
            if (contract.brand) {
                new MySQLQuery("UPDATE contract SET "
                        + "first_name = '" + fName + "', last_name = '" + lName + "', address = 'Sin Información' "
                        + "WHERE id = " + contract.contractId).executeUpdate(conn);
            } else {
                new MySQLQuery("UPDATE ord_contract SET "
                        + "first_name = '" + fName + "', last_name = '" + lName + "', address = 'Sin Información' "
                        + "WHERE id = " + contract.contractId).executeUpdate(conn);
            }
        }
        return contract;
    }

    private TrkTransaction setTransaction(JsonObject req, SessionLogin sess, JsonObjectBuilder ob, boolean offLine, List<String> warns, boolean phantomNif, ComCfg cfg, Connection conn) throws Exception {
        TrkTransaction trans = new TrkTransaction();
        WarningListWrapper lw = new WarningListWrapper(warns);
        try {
            trans.document = req.getString("clientDoc");
            trans.dt = new Date();
            if (offLine) {
                if (req.containsKey("clientDate")) {
                    trans.dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(req.getString("clientDate"));
                }
            }

            Contract contract;
            if (req.containsKey("firstName")) {
                contract = getContract(trans.document, req.getString("firstName"), req.getString("lastName"), conn);
            } else {
                contract = getContract(trans.document, null, null, conn);
            }

            //aaaa 
            trans.indexId = contract.indexId;
            trans.price = req.getInt("price");
            trans.valSub = req.getInt("valSub");
            trans.lat = req.getString("lat");
            trans.lon = req.getString("lon");

            if (req.containsKey("minasError")) {
                manageException(req.getString("minasError"), offLine, warns);
            }
            if (req.containsKey("bill")) {
                trans.bill = req.getString("bill");
                validarBillRepetido(trans.bill, sess.employeeId, conn);
            }
            trans.empId = sess.employeeId;
            Cylinder nifCyl = Cylinder.getCylinder(req.getString("nif"), true, true, offLine, lw, trans.empId, conn);
            trans.cylId = nifCyl.trkCylId;

            if (req.containsKey("nifReceived")) {
                int recId = Cylinder.getCylinder(req.getString("nifReceived"), false, false, offLine, lw, trans.empId, conn).trkCylId;
                trans.cylReceivedId = recId != 0 ? recId : null;
            }

            if (!GPSCheck.hit(Double.valueOf(trans.lon), Double.valueOf(trans.lat))) {
                manageException("Coordenadas inválidas", offLine, warns);
            }

            if (new MySQLQuery("SELECT COUNT(*) > 0 FROM ord_miss_document WHERE end_date IS NULL AND document = '" + trans.document + "'").getAsBoolean(conn)) {
                manageException("Atención cédula reportada en SAC", offLine, warns);
            }

            validatePrice(trans.price, sess.document, nifCyl.cylTypeId, nifCyl.mgName, offLine, lw, conn);

            Boolean cylsMatch = findMatch(contract, nifCyl, conn);
            if (cylsMatch != null) {
                ob.add("cylMatch", cylsMatch);
            }
            ob.add("hasContract", true);
            if (contract.phones != null && !contract.phones.isEmpty()) {
                ob.add("phone", contract.phones);
            }

            if (!offLine) {
                Promo p = isInmediatlyPromoApplicant(conn, trans.document, true, trans.indexId, null, null, null, MySQLQuery.getAsBigDecimal(trans.lat, true), MySQLQuery.getAsBigDecimal(trans.lon, true), trans.cylId);
                if (p != null) {
                    if (p.prizePercent != null) {
                        ob.add("prizePercent", p.prizePercent);
                    } else if (p.prizeValue != null) {
                        ob.add("prizeValue", p.prizeValue);
                    }
                }
                int anotherPrize = p != null ? p.prizeValue != null ? p.prizeValue : MySQLQuery.getAsInteger(trans.price * (p.prizePercent / 100d)) : 0;
                PromoMultiple pm = isMultiPromoApplicant(trans.indexId, nifCyl.cylTypeId, trans.price, trans.valSub, anotherPrize, contract.phones, false, cfg, conn);
                if (pm != null && pm.prizeValue != 0) {
                    ob.add("prizeValue", p.prizeValue);
                }
            }
            ob.add("cylCap", nifCyl.mgName);
        } catch (Exception ex) {
            trans.error1 = ex.getMessage();
            throw ex;
        } finally {
            trans.id = trans.insert(trans, conn);
            ob.add("transId", trans.id);
        }

        if (phantomNif) {
            if (req.containsKey("altCylId")) {
                int altCylId = req.getInt("altCylId");
                if (new MySQLQuery("SELECT COUNT(*) = 0 FROM trk_no_rot_sold WHERE no_rot_cyl = " + altCylId + " AND transaction_id = " + trans.id + " FOR UPDATE").getAsBoolean(conn)) {
                    new MySQLQuery("INSERT INTO trk_no_rot_sold SET no_rot_cyl = " + altCylId + ", transaction_id = " + trans.id).executeInsert(conn);
                }
            }
        }
        return trans;
    }

    private class MultiPrices {

        public int cylTypeId;
        public String cylName;
        public int amount;
        public int price;
        public String priceListId;
        public String discListId;

    }

    private class AmountComparator {

        public int cylTypeId;
        public String cylName;
        public int writeAmount;
        public int scnAmount;

    }

    public static class ZoneInfo {

        public Integer danePobId;
        public String zone;

    }

    public class Applicant {

        public Boolean applZone;
        public Boolean applCity;
        public Boolean applSector;
        public Boolean applCylType;
        public Boolean applIndex;
        public Boolean applPref;
        public Boolean applEst;

    }

    public class Promo {

        public List<Integer> ids = new ArrayList<>();
        public Integer prizeValue;
        public Integer prizePercent;

    }

    public class PromoMultiple {

        public int indexId;
        public Integer superIndexId;
        public int prizeValue;
        public Date saleDt;
        public int promoId;

    }
}
