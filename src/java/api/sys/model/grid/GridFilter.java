package api.sys.model.grid;

public class GridFilter {

    public static final String YEAR = "YEAR";
    public static final String MONTH = "MONTH";
    public static final String DAY = "DAY";
    public static final String CMB = "CMB";
    public static final String ENUM = "ENUM";
    public static final String BOOLEAN = "BOOLEAN";

    public String name;
    public String label;
    public String type;
    public GridFilterComboDto cmb;
    public GridFilterEnumDto enumOpts;
    public int slot;
}
