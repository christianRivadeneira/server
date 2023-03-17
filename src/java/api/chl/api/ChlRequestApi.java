package api.chl.api;

import api.BaseAPI;
import api.MultiPartRequest;
import api.chl.model.ChlItem;
import api.chl.model.ChlRequest;
import api.chl.model.ChlRequestImport;
import api.chl.model.ChlRequestItem;
import api.sys.api.SysFlowApi;
import api.sys.model.SysFlowReq;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import utilities.importer.Importer;
import utilities.importer.ImporterCol;
import utilities.xlsReader.XlsReader;
import web.fileManager;
import web.system.flow.Randi;
import web.system.flow.Randi.RandiRequest;

@Path("/chlRequest")
public class ChlRequestApi extends BaseAPI {

    @POST
    @Path("/import")
    public Response importRequest(@Context HttpServletRequest request) throws Exception {
        try (Connection conn = getConnection()) {
            SessionLogin sess = getSession(conn);

            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);

            Map<String, String> pars = mr.params;

            ChlRequestImport reqImport = new ChlRequestImport();
            reqImport.empId = 1; // se importan a nombre del admin admin 
            reqImport.date = new Date();
            reqImport.notes = pars.get("notes");
            reqImport.provId = MySQLQuery.getAsInteger(pars.get("provId"));

            Object[][] data = XlsReader.readExcel(mr.getFile().file.getPath(), 0).getData();
            List<OrderDummy> allRows = new ArrayList<>();

            List<ImporterCol> cols = new ArrayList<>();
            cols.add(new ImporterCol("Referencia", ImporterCol.TYPE_TEXT, false));//0
            cols.add(new ImporterCol("Ref Proveedor", ImporterCol.TYPE_TEXT, true));//1
            cols.add(new ImporterCol("Detalle", ImporterCol.TYPE_TEXT, false));//2
            cols.add(new ImporterCol("Marca", ImporterCol.TYPE_TEXT, false));//3
            cols.add(new ImporterCol("Clase", ImporterCol.TYPE_INTEGER, false));//4
            cols.add(new ImporterCol("Cantidad pedida", ImporterCol.TYPE_INTEGER, false));//5
            cols.add(new ImporterCol("Cantidad sugerida", ImporterCol.TYPE_INTEGER, false));
            cols.add(new ImporterCol("Saldo actual", ImporterCol.TYPE_INTEGER, false));
            cols.add(new ImporterCol("Stock mínimo", ImporterCol.TYPE_INTEGER, false));
            cols.add(new ImporterCol("Stock máximo", ImporterCol.TYPE_INTEGER, false));
            cols.add(new ImporterCol("Ventas", ImporterCol.TYPE_INTEGER, false));
            cols.add(new ImporterCol("Prom. ventas/dia", ImporterCol.TYPE_INTEGER, false));
            cols.add(new ImporterCol("Prom. ventas/mes", ImporterCol.TYPE_INTEGER, false));
            cols.add(new ImporterCol("Ult costo", ImporterCol.TYPE_DECIMAL, false));

            Importer importer = new Importer(data, cols);

            for (int i = importer.headRow + 1; i < data.length; i++) {
                importer.row = data[i];
                if (!isAllWhite(importer)) {
                    importer.validateValues(importer.row, i);
                    OrderDummy get = new OrderDummy();
                    get.itemCode = MySQLQuery.getAsString(importer.get(0)).toUpperCase().trim();
                    get.provCode = importer.get(1) == null ? "" : MySQLQuery.getAsString(importer.get(1)).toUpperCase().trim();
                    get.provCode = get.provCode.isEmpty() ? null : get.provCode;
                    get.notes = MySQLQuery.getAsString(importer.get(2)).trim();
                    get.brand = MySQLQuery.getAsString(importer.get(3)).toUpperCase().trim();
                    get.amount = (importer.get(5) == null ? 1 : MySQLQuery.getAsInteger(importer.get(5).toString().trim()));
                    get.price = MySQLQuery.getAsBigDecimal(importer.get(13).toString().trim(), true);

                    Boolean item = new MySQLQuery("SELECT COUNT(*)>0 FROM chl_item ci "
                            + "WHERE "
                            + "UPPER(ci.refsys) = " + get.itemCode + " AND "
                            + (get.provCode != null ? "UPPER(ci.refprov) = '" + get.provCode + "' ;" : "UPPER(ci.refprov) IS NULL;")).getAsBoolean(conn);
                    if (!item) {
                        throw new Exception("El artículo \"" + get.itemCode + "\" No se encuentra en el sistema");
                    }

                    get.provId = reqImport.provId;

                    allRows.add(get);
                } else {
                    break;
                }
            }

            allRows = sortByProvider((ArrayList<OrderDummy>) allRows);

            List<Request> reqs = new ArrayList<>();
            int provId = 0;

            Request req = new Request();

            for (int i = 0; i < allRows.size(); i++) {
                OrderDummy dm = allRows.get(i);

                if (provId != allRows.get(i).provId) {
                    provId = allRows.get(i).provId;
                    req.request = new ChlRequest();
                    req.request.requestDt = new Date();
                    req.request.providerId = provId;
                    req.request.orderDt = new Date();
                    req.request.notes = "Importación";
                    req.request.kind = "supply";
                    req.request.clientId = 1310;
                    req.items = new ArrayList<>();
                }

                if (provId == allRows.get(i).provId) {

                    DummyItem item = new DummyItem();

                    ChlRequestItem reqItem = new ChlRequestItem();
                    reqItem.name = dm.notes;
                    reqItem.notes = dm.brand + " - " + dm.notes;
                    reqItem.amount = dm.amount;
                    reqItem.price = dm.price;

                    ChlItem chlItem = new ChlItem();
                    chlItem.refsys = dm.itemCode;
                    chlItem.refprov = dm.provCode;
                    chlItem.notes = dm.notes;
                    chlItem.brand = dm.brand;
                    chlItem.provId = dm.provId;

                    item.reqItem = reqItem;
                    item.chlItem = chlItem;

                    req.items.add(item);
                }

                if (i == allRows.size() - 1 || provId != allRows.get(i + 1).provId) {
                    reqs.add(req);
                    req = new Request();
                }
            }

            insertData(reqImport, reqs, sess, conn);
            return createResponse("ok");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/importOrdens")
    public Response importOrdens(@Context HttpServletRequest request) throws Exception {
        try (Connection conn = getConnection()) {
            SessionLogin sess = getSession(conn);

            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);

            Map<String, String> pars = mr.params;

            ChlRequestImport reqImport = new ChlRequestImport();
            reqImport.empId = 1; // se importan a nombre del admin admin 
            reqImport.date = new Date();
            reqImport.notes = pars.get("notes");
            reqImport.provId = MySQLQuery.getAsInteger(pars.get("provId"));

            Object[][] data = XlsReader.readExcel(mr.getFile().file.getPath(), 0).getData();
            List<OrderProv> allRows = new ArrayList<>();

            List<ImporterCol> cols = new ArrayList<>();
            cols.add(new ImporterCol("tipo", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("numero", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("ano", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("mes", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("dia", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("fecha", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("cliente", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("razonsoc", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("perjur", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("nomcomer", ImporterCol.TYPE_TEXT, true));
            cols.add(new ImporterCol("ivendedor", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("vendedor", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("referencia", ImporterCol.TYPE_TEXT, true));
            cols.add(new ImporterCol("detallec", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("marca", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("vrunit", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("vrtotal", ImporterCol.TYPE_DECIMAL, false));

            Importer importer = new Importer(data, cols);

            for (int i = importer.headRow + 1; i < data.length; i++) {
                importer.row = data[i];
                if (!isAllWhite(importer)) {
                    importer.validateValues(importer.row, i);
                    OrderProv get = new OrderProv();
                    get.numero = MySQLQuery.getAsString(importer.get(1)).toUpperCase().trim();
                    get.year = MySQLQuery.getAsString(importer.get(2));
                    get.month = MySQLQuery.getAsString(importer.get(3));
                    get.day = MySQLQuery.getAsString(importer.get(4));
                    get.cliente = MySQLQuery.getAsString(importer.get(6)).toUpperCase().trim();
                    get.idVendedor = MySQLQuery.getAsString(importer.get(10));
                    get.vendedor = MySQLQuery.getAsString(importer.get(11));
                    get.referencia = MySQLQuery.getAsString(importer.get(12));
                    get.detalle = MySQLQuery.getAsString(importer.get(13));
                    get.vrlTotal = MySQLQuery.getAsBigDecimal(importer.get(16), true);
                    allRows.add(get);
                } else {
                    break;
                }
            }

            List<Request> reqs = new ArrayList<>();

            Integer provId = new MySQLQuery("SELECT id FROM prov_provider p WHERE p.name = 'OTROS'").getAsInteger(conn);

            if (provId == null) {
                provId = new MySQLQuery("INSERT INTO `prov_provider` (`name`, `nit`, `approved`, `state`, `evaluated`, `service_station`, `eqs_type`, `mto_type`, `active`) VALUES ('OTROS', '000000', 0, 'aprob', 0, 0, 0, 0, 1);").executeInsert(conn);
            }

            for (int i = 0; i < allRows.size(); i++) {
                OrderProv dm = allRows.get(i);

                if (dm.referencia != null && dm.referencia.length() > 0) {
                    Integer clientId = new MySQLQuery("SELECT id FROM crm_client c WHERE UPPER(c.name) = ?1;").setParam(1, dm.cliente).getAsInteger(conn);

                    if (clientId == null) {
                        clientId = new MySQLQuery("INSERT INTO `crm_client` ( `name`, `active`, `begin_date`, `person`, `type`) VALUES (?1, 1, NOW(), 0, 'client');").setParam(1, dm.cliente).executeInsert(conn);
                    }

                    Integer sellerId = new MySQLQuery("SELECT e.id FROM employee e WHERE e.document = ?1;").setParam(1, dm.idVendedor).getAsInteger(conn);

                    if (sellerId == null) {
                        sellerId = new MySQLQuery("INSERT INTO `employee` (`document`, `first_name`, `last_name`, `guest`) VALUES (?1, ?2, ' ', 0);")
                                .setParam(1, dm.idVendedor)
                                .setParam(2, dm.vendedor)
                                .executeInsert(conn);
                    }

                    // se cuenta en c la referenci si existe mas de un provedor con la misma ref , se pregunta si existe la ref con provedor otro 
                    // si no existe la ref con otro se la crea 
                    Integer itemId = null;
                    Integer c = new MySQLQuery("SELECT COUNT(*) FROM chl_item ci "
                            + "WHERE "
                            + "UPPER(ci.refsys) = ?1 ").setParam(1, dm.referencia.toUpperCase().trim()).getAsInteger(conn);
                    if (c > 1) { //varios proveedores tienen la referencia
                        itemId = new MySQLQuery("SELECT ci.id FROM chl_item ci "
                                + "WHERE "
                                + "UPPER(ci.refsys) = ?1 AND "
                                + "ci.prov_id = ?2 ").setParam(1, dm.referencia.toUpperCase().trim()).setParam(2, provId).getAsInteger(conn);
                    } else {
                        itemId = new MySQLQuery("SELECT ci.id FROM chl_item ci "
                                + "WHERE "
                                + "UPPER(ci.refsys) = ?1 ").setParam(1, dm.referencia.toUpperCase().trim()).getAsInteger(conn);
                    }
                    if (itemId == null) {
                        itemId = new MySQLQuery("INSERT INTO `chl_item` (`prov_id`, `refsys`, `refprov`, `brand`, `notes`) VALUES (?1, ?2, ?3, ?4, ?5);")
                                .setParam(1, provId)
                                .setParam(2, dm.referencia)
                                .setParam(3, dm.referencia)
                                .setParam(4, dm.detalle.length() > 64 ? dm.detalle.substring(0, 64) : dm.detalle)
                                .setParam(5, dm.detalle.length() > 200 ? dm.detalle.substring(0, 200) : dm.detalle)
                                .executeInsert(conn);
                    }

                    Date dateBill = new Date();

                    if (dm.day != null && dm.day.length() > 0 && dm.month != null && dm.month.length() > 0 && dm.year != null && dm.year.length() > 0) {
                        String strDate = dm.day + "/" + dm.month + "/" + dm.year;
                        dateBill = new SimpleDateFormat("dd/MM/yyyy").parse(strDate);
                    }

                    new MySQLQuery("INSERT INTO `chl_bill` (`emp_id`, `date_import`, `num`, `date`, `client_id`, `seller_id`, `item_id`, `total_value`) "
                            + "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8);")
                            .setParam(1, 1) // admin
                            .setParam(2, Calendar.getInstance().getTime())
                            .setParam(3, dm.numero)
                            .setParam(4, dateBill)
                            .setParam(5, clientId)
                            .setParam(6, sellerId)
                            .setParam(7, itemId)
                            .setParam(8, dm.vrlTotal.setScale(2, RoundingMode.HALF_UP))
                            .executeInsert(conn);
                }

            }

            return createResponse("ok");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private boolean isAllWhite(Importer imp) {
        if (imp.row[0] == null
                && imp.row[1] == null
                && imp.row[2] == null
                && imp.row[3] == null
                && imp.row[4] == null
                && imp.row[5] != null) {
            return true;
        }
        return imp.isAllWhite();
    }

    private void insertData(ChlRequestImport reqImport, List<Request> reqs, SessionLogin sess, Connection conn) throws Exception {
        conn.setAutoCommit(false);
        try {
            //registro de importacion
            int importId = reqImport.insert(conn);
            //registro de solicitud y flujo
            for (int i = 0; i < reqs.size(); i++) {
                ChlRequest chlReq = reqs.get(i).request;
                SysFlowReq sysReq = SysFlowApi.createSysFlowReq(2, conn, sess);
                int sysReqId = sysReq.insert(conn);
                chlReq.sysReqId = sysReqId;
                chlReq.importId = importId;
                chlReq.reqSerial = sysReqId + "";
                int reqId = chlReq.insert(conn);

                RandiRequest randiReq = new RandiRequest();
                randiReq.reqId = sysReq.id;
                randiReq.oper = "start";

                JsonObject result = new Randi().processRandiFlow(randiReq, conn, sess);
                if (!result.containsKey("status") || result.getString("status").equals("error")) {
                    throw new Exception("Ocurrio un error al crear la solicitud");
                }

                //registro de los items de la orden
                List<DummyItem> dummyItems = reqs.get(i).items;
                for (DummyItem obj : dummyItems) {
                    ChlItem chlItem = obj.chlItem;
                    ChlRequestItem reqItem = obj.reqItem;
                    reqItem.requestId = reqId;

                    Integer itemId = new MySQLQuery("SELECT ci.id FROM chl_item ci "
                            + "WHERE "
                            + "UPPER(ci.refsys) = " + chlItem.refsys + " AND "
                            + (chlItem.refprov != null ? "UPPER(ci.refprov) = '" + chlItem.refprov + "' " : "UPPER(ci.refprov) IS NULL ")
                            + "AND ci.prov_id = " + chlItem.provId + " LIMIT 1;").getAsInteger(conn);

                    if (itemId != null) {
                        reqItem.itemId = itemId;
                    } else {
                        reqItem.itemId = chlItem.insert(conn);
                    }
                    reqItem.insert(conn);
                }

                randiReq.oper = "getSteps";
                result = new Randi().processRandiFlow(randiReq, conn, sess);
                if (!result.containsKey("status") || result.getString("status").equals("error")) {
                    throw new Exception("Ocurrio un error al crear la solicitud");
                }
                int curStepId = result.getInt("curStepId");
                int toStepId = new MySQLQuery("SELECT r.id FROM "
                        + "sys_flow_stage s "
                        + "INNER JOIN sys_flow_step r ON r.stage_id = s.id "
                        + "WHERE s.type_id = 2 AND s.sname = 'purch'").getAsInteger(conn);

                randiReq.oper = "move";
                randiReq.inStepId = curStepId;
                randiReq.outStepId = toStepId;
                randiReq.subs = true;
                randiReq.notes = "Paso automatico";
                result = new Randi().processRandiFlow(randiReq, conn, sess);
                if (!result.containsKey("status") || result.getString("status").equals("error")) {
                    throw new Exception("Ocurrio un error al crear la solicitud");
                }

            }
            conn.commit();
        } catch (Exception ex) {
            conn.rollback();
            throw ex;
        }

    }

    private class OrderDummy implements Comparable<OrderDummy> {

        public String itemCode;
        public String provCode;
        public String notes;
        public String brand;
        public int amount;
        private Integer provId;
        public BigDecimal price;

        public Comparator<OrderDummy> provComparator = new Comparator<OrderDummy>() {
            @Override
            public int compare(OrderDummy o1, OrderDummy o2) {
                int vh1 = o1.provId;
                int vh2 = o2.provId;
                return vh1 - vh2;
            }
        };

        @Override
        public int compareTo(OrderDummy o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private class OrderProv implements Comparable<OrderProv> {

        public String numero;
        public String cliente;
        public String idVendedor;
        public String vendedor;
        public String referencia;
        public String detalle;
        public String day;
        public String month;
        public String year;
        public BigDecimal vrlTotal;

        public Comparator<OrderProv> provComparator = new Comparator<OrderProv>() {
            @Override
            public int compare(OrderProv o1, OrderProv o2) {
                return o1.numero.equals(o2.numero) ? 0 : 1;
            }
        };

        @Override
        public int compareTo(OrderProv o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private List<OrderDummy> sortByProvider(List<OrderDummy> data) {
        OrderDummy[] dataArray = data.toArray(new OrderDummy[data.size()]);
        Arrays.sort(dataArray, new OrderDummy().provComparator);
        return new ArrayList(Arrays.asList(dataArray));
    }

    private class Request {

        ChlRequest request;
        List<DummyItem> items = new ArrayList<>();

    }

    private class DummyItem {

        ChlItem chlItem;
        ChlRequestItem reqItem;
    }

}
