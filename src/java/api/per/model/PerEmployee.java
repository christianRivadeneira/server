package api.per.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class PerEmployee extends BaseModel<PerEmployee> {
//inicio zona de reemplazo

    public Integer empId;
    public String firstName;
    public String lastName;
    public String document;
    public String numHist;
    public Date bDate;
    public String bCity;
    public String curCity;
    public String mail;
    public String phones;
    public String celPhones;
    public String personPhones;
    public String personMail;
    public String address;
    public String neigh;
    public String gender;
    public String scLevel;
    public int arpId;
    public int ipsId;
    public int fpId;
    public Integer ccId;
    public Integer cesId;
    public boolean active;
    public String notes;
    public Integer professionId;
    public boolean fondo;
    public String size;
    public Integer elementId;
    public Date elementDate;
    public String cardNum;
    public Date cardDelivDate;
    public String allergies;
    public String bloodType;
    public Boolean card;
    public Date lastCheckIn;
    public boolean badFingerprints;
    public String bornPlace;
    public Date expeditionDocDate;
    public String cache;
    public String civilStatus;
    public String couple;
    public String couplePhone;
    public Date registry;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "emp_id",
            "first_name",
            "last_name",
            "document",
            "num_hist",
            "b_date",
            "b_city",
            "cur_city",
            "mail",
            "phones",
            "cel_phones",
            "person_phones",
            "person_mail",
            "address",
            "neigh",
            "gender",
            "sc_level",
            "arp_id",
            "ips_id",
            "fp_id",
            "cc_id",
            "ces_id",
            "active",
            "notes",
            "profession_id",
            "fondo",
            "size",
            "element_id",
            "element_date",
            "card_num",
            "card_deliv_date",
            "allergies",
            "blood_type",
            "card",
            "last_check_in",
            "bad_fingerprints",
            "born_place",
            "expedition_doc_date",
            "cache",
            "civil_status",
            "couple",
            "couple_phone",
            "registry"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, empId);
        q.setParam(2, firstName);
        q.setParam(3, lastName);
        q.setParam(4, document);
        q.setParam(5, numHist);
        q.setParam(6, bDate);
        q.setParam(7, bCity);
        q.setParam(8, curCity);
        q.setParam(9, mail);
        q.setParam(10, phones);
        q.setParam(11, celPhones);
        q.setParam(12, personPhones);
        q.setParam(13, personMail);
        q.setParam(14, address);
        q.setParam(15, neigh);
        q.setParam(16, gender);
        q.setParam(17, scLevel);
        q.setParam(18, arpId);
        q.setParam(19, ipsId);
        q.setParam(20, fpId);
        q.setParam(21, ccId);
        q.setParam(22, cesId);
        q.setParam(23, active);
        q.setParam(24, notes);
        q.setParam(25, professionId);
        q.setParam(26, fondo);
        q.setParam(27, size);
        q.setParam(28, elementId);
        q.setParam(29, elementDate);
        q.setParam(30, cardNum);
        q.setParam(31, cardDelivDate);
        q.setParam(32, allergies);
        q.setParam(33, bloodType);
        q.setParam(34, card);
        q.setParam(35, lastCheckIn);
        q.setParam(36, badFingerprints);
        q.setParam(37, bornPlace);
        q.setParam(38, expeditionDocDate);
        q.setParam(39, cache);
        q.setParam(40, civilStatus);
        q.setParam(41, couple);
        q.setParam(42, couplePhone);
        q.setParam(43, registry);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        empId = MySQLQuery.getAsInteger(row[0]);
        firstName = MySQLQuery.getAsString(row[1]);
        lastName = MySQLQuery.getAsString(row[2]);
        document = MySQLQuery.getAsString(row[3]);
        numHist = MySQLQuery.getAsString(row[4]);
        bDate = MySQLQuery.getAsDate(row[5]);
        bCity = MySQLQuery.getAsString(row[6]);
        curCity = MySQLQuery.getAsString(row[7]);
        mail = MySQLQuery.getAsString(row[8]);
        phones = MySQLQuery.getAsString(row[9]);
        celPhones = MySQLQuery.getAsString(row[10]);
        personPhones = MySQLQuery.getAsString(row[11]);
        personMail = MySQLQuery.getAsString(row[12]);
        address = MySQLQuery.getAsString(row[13]);
        neigh = MySQLQuery.getAsString(row[14]);
        gender = MySQLQuery.getAsString(row[15]);
        scLevel = MySQLQuery.getAsString(row[16]);
        arpId = MySQLQuery.getAsInteger(row[17]);
        ipsId = MySQLQuery.getAsInteger(row[18]);
        fpId = MySQLQuery.getAsInteger(row[19]);
        ccId = MySQLQuery.getAsInteger(row[20]);
        cesId = MySQLQuery.getAsInteger(row[21]);
        active = MySQLQuery.getAsBoolean(row[22]);
        notes = MySQLQuery.getAsString(row[23]);
        professionId = MySQLQuery.getAsInteger(row[24]);
        fondo = MySQLQuery.getAsBoolean(row[25]);
        size = MySQLQuery.getAsString(row[26]);
        elementId = MySQLQuery.getAsInteger(row[27]);
        elementDate = MySQLQuery.getAsDate(row[28]);
        cardNum = MySQLQuery.getAsString(row[29]);
        cardDelivDate = MySQLQuery.getAsDate(row[30]);
        allergies = MySQLQuery.getAsString(row[31]);
        bloodType = MySQLQuery.getAsString(row[32]);
        card = MySQLQuery.getAsBoolean(row[33]);
        lastCheckIn = MySQLQuery.getAsDate(row[34]);
        badFingerprints = MySQLQuery.getAsBoolean(row[35]);
        bornPlace = MySQLQuery.getAsString(row[36]);
        expeditionDocDate = MySQLQuery.getAsDate(row[37]);
        cache = MySQLQuery.getAsString(row[38]);
        civilStatus = MySQLQuery.getAsString(row[39]);
        couple = MySQLQuery.getAsString(row[40]);
        couplePhone = MySQLQuery.getAsString(row[41]);
        registry = MySQLQuery.getAsDate(row[42]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "per_employee";
    }

    public static String getSelFlds(String alias) {
        return new PerEmployee().getSelFldsForAlias(alias);
    }

    public static List<PerEmployee> getList(MySQLQuery q, Connection conn) throws Exception {
        return new PerEmployee().getListFromQuery(q, conn);
    }

    public static List<PerEmployee> getList(Params p, Connection conn) throws Exception {
        return new PerEmployee().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new PerEmployee().deleteById(id, conn);
    }

    public static List<PerEmployee> getAll(Connection conn) throws Exception {
        return new PerEmployee().getAllList(conn);
    }

//fin zona de reemplazo
    
    public String[][] getEnumOptionsAsMatrix(String fieldName) {
        if (fieldName.equals("gender")) {
            return getEnumStrAsMatrix("m=Masculino&f=Femenino");
        } else if (fieldName.equals("sc_level")) {
            return getEnumStrAsMatrix("pre=Preescolar&pri=Primaria&babac=Secundaria=&bachi=Media&tecno=Técnologo&tecni=Técnico&univ=Pregrado&post=Especialización&mast=Maestría&phd=Doctorado");
        } else if (fieldName.equals("sc_level_tbl")) {
            return getEnumStrAsMatrix("pre=1&"
                        + "pri=2&"
                        + "babac=3&"
                        + "bachi=4&"
                        + "tecno=5&"
                        + "tecni=6&"
                        + "univ=7&"
                        + "post=8&"
                        + "mast=9&"
                        + "phd=10");
        } else if (fieldName.equals("blood_type")) {
            return getEnumStrAsMatrix("AB-=AB-&AB+=AB+&A-=A-&A+=A+&B-=B-&B+=B+&O-=O-&O+=O+");
        } else if (fieldName.equals("civil_status")) {
            return getEnumStrAsMatrix("free=Union Libre&"
                        + "sing=Soltero(a)&"
                        + "sepa=Separado(a)&"
                        + "marr=Casado(a)&"
                        + "wido=Viudo(a)");
        }
        return null;
    }
}
