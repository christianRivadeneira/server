package api;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public class MySQLCol {

    public final int type;
    public int width;
    public String name;

    public boolean editable = false;
    public int align = LEFT;
    public boolean showZeros = false;
    public String[][] enumOpts;
    public boolean toString;

    public static final int TYPE_KEY = 0;
    public static final int TYPE_TEXT = 1;
    public static final int TYPE_DD_MM = 2;
    public static final int TYPE_DD_MM_YYYY = 3;
    public static final int TYPE_MMMM_YYYY = 4;
    public static final int TYPE_DD_MM_YYYY_HH12_MM_A = 5;
    public static final int TYPE_DD_MM_YYYY_HH12_MM_SS_A = 6;
    public static final int TYPE_HH12_MM_A = 7;
    public static final int TYPE_HH24_MM = 8;
    public static final int TYPE_HH12_MM_SS_A = 9;
    public static final int TYPE_DECIMAL_1 = 10;
    public static final int TYPE_DECIMAL_2 = 11;
    public static final int TYPE_DECIMAL_3 = 12;
    public static final int TYPE_DECIMAL_4 = 13;
    public static final int TYPE_INTEGER = 14;
    public static final int TYPE_INTEGER_NO_DOTS = 15;
    public static final int TYPE_BOOLEAN = 16;
    public static final int TYPE_ENUM = 17;
    public static final int TYPE_ICON = 18;
    public static final int TYPE_COLOR = 19;
    public static final int TYPE_COLOR_ICON = 20;

    public static final int CENTER = 0;
    public static final int LEFT = 2;
    public static final int RIGHT = 4;

    public static final Map<String, Integer> TYPES = new HashMap<>();

    static {
        TYPES.put("TYPE_KEY", TYPE_KEY);
        TYPES.put("TYPE_TEXT", TYPE_TEXT);
        TYPES.put("TYPE_DD_MM", TYPE_DD_MM);
        TYPES.put("TYPE_DD_MM_YYYY", TYPE_DD_MM_YYYY);
        TYPES.put("TYPE_MMMM_YYYY", TYPE_MMMM_YYYY);
        TYPES.put("TYPE_DD_MM_YYYY_HH12_MM_A", TYPE_DD_MM_YYYY_HH12_MM_A);
        TYPES.put("TYPE_DD_MM_YYYY_HH12_MM_SS_A", TYPE_DD_MM_YYYY_HH12_MM_SS_A);
        TYPES.put("TYPE_HH12_MM_A", TYPE_HH12_MM_A);
        TYPES.put("TYPE_HH24_MM", TYPE_HH24_MM);
        TYPES.put("TYPE_HH12_MM_SS_A", TYPE_HH12_MM_SS_A);
        TYPES.put("TYPE_DECIMAL_1", TYPE_DECIMAL_1);
        TYPES.put("TYPE_DECIMAL_2", TYPE_DECIMAL_2);
        TYPES.put("TYPE_DECIMAL_3", TYPE_DECIMAL_3);
        TYPES.put("TYPE_DECIMAL_4", TYPE_DECIMAL_4);
        TYPES.put("TYPE_INTEGER", TYPE_INTEGER);
        TYPES.put("TYPE_INTEGER_NO_DOTS", TYPE_INTEGER_NO_DOTS);
        TYPES.put("TYPE_BOOLEAN", TYPE_BOOLEAN);
        TYPES.put("TYPE_ENUM", TYPE_ENUM);
        TYPES.put("TYPE_ICON", TYPE_ICON);
        TYPES.put("TYPE_COLOR", TYPE_COLOR);
        TYPES.put("TYPE_COLOR_ICON", TYPE_COLOR_ICON);
    }

    /* public MySQLCol(int type, int width, String name, boolean editable, int align, String format, boolean showZeros, Map<String, String> enumOpts) {
        this.type = type;
        this.width = width;
        this.name = name;
        this.editable = editable;
        this.align = align;
        this.format = format;
        this.showZeros = showZeros;
        this.enumOpts = enumOpts;
        checkFormat();
    }*/
    public MySQLCol(int type, int width, String name, int align) {
        this.type = type;
        this.width = width;
        this.name = name;
        this.align = align;
    }

    /**
     *
     * @param type
     * @param width
     * @param name
     * @param showZeros para números, si se muestra el zero, para date time si
     * se muestran los segundos
     */
    public MySQLCol(int type, int width, String name, boolean showZeros) {
        this.type = type;
        this.width = width;
        this.name = name;
        this.showZeros = showZeros;

    }

    public MySQLCol(int type, int width, String name, String[][] enumOpts) {
        this.type = type;
        this.width = width;
        this.name = name;
        this.enumOpts = enumOpts;
    }

    public MySQLCol(int type, int width, String name) {
        this.type = type;
        this.width = width;
        this.name = name;
    }

    public MySQLCol(int type) {
        this.type = type;
        if (type != TYPE_KEY) {
            throw new RuntimeException("Solo el TYPE_KEY se puede crear sin ancho y nombre");
        }
    }

    public static boolean isInteger(int type) {
        return TYPE_INTEGER == type
                || TYPE_INTEGER_NO_DOTS == type;
    }

    public static boolean isDecimal(int type) {
        return TYPE_DECIMAL_1 == type
                || TYPE_DECIMAL_2 == type
                || TYPE_DECIMAL_3 == type
                || TYPE_DECIMAL_4 == type;
    }

    public static boolean isDate(int type) {
        return TYPE_DD_MM == type
                || TYPE_DD_MM_YYYY == type
                || TYPE_MMMM_YYYY == type
                || TYPE_DD_MM_YYYY_HH12_MM_A == type
                || TYPE_DD_MM_YYYY_HH12_MM_SS_A == type;
    }

    public static boolean isTime(int type) {
        return TYPE_HH12_MM_A == type
                || TYPE_HH24_MM == type
                || TYPE_HH12_MM_SS_A == type;
    }

    public static Class getTypeClass(int type) {
        if (isDate(type) || isTime(type)) {
            return Date.class;
        } else if (isInteger(type)) {
            return Integer.class;
        } else if (isDecimal(type)) {
            return BigDecimal.class;
        }

        switch (type) {
            case TYPE_TEXT:
                return String.class;
            case TYPE_BOOLEAN:
                return Boolean.class;
            case TYPE_ENUM:
                return Enum.class;
            case TYPE_ICON:
                return Integer.class;
            case TYPE_COLOR:
                return Integer.class;
            case TYPE_COLOR_ICON:
                return Integer.class;
            default:
                throw new RuntimeException("Unrecognized type " + type);
        }
    }

    public int getType() {
        return type;
    }

    public int getWidth() {
        return width;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MySQLCol)) {
            return false;
        }
        return MySQLCol.this.hashCode() == ((MySQLCol) obj).hashCode();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 31 * hash + this.type;
        hash = 31 * hash + this.width;
        hash = 31 * hash + Objects.hashCode(this.name);
        return hash;
    }

    public static final String DATES_FORMAT_LOCALE = "es_CO";
    public static final String NUM_FORMAT_LOCALE = "es_AR";

    public static String getDefaultSQLFormat(String colName, int type) {

        switch (type) {
            case TYPE_DD_MM:
                return "DATE_FORMAT(" + colName + ", '%d/%m')";
            case TYPE_DD_MM_YYYY:
                return "DATE_FORMAT(" + colName + ", '%d/%m/%Y')";
            case TYPE_MMMM_YYYY:
                return "DATE_FORMAT(" + colName + ", '%M/%Y')";
            case TYPE_DD_MM_YYYY_HH12_MM_A:
                return "DATE_FORMAT(" + colName + ", '%d/%m/%Y %h:%i %p')";
            case TYPE_DD_MM_YYYY_HH12_MM_SS_A:
                return "DATE_FORMAT(" + colName + ", '%d/%m/%Y %h:%i:%s %p')";
            case TYPE_HH12_MM_A:
                return "DATE_FORMAT(" + colName + ", '%h:%i %p')";
            case TYPE_HH24_MM:
                return "DATE_FORMAT(" + colName + ", '%H:%i')";
            case TYPE_HH12_MM_SS_A:
                return "DATE_FORMAT(" + colName + ", '%h:%i:%s %p')";
            case TYPE_DECIMAL_1:
                return "FORMAT(" + colName + ", 1, '" + NUM_FORMAT_LOCALE + "')";
            case TYPE_DECIMAL_2:
                return "FORMAT(" + colName + ", 2, '" + NUM_FORMAT_LOCALE + "')";
            case TYPE_DECIMAL_3:
                return "FORMAT(" + colName + ", 3, '" + NUM_FORMAT_LOCALE + "')";
            case TYPE_DECIMAL_4:
                return "FORMAT(" + colName + ", 4, '" + NUM_FORMAT_LOCALE + "')";
            case TYPE_INTEGER:
                return "FORMAT(" + colName + ", 0, '" + NUM_FORMAT_LOCALE + "')";
            case TYPE_INTEGER_NO_DOTS:
                return "FORMAT(" + colName + ", 0, '" + NUM_FORMAT_LOCALE + "')";//mysql no tiene esta opción
            default:
                throw new RuntimeException("Unrecognized: " + type);
        }
    }

    public static String getFormat(int type) {
        switch (type) {
            case TYPE_DD_MM:
                return "dd/MM";
            case TYPE_DD_MM_YYYY:
                return "dd/MM/yyyy";
            case TYPE_MMMM_YYYY:
                return "MMMM/yyyy";
            case TYPE_DD_MM_YYYY_HH12_MM_A:
                return "dd/MM/yyyy hh:mm a";
            case TYPE_DD_MM_YYYY_HH12_MM_SS_A:
                return "dd/MM/yyyy hh:mm:ss a";
            case TYPE_HH12_MM_A:
                return "hh:mm a";
            case TYPE_HH24_MM:
                return "HH:mm";
            case TYPE_HH12_MM_SS_A:
                return "hh:mm:ss a";
            case TYPE_DECIMAL_1:
                return "#,##0.0";
            case TYPE_DECIMAL_2:
                return "#,##0.00";
            case TYPE_DECIMAL_3:
                return "#,##0.000";
            case TYPE_DECIMAL_4:
                return "#,##0.0000";
            case TYPE_INTEGER:
                return "#,###";
            case TYPE_INTEGER_NO_DOTS:
                return "#";
            default:
                throw new RuntimeException();
        }
    }

    public static boolean hasFormat(int type) {
        return isDecimal(type) || isInteger(type) || isDate(type) || isTime(type);
    }

    public static int getConstFromStr(String type) {
        return TYPES.get(type);
    }

    public static String getStringFromConst(int type) {
        Iterator<Map.Entry<String, Integer>> it = TYPES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> next = it.next();
            if (next.getValue().equals(type)) {
                return next.getKey();
            }
        }
        return null;
    }
}
