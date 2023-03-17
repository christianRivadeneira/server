package api.per.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class PerEmpDoc extends BaseModel<PerEmpDoc> {
//inicio zona de reemplazo

    public String state;
    public String notes;
    public int docTypeId;
    public int empId;
    public String cmp1;
    public String cmp2;
    public String cmp3;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "state",
            "notes",
            "doc_type_id",
            "emp_id",
            "cmp_1",
            "cmp_2",
            "cmp_3"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, state);
        q.setParam(2, notes);
        q.setParam(3, docTypeId);
        q.setParam(4, empId);
        q.setParam(5, cmp1);
        q.setParam(6, cmp2);
        q.setParam(7, cmp3);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        state = MySQLQuery.getAsString(row[0]);
        notes = MySQLQuery.getAsString(row[1]);
        docTypeId = MySQLQuery.getAsInteger(row[2]);
        empId = MySQLQuery.getAsInteger(row[3]);
        cmp1 = MySQLQuery.getAsString(row[4]);
        cmp2 = MySQLQuery.getAsString(row[5]);
        cmp3 = MySQLQuery.getAsString(row[6]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "per_emp_doc";
    }

    public static String getSelFlds(String alias) {
        return new PerEmpDoc().getSelFldsForAlias(alias);
    }

    public static List<PerEmpDoc> getList(MySQLQuery q, Connection conn) throws Exception {
        return new PerEmpDoc().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new PerEmpDoc().deleteById(id, conn);
    }

    public static List<PerEmpDoc> getAll(Connection conn) throws Exception {
        return new PerEmpDoc().getAllList(conn);
    }

//fin zona de reemplazo
}
