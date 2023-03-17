package api.rpt.api;

import api.rpt.model.RptInfo;
import api.rpt.model.rptTbls.PivotTable;
import api.BaseAPI;
import api.rpt.api.dataTypes.DataType;
import api.rpt.model.rptTbls.BarTable;
import api.rpt.model.rptTbls.PieTable;
import api.rpt.model.RptCubeFld;
import api.rpt.model.RptRpt;
import api.rpt.model.RptRptFld;
import api.rpt.model.rptTbls.RptData;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;

@Path("/rptRpt")
public class RptRptApi extends BaseAPI {

    @POST
    public Response insert(RptRpt obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.created = new Date();
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(RptRpt obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            if (obj.created == null) {
                obj.created = new Date();
            }
            obj.update(conn);
            if ((obj.type.equals("pie") || obj.type.equals("donnut"))) {
                new MySQLQuery("DELETE FROM rpt_rpt_fld WHERE rpt_id = " + obj.id + " AND type = 'col'").executeUpdate(conn);
            } else if (obj.type.equals("table")) {
                new MySQLQuery("DELETE FROM rpt_rpt_fld WHERE rpt_id = " + obj.id + " AND (type = 'col' || type = 'join')").executeUpdate(conn);
            }
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            RptRpt obj = new RptRpt().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            RptRpt.delete(id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/replaceParent")
    public Response replaceParent(@QueryParam("rptId") int rptId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            RptRpt child = new RptRpt().select(rptId, conn);
            RptRpt parent = new RptRpt().select(child.drillOfId, conn);
            RptRpt.delete(parent.id, conn);
            //hay que hacer este query a mano porque el generador no está diseñado para cambiar el Id
            new MySQLQuery("UPDATE rpt_rpt SET drill_level = 0, id = " + parent.id + " WHERE id = " + child.id).executeUpdate(conn);
            //se cambian en el Obj para reportarle el cambio al front
            child.drillLevel = 0;
            child.id = parent.id;
            return createResponse(child);
        } catch (Exception ex) {
            return createResponse(ex);
        }

    }

    @GET
    @Path("/getByDash")
    public Response getByDash(@QueryParam("dashId") int dashId, @QueryParam("updateViews") boolean updateViews) {
        try (Connection con = getConnection()) {
            SessionLogin sl = getSession(con);
            if (updateViews) {
                new MySQLQuery("UPDATE rpt_dash SET last_view = NOW(), views = views + 1 WHERE id = " + dashId).executeUpdate(con);
            }
            String q;
            if (sl.employeeId == 1) {
                if (updateViews) {
                    if (new MySQLQuery("SELECT COUNT(*) > 0 FROM rpt_perm WHERE dash_id = ?1 AND emp_id = 1").setParam(1, dashId).getAsBoolean(con)) {
                        new MySQLQuery("UPDATE rpt_perm SET last_view = NOW(), views = views + 1 WHERE dash_id = ?1 AND emp_id = 1").setParam(1, dashId).setParam(2, sl.employeeId).executeUpdate(con);
                    } else {
                        new MySQLQuery("INSERT INTO rpt_perm SET last_view = NOW(), views = 1, dash_id = ?1, emp_id = 1, can_share = 1, active = 1").setParam(1, dashId).executeUpdate(con);
                    }
                }
                q = "SELECT " + RptRpt.getSelFlds("") + " FROM rpt_rpt WHERE drill_level = 0 AND dash_id = " + dashId;
            } else {
                if (updateViews) {
                    new MySQLQuery("UPDATE rpt_perm SET last_view = NOW(), views = views + 1 WHERE dash_id = ?1 AND emp_id = ?2").setParam(1, dashId).setParam(2, sl.employeeId).executeUpdate(con);
                }
                q = "SELECT DISTINCT "
                        + RptRpt.getSelFlds("r")
                        + "FROM "
                        + "rpt_rpt AS r "
                        + "INNER JOIN rpt_cube_profile AS cp ON r.cube_id = cp.cube_id "
                        + "INNER JOIN login AS pe ON cp.profile_id = pe.profile_id "
                        + "WHERE "
                        + "r.drill_level = 0 AND r.dash_id = " + dashId + " AND "
                        + "pe.employee_id = " + sl.employeeId + "";
            }
            return createResponse(RptRpt.getList(new MySQLQuery(q), con));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    /*@GET
    @Path("/getQueryData")
    public Response getQueryData(@QueryParam("rptId") int rptId) {
        try (Connection con = getConnection()) {
            SessionLogin sl = getSession(con);
            RptInfo rptInfo = new RptInfo(rptId, con);
            String q = GetReportQuery.getRptQuery(rptInfo);
            return Response.ok(new MySQLQuery(q).getRecordsAsList(con)).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }*/
    @GET
    @Path("/rptInfo")
    public Response getRptInfo(@QueryParam("rptId") int rptId) {
        try (Connection con = getConnection()) {
            RptInfo info = new RptInfo(rptId, con);
            return createResponse(info);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/data")
    public Response data(@QueryParam("rptId") int rptId) throws Exception {
        try (Connection con = getConnection()) {
            SessionLogin sess = getSession(con);
            long t = System.currentTimeMillis();
            RptData rta = new RptData();
            RptInfo info = new RptInfo(rptId, con);
            rta.info = info;
            if (info.rpt.isSetup) {
                String q = GetReportQuery.getRptQuery(info);
                if (sess.employeeId == 1) {
                    System.out.println(q);
                }
                switch (info.rpt.type) {
                    case "clustered":
                    case "stacked":
                    case "stacked100":
                    case "line":
                        rta.barTable = new BarTable(info, new MySQLQuery(q).getRecords(con));
                        break;
                    case "pie":
                    case "donnut":
                        rta.pieTable = new PieTable(info, new MySQLQuery(q).getRecords(con));
                        break;
                    case "pivot":
                    case "table":
                        rta.pivotTable = new PivotTable(info, new MySQLQuery(q).getRecords(con));
                        break;
                    default:
                        throw new Exception("Tipo no reconocido: " + info.rpt.type);
                }
            }
            setTime(info.rpt, t, con);
            return createResponse(rta);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getAsXlsReport")
    public Response getAsXlsReport(@QueryParam("rptId") int rptId) {
        try (Connection con = getConnection()) {
            long t = System.currentTimeMillis();
            RptInfo info = new RptInfo(rptId, con);
            String q = GetReportQuery.getRptQuery(info);
            Object[][] rawData = new MySQLQuery(q).getRecords(con);
            PivotTable pt = new PivotTable(info, rawData);
            File file = WriteExcel.writeTable(pt, info);
            setTime(info.rpt, t, con);
            return createResponse(file, file.getName());
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private static void setTime(RptRpt rpt, long t, Connection ep) throws Exception {
        int segs = (int) ((System.currentTimeMillis() - t) / 1000);
        new MySQLQuery("UPDATE rpt_rpt SET last_time = ?1 WHERE id = ?2").setParam(1, segs).setParam(2, rpt.id).executeUpdate(ep);
        if (rpt.drillLevel == 1 && rpt.drillOfId != null) {
            new MySQLQuery("UPDATE rpt_rpt SET last_time = ?2 WHERE id = ?1 AND drill_level = 0").setParam(1, rpt.drillOfId).setParam(2, segs).executeUpdate(ep);
        }
    }

    @GET
    @Path("/copyForOpening")
    public Response copyForOpening(@QueryParam("rptId") int rptId) {
        try (Connection con = getConnection()) {
            SessionLogin sl = getSession(con);
            RptRpt rpt = new RptRpt().select(rptId, con);
            rpt.drillOfId = rpt.id;
            rpt.created = new Date();
            rpt.drillLevel = 1;
            rpt.insert(con);

            List<RptRptFld> flds = RptRptFld.getByRptQuery(rptId, con);
            Map<Integer, Integer> oldToNew = new HashMap<>();
            for (int i = 0; i < flds.size(); i++) {
                RptRptFld fld = flds.get(i);
                fld.rptId = rpt.id;
                int oldId = fld.id;
                fld.insert(con);
                oldToNew.put(oldId, fld.id);
            }

            for (int i = 0; i < flds.size(); i++) {
                RptRptFld fld = flds.get(i);
                if (fld.fx != null) {
                    String origFx = fld.fx;
                    Iterator<Map.Entry<Integer, Integer>> it = oldToNew.entrySet().iterator();
                    while (it.hasNext()) {
                        Map.Entry<Integer, Integer> next = it.next();
                        int origId = next.getKey();
                        int newId = next.getValue();
                        fld.fx = fld.fx.replaceAll("@" + origId + "@", "@" + newId + "@");
                    }
                    if (!origFx.equals(fld.fx)) {
                        fld.update(con);
                    }
                }

                if (fld.kpiL1Id != null || fld.kpiL2Id != null || fld.kpiValId != null) {
                    if (fld.kpiL1Id != null) {
                        fld.kpiL1Id = oldToNew.get(fld.kpiL1Id);
                    }

                    if (fld.kpiL2Id != null) {
                        fld.kpiL2Id = oldToNew.get(fld.kpiL2Id);
                    }

                    if (fld.kpiValId != null) {
                        fld.kpiValId = oldToNew.get(fld.kpiValId);
                    }
                    fld.update(con);
                }
            }
            return Response.ok(rpt).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/drill")
    public Response drill(DrillRequest req) throws Exception {
        try (Connection con = getConnection()) {
            SessionLogin sl = getSession(con);
            RptInfo info = new RptInfo(req.rptId, con);
            RptRpt rpt = info.rpt;
            rpt.id = 0;
            rpt.drillLevel = info.rpt.drillLevel + 1;
            rpt.insert(con);

            for (int i = 0; i < req.fldKeys.size(); i++) {
                Object key = req.fldKeys.get(i);
                Integer fldId = req.fldIds.get(i);
                RptRptFld fld = RptRptFld.find(fldId, info.allrFlds);
                createFilt(con, info, fld, key);
            }
            filtAndDrill(con, rpt, info);
            return Response.ok(rpt).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private void createFilt(Connection con, RptInfo info, RptRptFld oFld, Object val) throws Exception {
        RptRptFld fld = (RptRptFld) RptDashFiltApi.deepCopy(oFld);
        fld.setCubeFld(RptCubeFld.find(fld.fldId, info.cubeFlds));
        DataType dt = DataType.getType(fld.getCubeFld().dataType);
        fld.type = "filt";
        fld.filtDesc = dt.getAsXlsString(val);
        fld.setFiltList(new ArrayList<>());
        if (dt.hasMaxMin()) {
            fld.setFiltType("eq");
            fld.getFiltList().add(val);
        } else if (fld.getCubeFld().unique) {
            fld.setFiltType("lst");
            String colQuery = GetReportQuery.getColQuery(fld);
            MySQLQuery mq = new MySQLQuery("SELECT id FROM " + fld.getCubeFld().tbls[0].tbl + " " + fld.getCubeFld().tbls[0].alias + " WHERE " + colQuery + " = ?1");
            mq.setParam(1, val);
            fld.getFiltList().add(mq.getSingleValue(con));
        } else {
            fld.setFiltType("lst");
            fld.getFiltList().add(val);
        }
        fld.rptId = info.rpt.id;
        fld.writeFileList();
        fld.insert(con);
    }

    private void filtAndDrill(Connection conn, RptRpt rpt, RptInfo info) throws Exception {
        //para reconectar las fórmulas e indicadores, como este método clona los joins hay que actualizar
        //los indices que le correspondieron a los insert de los joins dentro del MQ.

        Map<Integer, Integer> oldToNew = new HashMap<>();

        List<RptRptFld> flds = info.allrFlds;
        for (RptRptFld fld : flds) {
            fld.rptId = rpt.id;
            if (fld.type.equals("filt")) {
                boolean add = true;
                for (RptRptFld col : info.dims) {
                    if (Objects.equals(col.fldId, fld.fldId)) {
                        add = false;
                    }
                }
                if (add) {
                    fld.insert(conn);
                }
            } else if (fld.type.equals("col") || fld.type.equals("row") || fld.type.equals("join")) {
                if (fld.getCubeFld() != null && fld.getCubeFld().drillToId != null) {
                    fld.fldId = fld.getCubeFld().drillToId;
                }
                //para actualización de fórmulas
                if (fld.type.equals("join")) {
                    int oldId = fld.id;
                    fld.insert(conn);
                    oldToNew.put(oldId, fld.id);
                } else {
                    fld.insert(conn);
                }
            }
        }

        for (RptRptFld fld : flds) {
            if (fld.fx != null) {
                String origFx = fld.fx;
                Iterator<Map.Entry<Integer, Integer>> it = oldToNew.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Integer, Integer> next = it.next();
                    int origId = next.getKey();
                    int newId = next.getValue();
                    fld.fx = fld.fx.replaceAll("@" + origId + "@", "@" + newId + "@");
                }
                if (!origFx.equals(fld.fx)) {
                    MySQLQuery q = new MySQLQuery("UPDATE rpt_rpt_fld SET fx = ?1 WHERE id = ?2");
                    q.setParam(1, fld.fx);
                    q.setParam(2, fld.id);
                    q.executeUpdate(conn);
                }
            }

            if (fld.kpiL1Id != null || fld.kpiL2Id != null || fld.kpiValId != null) {
                if (fld.kpiL1Id != null) {
                    fld.kpiL1Id = oldToNew.get(fld.kpiL1Id);
                }

                if (fld.kpiL2Id != null) {
                    fld.kpiL2Id = oldToNew.get(fld.kpiL2Id);
                }

                if (fld.kpiValId != null) {
                    fld.kpiValId = oldToNew.get(fld.kpiValId);
                }
                MySQLQuery q = new MySQLQuery("UPDATE rpt_rpt_fld SET kpi_l1_id = ?1, kpi_l1_id = ?2, kpi_val_id = ?3 WHERE id = ?4");
                q.setParam(1, fld.kpiL1Id);
                q.setParam(2, fld.kpiL2Id);
                q.setParam(3, fld.kpiValId);
                q.setParam(4, fld.id);
                q.executeUpdate(conn);
            }
        }
    }
}
