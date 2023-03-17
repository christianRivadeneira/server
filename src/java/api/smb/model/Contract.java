package api.smb.model;

import api.BaseModel;
import api.Params;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class Contract extends BaseModel<Contract> {
//inicio zona de reemplazo

    public String contractNum;
    public String address;
    public String delivAddress;
    public Integer delivNeighId;
    public String phones;
    public Boolean own;
    public Date signDate;
    public Date cancelDate;
    public Integer sower;
    public Integer writer;
    public Integer anullCauseId;
    public Integer cancelCauseId;
    public Integer establishId;
    public Integer energyId;
    public Integer vehicleId;
    public Integer neighId;
    public Integer people;
    public Integer clientId;
    public Integer invStoreId;
    public Integer exportContractId;
    public boolean checked;
    public String notes;
    public Date created;
    public Date modified;
    public Integer creaUsuId;
    public Integer modUsuId;
    public String delivPhones;
    public Date modDeliv;
    public Integer orderAvg;
    public String ctrType;
    public String estName;
    public BigDecimal deposit;
    public Date comoBegDate;
    public Date comoEndDate;
    public Date comoCollectDate;
    public int pendDocs;
    public String document;
    public String firstName;
    public String lastName;
    public String cliType;
    public String email;
    public boolean pref;
    public boolean banned;
    public String sucursal;
    public Integer smbCiiuId;
    public String cliLipre;
    public String cliLides;
    public Boolean greatContributor;
    public Boolean liqTax;
    public Boolean liqReten;
    public String qualification;
    public String bblClassCliCode;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "contract_num",
            "address",
            "deliv_address",
            "deliv_neigh_id",
            "phones",
            "own",
            "sign_date",
            "cancel_date",
            "sower",
            "writer",
            "anull_cause_id",
            "cancel_cause_id",
            "establish_id",
            "energy_id",
            "vehicle_id",
            "neigh_id",
            "people",
            "client_id",
            "inv_store_id",
            "export_contract_id",
            "checked",
            "notes",
            "created",
            "modified",
            "crea_usu_id",
            "mod_usu_id",
            "deliv_phones",
            "mod_deliv",
            "order_avg",
            "ctr_type",
            "est_name",
            "deposit",
            "como_beg_date",
            "como_end_date",
            "como_collect_date",
            "pend_docs",
            "document",
            "first_name",
            "last_name",
            "cli_type",
            "email",
            "pref",
            "banned",
            "sucursal",
            "smb_ciiu_id",
            "cli_lipre",
            "cli_lides",
            "great_contributor",
            "liq_tax",
            "liq_reten",
            "qualification",
            "bbl_class_cli_code"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, contractNum);
        q.setParam(2, address);
        q.setParam(3, delivAddress);
        q.setParam(4, delivNeighId);
        q.setParam(5, phones);
        q.setParam(6, own);
        q.setParam(7, signDate);
        q.setParam(8, cancelDate);
        q.setParam(9, sower);
        q.setParam(10, writer);
        q.setParam(11, anullCauseId);
        q.setParam(12, cancelCauseId);
        q.setParam(13, establishId);
        q.setParam(14, energyId);
        q.setParam(15, vehicleId);
        q.setParam(16, neighId);
        q.setParam(17, people);
        q.setParam(18, clientId);
        q.setParam(19, invStoreId);
        q.setParam(20, exportContractId);
        q.setParam(21, checked);
        q.setParam(22, notes);
        q.setParam(23, created);
        q.setParam(24, modified);
        q.setParam(25, creaUsuId);
        q.setParam(26, modUsuId);
        q.setParam(27, delivPhones);
        q.setParam(28, modDeliv);
        q.setParam(29, orderAvg);
        q.setParam(30, ctrType);
        q.setParam(31, estName);
        q.setParam(32, deposit);
        q.setParam(33, comoBegDate);
        q.setParam(34, comoEndDate);
        q.setParam(35, comoCollectDate);
        q.setParam(36, pendDocs);
        q.setParam(37, document);
        q.setParam(38, firstName);
        q.setParam(39, lastName);
        q.setParam(40, cliType);
        q.setParam(41, email);
        q.setParam(42, pref);
        q.setParam(43, banned);
        q.setParam(44, sucursal);
        q.setParam(45, smbCiiuId);
        q.setParam(46, cliLipre);
        q.setParam(47, cliLides);
        q.setParam(48, greatContributor);
        q.setParam(49, liqTax);
        q.setParam(50, liqReten);
        q.setParam(51, qualification);
        q.setParam(52, bblClassCliCode);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        contractNum = MySQLQuery.getAsString(row[0]);
        address = MySQLQuery.getAsString(row[1]);
        delivAddress = MySQLQuery.getAsString(row[2]);
        delivNeighId = MySQLQuery.getAsInteger(row[3]);
        phones = MySQLQuery.getAsString(row[4]);
        own = MySQLQuery.getAsBoolean(row[5]);
        signDate = MySQLQuery.getAsDate(row[6]);
        cancelDate = MySQLQuery.getAsDate(row[7]);
        sower = MySQLQuery.getAsInteger(row[8]);
        writer = MySQLQuery.getAsInteger(row[9]);
        anullCauseId = MySQLQuery.getAsInteger(row[10]);
        cancelCauseId = MySQLQuery.getAsInteger(row[11]);
        establishId = MySQLQuery.getAsInteger(row[12]);
        energyId = MySQLQuery.getAsInteger(row[13]);
        vehicleId = MySQLQuery.getAsInteger(row[14]);
        neighId = MySQLQuery.getAsInteger(row[15]);
        people = MySQLQuery.getAsInteger(row[16]);
        clientId = MySQLQuery.getAsInteger(row[17]);
        invStoreId = MySQLQuery.getAsInteger(row[18]);
        exportContractId = MySQLQuery.getAsInteger(row[19]);
        checked = MySQLQuery.getAsBoolean(row[20]);
        notes = MySQLQuery.getAsString(row[21]);
        created = MySQLQuery.getAsDate(row[22]);
        modified = MySQLQuery.getAsDate(row[23]);
        creaUsuId = MySQLQuery.getAsInteger(row[24]);
        modUsuId = MySQLQuery.getAsInteger(row[25]);
        delivPhones = MySQLQuery.getAsString(row[26]);
        modDeliv = MySQLQuery.getAsDate(row[27]);
        orderAvg = MySQLQuery.getAsInteger(row[28]);
        ctrType = MySQLQuery.getAsString(row[29]);
        estName = MySQLQuery.getAsString(row[30]);
        deposit = MySQLQuery.getAsBigDecimal(row[31], false);
        comoBegDate = MySQLQuery.getAsDate(row[32]);
        comoEndDate = MySQLQuery.getAsDate(row[33]);
        comoCollectDate = MySQLQuery.getAsDate(row[34]);
        pendDocs = MySQLQuery.getAsInteger(row[35]);
        document = MySQLQuery.getAsString(row[36]);
        firstName = MySQLQuery.getAsString(row[37]);
        lastName = MySQLQuery.getAsString(row[38]);
        cliType = MySQLQuery.getAsString(row[39]);
        email = MySQLQuery.getAsString(row[40]);
        pref = MySQLQuery.getAsBoolean(row[41]);
        banned = MySQLQuery.getAsBoolean(row[42]);
        sucursal = MySQLQuery.getAsString(row[43]);
        smbCiiuId = MySQLQuery.getAsInteger(row[44]);
        cliLipre = MySQLQuery.getAsString(row[45]);
        cliLides = MySQLQuery.getAsString(row[46]);
        greatContributor = MySQLQuery.getAsBoolean(row[47]);
        liqTax = MySQLQuery.getAsBoolean(row[48]);
        liqReten = MySQLQuery.getAsBoolean(row[49]);
        qualification = MySQLQuery.getAsString(row[50]);
        bblClassCliCode = MySQLQuery.getAsString(row[51]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "contract";
    }

    public static String getSelFlds(String alias) {
        return new Contract().getSelFldsForAlias(alias);
    }

    public static List<Contract> getList(MySQLQuery q, Connection conn) throws Exception {
        return new Contract().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new Contract().deleteById(id, conn);
    }

    public static List<Contract> getAll(Connection conn) throws Exception {
        return new Contract().getAllList(conn);
    }

//fin zona de reemplazo
    public static Contract getContractByNum(String num, Connection conn) throws Exception {
        Params p = new Params();
        p.param("contract_num", num);
        p.param("ctr_type", "afil");
        return new Contract().select(p, conn);
    }

}
