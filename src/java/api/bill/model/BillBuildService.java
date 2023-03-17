package api.bill.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.math.BigDecimal;

public class BillBuildService extends BaseModel<BillBuildService> {
//inicio zona de reemplazo

    public Integer typeId;
    public int billBuildingId;
    public int payments;
    public BigDecimal total;
    public String description;
    public int billSpanId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "type_id",
            "bill_building_id",
            "payments",
            "total",
            "description",
            "bill_span_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, typeId);
        q.setParam(2, billBuildingId);
        q.setParam(3, payments);
        q.setParam(4, total);
        q.setParam(5, description);
        q.setParam(6, billSpanId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        typeId = MySQLQuery.getAsInteger(row[0]);
        billBuildingId = MySQLQuery.getAsInteger(row[1]);
        payments = MySQLQuery.getAsInteger(row[2]);
        total = MySQLQuery.getAsBigDecimal(row[3], false);
        description = MySQLQuery.getAsString(row[4]);
        billSpanId = MySQLQuery.getAsInteger(row[5]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_build_service";
    }

    public static String getSelFlds(String alias) {
        return new BillBuildService().getSelFldsForAlias(alias);
    }

    public static List<BillBuildService> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillBuildService().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillBuildService().deleteById(id, conn);
    }

    public static List<BillBuildService> getAll(Connection conn) throws Exception {
        return new BillBuildService().getAllList(conn);
    }

//fin zona de reemplazo
    public boolean isEditable(BillInstance inst, Connection conn) throws Exception {
        int recaId = BillSpan.getByBuilding("reca", billBuildingId, inst, conn).id;
        return recaId < billSpanId;
    }
}
