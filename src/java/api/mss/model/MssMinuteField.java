package api.mss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class MssMinuteField extends BaseModel<MssMinuteField> {
//inicio zona de reemplazo

    public int typeId;
    public String name;
    public boolean active;
    public int place;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "type_id",
            "name",
            "active",
            "place"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, typeId);
        q.setParam(2, name);
        q.setParam(3, active);
        q.setParam(4, place);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        typeId = MySQLQuery.getAsInteger(row[0]);
        name = MySQLQuery.getAsString(row[1]);
        active = MySQLQuery.getAsBoolean(row[2]);
        place = MySQLQuery.getAsInteger(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_minute_field";
    }

    public static String getSelFlds(String alias) {
        return new MssMinuteField().getSelFldsForAlias(alias);
    }

    public static List<MssMinuteField> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssMinuteField().getListFromQuery(q, conn);
    }

    public static List<MssMinuteField> getList(Params p, Connection conn) throws Exception {
        return new MssMinuteField().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssMinuteField().deleteById(id, conn);
    }

    public static List<MssMinuteField> getAll(Connection conn) throws Exception {
        return new MssMinuteField().getAllList(conn);
    }

//fin zona de reemplazo
    public static List<MssMinuteField> getAll(int typeId, Connection conn) throws Exception {
        Params p = new Params("type_id", typeId).param("active", true).sort("place");
        return new MssMinuteField().getListFromParams(p, conn);
    }
    
}
