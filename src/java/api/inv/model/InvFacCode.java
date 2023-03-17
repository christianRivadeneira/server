package api.inv.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class InvFacCode extends BaseModel<InvFacCode> {
//inicio zona de reemplazo

    public Integer code;
    public int invFactoryId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "code",
            "inv_factory_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, code);
        q.setParam(2, invFactoryId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        code = MySQLQuery.getAsInteger(row[0]);
        invFactoryId = MySQLQuery.getAsInteger(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "inv_fac_code";
    }

    public static String getSelFlds(String alias) {
        return new InvFacCode().getSelFldsForAlias(alias);
    }

    public static List<InvFacCode> getList(MySQLQuery q, Connection conn) throws Exception {
        return new InvFacCode().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new InvFacCode().deleteById(id, conn);
    }

    public static List<InvFacCode> getAll(Connection conn) throws Exception {
        return new InvFacCode().getAllList(conn);
    }

//fin zona de reemplazo

}