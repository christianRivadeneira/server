package api.bill.api;

import api.BaseAPI;
import api.bill.model.BillInstance;
import api.bill.model.dto.BillInstanceCreationRequest;
import api.sys.model.City;
import api.sys.model.SysCrudLog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import web.billing.BillingServlet;

@Path("/billInstance")
public class BillInstanceApi extends BaseAPI {

    private static String getDBName(String instName) {
        instName = instName.toLowerCase().trim();
        instName = instName.replaceAll("á", "a").replaceAll("é", "e").replaceAll("í", "i").replaceAll("ó", "o").replaceAll("ú", "u").replaceAll("ñ", "n");
        instName = instName.replaceAll("[^a-z0-9]", " ");
        instName = instName.replaceAll("[ ]+", "_");
        return "billing_" + instName;
    }

    @POST
    public Response insert(BillInstanceCreationRequest req) {
        File fpass = null;
        File fscript = null;

        try (Connection conn = getConnection()) {
            getSession(conn);
            City c = new City().select(req.inst.cityId, conn);
            req.inst.pobId = Integer.valueOf(c.munId + "000");

            fpass = File.createTempFile("tmp", ".txt");
            fscript = File.createTempFile("tmp", ".sh");

            BillInstance inst = req.inst;

            if (inst.siteBilling && inst.isTankInstance()) {
                throw new Exception("Combinación no permitida: Facturacion tanques y en sitio");
            }

            if (!inst.siteBilling && inst.isNetInstance()) {
                throw new Exception("Combinación no permitida: Facturacion redes y global");
            }

            if (inst.suspDebtMonths < 1) {
                throw new Exception("Meses en mora para suspensión debe ser mayor que 0");
            }

            String mysqlPass = req.mysqlPass;
            inst.db = getDBName(inst.name);
            String dbName = inst.db;
            try (FileWriter fpwd = new FileWriter(fpass)) {
                fpwd.write("AS_ADMIN_PASSWORD=" + req.glassPass);
            }

            try (FileWriter fw = new FileWriter(fscript)) {
                fw.write("#!/bin/sh" + System.lineSeparator());
                fw.write("mysql -uroot -p" + mysqlPass + " -e \"CREATE DATABASE " + dbName + "\"" + System.lineSeparator());
                fw.write("mysqldump -uroot -p" + mysqlPass + " --skip-comments --default-character-set=latin1 --hex-blob --no-data --add-drop-database --databases billing_pasto > /tmp/orig.sql" + System.lineSeparator());
                fw.write("sed -i 's/billing_pasto/" + dbName + "/g' /tmp/orig.sql" + System.lineSeparator());
                fw.write("mysql -uroot -p" + mysqlPass + " " + dbName + " < /tmp/orig.sql" + System.lineSeparator());
                fw.write("rm -f /tmp/orig.sql" + System.lineSeparator());
                fw.write("mysql -uroot -p" + mysqlPass + " -e \"INSERT INTO " + dbName + ".bill_cfg SELECT * FROM billing_pasto.bill_cfg\"" + System.lineSeparator());
                fw.write("mysql -uroot -p" + mysqlPass + " -e \"INSERT INTO " + dbName + ".bill_bank SELECT * FROM billing_pasto.bill_bank\"" + System.lineSeparator());
                fw.write("mysql -uroot -p" + mysqlPass + " -e \"INSERT INTO " + dbName + ".bill_serial_ticket SET id = 1, last = 1, locked = 0\"" + System.lineSeparator());

                if (inst.isTankInstance()) {
                    fw.write("mysql -uroot -p" + mysqlPass + " -e \"INSERT INTO " + dbName + ".bill_price_list SELECT * FROM billing_pasto.bill_price_list\"" + System.lineSeparator());
                    fw.write("mysql -uroot -p" + mysqlPass + " -e \"INSERT INTO " + dbName + ".bill_build_type SELECT * FROM billing_pasto.bill_build_type\"" + System.lineSeparator());
                    fw.write("mysql -uroot -p" + mysqlPass + " -e \"UPDATE " + dbName + ".bill_cfg SET bill_writer_class = 'api.bill.writers.bill.BillWriterEANSafa3'\"" + System.lineSeparator());
                    fw.write("mysql -uroot -p" + mysqlPass + " -e \"INSERT INTO " + dbName + ".bill_service_type SELECT * FROM billing_pasto.bill_service_type\"" + System.lineSeparator());
                } else {
                    fw.write("mysql -uroot -p" + mysqlPass + " -e \"UPDATE " + dbName + ".bill_cfg SET bill_writer_class = 'api.bill.writers.bill.BillWriterNetPdf'\"" + System.lineSeparator());
                    fw.write("mysql -uroot -p" + mysqlPass + " -e \"INSERT INTO " + dbName + ".bill_service_type SELECT * FROM billing_pasto.bill_service_type\" WHERE type='user'" + System.lineSeparator());
                }

                fw.write("mysql -uroot -p" + mysqlPass + " -e \"DELETE FROM sys_crud_log WHERE sys_crud_log.bill_inst_id = " + inst.id + ";\"" + System.lineSeparator());

                fw.write("/opt/glassfish/glassfish4/bin/asadmin --user admin --passwordfile " + fpass.getAbsolutePath() + " create-jdbc-connection-pool --datasourceclassname com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource  --restype javax.sql.ConnectionPoolDataSource --property "
                        + "user=root:password=" + mysqlPass + ":databaseName=" + dbName + ":serverName=" + req.mysqlHost + ":portNumber=3306 " + dbName + "_pool" + System.lineSeparator());
                fw.write("/opt/glassfish/glassfish4/bin/asadmin --user admin --passwordfile " + fpass.getAbsolutePath() + " create-jdbc-resource  --connectionpoolid " + dbName + "_pool " + dbName + System.lineSeparator());
            } catch (Exception ex) {
                throw ex;
            }
            exec("chmod +x " + fscript.getAbsolutePath());
            exec(fscript.getAbsolutePath());
            req.inst.insert(conn);
            return Response.ok(req).build();
        } catch (Exception ex) {
            return createResponse(ex);
        } finally {
            if (fpass != null) {
                fpass.delete();
            }
            if (fscript != null) {
                fscript.delete();
            }
        }
    }

    @PUT
    public Response update(BillInstance inst) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillInstance orig = new BillInstance().select(inst.id, conn);
            if (inst.db == null || inst.db.isEmpty()) {
                inst.db = getDBName(inst.name);
            }
            City c = new City().select(inst.cityId, conn);
            inst.pobId = Integer.valueOf(c.munId + "000");
            if (inst.siteBilling && inst.isTankInstance()) {
                throw new Exception("Combinación no permitida: Facturacion tanques y en sitio");
            }

            if (!inst.siteBilling && inst.isNetInstance()) {
                throw new Exception("Combinación no permitida: Facturacion redes y global");
            }

            if (inst.suspDebtMonths < 1) {
                throw new Exception("Meses en mora para suspensión debe ser mayor que 0");
            }
            inst.update(conn);
            useDefault(conn);
            SysCrudLog.updated(this, inst, orig, conn);
            BillingServlet.clearCache();
            return Response.ok(inst).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            BillInstance obj = new BillInstance().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillInstance.delete(id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getAll")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            return createResponse(BillInstance.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private static void exec(String command) throws Exception {
        Process p = Runtime.getRuntime().exec(command);
        p.waitFor();
        if (p.exitValue() == 0) {
            System.out.println(readStream(p.getInputStream()));
        } else {
            throw new Exception(readStream(p.getErrorStream()));
        }
    }

    private static String readStream(InputStream is) throws Exception {
        StringBuilder sb = new StringBuilder();
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = in.readLine()) != null) {
            sb.append(line);
            sb.append(System.lineSeparator());
        }
        return sb.toString();
    }

    @GET
    @Path("/instances")
    public Response instances() {
        try (Connection conn = getConnection()) {
            MySQLQuery mq = new MySQLQuery("SELECT " + BillInstance.getSelFlds("c") + " "
                    + "FROM bill_instance c "
                    + "ORDER by c.`name` ASC");
            List<BillInstance> list = BillInstance.getList(mq, conn);
            return createResponse(list);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/billingByMarket")
    public Response getBillingByMarket(@QueryParam("marketId") int marketId) {
        try (Connection conn = getConnection()) {
            return createResponse(BillInstance.getBillingByMarket(marketId, conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/reset")
    public Response getReset(@QueryParam("instId") int instId) {
        try (Connection conn = getConnection()) {
            BillInstance obj = BillingServlet.getInst(instId);
            String db = obj.db;
            new MySQLQuery("DELETE FROM " + db + ".bill_transaction;").executeUpdate(conn);
            new MySQLQuery("DELETE FROM " + db + ".bill_bill;").executeUpdate(conn);
            new MySQLQuery("UPDATE      " + db + ".bill_client_tank SET span_closed = 0;").executeUpdate(conn);
            new MySQLQuery("DELETE FROM " + db + ".bill_antic_note;").executeUpdate(conn);
            new MySQLQuery("DELETE FROM " + db + ".bill_service_fail;").executeUpdate(conn);
            new MySQLQuery("DELETE FROM " + db + ".bill_reading_bk;").executeUpdate(conn);
            new MySQLQuery("DELETE FROM " + db + ".bill_reading;").executeUpdate(conn);
            new MySQLQuery("DELETE FROM " + db + ".bill_measure;").executeUpdate(conn);
            new MySQLQuery("DELETE FROM " + db + ".bill_user_service_fee;").executeUpdate(conn);
            new MySQLQuery("DELETE FROM " + db + ".bill_user_service;").executeUpdate(conn);
            new MySQLQuery("DELETE FROM " + db + ".bill_inst_check;").executeUpdate(conn);
            new MySQLQuery("DELETE FROM " + db + ".bill_inst_check_poll;").executeUpdate(conn);
            new MySQLQuery("DELETE FROM " + db + ".bill_meter;").executeUpdate(conn);
            new MySQLQuery("DELETE FROM " + db + ".bill_clie_cau;").executeUpdate(conn);
            new MySQLQuery("DELETE FROM " + db + ".bill_note;").executeUpdate(conn);
            new MySQLQuery("DELETE FROM " + db + ".bill_clie_rebill;").executeUpdate(conn);
            //Corrección reiniciar capacitación pruebas de redes
            new MySQLQuery("DELETE FROM " + db + ".bill_susp;").executeUpdate(conn);
            new MySQLQuery("DELETE FROM " + db + ".bill_client_tank;").executeUpdate(conn);
            new MySQLQuery("DELETE FROM " + db + ".bill_span;").executeUpdate(conn);
            new MySQLQuery("UPDATE      " + db + ".bill_prospect p SET p.converted = 0;").executeUpdate(conn);
            new MySQLQuery("DELETE FROM sys_crud_log WHERE sys_crud_log.bill_inst_id = " + instId + ";").executeUpdate(conn);

            new MySQLQuery("DELETE FROM "
                    + "sigma.ord_pqr_request WHERE client_tank_id IN ("
                    + "SELECT id FROM sigma.ord_pqr_client_tank WHERE bill_instance_id = " + instId + ");").executeUpdate(conn);

            new MySQLQuery("DELETE FROM "
                    + "sigma.ord_repairs WHERE client_id IN ("
                    + "SELECT id FROM sigma.ord_pqr_client_tank WHERE bill_instance_id = " + instId + ");").executeUpdate(conn);

            new MySQLQuery("DELETE FROM sigma.ord_pqr_client_tank WHERE bill_instance_id = " + instId + ";").executeUpdate(conn);
            
            SysCrudLog.updated(this, obj, "Se reestableció la instancia.", conn);
            
            return createResponse(true);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
