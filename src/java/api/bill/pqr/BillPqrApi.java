package api.bill.pqr;

import api.BaseAPI;
import api.MultiPartRequest;
import api.bill.model.PqrPoll;
import api.bill.model.dto.PqrItem;
import api.bill.model.dto.PqrMail;
import api.ord.api.OrdPqrCylApi;
import api.ord.api.OrdPqrTankApi;
import api.ord.api.OrdRepairsApi;
import api.ord.model.OrdActivityPqr;
import api.ord.model.OrdCfg;
import api.ord.model.OrdPoll;
import api.ord.model.OrdPqrCyl;
import api.ord.model.OrdPqrTank;
import api.ord.model.OrdRepairs;
import api.ord.model.OrdTechnician;
import api.ord.model.OrdTextPoll;
import api.ord.orfeo.OrfeoClient;
import api.ord.orfeo.OrfeoClosePqrCommand;
import api.ord.orfeo.OrfeoTechnicalAssistanceCommand;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import utilities.SysTask;
import utilities.apiClient.IntegerResponse;
import utilities.apiClient.StringResponse;
import utilities.json.JSONDecoder;
import web.fileManager;
import web.quality.SendMail;

@Path("/billPqrApi")
public class BillPqrApi extends BaseAPI {

    private static final int PQR_POLL_SIGN = 112;
    private static final String STATUS_OK = "OK";

    @POST
    @Path("createPqrPoll")
    public Response createPqrPoll(PqrPoll obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            try {
                conn.setAutoCommit(false);
                OrdCfg ordCfg = new OrdCfg().select(1, conn);
                boolean existPoll = false;
                switch (obj.pqrType) {
                    case 1://cyl
                        existPoll = new MySQLQuery("SELECT pqr_poll_id IS NOT NULL FROM ord_pqr_cyl WHERE id = " + obj.pqrId).getAsBoolean(conn);
                        break;
                    case 2://estac
                        existPoll = new MySQLQuery("SELECT pqr_poll_id IS NOT NULL FROM ord_pqr_tank WHERE id = " + obj.pqrId).getAsBoolean(conn);
                        break;
                    case 3://asis
                        existPoll = new MySQLQuery("SELECT pqr_poll_id IS NOT NULL FROM ord_repairs WHERE id = " + obj.pqrId).getAsBoolean(conn);
                        break;
                }

                if (existPoll) {
                    return createResponse(new StringResponse(STATUS_OK));
                }

                OrdPoll ordPoll = new OrdPoll();
                ordPoll.answer = obj.answer;
                ordPoll.notes = obj.notes;
                ordPoll.pollVersionId = obj.pollVersionId;
                ordPoll.empId = obj.empId;
                ordPoll.id = ordPoll.insert(conn);

                if (obj.answer != null && !obj.answer.isEmpty()) {
                    ByteArrayInputStream bais = new ByteArrayInputStream(obj.textPoll.getBytes());
                    List<OrdTextPoll> listTP = new JSONDecoder().getList(bais, OrdTextPoll.class);

                    for (OrdTextPoll i : listTP) {
                        OrdTextPoll textPoll = new OrdTextPoll();
                        textPoll.ordinal = i.ordinal;
                        textPoll.text = i.text;
                        textPoll.pollId = ordPoll.id;
                        textPoll.insert(conn);
                    }
                }

                String employeeName = new MySQLQuery("SELECT CONCAT(first_name, ' ', last_name) FROM employee "
                        + "WHERE id = " + obj.empId).getAsString(conn);

                OrdActivityPqr actPqr = new OrdActivityPqr();
                actPqr.createId = obj.empId;
                actPqr.modId = obj.empId;
                actPqr.actDate = new Date();
                actPqr.creationDate = new Date();
                actPqr.modDate = new Date();
                actPqr.activity = "Encuesta diligenciada";
                actPqr.actDeveloper = employeeName;
                actPqr.pqrCylId = (obj.pqrType == 1 ? obj.pqrId : null);
                actPqr.pqrTankId = (obj.pqrType == 2 ? obj.pqrId : null);
                actPqr.repairId = (obj.pqrType == 3 ? obj.pqrId : null);
                actPqr.pqrOtherId = null;
                actPqr.observation = obj.notes;
                actPqr.insert(conn);

                Date now = now(conn);
                Integer technicianId = null;
                String radOrfeo = null;
                switch (obj.pqrType) {
                    case 1://cyl
                        OrdPqrCyl pqrCyl = new OrdPqrCyl().select(obj.pqrId, conn);
                        pqrCyl.pqrPollId = ordPoll.id;
                        pqrCyl.attentionDate = now;
                        pqrCyl.attentionHour = now;
                        pqrCyl.nif = obj.nif;
                        pqrCyl.update(conn);
                        technicianId = pqrCyl.technicianId;
                        radOrfeo = pqrCyl.radOrfeo;
                        break;
                    case 2://estac
                        OrdPqrTank pqrTank = new OrdPqrTank().select(obj.pqrId, conn);
                        pqrTank.pqrPollId = ordPoll.id;
                        pqrTank.attentionDate = now;
                        pqrTank.attentionHour = now;
                        pqrTank.update(conn);
                        technicianId = pqrTank.technicianId;
                        radOrfeo = pqrTank.radOrfeo;
                        break;
                    case 3://asistencia
                        OrdRepairs pqrRepair = new OrdRepairs().select(obj.pqrId, conn);
                        if (ordCfg.showAssiPoll) {
                            BigDecimal rnd = BigDecimal.valueOf(new Random().nextDouble());
                            rnd = rnd.setScale(2, RoundingMode.HALF_EVEN);
                            BigDecimal percent = new BigDecimal(ordCfg.assisPollRatio / 100d);
                            percent = percent.setScale(2, RoundingMode.HALF_EVEN);
                            pqrRepair.toPoll = percent.compareTo(rnd) >= 0;
                        }
                        pqrRepair.pqrPollId = ordPoll.id;
                        pqrRepair.confirmDate = now;
                        pqrRepair.confirmTime = now;
                        pqrRepair.update(conn);

                        if (obj.pollNegativeType != null && !obj.pollNegativeType.isEmpty()) {
                            new MySQLQuery("UPDATE com_app_neg_quest SET type = '" + obj.pollNegativeType + "' WHERE repair_id = " + obj.pqrId).executeUpdate(conn);
                        }
                        technicianId = pqrRepair.technicianId;
                        radOrfeo = pqrRepair.radOrfeo;
                        break;
                }

                if (ordCfg.orfeo && radOrfeo != null) {
                    SysTask t = new SysTask(BillPqrApi.class, System.getProperty("user.name"), 1, conn);
                    try {
                        OrfeoTechnicalAssistanceCommand command = new OrfeoTechnicalAssistanceCommand();

                        switch (obj.pqrType) {
                            case 1:
                                command.file = OrdPqrCylApi.getPqrFile(obj.pqrId, conn);
                                break;
                            case 2:
                                command.file = OrdPqrTankApi.getPqrFile(obj.pqrId, conn);
                                break;
                            case 3:
                                command.file = OrdRepairsApi.getPqrFile(obj.pqrId, conn);
                                break;
                            default:
                                break;
                        }

                        if (command.file == null) {
                            throw new Exception("No se pudo generar el pdf");
                        }

                        command.radNumber = radOrfeo;
                        command.notes = OrdTechnician.getTecnicianLabel(conn, technicianId) + ": " + (obj.notes == null ? "" : obj.notes);
                        command.date = now;

                        String orfeoMessage = new OrfeoClient().registerAssistance(command).tryGetOkValue();
                        System.out.println("orfeo: " + orfeoMessage);

                        if (obj.pqrType == 3) {//si es asistencia se debe cerrar
                            OrfeoClosePqrCommand closeCommand = new OrfeoClosePqrCommand();
                            closeCommand.radNumber = radOrfeo;
                            closeCommand.date = now;
                            closeCommand.employeeDocument = sl.document;
                            closeCommand.dependencyCode = ordCfg.orfeoDependencyCode;

                            String closeMessage = new OrfeoClient().closePqr(closeCommand).tryGetOkValue();
                            System.out.println("orfeo cierre asistencia: " + closeMessage);
                        }

                    } catch (Exception ex) {
                        Logger.getLogger(BillPqrApi.class.getName()).log(Level.SEVERE, null, ex);
                        t.error(ex, conn);
                        throw ex;
                    }
                }

                conn.commit();
                return createResponse(new StringResponse(STATUS_OK));
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception ex) {
            Logger.getLogger(BillPqrApi.class.getName()).log(Level.SEVERE, null, ex);
            return createResponse(ex);
        }
    }

    @POST
    @Path("/syncPqrs")
    public Response syncPqrs(@Context HttpServletRequest request) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);

            File file = null;
            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);
            Map<String, String> pars = mr.params;
            String fileName = MySQLQuery.getAsString(pars.get("fileName"));
            String strHasMail = MySQLQuery.getAsString(pars.get("hasMail"));
            boolean hasMail = false;
            String subject = null;
            String body = null;
            String mail = null;

            if (strHasMail != null) {
                hasMail = MySQLQuery.getAsString(pars.get("hasMail")).equals("1");
            }
            if (hasMail) {
                subject = MySQLQuery.getAsString(pars.get("subject"));
                body = MySQLQuery.getAsString(pars.get("body"));
                mail = MySQLQuery.getAsString(pars.get("mail"));
            }

            String name = fileName;
            int ownerType = PQR_POLL_SIGN;
            String desc = "Firma PQR";

            try {
                fileName = fileName.substring(0, fileName.lastIndexOf('.'));

                String fileparts[] = fileName.split("_");
                Integer pqrId = Integer.valueOf(fileparts[0]);
                Integer pqrType = Integer.valueOf(fileparts[1]);
                Integer empId = Integer.valueOf(fileparts[2]);

                Integer regPollId = null;
                Integer bfileId = null;
                Object[] data;

                switch (pqrType) {
                    case 1:
                        data = new MySQLQuery("SELECT e.id ,b.id "
                                + "FROM ord_pqr_cyl p "
                                + "INNER JOIN ord_poll e ON e.id = p.pqr_poll_id "
                                + "LEFT JOIN bfile b ON b.owner_id = e.id "
                                + "AND b.owner_type = " + PQR_POLL_SIGN + " "
                                + "WHERE "
                                + "p.id = " + pqrId).getRecord(conn);
                        desc = "Firma PQR Cilindros";
                        if (data != null) {
                            regPollId = MySQLQuery.getAsInteger(data[0]);
                            bfileId = MySQLQuery.getAsInteger(data[1]);
                        }
                        break;
                    case 2:
                        data = new MySQLQuery("SELECT e.id ,b.id "
                                + "FROM ord_pqr_tank p "
                                + "INNER JOIN ord_poll e ON e.id = p.pqr_poll_id "
                                + "LEFT JOIN bfile b ON b.owner_id = e.id "
                                + "AND b.owner_type = " + PQR_POLL_SIGN + " "
                                + "WHERE "
                                + "p.id = " + pqrId).getRecord(conn);
                        desc = "Firma PQR Estacionarios";
                        if (data != null) {
                            regPollId = MySQLQuery.getAsInteger(data[0]);
                            bfileId = MySQLQuery.getAsInteger(data[1]);
                        }
                        break;
                    case 3:
                        data = new MySQLQuery("SELECT e.id ,b.id "
                                + "FROM ord_repairs p "
                                + "INNER JOIN ord_poll e ON e.id = p.pqr_poll_id "
                                + "LEFT JOIN bfile b ON b.owner_id = e.id "
                                + "AND b.owner_type = " + PQR_POLL_SIGN + " "
                                + "WHERE "
                                + "p.id = " + pqrId).getRecord(conn);
                        desc = "Firma PQR Asistencia";
                        if (data != null) {
                            regPollId = MySQLQuery.getAsInteger(data[0]);
                            bfileId = MySQLQuery.getAsInteger(data[1]);
                        }
                        break;
                }

                if (regPollId != null) {
                    if (bfileId != null) {
                        if (hasMail) {
                            sendMailPqr(conn, pqrType, pqrId, mail, subject, body);
                        }
                        return createResponse(new StringResponse(STATUS_OK));
                    } else {
                        fileManager.upload(empId, regPollId, ownerType, name, desc, true, null, pi, mr.getFile().file, conn);
                        if (hasMail) {
                            sendMailPqr(conn, pqrType, pqrId, mail, subject, body);
                        }
                        return createResponse(new StringResponse(STATUS_OK));
                    }
                } else {
                    return createResponse(new StringResponse("DONT_DELETE"));
                }
            } catch (Exception ex) {
                Logger.getLogger(BillPqrApi.class.getName()).log(Level.SEVERE, null, ex);
                return createResponse(ex);
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    public static void sendMailPqr(Connection conn, int type, int id, String mail, String subject, String body) throws Exception {
        File file;
        switch (type) {
            case 1:
                file = OrdPqrCylApi.getPqrFile(id, conn);
                break;
            case 2:
                file = OrdPqrTankApi.getPqrFile(id, conn);
                break;
            default:
                file = OrdRepairsApi.getPqrFile(id, conn);
                break;
        }

        SendMail.sendMail(conn, mail, subject, SendMail.getHtmlMsg(conn, subject, body), body, new String[]{file.getName()}, new File[]{file}, null, null);
    }

    @POST
    @Path("createOrdActivityPqr")
    public Response createOrdActivityPqr(OrdActivityPqr obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            try {
                conn.setAutoCommit(false);

                obj.id = obj.insert(conn);

                conn.commit();
                return createResponse(new StringResponse(STATUS_OK));
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception ex) {
            Logger.getLogger(BillPqrApi.class.getName()).log(Level.SEVERE, null, ex);
            return createResponse(ex);
        }
    }

    @GET
    @Path("pqrs")
    public Response createOrdActivityPqr() throws Exception {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            Integer tecId = new MySQLQuery("SELECT e.technician_id FROM employee e WHERE e.id = " + sl.employeeId).getAsInteger(conn);
            if (tecId == null) {
                throw new Exception("No se encuentra registrado como tecnico en SAC");
            }
            Object[][] data = new MySQLQuery("(SELECT p.id, 1, CONCAT(p.serial, ' - [PQR Cilindros]'), CONCAT(i.first_name, ' ', i.last_name) AS cliente, CONCAT(i.address, IF(neigh.`name` IS NOT NULL, CONCAT(' ', neigh.`name`), ''),' [', c.name,']') AS address, i.phones, r.description, e.name, p.creation_date, p.regist_hour, IF(i.type = 'brand', IF(i.ctr_type='afil','Afiliado','Comodato'), IF(i.type = 'univ', 'Provisional', IF(i.type = 'app', 'App', NULL))) AS typeindex, COALESCE(em.short_name, CONCAT(em.first_name, ' ',em.last_name)), p.arrival_date, p.notes, "
                    + "CONCAT(COALESCE(CONCAT(i.email, ','), '')) as mail "
                    + "FROM ord_pqr_cyl AS p "
                    + "INNER JOIN ord_technician AS t ON t.id = p.technician_id "
                    + "INNER JOIN ord_contract_index AS i ON i.id = p.index_id "
                    + "INNER JOIN ord_pqr_reason AS r ON r.id = p.pqr_reason "
                    + "LEFT JOIN neigh ON neigh.id = i.neigh_id "
                    + "LEFT JOIN enterprise AS e ON p.enterprise_id = e.id "
                    + "INNER JOIN employee em ON em.id = p.regist_by "
                    + "INNER JOIN city c ON c.id = i.city_id "
                    + "INNER JOIN ord_office o ON o.id = p.office_id AND o.pqrs_app "
                    + "WHERE t.id = " + tecId + " AND p.pqr_poll_id IS NULL AND p.pqr_anul_cause_id IS NULL AND p.satis_poll_id IS NULL) "
                    + "UNION ALL ( "
                    + "SELECT pqr.id, 2, CONCAT(pqr.serial, ' - [PQR Estacionarios]'),  "
                    + "IFNULL(est.name, CONCAT(cli.first_name, ' ', COALESCE(cli.last_name, ''))) AS cliente, "
                    + "IF(est.id IS NOT NULL, 	CONCAT(est.address,' ',' [', c.name,']', IFNULL(CONCAT(' Apto-',cli.apartament),'')), "
                    + "	IF(cli.neigh_id IS NULL , CONCAT(build.address,' ', build.name,' [', c.name,']', IFNULL(CONCAT(' Apto-',cli.apartament),'')),  "
                    + "	CONCAT(cli.address,' ', n.name,' [', c.name,']'))	) AS address, "
                    + "IFNULL(est.phones, IF(cli.neigh_id IS NULL,build.phones, cli.phones)) AS phone, "
                    + "r.description, e.name,  pqr.regist_date, pqr.regist_hour, NULL,  "
                    + "IFNULL(em.short_name, CONCAT(em.first_name, ' ',em.last_name)), pqr.arrival_date, pqr.notes, "
                    + "CONCAT(COALESCE(CONCAT(build.contact_mail, ','), ''), COALESCE(CONCAT(est.contact_mail, ','), '')) as mail "
                    + "FROM ord_pqr_tank pqr "
                    + "INNER JOIN employee em ON em.id = pqr.regist_by "
                    + "INNER JOIN ord_pqr_reason r ON r.id = pqr.reason_id "
                    + "INNER JOIN ord_office o ON o.id = pqr.office_id AND o.pqrs_app "
                    + "LEFT JOIN ord_pqr_client_tank cli ON cli.id = pqr.client_id "
                    + "LEFT JOIN ord_tank_client build ON build.id = cli.build_ord_id "
                    + "LEFT JOIN ord_tank_client est ON est.id = pqr.build_id "
                    + "INNER JOIN city c ON c.id = IFNULL(cli.city_id,est.city_id) "
                    + "LEFT JOIN neigh n ON n.id = cli.neigh_id "
                    + "LEFT JOIN enterprise e ON e.id = pqr.enterprise_id "
                    + "WHERE pqr.technician_id = " + tecId + " AND pqr.pqr_poll_id IS NULL AND pqr.anul_cause_id IS NULL AND pqr.satis_poll_id IS NULL) "
                    + "UNION ALL ( "
                    + "SELECT pqr.id, 3, CONCAT(pqr.serial, ' - [Asistencia Técnica]'),  "
                    + "COALESCE(CONCAT(i.first_name, ' ', i.last_name), est.name, CONCAT(cli.first_name, ' ', COALESCE(cli.last_name, ''))) AS cliente, "
                    + "IFNULL(CONCAT(i.address,' ', IFNULL(ne.`name`,''), ' [', c.name,']'),IF(est.id IS NOT NULL, 	CONCAT(est.address,' ',' [', c.name,']', IFNULL(CONCAT(' Apto-',cli.apartament),'')), "
                    + "	IF(cli.neigh_id IS NULL , CONCAT(build.address,' ', build.name,' [', c.name,']', IFNULL(CONCAT(' Apto-',cli.apartament),'')),  "
                    + "	CONCAT(cli.address,' ', necli.name,' [', c.name,']'))) )AS address, "
                    + "IFNULL(i.phones, IFNULL(est.phones, IF(cli.neigh_id IS NULL,build.phones, cli.phones))) AS phones,  "
                    + "r.description, e.name, pqr.regist_date, pqr.regist_hour, IF(i.type = 'brand', IF(i.ctr_type='afil','Afiliado','Comodato'),  "
                    + "IF(i.type = 'univ', 'Provisional', IF(i.type = 'app', 'App', NULL))) AS typeindex,  "
                    + "COALESCE(em.short_name, CONCAT(em.first_name, ' ',em.last_name)), NULL, pqr.notes, "
                    + "CONCAT(COALESCE(CONCAT(build.contact_mail, ','), ''), COALESCE(CONCAT(est.contact_mail, ','), ''), COALESCE(CONCAT(i.email, ','), '')) as mail "
                    + "FROM ord_repairs AS pqr "
                    + "LEFT JOIN ord_contract_index AS i ON i.id = pqr.index_id "
                    + "LEFT JOIN neigh AS ne ON ne.id = i.neigh_id "
                    + "LEFT JOIN ord_pqr_client_tank AS cli ON cli.id = pqr.client_id "
                    + "LEFT JOIN ord_tank_client AS build ON build.id = cli.build_ord_id "
                    + "LEFT JOIN ord_tank_client AS est ON est.id = pqr.build_id "
                    + "LEFT JOIN neigh necli ON necli.id = cli.neigh_id "
                    + "LEFT JOIN enterprise e ON e.id = pqr.enterprise_id "
                    + "INNER JOIN employee em ON em.id = pqr.regist_by "
                    + "INNER JOIN ord_pqr_reason AS r ON r.id = pqr.reason_id "
                    + "INNER JOIN city c ON c.id = IFNULL(i.city_id, IFNULL(cli.city_id,est.city_id)) "
                    + "INNER JOIN ord_office o ON o.id = pqr.office_id AND o.pqrs_app "
                    + "WHERE pqr.technician_id = " + tecId + " AND pqr.pqr_poll_id IS NULL AND pqr.anul_cause_id IS NULL AND pqr.satis_poll_id IS NULL)").getRecords(conn);

            List<PqrItem> pqrs = new ArrayList<>();
            if (data != null && data.length > 0) {
                for (Object[] row : data) {
                    PqrItem obj = new PqrItem();
                    obj.id = MySQLQuery.getAsInteger(row[0]);
                    obj.type = MySQLQuery.getAsInteger(row[1]);
                    obj.serial = MySQLQuery.getAsString(row[2]);
                    obj.client = MySQLQuery.getAsString(row[3]);
                    obj.address = MySQLQuery.getAsString(row[4]);
                    obj.phone = MySQLQuery.getAsString(row[5]);
                    obj.reason = MySQLQuery.getAsString(row[6]);
                    obj.enterprise = MySQLQuery.getAsString(row[7]);
                    obj.date = MySQLQuery.getAsDate(row[8]);
                    obj.hour = MySQLQuery.getAsDate(row[9]);
                    obj.contract = MySQLQuery.getAsString(row[10]);
                    obj.captured = MySQLQuery.getAsString(row[11]);
                    obj.arrivHour = MySQLQuery.getAsDate(row[12]);
                    obj.notes = MySQLQuery.getAsString(row[13]);
                    obj.mail = MySQLQuery.getAsString(row[14]);
                    obj.hasInformation = true;
                    obj.hasCheck = true;
                    obj.hasSummary = true;
                    pqrs.add(obj);
                }
            }
            return createResponse(pqrs, "dataPqrs" + sl.employeeId);
        }
    }

}
