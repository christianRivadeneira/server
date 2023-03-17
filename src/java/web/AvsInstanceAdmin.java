package web;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import model.system.SessionLogin;
import utilities.IO;
import utilities.MySQLQuery;

@WebServlet(name = "AvsInstanceAdmin", urlPatterns = {"/AvsInstanceAdmin"})
public class AvsInstanceAdmin extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            Map<String, String> pars = MySQLQuery.scapedParams(request);
            String header = pars.get("header");
            String pwdM = "BQUfUflr";
            String pwdGf = "PzpYtp8YmTRzCVPv";

            if (header == null) {
                throw new Exception("Falta parámetro header");
            }

            switch (header) {
                case "add": {
                    String nameSrc = "base"; //La bd tomada como base para ésto es 'base'
                    String serverName = "127.0.0.1";
                    String sessionId = pars.get("sessionId");

                    String nameDest = pars.get("nameDest");
                    String entName = pars.get("entName");
                    String poolName = pars.get("poolName");
                    String tz = pars.get("tz");
                    SessionLogin.validate(sessionId, poolName, tz);
                    String capitalDest = Character.toUpperCase(nameDest.charAt(0)) + nameDest.substring(1);
                    String capitalSrc = Character.toUpperCase(nameSrc.charAt(0)) + nameSrc.substring(1);
                    try (FileWriter fpwd = new FileWriter("/tmp/pwd.txt")) {
                        fpwd.write("AS_ADMIN_PASSWORD=" + pwdGf);
                    } catch (Exception ex) {
                        throw ex;
                    }

                    try (FileWriter fw = new FileWriter("/tmp/script.sh")) {
                        fw.write("#!/bin/sh" + System.lineSeparator());
                        fw.write("mysql -uroot -p" + pwdM + " -e \"CREATE DATABASE " + nameDest + "\"" + System.lineSeparator());
                        fw.write("mysqldump -uroot -p" + pwdM + " " + nameSrc + " > /tmp/orig.sql" + System.lineSeparator());
                        fw.write("mysql -uroot -p" + pwdM + " " + nameDest + " < /tmp/orig.sql" + System.lineSeparator());
                        fw.write("rm -f /tmp/orig.sql" + System.lineSeparator());
                        fw.write("mysql -uroot -p" + pwdM + " " + nameDest + " -e \"UPDATE sys_cfg SET max_usrs = 0, db_url = 'jdbc:mysql//" + serverName + ":3306/" + nameDest + "?user=root&pass=" + pwdM + "', files_path = '/sigma/bdata" + capitalDest + "/',url_new_launcher='launcher/generate?repoPath=&poolName=" + nameDest + "ds&name=sigma'\"" + System.lineSeparator());
                        fw.write("mysql -uroot -p" + pwdM + " " + nameDest + " -e \"UPDATE enterprise SET name = '" + entName + "', short_name = '" + capitalDest + "'\"" + System.lineSeparator());
                        fw.write("cp -R /sigma/bdata" + capitalSrc + "/ /sigma/bdata" + capitalDest + System.lineSeparator());
                        fw.write("cp -R /var/www/" + nameSrc + "/ /var/www/" + nameDest + "/" + System.lineSeparator());
                        fw.write("chmod -R 777 /var/www/" + nameDest + "/" + System.lineSeparator());
                        fw.write("replace " + nameSrc + " " + nameDest + " -- /var/www/" + nameDest + "/index.php" + System.lineSeparator());
                        fw.write("/root/glassfish4/bin/asadmin --user admin --passwordfile /tmp/pwd.txt --port 4848 create-jdbc-connection-pool --datasourceclassname com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource  --restype javax.sql.ConnectionPoolDataSource --property user=root:password=" + pwdM + ":databaseName=" + nameDest + ":serverName=" + serverName + ":portNumber=3306 " + nameDest + "_pool" + System.lineSeparator());
                        fw.write("/root/glassfish4/bin/asadmin --user admin --passwordfile /tmp/pwd.txt --port 4848 create-jdbc-resource  --connectionpoolid " + nameDest + "_pool " + nameDest + "ds" + System.lineSeparator());
                    } catch (Exception ex) {
                        throw ex;
                    }
                    exec("chmod +x /tmp/script.sh");
                    exec("/tmp/script.sh");
                    exec("rm -R /tmp/pwd.txt");
                    response.setStatus(200);
                    response.getWriter().write("Instancia creada con éxito");
                    break;
                }
                case "del": {
                    String sessionId = pars.get("sessionId");
                    String poolName = pars.get("poolName");
                    String tz = pars.get("tz");
                    SessionLogin.validate(sessionId, poolName, tz);
                    String instanceName = pars.get("instanceName");
                    String capitalDest = Character.toUpperCase(instanceName.charAt(0)) + instanceName.substring(1);
                    try (FileWriter fpwd = new FileWriter("/tmp/pwd.txt")) {
                        fpwd.write("AS_ADMIN_PASSWORD=" + pwdGf);
                    }
                    try (FileWriter fw = new FileWriter("/tmp/script.sh")) {
                        fw.write("#!/bin/sh" + System.lineSeparator());
                        fw.write("mysql -uroot -p" + pwdM + " -e \"DROP DATABASE " + instanceName + "\"" + System.lineSeparator());
                        fw.write("rm -R /sigma/bdata" + capitalDest + System.lineSeparator());
                        fw.write("rm -R /var/www/" + instanceName + "/" + System.lineSeparator());
                        fw.write("/root/glassfish4/bin/asadmin --user admin --passwordfile /tmp/pwd.txt --port 4848 delete-jdbc-resource " + instanceName + "ds" + System.lineSeparator());
                        fw.write("/root/glassfish4/bin/asadmin --user admin --passwordfile /tmp/pwd.txt --port 4848 delete-jdbc-connection-pool " + instanceName + "_pool" + System.lineSeparator());
                    } catch (Exception ex) {
                        throw ex;
                    }
                    exec("chmod +x /tmp/script.sh");
                    exec("/tmp/script.sh");
                    exec("rm -R /tmp/pwd.txt");
                    response.setStatus(200);
                    response.getWriter().write("Instancia eliminada con éxito");
                    break;
                }
                default:
                    throw new Exception("Opción no válida " + header);
            }
        } catch (Exception ex) {
            sendError(response, ex);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }

    public static String convertStreamToString(Part is) throws IOException {
        if (is == null) {
            return null;
        }
        return IO.convertStreamToString(is.getInputStream());
    }

    private void sendError(HttpServletResponse resp, Exception ex) throws IOException {
        Logger.getLogger(AvsInstanceAdmin.class.getName()).log(Level.SEVERE, null, ex);
        resp.setStatus(500);
        if (ex.getMessage() != null) {
            resp.getOutputStream().write(ex.getMessage().getBytes("UTF8"));
        } else {
            resp.getOutputStream().write(ex.getClass().getName().getBytes("UTF8"));
        }
    }

    private static void exec(String command) throws Exception {
        Process p = Runtime.getRuntime().exec(command);
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            System.out.println(line);
        }
    }
}
