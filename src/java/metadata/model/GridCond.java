package metadata.model;

public class GridCond {

    public static final String INT = "INT";
    public static final String BOOL = "BOOL";
    public static final String DATE = "DATE";
    public static final String STRING = "STRING";

    public static final String LIKE = "LIKE";
    public static final String EQUALS = "EQUALS";
    public static final String NOT_EQUALS = "NOT_EQUALS";

    public static final String IS_NULL = "IS_NULL";
    public static final String IS_NOT_NULL = "IS_NOT_NULL";
    public static final String IS_TRUE = "IS_TRUE";
    public static final String IS_FALSE = "IS_FALSE";

    public static final String ON_DAY = "ON_DAY";
    public static final String ON_MONTH = "ON_MONTH";
    public static final String ON_YEAR = "ON_YEAR";
    public static final String PERMISSION = "PERMISSION";
    public static final String FIXED = "FIXED";

    public String fldId;
    public String comparison;
    public String slotType;
    public Integer slot;
    public Boolean readFromParentId;
    public Boolean readFromTextFilter;
    public Boolean readFromFilter;
    public String cmbGridName;
    public String fixedEnum;
    public Integer fixedInt;

    public String permissionChecker;

    public static String getSlotType(Field fld) {
        switch (fld.type) {
            case Field.ENUM:
            case Field.TEXT:
            case Field.LONG_TEXT:
                return GridCond.STRING;
            case Field.BOOLEAN:
                return GridCond.BOOL;
            case Field.DATE:
            case Field.DATE_TIME:
            case Field.TIME:
                return GridCond.DATE;
            case Field.INTEGER:
            case Field.BIG_INTEGER:
                return GridCond.INT;
            case Field.BIG_DECIMAL:
            case Field.FLOAT:
            case Field.DOUBLE:
            case Field.BYTE_ARR:
                return null;
            default:
                throw new RuntimeException("Unsupported: " + fld.type);
        }
    }

}
