package api.sys.dto;

import java.util.List;

public class Option {

    public Integer id;
    public String name;
    public String webPath;
    public String webIcon;
    public String type;
    public int place;
    public List<Option> options;
    public String crudTblName;
    public String crudGridName;
    public boolean showBi;
}
