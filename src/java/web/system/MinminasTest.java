/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web.system;

import api.sys.model.SysMinasTest;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@Startup
@Singleton

public class MinminasTest {

    @Schedule(minute = "*/1", second = "0", hour = "*")
    public void checkMinas() throws Exception {
        Integer time = null;
        String exName = null;
        try (Connection con = MySQLCommon.getDefaultConnection()) {
            if (new MySQLQuery("SELECT sys_minas_test FROM sys_cfg").getAsBoolean(con)) {
                try {
                    long t = System.currentTimeMillis();
                    HttpURLConnection conn = (HttpURLConnection) new URL("http://webglp.minminas.gov.co:8081/").openConnection();
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(10000);
                    conn.getResponseCode();
                    time = (int) (System.currentTimeMillis() - t);
                } catch (IOException ex) {
                    exName = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getName();
                }
                SysMinasTest t = new SysMinasTest();
                t.dt = new Date();
                t.t = time;
                t.ex = exName;
                t.insert(con);
            }
        } catch (Exception ex) {
            Logger.getLogger(MinminasTest.class.getName()).log(Level.INFO, "message", ex);
        }
    }
}
