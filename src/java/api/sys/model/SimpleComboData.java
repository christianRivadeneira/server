package api.sys.model;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import metadata.log.Descriptor;
import utilities.MySQLQuery;

public class SimpleComboData {

    public List<SimpleComboRow> data = new ArrayList<>();

    public SimpleComboData() {

    }

    public SimpleComboData(Object[][] rows) {
        for (Object[] row : rows) {
            data.add(new SimpleComboRow(row));
        }
    }

    public static SimpleComboData getData(String tableName, String gridName, Connection conn) throws Exception {
        SimpleComboData rta = new SimpleComboData();
        Object[][] data = new MySQLQuery(Descriptor.getDescQuery(tableName, null, null, true)).getRecords(conn);
        for (Object[] row : data) {
            rta.data.add(new SimpleComboRow(row));
        }
        return rta;
    }
}
