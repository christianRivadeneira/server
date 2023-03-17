package api.mss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class MssMinuteIncidentType extends BaseModel<MssMinuteIncidentType> {
//inicio zona de reemplazo

    public int typeId;
    public String name;
    public boolean active;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "type_id",
            "name",
            "active"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, typeId);
        q.setParam(2, name);
        q.setParam(3, active);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        typeId = MySQLQuery.getAsInteger(row[0]);
        name = MySQLQuery.getAsString(row[1]);
        active = MySQLQuery.getAsBoolean(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_minute_incident_type";
    }

    public static String getSelFlds(String alias) {
        return new MssMinuteIncidentType().getSelFldsForAlias(alias);
    }

    public static List<MssMinuteIncidentType> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssMinuteIncidentType().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssMinuteIncidentType().deleteById(id, conn);
    }

    public static List<MssMinuteIncidentType> getAll(Connection conn) throws Exception {
        return new MssMinuteIncidentType().getAllList(conn);
    }

//fin zona de reemplazo

    public List<MssMinuteIncidentType> getAllActive(Connection conn) throws Exception {
        return new MssMinuteIncidentType().getListFromParams(new Params("active", true), conn);
    }
}
