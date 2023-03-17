package web.gps;

import api.BaseAPI;
import api.sys.model.Token;
import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import utilities.IO;
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "GpsCoordinatesV2", urlPatterns = {"/GpsCoordinatesV2"})
public class GpsCoordinatesReceiverV2 extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");
        try (GZIPInputStream giz = new GZIPInputStream(request.getPart("coords").getInputStream())) {
            String raw;
            raw = IO.convertStreamToString(giz);
            String[] rows = raw.split(";");
            String sessionKey = MySQLQuery.scape(rows[0]);
            Token token = BaseAPI.getToken(sessionKey);
            try (Connection conn = MySQLCommon.getConnection(token.p, token.t)) {
                if (rows != null && rows.length > 1) {
                    StringBuilder sb = new StringBuilder("INSERT INTO gps_coordinate (`latitude`, "
                            + "`longitude`, "
                            + "`accuracy`, "
                            + "`employee_id`, "
                            + "`date`, "
                            + "`type`, "
                            + "`speed`, "
                            + "`charge`, "
                            + "`mov`, "
                            + "`plugged`, "
                            + "`app_id`, "
                            + "`session_id`, "
                            + "`interval`) values ");

                    GpsCoordinate last = null;
                    boolean insert = false;
                    SessionLogin sl = null;
                    try {
                        sl = SessionLogin.validate(sessionKey, conn, null);
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }

                    if (sl != null) {

                        int employeeId = sl.employeeId;

                        for (int i = 1; i < rows.length; i++) {
                            String[] parts = rows[i].split(",");
                            GpsCoordinate coord = new GpsCoordinate();
                            coord.accuracy = MySQLQuery.getAsInteger(parts[0]);
                            coord.date = DATE_FORMAT.parse(parts[1]);
                            coord.latitude = MySQLQuery.getAsBigDecimal(parts[2], false);
                            coord.longitude = MySQLQuery.getAsBigDecimal(parts[3], false);
                            coord.type = MySQLQuery.scape(parts[4]);
                            coord.speed = MySQLQuery.getAsInteger(parts[5]);
                            coord.charge = MySQLQuery.getAsInteger(parts[6]);
                            coord.plugged = parts[7].equals("1");
                            coord.mov = parts[8].equals("1");
                            coord.interval = Integer.parseInt(parts[9]);
                            coord.employeeId = employeeId;
                            coord.appId = sl.appId;
                            coord.sessionId = sl.id;

                            if (coord.interval != null && coord.interval > 1000) {
                                coord.interval = (int) (coord.interval / 1000d);
                            }

                            if (coord.charge != null && coord.charge < 0) {
                                coord.charge = null;
                            }

                            if (coord.charge != null && coord.charge > 100) {
                                coord.charge = 100;
                            }

                            if (coord.speed != null && coord.speed < 0) {
                                coord.speed = null;
                            }

                            if (coord.speed != null && coord.speed > 255) {
                                coord.speed = 255;
                            }

                            switch (coord.type) {
                                case "norm":
                                    sb.append(coord.getInsertRow(coord));
                                    sb.append(",");
                                    last = coord;
                                    insert = true;
                                    break;
                                case "gps_off":
                                    sb.append(coord.getInsertRow(coord));
                                    sb.append(",");
                                    insert = true;
                                    break;
                                case "no_change":
                                    last = coord;
                                    break;
                                default:
                                    break;
                            }
                        }
                        if (insert) {
                            sb.deleteCharAt(sb.length() - 1);
                            new MySQLQuery(sb.toString()).executeUpdate(conn);
                        }
                        if (last != null) {
                            GpsLastCoord.sync(last, conn);
                        }
                    }
                }
                response.getOutputStream().write("ok".getBytes());
            } catch (Exception ex) {
                /*String stack = ExceptionUtils.getStackTrace(ex);
                stack += "\r\n---------\r\n" + raw;
                try {
                    sendMail.sendMail(conn, "karol.mendoza@montagas.com.co", "Error GPS", stack, stack);
                } catch (Exception ex1) {
                    Logger.getLogger(GpsCoordinatesReceiverV2.class.getName()).log(Level.SEVERE, null, ex1);
                }*/
                Logger.getLogger(GpsCoordinatesReceiverV2.class.getName()).log(Level.SEVERE, null, ex);
                response.getOutputStream().write("error".getBytes());
            }
        } catch (Exception ex) {
            Logger.getLogger(GpsCoordinatesReceiverV2.class.getName()).log(Level.SEVERE, null, ex);
            response.getOutputStream().write("error".getBytes());
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
