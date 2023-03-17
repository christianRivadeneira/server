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
import api.gt.model.GtCentersProdQuality;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import model.system.SessionLogin;
import javax.ws.rs.core.Response;

/**
 *
 * @author Programador Sistemas
 */
@Path("/gtCentersProdQuality")
public class GtCentersProdQualityApi extends BaseAPI{
    
    @GET
    public Response get(@QueryParam("id") int id){
        try(Connection conn = getConnection()){
            SessionLogin s1 = getSession(conn);
            api.gt.model.GtCentersProdQuality obj = new api.gt.model.GtCentersProdQuality().select(id, conn);//se debe crear el modelo
            return createResponse(obj);
        }catch(Exception ex) {
            return createResponse(ex);
        }
    }
    
    @POST
    public Response insert(api.gt.model.GtCentersProdQuality obj){
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
    public Response update(api.gt.model.GtCentersProdQuality obj){
        try(Connection conn =getConnection()){
            SessionLogin sl = getSession(conn);
            api.gt.model.GtCentersProdQuality old= new api.gt.model.GtCentersProdQuality().select(obj.id, conn);
            obj.update(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return createResponse();
        }
        catch (Exception ex){
            return createResponse(ex);
        }
    }
}
