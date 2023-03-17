package api.rpt.api;

import api.BaseAPI;
import static api.rpt.api.GetReportQuery.findDepTbls;

import static api.rpt.api.GetReportQuery.getColQuery;
import api.rpt.model.RptCubeFld;
import api.rpt.model.RptCubeTbl;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import api.rpt.model.RptDashFilt;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import utilities.MySQLQuery;
import utilities.json.JSONDecoder;
import utilities.json.JSONEncoder;

@Path("/rptDashFilt")
public class RptDashFiltApi extends BaseAPI {

    @POST
    public Response insert(RptDashFilt obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(RptDashFilt obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.update(conn);

            MySQLQuery q = new MySQLQuery("UPDATE rpt_rpt_fld SET filt_type = ?1, filt_json = ?2, filt_desc = ?3 WHERE dash_filt_id = " + obj.id);            
            q.setParam(1, obj.filtType);
            q.setParam(2, obj.filtJson);
            q.setParam(3, obj.filtDesc);
            q.executeUpdate(conn);

            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            RptDashFilt obj = new RptDashFilt().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            RptDashFilt.delete(id, conn);
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
            return createResponse(RptDashFilt.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getFiltData")
    public Response getFiltData(@QueryParam("dashFiltId") int dashFiltId) {
        try (Connection con = getConnection()) {
            SessionLogin sl = getSession(con);
            RptDashFilt df = new RptDashFilt().select(dashFiltId, con);
            RptCubeFld cf = new RptCubeFld().select(df.fldId, con);
            List<RptCubeTbl> allCTbls = RptCubeTbl.getByCubeQuery(cf.cubeId, con);
            cf.setTables(allCTbls);

            List<RptCubeTbl> selTbls = new ArrayList<>();
            for (RptCubeTbl tbl : cf.tbls) {
                tbl.setTables(allCTbls);
                findDepTbls(tbl, allCTbls, selTbls);
            }
            GetReportQuery.sortTbls(selTbls);

            int k;
            boolean found = false;
            Integer[] tblIds = cf.getTblIds();
            for (k = 0; k < selTbls.size() && !found; k++) {
                for (int j = 0; j < tblIds.length && !found; j++) {
                    if (selTbls.get(k).id == tblIds[j]) {
                        k--;
                        found = true;
                    }
                }
            }

            List<RptCubeTbl> neededTbls = new ArrayList<>();
            for (int i = k; i < selTbls.size(); i++) {
                //neededTbls.add((RptCubeTbl) deepCopy(selTbls.get(i)));
                neededTbls.add(selTbls.get(i));
            }

            neededTbls.get(0).type = "main";

            PlainBuilder sb = new PlainBuilder();
            sb.add("SELECT DISTINCT ");
            if (cf.unique) {
                sb.add(neededTbls.get(0).alias).add(".id, ");
            }
            sb.add(getColQuery(cf)).add(", FALSE").br();
            GetReportQuery.addFrom(sb, neededTbls);

            return Response.ok(new MySQLQuery(sb.toString()).getRecordsAsList(con)).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    public static Object deepCopy(Object orig) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JSONEncoder.encode(orig, baos, false);
        baos.close();
        byte[] bytes = baos.toByteArray();
        return new JSONDecoder().getObject(new ByteArrayInputStream(bytes), orig.getClass());
    }
}
