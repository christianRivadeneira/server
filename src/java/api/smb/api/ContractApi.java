package api.smb.api;

import api.BaseAPI;
import api.smb.dto.BatchAnullRequest;
import api.smb.model.Contract;
import api.sys.model.SysCrudLog;
import controller.marketing.ContractController;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import utilities.apiClient.IntegerResponse;

@Path("/contract")
public class ContractApi extends BaseAPI {

    @POST
    public Response insert(Contract obj) {

        try (Connection conn = getConnection()) {
            getSession(conn);
            BigDecimal num = new MySQLQuery("SELECT count(*) FROM contract AS o WHERE o.contract_num = ?1 AND o.ctr_type = ?2").setParam(1, obj.contractNum).setParam(2, obj.ctrType).getAsBigDecimal(conn, true);

            if (num.intValue() > 0) {
                throw new Exception("Ya existe un contracto con el mismo número");
            }

            obj.created = new Date();
            obj.modified = obj.created;
            obj.insert(conn);
            setHexToStringLiPreLiDes(obj, conn);
            SysCrudLog.created(this, obj, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(Contract obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Contract old = new Contract().select(obj.id, conn);

            if (old == null) {
                throw new Exception("El registro que intenta modificar ha sido removido por otro usuario");
            }

            BigDecimal num = new MySQLQuery("select count(*) from contract AS o WHERE o.id <> ?1 AND o.contract_num = ?2 AND o.ctr_type = ?3").setParam(1, obj.id).setParam(2, obj.contractNum).setParam(3, obj.ctrType).getAsBigDecimal(conn, true);
            if (num.intValue() > 0) {
                throw new Exception("Ya existe un contracto con el mismo número");
            }

            obj.modified = new Date();
            obj.update(conn);
            System.out.println(obj.cliLides);
            setHexToStringLiPreLiDes(obj, conn);
            SysCrudLog.updated(this, obj, old, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    public static void setHexToStringLiPreLiDes(Contract ct, Connection conn) throws Exception {
        String hexaLiPre = ct.cliLipre;
        if (hexaLiPre != null) {
            byte[] parseHexBinaryLipre = javax.xml.bind.DatatypeConverter.parseHexBinary(hexaLiPre);
            String convertLiPre = new String(parseHexBinaryLipre, "UTF-8");
            ct.cliLipre = convertLiPre;
            new MySQLQuery("UPDATE contract SET cli_lipre = \"" + MySQLQuery.scape(ct.cliLipre) + "\" WHERE id = " + ct.id).executeUpdate(conn);
        }
        String hexaLiDes = ct.cliLides;
        if (hexaLiDes != null) {
            byte[] parseHexBinary = javax.xml.bind.DatatypeConverter.parseHexBinary(hexaLiDes);
            String convertLiDes = new String(parseHexBinary, "UTF-8");
            ct.cliLides = convertLiDes;
            new MySQLQuery("UPDATE contract SET cli_lides = \"" + MySQLQuery.scape(ct.cliLides) + "\" WHERE id = " + ct.id).executeUpdate(conn);
        }
    }

    public static void parseToHexa(Contract contract) {
        if (contract.cliLipre != null) {
            byte[] bytesLiPre = contract.cliLipre.getBytes();
            String hexLiPre = hex(bytesLiPre);
            contract.cliLipre = hexLiPre;
        }

        if (contract.cliLides != null) {
            byte[] bytesLiDes = contract.cliLides.getBytes();
            String hexLiDes = hex(bytesLiDes);
            contract.cliLides = hexLiDes;
        }
    }

    public static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            result.append(String.format("%02x", aByte));
        }
        return result.toString();
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            Contract obj = new Contract().select(id, conn);
            parseToHexa(obj);
            System.out.println(obj.cliLides);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            Contract.delete(id, conn);
            SysCrudLog.deleted(this, Contract.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/all")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            return createResponse(Contract.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/searchGrid")
    public Response getSearchGrid(@QueryParam("type") String type, @QueryParam("find") String find, @QueryParam("by") int by, @QueryParam("page") int page) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(ContractController.getSearchGrid(type, find, by, page, conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/searchGridCount")
    public Response getSearchGridCount(@QueryParam("type") String type, @QueryParam("find") String find, @QueryParam("by") int by) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(new IntegerResponse(ContractController.getSearchGridCount(type, find, by, conn)));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/anullBatch")
    public Response getAnullBatch(BatchAnullRequest req) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);

            for (long i = req.from; i <= req.to; i++) {
                if (Contract.getContractByNum(i + "", conn) != null) {
                    throw new Exception("El contrato " + i + " ya existe.");
                }
            }

            int j = 0;
            for (long i = req.from; i <= req.to; i++) {
                Contract ct = new Contract();
                ct.contractNum = i + "";
                ct.sower = null;
                ct.signDate = req.when;
                ct.anullCauseId = req.anullCauseId;
                ct.notes = "Anulado en Bloque";
                ct.ctrType = "afil";
                ct.checked = true;
                ct.pendDocs = 0;
                ct.created = new Date();
                ct.insert(conn);
                j++;
            }
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
