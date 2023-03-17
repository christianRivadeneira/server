package api.rpt.api;

import api.rpt.model.RptInfo;
import api.BaseAPI;
import static api.rpt.api.GetReportQuery.addFrom;
import static api.rpt.api.GetReportQuery.addWhere;
import static api.rpt.api.GetReportQuery.findDepTbls;
import static api.rpt.api.GetReportQuery.getColQuery;
import static api.rpt.api.GetReportQuery.sortTbls;
import api.rpt.model.RptCubeCond;
import api.rpt.model.RptCubeFld;
import api.rpt.model.RptCubeTbl;
import api.rpt.model.RptRptFld;
import java.sql.Connection;
import java.util.ArrayList;
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

@Path("/rptRptFld")
public class RptRptFldApi extends BaseAPI {

    @POST
    public Response insert(RptRptFld obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            Integer nPlace = new MySQLQuery("SELECT max(place) + 1 FROM rpt_rpt_fld WHERE rpt_id = ?1 AND type = ?2").setParam(1, obj.rptId).setParam(2, obj.type).getAsInteger(conn);
            obj.place = (nPlace == null ? 1 : nPlace);
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(RptRptFld obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.update(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            RptRptFld obj = new RptRptFld().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            RptRptFld obj = new RptRptFld().select(id, conn);
            RptRptFld.delete(id, conn);
            MySQLQuery q = new MySQLQuery("UPDATE rpt_rpt_fld SET place = place - 1 WHERE rpt_id = ?1 AND type = ?2 AND place >= ?3");
            q.setParam(1, obj.rptId);
            q.setParam(2, obj.type);
            q.setParam(3, obj.place);
            q.executeUpdate(conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    public static List<RptRptFld> getByRptAndType(int rptId, String type, Connection conn) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT "
                + RptRptFld.getSelFlds("f")
                + "FROM "
                + "rpt_rpt_fld f "
                + "WHERE f.rpt_id = ?1 AND f.type = ?2").setParam(1, rptId).setParam(2, type);
        return RptRptFld.getList(q, conn);
    }

    @GET
    @Path("/getFiltData")
    public Response getFiltData(@QueryParam("rptFldId") int rptFldId) {
        try (Connection con = getConnection()) {
            SessionLogin sl = getSession(con);

            RptRptFld fFld = new RptRptFld().select(rptFldId, con);
            RptInfo info = new RptInfo(fFld.rptId, con);
            RptCubeFld getCubeFld = RptCubeFld.find(fFld.fldId, info.cubeFlds);

            List<RptCubeTbl> tbls = new ArrayList<>();
            for (RptCubeTbl tbl : getCubeFld.tbls) {
                findDepTbls(tbl, info.cubeTbls, tbls);
            }

            for (RptCubeCond cond : info.cubeConds) {
                for (RptCubeTbl tbl : cond.tbls) {
                    findDepTbls(RptCubeTbl.find(tbl.id, info.cubeTbls), info.cubeTbls, tbls);
                }
            }

            for (RptRptFld flt : info.filts) {
                for (RptCubeTbl tbl : flt.getCubeFld().tbls) {
                    findDepTbls(tbl, info.cubeTbls, tbls);
                }
            }
            sortTbls(tbls);

            PlainBuilder hb = new PlainBuilder();
            hb.add("SELECT DISTINCT ").br();
            if (getCubeFld.unique) {
                hb.add(getCubeFld.tbls[0].alias).add(".id, ");
            }
            hb.add(getColQuery(getCubeFld)).add(", FALSE").br();
            addFrom(hb, tbls);
            addWhere(hb, info, fFld.id);

            hb.br().add("ORDER BY ").br();
            hb.add(getColQuery(getCubeFld));

            if (sl.employeeId == 1) {
                System.out.println(hb.toString());
            }
            return Response.ok(new MySQLQuery(hb.toString()).getRecordsAsList(con)).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
