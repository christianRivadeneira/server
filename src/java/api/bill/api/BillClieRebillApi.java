package api.bill.api;

import api.BaseAPI;
import api.Params;
import api.bill.dto.ReBillSimulationDto;
import api.bill.model.BillAccBalance;
import api.bill.model.BillAnticNote;
import api.bill.model.BillAnticNoteRequest;
import api.bill.model.BillBill;
import api.bill.model.BillClieCau;
import api.bill.model.BillClieRebill;
import api.bill.model.BillClientTank;
import api.bill.model.BillInstance;
import api.bill.model.BillMeter;
import api.bill.model.BillNote;
import api.bill.model.BillNoteRequest;
import api.bill.model.BillPartialPayRequest;
import api.bill.model.BillPlan;
import api.bill.model.BillReading;
import api.bill.model.BillSpan;
import api.bill.model.BillTransaction;
import api.bill.writers.note.RebillNoteWriter;
import api.sys.model.SysCrudLog;
import controller.billing.BillTransactionController;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
import model.billing.constants.Accounts;
import model.system.SessionLogin;
import utilities.MySQLPreparedQuery;
import utilities.MySQLQuery;
import utilities.json.JSONDecoder;
import utilities.json.JSONEncoder;

@Path("/billClieRebill")
public class BillClieRebillApi extends BaseAPI {

    @PUT
    public Response update(BillClieRebill obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillClieRebill orig = new BillClieRebill().select(obj.id, conn);
            BillClieRebill n = orig.duplicate();
            n.reason = obj.reason;
            n.update(conn);
            useDefault(conn);
            SysCrudLog.updated(this, n, orig, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillClieRebill obj = new BillClieRebill().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            conn.setAutoCommit(false);
            try {
                BillClieRebill rb = new BillClieRebill().select(id, conn);
                if (rb.lastTransId != BillTransactionController.getLastTrasactionIdByClient(rb.clientId, conn).intValue()) {
                    throw new Exception("Han habido cambios en la cuenta, no se puede anular");
                }
                useBillInstance(conn);
                BillNote n = BillNote.getByRebill(id, conn);
                if (n != null) {
                    BillNote.cancel(this, n, true, conn);
                }

                useBillInstance(conn);
                BillAnticNote an = BillAnticNote.getByRebill(id, conn);
                if (an != null) {
                    BillAnticNote.cancel(this, an, true, conn);
                }
                
                useBillInstance(conn);
                BillReading r = BillReading.getByClientSpan(rb.clientId, rb.errorSpanId, conn);
                r.lastReading = rb.origBegRead;
                r.reading = rb.origEndRead;
                r.update(conn);
                
                rb.active = false;
                rb.update(conn);
                useDefault(conn);
                SysCrudLog.updated(this, rb, "Se anuló la refacturación", conn);
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

    private ReBillSimulationDto calcRebill(int spanId, int clientId, String sectorType, int stratum, boolean skipContrib, BigDecimal meterFactor, BigDecimal begRead, BigDecimal endRead, Connection conn) throws Exception {
        BillClieRebill r = new BillClieRebill().select(new Params("errorSpanId", spanId).param("clientId", clientId).param("active", true), conn);
        if (r != null) {
            throw new Exception("Ya se ha realizado una refacturación para el periodo");
        }

        BillClientTank client = new BillClientTank();
        client.sectorType = sectorType;
        client.stratum = stratum;
        client.skipContrib = skipContrib;

        BillMeter meter = new BillMeter();
        meter.factor = meterFactor;

        BillSpan span = new BillSpan().select(spanId, conn);

        BillClieCau orig = new BillClieCau().select(new Params("clientId", clientId).param("spanId", spanId), conn);
        BillClieCau reBill = BillClieCau.calc(client, span, meter, endRead.subtract(begRead));
        ReBillSimulationDto rta = new ReBillSimulationDto(orig, reBill);
        return rta;
    }

    @GET
    @Path("simulateRebill")
    public Response simulateRebill(
            @QueryParam("clientId") int clientId,
            @QueryParam("spanId") int spanId,
            @QueryParam("stratum") int stratum,
            @QueryParam("sectorType") String sectorType,
            @QueryParam("meterFactor") BigDecimal meterFactor,
            @QueryParam("begRead") BigDecimal begRead,
            @QueryParam("endRead") BigDecimal endRead,
            @QueryParam("skipContrib") boolean skipContrib) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            /*
            por el momento se desactiva el cambio de estrato, sector y excención para la refactutación (mg dice que solo se refactura por cambio de lectura)
            Si se desea habilitar el cambio de esos datos debe validarse que lo que venga de desk coincida con las condiciones actuales de la ficha del cliente
            y habilitar la siguiente línea en lugar de lo que hay a continuación:
            ReBillSimulationDto rta = calcRebill(spanId, clientId, sectorType, stratum, skipContrib, meterFactor, begRead, endRead, conn);
            aquí y en la simulación             */
            BillClieCau cau = BillClieCau.getByClientSpan(clientId, spanId, conn);
            ReBillSimulationDto rta = calcRebill(spanId, clientId, cau.sector, cau.stratum, cau.valExcContrib.compareTo(BigDecimal.ZERO) > 0, cau.meterFactor, begRead, endRead, conn);

            return createResponse(rta);
        } catch (Exception e) {
            return createResponse(e);
        }
    }

    @GET
    @Path("rebill")
    public Response rebill(
            @QueryParam("clientId") int clientId,
            @QueryParam("spanId") int spanId,
            @QueryParam("stratum") int stratum,
            @QueryParam("sectorType") String sectorType,
            @QueryParam("meterFactor") BigDecimal meterFactor,
            @QueryParam("skipContrib") boolean skipContrib,
            @QueryParam("begRead") BigDecimal begRead,
            @QueryParam("endRead") BigDecimal endRead,
            @QueryParam("reason") String reason) {
        try (Connection conn = getConnection()) {
            SessionLogin sess = getSession(conn);
            conn.setAutoCommit(false);
            try {
                useBillInstance(conn);

                BillSpan cons = BillSpan.getByClient("cons", clientId, getBillInstance(), conn);
                /*
                por el momento se desactiva el cambio de estrato, sector y excención para la refactutación (mg dice que solo se refactura por cambio de lectura)
                Si se desea habilitar el cambio de esos datos debe validarse que lo que venga de desk coincida con las condiciones actuales de la ficha del cliente
                y habilitar la siguiente línea en lugar de lo que hay a continuación:
                ReBillSimulationDto rta = calcRebill(spanId, clientId, sectorType, stratum, skipContrib, meterFactor, begRead, endRead, conn);
                aquí y en la simulación
                 */
                BillClieCau cau = BillClieCau.getByClientSpan(clientId, spanId, conn);
                ReBillSimulationDto rta = calcRebill(spanId, clientId, cau.sector, cau.stratum, cau.valExcContrib.compareTo(BigDecimal.ZERO) > 0, cau.meterFactor, begRead, endRead, conn);
                BillClieCau dif = rta.dif;

                //si el valor es positivo es nota débito para incrementar la deuda
                BillClieRebill rb = new BillClieRebill();
                BillReading origRead = BillReading.getByClientSpan(clientId, spanId, conn);
                rb.origBegRead = origRead.lastReading;
                rb.origEndRead = origRead.reading;
                rb.diffM3NoSubs = dif.m3NoSubs;
                rb.diffM3Subs = dif.m3Subs;
                rb.diffFixedCharge = dif.fixedCharge;
                rb.diffValConsNoSubs = dif.valConsNoSubs;
                rb.diffValConsSubs = dif.valConsSubs;
                rb.diffValSubs = dif.valSubs;
                rb.diffValExcContrib = dif.valExcContrib;
                rb.diffValContrib = dif.valContrib;
                rb.created = new Date();
                rb.creatorId = sess.employeeId;

                rb.errorSpanId = spanId;
                rb.clientId = clientId;
                rb.newMeterFactor = meterFactor;
                rb.rebillSpanId = cons.id;
                rb.newSector = sectorType;
                rb.newStratum = stratum;
                rb.reason = reason;
                rb.active = true;

                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    JSONEncoder.encode(rta, baos, false);
                    baos.close();
                    rb.jsonText = new String(baos.toByteArray());
                }
                rb.insert(conn);

                useDefault(conn);
                SysCrudLog.created(this, rb, conn);
                useBillInstance(conn);

                BillNoteRequest debConsReq = new BillNoteRequest();
                BillAnticNoteRequest sfReq = new BillAnticNoteRequest();
                sfReq.value = BigDecimal.ZERO;

                MySQLPreparedQuery credQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_cred_id = ?1 AND t.cli_tank_id = ?2", conn);
                MySQLPreparedQuery debQ = new MySQLPreparedQuery(" SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_deb_id  = ?1 AND t.cli_tank_id = ?2", conn);
                credQ.setParameter(2, clientId);
                debQ.setParameter(2, clientId);

                BigDecimal subs = dif.valConsSubs.subtract(dif.valSubs);
                a(dif.fixedCharge, debConsReq, sfReq);
                a(dif.valContrib, debConsReq, sfReq);
                a(dif.valConsNoSubs, debConsReq, sfReq);
                a(subs, debConsReq, sfReq);

                if (!debConsReq.trans.isEmpty()) {
                    useBillInstance(conn);
                    debConsReq.note = new BillNote();
                    debConsReq.note.billSpanId = cons.id;
                    debConsReq.note.clientTankId = clientId;
                    debConsReq.note.typeNotes = "n_deb";
                    debConsReq.note.descNotes = "Por refacturación, periodo en error: " + new BillSpan().select(spanId, conn).getConsLabel();
                    BillNote debNote = BillNoteRequest.createNote(debConsReq, "cons", sess, this, conn);
                    debNote.rebillId = rb.id;
                    useBillInstance(conn);
                    debNote.update(conn);
                }

                if (sfReq.value.compareTo(BigDecimal.ZERO) > 0) {
                    useBillInstance(conn);
                    BillAnticNote n = new BillAnticNote();
                    n.billSpanId = cons.id;
                    n.clientTankId = clientId;
                    n.label = "Ajuste Refacturación Meses Ant.";
                    n.typeId = new MySQLQuery("SELECT id FROM sigma.bill_antic_note_type WHERE type = 'rebill'").getAsInteger(conn);
                    n.descNotes = "Por refacturación, periodo en error: " + new BillSpan().select(spanId, conn).getConsLabel();
                    sfReq.value = sfReq.value.divide(new BigDecimal(cons.adjust), 4, RoundingMode.HALF_EVEN).setScale(0, RoundingMode.HALF_EVEN).multiply(new BigDecimal(cons.adjust));
                    sfReq.note = n;
                    BillAnticNote anticNote = BillAnticNote.createNote(sfReq, sess, getBillInstance(), conn);
                    anticNote.rebillId = rb.id;
                    useBillInstance(conn);
                    anticNote.update(conn);
                }

                useBillInstance(conn);
                rb.lastTransId = BillTransactionController.getLastTrasactionIdByClient(clientId, conn);
                rb.update(conn);

                BillReading newRead = origRead.duplicate();
                newRead.lastReading = begRead;
                newRead.reading = endRead;
                newRead.update(conn);
                useDefault(conn);
                SysCrudLog.updated(this, newRead, origRead, conn);
                SysCrudLog.updated(this, origRead, "Refacturación", conn);
                conn.commit();
                return createResponse(rta);
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception e) {
            return createResponse(e);
        }
    }

    @GET
    @Path("/partialBill")
    public Response getPartialBill(@QueryParam("rebillId") int rebillId) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            useBillInstance(conn);
            BillInstance inst = this.getBillInstance();
            BillClieRebill rb = new BillClieRebill().select(rebillId, conn);

            int clientId = rb.clientId;
            int recaId = BillSpan.getByClient("reca", clientId, inst, conn).id;

            //la refacturación se hace para el periodo en consumo
            if (recaId != rb.rebillSpanId - 1) {
                throw new Exception("La refacturación corresponde a otro periodo");
            }

            int noMovs = 0;
            noMovs += (noExtraMovements(Accounts.C_BASI, clientId, recaId, conn) ? 1 : 0);
            noMovs += (noExtraMovements(Accounts.C_CONTRIB, clientId, recaId, conn) ? 1 : 0);
            noMovs += (noExtraMovements(Accounts.C_CONS, clientId, recaId, conn) ? 1 : 0);
            noMovs += (noExtraMovements(Accounts.C_CONS_SUBS, clientId, recaId, conn) ? 1 : 0);
            if (noMovs != 4) {
                throw new Exception("Ya se han hecho cambios en la cuenta");
            }

            ReBillSimulationDto sim;
            try (ByteArrayInputStream bais = new ByteArrayInputStream(rb.jsonText.getBytes())) {
                sim = new JSONDecoder().getObject(bais, ReBillSimulationDto.class);
            }

            BillPartialPayRequest req = new BillPartialPayRequest();
            List<BillAccBalance> bal = BillClientTankApi.getBankBalance(clientId, inst, conn);

            req.clientId = clientId;
            for (int i = 0; i < bal.size(); i++) {
                BillAccBalance b = bal.get(i);
                BigDecimal val;
                switch (b.accId) {
                    case Accounts.C_CONS:
                        val = sim.reBill.valConsNoSubs;
                        break;
                    case Accounts.C_BASI:
                        val = sim.reBill.fixedCharge;
                        break;
                    case Accounts.C_CONTRIB:
                        val = sim.reBill.valContrib;
                        break;
                    case Accounts.C_CONS_SUBS:
                        val = sim.reBill.valConsSubs.subtract(sim.reBill.valSubs);
                        break;
                    default:
                        val = b.curBalance;
                        break;
                }

                if (val.compareTo(BigDecimal.ZERO) != 0) {
                    BillPlan plan = new BillPlan();
                    plan.accountCredId = b.accId;
                    plan.accountDebId = b.oppAccId;
                    plan.value = val;
                    req.plans.add(plan);
                }
            }

            BillBill bill = BillPartialPayRequest.processRequest(req, inst, sl, conn);
            File f = BillBill.reprint(bill.id, this.getBillInstance(), conn);
            return createResponse(f, "bill.pdf");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/print")
    public Response print(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            RebillNoteWriter w = new RebillNoteWriter(getBillInstance(), conn);
            useBillInstance(conn);
            BillClieRebill rb = new BillClieRebill().select(id, conn);
            BillClientTank client = new BillClientTank().select(rb.clientId, conn);
            BillSpan span = new BillSpan().select(rb.errorSpanId, conn);
            w.beginDocument();
            w.addNote(rb, client, span);
            return createResponse(w.endDocument(), "refacturacion.pdf");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private void a(BigDecimal dif, BillNoteRequest debConsReq, BillAnticNoteRequest sfReq) throws Exception {
        if (dif.compareTo(BigDecimal.ZERO) > 0) {
            debConsReq.trans.add(createTrans(Accounts.C_REBILL, Accounts.E_ING_OP, dif));
        } else if (dif.compareTo(BigDecimal.ZERO) < 0) {
            sfReq.value = sfReq.value.add(dif.abs());
        }
    }

    private BillTransaction createTrans(int accDebId, int accCredId, BigDecimal value) {
        BillTransaction t = new BillTransaction();
        t.accountCredId = accCredId;
        t.accountDebId = accDebId;
        t.value = value;
        return t;
    }

    private boolean noExtraMovements(int account, int clientId, int spanId, Connection conn) throws Exception {
        int cred = new MySQLQuery("SELECT COUNT(*) FROM bill_transaction as t WHERE t.account_cred_id = ?1 AND t.cli_tank_id = ?2 AND t.bill_span_id = ?3 AND account_deb_id <> " + Accounts.E_SUBS).setParam(1, account).setParam(2, clientId).setParam(3, spanId).getAsInteger(conn);
        int deb = new MySQLQuery("SELECT COUNT(*) FROM bill_transaction as t WHERE t.account_deb_id = ?1 AND t.cli_tank_id = ?2 AND t.bill_span_id = ?3").setParam(1, account).setParam(2, clientId).setParam(3, spanId).getAsInteger(conn);
        return deb <= 1 && cred == 0;
    }

    //una versión de rebill que hace las correcciones dentro del mismo mes cuando es posible, sino, las intenta en cartera, y deja las notas para el periodo entrante como ultima opción
    //se comenta porque para los reportes del FSSRI es más sencillo tener unicamente la opción de las notas para el periodo entrante
    /*  @GET
    @Path("rebillFull")
    public Response rebillFull(
            @QueryParam("clientId") int clientId,
            @QueryParam("spanId") int spanId,
            @QueryParam("stratum") int stratum,
            @QueryParam("sectorType") String sectorType,
            @QueryParam("meterFactor") BigDecimal meterFactor,
            @QueryParam("begRead") BigDecimal begRead,
            @QueryParam("endRead") BigDecimal endRead,
            @QueryParam("skipContrib") boolean skipContrib) {
        try (Connection conn = getConnection()) {
            SessionLogin sess = getSession(conn);
            conn.setAutoCommit(false);
            try {
                useBillInstance(conn);
                BillSpan reca = BillSpan.getByClient("reca", clientId, getBillInstance(), conn);
                BillSpan cons = BillSpan.getByClient("cons", clientId, getBillInstance(), conn);
                ReBillSimulationDto rta = calcRebill(spanId, clientId, sectorType, stratum, skipContrib, meterFactor, begRead, endRead, conn);

                BillClieCau dif = rta.dif;

                //si el valor es positivo es nota débito para incrementar la deuda
                BillClieRebill rb = new BillClieRebill();
                rb.m3NoSubs = dif.m3NoSubs;
                rb.m3Subs = dif.m3Subs;
                rb.fixedCharge = dif.fixedCharge;
                rb.valConsNoSubs = dif.valConsNoSubs;
                rb.valConsSubs = dif.valConsSubs;
                rb.valSubs = dif.valSubs;
                rb.valExcContrib = dif.valExcContrib;
                rb.valContrib = dif.valContrib;

                rb.errorSpanId = spanId;
                rb.clientId = clientId;
                rb.meterFactor = meterFactor;
                rb.rebillSpanId = cons.id;
                rb.sector = sectorType;
                rb.stratum = stratum;

//                rb.insert(conn);
                //no puede haber skipcontrib sin actividad
                //se debe actualizar el factor del medidor?
                //se deben actualizar los datos del usuario?
                //se deben actualizar la lectura?
                //mostrar la advertencia cuando no se vayan a crear las notas
                BillNoteRequest debConsReq = new BillNoteRequest();
                BillNoteRequest debRecaReq = new BillNoteRequest();
                BillNoteRequest credReq = new BillNoteRequest();
                BillAnticNoteRequest sfReq = new BillAnticNoteRequest();
                sfReq.value = BigDecimal.ZERO;

                MySQLPreparedQuery credQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_cred_id = ?1 AND t.cli_tank_id = ?2", conn);
                MySQLPreparedQuery debQ = new MySQLPreparedQuery(" SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_deb_id  = ?1 AND t.cli_tank_id = ?2", conn);
                credQ.setParameter(2, clientId);
                debQ.setParameter(2, clientId);

                BigDecimal glpCart = getBalance(debQ, credQ, Accounts.C_CAR_GLP);
                BigDecimal subs = dif.valConsSubs.subtract(dif.valSubs);
                BigDecimal totalConsCred = (subs.compareTo(BigDecimal.ZERO) < 0 ? subs : BigDecimal.ZERO).add((dif.valConsNoSubs.compareTo(BigDecimal.ZERO) < 0 ? dif.valConsNoSubs : BigDecimal.ZERO));

                boolean credConsFitCart = glpCart.add(totalConsCred).compareTo(BigDecimal.ZERO) >= 0;

                //si se vuelve a implemetar, esta parte no va a funcionar
                boolean rebillOnSpan = canRebillOnSpan(clientId, reca.id, spanId, conn);

                a(dif.fixedCharge, Accounts.C_BASI, Accounts.E_ING_OP, Accounts.C_CAR_GLP, rebillOnSpan, credConsFitCart, credReq, debRecaReq, debConsReq, sfReq);
                a(dif.valContrib, Accounts.C_CONTRIB, Accounts.E_CONTRIB, Accounts.C_CAR_CONTRIB, rebillOnSpan, credConsFitCart, credReq, debRecaReq, debConsReq, sfReq);
                a(dif.valConsNoSubs, Accounts.C_CONS, Accounts.E_ING_OP, Accounts.C_CAR_GLP, rebillOnSpan, credConsFitCart, credReq, debRecaReq, debConsReq, sfReq);
                a(subs, Accounts.C_CONS_SUBS, Accounts.E_ING_OP, Accounts.C_CAR_GLP, rebillOnSpan, credConsFitCart, credReq, debRecaReq, debConsReq, sfReq);

                if (!credReq.trans.isEmpty()) {
                    credReq.note = new BillNote();
                    credReq.note.billSpanId = reca.id;
                    credReq.note.clientTankId = clientId;
                    credReq.note.typeNotes = "n_cred";
                    BillNoteRequest.createNote(credReq, sess, this, conn);
                }
                if (!debRecaReq.trans.isEmpty()) {
                    debRecaReq.note = new BillNote();
                    debRecaReq.note.billSpanId = reca.id;
                    debRecaReq.note.clientTankId = clientId;
                    debRecaReq.note.typeNotes = "n_deb";
                    BillNoteRequest.createNote(debRecaReq, sess, this, conn);
                }
                if (!debConsReq.trans.isEmpty()) {
                    debConsReq.note = new BillNote();
                    //si se desea que el débito aparezca en factura como refacturación habria que crearlo para el periodo en consumo
                    debConsReq.note.billSpanId = reca.id;
                    debConsReq.note.clientTankId = clientId;
                    debConsReq.note.typeNotes = "n_deb";
                    BillNoteRequest.createNote(debConsReq, sess, this, conn);
                }
                if (sfReq.value.compareTo(BigDecimal.ZERO) > 0) {
                    BillAnticNote n = new BillAnticNote();
                    n.billSpanId = cons.id;
                    n.clientTankId = clientId;
                    n.label = "Refacturación";
                    n.typeId = new MySQLQuery("SELECT id FROM sigma.bill_antic_note_type WHERE type = 'rebill'").getAsInteger(conn);
                    n.descNotes = "Refacturación";
                    sfReq.note = n;
                    BillAnticNote.createNote(sfReq, sess, getBillInstance(), conn);
                }
                conn.commit();
                return createResponse(rta);
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception e) {
            return createResponse(e);
        }
    }
    
    private void a(BigDecimal dif, int account, int opAccount, int cartAccount, boolean autoNotes, boolean credConsCart, BillNoteRequest credReq, BillNoteRequest debRecaReq, BillNoteRequest debConsReq, BillAnticNoteRequest sfReq) throws Exception {
        if (dif.compareTo(BigDecimal.ZERO) > 0) {
            if (autoNotes) {
                debRecaReq.trans.add(createTrans(account, opAccount, dif));
            } else {
                debConsReq.trans.add(createTrans(Accounts.C_REBILL, opAccount, dif));
            }
        } else if (dif.compareTo(BigDecimal.ZERO) < 0) {
            if (autoNotes) {
                credReq.trans.add(createTrans(opAccount, account, dif.abs()));
            } else {
                if (credConsCart) {
                    boolean found = false;
                    for (BillTransaction t : credReq.trans) {
                        if (t.accountCredId == cartAccount) {
                            t.value = t.value.add(dif.abs());
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        credReq.trans.add(createTrans(opAccount, cartAccount, dif.abs()));
                    }
                } else {
                    sfReq.value = sfReq.value.add(dif.abs());
                }
            }
        }
    }
    
    private BigDecimal getBalance(MySQLPreparedQuery debQ, MySQLPreparedQuery credQ, int account) throws Exception {
        credQ.setParameter(1, account);
        debQ.setParameter(1, account);
        return debQ.getAsBigDecimal(true).subtract(credQ.getAsBigDecimal(true));
    }    
    
     */
}
