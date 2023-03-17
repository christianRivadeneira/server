package api.sys.model;

import api.BaseModel;
import api.Params;
import api.sys.dto.Option;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import utilities.MySQLQuery;

public class Menu extends BaseModel<Menu> {
//inicio zona de reemplazo

    public String label;
    public String className;
    public int place;
    public String iconPath;
    public String webPath;
    public String webIcon;
    public String regType;
    public Integer supId;
    public String cache;
    public Boolean hasShortName;
    public boolean locked;
    public Integer color;
    public String modCfgClass;
    public String profCfgClass;
    public String frmProfClass;
    public String frmModClass;
    public String pnlClass;
    public String frmAuthDeskClass;
    public String frmAuthMobileClass;
    public String crudType;
    public String crudTblName;
    public String crudGridName;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "label",
            "className",
            "place",
            "icon_path",
            "web_path",
            "web_icon",
            "reg_type",
            "sup_id",
            "cache",
            "has_short_name",
            "locked",
            "color",
            "mod_cfg_class",
            "prof_cfg_class",
            "frm_prof_class",
            "frm_mod_class",
            "pnl_class",
            "frm_auth_desk_class",
            "frm_auth_mobile_class",
            "crud_type",
            "crud_tbl_name",
            "crud_grid_name"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, label);
        q.setParam(2, className);
        q.setParam(3, place);
        q.setParam(4, iconPath);
        q.setParam(5, webPath);
        q.setParam(6, webIcon);
        q.setParam(7, regType);
        q.setParam(8, supId);
        q.setParam(9, cache);
        q.setParam(10, hasShortName);
        q.setParam(11, locked);
        q.setParam(12, color);
        q.setParam(13, modCfgClass);
        q.setParam(14, profCfgClass);
        q.setParam(15, frmProfClass);
        q.setParam(16, frmModClass);
        q.setParam(17, pnlClass);
        q.setParam(18, frmAuthDeskClass);
        q.setParam(19, frmAuthMobileClass);
        q.setParam(20, crudType);
        q.setParam(21, crudTblName);
        q.setParam(22, crudGridName);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        label = MySQLQuery.getAsString(row[0]);
        className = MySQLQuery.getAsString(row[1]);
        place = MySQLQuery.getAsInteger(row[2]);
        iconPath = MySQLQuery.getAsString(row[3]);
        webPath = MySQLQuery.getAsString(row[4]);
        webIcon = MySQLQuery.getAsString(row[5]);
        regType = MySQLQuery.getAsString(row[6]);
        supId = MySQLQuery.getAsInteger(row[7]);
        cache = MySQLQuery.getAsString(row[8]);
        hasShortName = MySQLQuery.getAsBoolean(row[9]);
        locked = MySQLQuery.getAsBoolean(row[10]);
        color = MySQLQuery.getAsInteger(row[11]);
        modCfgClass = MySQLQuery.getAsString(row[12]);
        profCfgClass = MySQLQuery.getAsString(row[13]);
        frmProfClass = MySQLQuery.getAsString(row[14]);
        frmModClass = MySQLQuery.getAsString(row[15]);
        pnlClass = MySQLQuery.getAsString(row[16]);
        frmAuthDeskClass = MySQLQuery.getAsString(row[17]);
        frmAuthMobileClass = MySQLQuery.getAsString(row[18]);
        crudType = MySQLQuery.getAsString(row[19]);
        crudTblName = MySQLQuery.getAsString(row[20]);
        crudGridName = MySQLQuery.getAsString(row[21]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "menu";
    }

    public static String getSelFlds(String alias) {
        return new Menu().getSelFldsForAlias(alias);
    }

    public static List<Menu> getList(MySQLQuery q, Connection conn) throws Exception {
        return new Menu().getListFromQuery(q, conn);
    }

    public static List<Menu> getList(Params p, Connection conn) throws Exception {
        return new Menu().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new Menu().deleteById(id, conn);
    }

    public static List<Menu> getAll(Connection conn) throws Exception {
        return new Menu().getAllList(conn);
    }

//fin zona de reemplazo
    
    public static Menu[] getAllMenues(Connection ep) throws Exception {
        String qs = "SELECT " + getSelFlds("m") + ", m.id FROM menu as m ORDER BY m.place ASC ";
        Object[][] res = new MySQLQuery(qs).getRecords(ep);
        Menu[] lst = new Menu[res.length];

        for (int i = 0; i < lst.length; i++) {
            Object[] row = res[i];
            lst[i] = new Menu();
            lst[i].setRow(row);
        }
        return lst;
    }

    public static Menu[] getMenues(String type1, String type2, Integer parId, Menu[] all) throws Exception {
        List<Menu> res = new ArrayList<>();
        for (Menu menu : all) {
            if (type2 != null) {
                if ((type1.equals(menu.regType) || type2.equals(menu.regType)) && (parId != null ? parId.equals(menu.supId) : menu.supId == null)) {
                    res.add(menu);
                }
            } else {
                if (type1.equals(menu.regType) && (parId != null ? parId.equals(menu.supId) : menu.supId == null)) {
                    res.add(menu);
                }
            }
        }
        return res.toArray(new Menu[0]);
    }

    public static String getModsByUsrQuery(int empId) {
        if (empId == 1) {
            return "SELECT " + getSelFlds("m") + ", id FROM menu m WHERE id in ("
                    + "select distinct m.id from  "
                    + "menu m "
                    + "inner join profile p on p.menu_id = m.id and p.active and p.is_mobile = false "
                    + "inner join login l on l.profile_id = p.id) "
                    + "order by label;";

        } else {
            return "SELECT " + getSelFlds("m") + " "
                    + "FROM login l "
                    + "INNER JOIN profile p ON p.id = l.profile_id AND p.is_mobile = false AND p.active "
                    + "INNER JOIN menu m ON m.id = p.menu_id "
                    + "WHERE l.employee_id = " + empId + " "
                    + "GROUP BY p.menu_id "
                    + "ORDER BY m.label ";
        }
    }

    public static List<Option> getBarOptionsByProfile(int profId, Connection conn) throws Exception {
        MySQLQuery mq = new MySQLQuery("SELECT bar.id, bar.label, bar.web_icon, "
                + " bar.web_path, bar.place, bar.reg_type, bar.crud_tbl_name, bar.crud_grid_name "
                + " FROM menu AS `mod` "
                + " INNER JOIN menu AS bar ON bar.sup_id = `mod`.id AND bar.reg_type = 'bar' "
                + " LEFT JOIN menu AS opt ON opt.sup_id = bar.id AND opt.reg_type = 'opt' "
                + " LEFT JOIN menu AS sub ON sub.sup_id = bar.id AND sub.reg_type = 'sub' "
                + " LEFT JOIN menu AS sopt ON sopt.sup_id = sub.id AND sopt.reg_type = 'sopt' "
                + " LEFT JOIN permission AS soptPerm ON soptPerm.menu_id = sopt.id "
                + " AND soptPerm.profile_id = " + profId
                + " LEFT JOIN permission AS optPerm ON optPerm.menu_id = opt.id "
                + " AND optPerm.profile_id = " + profId
                + " WHERE mod.reg_type = 'mod' AND soptPerm.id IS NOT NULL OR optPerm.id IS NOT NULL "
                + " GROUP BY bar.id "
                + " ORDER BY `mod`.place ASC, bar.place ASC ");
        Object[][] data = mq.getRecords(conn);
        List<Option> barOpts = new ArrayList<>();
        if (data != null) {
            for (Object[] obj : data) {
                Option opt = new Option();
                opt.id = MySQLQuery.getAsInteger(obj[0]);
                opt.name = MySQLQuery.getAsString(obj[1]);
                opt.webIcon = MySQLQuery.getAsString(obj[2]);
                opt.webPath = MySQLQuery.getAsString(obj[3]);
                opt.place = MySQLQuery.getAsInteger(obj[4]);
                opt.type = MySQLQuery.getAsString(obj[5]);
                opt.crudTblName = MySQLQuery.getAsString(obj[6]);
                opt.crudGridName = MySQLQuery.getAsString(obj[7]);
                barOpts.add(opt);
            }
        }
        return barOpts;
    }

    public static List<Option> getOptionsByBar(int profId, int barId, Connection conn) throws Exception {
        MySQLQuery mq = new MySQLQuery("SELECT DISTINCT "
                + " menu.id, menu.label, menu.web_path, menu.place, "
                + " menu.web_icon, menu.reg_type, menu.crud_tbl_name, menu.crud_grid_name "
                + " FROM menu "
                + " LEFT JOIN permission AS optPerm ON optPerm.menu_id = menu.id AND optPerm.profile_id = " + profId
                + " LEFT JOIN menu AS sopt ON sopt.sup_id = menu.id "
                + " LEFT JOIN permission AS soptPerm ON soptPerm.menu_id = sopt.id AND soptPerm.profile_id = " + profId
                + " WHERE menu.sup_id = " + barId + " AND (optPerm.id IS NOT NULL OR soptPerm.id IS NOT NULL) "
                + " ORDER BY menu.place ASC ");

        Object[][] data = mq.getRecords(conn);
        List<Option> barOpts = new ArrayList<>();
        if (data != null) {
            for (Object[] obj : data) {
                Option opt = new Option();
                opt.id = MySQLQuery.getAsInteger(obj[0]);
                opt.name = MySQLQuery.getAsString(obj[1]);
                opt.webPath = MySQLQuery.getAsString(obj[2]);
                opt.place = MySQLQuery.getAsInteger(obj[3]);
                opt.webIcon = MySQLQuery.getAsString(obj[4]);
                opt.type = MySQLQuery.getAsString(obj[5]);
                opt.crudTblName = MySQLQuery.getAsString(obj[6]);
                opt.crudGridName = MySQLQuery.getAsString(obj[7]);
                barOpts.add(opt);
            }
        }
        return barOpts;
    }

    public static List<Option> getSubOptions(int profId, int optId, Connection conn) throws Exception {
        MySQLQuery mq = new MySQLQuery("SELECT "
                + " menu.id, menu.label, menu.web_path, menu.place, "
                + " menu.web_icon, menu.reg_type, menu.crud_tbl_name, menu.crud_grid_name "
                + " FROM menu "
                + " INNER JOIN permission AS soptPerm ON soptPerm.menu_id = menu.id "
                + " WHERE menu.reg_type = 'sopt' AND soptPerm.profile_id = " + profId + " AND menu.sup_id = " + optId + " "
                + " ORDER BY menu.place ASC ");

        Object[][] data = mq.getRecords(conn);
        List<Option> subOpts = new ArrayList<>();
        if (data != null) {
            for (Object[] obj : data) {
                Option opt = new Option();
                opt.id = MySQLQuery.getAsInteger(obj[0]);
                opt.name = MySQLQuery.getAsString(obj[1]);
                opt.webPath = MySQLQuery.getAsString(obj[2]);
                opt.place = MySQLQuery.getAsInteger(obj[3]);
                opt.webIcon = MySQLQuery.getAsString(obj[4]);
                opt.type = MySQLQuery.getAsString(obj[5]);
                opt.crudTblName = MySQLQuery.getAsString(obj[6]);
                opt.crudGridName = MySQLQuery.getAsString(obj[7]);
                subOpts.add(opt);
            }
        }
        return subOpts;

    }

}
