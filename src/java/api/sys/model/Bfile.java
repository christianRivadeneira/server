package api.sys.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class Bfile extends BaseModel<Bfile> {
//inicio zona de reemplazo

    public String fileName;
    public String description;
    public String table;
    public int ownerId;
    public Integer ownerType;
    public Date created;
    public Date updated;
    public int createdBy;
    public int updatedBy;
    public String keywords;
    public Boolean shrunken;
    public int size;    

    @Override
    protected String[] getFlds() {
        return new String[]{
            "file_name",
            "description",
            "table",
            "owner_id",
            "owner_type",
            "created",
            "updated",
            "created_by",
            "updated_by",
            "keywords",
            "shrunken",
            "size"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, fileName);
        q.setParam(2, description);
        q.setParam(3, table);
        q.setParam(4, ownerId);
        q.setParam(5, ownerType);
        q.setParam(6, created);
        q.setParam(7, updated);
        q.setParam(8, createdBy);
        q.setParam(9, updatedBy);
        q.setParam(10, keywords);
        q.setParam(11, shrunken);
        q.setParam(12, size);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        fileName = MySQLQuery.getAsString(row[0]);
        description = MySQLQuery.getAsString(row[1]);
        table = MySQLQuery.getAsString(row[2]);
        ownerId = MySQLQuery.getAsInteger(row[3]);
        ownerType = MySQLQuery.getAsInteger(row[4]);
        created = MySQLQuery.getAsDate(row[5]);
        updated = MySQLQuery.getAsDate(row[6]);
        createdBy = MySQLQuery.getAsInteger(row[7]);
        updatedBy = MySQLQuery.getAsInteger(row[8]);
        keywords = MySQLQuery.getAsString(row[9]);
        shrunken = MySQLQuery.getAsBoolean(row[10]);
        size = MySQLQuery.getAsInteger(row[11]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bfile";
    }

    public static String getSelFlds(String alias) {
        return new Bfile().getSelFldsForAlias(alias);
    }

    public static List<Bfile> getList(MySQLQuery q, Connection conn) throws Exception {
        return new Bfile().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new Bfile().deleteById(id, conn);
    }

    public static List<Bfile> getAll(Connection conn) throws Exception {
        return new Bfile().getAllList(conn);
    }

//fin zona de reemplazo
    
}
