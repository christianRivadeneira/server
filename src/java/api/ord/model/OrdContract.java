package api.ord.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class OrdContract extends BaseModel<OrdContract> {
//inicio zona de reemplazo

    public String address;
    public String phones;
    public Boolean own;
    public Integer establishId;
    public Integer energyId;
    public Integer neighId;
    public int people;
    public Integer clientId;
    public String notes;
    public String state;
    public Date closedPendingDate;
    public String closedPendingNotes;
    public Integer cityId;
    public Integer creatorId;
    public Date createdDate;
    public String createdFrom;
    public String document;
    public String firstName;
    public String lastName;
    public String estName;
    public String cliType;
    public String email;
    public boolean pref;
    public boolean banned;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "address",
            "phones",
            "own",
            "establish_id",
            "energy_id",
            "neigh_id",
            "people",
            "client_id",
            "notes",
            "state",
            "closed_pending_date",
            "closed_pending_notes",
            "city_id",
            "creator_id",
            "created_date",
            "created_from",
            "document",
            "first_name",
            "last_name",
            "est_name",
            "cli_type",
            "email",
            "pref",
            "banned"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, address);
        q.setParam(2, phones);
        q.setParam(3, own);
        q.setParam(4, establishId);
        q.setParam(5, energyId);
        q.setParam(6, neighId);
        q.setParam(7, people);
        q.setParam(8, clientId);
        q.setParam(9, notes);
        q.setParam(10, state);
        q.setParam(11, closedPendingDate);
        q.setParam(12, closedPendingNotes);
        q.setParam(13, cityId);
        q.setParam(14, creatorId);
        q.setParam(15, createdDate);
        q.setParam(16, createdFrom);
        q.setParam(17, document);
        q.setParam(18, firstName);
        q.setParam(19, lastName);
        q.setParam(20, estName);
        q.setParam(21, cliType);
        q.setParam(22, email);
        q.setParam(23, pref);
        q.setParam(24, banned);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        address = MySQLQuery.getAsString(row[0]);
        phones = MySQLQuery.getAsString(row[1]);
        own = MySQLQuery.getAsBoolean(row[2]);
        establishId = MySQLQuery.getAsInteger(row[3]);
        energyId = MySQLQuery.getAsInteger(row[4]);
        neighId = MySQLQuery.getAsInteger(row[5]);
        people = MySQLQuery.getAsInteger(row[6]);
        clientId = MySQLQuery.getAsInteger(row[7]);
        notes = MySQLQuery.getAsString(row[8]);
        state = MySQLQuery.getAsString(row[9]);
        closedPendingDate = MySQLQuery.getAsDate(row[10]);
        closedPendingNotes = MySQLQuery.getAsString(row[11]);
        cityId = MySQLQuery.getAsInteger(row[12]);
        creatorId = MySQLQuery.getAsInteger(row[13]);
        createdDate = MySQLQuery.getAsDate(row[14]);
        createdFrom = MySQLQuery.getAsString(row[15]);
        document = MySQLQuery.getAsString(row[16]);
        firstName = MySQLQuery.getAsString(row[17]);
        lastName = MySQLQuery.getAsString(row[18]);
        estName = MySQLQuery.getAsString(row[19]);
        cliType = MySQLQuery.getAsString(row[20]);
        email = MySQLQuery.getAsString(row[21]);
        pref = MySQLQuery.getAsBoolean(row[22]);
        banned = MySQLQuery.getAsBoolean(row[23]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_contract";
    }

    public static String getSelFlds(String alias) {
        return new OrdContract().getSelFldsForAlias(alias);
    }

    public static List<OrdContract> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdContract().getListFromQuery(q, conn);
    }

    public static List<OrdContract> getList(Params p, Connection conn) throws Exception {
        return new OrdContract().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdContract().deleteById(id, conn);
    }

    public static List<OrdContract> getAll(Connection conn) throws Exception {
        return new OrdContract().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<OrdContract> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}