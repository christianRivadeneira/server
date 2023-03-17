package api.sys.api;

import api.BaseAPI;
import api.bill.model.BillMarket;
import api.sys.model.SysCrudLog;
import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import metadata.log.Descriptor;
import metadata.log.LogData;
import utilities.MySQLQuery;
import utilities.json.JSONDecoder;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.Table;

@Path("/sysCrudLog")
public class SysCrudLogApi extends BaseAPI {

    @GET
    @Path("/test")
    public Response test() {
        try (Connection conn = getConnection()) {

            BillMarket market1 = new BillMarket();
            market1.id = 1;
            market1.code = "001";
            market1.name = null;
            market1.baseMonth = new GregorianCalendar(2016, 11, 1).getTime();
            market1.cfBase = new BigDecimal("3.141592654");

            BillMarket market2 = new BillMarket();
            market2.id = 1;
            market2.code = "002";
            market2.name = "aldana";
            market2.baseMonth = new GregorianCalendar(2016, 11, 2).getTime();
            market2.cfBase = null;

            for (int i = 0; i < 10000; i++) {
                SysCrudLog.updated(this, market1, market2, conn);
            }

            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/report")
    public Response report(@QueryParam("tableName") String table, @QueryParam("id") int id, @QueryParam("instId") Integer instId) {
        try (Connection conn = getConnection()) {
            metadata.model.Table mTbl = metadata.model.Table.getByName(table);
            if (instId != null) {
                useBillInstance(instId, conn);
            }
            String desc = Descriptor.getDescription(table, id, conn);
            if (instId != null) {
                useDefault(conn);
            }
            String title = "Logs " + (mTbl.male ? " del " : "de la ") + mTbl.singular + (desc != null ? " - " + desc : "");
            MySQLReport rep = new MySQLReport(title, null, "Hoja 1", now(conn));
            rep.setVerticalFreeze(5);
            rep.setHorizontalFreeze(0);
            rep.setZoomFactor(80);

            rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT, true));//0
            rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy HH:mm:ss"));//1
            rep.getFormats().get(0).setWrap(true);

            //Columnas
            List<Column> cols = new ArrayList<>();
            cols.add(new Column("Fecha", 20, 1));//0
            cols.add(new Column("IP", 20, 0));//0
            cols.add(new Column("Documento", 20, 0));//0
            cols.add(new Column("Nombres", 40, 0));//0
            cols.add(new Column("Descripción", 80, 0));//0

            Table tbl = new Table("Eventos");
            tbl.setColumns(cols);
            Object[][] data = new MySQLQuery(""
                    + "SELECT l.dt, sl.user_ip, e.document, CONCAT(e.first_name, ' ', e.last_name), l.type, l.json, l.txt "
                    + "FROM "
                    + "sys_crud_log l "
                    + "LEFT JOIN session_login sl ON sl.id = l.session_id "
                    + "INNER JOIN employee e ON e.id = l.employee_id "
                    + "WHERE l.owner_serial = ?1 AND l.table = ?2 "
                    + (instId != null ? "AND l.bill_inst_id = " + instId + " " : "")
                    + "ORDER BY l.dt").setParam(1, id).setParam(2, table).getRecords(conn);
            tbl.setData(data);

            for (int i = 0; i < data.length; i++) {
                Object[] rawRow = data[i];
                Object[] row = new Object[5];
                System.arraycopy(rawRow, 0, row, 0, 4);

                Object type = rawRow[4];
                if (type.equals("crea")) {
                    row[4] = "Se creó el registro";
                } else if (type.equals("upd")) {
                    if (rawRow[6] != null) {
                        row[4] = rawRow[6];
                    } else {
                        try (ByteArrayInputStream bais = new ByteArrayInputStream(rawRow[5].toString().getBytes())) {
                            LogData ldata = new JSONDecoder().getObject(bais, LogData.class);
                            row[4] = ldata.getAsString(table, conn);
                        }
                    }
                } else if (type.equals("del")) {
                    row[4] = "Se eliminó el registro";
                } else {
                    throw new RuntimeException();
                }
                data[i] = row;
            }

            if (data.length > 0) {
                rep.getTables().add(tbl);
            }
            return createResponse(rep.write(conn), "Logs.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
