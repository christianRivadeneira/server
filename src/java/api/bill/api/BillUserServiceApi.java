package api.bill.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.bill.model.BillClientTank;
import api.bill.model.BillInstance;
import api.bill.model.BillServiceType;
import api.bill.model.BillSpan;
import api.bill.model.BillUserService;
import api.bill.model.BillUserServiceFee;
import api.bill.model.EqualPayment;
import api.sys.model.SysCrudLog;
import java.math.BigDecimal;
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
import model.system.SessionLogin;
import utilities.MySQLQuery;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.SummaryRow;
import utilities.mysqlReport.Table;

@Path("/billUserService") /* Tabla de redes */
public class BillUserServiceApi extends BaseAPI {

    public static void create(BillUserService obj, Connection conn, BaseAPI caller) throws Exception {
        caller.useBillInstance(conn);
        int consId = BillSpan.getByClient("cons", obj.billClientTankId, caller.getBillInstance(), conn).id;
        if (obj.billSpanId != consId) {
            throw new Exception("No se puede crear para el periodo seleccionado");
        }
        obj.insert(conn);
        EqualPayment[] values = EqualPayment.getValues(obj.total, obj.ivaRate, obj.creditInter, obj.inteIvaRate, obj.payments);
        for (int i = 0; i < values.length; i++) {
            EqualPayment value = values[i];
            BillUserServiceFee fee = new BillUserServiceFee();
            fee.place = i;
            fee.value = value.capital;
            fee.extPay = BigDecimal.ZERO;
            fee.inter = value.interest;
            fee.extInter = BigDecimal.ZERO;
            fee.interTax = value.interVat;
            fee.extInterTax = BigDecimal.ZERO;
            fee.serviceId = obj.id;
            fee.insert(conn);
        }
        caller.useDefault(conn);
        SysCrudLog.created(caller, obj, conn);
    }

    @POST
    public Response insert(BillUserService obj) {
        try (Connection conn = getConnection()) {
            try {
                getSession(conn);
                conn.setAutoCommit(false);
                create(obj, conn, this);
                conn.commit();
                return Response.ok(obj).build();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(BillUserService obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillUserService old = new BillUserService().select(obj.id, conn);
            old.fullyCaused = false;
            obj.update(conn);
            useDefault(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillUserService obj = new BillUserService().select(id, conn);
            BillSpan reca = BillSpan.getByClient("reca", obj.billClientTankId, getBillInstance(), conn);
            Integer bills = new MySQLQuery("SELECT COUNT(*) FROM bill_user_service_fee WHERE service_id = ?1").setParam(1, obj.id).getAsInteger(conn);
            //reca + 1 es el periodo en consumo
            //startSpanId + bills.size() -1 + 1 es el ID del periodo que le correspondería a la nueva cuota
            obj.fullyCaused = (reca.id + 1 > obj.billSpanId + bills);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            try {
                conn.setAutoCommit(false);
                BillInstance inst = getBillInstance();
                useBillInstance(conn);
                BillUserService obj = new BillUserService().select(id, conn);
                BillSpan reca = BillSpan.getByClient("reca", obj.billClientTankId, inst, conn);
                Integer bills = new MySQLQuery("SELECT COUNT(*) FROM bill_user_service_fee WHERE service_id = ?1").setParam(1, obj.id).getAsInteger(conn);
                //reca + 1 es el periodo en consumo
                //startSpanId + bills.size() -1 + 1 es el ID del periodo que le correspondería a la nueva cuota
                obj.fullyCaused = (reca.id + 1 > obj.billSpanId + bills);
                if (obj.fullyCaused) {
                    throw new Exception("Ya fue causado en su totalidad.");
                }

                int caused = reca.id - obj.billSpanId + 1;
                if (caused > 0) {
                    throw new Exception("Ya hay " + caused + " cuotas causadas.");
                }

                new MySQLQuery("DELETE FROM bill_user_service_fee WHERE service_id = ?1").setParam(1, obj.id).executeUpdate(conn);
                BillUserService.delete(id, conn);
                useDefault(conn);
                SysCrudLog.deleted(this, BillUserService.class, id, conn);
                conn.commit();
                return createResponse();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/grid")
    public Response getGrid(@QueryParam("clientId") int clientId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            useBillInstance(conn);
            BillSpan reca = BillSpan.getByClient("reca", clientId, getBillInstance(), conn);
            GridResult gr = new GridResult();
            String q = ""
                    + "SELECT "
                    + "s.id, "
                    + "t.name, "
                    + "CONCAT(DATE_FORMAT(p.begin_date,'%d/%m/%Y'), ' - ', DATE_FORMAT(p.end_date,'%d/%m/%Y')), "
                    + "(SELECT SUM(f.value + COALESCE(f.inter, 0) + IFNULL(f.inter_tax, 0)) FROM bill_user_service_fee f WHERE f.service_id = s.id), "
                    + "(SELECT SUM(f.ext_pay + COALESCE(f.ext_inter, 0) + IFNULL(f.ext_inter_tax, 0)) FROM bill_user_service_fee f WHERE f.service_id = s.id), "
                    + "(SELECT SUM(f.value + COALESCE(f.inter, 0) + IFNULL(f.inter_tax, 0) - (f.ext_pay + COALESCE(f.ext_inter, 0) + IFNULL(f.ext_inter_tax, 0))) FROM bill_user_service_fee f WHERE f.service_id = s.id AND s.bill_span_id + f.place <= ?1), "
                    + "(SELECT SUM(f.value + COALESCE(f.inter, 0) + IFNULL(f.inter_tax, 0) - (f.ext_pay + COALESCE(f.ext_inter, 0) + IFNULL(f.ext_inter_tax, 0))) FROM bill_user_service_fee f WHERE f.service_id = s.id AND s.bill_span_id + f.place > ?1) "
                    + "FROM "
                    + "bill_user_service as s "
                    + "INNER JOIN bill_service_type as t ON s.type_id = t.id "
                    + "INNER JOIN bill_span as p ON s.bill_span_id = p.id "
                    + "WHERE s.bill_client_tank_id = " + clientId;
            gr.data = new MySQLQuery(q).setParam(1, reca.id).getRecords(conn);
            gr.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 350, "Tipo"),
                new MySQLCol(MySQLCol.TYPE_TEXT, 350, "Desde"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 200, "Valor"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 200, "Abonos Externos"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 200, "Facturado"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 200, "Por Cobrar")};
            return createResponse(gr);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/report")
    public Response getReport(@QueryParam("justPending") boolean justPending) throws Exception {
        try (Connection conn = getConnection()) {
            getSession(conn);
            BillInstance inst = getBillInstance();
            useBillInstance(conn);

            if (new MySQLQuery("SELECT COUNT(*)>0 FROM bill_client_tank t WHERE t.span_closed and t.active").getAsBoolean(conn)) {
                throw new Exception("El cierre del periodo está en progreso");
            }
            MySQLReport rep = new MySQLReport("Servicios por Cliente - " + inst.name, "", "Hoja 1", now(conn));

            rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
            rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$ #,##0.00"));//1
            rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//2
            rep.setZoomFactor(85);
            rep.setVerticalFreeze(5);

            List<BillUserService> srvs = BillUserService.getList(new MySQLQuery("SELECT " + BillUserService.getSelFlds("s") + " FROM bill_user_service s ORDER BY s.id DESC"), conn);
            List<Object[]> data = new ArrayList<>();

            for (int i = 0; i < srvs.size(); i++) {
                BillUserService srv = srvs.get(i);
                BillSpan reca = BillSpan.getByClient("reca", srv.billClientTankId, inst, conn);
                Object[] extraRow = new MySQLQuery("SELECT "
                        + "(SELECT SUM(f.value) FROM bill_user_service_fee f WHERE f.service_id = s.id), "
                        + "(SELECT COUNT(*) FROM bill_user_service_fee f1 WHERE f1.service_id = s.id), "
                        + "(SELECT SUM(f.value - f.ext_pay) FROM bill_user_service_fee f WHERE f.service_id = s.id AND s.bill_span_id + f.place <= ?1), "
                        + "(SELECT SUM(f.ext_pay) FROM bill_user_service_fee f WHERE f.service_id = s.id), "
                        + "(SELECT COUNT(*) FROM bill_user_service_fee f1 WHERE f1.service_id = s.id AND s.bill_span_id + f1.place > ?1), "
                        + "(SELECT SUM(f.value - f.ext_pay) FROM bill_user_service_fee f WHERE f.service_id = s.id AND s.bill_span_id + f.place > ?1) "
                        + "FROM "
                        + "bill_user_service as s "
                        + "WHERE s.id = ?2").setParam(1, reca.id).setParam(2, srv.id).getRecord(conn);
                BigDecimal pending = MySQLQuery.getAsBigDecimal(extraRow[5], true);

                if (pending.compareTo(BigDecimal.ZERO) != 0 || !justPending) {
                    BillClientTank c = new BillClientTank().select(srv.billClientTankId, conn);
                    BillServiceType t = new BillServiceType().select(srv.typeId, conn);
                    BillSpan sp = new BillSpan().select(srv.billSpanId, conn);
                    Object[] row = new Object[10];
                    row[0] = c.numInstall;
                    row[1] = c.firstName + (c.lastName != null ? " " + c.lastName : "");
                    row[2] = t.name;
                    row[3] = sp.getConsLabel();
                    System.arraycopy(extraRow, 0, row, 4, extraRow.length);
                    data.add(row);
                }
            }

            if (!data.isEmpty()) {
                Table bTable = new Table("Servicios por Cliente");
                bTable.getColumns().add(new Column("Num. Inst", 12, 0));//0
                bTable.getColumns().add(new Column("Nombre", 40, 0));//0
                bTable.getColumns().add(new Column("Tipo", 40, 0));//0
                bTable.getColumns().add(new Column("Desde", 28, 0));//2
                bTable.getColumns().add(new Column("Valor", 20, 1));//1
                bTable.getColumns().add(new Column("Cuotas Acordadas", 20, 2));//2
                bTable.getColumns().add(new Column("Facturado", 20, 1));//1
                bTable.getColumns().add(new Column("Abonos Externos", 20, 1));//1
                bTable.getColumns().add(new Column("Cuotas por Cobrar", 20, 2));//2
                bTable.getColumns().add(new Column("Por Cobrar", 20, 1));//1
                bTable.setData(data);
                bTable.setSummaryRow(new SummaryRow("Totales", 6));
                rep.getTables().add(bTable);
                useDefault(conn);
                return createResponse(rep.write(conn), "servicios_usuario.xls");
            } else {
                throw new Exception("No se hallaron datos");
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
