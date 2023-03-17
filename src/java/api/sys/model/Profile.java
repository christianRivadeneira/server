package api.sys.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class Profile extends BaseModel<Profile> {
//inicio zona de reemplazo

    public String name;
    public String description;
    public String restrictionForm;
    public Boolean restricted;
    public String welcomeForm;
    public Boolean showEveryLogin;
    public Integer authById;
    public Integer menuId;
    public boolean guest;
    public boolean isMobile;
    public boolean editCfg;
    public boolean showPnl;
    public boolean lockIdleSessions;
    public String helpAddr;
    public boolean active;
    public boolean showBi;
    public boolean createDash;
    public boolean createGraph;
    public boolean bigIcon;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "description",
            "restriction_form",
            "restricted",
            "welcome_form",
            "show_every_login",
            "auth_by_id",
            "menu_id",
            "guest",
            "is_mobile",
            "edit_cfg",
            "show_pnl",
            "lock_idle_sessions",
            "help_addr",
            "active",
            "show_bi",
            "create_dash",
            "create_graph",
            "big_icon"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, description);
        q.setParam(3, restrictionForm);
        q.setParam(4, restricted);
        q.setParam(5, welcomeForm);
        q.setParam(6, showEveryLogin);
        q.setParam(7, authById);
        q.setParam(8, menuId);
        q.setParam(9, guest);
        q.setParam(10, isMobile);
        q.setParam(11, editCfg);
        q.setParam(12, showPnl);
        q.setParam(13, lockIdleSessions);
        q.setParam(14, helpAddr);
        q.setParam(15, active);
        q.setParam(16, showBi);
        q.setParam(17, createDash);
        q.setParam(18, createGraph);
        q.setParam(19, bigIcon);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        description = MySQLQuery.getAsString(row[1]);
        restrictionForm = MySQLQuery.getAsString(row[2]);
        restricted = MySQLQuery.getAsBoolean(row[3]);
        welcomeForm = MySQLQuery.getAsString(row[4]);
        showEveryLogin = MySQLQuery.getAsBoolean(row[5]);
        authById = MySQLQuery.getAsInteger(row[6]);
        menuId = MySQLQuery.getAsInteger(row[7]);
        guest = MySQLQuery.getAsBoolean(row[8]);
        isMobile = MySQLQuery.getAsBoolean(row[9]);
        editCfg = MySQLQuery.getAsBoolean(row[10]);
        showPnl = MySQLQuery.getAsBoolean(row[11]);
        lockIdleSessions = MySQLQuery.getAsBoolean(row[12]);
        helpAddr = MySQLQuery.getAsString(row[13]);
        active = MySQLQuery.getAsBoolean(row[14]);
        showBi = MySQLQuery.getAsBoolean(row[15]);
        createDash = MySQLQuery.getAsBoolean(row[16]);
        createGraph = MySQLQuery.getAsBoolean(row[17]);
        bigIcon = MySQLQuery.getAsBoolean(row[18]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "profile";
    }

    public static String getSelFlds(String alias) {
        return new Profile().getSelFldsForAlias(alias);
    }

    public static List<Profile> getList(MySQLQuery q, Connection conn) throws Exception {
        return new Profile().getListFromQuery(q, conn);
    }

    public static List<Profile> getList(Params p, Connection conn) throws Exception {
        return new Profile().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new Profile().deleteById(id, conn);
    }

    public static List<Profile> getAll(Connection conn) throws Exception {
        return new Profile().getAllList(conn);
    }

//fin zona de reemplazo
    public static Profile getFromRow(Object[] row) throws Exception {
        Profile obj = new Profile();
        obj.name = MySQLQuery.getAsString(row[0]);
        obj.description = MySQLQuery.getAsString(row[1]);
        obj.restrictionForm = MySQLQuery.getAsString(row[2]);
        obj.restricted = MySQLQuery.getAsBoolean(row[3]);
        obj.welcomeForm = MySQLQuery.getAsString(row[4]);
        obj.showEveryLogin = MySQLQuery.getAsBoolean(row[5]);
        obj.authById = MySQLQuery.getAsInteger(row[6]);
        obj.menuId = MySQLQuery.getAsInteger(row[7]);
        obj.guest = MySQLQuery.getAsBoolean(row[8]);
        obj.isMobile = MySQLQuery.getAsBoolean(row[9]);
        obj.editCfg = MySQLQuery.getAsBoolean(row[10]);
        obj.showPnl = MySQLQuery.getAsBoolean(row[11]);
        obj.lockIdleSessions = MySQLQuery.getAsBoolean(row[12]);
        obj.helpAddr = MySQLQuery.getAsString(row[13]);
        obj.active = MySQLQuery.getAsBoolean(row[14]);
        obj.showBi = MySQLQuery.getAsBoolean(row[15]);
        obj.createDash = MySQLQuery.getAsBoolean(row[16]);
        obj.createGraph = MySQLQuery.getAsBoolean(row[17]);
        obj.bigIcon = MySQLQuery.getAsBoolean(row[18]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);

        return obj;
    }

    public static Profile[] getProfiles(Object[][] data) throws Exception {
        Profile[] res = new Profile[data.length];
        for (int i = 0; i < data.length; i++) {
            res[i] = getFromRow(data[i]);
        }
        return res;
    }

    public static String getProfilesMobileByEmployeeQuery(Integer empId, int menuId) {
        if (empId == 1) {
            return "SELECT DISTINCT " + getSelFlds("p") + ", p.id "
                    + "FROM profile p  "
                    + "WHERE p.active "
                    + "AND p.menu_id = " + menuId + " "
                    + "AND p.is_mobile = true "
                    + "ORDER BY p.menu_id, p.name;";

        } else {
            return "SELECT " + getSelFlds("") + ", id "
                    + "FROM profile "
                    + "WHERE id IN (SELECT DISTINCT p.id "
                    + "FROM profile p  "
                    + "INNER JOIN login l ON l.profile_id = p.id "
                    + "WHERE p.active "
                    + "AND l.employee_id = " + empId + " "
                    + "AND p.is_mobile = true "
                    + "AND p.menu_id = " + menuId + ") "
                    + "ORDER BY menu_id, name;";
        }
    }

    public static String getProfilesByEmployeeQuery(int empId) {
        if (empId == 1) {
            return "SELECT DISTINCT " + getSelFlds("p") + ", id "
                    + "FROM profile p  "
                    + "WHERE p.active and p.menu_id IS NOT NULL and p.is_mobile = false "
                    + "ORDER BY p.menu_id, p.name;";

        } else {
            return "SELECT " + getSelFlds("p")
                    + "FROM profile p "
                    + "INNER JOIN menu m ON m.id = p.menu_id "
                    + "INNER JOIN login l ON l.profile_id = p.id "
                    + "WHERE "
                    + "p.is_mobile = false AND p.active AND l.employee_id = " + empId + " "
                    + "ORDER BY m.label ";
        }
    }

    public static Integer getRandomProfile(int empId, Connection conn) throws Exception {
        return new MySQLQuery("SELECT p.id "
                + "FROM profile p "
                + "INNER JOIN menu m ON m.id = p.menu_id "
                + "INNER JOIN login l ON l.profile_id = p.id "
                + "WHERE "
                + "p.is_mobile = false AND p.active AND l.employee_id = " + empId + " "
                + "ORDER BY m.label DESC LIMIT 1 ").getAsInteger(conn);
    }

    public static List<List<Object>> getProfilesApp(int empId, String cfgTable, List<String> fields, Connection conn) throws Exception {
        StringBuilder sb = new StringBuilder();
        for (String fld : fields) {
            sb.append("cfg").append(".");
            sb.append("`").append(fld).append("`").append(",");
        }

        String field = sb.toString();
        field = field.substring(0, field.length()-1);
        cfgTable = MySQLQuery.scape(cfgTable);
        MySQLQuery mq = new MySQLQuery("SELECT " + field + " "
                + "FROM profile p "
                + "INNER JOIN " + cfgTable + " cfg ON cfg.prof_id = p.id "
                + "INNER JOIN login l ON l.profile_id = p.id AND l.employee_id = ?1 "
                + "WHERE p.is_mobile").setParam(1, empId);

        return mq.getRecordsAsList(conn);
    }

}
