package web.providers;

import api.sys.model.SysFlowReq;
import java.sql.Connection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import web.system.flow.SysFlowChk;
import web.system.flow.SysFlowStep;

/**
 * Se llama para terminar las solicitudes de proveedores que estan anuladas hace
 * mas de dos meses
 */
@Singleton
@Startup
public class MoveFinishRequest {

    static String BOOLEAN = "bool";
    static String INTEGER = "int";
    static String DATA = "data";
    static String NONE = "upd";
 
    @Schedule(hour = "12", minute = "49")
    protected void processRequest() {
        System.out.println("TAREA MOVER TERMINAR SOLICITUDES EN PROVEEDORES");
        try {
            try (Connection conn = MySQLCommon.getConnection("sigmads", null)) {

                Object[][] dataReqs = new MySQLQuery("SELECT DISTINCT r.id "
                        + "FROM sys_flow_req r "
                        + "INNER JOIN sys_flow_chk cc ON cc.id = r.cur_chk_id "
                        + "INNER JOIN sys_flow_step st ON st.id = cc.from_step_id "
                        + "INNER JOIN employee re ON re.id = r.employee_id "
                        + "INNER JOIN employee ce ON ce.id = cc.emp_id "
                        + "INNER JOIN prov_request req ON req.sys_req_id = r.id AND req.visible "
                        + "WHERE st.stage_id = 3 AND DATEDIFF(CURDATE(), r.beg_date) > 60").getRecords(conn);

                if (dataReqs != null && dataReqs.length > 0) {
                    for (Object[] rowReq : dataReqs) {
                        Integer reqId = (Integer) rowReq[0];
                        String notes = "Tarea Programada";
                        SysFlowReq req = new SysFlowReq().select(reqId, conn);
                    
                        SysFlowChk chk = SysFlowChk.select(req.curChkId, conn);

                        int fromStepId = 5;
                        int toStepId = 3;

                        if (!chk.fromStepId.equals(fromStepId)) {
                            throw new Exception("Otro usuario ha modificado el proceso.");
                        }

                        leaveStep(chk, toStepId, notes, conn);
                        arriveToStep(req, fromStepId, toStepId, notes, conn);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(MoveFinishRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void arriveToStep(SysFlowReq req, int fromStepId, int toStepId, String notes,  Connection conn) throws Exception {
        SysFlowStep step = SysFlowStep.select(toStepId, conn);
        if (req.begDate == null && step.nodeType.equals("task")) {
            String toStage = new MySQLQuery("select st.extra from sys_flow_stage st where st.id = " + step.stageId).getAsString(conn);
            if (!toStage.equals("begin")) {
                req.begDate = new Date();
            }
        }
        SysFlowChk chk = new SysFlowChk();
        chk.createDt = new Date();
        chk.fromStepId = toStepId;
        chk.reqId = req.id;
        chk.notes = notes;
        chk.empId = 1; // por ser tarea programada colocamos el administrador 

        switch (step.nodeType) {
            case "end":
                String connType = new MySQLQuery("SELECT c.`type` FROM sys_flow_conn c where c.from_step_id = ?1 and c.to_step_id = ?2;").setParam(1, fromStepId).setParam(2, toStepId).getAsString(conn);
                chk.id = SysFlowChk.insert(chk, conn);
                req.curChkId = chk.id;
                req.endDate = new Date();
                req.endType = connType;
                req.update(conn);
                break;
            default:
                throw new Exception("Tipo desconocido: " + step.nodeType);
        }
    }

    private void leaveStep(SysFlowChk chk, int toStepid, String notes, Connection conn) throws Exception {
        chk.checkDt = new Date();
        chk.toStepId = toStepid;
        chk.notes = notes;
        chk.empId = 1; // por ser tarea programada colocamos el administrador 
        chk.update(chk, conn);
    }

   
}
