package api.sys.model;

import api.BaseModel;
import api.Params;
import api.mss.app.MssAppProfCfg;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class SysAppProfile extends BaseModel<SysAppProfile> {
//inicio zona de reemplazo

    public int appId;
    public String profCfgClass;
    public String frmProfCfgClass;
    public String name;
    public String description;
    public String testVersion;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "app_id",
            "prof_cfg_class",
            "frm_prof_cfg_class",
            "name",
            "description",
            "test_version"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, appId);
        q.setParam(2, profCfgClass);
        q.setParam(3, frmProfCfgClass);
        q.setParam(4, name);
        q.setParam(5, description);
        q.setParam(6, testVersion);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        appId = MySQLQuery.getAsInteger(row[0]);
        profCfgClass = MySQLQuery.getAsString(row[1]);
        frmProfCfgClass = MySQLQuery.getAsString(row[2]);
        name = MySQLQuery.getAsString(row[3]);
        description = MySQLQuery.getAsString(row[4]);
        testVersion = MySQLQuery.getAsString(row[5]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "sys_app_profile";
    }

    public static String getSelFlds(String alias) {
        return new SysAppProfile().getSelFldsForAlias(alias);
    }

    public static List<SysAppProfile> getList(MySQLQuery q, Connection conn) throws Exception {
        return new SysAppProfile().getListFromQuery(q, conn);
    }

    public static List<SysAppProfile> getList(Params p, Connection conn) throws Exception {
        return new SysAppProfile().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new SysAppProfile().deleteById(id, conn);
    }

    public static List<SysAppProfile> getAll(Connection conn) throws Exception {
        return new SysAppProfile().getAllList(conn);
    }

//fin zona de reemplazo
    public static SysAppProfile getProfileByName(String packName, String name, Connection conn) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT "+getSelFlds("p")+" FROM "
                + "sys_app_profile p "
                + "INNER JOIN system_app a ON a.id = p.app_id "
                + "WHERE a.package_name = ?1 "
                + "AND UPPER(p.name) LIKE ?2 ").setParam(1, packName).setParam(2,  name.toUpperCase());        
        return new SysAppProfile().select(q, conn);
    }
}
