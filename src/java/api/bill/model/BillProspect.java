package api.bill.model;

import api.BaseModel;
import api.Params;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;
import utilities.NameSplitter;

public class BillProspect extends BaseModel<BillProspect> {
//inicio zona de reemplazo

    public String docType;
    public String perType;
    public String doc;
    public String docCity;
    public String contractNum; //================ numero de contrato=================
    public String meterNum;
    public BigDecimal firstReading;
    public String realStateCode;
    public String cadastralCode;
    public String ciiu;
    public Boolean grandContrib;
    public String firstName;
    public String lastName;
    public String phones;
    public String mail;
    public Integer buildingId;
    public Integer buildingTypeId;
    public Integer clientTypeId;
    public boolean active;
    public int oldEstUsu;
    public String apartment;
    public boolean skipInterest;
    public boolean skipReconnect;
    public boolean skipContrib;
    public String exemptActivity;
    public Date netBuilding;
    public Date dateBeg;
    public Date dateLast;
    public Date creationDate;
    public String notes;
    public String cache;
    public boolean discon;
    public boolean mailPromo;
    public boolean mailBill;
    public boolean smsPromo;
    public boolean smsBill;
    public Integer stratum;
    public Integer neighId;
    public String address;
    public String sectorType;
    public String cadInfo;
    public boolean icfbHome;
    public boolean priorityHome;
    public boolean asentIndigena;
    public String netInstaller;
    public boolean converted;
    public String location;
    public Date birthDate;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "doc_type",
            "per_type",
            "doc",
            "doc_city",
            "contract_num",
            "meter_num",
            "first_reading",
            "real_state_code",
            "cadastral_code",
            "ciiu",
            "grand_contrib",
            "first_name",
            "last_name",
            "phones",
            "mail",
            "building_id",
            "building_type_id",
            "client_type_id",
            "active",
            "old_est_usu",
            "apartment",
            "skip_interest",
            "skip_reconnect",
            "skip_contrib",
            "exempt_activity",
            "net_building",
            "date_beg",
            "date_last",
            "creation_date",
            "notes",
            "cache",
            "discon",
            "mail_promo",
            "mail_bill",
            "sms_promo",
            "sms_bill",
            "stratum",
            "neigh_id",
            "address",
            "sector_type",
            "cad_info",
            "icfb_home",
            "priority_home",
            "asent_indigena",
            "net_installer",
            "converted",
            "location",
            "birth_date"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, docType);
        q.setParam(2, perType);
        q.setParam(3, doc);
        q.setParam(4, docCity);
        q.setParam(5, contractNum); // ================== numero de contrato =================
        q.setParam(6, meterNum);
        q.setParam(7, firstReading);
        q.setParam(8, realStateCode);
        q.setParam(9, cadastralCode);
        q.setParam(10, ciiu);
        q.setParam(11, grandContrib);
        q.setParam(12, firstName);
        q.setParam(13, lastName);
        q.setParam(14, phones);
        q.setParam(15, mail);
        q.setParam(16, buildingId);
        q.setParam(17, buildingTypeId);
        q.setParam(18, clientTypeId);
        q.setParam(19, active);
        q.setParam(20, oldEstUsu);
        q.setParam(21, apartment);
        q.setParam(22, skipInterest);
        q.setParam(23, skipReconnect);
        q.setParam(24, skipContrib);
        q.setParam(25, exemptActivity);
        q.setParam(26, netBuilding);
        q.setParam(27, dateBeg);
        q.setParam(28, dateLast);
        q.setParam(29, creationDate);
        q.setParam(30, notes);
        q.setParam(31, cache);
        q.setParam(32, discon);
        q.setParam(33, mailPromo);
        q.setParam(34, mailBill);
        q.setParam(35, smsPromo);
        q.setParam(36, smsBill);
        q.setParam(37, stratum);
        q.setParam(38, neighId);
        q.setParam(39, address);
        q.setParam(40, sectorType);
        q.setParam(41, cadInfo);
        q.setParam(42, icfbHome);
        q.setParam(43, priorityHome);
        q.setParam(44, asentIndigena);
        q.setParam(45, netInstaller);
        q.setParam(46, converted);
        q.setParam(47, location);
        q.setParam(48, birthDate);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        docType = MySQLQuery.getAsString(row[0]);
        perType = MySQLQuery.getAsString(row[1]);
        doc = MySQLQuery.getAsString(row[2]);
        docCity = MySQLQuery.getAsString(row[3]);
        contractNum = MySQLQuery.getAsString(row[4]); //================ numero de contrato ================
        meterNum = MySQLQuery.getAsString(row[5]);
        firstReading = MySQLQuery.getAsBigDecimal(row[6], false);
        realStateCode = MySQLQuery.getAsString(row[7]);
        cadastralCode = MySQLQuery.getAsString(row[8]);
        ciiu = MySQLQuery.getAsString(row[9]);
        grandContrib = MySQLQuery.getAsBoolean(row[10]);
        firstName = MySQLQuery.getAsString(row[11]);
        lastName = MySQLQuery.getAsString(row[12]);
        phones = MySQLQuery.getAsString(row[13]);
        mail = MySQLQuery.getAsString(row[14]);
        buildingId = MySQLQuery.getAsInteger(row[15]);
        buildingTypeId = MySQLQuery.getAsInteger(row[16]);
        clientTypeId = MySQLQuery.getAsInteger(row[17]);
        active = MySQLQuery.getAsBoolean(row[18]);
        oldEstUsu = MySQLQuery.getAsInteger(row[19]);
        apartment = MySQLQuery.getAsString(row[20]);
        skipInterest = MySQLQuery.getAsBoolean(row[21]);
        skipReconnect = MySQLQuery.getAsBoolean(row[22]);
        skipContrib = MySQLQuery.getAsBoolean(row[23]);
        exemptActivity = MySQLQuery.getAsString(row[24]);
        netBuilding = MySQLQuery.getAsDate(row[25]);
        dateBeg = MySQLQuery.getAsDate(row[26]);
        dateLast = MySQLQuery.getAsDate(row[27]);
        creationDate = MySQLQuery.getAsDate(row[28]);
        notes = MySQLQuery.getAsString(row[29]);
        cache = MySQLQuery.getAsString(row[30]);
        discon = MySQLQuery.getAsBoolean(row[31]);
        mailPromo = MySQLQuery.getAsBoolean(row[32]);
        mailBill = MySQLQuery.getAsBoolean(row[33]);
        smsPromo = MySQLQuery.getAsBoolean(row[34]);
        smsBill = MySQLQuery.getAsBoolean(row[35]);
        stratum = MySQLQuery.getAsInteger(row[36]);
        neighId = MySQLQuery.getAsInteger(row[37]);
        address = MySQLQuery.getAsString(row[38]);
        sectorType = MySQLQuery.getAsString(row[39]);
        cadInfo = MySQLQuery.getAsString(row[40]);
        icfbHome = MySQLQuery.getAsBoolean(row[41]);
        priorityHome = MySQLQuery.getAsBoolean(row[42]);
        asentIndigena = MySQLQuery.getAsBoolean(row[43]);
        netInstaller = MySQLQuery.getAsString(row[44]);
        converted = MySQLQuery.getAsBoolean(row[45]);
        location = MySQLQuery.getAsString(row[46]);
        birthDate = MySQLQuery.getAsDate(row[47]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_prospect";
    }

    public static String getSelFlds(String alias) {
        return new BillProspect().getSelFldsForAlias(alias);
    }

    public static List<BillProspect> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillProspect().getListFromQuery(q, conn);
    }

    public static List<BillProspect> getList(Params p, Connection conn) throws Exception {
        return new BillProspect().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillProspect().deleteById(id, conn);
    }

    public static List<BillProspect> getAll(Connection conn) throws Exception {
        return new BillProspect().getAllList(conn);
    }

//fin zona de reemplazo
    public void checkStrLengthsCguno(Connection conn) throws Exception {
        if (doc.length() > 13) {
            throw new Exception("El documento '" + doc + "' tiene más de 13 dígitos.");
        }

        String neigh = new MySQLQuery("SELECT name FROM sigma.neigh WHERE id = ?1").setParam(1, neighId).getAsString(conn);

        if ((address + " " + neigh).length() > 40) {
            throw new Exception("La dirección '" + (address + " " + neigh) + "' tiene más de 40 letras.");
        }

        if (phones.length() > 40) {
            throw new Exception("El teléfono '" + phones + "' tiene más de 15 dígitos.");
        }

        if (mail.length() > 50) {
            throw new Exception("El mail '" + mail + "' tiene más de 50 letras.");
        }

        if (perType.equals("jur")) {
            if (firstName.length() > 50) {
                throw new Exception("La razón social '" + firstName + "' tiene más de 15 letras.");
            }
        } else {
            if (lastName == null) {
                if (firstName.length() > 50) {
                    throw new Exception("El nombre '" + firstName + "' tiene más de 15 letras.");
                }
            } else {
                NameSplitter split = NameSplitter.split(lastName);
                if (split.cad1.length() > 15) {
                    throw new Exception("Los apellidos '" + split.cad1 + "' tienen más de 15 letras.");
                }
                if (split.cad2.length() > 15) {
                    throw new Exception("Los apellidos '" + split.cad2 + "' tienen más de 15 letras.");
                }
                if (firstName.length() > 20) {
                    throw new Exception("Los nombres '" + firstName + "' tienen más de 20 letras.");
                }
            }
        }
    }

}
