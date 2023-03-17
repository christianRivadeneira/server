package api.bill.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class BillServiceType extends BaseModel<BillServiceType> {
//inicio zona de reemplazo

    public String name;
    public String accCode;
    public String type;
    public String inteType;
    public String transType;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "acc_code",
            "type",
            "inte_type",
            "trans_type"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, accCode);
        q.setParam(3, type);
        q.setParam(4, inteType);
        q.setParam(5, transType);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        accCode = MySQLQuery.getAsString(row[1]);
        type = MySQLQuery.getAsString(row[2]);
        inteType = MySQLQuery.getAsString(row[3]);
        transType = MySQLQuery.getAsString(row[4]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_service_type";
    }

    public static String getSelFlds(String alias) {
        return new BillServiceType().getSelFldsForAlias(alias);
    }

    public static List<BillServiceType> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillServiceType().getListFromQuery(q, conn);
    }

    public static List<BillServiceType> getList(Params p, Connection conn) throws Exception {
        return new BillServiceType().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillServiceType().deleteById(id, conn);
    }

    public static List<BillServiceType> getAll(Connection conn) throws Exception {
        return new BillServiceType().getAllList(conn);
    }

//fin zona de reemplazo
    public static List<BillServiceType> getByType(String type, Connection conn) throws Exception {
        return new BillServiceType().getListFromParams(new Params("type", type).sort("name"), conn);
    }

}
