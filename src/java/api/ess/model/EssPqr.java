package api.ess.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class EssPqr extends BaseModel<EssPqr> {
//inicio zona de reemplazo

    public Integer buildId;
    public int typeId;
    public int regByEmpId;
    public int reqById;
    public Date begDate;
    public Date endDate;
    public String notes;
    public String endNotes;
    public Integer unitId;
    public String type;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "build_id",
            "type_id",
            "reg_by_emp_id",
            "req_by_id",
            "beg_date",
            "end_date",
            "notes",
            "end_notes",
            "unit_id",
            "type"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, buildId);
        q.setParam(2, typeId);
        q.setParam(3, regByEmpId);
        q.setParam(4, reqById);
        q.setParam(5, begDate);
        q.setParam(6, endDate);
        q.setParam(7, notes);
        q.setParam(8, endNotes);
        q.setParam(9, unitId);
        q.setParam(10, type);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        buildId = MySQLQuery.getAsInteger(row[0]);
        typeId = MySQLQuery.getAsInteger(row[1]);
        regByEmpId = MySQLQuery.getAsInteger(row[2]);
        reqById = MySQLQuery.getAsInteger(row[3]);
        begDate = MySQLQuery.getAsDate(row[4]);
        endDate = MySQLQuery.getAsDate(row[5]);
        notes = MySQLQuery.getAsString(row[6]);
        endNotes = MySQLQuery.getAsString(row[7]);
        unitId = MySQLQuery.getAsInteger(row[8]);
        type = MySQLQuery.getAsString(row[9]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ess_pqr";
    }

    public static String getSelFlds(String alias) {
        return new EssPqr().getSelFldsForAlias(alias);
    }

    public static List<EssPqr> getList(MySQLQuery q, Connection conn) throws Exception {
        return new EssPqr().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new EssPqr().deleteById(id, conn);
    }

    public static List<EssPqr> getAll(Connection conn) throws Exception {
        return new EssPqr().getAllList(conn);
    }

//fin zona de reemplazo
}