package api.bill.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.bill.model.BillInstCheck;
import api.bill.model.BillInstance;
import api.bill.model.BillSpan;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import metadata.model.GridRequest;
import utilities.MySQLQuery;
import utilities.cast;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.Table;

@Path("/billInstCheck")
public class BillInstCheckApi extends BaseAPI {

    @POST
    public Response insert(BillInstCheck obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            obj.insert(conn);
            new MySQLQuery("UPDATE bill_inst_check_poll SET reason = 'other', notes = 'Prensetó el Certificado' WHERE client_id = ?1 AND reason IS NULL").setParam(1, obj.clientId).executeUpdate(conn);
            useDefault(conn);
            SysCrudLog.created(this, obj, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(BillInstCheck obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillInstCheck old = new BillInstCheck().select(obj.id, conn);
            obj.update(conn);
            useDefault(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillInstCheck obj = new BillInstCheck().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillInstCheck.delete(id, conn);
            useDefault(conn);
            SysCrudLog.deleted(this, BillInstCheck.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/grid")
    public Response getGrid(GridRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillInstance inst = getBillInstance();
            useBillInstance(conn);
            GridResult tbl = new GridResult();
            tbl.data = new MySQLQuery("SELECT c.id, chk_date, t.name, i.name, NULL, NULL FROM "
                    + "bill_inst_check c "
                    + "INNER JOIN sigma.bill_inst_check_type t ON c.type_id = t.id "
                    + "INNER JOIN sigma.bill_inst_inspector i ON c.inspector_id = i.id "
                    + "WHERE c.client_id = ?1").setParam(1, req.ints.get(0)).getRecords(conn);

            for (Object[] row : tbl.data) {
                BillInstCheck.InstCheckInfo nds = BillInstCheck.getNextDates(req.ints.get(0), inst, cast.asDate(row, 1), conn);

                if (!row[1].equals(nds.lastCheck)) {                    
                    throw new Exception("WTF");
                }
                row[4] = nds.minDate;
                row[5] = nds.maxDate;
            }

            tbl.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 100, "Fecha"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 180, "Tipo"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 180, "Inspeccionó"),
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 100, "Próx. Mín."),
                new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 100, "Próx. Máx.")
            };
            return createResponse(tbl);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/rptPendInstChecks")
    public Response getRptPendInstChecks(@QueryParam("spanId") int spanId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillSpan span = new BillSpan().select(spanId, conn);

            MySQLReport rep = new MySQLReport("Reporte de Revisión de Instalaciones por Vencer", "", "", MySQLQuery.now(conn));
            rep.getSubTitles().add("Instancia: " + getBillInstance().name);
            rep.getSubTitles().add("Periodo: " + span.getConsLabel());

            rep.setShowNumbers(true);

            rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
            rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//1
            rep.setZoomFactor(80);
            Table tb = new Table("Revisiones");
            MySQLQuery q;
            if (getBillInstance().isTankInstance()) {
                tb.getColumns().add(new Column("Edificio", 40, 0));//0
                tb.getColumns().add(new Column("Dirección", 30, 0));//1
                tb.getColumns().add(new Column("Nombres", 45, 0));//2
                tb.getColumns().add(new Column("Documento", 15, 0));//3
                tb.getColumns().add(new Column("Apto", 12, 0));//4
                tb.getColumns().add(new Column("No. Instalación", 15, 0));//5
                tb.getColumns().add(new Column("Ref. Pago", 15, 0));//6
                tb.getColumns().add(new Column("Mínima", 15, 1));//7
                tb.getColumns().add(new Column("Máxima", 15, 1));//8

                q = new MySQLQuery(
                        " SELECT "
                        + "bb.name, "
                        + "bb.address, "
                        + "CONCAT(c.first_name, ' ' ,IFNULL(c.last_name,'')), "
                        + "c.doc, "
                        + "c.apartment, "
                        + "c.num_install, "
                        + "c.code, "
                        + "c.id,"
                        + "null "
                        + "FROM "
                        + "bill_client_tank c "
                        + "INNER JOIN bill_building bb ON bb.id = c.building_id "
                        + "WHERE  "
                        + "c.active "
                        + "ORDER BY bb.name, c.num_install; ");
            } else {
                tb.getColumns().add(new Column("Barrio", 40, 0));//0
                tb.getColumns().add(new Column("Dirección", 30, 0));//1
                tb.getColumns().add(new Column("Nombres", 45, 0));//2      
                tb.getColumns().add(new Column("Documento", 15, 0));//3
                tb.getColumns().add(new Column("Código", 15, 0));//4
                tb.getColumns().add(new Column("Mínima", 15, 1));//5
                tb.getColumns().add(new Column("Máxima", 15, 1));//6

                q = new MySQLQuery(
                        "SELECT "
                        + "bb.name, "
                        + "c.address, "
                        + "CONCAT(c.first_name, ' ' ,IFNULL(c.last_name,'')), "
                        + "c.doc, "
                        + "c.num_install,"
                        + "c.id,"
                        + "null "
                        + "FROM "
                        + "bill_client_tank c "
                        + "LEFT JOIN sigma.neigh bb ON bb.id = c.neigh_id "
                        + "WHERE c.active "
                        + "ORDER BY bb.name, c.code");
            }

            BillInstance inst = getBillInstance();

            Object[][] rawData = q.getRecords(conn);
            List<Object[]> ldata = new ArrayList();
            for (Object[] row : rawData) {
                int id = MySQLQuery.getAsInteger(row[row.length - 2]);
                BillInstCheck.InstCheckInfo ds = BillInstCheck.getNextDates(id, inst, null, conn);
                if (ds.overlaps(span)) {
                    row[row.length - 2] = ds.minDate;
                    row[row.length - 1] = ds.maxDate;
                    ldata.add(row);
                }
            }
            if (!ldata.isEmpty()) {
                tb.setData(ldata);
                rep.getTables().add(tb);
            }
            useDefault(conn);
            return createResponse(rep.write(conn), "listado_fras.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
