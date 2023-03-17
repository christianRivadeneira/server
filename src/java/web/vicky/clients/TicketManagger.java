package web.vicky.clients;

import web.vicky.servlets.MimonRequest;
import java.io.IOException;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import service.MySQL.MySQLCommon;
import service.MySQL.MySQLSelect;
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "/TicketManagger", urlPatterns = {"/TicketManagger"})
public class TicketManagger extends HttpServlet {

    private static final String STATUS_OK = "OK";
    private static final String STATUS_ERROR = "ERROR";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Connection conn = null;

        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            JsonObjectBuilder ob = Json.createObjectBuilder();
            conn = MySQLCommon.getConnection("sigmads", null);
            response.addHeader("Access-Control-Allow-Credentials", "true");
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");
            JsonObject req = MySQLQuery.scapeJsonObj(request);

            String header = req.getString("header");

            try {
                switch (header) {

                    case "AddTicket": {
                        String title = req.getString("title");
                        String msgTicket = req.getString("msgTicket");
                        int indexId = req.getInt("indexId");

                        Integer idTicket = new MySQLQuery("INSERT INTO clie_ticket SET"
                                + " index_id=" + indexId + ","
                                + " status='op'").executeInsert(conn);

                        new MySQLQuery("INSERT INTO clie_msg_ticket SET"
                                + " subjet = ?1,"
                                + " msg = ?2,"
                                + " sent_to='sac',"
                                + " date=NOW(),"
                                + " ticket_id =" + idTicket).setParam(1, title).setParam(2, msgTicket)
                                .executeInsert(conn);

                        ob.add("status", STATUS_OK);
                        break;

                    }

                    case "AddMsgTicket": {
                        String msgTicket = req.getString("msgTicket");
                        int IdTicket = req.getInt("IdTicket");

                        new MySQLQuery("INSERT INTO clie_msg_ticket SET"
                                + " msg = ?1,"
                                + " sent_to='sac',"
                                + " date=NOW(),"
                                + " ticket_id =" + IdTicket).setParam(1, msgTicket)
                                .executeInsert(conn);

                        ob.add("status", STATUS_OK);
                        break;

                    }

                    case "ListMsgTicket": {

                        int indexTick = req.getInt("indexTic");
                        Object[][] ListMsgT = new MySQLQuery("SELECT * from clie_msg_ticket ct"
                                + " INNER JOIN clie_ticket c ON ct.ticket_id=c.id WHERE ct.ticket_id=" + indexTick).getRecords(conn);

                        JsonArrayBuilder datosMsgTicket = Json.createArrayBuilder();

                        for (int i = 0; i < ListMsgT.length; i++) {
                            JsonObjectBuilder row = Json.createObjectBuilder();
                            row.add("idMsg", MySQLQuery.getAsInteger(ListMsgT[i][0]));
                            row.add("messeger", MySQLQuery.getAsString(ListMsgT[i][2]));
                            row.add("date", MySQLQuery.getAsString(ListMsgT[i][4]));
                            String send = MySQLQuery.getAsString(ListMsgT[i][3]);
                            switch (send) {
                                case "sac":
                                    row.add("send", true);
                                    break;
                                case "cli":
                                    row.add("send", false);
                                    break;
                            }
                            datosMsgTicket.add(row);
                        }

                        ob.add("listMsg", datosMsgTicket);
                        ob.add("status", STATUS_OK);
                        break;

                    }

                    case "UpdateListTicket": {
                        int indexId = req.getInt("indexId");

                        Object[][] ListTicket = new MySQLQuery("SELECT * from clie_msg_ticket ct"
                                + " INNER JOIN clie_ticket c ON ct.ticket_id=c.id WHERE c.index_id=" + indexId + " AND ct.subjet IS NOT NULL").getRecords(conn);

                        JsonArrayBuilder datosTicket = Json.createArrayBuilder();

                        for (int i = 0; i < ListTicket.length; i++) {
                            JsonObjectBuilder row = Json.createObjectBuilder();

                            row.add("idT", MySQLQuery.getAsInteger(ListTicket[i][6]));
                            row.add("title", MySQLQuery.getAsString(ListTicket[i][1]));
                            row.add("date", MySQLQuery.getAsString(ListTicket[i][4]));
                            row.add("msg", MySQLQuery.getAsString(ListTicket[i][2]));
                            String status = MySQLQuery.getAsString(ListTicket[i][9]);

                            String send = new MySQLQuery("SELECT sent_to from clie_msg_ticket "
                                    + " WHERE ticket_id=" + MySQLQuery.getAsInteger(ListTicket[i][6]) + "   ORDER BY id desc limit 1 ").getAsString(conn);
                            switch (send) {
                                case "sac":
                                    row.add("send", true);
                                    break;
                                case "cli":
                                    row.add("send", false);
                                    break;
                            }

                            switch (status) {
                                case "op":
                                    row.add("status", "Abierto");
                                    break;
                                case "pro":
                                    row.add("status", "En Proceso");
                                    break;
                                case "clo":
                                    row.add("status", "Cerrado");
                                    break;
                            }

                            datosTicket.add(row);

                        }
                        ob.add("list", datosTicket);
                        ob.add("status", STATUS_OK);
                        break;
                    }

                    default:
                        break;
                }
            } catch (Exception ex) {
                Logger.getLogger(MimonRequest.class.getName()).log(Level.SEVERE, null, ex);
                ob.add("status", STATUS_ERROR);
                ob.add("msg", ex.getMessage());
            } finally {
                w.writeObject(ob.build());
            }

        } catch (Exception ex) {
            Logger.getLogger(MimonRequest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            MySQLSelect.tryClose(conn);
        }

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }

}
