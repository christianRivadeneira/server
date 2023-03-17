package api.chl.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class ChlRequestImport extends BaseModel<ChlRequestImport> {
//inicio zona de reemplazo

    public int empId;
    public Date date;
    public String notes;
    public Integer provId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "emp_id",
            "date",
            "notes",
            "prov_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, empId);
        q.setParam(2, date);
        q.setParam(3, notes);
        q.setParam(4, provId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        empId = MySQLQuery.getAsInteger(row[0]);
        date = MySQLQuery.getAsDate(row[1]);
        notes = MySQLQuery.getAsString(row[2]);
        provId = MySQLQuery.getAsInteger(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "chl_request_import";
    }

    public static String getSelFlds(String alias) {
        return new ChlRequestImport().getSelFldsForAlias(alias);
    }

    public static List<ChlRequestImport> getList(MySQLQuery q, Connection conn) throws Exception {
        return new ChlRequestImport().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new ChlRequestImport().deleteById(id, conn);
    }

    public static List<ChlRequestImport> getAll(Connection conn) throws Exception {
        return new ChlRequestImport().getAllList(conn);
    }

//fin zona de reemplazo
}
