package api.sys.model.form;

import java.util.ArrayList;
import java.util.List;

public class FormDto {

    public String name;
    public String tblName;
    public String plural;
    public String singular;
    public boolean male;
    public List<FormTabDto> tabs = new ArrayList<>();
    public String fromParentFldName;
    public String activeFldName;

    public String insertPath;
    public String updatePath;
    public String selectPath;

    public String customNewTitle;
    public String customEditTitle;
}
