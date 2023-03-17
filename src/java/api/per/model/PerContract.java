package api.per.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.math.BigDecimal;
import java.util.Date;

public class PerContract extends BaseModel<PerContract> {
//inicio zona de reemplazo

    public Date begDate;
    public Date endDate;
    public Integer hireMotId;
    public Integer fireMotId;
    public int employeerId;
    public boolean homeAssist;
    public boolean vehAssist;
    public boolean transAssist;
    public boolean commission;
    public boolean bonus;
    public boolean beginRot;
    public boolean endRot;
    public Date clearDate;
    public Date workOutDate;
    public Date paymentDate;
    public Date leaveDate;
    public boolean travel;
    public boolean policy;
    public int cityId;
    public int enterpriseId;
    public int empId;
    public int posId;
    public Integer payTypeId;
    public BigDecimal payValue;
    public String notes;
    public Date trialEnd;
    public Date trialReinduc;
    public boolean trialEnded;
    public boolean active;
    public boolean last;
    public Integer officeId;
    public Integer chiefId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "beg_date",
            "end_date",
            "hire_mot_id",
            "fire_mot_id",
            "employeer_id",
            "home_assist",
            "veh_assist",
            "trans_assist",
            "commission",
            "bonus",
            "begin_rot",
            "end_rot",
            "clear_date",
            "work_out_date",
            "payment_date",
            "leave_date",
            "travel",
            "policy",
            "city_id",
            "enterprise_id",
            "emp_id",
            "pos_id",
            "pay_type_id",
            "pay_value",
            "notes",
            "trial_end",
            "trial_reinduc",
            "trial_ended",
            "active",
            "last",
            "office_id",
            "chief_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, begDate);
        q.setParam(2, endDate);
        q.setParam(3, hireMotId);
        q.setParam(4, fireMotId);
        q.setParam(5, employeerId);
        q.setParam(6, homeAssist);
        q.setParam(7, vehAssist);
        q.setParam(8, transAssist);
        q.setParam(9, commission);
        q.setParam(10, bonus);
        q.setParam(11, beginRot);
        q.setParam(12, endRot);
        q.setParam(13, clearDate);
        q.setParam(14, workOutDate);
        q.setParam(15, paymentDate);
        q.setParam(16, leaveDate);
        q.setParam(17, travel);
        q.setParam(18, policy);
        q.setParam(19, cityId);
        q.setParam(20, enterpriseId);
        q.setParam(21, empId);
        q.setParam(22, posId);
        q.setParam(23, payTypeId);
        q.setParam(24, payValue);
        q.setParam(25, notes);
        q.setParam(26, trialEnd);
        q.setParam(27, trialReinduc);
        q.setParam(28, trialEnded);
        q.setParam(29, active);
        q.setParam(30, last);
        q.setParam(31, officeId);
        q.setParam(32, chiefId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        begDate = MySQLQuery.getAsDate(row[0]);
        endDate = MySQLQuery.getAsDate(row[1]);
        hireMotId = MySQLQuery.getAsInteger(row[2]);
        fireMotId = MySQLQuery.getAsInteger(row[3]);
        employeerId = MySQLQuery.getAsInteger(row[4]);
        homeAssist = MySQLQuery.getAsBoolean(row[5]);
        vehAssist = MySQLQuery.getAsBoolean(row[6]);
        transAssist = MySQLQuery.getAsBoolean(row[7]);
        commission = MySQLQuery.getAsBoolean(row[8]);
        bonus = MySQLQuery.getAsBoolean(row[9]);
        beginRot = MySQLQuery.getAsBoolean(row[10]);
        endRot = MySQLQuery.getAsBoolean(row[11]);
        clearDate = MySQLQuery.getAsDate(row[12]);
        workOutDate = MySQLQuery.getAsDate(row[13]);
        paymentDate = MySQLQuery.getAsDate(row[14]);
        leaveDate = MySQLQuery.getAsDate(row[15]);
        travel = MySQLQuery.getAsBoolean(row[16]);
        policy = MySQLQuery.getAsBoolean(row[17]);
        cityId = MySQLQuery.getAsInteger(row[18]);
        enterpriseId = MySQLQuery.getAsInteger(row[19]);
        empId = MySQLQuery.getAsInteger(row[20]);
        posId = MySQLQuery.getAsInteger(row[21]);
        payTypeId = MySQLQuery.getAsInteger(row[22]);
        payValue = MySQLQuery.getAsBigDecimal(row[23], false);
        notes = MySQLQuery.getAsString(row[24]);
        trialEnd = MySQLQuery.getAsDate(row[25]);
        trialReinduc = MySQLQuery.getAsDate(row[26]);
        trialEnded = MySQLQuery.getAsBoolean(row[27]);
        active = MySQLQuery.getAsBoolean(row[28]);
        last = MySQLQuery.getAsBoolean(row[29]);
        officeId = MySQLQuery.getAsInteger(row[30]);
        chiefId = MySQLQuery.getAsInteger(row[31]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "per_contract";
    }

    public static String getSelFlds(String alias) {
        return new PerContract().getSelFldsForAlias(alias);
    }

    public static List<PerContract> getList(MySQLQuery q, Connection conn) throws Exception {
        return new PerContract().getListFromQuery(q, conn);
    }

//fin zona de reemplazo

}