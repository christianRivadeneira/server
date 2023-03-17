package api.sys.model;

import api.BaseModel;
import api.Params;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class SysCenter extends BaseModel<SysCenter> {
//inicio zona de reemplazo

    public String code;
    public String name;
    public String address;
    public String nie;
    public String autosaleCode;
    public String origWhouse;
    public BigDecimal lat;
    public BigDecimal lon;
    public int typeId;
    public Integer enterpriseId;
    public Integer zoneId;
    public Integer daneDepId;
    public Integer cityId;
    public Integer dtoCenterId;
    public Integer estOfficeId;
    public Integer agencyId;
    public Integer invCenterId;
    public Integer gtCenterId;
    public Integer eqsCenterId;
    public boolean visibleDto;
    public boolean visibleEst;
    public boolean visibleMto;
    public boolean visibleCyl;
    public boolean visibleGt;
    public boolean visibleEqs;
    public boolean active;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "code",
            "name",
            "address",
            "nie",
            "autosale_code",
            "orig_whouse",
            "lat",
            "lon",
            "type_id",
            "enterprise_id",
            "zone_id",
            "dane_dep_id",
            "city_id",
            "dto_center_id",
            "est_office_id",
            "agency_id",
            "inv_center_id",
            "gt_center_id",
            "eqs_center_id",
            "visible_dto",
            "visible_est",
            "visible_mto",
            "visible_cyl",
            "visible_gt",
            "visible_eqs",
            "active"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, code);
        q.setParam(2, name);
        q.setParam(3, address);
        q.setParam(4, nie);
        q.setParam(5, autosaleCode);
        q.setParam(6, origWhouse);
        q.setParam(7, lat);
        q.setParam(8, lon);
        q.setParam(9, typeId);
        q.setParam(10, enterpriseId);
        q.setParam(11, zoneId);
        q.setParam(12, daneDepId);
        q.setParam(13, cityId);
        q.setParam(14, dtoCenterId);
        q.setParam(15, estOfficeId);
        q.setParam(16, agencyId);
        q.setParam(17, invCenterId);
        q.setParam(18, gtCenterId);
        q.setParam(19, eqsCenterId);
        q.setParam(20, visibleDto);
        q.setParam(21, visibleEst);
        q.setParam(22, visibleMto);
        q.setParam(23, visibleCyl);
        q.setParam(24, visibleGt);
        q.setParam(25, visibleEqs);
        q.setParam(26, active);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        code = MySQLQuery.getAsString(row[0]);
        name = MySQLQuery.getAsString(row[1]);
        address = MySQLQuery.getAsString(row[2]);
        nie = MySQLQuery.getAsString(row[3]);
        autosaleCode = MySQLQuery.getAsString(row[4]);
        origWhouse = MySQLQuery.getAsString(row[5]);
        lat = MySQLQuery.getAsBigDecimal(row[6], false);
        lon = MySQLQuery.getAsBigDecimal(row[7], false);
        typeId = MySQLQuery.getAsInteger(row[8]);
        enterpriseId = MySQLQuery.getAsInteger(row[9]);
        zoneId = MySQLQuery.getAsInteger(row[10]);
        daneDepId = MySQLQuery.getAsInteger(row[11]);
        cityId = MySQLQuery.getAsInteger(row[12]);
        dtoCenterId = MySQLQuery.getAsInteger(row[13]);
        estOfficeId = MySQLQuery.getAsInteger(row[14]);
        agencyId = MySQLQuery.getAsInteger(row[15]);
        invCenterId = MySQLQuery.getAsInteger(row[16]);
        gtCenterId = MySQLQuery.getAsInteger(row[17]);
        eqsCenterId = MySQLQuery.getAsInteger(row[18]);
        visibleDto = MySQLQuery.getAsBoolean(row[19]);
        visibleEst = MySQLQuery.getAsBoolean(row[20]);
        visibleMto = MySQLQuery.getAsBoolean(row[21]);
        visibleCyl = MySQLQuery.getAsBoolean(row[22]);
        visibleGt = MySQLQuery.getAsBoolean(row[23]);
        visibleEqs = MySQLQuery.getAsBoolean(row[24]);
        active = MySQLQuery.getAsBoolean(row[25]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "sys_center";
    }

    public static String getSelFlds(String alias) {
        return new SysCenter().getSelFldsForAlias(alias);
    }

    public static List<SysCenter> getList(MySQLQuery q, Connection conn) throws Exception {
        return new SysCenter().getListFromQuery(q, conn);
    }

    public static List<SysCenter> getList(Params p, Connection conn) throws Exception {
        return new SysCenter().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new SysCenter().deleteById(id, conn);
    }

    public static List<SysCenter> getAll(Connection conn) throws Exception {
        return new SysCenter().getAllList(conn);
    }

//fin zona de reemplazo
    public static SysCenter getSysCenterByCode(String centerCode, Connection conn) throws Exception {
        MySQLQuery mq = new MySQLQuery("SELECT " + getSelFlds("") + " FROM sys_center WHERE code = ?1").setParam(1, centerCode);
        return new SysCenter().select(mq, conn);
    }
}
