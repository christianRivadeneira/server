package api.sys.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class SystemApp extends BaseModel<SystemApp> {
//inicio zona de reemplazo

    public String packageName;
    public String version;
    public String googleProjectNumber;
    public String googleServerKey;
    public String showName;
    public String description;
    public Boolean showInWeb;
    public Boolean mandatory;
    public Integer gpsPriority;
    public String appDownloadImg;
    public String appDownloadUrl;
    public String minVerAllow;
    public boolean remoteLog;
    public Date uploadDate;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "package_name",
            "version",
            "google_project_number",
            "google_server_key",
            "show_name",
            "description",
            "show_in_web",
            "mandatory",
            "gps_priority",
            "app_download_img",
            "app_download_url",
            "min_ver_allow",
            "remote_log",
            "upload_date"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, packageName);
        q.setParam(2, version);
        q.setParam(3, googleProjectNumber);
        q.setParam(4, googleServerKey);
        q.setParam(5, showName);
        q.setParam(6, description);
        q.setParam(7, showInWeb);
        q.setParam(8, mandatory);
        q.setParam(9, gpsPriority);
        q.setParam(10, appDownloadImg);
        q.setParam(11, appDownloadUrl);
        q.setParam(12, minVerAllow);
        q.setParam(13, remoteLog);
        q.setParam(14, uploadDate);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        packageName = MySQLQuery.getAsString(row[0]);
        version = MySQLQuery.getAsString(row[1]);
        googleProjectNumber = MySQLQuery.getAsString(row[2]);
        googleServerKey = MySQLQuery.getAsString(row[3]);
        showName = MySQLQuery.getAsString(row[4]);
        description = MySQLQuery.getAsString(row[5]);
        showInWeb = MySQLQuery.getAsBoolean(row[6]);
        mandatory = MySQLQuery.getAsBoolean(row[7]);
        gpsPriority = MySQLQuery.getAsInteger(row[8]);
        appDownloadImg = MySQLQuery.getAsString(row[9]);
        appDownloadUrl = MySQLQuery.getAsString(row[10]);
        minVerAllow = MySQLQuery.getAsString(row[11]);
        remoteLog = MySQLQuery.getAsBoolean(row[12]);
        uploadDate = MySQLQuery.getAsDate(row[13]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "system_app";
    }

    public static String getSelFlds(String alias) {
        return new SystemApp().getSelFldsForAlias(alias);
    }

    public static List<SystemApp> getList(MySQLQuery q, Connection conn) throws Exception {
        return new SystemApp().getListFromQuery(q, conn);
    }

    public static List<SystemApp> getList(Params p, Connection conn) throws Exception {
        return new SystemApp().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new SystemApp().deleteById(id, conn);
    }

    public static List<SystemApp> getAll(Connection conn) throws Exception {
        return new SystemApp().getAllList(conn);
    }

//fin zona de reemplazo
    public static List<SystemApp> getAllApps(Connection con) throws Exception {
        Params p = new Params("show_in_web", true);
        p.orderBy("show_name");
        return new SystemApp().getListFromParams(p, con);
    }

    public static void verifyMinVersion(Integer appId, String packageName, String version, Connection conn) throws Exception {
        if (version.isEmpty()) {
            throw new Exception("No se ha suministrado información de la versión");
        }

        SystemApp app = null;
        if (appId != null) {
            app = new SystemApp().select(appId, conn);
        } else if (!MySQLQuery.isEmpty(packageName)) {
            MySQLQuery mq = new MySQLQuery("SELECT " + SystemApp.getSelFlds("") + " FROM system_app WHERE package_name = ?1 ").setParam(1, packageName);
            app = new SystemApp().select(mq, conn);
        } else {
            throw new Exception("No se ha suministrado información de la apliación");
        }

        if (app.minVerAllow != null) {
            String versionParts[] = version.split("\\.");
            String minVersionParts[] = app.minVerAllow.split("\\.");
            int maxLength = Math.max(versionParts.length, minVersionParts.length);
            for (int i = 0; i < maxLength; i++) {
                int min = minVersionParts.length < i + 1 || MySQLQuery.isEmpty(minVersionParts[i]) ? 0 : Integer.valueOf(minVersionParts[i]);
                int cur = versionParts.length < i + 1 || MySQLQuery.isEmpty(versionParts[i]) ? 0 : Integer.valueOf(versionParts[i]);
                if (cur < min) {
                    throw new Exception("Debe actualizar la aplicación para poder continuar");
                }
            }
        }
    }

    public static SystemApp getByPkgName(String pkgName, Connection conn) throws Exception {
        Params params = new Params("package_name", pkgName);
        return new SystemApp().select(params, conn);
    }
}
