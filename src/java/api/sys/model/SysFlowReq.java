package api.sys.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class SysFlowReq extends BaseModel<SysFlowReq> {
//inicio zona de reemplazo

    public Date creaDate;
    public Date begDate;
    public Date endDate;
    public String endType;
    public int employeeId;
    public Integer perEmpId;
    public Integer perOfficeId;
    public Integer perAreaId;
    public Integer perSareaId;
    public Integer typeId;
    public Integer curChkId;
    public String notes;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "crea_date",
            "beg_date",
            "end_date",
            "end_type",
            "employee_id",
            "per_emp_id",
            "per_office_id",
            "per_area_id",
            "per_sarea_id",
            "type_id",
            "cur_chk_id",
            "notes"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, creaDate);
        q.setParam(2, begDate);
        q.setParam(3, endDate);
        q.setParam(4, endType);
        q.setParam(5, employeeId);
        q.setParam(6, perEmpId);
        q.setParam(7, perOfficeId);
        q.setParam(8, perAreaId);
        q.setParam(9, perSareaId);
        q.setParam(10, typeId);
        q.setParam(11, curChkId);
        q.setParam(12, notes);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        creaDate = MySQLQuery.getAsDate(row[0]);
        begDate = MySQLQuery.getAsDate(row[1]);
        endDate = MySQLQuery.getAsDate(row[2]);
        endType = MySQLQuery.getAsString(row[3]);
        employeeId = MySQLQuery.getAsInteger(row[4]);
        perEmpId = MySQLQuery.getAsInteger(row[5]);
        perOfficeId = MySQLQuery.getAsInteger(row[6]);
        perAreaId = MySQLQuery.getAsInteger(row[7]);
        perSareaId = MySQLQuery.getAsInteger(row[8]);
        typeId = MySQLQuery.getAsInteger(row[9]);
        curChkId = MySQLQuery.getAsInteger(row[10]);
        notes = MySQLQuery.getAsString(row[11]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "sys_flow_req";
    }

    public static String getSelFlds(String alias) {
        return new SysFlowReq().getSelFldsForAlias(alias);
    }

    public static List<SysFlowReq> getList(MySQLQuery q, Connection conn) throws Exception {
        return new SysFlowReq().getListFromQuery(q, conn);
    }

//fin zona de reemplazo
   
}
