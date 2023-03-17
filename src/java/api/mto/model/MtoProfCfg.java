package api.mto.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class MtoProfCfg extends BaseModel<MtoProfCfg> {

public static final int MODULE_ID = 145;
//inicio zona de reemplazo

    public int profId;
    public boolean allCities;
    public boolean anyDate;
    public boolean orders;
    public boolean onlyWorkOrder;
    public boolean fuels;
    public boolean freeContractors;
    public boolean vehicleDtGral;
    public boolean vehicleDocs;
    public boolean driverDocs;
    public boolean assignDrivers;
    public boolean assignAgency;
    public boolean changeCodAcc;
    public boolean createNovs;
    public boolean changeInternalVh;
    public boolean assignContractor;
    public boolean printOrder;
    public boolean vehiclePhoto;
    public boolean vehicleDtFormat;
    public boolean pnlSearch;
    public boolean pnlFleet;
    public boolean pnlFormats;
    public boolean pnlTracing;
    public boolean pnlMto;
    public boolean pnlAlerts;
    public boolean historyWithValues;
    public boolean pnlChkOrders;
    public boolean isSuperAdmin;
    public boolean appInspec;
    public boolean appWork;
    public boolean appDriver;
    public boolean editVehicle;
    public boolean workElement;
    public boolean appViewAllTrips;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "prof_id",
            "all_cities",
            "any_date",
            "orders",
            "only_work_order",
            "fuels",
            "free_contractors",
            "vehicle_dt_gral",
            "vehicle_docs",
            "driver_docs",
            "assign_drivers",
            "assign_agency",
            "change_cod_acc",
            "create_novs",
            "change_internal_vh",
            "assign_contractor",
            "print_order",
            "vehicle_photo",
            "vehicle_dt_format",
            "pnl_search",
            "pnl_fleet",
            "pnl_formats",
            "pnl_tracing",
            "pnl_mto",
            "pnl_alerts",
            "history_with_values",
            "pnl_chk_orders",
            "is_super_admin",
            "app_inspec",
            "app_work",
            "app_driver",
            "edit_vehicle",
            "work_element",
            "app_view_all_trips"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, profId);
        q.setParam(2, allCities);
        q.setParam(3, anyDate);
        q.setParam(4, orders);
        q.setParam(5, onlyWorkOrder);
        q.setParam(6, fuels);
        q.setParam(7, freeContractors);
        q.setParam(8, vehicleDtGral);
        q.setParam(9, vehicleDocs);
        q.setParam(10, driverDocs);
        q.setParam(11, assignDrivers);
        q.setParam(12, assignAgency);
        q.setParam(13, changeCodAcc);
        q.setParam(14, createNovs);
        q.setParam(15, changeInternalVh);
        q.setParam(16, assignContractor);
        q.setParam(17, printOrder);
        q.setParam(18, vehiclePhoto);
        q.setParam(19, vehicleDtFormat);
        q.setParam(20, pnlSearch);
        q.setParam(21, pnlFleet);
        q.setParam(22, pnlFormats);
        q.setParam(23, pnlTracing);
        q.setParam(24, pnlMto);
        q.setParam(25, pnlAlerts);
        q.setParam(26, historyWithValues);
        q.setParam(27, pnlChkOrders);
        q.setParam(28, isSuperAdmin);
        q.setParam(29, appInspec);
        q.setParam(30, appWork);
        q.setParam(31, appDriver);
        q.setParam(32, editVehicle);
        q.setParam(33, workElement);
        q.setParam(34, appViewAllTrips);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        profId = MySQLQuery.getAsInteger(row[0]);
        allCities = MySQLQuery.getAsBoolean(row[1]);
        anyDate = MySQLQuery.getAsBoolean(row[2]);
        orders = MySQLQuery.getAsBoolean(row[3]);
        onlyWorkOrder = MySQLQuery.getAsBoolean(row[4]);
        fuels = MySQLQuery.getAsBoolean(row[5]);
        freeContractors = MySQLQuery.getAsBoolean(row[6]);
        vehicleDtGral = MySQLQuery.getAsBoolean(row[7]);
        vehicleDocs = MySQLQuery.getAsBoolean(row[8]);
        driverDocs = MySQLQuery.getAsBoolean(row[9]);
        assignDrivers = MySQLQuery.getAsBoolean(row[10]);
        assignAgency = MySQLQuery.getAsBoolean(row[11]);
        changeCodAcc = MySQLQuery.getAsBoolean(row[12]);
        createNovs = MySQLQuery.getAsBoolean(row[13]);
        changeInternalVh = MySQLQuery.getAsBoolean(row[14]);
        assignContractor = MySQLQuery.getAsBoolean(row[15]);
        printOrder = MySQLQuery.getAsBoolean(row[16]);
        vehiclePhoto = MySQLQuery.getAsBoolean(row[17]);
        vehicleDtFormat = MySQLQuery.getAsBoolean(row[18]);
        pnlSearch = MySQLQuery.getAsBoolean(row[19]);
        pnlFleet = MySQLQuery.getAsBoolean(row[20]);
        pnlFormats = MySQLQuery.getAsBoolean(row[21]);
        pnlTracing = MySQLQuery.getAsBoolean(row[22]);
        pnlMto = MySQLQuery.getAsBoolean(row[23]);
        pnlAlerts = MySQLQuery.getAsBoolean(row[24]);
        historyWithValues = MySQLQuery.getAsBoolean(row[25]);
        pnlChkOrders = MySQLQuery.getAsBoolean(row[26]);
        isSuperAdmin = MySQLQuery.getAsBoolean(row[27]);
        appInspec = MySQLQuery.getAsBoolean(row[28]);
        appWork = MySQLQuery.getAsBoolean(row[29]);
        appDriver = MySQLQuery.getAsBoolean(row[30]);
        editVehicle = MySQLQuery.getAsBoolean(row[31]);
        workElement = MySQLQuery.getAsBoolean(row[32]);
        appViewAllTrips = MySQLQuery.getAsBoolean(row[33]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mto_prof_cfg";
    }

    public static String getSelFlds(String alias) {
        return new MtoProfCfg().getSelFldsForAlias(alias);
    }

    public static List<MtoProfCfg> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MtoProfCfg().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MtoProfCfg().deleteById(id, conn);
    }

    public static List<MtoProfCfg> getAll(Connection conn) throws Exception {
        return new MtoProfCfg().getAllList(conn);
    }

//fin zona de reemplazo
}