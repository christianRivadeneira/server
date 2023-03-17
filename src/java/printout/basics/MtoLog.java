package printout.basics;

import utilities.logs.Log;
import utilities.logs.LogType;
import java.sql.Connection;
import java.util.Date;

public class MtoLog extends Log {

    public static final LogType VEHICLE = new LogType(1, "SELECT CONCAT('Placa: ',plate) FROM vehicle WHERE id = ?1" , "Vehículo");
    public static final LogType DRIVER = new LogType(2, "SELECT CONCAT(first_name, ' ', last_name) FROM employee WHERE id = ?1" , "Conductor");
    public static final LogType VH_ELEMENT = new LogType(3, "SELECT CONCAT('Placa ',v.plate,' ,',me.description) FROM mto_veh_element mt LEFT JOIN vehicle v ON v.id = mt.vehicle_id LEFT JOIN mto_element me ON me.id = mt.element_id WHERE mt.id = ?1 ", "Elemento Vehículo");
    public static final LogType DEPOSIT = new LogType(4,"SELECT CONCAT(md.deposit_date , ' ' , md.value , ' ' , md.notes ) FROM mto_deposit md WHERE md.id = ?1 ", "Depósito");
    public static final LogType SYSTEM = new LogType(5);
    public static final LogType VH_DOCUMENT = new LogType(6, "SELECT CONCAT('Placa ',v.plate) FROM document_vehicle dt LEFT JOIN vehicle v ON v.id = dt.vehicle_id WHERE dt.id = ?1 ","Documento Vehículo");
    public static final LogType DRV_DOCUMENT = new LogType(7,"SELECT CONCAT(em.first_name , ' ' , em.last_name) FROM mto_driver_doc mt LEFT JOIN employee em ON em.id = mt.driver_id  WHERE mt.id = ?1","Documento Conductor");
    public static final LogType FUEL_LOAD = new LogType(8,"SELECT CONCAT(em.first_name , ' ' , em.last_name, ', Placa ' , v.plate) FROM fuel_load t LEFT JOIN employee em ON em.id = t.driver_id LEFT JOIN vehicle v ON v.id = t.vehicle_id WHERE t.id = ?1", "Tanqueo");
    public static final LogType WORK_ORDER = new LogType(9, "SELECT CONCAT(w.bill_num, ' ', v.plate) FROM work_order w INNER JOIN vehicle v ON v.id = w.vehicle_id WHERE w.id = ?1");
    public static final LogType ITEM = new LogType(10,"SELECT CONCAT(t.description ) FROM item t WHERE t.id = ?1" , "Item");
    public static final LogType WORK_MAINT_TASK = new LogType(11);
    public static final LogType VH_NOVS = new LogType(12, "SELECT CONCAT('Placa ' , v.plate , ', ', t.note ) FROM mto_vh_note t LEFT JOIN vehicle v ON v.id = t.vehicle_id WHERE t.id = ?1","Novedad Vehículo");
    public static final LogType VH_ACCIDENT = new LogType(13,"SELECT CONCAT(em.first_name , ' ' , em.last_name ,', Placa ' , v.plate , ', ', t.place ) FROM mto_accident t LEFT JOIN employee em ON em.id = t.employee_id LEFT JOIN vehicle v ON v.id = t.vehicle_id WHERE t.id = ?1", "Accidente");
    public static final LogType VH_OUT_SERVICE = new LogType(14,"SELECT CONCAT('Placa ',v.plate ,  IF(t.comments IS NOT NULL , CONCAT(' ,',t.comments),'')) FROM mto_out_service t LEFT JOIN vehicle v ON v.id = t.vehicle_id WHERE t.id = ?1","Fuera de Servicio");
    public static final LogType VH_CONTRACTOR = new LogType(15,"SELECT CONCAT('Placa ',v.plate ,  IF(t.notes IS NOT NULL , CONCAT(' ,',t.notes),'')) FROM mto_veh_contractor t LEFT JOIN vehicle v ON v.id = t.vehicle_id WHERE t.id = ?1","Movimientos Contratistas");
    public static final LogType FORMATS = new LogType(16, "SELECT CONCAT(em.first_name , ' ' , em.last_name ,' ,Placa ',v.plate , ' ,' , t.notes) FROM mto_chk_lst t LEFT JOIN vehicle v ON v.id = t.vh_id LEFT JOIN employee em ON em.id = t.driver_id WHERE t.id = ?1","Formatos");
    public static final LogType CONTRACTOR = new LogType(17, "SELECT CONCAT(t.first_name , ' ' ,t.last_name) FROM mto_contractor t WHERE t.id = ?1","Contratistas");
    public static final LogType STORE = new LogType(18,"SELECT CONCAT(t.name) FROM mto_store t WHERE t.id = ?1","Almacen");
    public static final LogType REFERENCES = new LogType(19,"SELECT CONCAT(t.name) FROM mto_store_ref t WHERE t.id = ?1","Referencia");
    public static final LogType STORE_TERC = new LogType(20,"SELECT CONCAT(t.name) FROM mto_store_terc t WHERE t.id = ?1","Tercero");
    public static final LogType MTO_STORE_MV = new LogType(21);
    public static final LogType AGENCY = new LogType(22,"SELECT CONCAT(ent.name,' ' ,IF(ent.address IS NOT NULL , CONCAT(', ',ent.address),'') ) FROM agency t LEFT JOIN enterprise ent ON ent.id = t.enterprise_id WHERE t.id = ?1","Agencias");
    public static final LogType CENTER_COSTS = new LogType(23, "SELECT CONCAT(t.name ) FROM mto_center_cost t WHERE t.id = ?1", "Centros de Costo");
    public static final LogType VEH_CLASS = new LogType(24,"SELECT CONCAT(t.name ) FROM vehicle_class t WHERE t.id = ?1","Clases de Vehículos");
    public static final LogType VEH_TYPE = new LogType(25, "SELECT CONCAT(t.name ) FROM vehicle_type t WHERE t.id = ?1", "Tipo Vehículo");
    public static final LogType MAINT_TASK = new LogType(26, "SELECT t.description FROM maint_task t WHERE t.id = ?1", "Tareas de Mantenimiento");
    public static final LogType MTO_AREA = new LogType(27, "SELECT t.name FROM area t WHERE t.id = ?1", "Área");
    public static final LogType MTO_CITY_EMP = new LogType(28,"SELECT CONCAT(IF(em.first_name IS NOT NULL ,em.first_name,''),' ', IF(em.last_name IS NOT NULL ,em.last_name,'')) FROM mto_city_employee t LEFT JOIN employee em ON em.id = t.emp_id  WHERE t.id = ?1","Encargados Ciudad");
    public static final LogType MTO_FUEL_TYPE = new LogType(29,"SELECT t.name FROM fuel_type t WHERE t.id = ?1","Tipo Combustible");
    public static final LogType MTO_FLOW = new LogType(30, "SELECT CONCAT(t.name) FROM mto_flow t WHERE t.id = ?1" , "Flujos");
    public static final LogType MTO_FLOW_STEP = new LogType(31,"SELECT CONCAT(em.first_name , ' ' , em.last_name , ' ,',t.name) FROM mto_flow_step t LEFT JOIN employee em ON em.id = t.emp_id  WHERE t.id = ?1", "Pasos de Aprobación");
    public static final LogType MTO_VEH_AGENCY = new LogType(32,"SELECT CONCAT(em.first_name , ' ' , em.last_name , ' ,',t.name) FROM mto_flow_step t LEFT JOIN employee em ON em.id = t.emp_id  WHERE t.id = ?1", "Agencia");
    public static final LogType MTO_CHK_ORDER = new LogType(33);    
    public static final LogType MTO_ORD_ITEM = new LogType(34);  
    public static final LogType MTO_CFG = new LogType(35);//último creado  

    private static final MtoLog LOG = new MtoLog();

    public static void createSystemLog(LogType type, String notes, int empId, Connection ep) throws Exception {
        LOG.createLogNs(null, type, notes, empId, ep);
    }

    public static void createLog(Integer ownerId, LogType type, String notes, int empId, Connection ep) throws Exception {
        LOG.createLogNs(ownerId, type, notes, empId, ep);
    }

    public static String getLogQuery(Integer ownerId, LogType type, String notes, int empId, Connection ep) throws Exception {
        return LOG.getLogQueryNs(ownerId, type, notes, empId, ep);
    }

    public static String getLogQuery(Integer ownerId, LogType type, String notes, Date d, int empId, Connection ep) throws Exception {
        return LOG.getLogQueryNs(ownerId, type, notes, d, empId, ep);
    }

    @Override
    public String getTableName() {
        return "mto_log";
    }
}
