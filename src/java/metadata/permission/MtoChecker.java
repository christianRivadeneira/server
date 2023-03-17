package metadata.permission;

import api.sys.model.Menu;
import api.sys.model.Profile;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class MtoChecker extends PermissionChecker {

    @Override
    public List<Integer> checkByEmployee(int employeeId, int profileId, Connection conn) throws Exception {
        if (employeeId == 1) {
            return null;
        } else {
            checkProfile(employeeId, profileId, conn);
            Profile prof = new Profile().select(profileId, conn);
            boolean all;

            switch (prof.menuId) {
                case 145:
                    //flotas
                    all = new MySQLQuery("SELECT all_cities FROM mto_prof_cfg WHERE prof_id = ?1").setParam(1, profileId).getAsBoolean(conn);
                    if (!all) {
                        return getAsList(new MySQLQuery("SELECT city_id FROM mto_city_employee WHERE emp_id = ?1").setParam(1, employeeId), conn);
                    }
                    break;
                case 872:
                    //activos
                    all = new MySQLQuery("SELECT allow_all_cities FROM eqs_prof_cfg WHERE prof_id = ?1").setParam(1, profileId).getAsBoolean(conn);
                    if (!all) {
                        return getAsList(new MySQLQuery("SELECT scc.city_id FROM eqs_perm p "
                                + "INNER JOIN eqs_center c ON p.center_id = c.id "
                                + "INNER JOIN sys_center_city scc ON scc.sys_center_id = c.sys_center_id "
                                + "WHERE p.employee_id = ?1").setParam(1, employeeId), conn);
                    }
                    break;
                case 199:
                    //proveedores
                    all = new MySQLQuery("SELECT !is_agency_check || is_admin FROM prov_prof_cfg WHERE prof_id = ?1").setParam(1, profileId).getAsBoolean(conn);
                    if (!all) {
                        throw new Exception("No autorizado");
                    }
                    break;
                default:
                    Menu mod = new Menu().select(prof.menuId, conn);
                    throw new Exception("El módulo " + mod.label + " no está autorizado.");
            }

            return null;
        }
    }
}
