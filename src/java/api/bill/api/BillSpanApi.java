package api.bill.api;

import api.BaseAPI;
import api.bill.Locking;
import api.bill.dto.StartBillingRequestDto;
import api.bill.model.BillBuildFactor;
import api.bill.model.BillBuilding;
import api.bill.model.BillCfg;
import api.bill.model.BillClientTank;
import api.bill.model.BillCloseRequest;
import api.bill.model.BillInstance;
import api.bill.model.BillPriceSpan;
import api.bill.model.BillReading;
import api.bill.model.BillSpan;
import api.bill.model.dto.BillReadingsCheck;
import api.bill.writers.bill.BillWriter;
import api.bill.writers.bill.GetBills;
import api.ord.model.OrdPqrRequest;
import api.sys.model.SysCfg;
import api.sys.model.SysCrudLog;
import controller.billing.BillReadingController;
import controller.billing.BillSpanController;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import service.MySQL.MySQLSelect;
import utilities.MySQLPreparedQuery;
import utilities.MySQLQuery;
import utilities.apiClient.BooleanResponse;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.SummaryRow;
import utilities.mysqlReport.Table;
import web.billing.BillingServlet;

@Path("/billSpan")
public class BillSpanApi extends BaseAPI {

    @PUT
    @Path("/setCostParams")
    public Response setCostParams(BillSpan obj) {
        try (Connection gral = getConnection(); Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);

            if (!new BillSpan().select(obj.id, conn).state.equals("cons")) {
                throw new Exception("Solo se puede hacer para el periodo en consumo");
            }

            BillSpan old = new BillSpan().select(obj.id, conn);
            BillInstance inst = getBillInstance();
            if (inst.isNetInstance()) {
                System.out.println("1- Procedimiento calcular ceq1");
                BillSpan.calculateNetParams(obj, inst, gral, conn);

            }

            obj.costsSet = true;
            obj.update(conn);

            useDefault(conn);

            if (inst.isNetInstance()) {
                List<BillInstance> siblings = BillInstance.getBillingByMarket(inst.marketId, conn);
                for (int i = 0; i < siblings.size(); i++) {
                    BillInstance si = siblings.get(i);
                    if (si.id != inst.id) {
                        si.useInstance(conn);
                        BillSpan ss = BillSpan.getByMonth(si.db, obj.consMonth, conn);
                        if (!ss.paramsDone) {
                            ss.adjust = obj.adjust;
                            ss.reconnect = obj.reconnect;
                            ss.contribNr = obj.contribNr;
                            ss.contribR = obj.contribR;
                            ss.vitalCons = obj.vitalCons;
                            ss.p = obj.p;
                            ss.fpc = obj.fpc;
                            ss.pms = obj.pms;
                            ss.cglp = obj.cglp;
                            ss.t = obj.t;
                            ss.tv = obj.tv;
                            ss.fv = obj.fv;
                            ss.power = obj.power;
                            ss.covidEmergency = obj.covidEmergency;
                            ss.update(conn);
                        }
                    }
                }
            }
            useDefault(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillSpan obj = new BillSpan().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/byClient")
    public Response byClient(@QueryParam("state") String state, @QueryParam("clientId") Integer clientId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            return Response.ok(BillSpan.getByClient(state, clientId, getBillInstance(), conn)).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/byState")
    public Response getByState(@QueryParam("state") String state) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            return Response.ok(BillSpan.getByState(state, conn)).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/byBuilding")
    public Response byBuilding(@QueryParam("state") String state, @QueryParam("buildId") int buildId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            return Response.ok(BillSpan.getByBuilding(state, buildId, getBillInstance(), conn)).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getAll")
    public Response getAll(@QueryParam("onlyWithReadings") boolean onlyWithReadings) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            return createResponse(BillSpan.getAll(conn, onlyWithReadings));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/noCons")
    public Response noCons() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            return createResponse(BillSpan.getNoConsum(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/sui")
    public Response sui() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            return createResponse(BillSpan.getSui(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/byMonth")
    public Response byMonth(@QueryParam("month") int month, @QueryParam("year") int year) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            return createResponse(BillSpan.getByMonth(year, month, getBillInstance(), conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/isPricesListOpen")
    public Response isPricesListOpen(@QueryParam("spanId") int spanId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillSpan s = new BillSpan().select(spanId, conn);
            return createResponse(new BooleanResponse(BillSpan.isPricesListOpen(s, getBillInstance(), conn)));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/isParamsOpen")
    public Response isParamsOpen(@QueryParam("spanId") int spanId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillSpan s = new BillSpan().select(spanId, conn);
            return createResponse(new BooleanResponse(BillSpan.isParamsOpen(s, getBillInstance(), conn)));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/isInterOpen")
    public Response isInterOpen(@QueryParam("spanId") int spanId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillSpan s = new BillSpan().select(spanId, conn);
            return createResponse(new BooleanResponse(BillSpan.isInterOpen(s.id, getBillInstance(), conn)));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/openPricesSpan")
    public Response getOpenPricesSpan() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            return createResponse(BillSpan.getOpenPricesSpan(getBillInstance(), conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/revertClose")
    public Response revertClose() {
        try (Connection conn = getConnection()) {
            SessionLogin session = getSession(conn);
            BillInstance inst = getBillInstance();
            useBillInstance(conn);
            BillSpanController.revertSpan(session.employeeId, inst, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("tankSuiReport")
    public Response getTankSuiReport(@QueryParam("spanId") int spanId) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);
            SysCfg sysCfg = SysCfg.select(conn);
            BillInstance inst = getBillInstance();
            useBillInstance(conn);
            BillSpan span = new BillSpan().select(spanId, conn);

            Map<Integer, BigDecimal> prices = BillPriceSpan.getPricesMap(conn, spanId);
            MySQLReport rep = new MySQLReport("Reporte General SUI - " + inst.name, span.getConsLabel(), "SUI", now(conn));
            rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
            rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.000"));//1
            rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.00"));//2
            rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$ #,##0.000"));//3
            rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//4
            rep.setZoomFactor(80);
            Table tb = new Table("model");
            tb.getColumns().add(new Column("Núm Instal", 15, 0));//0
            tb.getColumns().add(new Column("Cliente", 30, 0));//1
            tb.getColumns().add(new Column("Factor", 8, 2));//2
            tb.getColumns().add(new Column("Cupón", 15, 0));//3
            tb.getColumns().add(new Column("Fecha", 12, 4));//4
            tb.getColumns().add(new Column("Cons. m3", 14, 1));//5
            tb.getColumns().add(new Column("Cons. gal", 14, 1));//6
            tb.getColumns().add(new Column("Cons. kg", 14, 1));//7
            tb.getColumns().add(new Column("Cons. gal * Fact", 18, 1));//8
            tb.getColumns().add(new Column("Cons. kg * Fact", 18, 1));//9
            tb.getColumns().add(new Column("Valor", 20, 3));//10

            List<BillBuilding> buildings = BillBuilding.getAll(conn);
            MySQLPreparedQuery factorQ = new MySQLPreparedQuery(BillBuildFactor.FACTOR_QUERY, conn);
            MySQLPreparedQuery qBill = new MySQLPreparedQuery("SELECT b.bill_num, b.creation_date "
                    + "FROM bill_bill as b "
                    + "WHERE b.client_tank_id = ?1 AND b.bill_span_id = " + spanId + " "
                    + "ORDER BY b.id ASC LIMIT 1 ", conn);

            for (BillBuilding build : buildings) {
                BigDecimal buildFactor = BillBuildFactor.getFactor(spanId, build.id, factorQ);
                MySQLQuery qClients = new MySQLQuery("SELECT num_install, first_name, COALESCE(c.last_name,''), (r.reading - r.last_reading), c.id "
                        + " FROM bill_client_tank c "
                        + " INNER join bill_reading r on r.client_tank_id = c.id and r.span_id = " + spanId + " "
                        + " WHERE c.building_id = " + build.id + " ");
                Object[][] clientsData = qClients.getRecords(conn);
                if (clientsData.length > 0) {
                    Table bTable = new Table(build.oldId + " " + build.name + " " + build.address + "  Clientes " + clientsData.length);
                    List<Object[]> data = new ArrayList<>();
                    bTable.setColumns(tb.getColumns());
                    for (Object[] clientsRow : clientsData) {
                        Integer clientId = (Integer) clientsRow[4];
                        Integer listId = BillPriceSpan.getListId(conn, spanId, clientId);
                        //Integer listId = 1;
                        if (listId != null) {
                            BigDecimal m3Consu = (BigDecimal) (clientsRow[3] != null ? clientsRow[3] : BigDecimal.ZERO);
                            BigDecimal valCons = span.getConsVal(m3Consu, buildFactor, prices.get(MySQLQuery.getAsInteger(listId)));
                            String client = clientsRow[1] + " " + clientsRow[2];
                            Object[] row = new Object[11];
                            row[0] = clientsRow[0]; //Núm Instal
                            row[1] = client;//Cliente
                            row[2] = buildFactor;//Factor
                            qBill.setParameter(1, clientId);
                            Object[] lbill = qBill.getRecord();
                            if (lbill != null && lbill.length > 0) {
                                row[3] = lbill[0];//Cupón
                                row[4] = lbill[1];//Fecha
                            } else {
                                row[3] = null;//Cupón
                                row[4] = null;//Fecha
                            }

                            row[5] = m3Consu;//Cons. m3
                            row[6] = m3Consu.multiply(span.getM3ToGalKte());//Cons. gal
                            row[7] = m3Consu.multiply(span.getM3ToGalKte()).multiply(span.galToKgKte);//Cons. kg
                            row[8] = m3Consu.multiply(span.getM3ToGalKte()).multiply(buildFactor);//gal build                            
                            row[9] = m3Consu.multiply(span.getM3ToGalKte()).multiply(span.galToKgKte).multiply(buildFactor);//kg * fac 

                            row[10] = valCons;//Valor
                            if (!sysCfg.skipMinCons || valCons.compareTo(span.minConsValue) >= 0) {
                                data.add(row);
                            }
                        }
                    }
                    bTable.setSummaryRow(new SummaryRow("Totales", 5));
                    if (!data.isEmpty()) {
                        bTable.setData(data);
                        rep.getTables().add(bTable);
                    }
                }
            }
            useDefault(conn);
            return createResponse(rep.write(conn), "sui.xls", true);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/closeReadings")
    public Response closeReadings(@QueryParam("spanId") int spanId) throws Exception {
        try (Connection billConn = getConnection(); Connection sigmaConn = getConnection()) {
            getSession(sigmaConn);
            useBillInstance(billConn);
            BillInstance inst = getBillInstance();
            if (inst.isNetInstance()) {
                throw new Exception("Opción no disponible en facturación de redes");
            }
            if (inst.siteBilling) {
                throw new Exception("Opción no disponible en facturación en sitio");
            }

            BillSpan cons = BillSpan.getByState("cons", billConn);

            if (cons.id != spanId) {
                throw new Exception("Unicamente se puede hacer para el periodo en consumo");
            }

            if (cons.readingsClosed) {
                throw new Exception("Ya se han cerrado las lecturas");
            }

            BillReadingsCheck chk = BillReadingController.checkReadings(cons.id, inst, billConn, sigmaConn);
            if (chk.clients != chk.clientReads) {
                throw new Exception("Las lecturas no están completas");
            }
            cons.readingsClosed = true;
            useBillInstance(billConn);
            cons.update(billConn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/setInterests")
    public Response setInterests(BillSpan span) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillSpan reca = BillSpan.getByState("reca", conn);

            if (reca.id != span.id) {
                throw new Exception("Unicamente se puede hacer para el periodo en recaudo");
            }

            if (!BillSpan.isInterOpen(reca.id, getBillInstance(), conn)) {
                throw new Exception("No se pueden modificar los intereses");
            }

            reca.interes = span.interes;
            reca.interesSrv = span.interesSrv;
            reca.readingsClosed = true;
            reca.interestSet = true;
            reca.update(conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("closeSpan")
    public Response closeSpan() throws Exception {
        try {
            final Connection billConn = getConnection();
            final Connection sigmaConn = getConnection();

            final SessionLogin sess = getSession(billConn);
            final BillInstance inst = getBillInstance();
            final BillCloseRequest r = new BillCloseRequest();
            useBillInstance(billConn);

            r.begDt = new Date();
            r.empId = sess.employeeId;
            r.instId = getBillInstId();
            r.status = "running";
            r.insert(sigmaConn);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Locking.lock(inst.id);
                        try {
                            BillSpanController.closeSpan(inst, r, sess, billConn, sigmaConn);
                            r.endDt = new Date();
                            r.setStaus("success", "Terminado con éxito");
                            inst.minPaymentDate = new Date();
                            inst.update(sigmaConn);
                        } catch (Exception ex) {
                            r.setException(ex);
                        }
                    } catch (Exception ex) {
                        try {
                            r.setException(ex);
                        } catch (Exception ex1) {
                            Logger.getLogger(BillSpanApi.class.getName()).log(Level.SEVERE, null, ex1);
                        }
                    } finally {
                        Locking.unlock(inst.id);
                        MySQLSelect.tryClose(sigmaConn);
                        MySQLSelect.tryClose(billConn);
                    }
                }
            }) {
            }.start();
            return createResponse(r);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("advanceSpan")
    public Response advanceSpan() throws Exception {
        try (Connection billConn = getConnection()) {
            billConn.setAutoCommit(false);
            getSession(billConn);
            BillInstance inst = getBillInstance();
            useBillInstance(billConn);

            BillSpan reca = BillSpan.getByState("reca", billConn);
            BillSpan cons = BillSpan.getByState("cons", billConn);

            if (!getBillInstance().siteBilling) {
                throw new Exception("Opción no disponible en facturación global");
            }

            if (inst.isNetInstance()) {
                if ((cons.cuf == null || cons.cuf.compareTo(BigDecimal.ZERO) == 0 || cons.dAomR == null || cons.dAomR.compareTo(BigDecimal.ZERO) == 0) || !cons.paramsDone) {
                    throw new Exception("Aún no se ha parametrizado el periodo");
                }
            }

            if (new MySQLQuery("SELECT COUNT(*)>0 FROM bill_measure WHERE taken_dt IS NULL AND span_id = ?1").setParam(1, cons.id).getAsBoolean(billConn)) {
                throw new Exception("Hay mediciones pendientes.");
            }

            int openClients = new MySQLQuery("SELECT COUNT(*) FROM bill_client_tank t WHERE t.span_closed = 0 and t.active").getAsInteger(billConn);
            if (openClients > 0) {
                throw new Exception("Aún " + (openClients > 1
                        ? "existen " + openClients + " clientes" : "existe " + openClients + " cliente") + " sin facturar para este periodo");
            }

            reca.closeFirstId = new MySQLQuery("SELECT max(id) FROM bill_transaction").getAsInteger(billConn);
            reca.update(billConn);

            try (Connection sigmaConn = getConnection()) {
                BillSpanController.advanceSpan(reca, cons, inst, billConn, sigmaConn);
            }
            new MySQLQuery("UPDATE bill_client_tank SET span_closed = 0").executeUpdate(billConn);

            billConn.commit();
            return Response.ok().build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/openReadingsCapture")
    public Response openReadingsCapture(@QueryParam("marketId") int marketId) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);
            List<BillInstance> insts = BillInstance.getBillingByMarket(marketId, conn);
            BillSpan[] spans = new BillSpan[insts.size()];
            boolean esTarifa=new MySQLQuery("SELECT tarifa_plena FROM sigma.bill_market WHERE id=?1").setParam(1, marketId).getAsBoolean(conn);
            for (int i = 0; i < insts.size(); i++) {
                BillInstance inst = insts.get(i);
                useBillInstance(inst.id, conn);
                BillSpan reca = BillSpan.getByState("reca", conn);
                BillSpan cons = BillSpan.getByState("cons", conn);

                if (new MySQLQuery("SELECT COUNT(*) = 0 FROM bill_client_tank WHERE active").getAsBoolean(conn)) {
                    throw new Exception("No hay clientes activos en " + inst.name + ".");
                }

                if (!cons.costsSet) {
                    throw new Exception("Debe definir los parámetros de causación " + inst.name + ".");
                }
                if(esTarifa && cons.cEq1TP==null){
                    throw new Exception("Debe calcular los parámetros de tarifa plena instancia: " + inst.name + ".");
            }
                if (!reca.interestSet) {
                    throw new Exception("Debe definir los intereses en " + inst.name + ".");
                }
                if (!inst.siteBilling) {
                    throw new Exception("Opción no disponible en facturación global en " + inst.name + ".");
                }
                
                int closeClient = new MySQLQuery("SELECT COUNT(*) FROM bill_client_tank t WHERE t.span_closed = 1").getAsInteger(conn);
                if (closeClient > 0) {
                    throw new Exception("El periodo de facturacion aún esta en proceso de toma de lecturas en " + inst.name + ".");
                }
                spans[i] = cons;
            }
            /*
            Los parámetros de las instancias dentro del mismo mercado debe ser iguales.
            Se toma una instancia cualquiera y se compara a todas contra esa porque si A=B y A=C, C=B sin necesidad de comprobarlo.
            No es necesario probar todas contra todas.*/
            for (int i = 1; i < spans.length; i++) {
                BillSpan span = spans[i];
                if (span.adjust != spans[0].adjust) {
                    throw new Exception("Hay diferencias en ajuste a la unidad entre " + insts.get(0).name + " y " + insts.get(i).name);
                }
                if (span.reconnect.compareTo(spans[0].reconnect) != 0) {
                    throw new Exception("Hay diferencias en reconexión entre " + insts.get(0).name + " y " + insts.get(i).name);
                }
                //el factor de corrección puede ser difernte entre ciudades
                if (span.contribR.compareTo(spans[0].contribR) != 0) {
                    throw new Exception("Hay diferencias en % de contribución residencial entre " + insts.get(0).name + " y " + insts.get(i).name);
                }
                if (span.contribNr.compareTo(spans[0].contribNr) != 0) {
                    throw new Exception("Hay diferencias en % de contribución residencial entre " + insts.get(0).name + " y " + insts.get(i).name);
                }
                if (span.consMonth.compareTo(spans[0].consMonth) != 0) {
                    throw new Exception("Hay diferencias en el mes de consumo entre " + insts.get(0).name + " y " + insts.get(i).name);
                }
                if (span.vitalCons.compareTo(spans[0].vitalCons) != 0) {
                    throw new Exception("Hay diferencias en el consumo de subsistencia entre " + insts.get(0).name + " y " + insts.get(i).name);
                }
                if (span.p.compareTo(spans[0].p) != 0) {
                    throw new Exception("Hay diferencias en pérdidas máximas entre " + insts.get(0).name + " y " + insts.get(i).name);
                }
                if (span.fpc.compareTo(spans[0].fpc) != 0) {
                    throw new Exception("Hay diferencias en factor poder calorífico entre " + insts.get(0).name + " y " + insts.get(i).name);
                }
                if (span.fpc.compareTo(spans[0].fpc) != 0) {
                    throw new Exception("Hay diferencias en factor poder calorífico entre " + insts.get(0).name + " y " + insts.get(i).name);
                }
                if (span.pms.compareTo(spans[0].pms) != 0) {
                    throw new Exception("Hay diferencias en costo de compras entre " + insts.get(0).name + " y " + insts.get(i).name);
                }
                if (span.cglp.compareTo(spans[0].cglp) != 0) {
                    throw new Exception("Hay diferencias en cnt de glp inyectado entre " + insts.get(0).name + " y " + insts.get(i).name);
                }
                if (span.t.compareTo(spans[0].t) != 0) {
                    throw new Exception("Hay diferencias en costo de traslado por ductos entre " + insts.get(0).name + " y " + insts.get(i).name);
                }
                if (span.tv.compareTo(spans[0].tv) != 0) {
                    throw new Exception("Hay diferencias en costo de transporte terrestre entre " + insts.get(0).name + " y " + insts.get(i).name);
                }
                if (span.fv.compareTo(spans[0].fv) != 0) {
                    throw new Exception("Hay diferencias en factor volumétrico entre " + insts.get(0).name + " y " + insts.get(i).name);
                }
                if (span.power.compareTo(spans[0].power) != 0) {
                    throw new Exception("Hay diferencias en poder calorífico entre " + insts.get(0).name + " y " + insts.get(i).name);
                }
                if (span.finalTarif1.compareTo(spans[0].finalTarif1) != 0) {
                    throw new Exception("Hay diferencias en tarifa estrato 1 entre " + insts.get(0).name + " y " + insts.get(i).name);
                }
                if (span.finalTarif2.compareTo(spans[0].finalTarif2) != 0) {
                    throw new Exception("Hay diferencias en tarifa estrato 2 entre " + insts.get(0).name + " y " + insts.get(i).name);
                }
                if (span.rawTarif1.compareTo(spans[0].rawTarif1) != 0) {
                    throw new Exception("Hay diferencias en tarifa estrato 1 entre " + insts.get(0).name + " y " + insts.get(i).name);
                }
                if (span.rawTarif2.compareTo(spans[0].rawTarif2) != 0) {
                    throw new Exception("Hay diferencias en tarifa estrato 2 entre " + insts.get(0).name + " y " + insts.get(i).name);
                }
            }

            for (int i = 0; i < insts.size(); i++) {
                BillInstance inst = insts.get(i);
                useBillInstance(inst.id, conn);
                spans[i].paramsDone = true;
                spans[i].update(conn);

                if (new MySQLQuery("SELECT COUNT(*) = 0 FROM bill_measure WHERE span_id = " + spans[i].id + "").getAsBoolean(conn)) {
                    int cnt = new MySQLQuery("SELECT COUNT(*) FROM bill_client_tank WHERE active").getAsInteger(conn);
                    cnt = (int) (cnt * inst.ipliIoRate.doubleValue() / 100d);
                    new MySQLQuery("INSERT INTO bill_measure (client_id, span_id, odorant_id, pressure, odorant_amount) (SELECT id, " + spans[i].id + ", " + inst.odorantId + ", NULL, NULL FROM bill_client_tank WHERE active ORDER BY RAND() LIMIT " + cnt + ") ").executeUpdate(conn);
                }
                inst.minPaymentDate = new Date();
                inst.useDefault(conn);
                inst.update(conn);
            }
            return Response.ok().build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    public BillSpan[] getInitializationSimulation(StartBillingRequestDto req, Connection billConn) throws Exception {
        BillSpan[] rta = new BillSpan[3];
        useBillInstance(req.instId, billConn);
        BillInstance inst = BillingServlet.getInst(req.instId);
        if (inst.lowerCriticalReadRateNr == null || inst.upperCriticalReadRateNr == null
                || inst.lowerCriticalReadRateR == null || inst.upperCriticalReadRateR == null) {
            throw new Exception("Debe definir los niveles de crítica.");
        }

        if (new MySQLQuery("SELECT COUNT(*) > 0 FROM bill_span").getAsBoolean(billConn)) {
            throw new Exception("La instancia ya está facturando periodicamente.");
        }

        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(req.endDate);

        if (gc.get(GregorianCalendar.DAY_OF_MONTH) > 28 || gc.get(GregorianCalendar.DAY_OF_MONTH) < 1) {
            throw new Exception("El día debe estar entre 1 y 28");
        }

        gc.setTime(req.endDate);
        if (gc.get(GregorianCalendar.DAY_OF_MONTH) > 28 || gc.get(GregorianCalendar.DAY_OF_MONTH) < 1) {
            throw new Exception("El límite debe estar entre 1 y 28");
        }

        if (req.endDate.compareTo(req.limitDate) > 0) {
            throw new Exception("El limite debe ser posterior al recaudo");
        }

        BillSpan span = new BillSpan();
        if (req.testData) {
            span.adjust = 50;
            span.reconnect = new BigDecimal(15000);
            span.fadj = new BigDecimal("1.1");
            span.contribR = new BigDecimal("20");
            span.contribNr = new BigDecimal("8.9");
            span.vitalCons = new BigDecimal("7.26");
            span.p = new BigDecimal("3");
            span.fpc = new BigDecimal("1.11");
            span.pms = new BigDecimal("850");
            span.cglp = new BigDecimal(1);
            span.t = new BigDecimal(0);
            span.tv = new BigDecimal(700);
            span.fv = new BigDecimal("2.11");
            span.power = new BigDecimal("2862");
        } else {
            span.reconnect = BigDecimal.ZERO;
            span.power = BigDecimal.ZERO;
        }
        span.fixedCharge = BigDecimal.ZERO;
        span.interes = BigDecimal.ZERO;
        span.interesSrv = BigDecimal.ZERO;
        span.divisor = BigDecimal.ZERO;
        span.galToKgKte = BigDecimal.ZERO;

        span.priceType = "gal";
        span.paramsDone = false;

        gc.setTime(req.endDate);
        span.endDate = gc.getTime();
        gc.add(GregorianCalendar.DAY_OF_MONTH, 1);
        gc.add(GregorianCalendar.MONTH, -1);
        span.beginDate = gc.getTime();

        gc.setTime(req.limitDate);
        span.limitDate = gc.getTime();

        if (req.suspDate != null) {
            gc.setTime(req.suspDate);
            span.suspDate = gc.getTime();
        }
        span.state = "cons";
        span.id = 3;
        BillSpan.setConsMonth(span);
        rta[2] = span.duplicate();

        gc.setTime(req.endDate);
        gc.add(GregorianCalendar.MONTH, -1);
        span.endDate = gc.getTime();
        gc.add(GregorianCalendar.DAY_OF_MONTH, 1);
        gc.add(GregorianCalendar.MONTH, -1);
        span.beginDate = gc.getTime();

        gc.setTime(req.limitDate);
        gc.add(GregorianCalendar.MONTH, -1);
        span.limitDate = gc.getTime();

        if (req.suspDate != null) {
            gc.setTime(req.suspDate);
            gc.add(GregorianCalendar.MONTH, -1);
            span.suspDate = gc.getTime();
        }
        span.state = "reca";
        span.id = 2;
        BillSpan.setConsMonth(span);
        rta[1] = span.duplicate();

        gc.setTime(req.endDate);
        gc.add(GregorianCalendar.MONTH, -2);
        span.endDate = gc.getTime();
        gc.add(GregorianCalendar.DAY_OF_MONTH, 1);
        gc.add(GregorianCalendar.MONTH, -1);
        span.beginDate = gc.getTime();

        gc.setTime(req.limitDate);
        gc.add(GregorianCalendar.MONTH, -2);
        span.limitDate = gc.getTime();

        if (req.suspDate != null) {
            gc.setTime(req.suspDate);
            gc.add(GregorianCalendar.MONTH, -2);
            span.suspDate = gc.getTime();
        }
        span.state = "cart";
        span.id = 1;
        BillSpan.setConsMonth(span);
        rta[0] = span.duplicate();
        return rta;
    }

    @POST
    @Path("initializationSimulation")
    public Response initializationSimulation(StartBillingRequestDto req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillSpan[] spans = getInitializationSimulation(req, conn);
            return createResponse(spans);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("initializePeriodicBilling")
    public Response initializePeriodicBilling(StartBillingRequestDto req) {
        try (Connection conn = getConnection()) {
            try {
                getSession(conn);
                BillInstance inst = new BillInstance().select(req.instId, conn);
                conn.setAutoCommit(false);
                BillSpan[] spans = getInitializationSimulation(req, conn);
                for (BillSpan span : spans) {
                    span.insertWithId(conn);
                }
                useDefault(conn);
                SysCrudLog.updated(this, inst, "Se inició la facturación periódica", conn);
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

    @POST
    @Path("closeClient")
    public Response closeClient(BillReading obj) throws Exception {
        try (Connection billConn = getConnection(); Connection sigmaConn = getConnection()) {
            try {
                billConn.setAutoCommit(false);
                sigmaConn.setAutoCommit(false);
                SysCfg sysCfg = SysCfg.select(sigmaConn);
                SessionLogin sess = getSession(billConn);
                useBillInstance(billConn);
                BillCfg cfg = new BillCfg().select(1, billConn);

                BillInstance inst = getBillInstance();
                if (!getBillInstance().siteBilling) {
                    throw new Exception("Opción no disponible en facturación global");
                }

                BillClientTank client = new BillClientTank().select(obj.clientTankId, billConn);
                if (client.spanClosed) {
                    throw new Exception("El cliente ya fue facturado en este periodo, use la opción de reimpresión.");
                }

                BillSpan consSpan = BillSpan.getByClient("cons", client.id, inst, billConn);
                obj.spanId = consSpan.id;

                if (!consSpan.paramsDone) {
                    throw new Exception("Aún no se ha parametrizado el periodo.");
                }

                //INSERTAR LECTURA
                if (obj.reading != null) {
                    Integer rId = new MySQLQuery("SELECT id FROM bill_reading WHERE client_tank_id = ?1 AND span_id = ?2").setParam(1, obj.clientTankId).setParam(2, obj.spanId).getAsInteger(billConn);
                    obj.empId = sess.employeeId;
                    if (rId != null) {
                        obj.id = rId;
                        obj.update(billConn);
                    } else {
                        obj.insert(billConn);
                    }
                }

                if (obj.criticalReading != null) {
                    OrdPqrRequest.createCritical(obj, inst, billConn, sigmaConn);
                }

                OrdPqrRequest.createZeroOrSame(obj, inst, billConn, sigmaConn);

                //CERRAR PERIODO DEL USUARIO
                BillSpan reca = BillSpan.getByState("reca", billConn);
                BillSpanController.closeUsers(obj.clientTankId, sess, inst, reca, null, billConn, sigmaConn);

                //GENERAR CUPON DE ABONO TOTAL
                File f = File.createTempFile("bill", ".zpl");
                BillWriter writer = BillWriter.getCurrentZplWriter(getBillInstance(), sysCfg, cfg, billConn, f, true);
                GetBills.create(obj.clientTankId, getBillInstance(), sysCfg, null, sigmaConn, billConn, sess, writer);
                writer.endDocument();

                billConn.commit();
                sigmaConn.commit();

                return createResponse(f, "bill.zpl");
            } catch (Exception ex) {
                billConn.rollback();
                sigmaConn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
