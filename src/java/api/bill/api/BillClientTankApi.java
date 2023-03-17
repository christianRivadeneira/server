package api.bill.api;

import api.BaseAPI;
import api.GridResult;
import api.GridResultsPdf;
import api.MySQLCol;
import api.bill.app.GetClientList;
import api.bill.model.BillAccBalance;
import api.bill.model.BillBuilding;
import api.bill.model.BillClientTank;
import api.bill.model.BillInstCheck;
import api.bill.model.BillInstance;
import api.bill.model.BillMeter;
import api.bill.model.BillMeterCheck;
import api.bill.model.BillSpan;
import api.bill.model.CreateBillClientRequest;
import api.bill.model.dto.BillAppClient;
import api.bill.model.dto.DataClientRequest;
import api.bill.rpt.BillClientReports;
import api.ord.model.OrdPqrClientTank;
import api.ord.model.OrdTankClient;
import api.sys.model.SysCrudLog;
import controller.billing.BillClientTankController;
import java.awt.Color;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.billing.constants.Accounts;
import model.system.SessionLogin;
import utilities.MySQLPreparedQuery;
import utilities.MySQLQuery;
import utilities.apiClient.BooleanResponse;
import utilities.apiClient.IntegerResponse;
import utilities.mysqlReport.MySQLReport;
import web.billing.BillingServlet;

@Path("/billClientTank")
public class BillClientTankApi extends BaseAPI {

    @POST
    public Response insert(CreateBillClientRequest req) {
        BillClientTank obj = req.client;
        try (Connection conn = getConnection()) {
            try {
                BillInstance inst = BillingServlet.getInst(getBillInstId());
                if (inst.isNetInstance()) {
                    throw new Exception("Los clientes de redes deben crearse desde prospectos.");
                }
                conn.setAutoCommit(false);
                getSession(conn);
                BillClientTank.create(req, inst, conn, this);
                return Response.ok(obj).build();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(BillClientTank obj) {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);
                getSession(conn);
                useBillInstance(conn);
                BillClientTank old = new BillClientTank().select(obj.id, conn);
                obj.spanClosed = old.spanClosed;
                if (old.active && !obj.active) {
                    Object[] consObj = new MySQLQuery("SELECT reading - last_reading FROM bill_reading WHERE span_id = " + BillSpan.getByState("cons", conn).id + " AND client_tank_id = " + obj.id).getRecord(conn);

                    BigDecimal cons = BigDecimal.ZERO;
                    if (consObj != null && consObj.length > 0 && consObj[0] != null) {
                        cons = MySQLQuery.getAsBigDecimal(consObj[0], true);
                    }

                    if (cons.compareTo(BigDecimal.ZERO) > 0) {
                        throw new Exception("No se puede desactivar el cliente en este periodo, ya tiene consumo registrado.");
                    }
                }

                if (old.active && !obj.active) {
                    DecimalFormat df = new DecimalFormat("#,##0");
                    List<BillAccBalance> data = getNormalBalance(true, obj.id, getBillInstance(), conn);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < data.size(); i++) {
                        BillAccBalance a = data.get(i);
                        if (a.accId >= 0 && a.curBalance.compareTo(BigDecimal.ZERO) != 0) {
                            sb.append(a.accName);
                            sb.append(": ");
                            sb.append(df.format(a.curBalance));
                            sb.append(" ");
                        }
                    }

                    if (!sb.toString().isEmpty()) {
                        throw new Exception("No se puede inactivar, tiene deudas pendientes: " + sb.toString());
                    }
                    //es una mala práctica de los usuarios para saltarse las uniques, mario eraso 05/09/2020
//                    else {
//                       // obj.numMeter = "000-" + obj.numMeter;
//                        obj.apartment = "000-" + obj.apartment;
//                        obj.doc = "000-" + obj.doc;
//                        obj.numInstall = obj.numInstall + "(inac)";
//                    }
                } else if (!old.active && obj.active) {
                    throw new Exception("No se puede activar este Cliente");
                }

                obj.update(conn);
                BillClientTank.updateCache(obj.id, conn);

                useDefault(conn);

                //espejo
                OrdPqrClientTank mirror = OrdPqrClientTank.getByMirror(obj.id, getBillInstId(), conn);
                mirror.doc = obj.doc;
                mirror.apartament = obj.apartment;
                mirror.firstName = obj.firstName;
                mirror.lastName = obj.lastName;
                mirror.numInstall = obj.numInstall;
                mirror.phones = obj.phones;

                if (getBillInstance().isTankInstance()) {
                    useBillInstance(conn);
                    int oldBuildId = new BillClientTank().select(obj.id, conn).buildingId;
                    if (oldBuildId != obj.buildingId) {
                        BillClientTank.updateClientCounters(oldBuildId, getBillInstId(), conn);
                    }
                    BillClientTank.updateClientCounters(obj.buildingId, getBillInstId(), conn);
                } else {
                    mirror.neighId = obj.neighId;
                    mirror.address = obj.address;
                }
                useDefault(conn);
                mirror.update(conn);
                SysCrudLog.updated(this, obj, old, conn);
                conn.commit();
                return Response.ok(obj).build();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillClientTank obj = new BillClientTank().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            try {
                getSession(conn);
                BillInstance inst = getBillInstance();
                useBillInstance(conn);
                conn.setAutoCommit(false);
                new MySQLQuery("DELETE FROM ord_pqr_client_tank WHERE bill_instance_id = " + inst.id + " AND mirror_id = " + id).executeDelete(conn);
                new MySQLQuery("DELETE FROM bill_client_tank WHERE id = " + id).executeDelete(conn);
                conn.commit();
                return createResponse();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getByNumInstall")
    public Response getByNumInstall(@QueryParam("numInstall") String numInstall) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            MySQLQuery mq = new MySQLQuery("SELECT " + BillClientTank.getSelFlds("") + ", id FROM bill_client_tank WHERE UPPER(`num_install`) = ?1");
            mq.setParam(1, numInstall.toUpperCase());
            BillClientTank obj = new BillClientTank().select(mq, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/getClientData")
    public Response getClientData(DataClientRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            GridResult res;
            if (getBillInstance().isTankInstance()) {
                res = BillClientTankController.getTankClientsPage(req, conn);
            } else {
                res = BillClientTankController.getNetClientsPage(req, conn);
            }
            return createResponse(res);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/clientDataCount")
    public Response getClientDataCount(DataClientRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            int res;
            if (getBillInstance().isTankInstance()) {
                res = BillClientTankController.getTankClientsPageCount(req, conn);
            } else {
                res = BillClientTankController.getNetClientsPageCount(req, conn);
            }
            return createResponse(new IntegerResponse(res));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/moveToInst")
    public Response moveToInst(@QueryParam("clientId") int clientId, @QueryParam("newInstId") int newInstId) {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);
                getSession(conn);
                useBillInstance(conn);
                BillClientTank orig = new BillClientTank().select(clientId, conn);
                orig.active = false;
                orig.update(conn);
                BillInstance newInst = BillingServlet.getInst(newInstId);
                newInst.useInstance(conn);
                orig.active = true;
                orig.insert(conn);
                conn.commit();
                return createResponse();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/move")
    public Response move(@QueryParam("clientId") int clientId, @QueryParam("newBuildingId") int newBuildingId) {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);
                getSession(conn);
                useBillInstance(conn);
                BillClientTank orig = new BillClientTank().select(clientId, conn);
                BillClientTank client = new BillClientTank().select(clientId, conn);
                BillBuilding building = new BillBuilding().select(client.buildingId, conn);

                if (client.buildingId == newBuildingId) {
                    throw new Exception("Debe seleccionar un edificio diferente");
                }

                BillSpan origSpan = BillSpan.getByBuilding("reca", building.id, getBillInstance(), conn);
                BillSpan destSpan = BillSpan.getByBuilding("reca", newBuildingId, getBillInstance(), conn);

                if (origSpan.id != destSpan.id) {
                    throw new Exception("Los edificios deben estar en el mismo periodo.");
                }

                int spanId = origSpan.id;
                BigDecimal service1 = new MySQLQuery("SELECT SUM(total) FROM bill_build_service WHERE `bill_building_id` = " + building.id + " AND `bill_span_id` = " + spanId).getAsBigDecimal(conn, true);
                BigDecimal service2 = new MySQLQuery("SELECT SUM(total) FROM bill_build_service WHERE `bill_building_id` = " + newBuildingId + " AND `bill_span_id` = " + spanId).getAsBigDecimal(conn, true);
                BigDecimal factor1 = new MySQLQuery("SELECT factor FROM bill_build_factor WHERE `build_id` = " + building.id + " AND `bill_span_id` <= " + spanId + " ORDER BY `bill_span_id` DESC LIMIT 1").getAsBigDecimal(conn, true);
                BigDecimal factor2 = new MySQLQuery("SELECT factor FROM bill_build_factor WHERE `build_id` = " + newBuildingId + " AND `bill_span_id` <= " + spanId + " ORDER BY `bill_span_id` DESC LIMIT 1").getAsBigDecimal(conn, true);

                DecimalFormat numberFormat = new DecimalFormat("#,##0.00");

                if (!Objects.equals(service1, service2) || !Objects.equals(factor1, factor2)) {
                    String msg = "<html>Se encontró las siguientes inconsistencias:<br>";
                    if (!Objects.equals(service1, service2)) {
                        msg += "<b>Servicios: </b><br>";
                        msg += "Edificio Anterior: $" + numberFormat.format(service1) + "<br>";
                        msg += "Edificio Actual: $" + numberFormat.format(service2) + "<br>";
                    }
                    if (!Objects.equals(factor1, factor2)) {
                        msg += "<b>Factores: </b><br>";
                        msg += "Edificio Anterior: " + numberFormat.format(factor1) + "<br>";
                        msg += "Edificio Actual: " + numberFormat.format(factor2) + "<br>";
                    }
                    msg += "<br>No es posible continuar.</html>";
                    throw new Exception(msg);
                }
                client.buildingId = newBuildingId;
                client.update(conn);
                
                useDefault(conn);
                OrdPqrClientTank sigmaApto = OrdPqrClientTank.getByMirror(orig.id, getBillInstId(), conn);
                OrdTankClient sigmaBuilding = OrdTankClient.getByMirror(newBuildingId, getBillInstId(), conn);
                sigmaApto.buildOrdId = sigmaBuilding.id;
                sigmaApto.update(conn);                
                SysCrudLog.updated(this, client, orig, conn);
                conn.commit();
                return createResponse();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    public static String getCode(int instId, int clieId) {
        String instCode = String.valueOf(instId);
        String ref = String.valueOf(instCode.length() + 5) + instCode + String.valueOf(clieId);
        return ref;
    }

    public static String zeroFill(String str, int length) {
        int l = length - str.length();
        if (l <= 0) {
            return str;
        }
        char[] fill = new char[l];
        for (int j = 0; j < fill.length; j++) {
            fill[j] = '0';
        }
        return new String(fill).concat(str);
    }

    //  APP **********************************************************************
    //  **************************************************************************    
    @POST
    @Path("/getClientsApp")
    public synchronized Response getClientsApp(@QueryParam("version") int version) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            useBillInstance(conn);
            File tmp = null;
            switch (version) {
                case 3:
                    tmp = GetClientList.getClientsList(getBillInstance(), sl, conn);
                    break;
                default:
                    throw new Exception("Debe actualizar la versión");
            }
            return createResponse(tmp, "clientes.zip");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/consultClient")
    public Response consultClient(@QueryParam("queryType") String queryType, @QueryParam("query") String query) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillInstance bi = getBillInstance();
            Object[][] data;

            String q = "SELECT c.id, "
                    + (bi.isTankInstance() ? "b.name, " : "n.name, ")
                    + "c.num_install, CONCAT(c.first_name, ' ' , IFNULL(c.last_name,'')), c.doc, (SELECT `number` FROM bill_meter WHERE client_id = c.id ORDER BY start_span_id DESC LIMIT 1) "
                    + "FROM bill_client_tank c "
                    + (bi.isTankInstance() ? "INNER JOIN bill_building b ON b.id = c.building_id " : "INNER JOIN sigma.neigh n ON n.id = c.neigh_id ")
                    + "WHERE ";

            switch (queryType) {
                case "NUM_INSTALL":
                    q += "c.num_install LIKE '%" + query + "%' ";
                    break;
                case "NUM_METER":
                    q += "(SELECT `number` FROM bill_meter WHERE client_id = c.id ORDER BY start_span_id DESC LIMIT 1) LIKE '%" + query + "%' ";
                    break;
                case "LAST_NAME":
                    q += "c.last_name LIKE '%" + query + "%' ";
                    break;
                case "DOCUMENT":
                    q += "c.doc LIKE '%" + query + "%' ";
                    break;
                default:
                    throw new Exception("No implementado");
            }

            data = new MySQLQuery(q).getRecords(conn);

            List<BillAppClient> rta = new ArrayList<>();
            for (Object[] row : data) {
                BillAppClient obj = new BillAppClient();
                int clientId = MySQLQuery.getAsInteger(row[0]);
                obj.id = clientId;
                obj.buildName = MySQLQuery.getAsString(row[1]);
                obj.numInstall = MySQLQuery.getAsString(row[2]);
                obj.ownerName = MySQLQuery.getAsString(row[3]);
                obj.document = MySQLQuery.getAsString(row[4]);
                obj.numMeter = row[5] != null ? MySQLQuery.getAsString(row[5]) : "";
                obj.months = BillClientTankController.getDebtsMonthsClient(clientId, Accounts.C_CAR_GLP, conn);
                rta.add(obj);
            }

            return createResponse(rta);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    //  REPORTES-----------------------------------------------------------------------
    //  --------------------------------------------------------------------------------
    @POST
    @Path("/rptClientList")
    public Response rptClientList(@QueryParam("buildId") Integer buildId, @QueryParam("neighId") Integer neighId, @QueryParam("active") boolean active) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            MySQLReport rep;
            if (getBillInstance().isTankInstance()) {
                rep = BillClientReports.getClientsTank(buildId, active, conn);
            } else {
                rep = BillClientReports.getClientsNet(neighId, active, conn);
            }
            useDefault(conn);
            return createResponse(rep.write(conn), "Listado_de_Clientes.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/rptAllClientList")
    public Response rptAllClientList() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep;
            rep = BillClientReports.getAllClientsTank(conn);
            return createResponse(rep.write(conn), "Listado_de_Clientes.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    @Path("/rptTransactional")
    public Response rptTransactional(@QueryParam("date") String date, @QueryParam("period") String period) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            MySQLReport rep;
            rep = BillClientReports.getTransactionalClients(date, period, conn);
            return createResponse(rep.write(conn), "Transaccional_Fact_Tanques.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/rptReadingsByClient")
    public Response rptReadingsByClient(@QueryParam("spanId") Integer spanId, @QueryParam("buildId") Integer buildId, @QueryParam("notRead") boolean notRead) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            MySQLReport rep;
            BillInstance bi = getBillInstance();
            if (bi.type.equals("tank")) {
                rep = BillClientReports.getReadingsTankByClient(bi, spanId, buildId, notRead, conn);
            } else {
                rep = BillClientReports.getReadingsNetByClient(bi, spanId, notRead, conn);
            }
            useDefault(conn);
            return createResponse(rep.write(conn), "Listado_de_Clientes.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private static List<BillAccBalance> getBalance(int clientId, List<int[]> accs, Connection conn) throws Exception {
        MySQLPreparedQuery credQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_cred_id = ?1 AND t.cli_tank_id = ?2", conn);
        MySQLPreparedQuery debQ = new MySQLPreparedQuery(" SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_deb_id  = ?1 AND t.cli_tank_id = ?2", conn);

        List<BillAccBalance> rta = new ArrayList<>();

        for (int k = 0; k < accs.size(); k++) {

            int accountId = accs.get(k)[0];
            BillAccBalance row = new BillAccBalance();
            if (accountId >= 0) {
                credQ.setParameter(1, accountId);
                credQ.setParameter(2, clientId);
                debQ.setParameter(1, accountId);
                debQ.setParameter(2, clientId);

                row.accId = accountId;
                row.oppAccId = accs.get(k)[1];
                row.anticip = Accounts.anticipAccs.contains(accs.get(k)[0]);
                row.accName = Accounts.accNames.get(accountId);
                row.curBalance = debQ.getAsBigDecimal(true).subtract(credQ.getAsBigDecimal(true));
            } else {
                row.accId = accountId;
                row.oppAccId = accountId;
                row.anticip = false;
                row.accName = ("<html><body><b>" + (accountId == -1 ? "A favor del cliente" : "A favor de la empresa") + "</b></body><html>");
                row.curBalance = null;
            }
            rta.add(row);
        }
        return rta;
    }

    private static List<BillAccBalance> getNormalBalance(boolean anticip, int clientId, BillInstance inst, Connection conn) throws Exception {
        List<int[]> accs = new ArrayList<>();
        if (anticip) {
            accs.add(new int[]{-1});
            accs.add(new int[]{Accounts.C_ANTICIP, Accounts.E_ING_OP});
            accs.add(new int[]{-2});
        }
        accs.add(new int[]{Accounts.C_CONS, Accounts.E_ING_OP});
        if (inst.isNetInstance()) {
            accs.add(new int[]{Accounts.C_CONS_SUBS, Accounts.E_ING_OP});
            accs.add(new int[]{Accounts.C_CONTRIB, Accounts.E_CONTRIB});
            accs.add(new int[]{Accounts.C_REBILL, Accounts.E_ING_OP});
        }
        accs.add(new int[]{Accounts.C_BASI, Accounts.E_ING_OP});
        accs.add(new int[]{Accounts.C_CUOTA_SER_CLI_GLP, Accounts.E_ING_OP});
        accs.add(new int[]{Accounts.C_CUOTA_SER_CLI_SRV, Accounts.E_ING_OP});
        accs.add(new int[]{Accounts.C_CUOTA_SER_EDI, Accounts.E_ING_OP});
        accs.add(new int[]{Accounts.C_CUOTA_FINAN_DEU, Accounts.E_ING_OP});
        accs.add(new int[]{Accounts.C_CUOTA_INT_CRE, Accounts.E_INTER});

        accs.add(new int[]{Accounts.C_RECON, Accounts.E_ING_OP});
        accs.add(new int[]{Accounts.C_CAR_GLP, Accounts.E_ING_OP});
        accs.add(new int[]{Accounts.C_CAR_SRV, Accounts.E_ING_OP});
        accs.add(new int[]{Accounts.C_CAR_FINAN_DEU, Accounts.E_ING_OP});
        if (inst.isNetInstance()) {
            accs.add(new int[]{Accounts.C_CAR_CONTRIB, Accounts.E_CONTRIB});
        }
        accs.add(new int[]{Accounts.C_CAR_INTE_CRE, Accounts.E_INTER});
        accs.add(new int[]{Accounts.C_CAR_OLD, Accounts.E_ING_OP});
        accs.add(new int[]{Accounts.C_INT_GLP, Accounts.E_INTER});
        accs.add(new int[]{Accounts.C_INT_SRV, Accounts.E_INTER});
        accs.add(new int[]{Accounts.C_INT_FINAN_DEU, Accounts.E_INTER});
        if (inst.isNetInstance()) {
            accs.add(new int[]{Accounts.C_INT_CONTRIB, Accounts.E_INTER});
        }
        accs.add(new int[]{Accounts.C_INT_OLD, Accounts.E_INTER});
        return getBalance(clientId, accs, conn);
    }

    @GET
    @Path("/normalBalance")
    public Response getNormalBalance(@QueryParam("clientId") int clientId, @QueryParam("anticip") boolean anticip) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillInstance inst = getBillInstance();
            return createResponse(getNormalBalance(anticip, clientId, inst, conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    public static List<BillAccBalance> getBankBalance(int clientId, BillInstance inst, Connection conn) throws Exception {
        inst.useInstance(conn);
        List<int[]> accs = new ArrayList<>();
        accs.add(new int[]{Accounts.C_CONS, Accounts.BANCOS});
        if (inst.isNetInstance()) {
            accs.add(new int[]{Accounts.C_CONS_SUBS, Accounts.BANCOS});
            accs.add(new int[]{Accounts.C_CONTRIB, Accounts.BANCOS});
            accs.add(new int[]{Accounts.C_REBILL, Accounts.BANCOS});
        }
        accs.add(new int[]{Accounts.C_BASI, Accounts.BANCOS});
        accs.add(new int[]{Accounts.C_CUOTA_SER_CLI_GLP, Accounts.BANCOS});
        accs.add(new int[]{Accounts.C_CUOTA_SER_CLI_SRV, Accounts.BANCOS});
        accs.add(new int[]{Accounts.C_CUOTA_SER_EDI, Accounts.BANCOS});
        accs.add(new int[]{Accounts.C_CUOTA_FINAN_DEU, Accounts.BANCOS});
        accs.add(new int[]{Accounts.C_CUOTA_INT_CRE, Accounts.BANCOS});
        accs.add(new int[]{Accounts.C_RECON, Accounts.BANCOS});
        accs.add(new int[]{Accounts.C_CAR_GLP, Accounts.BANCOS});
        accs.add(new int[]{Accounts.C_CAR_SRV, Accounts.BANCOS});
        accs.add(new int[]{Accounts.C_CAR_FINAN_DEU, Accounts.BANCOS});
        if (inst.isNetInstance()) {
            accs.add(new int[]{Accounts.C_CAR_CONTRIB, Accounts.BANCOS});
        }
        accs.add(new int[]{Accounts.C_CAR_INTE_CRE, Accounts.BANCOS});
        accs.add(new int[]{Accounts.C_CAR_OLD, Accounts.BANCOS});
        accs.add(new int[]{Accounts.C_INT_GLP, Accounts.BANCOS});
        accs.add(new int[]{Accounts.C_INT_SRV, Accounts.BANCOS});
        accs.add(new int[]{Accounts.C_INT_FINAN_DEU, Accounts.BANCOS});
        if (inst.isNetInstance()) {
            accs.add(new int[]{Accounts.C_INT_CONTRIB, Accounts.BANCOS});
        }
        accs.add(new int[]{Accounts.C_INT_OLD, Accounts.BANCOS});
        return getBalance(clientId, accs, conn);
    }

    @GET
    @Path("/bankBalance")
    public Response getBalanceForBankNote(@QueryParam("clientId") int clientId) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(getBankBalance(clientId, this.getBillInstance(), conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/hasDebt")
    public Response hasDebt(@QueryParam("clientId") Integer clientId) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);

            int[] accs = new int[]{
                Accounts.C_CAR_GLP,
                Accounts.C_CAR_SRV,
                Accounts.C_CAR_FINAN_DEU,
                Accounts.C_CAR_CONTRIB,
                Accounts.C_CAR_INTE_CRE,
                Accounts.C_CAR_OLD,
                Accounts.C_INT_GLP,
                Accounts.C_INT_SRV,
                Accounts.C_INT_FINAN_DEU,
                Accounts.C_INT_CONTRIB,
                Accounts.C_INT_OLD};

            MySQLPreparedQuery credQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_cred_id = ?1 AND t.cli_tank_id = ?2", conn);
            MySQLPreparedQuery debQ = new MySQLPreparedQuery(" SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_deb_id  = ?1 AND t.cli_tank_id = ?2", conn);

            for (int accountId : accs) {

                credQ.setParameter(1, accountId);
                credQ.setParameter(2, clientId);
                debQ.setParameter(1, accountId);
                debQ.setParameter(2, clientId);

                BigDecimal balance = debQ.getAsBigDecimal(true).subtract(credQ.getAsBigDecimal(true));
                if (balance.compareTo(BigDecimal.ZERO) != 0) {
                    return createResponse(new BooleanResponse(true));
                }
            }
            return createResponse(new BooleanResponse(false));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("tankGrid")
    public Response getTankClientsTable(@QueryParam("justActive") boolean justActive, @QueryParam("buildId") int buildId) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            GridResult gr = new GridResult();
            String type = new MySQLQuery("SELECT t.`name` "
                    + "FROM bill_build_type t "
                    + "INNER JOIN bill_building b ON b.build_type_id = t.id "
                    + "WHERE b.id = " + buildId).getAsString(conn);
            gr.data = new MySQLQuery("SELECT "
                    + "t.id, CONCAT(t.first_name, ' ', IFNULL(t.last_name, ' ')), "
                    + "t.doc, "
                    + "p.name, "
                    + "t.apartment, t.num_install, (SELECT `number` FROM bill_meter WHERE client_id = t.id ORDER BY start_span_id DESC LIMIT 1), t.active "
                    + "FROM bill_client_tank t "
                    + "LEFT JOIN bill_client_list cl ON cl.client_id = t.id AND cl.span_id =  "
                    + "                                     (SELECT MAX(cl2.span_id) "
                    + "                                     FROM bill_client_list cl2 "
                    + "                                     WHERE cl2.client_id = t.id) "
                    + "LEFT JOIN bill_price_list p ON p.id = cl.list_id "
                    + "WHERE t.building_id = " + buildId + (justActive ? " AND t.active = 1 " : " ")
                    + "ORDER BY t.num_install"
            ).getRecords(conn);

            gr.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 170, "Nombres"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 90, "Documento"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 75, "Precio"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 85, (type != null ? type : "Apartamento")),
                new MySQLCol(MySQLCol.TYPE_TEXT, 85, "Instalación"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 80, "Medidor"),
                new MySQLCol(MySQLCol.TYPE_BOOLEAN, 35, "Activo")
            };
            return createResponse(gr);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/netGrid")
    public Response getNetClientsGrid(@QueryParam("active") boolean active) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            GridResult gr = new GridResult();
            gr.data = new MySQLQuery("SELECT "
                    + "t.id, CONCAT(t.first_name, ' ', IFNULL(t.last_name, ' ')), "
                    + "t.doc, "
                    + "t.address, t.code, (SELECT `number` FROM bill_meter WHERE client_id = t.id ORDER BY start_span_id DESC LIMIT 1), t.active "
                    + "FROM bill_client_tank t "
                    + "LEFT JOIN bill_client_list cl ON cl.client_id = t.id AND cl.span_id =  "
                    + "                                     (SELECT MAX(cl2.span_id) "
                    + "                                     FROM bill_client_list cl2 "
                    + "                                     WHERE cl2.client_id = t.id) "
                    + "LEFT JOIN bill_price_list p ON p.id = cl.list_id "
                    + "WHERE  t.active = " + active + " "
                    + "ORDER BY t.num_install"
            ).getRecords(conn);

            gr.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 170, "Nombres"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 90, "Documento"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 85, "Dirección"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 85, "Código"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 80, "Medidor"),
                new MySQLCol(MySQLCol.TYPE_BOOLEAN, 35, "Activo")
            };
            return createResponse(gr);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/hv")
    public Response hv(@QueryParam("clientId") int clientId, @QueryParam("limit") boolean limit) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillInstance inst = getBillInstance();
            File f = File.createTempFile("hojaDeVida", ".pdf");
            Color colorBackground = new Color(255, 248, 248);
            Color colorBorder = new Color(250, 69, 30);

            GridResultsPdf pdf = new GridResultsPdf(f, colorBackground, colorBorder);

            pdf.addDocumentTitle("Hoja de Vida");

            GridResult r;

            if (inst.isNetInstance()) {
                r = new GridResult();
                r.data = new MySQLQuery("SELECT "
                        + "c.code, "
                        + "c.contract_num, "
                        + "ct.name, "
                        + "c.first_name, "
                        + "c.last_name, "
                        + "c.doc_type, "
                        + "c.doc, "
                        + "c.doc_city, "
                        + "c.phones, "
                        + "c.mail "
                        + "FROM "
                        + "bill_client_tank c "
                        + "INNER JOIN sigma.bill_net_clie_type ct ON ct.id = c.client_type_id "
                        + "WHERE c.id = ?1").setParam(1, clientId).getRecords(conn);
                r.cols = new MySQLCol[]{
                    new MySQLCol(MySQLCol.TYPE_TEXT, 15, "NIU"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 15, "Contrato"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Tipo de Suscriptor"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Nombres"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Apellidos"),
                    new MySQLCol(MySQLCol.TYPE_ENUM, 30, "Tipo de Documento", new String[][]{new String[]{"cc", "Cédula de Ciudadanía"}, new String[]{"ce", "Cédula de Extrangería"}, new String[]{"pa", "Pasaporte"}, new String[]{"nit", "NIT"}}),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Documento"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Expedido en"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Teléfonos"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Email")
                };
                pdf.addVerticalGrid("Datos del Suscriptor", r);

                r.data = new MySQLQuery("SELECT "
                        + "c.address, "
                        + "n.name, "
                        + "bt.name, "
                        + "c.cadastral_code, "
                        + "c.real_state_code, "
                        + "c.sector_type, "
                        + "c.stratum "
                        + "FROM  "
                        + "bill_client_tank c "
                        + "INNER JOIN sigma.neigh n ON n.id = c.neigh_id "
                        + "INNER JOIN sigma.bill_net_building_type bt ON bt.id = c.building_type_id "
                        + "WHERE c.id = ?1").setParam(1, clientId).getRecords(conn);
                r.cols = new MySQLCol[]{
                    new MySQLCol(MySQLCol.TYPE_TEXT, 15, "Dirección"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Barrio"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Tipo de Construcción"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Código Catastral"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Matrícula Inmobiliaria"),
                    new MySQLCol(MySQLCol.TYPE_ENUM, 30, "Sector",
                    MySQLQuery.getEnumOptionsAsMatrix("r=Residencial&"
                    + "c=Comercial&"
                    + "i=Industrial&"
                    + "o=Oficial&"
                    + "ea=Especial Asistencial&"
                    + "ed=Especial Educativo")),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Estrato")
                };
                pdf.addVerticalGrid("Datos del Inmueble", r);

                r.data = new MySQLQuery("SELECT "
                        + "c.skip_interest, "
                        + "c.skip_reconnect, "
                        + "c.skip_contrib, "
                        + "c.mail_promo, "
                        + "c.mail_bill, "
                        + "c.sms_promo, "
                        + "c.sms_bill, "
                        + "c.net_building, "
                        + "c.notes "
                        + "FROM "
                        + "bill_client_tank c "
                        + "WHERE "
                        + "c.id = ?1;").setParam(1, clientId).getRecords(conn);
                r.cols = new MySQLCol[]{
                    new MySQLCol(MySQLCol.TYPE_BOOLEAN, 30, "Exento de Intereses"),
                    new MySQLCol(MySQLCol.TYPE_BOOLEAN, 30, "Exento de Reconexión"),
                    new MySQLCol(MySQLCol.TYPE_BOOLEAN, 30, "Exento de Contribución"),
                    new MySQLCol(MySQLCol.TYPE_BOOLEAN, 30, "Correos Promocionales"),
                    new MySQLCol(MySQLCol.TYPE_BOOLEAN, 30, "Correos del Servicio"),
                    new MySQLCol(MySQLCol.TYPE_BOOLEAN, 30, "SMS Promocionales"),
                    new MySQLCol(MySQLCol.TYPE_BOOLEAN, 30, "SMS del Servicio"),
                    new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 30, "Contrucción de la Red"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Notas")
                };
                pdf.addVerticalGrid("Datos del Servicio", r);
            } else {

                r = new GridResult();
                r.data = new MySQLQuery("SELECT "
                        + "c.code, "
                        + "c.num_install, "
                        + "c.first_name, "
                        + "c.last_name, "
                        + "c.doc, "
                        + "c.phones, "
                        + "c.mail "
                        + "FROM "
                        + "bill_client_tank c "
                        + "WHERE c.id = ?1").setParam(1, clientId).getRecords(conn);
                r.cols = new MySQLCol[]{
                    new MySQLCol(MySQLCol.TYPE_TEXT, 15, "Referencia para Pagos"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 15, "Número de Instalación"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Nombres"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Apellidos"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Documento"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Teléfonos"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Email")
                };
                pdf.addVerticalGrid("Datos del Suscriptor", r);

                r.data = new MySQLQuery("SELECT "
                        + "b.name, "
                        + "b.address, "
                        + "c.apartment "
                        + "FROM  "
                        + "bill_client_tank c "
                        + "INNER JOIN bill_building b ON b.id = c.building_id "
                        + "WHERE c.id = ?1").setParam(1, clientId).getRecords(conn);

                String apartLabel = new MySQLQuery("SELECT t.name FROM "
                        + "bill_build_type t "
                        + "INNER JOIN bill_building b ON t.id = b.build_type_id "
                        + "INNER JOIN bill_client_tank c ON b.id = c.building_id "
                        + "WHERE c.id = ?1").setParam(1, clientId).getAsString(conn);

                r.cols = new MySQLCol[]{
                    new MySQLCol(MySQLCol.TYPE_TEXT, 15, "Edificio"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 15, "Dirección"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 30, apartLabel)
                };
                pdf.addVerticalGrid("Datos del Inmueble", r);

                r.data = new MySQLQuery("SELECT "
                        + "c.skip_interest, "
                        + "c.skip_reconnect, "
                        + "c.skip_contrib, "
                        + "c.mail_promo, "
                        + "c.mail_bill, "
                        + "c.sms_promo, "
                        + "c.sms_bill, "
                        + "c.notes "
                        + "FROM "
                        + "bill_client_tank c "
                        + "WHERE "
                        + "c.id = ?1;").setParam(1, clientId).getRecords(conn);
                r.cols = new MySQLCol[]{
                    new MySQLCol(MySQLCol.TYPE_BOOLEAN, 30, "Exento de Intereses"),
                    new MySQLCol(MySQLCol.TYPE_BOOLEAN, 30, "Exento de Reconexión"),
                    new MySQLCol(MySQLCol.TYPE_BOOLEAN, 30, "Exento de Contribución"),
                    new MySQLCol(MySQLCol.TYPE_BOOLEAN, 30, "Correos Promocionales"),
                    new MySQLCol(MySQLCol.TYPE_BOOLEAN, 30, "Correos del Servicio"),
                    new MySQLCol(MySQLCol.TYPE_BOOLEAN, 30, "SMS Promocionales"),
                    new MySQLCol(MySQLCol.TYPE_BOOLEAN, 30, "SMS del Servicio"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Notas")
                };
                pdf.addVerticalGrid("Datos del Servicio", r);
            }

            r = new GridResult();
            r.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 80, "Instalación"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 80, "Desde"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 80, "Medidor"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 40, "Factor")
            };

            List<BillMeter> facs = BillMeter.getList(new MySQLQuery("SELECT " + BillMeter.getSelFlds("m") + " FROM bill_meter m WHERE m.client_id = ?1 ORDER BY m.start_span_id").setParam(1, clientId), conn);

            Object[][] data = new Object[facs.size()][4];
            for (int i = 0; i < facs.size(); i++) {
                BillMeter m = facs.get(i);
                BillSpan span = new BillSpan().select(m.startSpanId, conn);
                data[i][0] = m.start;
                data[i][1] = span.getConsLabel();
                data[i][2] = m.number;
                data[i][3] = m.factor;
            }
            r.data = data;
            pdf.addGrid("Medidores", r);

            ///////////////inicio revisiones
            Integer meterId = new MySQLQuery("SELECT bill_meter.id FROM bill_meter WHERE client_id = " + clientId + " ORDER BY start_span_id DESC LIMIT 1").getAsInteger(conn);
            BillInstCheck.InstCheckInfo ic = BillInstCheck.getNextDates(clientId, inst, null, conn);
            BillMeterCheck.MeterCheckInfo mc = BillMeterCheck.getNextDate(clientId, meterId, inst, null, conn);
            r = new GridResult();
            r.data = new Object[][]{{
                ic.lastCheck,
                ic.minDate,
                ic.maxDate,
                mc.lastCheck,
                mc.nextDate
            }};

            r.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 100, "Anterior de Instalaciones"),
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 100, "Mínima de Instalaciones"),
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 100, "Máxima de Instalaciones"),
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 100, "Anterior de Medidor"),
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 100, "Próxima de Medidor")
            };

            pdf.addVerticalGrid("Revisiones", r);
            /////////////////fin revisiones
            /////lecturas//////////////////           
            Object[][] readingsData = new MySQLQuery("SELECT "
                    + "sp.id, "
                    + "UPPER(cau.sector),  "
                    + "cau.stratum,  "
                    //+ "IFNULL((SELECT `number` FROM bill_meter WHERE client_id = cau.client_id AND start_span_id <= cau.span_id ORDER BY start_span_id DESC LIMIT 1), ''),  "
                    + "IFNULL(r1.last_reading, r2.last_reading),  "
                    + "IFNULL(r1.reading, r2.reading),  "
                    + "IFNULL(r1.reading, r2.reading) - IFNULL(r1.last_reading, r2.last_reading),  "
                    + "IF(cau.meter_factor IS NOT NULL AND cau.meter_factor <> 1, cau.meter_factor, sp.fadj),  "
                    + "cau.m3_subs + cau.m3_no_subs,  "
                    + "IF(r1.critical_reading IS NOT NULL, 'Desviación Crítica', f.name)  "
                    + "FROM bill_clie_cau cau  "
                    + "LEFT join bill_reading    r1 on r1.client_tank_id = cau.client_id and r1.span_id = cau.span_id  "
                    + "LEFT join bill_reading_bk r2 on r2.client_tank_id = cau.client_id and r2.span_id = cau.span_id  "
                    + "LEFT JOIN sigma.bill_reading_fault f ON f.id = r1.fault_id  "
                    + "INNER JOIN bill_span sp ON sp.id = cau.span_id  "
                    + "WHERE cau.client_id = ?1 "
                    + "ORDER BY cau.span_id ASC "
                    + (limit ? "LIMIT 15" : "")).setParam(1, clientId).getRecords(conn);

            for (Object[] row : readingsData) {
                row[0] = new BillSpan().select(MySQLQuery.getAsInteger(row[0]), conn).getConsLabel();
            }

            r = new GridResult();
            r.data = readingsData;
            r.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_TEXT, 100, "Periodo"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 50, "Sector"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 50, "Estrato"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_4, 80, "Anterior"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_4, 80, "Actual"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_4, 80, "Consumo Neto"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_4, 80, "Factor"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_4, 80, "Consumo Corregido"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 160, "Novedad"),};
            pdf.addGrid("Lecturas", r);

            if (inst.isNetInstance()) {

                ////inicio mediciones
                r = new GridResult();

                r.cols = new MySQLCol[]{
                    new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY_HH12_MM_SS_A, 180, "Fecha"),
                    new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 140, "Presión Medida"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 140, "Sustancia odorante"),
                    new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 140, "Nivel de concentración"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 220, "Observaciones")
                };

                r.data = new MySQLQuery("SELECT "
                        + "m.taken_dt, "
                        + "m.pressure, "
                        + "o.name, "
                        + "m.odorant_amount, "
                        + "m.notes "
                        + "FROM  "
                        + "bill_measure m "
                        + "INNER JOIN bill_client_tank c ON c.id = m.client_id "
                        + "INNER JOIN sigma.bill_odorant o ON o.id = m.odorant_id "
                        + "WHERE  "
                        + "c.id = ?1 AND m.taken_dt IS NOT NULL "
                        + (limit ? "LIMIT 15" : "")).setParam(1, clientId).getRecords(conn);
                pdf.addGrid("Mediciones de Calidad", r);
                ////fin mediciones 

                //inicio suspenciones
                r = new GridResult();
                r.data = new MySQLQuery("SELECT "
                        + "s.span_id, "
                        + "s.beg_dt, "
                        + "TIME_FORMAT(TIMEDIFF(s.end_dt, s.beg_dt), '%H:%i'), "
                        + "s.notes "
                        + "FROM bill_service_fail s "
                        + "INNER JOIN bill_client_tank c ON c.id = s.client_id "
                        + "WHERE c.id = ?1 "
                        + (limit ? "LIMIT 15" : "")
                ).setParam(1, clientId).getRecords(conn);

                for (Object[] row : r.data) {
                    row[0] = new BillSpan().select(MySQLQuery.getAsInteger(row[0]), conn).getConsLabel();
                }

                r.cols = new MySQLCol[]{
                    new MySQLCol(MySQLCol.TYPE_TEXT, 180, "Periodo"),
                    new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY_HH12_MM_SS_A, 180, "Fecha Inicio"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 140, "Duración"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 140, "Justificación")
                };
                pdf.addGrid("Suspensiones no Programadas", r);
                //fin suspenciones

                //inicio compensaciones
                r = new GridResult();
                r.data = new MySQLQuery("SELECT "
                        + "s.span_id, "
                        + "IF(c.sector_type = 'r', 'Residencial', 'No Residencial'), "
                        + "(SELECT b.bill_num FROM "
                        + "bill_bill b "
                        + "INNER JOIN bill_antic_note n ON b.bill_span_id = n.bill_span_id AND b.client_tank_id = n.client_tank_id "
                        + "WHERE n.srv_fail_id = s.id "
                        + "ORDER BY b.creation_date ASC LIMIT 1), "
                        + "TIME_FORMAT(TIMEDIFF(s.end_dt, s.beg_dt), '%H:%i'), "
                        + "s.creg_cost, "
                        + "s.avg_cons, "
                        + "s.cost "
                        + "FROM bill_service_fail s "
                        + "INNER JOIN bill_client_tank c ON c.id = s.client_id "
                        + "INNER JOIN bill_span p ON s.span_id = p.id "
                        + "WHERE s.client_id = ?1 "
                        + (limit ? "LIMIT 15" : "")).setParam(1, clientId).getRecords(conn);

                for (Object[] row : r.data) {
                    row[0] = new BillSpan().select(MySQLQuery.getAsInteger(row[0]), conn).getConsLabel();
                }

                r.cols = new MySQLCol[]{
                    new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Periodo"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 25, "Sector"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Factura"),
                    new MySQLCol(MySQLCol.TYPE_TEXT, 15, "DES"),
                    new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 25, "CI $/m3"),
                    new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 25, "Demanda Promedio m3/hr"),
                    new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 25, "Valor Compensado")
                };
                pdf.addGrid("Compensaciones", r);
                //fin compensaciones
            }

            ///facturas
            r = new GridResult();
            Object[][] dataBills = new MySQLQuery("SELECT "
                    + "b.bill_num, "//0
                    + "b.creation_date, "//4
                    + "b.payment_date, "//5
                    + "b.regist_date, "//6
                    + "k.name, "//3
                    + "NOT(b.active), "//7
                    + "NOT(b.total), "//8
                    + "(SELECT SUM(p.value) FROM bill_plan p WHERE p.account_deb_id = " + Accounts.BANCOS + " "
                    + "AND p.cli_tank_id = " + clientId + " AND p.doc_id = b.id AND p.doc_type = 'fac') "//11
                    + "FROM "
                    + "bill_bill AS b "
                    + "LEFT JOIN bill_bank k ON k.id = b.bank_id "
                    + "WHERE "
                    + "b.client_tank_id = " + clientId + " ORDER BY creation_date DESC "
                    + (limit ? "LIMIT 15" : "")).getRecords(conn);

            r.data = dataBills;
            r.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_TEXT, 180, "Cupón"),
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 140, "Creación"),
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 140, "Pago"),
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 140, "Registro"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 220, "Banco"),
                new MySQLCol(MySQLCol.TYPE_BOOLEAN, 100, "Anulada"),
                new MySQLCol(MySQLCol.TYPE_BOOLEAN, 100, "Parcial"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 120, "Total")};
            pdf.addGrid("Facturas", r);

            pdf.addGrid("Notas Crédito Normales", getNoteGrid(clientId, "n_cred", limit, conn));
            pdf.addGrid("Notas Débito Normales", getNoteGrid(clientId, "n_deb", limit, conn));
            pdf.addGrid("Notas Crédito Bancos", getNoteGrid(clientId, "aj_cred", limit, conn));
            pdf.addGrid("Notas Débito Bancos", getNoteGrid(clientId, "aj_deb", limit, conn));

            ///cortes y reconexiones
            r = new GridResult();
            r.data = new MySQLQuery("SELECT  "
                    + "s.susp_order_date, "
                    + "s.susp_date, "
                    + "s.recon_order_date, "
                    + "s.recon_date, "
                    + "s.cancelled, "
                    + "CONCAT(IFNULL(CONCAT(s.cancel_notes, '. '), ''), IFNULL(CONCAT(s.field_notes, '. '), ''), IFNULL(CONCAT(s.susp_notes, '. '), '')) "
                    + "FROM  "
                    + "bill_susp s  "
                    + "WHERE s.client_id = ?1 "
                    + (limit ? "LIMIT 15" : "")).setParam(1, clientId).getRecords(conn);

            r.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 140, "Orden de Suspención"),
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 140, "Suspención"),
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 140, "Orden de Reconexión"),
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 140, "Reconexión"),
                new MySQLCol(MySQLCol.TYPE_BOOLEAN, 100, "Cancelada"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 250, "Notas")
            };
            pdf.addGrid("Suspeciones y Reconexiónes", r);

            //pqrs
            r = new GridResult();
            r.data = new MySQLQuery("SELECT p.regist_date, p.arrival_date, p.attention_date, r.description FROM "
                    + "sigma.ord_pqr_tank p "
                    + "INNER JOIN sigma.ord_pqr_client_tank c ON p.client_id = c.id "
                    + "INNER JOIN sigma.ord_pqr_reason r ON p.reason_id = r.id "
                    + "WHERE p.anul_cause_id IS NULL AND c.mirror_id = ?1 AND c.bill_instance_id = ?2 "
                    + (limit ? "LIMIT 15" : "")).setParam(1, clientId).setParam(2, getBillInstId()).getRecords(conn);

            r.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 140, "Registro"),
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 140, "Llegada"),
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 140, "Atención"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 250, "Motivo")
            };
            pdf.addGrid("Pqrs", r);

            //asistencias técnicas
            r = new GridResult();
            r.data = new MySQLQuery("SELECT "
                    + "of.sname, "
                    + "p.serial, "
                    + "cast(TIMESTAMP(p.regist_date, p.regist_hour) as datetime), "
                    + "TIMESTAMP(p.confirm_date, p.confirm_time), "
                    + "r.description, "
                    + "CONCAT(t.first_name, ' ', t.last_name), "
                    + "p.cancel_date IS NOT NULL "
                    + "FROM sigma.ord_repairs AS p "
                    + "LEFT JOIN sigma.ord_technician AS t ON t.id = p.technician_id "
                    + "INNER JOIN sigma.ord_pqr_client_tank AS cl ON cl.id = p.client_id "
                    + "INNER JOIN sigma.ord_pqr_reason AS r ON r.id = p.reason_id "
                    + "INNER JOIN sigma.employee em ON em.id = p.regist_by "
                    + "INNER JOIN sigma.ord_office AS of ON of.id = p.office_id "
                    + "INNER JOIN sigma.ord_office o ON o.id = p.office_id "
                    + "WHERE cl.bill_instance_id = ?2 AND cl.mirror_id = ?1 "
                    + (limit ? "LIMIT 15" : "")).setParam(1, clientId).setParam(2, getBillInstId()).getRecords(conn);

            for (int i = 0; i < r.data.length; i++) {
                Object[] row = r.data[i];
                System.out.println(row[2].getClass());
            }

            r.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_TEXT, 140, "Oficina"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 140, "Serial"),
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY_HH12_MM_A, 140, "Fecha Registro"),
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY_HH12_MM_A, 140, "Fecha Confirmación"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 250, "Motivo"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 250, "Técnico"),
                new MySQLCol(MySQLCol.TYPE_BOOLEAN, 250, "Cancelado")
            };
            pdf.addGrid("Asistencias Técnicas", r);

            pdf.close();

            return createResponse(f, f.getName());
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private GridResult getNoteGrid(int clientId, String type, boolean limit, Connection conn) throws Exception {
        GridResult tbl = new GridResult();
        tbl.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 90, "Creación"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 150, "Periodo"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 150, "Usuario"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 200, "Descripción")
        };
        tbl.data = new MySQLQuery("SELECT n.when_notes, DATE_FORMAT(s.cons_month, 'Consumos de %M/%Y'), (SELECT CONCAT(e.first_name, ' ', e.last_name) FROM bill_transaction t "
                + "INNER JOIN sigma.employee e ON t.cre_usu_id = e.id "
                + "WHERE t.doc_id = n.id AND t.doc_type = 'not' LIMIT 1), n.desc_notes  FROM "
                + "bill_note n "
                + "INNER JOIN bill_span s ON s.id = n.bill_span_id "
                + "WHERE n.client_tank_id = ?1 AND n.type_notes = ?2 "
                + "ORDER BY n.when_notes DESC "
                + (limit ? "LIMIT 15" : "")).setParam(1, clientId).setParam(2, type).getRecords(conn);
        return tbl;
    }

}
