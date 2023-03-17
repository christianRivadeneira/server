package api.mto.api;

import api.BaseAPI;
import java.sql.Connection;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import utilities.MySQLQuery;

@Path("/mtoChkList")
public class MtoChkListlApi extends BaseAPI {

    @PUT
    @Path("/{lstId}/updateChkStatus")
    public Response updateChkStatus(@PathParam("lstId") int lstId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            boolean stateByAsk = new MySQLQuery("SELECT v.state_by_ask from "
                    + "mto_chk_lst l "
                    + "INNER JOIN mto_chk_version v ON v.id = l.version_id "
                    + "WHERE l.id = " + lstId).getAsBoolean(conn);

            String responName = new MySQLQuery("SELECT CONCAT(e.first_name, ' ' , IFNULL(e.last_name, '')) FROM employee e "
                    + "INNER JOIN mto_chk_lst l ON l.driver_id = e.id WHERE l.id = " + lstId).getAsString(conn);

            //+ "IF(tbl.ssv='empty', 'empty','ok'))) AS state FROM "
            if (stateByAsk) {
                new MySQLQuery("UPDATE mto_chk_lst SET state = "
                        + "(SELECT state FROM "
                        + "((SELECT "
                        + "IF(tbl.ctx >= serr AND tbl.ssv='error','error', "
                        + "IF(tbl.ctx >= swar AND tbl.ssv='error','warn', "
                        + "'ok')) AS state FROM "
                        + "(SELECT mto_chk_val.state AS ssv, ask.id, COUNT(ask.id) as ctx, ask.status_error AS serr, ask.status_warning AS swar "
                        + "FROM mto_chk_val "
                        + "INNER JOIN mto_chk_row ON mto_chk_val.row_id = mto_chk_row.id "
                        + "INNER JOIN mto_chk_ask_type ask ON mto_chk_row.ask_type_id = ask.id "
                        + "WHERE mto_chk_val.lst_id = " + lstId + " "
                        + "AND mto_chk_row.type <> 'tit' "
                        + "AND (mto_chk_row.mandatory = 1 OR mto_chk_val.col_id IS NOT NULL) "
                        + "GROUP BY ask.id,mto_chk_val.state "
                        + ") AS tbl "
                        + ") "
                        + "UNION ALL "
                        + "( "
                        + "SELECT IF(!checked, 'error', "
                        + "IF(review AND !mto_chk_element.optional_review, IF(DATE(dt) <= DATE(rev_date), 'ok', 'error'), 'ok')) "
                        + "FROM mto_chk_element "
                        + "INNER JOIN mto_element ON mto_chk_element.elem_id = mto_element.id "
                        + "INNER JOIN mto_chk_lst ON mto_chk_element.lst_id = mto_chk_lst.id "
                        + "WHERE mto_chk_lst.id = " + lstId + ")) AS l "
                        + "ORDER BY "
                        + "IF(state = 'empty', 1, IF(state = 'error', 2, IF(state = 'warn', 3, IF(state = 'ok', 4, 4)))) "
                        + "LIMIT 0,1) WHERE id = " + lstId).executeUpdate(conn);
            } else {
                new MySQLQuery("UPDATE mto_chk_lst SET state = "
                        + "(SELECT state FROM "
                        + "((SELECT state "
                        + "FROM "
                        + "mto_chk_val "
                        + "INNER JOIN mto_chk_row ON mto_chk_val.row_id = mto_chk_row.id "
                        + "WHERE mto_chk_val.lst_id = " + lstId + " "
                        + "AND mto_chk_row.type <> 'tit' "
                        + "AND (mto_chk_row.mandatory = 1 OR mto_chk_val.col_id IS NOT NULL)) "
                        + "UNION ALL "
                        + "(SELECT "
                        + "IF(!checked, 'error', "
                        + "IF(review AND !mto_chk_element.optional_review, IF(DATE(dt) <= DATE(rev_date), 'ok', 'error'), 'ok')) "
                        + "FROM "
                        + "mto_chk_element "
                        + "INNER JOIN mto_element ON mto_chk_element.elem_id = mto_element.id "
                        + "INNER JOIN mto_chk_lst ON mto_chk_element.lst_id = mto_chk_lst.id "
                        + "WHERE "
                        + "mto_chk_lst.id = " + lstId + ")) AS l "
                        + "ORDER BY IF(state = 'empty', 1, if(state = 'error', 2, if(state = 'warn', 3, if(state = 'ok', 4, 4)))) "
                        + "LIMIT 0,1) WHERE id = " + lstId).executeUpdate(conn);
            }

            new MySQLQuery("UPDATE mto_chk_lst SET is_ok = ("
                    + " SELECT IF( "
                    + " (SELECT COUNT(*)>0 "
                    + " FROM mto_chk_element AS e "
                    + " INNER JOIN mto_element AS me ON me.id = e.elem_id "
                    + " WHERE "
                    + " e.lst_id = " + lstId + " AND "
                    + " !e.optional_review AND "
                    + " IF(e.need_review, e.rev_date IS NULL, !e.checked) AND "
                    + " (e.work_order_id IS NULL AND e.corr_date IS NULL)) "
                    + " OR "
                    + " (SELECT COUNT(*)>0 "
                    + " FROM mto_chk_val AS v "
                    + " INNER JOIN mto_chk_row AS r ON r.id = v.row_id "
                    + " WHERE "
                    + " v.lst_id = " + lstId + " AND "
                    + " v.state NOT IN ('empty', 'ok') AND "
                    + " r.mandatory = 1 AND r.type <> 'tit' AND "
                    + " (v.corr_date IS NULL AND v.work_order_id IS NULL)),0,1) "
                    + ") WHERE id = " + lstId).executeUpdate(conn);

            new MySQLQuery("UPDATE mto_chk_lst SET cda_respon = false, respon_name = '', "
                    + "respon_job = '' WHERE id = " + lstId + " AND state <> 'error'").executeUpdate(conn);
            new MySQLQuery("UPDATE mto_chk_lst SET respon_name = '" + responName + "', "
                    + "respon_job = 'Conductor' WHERE id = " + lstId + " AND state = 'error' "
                    + "AND (respon_name = '' OR respon_name IS NULL)").executeUpdate(conn);

            return Response.ok().build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
