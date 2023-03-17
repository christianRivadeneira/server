package api.sys.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class SysFrmValue extends BaseModel<SysFrmValue> {
//inicio zona de reemplazo

    public int ownerId;
    public int fieldId;
    public String data;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "owner_id",
            "field_id",
            "data"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, ownerId);
        q.setParam(2, fieldId);
        q.setParam(3, data);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        ownerId = MySQLQuery.getAsInteger(row[0]);
        fieldId = MySQLQuery.getAsInteger(row[1]);
        data = MySQLQuery.getAsString(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "sys_frm_value";
    }

    public static String getSelFlds(String alias) {
        return new SysFrmValue().getSelFldsForAlias(alias);
    }

    public static List<SysFrmValue> getList(MySQLQuery q, Connection conn) throws Exception {
        return new SysFrmValue().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new SysFrmValue().deleteById(id, conn);
    }

    public static List<SysFrmValue> getAll(Connection conn) throws Exception {
        return new SysFrmValue().getAllList(conn);
    }

//fin zona de reemplazo
    public SysFrmValue getSysFrmValue(MySQLQuery q, Connection conn) throws Exception {
        return new SysFrmValue().select(q, conn);
    }
}
