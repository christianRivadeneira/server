package api.bill.model;

import api.BaseModel;
import api.Params;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class BillCfg extends BaseModel<BillCfg> {
//inicio zona de reemplazo

    public String nit;
    public String codEnt;
    public String fssri;
    public Integer inspectionInterval;
    public Integer inspectionAlert;
    public boolean tracking;
    public boolean suspRecon;
    public BigDecimal suspValue;
    public String fac1;
    public String fac2;
    public String fac3;
    public String fac4;
    public String fac5;
    public String billWriterClass;
    public boolean mandatoryDocument;
    public boolean showKg;
    public boolean inteTypes;
    public boolean showTicket;
    public String legend1;
    public String legend2;
    public String legend3;
    public String legend4;
    public String legend5;
    public String legend6;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "nit",
            "cod_ent",
            "fssri",
            "inspection_interval",
            "inspection_alert",
            "tracking",
            "susp_recon",
            "susp_value",
            "fac1",
            "fac2",
            "fac3",
            "fac4",
            "fac5",
            "bill_writer_class",
            "mandatory_document",
            "show_kg",
            "inte_types",
            "show_ticket",
            "legend_1",
            "legend_2",
            "legend_3",
            "legend_4",
            "legend_5",
            "legend_6"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, nit);
        q.setParam(2, codEnt);
        q.setParam(3, fssri);
        q.setParam(4, inspectionInterval);
        q.setParam(5, inspectionAlert);
        q.setParam(6, tracking);
        q.setParam(7, suspRecon);
        q.setParam(8, suspValue);
        q.setParam(9, fac1);
        q.setParam(10, fac2);
        q.setParam(11, fac3);
        q.setParam(12, fac4);
        q.setParam(13, fac5);
        q.setParam(14, billWriterClass);
        q.setParam(15, mandatoryDocument);
        q.setParam(16, showKg);
        q.setParam(17, inteTypes);
        q.setParam(18, showTicket);
        q.setParam(19, legend1);
        q.setParam(20, legend2);
        q.setParam(21, legend3);
        q.setParam(22, legend4);
        q.setParam(23, legend5);
        q.setParam(24, legend6);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        nit = MySQLQuery.getAsString(row[0]);
        codEnt = MySQLQuery.getAsString(row[1]);
        fssri = MySQLQuery.getAsString(row[2]);
        inspectionInterval = MySQLQuery.getAsInteger(row[3]);
        inspectionAlert = MySQLQuery.getAsInteger(row[4]);
        tracking = MySQLQuery.getAsBoolean(row[5]);
        suspRecon = MySQLQuery.getAsBoolean(row[6]);
        suspValue = MySQLQuery.getAsBigDecimal(row[7], false);
        fac1 = MySQLQuery.getAsString(row[8]);
        fac2 = MySQLQuery.getAsString(row[9]);
        fac3 = MySQLQuery.getAsString(row[10]);
        fac4 = MySQLQuery.getAsString(row[11]);
        fac5 = MySQLQuery.getAsString(row[12]);
        billWriterClass = MySQLQuery.getAsString(row[13]);
        mandatoryDocument = MySQLQuery.getAsBoolean(row[14]);
        showKg = MySQLQuery.getAsBoolean(row[15]);
        inteTypes = MySQLQuery.getAsBoolean(row[16]);
        showTicket = MySQLQuery.getAsBoolean(row[17]);
        legend1 = MySQLQuery.getAsString(row[18]);
        legend2 = MySQLQuery.getAsString(row[19]);
        legend3 = MySQLQuery.getAsString(row[20]);
        legend4 = MySQLQuery.getAsString(row[21]);
        legend5 = MySQLQuery.getAsString(row[22]);
        legend6 = MySQLQuery.getAsString(row[23]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_cfg";
    }

    public static String getSelFlds(String alias) {
        return new BillCfg().getSelFldsForAlias(alias);
    }

    public static List<BillCfg> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillCfg().getListFromQuery(q, conn);
    }

    public static List<BillCfg> getList(Params p, Connection conn) throws Exception {
        return new BillCfg().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillCfg().deleteById(id, conn);
    }

    public static List<BillCfg> getAll(Connection conn) throws Exception {
        return new BillCfg().getAllList(conn);
    }

//fin zona de reemplazo
}
