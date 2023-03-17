/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.per.api;

import api.BaseAPI;
import api.per.model.PerProfCfg;
import java.sql.Connection;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;

/**
 *
 * @author Danny
 */
@Path("/perProfCfg")
public class PerProfCfgApi extends BaseAPI {
	
	@GET
	@Path("/getFromSession")
	public Response getPerProfCfgFromSession() {
		try(Connection con = getConnection()) {
			SessionLogin sl = getSession(con);
			MySQLQuery q = new MySQLQuery("SELECT " +
					PerProfCfg.getSelFlds("c") +
					"FROM login l " +
					"INNER JOIN profile p ON l.profile_id = p.id " +
					"INNER JOIN per_prof_cfg c ON p.id = c.prof_id " +
					"WHERE p.menu_id = 391 " +
					"AND l.employee_id = ?1")
				.setParam(1, sl.employeeId);

			List<PerProfCfg> c = PerProfCfg.getList(q, con);
			return createResponse(c);
		} catch(Exception ex){
			ex.printStackTrace();
			return createResponse(ex);
		}
	}

	@GET
	@Path("/test")
	public Response test() {
		return createResponse("Test!!! 3");
	}
}
