package api.ess.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class EssEvent extends BaseModel<EssEvent> {

//Fuera de la zona de reemplazo
    public String typeName;
//---------------------------------------------------

//inicio zona de reemplazo

    public Date regDate;
    public Integer empId;
    public Integer eventTypeId;
    public Integer buildId;
    public Integer unitId;
    public Integer authById;
    public Integer personId;
    public Integer progId;
    public String notes;
    public boolean active;
    public Integer enterpriseId;
    public String type;
    public String pkgNum;
    public Integer pkgTypeId;
    public Integer dropLocationId;
    public String vhPlate;
    public Date importDt;
    public boolean isImport;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "reg_date",
            "emp_id",
            "event_type_id",
            "build_id",
            "unit_id",
            "auth_by_id",
            "person_id",
            "prog_id",
            "notes",
            "active",
            "enterprise_id",
            "type",
            "pkg_num",
            "pkg_type_id",
            "drop_location_id",
            "vh_plate",
            "import_dt",
            "is_import"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, regDate);
        q.setParam(2, empId);
        q.setParam(3, eventTypeId);
        q.setParam(4, buildId);
        q.setParam(5, unitId);
        q.setParam(6, authById);
        q.setParam(7, personId);
        q.setParam(8, progId);
        q.setParam(9, notes);
        q.setParam(10, active);
        q.setParam(11, enterpriseId);
        q.setParam(12, type);
        q.setParam(13, pkgNum);
        q.setParam(14, pkgTypeId);
        q.setParam(15, dropLocationId);
        q.setParam(16, vhPlate);
        q.setParam(17, importDt);
        q.setParam(18, isImport);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        regDate = MySQLQuery.getAsDate(row[0]);
        empId = MySQLQuery.getAsInteger(row[1]);
        eventTypeId = MySQLQuery.getAsInteger(row[2]);
        buildId = MySQLQuery.getAsInteger(row[3]);
        unitId = MySQLQuery.getAsInteger(row[4]);
        authById = MySQLQuery.getAsInteger(row[5]);
        personId = MySQLQuery.getAsInteger(row[6]);
        progId = MySQLQuery.getAsInteger(row[7]);
        notes = MySQLQuery.getAsString(row[8]);
        active = MySQLQuery.getAsBoolean(row[9]);
        enterpriseId = MySQLQuery.getAsInteger(row[10]);
        type = MySQLQuery.getAsString(row[11]);
        pkgNum = MySQLQuery.getAsString(row[12]);
        pkgTypeId = MySQLQuery.getAsInteger(row[13]);
        dropLocationId = MySQLQuery.getAsInteger(row[14]);
        vhPlate = MySQLQuery.getAsString(row[15]);
        importDt = MySQLQuery.getAsDate(row[16]);
        isImport = MySQLQuery.getAsBoolean(row[17]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ess_event";
    }

    public static String getSelFlds(String alias) {
        return new EssEvent().getSelFldsForAlias(alias);
    }

    public static List<EssEvent> getList(MySQLQuery q, Connection conn) throws Exception {
        return new EssEvent().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new EssEvent().deleteById(id, conn);
    }

    public static List<EssEvent> getAll(Connection conn) throws Exception {
        return new EssEvent().getAllList(conn);
    }

//fin zona de reemplazo
}
