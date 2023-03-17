package api.est.api;

import api.BaseAPI;
import api.MultiPartRequest;
import api.est.dto.Data;
import api.est.dto.ResultDataMto;
import api.est.model.EstMto;
import api.est.model.EstMtoCertImport;
import api.est.model.EstMtoImport;
import api.sys.model.Bfile;
import api.sys.model.SysCrudLog;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import utilities.importer.Importer;
import utilities.importer.ImporterCol;
import utilities.xlsReader.XlsReader;
import web.fileManager;

@Path("/estMto")
public class EstMtoApi extends BaseAPI {

    public static final int EST_MTO_IMPORT = 146;
    public static final int EST_MTO_CERT_IMPORT = 147;
    public static final int EST_MTO_REG = 28;

    @POST
    public Response insert(EstMto obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(EstMto obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            EstMto old = new EstMto().select(obj.id, conn);
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
            EstMto obj = new EstMto().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            EstMto.delete(id, conn);
            SysCrudLog.deleted(this, EstMto.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/all")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            return createResponse(EstMto.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/valMto")
    public Response valMto(@Context HttpServletRequest request) throws Exception {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);

            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 4096);

            Object[][] data = XlsReader.readExcel(mr.getFile().file.getPath(), 0).getData();

            if (data == null || data.length < 2) {
                throw new Exception("El archivo no contiene registros");
            }

            List<ImporterCol> cols = new ArrayList<>();
            cols.add(new ImporterCol("Serie", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("FECHA INSPECCION PARCIAL MAIKO ", ImporterCol.TYPE_DATE, false));
            cols.add(new ImporterCol("RESULTADO INSPECCION PARCIAL ", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("CERTIFICADO PARCIAL MAIKO ", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("OBSERVACIONES ", ImporterCol.TYPE_TEXT, false));

            Importer importer = new Importer(data, cols);

            ResultDataMto result = new ResultDataMto();
            List<Data> allRows = new ArrayList<>();
            List<Data> errorRows = new ArrayList<>();
            result.allRows = allRows;
            result.errorRows = errorRows;

            for (int i = importer.headRow + 1; i < data.length; i++) {
                importer.row = data[i];

                String serie = MySQLQuery.getAsString(importer.get(0));
                Date date = MySQLQuery.getAsDate(importer.get(1));
                String res = MySQLQuery.getAsString(importer.get(2));
                String cert = (MySQLQuery.getAsString(importer.get(3)) != null ? MySQLQuery.getAsString(importer.get(3)).toUpperCase() : null);
                String obs = MySQLQuery.getAsString(importer.get(4));

                Data fd = new Data();
                if (serie != null && !serie.isEmpty()) {
                    fd.serie = serie;
                } else {
                    fd.msgError = "Serie es obligatorio" + (fd.msgError.isEmpty() ? "" : ", " + fd.msgError);
                }

                if (date == null) {
                    fd.msgError = "Fecha es obligatorio" + (fd.msgError.isEmpty() ? "" : ", " + fd.msgError);
                } else {
                    fd.date = date;
                }

                if (res != null && !res.isEmpty()) {
                    fd.result = res.toUpperCase();
                } else {
                    fd.msgError = "Resultado es obligatorio" + (fd.msgError.isEmpty() ? "" : ", " + fd.msgError);
                }

                if (cert != null && !cert.isEmpty()) {
                    fd.certificate = cert;
                } else if (fd.result != null && fd.result.equals("SI")) {
                    fd.msgError = "Certificado es obligatorio" + (fd.msgError.isEmpty() ? "" : ", " + fd.msgError);
                }

                allRows.add(fd);
            }

            for (int i = 0; i < allRows.size(); i++) {
                Data v = allRows.get(i);
                MySQLQuery mq = new MySQLQuery("SELECT COUNT(*)>0 FROM est_mto m INNER JOIN est_tank t ON t.id = m.tank_id WHERE t.serial = ?1 AND m.certificate = ?2");
                mq.setParam(1, v.serie);
                mq.setParam(2, v.serie);
                v.duplicate = mq.getAsBoolean(conn);

                MySQLQuery mqTank = new MySQLQuery("SELECT COUNT(*)>0 FROM est_tank t WHERE t.serial = ?1");
                mqTank.setParam(1, v.serie);
                boolean isTank = mqTank.getAsBoolean(conn);

                if (v.msgError.isEmpty()) {
                    if (!isTank) {
                        v.msgError = "No se encontro el tanque";
                    }
                    if (!v.msgError.isEmpty()) {
                        errorRows.add(v);
                    }
                }

                if (!v.msgError.isEmpty()) {
                    errorRows.add(v);
                }
            }

            for (int i = 0; i < allRows.size() && errorRows.isEmpty(); i++) {
                Data get = allRows.get(i);
                for (int j = 0; j < allRows.size(); j++) {
                    Data obj = allRows.get(j);
                    if (get.date.equals(obj.date) && get.certificate.equals(obj.certificate) && i != j) {
                        boolean exists = false;

                        for (int k = 0; k < errorRows.size(); k++) {
                            Data error = errorRows.get(k);
                            if (obj.certificate.equals(error.certificate)) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            obj.msgError = "Registro duplicado en Fila " + (j + 2) + " Columna CERTIFICADO PARCIAL MAIKO, se repite " + obj.certificate + (get.msgError.isEmpty() ? "" : ", " + get.msgError);
                            errorRows.add(obj);
                        }
                    }
                }
            }

            return createResponse(result);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/importMto")
    public Response importMto(@Context HttpServletRequest request) throws Exception {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);

            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 4096);
            String notes = mr.params.get("notes");
            String fileName = mr.params.get("f");

            Object[][] data = XlsReader.readExcel(mr.getFile().file.getPath(), 0).getData();

            if (data == null || data.length < 2) {
                throw new Exception("El archivo no contiene registros");
            }

            List<ImporterCol> cols = new ArrayList<>();
            cols.add(new ImporterCol("Serie", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("FECHA INSPECCION PARCIAL MAIKO ", ImporterCol.TYPE_DATE, false));
            cols.add(new ImporterCol("RESULTADO INSPECCION PARCIAL ", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("CERTIFICADO PARCIAL MAIKO ", ImporterCol.TYPE_TEXT, false));
            cols.add(new ImporterCol("OBSERVACIONES ", ImporterCol.TYPE_TEXT, false));

            Importer importer = new Importer(data, cols);

            EstMtoImport objImport = new EstMtoImport();
            objImport.date = MySQLQuery.now(conn);
            objImport.notes = notes;
            objImport.empId = sl.employeeId;
            int importId = objImport.insert(conn);

            fileManager.upload(
                    sl.employeeId,
                    importId, //ownerId
                    EST_MTO_IMPORT,//ownerType, 
                    null, //tableName
                    fileName, //fileName,
                    null, //desc, 
                    false, //unique
                    null,//shrinkType
                    pi, mr.getFile().file, conn
            );

            for (int i = importer.headRow + 1; i < data.length; i++) {
                importer.row = data[i];

                String serie = MySQLQuery.getAsString(importer.get(0));
                Date date = MySQLQuery.getAsDate(importer.get(1));
                String res = MySQLQuery.getAsString(importer.get(2));
                String cert = MySQLQuery.getAsString(importer.get(3)).toUpperCase();
                String obs = MySQLQuery.getAsString(importer.get(4));

                MySQLQuery mq = new MySQLQuery("SELECT COUNT(*)>0 FROM est_mto m INNER JOIN est_tank t ON t.id = m.tank_id WHERE t.serial = ?1 AND m.certificate = ?2");
                mq.setParam(1, serie);
                mq.setParam(2, cert);
                boolean duplic = mq.getAsBoolean(conn);

                MySQLQuery mqTank = new MySQLQuery("SELECT t.id FROM est_tank t WHERE t.serial = ?1");
                mqTank.setParam(1, serie);
                Integer tankId = mqTank.getAsInteger(conn);

                if (!duplic && tankId != null && res.toUpperCase().trim().equals("SI")) {
                    new MySQLQuery("DELETE FROM est_mto WHERE tank_id = ?1 AND exec_date IS NULL;").setParam(1, tankId).executeDelete(conn);

                    EstMto obj = new EstMto();
                    obj.certificate = cert;
                    obj.execDate = date;
                    obj.notes = (obs != null && obs.length() > 512 ? obs.substring(0, 511) : obs);
                    obj.progDate = date;
                    obj.tankId = tankId;
                    obj.type = (cert.contains("T") ? "tot" : "par");
                    obj.importId = importId;
                    obj.lastPar = new MySQLQuery("SELECT last_par FROM est_tank WHERE id = ?1").setParam(1, tankId).getAsDate(conn);
                    obj.lastTot = new MySQLQuery("SELECT last_tot FROM est_tank WHERE id = ?1").setParam(1, tankId).getAsDate(conn);
                    obj.insert(conn);

                    new MySQLQuery("UPDATE est_tank SET "
                            + "last_par = COALESCE((SELECT MAX(exec_date) FROM est_mto WHERE type = 'par' AND tank_id = ?1), par_beg), "
                            + "last_tot = COALESCE((SELECT MAX(exec_date) FROM est_mto WHERE type = 'tot' AND tank_id = ?1), tot_beg)  "
                            + "WHERE id = ?1").setParam(1, tankId).executeUpdate(conn);

                    new MySQLQuery("UPDATE est_tank SET "
                            + "last_par = IF(last_tot > last_par, last_tot, last_par) "
                            + "WHERE id = ?1").setParam(1, tankId).executeUpdate(conn);
                }
            }

            return createResponse("ok");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/importCertMto")
    public Response importCertMto(@Context HttpServletRequest request) throws Exception {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);

            fileManager.PathInfo pi = new fileManager.PathInfo(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 4096);
            String notes = mr.params.get("notes");

            if (mr.files.isEmpty()) {
                throw new Exception("No hay elementos dentro de la carpeta actual");
            } else {
                String ids = "";
                for (int i = 0; i < mr.files.size(); i++) {
                    String name = mr.params.get("f" + i);
                    File file = mr.files.get("f" + i).file;
                    String cert = null;
                    String serie = null;
                    if (name.contains("[") && name.contains("]")) {
                        int indBeg = name.indexOf("[");
                        int indEnd = name.indexOf("]");
                        cert = name.substring(indBeg + 1, indEnd).toUpperCase();
                    }
                    if (name.contains("(") && name.contains(")")) {
                        int indBeg = name.indexOf("(");
                        int indEnd = name.indexOf(")");
                        serie = name.substring(indBeg + 1, indEnd);
                    }

                    MySQLQuery mq = new MySQLQuery("SELECT m.id FROM est_mto m INNER JOIN est_tank t ON t.id = m.tank_id WHERE t.serial = ?1 AND m.`type` = ?2 ORDER BY m.exec_date DESC LIMIT 1");
                    mq.setParam(1, serie);
                    mq.setParam(2, ((cert != null && cert.contains("T") ? "tot" : "par")));
                    Integer estMtoId = mq.getAsInteger(conn);

                    MySQLQuery mqTank = new MySQLQuery("SELECT t.id FROM est_tank t WHERE t.serial = ?1");
                    mqTank.setParam(1, serie);
                    Integer tankId = mqTank.getAsInteger(conn);

                    if (estMtoId != null && tankId != null) {
                        Bfile bfile = fileManager.upload(
                                sl.employeeId,
                                estMtoId, //ownerId
                                EST_MTO_REG,//ownerType, 
                                null, //tableName
                                name, //fileName,
                                null, //desc, 
                                false, //unique
                                null,//shrinkType
                                pi, file, conn
                        );
                        ids = ids + (ids.length() == 0 ? "" : ",") + bfile.id;
                    }

                }

                EstMtoCertImport objImport = new EstMtoCertImport();
                objImport.date = MySQLQuery.now(conn);
                objImport.notes = notes;
                objImport.empId = sl.employeeId;
                objImport.ids = ids;
                objImport.insert(conn);
            }
            return createResponse("ok");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
