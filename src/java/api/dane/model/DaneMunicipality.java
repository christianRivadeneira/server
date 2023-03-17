package api.dane.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class DaneMunicipality extends BaseModel<DaneMunicipality> {
//inicio zona de reemplazo

    public String code;
    public String name;
    public int depId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "code",
            "name",
            "dep_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, code);
        q.setParam(2, name);
        q.setParam(3, depId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        code = MySQLQuery.getAsString(row[0]);
        name = MySQLQuery.getAsString(row[1]);
        depId = MySQLQuery.getAsInteger(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "dane_municipality";
    }

    public static String getSelFlds(String alias) {
        return new DaneMunicipality().getSelFldsForAlias(alias);
    }

    public static List<DaneMunicipality> getList(MySQLQuery q, Connection conn) throws Exception {
        return new DaneMunicipality().getListFromQuery(q, conn);
    }

    public static List<DaneMunicipality> getList(Params p, Connection conn) throws Exception {
        return new DaneMunicipality().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new DaneMunicipality().deleteById(id, conn);
    }

    public static List<DaneMunicipality> getAll(Connection conn) throws Exception {
        return new DaneMunicipality().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<DaneMunicipality> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}