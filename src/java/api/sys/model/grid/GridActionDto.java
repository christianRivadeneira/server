package api.sys.model.grid;

import java.util.ArrayList;
import java.util.List;

public class GridActionDto {

    public boolean global;
    public String name;
    public String label;
    public String type;
    public String tableName;
    public String gridName;
    public String newFormName;
    public String editFormName;
    //public String gridGroupName;
    public String deskIcon;
    public String webIcon;
    public Integer enumRouteKeyPos;
    public boolean additionalAction;

    public String apiEndPoint;
    public String apiResponse;
    public String apiConfirmDialog;
    public Boolean apiReload;
    public String apiMethod;
    public Integer apiRowIndex;

    public List<GridActionDto> opts = new ArrayList<>();
}
