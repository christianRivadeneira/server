package api.com.api;

import api.BaseAPI;
import api.com.dto.ClientData;
import api.com.dto.ImporterMultiPromoDto;
import api.com.model.ComMultiPromo;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import api.com.model.ComMultiPromoTarget;
import api.ord.model.OrdContract;
import api.ord.model.OrdContractIndex;
import java.text.SimpleDateFormat;
import java.util.Date;
import utilities.MySQLQuery;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.SummaryRow;
import utilities.mysqlReport.Table;

@Path("/comMultiPromoTarget")
public class ComMultiPromoTargetApi extends BaseAPI {

    @POST
    public Response insert(ComMultiPromoTarget obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(ComMultiPromoTarget obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            ComMultiPromoTarget old = new ComMultiPromoTarget().select(obj.id, conn);
            obj.update(conn);
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
            ComMultiPromoTarget obj = new ComMultiPromoTarget().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            ComMultiPromoTarget.delete(id, conn);
            SysCrudLog.deleted(this, ComMultiPromoTarget.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/all")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(ComMultiPromoTarget.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/import")
    public Response importClients(ImporterMultiPromoDto obj) {
        try (Connection conn = getConnection()) {

            for (int i = 0; i < obj.clieLst.size(); i++) {
                ClientData clie = obj.clieLst.get(i);
                clie.indexId = new MySQLQuery("SELECT id "
                        + "FROM ord_contract_index "
                        + "WHERE document = ?1 "
                        + "AND active").setParam(1, clie.document).getAsInteger(conn);

                if (clie.indexId != null) {
                    new MySQLQuery("UPDATE ord_contract_index SET pref = 1 WHERE id = " + clie.indexId).executeUpdate(conn);
                } else {
                    OrdContract contract = new OrdContract();
                    contract.firstName = clie.firstName;
                    contract.lastName = clie.lastName;
                    contract.cliType = "nat";
                    contract.document = clie.document;
                    contract.address = clie.address;
                    contract.neighId = obj.neighId;
                    contract.phones = clie.phone;
                    contract.cityId = obj.cityId;
                    contract.pref = true;
                    contract.state = "open";
                    contract.id = contract.insert(conn);

                    OrdContractIndex index = new OrdContractIndex();
                    index.address = contract.address;
                    index.contractId = contract.id;
                    index.type = "univ";
                    index.document = contract.document;
                    index.firstName = contract.firstName;
                    index.lastName = contract.lastName;
                    index.cliType = contract.cliType;
                    index.ctrType = null;
                    index.estName = null;
                    index.neighId = contract.neighId;
                    index.phones = contract.phones;
                    index.cityId = contract.cityId;
                    index.active = true;
                    index.birthDate = null;
                    index.email = contract.email;
                    index.estName = (index.firstName + " " + index.lastName);
                    index.pref = contract.pref;
                    clie.indexId = index.insert(conn);
                }

                new MySQLQuery("INSERT INTO com_multi_promo_target "
                        + "SET promo_id = " + obj.promoId + ", "
                        + "index_id = " + clie.indexId + ", "
                        + "sale_count = 0").executeInsertIgnore(conn);
            }

            return createResponse();
        } catch (Exception e) {
            return createResponse(e);
        }
    }

    @GET
    @Path("/targets")
    public Response getTargets(@QueryParam("promoId") int promoId) {
        try (Connection conn = getConnection()) {

            ComMultiPromo promo = new ComMultiPromo().select(promoId, conn);

            MySQLReport rep = new MySQLReport("Beneficiarios", "Listado de Beneficiarios", "Clientes", new Date());
            SimpleDateFormat msqdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat cdf = new SimpleDateFormat("dd/MM/yyyy");

            Object[][] clieData = new MySQLQuery("SELECT "
                    + "i.id, "
                    + "i.document, "
                    + "CONCAT(i.first_name, ' ', i.last_name), "
                    + "CONCAT(i.address, ' ', c.name), "
                    + "i.phones, "
                    + "'Establecimiento', "
                    + "(SELECT COUNT(*) FROM trk_sale s INNER JOIN ord_contract_index ind ON s.index_id = ind.id WHERE i.document = ind.document "
                    + "AND s.cube_cyl_type_id = " + promo.cylTypeId + " "
                    + "AND s.date BETWEEN '" + msqdf.format(promo.startDate) + " 00:00:00' AND '" + msqdf.format(promo.endDate) + " 23:59:59') "
                    + "FROM com_multi_promo_target t "
                    + "INNER JOIN ord_contract_index i ON t.index_id = i.id "
                    + "LEFT JOIN city c ON i.city_id = c.id "
                    + "WHERE t.promo_id = " + promo.id).getRecords(conn);

            rep.getSubTitles().add("Promoción: " + promo.name);
            rep.getSubTitles().add("Vigente desde: " + cdf.format(promo.startDate) + " Hasta: " + cdf.format(promo.endDate));
            rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
            rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.LEFT, "#,##0"));//0
            rep.getFormats().get(0).setWrap(true);
            rep.setShowNumbers(true);
            rep.setZoomFactor(85);

            for (int i = 0; i < clieData.length; i++) {

                Object[][] sucData = new MySQLQuery("SELECT "
                        + "s.document, "
                        + "CONCAT(s.first_name, ' ', s.last_name), "
                        + "s.address, "
                        + "s.phone, "
                        + "'Sede', "
                        + "(SELECT COUNT(*) FROM trk_sale s "
                        + "INNER JOIN ord_contract_index i ON s.index_id = i.id "
                        + "WHERE i.document = s.document "
                        + "AND s.cube_cyl_type_id = " + promo.cylTypeId + " "
                        + "AND s.date BETWEEN '" + msqdf.format(promo.startDate) + " 00:00:00' AND '" + msqdf.format(promo.endDate) + " 23:59:59') "
                        + "FROM com_multi_sede s "
                        + "WHERE s.index_id = " + MySQLQuery.getAsInteger(clieData[i][0])).getRecords(conn);

                Object[][] result = new Object[sucData.length + 1][];
                result[0] = new Object[6];
                result[0][0] = clieData[i][1];
                result[0][1] = clieData[i][2];
                result[0][2] = clieData[i][3];
                result[0][3] = clieData[i][4];
                result[0][4] = clieData[i][5];
                result[0][5] = clieData[i][6];
                for (int j = 0; j < sucData.length; j++) {
                    result[j + 1] = sucData[j];
                }
                
                Table tbl = new Table(MySQLQuery.getAsString(clieData[i][2]));
                tbl.getColumns().add(new Column("Documento", 20, 0));
                tbl.getColumns().add(new Column("Nombre", 30, 0));
                tbl.getColumns().add(new Column("Dirección", 30, 0));
                tbl.getColumns().add(new Column("Teléfono", 15, 0));
                tbl.getColumns().add(new Column("Tipo", 15, 0));
                tbl.getColumns().add(new Column("Nro. Ventas", 15, 1));
                tbl.setData(result);
                tbl.setSummaryRow(new SummaryRow("Totales", 6));
                if (tbl.getData().length > 0) {
                    rep.getTables().add(tbl);
                }
            }

            return createResponse(rep.write(conn), "beneficiarios.xls");
        } catch (Exception e) {
            return createResponse(e);
        }
    }

    /*@GET
    @Path("/grid")
    public Response getGrid() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GridResult tbl = new GridResult();
            tbl.data = new MySQLQuery("").getRecords(conn);
            tbl.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 180, "Cupón"),
            };
            tbl.sortColIndex = 4;
            tbl.sortType = GridResult.SORT_ASC;
            return createResponse(tbl);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }*/
}
