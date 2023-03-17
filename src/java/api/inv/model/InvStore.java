package api.inv.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.math.BigDecimal;
import java.util.Date;

public class InvStore extends BaseModel<InvStore> {
//inicio zona de reemplazo

    public String firstName;
    public String lastName;
    public int centerId;
    public String internal;
    public Date updateDate;
    public Date creationDate;
    public String document;
    public String contact;
    public String address;
    public String phones;
    public String notes;
    public int cityId;
    public boolean active;
    public boolean sower;
    public String state;
    public boolean checked;
    public int typeId;
    public Date closedDate;
    public int pendDocs;
    public BigDecimal lat;
    public BigDecimal lon;
    public Integer neighId;
    public String estName;
    public Integer coordId;
    public Integer sowerId;
    public Integer pathId;
    public Integer frecId;
    public Boolean microAlly;
    public Date verified;
    public Date movCenterDate;
    public Integer stateId;
    public Date lastPollDate;
    public Integer employeeId;
    public String mail;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "first_name",
            "last_name",
            "center_id",
            "internal",
            "update_date",
            "creation_date",
            "document",
            "contact",
            "address",
            "phones",
            "notes",
            "city_id",
            "active",
            "sower",
            "state",
            "checked",
            "type_id",
            "closed_date",
            "pend_docs",
            "lat",
            "lon",
            "neigh_id",
            "est_name",
            "coord_id",
            "sower_id",
            "path_id",
            "frec_id",
            "micro_ally",
            "verified",
            "mov_center_date",
            "state_id",
            "last_poll_date",
            "employee_id",
            "mail"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, firstName);
        q.setParam(2, lastName);
        q.setParam(3, centerId);
        q.setParam(4, internal);
        q.setParam(5, updateDate);
        q.setParam(6, creationDate);
        q.setParam(7, document);
        q.setParam(8, contact);
        q.setParam(9, address);
        q.setParam(10, phones);
        q.setParam(11, notes);
        q.setParam(12, cityId);
        q.setParam(13, active);
        q.setParam(14, sower);
        q.setParam(15, state);
        q.setParam(16, checked);
        q.setParam(17, typeId);
        q.setParam(18, closedDate);
        q.setParam(19, pendDocs);
        q.setParam(20, lat);
        q.setParam(21, lon);
        q.setParam(22, neighId);
        q.setParam(23, estName);
        q.setParam(24, coordId);
        q.setParam(25, sowerId);
        q.setParam(26, pathId);
        q.setParam(27, frecId);
        q.setParam(28, microAlly);
        q.setParam(29, verified);
        q.setParam(30, movCenterDate);
        q.setParam(31, stateId);
        q.setParam(32, lastPollDate);
        q.setParam(33, employeeId);
        q.setParam(34, mail);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        firstName = MySQLQuery.getAsString(row[0]);
        lastName = MySQLQuery.getAsString(row[1]);
        centerId = MySQLQuery.getAsInteger(row[2]);
        internal = MySQLQuery.getAsString(row[3]);
        updateDate = MySQLQuery.getAsDate(row[4]);
        creationDate = MySQLQuery.getAsDate(row[5]);
        document = MySQLQuery.getAsString(row[6]);
        contact = MySQLQuery.getAsString(row[7]);
        address = MySQLQuery.getAsString(row[8]);
        phones = MySQLQuery.getAsString(row[9]);
        notes = MySQLQuery.getAsString(row[10]);
        cityId = MySQLQuery.getAsInteger(row[11]);
        active = MySQLQuery.getAsBoolean(row[12]);
        sower = MySQLQuery.getAsBoolean(row[13]);
        state = MySQLQuery.getAsString(row[14]);
        checked = MySQLQuery.getAsBoolean(row[15]);
        typeId = MySQLQuery.getAsInteger(row[16]);
        closedDate = MySQLQuery.getAsDate(row[17]);
        pendDocs = MySQLQuery.getAsInteger(row[18]);
        lat = MySQLQuery.getAsBigDecimal(row[19], false);
        lon = MySQLQuery.getAsBigDecimal(row[20], false);
        neighId = MySQLQuery.getAsInteger(row[21]);
        estName = MySQLQuery.getAsString(row[22]);
        coordId = MySQLQuery.getAsInteger(row[23]);
        sowerId = MySQLQuery.getAsInteger(row[24]);
        pathId = MySQLQuery.getAsInteger(row[25]);
        frecId = MySQLQuery.getAsInteger(row[26]);
        microAlly = MySQLQuery.getAsBoolean(row[27]);
        verified = MySQLQuery.getAsDate(row[28]);
        movCenterDate = MySQLQuery.getAsDate(row[29]);
        stateId = MySQLQuery.getAsInteger(row[30]);
        lastPollDate = MySQLQuery.getAsDate(row[31]);
        employeeId = MySQLQuery.getAsInteger(row[32]);
        mail = MySQLQuery.getAsString(row[33]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "inv_store";
    }

    public static String getSelFlds(String alias) {
        return new InvStore().getSelFldsForAlias(alias);
    }

    public static List<InvStore> getList(MySQLQuery q, Connection conn) throws Exception {
        return new InvStore().getListFromQuery(q, conn);
    }

    public static List<InvStore> getList(Params p, Connection conn) throws Exception {
        return new InvStore().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new InvStore().deleteById(id, conn);
    }

    public static List<InvStore> getAll(Connection conn) throws Exception {
        return new InvStore().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<InvStore> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}