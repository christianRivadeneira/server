/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.ess.api;

import api.BaseAPI;
import api.ess.dto.ChatDto;
import api.sys.model.SysChatMsg;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;

/**
 *
 * @author mario
 */
@Path("/essChat")
public class EssChat extends BaseAPI {

	@GET
	@Path("/activeChats")
	public Response getActiveChats() {
		try (Connection conn = getConnection()) {
			SessionLogin sl = getSession(conn);

			String queryStr = "SELECT DISTINCT "
				+ "per.emp_id, "
				+ "per.first_name, "
				+ "per.last_name "
				+ "FROM ess_person per JOIN sys_chat_msg msg ON ( "
				+ "	msg.to_id = per.emp_id "
				+ "	OR msg.from_id = per.emp_id "
				+ ")";

			Object[][] records = new MySQLQuery(queryStr).getRecords(conn);
			List<ChatDto> chats = new ArrayList<ChatDto>();

			for(Object[] record : records) {
				String qStr = "SELECT " + 
					SysChatMsg.getSelFlds("msg1") +
					" FROM sys_chat_msg msg1 " +
					"WHERE msg1.to_id = ?1  " +
					"OR msg1.from_id = ?1 " +
					"ORDER BY msg1.id DESC  " +
					"LIMIT 1 ";
				MySQLQuery q = new MySQLQuery(qStr)
					.setParam(1, record[0]);
				ChatDto dto = new ChatDto();
				dto.employeeId = (int) record[0];
				dto.employeeName = record[1].toString() + " " + record[2].toString();
				dto.message = new SysChatMsg().select(q, conn);
				chats.add(dto);
			}


			return Response.ok(chats).build();
		} catch (Exception ex) {
			return createResponse(ex);
		}
	}
}
