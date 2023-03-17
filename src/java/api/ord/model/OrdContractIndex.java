package api.ord.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.math.BigDecimal;
import java.util.Date;

public class OrdContractIndex extends BaseModel<OrdContractIndex> {
//inicio zona de reemplazo

    public String contractNum;
    public String ctrType;
    public String cliType;
    public String document;
    public String address;
    public String phones;
    public String firstName;
    public String lastName;
    public String estName;
    public int contractId;
    public Integer neighId;
    public int cityId;
    public Integer vehicleId;
    public Integer sowerId;
    public Integer ordAvg;
    public Date nextOrder;
    public boolean active;
    public Date birthDate;
    public String email;
    public BigDecimal lat;
    public BigDecimal lon;
    public boolean authAdvertising;
    public String type;
    public boolean pref;
    public boolean institutional;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "contract_num",
            "ctr_type",
            "cli_type",
            "document",
            "address",
            "phones",
            "first_name",
            "last_name",
            "est_name",
            "contract_id",
            "neigh_id",
            "city_id",
            "vehicle_id",
            "sower_id",
            "ord_avg",
            "next_order",
            "active",
            "birth_date",
            "email",
            "lat",
            "lon",
            "auth_advertising",
            "type",
            "pref",
            "institutional"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, contractNum);
        q.setParam(2, ctrType);
        q.setParam(3, cliType);
        q.setParam(4, document);
        q.setParam(5, address);
        q.setParam(6, phones);
        q.setParam(7, firstName);
        q.setParam(8, lastName);
        q.setParam(9, estName);
        q.setParam(10, contractId);
        q.setParam(11, neighId);
        q.setParam(12, cityId);
        q.setParam(13, vehicleId);
        q.setParam(14, sowerId);
        q.setParam(15, ordAvg);
        q.setParam(16, nextOrder);
        q.setParam(17, active);
        q.setParam(18, birthDate);
        q.setParam(19, email);
        q.setParam(20, lat);
        q.setParam(21, lon);
        q.setParam(22, authAdvertising);
        q.setParam(23, type);
        q.setParam(24, pref);
        q.setParam(25, institutional);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        contractNum = MySQLQuery.getAsString(row[0]);
        ctrType = MySQLQuery.getAsString(row[1]);
        cliType = MySQLQuery.getAsString(row[2]);
        document = MySQLQuery.getAsString(row[3]);
        address = MySQLQuery.getAsString(row[4]);
        phones = MySQLQuery.getAsString(row[5]);
        firstName = MySQLQuery.getAsString(row[6]);
        lastName = MySQLQuery.getAsString(row[7]);
        estName = MySQLQuery.getAsString(row[8]);
        contractId = MySQLQuery.getAsInteger(row[9]);
        neighId = MySQLQuery.getAsInteger(row[10]);
        cityId = MySQLQuery.getAsInteger(row[11]);
        vehicleId = MySQLQuery.getAsInteger(row[12]);
        sowerId = MySQLQuery.getAsInteger(row[13]);
        ordAvg = MySQLQuery.getAsInteger(row[14]);
        nextOrder = MySQLQuery.getAsDate(row[15]);
        active = MySQLQuery.getAsBoolean(row[16]);
        birthDate = MySQLQuery.getAsDate(row[17]);
        email = MySQLQuery.getAsString(row[18]);
        lat = MySQLQuery.getAsBigDecimal(row[19], false);
        lon = MySQLQuery.getAsBigDecimal(row[20], false);
        authAdvertising = MySQLQuery.getAsBoolean(row[21]);
        type = MySQLQuery.getAsString(row[22]);
        pref = MySQLQuery.getAsBoolean(row[23]);
        institutional = MySQLQuery.getAsBoolean(row[24]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_contract_index";
    }

    public static String getSelFlds(String alias) {
        return new OrdContractIndex().getSelFldsForAlias(alias);
    }

    public static List<OrdContractIndex> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdContractIndex().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdContractIndex().deleteById(id, conn);
    }

    public static List<OrdContractIndex> getAll(Connection conn) throws Exception {
        return new OrdContractIndex().getAllList(conn);
    }

//fin zona de reemplazo
    
    public String getEnumOptions(String fieldName) {
        if (fieldName.equals("ctr_type")) {
            return "como=como&afil=afil";
        }
        if (fieldName.equals("ctr_type_all")) {
            return "rut_nat=Rut_Persona Natural&rut_jur=Rut_Persona Jurídica&nat=Persona Natural&cc_ext=Cédula Extranjería&pas=Pasaporte&cc_ven=Cédula Venezolana&others=Otros";
        }
        if (fieldName.equals("cli_type")) {
            return "rut_nat=rut_nat&rut_jur=rut_jur&nat=nat&ext=ext&ti=ti";
        }
        if (fieldName.equals("contract_type")) {
            return "univ=Prov&brand=Afil&app=App&store=Tend";
        }
        if (fieldName.equals("complete_type")) {
            return "univ=Provisional&brand=Afiliado&app=App";
        }
        if (fieldName.equals("complete_type_clc")) {
            return "univ=Provisional&brand=Afiliado";
        }
        return null;
    }

}