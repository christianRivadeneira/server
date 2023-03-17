package api.dto.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class DtoCylType extends BaseModel<DtoCylType> {

    public int alias;

//inicio zona de reemplazo
    public int cylinderTypeId;
    public String minasName;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "cylinder_type_id",
            "minas_name"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, cylinderTypeId);
        q.setParam(2, minasName);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        cylinderTypeId = MySQLQuery.getAsInteger(row[0]);
        minasName = MySQLQuery.getAsString(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "dto_cyl_type";
    }

    public static String getSelFlds(String alias) {
        return new DtoCylType().getSelFldsForAlias(alias);
    }

    public static List<DtoCylType> getList(MySQLQuery q, Connection conn) throws Exception {
        return new DtoCylType().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new DtoCylType().deleteById(id, conn);
    }

    public static List<DtoCylType> getAll(Connection conn) throws Exception {
        return new DtoCylType().getAllList(conn);
    }

//fin zona de reemplazo
    @Override
    public void afterSelect(Connection con) throws Exception {
        alias = Integer.valueOf(minasName.replaceAll("[^0-9]", ""));
    }

    public static DtoCylType findCylType(List<DtoCylType> types, Integer alias) {
        if (alias == null) {
            return null;
        }
        for (DtoCylType type : types) {
            if (type.alias == alias) {
                return type;
            }
        }
        return null;
    }

}
