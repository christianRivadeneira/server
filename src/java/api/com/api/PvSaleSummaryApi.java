package api.com.api;

import api.BaseAPI;
import java.sql.Connection;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import api.com.model.PvSaleSummary;
import java.util.ArrayList;
import java.util.List;
import model.system.SessionLogin;
import utilities.MySQLQuery;

@Path("/pvSaleSummary")
public class PvSaleSummaryApi extends BaseAPI {

    @GET
    @Path("/getPvSaleSummary")
    public Response update() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);

            Object[][] data = new MySQLQuery("SELECT "
                    + "ps.id, "
                    + "ps.dt, "
                    + "s.document, "
                    + "CONCAT(s.first_name, ' ', s.last_name), "
                    + "ps.bill, "
                    + "ps.credit "
                    + "FROM trk_pv_sale ps "
                    + "INNER JOIN inv_store s ON ps.store_id = s.id "
                    + "INNER JOIN gt_cyl_trip t ON ps.emp_id = t.driver_id "
                    + "WHERE t.driver_id = " + sl.employeeId + " "
                    + "AND t.type_id = 144 "
                    + "AND t.steps <> t.req_steps "
                    + "AND !t.cancel "
                    + "AND Date(ps.dt) >= DATE(t.sdt) "
                    + "ORDER BY ps.dt DESC").getRecords(conn);

            List<PvSaleSummary> lst = new ArrayList<>();
            for (int i = 0; i < data.length; i++) {
                Object[] row = data[i];
                PvSaleSummary item = new PvSaleSummary(row);
                item.salePos = i + 1;
                item.amounts = new MySQLQuery("SELECT GROUP_CONCAT(cnt) "
                        + "FROM (SELECT "
                        + "CONCAT(COUNT(tc.id), ' x ', ct.name, 'Lb') AS cnt "
                        + "FROM trk_pv_cyls tpc "
                        + "INNER JOIN trk_cyl tc ON tpc.cyl_id = tc.id "
                        + "INNER JOIN cylinder_type ct ON tc.cyl_type_id = ct.id "
                        + "WHERE tpc.`type` = 'del' "
                        + "AND tpc.pv_sale_id = " + MySQLQuery.getAsInteger(row[0]) + " "
                        + "GROUP BY ct.id) AS l").getAsString(conn);
                lst.add(item);
            }
            
            return createResponse(lst);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
