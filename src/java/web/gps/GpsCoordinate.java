package web.gps;

import api.BaseModel;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import utilities.MySQLQuery;

public class GpsCoordinate extends BaseModel<GpsCoordinate>{
//inicio zona de reemplazo

    public int id;
    public BigDecimal latitude;
    public BigDecimal longitude;
    public Integer accuracy;
    public int employeeId;
    public Date date;
    public String type;
    public Integer speed;
    public Integer charge;
    public Boolean mov;
    public Boolean plugged;
    public Integer appId;
    public Integer sessionId;
    public Integer interval;

    private static void setFields(GpsCoordinate obj, MySQLQuery q) {
        q.setParam(1, obj.latitude);
        q.setParam(2, obj.longitude);
        q.setParam(3, obj.accuracy);
        q.setParam(4, obj.employeeId);
        q.setParam(5, obj.date);
        q.setParam(6, obj.type);
        q.setParam(7, obj.speed);
        q.setParam(8, obj.charge);
        q.setParam(9, obj.mov);
        q.setParam(10, obj.plugged);
        q.setParam(11, obj.appId);
        q.setParam(12, obj.sessionId);
        q.setParam(13, obj.interval);

    }

    public static GpsCoordinate getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        GpsCoordinate obj = new GpsCoordinate();
        obj.latitude = MySQLQuery.getAsBigDecimal(row[0], false);
        obj.longitude = MySQLQuery.getAsBigDecimal(row[1], false);
        obj.accuracy = MySQLQuery.getAsInteger(row[2]);
        obj.employeeId = MySQLQuery.getAsInteger(row[3]);
        obj.date = MySQLQuery.getAsDate(row[4]);
        obj.type = MySQLQuery.getAsString(row[5]);
        obj.speed = MySQLQuery.getAsInteger(row[6]);
        obj.charge = MySQLQuery.getAsInteger(row[7]);
        obj.mov = MySQLQuery.getAsBoolean(row[8]);
        obj.plugged = MySQLQuery.getAsBoolean(row[9]);
        obj.appId = MySQLQuery.getAsInteger(row[10]);
        obj.sessionId = MySQLQuery.getAsInteger(row[11]);
        obj.interval = MySQLQuery.getAsInteger(row[12]);

        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
    }

//fin zona de reemplazo
    public String getInsertRow(GpsCoordinate pobj) throws Exception {
        GpsCoordinate obj = (GpsCoordinate) pobj;
        MySQLQuery q = new MySQLQuery("(?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8, ?9, ?10, ?11, ?12, ?13)");
        setFields(obj, q);
        return q.getParametrizedQuery();
    }

    public void delete(int id, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("DELETE FROM gps_coordinates WHERE id = " + id);
        q.executeDelete(ep);
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, latitude);
        q.setParam(2, longitude);
        q.setParam(3, accuracy);
        q.setParam(4, employeeId);
        q.setParam(5, date);
        q.setParam(6, type);
         q.setParam(7, speed);
        q.setParam(8, charge);
        q.setParam(9, mov);
        q.setParam(10, plugged);
        q.setParam(11, appId);
        q.setParam(12, sessionId);
        q.setParam(13, interval);
    }

    @Override
    protected void setRow(Object[] row) throws Exception {
        latitude = MySQLQuery.getAsBigDecimal(row[0],true);
        longitude = MySQLQuery.getAsBigDecimal(row[1],true);
        accuracy = MySQLQuery.getAsInteger(row[2]);
        employeeId = MySQLQuery.getAsInteger(row[3]);
        date = MySQLQuery.getAsDate(row[4]);
        type = MySQLQuery.getAsString(row[5]);
        speed = MySQLQuery.getAsInteger(row[6]);
        charge = MySQLQuery.getAsInteger(row[7]);
        mov = MySQLQuery.getAsBoolean(row[8]);
        plugged = MySQLQuery.getAsBoolean(row[9]);
        appId = MySQLQuery.getAsInteger(row[10]);
        sessionId = MySQLQuery.getAsInteger(row[11]);
        interval = MySQLQuery.getAsInteger(row[12]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String[] getFlds() {
        return new String[]{
            "latitude",
            "longitude",
            "accuracy",
            "employeeId",
            "date",
            "type",            
            "speed",
            "charge",
            "mov",
            "plugged",
            "appId",
            "sessionId",
            "interval",
            "id"
        };
    }

    @Override
    protected String getTblName() {
       return "gps_coordinate";
    }
}
