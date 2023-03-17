package api.per.api;

import api.BaseAPI;
import api.per.dto.ApprovExtra;
import api.per.dto.ApprovSurcharge;
import api.per.model.PerExtra;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import api.per.model.PerExtraRequest;
import api.per.model.PerSurcharge;
import java.util.Date;
import utilities.Dates;
import utilities.MySQLQuery;

@Path("/perExtraRequest")
public class PerExtraRequestApi extends BaseAPI {

    @POST
    public Response insert(PerExtraRequest obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(PerExtraRequest obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            obj.update(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            PerExtraRequest obj = new PerExtraRequest().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            PerExtraRequest.delete(id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getAll")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(PerExtraRequest.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/approveExtras")
    public Response approveExtras(ApprovExtra obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            try {
                conn.setAutoCommit(false);

                PerExtraRequest objReq = new PerExtraRequest().select(obj.reqId, conn);
                objReq.approvById = obj.approvById;
                objReq.approvedTime = obj.approvedTime;
                objReq.detail = obj.detail;
                objReq.paymentType = obj.paymentType;

                String eventDate = Dates.getSQLDateFormat().format(objReq.begDate);

                //Traer el empleador y el cargo
                Object[] row = new MySQLQuery("SELECT c.employeer_id, c.pos_id "
                        + "FROM per_contract AS c "
                        + "WHERE c.emp_id = " + objReq.perEmpId + " "
                        + "AND c.`last` AND c.active").getRecord(conn);
                if (row == null) {
                    throw new Exception("El empleado no tiene contrato activo.");
                }

                if (objReq.paymentType.equals("gate")) {
                    new MySQLQuery("UPDATE per_extra SET "
                            + "pay_month = '" + Dates.getSQLDateFormat().format(obj.payMonth) + "', "
                            + "checked_by_id = " + objReq.approvById + ", "
                            + "checked = true, "
                            + "extra_req_id = " + obj.reqId + " "
                            + "WHERE "
                            + "ev_date = '" + eventDate + "' ").executeUpdate(conn);

                } else {// 1) Eliminacion de extras existentes si NO es por marcacion
                    new MySQLQuery("DELETE FROM per_extra WHERE "
                            + "emp_id = " + objReq.perEmpId + " "
                            + "AND ev_date = '" + eventDate + "' "
                            + "AND (reg_type = 'prog' OR reg_type = 'bill' )").executeDelete(conn);

                    PerExtra extra = new PerExtra();
                    extra.empId = objReq.perEmpId;
                    extra.regById = objReq.approvById;
                    extra.checkedById = objReq.approvById;
                    extra.checked = true;
                    extra.payMonth = obj.payMonth;
                    extra.employeerId = MySQLQuery.getAsInteger(row[0]);
                    extra.posId = MySQLQuery.getAsInteger(row[1]);
                    extra.notes = objReq.detail;
                    extra.inputType = "man";
                    extra.regDate = new Date();
                    extra.regType = "total";
                    extra.evDate = objReq.begDate;
                    extra.evType = obj.eventType;//tipo de evento
                    extra.approvedTime = objReq.approvedTime;//tiempo en segundos
                    extra.active = true;
                    extra.extraReqId = obj.reqId;
                    extra.insert(conn);
                }
                objReq.checked = true;
                objReq.update(conn);
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
            return createResponse("OK");
        } catch (Exception ex) {

            return createResponse(ex);
        }
    }

    @POST
    @Path("/approveSurcharge")
    public Response approveSurcharge(ApprovSurcharge obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            try {
                conn.setAutoCommit(false);

                PerExtraRequest objReq = new PerExtraRequest().select(obj.reqId, conn);
                objReq.approvById = obj.approvById;
                objReq.approvedTime = obj.approvedTime;
                objReq.detail = obj.detail;
                objReq.paymentType = obj.paymentType;

                String regDate = Dates.getSQLDateFormat().format(objReq.begDate);

                //Traer el empleador y el cargo
                Object[] row = new MySQLQuery("SELECT c.employeer_id, c.pos_id "
                        + "FROM per_contract AS c "
                        + "WHERE c.emp_id = " + objReq.perEmpId + " "
                        + "AND c.`last` AND c.active").getRecord(conn);
                if (row == null) {
                    throw new Exception("El empleado no tiene contrato activo.");
                }

                PerSurcharge surcharge = new PerSurcharge();
                surcharge.empId = objReq.perEmpId;
                surcharge.regById = objReq.approvById;
                surcharge.payMonth = obj.payMonth;
                surcharge.employeerId = MySQLQuery.getAsInteger(row[0]);
                surcharge.posId = MySQLQuery.getAsInteger(row[1]);
                surcharge.notes = objReq.detail;
                surcharge.regDate = new Date();
                surcharge.regType = "total";
                surcharge.regDate = objReq.begDate;
                surcharge.evType = obj.surEvType;//tipo de evento
                surcharge.approvedTime = objReq.approvedTime;//tiempo en segundos
                surcharge.active = true;
                surcharge.insert(conn);

                objReq.checked = true;
                objReq.update(conn);
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
            return createResponse("OK");
        } catch (Exception ex) {

            return createResponse(ex);
        }
    }
}
