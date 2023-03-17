/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.rpt.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;
import utilities.MySQLQuery;
import utilities.json.JSONEncoder;

public class Fix {

    public static void filtLists(Connection conn) throws Exception {
        Object[][] data = new MySQLQuery("SELECT id, filt FROM rpt_rpt_fld WHERE filt IS NOT NULL").getRecords(conn);
        for (Object[] row : data) {
            int id = MySQLQuery.getAsInteger(row[0]);
            byte[] bin = (byte[]) row[1];
            String json = writeJSON(readFiltList(bin));
            new MySQLQuery("UPDATE rpt_rpt_fld SET filt_json = ?1 WHERE id = ?2").setParam(1, json).setParam(2, id).executeUpdate(conn);
        }

        data = new MySQLQuery("SELECT id, filt FROM rpt_dash_filt WHERE filt IS NOT NULL").getRecords(conn);
        for (Object[] row : data) {
            int id = MySQLQuery.getAsInteger(row[0]);
            byte[] bin = (byte[]) row[1];
            String json = writeJSON(readFiltList(bin));
            new MySQLQuery("UPDATE rpt_dash_filt SET filt_json = ?1 WHERE id = ?2").setParam(1, json).setParam(2, id).executeUpdate(conn);
        }
    }

    protected static String writeJSON(List<Object> objs) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JSONEncoder.encode(objs, baos, false);
        baos.close();
        return new String(baos.toByteArray());
    }

    protected static List<Object> readFiltList(byte[] bytes) throws Exception {
        try (ByteArrayInputStream baos = new ByteArrayInputStream(bytes); GZIPInputStream goz = new GZIPInputStream(baos); ObjectInputStream oos = new ObjectInputStream(goz)) {
            Object[] rlst = (Object[]) oos.readObject();
            List<Object> lst = new ArrayList();
            Collections.addAll(lst, rlst);
            return lst;
        }
    }
}
