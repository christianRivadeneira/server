/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.gt.api;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.gt.model.GtTanks;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import model.system.SessionLogin;
import javax.ws.rs.core.Response;
import utilities.MySQLQuery;

/**
 *
 * @author Programador Sistemas
 */
@Path("/gtTanks")
public class GtTanksApi extends BaseAPI{
    
    @GET
    public Response get(@QueryParam("id") int id){
        try(Connection conn = getConnection()){
            SessionLogin s1 = getSession(conn);
            api.gt.model.GtTanks obj = new api.gt.model.GtTanks().select(id, conn);//se debe crear el modelo
            return createResponse(obj);
        }catch(Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    public Response insert(api.gt.model.GtTanks obj){
        try (Connection conn=getConnection()){
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return createResponse(obj);
        }
        catch (Exception ex){
            return createResponse(ex);
        }
    }
    
    @PUT
    public Response update(api.gt.model.GtTanks obj){
        try(Connection conn =getConnection()){
            SessionLogin sl = getSession(conn);
            api.gt.model.GtTanks old= new api.gt.model.GtTanks().select(obj.id, conn);
            obj.update(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return createResponse();
        }
        catch (Exception ex){
            return createResponse(ex);
        }
    }
    
    @GET
    @Path("/getByCenter")
    public Response getByCenterId(@QueryParam("id") int id){
        try(Connection conn=getConnection()){
            getSession(conn);
            return createResponse(GtTanks.getByCenterId(id, conn));
        }
        catch (Exception ex){
            return createResponse(ex);
        }
    }
    
//    @GET
//    @Path("/tanksGrid")
//    public Response getTanksTable(@QueryParam("centerId") Integer centerId) throws Exception {
//        System.out.println("¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡¡ Esto llega en la API del servidor: "+centerId);
//        try (Connection conn = getConnection()) {
//            getSession(conn);
//            useBillInstance(conn);
//            GridResult gr = new GridResult();
//            
//            gr.data = new MySQLQuery("SELECT * FROM gt_glp_tank WHERE center_id = "+centerId).getRecords(conn);
//
//            gr.cols = new MySQLCol[]{
//                new MySQLCol(MySQLCol.TYPE_KEY),
//                new MySQLCol(MySQLCol.TYPE_TEXT, 100, "Tanque"),
//                new MySQLCol(MySQLCol.TYPE_TEXT, 100, "Serial"),
//                new MySQLCol(MySQLCol.TYPE_INTEGER, 100, "Capacidad"),  
//            };
//            return createResponse(gr);
//        } catch (Exception ex) {
//            return createResponse(ex);
//        }
//    }
}
