package api.per.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class PerFlowInsClaim extends BaseModel<PerFlowInsClaim> {
//inicio zona de reemplazo

    public int insClaimId;
    public Date regDate;
    public String state;
    public String notes;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "ins_claim_id",
            "reg_date",
            "state",
            "notes"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, insClaimId);
        q.setParam(2, regDate);
        q.setParam(3, state);
        q.setParam(4, notes);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        insClaimId = MySQLQuery.getAsInteger(row[0]);
        regDate = MySQLQuery.getAsDate(row[1]);
        state = MySQLQuery.getAsString(row[2]);
        notes = MySQLQuery.getAsString(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "per_flow_ins_claim";
    }

    public static String getSelFlds(String alias) {
        return new PerFlowInsClaim().getSelFldsForAlias(alias);
    }

    public static List<PerFlowInsClaim> getList(MySQLQuery q, Connection conn) throws Exception {
        return new PerFlowInsClaim().getListFromQuery(q, conn);
    }

//fin zona de reemplazo

}