package metadata.model;

import java.util.List;

public class GridTable {

    public static final String INNER = "INNER";
    public static final String LEFT = "LEFT";

    public String tblName;//no redunda porque no la tabla de fldId, sino la tabla a donde punta el fk
    public String fldId;
    public List<GridTableCond> conds;
    public String type;

}
