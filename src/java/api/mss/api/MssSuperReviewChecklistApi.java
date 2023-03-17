package api.mss.api;

import api.BaseAPI;
import api.mss.dto.ChecklistInfo;
import api.mss.model.MssSuperReview;
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
import api.mss.model.MssSuperReviewChecklist;
import java.util.ArrayList;
import java.util.List;
import utilities.MySQLQuery;

@Path("/mssSuperReviewChecklist")
public class MssSuperReviewChecklistApi extends BaseAPI {

    @POST
    public Response insert(MssSuperReviewChecklist obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(MssSuperReviewChecklist obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssSuperReviewChecklist old = new MssSuperReviewChecklist().select(obj.id, conn);
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
            SessionLogin sl = getSession(conn);
            MssSuperReviewChecklist obj = new MssSuperReviewChecklist().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssSuperReviewChecklist.delete(id, conn);
            SysCrudLog.deleted(this, MssSuperReviewChecklist.class, id, conn);
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
            return createResponse(MssSuperReviewChecklist.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/checklistReview")
    public Response getHobbiesInfo(@QueryParam("reviewId") int reviewId) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            Object[][] items = new MySQLQuery("SELECT rf.id, rf.name, IF(ch.id IS NULL , 0 , 1), ch.review_id "
                    + " FROM mss_super_review_finding rf "
                    + " LEFT JOIN mss_super_review_checklist ch ON ch.finding_id = rf.id AND ch.review_id = ?1 "
                    + " WHERE rf.active "
                    + " ORDER BY place ASC"
            ).setParam(1, reviewId).getRecords(conn);

            ChecklistInfo info = new ChecklistInfo();
            info.reviewId = reviewId;
            List<ChecklistInfo> listInfo = new ArrayList<>();
            for (Object[] item : items) {
                ChecklistInfo chkObj = new ChecklistInfo();
                chkObj.itemId = MySQLQuery.getAsInteger(item[0]);
                chkObj.itemName = MySQLQuery.getAsString(item[1]);
                chkObj.isChecked = MySQLQuery.getAsBoolean(item[2]);
                listInfo.add(chkObj);
            }
            info.items = listInfo;

            return createResponse(info);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/checklistReview")
    public Response setHobbiesInfo(ChecklistInfo obj) {
        try (Connection con = getConnection()) {
            if (obj != null) {
                if(obj.reviewId == 0){
                    throw new Exception("No se encontro la revisión");
                }
                new MySQLQuery("DELETE FROM mss_super_review_checklist WHERE review_id = ?1").setParam(1, obj.reviewId).executeDelete(con);
                if (!obj.items.isEmpty()) {
                    for (int i = 0; i < obj.items.size(); i++) {
                        if (obj.items.get(i).isChecked) {
                            MssSuperReviewChecklist chk = new MssSuperReviewChecklist();
                            chk.findingId = obj.items.get(i).itemId;
                            chk.reviewId = obj.reviewId;
                            chk.insert(con);
                        }
                    }
                    MssSuperReview.sendMailSuperReview(obj.reviewId, con);
                }
            }
            return Response.ok().build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
