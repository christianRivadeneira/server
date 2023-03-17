package api.mto.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class Vehicle extends BaseModel<Vehicle> {
//inicio zona de reemplazo

    public String plate;
    public String internal;
    public String accCode;
    public Integer model;
    public Integer vehicleTypeId;
    public int agencyId;
    public Integer contractorId;
    public Boolean contract;
    public String chasis;
    public String engine;
    public Integer cylinderCap;
    public boolean active;
    public String observ;
    public boolean visible;
    public String str1;
    public String str2;
    public String str3;
    public String color;
    public Date registerDate;
    public Integer eqsCenterId;
    public Integer eqsLocationId;
    public String eqsInternal;
    public Integer eqsAccCode;
    public String cache;
    public boolean hasServiceManager;
    public String zoneType;
    public boolean prevMto;
    public String kmSrc;
    public boolean hrSrc;
    public String mtoType;
    public String codGps;
    public Integer vhTreadId;
    public boolean isTank;
    public Integer galCap;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "plate",
            "internal",
            "acc_code",
            "model",
            "vehicle_type_id",
            "agency_id",
            "contractor_id",
            "contract",
            "chasis",
            "engine",
            "cylinder_cap",
            "active",
            "observ",
            "visible",
            "str1",
            "str2",
            "str3",
            "color",
            "register_date",
            "eqs_center_id",
            "eqs_location_id",
            "eqs_internal",
            "eqs_acc_code",
            "cache",
            "has_service_manager",
            "zone_type",
            "prev_mto",
            "km_src",
            "hr_src",
            "mto_type",
            "cod_gps",
            "vh_tread_id",
            "is_tank",
            "gal_cap"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, plate);
        q.setParam(2, internal);
        q.setParam(3, accCode);
        q.setParam(4, model);
        q.setParam(5, vehicleTypeId);
        q.setParam(6, agencyId);
        q.setParam(7, contractorId);
        q.setParam(8, contract);
        q.setParam(9, chasis);
        q.setParam(10, engine);
        q.setParam(11, cylinderCap);
        q.setParam(12, active);
        q.setParam(13, observ);
        q.setParam(14, visible);
        q.setParam(15, str1);
        q.setParam(16, str2);
        q.setParam(17, str3);
        q.setParam(18, color);
        q.setParam(19, registerDate);
        q.setParam(20, eqsCenterId);
        q.setParam(21, eqsLocationId);
        q.setParam(22, eqsInternal);
        q.setParam(23, eqsAccCode);
        q.setParam(24, cache);
        q.setParam(25, hasServiceManager);
        q.setParam(26, zoneType);
        q.setParam(27, prevMto);
        q.setParam(28, kmSrc);
        q.setParam(29, hrSrc);
        q.setParam(30, mtoType);
        q.setParam(31, codGps);
        q.setParam(32, vhTreadId);
        q.setParam(33, isTank);
        q.setParam(34, galCap);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        plate = MySQLQuery.getAsString(row[0]);
        internal = MySQLQuery.getAsString(row[1]);
        accCode = MySQLQuery.getAsString(row[2]);
        model = MySQLQuery.getAsInteger(row[3]);
        vehicleTypeId = MySQLQuery.getAsInteger(row[4]);
        agencyId = MySQLQuery.getAsInteger(row[5]);
        contractorId = MySQLQuery.getAsInteger(row[6]);
        contract = MySQLQuery.getAsBoolean(row[7]);
        chasis = MySQLQuery.getAsString(row[8]);
        engine = MySQLQuery.getAsString(row[9]);
        cylinderCap = MySQLQuery.getAsInteger(row[10]);
        active = MySQLQuery.getAsBoolean(row[11]);
        observ = MySQLQuery.getAsString(row[12]);
        visible = MySQLQuery.getAsBoolean(row[13]);
        str1 = MySQLQuery.getAsString(row[14]);
        str2 = MySQLQuery.getAsString(row[15]);
        str3 = MySQLQuery.getAsString(row[16]);
        color = MySQLQuery.getAsString(row[17]);
        registerDate = MySQLQuery.getAsDate(row[18]);
        eqsCenterId = MySQLQuery.getAsInteger(row[19]);
        eqsLocationId = MySQLQuery.getAsInteger(row[20]);
        eqsInternal = MySQLQuery.getAsString(row[21]);
        eqsAccCode = MySQLQuery.getAsInteger(row[22]);
        cache = MySQLQuery.getAsString(row[23]);
        hasServiceManager = MySQLQuery.getAsBoolean(row[24]);
        zoneType = MySQLQuery.getAsString(row[25]);
        prevMto = MySQLQuery.getAsBoolean(row[26]);
        kmSrc = MySQLQuery.getAsString(row[27]);
        hrSrc = MySQLQuery.getAsBoolean(row[28]);
        mtoType = MySQLQuery.getAsString(row[29]);
        codGps = MySQLQuery.getAsString(row[30]);
        vhTreadId = MySQLQuery.getAsInteger(row[31]);
        isTank = MySQLQuery.getAsBoolean(row[32]);
        galCap = MySQLQuery.getAsInteger(row[33]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "vehicle";
    }

    public static String getSelFlds(String alias) {
        return new Vehicle().getSelFldsForAlias(alias);
    }

    public static List<Vehicle> getList(MySQLQuery q, Connection conn) throws Exception {
        return new Vehicle().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new Vehicle().deleteById(id, conn);
    }

    public static List<Vehicle> getAll(Connection conn) throws Exception {
        return new Vehicle().getAllList(conn);
    }

//fin zona de reemplazo
}