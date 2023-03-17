package web.marketing.pvs;

import java.io.IOException;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.zip.GZIPOutputStream;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import utilities.SysTask;

@WebServlet(name = "SyncPvs", urlPatterns = {"/SyncPvs"})
public class SyncPvs extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        Connection conn = null;
        SysTask t = null;
        try {
            conn = MySQLCommon.getConnection("sigmads", null);
            Map<String, String> req = MySQLQuery.scapedParams(request);
            int empId = MySQLQuery.getAsInteger(req.get("empId"));
            t = new SysTask(SyncPvs.class, empId, conn);
            Boolean unifiedCenter = new MySQLQuery("SELECT unified_center FROM inv_cfg WHERE id = 1").getAsBoolean(conn);

            if (unifiedCenter == null) {
                throw new Exception("No se ha configurado el modulo de cilindros");
            }

            Object[] cityRow;
            Object[][] cylTypes = new MySQLQuery("SELECT id, `name` FROM cylinder_type").getRecords(conn);
            Object[][] storeData;
            Object[][] indexData;
            String cities;
            String munCodes = "";
            String centersCode = "";

            if (unifiedCenter) {
                Integer salesmanId = new MySQLQuery("SELECT IFNULL(drv.id, IFNULL(dis.id, IFNULL(sto.id, ctr.id))) "
                        + "FROM employee e "
                        + "LEFT JOIN dto_salesman drv ON drv.driver_id = e.id AND drv.active "
                        + "LEFT JOIN dto_salesman dis ON dis.distributor_id = e.id AND dis.active "
                        + "LEFT JOIN dto_salesman sto ON sto.store_id = e.store_id AND sto.active "
                        + "LEFT JOIN dto_salesman ctr ON ctr.contractor_id = e.contractor_id AND ctr.active "
                        + "WHERE e.id = " + empId).getAsInteger(conn);

                if (salesmanId == null) {
                    throw new Exception("Usuario no registrado como vendedor");
                }
                //centros de subsidios
                centersCode = new MySQLQuery("SELECT GROUP_CONCAT(\"\'\",code,\"\'\") FROM "
                        + "(SELECT DISTINCT UPPER(sc.code) AS code "
                        + "FROM dto_salesman d "
                        + "INNER JOIN dto_center c ON c.id = d.center_id "
                        + "INNER JOIN sys_center sc ON sc.id = c.sys_center_id "
                        + "WHERE d.id = " + salesmanId + ")AS tbl ").getAsString(conn);

                if (centersCode == null || centersCode.isEmpty()) {
                    throw new Exception("El empleado no esta asignado a ningun centro.");
                }

                //TODO preguntar si se neceitara city row
                cityRow = new MySQLQuery("SELECT GROUP_CONCAT(c.id) "
                        + "FROM city c "
                        + "INNER JOIN sys_center_city cc ON cc.city_id = c.id "
                        + "INNER JOIN sys_center sc ON sc.id = cc.sys_center_id "
                        + "AND sc.code IN (" + centersCode + ")").getRecord(conn);

                if (cityRow == null) {
                    throw new Exception("No se encontro ciudades para los centros: " + centersCode.replace("\'", ""));
                }

                cities = MySQLQuery.getAsString(cityRow[0]);

                storeData = new MySQLQuery("SELECT s.id, COALESCE(s.internal, ' '), COALESCE(s.document, ' '), COALESCE(s.address, ' '), COALESCE(s.phones, ' ') "
                        + "FROM inv_store s "
                        + "INNER JOIN inv_store_state st ON s.state_id = st.id AND st.type <> 'clo' "
                        + "INNER JOIN sys_center sc ON sc.inv_center_id = s.center_id AND  sc.code IN (" + centersCode + ") "
                        + "WHERE s.active").getRecords(conn);

                indexData = new MySQLQuery("SELECT i.id, "
                        + "COALESCE(c.document, ' '), COALESCE(c.address, ' '), COALESCE(c.phones, ' ') "
                        + "FROM ord_contract_index i "
                        + "INNER JOIN contract c ON c.id = i.contract_id "
                        + "WHERE i.`type` = 'brand' AND "
                        + "i.city_id IN (" + cities + ") AND i.active "
                        + "GROUP BY i.document ").getRecords(conn);
            } else {
                String centersId = new MySQLQuery("SELECT GROUP_CONCAT(center_id) FROM inv_emp_center WHERE employee_id = " + empId).getAsString(conn);

                if (centersId == null) {
                    throw new Exception("El empleado no esta asignado a ningun centro.");
                }

                cityRow = new MySQLQuery("SELECT GROUP_CONCAT(c.id), GROUP_CONCAT(mun_id) FROM city c INNER JOIN inv_city_center cc ON cc.city_id = c.id WHERE cc.center_id IN (" + centersId + ")").getRecord(conn);
                cities = MySQLQuery.getAsString(cityRow[0]);
                munCodes = MySQLQuery.getAsString(cityRow[1]);

                storeData = new MySQLQuery("SELECT s.id, "
                        + "COALESCE(s.internal, ' '), COALESCE(s.document, ' '), COALESCE(s.address, ' '), COALESCE(s.phones, ' ') "
                        + "FROM inv_store s "
                        + "INNER JOIN inv_store_state st ON s.state_id = st.id "
                        + "WHERE s.center_id IN (" + centersId + ") AND s.active AND st.type <> 'clo' ").getRecords(conn);

                indexData = new MySQLQuery("SELECT id, "
                        + "COALESCE(document, ' '), COALESCE(address, ' '), COALESCE(phones, ' ') "
                        + "FROM ord_contract_index "
                        + "WHERE city_id IN (" + cities + ") AND active GROUP BY document ").getRecords(conn);
            }

            List<pricesData> stDataList = new ArrayList<>();
            List<DebtData> debtDataList = new ArrayList<>();

//            if (storeData.length == 0) {
//                throw new Exception("No hay expendios para el centro.");
//            }
            //-----------------------------------------------------------------------
            //STORE HASMAP
            HashMap<String, pricesData> storeMap = new HashMap<>();
            for (Object[] storeRow : storeData) {
                if (storeRow != null) {
                    pricesData aux = new pricesData();
                    aux.storeId = MySQLQuery.getAsInteger(storeRow[0]);
                    aux.internal = MySQLQuery.getAsString(storeRow[1]);
                    aux.document = MySQLQuery.getAsString(storeRow[2]);
                    aux.address = MySQLQuery.getAsString(storeRow[3]);
                    aux.phones = MySQLQuery.getAsString(storeRow[4]);
                    storeMap.put(aux.internal, aux);
                }
            }
            //-----------------------------------------------------------------------
            //INDEX HASMAP
            HashMap<String, pricesData> indexMap = new HashMap<>();
            for (Object[] indexRow : indexData) {
                if (indexRow != null) {
                    pricesData aux = new pricesData();
                    aux.indexId = MySQLQuery.getAsInteger(indexRow[0]);
                    aux.document = MySQLQuery.getAsString(indexRow[1]);
                    aux.address = MySQLQuery.getAsString(indexRow[2]);
                    aux.phones = MySQLQuery.getAsString(indexRow[3]);
                    indexMap.put(aux.document, aux);
                }
            }
            //-----------------------------------------------------------------------
            for (Object[] cylType : cylTypes) {
                String cylName = MySQLQuery.getAsString(cylType[1]);
                Object[][] thirdData = new MySQLQuery("SELECT DISTINCT "
                        + "lp.precio_min_1, "//0
                        + "dd.vlrs_des1, "//1
                        + "IF(t.ind_liq_impto_c = 1, lp.impoconsumo1_1, 0), "//2
                        + "t.sucursal, "//3
                        + "lp.bin_id, "//4
                        + "dd.bin_id, "//5
                        + "t.cli_forma_pago, "//6
                        + "t.cli_ind_blo_cupo, "//7
                        + "t.codigo "//8
                        + "FROM bbl_terceros t "
                        + "INNER JOIN bbl_price_list lp ON lp.bin_id = t.cli_lipre_bin AND lp.active AND lp.id_referencia = '" + cylName + "' "
                        + "LEFT JOIN bbl_discount_list dd ON dd.bin_id = t.cli_lides_bin AND dd.active AND dd.id_referencia = '" + cylName + "' "
                        + (unifiedCenter ? "WHERE t.cli_co IN (" + centersCode + ") " : " ")).getRecords(conn);

                if (thirdData != null && thirdData.length > 0) {
                    for (Object[] obj : thirdData) {
                        if (obj != null) {
                            String codigoId = MySQLQuery.getAsString(obj[8]);

                            if (storeMap.containsKey(codigoId)) {//buscar en stores
                                pricesData stDt = new pricesData();

                                stDt.storeId = storeMap.get(codigoId).storeId;
                                stDt.internal = storeMap.get(codigoId).internal;
                                stDt.document = storeMap.get(codigoId).document;
                                stDt.address = storeMap.get(codigoId).address;
                                stDt.phones = storeMap.get(codigoId).phones;

                                stDt.cylName = cylName;
                                stDt.price = (MySQLQuery.getAsDouble(obj[0]) - (obj[1] != null ? MySQLQuery.getAsDouble(obj[1]) : 0)) + MySQLQuery.getAsDouble(obj[2]);
                                stDt.sucursal = MySQLQuery.getAsString(obj[3]);
                                stDt.priceListId = MySQLQuery.getAsString(obj[4]);
                                stDt.discListId = MySQLQuery.getAsString(obj[5]);
                                stDt.payToCredit = MySQLQuery.getAsBoolean(obj[6]);
                                stDt.blockByQuota = MySQLQuery.getAsBoolean(obj[7]);
                                stDataList.add(stDt);
                            } else if (indexMap.containsKey(codigoId)) {//buscar en index
                                pricesData stDt = new pricesData();

                                stDt.indexId = indexMap.get(codigoId).indexId;
                                stDt.document = indexMap.get(codigoId).document;
                                stDt.address = indexMap.get(codigoId).address;
                                stDt.phones = indexMap.get(codigoId).phones;

                                stDt.cylName = cylName;
                                stDt.price = (MySQLQuery.getAsDouble(obj[0]) - (obj[1] != null ? MySQLQuery.getAsDouble(obj[1]) : 0)) + MySQLQuery.getAsDouble(obj[2]);
                                stDt.sucursal = MySQLQuery.getAsString(obj[3]);
                                stDt.priceListId = MySQLQuery.getAsString(obj[4]);
                                stDt.discListId = MySQLQuery.getAsString(obj[5]);
                                stDt.payToCredit = MySQLQuery.getAsBoolean(obj[6]);
                                stDt.blockByQuota = MySQLQuery.getAsBoolean(obj[7]);
                                stDataList.add(stDt);
                            }
                        }
                    }
                }
            }

            //----- Calculo de saldos para stores
            Object[][] debtStr = new MySQLQuery("SELECT "
                    + "t.cli_cupo_cre, "//0
                    + "bd.saldos_tot_cartera, "//1
                    + "bd.id_suc, "//2
                    + "bd.id_terc "//3
                    + "FROM bbl_debt bd "
                    + "INNER JOIN bbl_terceros t on bd.id_terc = t.codigo "
                    + "AND bd.id_suc = t.sucursal AND t.cli_forma_pago = 1 "
                    + (unifiedCenter ? "AND t.cli_co IN (" + centersCode + ") " : " ")
                    + "WHERE bd.id_terc IS NOT NULL "
                    + "ORDER BY bd.id_terc DESC").getRecords(conn);

            HashMap<String, List<DebtData>> tmpDebtListStore = new HashMap<>();

            if (debtStr != null && debtStr.length > 0) {
                String lastCode = "-1";
                List<DebtData> ddList = new ArrayList<>();
                int limit = debtStr.length - 1;

                for (int i = 0; i < debtStr.length; i++) {
                    Object[] obj = debtStr[i];
                    if (obj != null) {
                        String code = MySQLQuery.getAsString(obj[3]);
                        DebtData dD = new DebtData();
                        dD.credAmount = MySQLQuery.getAsDouble(obj[0]);
                        dD.debt = MySQLQuery.getAsDouble(obj[1]);
                        dD.sucursal = MySQLQuery.getAsString(obj[2]);

                        if (i == 0) {
                            ddList.add(dD);
                            if (i == limit) {
                                tmpDebtListStore.put(code, ddList);
                            }
                        } else if (lastCode.equals(code)) {
                            ddList.add(dD);
                            if (i == limit) {
                                tmpDebtListStore.put(lastCode, ddList);
                            }
                        } else {
                            tmpDebtListStore.put(lastCode, ddList);
                            ddList = new ArrayList<>();
                            ddList.add(dD);
                            if (i == limit) {
                                tmpDebtListStore.put(code, ddList);
                            }
                        }
                        lastCode = code;
                    }
                }
            }

            for (Object[] storeRow : storeData) {
                Integer storeId = MySQLQuery.getAsInteger(storeRow[0]);
                String terceroId = MySQLQuery.getAsString(storeRow[1]);
                if (tmpDebtListStore.containsKey(terceroId)) {
                    List<DebtData> auxList = tmpDebtListStore.get(terceroId);
                    for (int i = 0; i < auxList.size(); i++) {
                        DebtData aux = auxList.get(i);
                        aux.storeId = storeId;
                        debtDataList.add(aux);
                    }
                }
            }

            //----- Calculo de saldos para contratos
            Object[][] debtCtr = new MySQLQuery("SELECT "
                    + "t.cli_d_gracia, "//0
                    + "t.cli_cupo_cre, "//1
                    + "bd.saldos_tot_cartera, "//2
                    + "bd.id_suc, "//3      
                    + "bd.id_terc "//4    
                    + "FROM bbl_debt bd "
                    + "INNER JOIN bbl_terceros t on bd.id_terc = t.codigo AND t.sucursal = bd.id_suc AND t.cli_forma_pago = 1 "
                    + "WHERE bd.id_terc IS NOT NULL "
                    + "ORDER BY bd.id_terc DESC").getRecords(conn);

            HashMap<String, List<DebtData>> tmpDebtListCtr = new HashMap<>();

            if (debtCtr != null && debtCtr.length > 0) {
                String lastCode = "-1";
                List<DebtData> ddList = new ArrayList<>();
                int limit = debtCtr.length - 1;

                for (int i = 0; i < debtCtr.length; i++) {
                    Object[] obj = debtCtr[i];
                    if (obj != null) {

                        String code = MySQLQuery.getAsString(obj[4]);

                        DebtData dD = new DebtData();
                        dD.credAmount = (obj[1] != null ? MySQLQuery.getAsDouble(obj[1]) : 0d);
                        dD.debt = (obj[2] != null ? MySQLQuery.getAsDouble(obj[2]) : 0d);
                        dD.sucursal = MySQLQuery.getAsString(obj[3]);

                        if (i == 0) {
                            ddList.add(dD);
                            if (i == limit) {
                                tmpDebtListCtr.put(code, ddList);
                            }
                        } else if (lastCode.equals(code)) {
                            ddList.add(dD);
                            if (i == limit) {
                                tmpDebtListCtr.put(lastCode, ddList);
                            }
                        } else {
                            tmpDebtListCtr.put(lastCode, ddList);
                            ddList = new ArrayList<>();
                            ddList.add(dD);
                            if (i == limit) {
                                tmpDebtListCtr.put(code, ddList);
                            }
                        }
                        lastCode = code;
                    }
                }
            }

            for (Object[] indexRow : indexData) {
                Integer indexId = MySQLQuery.getAsInteger(indexRow[0]);
                String terceroId = MySQLQuery.getAsString(indexRow[1]);
                if (tmpDebtListCtr.containsKey(terceroId)) {
                    List<DebtData> auxList = tmpDebtListCtr.get(terceroId);
                    for (int i = 0; i < auxList.size(); i++) {
                        DebtData aux = auxList.get(i);
                        aux.indexId = indexId;
                        debtDataList.add(aux);
                    }
                }
            }

            //----- Precio full para clientes ocasionales residenciales
            for (Object[] cylType : cylTypes) {
                Object[][] lpByCylByIndex = new MySQLQuery("SELECT DISTINCT "
                        + "t.codigo, "//0
                        + "lp.precio_min_1, "//1
                        + "dd.vlrs_des1, "//2
                        + "IF(t.ind_liq_impto_c = 1, lp.impoconsumo1_1, 0), "//3
                        + "lp.bin_id, "//4
                        + "dd.bin_id, "//5
                        + "t.cli_forma_pago, "//6
                        + "t.cli_ind_blo_cupo "//7
                        + "FROM bbl_terceros t "
                        + "INNER JOIN bbl_price_list lp ON lp.bin_id = t.cli_lipre_bin AND lp.active AND lp.id_referencia = '" + MySQLQuery.getAsString(cylType[1]) + "' "
                        + "LEFT JOIN bbl_discount_list dd ON dd.bin_id = t.cli_lides_bin AND dd.active AND dd.id_referencia = '" + MySQLQuery.getAsString(cylType[1]) + "' "
                        + "WHERE t.codigo LIKE 'DOM%' "
                        + (unifiedCenter ? " " : "AND t.ciudad_corresp IN (" + munCodes + ") ")
                        + (unifiedCenter ? "AND t.cli_co IN (" + centersCode + ") " : " ")).getRecords(conn);

                if (lpByCylByIndex != null && lpByCylByIndex.length > 0) {
                    for (int i = 0; i < lpByCylByIndex.length; i++) {
                        pricesData stDt = new pricesData();
                        stDt.indexId = 0;
                        stDt.document = MySQLQuery.getAsString(lpByCylByIndex[i][0]);
                        stDt.address = "";
                        stDt.phones = "";
                        stDt.cylName = MySQLQuery.getAsString(cylType[1]);
                        stDt.price = (MySQLQuery.getAsDouble(lpByCylByIndex[i][1]) - (lpByCylByIndex[i][2] != null ? MySQLQuery.getAsDouble(lpByCylByIndex[i][2]) : 0)) + MySQLQuery.getAsDouble(lpByCylByIndex[i][3]);
                        stDt.sucursal = "00";
                        stDt.priceListId = MySQLQuery.getAsString(lpByCylByIndex[i][4]);
                        stDt.discListId = MySQLQuery.getAsString(lpByCylByIndex[i][5]);
                        stDt.payToCredit = MySQLQuery.getAsBoolean(lpByCylByIndex[i][6]);
                        stDt.blockByQuota = MySQLQuery.getAsBoolean(lpByCylByIndex[i][7]);
                        stDataList.add(stDt);
                    }
                }
            }

            response.setContentType("application/octet-stream");
            try (GZIPOutputStream goz = new GZIPOutputStream(response.getOutputStream()); OutputStreamWriter osw = new OutputStreamWriter(goz, "UTF8"); PrintWriter w = new PrintWriter(osw, true)) {
                w.write(String.valueOf(stDataList.size()));
                w.write(13);
                w.write(String.valueOf(debtDataList.size()));
                for (int i = 0; i < stDataList.size(); i++) {
                    pricesData item = stDataList.get(i);
                    w.write(13);
                    w.write(item.storeId != null ? String.valueOf(item.storeId) : "0");
                    w.write(9);
                    w.write(item.indexId != null ? String.valueOf(item.indexId) : "0");
                    w.write(9);
                    w.write(item.cylName);
                    w.write(9);
                    w.write(String.valueOf(item.price));
                    w.write(9);
                    w.write(item.document);
                    w.write(9);
                    w.write(item.internal != null ? item.internal : "0");
                    w.write(9);
                    w.write(item.address);
                    w.write(9);
                    w.write(item.phones);
                    w.write(9);
                    w.write(item.sucursal);
                    w.write(9);
                    w.write(item.priceListId != null ? item.priceListId : "null");
                    w.write(9);
                    w.write(item.discListId != null ? item.discListId : "null");
                    w.write(9);
                    w.write(item.payToCredit ? "1" : "0");
                    w.write(9);
                    w.write(item.blockByQuota ? "1" : "0");
                }
                for (int i = 0; i < debtDataList.size(); i++) {
                    DebtData item = debtDataList.get(i);
                    w.write(13);
                    w.write(item.storeId != null ? String.valueOf(item.storeId) : "0");
                    w.write(9);
                    w.write(item.indexId != null ? String.valueOf(item.indexId) : "0");
                    w.write(9);
                    w.write(String.valueOf(item.debt));
                    w.write(9);
                    w.write(String.valueOf(item.credAmount));
                    w.write(9);
                    w.write(item.sucursal);
                }
            } catch (Exception e) {
                Logger.getLogger(SyncPvs.class.getName()).log(Level.SEVERE, null, e);
                throw new Exception(e);
            }
            t.success(conn);
        } catch (Exception ex) {
            if (t != null && conn != null) {
                try {
                    t.error(ex, conn);
                } catch (Exception ex1) {
                    Logger.getLogger(SyncPvs.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
            Logger.getLogger(SyncPvs.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(500, ex.getMessage());
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
        return "";

    }

    private class pricesData {

        public Integer storeId;
        public Integer indexId;
        public String cylName;
        public Double price;
        public String internal;
        public String document;
        public String address;
        public String phones;
        public String sucursal;
        public String priceListId;
        public String discListId;
        public boolean payToCredit;//1 credito , 0 contado
        public boolean blockByQuota;//1 bloquea , 0 no bloquea
    }

    private class DebtData {

        public Integer storeId;
        public Integer indexId;
        public Double debt;
        public Double credAmount;
        public String sucursal;

    }
}
