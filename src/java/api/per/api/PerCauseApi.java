package api.per.api;

import api.BaseAPI;
import api.per.model.PerCause;
import java.sql.Connection;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@Path("/perCause")
public class PerCauseApi extends BaseAPI {
	@GET	
	public Response listAll(@QueryParam("type") String type) {
		try(Connection con = getConnection()){
			MySQLQuery q = new MySQLQuery("SELECT " +
					PerCause.getSelFlds("pc") +
					"FROM per_cause pc " +
					"WHERE pc.`type` = ?1 " +
					"ORDER BY pc.name")
					.setParam(1, type);

			List<PerCause> perCauseList = PerCause.getList(q, con);
			return createResponse(perCauseList);	
		} catch(Exception ex) {
			ex.printStackTrace();
			return createResponse(ex);
		}
	}
}
