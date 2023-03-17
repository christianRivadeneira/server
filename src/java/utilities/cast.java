package utilities;

import java.math.BigDecimal;
import java.util.Date;

public class cast {

    public static Integer asInt(Object obj) {
        return MySQLQuery.getAsInteger(obj);
    }

    public static Integer asInt(Object[] row, int i) {
        return MySQLQuery.getAsInteger(row[i]);
    }

    public static String asString(Object obj) {
        return MySQLQuery.getAsString(obj);
    }

    public static String asString(Object[] row, int i) {
        return MySQLQuery.getAsString(row[i]);
    }

    public static Boolean asBoolean(Object obj) {
        return MySQLQuery.getAsBoolean(obj);
    }

    public static Boolean asBoolean(Object[] row, int i) {
        return MySQLQuery.getAsBoolean(row[i]);
    }

    public static Date asDate(Object obj) {
        return MySQLQuery.getAsDate(obj);
    }

    public static Date asDate(Object[] row, int i) {
        return MySQLQuery.getAsDate(row[i]);
    }

    public static Double asDouble(Object obj) {
        return MySQLQuery.getAsDouble(obj);
    }

    public static Double asDouble(Object[] row, int i) {
        return MySQLQuery.getAsDouble(row[i]);
    }

     public static Long asLong(Object obj) {
        return MySQLQuery.getAsLong(obj);
    }

    public static Long asLong(Object[] row, int i) {
        return MySQLQuery.getAsLong(row[i]);
    }
    
    public static BigDecimal asBigDecimal(Object obj, boolean nullaszero) {
        return MySQLQuery.getAsBigDecimal(obj, nullaszero);
    }

    public static BigDecimal asBigDecimal(Object[] row, int i, boolean nullaszero) {
        return MySQLQuery.getAsBigDecimal(row[i], nullaszero);
    }

    public static BigDecimal asBigDecimal(Object obj) {
        return MySQLQuery.getAsBigDecimal(obj, false);
    }

    public static BigDecimal asBigDecimal(Object[] row, int i) {
        return MySQLQuery.getAsBigDecimal(row[i], false);
    }
}
