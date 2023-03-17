package api.bill.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.bill.model.BillDto;
import api.bill.model.BillSpan;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import utilities.MySQLQuery;

@Path("/billDto")
public class BillDtoApi extends BaseAPI {

    @POST
    public Response insert(BillDto obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillSpan cons = BillSpan.getByBuilding("cons", obj.buildId, getBillInstance(), conn);
            if (cons.id != obj.spanId) {
                throw new Exception("No se puede crear en el periodo seleccionado");
            }
            obj.insert(conn);
            useDefault(conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(BillDto obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillSpan cons = BillSpan.getByBuilding("cons", obj.buildId, getBillInstance(), conn);
            if (cons.id != obj.spanId) {
                throw new Exception("Ya se causó");
            }
            BillDto old = new BillDto().select(obj.id, conn);
            obj.update(conn);
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
            BillDto obj = new BillDto().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillDto obj = new BillDto().select(id, conn);
            BillSpan cons = BillSpan.getByBuilding("cons", obj.buildId, getBillInstance(), conn);
            if (cons.id != obj.spanId) {
                throw new Exception("Ya se causó");
            }
            BillDto.delete(id, conn);
            useDefault(conn);
            SysCrudLog.deleted(this, BillDto.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getBillDtoGrid")
    public Response getBillDtoGrid(@QueryParam("spanId") int spanId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            GridResult tbl = new GridResult();
            tbl.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_INTEGER, 100, "Código"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 250, "Nombre"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 150, "Valor")};

            Object[][] data = new MySQLQuery("SELECT dto.id, build.old_id, build.`name`, dto.amount "
                    + "FROM bill_dto AS dto "
                    + "INNER JOIN bill_building AS build ON build.id = dto.build_id "
                    + "WHERE dto.span_id = " + spanId).getRecords(conn);
            tbl.data = data;
            return createResponse(tbl);
        } catch (Exception ex) {
            return createResponse(ex);
        }

    }

    @POST
    @Path("/copy")
    public Response copy(@QueryParam("destSpanId") int destSpanId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillSpan cons = BillSpan.getByState("cons", conn);
            if (cons.id != destSpanId) {
                throw new Exception("Solo se puede hacer para el periodo actual.");
            }

            if (new MySQLQuery("SELECT COUNT(*)>0 FROM bill_dto WHERE span_id = ?1").setParam(1, destSpanId).getAsBoolean(conn)) {
                throw new Exception("Ya hay descuentos en el periodo");
            }

            if (getBillInstance().siteBilling) {
                if (new MySQLQuery("SELECT SUM(c.span_closed) > 0 FROM "
                        + "bill_dto d "
                        + "INNER JOIN bill_client_tank c ON c.building_id = d.build_id "
                        + "WHERE d.span_id = ?1").setParam(1, cons.id - 1).getAsBoolean(conn)) {
                    throw new Exception("Hay usuarios ya causados");
                }
            }

            new MySQLQuery("INSERT INTO bill_dto (build_id, span_id, amount) "
                    + "(SELECT build_id, " + cons.id + ", amount "
                    + "FROM bill_dto WHERE span_id = " + (cons.id - 1) + ")").executeInsert(conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
