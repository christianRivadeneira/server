package metadata.permission;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import utilities.MySQLQuery;

public abstract class PermissionChecker {

    public abstract List<Integer> checkByEmployee(int employeeId, int profileId, Connection conn) throws Exception;

    public void checkProfile(int employeeId, int profileId, Connection conn) throws Exception {
        if (new MySQLQuery("SELECT COUNT(*)=0 FROM login WHERE profile_id = ?1 AND employee_id = ?2").setParam(1, profileId).setParam(2, employeeId).getAsBoolean(conn)) {
            throw new Exception("No tiene el perfil requerido");
        }
    }

    public List<Integer> getAsList(MySQLQuery q, Connection conn) throws Exception {
        Object[][] data = q.getRecords(conn);
        List<Integer> rta = new ArrayList<>();
        for (Object[] row : data) {
            rta.add(MySQLQuery.getAsInteger(row[0]));
        }
        return !rta.isEmpty() ? rta : null;
    }

    public static String getAsString(List<Integer> lst) {
        if (lst == null || lst.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lst.size(); i++) {
            sb.append(lst.get(i));
            if (i < lst.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
