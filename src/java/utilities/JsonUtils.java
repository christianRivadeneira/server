package utilities;

import java.math.BigDecimal;
import java.util.Date;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class JsonUtils {

    public static Long getLong(JsonObject obj, String fieldName) {
        if (obj.isNull(fieldName)) {
            return null;
        }
        return obj.getJsonNumber(fieldName).longValue();
    }

    public static BigDecimal getBigdecimal(JsonObject obj, String fieldName) {
        if (obj.isNull(fieldName)) {
            return null;
        }
        return obj.getJsonNumber(fieldName).bigDecimalValue();
    }

    public static String getString(JsonObject obj, String fieldName) {
        if (obj.isNull(fieldName)) {
            return null;
        }
        return obj.getJsonString(fieldName).getString();
    }

    public static Integer getInt(JsonObject obj, String fieldName) {
        if (obj.isNull(fieldName)) {
            return null;
        }
        return obj.getJsonNumber(fieldName).intValue();
    }

    public static JsonArray getJsonArray(JsonObject obj, String fieldName) {
        if (obj.isNull(fieldName)) {
            return null;
        }

        return obj.getJsonArray(fieldName);
    }

    public static String getBool(JsonObject o, String n, boolean nullAsFalse) {
        if (o.containsKey(n)) {
            return o.getBoolean(n) ? "1" : "0";
        } else {
            return nullAsFalse ? "0" : "NULL";
        }
    }

    public static Boolean getBoolean(JsonObject obj, String fieldName, boolean nullAsFalse) {
        if (obj.isNull(fieldName)) {
            return nullAsFalse ? false : null;
        }
        return obj.getBoolean(fieldName);
    }

    public static void addLong(JsonObjectBuilder ob, String field, Object val) {
        if (val != null) {
            ob.add(field, MySQLQuery.getAsLong(val));
        } else {
            ob.addNull(field);
        }
    }

    public static void addDate(JsonObjectBuilder ob, String field, Object val) {
        if (val != null) {
            ob.add(field, MySQLQuery.getAsDate(val).getTime());
        } else {
            ob.addNull(field);
        }
    }

    public static void addInt(JsonObjectBuilder ob, String field, Object val) {
        if (val != null) {
            ob.add(field, MySQLQuery.getAsInteger(val));
        } else {
            ob.addNull(field);
        }
    }

    public static void addString(JsonObjectBuilder ob, String field, Object val) {
        if (val != null) {
            ob.add(field, MySQLQuery.getAsString(val));
        } else {
            ob.addNull(field);
        }
    }

    public static Date getDate(JsonObject obj, String fieldName) {
        if (!obj.containsKey(fieldName)) {
            return null;
        }
        if (obj.isNull(fieldName)) {
            return null;
        }
        return new Date(obj.getJsonNumber(fieldName).longValue());
    }

    public static void addBigDecimal(JsonObjectBuilder ob, String field, Object val, boolean nullAsZero) {
        if (val != null) {
            ob.add(field, MySQLQuery.getAsBigDecimal(val, nullAsZero));
        } else {
            ob.addNull(field);
        }
    }
}
