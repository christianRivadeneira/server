package api.ord.model;

import api.BaseModel;
import api.Params;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class OrdTankClient extends BaseModel<OrdTankClient> {
//inicio zona de reemplazo

    public String docType;
    public String document;
    public String dv;
    public String represenName;
    public String name;
    public String address;
    public String branch;
    public String phones;
    public BigDecimal lat;
    public BigDecimal lon;
    public int categId;
    public String neigh;
    public Integer neighId;
    public String type;
    public Date lastPollDate;
    public int mirrorId;
    public String description;
    public int cityId;
    public String folderName;
    public String folderNotes;
    public boolean active;
    public Integer createdId;
    public Date createdDate;
    public Integer modifiedId;
    public Date modifiedDate;
    public String regType;
    public int pendDocs;
    public int danePob;
    public Date beginDate;
    public Integer refClientId;
    public Boolean alert;
    public String network;
    public Integer accCityId;
    public Integer priceTypeId;
    public Integer billCityId;
    public Integer billInstanceId;
    public boolean checkedCoords;
    public String cache;
    public Integer frecId;
    public Integer pathId;
    public boolean hasFireSystem;
    public Integer contractorId;
    public boolean billWithoutMeter;
    public Integer execRegId;
    public boolean isExecMan;
    public Integer stratum;
    public Integer expectedUsers;
    public Integer createdUsers;
    public Integer sysCenterId;
    public String contactName;
    public String contactMail;
    public String contactPhone;
    public Integer sequence;
    public String frecType;
    public Integer intensity;
    public Boolean isEventual;
    public int criticLevelPerc;
    public BigDecimal quotaAsign;
    public BigDecimal quotaDispo;
    public Integer daysDebt;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "doc_type",
            "document",
            "dv",
            "represen_name",
            "name",
            "address",
            "branch",
            "phones",
            "lat",
            "lon",
            "categ_id",
            "neigh",
            "neigh_id",
            "type",
            "last_poll_date",
            "mirror_id",
            "description",
            "city_id",
            "folder_name",
            "folder_notes",
            "active",
            "created_id",
            "created_date",
            "modified_id",
            "modified_date",
            "reg_type",
            "pend_docs",
            "dane_pob",
            "begin_date",
            "ref_client_id",
            "alert",
            "network",
            "acc_city_id",
            "price_type_id",
            "bill_city_id",
            "bill_instance_id",
            "checked_coords",
            "cache",
            "frec_id",
            "path_id",
            "has_fire_system",
            "contractor_id",
            "bill_without_meter",
            "exec_reg_id",
            "is_exec_man",
            "stratum",
            "expected_users",
            "created_users",
            "sys_center_id",
            "contact_name",
            "contact_mail",
            "contact_phone",
            "sequence",
            "frec_type",
            "intensity",
            "is_eventual",
            "critic_level_perc",
            "quota_asign",
            "quota_dispo",
            "days_debt"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, docType);
        q.setParam(2, document);
        q.setParam(3, dv);
        q.setParam(4, represenName);
        q.setParam(5, name);
        q.setParam(6, address);
        q.setParam(7, branch);
        q.setParam(8, phones);
        q.setParam(9, lat);
        q.setParam(10, lon);
        q.setParam(11, categId);
        q.setParam(12, neigh);
        q.setParam(13, neighId);
        q.setParam(14, type);
        q.setParam(15, lastPollDate);
        q.setParam(16, mirrorId);
        q.setParam(17, description);
        q.setParam(18, cityId);
        q.setParam(19, folderName);
        q.setParam(20, folderNotes);
        q.setParam(21, active);
        q.setParam(22, createdId);
        q.setParam(23, createdDate);
        q.setParam(24, modifiedId);
        q.setParam(25, modifiedDate);
        q.setParam(26, regType);
        q.setParam(27, pendDocs);
        q.setParam(28, danePob);
        q.setParam(29, beginDate);
        q.setParam(30, refClientId);
        q.setParam(31, alert);
        q.setParam(32, network);
        q.setParam(33, accCityId);
        q.setParam(34, priceTypeId);
        q.setParam(35, billCityId);
        q.setParam(36, billInstanceId);
        q.setParam(37, checkedCoords);
        q.setParam(38, cache);
        q.setParam(39, frecId);
        q.setParam(40, pathId);
        q.setParam(41, hasFireSystem);
        q.setParam(42, contractorId);
        q.setParam(43, billWithoutMeter);
        q.setParam(44, execRegId);
        q.setParam(45, isExecMan);
        q.setParam(46, stratum);
        q.setParam(47, expectedUsers);
        q.setParam(48, createdUsers);
        q.setParam(49, sysCenterId);
        q.setParam(50, contactName);
        q.setParam(51, contactMail);
        q.setParam(52, contactPhone);
        q.setParam(53, sequence);
        q.setParam(54, frecType);
        q.setParam(55, intensity);
        q.setParam(56, isEventual);
        q.setParam(57, criticLevelPerc);
        q.setParam(58, quotaAsign);
        q.setParam(59, quotaDispo);
        q.setParam(60, daysDebt);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        docType = MySQLQuery.getAsString(row[0]);
        document = MySQLQuery.getAsString(row[1]);
        dv = MySQLQuery.getAsString(row[2]);
        represenName = MySQLQuery.getAsString(row[3]);
        name = MySQLQuery.getAsString(row[4]);
        address = MySQLQuery.getAsString(row[5]);
        branch = MySQLQuery.getAsString(row[6]);
        phones = MySQLQuery.getAsString(row[7]);
        lat = MySQLQuery.getAsBigDecimal(row[8], false);
        lon = MySQLQuery.getAsBigDecimal(row[9], false);
        categId = MySQLQuery.getAsInteger(row[10]);
        neigh = MySQLQuery.getAsString(row[11]);
        neighId = MySQLQuery.getAsInteger(row[12]);
        type = MySQLQuery.getAsString(row[13]);
        lastPollDate = MySQLQuery.getAsDate(row[14]);
        mirrorId = MySQLQuery.getAsInteger(row[15]);
        description = MySQLQuery.getAsString(row[16]);
        cityId = MySQLQuery.getAsInteger(row[17]);
        folderName = MySQLQuery.getAsString(row[18]);
        folderNotes = MySQLQuery.getAsString(row[19]);
        active = MySQLQuery.getAsBoolean(row[20]);
        createdId = MySQLQuery.getAsInteger(row[21]);
        createdDate = MySQLQuery.getAsDate(row[22]);
        modifiedId = MySQLQuery.getAsInteger(row[23]);
        modifiedDate = MySQLQuery.getAsDate(row[24]);
        regType = MySQLQuery.getAsString(row[25]);
        pendDocs = MySQLQuery.getAsInteger(row[26]);
        danePob = MySQLQuery.getAsInteger(row[27]);
        beginDate = MySQLQuery.getAsDate(row[28]);
        refClientId = MySQLQuery.getAsInteger(row[29]);
        alert = MySQLQuery.getAsBoolean(row[30]);
        network = MySQLQuery.getAsString(row[31]);
        accCityId = MySQLQuery.getAsInteger(row[32]);
        priceTypeId = MySQLQuery.getAsInteger(row[33]);
        billCityId = MySQLQuery.getAsInteger(row[34]);
        billInstanceId = MySQLQuery.getAsInteger(row[35]);
        checkedCoords = MySQLQuery.getAsBoolean(row[36]);
        cache = MySQLQuery.getAsString(row[37]);
        frecId = MySQLQuery.getAsInteger(row[38]);
        pathId = MySQLQuery.getAsInteger(row[39]);
        hasFireSystem = MySQLQuery.getAsBoolean(row[40]);
        contractorId = MySQLQuery.getAsInteger(row[41]);
        billWithoutMeter = MySQLQuery.getAsBoolean(row[42]);
        execRegId = MySQLQuery.getAsInteger(row[43]);
        isExecMan = MySQLQuery.getAsBoolean(row[44]);
        stratum = MySQLQuery.getAsInteger(row[45]);
        expectedUsers = MySQLQuery.getAsInteger(row[46]);
        createdUsers = MySQLQuery.getAsInteger(row[47]);
        sysCenterId = MySQLQuery.getAsInteger(row[48]);
        contactName = MySQLQuery.getAsString(row[49]);
        contactMail = MySQLQuery.getAsString(row[50]);
        contactPhone = MySQLQuery.getAsString(row[51]);
        sequence = MySQLQuery.getAsInteger(row[52]);
        frecType = MySQLQuery.getAsString(row[53]);
        intensity = MySQLQuery.getAsInteger(row[54]);
        isEventual = MySQLQuery.getAsBoolean(row[55]);
        criticLevelPerc = MySQLQuery.getAsInteger(row[56]);
        quotaAsign = MySQLQuery.getAsBigDecimal(row[57], false);
        quotaDispo = MySQLQuery.getAsBigDecimal(row[58], false);
        daysDebt = MySQLQuery.getAsInteger(row[59]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_tank_client";
    }

    public static String getSelFlds(String alias) {
        return new OrdTankClient().getSelFldsForAlias(alias);
    }

    public static List<OrdTankClient> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdTankClient().getListFromQuery(q, conn);
    }

    public static List<OrdTankClient> getList(Params p, Connection conn) throws Exception {
        return new OrdTankClient().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdTankClient().deleteById(id, conn);
    }

    public static List<OrdTankClient> getAll(Connection conn) throws Exception {
        return new OrdTankClient().getAllList(conn);
    }

//fin zona de reemplazo
    public String[][] getEnumOptionsAsMatrix(String fieldName) {
        if (fieldName.equals("type")) {
            return getEnumStrAsMatrix("norm=Ventas&build=Remisiones");
        }
        if (fieldName.equals("network")) {
            return getEnumStrAsMatrix("enterp=Empresa&third=Terceros");
        }
        return null;
    }
    
    public String getEnumOptions(String fieldName) {
        if (fieldName.equals("doc_type")) {
            return "nat=Persona Natural&ext=Cédula de Extranjería&rut_nat=Rut_Persona Natural&rut_jur=Rut_Persona Jurídica";
        }
        if (fieldName.equals("type")) {
            return "norm=Ventas&build=Remisiones&sac=Cliente Ocasional";
        }
        if (fieldName.equals("network")) {
            return "enterp=Empresa&third=Terceros";
        }
        if (fieldName.equals("frec_type")) {
            return "week=Semanal&biweek=Quincenal&month=Mensual";
        }
        if (fieldName.equals("frec_type2")) {
            return "biweek=Quincenal&month=Mensual";
        }
        if (fieldName.equals("frec_type3")) {
            return "week=Semanal&month=Mensual";
        }
        return null;
    }

    public static OrdTankClient getByMirror(int mirrorId, int instanceId, Connection conn) throws Exception {
        return new OrdTankClient().select(new MySQLQuery("SELECT " + getSelFlds("c") + " FROM ord_tank_client c WHERE c.bill_instance_id = " + instanceId + " AND c.mirror_id = " + mirrorId), conn);
    }

    public static String getCacheQuery(int tankClientId) {
        return "update ord_tank_client, ( "
                + " "
                + "SELECT otc.id as id,  "
                + "concat( "
                + "ifnull(group_concat(ifnull(v.`data`, '')), ''),',', "
                + "document,',', "
                + "IFNULL(dv,''),',', "
                + "IFNULL(represen_name,''),',', "
                + "IFNULL(name,''),',', "
                + "IFNULL(address,''),',', "
                + "IFNULL(branch,''),',', "
                + "IFNULL(phones,''),',', "
                + "IFNULL(neigh,''),',', "
                + "IFNULL(description,''),',', "
                + "IFNULL(folder_name,''),',', "
                + "IFNULL(folder_notes,''),',', "
                + "IFNULL((SELECT description FROM est_tank_category WHERE id = otc.categ_id), ''),',', "
                + "IFNULL((SELECT neigh.name FROM neigh WHERE id = otc.neigh_id), ''),',', "
                + "IFNULL((select p.name from dane_poblado p WHERE p.id = otc.dane_pob), ''),',',"
                + "IFNULL((SELECT CONCAT(sc.name,', Nie ', IFNULL(sc.nie,''),'') FROM sys_center AS sc WHERE sc.id = otc.sys_center_id), ''),',', "
                + "IFNULL((SELECT name FROM est_price_type WHERE id = price_type_id), ''),',', "
                + "IFNULL((SELECT name FROM bill_instance WHERE id = otc.bill_instance_id), ''),',' "
                + ") as c "
                + "FROM ord_tank_client otc "
                + "LEFT JOIN sys_frm_value v on v.owner_id = otc.id and v.field_id in (SELECT id FROM sys_frm_field f WHERE f.type_id = 13) "
                + (tankClientId != -1 ? "WHERE otc.id = " + tankClientId + " " : "")
                + "group by otc.id) as l set ord_tank_client.`cache` = l.c where ord_tank_client.id = l.id;";
    }

}
