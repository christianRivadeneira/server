package api.per.api;

import api.BaseAPI;
import api.MultiPartRequest;
import api.per.dto.GateEvent;
import api.per.model.RegisterPerGateEvent;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import utilities.Base64;
import utilities.json.JSONDecoder;
import web.fileManager;

@Path("/perGateEvent")
public class PerGateEventApi extends BaseAPI {

    @GET
    @Path("/regEvent")
    public Response regEvent(@QueryParam("empId") int empId,
            @QueryParam("ownerType") int ownerType,
            @QueryParam("hasReplace") boolean hasReplace,
            @QueryParam("regOfficeId") Integer regOfficeId,
            @QueryParam("nearestEvent") boolean nearestEvent,
            @QueryParam("tempPrintId") Integer tempPrintId,
            @QueryParam("normalEvent") boolean normalEvent,
            @QueryParam("regOfficeName") String regOfficeName) {
        try (Connection con = getConnection()) {
            getSession(con);
            RegisterPerGateEvent d = RegisterPerGateEvent.registerEvent(empId, ownerType, hasReplace, regOfficeId, nearestEvent, tempPrintId, normalEvent, regOfficeName, null, con);
            return Response.ok(d).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/regFromFile")
    public Response regFromFile(@Context HttpServletRequest request) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);
            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 1024);

            File f = new File(mr.getFile().file.getPath());
            List<GateEvent> lstGateEvents = new ArrayList<>();

            if (f.exists()) {
                try (BufferedReader br = new BufferedReader(new FileReader(f))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        line = new String(Base64.decode(line));
                        ByteArrayInputStream bais = new ByteArrayInputStream(line.getBytes(StandardCharsets.UTF_8));
                        GateEvent obj = (GateEvent) new JSONDecoder().getObject(bais, GateEvent.class);
                        RegisterPerGateEvent d = RegisterPerGateEvent.registerEvent(
                                obj.empId, obj.ownerType, obj.hasReplace,
                                obj.regOfficeId, obj.nearestEvent, obj.tempPrintId,
                                obj.normalEvent, obj.regOfficeName, obj.regTime, conn);
                        lstGateEvents.add(obj);
                    }
                    System.out.println(new Date() + "\nsize events offline: " + lstGateEvents.size());
                    return createResponse("ok");
                } catch (Exception ex) {
                    Logger.getLogger(PerGateEventApi.class.getName()).log(Level.SEVERE, null, ex);
                    return createResponse(ex);
                }
            }
            return createResponse("none");
        } catch (Exception ex) {
            Logger.getLogger(PerGateEventApi.class.getName()).log(Level.SEVERE, null, ex);
            return createResponse(ex);
        }
    }

}
