package api.sys.model;

import utilities.MySQLQuery;

public class Token {

    public static final String KEY = "mV9z782rUZJQ7VZg";

    public int id;
    public String p;
    public String t;

    public String serialize() {
        return id + "@" + p + "@" + t;
    }

    public Token deserialize(String s) {
        String[] parts = s.split("@");
        id = MySQLQuery.getAsInteger(parts[0]);
        p = parts[1];
        t = parts[2].equals("null") ? null : parts[2];
        return this;
    }

    /**
     * La zona horaria en format +/-hhmm que corresponde a Z en un simple date
     * format
     *
     * @return
     */
    public String getTimeZoneZformat() {
        return getTimeZoneZformat(t);
    }
    
    /**
     * La zona horaria en format +/-hhmm que corresponde a Z en un simple date
     * format
     *
     * @param s zona horaria en formato GMT+/-hh:mm
     * @return
     */
    public static String getTimeZoneZformat(String s) {
        s = s.toLowerCase();
        if (s.startsWith("gmt") || s.startsWith("utc")) {
            s = s.substring(3);
        }
        boolean minus = s.startsWith("-");
        s = s.replaceAll("[+-]", "");
        String[] parts = s.split(":");
        return (minus ? "-" : "+") + String.format("%02d", Integer.parseInt(parts[0])) + String.format("%02d", Integer.parseInt(parts[1]));
    }
}
