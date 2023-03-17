package api.per.model;

import api.BaseModel;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class PerInsClaim extends BaseModel<PerInsClaim> {
//inicio zona de reemplazo

    public Integer epsId;
    public Integer arlId;
    public String state;
    public String rad;
    public BigDecimal vlrRad;
    public BigDecimal vlrAprob;
    public BigDecimal vlrPend;
    public Integer sickId;
    public Integer accId;
    public Integer conHisId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "eps_id",
            "arl_id",
            "state",
            "rad",
            "vlr_rad",
            "vlr_aprob",
            "vlr_pend",
            "sick_id",
            "acc_id",
            "con_his_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, epsId);
        q.setParam(2, arlId);
        q.setParam(3, state);
        q.setParam(4, rad);
        q.setParam(5, vlrRad);
        q.setParam(6, vlrAprob);
        q.setParam(7, vlrPend);
        q.setParam(8, sickId);
        q.setParam(9, accId);
        q.setParam(10, conHisId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        epsId = MySQLQuery.getAsInteger(row[0]);
        arlId = MySQLQuery.getAsInteger(row[1]);
        state = MySQLQuery.getAsString(row[2]);
        rad = MySQLQuery.getAsString(row[3]);
        vlrRad = MySQLQuery.getAsBigDecimal(row[4], false);
        vlrAprob = MySQLQuery.getAsBigDecimal(row[5], false);
        vlrPend = MySQLQuery.getAsBigDecimal(row[6], false);
        sickId = MySQLQuery.getAsInteger(row[7]);
        accId = MySQLQuery.getAsInteger(row[8]);
        conHisId = MySQLQuery.getAsInteger(row[9]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "per_ins_claim";
    }

    public static String getSelFlds(String alias) {
        return new PerInsClaim().getSelFldsForAlias(alias);
    }

    public static List<PerInsClaim> getList(MySQLQuery q, Connection conn) throws Exception {
        return new PerInsClaim().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new PerInsClaim().deleteById(id, conn);
    }

    public static List<PerInsClaim> getAll(Connection conn) throws Exception {
        return new PerInsClaim().getAllList(conn);
    }

//fin zona de reemplazo

}
