package web.maps;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import utilities.MySQLQuery;
import web.DownloadApk;

@MultipartConfig
@WebServlet(name = "CreateKml", urlPatterns = {"/CreateKml"})
public class CreateKml extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        JsonObject req = MySQLQuery.scapeJsonObj(request);

        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            JsonObjectBuilder ob = Json.createObjectBuilder();
            try {
                JsonArray arrRoute = req.containsKey("route") ? req.getJsonArray("route") : null;
                JsonArray arrPoints = req.containsKey("points") ? req.getJsonArray("points") : null;

                StringBuilder sb = new StringBuilder();
                sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(System.lineSeparator())
                        .append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">").append(System.lineSeparator())
                        .append("  <Document>").append(System.lineSeparator())
                        .append("    <name>Mapa sin nombre</name>").append(System.lineSeparator())
                        .append("    <description/>").append(System.lineSeparator())
                        .append("    <Style id=\"line-000000-1200-nodesc-normal\">").append(System.lineSeparator())
                        .append("      <LineStyle>").append(System.lineSeparator())
                        .append("        <color>ffd18802</color>").append(System.lineSeparator())
                        .append("        <width>1.2</width>").append(System.lineSeparator())
                        .append("      </LineStyle>").append(System.lineSeparator())
                        .append("      <BalloonStyle>").append(System.lineSeparator())
                        .append("        <text><![CDATA[<h3>$[name]</h3>]]></text>").append(System.lineSeparator())
                        .append("      </BalloonStyle>").append(System.lineSeparator())
                        .append("    </Style>").append(System.lineSeparator())
                        .append("    <Style id=\"line-000000-1200-nodesc-highlight\">").append(System.lineSeparator())
                        .append("      <LineStyle>").append(System.lineSeparator())
                        .append("        <color>ffd18802</color>").append(System.lineSeparator())
                        .append("        <width>1.8</width>").append(System.lineSeparator())
                        .append("      </LineStyle>").append(System.lineSeparator())
                        .append("      <BalloonStyle>").append(System.lineSeparator())
                        .append("        <text><![CDATA[<h3>$[name]</h3>]]></text>").append(System.lineSeparator())
                        .append("      </BalloonStyle>").append(System.lineSeparator())
                        .append("    </Style>").append(System.lineSeparator())
                        .append("	<Style id=\"west_campus_style\">").append(System.lineSeparator())
                        .append("      <IconStyle>").append(System.lineSeparator())
                        .append("        <Icon>").append(System.lineSeparator())
                        .append("          <href>http://maps.google.com/mapfiles/kml/paddle/red-circle.png</href>").append(System.lineSeparator())
                        .append("        </Icon>").append(System.lineSeparator())
                        .append("      </IconStyle>").append(System.lineSeparator())
                        .append("      <BalloonStyle>").append(System.lineSeparator())
                        .append("        <text>$[texto]</text>").append(System.lineSeparator())
                        .append("      </BalloonStyle>").append(System.lineSeparator())
                        .append("    </Style>").append(System.lineSeparator())
                        .append("    <StyleMap id=\"line-000000-1200-nodesc\">").append(System.lineSeparator())
                        .append("      <Pair>").append(System.lineSeparator())
                        .append("        <key>normal</key>").append(System.lineSeparator())
                        .append("        <styleUrl>#line-000000-1200-nodesc-normal</styleUrl>").append(System.lineSeparator())
                        .append("      </Pair>").append(System.lineSeparator())
                        .append("      <Pair>").append(System.lineSeparator())
                        .append("        <key>highlight</key>").append(System.lineSeparator())
                        .append("        <styleUrl>#line-000000-1200-nodesc-highlight</styleUrl>").append(System.lineSeparator())
                        .append("      </Pair>").append(System.lineSeparator())
                        .append("    </StyleMap>").append(System.lineSeparator())
                        .append("    <Folder>").append(System.lineSeparator())
                        .append("      <name>Capa sin nombre</name>").append(System.lineSeparator())
                        .append("      <Placemark>").append(System.lineSeparator())
                        .append("        <name>Línea 1</name>").append(System.lineSeparator())
                        .append("        <styleUrl>#line-000000-1200-nodesc</styleUrl>").append(System.lineSeparator())
                        .append("        <LineString>").append(System.lineSeparator())
                        .append("          <tessellate>1</tessellate>").append(System.lineSeparator())
                        .append("          <coordinates>").append(System.lineSeparator());
                if (arrRoute != null) {
                    for (int i = 0; i < arrRoute.size(); i++) {
                        JsonObject obj = arrRoute.getJsonObject(i);
                        sb.append("          ").append(obj.getString("lon")).append(",").append(obj.getString("lat")).append(",0").append(System.lineSeparator());
                    }
                }
                sb.append("          </coordinates>").append(System.lineSeparator())
                        .append("        </LineString>").append(System.lineSeparator())
                        .append("      </Placemark>").append(System.lineSeparator())
                        .append("    </Folder>").append(System.lineSeparator())
                        .append("    <Folder>").append(System.lineSeparator());
                if (arrPoints != null) {
                    for (int i = 0; i < arrPoints.size(); i++) {
                        JsonObject obj = arrPoints.getJsonObject(i);
                        sb.append("	<Placemark>").append(System.lineSeparator())
                                .append("      <name>Venta</name>").append(System.lineSeparator())
                                .append("      <styleUrl>#west_campus_style</styleUrl>").append(System.lineSeparator())
                                .append("      <ExtendedData>").append(System.lineSeparator())
                                .append("        <Data name=\"texto\">").append(System.lineSeparator())
                                .append("          <value>").append(obj.getString("text")).append("</value>").append(System.lineSeparator())
                                .append("        </Data>").append(System.lineSeparator())
                                .append("      </ExtendedData>").append(System.lineSeparator())
                                .append("      <Point>").append(System.lineSeparator())
                                .append("        <coordinates>").append(obj.getString("lon")).append(",").append(obj.getString("lat")).append(",0</coordinates>").append(System.lineSeparator())
                                .append("      </Point>").append(System.lineSeparator())
                                .append("    </Placemark>").append(System.lineSeparator());
                    }
                }
                sb.append("    </Folder>").append(System.lineSeparator())
                        .append("  </Document>").append(System.lineSeparator())
                        .append("</kml>").append(System.lineSeparator());

                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                date = date.replace(" ", "_").replace(":", "");
                String fileName = date + ".kml";
                PrintWriter writer = new PrintWriter("d:\\" + fileName, "UTF-8");
                writer.write(sb.toString());
                writer.close();

                ob.add("status", "ok");
                ob.add("fileName", fileName);
            } catch (Exception ex) {
                Logger.getLogger(DownloadApk.class.getName()).log(Level.SEVERE, null, ex);
                ob.add("status", "error");
                ob.add("msg", ex.getMessage());
            } finally {
                w.writeObject(ob.build());
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
