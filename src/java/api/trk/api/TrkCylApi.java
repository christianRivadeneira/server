package api.trk.api;

import api.BaseAPI;
import api.MultiPartRequest;
import api.dto.importer.CSVReader;
import api.trk.model.TrkCyl;
import api.trk.model.TrkCylNov;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import web.ShortException;
import web.fileManager.PathInfo;
import web.marketing.cylSales.CylValidations;

@Path("/trkCyl")
public class TrkCylApi extends BaseAPI {

    private static final DecimalFormat DF = new DecimalFormat("0.00");

    @GET
    @Path("/verify")
    public Response verify(@QueryParam("nif") String nif, @QueryParam("invCenterId") Integer invCenterId) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            String pkgName = new MySQLQuery("SELECT package_name FROM system_app WHERE id = ?1").setParam(1, sl.appId).getAsString(conn);
            boolean alternate = pkgName.equals(CylValidations.SALES);
            CylValidations val = CylValidations.getValidations(nif, sl.employeeId, alternate, pkgName, invCenterId, conn);
            return Response.ok(val).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    @Path("/{id}/updateTara/")
    public Response updateTara(@PathParam("id") int id, @QueryParam("tara") String sTara) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            sTara = sTara.replaceAll(",", ".");
            BigDecimal tara = sTara != null ? new BigDecimal(sTara) : null;
            if (tara == null || tara.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ShortException("La tara debe ser mayor que cero.");
            }
            TrkCyl cyl = new TrkCyl().select(id, conn);
            if (cyl == null) {
                throw new ShortException("Id inválido.");
            }

            TrkCylNov nov = new TrkCylNov();
            nov.cylId = id;
            nov.dt = new Date();
            nov.empId = sl.employeeId;
            nov.notes = "Se cambió la tara, era " + (cyl.tara != null ? DF.format(cyl.tara) : DF.format(BigDecimal.ZERO));
            nov.type = "upd_tara";
            nov.insert(conn);

            cyl.tara = tara;
            cyl.update(conn);
            return Response.ok(cyl).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/alternativeNif")
    public Response alternativeNif(@QueryParam("nif") String nif, @QueryParam("empId") int empId) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            TrkCyl result = TrkCyl.selectByNif(nif, conn);
            if (result == null) {
                throw new ShortException("El nif " + nif + " no está registrado en " + new MySQLQuery("SELECT name FROM enterprise WHERE !alternative").getAsString(conn));
            }
            if (new MySQLQuery("SELECT phantom_nif FROM com_cfg WHERE id = 1").getAsBoolean(conn)) {
                Object[] row = new MySQLQuery("SELECT "
                        + "MAX(s.date) < DATE_SUB(NOW(), INTERVAL (SELECT cfg.days_sale FROM dto_cfg cfg WHERE cfg.id = 1) DAY), "
                        + "c.cyl_type_id "
                        + "FROM trk_cyl c "
                        + "INNER JOIN trk_sale s ON s.cylinder_id = c.id "
                        + "WHERE "
                        + "s.sale_type = 'sub' "
                        + "AND !s.training "
                        + "AND c.nif_y = " + result.nifY + " "
                        + "AND c.nif_f = " + result.nifF + " "
                        + "AND c.nif_s = " + result.nifS).getRecord(conn);

                if (row != null) {
                    Boolean isValid = MySQLQuery.getAsBoolean(row[0]);
                    if (isValid != null && !isValid) {
                        Object[] altNifRow = CylValidations.getAlternativeNif(MySQLQuery.getAsInteger(row[1]), empId, conn);
                        if (altNifRow != null && altNifRow.length > 0) {
                            result = new TrkCyl();
                            Integer[] nifParts = TrkCyl.getNifParts(MySQLQuery.getAsString(altNifRow[1]));
                            result.nifY = nifParts[0];
                            result.nifF = nifParts[1];
                            result.nifS = nifParts[2];
                            result.cylTypeId = MySQLQuery.getAsInteger(altNifRow[0]);
                            result.id = MySQLQuery.getAsInteger(altNifRow[0]);
                        }
                    }
                }
            }

            return Response.ok(result).build();
        } catch (Exception ex) {
            Logger.getLogger(TrkCylApi.class.getName()).log(Level.SEVERE, null, ex);
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getCylFromNif")
    public Response getCylFromNif(@QueryParam("nif") String nif) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            TrkCyl cyl = TrkCyl.selectByNif(nif, conn);
            cyl.typeName = new MySQLQuery("SELECT name FROM cylinder_type WHERE id = " + cyl.cylTypeId).getAsString(conn);
            return createResponse(cyl);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("suiFile")
    public Response anallyzeSuiFile(@Context HttpServletRequest request) {
        try (Connection conn = getConnection()) {
            PathInfo pi = new PathInfo(conn);
            MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 4096);
            Class[] colsCsv = new Class[]{
                String.class,
                String.class,
                String.class,
                String.class};

            Object[][] data = CSVReader.read(mr.getFile().file, Charset.forName("UTF-8"), ";", colsCsv);
            if (data == null || data.length < 2) {
                throw new Exception("El archivo no contiene registros");
            }

            Object[][] factData = new MySQLQuery("SELECT "
                    + "f.id, "
                    + "f.name, "
                    + "c.code "
                    + "FROM inv_factory f "
                    + "INNER JOIN inv_fac_code c ON c.inv_factory_id = f.id "
                    + "WHERE f.active "
                    + "ORDER BY c.code ASC").getRecords(conn);

            Object[][] typeData = new MySQLQuery("SELECT "
                    + "t.id, "
                    + "t.sui_rpt_code "
                    + "FROM cylinder_type t "
                    + "WHERE t.sui_rpt_code IS NOT NULL").getRecords(conn);

            int headRow = -1;
            for (int i = 0; i < data.length; i++) {
                if (Header.isHeader(data[i])) {
                    headRow = i;
                    break;
                }
            }
            if (headRow == -1) {
                throw new Exception("No se encontró la fila de encabezados.");
            }

            Header head = new Header(data[headRow]);
            List<String> errors = new ArrayList<>();

            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(new Date());
            int curYear = gc.get(GregorianCalendar.YEAR);
            for (int i = headRow + 1; i < data.length; i++) {
                Object[] row = data[i];
                if (!Header.isAllWhite(row)) {
                    if (MySQLQuery.getAsInteger(row[head.yearPos]) > (curYear - 2000)) {
                        errors.add("Se encontró un año superior al actual: " + row[head.yearPos] + ". Fila " + (i + 1));
                    }
                    if (getFactory(MySQLQuery.getAsInteger(row[head.factPos]), factData) == null) {
                        errors.add("No se encontró fábrica con el código " + row[head.factPos] + ". Fila " + (i + 1));
                    }
                    if (MySQLQuery.getAsInteger(row[head.serialPos]).toString().length() > 6) {
                        errors.add("El consecutivo de fábrica " + row[head.serialPos] + " excede el límite de 6 cifras. Fila " + (i + 1));
                    }
                    if (getCylType(MySQLQuery.getAsString(row[head.refPos]), typeData) == null) {
                        errors.add("No se encontró referencia con el código " + row[head.refPos] + ". Fila " + (i + 1));
                    }
                }
            }

            return createResponse(errors);
        } catch (Exception e) {
            return createResponse(e);
        }
    }

    @POST
    @Path("sui")
    public Response importSuiFile(@Context HttpServletRequest request) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                SessionLogin sl = getSession(conn);
                PathInfo pi = new PathInfo(conn);
                MultiPartRequest mr = new MultiPartRequest(request, (pi.maxFileSizeKb + 128) * 4096);
                Class[] colsCsv = new Class[]{
                    String.class,
                    String.class,
                    String.class,
                    String.class};

                Object[][] data = CSVReader.read(mr.getFile().file, Charset.forName("UTF-8"), ";", colsCsv);
                if (data == null || data.length < 2) {
                    throw new Exception("El archivo no contiene registros");
                }

                new MySQLQuery("UPDATE trk_cyl SET sui_reported = 0").executeUpdate(conn);
                Integer wantedId = new MySQLQuery("SELECT id FROM trk_wanted WHERE sui_importer").getAsInteger(conn);
                new MySQLQuery("DELETE FROM trk_cyl_wanted WHERE wanted_id = " + wantedId).executeDelete(conn);

                Object[][] factData = new MySQLQuery("SELECT "
                        + "f.id, "
                        + "f.name, "
                        + "c.code "
                        + "FROM inv_factory f "
                        + "INNER JOIN inv_fac_code c ON c.inv_factory_id = f.id "
                        + "WHERE f.active "
                        + "ORDER BY c.code ASC").getRecords(conn);

                Object[][] typeData = new MySQLQuery("SELECT "
                        + "t.id, "
                        + "t.sui_rpt_code "
                        + "FROM cylinder_type t "
                        + "WHERE t.sui_rpt_code IS NOT NULL").getRecords(conn);

                List<TrkCyl> cylsToInsert = new ArrayList<>();
                List<TrkCyl> cylsToUpdate = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                int headRow = -1;
                for (int i = 0; i < data.length; i++) {
                    if (Header.isHeader(data[i])) {
                        headRow = i;
                        break;
                    }
                }
                if (headRow == -1) {
                    throw new Exception("No se encontró la fila de encabezados.");
                }

                Header head = new Header(data[headRow]);
                for (int i = headRow + 1; i < data.length; i++) {
                    Object[] row = data[i];

                    TrkCyl cyl = new TrkCyl();
                    cyl.suiReported = true;
                    cyl.nifY = MySQLQuery.getAsInteger(row[head.yearPos]);
                    cyl.nifF = MySQLQuery.getAsInteger(row[head.factPos]);
                    cyl.nifS = MySQLQuery.getAsInteger(row[head.serialPos]);
                    cyl.cylTypeId = getCylType(MySQLQuery.getAsString(row[head.refPos]), typeData);
                    cyl.ok = true;
                    cyl.active = true;
                    Integer cylId = new MySQLQuery("SELECT id FROM trk_cyl WHERE nif_y = ?1 AND nif_f = ?2 AND nif_s = ?3")
                            .setParam(1, cyl.nifY)
                            .setParam(2, cyl.nifF)
                            .setParam(3, cyl.nifS)
                            .getAsInteger(conn);

                    if (cylId != null) {
                        cyl.id = cylId;
                        cylsToUpdate.add(cyl);
                    } else {
                        cyl.fabDate = sdf.parse((cyl.nifY + 2000) + "-01-01");
                        cyl.factoryId = getFactory(MySQLQuery.getAsInteger(row[head.factPos]), factData);
                        cyl.imported = true;
                        cyl.facLen = MySQLQuery.getAsString(cyl.nifF).length();
                        cyl.hasLabel = false;
                        cyl.createDate = new Date();
                        cyl.empVerifier = sl.employeeId;
                        cyl.salable = false;
                        cyl.minasReported = new MySQLQuery("SELECT COUNT(*) > 0 "
                                + "FROM dto_minas_cyl c "
                                + "WHERE c.y = ?1 "
                                + "AND c.f = ?2 "
                                + "AND c.s = ?3")
                                .setParam(1, cyl.nifY)
                                .setParam(2, cyl.nifF)
                                .setParam(3, cyl.nifS)
                                .getAsBoolean(conn);

                        cylsToInsert.add(cyl);
                    }
                }

                for (int i = 0; i < cylsToInsert.size(); i++) {
                    TrkCyl cyl = cylsToInsert.get(i);
                    cyl.insert(conn);
                }

                for (int i = 0; i < cylsToUpdate.size(); i++) {
                    TrkCyl cyl = cylsToUpdate.get(i);
                    new MySQLQuery("UPDATE trk_cyl SET sui_reported = 1 WHERE id = " + cyl.id).executeUpdate(conn);
                }

                Object[][] cyls = new MySQLQuery("SELECT "
                        + "c.id "
                        + "FROM trk_cyl c "
                        + "WHERE !c.sui_reported").getRecords(conn);

                for (int i = 0; i < cyls.length; i++) {
                    Object[] cyl = cyls[i];
                    new MySQLQuery("INSERT INTO trk_cyl_wanted "
                            + "SET wanted_id = " + wantedId + ", "
                            + "cyl_id = " + cyl[0]).executeInsert(conn);
                }

                conn.commit();
                String result = "Creados: " + cylsToInsert.size() + " cilindros\nActualizados: " + cylsToUpdate.size() + " cilindros.";
                return createResponse(result);
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            return createResponse(e);
        }
    }

    private Integer getFactory(int fileFactCode, Object[][] factData) {
        Integer result = null;

        for (int i = 0; i < factData.length; i++) {
            Object[] row = factData[i];
            if (fileFactCode == MySQLQuery.getAsInteger(row[2])) {
                result = MySQLQuery.getAsInteger(row[0]);
                break;
            }
        }

        return result;
    }

    private Integer getCylType(String fileTypeCode, Object[][] typeData) {
        Integer result = null;

        for (int i = 0; i < typeData.length; i++) {
            Object[] row = typeData[i];
            if (MySQLQuery.getAsString(fileTypeCode).toUpperCase().equals(MySQLQuery.getAsString(row[1]).toUpperCase())) {
                result = MySQLQuery.getAsInteger(row[0]);
                break;
            }
        }

        return result;
    }

}

class Header {

    public int yearPos = -1;
    public int factPos = -1;
    public int serialPos = -1;
    public int refPos = -1;

    public static boolean isHeader(Object[] row) {
        boolean yearFound = false;
        boolean factFound = false;
        boolean serialFound = false;
        boolean refFound = false;

        for (Object cell : row) {
            String s = normalize(cell);
            if (in(s, "AÑO", "año")) {
                yearFound = true;
            } else if (in(s, "CODIGO FABRICA", "codigo fabrica")) {
                factFound = true;
            } else if (in(s, "CONSECUTIVO FABRICA", "consecutivo fabrica")) {
                serialFound = true;
            } else if (in(s, "REFERENCIA", "referencia")) {
                refFound = true;
            }
        }

        return yearFound && factFound && serialFound && refFound;
    }

    public Header(Object[] headerRow) throws Exception {
        //FECHA HORA	DOCUMENTO   CAP         VALOR   SUBSIDIO    ID VENDEDOR	NIF	ESTRATO	DEPTO   MUNICIPIO   No. APROBACIÓN      FACTURA
        for (int i = 0; i < headerRow.length; i++) {
            if (in(headerRow[i], "AÑO", "año")) {
                yearPos = i;
            } else if (in(headerRow[i], "CODIGO FABRICA", "codigo fabrica", "CÓDIGO FÁBRICA", "código fábrica")) {
                factPos = i;
            } else if (in(headerRow[i], "CONSECUTIVO FABRICA", "consecutivo fabrica", "CONSECUTIVO FÁBRICA", "consecutivo fábrica")) {
                serialPos = i;
            } else if (in(headerRow[i], "REFERENCIA", "referencia")) {
                refPos = i;
            }
        }

        if (yearPos == -1) {
            throw new Exception("Falta la columna de Año");
        }
        if (factPos == -1) {
            throw new Exception("Falta la columna de Código fábrica");
        }
        if (serialPos == -1) {
            throw new Exception("Falta la columna de Consecutivo fábrica");
        }
        if (refPos == -1) {
            throw new Exception("Falta la columna de Referencia");
        }

    }

    public static boolean in(Object header, Object... lst) {
        String headerStr = normalize(header);
        for (Object e : lst) {
            if (headerStr.equals(normalize(e))) {
                return true;
            }
        }
        return false;
    }

    public static String normalize(Object o) {
        return o != null ? o.toString().toUpperCase().replaceAll("[^A-Z]", " ").replaceAll("\\s+", " ").trim() : "";
    }

    public static boolean isAllWhite(Object[] row) {
        for (Object cell : row) {
            if (cell != null && !cell.toString().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
