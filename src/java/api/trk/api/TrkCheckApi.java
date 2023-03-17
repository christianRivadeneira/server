package api.trk.api;

import api.BaseAPI;
import api.trk.model.TrkCheck;
import api.trk.model.TrkCyl;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.util.Date;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import web.ShortException;

@Path("/trkCheck")
public class TrkCheckApi extends BaseAPI {

    @POST
    public Response insert(@QueryParam("cylId") int cylId, @QueryParam("centerId") Integer centerId, @QueryParam("type") String type, @QueryParam("areaId") Integer areaId, @QueryParam("tankId") Integer tankId) {
        System.out.println("ESTAMOS EN LA API DE TRK_CHECK "+tankId+" este es id cyl "+cylId);
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            //04/09/2019 se comenta para que quede registro de cada prellenado, así se hagan dos veces el mismo día.
            //Integer chkId = new MySQLQuery("SELECT id FROM trk_check WHERE date(dt) = CURDATE() AND trk_cyl_id = ?1").setParam(1, cylId).getAsInteger(conn);
            TrkCheck chk;
            /*if (chkId != null) {
                chk = new TrkCheck().select(chkId, conn);
                chk.ok = true;
                chk.update(conn);
                new MySQLQuery("DELETE FROM trk_neg_ans WHERE check_id = " + chkId).executeUpdate(conn);
                TrkTreatApplApi.removeTreatment(chkId, conn);
            } else {*/
            chk = new TrkCheck();
            chk.checkVersionId = new MySQLQuery("SELECT id FROM trk_check_version WHERE active").getAsInteger(conn);
            if (chk.checkVersionId == null) {
                throw new ShortException("No hay una versión del cuestionario activa.");
            }
            //chk.invCenterId = new MySQLQuery("SELECT c.center_id FROM inv_emp_center c WHERE c.employee_id = ?1").setParam(1, sl.employeeId).getAsInteger(conn);
            chk.invCenterId = centerId;
            chk.dt = new Date();
            chk.empId = sl.employeeId;
            chk.trkCylId = cylId;
            chk.ok = true;
            if (type != null) {
                chk.type = type;
            } else {
                chk.type = "ci"; //Por defecto el prellenado sería clasificación inicial.
            }
            if (areaId != null) {
                chk.procAreaId = areaId;
            }
            Object[] quality=null;
            Object[][] tanques=null;
            //Conocer id de planta
            Integer idCo=new MySQLQuery("SELECT co.id "
                    + "FROM gt_center co "
                    + "INNER JOIN sys_center s ON s.gt_center_id = co.id "
                    + "INNER JOIN inv_center ic ON ic.sys_center_id =s.id "
                    + "WHERE ic.id ="+centerId).getAsInteger(conn);
            //Conocer tanques activos
            if(idCo!=null){
                tanques=new MySQLQuery("SELECT id "
                    + "FROM gt_glp_tank "
                    + "WHERE quality_cyl=1 "
                    + "AND center_id="+idCo).getRecords(conn);
            }
            //chk.tankId= tankId; esto era para calidad desde la app de operaciones
            //Calidad glp
            if (tanques != null){
                //Calidad glp
                quality=darPromedio(tanques, conn);
            }
            chk.insert(conn);
            // }

            TrkCyl cyl = new TrkCyl().select(cylId, conn);
            cyl.ok = true;
            cyl.salable = true;
            cyl.respId = null;
            cyl.lastVerify = new Date();
            cyl.empVerifier = sl.employeeId;
            
            //Calidad glp
            cyl.c3=quality!=null?(BigDecimal) quality[0]:null;
            cyl.c4=quality!=null?(BigDecimal) quality[1]:null;
            cyl.c5=quality!=null?(BigDecimal) quality[2]:null;
            cyl.agua=quality!=null?(BigDecimal) quality[3]:null;
            
            /*if(quality==null){
                 throw new ShortException("No se ingresó calidad glp");
            }*/
            
            cyl.update(conn);
            return Response.ok(chk).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
    public static Object[] darPromedio(Object[][] tanques, Connection conn) throws Exception{
        Object[] quality=null;
        BigDecimal sumaC3 = BigDecimal.ZERO;
        BigDecimal sumaC4 = BigDecimal.ZERO;
        BigDecimal sumaC5 = BigDecimal.ZERO;
        BigDecimal sumaAgua = BigDecimal.ZERO;
        for(int i=0; i<=tanques.length-1;i++){
            quality=new MySQLQuery("SELECT "
                    + "q.c_3_propano, q.c_4_butano, q.c_5_olefinas, q.agua "
                    + "FROM sigma.gt_tanks_prod_quality q "
                    + "WHERE q.id_tank ="+tanques[i][0]+" AND q.fecha_calidad = CURDATE()").getRecord(conn);
            if(quality==null){
                quality=new MySQLQuery("SELECT "
                    + "q.c_3_propano, q.c_4_butano, q.c_5_olefinas, q.agua "
                    + "FROM sigma.gt_tanks_prod_quality q "
                    + "WHERE q.id_tank ="+tanques[i][0]+" AND q.fecha_calidad <= CURDATE() "
                    + "ORDER BY q.fecha_calidad DESC LIMIT 1").getRecord(conn);
            }
            if(quality!=null){
                BigDecimal c3=(BigDecimal) quality[0];
                BigDecimal c4=(BigDecimal) quality[1];
                BigDecimal c5=(BigDecimal) quality[2];
                BigDecimal agua=(BigDecimal) quality[3];
                sumaC3=(sumaC3.add(c3));
                sumaC4=(sumaC4.add(c4));
                sumaC5=(sumaC5.add(c5));
                sumaAgua=(sumaAgua.add(agua));
            }
        }
        if(quality!=null){
            quality[0]=sumaC3.divide(new BigDecimal(tanques.length), RoundingMode.HALF_EVEN);
            quality[1]=sumaC4.divide(new BigDecimal(tanques.length), RoundingMode.HALF_EVEN);
            quality[2]=sumaC5.divide(new BigDecimal(tanques.length), RoundingMode.HALF_EVEN);
            quality[3]=sumaAgua.divide(new BigDecimal(tanques.length), RoundingMode.HALF_EVEN);
            
            System.out.println("######## esto es quality C3: "+quality[0]+" C4: "+quality[1]+" C5: "+quality[2]+" Agua:"+quality[3]);
        }
        return quality;
    }
}
