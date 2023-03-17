package web.tanks;

import web.ShortException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
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
import service.MySQL.MySQLCommon;
import service.MySQL.MySQLSelect;
import utilities.Dates;
import utilities.MySQLQuery;
import web.marketing.cylSales.promo.ComPromoTarget;
import web.marketing.cylSales.promo.ComPromoTargetGrouped;
import web.marketing.smsClaro.ClaroSmsSender;
import web.push.GCMUtils;
import web.quality.MailCfg;
import web.quality.SendMail;

@MultipartConfig
@WebServlet(name = "EstSales", urlPatterns = {"/EstSales"})
public class EstSaleServlet extends HttpServlet {

    /**
     * Toda venta se llama aqui
     *
     * @param sale
     * @param conn
     * @throws Exception
     */
    private String isPromoApplicant(EstSale sale, String phone, Connection conn) throws Exception {
        Object[][] promos = new MySQLQuery("SELECT id, beg_date, sale_amount, prize_value, prize_percent, prize_id FROM com_promo WHERE active AND tank_promo AND NOW() BETWEEN beg_date AND end_date AND auth_date IS NOT NULL").getRecords(conn);
        Applicant applicant;
        Integer zoneId = null;
        Integer cityId = null;
        Integer sectorId = null;
        Integer tkClieId = null;
        Integer tkTypeId = null;
        Integer categId = null;
        String msg = null;
        for (int i = 0; i < promos.length; i++) {
            int promoId = MySQLQuery.getAsInteger(promos[i][0]);
            List<ComPromoTargetGrouped> targLst = ComPromoTarget.getFromPromoGroupedTanks(promoId, conn);
            applicant = new Applicant();
            if (targLst != null) {
                for (int j = 0; j < targLst.size(); j++) {
                    ComPromoTargetGrouped item = targLst.get(j);
                    if (item.targType == ComPromoTargetGrouped.TYPE_ZONE) {
                        Integer saleZoneId = new MySQLQuery("SELECT c.zone_id "
                                + "FROM ord_tank_client tc "
                                + "INNER JOIN neigh n ON tc.neigh_id = n.id "
                                + "INNER JOIN sector s ON n.sector_id = s.id "
                                + "INNER JOIN city c ON s.city_id = c.id "
                                + "WHERE tc.id = " + sale.clientId).getAsInteger(conn);
                        if (saleZoneId != null) {
                            for (int k = 0; k < item.targLst.size(); k++) {
                                ComPromoTarget target = item.targLst.get(k);
                                if (target.zoneId != null) {
                                    applicant.applZone = Objects.equals(target.zoneId, saleZoneId);
                                    if (applicant.applZone) {
                                        zoneId = target.zoneId;
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
                        Integer saleCityId = new MySQLQuery("SELECT c.city_id FROM ord_tank_client c WHERE c.id = " + sale.clientId).getAsInteger(conn);
                        if (saleCityId != null) {
                            for (int k = 0; k < item.targLst.size(); k++) {
                                ComPromoTarget target = item.targLst.get(k);
                                if (target.cityId != null) {
                                    applicant.applCity = Objects.equals(target.cityId, saleCityId);
                                    if (applicant.applCity) {
                                        cityId = target.cityId;
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
                        Integer saleSectorId = new MySQLQuery("SELECT n.sector_id FROM ord_tank_client c INNER JOIN neigh n ON c.neigh_id = n.id WHERE c.id = " + sale.clientId).getAsInteger(conn);
                        if (saleSectorId != null) {
                            for (int k = 0; k < item.targLst.size(); k++) {
                                ComPromoTarget target = item.targLst.get(k);
                                if (target.sectorId != null) {
                                    applicant.applSector = Objects.equals(target.sectorId, saleSectorId);
                                    if (applicant.applSector) {
                                        sectorId = target.sectorId;
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

                    if (item.targType == ComPromoTargetGrouped.TYPE_TK_CLIENT) {
                        for (int k = 0; k < item.targLst.size(); k++) {
                            ComPromoTarget target = item.targLst.get(k);
                            applicant.applTkClient = target.tkClientId.equals(sale.clientId);
                            if (applicant.applTkClient) {
                                tkClieId = sale.clientId;
                                break;
                            }
                        }
                    }

                    boolean indCount = false;
                    if (item.targType == ComPromoTargetGrouped.TYPE_TK_CATEGORY) {
                        Integer catId = new MySQLQuery("SELECT categ_id FROM ord_tank_client WHERE id = " + sale.clientId).getAsInteger(conn);
                        if (catId != null) {
                            for (int k = 0; k < item.targLst.size(); k++) {
                                ComPromoTarget target = item.targLst.get(k);
                                applicant.applTkCategory = Objects.equals(catId, target.tkCategory);
                                if (applicant.applTkCategory) {
                                    categId = catId;
                                    indCount = target.individualCount;
                                    break;
                                }
                            }
                        } else {
                            applicant.applTkCategory = false;
                        }
                    }

                    if (item.targType == ComPromoTargetGrouped.TYPE_TK_CAT_TYPE) {
                        Integer typeId = new MySQLQuery("SELECT ect.id "
                                + "FROM ord_tank_client otc "
                                + "INNER JOIN est_tank_category etc ON otc.categ_id = etc.id "
                                + "INNER JOIN est_categ_type ect ON etc.type_id = ect.id "
                                + "WHERE otc.id = " + sale.clientId).getAsInteger(conn);
                        if (typeId != null) {
                            for (int k = 0; k < item.targLst.size(); k++) {
                                ComPromoTarget target = item.targLst.get(k);
                                applicant.applTkCatType = Objects.equals(typeId, target.tkCatType);
                                if (applicant.applTkCatType) {
                                    tkTypeId = typeId;
                                    indCount = target.individualCount;
                                    break;
                                }
                            }
                        } else {
                            applicant.applTkCatType = false;
                        }
                    }

                    if ((applicant.applTkCatType != null && applicant.applTkCatType) || (applicant.applTkCategory != null && applicant.applTkCategory)) {
                        if (indCount) {
                            tkClieId = sale.clientId;
                        }
                    }

                }
            }

            if (applToPromo(applicant)) {
                Integer salesAmount = MySQLQuery.getAsInteger(promos[i][2]);
                DecimalFormat df = new DecimalFormat("#,##0");
                Object[] summRow = new MySQLQuery("SELECT id, cnt "
                        + "FROM com_promo_count "
                        + "WHERE promo_id = " + promoId + " "
                        + "AND zone_id " + (zoneId == null ? " IS NULL" : " = " + zoneId) + "  "
                        + "AND city_id " + (cityId == null ? " IS NULL" : " = " + cityId) + "  "
                        + "AND sector_id " + (sectorId == null ? " IS NULL" : " = " + sectorId) + "  "
                        + "AND tk_client_id " + (tkClieId == null ? " IS NULL" : " = " + tkClieId) + " "
                        + "AND tk_cat_type " + (tkTypeId == null ? " IS NULL" : " = " + tkTypeId) + " "
                        + "AND tk_category " + (categId == null ? " IS NULL" : " = " + categId)).getRecord(conn);
                if (summRow != null) {
                    Integer summ = MySQLQuery.getAsInteger(summRow[1]);
                    if ((summ + 1) % salesAmount == 0) {
                        String prizePrice = (promos[i][3] != null ? "bono x " + df.format(MySQLQuery.getAsInteger(promos[i][3])) : (promos[i][4] != null ? "bono x " + df.format(sale.total.multiply((new BigDecimal(MySQLQuery.getAsInteger(promos[i][4]) / 100d)))) : null));
                        if (prizePrice == null) {
                            prizePrice = new MySQLQuery("SELECT prize FROM com_promo_prize WHERE id = " + MySQLQuery.getAsInteger(promos[i][5])).getAsString(conn);
                        }

                        String cleanPhone = phone != null ? cleanPhone(phone) : null;
                        String promoCode = new MySQLQuery("SELECT HEX(RAND() * 1000000000000)").getAsString(conn);
                        String code = null;
                        if (cleanPhone != null) {
                            msg = "Ud es ganador de " + prizePrice + ", acerquese a oficina de atencion al cliente Montagas con su cedula y el codigo " + promoCode + " visite www.montagas.com.co";
                            code = new ClaroSmsSender().sendMsg(msg, "1", cleanPhone);
                        }
                        new MySQLQuery("INSERT INTO com_promo_claim SET est_sale_id = " + sale.id + ", promo_id = " + promoId + ", claim_code = '" + promoCode + "', send_sms_dt = NOW(), resp_sms_code = '" + (code != null ? code : "Imposible enviar mensaje al tlf: " + phone) + "'").executeInsert(conn);
                        String clie = new MySQLQuery("SELECT name FROM ord_tank_client WHERE id = " + sale.clientId).getAsString(conn);

                        if (sale.execId != null) {
                            try {
                                String execMsg = clie + " es el ganador de " + (prizePrice.matches("[A-Za-z' ']+") ? prizePrice : "bono por " + prizePrice);

                                JsonObjectBuilder ob1 = Json.createObjectBuilder();
                                ob1.add("type", "winnerPromo");
                                ob1.add("subject", clie);
                                ob1.add("brief", "Cliente Ganador");
                                ob1.add("message", execMsg);
                                ob1.add("user", "Montagas");
                                ob1.add("dt", Dates.getCheckFormat().format(new Date()));

                                Integer empId = new MySQLQuery("SELECT id FROM employee WHERE active AND per_employee_id = " + sale.execId).getAsInteger(conn);
                                if (empId != null) {
                                    GCMUtils.sendToAppManagers(ob1.build(), String.valueOf(empId), conn);
                                }
                            } catch (Exception e) {
                                Logger.getLogger(EstSaleServlet.class.getName()).log(Level.SEVERE, null, e);
                            }
                        } else {
                            System.out.println("No existe ejecutivo de cuenta asociado al cliente o a su zona");
                        }
                    }
                    new MySQLQuery("UPDATE com_promo_count SET cnt = cnt + 1 WHERE id = " + MySQLQuery.getAsInteger(summRow[0])).executeUpdate(conn);
                } else {
                    new MySQLQuery("INSERT INTO com_promo_count "
                            + "SET promo_id = " + promoId + ", "
                            + "cnt = 1, "
                            + "zone_id = " + (zoneId == null ? "NULL" : zoneId) + ", "
                            + "city_id = " + (cityId == null ? "NULL" : cityId) + ", "
                            + "sector_id = " + (sectorId == null ? "NULL" : sectorId) + ", "
                            + "tk_client_id = " + (tkClieId == null ? "NULL" : tkClieId) + ", "
                            + "tk_cat_type = " + (tkTypeId == null ? "NULL" : tkTypeId) + ", "
                            + "tk_category = " + (categId == null ? "NULL" : categId)).executeInsert(conn);
                }

                break;
            }
        }
        return msg;
    }

    private String cleanPhone(String phone) {
        String clnPhone = null;
        if (phone.contains("-")) {
            String[] str = phone.split("-");
            for (int i = 0; i < str.length; i++) {
                if (str[i].startsWith("3")) {
                    clnPhone = str[i];
                    break;
                }
            }
        } else if (phone.startsWith("3")) {
            clnPhone = phone;
        }
        return clnPhone;
    }

    private boolean applToPromo(Applicant app) {
        return (app.applZone == null || app.applZone)
                && (app.applCity == null || app.applCity)
                && (app.applSector == null || app.applSector)
                && (app.applTkCategory == null || app.applTkCategory)
                && (app.applTkClient == null || app.applTkClient)
                && (app.applTkCatType == null || app.applTkCatType);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;
        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            JsonObjectBuilder ob = Json.createObjectBuilder();
            JsonObject req = MySQLQuery.scapeJsonObj(request);
            String poolName = req.getString("poolName");
            conn = MySQLCommon.getConnection(poolName, null);
            conn.setAutoCommit(true);
            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");

            try {
                if (req.containsKey("sync")) {
                    JsonArrayBuilder jab = Json.createArrayBuilder();
                    JsonArray jar = req.getJsonArray("sales");
                    for (int i = 0; i < jar.size(); i++) {
                        JsonObject job = jar.getJsonObject(i);
                        int id = proccessSale(job, i, conn);
                        JsonObjectBuilder builder = Json.createObjectBuilder();
                        builder.add("movId" + i, job.getInt("movId" + i));
                        builder.add("insId" + i, id);
                        jab.add(builder.build());
                    }
                    ob.add("insIds", jab);
                } else {
                    int id = proccessSale(req, null, conn);
                    int movId = req.getInt("movId");
                    ob.add("movId", movId);
                    ob.add("insId", id);
                }
                ob.add("result", "OK");
            } catch (Exception ex) {
                if (ex instanceof ShortException) {
                    ((ShortException) ex).simplePrint();
                } else {
                    Logger.getLogger(EstSaleServlet.class.getName()).log(Level.SEVERE, null, ex);
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
        } catch (Exception ex) {
            Logger.getLogger(EstSaleServlet.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            MySQLSelect.tryClose(conn);
        }
    }

    private int proccessSale(JsonObject req, Integer index, Connection conn) throws Exception {
        String pos = index != null ? index + "" : "";
        EstSale sale = new EstSale();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = req.getString("saleDate" + pos);
        sale.movId = req.getInt("movId" + pos);
        sale.saleDate = sdf.parse(date);
        sale.createdDate = sale.modifiedDate = new Date();
        sale.clientId = req.getInt("clientId" + pos);
        sale.billNum = req.getString("billNum" + pos);
        sale.kgs = MySQLQuery.getAsBigDecimal(req.get("kgs" + pos).toString(), false);
        sale.gls = MySQLQuery.getAsBigDecimal(req.get("gls" + pos).toString(), false);
        sale.total = MySQLQuery.getAsBigDecimal(req.get("vlrTotal" + pos).toString(), false);
        sale.unitPrice = MySQLQuery.getAsBigDecimal(req.get("unitPrice" + pos).toString(), false);
        sale.unitKgPrice = MySQLQuery.getAsBigDecimal(req.get("unitPriceKg" + pos).toString(), false);
        sale.kte = MySQLQuery.getAsBigDecimal(req.get("kte" + pos).toString(), false);
        sale.estTankId = req.getInt("tankId" + pos);
        sale.vhId = req.getInt("vehicleId" + pos);
        sale.createdId = sale.modifiedId = req.getInt("employeeId" + pos);
        sale.driverId = req.getInt("employeeId" + pos);
        sale.billType = req.getString("billType" + pos);
        sale.isCredit = req.getBoolean("isCredit" + pos);
        sale.notes = req.getString("notes" + pos);
        sale.tripId = req.getInt("trip_id" + pos);
        sale.source = "mob";
        sale.cancel = req.containsKey("cancel" + pos) ? req.getInt("cancel" + pos) == 1 : false;
        sale.lastImport = false;
        sale.lat = req.containsKey("lat" + pos) ? MySQLQuery.getAsBigDecimal(req.get("lat" + pos).toString(), false) : null;
        sale.lon = req.containsKey("lon" + pos) ? MySQLQuery.getAsBigDecimal(req.get("lon" + pos).toString(), false) : null;
        sale.percBegin = req.containsKey("percBegin" + pos) ? MySQLQuery.getAsInteger(req.get("percBegin" + pos).toString()) : null;
        sale.percEnd = req.containsKey("percEnd" + pos) ? MySQLQuery.getAsInteger(req.get("percEnd" + pos).toString()) : null;      
        if (req.containsKey("saleClose" + pos)) {
            sale.saleClose = sdf.parse(req.getString("saleClose" + pos));
        }

        sale.id = new MySQLQuery("SELECT id FROM est_sale "
                + "WHERE bill_type = '" + sale.billType + "' "
                + "AND sale_date = '" + sdf.format(sale.saleDate) + "' "
                + "AND est_tank_id = " + sale.estTankId + " "
                + "AND gls = " + sale.gls + " "
                + "AND kgs = " + sale.kgs).getAsInteger(conn);

        if (sale.id == null) {
            String phone = req.containsKey("phone" + pos) ? req.getString("phone" + pos) : null;
            if (phone != null) {
                new MySQLQuery("UPDATE ord_tank_client SET phones = '" + phone + "' WHERE id = " + sale.clientId).executeUpdate(conn);
            }

            if (req.containsKey("contactName" + pos)) {
                new MySQLQuery("UPDATE ord_tank_client SET contact_name = '" + req.getString("contactName" + pos)
                        + "', contact_phone = '" + req.getString("contactPhone" + pos)
                        + "', contact_mail = '" + req.getString("contactMail" + pos).trim() + "' "
                        + "WHERE id = " + sale.clientId).executeUpdate(conn);

                Integer crmClientId = new MySQLQuery("SELECT c.id"
                        + " FROM crm_client c"
                        + " INNER JOIN est_prospect p ON c.prospect_id = p.id"
                        + " INNER JOIN ord_tank_client t ON t.id = p.client_id"
                        + " WHERE t.id = " + sale.clientId + "").getAsInteger(conn);

                if (crmClientId != null) {
                    new MySQLQuery("UPDATE crm_client SET "
                            + "main_contact = '" + req.getString("contactName" + pos)
                            + "', phone = '" + req.getString("contactPhone" + pos)
                            + "', email = '" + req.getString("contactMail" + pos).trim() + "' "
                            + "WHERE id = " + crmClientId).executeUpdate(conn);
                }
            }

            sale.execId = new MySQLQuery("SELECT exec_reg_id FROM ord_tank_client WHERE id = " + sale.clientId).getAsInteger(conn);
            if (sale.execId == null) {
                Object[] clieInfo = new MySQLQuery("SELECT "
                        + "c.zone_id, "//0
                        + "c.id, "//1
                        + "s.id, "//2
                        + "n.id "//3
                        + "FROM ord_tank_client tc "
                        + "INNER JOIN neigh n ON tc.neigh_id = n.id "
                        + "INNER JOIN sector s ON n.sector_id = s.id "
                        + "INNER JOIN city c ON s.city_id = c.id "
                        + "WHERE tc.id = " + sale.clientId).getRecord(conn);

                sale.execId = new MySQLQuery("SELECT per_emp_id FROM est_exec_reg WHERE neigh_id = " + MySQLQuery.getAsInteger(clieInfo[3])).getAsInteger(conn);
                if (sale.execId == null) {
                    sale.execId = new MySQLQuery("SELECT per_emp_id FROM est_exec_reg WHERE sector_id = " + MySQLQuery.getAsInteger(clieInfo[2])).getAsInteger(conn);
                }
                if (sale.execId == null) {
                    sale.execId = new MySQLQuery("SELECT per_emp_id FROM est_exec_reg WHERE city_id = " + MySQLQuery.getAsInteger(clieInfo[1])).getAsInteger(conn);
                }
                if (sale.execId == null) {
                    sale.execId = new MySQLQuery("SELECT per_emp_id FROM est_exec_reg WHERE zone_id = " + MySQLQuery.getAsInteger(clieInfo[0])).getAsInteger(conn);
                }
            }

            //ultimo cargue con calidad
            Integer idQuality=new MySQLQuery("SELECT id_quality " 
                + "FROM est_sale " 
                + "where client_id = "+sale.clientId 
                + " order by created_date  desc " 
                + "limit 1").getAsInteger(conn);
            //primer cargue
            boolean firstTime=false;
            if (idQuality==null){
                firstTime=true;
            }
            
            sale.id = sale.insert(sale, conn);
            //consulta items de calidad desde co con fecha actual
            System.out.println("Cual es el id de trip actual "+sale.tripId);
            Object[] quality= new MySQLQuery("SELECT "
                    + "q.c_3_propano, "
                    + "q.c_4_butano, "
                    + "q.c_5_olefinas, "
                    + "q.agua "
                    + "FROM gt_glp_trip t "
                    + "INNER JOIN gt_glp_inv i on i.trip_id = t.id "
                    + "INNER JOIN gt_glp_inv_quality q on q.id = i.id_quality "
                    + "WHERE t.id =" +sale.tripId+" AND i.`type` = 'c'").getRecord(conn);
            
            EstSaleQuality qSale=new EstSaleQuality();
            /*for(int i=0; i<=quality.length-1; i++){
                System.out.println("# "+i+" tenemos "+quality[i]);
            }*/
            
            //Si es primer cargue
            if (firstTime){
                //si existen items de calidad agregar en la tabla est_sale 
                if (quality!=null){
                    qSale.c3Propano=MySQLQuery.getAsBigDecimal(quality[0],false);
                    qSale.c4Butano=MySQLQuery.getAsBigDecimal(quality[1],false);
                    qSale.c5Olefinas=MySQLQuery.getAsBigDecimal(quality[2],false);
                    qSale.agua=MySQLQuery.getAsBigDecimal(quality[3],false);
                    qSale.insert(qSale, conn);

                    new MySQLQuery("UPDATE est_sale " 
                        + "SET id_quality= "+qSale.id 
                        + " WHERE id= "+ sale.id).executeUpdate(conn);
                }
            }
            else{
                if(quality!=null){
                    Object[] prevQuality= new MySQLQuery("SELECT "
                            + "q.c_3_propano, "
                            + "q.c_4_butano, "
                            + "q.c_5_olefinas, "
                            + "q.agua, " 
                            + "e.capacity "
                            + "FROM est_sale s "
                            + "inner join est_sale_quality q on q.id = s.id_quality " 
                            + "inner join est_tank e on e.id = s.est_tank_id "
                            + "WHERE q.id = "+idQuality).getRecord(conn);
                    if (prevQuality!=null){
                        
                        BigDecimal porcInicial=BigDecimal.valueOf(sale.percBegin);
                        BigDecimal capFull=MySQLQuery.getAsBigDecimal(prevQuality[4],true);
                        System.out.println("Rotogage actual debe ser "+sale.percBegin);
                        BigDecimal glsv;
                        glsv=darGalones(porcInicial, capFull);
                        //mezcla
                        for (int i=0; i<prevQuality.length-1; i++){
                            System.out.println("envio # Envio "+i+" galonesN "+sale.gls+" galonesV "+glsv+" porcentajeN "+MySQLQuery.getAsBigDecimal(quality[i],true)+" porcentajeV "+MySQLQuery.getAsBigDecimal(prevQuality[i],true));
                            BigDecimal resultado=darMezcla(sale.gls, glsv, MySQLQuery.getAsBigDecimal(quality[i],true),MySQLQuery.getAsBigDecimal(prevQuality[i],true));
                            if (i==0){
                                qSale.c3Propano=resultado;
                            }
                            else if (i==1){
                                qSale.c4Butano=resultado;
                            }
                            else if (i==2){
                                qSale.c5Olefinas=resultado;
                            }
                            else if (i==3){
                                qSale.agua=resultado;
                            }
                        }
                        System.out.println("c3 "+qSale.c3Propano+" c4 "+qSale.c4Butano+" c5 "+qSale.c5Olefinas+" agua "+qSale.agua);
                        qSale.insert(qSale, conn);
                        new MySQLQuery("UPDATE est_sale " 
                        + "SET id_quality= "+qSale.id 
                        + " WHERE id= "+ sale.id).executeUpdate(conn);
                    }
                }
            }

            if (req.containsKey("schedId" + pos)) {
                new MySQLQuery("UPDATE est_schedule SET visit_id = " + sale.id + ", emp_id = " + sale.createdId + " "
                        + "WHERE "
                        + "clie_tank_id = " + sale.clientId + " AND "
                        + "visit_date = '" + Dates.getSQLDateFormat().format(sale.saleDate) + "'").executeUpdate(conn);
            }
            if (req.containsKey("progId" + pos)) {
                Integer orderId = new MySQLQuery("SELECT ps.order_tank_id "
                        + "FROM est_prog_sede ps "
                        + "INNER JOIN est_prog p ON ps.prog_id = p.id "
                        + "WHERE "
                        + "p.vh_id = " + sale.vhId + " "
                        + "AND DATE('" + date + "') BETWEEN p.prog_date AND p.end_date "
                        + "AND ps.tank_client_id = " + sale.clientId).getAsInteger(conn);
                if (orderId != null) {
                    int driverId = req.getInt("empId" + pos);
                    new MySQLQuery("UPDATE ord_tank_order SET "
                            + "confirm_dt = DATE('" + date + "'), "
                            + "confirm_hour = TIME('" + date + "'), "
                            + "confirmed_by_id = " + driverId + ", "
                            + "driver_id = " + driverId + ", "
                            + "vehicle_id = " + sale.vhId + " "
                            + "WHERE id = " + orderId + " "
                            + "AND confirmed_by_id IS NULL "
                            + "AND cancelled_by IS NULL").executeUpdate(conn);
                }
            }

            new MySQLQuery("UPDATE est_sede_nov SET num_visit = num_visit - 1 WHERE clie_tank_id = " + sale.clientId + " AND active").executeUpdate(conn);

            if (req.containsKey("salePayId" + pos)) {
                String salePayId = req.getString("salePayId" + pos).replaceAll("[^0-9,]+", "");
                if (!salePayId.isEmpty()) {
                    if (salePayId.contains(",")) {
                        String[] ids = salePayId.split(",");
                        for (String id : ids) {
                            new MySQLQuery("UPDATE est_sale SET sale_pay_id = " + sale.id + " WHERE id = " + id).executeUpdate(conn);
                        }
                    } else {
                        new MySQLQuery("UPDATE est_sale SET sale_pay_id = " + sale.id + " WHERE id = " + salePayId.replaceAll("'", "")).executeUpdate(conn);
                    }
                }
            }
            if (sale.billType.equals("rem")) {
                new MySQLQuery("UPDATE est_res_rem_hist SET used = 1 WHERE rem_num = " + sale.billNum + "").executeUpdate(conn);
            } else {
                new MySQLQuery("UPDATE est_res_bill_hist SET used = 1 WHERE bill_num = " + sale.billNum + "").executeUpdate(conn);
            }
            if (req.containsKey("negAnsIds" + pos)) {
                String ids[] = req.getString("negAnsIds" + pos).split(",");
                int neg_quest_id = new MySQLQuery("INSERT INTO com_app_neg_quest SET est_sale_id = " + sale.id).executeInsert(conn);
                for (String id : ids) {
                    new MySQLQuery("INSERT INTO com_app_answer SET neg_quest_id = " + neg_quest_id + ", question_id = " + id).executeInsert(conn);
                }
            }

            //Verificar si hay visitas el dia de la venta
            Integer visitId = new MySQLQuery("SELECT v.id FROM ord_tank_visit v WHERE "
                    + "v.visit_date = '" + Dates.getSQLDateFormat().format(sale.saleDate) + "' "
                    + "AND v.client_id = " + sale.clientId).getAsInteger(conn);
            if (visitId != null) {
                new MySQLQuery("UPDATE ord_tank_visit v SET v.done = 1, vehicle_id = " + sale.vhId + ", driver_id = (SELECT driver_id FROM driver_vehicle WHERE vehicle_id = " + sale.vhId + " AND end IS NULL ORDER BY id DESC LIMIT 1) WHERE v.id = " + visitId).executeUpdate(conn);
            }

            if (sale.lat != null && sale.lon != null) {
                new MySQLQuery("UPDATE ord_tank_client "
                        + "SET "
                        + "lat =  " + sale.lat + ", "
                        + "lon = " + sale.lon + " "
                        + "WHERE "
                        + "((lat IS NULL OR lon IS NULL) OR (lat = 0 OR lon = 0)) "
                        + "AND id = " + sale.clientId).executeUpdate(conn);
            }

            new MySQLQuery("UPDATE ord_tank_client, "
                    + "(SELECT * FROM (SELECT "
                    + "ord_tank_client.id AS cid , MIN(est_sale.created_date) as fecha "
                    + "FROM "
                    + "ord_tank_client "
                    + "INNER JOIN est_sale ON ord_tank_client.id = est_sale.client_id "
                    + "WHERE "
                    + "ord_tank_client.begin_date IS NULL  AND "
                    + "ord_tank_client.id=" + sale.clientId + " "
                    + "GROUP BY "
                    + "ord_tank_client.id)as l ) as l2 "
                    + "SET ord_tank_client.begin_date= l2.fecha "
                    + "WHERE ord_tank_client.id = l2.cid").executeUpdate(conn);

            new MySQLQuery("UPDATE est_tank AS t "
                    + "INNER JOIN est_tank_read AS r ON r.tank_id = t.id "
                    + "SET r.sales = 1 "
                    + "WHERE "
                    + "t.id = " + sale.estTankId + " "
                    + "AND t.client_id = " + sale.clientId).executeUpdate(conn);

            String prize = null;
            if (!sale.isCredit && sale.billType.equals("fac") && index == null) {
                prize = isPromoApplicant(sale, phone, conn);
            }

            String mail = null;
            if (new MySQLQuery("SELECT mail_sale_tk FROM sys_cfg").getAsBoolean(conn)) {
                mail = new MySQLQuery("SELECT contact_mail FROM ord_tank_client WHERE id = " + sale.clientId).getAsString(conn);
            }
            if (mail != null) {
                StringBuilder sb = new StringBuilder();
                String ls = System.lineSeparator();
                sb.append("Información de Venta:").append(System.lineSeparator())
                        .append("NOMBRE DEL CLIENTE: ").append(new MySQLQuery("SELECT name FROM ord_tank_client WHERE id = " + sale.clientId).getAsString(conn)).append(ls)
                        //.append("ASIGNACIÓN: ").append(new MySQLQuery("SELECT IFNULL(et.name, '') FROM ord_tank_client as oc LEFT JOIN est_price_type as et ON oc.price_type_id = et.id WHERE oc.id =  " + sale.clientId).getAsString(conn)).append(ls)
                        .append("KILOS ENTREGADOS: ").append(sale.kgs).append(ls).append(ls)
                        .append("GRACIAS POR SU COMPRA ").append(ls)
                        .append("PEDIDOS AL #876 ").append(ls)
                        .append("EJECUTIVO DE CUENTA: ").append(new MySQLQuery("SELECT "
                        + "IF(otc1.is_exec_man, CONCAT(pe.id, ' - ', pe.first_name, ' ', pe.last_name), (SELECT "
                        + "CONCAT(pe.id, ' - ', pe.first_name, ' ', pe.last_name) "
                        + "FROM ord_tank_client otc "
                        + "INNER JOIN neigh n ON otc.neigh_id = n.id "
                        + "INNER JOIN sector s ON n.sector_id = s.id "
                        + "INNER JOIN city c ON s.city_id = c.id "
                        + "INNER JOIN zone z ON c.zone_id = z.id "
                        + "LEFT JOIN est_exec_reg exn ON exn.neigh_id = otc.neigh_id AND exn.active "
                        + "LEFT JOIN est_exec_reg exs ON exs.sector_id = s.id AND exs.active "
                        + "LEFT JOIN est_exec_reg exc ON exc.city_id = c.id AND exc.active "
                        + "LEFT JOIN est_exec_reg exz ON exz.zone_id = z.id AND exz.active "
                        + "LEFT JOIN per_employee pe ON pe.id = COALESCE(exn.per_emp_id, exs.per_emp_id, exc.per_emp_id, exz.per_emp_id) "
                        + "WHERE otc.id = otc1.id)) "
                        + "FROM ord_tank_client otc1 "
                        + "LEFT JOIN per_employee pe ON otc1.exec_reg_id = pe.id "
                        + "WHERE otc1.id = " + sale.clientId).getAsString(conn)).append(ls)
                        .append("MOVIL: ").append(new MySQLQuery("SELECT "
                        + "IF(otc1.is_exec_man, COALESCE(pe.cel_phones, ''), (SELECT "
                        + "COALESCE(pe.cel_phones, '') "
                        + "FROM ord_tank_client otc "
                        + "INNER JOIN neigh n ON otc.neigh_id = n.id "
                        + "INNER JOIN sector s ON n.sector_id = s.id "
                        + "INNER JOIN city c ON s.city_id = c.id "
                        + "INNER JOIN zone z ON c.zone_id = z.id "
                        + "LEFT JOIN est_exec_reg exn ON exn.neigh_id = otc.neigh_id AND exn.active "
                        + "LEFT JOIN est_exec_reg exs ON exs.sector_id = s.id AND exs.active "
                        + "LEFT JOIN est_exec_reg exc ON exc.city_id = c.id AND exc.active "
                        + "LEFT JOIN est_exec_reg exz ON exz.zone_id = z.id AND exz.active "
                        + "LEFT JOIN per_employee pe ON pe.id = COALESCE(exn.per_emp_id, exs.per_emp_id, exc.per_emp_id, exz.per_emp_id) "
                        + "WHERE otc.id = otc1.id)) "
                        + "FROM ord_tank_client otc1 "
                        + "LEFT JOIN per_employee pe ON otc1.exec_reg_id = pe.id "
                        + "WHERE otc1.id = " + sale.clientId).getAsString(conn)).append(ls);

                if (prize != null) {
                    sb.append(prize);
                }

                String plain = sb.toString();
                plain = Matcher.quoteReplacement(plain);
                sendMailThread(mail, plain, conn);
            }
        }

        return sale.id;
    }
    
    public BigDecimal darMezcla(BigDecimal vol1, BigDecimal vol2, BigDecimal grad1, BigDecimal grad2){
        BigDecimal mezcla=BigDecimal.ZERO;
        BigDecimal numerador;
        BigDecimal denominador;
        numerador=(vol1.multiply(grad1)).add(vol2.multiply(grad2));
        denominador=(vol1.add(vol2));
        if (denominador.compareTo(BigDecimal.ZERO)>0){
            mezcla=numerador.divide(denominador,2,RoundingMode.HALF_UP);
                    }
        System.out.println("resultado mezcla en metodo "+mezcla);
        return mezcla;
    }
    public BigDecimal darGalones(BigDecimal porcenInicial, BigDecimal capFull){
        //System.out.println("%INICIAL "+porcenInicial+" full "+capFull);
        BigDecimal galones=BigDecimal.ZERO;
        BigDecimal porciento=new BigDecimal(100);
        if (porcenInicial!=null && capFull!=null){
            galones=(porcenInicial.multiply(capFull).divide(porciento));
        }
        return galones;
    }

    private void sendMailThread(final String mails, final String plain, Connection conn) throws Exception {
        Object[] cfg = new MySQLQuery("SELECT mail_alert_logo_url, mail_sale_tk FROM sys_cfg").getRecord(conn);
        if (MySQLQuery.getAsBoolean(cfg[1])) {
            String logo = (cfg[0] != null ? MySQLQuery.getAsString(cfg[0]) : "http://qualisys.com.co/ent_logos/qualisys_new.png");

            String html = SendMail.readTemplate("/web/template.html");
            html = html.replaceAll("\\{headerTitle\\}", "");
            html = html.replaceAll("\\{titleAlerts\\}", "Venta Estacionario");
            html = html.replaceAll("\\{ent_logo\\}", logo);
            html = html.replaceAll("[\r\n\t]", "");
            html = html.replaceAll("\\{rows\\}", plain);
            final String fhtml = html;
            final MailCfg mailCfg = new MailCfg().select(conn);

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        SendMail.sendMail(mailCfg, mails, "Venta Estacionario", fhtml, plain);
                    } catch (Exception ex) {
                        Logger.getLogger(EstSaleServlet.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            t.start();
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
        return "Est Info";
    }

    public class Applicant {

        public Boolean applZone;
        public Boolean applCity;
        public Boolean applSector;
        public Boolean applTkClient;
        public Boolean applTkCatType;
        public Boolean applTkCategory;

    }

}
