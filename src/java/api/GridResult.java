package api;

public class GridResult {

    public Object[][] data;
    public MySQLCol[] cols;

    public String sortType;
    public Integer sortColIndex;

    public static final String SORT_ASC = "SORT_ASC";
    public static final String SORT_DESC = "SORT_DESC";
    public static final String SORT_NONE = "SORT_NONE";
    public static final String SORT_DEFAULT = "SORT_DEFAULT";

}
