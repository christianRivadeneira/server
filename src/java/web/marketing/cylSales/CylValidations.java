package web.marketing.cylSales;

import api.com.model.ComCfg;
import api.trk.model.TrkCyl;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;
import utilities.MySQLQuery;
import web.ShortException;

/*
**************************
Servlet que se llama desde el app de ventas (escaneo de carga) y operaciones (prellenado), tener en cuenta para 
los cambios. 
**************************
 */
public class CylValidations {

    public String cylError;
    public String fillError;
    public String saleError;
    public String subsidyError;
    public boolean mustCheck;

    public int cylId;
    public int typeId;
    public String typeName;
    public String kgName;
    public BigDecimal tara;

    public Date lastSubDate;
    public Integer days;
    public boolean minasRep;
    public boolean wanted;
    public boolean salable;

    public Integer respId;
    public int lastMtoYears;
    public Date lastMtoDate;
    public Date lastChkDt;
    public Date fabDate;
    public Date freeSubDate;

    public int year;
    public int factory;
    public int serial;

    public String altNif;
    public Integer altCylId;

    public static final String FULL = "com.qualisys.cylSales";
    public static final String SALES = "com.glp.subsidiosonline";
    public static final String TRACKING = "com.qualisys.tracking";

    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");

    //Estas validaciones se debe usar solamente en el momento de vender un cilindro, si se necesita validar
    //caracteristicas de otra app, se deben agregar por aparte
    public static CylValidations getValidations(String nif, Integer empId, boolean getAlternate, String pkgName, Integer invCenterId, Connection conn) throws Exception {
        CylValidations rta = new CylValidations();
        if (!pkgName.equals(SALES) && !pkgName.equals(TRACKING) && !pkgName.equals(FULL)) {
            throw new Exception("La app " + pkgName + " no está autorizada");
        }
        TrkCyl cyl = null;
        try {
            cyl = TrkCyl.selectByNif(nif, conn);
        } catch (Exception ex) {
            System.out.println("Problema con el nif: " + nif + " " + ex.getMessage());
            rta.cylError = ex.getMessage();
        }

        //Boolean reserveCyls = new MySQLQuery("SELECT lock_cyl_sale FROM com_cfg WHERE id = 1").getAsBoolean(conn);
        if (rta.cylError == null) {
            if (cyl != null) {

                /**
                 * No hay bandera general de bloqueo de cilindros por centro.
                 * Cuando se liberan todos los cilindros el invCenterId queda en
                 * null.
                 */
                if ((pkgName.equals(FULL) || pkgName.equals(SALES)) && cyl.invCenterId != null) {
                    Integer smanInvCenterId = new MySQLQuery("SELECT "
                            + "c.inv_center_id "
                            + "FROM employee e "
                            + "LEFT JOIN dto_salesman drv ON drv.driver_id = e.id AND drv.active "
                            + "LEFT JOIN dto_salesman sto ON sto.store_id = e.store_id AND sto.active "
                            + "LEFT JOIN dto_salesman dis ON dis.distributor_id = e.id AND dis.active "
                            + "LEFT JOIN dto_salesman ctr ON ctr.contractor_id = e.contractor_id AND ctr.active "
                            + "INNER JOIN sys_center c ON c.dto_center_id = COALESCE(drv.center_id, sto.center_id, dis.center_id, ctr.center_id) "
                            + "WHERE e.id = " + empId).getAsInteger(conn);

                    if (smanInvCenterId == null) {
                        throw new Exception("El centro del vendedor está pendiente de configurar. Comuníquese con Sistemas.");
                    }

                    String centerName = new MySQLQuery("SELECT name FROM inv_center WHERE id = " + cyl.invCenterId).getAsString(conn);
                    if (!Objects.equals(cyl.invCenterId, smanInvCenterId)) {
                        rta.saleError = "El nif " + nif + " está reservado para el centro " + centerName;
                    } else {
                        new MySQLQuery("UPDATE trk_cyl SET inv_center_id = NULL WHERE id = " + cyl.id).executeUpdate(conn);
                    }
                }

                Integer daysLimit = new MySQLQuery("SELECT days_sale FROM dto_cfg WHERE id = 1").getAsInteger(conn);
                ComCfg comCfg = new ComCfg().select(1, conn);
                rta.year = cyl.nifY;
                rta.factory = cyl.nifF;
                rta.serial = cyl.nifS;
                rta.cylId = cyl.id;
                rta.salable = cyl.salable;
                rta.respId = cyl.respId;
                rta.fabDate = cyl.fabDate;

                Object[] row = new MySQLQuery("SELECT "
                        + "@dt := (SELECT DATE(MAX(date)) FROM trk_sale WHERE !training AND cylinder_id = c.id AND sale_type = 'sub'), "
                        + "DATEDIFF(CURDATE(), @dt), "
                        + "ct.name, "
                        + "IF(ct.id = 7, false, (SELECT COUNT(*) > 0 FROM dto_minas_cyl WHERE y = c.nif_y AND f = c.nif_f AND s = nif_s)), "
                        + "c.cyl_type_id, "
                        + "c.tara,"
                        + "ct.kg,"
                        + "DATE_ADD(@dt, INTERVAL " + (daysLimit + 1) + " DAY) "
                        + "FROM trk_cyl c "
                        + "INNER JOIN cylinder_type ct ON c.cyl_type_id = ct.id "
                        + "WHERE c.id = " + rta.cylId).getRecord(conn);

                if (comCfg.phantomNif) {
                    boolean noRotCyl = new MySQLQuery("SELECT COUNT(*) > 0 FROM trk_no_rot_cyls WHERE cyl_id = " + cyl.id).getAsBoolean(conn);
                    if (noRotCyl && (pkgName.equals(FULL) || pkgName.equals(SALES))) {
                        rta.saleError = "NIf " + nif + " sin rotación. Debe pasar primero por plataforma. ";
                    } else if (noRotCyl && pkgName.equals(TRACKING)) {
                        new MySQLQuery("DELETE FROM trk_no_rot_cyls WHERE cyl_id = " + cyl.id).executeDelete(conn);
                    }
                }
                rta.wanted = new MySQLQuery("SELECT COUNT(*) > 0 FROM trk_cyl_wanted WHERE cyl_id = " + rta.cylId + " AND aprov_dt IS NULL").getAsBoolean(conn);
                if (rta.wanted && pkgName.equals(TRACKING)) {
                    new MySQLQuery("UPDATE trk_cyl_wanted SET find_dt = NOW(), find_id = " + empId + " WHERE cyl_id = " + rta.cylId + " AND aprov_dt IS NULL").executeUpdate(conn);
                }
                rta.lastMtoDate = new MySQLQuery("SELECT MAX(`date`) FROM trk_mto WHERE trk_cyl_id = " + rta.cylId).getAsDate(conn);
                rta.lastChkDt = new MySQLQuery("SELECT DATE(MAX(dt)) FROM trk_check WHERE trk_cyl_id = " + rta.cylId + " AND DATE(dt) < CURDATE()").getAsDate(conn);

                if (rta.lastMtoDate == null) {
                    rta.lastMtoDate = rta.fabDate;
                }

                GregorianCalendar gc = new GregorianCalendar();
                gc.setTime(rta.lastMtoDate);

                rta.lastMtoYears = Period.between(LocalDate.of(gc.get(GregorianCalendar.YEAR), gc.get(GregorianCalendar.MONTH) + 1, gc.get(GregorianCalendar.DAY_OF_MONTH)), LocalDate.now()).getYears();
                rta.lastSubDate = MySQLQuery.getAsDate(row[0]);
                rta.days = MySQLQuery.getAsInteger(row[1]);
                rta.typeName = MySQLQuery.getAsString(row[2]);
                rta.minasRep = MySQLQuery.getAsBoolean(row[3]);
                rta.typeId = MySQLQuery.getAsInteger(row[4]);
                rta.tara = MySQLQuery.getAsBigDecimal(row[5], true);
                rta.kgName = MySQLQuery.getAsString(row[6]);
                rta.freeSubDate = MySQLQuery.getAsDate(row[7]);

                //lockCylSale es reservar cilindros para el vendedor
                if (comCfg.lockCylSale && (pkgName.equals(FULL) || pkgName.equals(SALES))) {
                    try {
                        checkSalable(rta.salable, rta.respId, empId, nif, comCfg, conn);
                    } catch (ShortException ex) {
                        rta.saleError = ex.getMessage();
                    }
                }

                if (comCfg.subsidy) {
                    if (rta.typeId == 7) {
                        rta.subsidyError = "Los cilindros de " + rta.typeName + " son solo para venta full";
                    } else {
                        if ((rta.days != null && daysLimit > 0) && rta.days <= daysLimit) {
                            if ((pkgName.equals(FULL) || pkgName.equals(SALES))) {
                                if (!comCfg.phantomNif) {
                                    rta.subsidyError = "Deben pasar " + daysLimit + " días entre subsidios. El NIF " + nif + " quedará libre el " + SDF.format(rta.freeSubDate);
                                } else {
                                    if (getAlternate) {
                                        Object[] altCylRow = getAlternativeNif(cyl.cylTypeId, empId, conn);
                                        rta.altCylId = MySQLQuery.getAsInteger(altCylRow[0]);
                                        rta.altNif = MySQLQuery.getAsString(altCylRow[1]);
                                    }
                                }
                            } else {
                                if (!comCfg.phantomNif) {
                                    rta.subsidyError = "Deben pasar " + daysLimit + " días entre subsidios. El NIF " + nif + " quedará libre el " + SDF.format(rta.freeSubDate);
                                }
                            }
                        }

                        if (!rta.minasRep) {
                            rta.subsidyError = "El nif " + nif + " no se ha reportado al ministerio de minas";
                        }
                    }
                }

                if (rta.wanted) {
                    rta.fillError = "Se ha ordenado la recolección del nif " + nif;
                }

                if (rta.lastMtoYears > 10) {
                    rta.fillError = "El nif " + nif + " debe ir a mantenimiento";
                }

                if (pkgName.equals(TRACKING) && empId != 1 && invCenterId != null) {
                    if (new MySQLQuery("SELECT lock_cyls FROM inv_center WHERE id = " + invCenterId).getAsBoolean(conn)) {
                        new MySQLQuery("UPDATE trk_cyl SET inv_center_id = " + invCenterId + " WHERE id = " + cyl.id).executeUpdate(conn);
                    }
                }

                //si ha pasado éste número de meses desde el último check, es obligatorio que llenen el cuestionario
                Integer chkLimitMonths = new MySQLQuery("SELECT chk_limit_months FROM inv_cfg c").getAsInteger(conn);

                gc = new GregorianCalendar();
                gc.add(GregorianCalendar.MONTH, -chkLimitMonths);
                Date begDate = (rta.lastChkDt != null ? rta.lastChkDt : rta.fabDate);
                rta.mustCheck = begDate.before(gc.getTime());
            } else {
                rta.cylError = "El cilindro " + nif + " no está registrado en " + new MySQLQuery("SELECT `name` FROM enterprise WHERE !alternative").getAsString(conn) + ".";
            }
        }
        return rta;
    }

    public synchronized static Object[] getAlternativeNif(int cylTypeId, int empId, Connection conn) throws Exception {
        Object[] result = new MySQLQuery("SELECT "
                + "nr.cyl_id, "
                + "CONCAT(LPAD(c.nif_y, 2, 0), LPAD(c.nif_f, 4, 0), LPAD(c.nif_s, 6, 0)) "
                + "FROM trk_no_rot_cyls nr "
                + "INNER JOIN trk_cyl c ON nr.cyl_id = c.id "
                + "WHERE "
                + "c.cyl_type_id = " + cylTypeId + " "
                + "AND nr.assign_date IS NULL "
                + "ORDER BY nr.id ASC "
                + "LIMIT 1").getRecord(conn);
        if (result == null || result.length == 0) {
            throw new Exception("No fue posible obtener el cilindro alterno. Comuníquese con Sistemas.");
        }
        new MySQLQuery("UPDATE trk_no_rot_cyls SET assign_date = NOW(), resp_id = " + empId + " "
                + "WHERE cyl_id = " + result[0]).executeUpdate(conn);

        return result;
    }

    private static void checkSalable(boolean salable, Integer respId, Integer empId, String nif, ComCfg cfg, Connection conn) throws ShortException, Exception {
        if (!salable) {
            if (respId == null) {
                if (cfg.chkPlatfBeforeSale) {
                    throw new ShortException("El cilindro " + nif + " fue vendido y no ha pasado por plataforma.");
                } else {
                    return;
                }
            }
            if (!Objects.equals(respId, empId)) {
                int origEmpId = empId;
                //dado el ID del employee consigue el dto_salesman y le extrae el liquidator
                Integer smanId = new MySQLQuery("SELECT COALESCE(dist.liquidator_id, drv.liquidator_id, sto.liquidator_id, ctr.liquidator_id) "
                        + "FROM employee e "
                        + "LEFT JOIN dto_salesman drv ON drv.driver_id = e.id AND drv.active "
                        + "LEFT JOIN dto_salesman sto ON sto.store_id = e.store_id AND sto.active "
                        + "LEFT JOIN dto_salesman dist ON dist.distributor_id = e.id AND dist.active "
                        + "LEFT JOIN dto_salesman ctr ON ctr.contractor_id = e.contractor_id AND ctr.active "
                        + "WHERE e.id = " + empId).getAsInteger(conn);
                if (smanId != null) {
                    //dado el id del liquidator que es un salesman, obtiene su employee.id
                    empId = new MySQLQuery("SELECT COALESCE(sm.driver_id, sto.id, sm.distributor_id, ctr.id) "
                            + "FROM dto_salesman sm "
                            + "LEFT JOIN employee sto ON sto.store_id = sm.store_id AND sto.active "
                            + "LEFT JOIN employee ctr ON ctr.contractor_id = sm.contractor_id AND ctr.active "
                            + "WHERE sm.id = " + smanId).getAsInteger(conn);
                }

                if (!Objects.equals(empId, respId)) {
                    //si no lo consigue por liquidador y vendedor, lo intenta buscar por el trasladador
                    Integer stoId = new MySQLQuery("SELECT COALESCE(e.store_id, liq.store_id) "
                            + "FROM employee e "
                            + "LEFT JOIN dto_salesman dist ON dist.distributor_id = e.id AND dist.active "
                            + "LEFT JOIN dto_salesman liq ON dist.liquidator_id = liq.id AND liq.active "
                            + "WHERE "
                            + "e.id = " + origEmpId).getAsInteger(conn);

                    if (stoId != null) {
                        empId = new MySQLQuery("SELECT "
                                + "dv.driver_id "
                                + "FROM "
                                + "com_store_order o "
                                + "INNER JOIN driver_vehicle dv ON o.vh_id = dv.vehicle_id "
                                + "WHERE "
                                + "o.store_id = " + stoId + " "
                                + "AND o.cancel_dt IS NULL "
                                + "AND dv.`end` IS NULL "
                                + "ORDER BY o.taken_dt DESC "
                                + "LIMIT 1").getAsInteger(conn);
                    }
                }

                if (!Objects.equals(empId, respId)) {
                    //En caso de llegar a este punto, vamos a verificar si el cilindro proviene de un recargue en pulmón
                    //El recargue se evalua por la fecha actual porque el vendedor solo tiene 15 minutos luego de recargar para escanear.
                    empId = null;
                    Object[][] relData = new MySQLQuery("SELECT "
                            + "pul.driver_id "
                            + "FROM "
                            + "gt_cyl_trip pul "
                            + "INNER JOIN gt_trip_type tt ON pul.type_id = tt.id "
                            + "INNER JOIN gt_reload r ON r.pulm_trip_id = pul.id "
                            + "INNER JOIN gt_cyl_trip sale ON r.vh_trip_id = sale.id "
                            + "WHERE "
                            + "tt.pul "
                            + "AND !pul.cancel "
                            + "AND DATE(r.reload_date) = CURDATE() "
                            + "AND sale.driver_id = " + origEmpId).getRecords(conn);
                    
                    for (int i = 0; i < relData.length; i++) {
                        if (MySQLQuery.getAsInteger(relData[i][0]).equals(respId)) {
                            empId = MySQLQuery.getAsInteger(relData[i][0]);
                            break;
                        }
                    }
                }
            }
            if (!Objects.equals(empId, respId)) {
                String respName = new MySQLQuery("SELECT CONCAT('El ', "
                        + "CASE "
                        + "WHEN drv.id IS NOT NULL THEN 'vendedor' "
                        + "WHEN dis.id IS NOT NULL THEN 'distribuidor' "
                        + "WHEN sto.id IS NOT NULL THEN 'almacén' "
                        + "WHEN ctr.id IS NOT NULL THEN 'contratista' "
                        + "ELSE 'usuario' "
                        + "END, ' ',"
                        + "CONCAT(e.first_name, ' ', e.last_name), "
                        + "IF(dc.id IS NOT NULL, CONCAT(' de ',dc.name),'')) "
                        + "FROM employee e "
                        + "LEFT JOIN dto_salesman drv ON drv.driver_id = e.id AND drv.active "
                        + "LEFT JOIN dto_salesman dis ON dis.distributor_id = e.id AND dis.active "
                        + "LEFT JOIN dto_salesman sto ON sto.store_id = e.store_id AND sto.active "
                        + "LEFT JOIN dto_salesman ctr ON ctr.contractor_id = e.contractor_id AND ctr.active "
                        + "LEFT JOIN dto_center dc ON dc.id = COALESCE(drv.center_id, dis.center_id, sto.center_id, ctr.center_id) "
                        + "WHERE e.id = " + respId).getAsString(conn);
                throw new ShortException("El cilindro " + nif + " está reservado por\n" + respName);
            }
        }
    }

}
