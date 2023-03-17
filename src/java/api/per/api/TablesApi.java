package api.per.api;

import api.BaseAPI;
import java.sql.Connection;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import utilities.MySQLQuery;

@Path("/tables")
public class TablesApi extends BaseAPI {

    @GET
    @Path("/perEntity")
    public Response perInsClaimGroupedByEntity(){
        try(Connection con = getConnection()) {
            MySQLQuery q = getPerEntityQuery();
            List<PerTableRow> tableRows = PerTableRow.getList(q, con);

            return createResponse(tableRows);
        } catch (Exception e) {
            e.printStackTrace();
            return createResponse(e);
        }
    }

    private MySQLQuery getPerEntityQuery(){
        MySQLQuery q = new MySQLQuery("SELECT nombre, vlrRad, vlrAprob, vlrPend, tipo " +
            "FROM ( " +
            "	SELECT  " +
            "       'eps' as tipo, " +
            "		pe.NAME AS nombre, " +
            "		SUM(COALESCE(pic.vlr_rad, 0)) AS vlrRad, " +
            "		SUM(COALESCE(pic.vlr_aprob, 0)) AS vlrAprob, " +
            "		SUM(COALESCE(pic.vlr_pend, 0)) AS vlrPend " +
            "	FROM per_ins_claim pic  " +
            "	JOIN per_entity pe ON pic.eps_id = pe.id " +
            "	JOIN per_sick_leave psl ON psl.id = pic.sick_id " +
            "	WHERE psl.active = 1 " +
            "   AND pic.state <> 'cancel' " +
            "   AND pic.state <> 'closed' " +
            "	GROUP BY pe.id " +
            "	UNION " +
            "	SELECT " +
            "       'arl' as tipo, " +
            "		pe.NAME AS nombre, " +
            "		SUM(COALESCE(pic.vlr_rad, 0)) AS vlrRad, " +
            "		SUM(COALESCE(pic.vlr_aprob, 0)) AS vlrAprob, " +
            "		SUM(COALESCE(pic.vlr_pend, 0)) AS vlrPend " +
            "	FROM per_ins_claim pic " +
            "	JOIN per_entity pe ON pic.arl_id = pe.id " +
            "	JOIN per_accident pa ON pic.acc_id = pa.id " +
            "	WHERE pa.active = 1 " +
            "   AND pic.state <> 'cancel' " +
            "   AND pic.state <> 'closed' " +
            "	GROUP BY pe.id " +
            ") " +
            "AS per_ins_claim_entity " +
            "GROUP BY nombre " +
            "ORDER BY nombre ");

        return q;
    }

    @GET
    @Path("/perAccident")
    public Response perInsClaimGroupedByCauseAccident(){
        try(Connection con = getConnection()) {
            MySQLQuery q =getPerCauseAccidentQuery();
            List<PerTableRow> tableRows = PerTableRow.getList(q, con);
            return createResponse(tableRows);
        } catch (Exception e) {
            e.printStackTrace();
            return createResponse(e);
        }
    }

    private MySQLQuery getPerCauseAccidentQuery(){
        MySQLQuery q = new MySQLQuery("SELECT * " +
            "FROM " +
            "( " +
            "	SELECT " +
            "		pc.NAME, " +
            "		SUM(COALESCE(pic.vlr_rad, 0)), " +
            "		SUM(COALESCE(pic.vlr_aprob, 0)), " +
            "		SUM(COALESCE(pic.vlr_pend, 0)), " +
            "       'acc' as type " +
            "	FROM per_ins_claim pic " +
            "	JOIN per_accident pa ON pa.id = pic.acc_id " +
            "	JOIN per_cause pc ON pc.id = pa.cause_id " +
            "	WHERE pa.active = 1 " +
            "   AND pic.state <> 'cancel' " +
            "   AND pic.state <> 'closed' " +
            "	GROUP BY pc.id " +
            "	UNION " +
            "	SELECT " +
            "		pc.NAME, " +
            "		SUM(COALESCE(pic.vlr_rad, 0)), " +
            "		SUM(COALESCE(pic.vlr_aprob, 0)), " +
            "		SUM(COALESCE(pic.vlr_pend, 0)), " +
            "       'sick' as type " +
            "	FROM per_ins_claim pic " +
            "	JOIN per_sick_leave pa ON pa.id = pic.sick_id " +
            "	JOIN per_cause pc ON pc.id = pa.cause_id " +
            "	WHERE pa.active = 1 " +
            "   AND pic.state <> 'cancel' " +
            "   AND pic.state <> 'closed' " +
            "	GROUP BY pc.id " +
            ") AS per_ins_claim_by_type " +
            "ORDER BY name ");

        return q;
    }

    @GET
    @Path("/perSickLeave")
    public Response perInsClaimGroupedByCauseSick(){
        try(Connection con = getConnection()) {
            MySQLQuery q = getPerCauseSickQuery();
            List<PerTableRow> tableRows = PerTableRow.getList(q, con);
            return createResponse(tableRows);
        } catch (Exception e) {
            e.printStackTrace();
            return createResponse(e);
        }
    }

    private MySQLQuery getPerCauseSickQuery(){
        MySQLQuery q = new MySQLQuery("SELECT pc.NAME, " +
            "	sum(coalesce(pic.vlr_rad,0)), " +
            "	sum(coalesce(pic.vlr_aprob, 0)), " +
            "	sum(coalesce(pic.vlr_pend,0)) " +
            "FROM per_ins_claim pic " +
            "JOIN per_sick_leave psl ON psl.id = pic.sick_id " +
            "JOIN per_cause pc ON pc.id = psl.cause_id " +
            "GROUP BY pc.NAME");

        return q;
    }

    @GET
    @Path("/perOffice")
    public Response perInsClaimGroupByOffice(){
        try(Connection con = getConnection()) {
            MySQLQuery q = getPerOfficeQuery();
            List<PerTableRow> tableRows = PerTableRow.getList(q, con);

            return createResponse(tableRows);
        } catch (Exception e) {
            return createResponse(e);
        }
    }

    private MySQLQuery getPerOfficeQuery() {
        String query = "SELECT " +
            "	nombre, " +
            "	sum(valorRad) AS rad, " +
            "	sum(valorAprob) AS aprob, " +
            "	SUM(valorPend) AS pend " +
            "FROM( " +
            "	 SELECT     " +
            "     	po.NAME AS nombre,    " +
            "     	SUM(COALESCE(pic.vlr_rad, 0)) AS valorRad,    " +
            "     	SUM(COALESCE(pic.vlr_aprob, 0)) AS valorAprob,    " +
            "     	SUM(COALESCE(pic.vlr_pend, 0)) AS valorPend    " +
            "     FROM per_ins_claim pic    " +
            "     JOIN per_accident pa ON pa.id = pic.acc_id    " +
            "	  JOIN per_contract_hist pch ON pch.id = pic.con_his_id " +
            "     JOIN per_office po ON po.id = pch.office_id " +
            "     WHERE pa.active = 1     " +
            "     AND pic.state <> 'cancel'    " +
            "     AND pic.state <> 'closed'     " +
            "     GROUP BY po.id     " +
            "     UNION     " +
            "     SELECT     " +
            "     	po.NAME AS nombre,     " +
            "     	SUM(COALESCE(pic.vlr_rad, 0)) AS valorRad,     " +
            "     	SUM(COALESCE(pic.vlr_aprob, 0)) AS valorAprob,     " +
            "     	SUM(COALESCE(pic.vlr_pend, 0)) AS valorPend     " +
            "     FROM per_ins_claim pic     " +
            "     JOIN per_sick_leave psl ON psl.id = pic.sick_id     " +
            "	  JOIN per_contract_hist pch ON pch.id = pic.con_his_id " +
            "     JOIN per_office po ON po.id = pch.office_id     " +
            "     WHERE psl.active = 1     " +
            "     AND pic.state <> 'cancel'     " +
            "     AND pic.state <> 'closed'     " +
            "     GROUP BY po.id     " +
            ") AS per_ins_claim_by_office " +
            "GROUP BY nombre";

        return new MySQLQuery(query);
    }


}
