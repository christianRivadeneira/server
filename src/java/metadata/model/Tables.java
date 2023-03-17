package metadata.model;

import java.util.List;
import static metadata.model.Table.readJson;

public class Tables {

    public List<String> tables;

    public static Tables read() throws Exception {
        return (Tables) readJson("tables", Tables.class);
    }
}
