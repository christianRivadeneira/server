package api.ess.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class EssPersonUnit extends BaseModel<EssPersonUnit> {
//inicio zona de reemplazo

    public int personId;
    public int unitId;
    public String callPriority;
    public boolean notify;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "person_id",
            "unit_id",
            "call_priority",
            "notify"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, personId);
        q.setParam(2, unitId);
        q.setParam(3, callPriority);
        q.setParam(4, notify);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        personId = MySQLQuery.getAsInteger(row[0]);
        unitId = MySQLQuery.getAsInteger(row[1]);
        callPriority = MySQLQuery.getAsString(row[2]);
        notify = MySQLQuery.getAsBoolean(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ess_person_unit";
    }

    public static String getSelFlds(String alias) {
        return new EssPersonUnit().getSelFldsForAlias(alias);
    }

    public static List<EssPersonUnit> getList(MySQLQuery q, Connection conn) throws Exception {
        return new EssPersonUnit().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new EssPersonUnit().deleteById(id, conn);
    }

    public static List<EssPersonUnit> getAll(Connection conn) throws Exception {
        return new EssPersonUnit().getAllList(conn);
    }

//fin zona de reemplazo
    public static EssPersonUnit getByUnit(int unitId, int personId, Connection conn) throws Exception {
        return new EssPersonUnit().select(new MySQLQuery("SELECT " + getSelFlds("") + " FROM ess_person_unit WHERE person_id = ?1 AND unit_id = ?2").setParam(1, personId).setParam(2, unitId), conn);
    }
}
