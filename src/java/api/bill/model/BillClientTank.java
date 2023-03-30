package api.bill.model;

import api.BaseAPI;
import api.BaseModel;
import api.Params;
import api.bill.api.BillMeterApi;
import api.ord.model.OrdPqrClientTank;
import api.sys.model.SysCrudLog;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static api.bill.api.BillClientTankApi.getCode;
import static api.bill.api.BillClientTankApi.zeroFill;
import utilities.MySQLQuery;

public class BillClientTank extends BaseModel<BillClientTank> {

    public static int getCountClients(int buildingId, Connection conn) throws Exception {
        return new MySQLQuery("select count(*) "
                + "from bill_client_tank o "
                + "where o.building_id = ?1 AND o.active = 1").setParam(1, buildingId).getAsInteger(conn);
    }

//inicio zona de reemplazo

    public String docType;
    public String perType;
    public String doc;
    public String docCity;
    public String contractNum;
    public String realStateCode;
    public String cadastralCode;
    public String ciiu;
    public Boolean grandContrib;
    public String firstName;
    public String lastName;
    public String phones;
    public String mail;
    public String numInstall;
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
    public String code;
    public String cache;
    public boolean discon;
    public boolean mailPromo;
    public boolean mailBill;
    public boolean smsPromo;
    public boolean smsBill;
    public Integer stratum;
    public Integer neighId;
    public Integer prospectId;
    public String address;
    public String sectorType;
    public String cadInfo;
    public boolean icfbHome;
    public boolean priorityHome;
    public boolean asentIndigena;
    public String netInstaller;
    public boolean spanClosed;
    public boolean ebillSent;
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
            "real_state_code",
            "cadastral_code",
            "ciiu",
            "grand_contrib",
            "first_name",
            "last_name",
            "phones",
            "mail",
            "num_install",
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
            "code",
            "cache",
            "discon",
            "mail_promo",
            "mail_bill",
            "sms_promo",
            "sms_bill",
            "stratum",
            "neigh_id",
            "prospect_id",
            "address",
            "sector_type",
            "cad_info",
            "icfb_home",
            "priority_home",
            "asent_indigena",
            "net_installer",
            "span_closed",
            "ebill_sent",
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
        q.setParam(5, contractNum);
        q.setParam(6, realStateCode);
        q.setParam(7, cadastralCode);
        q.setParam(8, ciiu);
        q.setParam(9, grandContrib);
        q.setParam(10, firstName);
        q.setParam(11, lastName);
        q.setParam(12, phones);
        q.setParam(13, mail);
        q.setParam(14, numInstall);
        q.setParam(15, buildingId);
        q.setParam(16, buildingTypeId);
        q.setParam(17, clientTypeId);
        q.setParam(18, active);
        q.setParam(19, oldEstUsu);
        q.setParam(20, apartment);
        q.setParam(21, skipInterest);
        q.setParam(22, skipReconnect);
        q.setParam(23, skipContrib);
        q.setParam(24, exemptActivity);
        q.setParam(25, netBuilding);
        q.setParam(26, dateBeg);
        q.setParam(27, dateLast);
        q.setParam(28, creationDate);
        q.setParam(29, notes);
        q.setParam(30, code);
        q.setParam(31, cache);
        q.setParam(32, discon);
        q.setParam(33, mailPromo);
        q.setParam(34, mailBill);
        q.setParam(35, smsPromo);
        q.setParam(36, smsBill);
        q.setParam(37, stratum);
        q.setParam(38, neighId);
        q.setParam(39, prospectId);
        q.setParam(40, address);
        q.setParam(41, sectorType);
        q.setParam(42, cadInfo);
        q.setParam(43, icfbHome);
        q.setParam(44, priorityHome);
        q.setParam(45, asentIndigena);
        q.setParam(46, netInstaller);
        q.setParam(47, spanClosed);
        q.setParam(48, ebillSent);
        q.setParam(49, location);
        q.setParam(50, birthDate);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        docType = MySQLQuery.getAsString(row[0]);
        perType = MySQLQuery.getAsString(row[1]);
        doc = MySQLQuery.getAsString(row[2]);
        docCity = MySQLQuery.getAsString(row[3]);
        //contractNum = MySQLQuery.getAsString(row[4]); //================ numero de contrato ================
        realStateCode = MySQLQuery.getAsString(row[5]);
        cadastralCode = MySQLQuery.getAsString(row[6]);
        ciiu = MySQLQuery.getAsString(row[7]);
        grandContrib = MySQLQuery.getAsBoolean(row[8]);
        firstName = MySQLQuery.getAsString(row[9]);
        lastName = MySQLQuery.getAsString(row[10]);
        phones = MySQLQuery.getAsString(row[11]);
        mail = MySQLQuery.getAsString(row[12]);
        numInstall = MySQLQuery.getAsString(row[13]);
        buildingId = MySQLQuery.getAsInteger(row[14]);
        buildingTypeId = MySQLQuery.getAsInteger(row[15]);
        clientTypeId = MySQLQuery.getAsInteger(row[16]);
        active = MySQLQuery.getAsBoolean(row[17]);
        oldEstUsu = MySQLQuery.getAsInteger(row[18]);
        apartment = MySQLQuery.getAsString(row[19]);
        skipInterest = MySQLQuery.getAsBoolean(row[20]);
        skipReconnect = MySQLQuery.getAsBoolean(row[21]);
        skipContrib = MySQLQuery.getAsBoolean(row[22]);
        exemptActivity = MySQLQuery.getAsString(row[23]);
        netBuilding = MySQLQuery.getAsDate(row[24]);
        dateBeg = MySQLQuery.getAsDate(row[25]);
        dateLast = MySQLQuery.getAsDate(row[26]);
        creationDate = MySQLQuery.getAsDate(row[27]);
        notes = MySQLQuery.getAsString(row[28]);
        code = MySQLQuery.getAsString(row[29]);
        cache = MySQLQuery.getAsString(row[30]);
        discon = MySQLQuery.getAsBoolean(row[31]);
        mailPromo = MySQLQuery.getAsBoolean(row[32]);
        mailBill = MySQLQuery.getAsBoolean(row[33]);
        smsPromo = MySQLQuery.getAsBoolean(row[34]);
        smsBill = MySQLQuery.getAsBoolean(row[35]);
        stratum = MySQLQuery.getAsInteger(row[36]);
        neighId = MySQLQuery.getAsInteger(row[37]);
        prospectId = MySQLQuery.getAsInteger(row[38]);
        address = MySQLQuery.getAsString(row[39]);
        sectorType = MySQLQuery.getAsString(row[40]);
        cadInfo = MySQLQuery.getAsString(row[41]);
        icfbHome = MySQLQuery.getAsBoolean(row[42]);
        priorityHome = MySQLQuery.getAsBoolean(row[43]);
        asentIndigena = MySQLQuery.getAsBoolean(row[44]);
        netInstaller = MySQLQuery.getAsString(row[45]);
        spanClosed = MySQLQuery.getAsBoolean(row[46]);
        ebillSent = MySQLQuery.getAsBoolean(row[47]);
        location = MySQLQuery.getAsString(row[48]);
        birthDate = MySQLQuery.getAsDate(row[49]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }
    
    public static void main(String[] args) {
        
    }
    
    @Override
    protected String getTblName() {
        return "bill_client_tank";
    }

    public static String getSelFlds(String alias) {
        return new BillClientTank().getSelFldsForAlias(alias);
    }

    public static List<BillClientTank> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillClientTank().getListFromQuery(q, conn);
    }

    public static List<BillClientTank> getList(Params p, Connection conn) throws Exception {
        return new BillClientTank().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillClientTank().deleteById(id, conn);
    }

    public static List<BillClientTank> getAll(Connection conn) throws Exception {
        return new BillClientTank().getAllList(conn);
    }

//fin zona de reemplazo
    public static String getSectorDescription(String sectorType) {
        switch (sectorType) {
            case "r":
                return "Residencial";
            case "c":
                return "Comercial";
            case "i":
                return "Industrial";
            case "o":
                return "Oficial";
            case "ea":
                return "Especial Asistencial";
            case "ed":
                return "Especial Educativo";
            default:
                throw new RuntimeException("Unrecognized sectorType");
        }
    }

    public static BillClientTank create(CreateBillClientRequest req, BillInstance inst, Connection conn, BaseAPI caller) throws Exception {
        inst.useInstance(conn);

        BillClientTank obj = req.client;
        obj.creationDate = new Date();
        if (inst.isTankInstance()) {
            Boolean showUsersNumber = new MySQLQuery("SELECT e.show_users_number FROM sigma.est_cfg e WHERE e.id = 1").getAsBoolean(conn);
            if (showUsersNumber && !getAllowCreateUsers(obj.buildingId, inst, conn)) {
                throw new Exception("Se alcanzó el máximo número de usuarios permitidos");

            }
        }

        switch (inst.type) {
            case "net":
                obj.numInstall = System.currentTimeMillis() + "";
                obj.insert(conn);
                obj.code = getCode(inst.id, obj.prospectId);
                obj.numInstall = obj.code;
                break;
            case "tank":
                String build = zeroFill(new BillBuilding().select(obj.buildingId, conn).oldId.toString(), 3);
                obj.numInstall = (build + "-" + obj.apartment);
                obj.insert(conn);
                obj.code = getCode(inst.id, obj.id);
                break;
            default:
                throw new Exception();
        }
        obj.update(conn);

        if (inst.isTankInstance()) {
            updateClientCounters(obj.buildingId, inst.id, conn);
        }

        BillSpan reca = BillSpan.getByState("reca", conn);
        int recaId = reca.id;
        if (inst.isTankInstance()) {
            new MySQLQuery("INSERT INTO bill_client_list SET client_id = " + obj.id + ", span_id = " + recaId + ", list_id = (SELECT id FROM bill_price_list WHERE default_opt = 1)").executeUpdate(conn);
        }
        BillClientTank.updateCache(obj.id, conn);

        BillMeter m = new BillMeter();
        m.clientId = obj.id;
        m.factor = new BigDecimal(1);
        m.number = req.meterNum;
        m.start = new Date();
        m.startReading = req.initialReading;
        m.startSpanId = recaId + 1;
        BillMeterApi.insert(m, conn, caller);

        caller.useDefault(conn);
        //espejo
        OrdPqrClientTank mirror = new OrdPqrClientTank();
        mirror.apartament = (obj.apartment);
        mirror.cityId = inst.cityId;
        mirror.billInstanceId = inst.id;
        mirror.firstName = obj.firstName;
        mirror.lastName = obj.lastName;
        mirror.numInstall = obj.numInstall;
        mirror.mirrorId = obj.id;
        mirror.phones = obj.phones;
        mirror.doc = obj.doc;
        if (inst.isTankInstance()) {
            Integer ordTankClientId = MySQLQuery.getAsInteger(new MySQLQuery("SELECT id FROM sigma.ord_tank_client c WHERE c.bill_instance_id = " + inst.id + " AND c.mirror_id = " + obj.buildingId).getAsInteger(conn));
            if (ordTankClientId == null) {
                throw new Exception("No se encontro el cliente estacionario en esta instancia");
            }
            mirror.buildOrdId = ordTankClientId;
            mirror.neighId = null;
            mirror.address = null;
        } else {
            mirror.buildOrdId = null;
            mirror.neighId = obj.neighId;
            mirror.address = obj.address;
        }
        mirror.insert(conn);

        conn.commit();
        caller.useDefault(conn);
        SysCrudLog.created(caller, obj, conn);
        return obj;
    }

    private static Boolean getAllowCreateUsers(int buildingId, BillInstance inst, Connection conn) throws Exception {
        Integer ordClientTankId = new MySQLQuery("SELECT c.id FROM sigma.ord_tank_client c "
                + "WHERE c.bill_instance_id = " + inst.id + " AND c.mirror_id = " + buildingId + " AND c.active = 1").getAsInteger(conn);

        return new MySQLQuery("SELECT "
                + "c.expected_users > "
                + "(SELECT COUNT(*) FROM bill_client_tank c WHERE c.building_id = " + buildingId + " AND c.active = 1 ) "
                + "FROM sigma.ord_tank_client c "
                + "WHERE id = " + ordClientTankId).getAsBoolean(conn);
    }

    public static void updateClientCounters(int billBuildId, int instId, Connection conn) throws Exception {
        Integer ordClientTank = new MySQLQuery("SELECT c.id "
                + "FROM sigma.ord_tank_client c "
                + "WHERE c.bill_instance_id = " + instId + " AND c.mirror_id = " + billBuildId + " AND c.active = 1").getAsInteger(conn);

        new MySQLQuery("UPDATE sigma.ord_tank_client  SET created_users = "
                + " (SELECT COUNT(*) FROM bill_client_tank c WHERE c.building_id = " + billBuildId + " AND c.active = 1 ) "
                + " WHERE id = " + ordClientTank).executeUpdate(conn);

    }

    public static BillClientTank getByInstNum(String instNum, Connection conn) throws Exception {
        return new BillClientTank().select(new MySQLQuery("SELECT " + getSelFlds("") + ", id FROM bill_client_tank WHERE UPPER(`num_install`) = ?1").setParam(1, instNum.toUpperCase()), conn);
    }

    public static BillClientTank[] getAll(boolean justActive, Connection conn) throws Exception {
        List<BillClientTank> list = getList(new MySQLQuery("SELECT " + getSelFlds("") + ", id FROM bill_client_tank " + (justActive ? " WHERE active = 1" : "") + " ORDER BY num_install"), conn);
        return list.toArray(new BillClientTank[list.size()]);
    }

    public static BillClientTank[] getByBuildId(int buildId, boolean justActive, Connection conn) throws Exception {
        List<BillClientTank> list = getList(new MySQLQuery("SELECT " + getSelFlds("") + ", id FROM bill_client_tank WHERE building_id = " + buildId + (justActive ? " AND active = 1" : "") + " ORDER BY num_install"), conn);
        return list.toArray(new BillClientTank[list.size()]);
    }

    public static void updateCache(int clientId, Connection conn) throws Exception {
        new MySQLQuery(BillClientTank.getCacheQuery(clientId)).executeUpdate(conn);
    }

    public static String getCacheQuery(int clientId) {
        return "update bill_client_tank, ( "
                + " "
                + "SELECT bc.id as id,  "
                + "concat( "
                + "IFNULL(bc.doc,''),',', "
                + "IFNULL(bc.first_name,''),',', "
                + "IFNULL(bc.last_name,''),',', "
                + "IFNULL(bc.phones,''),',', "
                + "IFNULL((SELECT `number` FROM bill_meter WHERE client_id = bc.id ORDER BY start_span_id DESC LIMIT 1), ''),',', "
                + "IFNULL(bc.num_install,''),',', "
                + "IFNULL(bc.apartment,''),',', "
                + "IFNULL(bc.notes,''),',', "
                + "IFNULL((SELECT name FROM bill_building bb WHERE bb.id = bc.building_id), ''),',', "
                + "IFNULL(bc.code,''),',', "
                + "IFNULL(bc.address,''),',', "
                + "IFNULL((SELECT name FROM sigma.neigh n WHERE n.id = bc.neigh_id),''),',' "
                + ") as c "
                + "FROM bill_client_tank bc "
                + "WHERE bc.id = " + clientId + " "
                + "group by bc.id) as l set bill_client_tank.`cache` = l.c where bill_client_tank.id = l.id";
    }

    public List<String> getPhonesForSMS() {
        List<String> rta = new ArrayList<>();
        String s = phones.toLowerCase().replaceAll("[^0-9,\\- ]", "").replaceAll("[- ]", ",");
        String[] parts = s.split(",");
        for (String part : parts) {
            if (part.matches("3\\d\\d\\d\\d\\d\\d\\d\\d\\d")) {
                rta.add(part);
            }
        }
        return rta;
    }

    public static String getClientName(BillClientTank cl) {
        String name = "";
        if (cl.firstName != null && cl.firstName.length() > 0) {
            name += cl.firstName;
        }
        if (cl.lastName != null && cl.lastName.length() > 0) {
            name += " " + cl.lastName;
        }
        return name;
    }
}
