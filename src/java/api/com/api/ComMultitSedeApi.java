package api.com.api;

import api.BaseAPI;
import api.com.dto.PrefClients;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import api.com.model.ComMultiSede;
import java.util.ArrayList;
import java.util.List;
import utilities.MySQLQuery;

@Path("/comMultiSede")
public class ComMultitSedeApi extends BaseAPI {

    @POST
    public Response insert(ComMultiSede obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(ComMultiSede obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            ComMultiSede old = new ComMultiSede().select(obj.id, conn);
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
            getSession(conn);
            ComMultiSede obj = new ComMultiSede().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            ComMultiSede.delete(id, conn);
            SysCrudLog.deleted(this, ComMultiSede.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/all")
    public Response getAll() {
        try (Connection conn = getConnection()) {
                getSession(conn);
            return createResponse(ComMultiSede.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    
    @GET
    @Path("/prefClients")
    public Response getPrefClients() {
        try (Connection conn = getConnection()) {
            List<PrefClients> res = new ArrayList<>();
            Object[][] data = new MySQLQuery("SELECT "
                    + "id, "
                    + "CONCAT(first_name, ' ', last_name) AS nm, "
                    + "address, "
                    + "phones, "
                    + "document "
                    + "FROM ord_contract_index "
                    + "WHERE pref "
                    + "ORDER BY nm ASC").getRecords(conn);
            
            for (int i = 0; i < data.length; i++) {
                Object[] row = data[i];
                PrefClients cl = new PrefClients();
                cl.indexId = MySQLQuery.getAsInteger(row[0]);
                cl.clieName = MySQLQuery.getAsString(row[1]);
                cl.address = MySQLQuery.getAsString(row[2]);
                cl.phones = MySQLQuery.getAsString(row[3]);
                cl.document = MySQLQuery.getAsString(row[4]);
                res.add(cl);
            }
            return createResponse(res);
        } catch (Exception e) {
            return createResponse(e);
        }
    }


    /*@GET
    @Path("/grid")
    public Response getGrid() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GridResult tbl = new GridResult();
            tbl.data = new MySQLQuery("").getRecords(conn);
            tbl.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 180, "Cupón"),
            };
            tbl.sortColIndex = 4;
            tbl.sortType = GridResult.SORT_ASC;
            return createResponse(tbl);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }*/

}
