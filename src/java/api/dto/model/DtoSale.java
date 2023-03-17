package api.dto.model;

import api.BaseModel;
import java.math.BigInteger;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class DtoSale extends BaseModel<DtoSale> {
//inicio zona de reemplazo

    public Date dt;
    public int clieDoc;
    public Integer centerId;
    public Integer stratum;
    public Integer valueTotal;
    public Integer subsidy;
    public String nif;
    public Integer cylTypeId;
    public Integer salesmanId;
    public Integer dtoLiqId;
    public Integer causalId;
    public String cauNotes;
    public String importNotes;
    public String state;
    public String authNotes;
    public Integer origCapa;
    public Integer aprovNumber;
    public BigInteger bill;
    public String notes;
    public Date skipCauDate;
    public Integer importId;
    public Integer trkSaleId;
    public Date hideDt;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "dt",
            "clie_doc",
            "center_id",
            "stratum",
            "value_total",
            "subsidy",
            "nif",
            "cyl_type_id",
            "salesman_id",
            "dto_liq_id",
            "causal_id",
            "cau_notes",
            "import_notes",
            "state",
            "auth_notes",
            "orig_capa",
            "aprov_number",
            "bill",
            "notes",
            "skip_cau_date",
            "import_id",
            "trk_sale_id",
            "hide_dt"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, dt);
        q.setParam(2, clieDoc);
        q.setParam(3, centerId);
        q.setParam(4, stratum);
        q.setParam(5, valueTotal);
        q.setParam(6, subsidy);
        q.setParam(7, nif);
        q.setParam(8, cylTypeId);
        q.setParam(9, salesmanId);
        q.setParam(10, dtoLiqId);
        q.setParam(11, causalId);
        q.setParam(12, cauNotes);
        q.setParam(13, importNotes);
        q.setParam(14, state);
        q.setParam(15, authNotes);
        q.setParam(16, origCapa);
        q.setParam(17, aprovNumber);
        q.setParam(18, bill);
        q.setParam(19, notes);
        q.setParam(20, skipCauDate);
        q.setParam(21, importId);
        q.setParam(22, trkSaleId);
        q.setParam(23, hideDt);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        dt = MySQLQuery.getAsDate(row[0]);
        clieDoc = MySQLQuery.getAsInteger(row[1]);
        centerId = MySQLQuery.getAsInteger(row[2]);
        stratum = MySQLQuery.getAsInteger(row[3]);
        valueTotal = MySQLQuery.getAsInteger(row[4]);
        subsidy = MySQLQuery.getAsInteger(row[5]);
        nif = MySQLQuery.getAsString(row[6]);
        cylTypeId = MySQLQuery.getAsInteger(row[7]);
        salesmanId = MySQLQuery.getAsInteger(row[8]);
        dtoLiqId = MySQLQuery.getAsInteger(row[9]);
        causalId = MySQLQuery.getAsInteger(row[10]);
        cauNotes = MySQLQuery.getAsString(row[11]);
        importNotes = MySQLQuery.getAsString(row[12]);
        state = MySQLQuery.getAsString(row[13]);
        authNotes = MySQLQuery.getAsString(row[14]);
        origCapa = MySQLQuery.getAsInteger(row[15]);
        aprovNumber = MySQLQuery.getAsInteger(row[16]);
        bill = MySQLQuery.getAsBigInteger(row[17]);
        notes = MySQLQuery.getAsString(row[18]);
        skipCauDate = MySQLQuery.getAsDate(row[19]);
        importId = MySQLQuery.getAsInteger(row[20]);
        trkSaleId = MySQLQuery.getAsInteger(row[21]);
        hideDt = MySQLQuery.getAsDate(row[22]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "dto_sale";
    }

    public static String getSelFlds(String alias) {
        return new DtoSale().getSelFldsForAlias(alias);
    }

    public static List<DtoSale> getList(MySQLQuery q, Connection conn) throws Exception {
        return new DtoSale().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new DtoSale().deleteById(id, conn);
    }

    public static List<DtoSale> getAll(Connection conn) throws Exception {
        return new DtoSale().getAllList(conn);
    }

//fin zona de reemplazo

}
