package api.dto.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class DtoImportLog extends BaseModel<DtoImportLog> {
//inicio zona de reemplazo

    public int employeeId;
    public String fileName;
    public Date dtImport;
    public Integer fileRows;
    public String notes;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "employee_id",
            "file_name",
            "dt_import",
            "file_rows",
            "notes"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, employeeId);
        q.setParam(2, fileName);
        q.setParam(3, dtImport);
        q.setParam(4, fileRows);
        q.setParam(5, notes);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        employeeId = MySQLQuery.getAsInteger(row[0]);
        fileName = MySQLQuery.getAsString(row[1]);
        dtImport = MySQLQuery.getAsDate(row[2]);
        fileRows = MySQLQuery.getAsInteger(row[3]);
        notes = MySQLQuery.getAsString(row[4]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "dto_import_log";
    }

    public static String getSelFlds(String alias) {
        return new DtoImportLog().getSelFldsForAlias(alias);
    }

    public static List<DtoImportLog> getList(MySQLQuery q, Connection conn) throws Exception {
        return new DtoImportLog().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new DtoImportLog().deleteById(id, conn);
    }

    public static List<DtoImportLog> getAll(Connection conn) throws Exception {
        return new DtoImportLog().getAllList(conn);
    }

//fin zona de reemplazo
}