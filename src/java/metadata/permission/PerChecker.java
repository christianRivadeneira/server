package metadata.permission;

import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class PerChecker extends PermissionChecker {

    @Override
    public List<Integer> checkByEmployee(int employeeId, int profileId, Connection conn) throws Exception {
        checkProfile(employeeId, profileId, conn);
        if (employeeId == 1) {
            return null;
        } else {
            if (new MySQLQuery("SELECT all_cities FROM mto_prof_cfg WHERE profile_id = ?1").setParam(1, profileId).getAsBoolean(conn)) {
                return null;
            } else {
                return getAsList(new MySQLQuery("SELECT city_id FROM mto_city_employee WHERE emp_id = ?1").setParam(1, employeeId), conn);
            }
        }
    }

}
