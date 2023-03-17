package api.per.api;

import api.BaseAPI;
import api.per.model.PerBadFp;
import api.per.model.PerDoorman;
import api.per.model.PerFprint;
import api.sys.model.Employee;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import utilities.MySQLQuery;

@Path("/perFprint")
public class PerFprintApi extends BaseAPI {

    @GET
    @Path("/getFprints")
    public Response getFprints(@QueryParam("clieDate") String clieDate) {
        try (Connection con = getConnection()) {
            getSession(con);

            Date servDate = new Date();
            long serv = servDate.getTime();
            long clie = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").parse(clieDate).getTime();
            if (Math.abs(serv - clie) > 300000) {
                throw new Exception("La hora de su equipo no está correctamente configurada.\nLa hora del servidor es " + new SimpleDateFormat("dd/MMMM/yyyy hh:mm:ss a").format(servDate) + "\nContacte a la oficina de sistemas.");
            }

            List<PerFprint> list = new PerFprint().getList(new MySQLQuery("SELECT "
                    + "pf.`id`, "//0
                    + "pf.`emp_id`, "//1
                    + "pf.`blob`, "//2
                    + "pc.office_id, "//3
                    + "CONCAT(pe.first_name, ' ', pe.last_name), "//4
                    + "pe.document "//5
                    + "FROM per_fprint AS pf "
                    + "INNER JOIN per_employee pe ON pf.emp_id = pe.id "
                    + "LEFT JOIN per_contract AS pc ON pc.emp_id = pf.emp_id AND pc.beg_date = (SELECT MAX(beg_date) FROM per_contract WHERE emp_id = pf.emp_id) "
                    + "ORDER BY pf.`last` DESC, pc.office_id ASC").getRecords(con));
            return Response.ok(list).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/getPerDoorman")
    public Response getPerDoorman(Employee emp) {
        try (Connection con = getConnection()) {
            PerDoorman dorman = new PerDoorman().selectByEmpId(emp.id, con);
            if (dorman == null) {
                throw new Exception("El usuario " + (emp.firstName + " " + emp.lastName) + " no se encuentra asociado a ninguna oficina.");
            }
            return Response.ok(dorman).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getBadFingerprints")
    public Response getBadFingerprints() {
        try (Connection con = getConnection()) {
            List<PerBadFp> lstBadFp = PerBadFp.getList(new MySQLQuery("SELECT id, document, CONCAT(first_name, ' ', last_name) FROM per_employee WHERE bad_fingerprints").getRecords(con));
            return Response.ok(lstBadFp).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
