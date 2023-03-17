package api.bill.api;

import api.BaseAPI;
import api.GridResult;
import api.MultiPartRequest;
import api.MySQLCol;
import api.Params;
import api.bill.app.SetClientReads;
import api.bill.model.BillBuildFactor;
import api.bill.model.BillBuilding;
import api.bill.model.BillClientFactor;
import api.bill.model.BillClientTank;
import api.bill.model.BillInstance;
import api.bill.model.BillPriceSpan;
import api.bill.model.BillReading;
import api.bill.model.BillReadingFault;
import api.bill.model.BillSpan;
import api.bill.model.dto.BillReadingsCheck;
import api.bill.model.dto.ClientWarning;
import api.ord.model.OrdPqrRequest;
import api.sys.model.Bfile;
import api.sys.model.SysCfg;
import api.sys.model.SysCrudLog;
import controller.billing.BillReadingController;
import controller.billing.BillSpanController;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLPreparedQuery;
import utilities.MySQLQuery;
import utilities.Reports;
import utilities.apiClient.BigDecimalResponse;
import utilities.apiClient.DateResponse;
import utilities.cast;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.SummaryRow;
import utilities.mysqlReport.Table;
import web.billing.BillingServlet;
import web.fileManager;

@Path("/billReading")
public class BillReadingApi extends BaseAPI {

    @POST
    public Response insert(BillReading obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            BillReadingFault f = null;
            if (obj.faultId != null) {
                f = new BillReadingFault().select(obj.faultId, conn);
            }
            useBillInstance(conn);

            if (f != null) {
                switch (f.consType) {
                    case "zero":
                        obj.reading = obj.lastReading;
                        break;
                    case "avg":
                        obj.reading = obj.lastReading.add(BillReading.getSixMonthsAvg(obj.clientTankId, obj.spanId, conn));
                        break;
                    default:
                        throw new RuntimeException("Unknown cons type: " + f.consType);
                }
            }
            if (obj.reading.compareTo(obj.lastReading) < 0) {
                throw new Exception("La lectura actual debe ser mayor que la anterior.");
            }

            obj.empId = sl.employeeId;
            obj.insert(conn);
            useDefault(conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(BillReading obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillReadingFault f = null;
            if (obj.faultId != null) {
                f = new BillReadingFault().select(obj.faultId, conn);
            }
            useBillInstance(conn);
            BigDecimal last = obj.lastReading;
            BigDecimal reading = obj.reading;
            Integer faultId = obj.faultId;

            BillReading old = new BillReading().select(obj.id, conn);
            obj = new BillReading().select(obj.id, conn);

            if (f != null) {
                switch (f.consType) {
                    case "zero":
                        obj.reading = obj.lastReading;
                        break;
                    case "avg":
                        obj.reading = obj.lastReading.add(BillReading.getSixMonthsAvg(obj.clientTankId, obj.spanId, conn));
                        break;
                    default:
                        throw new RuntimeException("Unknown cons type: " + f.consType);
                }
                obj.faultId = faultId;
            } else {
                obj.reading = reading;
                obj.faultId = null;
            }
            obj.lastReading = last;

            if (obj.reading.compareTo(obj.lastReading) < 0) {
                throw new Exception("La lectura actual debe ser mayor que la anterior.");
            }

            obj.update(conn);
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
            BillReading obj = new BillReading().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    
    @GET
    @Path("/lastReadingDateByClient")
    public Response getLastReadingDateByClient(@QueryParam("clientId") int clientId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            Date d = BillReading.getLastReadingDateByClient(clientId, conn);           
            return createResponse(new DateResponse(d));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    
    @GET
    @Path("byClientSpan")
    public Response get(@QueryParam("clientId") int clientId, @QueryParam("spanId") int spanId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillReading r = new BillReading().select(new Params("client_tank_id", clientId).param("spanId", spanId), conn);
            return Response.ok(r).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            BillReading.delete(id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getReadingList")
    public Response getReadingList(@QueryParam("spanId") int spanId, @QueryParam("buildId") Integer buildId, @QueryParam("instanceId") Integer instanceId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(instanceId, conn);
            BillInstance inst = BillingServlet.getInst(instanceId);
            Object[][] readData = new MySQLQuery("SELECT "
                    + "c.id, "
                    + "act.id, "
                    + "COALESCE(act.last_reading, m.start_reading, ant.reading, 0), "//anterior
                    + "TRIM(CONCAT(c.first_name, ' ' , IFNULL(c.last_name, ''))), "
                    + (inst.type.equals("tank") ? " c.apartment, " : " c.address, ")
                    + "c.num_install, "
                    + "COALESCE(act.last_reading, m.start_reading, ant.reading, 0), "//7 anterior
                    + "IF(act.fault_id IS NULL, act.reading, NULL), "
                    + "act.reading - act.last_reading, "
                    + "null, "//avg
                    + "f.name "
                    + "FROM "
                    + "bill_client_tank c "
                    + "LEFT JOIN bill_reading ant ON ant.client_tank_id = c.id AND ant.span_id = " + (spanId - 1) + " "
                    + "LEFT JOIN bill_reading act ON act.client_tank_id = c.id AND act.span_id = " + spanId + " "
                    + "LEFT JOIN bill_meter AS m ON m.client_id = c.id AND m.start_span_id = " + spanId + " "
                    + "LEFT JOIN sigma.bill_reading_fault f ON f.id = act.fault_id "
                    + "where  "
                    + "c.active= 1 "
                    + (inst.type.equals("tank") ? " AND c.building_id = " + buildId : "")
            ).getRecords(conn);

            for (Object[] row : readData) {
                row[9] = BillReading.getSixMonthsAvg(cast.asInt(row[0]), spanId, conn);
            }

            GridResult r = new GridResult();
            r.sortType = GridResult.SORT_ASC;
            r.sortColIndex = 2;
            r.data = readData;
            r.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 170, "Cliente"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 110, inst.isNetInstance() ? "Dirección" : "Apartamento"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 110, inst.isNetInstance() ? "Código" : "Instalación"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 90, "Anterior"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 90, "Actual"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 80, "Consumo"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 80, "Promedio"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 170, "Novedad")
            };
            return createResponse(r);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/checkReadings")
    public Response checkReadings(@QueryParam("spanId") int spanId, @QueryParam("instanceId") Integer instanceId) {
        try (Connection billConn = getConnection(); Connection sigmaConn = getConnection()) {
            getSession(sigmaConn);
            BillInstance bi = new BillInstance().select(instanceId, billConn);
            useBillInstance(instanceId, billConn);
            BillReadingsCheck res = BillReadingController.checkReadings(spanId, bi, billConn, sigmaConn);
            return createResponse(res);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/consumBuildings")
    public Response getConsumBuildings(@QueryParam("spanId") int spanId) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillInstance bi = getBillInstance();
            if (bi.type.equals("net")) {
                throw new Exception("Opción no disponible en facturación de redes");
            }
            SysCfg sysCfg = SysCfg.select(conn);
            useBillInstance(conn);
            BillSpan sp = new BillSpan().select(spanId, conn);
            SimpleDateFormat shortDateFormat = new SimpleDateFormat("MMMM yyyy");
            String periodo = "Consumos de " + shortDateFormat.format(sp.consMonth);
            MySQLReport rep = new MySQLReport("Consumo Mensual por Edificios - " + bi.name, periodo, "Consumos", now(conn));
            rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
            rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.000"));//1
            rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$ #,##0.000"));//2
            rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, " #0.00"));//3
            rep.setVerticalFreeze(5);
            rep.setZoomFactor(80);

            List<BillBuilding> buildings = BillBuilding.getAll(conn);
            Map<Integer, BigDecimal> prices = BillPriceSpan.getPricesMap(conn, spanId);
            MySQLPreparedQuery factorQ = BillBuildFactor.getFactorQuery(conn);
            MySQLPreparedQuery clientFactorQ = BillClientFactor.getFactorQuery(conn);

            MySQLPreparedQuery qClients = new MySQLPreparedQuery("SELECT (r.reading - r.last_reading), c.id "
                    + " FROM bill_client_tank c "
                    + " INNER JOIN bill_reading r ON r.client_tank_id = c.id and r.span_id = " + spanId + " "
                    + " WHERE c.building_id = ?1", conn);

            List<Object[]> data = new ArrayList<>();
            for (BillBuilding build : buildings) {
                BigDecimal buildFac = BillBuildFactor.getFactor(spanId, build.id, factorQ);
                BigDecimal buildM3Cons = BigDecimal.ZERO;
                BigDecimal buildVal = BigDecimal.ZERO;
                qClients.setParameter(1, build.id);
                Object[][] clientsData = qClients.getRecords();
                if (clientsData.length > 0) {
                    for (Object[] clientsRow : clientsData) {
                        Integer clientId = (Integer) clientsRow[1];
                        BigDecimal clientFac = BillClientFactor.getFactor(spanId, clientId, clientFactorQ);
                        Integer listId = BillPriceSpan.getListId(conn, spanId, clientId);
                        if (listId != null) {
                            BigDecimal usrM3Cons = (BigDecimal) (clientsRow[0] != null ? clientsRow[0] : BigDecimal.ZERO);
                            BigDecimal consVal = sp.getConsVal(usrM3Cons, (clientFac == BigDecimal.ZERO ? buildFac : clientFac), prices.get(MySQLQuery.getAsInteger(listId)));
                            if (!sysCfg.skipMinCons || consVal.compareTo(sp.minConsValue) >= 0) {
                                buildVal = buildVal.add(consVal);
                                buildM3Cons = buildM3Cons.add(usrM3Cons);
                            }
                        }
                    }

                    Object[] row = new Object[10];
                    row[0] = BillSpanController.zeroFill((build.oldId).toString(), 3);//Código
                    row[1] = build.name != null ? build.name : "";//Edificio
                    row[2] = build.address != null ? build.address : "";//Dirección
                    row[3] = buildFac;//Factor
                    row[4] = buildM3Cons;//m3
                    row[5] = buildM3Cons.multiply(sp.getM3ToGalKte());//gal
                    row[6] = buildM3Cons.multiply(sp.getM3ToGalKte()).multiply(sp.galToKgKte);//kg
                    row[7] = buildM3Cons.multiply(sp.getM3ToGalKte()).multiply(buildFac);//gal * fac
                    row[8] = buildM3Cons.multiply(sp.getM3ToGalKte()).multiply(sp.galToKgKte).multiply(buildFac);//kg * fac 
                    row[9] = buildVal;//Valor
                    data.add(row);
                }
            }

            if (data.isEmpty()) {
                throw new Exception("No se hallaron datos");
            } else {
                Table tb = new Table("Consumos");
                tb.getColumns().add(new Column("Código", 10, 0));
                tb.getColumns().add(new Column("Edificio", 35, 0));
                tb.getColumns().add(new Column("Dirección", 35, 0));
                tb.getColumns().add(new Column("Factor", 7, 3));
                tb.getColumns().add(new Column("Cons. m3", 12, 1));
                tb.getColumns().add(new Column("Cons. gal", 12, 1));
                tb.getColumns().add(new Column("Cons. kg", 12, 1));
                tb.getColumns().add(new Column("Cons. gal * Fact", 18, 1));
                tb.getColumns().add(new Column("Cons. kg * Fact", 18, 1));
                tb.getColumns().add(new Column("Valor", 18, 2));
                tb.setSummaryRow(new SummaryRow("Totales", 4));
                tb.setData(data);
                rep.getTables().add(tb);
            }
            useDefault(conn);
            return createResponse(rep.write(conn), "report.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("buildingMonthlyCons")
    public Response getBuildingMonthlyCons(@PathParam("year") int year, @PathParam("month") int month, @PathParam("buildId") int buildId, @PathParam("instId") int instId) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);
            SysCfg sysCfg = SysCfg.select(conn);
            BillInstance bi = BillingServlet.getInst(instId);
            useBillInstance(instId, conn);
            return createResponse(new BigDecimalResponse(BillReading.getBuildingMonthlyCons(year, month, buildId, bi, sysCfg, conn)));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("categMonthlyCons")
    public Response getCategMonthlyCons(@PathParam("year") int year, @PathParam("month") int month, @PathParam("categId") int categId, @PathParam("instId") int instId) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);
            SysCfg sysCfg = SysCfg.select(conn);
            BillInstance bi = BillingServlet.getInst(instId);
            useBillInstance(instId, conn);
            return createResponse(new BigDecimalResponse(BillReading.getCategMonthlyCons(year, month, categId, bi, sysCfg, conn)));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("consumCostsByBuilds")
    public Response getConsumCostsByBuilds(@QueryParam("spanId") int spanId, @QueryParam("instId") int instId) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);
            SysCfg sysCfg = SysCfg.select(conn);
            useBillInstance(instId, conn);
            return createResponse(BillReading.getConsumCostsByBuilds(spanId, instId, sysCfg, conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("readsByCity")
    public Response getReadsByCity(@QueryParam("instId") Integer justInstId, @QueryParam("year") int year, @QueryParam("empIds") String empIds) {
        String[] months = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        try (Connection conn = getConnection()) {

            MySQLQuery mq = new MySQLQuery("SELECT ct.id, ct.name "
                    + "FROM bill_instance ct "
                    + (justInstId != null ? "WHERE ct.id= " + justInstId : " ")
            );
            Object[][] db = mq.getRecords(conn);
            Object[][] dataT = new Object[db.length][13]; // ciudad + 12 meses 

            for (int i = 0; i < db.length; i++) {
                int instId = MySQLQuery.getAsInteger(db[i][0]);
                String instName = MySQLQuery.getAsString(db[i][1]);
                dataT[i][0] = instName;
                for (int j = 0; j < months.length; j++) {
                    useBillInstance(instId, conn);
                    BillSpan span = BillSpan.getByMonth(year, j + 1, BillingServlet.getInst(instId), conn);
                    if (span != null) {
                        dataT[i][j + 1] = new MySQLQuery(("SELECT COUNT(*) "
                                + "FROM bill_reading_bk r "
                                + "INNER JOIN bill_client_tank ct ON ct.id = r.client_tank_id "
                                + "WHERE "
                                + "ct.active  AND "
                                + (empIds != null && !empIds.isEmpty() ? " r.emp_id IN  (" + empIds + ") AND " : "  ")
                                + "r.span_id = " + span.id)).getAsInteger(conn);
                        if (((Integer) dataT[i][j + 1]) == 0) {
                            dataT[i][j + 1] = new MySQLQuery(("SELECT COUNT(*) "
                                    + "FROM bill_reading r "
                                    + "INNER JOIN bill_client_tank ct ON ct.id = r.client_tank_id "
                                    + "WHERE ct.active  AND "
                                    + (empIds != null && !empIds.isEmpty() ? " r.emp_id IN  (" + empIds + ") AND " : "  ")
                                    + "r.span_id = " + span.id)).getAsInteger(conn);
                        }
                    } else {
                        dataT[i][j + 1] = 0;
                    }
                }
            }

            MySQLReport rep = new MySQLReport("Lecturas por Ciudad", "", "reads_city", now(conn));
            List<String> subt = new ArrayList<>();
            subt.add("Instancia: " + (justInstId != null ? BillingServlet.getInstName(justInstId) + "." : "Todas"));
            rep.setSubTitles(subt);
            rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
            rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//4
            rep.getFormats().get(0).setWrap(true);
            rep.setZoomFactor(85);
            Table tblReads = new Table("Lecturas");
            tblReads.getColumns().add(new Column("Ciudad", 15, 0));
            for (String month : months) {
                tblReads.getColumns().add(new Column(month, 12, 1));
            }
            tblReads.setSummaryRow(new SummaryRow("Total", 1));
            tblReads.setData(dataT);
            if (tblReads.getData().length > 0) {
                rep.getTables().add(tblReads);
            }
            return createResponse(rep.write(useDefault(conn)), "lecturas.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/readingHistory")
    public Response getReadingHistory(@QueryParam("sigmaClientId") int sigmaClient, @QueryParam("spanId") int spanId) {
        try (Connection billConn = getConnection(); Connection sigmaConn = getConnection()) {
            getSession(sigmaConn);
            useBillInstance(billConn);

            int clientId = new MySQLQuery("SELECT t.id "
                    + " FROM bill_client_tank t "
                    + " INNER JOIN sigma.ord_pqr_client_tank c ON c.mirror_id = t.id "
                    + " WHERE c.id = ?1 AND c.bill_instance_id = ?2")
                    .setParam(1, sigmaClient).setParam(2, getBillInstId()).getAsInteger(billConn);

            new MySQLQuery("SET lc_time_names = 'es_ES';").executeUpdate(billConn);

            GridResult gr = new GridResult();
            gr.data = new MySQLQuery("SELECT "
                    + " CONCAT(DATE_FORMAT(s.begin_date,\"%M/%d/%Y\") , ' - ', DATE_FORMAT(s.end_date,\"%M/%d/%Y\")), "
                    + " r.last_reading, r.reading, ROUND(r.reading - r.last_reading,3) "
                    + " FROM bill_reading r "
                    + " LEFT JOIN bill_reading_bk b ON b.client_tank_id = r.client_tank_id AND b.span_id BETWEEN ?2 AND ?3 "
                    + " INNER JOIN bill_span s ON s.id = r.span_id AND r.span_id BETWEEN ?2 AND ?3 "
                    + " WHERE r.client_tank_id = ?1 "
                    + " ORDER BY s.end_date ASC")
                    .setParam(1, clientId)
                    .setParam(2, spanId - 6)
                    .setParam(3, spanId)
                    .getRecords(billConn);

            gr.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_TEXT, 50, "Periodo"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_3, 25, "Lectura Anterior"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_3, 25, "Lectura Actual"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_3, 15, "Consumo", true)
            };

            gr.sortType = GridResult.SORT_NONE;

            return createResponse(gr);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/readingCritic")
    public Response readingCritic(@QueryParam("billClient") int billClientId) {
        try (Connection billConn = getConnection(); Connection sigmaConn = getConnection()) {
            getSession(sigmaConn);
            useBillInstance(billConn);

            Integer sigmaClientId = new MySQLQuery("SELECT c.id "
                    + " FROM bill_client_tank t "
                    + " INNER JOIN sigma.ord_pqr_client_tank c ON c.mirror_id = t.id "
                    + " WHERE t.id = ?1 AND c.bill_instance_id = ?2")
                    .setParam(1, billClientId).setParam(2, getBillInstId()).getAsInteger(billConn);

            new MySQLQuery("SET lc_time_names = 'es_ES';").executeUpdate(billConn);

            GridResult gr = new GridResult();
            Object[][] data = new MySQLQuery("SELECT s.id, c.id, s.span_id, s.notes, NULL as reading, "
                    + "s.creation_date, NULL AS period, "
                    + "IF(s.dt_cancel IS NOT NULL, 'Cancelado', IF(s.`type` IS NULL, 'Pendiente', IF(s.`type` = 'tank', 'PQR Fuga Estacionario', IF(s.`type` = 'other', 'PQR Reclamante', 'Asistencia Técnica')))) AS state "
                    + "FROM ord_pqr_request s "
                    + "INNER JOIN ord_pqr_client_tank c ON c.id = s.client_tank_id "
                    + "INNER JOIN employee e ON s.created_id = e.id "
                    + "INNER JOIN city ci ON ci.id = c.city_id "
                    + "WHERE s.client_id is NULL AND c.id = ?1 AND s.bill_req_type = 'reading'")
                    .setParam(1, sigmaClientId)
                    .getRecords(sigmaConn);

            for (Object[] row : data) {
                Object[] rowBill = new MySQLQuery("SELECT CONCAT(DATE_FORMAT(s.begin_date,\"%M/%d/%Y\") , ' - ', DATE_FORMAT(s.end_date,\"%M/%d/%Y\")), r.reading "
                        + "FROM bill_span s "
                        + "LEFT JOIN bill_reading r ON r.span_id = s.id AND r.client_tank_id = ?2 "
                        + "WHERE s.id = ?1 LIMIT 1").setParam(1, row[2]).setParam(2, billClientId).getRecord(billConn);

                if (rowBill != null && rowBill.length > 0) {
                    row[4] = rowBill[1];
                    row[6] = rowBill[0];
                }
            }

            gr.data = data;

            gr.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY), //req_id
                new MySQLCol(MySQLCol.TYPE_KEY), //client_tank_id
                new MySQLCol(MySQLCol.TYPE_KEY), //span_id
                new MySQLCol(MySQLCol.TYPE_KEY), //notes
                new MySQLCol(MySQLCol.TYPE_KEY), //reading
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 30, "Fecha"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 40, "Periodo"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Estado")
            };

            gr.sortType = GridResult.SORT_NONE;

            return createResponse(gr);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    //APP -----------------------------------------
    @POST
    @Path("/setReadingsApp")
    public synchronized Response setReadings(@Context HttpServletRequest request) {
        try (Connection gral = getConnection(); Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            useBillInstance(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);

            ByteArrayOutputStream baos;
            try (FileInputStream fis = new FileInputStream(mr.getFile().file); GZIPInputStream giz = new GZIPInputStream(fis)) {
                baos = new ByteArrayOutputStream();
                Reports.copy(giz, baos);
                baos.close();
            }

            String str = new String(baos.toByteArray());
            Integer version = Integer.valueOf(mr.params.get("version"));
            Integer spanId = Integer.valueOf(mr.params.get("spanId"));

            if (version.equals(1)) {
                SetClientReads.SetClientReadsV1(getBillInstance(), spanId, str, sl.employeeId, conn, gral);
            } else {
                throw new Exception("No implementado");
            }
            ClientWarning rta = new ClientWarning();
            rta.errors = new ArrayList<>();
            return Response.ok(rta).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/saveAndPrint")
    public Response saveAndPrint(@Context HttpServletRequest request) {
        try (Connection conn = getConnection()) {
            try {
                getSession(conn);
                conn.setAutoCommit(false);
                useBillInstance(conn);
                BillInstance bi = getBillInstance();
                if (!bi.siteBilling) {
                    throw new Exception("Operación no disponible en esta instancia, comuniquese con sistemas");
                }
                fileManager.PathInfo pi = new fileManager.PathInfo(conn);
                MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);
                List<BillReading> data = mr.getList(BillReading.class);

                for (BillReading obj : data) {
                    BillClientTank client = new BillClientTank().select(obj.clientTankId, conn);
                    System.out.println("xxxxxxxxx " + obj.clientTankId + " " + client.firstName);
                }
                return Response.ok().build();
//                
//                BillClientTank client = new BillClientTank().select(req.clientTankId, conn);
//
//                if (client.spanClosed) {//REIMPRESION
//                    return Response.ok().build();
//                } else {//IMPRESION
//                    BillSpan span = BillSpan.getByState("cons", conn);
//                    req.spanId = span.id;
//                    req.insert(conn);
//
//                    client.spanClosed = true;
//                    client.update(conn);
//                    conn.commit();
//                    return Response.ok().build();
//                }
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/readingPhoto")
    public Response setReadingPhoto(@Context HttpServletRequest request) {
        try (Connection sigmaConn = getConnection()) {
            SessionLogin sl = getSession(sigmaConn);
            fileManager.PathInfo pi = new fileManager.PathInfo(sigmaConn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);
            try {
                if (!mr.params.containsKey("instId")) {
                    throw new Exception("Debe actualizar");
                }

                int instId = MySQLQuery.getAsInteger(mr.params.get("instId"));
                int billClientId = MySQLQuery.getAsInteger(mr.params.get("clientId"));
                String fileName = mr.params.get("fileName");

                if (mr.getFile().file != null) {
                    BillInstance inst = BillingServlet.getInst(instId);

                    BillSpan span;
                    try (Connection instConn = getConnection()) {
                        inst.useInstance(instConn);
                        span = BillSpan.getByState("cons", instConn);
                    }

                    Integer sigmaClientId = new MySQLQuery("SELECT c.id FROM "
                            + "ord_pqr_client_tank c "
                            + "WHERE "
                            + "c.mirror_id = ?1 AND c.bill_instance_id = ?2"
                    ).setParam(1, billClientId).setParam(2, instId).getAsInteger(sigmaConn);

                    Integer reqId = new MySQLQuery("SELECT r.id FROM "
                            + "ord_pqr_request r "
                            + "WHERE "
                            + "r.client_tank_id = ?1 AND r.span_id = ?2 AND r.bill_req_type = 'reading'"
                    ).setParam(1, sigmaClientId).setParam(2, span.id).getAsInteger(sigmaConn);

                    if (reqId != null) {
                        Bfile uploaded = fileManager.upload(
                                sl.employeeId,
                                reqId, //ownerId
                                143,//ownerType, 
                                null, //tableName
                                fileName, //fileName,
                                "Foto de lectura", //desc, 
                                false, //unique
                                null,//shrinkType
                                pi, mr.getFile().file, sigmaConn
                        );
                        return createResponse(uploaded);
                    } else {
                        Bfile uploaded = fileManager.upload(
                                sl.employeeId,
                                sigmaClientId, //ownerId
                                144,//ownerType, 
                                null, //tableName
                                fileName, //fileName,
                                "Foto de lectura", //desc, 
                                false, //unique
                                null,//shrinkType
                                pi, mr.getFile().file, sigmaConn
                        );
                        return createResponse(uploaded);
                    }
                } else {
                    throw new Exception("El archivo que intentó adjuntar está vacío");
                }
            } finally {
                mr.deleteFiles();
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("photo")
    public Response rebuild(@QueryParam("id") int id) {
        try (Connection sigmaConn = getConnection()) {
            return createResponse(new fileManager.PathInfo(sigmaConn).getExistingFile(id), "img.jpg");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("rebuild")
    public Response rebuild() {
        //143 adjunto al request
        //144 adjunto al cliente
        try (Connection sigmaConn = getConnection(); Connection instConn = getConnection()) {
            /*Object[][] data = new MySQLQuery("SELECT f.id, f.owner_id, created FROM bfile f WHERE f.created > '2020-12-21 00:00:00' AND f.owner_type = 143;").getRecords(sigmaConn);
            for (Object[] fRow : data) {
                int bfileId = cast.asInt(fRow, 0);
                int reqId = cast.asInt(fRow, 1);
                Date d = cast.asDate(fRow, 2);

                Object[] reqRow = new MySQLQuery("SELECT c.id, c.bill_instance_id, c.mirror_id FROM "
                        + "ord_pqr_request r "
                        + "INNER JOIN ord_pqr_client_tank c ON c.id = r.client_tank_id "
                        + "WHERE r.id = ?1").setParam(1, reqId).getRecord(sigmaConn);

                Integer sigmaClientId = cast.asInt(reqRow, 0);
                Integer instId = cast.asInt(reqRow, 1);
                Integer billClientId = cast.asInt(reqRow, 2);

                BillInstance inst = BillingServlet.getInst(instId);
                inst.useInstance(instConn);

                Object[] readRow = new MySQLQuery("SELECT span_id, emp_id FROM bill_reading WHERE client_tank_id = ?1 ORDER BY span_id DESC LIMIT 1").setParam(1, billClientId).getRecord(instConn);

                Integer spanId = cast.asInt(readRow, 0);
                Integer empId = cast.asInt(readRow, 1);

                BillSpan span = BillSpan.getByClient("cons", billClientId, inst, instConn);

                if (spanId == span.id) {
                    OrdPqrRequest rq = new OrdPqrRequest();
                    rq.createdId = empId;
                    rq.clientTankId = sigmaClientId;
                    rq.creationDate = d;
                    rq.notes = "Solicitud por lectura critica";
                    rq.spanId = spanId;
                    rq.instanceId = inst.id;
                    rq.billReqType = "reading";
                    rq.numMeter = new MySQLQuery("SELECT number "
                            + "FROM bill_meter m "
                            + "WHERE m.client_id = ?1 ORDER BY m.start_span_id DESC LIMIT 1").setParam(1, billClientId).getAsString(instConn);
                    rq.insert(sigmaConn);

                    new MySQLQuery("UPDATE bfile SET owner_id = ?1 WHERE id = ?2").setParam(1, rq.id).setParam(2, bfileId).executeUpdate(sigmaConn);
                }

            }*/

            Object[][] data = new MySQLQuery("SELECT f.id, f.owner_id, created, f.`read` FROM bfile f WHERE f.`read` > 0 AND f.created > '2020-12-21 00:00:00' AND f.owner_type = 144;").getRecords(sigmaConn);
            for (Object[] fRow : data) {
                int bfileId = cast.asInt(fRow, 0);
                int billClientId = cast.asInt(fRow, 1);
                Date d = cast.asDate(fRow, 2);
                int read = cast.asInt(fRow, 3);

                Object[][] clientsData = new MySQLQuery("SELECT c.id, bill_instance_id "
                        + " FROM sigma.ord_pqr_client_tank c "
                        + " WHERE c.mirror_id = " + billClientId + ";").getRecords(sigmaConn);

                int instId = 0;
                int sigmaClientId = 0;

                if (clientsData.length == 1) {
                    sigmaClientId = cast.asInt(clientsData[0], 0);
                    instId = cast.asInt(clientsData[0], 1);
                } else {
                    int matching = 0;
                    for (Object[] clientsRow : clientsData) {
                        int iInstId = cast.asInt(clientsRow, 1);
                        BillInstance inst = BillingServlet.getInst(iInstId);
                        inst.useInstance(instConn);
                        BillSpan span = BillSpan.getByState("cons", instConn);
                        Integer iRead = new MySQLQuery("SELECT CAST(reading AS SIGNED) FROM bill_reading WHERE client_tank_id = ?1 AND span_id = ?2").setParam(1, billClientId).setParam(2, span.id).getAsInteger(instConn);

                        System.out.println(iRead + " " + read + " " + billClientId + " " + iInstId);

                        if (iRead != null && Math.abs(iRead - read) <= 1) {
                            matching++;
                            sigmaClientId = cast.asInt(clientsRow, 0);
                            instId = iInstId;
                        }
                    }
                    if (matching != 1) {
                        sigmaClientId = 0;
                    }
                }

                if (sigmaClientId > 0) {
                    BillInstance inst = BillingServlet.getInst(instId);
                    inst.useInstance(instConn);
                    BillSpan span = BillSpan.getByClient("cons", billClientId, inst, instConn);

                    Integer empId = new MySQLQuery("SELECT emp_id FROM bill_reading WHERE client_tank_id = ?1 AND span_id = ?2").setParam(1, billClientId).setParam(2, span.id).getAsInteger(instConn);

                    if (empId != null) {
                        OrdPqrRequest rq = new OrdPqrRequest();
                        rq.createdId = empId;
                        rq.clientTankId = sigmaClientId;
                        rq.creationDate = d;
                        rq.notes = "Solicitud por lectura critica";
                        rq.spanId = span.id;
                        rq.instanceId = inst.id;
                        rq.billReqType = "reading";
                        rq.numMeter = new MySQLQuery("SELECT number "
                                + "FROM bill_meter m "
                                + "WHERE m.client_id = ?1 ORDER BY m.start_span_id DESC LIMIT 1").setParam(1, billClientId).getAsString(instConn);
                        rq.insert(sigmaConn);
                        new MySQLQuery("UPDATE bfile SET owner_id = ?1, owner_type = 143 WHERE id = ?2").setParam(1, rq.id).setParam(2, bfileId).executeUpdate(sigmaConn);
                    }
                }
            }

            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("clearNoCritical")
    public Response clearNoCritical() {
        try (Connection conn = getConnection()) {
            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            List<BillInstance> insts = BillInstance.getAll(conn);
            for (int i = 0; i < insts.size(); i++) {
                BillInstance inst = insts.get(i);
                String db = inst.db;
                useBillInstance(inst.id, conn);
                Integer spanId = new MySQLQuery("SELECT MAX(id) FROM " + db + ".bill_span").getAsInteger(conn);
                if (spanId != null) {

                    Object[][] data = new MySQLQuery("SELECT  "
                            + "r.id, "//0
                            + "c.mirror_id, "//1
                            + "(r0.reading - r0.last_reading), "//2
                            + "r0.discon, "//3
                            + "r0.no_meter, "//4
                            + "t.residential "//5
                            + "FROM  "
                            + "sigma.ord_pqr_request r "
                            + "INNER JOIN sigma.ord_pqr_client_tank c ON r.client_tank_id = c.id AND c.bill_instance_id = " + inst.id + " "
                            + "INNER JOIN sigma.ord_tank_client b ON b.id = c.build_ord_id "
                            + "INNER JOIN sigma.est_tank_category tc ON tc.id = b.categ_id "
                            + "INNER JOIN sigma.est_categ_type t ON t.id = tc.type_id "
                            + "INNER JOIN " + db + ".bill_client_tank bc ON bc.id = c.mirror_id "
                            + "INNER JOIN " + db + ".bill_reading r0 ON r0.client_tank_id = bc.id AND r0.span_id = " + (spanId - 0) + " "
                            + "WHERE r.creation_date > '2020-10-15' AND r.bill_req_type = 'reading'").getRecords(conn);

                    for (Object[] row : data) {
                        int pqrId = cast.asInt(row, 0);
                        int billClientId = cast.asInt(row, 1);
                        BigDecimal cons = cast.asBigDecimal(row, 2);
                        boolean discon = cast.asBoolean(row, 3);
                        boolean noMeter = cast.asBoolean(row, 4);
                        boolean residential = cast.asBoolean(row, 5);

                        if (!isCriticaReading(inst, residential, billClientId, cons, spanId, discon, noMeter, conn)) {
                            Object[][] files = new MySQLQuery("SELECT id FROM sigma.bfile WHERE owner_id = ?1 AND owner_type = ?2").setParam(1, pqrId).setParam(2, 143).getRecords(conn);
                            for (Object[] fileRow : files) {
                                Integer bFileId = cast.asInt(fileRow, 0);
                                pi.getExistingFile(bFileId).delete();
                                new MySQLQuery("DELETE FROM sigma.bfile WHERE id = ?1").setParam(1, bFileId).executeUpdate(conn);
                                new MySQLQuery("DELETE FROM sigma.ord_pqr_request WHERE id = ?1").setParam(1, pqrId).executeUpdate(conn);
                            }
                        }
                    }
                }
            }
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    public static boolean isCriticaReading(BillInstance ins, boolean residential, int billClientId, BigDecimal dif,
            int span, boolean disconnected, boolean noMeter, Connection conn) throws Exception {

        if (ins.upperCriticalReadRateNr != null && ins.upperCriticalReadRateR != null
                && ins.lowerCriticalReadRateNr != null && ins.lowerCriticalReadRateR != null) {
            if (ins.upperCriticalReadRateNr == 0 && ins.upperCriticalReadRateR == 0
                    && ins.lowerCriticalReadRateNr == 0 && ins.lowerCriticalReadRateR == 0) {
                throw new Exception("No se han configurado los parametros de lectura critica");
            }
        }

        if (disconnected || noMeter) {
            return false;
        }

        BigDecimal avg = new MySQLQuery("SELECT AVG(ROUND((r.reading - r.last_reading), 1)) "
                + " FROM bill_reading r "
                + " WHERE r.client_tank_id = ?1 "
                + " AND r.span_id BETWEEN ?2 AND ?3; ")
                .setParam(1, billClientId)
                .setParam(2, span - 5)
                .setParam(3, span - 1).getAsBigDecimal(conn, true);

        BigDecimal maxAvgAllow;
        BigDecimal minAvgAllow;

        if (residential) {
            maxAvgAllow = BigDecimal.valueOf(ins.upperCriticalReadRateR / 100d);
            minAvgAllow = BigDecimal.valueOf(ins.lowerCriticalReadRateR / 100d);
        } else {
            maxAvgAllow = BigDecimal.valueOf(ins.upperCriticalReadRateNr / 100d);
            minAvgAllow = BigDecimal.valueOf(ins.lowerCriticalReadRateNr / 100d);
        }

        if (avg.compareTo(BigDecimal.ZERO) == 0) {
            return false;
        }

        maxAvgAllow = avg.multiply(BigDecimal.ONE.add(maxAvgAllow));
        minAvgAllow = avg.multiply(BigDecimal.ONE.subtract(minAvgAllow));

        return dif.compareTo(maxAvgAllow) > 0 || dif.compareTo(minAvgAllow) < 0;
    }

    @GET
    @Path("/billingReadingDiffs")
    public Response billingReadingDiffs() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);

            if (getBillInstance().isNetInstance()) {
                throw new Exception("No disponible en redes");
            } else {
                Object[][] data = new MySQLQuery("SELECT c.code, t.value, ROUND((r.reading - r.last_reading)*ps.price* IFNULL((SELECT bf.factor FROM bill_build_factor bf WHERE bf.build_id = b.id ORDER BY bf.bill_span_id DESC LIMIT 1), 1), 2) "
                        + "FROM "
                        + "bill_building b "
                        + "INNER JOIN bill_client_tank c ON c.building_id = b.id "
                        + "INNER JOIN bill_transaction t ON t.account_deb_id = 1 AND t.cli_tank_id = c.id AND t.bill_span_id = 140 AND t.trans_type_id = 1 "
                        + "INNER JOIN bill_reading r ON r.client_tank_id = c.id AND r.span_id = 140 "
                        + "INNER JOIN bill_price_span ps ON ps.span_id = 140 AND ps.lst_id = (SELECT cl.list_id  FROM bill_client_list cl WHERE cl.client_id = c.id ORDER BY cl.span_id DESC LIMIT 1) "
                        + "WHERE "
                        + "abs(t.value - ROUND((r.reading - r.last_reading)*ps.price* IFNULL((SELECT bf.factor FROM bill_build_factor bf WHERE bf.build_id = b.id ORDER BY bf.bill_span_id DESC LIMIT 1), 1), 2))>0.1;")
                        .getRecords(conn);

                MySQLReport rep = new MySQLReport("Diferencias", "", "Hoja 1", now(conn));
                rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
                rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.00"));//1
                rep.getFormats().get(0).setWrap(true);
                rep.setZoomFactor(85);
                Table tbl = new Table("Diferencias");
                tbl.getColumns().add(new Column("Ref. Pago", 15, 0));
                tbl.getColumns().add(new Column("Facturado", 15, 1));
                tbl.getColumns().add(new Column("Por Lectura", 15, 1));
                tbl.setData(data);
                if (tbl.getData().length > 0) {
                    rep.getTables().add(tbl);
                }
                return createResponse(rep.write(useDefault(conn)), "diferencias.xls");
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
