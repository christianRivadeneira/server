package api.mss.app;

import static api.mss.model.MssGuard.getSuperIdFromEmployee;
import api.mss.dto.MssPostApp;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import utilities.MySQLQuery;

public class GetPostList {

    public static List<MssPostApp> GetListPostV1(int empId, Connection conn) throws Exception {

        Integer superId = getSuperIdFromEmployee(empId, conn);

        if (superId == null) {
            throw new Exception("No se encuentra registrado como supervisor en el sistema, Comuniquese con sistemas.");
        }

        List<MssPostApp> posts = new ArrayList<>();

        String selectData = " pu.id, pu.name, cl.name, "
                + " mp.lat, mp.lon, pt.id, IF(path_id IS NULL, 1, 0) AS eventual, "
                + " pt.beg_dt, pt.end_dt, "
                + " pt.arrival_dt, "
                + " (SELECT COUNT(*) FROM mss_super_review sr WHERE sr.prog_id = pt.id), "
                + " (SELECT COUNT(*) "
                + " FROM mss_shift s "
                + " INNER JOIN mss_guard g ON g.id = s.guard_id "
                + " WHERE NOW() > s.exp_beg AND NOW() < s.exp_end AND s.post_id = pu.id AND s.active AND s.reg_beg IS NOT NULL AND s.reg_end IS NULL) ";

        Object[][] inProgData = new MySQLQuery("SELECT " + selectData
                + " FROM mss_super_prog pt "
                + " INNER JOIN mss_post pu ON pu.id = pt.post_id "
                + " INNER JOIN mss_client cl ON cl.id = pu.client_id "
                + " LEFT JOIN mss_point mp ON mp.post_id = pu.id AND mp.is_check "
                + " WHERE CURDATE() BETWEEN pt.beg_dt AND pt.end_dt AND pt.super_id = ?1 "
                + " AND pu.active AND cl.active ORDER BY pt.beg_dt DESC, eventual ASC"
        ).setParam(1, superId).getRecords(conn);

        if (inProgData == null || inProgData.length == 0) {
            return posts;
        }

        for (Object[] row : inProgData) {
            MssPostApp obj = new MssPostApp();
            obj.postId = MySQLQuery.getAsInteger(row[0]);
            obj.postName = MySQLQuery.getAsString(row[1]);
            obj.clientName = MySQLQuery.getAsString(row[2]);
            obj.lat = MySQLQuery.getAsBigDecimal(row[3], false);
            obj.lon = MySQLQuery.getAsBigDecimal(row[4], false);
            obj.progId = MySQLQuery.getAsInteger(row[5]);
            obj.isEventual = MySQLQuery.getAsBoolean(row[6]);
            obj.begDate = MySQLQuery.getAsDate(row[7]);
            obj.endDate = MySQLQuery.getAsDate(row[8]);
            obj.arrivalDate = MySQLQuery.getAsDate(row[9]);
            obj.done = MySQLQuery.getAsInteger(row[10]);
            obj.numGuards = MySQLQuery.getAsInteger(row[11]);
            posts.add(obj);
        }

        return posts;

    }

}
