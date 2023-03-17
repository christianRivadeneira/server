package api.hlp.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class HlpRequest extends BaseModel<HlpRequest> {
//inicio zona de reemplazo

    public Integer employeeId;
    public Integer prjBacklogId;
    public Integer perThirdId;
    public Integer crmCliId;
    public Integer createdBy;
    public int typeId;
    public Date regDate;
    public Date closeDate;
    public Date begDate;
    public Date endDate;
    public Date restartDate;
    public Date cutDate;
    public Date expirateDate;
    public Integer totalTime;
    public Integer deadTime;
    public String duration;
    public Integer inCharge;
    public Boolean active;
    public Integer originId;
    public String state;
    public String userType;
    public String notes;
    public String subject;
    public String priority;
    public Integer equipId;
    public String solution;
    public String typeHlpDesk;
    public String typingDetail;
    public Integer serviceId;
    public Integer catOneId;
    public Integer catTwoId;
    public Integer solutionId;
    public Integer causeId;
    public Boolean running;
    public Integer projectId;
    public String cache;
    public Integer inChargeEmp;
    public boolean isRelapse;
    public boolean expirateNotif;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "employee_id",
            "prj_backlog_id",
            "per_third_id",
            "crm_cli_id",
            "created_by",
            "type_id",
            "reg_date",
            "close_date",
            "beg_date",
            "end_date",
            "restart_date",
            "cut_date",
            "expirate_date",
            "total_time",
            "dead_time",
            "duration",
            "in_charge",
            "active",
            "origin_id",
            "state",
            "user_type",
            "notes",
            "subject",
            "priority",
            "equip_id",
            "solution",
            "type_hlp_desk",
            "typing_detail",
            "service_id",
            "cat_one_id",
            "cat_two_id",
            "solution_id",
            "cause_id",
            "running",
            "project_id",
            "cache",
            "in_charge_emp",
            "is_relapse",
            "expirate_notif"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, employeeId);
        q.setParam(2, prjBacklogId);
        q.setParam(3, perThirdId);
        q.setParam(4, crmCliId);
        q.setParam(5, createdBy);
        q.setParam(6, typeId);
        q.setParam(7, regDate);
        q.setParam(8, closeDate);
        q.setParam(9, begDate);
        q.setParam(10, endDate);
        q.setParam(11, restartDate);
        q.setParam(12, cutDate);
        q.setParam(13, expirateDate);
        q.setParam(14, totalTime);
        q.setParam(15, deadTime);
        q.setParam(16, duration);
        q.setParam(17, inCharge);
        q.setParam(18, active);
        q.setParam(19, originId);
        q.setParam(20, state);
        q.setParam(21, userType);
        q.setParam(22, notes);
        q.setParam(23, subject);
        q.setParam(24, priority);
        q.setParam(25, equipId);
        q.setParam(26, solution);
        q.setParam(27, typeHlpDesk);
        q.setParam(28, typingDetail);
        q.setParam(29, serviceId);
        q.setParam(30, catOneId);
        q.setParam(31, catTwoId);
        q.setParam(32, solutionId);
        q.setParam(33, causeId);
        q.setParam(34, running);
        q.setParam(35, projectId);
        q.setParam(36, cache);
        q.setParam(37, inChargeEmp);
        q.setParam(38, isRelapse);
        q.setParam(39, expirateNotif);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        employeeId = MySQLQuery.getAsInteger(row[0]);
        prjBacklogId = MySQLQuery.getAsInteger(row[1]);
        perThirdId = MySQLQuery.getAsInteger(row[2]);
        crmCliId = MySQLQuery.getAsInteger(row[3]);
        createdBy = MySQLQuery.getAsInteger(row[4]);
        typeId = MySQLQuery.getAsInteger(row[5]);
        regDate = MySQLQuery.getAsDate(row[6]);
        closeDate = MySQLQuery.getAsDate(row[7]);
        begDate = MySQLQuery.getAsDate(row[8]);
        endDate = MySQLQuery.getAsDate(row[9]);
        restartDate = MySQLQuery.getAsDate(row[10]);
        cutDate = MySQLQuery.getAsDate(row[11]);
        expirateDate = MySQLQuery.getAsDate(row[12]);
        totalTime = MySQLQuery.getAsInteger(row[13]);
        deadTime = MySQLQuery.getAsInteger(row[14]);
        duration = MySQLQuery.getAsString(row[15]);
        inCharge = MySQLQuery.getAsInteger(row[16]);
        active = MySQLQuery.getAsBoolean(row[17]);
        originId = MySQLQuery.getAsInteger(row[18]);
        state = MySQLQuery.getAsString(row[19]);
        userType = MySQLQuery.getAsString(row[20]);
        notes = MySQLQuery.getAsString(row[21]);
        subject = MySQLQuery.getAsString(row[22]);
        priority = MySQLQuery.getAsString(row[23]);
        equipId = MySQLQuery.getAsInteger(row[24]);
        solution = MySQLQuery.getAsString(row[25]);
        typeHlpDesk = MySQLQuery.getAsString(row[26]);
        typingDetail = MySQLQuery.getAsString(row[27]);
        serviceId = MySQLQuery.getAsInteger(row[28]);
        catOneId = MySQLQuery.getAsInteger(row[29]);
        catTwoId = MySQLQuery.getAsInteger(row[30]);
        solutionId = MySQLQuery.getAsInteger(row[31]);
        causeId = MySQLQuery.getAsInteger(row[32]);
        running = MySQLQuery.getAsBoolean(row[33]);
        projectId = MySQLQuery.getAsInteger(row[34]);
        cache = MySQLQuery.getAsString(row[35]);
        inChargeEmp = MySQLQuery.getAsInteger(row[36]);
        isRelapse = MySQLQuery.getAsBoolean(row[37]);
        expirateNotif = MySQLQuery.getAsBoolean(row[38]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "hlp_request";
    }

    public static String getSelFlds(String alias) {
        return new HlpRequest().getSelFldsForAlias(alias);
    }

    public static List<HlpRequest> getList(MySQLQuery q, Connection conn) throws Exception {
        return new HlpRequest().getListFromQuery(q, conn);
    }

    public static List<HlpRequest> getList(Params p, Connection conn) throws Exception {
        return new HlpRequest().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new HlpRequest().deleteById(id, conn);
    }

    public static List<HlpRequest> getAll(Connection conn) throws Exception {
        return new HlpRequest().getAllList(conn);
    }

//fin zona de reemplazo
    public static HlpRequest getInProgressByBacklog(int backlogId, int empId, int perEmpId, Connection conn) throws Exception {
        Params p = new Params();
        p.param("prjBacklogId", backlogId);
        p.param("inChargeEmp", empId);
        p.param("inCharge", perEmpId);
        p.param("state", "prog");
        return new HlpRequest().select(p, conn);
    }

    /**
     * esta es una version incompleta de lo que hay en el cliente, no se pasaron
     * los enums
     *
     * @param hlpRequestId
     * @return
     * @throws Exception
     */
    public static String getCacheQuery(int hlpRequestId) throws Exception {
        return "update hlp_request, ( "
                + " "
                + "SELECT hpr.id as id,  "
                + "concat( id ,',',"
                + "IFNULL(subject,''),',', "
                //        + "IFNULL((" + new HlpRequest().getOptionsAsIfString("state") + "),''),',', "
                //        + new HlpRequest().getOptionsAsIfString("user_type") + ",',', "
                //       + "IFNULL((" + new HlpRequest().getOptionsAsIfString("priority") + "),''),',', "
                //     + "IFNULL((" + new HlpRequest().getOptionsAsIfString("type_hlp_desk") + "),''),',', "
                + "IFNULL((select st.name FROM hlp_service_type st WHERE hpr.service_id=st.id),''),',', "
                + "IFNULL((select cto.name FROM hlp_cat_one cto WHERE hpr.cat_one_id=cto.id),''),',', "
                + "IFNULL((select ctt.name FROM hlp_cat_two ctt WHERE hpr.cat_two_id=ctt.id),''),',', "
                + "IFNULL((select sol.name FROM hlp_solution sol WHERE hpr.solution_id=sol.id),''),',', "
                + "IFNULL((select cau.name FROM hlp_cause cau WHERE hpr.cause_id=cau.id),''),',', "
                + "ifnull((select concat(e.document, ',' ,e.first_name, ',',e.last_name) FROM employee e WHERE hpr.employee_id=e.id), ''),',', "
                + "ifnull((select concat(t.first_name,',',t.last_name) FROM hlp_per_third t WHERE hpr.per_third_id=t.id), ''),',', "
                + "ifnull((select cl.name FROM crm_client cl WHERE hpr.crm_cli_id=cl.id), ''),',', "
                + "ifnull((select concat(e.document, ',' ,e.first_name, ',',e.last_name) FROM employee e WHERE hpr.created_by=e.id), ''),',', "
                + "ifnull((select ty.name FROM hlp_type ty WHERE hpr.type_id=ty.id), ''),',', "
                + "ifnull((select concat(pe.first_name, ',',pe.last_name) FROM per_employee pe WHERE hpr.in_charge=pe.id), ''),',', "
                + "ifnull((select eqe.short_desc FROM eqs_equip eqe WHERE hpr.equip_id=eqe.id), ''),',', "
                + "ifnull((select pro.name FROM crm_project pro WHERE hpr.project_id=pro.id), ''),',' "
                + ") as c "
                + "FROM hlp_request hpr "
                + (hlpRequestId != -1 ? "WHERE hpr.id = " + hlpRequestId + " " : "")
                + "group by hpr.id) as l set hlp_request.`cache` = l.c where hlp_request.id = l.id;";
    }

}
