package metadata.model;

import java.util.ArrayList;
import java.util.List;
import static metadata.model.Table.readJson;

public class Field {

    public static final String ENUM = "ENUM";
    public static final String TEXT = "TEXT";
    public static final String LONG_TEXT = "LONG_TEXT";
    public static final String BOOLEAN = "BOOLEAN";
    public static final String DATE = "DATE";
    public static final String DATE_TIME = "DATE_TIME";
    public static final String TIME = "TIME";

    public static final String INTEGER = "INTEGER";
    public static final String BIG_INTEGER = "BIG_INTEGER";

    public static final String BIG_DECIMAL = "BIG_DECIMAL";
    public static final String FLOAT = "FLOAT";
    public static final String DOUBLE = "DOUBLE";

    public static final String BYTE_ARR = "BYTE_ARR";

    public String id;
    public String tblName;

    public String name;
    public String label;

    //public String gender;
    public boolean fk;
    public boolean pk;
    public boolean nullable;

    public String type;
    public String format;
    public String fkTblName;
    public String[][] emunOpts;
    public List<Validation> validations = new ArrayList<>();

    public static Field getById(String id) throws Exception {
        if (Table.DEVEL_MODE) {
            Table tbl = (Table) readJson(id.split("-")[0], Table.class);
            if (tbl == null) {
                throw new NotFoundException("Table " + id.split("-")[0] + " not found.");
            }
            Field f = tbl.getFieldById(id);
            if (f == null) {
                throw new NotFoundException("Field " + id + " not found.");
            }
            return f;
        } else {
            return Table.TABLES_CACHE.get(id.split("-")[0]).getFieldById(id);
        }
    }
}
