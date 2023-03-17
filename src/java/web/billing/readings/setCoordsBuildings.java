package web.billing.readings;

import java.io.IOException;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.system.SessionLogin;
import utilities.IO;
import utilities.MySQLQuery;
import web.billing.BillingServlet;
import java.sql.PreparedStatement;

@MultipartConfig
@WebServlet(name = "/readings/setCoordsBuildings", urlPatterns = {"/readings/setCoordsBuildings"})
public class setCoordsBuildings extends BillingServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String sessionId = IO.convertStreamToString(request.getPart("sessionId").getInputStream());
            String data = IO.convertStreamToString(request.getPart("data").getInputStream());
            int instanceId = Integer.valueOf(IO.convertStreamToString(request.getPart("cityId").getInputStream()));

            String[] lines = data.split("B");

            try (Connection gralConn = getConnection()) {
                SessionLogin.validate(sessionId, gralConn, null);
                Integer empId = new MySQLQuery("SELECT employee_id FROM session_login WHERE session_id = ?1 AND end_time IS NULL;").setParam(1, sessionId).getAsInteger(gralConn);
                if (empId == null) {
                    throw new Exception("No tiene autorización");
                }

                PreparedStatement stUpdate = gralConn.prepareStatement("UPDATE ord_tank_client "
                        + "SET lat = ?, "
                        + "lon = ?, "
                        + "checked_coords = 1 "
                        + "WHERE mirror_id = ? "
                        + "AND bill_instance_id = ? "
                        + "AND type='build'");

                for (String line : lines) {
                    String[] parts = line.split("A");
                    int buildingId = Integer.valueOf(parts[0]);
                    Double lat = parts[1].equals("null") ? null : Double.parseDouble(parts[1]);
                    Double lon = parts[2].equals("null") ? null : Double.parseDouble(parts[2]);

                    if (lat == null) {
                        stUpdate.setNull(1, java.sql.Types.DOUBLE);
                    } else {
                        stUpdate.setDouble(1, lat);
                    }

                    if (lon == null) {
                        stUpdate.setNull(2, java.sql.Types.DOUBLE);
                    } else {
                        stUpdate.setDouble(2, lon);
                    }

                    stUpdate.setInt(3, buildingId);
                    stUpdate.setInt(4, instanceId);
                    stUpdate.addBatch();
                }
                int[] executeUpdate = stUpdate.executeBatch();
                System.out.println("Edificios actualizados: " + executeUpdate.length);
                response.setStatus(200);
                response.getWriter().write("ok");
            } catch (Exception ex) {
                Logger.getLogger(setCoordsBuildings.class.getName()).log(Level.SEVERE, null, ex);
                response.sendError(500, ex.getMessage());
            }
        } catch (IOException | NumberFormatException | ServletException ex) {
            Logger.getLogger(setCoordsBuildings.class.getName()).log(Level.SEVERE, null, ex);
            response.sendError(500, ex.getMessage());
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
        return "";
    }
}
