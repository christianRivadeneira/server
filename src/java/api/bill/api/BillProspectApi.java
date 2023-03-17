package api.bill.api;

import api.BaseAPI;
import api.GridResult;
import api.bill.model.BillClientTank;
import api.bill.model.BillProspect;
import api.bill.model.BillProspectService;
import api.bill.model.BillSpan;
import api.bill.model.BillUserService;
import api.bill.model.CreateBillClientRequest;
import api.sys.model.SysCrudLog;
import controller.billing.BillClientTankController;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
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

@Path("/billProspect")
public class BillProspectApi extends BaseAPI {

    @POST
    public Response insert(BillProspect obj) {
        try (Connection conn = getConnection()) {
            try {
                getSession(conn);
                conn.setAutoCommit(false);
                useBillInstance(conn);
                if (getBillInstance().isNetInstance()) {
                    obj.checkStrLengthsCguno(conn);
                }
                obj.creationDate = new Date();
                obj.insert(conn);
                useDefault(conn);
                conn.commit();
                useDefault(conn);
                SysCrudLog.created(this, obj, conn);
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
    public Response update(BillProspect obj) {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);
                getSession(conn);
                useBillInstance(conn);
                if (obj.converted) {
                    throw new Exception("No pueden editarse prospectos ya convertidos");
                }
                if (getBillInstance().isNetInstance()) {
                    obj.checkStrLengthsCguno(conn);
                }
                BillProspect old = new BillProspect().select(obj.id, conn);
                obj.update(conn);
                useDefault(conn);
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
            BillProspect obj = new BillProspect().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            BillProspect.delete(id, conn);
            SysCrudLog.deleted(this, BillProspect.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/converter")
    public Response converter(@QueryParam("prospectId") int prospectId) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                getSession(conn);
                useBillInstance(conn);
                BillProspect p = new BillProspect().select(prospectId, conn);

                if (getBillInstance().isNetInstance()) {
                    if (p.contractNum == null || p.contractNum.isEmpty()) {
                        throw new Exception("El prospecto debe tener un número de contrato");
                    }
                } else {
                    if (p.doc == null || p.doc.isEmpty()) {
                        throw new Exception("El prospecto debe tener un documento");
                    }
                }

                if (p.meterNum == null || p.meterNum.isEmpty()) {
                    throw new Exception("El prospecto debe tener un medidor");
                }

                if (p.firstReading == null) {
                    throw new Exception("El prospecto debe tener una lectura inicial");
                }

                if (!p.active) {
                    throw new Exception("El prospecto está inactivo");
                }

                if (p.converted) {
                    throw new Exception("El prospecto ya fue convertido");
                }

                CreateBillClientRequest r = new CreateBillClientRequest();
                r.client = new BillClientTank();
                r.client.docType = p.docType;
                r.client.doc = p.doc;
                r.client.docCity = p.docCity;
                r.client.contractNum = p.contractNum;
                r.client.realStateCode = p.realStateCode;
                r.client.cadastralCode = p.cadastralCode;
                r.client.firstName = p.firstName;
                r.client.lastName = p.lastName;
                r.client.phones = p.phones;
                r.client.mail = p.mail;
                r.client.buildingId = p.buildingId;
                r.client.buildingTypeId = p.buildingTypeId;
                r.client.clientTypeId = p.clientTypeId;
                r.client.active = true;
                r.client.oldEstUsu = p.oldEstUsu;
                r.client.apartment = p.apartment;
                r.client.skipInterest = p.skipInterest;
                r.client.skipReconnect = p.skipReconnect;
                r.client.skipContrib = p.skipContrib;
                r.client.exemptActivity = p.exemptActivity;
                r.client.netBuilding = p.netBuilding;
                r.client.dateBeg = p.dateBeg;
                r.client.dateLast = p.dateLast;
                r.client.notes = p.notes;
                r.client.cache = p.cache;
                r.client.discon = p.discon;
                r.client.mailPromo = p.mailPromo;
                r.client.mailBill = p.mailBill;
                r.client.smsPromo = p.smsPromo;
                r.client.smsBill = p.smsBill;
                r.client.stratum = p.stratum;
                r.client.neighId = p.neighId;
                r.client.address = p.address;
                r.client.sectorType = p.sectorType;
                r.client.spanClosed = false;
                r.client.prospectId = p.id;
                r.client.grandContrib = p.grandContrib;
                r.client.ciiu = p.ciiu;
                r.client.perType = p.perType;
                r.client.netInstaller = p.netInstaller;
                r.client.cadInfo = p.cadInfo;
                r.client.icfbHome = p.icfbHome;
                r.client.priorityHome = p.priorityHome;
                r.client.location = p.location;
                r.client.birthDate = p.birthDate;

                r.initialReading = p.firstReading;
                r.meterNum = p.meterNum;

                p.converted = true;
                p.update(conn);

                BillClientTank c = BillClientTank.create(r, getBillInstance(), conn, this);

                SysCrudLog.updated(this, p, "Se convirtió en cliente", conn);
                SysCrudLog.updated(this, c, "Se convirtió desde un prospecto", conn);

                useBillInstance(conn);

                List<BillProspectService> pSrvs = BillProspectService.getByProspect(p.id, conn);
                BillSpan cons = BillSpan.getByState("cons", conn);
                for (int i = 0; i < pSrvs.size(); i++) {
                    useBillInstance(conn);
                    BillProspectService ps = pSrvs.get(i);
                    BillUserService s = new BillUserService();
                    BigDecimal payments = new MySQLQuery("SELECT SUM(total) FROM bill_prospect_payment WHERE service_id = ?1").setParam(1, ps.id).getAsBigDecimal(conn, true);

                    s.billClientTankId = c.id;
                    s.billSpanId = cons.id;
                    s.fullyCaused = false;
                    s.creditInter = ps.finanRate;
                    s.ivaRate = ps.taxRate;
                    s.inteIvaRate = ps.inteTax;
                    s.payments = ps.payments;
                    s.prospectServiceId = ps.id;
                    s.total = ps.netVal.add(ps.tax).subtract(payments);
                    s.typeId = ps.typeId;
                    if (s.total.compareTo(BigDecimal.ZERO) > 0) {
                        BillUserServiceApi.create(s, conn, this);
                    }
                }
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
    @Path("/grid")
    public Response getProspectData(@QueryParam("converted") boolean converted) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            GridResult res;
            if (getBillInstance().isTankInstance()) {// es net o no 
                res = BillClientTankController.getTankProspect(converted, conn);
            } else {
                res = BillClientTankController.getNetProspect(converted, conn);
            }
            return createResponse(res);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
