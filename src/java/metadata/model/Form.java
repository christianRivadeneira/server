package metadata.model;

import java.util.ArrayList;
import java.util.List;
import metadata.log.Diff;

public class Form {

    public String name;
    public String tblName;
    public String insertPath;
    public String updatePath;
    public String selectPath;
    public String customNewTitle;
    public String customEditTitle;

    public List<FormTab> tabs = new ArrayList<>();

    public Table table; //solo se usa para enviar el Front

    public FormTab getTabByName(String name) {
        String dbName = Diff.toDbName(name);
        for (int i = 0; i < tabs.size(); i++) {
            FormTab f = tabs.get(i);
            if (f.name.equals(dbName)) {
                return f;
            }
        }
        return null;
    }

    public boolean contains(String fieldId) {
        for (int i = 0; i < tabs.size(); i++) {
            FormTab t = tabs.get(i);
            for (int j = 0; j < t.flds.size(); j++) {
                FormField ff = t.flds.get(j);
                if (ff.fldId.equals(fieldId)) {
                    return true;
                }
            }
        }
        return false;
    }
}
