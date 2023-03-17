package api.sys.model;

import java.sql.Connection;
import metadata.log.Descriptor;
import utilities.MySQLQuery;

public class SimpleComboRow {

    public Integer id;
    public String label;

    public SimpleComboRow() {

    }

    public SimpleComboRow(Object[] row) {
        id = MySQLQuery.getAsInteger(row[0]);
        label = MySQLQuery.getAsString(row[1]);
    }

    public static SimpleComboRow getById(String table, int id, Connection conn) throws Exception {
        SimpleComboRow rta = new SimpleComboRow();
        rta.id = id;
        rta.label = new MySQLQuery(Descriptor.getDescQuery(table, id, null, null)).getAsString(conn);
        return rta;
    }
}
