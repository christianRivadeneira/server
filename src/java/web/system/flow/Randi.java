package web.system.flow;

import api.sys.model.SysFlowReq;
import web.system.flow.interfaces.SysFlowSplitter;
import web.system.flow.interfaces.SysFlowValidator;
import web.system.flow.interfaces.SysFlowUpdater;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;
import web.quality.MailCfg;
import web.quality.SendMail;

@WebServlet(name = "Randi", urlPatterns = {"/system/Randi"})
public class Randi extends HttpServlet {

    public static class RandiRequest {

        public Integer reqId;
        public Integer empId;
        public Integer fromEmpId;
        public Integer inStepId;
        public Integer outStepId;
        public String oper;
        public String type;
        public String q;
        public String text;
        public String notes;
        public Boolean isJava;
        public Boolean subs;

        public RandiRequest() {
        }

    }

    protected synchronized void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> pars = MySQLQuery.scapedParams(request);
        try {
            try (Connection conn = MySQLCommon.getConnection(pars.get("poolName"), pars.get("tz"))) {
                String session = pars.get("sessionId");
                SessionLogin sl = SessionLogin.validate(session, conn);

                RandiRequest randiReq = new RandiRequest();
                randiReq.reqId = pars.containsKey("reqId") ? Integer.valueOf(pars.get("reqId")) : null;
                randiReq.empId = pars.containsKey("empId") ? Integer.valueOf(pars.get("empId")) : null;
                randiReq.fromEmpId = pars.containsKey("fromEmpId") ? Integer.valueOf(pars.get("fromEmpId")) : null;
                randiReq.inStepId = pars.containsKey("inStepId") ? Integer.valueOf(pars.get("inStepId")) : null;
                randiReq.outStepId = pars.containsKey("outStepId") ? Integer.valueOf(pars.get("outStepId")) : null;
                randiReq.oper = pars.get("oper");
                randiReq.type = pars.get("type");
                randiReq.q = pars.get("q");
                randiReq.text = pars.get("text");
                randiReq.notes = pars.get("notes");
                randiReq.isJava = pars.containsKey("isJava") ? MySQLQuery.getAsBoolean(pars.get("isJava")) : null;
                randiReq.subs = pars.containsKey("subs") ? MySQLQuery.getAsBoolean(pars.get("subs")) : null;

                JsonObject randiFlow = processRandiFlow(randiReq, conn, sl);
                try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
                    w.writeObject(randiFlow);
                }
            } catch (Exception ex) {
                Logger.getLogger(Randi.class.getName()).log(Level.SEVERE, null, ex);
                String msg = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
                response.getWriter().print(msg);
                response.setStatus(500);
            }
        } catch (IOException ex) {
            Logger.getLogger(Randi.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(500, ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
        }
    }

    public JsonObject processRandiFlow(RandiRequest pars, Connection conn, SessionLogin sl) throws SQLException, Exception {
        String op = pars.oper;
        try {
            JsonObjectBuilder ob = Json.createObjectBuilder();

            try {
                conn.setAutoCommit(false);
                //SysFlowChk chk;
                switch (op) {
                    case "testQ": {
                        String type = pars.type;
                        boolean isJava = pars.isJava;
                        testQuery(pars.q, isJava, conn, type);
                        break;
                    }
                    case "sendMail": {
                        SysFlowReq req = new SysFlowReq().select(pars.reqId, conn);
                        SysFlowChk chk = SysFlowChk.select(req.curChkId, conn);
                        String type = pars.type;
                        String text = pars.text;

                        List<Integer> emps = new ArrayList<>();
                        switch (type) {
                            case "fixed":
                                emps.add(pars.empId);
                                break;
                            case "cur":
                                emps.add(chk.empId);
                                break;
                            case "crea":
                                emps.add(req.employeeId);
                                break;
                            case "subs": {
                                Object[][] subsData = new MySQLQuery("SELECT emp_id FROM sys_flow_subs WHERE req_id = " + req.id).getRecords(conn);
                                for (Object[] subsRow : subsData) {
                                    emps.add(MySQLQuery.getAsInteger(subsRow[0]));
                                }
                                break;
                            }
                            default:
                                throw new Exception("tipo desconocido: " + type);
                        }
                        if (!emps.isEmpty()) {
                            Integer reqSerial = null;
                            String serialQ = SysFlowStage.getSerialQueryByStepId(chk.fromStepId, conn);
                            if (serialQ != null) {
                                Map<String, Integer> reps = getReplacements(req, conn);
                                Object[][] serialData = getAsData(serialQ, reps, conn);
                                if (serialData != null && serialData.length > 0) {
                                    reqSerial = MySQLQuery.getAsInteger(serialData[0][0]);
                                }
                            }
                            if (reqSerial == null) {
                                reqSerial = req.id;
                            }
                            SysFlowType fType = SysFlowType.select(req.typeId, conn);
                            String subject = "Le dejaron un mensaje sobre " + fType.article.toLowerCase() + " " + fType.subject.toLowerCase() + " No " + reqSerial;
                            String fromName = new MySQLQuery("select concat(e.first_name, ' ', e.last_name) from employee e where e.id = " + pars.fromEmpId).getAsString(conn);
                            sendMails(MailCfg.select(conn), emps.toArray(new Integer[]{emps.size()}), fromName + " escribió", subject, text, conn);
                        }
                    }
                    case "getResps": {
                        Object[][] subsData = new MySQLQuery("SELECT e.id FROM employee e   "
                                + "INNER JOIN    "
                                + "(SELECT c.emp_id FROM sys_flow_chk c where c.req_id = ?1   "
                                + "UNION   "
                                + "SELECT s.emp_id FROM sys_flow_subs s where s.req_id = ?1) AS l ON e.id = l.emp_id   "
                                + "WHERE e.active AND e.mail <> '' AND e.mail IS NOT NULL"
                        ).setParam(1, pars.reqId).getRecords(conn);

                        JsonArrayBuilder jab = Json.createArrayBuilder();
                        for (Object[] subsRow : subsData) {
                            JsonObjectBuilder cob = Json.createObjectBuilder();
                            cob.add("id", MySQLQuery.getAsLong(subsRow[0]));
                            jab.add(cob);
                        }
                        ob.add("resps", jab);
                        break;
                    }
                    case "getSteps": {
                        SysFlowReq req = new SysFlowReq().select(pars.reqId, conn);
                        Map<String, Integer> reps = getReplacements(req, conn);

                        boolean subscribed = SysFlowSubs.getByIds(req.id, sl.employeeId, conn) != null;
                        SysFlowChk chk = SysFlowChk.select(req.curChkId, conn);
                        JsonArrayBuilder jab = Json.createArrayBuilder();
                        if (chk.empId != null && sl.employeeId == chk.empId) {
                            SysFlowStep curStep = SysFlowStep.select(chk.fromStepId, conn);
                            if (curStep.reject) {
                                SysFlowStep[] hists = SysFlowStep.getByHist(req.id, conn);
                                for (SysFlowStep hist : hists) {
                                    if (hist.id != curStep.id) {
                                        JsonObjectBuilder cob = Json.createObjectBuilder();
                                        cob.add("stepId", hist.id);
                                        cob.add("label", hist.name);
                                        cob.add("comment", true);
                                        cob.add("showSubs", !subscribed && !getSubcriptors(req, chk.fromStepId, hist.id, reps, conn).contains(chk.empId));
                                        jab.add(cob);
                                    }
                                }
                            }

                            SysFlowConn[] cxs = SysFlowConn.getByStep(chk.fromStepId, conn);
                            for (SysFlowConn cx : cxs) {
                                JsonObjectBuilder cob = Json.createObjectBuilder();
                                cob.add("stepId", cx.toStepId);
                                cob.add("label", cx.name);
                                cob.add("comment", cx.reqComment);
                                cob.add("showSubs", !subscribed && !getSubcriptors(req, chk.fromStepId, cx.toStepId, reps, conn).contains(chk.empId));
                                jab.add(cob);
                            }
                        }
                        ob.add("curStepId", chk.fromStepId);
                        ob.add("conns", jab);
                        String stepName = SysFlowStep.select(chk.fromStepId, conn).name;
                        if (stepName != null) {
                            ob.add("label", stepName);
                        }
                        break;
                    }
                    case "start": {
                        SysFlowReq req = new SysFlowReq().select(pars.reqId, conn);
                        Map<String, Integer> reps = getReplacements(req, conn);

                        Integer startStepId = new MySQLQuery("SELECT id FROM sys_flow_step WHERE node_type = 'start' and type_id = " + req.typeId + ";").getAsInteger(conn);
                        if (startStepId == null) {
                            throw new RandiException("No se ha definido el paso inicial del flujo.");
                        }
                        if (req.curChkId != null) {
                            throw new RandiException("Ya se ha iniciado el flujo");
                        }
                        arriveToStep(req, null, startStepId, startStepId, pars.notes, reps, sl, conn);
                        break;
                    }
                    case "validate": {
                        SysFlowReq req = new SysFlowReq().select(pars.reqId, conn);
                        Map<String, Integer> reps = getReplacements(req, conn);

                        SysFlowChk chk = SysFlowChk.select(req.curChkId, conn);

                        int fromStepId = pars.inStepId;
                        int toStepId = pars.outStepId;

                        if (sl.employeeId != chk.empId) {
                            throw new RandiException("No tiene autorización para dar el paso");
                        }
                        if (!chk.fromStepId.equals(fromStepId)) {
                            throw new RandiException("Otro usuario ha modificado el proceso.");
                        }
                        validate(req, fromStepId, "out_val", reps, conn);
                        validate(req, toStepId, "in_val", reps, conn);
                        break;
                    }
                    case "move": {
                        SysFlowReq req = new SysFlowReq().select(pars.reqId, conn);
                        Map<String, Integer> reps = getReplacements(req, conn);

                        SysFlowChk chk = SysFlowChk.select(req.curChkId, conn);

                        int fromStepId = pars.inStepId;
                        int toStepId = pars.outStepId;

                        if (sl.employeeId != chk.empId) {
                            throw new RandiException("No tiene autorización para dar el paso");
                        }
                        if (!chk.fromStepId.equals(fromStepId)) {
                            throw new RandiException("Otro usuario ha modificado el proceso.");
                        }

                        validate(req, fromStepId, "out_val", reps, conn);
                        validate(req, toStepId, "in_val", reps, conn);

                        boolean subs = MySQLQuery.getAsBoolean(pars.subs);

                        String mvType = new MySQLQuery("SELECT type FROM sys_flow_conn WHERE from_step_id = ?1 AND to_step_id = ?2").setParam(1, fromStepId).setParam(2, toStepId).getAsString(conn);
                        String splitter = new MySQLQuery("SELECT splitter FROM sys_flow_step WHERE id = ?1").setParam(1, fromStepId).getAsString(conn);

                        if ((mvType != null && mvType.equals("pos")) && splitter != null) {
                            SysFlowSplitter split = (SysFlowSplitter) Class.forName(splitter).newInstance();
                            int[] ids = split.split(req, sl, conn);
                            for (int i = 0; i < ids.length; i++) {
                                SysFlowReq r = new SysFlowReq().select(ids[i], conn);
                                SysFlowChk c = SysFlowChk.select(r.curChkId, conn);
                                Map<String, Integer> rs = getReplacements(r, conn);
                                leaveStep(r, mvType, c, fromStepId, toStepId, pars.notes, subs, rs, sl, conn);
                                arriveToStep(r, mvType, fromStepId, toStepId, pars.notes, rs, sl, conn);
                            }
                        } else {
                            leaveStep(req, mvType, chk, fromStepId, toStepId, pars.notes, subs, reps, sl, conn);
                            arriveToStep(req, mvType, fromStepId, toStepId, pars.notes, reps, sl, conn);
                        }
                        break;
                    }
                    case "unsubs": {
                        SysFlowReq req = new SysFlowReq().select(pars.reqId, conn);

                        new MySQLQuery("DELETE from sys_flow_subs "
                                + "where sys_flow_subs.emp_id = " + sl.employeeId + " and sys_flow_subs.req_id = " + req.id + " and sys_flow_subs.sub_id IS NULL").executeDelete(conn);

                        new MySQLQuery("delete sys_flow_subs.* from sys_flow_subs, sys_flow_step_subs "
                                + "where  "
                                + "sys_flow_subs.sub_id = sys_flow_step_subs.id AND "
                                + "sys_flow_subs.emp_id = " + sl.employeeId + "  AND "
                                + "sys_flow_subs.req_id = " + req.id + "  AND "
                                + "sys_flow_step_subs.disposable").executeDelete(conn);

                        break;
                    }
                    default:
                        throw new RandiException("Operación desconocida: " + op);
                }
                conn.commit();
                ob.add("status", "ok");
                return ob.build();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (RandiException ex) {
            Logger.getLogger(Randi.class.getName()).log(Level.SEVERE, null, ex);
            JsonObjectBuilder ob = Json.createObjectBuilder();
            ob.add("status", "error");
            ob.add("msg", ex.getMessage());
            return ob.build();
        }
    }

    private static Map<String, Integer> getReplacements(SysFlowReq req, Connection conn) throws Exception {
        Map<String, Integer> rta = new HashMap<>();
        rta.put("@req_id", req.id);
        rta.put("@per_id", req.perEmpId);
        rta.put("@emp_id", req.employeeId);
        rta.put("@sarea_id", req.perSareaId);
        rta.put("@area_id", req.perAreaId);
        rta.put("@pos_id", new MySQLQuery("select c.pos_id from per_contract c where c.emp_id = " + req.perEmpId + " and c.`last`").getAsInteger(conn));
        rta.put("@cur_emp_id", new MySQLQuery("SELECT c.emp_id FROM sys_flow_chk c WHERE c.req_id = " + req.id + " AND c.check_dt IS NULL LIMIT 1").getAsInteger(conn));
        return rta;
    }

    private void validate(SysFlowReq req, int stepId, String type, Map<String, Integer> reps, Connection conn) throws Exception {
        SysFlowQuery[] vals = SysFlowQuery.getByStepId(stepId, type, conn);
        for (SysFlowQuery val : vals) {
            if (val.java) {
                Object obj = Class.forName(val.query).newInstance();
                ((SysFlowValidator) obj).validate(req, conn);
            } else {
                if (getAsBoolean(val.query, reps, conn)) {
                    throw new RandiException(val.msg);
                }
            }
        }
    }

    private void update(SysFlowReq req, String mvType, int stepId, String type, Map<String, Integer> reps, SessionLogin sl, Connection conn) throws Exception {
        SysFlowQuery[] vals = SysFlowQuery.getByStepId(stepId, type, conn);
        for (SysFlowQuery val : vals) {
            if (val.java) {
                Object obj = Class.forName(val.query).newInstance();
                ((SysFlowUpdater) obj).update(req, mvType, sl, conn);
            } else {
                executeUpdates(val.query, reps, conn);
            }
        }
    }

    private List<Integer> getSubcriptors(SysFlowReq req, int fromStepId, int toStepId, Map<String, Integer> reps, Connection conn) throws Exception {
        List<Integer> rta = new ArrayList<>();
        getSubcriptors(req, fromStepId, "out", rta, reps, conn);
        getSubcriptors(req, toStepId, "in", rta, reps, conn);
        return rta;
    }

    private void getSubcriptors(SysFlowReq req, int stepId, String type, List<Integer> rta, Map<String, Integer> reps, Connection conn) throws Exception {
        SysFlowStepSubs[] subs = SysFlowStepSubs.getByStep(stepId, type, conn);
        for (SysFlowStepSubs stepSub : subs) {
            rta.add(getSubsriptor(stepSub, req, reps, conn));
        }
    }

    private int getSubsriptor(SysFlowStepSubs stepSub, SysFlowReq req, Map<String, Integer> reps, Connection conn) throws Exception {
        switch (stepSub.usrType) {
            case "fixed":
                return stepSub.empId;
            case "crea":
                return req.employeeId;
            case "sql":
                Integer empId = getAsInteger(stepSub.empQ, reps, conn);
                if (empId == null) {
                    throw new RandiException("No se pudo determinar el responsable.");
                } else {
                    return empId;
                }
            default:
                throw new RandiException("Tipo no reconocido: " + stepSub.usrType);
        }
    }

    private void sysSubscribe(SysFlowReq req, int stepId, String type, Map<String, Integer> reps, Connection conn) throws Exception {
        SysFlowStepSubs[] subs = SysFlowStepSubs.getByStep(stepId, type, conn);
        for (SysFlowStepSubs stepSub : subs) {
            SysFlowSubs reqSub = new SysFlowSubs();
            reqSub.empId = getSubsriptor(stepSub, req, reps, conn);
            reqSub.subId = stepSub.id;
            reqSub.reqId = req.id;
            SysFlowSubs curSub = SysFlowSubs.getByIds(reqSub.reqId, reqSub.empId, conn);
            if (curSub == null) {
                reqSub.insert(reqSub, conn);
            } else {
                if (curSub.subId == null) {
                    curSub.delete(curSub.id, conn);
                    reqSub.insert(reqSub, conn);
                }
            }
        }
    }

    private void leaveStep(SysFlowReq req, String mvType, SysFlowChk chk, int fromStepId, int toStepid, String notes, boolean susbs, Map<String, Integer> reps, SessionLogin sl, Connection conn) throws Exception {
        update(req, mvType, fromStepId, "out_upd", reps, sl, conn);
        sysSubscribe(req, fromStepId, "out", reps, conn);
        chk.checkDt = new Date();
        chk.toStepId = toStepid;
        chk.notes = notes;
        if (susbs) {
            SysFlowSubs sub = new SysFlowSubs();
            sub.empId = chk.empId;
            sub.reqId = req.id;
            sub.insert(sub, conn);
        }
        chk.update(chk, conn);
    }

    private void sendMails(SysFlowReq req, SysFlowChk chk, String notes, SysFlowStep step, Map<String, Integer> reps, int sessEmpId, Connection conn) throws Exception {
        //parche para que la aprobación de compra solo auttorice Gladis Amanda Cabrera Sanchez
        //Si en algun momento el usuario se inactiva o no va llevar más el control de aprobación
        //se deben borrar las línesas del siguiente if
        /*if(chk.fromStepId==32 || chk.fromStepId==21 || chk.fromStepId==23 || chk.fromStepId==26 || chk.fromStepId==44){
            chk.empId=1430;
        }*/
        SysFlowMail[] mails = SysFlowMail.getByStepId(chk.fromStepId, conn);
        Integer reqSerial = null;
        String serialQuery = SysFlowStage.getSerialQueryByStepId(step.id, conn);
        if (!MySQLQuery.isEmpty(serialQuery)) {
            Object[][] serialData = getAsData(serialQuery, reps, conn);
            if (serialData != null && serialData.length > 0) {
                reqSerial = MySQLQuery.getAsInteger(serialData[0][0]);
            }
        }
        if (mails.length == 0) {
            return;
        }
        SysFlowType type = SysFlowType.select(req.typeId, conn);
        MailCfg mailCfg = MailCfg.select(conn);

        List<Integer> emps = new ArrayList<>();

        String msgMail = null;

        for (SysFlowMail mail : mails) {
            msgMail = mail.msg;
            switch (mail.type) {
                case "new":
                    emps.add(chk.empId);
                    break;
                case "crea":
                    emps.add(req.employeeId);
                    break;
                case "fixed":
                    emps.add(mail.empId);
                    break;
                case "subs": {
                    Object[][] subsData = new MySQLQuery("SELECT emp_id FROM sys_flow_subs WHERE req_id = " + req.id).getRecords(conn);
                    for (Object[] subsRow : subsData) {
                        emps.add(MySQLQuery.getAsInteger(subsRow[0]));
                    }
                    break;
                }
                case "query": {
                    Object[][] subsData = getAsData(mail.query, reps, conn);
                    for (Object[] subsRow : subsData) {
                        emps.add(MySQLQuery.getAsInteger(subsRow[0]));
                    }
                    break;
                }
                default:
                    throw new Exception("tipo desconocido: " + mail.type);
            }

        }
        emps.remove(new Integer(sessEmpId));
        if (emps.isEmpty()) {
            return;
        }

        List<Integer> otherEmps = new ArrayList<>();
        boolean sendCur = false;

        for (int i = 0; i < emps.size(); i++) {
            Integer empId = emps.get(i);
            if (!Objects.equals(empId, chk.empId)) {
                otherEmps.add(empId);
            } else {
                sendCur = true;
            }
        }

        if (reqSerial == null) {
            reqSerial = req.id;
        }
        String curEmpName = new MySQLQuery("SELECT CONCAT(first_name, ' ', last_name) FROM employee WHERE id = " + chk.empId).getAsString(conn);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        if (sendCur) {// a quien llega el paso 
            String subject = "Se requiere su atención en " + type.subject + " No " + reqSerial;
            String title = type.subject + " No " + reqSerial;
            String plain = curEmpName + ", " + type.article.toLowerCase() + " " + type.subject.toLowerCase() + " No " + reqSerial
                    + " con fecha de creación " + sdf.format(req.creaDate) + ", necesita de su revisión en la actividad \""
                    + step.name.toLowerCase() + "\" del módulo de " + type.module.toLowerCase() + ".";
            if (notes != null && !notes.isEmpty()) {
                plain += "\n<strong>Notas:</strong> " + notes;
            }
            if (chk.notes != null && !chk.notes.isEmpty()) {
                plain += "\n\n<strong>Notas:</strong> " + chk.notes;
            }
            sendMails(mailCfg, new Integer[]{chk.empId}, title, subject, plain, conn);
        }

        if (!otherEmps.isEmpty()) { // a otras personas  
            String subject = "Hubo un cambio en " + type.subject + " No " + reqSerial;
            String title = type.subject + " No " + reqSerial;
            String plain = "Hubo un cambio en " + type.article.toLowerCase() + " " + type.subject.toLowerCase() + " No " + reqSerial
                    + " con fecha de creación " + sdf.format(req.creaDate) + ", que está en la actividad \"" + step.name.toLowerCase()
                    + "\" del módulo de " + type.module.toLowerCase();
            if (chk.empId != null) {
                plain += ", a cargo de " + curEmpName;
            }

            if (msgMail != null && !msgMail.isEmpty() && !msgMail.equals("null")) {
                plain += "\n\n" + msgMail;
            }

            plain += ".";
            if (notes != null && !notes.isEmpty()) {
                plain += "\n<strong>Notas:</strong> " + notes;
            }
            if (chk.notes != null && !chk.notes.isEmpty()) {
                plain += "\n\n<strong>Notas:</strong> " + chk.notes;
            }
            sendMails(mailCfg, otherEmps.toArray(new Integer[otherEmps.size()]), title, subject, plain, conn);
        }
    }

    private void sendMails(MailCfg cfg, Integer[] emps, String title, String subject, String plain, Connection conn) throws Exception {
        String logo = new MySQLQuery("SELECT mail_alert_logo_url FROM sys_cfg").getAsString(conn);
        logo = (logo != null ? logo : "http://qualisys.com.co/ent_logos/qualisys_new.png");
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT DISTINCT id, mail FROM employee WHERE id IN (");
        for (Integer emp : emps) {
            sb.append(emp).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(") AND active AND mail <> '' AND mail IS NOT NULL");
        Object[][] empsData = new MySQLQuery(sb.toString()).getRecords(conn);
        StringBuilder mails = new StringBuilder();
        for (Object[] empRow : empsData) {
            mails.append(MySQLQuery.getAsString(empRow[1])).append(",");
        }
        title = Matcher.quoteReplacement(title);
        plain = Matcher.quoteReplacement(plain);
        sendMailThread(cfg, logo, title, mails.toString(), subject, plain);
    }

    private void sendMailThread(final MailCfg mailCfg, String logo, String title, final String mails, final String subject, final String plain) throws Exception {
        String html = SendMail.readTemplate("/web/template.html");
        html = html.replaceAll("\\{headerTitle\\}", "");
        html = html.replaceAll("\\{titleAlerts\\}", title);
        html = html.replaceAll("\\{ent_logo\\}", logo);
        html = html.replaceAll("[\r\n\t]", "");
        html = html.replaceAll("\\{rows\\}", plain);
        final String fhtml = html;

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SendMail.sendMail(mailCfg, mails, subject, fhtml, plain);
                } catch (Exception ex) {
                    Logger.getLogger(Randi.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        t.start();
    }

    private void arriveToStep(SysFlowReq req, String mvType, int fromStepId, int toStepId, String notes, Map<String, Integer> reps, SessionLogin sl, Connection conn) throws Exception {
        SysFlowStep step = SysFlowStep.select(toStepId, conn);
        if (req.begDate == null && step.nodeType.equals("task")) {
            String toStage = new MySQLQuery("select st.extra from sys_flow_stage st where st.id = " + step.stageId).getAsString(conn);
            if (!toStage.equals("begin")) {
                req.begDate = new Date();
            }
        }
        update(req, mvType, toStepId, "in_upd", reps, sl, conn);
        sysSubscribe(req, toStepId, "in", reps, conn);
        SysFlowChk chk = new SysFlowChk();
        chk.createDt = new Date();
        chk.fromStepId = toStepId;
        chk.reqId = req.id;

        switch (step.nodeType) {
            case "task":
                switch (step.respType) {
                    case "fixed":
                        chk.empId = step.respId;
                        break;
                    case "crea":
                        chk.empId = req.employeeId;
                        break;
                    case "sql":
                        chk.empId = getAsInteger(step.respQ, reps, conn);
//                        System.out.println("---empleado" + chk.empId);
//                        System.out.println("---query" + step.respQ);
//                        Iterator<Map.Entry<String, Integer>> it = reps.entrySet().iterator();
//                        while (it.hasNext()) {
//                            Map.Entry<String, Integer> next = it.next();
//                            System.out.println("key" + next.getKey());
//                            System.out.println("value" + next.getValue());
//                        }
                        if (chk.empId == null) {
                            //System.out.println(step.respQ);
                            throw new RandiException("No se pudo determinar el responsable en\n" + step.name);
                        }
                        break;
                    default:
                        break;
                }
                chk.firstTime = new MySQLQuery("SELECT count(*) = 0 "
                        + "FROM sys_flow_chk c "
                        + "WHERE c.from_step_id = ?1 and c.emp_id = ?2 AND c.req_id = ?3").setParam(1, chk.fromStepId).setParam(2, chk.empId).setParam(3, chk.reqId).getAsBoolean(conn);
                chk.id = SysFlowChk.insert(chk, conn);
                req.curChkId = chk.id;
                req.update(conn);
                sendMails(req, chk, notes, step, reps, sl.employeeId, conn);
                break;
            case "gate":
                String type;
                if (getAsBoolean(step.gateQ, reps, conn)) {
                    type = "pos";
                } else {
                    type = "neg";
                }
                Integer nextId = new MySQLQuery("SELECT to_step_id FROM sys_flow_conn WHERE from_step_id = " + toStepId + " AND type = ?1").setParam(1, type).getAsInteger(conn);
                chk.checkDt = chk.createDt;
                chk.toStepId = nextId;
                SysFlowChk lastChk = SysFlowChk.getCurByReqId(req.id, conn);
                SysFlowChk.insert(chk, conn);
                arriveToStep(req, mvType, toStepId, nextId, lastChk.notes, reps, sl, conn);
                break;
            case "start":
                arriveToStep(req, mvType, toStepId, new MySQLQuery("SELECT to_step_id FROM sys_flow_conn WHERE from_step_id = " + toStepId + "").getAsInteger(conn), null, reps, sl, conn);
                break;
            case "end":
                String connType = new MySQLQuery("SELECT c.`type` FROM sys_flow_conn c where c.from_step_id = ?1 and c.to_step_id = ?2;").setParam(1, fromStepId).setParam(2, toStepId).getAsString(conn);
                chk.id = SysFlowChk.insert(chk, conn);
                req.curChkId = chk.id;
                req.endDate = new Date();
                req.endType = connType;
                req.update(conn);
                break;
            default:
                throw new RandiException("Tipo desconocido: " + step.nodeType);
        }
    }

    private void executeUpdates(String q, Map<String, Integer> map, Connection conn) throws Exception {
        executeQuery(q, map, conn, NONE);
    }

    private Integer getAsInteger(String q, Map<String, Integer> map, Connection conn) throws Exception {
        return (Integer) executeQuery(q, map, conn, INTEGER);
    }

    private Boolean getAsBoolean(String q, Map<String, Integer> map, Connection conn) throws Exception {
        return (Boolean) executeQuery(q, map, conn, BOOLEAN);
    }

    private Object[][] getAsData(String q, Map<String, Integer> map, Connection conn) throws Exception {
        return (Object[][]) executeQuery(q, map, conn, DATA);
    }

    public static void testQuery(String q, boolean isJava, Connection conn, String type) throws Exception {
        if (isJava) {
            try {
                Object obj = Class.forName(q).newInstance();
                if (type.equals(BOOLEAN)) {
                    if (!(obj instanceof SysFlowValidator)) {
                        throw new RandiException(q + " debe implementar SysFlowValidator");
                    }
                } else if (type.equals(NONE)) {
                    if (!(obj instanceof SysFlowUpdater)) {
                        throw new RandiException(q + " debe implementar SysFlowUpdater");
                    }
                } else {
                    throw new RandiException("No se espera el tipo: " + type);
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
                throw new RandiException("No se encontró la clase: " + q);
            }
        } else {
            SysFlowReq r = new SysFlowReq();
            r.curChkId = -1;
            r.creaDate = new Date();
            r.employeeId = -1;
            r.id = -1;
            r.perAreaId = -1;
            r.perEmpId = -1;
            r.perOfficeId = -1;
            r.perSareaId = -1;
            r.typeId = -1;
            Map<String, Integer> reps = getReplacements(r, conn);
            try {
                executeQuery(q, reps, conn, type);
            } catch (Exception ex) {
                Logger.getLogger(Randi.class.getName()).log(Level.SEVERE, null, ex);
                throw new RandiException("Hay errores en el query");
            } finally {
                conn.rollback();
            }
        }
    }

    private static List<String> getRegex(String regex, String q) {
        List<String> l = new ArrayList<>();
        Matcher m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE).matcher(q);
        while (m.find()) {
            String var = m.group(1);
            if (!l.contains(var)) {
                l.add(var);
            }
        }
        return l;
    }

    private static List<String> getSetVars(String q) {
        return getRegex("SET[ ]+(@[A-Z0-9_]+)", q);
    }

    private static List<String> getVars(String q) {
        return getRegex("(@[A-Z0-9_]+)", q);
    }

    static String BOOLEAN = "bool";
    static String INTEGER = "int";
    static String DATA = "data";
    static String NONE = "upd";

    private static String getParamQuery(String q, Map<String, Integer> map) throws Exception {
        q = q.replaceAll("\\\\\"", "\"");
        q = q.replaceAll("\\\\\'", "'");
        q = q.replaceAll(" ", " ");
        Iterator<Map.Entry<String, Integer>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> e = it.next();
            q = q.replaceAll(e.getKey(), (e.getValue() != null ? e.getValue().toString() : "NULL"));
        }
        List<String> set = getSetVars(q);
        List<String> vars = getVars(q);
        for (int i = 0; i < vars.size(); i++) {
            String var = vars.get(i);
            if (!set.contains(var)) {
                System.out.println(q);
                throw new Exception("Existen variables sin asignar: " + var);
            }
        }
        return q;
    }

    private static Object executeQuery(String q, Map<String, Integer> map, Connection conn, String type) throws Exception {
        q = getParamQuery(q, map);

        String[] qs = q.split(";");
        int len = (type.equals(NONE) ? qs.length : qs.length - 1);
        for (int i = 0; i < len; i++) {
            try {
                new MySQLQuery(qs[i]).executeUpdate(conn);
            } catch (Exception ex) {
                System.out.println(qs[i]);
                throw ex;
            }
        }

        try {
            if (type.equals(BOOLEAN)) {
                return new MySQLQuery(qs[qs.length - 1]).getAsBoolean(conn);
            } else if (type.equals(INTEGER)) {
                return new MySQLQuery(qs[qs.length - 1]).getAsInteger(conn);
            } else if (type.equals(DATA)) {
                return new MySQLQuery(qs[qs.length - 1]).getRecords(conn);
            } else if (type.equals(NONE)) {
                return null;
            } else {
                throw new RuntimeException();
            }
        } catch (Exception ex) {
            System.out.println(qs[qs.length - 1]);
            throw ex;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    static class RandiException extends Exception {

        public RandiException(String msg) {
            super(msg);
        }
    }
}
