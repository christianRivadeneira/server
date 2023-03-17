package utilities;

import java.sql.Connection;
import javax.persistence.EntityManager;

public class DBSettings {

    public String host;
    public String port;
    public String db;
    public String user;
    public String pass;

    /**
     * @param em La conexión permance abierta
     */
    
    public DBSettings(EntityManager em) {
        this(em.createNativeQuery("SELECT db_url FROM sys_cfg").getSingleResult().toString());
    }

    /**
     * 
     * @param conn la conexión permanece abierta
     * @throws Exception 
     */
    
    public DBSettings(Connection conn) throws Exception {
        this(new MySQLQuery("SELECT db_url FROM sys_cfg").getAsString(conn));
    }

    public DBSettings(String str) {
        int i1 = str.indexOf("//");
        int i2 = str.indexOf(":", i1);
        int i3 = str.indexOf("/", i2);
        int i4 = str.indexOf("?", i3);
        host = str.substring(i1 + 2, i2);
        port = str.substring(i2 + 1, i3);
        db = str.substring(i3 + 1, i4);
        String params = str.substring(i4 + 1);
        String[] pars = params.split("&");
        user = pars[0].split("=")[1];
        pass = pars[1].split("=")[1];
    }
}
