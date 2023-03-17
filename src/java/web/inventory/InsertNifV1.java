package web.inventory;

import api.trk.model.TrkSimerRequest;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import service.MySQL.MySQLCommon;
import service.MySQL.MySQLSelect;
import utilities.JsonUtils;
import utilities.MySQLQuery;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

@MultipartConfig
@WebServlet(name = "InsertNifV1", urlPatterns = {"/InsertNifV1"})
public class InsertNifV1 extends HttpServlet {

    private String msg = "";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        Connection conn = null;
        int trkCylId = -1;
        try (JsonWriter w = Json.createWriter(response.getOutputStream())) {
            JsonObjectBuilder ob = Json.createObjectBuilder();
            try {
                conn = MySQLCommon.getConnection("sigmads", null);
                response.addHeader("Access-Control-Allow-Credentials", "true");
                response.addHeader("Access-Control-Allow-Origin", "*");
                response.addHeader("Access-Control-Allow-Methods", "GET, PUT, POST, OPTIONS, DELETE");

                TrkSimerRequest tsr = new TrkSimerRequest();
                tsr.dt = new Date();

                JsonObject req = MySQLQuery.scapeJsonObj(request);
                Nif nif = new Nif();

                nif.hasTara = req.getBoolean("hasTara");
                nif.insertNif = req.getBoolean("insertNif");

                nif.nifY = req.getString("nifY").trim();
                nif.nifF = req.getString("nifF").trim();
                nif.nifS = req.getString("nifS").trim();

                nif.tara = JsonUtils.getString(req, "tara");
                nif.tara = nif.tara.replaceAll(",", ".");
                nif.fabDate = req.getString("fabDate").trim();
                nif.cylTypeId = req.getString("cylTypeId").trim();
                nif.kg = new MySQLQuery("SELECT ct.kg FROM cylinder_type ct WHERE ct.id = " + nif.cylTypeId).getAsString(conn);
                nif.factoryId = req.getString("factoryId").trim();

                int status = 200;//sucecs Montagas

                boolean exit = true;
                int tries = 1;

                Integer facLen = new MySQLQuery("SELECT c.fac_len  "
                        + "FROM trk_cyl c  "
                        + "INNER JOIN cylinder_type ct ON ct.id = c.cyl_type_id  "
                        + "WHERE  c.nif_y = '" + nif.nifY + "' AND c.nif_f = '" + nif.nifF + "' AND c.nif_s = '" + nif.nifS + "'").getAsInteger(conn);

                String nifTotal = (StringUtils.leftPad(nif.nifY, 2, "0") + StringUtils.leftPad(nif.nifF, facLen != null ? facLen : nif.nifF.length(), "0") + StringUtils.leftPad(nif.nifS, 6, "0"));
                Object[] invCfg = new MySQLQuery("SELECT c.link_simer, c.simer_key, c.send_nif FROM inv_cfg c").getRecord(conn);
                Boolean sendNif = MySQLQuery.getAsBoolean(invCfg[2]);
                if (sendNif) { // envio a Simer
                    //clcenvasado.com/api/addcilindro?nif=0,category=5&tara=32&key=21D554A7293A493CA3E87ABCF5FA9558

                    if (invCfg == null || invCfg.length == 0) {
                        throw new Exception("No se ha definido configuraciones del modulo");
                    } else if (invCfg[0] == null) {
                        throw new Exception("No se ha definido Link de Simer");
                    } else if (invCfg[1] == null) {
                        throw new Exception("No se ha definido Key de Simer");
                    }

                    String url = invCfg[0] // url
                            + "?nif=" + nifTotal
                            + "&category=" + nif.kg
                            + "&tara=" + nif.tara
                            + "&key=" + invCfg[1];// key

                    tsr.request = url;

                    do {
                        URLConnection con = new URL(url).openConnection();
                        con.setDoOutput(false);
                        try (InputStream is = con.getInputStream()) {
                            JsonObject reqSimer = Json.createReader(is).readObject();

                            String msgSimer = reqSimer.getString("msg");
                            int statusSimer = Integer.valueOf(reqSimer.getString("code"));
                            status = statusSimer;
                            switch (statusSimer) {
                                case 500:
                                    tsr.status = 500;
                                    if (tries >= 3) {
                                        exit = false;
                                        msg = "Error al guardar el Cilindro en Simer \nCode: [" + msgSimer + "]";
                                    }
                                    break;
                                case 401:
                                    tsr.status = 401;
                                    exit = false;
                                    msg = "No Autorizado Simer \nCode: [" + msgSimer + "]";
                                    break;
                                case 404:
                                    tsr.status = 404;
                                    exit = false;
                                    msg = "No se encontró respuesta del Servidor Simer \nCode: [" + msgSimer + "]";
                                    break;
                                case 200:
                                    tsr.status = 200;
                                    exit = false;
                                    msg = "Se envió Correctamente el Cilindro a Simer";
                                    break;
                                default:
                                    tsr.status = 0;
                                    msg = msgSimer;
                                    exit = false;
                                    break;
                            }
                            tsr.msg = msg;
                        } catch (Exception ex) {
                            String error = ex.getMessage() != null && !ex.getMessage().isEmpty() ? ex.getMessage() : ex.getClass().getSimpleName();
                            error = "Error en conexión con Simer: " + error;
                            throw new Exception(error);
                        }
                        tries++;
                        if (tries == 3) {
                            exit = true;
                        }
                    } while (exit);
                }
                String sigmaEvent = "no se inserta en sigma";
                if (status == 200) {// success
                    Object[] cyl = new MySQLQuery("SELECT c.id, c.tara "
                            + "FROM trk_cyl c  "
                            + "WHERE c.nif_y = '" + nif.nifY + " '  "
                            + "AND c.nif_f = '" + nif.nifF + "'  "
                            + "AND c.nif_s =  '" + nif.nifS + "'").getRecord(conn);
                    if (cyl == null || cyl.length == 0) {
                        if (nif.insertNif) {
                            trkCylId = insertNif(nif, conn);
                            msg = (sendNif ? msg : "Se agregó el Cilindro");
                            sigmaEvent = "Cilindro no existe\n"
                                    + "se inserta en sigma";
                        } else {
                            sigmaEvent = "Cilindro no existe\n"
                                    + "No se inserto el cilindro";
                            trkCylId = 0;
                            Logger.getLogger(InsertNifV1.class.getName()).log(Level.SEVERE, null, "No se inserto el cilindro");
                        }
                    } else {
                        sigmaEvent = "Existe cilindro\n";
                        trkCylId = MySQLQuery.getAsInteger(cyl[0]);
                        if (nif.hasTara) {
                            BigDecimal oldTara = MySQLQuery.getAsBigDecimal(cyl[1], true);
                            BigDecimal newTara = new BigDecimal(nif.tara.replaceAll(",", "."));
                            if (oldTara.compareTo(newTara) != 0) {
                                new MySQLQuery("UPDATE trk_cyl SET tara = ?1 WHERE id = " + trkCylId).setParam(1, newTara).executeUpdate(conn);
                                msg = (sendNif ? msg : "Se actualizó Tara en el Cilindro");
                                sigmaEvent += msg;
                            }
                        }
                    }
                }

                tsr.sigmaEvent = sigmaEvent;
                tsr.insert(conn);

                ob.add("status", "OK");
                ob.add("code", status);
                ob.add("msg", msg);
                ob.add("id", trkCylId);
            } catch (Exception ex) {
                Logger.getLogger(InsertNifV1.class.getName()).log(Level.SEVERE, null, ex);
                ob.add("result", "ERROR");
                ob.add("code", 500);
                ob.add("msg", (ex.getMessage() != null && !ex.getMessage().isEmpty()) ? ex.getMessage() : "Error desconocido.");
                ob.add("id", trkCylId);
            } finally {
                w.writeObject(ob.build());
            }
        } catch (Exception ex) {
            Logger.getLogger(InsertNifV1.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            MySQLSelect.tryClose(conn);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    private int insertNif(Nif nif, Connection conn) throws Exception {
        String q = "INSERT INTO trk_cyl SET "
                + "nif_y= " + nif.nifY + ", "
                + "nif_f= " + nif.nifF + ", "
                + "nif_s= " + nif.nifS + ", "
                + "ok=1, "
                + "active=1, "
                + "notes=null, "
                + "fab_date =  '" + nif.fabDate + "' , "
                + "cyl_type_id= " + nif.cylTypeId + ", "
                + "factory_id= " + nif.factoryId + ", "
                + "has_label=0, "
                + (nif.hasTara ? "tara = " + nif.tara + ", " : "")
                + "create_date = NOW()";
        return new MySQLQuery(q).executeInsert(conn);
    }

    class Nif {

        public String nifY;
        public String nifF;
        public String nifS;
        public String fabDate;
        public String cylTypeId;
        public String factoryId;
        public String tara;
        public String kg;
        public boolean hasTara;
        public boolean insertNif;

    }
}
