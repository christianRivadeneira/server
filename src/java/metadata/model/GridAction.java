package metadata.model;

import java.util.ArrayList;
import java.util.List;

public class GridAction {

    public boolean global;
    public String name;
    public String label;
    public String type;
    public String tableName;
    public String gridName;
    public String newFormName;
    public String editFormName;
    public String deskIcon;
    public String webIcon;
    public Integer enumRouteKeyPos;
    public String enumRouteFldId;
    public boolean additionalAction;

    public String apiEndPoint;
    public String apiResponse;
    public String apiConfirmDialog;
    public Boolean apiReload;
    public String apiMethod;
    public Integer apiRowIndex;

    //showStringResult;
    //downloadFile;
    //PreviewImage
    public List<GridAction> opts = new ArrayList<>();

    public static String ADD = "ADD";
    public static String EDIT = "EDIT";
    public static String REMOVE = "REMOVE";
    public static String SPACE = "SPACE";
    public static String EXPORT = "EXPORT";
    public static String LOGS = "LOGS";
    public static String GENERATED = "GENERATED";
    public static String ATTACHMENTS = "ATTACHMENTS";
    public static String MENU = "MENU";
    public static String API_CALL = "API_CALL";
    public static String CUSTOM = "CUSTOM";

    public GridAction() {

    }

    public GridAction(String type) {
        this.type = type;
    }
}
