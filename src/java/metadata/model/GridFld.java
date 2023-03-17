package metadata.model;

public class GridFld {

    public static final String AVG = "AVG";
    public static final String COUNT = "COUNT";
    public static final String GROUP_CONCAT = "GROUP_CONCAT";
    public static final String MAX = "MAX";
    public static final String MIN = "MIN";
    public static final String SUM = "SUM";

    public static final String LEFT = "LEFT";
    public static final String RIGHT = "RIGHT";
    public static final String CENTER = "CENTER";

    public String fldId;
    public String label;
    public String format;
    public Integer width;
    public String oper;
    public String align;
    public boolean showZero;
    public boolean editable;
    public boolean isKey;
    public boolean toString;

}
