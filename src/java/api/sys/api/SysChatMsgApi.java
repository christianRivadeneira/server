package api.sys.api;

import api.BaseAPI;
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
import api.sys.model.SysChatMsg;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

@Path("/sysChatMsg")
public class SysChatMsgApi extends BaseAPI {

	@POST
	public Response insert(SysChatMsg obj) {
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
	public Response update(SysChatMsg obj) {
		try (Connection conn = getConnection()) {
			SessionLogin sl = getSession(conn);
			SysChatMsg old = new SysChatMsg().select(obj.id, conn);
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
			SysChatMsg obj = new SysChatMsg().select(id, conn);
			return Response.ok(obj).build();
		} catch (Exception ex) {
			return createResponse(ex);
		}
	}

	@DELETE
	public Response delete(@QueryParam("id") int id) {
		try (Connection conn = getConnection()) {
			SessionLogin sl = getSession(conn);
			SysChatMsg.delete(id, conn);
			SysCrudLog.deleted(this, SysChatMsg.class, id, conn);
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
			return createResponse(SysChatMsg.getAll(conn));
		} catch (Exception ex) {
			return createResponse(ex);
		}
	}

	@POST
	@Path("/recent")
	public Response notSeenBy(@QueryParam("empId") int empId, @QueryParam("lastMsgId") int lastMsgId) {
		try (Connection conn = getConnection()) {
			SessionLogin sl = getSession(conn);
			String queryString = "SELECT " +
				SysChatMsg.getSelFlds("msg") +
				" FROM sys_chat_msg msg " +
				"WHERE " +
				"msg.id > ?2 " +
				"AND " +
				"( " +
				"	msg.to_id = ?1 " +
				"	OR msg.from_id = ?1 " +
				") " +
				"ORDER BY msg.dt DESC " +
				"LIMIT 100 ";

			MySQLQuery query = new MySQLQuery(queryString)
				.setParam(1, empId)
				.setParam(2, lastMsgId);

			List<SysChatMsg> messages = SysChatMsg.getList(query, conn);
			
			for(SysChatMsg msg : messages){
				if(msg.seen == false && msg.toId != null && msg.toId == sl.employeeId){
					msg.seen = true;
					msg.update(conn);
				} else if(msg.seen == false && msg.toId == null && sl.employeeId != empId){
					msg.toId = sl.employeeId;	
					msg.seen = true;
					msg.update(conn);
				} 
			}

			return createResponse(messages);
		} catch (Exception ex) {
			return createResponse(ex);
		}
	}

	@POST
	@Path("/fromApp")
	public Response notSeenBy(SysChatMsg msg) {
		try (Connection conn = getConnection()) {
			SessionLogin session = getSession(conn);

			msg.dt = new Date();
			msg.attachId = -1;
			msg.insert(conn);

			SysCrudLog.created(this, msg, conn);

			return Response.ok(msg).build();
		} catch(Exception ex) {
			return createResponse(ex);
		}
	}
}
