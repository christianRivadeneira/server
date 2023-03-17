package api.hlp.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.crm.model.CrmProject;
import api.hlp.model.HlpPrjBacklog;
import api.hlp.model.HlpRequest;
import api.hlp.model.HlpSpanRequest;
import api.hlp.model.HlpType;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import metadata.model.Table;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import utilities.ServerNow;
import utilities.cast;

//lista de programadores muesta gente que no es, agregar banderas
//anchos columnas
//logs
//eliminar baclog
@Path("/hlpPrjBacklog")
public class HlpPrjBacklogApi extends BaseAPI {

    @POST
    public Response insert(HlpPrjBacklog obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Integer place = new MySQLQuery("SELECT MAX(place) FROM hlp_prj_backlog WHERE prj_id = ?1").setParam(1, obj.prjId).getAsInteger(conn);
            if (place == null) {
                place = 0;
            } else {
                place++;
            }
            obj.place = place;
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(HlpPrjBacklog obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            HlpPrjBacklog old = new HlpPrjBacklog().select(obj.id, conn);
            if (obj.status.equals("done")) {
                if (new MySQLQuery("SELECT COUNT(*) > 0 FROM "
                        + "hlp_request r "
                        + "WHERE r.prj_backlog_id = ?1 AND r.state = 'clos'").setParam(1, obj.id).getAsBoolean(conn)) {
                    throw new Exception("Quedan casos por cerrar");
                }
            }

            MySQLQuery q = new MySQLQuery("UPDATE hlp_request notes = ?1, subject = ?2 WHERE prj_backlog_id = ?3");
            q.setParam(1, obj.description);
            q.setParam(2, obj.name);
            q.setParam(3, obj.id);
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
            HlpPrjBacklog obj = new HlpPrjBacklog().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            HlpPrjBacklog.delete(id, conn);
            SysCrudLog.deleted(this, HlpPrjBacklog.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/grid")
    public Response getGrid(@QueryParam("prjId") Integer prjId, @QueryParam("status") String status, @QueryParam("develId") Integer develId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GridResult tbl = new GridResult();

            String curDevelQ = "SELECT IFNULL(e.short_name, CONCAT(e.first_name,  ' ', e.last_name)) FROM "
                    + "hlp_request r "
                    + "INNER JOIN hlp_span_request s ON r.id = s.case_id "
                    + "INNER JOIN employee e ON e.id = s.emp_incharge_id "
                    + "WHERE r.prj_backlog_id = b.id AND s.end_date IS NULL ";

            String timeQ = ""
                    + "SELECT CONCAT(LPAD(FLOOR(SUM(TO_SECONDS(s.end_date) - TO_SECONDS(s.reg_date))/3600), 2, '0'), ':', LPAD(CEIL(MOD(SUM(TO_SECONDS(s.end_date) - TO_SECONDS(s.reg_date)), 3600)/60), 2, '0')) FROM "
                    + "hlp_request r "
                    + "INNER JOIN hlp_span_request s ON r.id = s.case_id "
                    + "INNER JOIN employee e ON e.id = s.emp_incharge_id "
                    + "WHERE r.prj_backlog_id = b.id AND s.end_date IS NOT NULL ";

            MySQLQuery q = new MySQLQuery("SELECT "
                    + "b.id, "
                    + "IFNULL(e.short_name, CONCAT(e.first_name, ' ', e.last_name)), "
                    + "(" + curDevelQ + "), "
                    + "b.name, "
                    + "b.priority, "
                    + "b.`status`, "
                    + "b.status_notes, "
                    + "CONCAT(LPAD(b.aprox_hrs, 2, '0'), ':00'), "
                    + "(" + timeQ + ") "
                    + "FROM "
                    + "hlp_prj_backlog b "
                    + "LEFT JOIN employee e ON e.id = b.devel_id "
                    + "WHERE "
                    + "b.prj_id = ?1 "
                    + (status != null && !status.isEmpty() ? "AND b.`status` = ?2 " : "")
                    + (develId != null ? "AND b.devel_id = ?3 " : "")
                    + "ORDER BY b.place");
            q.setParam(1, prjId);
            q.setParam(2, status);
            q.setParam(3, develId);

            tbl.data = q.getRecords(conn);
            tbl.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 180, "Encargado"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 180, "Ejecutando"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 180, "Tarea"),
                new MySQLCol(MySQLCol.TYPE_ENUM, 180, "Prioridad", Table.getByName("hlp_prj_backlog").getFieldByName("priority").emunOpts),
                new MySQLCol(MySQLCol.TYPE_ENUM, 180, "Estado", Table.getByName("hlp_prj_backlog").getFieldByName("status").emunOpts),
                new MySQLCol(MySQLCol.TYPE_TEXT, 180, "Notas Estado"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 80, "Hrs Estim."),
                new MySQLCol(MySQLCol.TYPE_TEXT, 80, "Hrs Ejec.")
            };
            tbl.sortType = GridResult.SORT_NONE;
            return createResponse(tbl);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/play")
    public Response play(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);
                SessionLogin sl = getSession(conn);

                //pausar cualquier otra tarea
                MySQLQuery q = new MySQLQuery("UPDATE "
                        + "hlp_request r "
                        + "INNER JOIN hlp_span_request s ON s.case_id = r.id "
                        + "SET s.end_date = NOW(), r.running = 0 "
                        + "WHERE s.emp_incharge_id = ?2 AND s.end_date IS NULL;");

                q.setParam(2, sl.employeeId);
                q.setParam(3, new ServerNow());
                q.executeUpdate(conn);

                Integer perEmpId = new MySQLQuery("SELECT id FROM per_employee WHERE emp_id = ?1").setParam(1, sl.employeeId).getAsInteger(conn);
                if (perEmpId == null) {
                    throw new Exception("No está registrado en talento humano");
                }
                HlpPrjBacklog b = new HlpPrjBacklog().select(id, conn);
                CrmProject prj = new CrmProject().select(b.prjId, conn);

                HlpRequest r = HlpRequest.getInProgressByBacklog(id, sl.employeeId, perEmpId, conn);
                if (r == null) {
                    r = new HlpRequest();
                    r.crmCliId = prj.clientId;
                    r.createdBy = sl.employeeId;
                    r.typeId = HlpType.getDevel(conn).id;
                    r.regDate = new ServerNow();
                    r.begDate = new ServerNow();
                    r.inCharge = perEmpId;
                    r.inChargeEmp = sl.employeeId;
                    r.state = "prog";
                    r.userType = "cli";
                    r.notes = b.description;
                    r.subject = b.name;
                    r.totalTime = 0;
                    r.deadTime = 0;
                    r.prjBacklogId = id;

                    if (null == b.priority) {
                        r.priority = "med";
                    } else {
                        switch (b.priority) {
                            case "l":
                                r.priority = "low";
                                break;
                            case "m":
                                r.priority = "med";
                                break;
                            case "h":
                                r.priority = "hig";
                                break;
                            default:
                                break;
                        }
                    }
                    r.projectId = b.prjId;
                    r.insert(conn);
                }

                r.running = true;
                r.update(conn);

                List<HlpSpanRequest> ss = HlpSpanRequest.getOpenByBacklog(id, sl.employeeId, conn);
                if (!ss.isEmpty()) {
                    throw new Exception("Ya se está ejecutando");
                }

                new MySQLQuery("UPDATE hlp_span_request SET last = 0 WHERE emp_incharge_id = ?1 AND case_id = ?2").setParam(1, sl.employeeId).setParam(2, id).executeUpdate(conn);

                HlpSpanRequest s = new HlpSpanRequest();
                s.caseId = r.id;
                s.empInchargeId = sl.employeeId;
                s.inchargeId = perEmpId;

                s.last = true;
                s.regDate = new ServerNow();
                s.insert(conn);
                new MySQLQuery(HlpRequest.getCacheQuery(r.id)).executeUpdate(conn);
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/pause")
    public Response pause(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);
                SessionLogin sl = getSession(conn);
                pause(id, sl.employeeId, conn);
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private static void pause(int backlogId, int employeeId, Connection conn) throws Exception {
        MySQLQuery q = new MySQLQuery("UPDATE "
                + "hlp_request r "
                + "inner join hlp_span_request s ON s.case_id = r.id "
                + "SET s.end_date = ?3, r.running = 0 "
                + "WHERE r.prj_backlog_id = ?1 AND s.emp_incharge_id = ?2 AND s.end_date IS NULL;");
        q.setParam(1, backlogId);
        q.setParam(2, employeeId);
        q.setParam(3, new ServerNow());
        q.executeUpdate(conn);
    }

    @PUT
    @Path("/stop")
    public Response stop(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            try {
                conn.setAutoCommit(false);
                SessionLogin sl = getSession(conn);

                pause(id, sl.employeeId, conn);
                MySQLQuery q = new MySQLQuery("SELECT r.id FROM "
                        + "hlp_request r "
                        + "WHERE r.prj_backlog_id = ?1 AND r.in_charge_emp = ?2");

                q.setParam(1, id);
                q.setParam(2, sl.employeeId);
                Object[][] idsData = q.getRecords(conn);

                q = new MySQLQuery("UPDATE "
                        + "hlp_request r "
                        + "SET r.close_date = NOW(), r.end_date = NOW(), r.state = 'clos', r.solution = 'Se completó el desarrollo', r.running = 0 "
                        + "WHERE r.prj_backlog_id = ?1 AND r.in_charge_emp = ?2");
                q.setParam(1, id);
                q.setParam(2, sl.employeeId);
                q.executeUpdate(conn);

                for (Object[] idsRow : idsData) {
                    new MySQLQuery(HlpRequest.getCacheQuery(cast.asInt(idsRow, 0))).executeUpdate(conn);
                }
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/up")
    public Response up(@QueryParam("id") int fieldId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            flip(fieldId, "up", conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/down")
    public Response down(@QueryParam("id") int fieldId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            flip(fieldId, "down", conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private static void flip(int id, String where, Connection conn) throws Exception {
        HlpPrjBacklog obj = new HlpPrjBacklog().select(id, conn);
        MySQLQuery q;
        if (where.equals("up")) {
            q = new MySQLQuery("SELECT id, place FROM hlp_prj_backlog WHERE prj_id = ?1 AND place < ?2 ORDER BY place DESC LIMIT 1");
        } else {
            q = new MySQLQuery("SELECT id, place FROM hlp_prj_backlog WHERE prj_id = ?1 AND place > ?2 ORDER BY place ASC LIMIT 1");
        }
        q.setParam(1, obj.prjId);
        q.setParam(2, obj.place);
        Object[] row = q.getRecord(conn);
        if (row != null) {
            q = new MySQLQuery("UPDATE hlp_prj_backlog SET place = ?1 WHERE id = ?2");
            q.setParam(1, cast.asInt(row, 1));
            q.setParam(2, obj.id);
            q.executeUpdate(conn);

            q = new MySQLQuery("UPDATE hlp_prj_backlog SET place = ?1 WHERE id = ?2");
            q.setParam(1, obj.place);
            q.setParam(2, cast.asInt(row, 0));
            q.executeUpdate(conn);
        }
    }
}
