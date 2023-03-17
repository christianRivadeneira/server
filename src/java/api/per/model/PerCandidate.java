package api.per.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class PerCandidate extends BaseModel<PerCandidate> {
//inicio zona de reemplazo

    public String firstName;
    public String lastName;
    public String document;
    public Date bDate;
    public String bCity;
    public String phones;
    public String celPhones;
    public String mail;
    public String address;
    public String gender;
    public String scLevel;
    public String state;
    public String notes;
    public Integer professionId;
    public Integer empId;
    public String bornPlace;
    public String socialLink;
    public String personalReference;
    public Integer channelId;
    public Integer perEmployeeId;
    public String institution;
    public Integer arpId;
    public Integer ipsId;
    public Integer fpId;
    public String civilStatus;
    public String couple;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "first_name",
            "last_name",
            "document",
            "b_date",
            "b_city",
            "phones",
            "cel_phones",
            "mail",
            "address",
            "gender",
            "sc_level",
            "state",
            "notes",
            "profession_id",
            "emp_id",
            "born_place",
            "social_link",
            "personal_reference",
            "channel_id",
            "per_employee_id",
            "institution",
            "arp_id",
            "ips_id",
            "fp_id",
            "civil_status",
            "couple"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, firstName);
        q.setParam(2, lastName);
        q.setParam(3, document);
        q.setParam(4, bDate);
        q.setParam(5, bCity);
        q.setParam(6, phones);
        q.setParam(7, celPhones);
        q.setParam(8, mail);
        q.setParam(9, address);
        q.setParam(10, gender);
        q.setParam(11, scLevel);
        q.setParam(12, state);
        q.setParam(13, notes);
        q.setParam(14, professionId);
        q.setParam(15, empId);
        q.setParam(16, bornPlace);
        q.setParam(17, socialLink);
        q.setParam(18, personalReference);
        q.setParam(19, channelId);
        q.setParam(20, perEmployeeId);
        q.setParam(21, institution);
        q.setParam(22, arpId);
        q.setParam(23, ipsId);
        q.setParam(24, fpId);
        q.setParam(25, civilStatus);
        q.setParam(26, couple);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        firstName = MySQLQuery.getAsString(row[0]);
        lastName = MySQLQuery.getAsString(row[1]);
        document = MySQLQuery.getAsString(row[2]);
        bDate = MySQLQuery.getAsDate(row[3]);
        bCity = MySQLQuery.getAsString(row[4]);
        phones = MySQLQuery.getAsString(row[5]);
        celPhones = MySQLQuery.getAsString(row[6]);
        mail = MySQLQuery.getAsString(row[7]);
        address = MySQLQuery.getAsString(row[8]);
        gender = MySQLQuery.getAsString(row[9]);
        scLevel = MySQLQuery.getAsString(row[10]);
        state = MySQLQuery.getAsString(row[11]);
        notes = MySQLQuery.getAsString(row[12]);
        professionId = MySQLQuery.getAsInteger(row[13]);
        empId = MySQLQuery.getAsInteger(row[14]);
        bornPlace = MySQLQuery.getAsString(row[15]);
        socialLink = MySQLQuery.getAsString(row[16]);
        personalReference = MySQLQuery.getAsString(row[17]);
        channelId = MySQLQuery.getAsInteger(row[18]);
        perEmployeeId = MySQLQuery.getAsInteger(row[19]);
        institution = MySQLQuery.getAsString(row[20]);
        arpId = MySQLQuery.getAsInteger(row[21]);
        ipsId = MySQLQuery.getAsInteger(row[22]);
        fpId = MySQLQuery.getAsInteger(row[23]);
        civilStatus = MySQLQuery.getAsString(row[24]);
        couple = MySQLQuery.getAsString(row[25]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "per_candidate";
    }

    public static String getSelFlds(String alias) {
        return new PerCandidate().getSelFldsForAlias(alias);
    }

    public static List<PerCandidate> getList(MySQLQuery q, Connection conn) throws Exception {
        return new PerCandidate().getListFromQuery(q, conn);
    }

    public static List<PerCandidate> getList(Params p, Connection conn) throws Exception {
        return new PerCandidate().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new PerCandidate().deleteById(id, conn);
    }

    public static List<PerCandidate> getAll(Connection conn) throws Exception {
        return new PerCandidate().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<PerCandidate> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}