package api.per.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.math.BigDecimal;
import java.util.Date;

public class PerContractHist extends BaseModel<PerContractHist> {
//inicio zona de reemplazo

    public int contractId;
    public Date changeDate;
    public Integer cityId;
    public Integer enterpriseId;
    public Integer posId;
    public Integer payTypeId;
    public BigDecimal payValue;
    public Date beginDate;
    public Date endDate;
    public String notes;
    public Integer officeId;
    public boolean active;
    public Integer employeerId;
    public Integer chiefId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "contract_id",
            "change_date",
            "city_id",
            "enterprise_id",
            "pos_id",
            "pay_type_id",
            "pay_value",
            "begin_date",
            "end_date",
            "notes",
            "office_id",
            "active",
            "employeer_id",
            "chief_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, contractId);
        q.setParam(2, changeDate);
        q.setParam(3, cityId);
        q.setParam(4, enterpriseId);
        q.setParam(5, posId);
        q.setParam(6, payTypeId);
        q.setParam(7, payValue);
        q.setParam(8, beginDate);
        q.setParam(9, endDate);
        q.setParam(10, notes);
        q.setParam(11, officeId);
        q.setParam(12, active);
        q.setParam(13, employeerId);
        q.setParam(14, chiefId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        contractId = MySQLQuery.getAsInteger(row[0]);
        changeDate = MySQLQuery.getAsDate(row[1]);
        cityId = MySQLQuery.getAsInteger(row[2]);
        enterpriseId = MySQLQuery.getAsInteger(row[3]);
        posId = MySQLQuery.getAsInteger(row[4]);
        payTypeId = MySQLQuery.getAsInteger(row[5]);
        payValue = MySQLQuery.getAsBigDecimal(row[6], false);
        beginDate = MySQLQuery.getAsDate(row[7]);
        endDate = MySQLQuery.getAsDate(row[8]);
        notes = MySQLQuery.getAsString(row[9]);
        officeId = MySQLQuery.getAsInteger(row[10]);
        active = MySQLQuery.getAsBoolean(row[11]);
        employeerId = MySQLQuery.getAsInteger(row[12]);
        chiefId = MySQLQuery.getAsInteger(row[13]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "per_contract_hist";
    }

    public static String getSelFlds(String alias) {
        return new PerContractHist().getSelFldsForAlias(alias);
    }

    public static List<PerContractHist> getList(MySQLQuery q, Connection conn) throws Exception {
        return new PerContractHist().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new PerContractHist().deleteById(id, conn);
    }

    public static List<PerContractHist> getAll(Connection conn) throws Exception {
        return new PerContractHist().getAllList(conn);
    }

//fin zona de reemplazo
}