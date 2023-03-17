package web.gates.cylTrip;

import utilities.logs.Log;
import utilities.logs.LogType;
import java.sql.Connection;

public class InvLog extends Log {

    public static final LogType INV_PATH = new LogType(1, "SELECT CONCAT('Nombre: ', name) FROM inv_path WHERE id = ?1", "Ruta");
    public static final LogType INV_PRICES = new LogType(2, "SELECT CONCAT('Capacidad: ', t.`name`) FROM inv_cyl_price p INNER JOIN cylinder_type t ON t.id = p.cyl_type_id WHERE p.id = ?1", "Precios");
    public static final LogType INV_QUALITY_TEST = new LogType(3, "SELECT CONCAT('NIF: ', LPAD(c.nif_y, 2, '0'), LPAD(c.nif_f, 4, '0'), LPAD(c.nif_s, 6, '0'), ' - Fecha: ', DATE_FORMAT(t.event_date, \"%d/%m/%Y\")) FROM trk_quality_test t INNER JOIN trk_cyl c ON c.id = t.trk_cyl_id WHERE t.id = ?1", "Prueba de Calidad");
    public static final LogType INV_CFG = new LogType(4, "SELECT 'Cfg'", "Cilindros:");
    public static final LogType INV_GOALS_CLI = new LogType(5, "SELECT CONCAT('Centro: ', name) FROM inv_center WHERE id = ?1", "Metas Clientes");
    public static final LogType INV_GOALS_STR = new LogType(6, "SELECT CONCAT('Centro: ', name) FROM inv_center WHERE id = ?1", "Metas Almacenes");
    public static final LogType INV_GOALS_STK = new LogType(7, "SELECT CONCAT('Centro: ', name) FROM inv_center WHERE id = ?1", "Stock");
    public static final LogType INV_MOV = new LogType(8, "SELECT CONCAT('Centro: ', c.name) FROM inv_movement m INNER JOIN inv_center c ON c.id = m.center_id WHERE m.id = ?1", "Movimiento Cilindros");
    public static final LogType INV_MOV_INT = new LogType(9, "SELECT CONCAT('Centro: ', c.name) FROM inv_mov_int m INNER JOIN inv_center c ON c.id = m.center_id WHERE m.id = ?1", "Movimiento Interno Cilindros");
    public static final LogType INV_PLANI = new LogType(10, "SELECT CONCAT('Número: ', num) FROM inv_planilla WHERE id = ?1", "Planilla");
    public static final LogType INV_STORE = new LogType(11, "SELECT CONCAT('Interno: ', internal) FROM inv_store WHERE id = ?1", "Almacenes");
   
    private static final InvLog LOG = new InvLog();

    public static String getLogQuery(Integer ownerId, LogType type, String notes, Integer empId, Connection ep) throws Exception {
        return LOG.getLogQueryNs(ownerId, type, notes, empId, ep);
    }

    
    @Override
    public String getTableName() {
        return "inv_log";
    }
}
