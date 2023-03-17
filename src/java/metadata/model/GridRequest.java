package metadata.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GridRequest {

    public String tableName;
    public String gridName;
    public Integer profileId;

    public List<Integer> ints = new ArrayList<>();
    public List<Date> dates = new ArrayList<>();
    public List<Boolean> bools = new ArrayList<>();
    public List<String> strings = new ArrayList<>();
    
}
