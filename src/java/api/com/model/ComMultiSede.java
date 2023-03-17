package api.com.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class ComMultiSede extends BaseModel<ComMultiSede> {
//inicio zona de reemplazo

    public int indexId;
    public String document;
    public String firstName;
    public String lastName;
    public String address;
    public String phone;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "index_id",
            "document",
            "first_name",
            "last_name",
            "address",
            "phone"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, indexId);
        q.setParam(2, document);
        q.setParam(3, firstName);
        q.setParam(4, lastName);
        q.setParam(5, address);
        q.setParam(6, phone);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        indexId = MySQLQuery.getAsInteger(row[0]);
        document = MySQLQuery.getAsString(row[1]);
        firstName = MySQLQuery.getAsString(row[2]);
        lastName = MySQLQuery.getAsString(row[3]);
        address = MySQLQuery.getAsString(row[4]);
        phone = MySQLQuery.getAsString(row[5]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "com_multi_sede";
    }

    public static String getSelFlds(String alias) {
        return new ComMultiSede().getSelFldsForAlias(alias);
    }

    public static List<ComMultiSede> getList(MySQLQuery q, Connection conn) throws Exception {
        return new ComMultiSede().getListFromQuery(q, conn);
    }

    public static List<ComMultiSede> getList(Params p, Connection conn) throws Exception {
        return new ComMultiSede().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new ComMultiSede().deleteById(id, conn);
    }

    public static List<ComMultiSede> getAll(Connection conn) throws Exception {
        return new ComMultiSede().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<ComInstSede> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}