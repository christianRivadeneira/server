package api.ess.api;

import api.BaseAPI;
import api.MultiPartRequest;
import api.chl.model.ChlRequestImport;
import api.ess.dto.EventDetail;
import api.ess.dto.EventsByEmpAndRangeQuery;
import api.ess.model.EssEvent;
import api.sys.dto.Notify;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import org.apache.commons.lang3.ArrayUtils;
import utilities.MySQLQuery;
import utilities.importer.Importer;
import utilities.importer.ImporterCol;
import utilities.json.JSONDecoder;
import utilities.xlsReader.XlsReader;
import web.fileManager;

@Path("/essEvent")
public class EssEventApi extends BaseAPI {

    @POST
    public Response insert(EssEvent obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.regDate = new Date();
            obj.empId = sl.employeeId;
            obj.isImport = false;
            obj.insert(conn);

            if (obj.unitId != null) {
                String empIds = new MySQLQuery("SELECT group_concat(cast(p.emp_id AS CHAR)) FROM ess_unit u "
                        + "INNER JOIN ess_person_unit pu ON u.id = pu.unit_id "
                        + "INNER JOIN ess_person p ON p.id = pu.person_id "
                        + "WHERE u.id = ?1").setParam(1, obj.unitId).getAsString(conn);

                Notify n = new Notify();
                n.empIds = empIds;
                n.title = "Seguridad del sur te informa";

                String regDate = JSONDecoder.encodeDate(obj.regDate);
                String notes = obj.notes;

                StringBuilder sb = new StringBuilder();
                sb.append("{");
                sb.append("\"regDate\":\"").append(regDate).append("\",");
                sb.append("\"notes\":\"").append(notes).append("\",");

                if (obj.authById != null) {
                    String authName = new MySQLQuery("SELECT CONCAT(first_name, ' ', last_name) FROM ess_person e WHERE e.id = " + obj.authById).getAsString(conn);
                    sb.append("\"authName\":\"").append(authName).append("\",");
                }
                if (obj.empId != null) {
                    String operator = new MySQLQuery("SELECT CONCAT(first_name, ' ', last_name) FROM employee e WHERE e.id = " + obj.empId).getAsString(conn);
                    sb.append("\"operator\":\"").append(operator).append("\"");
                }
                sb.append("}");

                n.message = sb.toString();
                Notify.sendNotification(conn, n);
            }
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(EssEvent obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            EssEvent old = new EssEvent().select(obj.id, conn);
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
            EssEvent obj = new EssEvent().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getDetail")
    public Response getDetail(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            EssEvent obj = new EssEvent().select(id, conn);

            StringBuilder sb = new StringBuilder();
            sb.append("<b>Detalle: </b>").append(MySQLQuery.isEmpty(obj.notes) ? "" : obj.notes).append("<br>");

            if (!MySQLQuery.isEmpty(obj.buildId)) {
                String buildName = new MySQLQuery("SELECT name FROM ess_building WHERE id = " + obj.buildId).getAsString(conn);
                sb.append("<b>Edificio: </b>").append(buildName).append("<br>");
            }
            if (!MySQLQuery.isEmpty(obj.pkgNum)) {
                sb.append("<b>Paquete Con Guia No.: </b>").append(obj.pkgNum).append("<br>");
            }
            if (obj.dropLocationId != null) {
                String location = new MySQLQuery("SELECT name FROM ess_drop_location WHERE id = " + obj.dropLocationId).getAsString(conn);
                sb.append("<b>Ubicación: </b>").append(location).append("<br>");
            }

            if (!MySQLQuery.isEmpty(obj.vhPlate)) {
                sb.append("<b>Placa: </b>").append(obj.vhPlate).append("<br>");
            }

            if (obj.enterpriseId != null) {
                String enterpriseName = new MySQLQuery("SELECT name FROM ess_enterprise WHERE id = " + obj.enterpriseId).getAsString(conn);
                sb.append("<b>Empresa: </b>").append(enterpriseName).append("<br>");
            }

            if (obj.authById != null) {
                String authName = new MySQLQuery("SELECT CONCAT(first_name, ' ', last_name) FROM ess_person e WHERE e.id = " + obj.authById).getAsString(conn);
                sb.append("<b>Autorizó: </b>").append(authName).append("<br>");
            }
            if (obj.empId != null) {
                String operator = new MySQLQuery("SELECT CONCAT(first_name, ' ', last_name) FROM employee e WHERE e.id = " + obj.empId).getAsString(conn);
                sb.append("<b>Operador: </b>").append(operator).append("<br>");
            }

            EventDetail rta = new EventDetail();
            rta.id = obj.id;
            rta.regDate = obj.regDate;
            rta.eventTypeName = getEventTypeName(obj, conn);
            rta.description = sb.toString();

            return Response.ok(rta).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            EssEvent.delete(id, conn);
            SysCrudLog.deleted(this, EssEvent.class, id, conn);
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
            return createResponse(EssEvent.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/getByEmp")
    public Response getByEmp(EventsByEmpAndRangeQuery eventsQuery) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            List<EssEvent> list = new ArrayList<>();
            String buildAdms = null;
            Object[][] data = null;
            boolean asAdmin = eventsQuery.asAdmin;
            int empId = eventsQuery.empId;

            Object[][] units = new MySQLQuery(" SELECT pu.unit_id, pu.notify "
                    + " FROM ess_person p "
                    + " INNER JOIN ess_person_unit pu ON pu.person_id = p.id "
                    + " WHERE p.emp_id = ?1").setParam(1, empId).getRecords(conn);

            if (asAdmin) {
                buildAdms = new MySQLQuery("SELECT GROUP_CONCAT(ba.build_id) "
                        + " FROM ess_build_admin ba "
                        + " INNER JOIN ess_person p ON p.id = ba.person_id "
                        + " WHERE p.emp_id = ?1 ").setParam(1, empId).getAsString(conn);
            }

            if (asAdmin) {
                if (buildAdms != null && !buildAdms.isEmpty()) {
                    data = new MySQLQuery(
                            "SELECT e.id, e.reg_date, et.name, e.type, e.is_import "
                            + "FROM ess_event e "
                            + "LEFT JOIN ess_event_type et ON et.id = e.event_type_id "
                            + "WHERE e.build_id IN (" + buildAdms + ") "
                            + "AND e.reg_date BETWEEN ?1 "
                            + "AND ?2 "
                            + "ORDER BY e.reg_date DESC "
                    )
                            .setParam(1, eventsQuery.start)
                            .setParam(2, eventsQuery.end)
                            .getRecords(conn);
                }
            } else {
                if (units != null && units.length > 0) {
                    for (Object[] unit : units) {
                        int unitId = MySQLQuery.getAsInteger(unit[0]);
                        boolean notify = MySQLQuery.getAsBoolean(unit[1]);//de todo el apto
                        Object[][] partialData;
                        if (notify) {
                            partialData = new MySQLQuery(
                                    "SELECT e.id, e.reg_date, et.name, e.type, e.is_import "
                                    + "FROM ess_event e "
                                    + "LEFT JOIN ess_event_type et ON et.id = e.event_type_id "
                                    + "WHERE e.unit_id = ?2 "
                                    + "AND reg_date BETWEEN ?3 "
                                    + "AND ?4 ORDER BY e.reg_date DESC "
                            )
                                    .setParam(2, unitId)
                                    .setParam(3, eventsQuery.start)
                                    .setParam(4, eventsQuery.end)
                                    .getRecords(conn);

                        } else {
                            partialData = new MySQLQuery(
                                    "SELECT e.id, e.reg_date, et.name, e.type, e.is_import "
                                    + "FROM ess_event e "
                                    + "LEFT JOIN ess_event_type et ON et.id = e.event_type_id "
                                    + "INNER JOIN ess_person esp ON esp.id = e.auth_by_id "
                                    + "WHERE esp.emp_id = ?1 AND e.unit_id = ?2 "
                                    + "AND reg_date BETWEEN ?3 "
                                    + "AND ?4 ORDER BY e.reg_date DESC"
                            )
                                    .setParam(1, empId)
                                    .setParam(2, unitId)
                                    .setParam(3, eventsQuery.start)
                                    .setParam(4, eventsQuery.end)
                                    .getRecords(conn);
                        }

                        if (data == null) {
                            data = partialData;
                        } else {
                            data = ArrayUtils.addAll(data, partialData);
                        }
                    }
                }
            }

            if (data != null) {
                for (Object[] obj : data) {
                    EssEvent ev = new EssEvent();
                    ev.id = MySQLQuery.getAsInteger(obj[0]);
                    ev.regDate = MySQLQuery.getAsDate(obj[1]);
                    ev.typeName = MySQLQuery.getAsString(obj[2]);
                    ev.type = MySQLQuery.getAsString(obj[3]);
                    ev.isImport = MySQLQuery.getAsBoolean(obj[4]);
                    if (MySQLQuery.isEmpty(ev.typeName)) {
                        ev.typeName = getEventTypeName(ev, conn);
                    }
                    list.add(ev);
                }
            }

            return createResponse(list);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private String getEventTypeName(EssEvent ev, Connection conn) throws Exception {
        String name = "";
        switch (ev.type) {
            case "norm":
                if (ev.isImport) {
                    name = "Evento de Huellero";
                } else {
                    String evTypeName = new MySQLQuery("SELECT name FROM ess_event_type WHERE id = " + ev.eventTypeId).getAsString(conn);
                    name = MySQLQuery.isEmpty(evTypeName) ? "Evento" : evTypeName;
                }
                break;
            case "pkg":
                name = "Recepción de Paquetes";
                break;
            case "vh":
                name = "Ingreso de Vehículo";
                break;
            default:
                name = "Evento no definido";
                break;
        }
        return name;
    }

    @POST
    @Path("/import")
    public Response importRequest(@Context HttpServletRequest request) throws Exception {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);

            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);

            Map<String, String> pars = mr.params;

            ChlRequestImport reqImport = new ChlRequestImport();
            reqImport.empId = 1; // se importan a nombre del admin admin 
            Date importDate = new Date();
            Integer buildId = MySQLQuery.getAsInteger(pars.get("buildId"));
            if (buildId == null) {
                throw new Exception("Seleccione un Edificio");
            }

            if (!mr.getFile().isXls()) {
                throw new Exception("El archivo no tiene el formato .xls");
            }

            Object[][] data = XlsReader.readExcel(mr.getFile().file.getPath(), 0).getData();

            if (data == null || data.length < 2) {
                throw new Exception("El archivo no contiene registros");
            }

            List<EventExcel> allRows = new ArrayList<>();

            List<ImporterCol> cols = new ArrayList<>();
            cols.add(new ImporterCol("Time", ImporterCol.TYPE_DATE, false));
            cols.add(new ImporterCol("User ID", ImporterCol.TYPE_TEXT, true));
            cols.add(new ImporterCol("Name", ImporterCol.TYPE_TEXT, true));
            cols.add(new ImporterCol("Card No.", ImporterCol.TYPE_TEXT, true));
            cols.add(new ImporterCol("Device", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("Door", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("Event", ImporterCol.TYPE_TEXT, true));
            cols.add(new ImporterCol("Verification Method", ImporterCol.TYPE_TEXT, true));
            cols.add(new ImporterCol("Access direction", ImporterCol.TYPE_TEXT, true));

            Importer importer = new Importer(data, cols);

            for (int i = importer.headRow + 1; i < data.length; i++) {
                importer.row = data[i];
                if (!isAllWhite(importer)) {
                    importer.validateValues(importer.row, i);
                    EventExcel get = new EventExcel();
                    get.regNum = i;
                    get.date = MySQLQuery.getAsDate(MySQLQuery.getAsString(importer.get(0)).replaceAll("'", ""));
                    String code = MySQLQuery.getAsString(importer.get(3));
                    if (code != null && !code.isEmpty()) {
                        Integer unitId = new MySQLQuery("SELECT u.id "
                                + "FROM ess_person p "
                                + "INNER JOIN ess_person_unit pu ON pu.person_id = p.id "
                                + "INNER JOIN ess_unit u ON u.id = pu.unit_id "
                                + "WHERE u.build_id = " + buildId + " AND p.f_print_code = '" + code + "' LIMIT 1").getAsInteger(conn);
                        if (unitId == null) {
                            throw new Exception("No se encontró el código " + code);
                        }
                        get.unitId = unitId;
                    }

                    String device = importer.get(4) == null ? "" : MySQLQuery.getAsString(importer.get(4)).trim();
                    String event = importer.get(6) == null ? "" : MySQLQuery.getAsString(importer.get(6)).trim();
                    get.notes = device + " - " + event;
                    allRows.add(get);
                }
            }

            allRows = sortByProvider((ArrayList<EventExcel>) allRows);

            int curUnit = -1;
            for (EventExcel req : allRows) {
                if (req.unitId != null && curUnit != req.unitId) {
                    curUnit = req.unitId;
                } else {
                    req.unitId = curUnit;
                }
            }

            for (EventExcel req : allRows) {
                EssEvent ev = new EssEvent();
                ev.regDate = req.date;
                ev.notes = req.notes;
                ev.unitId = req.unitId;
                //ev.buildId = buildId;
                ev.active = true;
                ev.isImport = true;
                ev.empId = sl.employeeId;
                ev.importDt = importDate;
                ev.type = "norm";
                ev.insert(conn);
            }

            return createResponse("ok");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private boolean isAllWhite(Importer imp) {
        if (imp.row[6] == null) {
            return true;
        }
        return imp.isAllWhite();
    }

    private class EventExcel implements Comparable<EventExcel> {

        public int regNum;
        public Date date;
        public Integer unitId;
        public String notes;

        public Comparator<EventExcel> provComparator = new Comparator<EventExcel>() {
            @Override
            public int compare(EventExcel o1, EventExcel o2) {
                int obj1 = o1.regNum;
                int obj2 = o2.regNum;
                return obj2 - obj1;
            }
        };

        @Override
        public int compareTo(EventExcel o) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    private List<EventExcel> sortByProvider(List<EventExcel> data) {
        EventExcel[] dataArray = data.toArray(new EventExcel[data.size()]);
        Arrays.sort(dataArray, new EventExcel().provComparator);
        return new ArrayList(Arrays.asList(dataArray));
    }
}
