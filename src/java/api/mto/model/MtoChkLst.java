package api.mto.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class MtoChkLst extends BaseModel<MtoChkLst> {
//inicio zona de reemplazo

    public int vhId;
    public int creatorId;
    public int versionId;
    public int driverId;
    public Integer auxDriverId;
    public Date dt;
    public String notes;
    public Integer mileage;
    public Integer lastMileage;
    public Date nextDate;
    public Integer period;
    public Integer nextId;
    public Integer cdaId;
    public Boolean cdaRespon;
    public String responName;
    public String responJob;
    public String state;
    public Integer contractorId;
    public byte[] signRespon;
    public Integer agencyId;
    public Integer revId;
    public Integer hr;
    public Integer lastHr;
    public boolean isOk;
    public String notesWarning;
    public String notesRev;
    public Date dateRev;
    public Integer empRevId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "vh_id",
            "creator_id",
            "version_id",
            "driver_id",
            "aux_driver_id",
            "dt",
            "notes",
            "mileage",
            "last_mileage",
            "next_date",
            "period",
            "next_id",
            "cda_id",
            "cda_respon",
            "respon_name",
            "respon_job",
            "state",
            "contractor_id",
            "sign_respon",
            "agency_id",
            "rev_id",
            "hr",
            "last_hr",
            "is_ok",
            "notes_warning",
            "notes_rev",
            "date_rev",
            "emp_rev_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, vhId);
        q.setParam(2, creatorId);
        q.setParam(3, versionId);
        q.setParam(4, driverId);
        q.setParam(5, auxDriverId);
        q.setParam(6, dt);
        q.setParam(7, notes);
        q.setParam(8, mileage);
        q.setParam(9, lastMileage);
        q.setParam(10, nextDate);
        q.setParam(11, period);
        q.setParam(12, nextId);
        q.setParam(13, cdaId);
        q.setParam(14, cdaRespon);
        q.setParam(15, responName);
        q.setParam(16, responJob);
        q.setParam(17, state);
        q.setParam(18, contractorId);
        q.setParam(19, signRespon);
        q.setParam(20, agencyId);
        q.setParam(21, revId);
        q.setParam(22, hr);
        q.setParam(23, lastHr);
        q.setParam(24, isOk);
        q.setParam(25, notesWarning);
        q.setParam(26, notesRev);
        q.setParam(27, dateRev);
        q.setParam(28, empRevId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        vhId = MySQLQuery.getAsInteger(row[0]);
        creatorId = MySQLQuery.getAsInteger(row[1]);
        versionId = MySQLQuery.getAsInteger(row[2]);
        driverId = MySQLQuery.getAsInteger(row[3]);
        auxDriverId = MySQLQuery.getAsInteger(row[4]);
        dt = MySQLQuery.getAsDate(row[5]);
        notes = MySQLQuery.getAsString(row[6]);
        mileage = MySQLQuery.getAsInteger(row[7]);
        lastMileage = MySQLQuery.getAsInteger(row[8]);
        nextDate = MySQLQuery.getAsDate(row[9]);
        period = MySQLQuery.getAsInteger(row[10]);
        nextId = MySQLQuery.getAsInteger(row[11]);
        cdaId = MySQLQuery.getAsInteger(row[12]);
        cdaRespon = MySQLQuery.getAsBoolean(row[13]);
        responName = MySQLQuery.getAsString(row[14]);
        responJob = MySQLQuery.getAsString(row[15]);
        state = MySQLQuery.getAsString(row[16]);
        contractorId = MySQLQuery.getAsInteger(row[17]);
        signRespon = (row[18] != null ? (byte[]) row[18] : null);
        agencyId = MySQLQuery.getAsInteger(row[19]);
        revId = MySQLQuery.getAsInteger(row[20]);
        hr = MySQLQuery.getAsInteger(row[21]);
        lastHr = MySQLQuery.getAsInteger(row[22]);
        isOk = MySQLQuery.getAsBoolean(row[23]);
        notesWarning = MySQLQuery.getAsString(row[24]);
        notesRev = MySQLQuery.getAsString(row[25]);
        dateRev = MySQLQuery.getAsDate(row[26]);
        empRevId = MySQLQuery.getAsInteger(row[27]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mto_chk_lst";
    }

    public static String getSelFlds(String alias) {
        return new MtoChkLst().getSelFldsForAlias(alias);
    }

    public static List<MtoChkLst> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MtoChkLst().getListFromQuery(q, conn);
    }

    public static List<MtoChkLst> getList(Params p, Connection conn) throws Exception {
        return new MtoChkLst().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MtoChkLst().deleteById(id, conn);
    }

    public static List<MtoChkLst> getAll(Connection conn) throws Exception {
        return new MtoChkLst().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<MtoChkLst> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}