package api.per.model;

import java.sql.Connection;
import utilities.logs.Log;
import utilities.logs.LogType;

public class PerLog extends Log {

    public static final LogType PER_EMPLOYEE = new LogType(1, "SELECT CONCAT(first_name,' ', last_name) FROM per_employee WHERE id = ?1", "Empleado");
    public static final LogType PER_CONTRACT = new LogType(2, "SELECT CONCAT(e.first_name, ' ', e.last_name, ' Inicio: ',c.beg_date) FROM per_contract c INNER JOIN per_employee e ON e.id = c.emp_id WHERE c.id = ?1 ", "Contrato");
    public static final LogType PER_NOV_NOMIN = new LogType(3, "SELECT CONCAT(e.first_name,' ', e.last_name, ' Novedad: ', t.name, ' Cantidad: ', n.amount) FROM per_emp_nov n INNER JOIN per_nov_type t ON t.id = n.nov_type_id INNER JOIN per_employee e ON e.id = n.employee_id WHERE n.id = ?1", "Novedades Nomina");
    public static final LogType PER_EXTRA_TOT = new LogType(4, "SELECT CONCAT(e.first_name,' ', e.last_name, ' Duración: ', ex.approved_time/3600 ,' Registrado: ', ex.reg_date) FROM per_extra ex LEFT JOIN per_employee e ON e.id = ex.emp_id WHERE ex.id = ?1 ", " Extra Total");
    public static final LogType PER_EXTRA_DETAIL = new LogType(5, "SELECT CONCAT(e.first_name,' ', e.last_name, ' Duración: ', ex.approved_time/3600 ,' Registrado: ', ex.reg_date) FROM per_extra ex INNER JOIN per_employee e ON e.id = ex.emp_id WHERE ex.id = ?1 ", "Extra Detallada");
    public static final LogType PER_LICENCE = new LogType(6, "SELECT CONCAT(emp.first_name, ' ', emp.last_name, ' Causal: ', c.name, ' Inicio:', l.beg_date, ' Fin: ', l.end_date ) FROM per_licence l INNER JOIN per_employee emp ON emp.id = l.emp_id INNER JOIN per_cause c ON c.id = l.cause_id WHERE l.id = ?1 ", "Permiso");
    public static final LogType PER_PENALTY = new LogType(7, "SELECT CONCAT(e.first_name,' ', e.last_name, ' Causal: ', c.name, ' Registro: ', p.reg_date ) FROM per_penalty p INNER JOIN per_employee e ON e.id = p.emp_id INNER JOIN per_cause c ON c.id = p.cause_id WHERE p.id = ?1 ", "Sanciones");
    public static final LogType PER_SICK_LEAVE = new LogType(8, "SELECT CONCAT(emp.first_name, ' ', emp.last_name, ' Causal: ',c.name, ' Inicio: ',l.reg_date, ' Fin:',l.end_date ) FROM per_sick_leave l INNER JOIN per_employee emp ON emp.id = l.emp_id  INNER JOIN per_cause c ON c.id = l.cause_id   WHERE l.id = ?1 ", "Incapacidad");
    public static final LogType PER_GATE_PROF = new LogType(9, "SELECT p.name FROM per_gate_prof p WHERE p.id = ?1 ", "Perfiles Entrada y Salida");
    public static final LogType PER_GATE_SPAN = new LogType(10, "SELECT CONCAT(emp.first_name, ' ', emp.last_name,' Día: ', s.event_day) FROM per_gate_span s INNER JOIN per_employee emp ON emp.id = s.emp_id WHERE s.id = ?1 ", "Eventos Asistencia Adicionales");
    public static final LogType PER_GATE_EVENT = new LogType(11, "SELECT CONCAT(emp.first_name, ' ', emp.last_name,' Día: ', e.event_day) FROM per_gate_event e INNER JOIN per_employee emp ON emp.id = e.emp_id  WHERE e.id = ?1 ", "Eventos Asistencia Programados");
    public static final LogType PER_VACATION = new LogType(12, "SELECT CONCAT(e.first_name,' ', e.last_name, ' Grupo: ', COALESCE(gv.name,'N/A'), ' Descripción: ', v.observation) FROM per_vacation v LEFT JOIN per_employee e ON e.id = v.employee_id LEFT JOIN per_group_vacation gv ON gv.id = v.group_vacation_id  WHERE v.id = ?1 ", "Vacaciones");
    public static final LogType PER_SURCHARGE = new LogType(13, "SELECT CONCAT(e.first_name,' ', e.last_name,' Duracion: ',s.approved_time/3600, ' registrado: ',s.reg_date ) FROM per_surcharge s INNER JOIN per_employee e ON e.id = s.emp_id WHERE s.id = ?1  ", "Recargos");
    public static final LogType PER_ERROR_DOCUMENT = new LogType(14, "SELECT CONCAT(e.first_name,' ', e.last_name) FROM per_log l INNER JOIN per_employee e ON e.id = l.owner_id WHERE l.id = ?1  ", " Error Documento");
    public static final LogType PER_WORKDAY = new LogType(15, "SELECT CONCAT(e.first_name,' ', e.last_name) FROM per_workday w INNER JOIN per_employee e ON e.id = w.emp_id WHERE w.id = ?1  ", " Extras Jornada Trabajo");
    public static final LogType PER_CTR_NOV = new LogType(16, "SELECT t.name  FROM per_nov_type t WHERE t.id = ?1 ", "Novedades del Contrato");
    public static final LogType PER_NOV_TYPE = new LogType(17, "SELECT t.name  FROM per_nov_type t WHERE t.id = ?1 ", "Novedades de Nomina");
    public static final LogType PER_DOC_GRP = new LogType(18, "SELECT g.name FROM per_doc_grp g  WHERE g.id = ?1 ", " Grupo");
    public static final LogType PER_DOC_TYPE = new LogType(19, "SELECT CONCAT(t.name,' Grupo: ', g.name) FROM per_doc_type t INNER JOIN per_doc_grp g ON g.id = t.doc_grp_id WHERE t.id = ?1 ", " Documento");
    public static final LogType PER_DELETE_DOC_GRP = new LogType(20, "SELECT g.name FROM per_doc_grp g  WHERE g.id = ?1 ", " Grupo");
    public static final LogType PER_NOV_CONTRACT = new LogType(21, "SELECT CONCAT(e.first_name, ' ', e.last_name, ' Registro: ', h.change_date) FROM per_contract_hist h INNER JOIN per_contract c ON c.id = h.contract_id INNER JOIN per_employee e ON e.id = c.emp_id WHERE h.id = ?1 ", "Historial Cambios Contrato");
    public static final LogType PER_CHK_TYPE = new LogType(22);
    public static final LogType PER_CHK_VERSION = new LogType(23);
    public static final LogType PER_DELETE_VAC_GRP = new LogType(24, "SELECT g.name FROM per_group_vacation g  WHERE g.id = ?1 ", "Vacaciones Grupales");
    public static final LogType PER_DELETE_LIC_GRP = new LogType(25, "SELECT g.name FROM per_group_licence g  WHERE g.id = ?1 ", "Licencias Grupales");//ultimo
    public static final LogType PER_ACCIDENT = new LogType(26, "SELECT CONCAT(emp.first_name, ' ', emp.last_name, ' Causal: ',c.name, ' Inicio: ',l.reg_date) FROM per_accident l INNER JOIN per_employee emp ON emp.id = l.emp_id  INNER JOIN per_cause c ON c.id = l.cause_id   WHERE l.id = ?1 ", "Accidente");

    private static final PerLog LOG = new PerLog();

    public static void createSystemLog(LogType type, String notes, int empId, Connection ep) throws Exception {
        LOG.createLogNs(null, type, notes, empId, ep);
    }

    public static void createLog(Integer ownerId, LogType type, String notes, int empId, Connection ep) throws Exception {
        LOG.createLogNs(ownerId, type, notes, empId, ep);
    }

    public static String getLogQuery(Integer ownerId, LogType type, String notes, int empId, Connection ep) throws Exception {
        return LOG.getLogQueryNs(ownerId, type, notes, empId, ep);
    }
    

    @Override
    public String getTableName() {
        return "per_log";
    }

}
