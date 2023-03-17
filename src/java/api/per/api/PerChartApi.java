//package api.per.api;
//
//import api.BaseAPI;
//import java.sql.Connection;
//import javax.ws.rs.GET;
//import javax.ws.rs.Path;
//import javax.ws.rs.core.Response;
//import utilities.MySQLQuery;
//
//@Path("/perCharts")
//public class PerChartApi extends BaseAPI {
//
////    @GET
////    @Path("/byMonth")
////    public Response getComparisonByMonth(int year){
////        try(Connection con = getConnection()) {
////            
////        } catch (Exception e) {
////            e.printStackTrace();
////            return createResponse(e);
////        }
////    }
////
////    private MySQLQuery getByMonthQuery(){
////        MySQLQuery mySQLQuery = new MySQLQuery("SELECT\n" +
////"	sum(COALESCE(pic.vlr_rad, 0)) AS vlr_rad, \n" +
////"	SUM(COALESCE(pic.vlr_aprob, 0)) AS vlr_aprob,\n" +
////"	MONTH(pa.reg_date) AS mnt,\n" +
////"	pa.reg_date\n" +
////"FROM per_ins_claim pic\n" +
////"JOIN per_accident pa ON pa.id = pic.acc_id\n" +
////"WHERE pa.reg_date BETWEEN '2019-01-01' AND '2019-12-01'\n" +
////"GROUP BY mnt");
////
////        return q;
////    }
//}
