package api.mss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class MssShiftIncidentType extends BaseModel<MssShiftIncidentType> {
//inicio zona de reemplazo

    public String name;
    public boolean push;
    public boolean active;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "push",
            "active"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, push);
        q.setParam(3, active);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        push = MySQLQuery.getAsBoolean(row[1]);
        active = MySQLQuery.getAsBoolean(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_shift_incident_type";
    }

    public static String getSelFlds(String alias) {
        return new MssShiftIncidentType().getSelFldsForAlias(alias);
    }

    public static List<MssShiftIncidentType> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssShiftIncidentType().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssShiftIncidentType().deleteById(id, conn);
    }

    public static List<MssShiftIncidentType> getAll(Connection conn) throws Exception {
        return new MssShiftIncidentType().getAllList(conn);
    }

//fin zona de reemplazo
    
     public static List<MssShiftIncidentType> getAllActive(Connection conn) throws Exception {
        return new MssShiftIncidentType().getListFromParams(new Params("active",true),conn);
    }
}
