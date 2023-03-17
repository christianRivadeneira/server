package api.mto.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class MtoTripNovelty extends BaseModel<MtoTripNovelty> {
//inicio zona de reemplazo

    public int empId;
    public int tripId;
    public int typeId;
    public Date regDt;
    public String notes;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "emp_id",
            "trip_id",
            "type_id",
            "reg_dt",
            "notes"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, empId);
        q.setParam(2, tripId);
        q.setParam(3, typeId);
        q.setParam(4, regDt);
        q.setParam(5, notes);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        empId = MySQLQuery.getAsInteger(row[0]);
        tripId = MySQLQuery.getAsInteger(row[1]);
        typeId = MySQLQuery.getAsInteger(row[2]);
        regDt = MySQLQuery.getAsDate(row[3]);
        notes = MySQLQuery.getAsString(row[4]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mto_trip_novelty";
    }

    public static String getSelFlds(String alias) {
        return new MtoTripNovelty().getSelFldsForAlias(alias);
    }

    public static List<MtoTripNovelty> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MtoTripNovelty().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MtoTripNovelty().deleteById(id, conn);
    }

    public static List<MtoTripNovelty> getAll(Connection conn) throws Exception {
        return new MtoTripNovelty().getAllList(conn);
    }
 
//fin zona de reemplazo
}