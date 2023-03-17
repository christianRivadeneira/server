package api.est.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class EstMtoImport extends BaseModel<EstMtoImport> {
//inicio zona de reemplazo

    public int empId;
    public Date date;
    public String notes;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "emp_id",
            "date",
            "notes"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, empId);
        q.setParam(2, date);
        q.setParam(3, notes);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        empId = MySQLQuery.getAsInteger(row[0]);
        date = MySQLQuery.getAsDate(row[1]);
        notes = MySQLQuery.getAsString(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "est_mto_import";
    }

    public static String getSelFlds(String alias) {
        return new EstMtoImport().getSelFldsForAlias(alias);
    }

    public static List<EstMtoImport> getList(MySQLQuery q, Connection conn) throws Exception {
        return new EstMtoImport().getListFromQuery(q, conn);
    }

    public static List<EstMtoImport> getList(Params p, Connection conn) throws Exception {
        return new EstMtoImport().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new EstMtoImport().deleteById(id, conn);
    }

    public static List<EstMtoImport> getAll(Connection conn) throws Exception {
        return new EstMtoImport().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<EstMtoImport> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}