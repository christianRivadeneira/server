package api.mto.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class MtoContractor extends BaseModel<MtoContractor> {
//inicio zona de reemplazo

    public String name;
    public String firstName;
    public String lastName;
    public String document;
    public boolean active;
    public Integer perEmployeeId;
    public String notes;
    public Integer empId;
    public String phones;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "first_name",
            "last_name",
            "document",
            "active",
            "per_employee_id",
            "notes",
            "emp_id",
            "phones"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, firstName);
        q.setParam(3, lastName);
        q.setParam(4, document);
        q.setParam(5, active);
        q.setParam(6, perEmployeeId);
        q.setParam(7, notes);
        q.setParam(8, empId);
        q.setParam(9, phones);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        firstName = MySQLQuery.getAsString(row[1]);
        lastName = MySQLQuery.getAsString(row[2]);
        document = MySQLQuery.getAsString(row[3]);
        active = MySQLQuery.getAsBoolean(row[4]);
        perEmployeeId = MySQLQuery.getAsInteger(row[5]);
        notes = MySQLQuery.getAsString(row[6]);
        empId = MySQLQuery.getAsInteger(row[7]);
        phones = MySQLQuery.getAsString(row[8]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mto_contractor";
    }

    public static String getSelFlds(String alias) {
        return new MtoContractor().getSelFldsForAlias(alias);
    }

    public static List<MtoContractor> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MtoContractor().getListFromQuery(q, conn);
    }

    public static List<MtoContractor> getList(Params p, Connection conn) throws Exception {
        return new MtoContractor().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MtoContractor().deleteById(id, conn);
    }

    public static List<MtoContractor> getAll(Connection conn) throws Exception {
        return new MtoContractor().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<MtoContractor> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}