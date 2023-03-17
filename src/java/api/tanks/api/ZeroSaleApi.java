package api.tanks.api;

import api.BaseAPI;
import api.tanks.model.ZeroSale;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import utilities.MySQLQuery;

@Path("/zeroApi")
public class ZeroSaleApi extends BaseAPI {

    @GET
    @Path("/getList")
    public Response getRecent(@QueryParam("idAccExec") int idAccExec, @QueryParam("build") boolean build) {
        try (Connection con = getConnection()) {
            getSession(con);

            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(new Date());
            gc.set(GregorianCalendar.DAY_OF_MONTH, 1);
            Date begDate = gc.getTime();

            gc.set(GregorianCalendar.DAY_OF_MONTH, gc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
            Date endDate = gc.getTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            Object[][] data = new MySQLQuery("SELECT cl.document, "
                    + "cl.name, "
                    + "cl.address, "
                    + "cl.phones  "
                    + "FROM ord_tank_client cl "
                    + "LEFT JOIN est_sale s ON s.client_id = cl.id AND s.sale_date BETWEEN '" + sdf.format(begDate) + " 00:00:00' AND '" + sdf.format(endDate) + " 23:59:59' "
                    + "WHERE "
                    + "s.id IS NULL AND "
                    + "cl.exec_reg_id = " + idAccExec + " "
                    + (build ? "AND cl.type = 'build' " : "AND cl.type <> 'build' ")
                    + "AND (cl.created_date < NOW() OR cl.created_date IS NULL) "
                    + "AND cl.active "
            ).getRecords(con);

            List<ZeroSale> lstClies = new ArrayList<>();
            for (int i = 0; i < data.length; i++) {
                ZeroSale item = new ZeroSale(data[i]);
                lstClies.add(item);
            }

            return Response.ok(lstClies).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
