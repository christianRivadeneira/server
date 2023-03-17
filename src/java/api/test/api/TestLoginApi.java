package api.test.api;

import api.BaseAPI;
import api.sys.model.Employee;
import java.sql.Connection;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import utilities.MySQLQuery;
import api.test.model.TestLogin;
import java.util.Random;

@Path("/testLogin")
public class TestLoginApi extends BaseAPI {

    @POST
    @Path("/login")
    public Response getByState(TestLogin obj) {
        try (Connection conn = getConnection()) {
            Employee emp = new Employee().select(new MySQLQuery("SELECT " + Employee.getSelFlds("e") + " "
                    + "FROM employee e "
                    + "WHERE e.login = '" + obj.user + "' "
                    + "AND e.password = MD5('" + obj.pwd + "') "
                    + "AND e.active"), conn);

            if (emp == null) {
                throw new Exception("Usuario o contraseña incorrectos.\nVerifique y vuelva a intentar.");
            }

            Random rand = new Random();
            StringBuilder buf = new StringBuilder();
            int i = 0;
            while (i < 64) {
                int x = rand.nextInt();
                if ((x > 64 && x < 91) || (x > 96 && x < 123)) {
                    buf.append((char) x);
                    i++;
                }
            }

            obj.token = buf.toString();
            obj.emp = emp;

            new MySQLQuery("INSERT INTO session_login SET "
                    + "employee_id = " + emp.id + ", "
                    + "begin_time = NOW(), "
                    + "session_id = '" + obj.token + "', "
                    + "user_ip = 'localhost', "
                    + "server_ip = 'localhost', "
                    + "type = 'pc'").executeInsert(conn);

            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
