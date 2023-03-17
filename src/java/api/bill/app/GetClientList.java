package api.bill.app;

import api.bill.model.BillInstance;
import api.bill.model.BillSpan;
import api.bill.model.dto.BillAppClient;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;
import model.system.SessionLogin;
import utilities.MySQLQuery;

public class GetClientList {

    public static File getClientsList(BillInstance bi, SessionLogin sl, Connection instConn) throws Exception {

        BillSpan cons = BillSpan.getByState("cons", instConn);
        int spanId = cons.id;
        if (bi.isNetInstance() && !cons.paramsDone) {
            throw new Exception("Debe iniciar la facturación en sitio");
        }

        boolean showApartment = new MySQLQuery("SELECT show_apartment FROM sigma.sys_cfg WHERE id = 1").getAsBoolean(instConn);

        Map<Integer, Boolean> buildTypeMap = new HashMap<>();
        if (bi.isTankInstance()) {
            Object[][] dataBuild = new MySQLQuery("SELECT b.id , t.residential "
                    + " FROM bill_building b "
                    + " INNER JOIN sigma.ord_tank_client o ON b.id = o.mirror_id AND o.bill_instance_id = ?1 "
                    + " INNER JOIN sigma.est_tank_category c ON c.id = o.categ_id "
                    + " INNER JOIN sigma.est_categ_type t ON t.id = c.type_id ").setParam(1, bi.id).getRecords(instConn);
            for (Object[] row : dataBuild) {
                buildTypeMap.put(MySQLQuery.getAsInteger(row[0]), MySQLQuery.getAsBoolean(row[1]));
            }
        }

        Object[][] clieData;
        clieData = new MySQLQuery("SELECT "
                + "cli.id, "//0
                + "cli.building_id, " //1
                + "cli.neigh_id, " //2
                + "CONCAT(cli.first_name,' ',COALESCE(last_name,'')), "//3
                + "IFNULL(cli.address, cli.apartment), " //4
                + "(SELECT `number` FROM bill_meter WHERE client_id = cli.id ORDER BY start_span_id DESC LIMIT 1), "//5
                + "cli.num_install, "//6
                + "COALESCE(curre.last_reading, m.start_reading, last1.reading, 0), "//7 anterior
                + "curre.reading, "//8 actual
                + "ROUND((last1.reading - last1.last_reading), 1), "//9
                + "ROUND((last2.reading - last2.last_reading), 1), "//10
                + "ROUND((last3.reading - last3.last_reading), 1), "//11
                + "ROUND((last4.reading - last4.last_reading), 1), "//12     
                + "ROUND((last5.reading - last5.last_reading), 1), "//13     
                + "ROUND((last6.reading - last6.last_reading), 1), "//14     
                + "cli.span_closed, "//15
                + "curre.fault_id, "//16
                + (bi.isTankInstance() ? "0 " : "IF(cli.sector_type IS NOT NULL AND cli.sector_type = 'r', 1, 0) ")//17
                + "FROM bill_client_tank AS cli "
                + "LEFT JOIN bill_reading AS curre ON curre.client_tank_id = cli.id AND curre.span_id = " + (spanId - 0) + " "
                + "LEFT JOIN bill_reading AS last1 ON last1.client_tank_id = cli.id AND last1.span_id = " + (spanId - 1) + " "
                + "LEFT JOIN bill_reading AS last2 ON last2.client_tank_id = cli.id AND last2.span_id = " + (spanId - 2) + " "
                + "LEFT JOIN bill_reading AS last3 ON last3.client_tank_id = cli.id AND last3.span_id = " + (spanId - 3) + " "
                + "LEFT JOIN bill_reading AS last4 ON last4.client_tank_id = cli.id AND last4.span_id = " + (spanId - 4) + " "
                + "LEFT JOIN bill_reading AS last5 ON last5.client_tank_id = cli.id AND last5.span_id = " + (spanId - 5) + " "
                + "LEFT JOIN bill_reading AS last6 ON last6.client_tank_id = cli.id AND last6.span_id = " + (spanId - 6) + " "
                + "LEFT JOIN bill_meter AS m ON m.client_id = cli.id AND m.start_span_id = " + spanId + " "
                + "WHERE active = 1 "
                + "ORDER BY cli.num_install ASC").getRecords(instConn);

        List<BillAppClient> clients = new ArrayList<>();

        /*IMPORTANTE LEER ANTES DE HACER CAMBIOS AQUI*/
 /*leer con calma*/
 /*el orden en este ciclo no importa, El orden que importa es el de la 
        clase BillClientApp -> con ese mismo orden se debe recuparar los datos en cliente*/
        for (Object[] row : clieData) {
            /*IMPORTANTE LEER LA NOTA ANTERIOR*/
            BillAppClient obj = new BillAppClient();
            obj.id = MySQLQuery.getAsInteger(row[0]);
            obj.buildId = MySQLQuery.getAsInteger(row[1]);
            obj.neighId = MySQLQuery.getAsInteger(row[2]);
            obj.ownerName = MySQLQuery.getAsString(row[3]);
            obj.address = MySQLQuery.getAsString(row[4]);
            obj.numMeter = MySQLQuery.getAsString(row[5]);
            obj.numInstall = MySQLQuery.getAsString(row[6]);
            obj.lastRead = MySQLQuery.getAsDouble(row[7]);
            obj.currRead = MySQLQuery.getAsDouble(row[8]);
            obj.c1 = MySQLQuery.getAsDouble(row[9]);
            obj.c2 = MySQLQuery.getAsDouble(row[10]);
            obj.c3 = MySQLQuery.getAsDouble(row[11]);
            obj.c4 = MySQLQuery.getAsDouble(row[12]);
            obj.c5 = MySQLQuery.getAsDouble(row[13]);
            obj.c6 = MySQLQuery.getAsDouble(row[14]);
            obj.months = 0;
            if (bi.isTankInstance()) {
                obj.buildName = (showApartment ? obj.address : obj.numInstall) + (obj.numMeter != null ? " (" + obj.numMeter + ")" : "");
            } else {
                obj.buildName = "";
            }

            obj.spanClosed = MySQLQuery.getAsBoolean(row[15]);
            obj.faultId = MySQLQuery.getAsInteger(row[16]);
            if (bi.isTankInstance()) {
                if (buildTypeMap.containsKey(obj.buildId)) {
                    obj.residential = buildTypeMap.get(obj.buildId);
                } else {
                    //en produccción debe estar activo
                    throw new Exception("Broken mirror, comuníquese con sistemas");
                }
            } else {
                obj.residential = MySQLQuery.getAsBoolean(row[17]);
            }
            clients.add(obj);
            /*IMPORTANTE LEER LA NOTA ANTERIOR*/
        }

        File tmp = File.createTempFile("readings", "");
        FileOutputStream fos = new FileOutputStream(tmp);

        try (GZIPOutputStream goz = new GZIPOutputStream(fos);
                OutputStreamWriter osw = new OutputStreamWriter(goz, "UTF8");
                PrintWriter w = new PrintWriter(osw, true)) {

            w.write(String.valueOf(clients.size()));

            for (BillAppClient obj : clients) {
                w.write(13);
                w.write(10);
                obj.writeObject(w);
            }
        }
        return tmp;
    }
}
