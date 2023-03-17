package api.hlp.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class HlpType extends BaseModel<HlpType> {
//inicio zona de reemplazo

    public Integer empId;
    public String name;
    public String desc;
    public Boolean defaultId;
    public Boolean hasSolutionCause;
    public Boolean mandatoryLimit;
    public Boolean hasEquips;
    public Boolean hasTyping;
    public Boolean isAdministrative;
    public boolean mandatoryDetailCase;
    public boolean tradeCase;
    public boolean hasServiceTyping;
    public boolean showInDesktop;
    public boolean hasRelapse;
    public boolean defaultTasks;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "emp_id",
            "name",
            "desc",
            "default_id",
            "has_solution_cause",
            "mandatory_limit",
            "has_equips",
            "has_typing",
            "is_administrative",
            "mandatory_detail_case",
            "trade_case",
            "has_service_typing",
            "show_in_desktop",
            "has_relapse",
            "default_tasks"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, empId);
        q.setParam(2, name);
        q.setParam(3, desc);
        q.setParam(4, defaultId);
        q.setParam(5, hasSolutionCause);
        q.setParam(6, mandatoryLimit);
        q.setParam(7, hasEquips);
        q.setParam(8, hasTyping);
        q.setParam(9, isAdministrative);
        q.setParam(10, mandatoryDetailCase);
        q.setParam(11, tradeCase);
        q.setParam(12, hasServiceTyping);
        q.setParam(13, showInDesktop);
        q.setParam(14, hasRelapse);
        q.setParam(15, defaultTasks);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        empId = MySQLQuery.getAsInteger(row[0]);
        name = MySQLQuery.getAsString(row[1]);
        desc = MySQLQuery.getAsString(row[2]);
        defaultId = MySQLQuery.getAsBoolean(row[3]);
        hasSolutionCause = MySQLQuery.getAsBoolean(row[4]);
        mandatoryLimit = MySQLQuery.getAsBoolean(row[5]);
        hasEquips = MySQLQuery.getAsBoolean(row[6]);
        hasTyping = MySQLQuery.getAsBoolean(row[7]);
        isAdministrative = MySQLQuery.getAsBoolean(row[8]);
        mandatoryDetailCase = MySQLQuery.getAsBoolean(row[9]);
        tradeCase = MySQLQuery.getAsBoolean(row[10]);
        hasServiceTyping = MySQLQuery.getAsBoolean(row[11]);
        showInDesktop = MySQLQuery.getAsBoolean(row[12]);
        hasRelapse = MySQLQuery.getAsBoolean(row[13]);
        defaultTasks = MySQLQuery.getAsBoolean(row[14]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "hlp_type";
    }

    public static String getSelFlds(String alias) {
        return new HlpType().getSelFldsForAlias(alias);
    }

    public static List<HlpType> getList(MySQLQuery q, Connection conn) throws Exception {
        return new HlpType().getListFromQuery(q, conn);
    }

    public static List<HlpType> getList(Params p, Connection conn) throws Exception {
        return new HlpType().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new HlpType().deleteById(id, conn);
    }

    public static List<HlpType> getAll(Connection conn) throws Exception {
        return new HlpType().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<HlpType> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/
    public static HlpType getDevel(Connection conn) throws Exception {
        Params p = new Params();
        p.param("isAdministrative", false);
        p.param("hasSolutionCause", false);
        p.param("tradeCase", false);
        p.param("default_tasks", true);        
        HlpType t = new HlpType().select(p, conn);
        if (t == null) {
            throw new Exception("No se ha configurado un tipo para desarrollo");
        }
        return t;
    }

}
