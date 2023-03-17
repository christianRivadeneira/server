package utilities.importer;

import java.math.BigDecimal;
import java.util.Date;

public class ImporterCol {

    public String name;
    public int type;
    public boolean isNullable;
    public int pos = -1;

    public static final int TYPE_TEXT = 1;
    public static final int TYPE_DATE = 2;
    public static final int TYPE_DD_MM_YYYY_HH_MM_A = 3;
    public static final int TYPE_HOUR = 4;
    public static final int TYPE_DECIMAL = 5;
    public static final int TYPE_INTEGER = 6;
    public static final int TYPE_BOOLEAN = 7;

    public ImporterCol(String name, int type, boolean isNullable) {
        this.name = name;
        this.type = type;
        this.isNullable = isNullable;
    }

    public static Class getTypeClass(int type) {
        switch (type) {
            case TYPE_TEXT:
                return String.class;
            case TYPE_DATE:
                return Date.class;
            case TYPE_DD_MM_YYYY_HH_MM_A:
                return Date.class;
            case TYPE_HOUR:
                return Date.class;
            case TYPE_DECIMAL:
                return BigDecimal.class;
            case TYPE_INTEGER:
                return Integer.class;
            case TYPE_BOOLEAN:
                return Boolean.class;
            default:
                throw new RuntimeException("Unrecognized type " + type);
        }
    }

    public static String getColTypeName(int type) {
        switch (type) {
            case TYPE_TEXT:
                return "Texto";
            case TYPE_DATE:
                return "Fecha";
            case TYPE_DD_MM_YYYY_HH_MM_A:
                return "Fecha y Hora";
            case TYPE_HOUR:
                return "Hora";
            case TYPE_DECIMAL:
                return "Numerico";
            case TYPE_INTEGER:
                return "Numerico";
            case TYPE_BOOLEAN:
                return "[si\no]";
            default:
                throw new RuntimeException("Unrecognized type " + type);
        }
    }
}
