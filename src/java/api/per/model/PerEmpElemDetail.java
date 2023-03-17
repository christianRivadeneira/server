package api.per.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class PerEmpElemDetail extends BaseModel<PerEmpElemDetail> {
//inicio zona de reemplazo

    public int perElemDetId;
    public int perEmpId;
    public String size;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "per_elem_det_id",
            "per_emp_id",
            "size"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, perElemDetId);
        q.setParam(2, perEmpId);
        q.setParam(3, size);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        perElemDetId = MySQLQuery.getAsInteger(row[0]);
        perEmpId = MySQLQuery.getAsInteger(row[1]);
        size = MySQLQuery.getAsString(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "per_emp_elem_detail";
    }

    public static String getSelFlds(String alias) {
        return new PerEmpElemDetail().getSelFldsForAlias(alias);
    }

    public static List<PerEmpElemDetail> getList(MySQLQuery q, Connection conn) throws Exception {
        return new PerEmpElemDetail().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new PerEmpElemDetail().deleteById(id, conn);
    }

    public static List<PerEmpElemDetail> getAll(Connection conn) throws Exception {
        return new PerEmpElemDetail().getAllList(conn);
    }

//fin zona de reemplazo
}