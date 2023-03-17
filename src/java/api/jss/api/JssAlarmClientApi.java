package api.jss.api;

import api.BaseAPI;
import api.MultiPartRequest;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import api.jss.model.JssAlarmClient;
import api.jss.model.JssClientZone;
import api.jss.model.JssSpan;
import api.jss.model.JssZone;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import utilities.MySQLQuery;
import utilities.Strings;
import utilities.apiClient.StringResponse;
import utilities.importer.Importer;
import utilities.importer.ImporterCol;
import utilities.xlsReader.XlsReader;
import web.fileManager;

@Path("/jssAlarmClient")
public class JssAlarmClientApi extends BaseAPI {

    @POST
    public Response insert(JssAlarmClient obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(JssAlarmClient obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            JssAlarmClient old = new JssAlarmClient().select(obj.id, conn);
            obj.update(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            JssAlarmClient obj = new JssAlarmClient().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            JssAlarmClient.delete(id, conn);
            SysCrudLog.deleted(this, JssAlarmClient.class, id, conn);
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
            return createResponse(JssAlarmClient.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/comClients")
    public Response getComClients(@QueryParam("empId") Integer empId) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            return createResponse(JssAlarmClient.getComClients(empId, conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/comClientsVisits")
    public Response getComClientsVisits(@QueryParam("empId") Integer empId) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            return createResponse(JssAlarmClient.getComClientsVisits(empId, conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/updateQr")
    public Response updateQr(JssAlarmClient obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);

            JssAlarmClient selectCode = JssAlarmClient.selectCode(obj.code, conn);
            if (selectCode != null) {
                throw new Exception("Ya se encuentra registrado un cliente con este codigo, por favor verificar");
            }

            JssAlarmClient old = new JssAlarmClient().select(obj.id, conn);
            JssAlarmClient newObj = old;
            newObj.lat = obj.lat;
            newObj.lon = obj.lon;
            newObj.code = obj.code;
            newObj.update(conn);
            SysCrudLog.updated(this, newObj, old, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/importClient")
    public Response importSchedule(@Context HttpServletRequest request) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);

            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);

            if (!mr.getFile().isXls()) {
                throw new Exception("El archivo no es una hoja de cáculo excel .xls");
            }
            Object[][] data = XlsReader.readExcel(mr.getFile().file.getPath(), 0).getData();

            if (data == null || data.length < 2) {
                throw new Exception("El archivo no contiene registros");
            }

            List<ImporterCol> cols = new ArrayList<>();
            cols.add(new ImporterCol("Nombre", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("Cuenta", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("Doc", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("Cod ruta", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("Dirección 1", ImporterCol.TYPE_TEXT, false));

            Importer importer = new Importer(data, cols);

            for (int i = importer.headRow + 1; i < data.length; i++) {
                importer.row = data[i];
                if (importer.row.length > 0) {
                    String acc = MySQLQuery.getAsString(importer.get(1));
                    JssAlarmClient old = JssAlarmClient.selectAcc(acc, conn);

                    String nameZone = Strings.toTitleType(MySQLQuery.getAsString(importer.get(3)) != null ? MySQLQuery.getAsString(importer.get(3)) : "");
                    JssZone zone = JssZone.selectName(nameZone, conn);

                    if (zone == null && nameZone != null && !nameZone.equals("")) {
                        zone = new JssZone();
                        zone.name = nameZone;
                        zone.goal = 0;
                        zone.insert(conn);
                    } else if (zone != null && nameZone != null && !nameZone.equals("")) {
                        zone.name = nameZone;
                        zone.update(conn);
                    }

                    int clientId;
                    if (old == null) {
                        JssAlarmClient obj = new JssAlarmClient();
                        obj.name = Strings.toTitleType(MySQLQuery.getAsString(importer.get(0)) != null ? MySQLQuery.getAsString(importer.get(0)) : "");
                        obj.acc = MySQLQuery.getAsString(importer.get(1));
                        obj.document = MySQLQuery.getAsString(importer.get(2));
                        obj.address = Strings.toTitleType(MySQLQuery.getAsString(importer.get(4)) != null ? MySQLQuery.getAsString(importer.get(4)) : "");
                        clientId = obj.insert(conn);
                    } else {
                        old.name = Strings.toTitleType(MySQLQuery.getAsString(importer.get(0)) != null ? MySQLQuery.getAsString(importer.get(0)) : "");
                        old.acc = MySQLQuery.getAsString(importer.get(1));
                        old.document = MySQLQuery.getAsString(importer.get(2));
                        old.address = Strings.toTitleType(MySQLQuery.getAsString(importer.get(4)) != null ? MySQLQuery.getAsString(importer.get(4)) : "");
                        old.update(conn);
                        clientId = old.id;
                    }

                    JssSpan jssSpan = JssSpan.selectNow(conn);

                    if (jssSpan == null) {
                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.DAY_OF_MONTH, 1);
                        jssSpan = new JssSpan();
                        jssSpan.begDt = cal.getTime();
                        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                        jssSpan.endDt = cal.getTime();
                        jssSpan.insert(conn);
                    }

                    JssClientZone clientZone = JssClientZone.selectClient(clientId, conn);

                    if (clientZone == null && zone != null) {
                        clientZone = new JssClientZone();
                        clientZone.clientId = clientId;
                        clientZone.zoneId = zone.id;
                        clientZone.insert(conn);
                    }
                }
            }

            return createResponse("ok");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/importUpdateMail")
    public Response importUpdateMail(@Context HttpServletRequest request) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);

            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);

            if (!mr.getFile().isXls()) {
                throw new Exception("El archivo no es una hoja de cáculo excel .xls");
            }
            Object[][] data = XlsReader.readExcel(mr.getFile().file.getPath(), 0).getData();

            if (data == null || data.length < 2) {
                throw new Exception("El archivo no contiene registros");
            }

            List<ImporterCol> cols = new ArrayList<>();
            cols.add(new ImporterCol("Cliente", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("Nro. docto. cruce", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("Total COP", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("Email", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("SECTOR", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("CODIGO", ImporterCol.TYPE_TEXT, false));

            Importer importer = new Importer(data, cols);

            for (int i = importer.headRow + 1; i < data.length; i++) {
                importer.row = data[i];
                if (importer.row.length > 0) {
                    List<JssAlarmClient> lstClients = new ArrayList<>();

                    String acc = MySQLQuery.getAsString(importer.get(5));
                    JssAlarmClient old = JssAlarmClient.selectAcc(acc, conn);

                    if (old == null) {
                        String doc = MySQLQuery.getAsString(importer.get(0));
                        lstClients = JssAlarmClient.selectDocument(doc, conn);
                    } else {
                        lstClients.add(old);
                    }

                    if (lstClients != null && lstClients.size() > 0) {
                        for (JssAlarmClient row : lstClients) {
                            row.mail = MySQLQuery.getAsString(importer.get(3));
                            row.type = (MySQLQuery.getAsString(importer.get(4)).contains("COMERCIAL") ? "com"
                                    : MySQLQuery.getAsString(importer.get(4)).contains("RESIDENCIAL") ? "res" : null);
                            row.notes = MySQLQuery.getAsString(importer.get(1)) + " - $" + MySQLQuery.getAsString(importer.get(2));
                            row.update(conn);
                        }
                    }
                }
            }

            return createResponse("ok");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getLocation")
    public Response getLocation(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            JssAlarmClient client = new JssAlarmClient().select(id, conn);
            StringResponse response;
            if (client.lat != null) {
                response = new StringResponse("SUCCESS", "https://www.google.com/maps/dir//" + client.lat + "," + client.lon + "/@" + client.lat + "," + client.lon + ",19z");
            } else {
                response = new StringResponse("ERROR", "No se ha ingresado coordenadas del cliente");
            }
            return Response.ok(response).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
