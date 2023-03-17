package metadata.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import metadata.model.Field;
import metadata.model.Form;
import metadata.model.FormField;
import metadata.model.FormTab;
import metadata.model.Table;
import org.apache.commons.io.IOUtils;

public class changeIds {

    public static void main(String[] args) throws Exception {
        Map<String, String> map = new HashMap<>();
        Table[] tbls = Table.getAll();
        for (int i = 0; i < tbls.length; i++) {
            Table tbl = tbls[i];
            System.out.println(tbl.name);
            List<Field> flds = tbl.fields;
            for (int j = 0; j < flds.size(); j++) {
                Field f = flds.get(j);
                String nId = tbl.name + "-" + f.name;
                map.put(f.id, nId);
                f.id = nId;
            }
        }

        for (int i = 0; i < tbls.length; i++) {
            Table tbl = tbls[i];
            File f = new File("C:\\Projects\\git\\sigma\\server\\src\\java\\metadata\\json\\" + tbl.name + ".json");

            String str = IOUtils.toString(new FileInputStream(f), "utf-8");

            Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> e = it.next();
                str = str.replaceAll(e.getKey(), e.getValue());
            }

            IOUtils.write(str, new FileOutputStream(f));
        }

    }
}
