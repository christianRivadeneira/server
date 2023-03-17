package api.mss.api;

import api.BaseAPI;
import api.mss.model.MssGuard;
import api.mss.model.MssMinute;
import api.mss.model.MssMinuteEvent;
import api.mss.model.MssMinuteField;
import api.mss.model.MssMinuteIncident;
import api.mss.model.MssMinuteIncidentType;
import api.mss.model.MssMinuteType;
import api.mss.model.MssMinuteValue;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import java.util.List;
import javax.ws.rs.GET;
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
import utilities.mysqlReport.Table;

@Path("/mssMinute")
public class MssMinuteApi extends BaseAPI {

    @PUT
    public Response update(MssMinute obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssMinute old = new MssMinute().select(obj.id, conn);
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
            SessionLogin sl = getSession(conn);
            MssMinute obj = new MssMinute().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/report")
    public Response report(@QueryParam("id") int minuteId) throws Exception {
        try (Connection conn = getConnection()) {

            MssMinute min = new MssMinute().select(minuteId, conn);
            MssMinuteType t = new MssMinuteType().select(min.typeId, conn);
            List<MssMinuteEvent> events = MssMinuteEvent.getAll(minuteId, conn);
            List<MssMinuteField> flds = MssMinuteField.getAll(min.typeId, conn);

            MySQLReport rep = new MySQLReport("Minuta", t.name, "Hoja 1", MySQLQuery.now(conn));
            rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
            rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy HH:mm:ss"));//1
            rep.setVerticalFreeze(5);
            rep.setZoomFactor(80);

            Object[][] data = new Object[events.size()][6 + flds.size()];

            for (int i = 0; i < events.size(); i++) {
                MssMinuteEvent ev = events.get(i);
                MssGuard guard = new MssGuard().select(ev.guardId, conn);
                List<MssMinuteIncident> novs = MssMinuteIncident.getByEvent(ev.id, conn);

                data[i][0] = ev.regDate;
                data[i][1] = guard.firstName + " " + guard.lastName;
                data[i][2] = ev.type.equals("in") ? "Entrada" : "Salida";
                for (int j = 0; j < flds.size(); j++) {
                    MssMinuteField fld = flds.get(j);
                    data[i][3 + j] = MssMinuteValue.getValue(fld.id, ev.id, conn);
                }
                data[i][3 + flds.size()] = ev.notes;
                if (!novs.isEmpty()) {
                    String novNames = "";
                    String novNotes = "";
                    for (int j = 0; j < novs.size(); j++) {
                        MssMinuteIncident n = novs.get(j);
                        MssMinuteIncidentType nt = new MssMinuteIncidentType().select(n.typeId, conn);
                        novNames += nt.name;
                        novNotes += n.notes;
                        if (j < novs.size() - 1) {
                            novNames += ", ";
                            novNotes += ", ";
                        }
                    }
                    data[i][4 + flds.size()] = novNames;
                    data[i][5 + flds.size()] = novNotes;
                }
            }

            Table tb = new Table("Eventos");
            tb.getColumns().add(new Column("Registro", 40, 0));
            tb.getColumns().add(new Column("Guardia", 40, 0));
            tb.getColumns().add(new Column("Tipo", 15, 0));
            for (int i = 0; i < flds.size(); i++) {
                MssMinuteField fld = flds.get(i);
                tb.getColumns().add(new Column(fld.name, 35, 0));
            }
            tb.getColumns().add(new Column("Notas", 50, 0));
            tb.getColumns().add(new Column("Tipos de Novedades", 50, 0));
            tb.getColumns().add(new Column("Novedades", 50, 0));

            tb.setData(data);
            rep.getTables().add(tb);

            useDefault(conn);
            return createResponse(rep.write(conn), "minuta.xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
