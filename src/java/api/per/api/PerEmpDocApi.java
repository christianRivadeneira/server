package api.per.api;

import api.BaseAPI;
import api.sys.model.SysCrudLog;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import api.per.model.PerEmpDoc;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import utilities.MySQLQuery;

@Path("/perEmpDoc")
public class PerEmpDocApi extends BaseAPI {

    @POST
    public Response insert(PerEmpDoc obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(PerEmpDoc obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            PerEmpDoc old = new PerEmpDoc().select(obj.id, conn);
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
            getSession(conn);
            PerEmpDoc obj = new PerEmpDoc().select(id, conn);
            return createResponse(obj);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            PerEmpDoc.delete(id, conn);
            SysCrudLog.deleted(this, PerEmpDoc.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/all")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(PerEmpDoc.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getDocumentZipByType")
    public Response getDocumentZipByType(@QueryParam("docTypeId") int docTypeId, @QueryParam("activeEmp") int activeEmp) {
        try (Connection conn = getConnection()) {
            String filesDir = new MySQLQuery("SELECT files_path FROM sys_cfg").getAsString(conn);

            if (filesDir == null || filesDir.isEmpty()) {
                throw new Exception("Atención, no se ha definido directorio de adjuntos.");
            }

            Object[][] filesData = new MySQLQuery("SELECT "
                    + "b.id, "
                    + "pe.document, "
                    + "b.file_name "
                    + "FROM bfile b "
                    + "INNER JOIN per_emp_doc ped ON b.owner_id = ped.id AND b.owner_type = 7 "
                    + "INNER JOIN per_employee pe ON ped.emp_id = pe.id "
                    + "WHERE ped.doc_type_id = " + docTypeId + " "
                    + (activeEmp == 0 ? "AND pe.active " : activeEmp == 1 ? "AND !pe.active " : " ")
                    + "ORDER BY pe.document ASC ").getRecords(conn);

            try (FileWriter fw = new FileWriter("/tmp/script.sh")) {
                fw.write("#!/bin/sh" + System.lineSeparator());
                fw.write("mkdir /tmp/documentos" + System.lineSeparator());
                fw.write("cd " + filesDir + System.lineSeparator());
                for (int i = 0; i < filesData.length; i++) {
                    Object[] file = filesData[i];
                    String[] fileName = MySQLQuery.getAsString(file[2]).split("[.]");
                    fw.write("find . -type f -name \"" + MySQLQuery.getAsInteger(file[0]) + ".bin\" -exec cp -f {} /tmp/documentos/" + MySQLQuery.getAsString(file[1]) + "." + fileName[fileName.length - 1] + " \\;" + System.lineSeparator());
                }
                
                fw.write("cd /tmp" + System.lineSeparator());
                fw.write("tar -czvf documentos.tar.gz documentos/" + System.lineSeparator());
            }

            exec("chmod +x /tmp/script.sh");
            exec("/tmp/script.sh");
            exec("rm -R /tmp/script.sh");
            exec("rm -R /tmp/documentos");

            File f = new File("/tmp/documentos.tar.gz");
            return createResponse(f, "documentos.tar.gz");
        } catch (Exception e) {
            return createResponse(e);
        }
    }

    private static void exec(String command) throws Exception {
        Process p = Runtime.getRuntime().exec(command);
        BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = in.readLine()) != null) {
            System.out.println(line);
        }
    }
}
