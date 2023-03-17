package api.bill.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.bill.model.BillBuildService;
import api.bill.model.BillBuilding;
import api.bill.model.BillInstance;
import api.bill.model.BillSpan;
import api.sys.model.SysCrudLog;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import utilities.apiClient.BooleanResponse;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.SummaryRow;
import utilities.mysqlReport.Table;

@Path("/billBuildService") 
public class BillBuildServiceApi extends BaseAPI {

    @POST
    public Response insert(BillBuildService obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            obj.billSpanId = BillSpan.getByBuilding("cons", 0, getBillInstance(), conn).id;
            obj.insert(conn);
            useDefault(conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(BillBuildService obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillBuildService old = new BillBuildService().select(obj.id, conn);
            if (old.isEditable(getBillInstance(), conn)) {
                obj.update(conn);
                useDefault(conn);
                SysCrudLog.updated(this, obj, old, conn);
                return Response.ok(obj).build();
            } else {
                throw new Exception("Ya ha sido causado.");
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
            BillBuildService obj = new BillBuildService().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            if (!new BillBuildService().select(id, conn).isEditable(getBillInstance(), conn)) {
                throw new Exception("Ya ha sido causado");
            }
            BillBuildService.delete(id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getAll")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            return createResponse(BillBuildService.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/editable")
    public Response isEditable(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            return createResponse(new BooleanResponse(new BillBuildService().select(id, conn).isEditable(getBillInstance(), conn)));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/grid")
    public Response getGrid(@QueryParam("buildId") int buildId) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            GridResult grid = new GridResult();
            SimpleDateFormat df = new SimpleDateFormat("MMMM yyyy");
            BillSpan reca = BillSpan.getByBuilding("reca", buildId, getBillInstance(), conn);
            grid.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 350, "Tipo"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 350, "Desde"),
                new MySQLCol(MySQLCol.TYPE_INTEGER, 200, "Cuotas"),
                new MySQLCol(MySQLCol.TYPE_INTEGER, 200, "Pendientes"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 200, "Total"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 200, "Cuota")
            };

            MySQLQuery qService = new MySQLQuery("SELECT s.id, p.id, t.name FROM "
                    + "bill_build_service as s "
                    + "INNER JOIN bill_span as p ON s.bill_span_id = p.id  "
                    + "INNER JOIN bill_service_type as t ON s.type_id = t.id "
                    + "WHERE s.bill_building_id = " + buildId + " ");
            Object[][] result = qService.getRecords(conn);

            grid.data = new Object[result.length][7];

            for (int i = 0; i < result.length; i++) {
                Object[] row = result[i];

                BillBuildService serv = new BillBuildService().select(MySQLQuery.getAsInteger(row[0]), conn);
                BillSpan span = new BillSpan().select(MySQLQuery.getAsInteger(row[1]), conn);
                String type = MySQLQuery.getAsString(row[2]);

                grid.data[i][0] = serv.id;
                grid.data[i][1] = type;
                grid.data[i][2] = "Consumos de " + df.format(span.consMonth);
                grid.data[i][3] = serv.payments;
                if (reca.id < serv.billSpanId) {
                    grid.data[i][4] = serv.payments;
                } else {
                    grid.data[i][4] = Math.max(0, serv.payments - reca.id + serv.billSpanId - 1);
                }
                grid.data[i][5] = serv.total;
                grid.data[i][6] = serv.total.divide(new BigDecimal(serv.payments), RoundingMode.HALF_EVEN).setScale(2, RoundingMode.HALF_EVEN);
            }
            return createResponse(grid);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/report")
    public Response getReport() throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillInstance inst = getBillInstance();
            useBillInstance(conn);
            MySQLReport rep = new MySQLReport("Servicios por Edificio - " + getBillInstance().name, "", "Hoja 1", now(conn));

            if (new MySQLQuery("SELECT COUNT(*)>0 FROM bill_client_tank t WHERE t.span_closed and t.active").getAsBoolean(conn)) {
                throw new Exception("El cierre del periodo está en progreso");
            }

            rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
            rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$ #,##0.00"));//1
            rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//2
            rep.setZoomFactor(85);

            List<BillBuildService> srvs = BillBuildService.getList(new MySQLQuery("SELECT " + BillBuildService.getSelFlds("s") + "FROM bill_build_service s ORDER BY bill_span_id DESC"), conn);
            if (!srvs.isEmpty()) {
                Object[][] data = new Object[srvs.size()][];
                for (int i = 0; i < srvs.size(); i++) {
                    BillBuildService sr = srvs.get(i);
                    BillBuilding b = new BillBuilding().select(sr.billBuildingId, conn);
                    BillSpan reca = new BillSpan().select(sr.billSpanId, conn);
                    BillSpan beg = BillSpan.getByBuilding("reca", b.id, inst, conn);

                    Object[] row = new Object[7];
                    data[i] = row;
                    row[0] = b.oldId;
                    row[1] = b.name;
                    row[2] = reca.getConsLabel();
                    row[3] = sr.payments;
                    if (between(beg.id, sr.billSpanId, sr.billSpanId + sr.payments - 1)) {
                        row[4] = sr.billSpanId + sr.payments - 1 - beg.id;
                    } else {
                        row[4] = 0;
                    }
                    row[5] = sr.total;
                    row[6] = sr.total.divide(new BigDecimal(sr.payments), 2, RoundingMode.HALF_EVEN);
                }

                Table bTable = new Table("Servicios por Edificio");
                bTable.getColumns().add(new Column("Código", 12, 0));//0
                bTable.getColumns().add(new Column("Nombre", 40, 0));//0
                bTable.getColumns().add(new Column("Desde", 28, 0));//2
                bTable.getColumns().add(new Column("Cuotas Total", 20, 2));//2
                bTable.getColumns().add(new Column("Pendientes Cobro", 20, 2));//2
                bTable.getColumns().add(new Column("Valor Total", 16, 1));//1
                bTable.getColumns().add(new Column("Valor Cuota", 16, 1));//1
                bTable.setSummaryRow(new SummaryRow("Totales", 5));
                bTable.setData(data);
                rep.getTables().add(bTable);
                return createResponse(rep.write(useDefault(conn)), "servicios_edificio.xls");
            } else {
                throw new Exception("No se hallaron datos");
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private static boolean between(int val, int min, int max) {
        return val >= min && val <= max;
    }
}
