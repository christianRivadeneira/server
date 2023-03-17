package api.sys.api;

import api.sys.model.grid.GridActionDto;
import api.sys.model.grid.GridFilter;
import java.util.List;

public class GridDto {

    public List<GridActionDto> actions;
    public List<GridFilter> filters;

    public Integer parentIdSlot = null;
    public Integer textFilterSlot = null;

    public String plural;
    public String singular;

    public String dataPath;
    public String deletePath;
    
    public String customTitle;

}
