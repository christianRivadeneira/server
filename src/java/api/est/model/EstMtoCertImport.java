package api.est.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class EstMtoCertImport extends BaseModel<EstMtoCertImport> {
//inicio zona de reemplazo

    public int empId;
    public Date date;
    public String notes;
    public String ids;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "emp_id",
            "date",
            "notes",
            "ids"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, empId);
        q.setParam(2, date);
        q.setParam(3, notes);
        q.setParam(4, ids);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        empId = MySQLQuery.getAsInteger(row[0]);
        date = MySQLQuery.getAsDate(row[1]);
        notes = MySQLQuery.getAsString(row[2]);
        ids = MySQLQuery.getAsString(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "est_mto_cert_import";
    }

    public static String getSelFlds(String alias) {
        return new EstMtoCertImport().getSelFldsForAlias(alias);
    }

    public static List<EstMtoCertImport> getList(MySQLQuery q, Connection conn) throws Exception {
        return new EstMtoCertImport().getListFromQuery(q, conn);
    }

    public static List<EstMtoCertImport> getList(Params p, Connection conn) throws Exception {
        return new EstMtoCertImport().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new EstMtoCertImport().deleteById(id, conn);
    }

    public static List<EstMtoCertImport> getAll(Connection conn) throws Exception {
        return new EstMtoCertImport().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<EstMtoCertImport> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}
