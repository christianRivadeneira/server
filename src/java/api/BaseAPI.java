package api;

import api.bill.model.BillInstance;
import api.sys.model.Token;
import api.sys.model.TokenThPoll;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import model.system.AuthenticationException;
import model.system.SessionLogin;
import service.MySQL.MySQLCommon;
import utilities.AES;
import utilities.MySQLQuery;
import utilities.json.JSONDecoder;
import utilities.json.JSONEncoder;
import web.ShortException;
import web.billing.BillingServlet;
import web.fileManager;

public class BaseAPI {

    @Context
    private HttpHeaders headers;

    protected Token token;
    private TokenThPoll tokenThPoll;
    private SessionLogin sl = null;
    private TimeZone clientTz;

    public String getAuthorization() {
        MultivaluedMap<String, String> heads = headers.getRequestHeaders();
        String s = null;
        if (heads.containsKey("authorization")) {
            s = heads.getFirst("authorization").trim();
        } else if (heads.containsKey("Authorization")) {
            s = heads.getFirst("Authorization").trim();
        }
        if (s != null) {
            if (s.startsWith("Bearer") || s.startsWith("bearer")) {
                s = s.substring(7);
            }
        }
        return s;
    }

    public Connection getConnectionThPoll() throws Exception {
        String authorization = getAuthorization();

        String decripted = AES.decrypt(authorization, TokenThPoll.KEY);
        if (decripted == null) {
            throw new AuthenticationException("Error inesperado");
        }
        tokenThPoll = new TokenThPoll().deserialize(decripted);
        return MySQLCommon.getConnection(tokenThPoll.p, tokenThPoll.t);
    }

    public TokenThPoll getTokenThPoll() throws Exception {
        if (tokenThPoll.validateToken()) {
            if (tokenThPoll.ipe == 0) {
                throw new Exception("invalid_token");
            }
            return tokenThPoll;
        } else {
            throw new Exception("expired_token");
        }
    }

    public static Token getToken(String session) throws Exception {
        if (session == null || session.length() < 25) {
            return null;
        } else {
            String decripted = AES.decrypt(session, Token.KEY);
            if (decripted == null) {
                throw new AuthenticationException("Error inesperado");
            }
            if (decripted.startsWith("{")) {
                ByteArrayInputStream bais = new ByteArrayInputStream(decripted.getBytes());
                return new JSONDecoder().getObject(bais, Token.class);
            } else {
                return new Token().deserialize(decripted);
            }
        }
    }

    public Connection getConnection() throws Exception {
        String authorization = getAuthorization();
        if (authorization == null || authorization.length() < 25) {
            return MySQLCommon.getDefaultConnection();
        } else {
            token = getToken(authorization);
            return MySQLCommon.getConnection(token.p, token.t);
        }
    }

    public TimeZone getTimeZone() {
        if (clientTz == null) {
            clientTz = TimeZone.getTimeZone(token.t);
        }
        return clientTz;
    }

    public BillInstance getBillInstance() throws Exception {
        return BillingServlet.getInst(getBillInstId());
    }

    public Integer getBillInstId() {
        return MySQLQuery.getAsInteger(headers.getHeaderString("x-billing-inst-id"));
    }

    public Connection useBillInstance(Connection conn) throws Exception {
        Integer i = getBillInstId();
        if (i == null) {
            throw new Exception("Request should contain instance_id");
        }
        new MySQLQuery("USE " + BillingServlet.getDbName(i)).executeUpdate(conn);
        return conn;
    }

    public Connection useBillInstance(Integer instanceId, Connection conn) throws Exception {
        new MySQLQuery("USE " + BillingServlet.getDbName(instanceId)).executeUpdate(conn);
        return conn;
    }

    public Connection useDefault(Connection conn) throws Exception {
        new MySQLQuery("USE sigma").executeUpdate(conn);
        return conn;
    }

    public SessionLogin getSession(Connection conn) throws Exception {
        if (sl != null) {
            return sl;
        }
        String authorization = getAuthorization();
        if (authorization == null) {
            throw new AuthenticationException("Error inesperado");
        }
        if (authorization.length() < 25) {
            sl = SessionLogin.validate(authorization, conn);
        } else {
            sl = SessionLogin.validate(token.id, conn, null);
        }
        return sl;
    }

    public Response createResponse() {
        return Response.ok().build();
    }

    public Response createResponse(Object obj) {
        if (obj instanceof ShortException) {
            ((ShortException) obj).simplePrint();
            return errorResponse((Exception) obj, Status.INTERNAL_SERVER_ERROR);
        } else if (obj instanceof AuthenticationException) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, (Exception) obj);
            return errorResponse((Exception) obj, Status.UNAUTHORIZED);
        } else if (obj instanceof Exception) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, (Exception) obj);
            return errorResponse((Exception) obj, Status.INTERNAL_SERVER_ERROR);
        }
        return Response.ok(obj).build();
    }

    public Response createResponse(final File f, String fileName) {
        return this.createResponse(f, fileName, false);
    }

    public Response createResponse(final File f, final String fileName, final boolean gzip) {
        StreamingOutput so = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                if (gzip) {
                    try (FileInputStream fis = new FileInputStream(f); GZIPOutputStream zos = new GZIPOutputStream(output)) {
                        fileManager.copy(fis, zos);
                    }
                } else {
                    try (FileInputStream fis = new FileInputStream(f)) {
                        fileManager.copy(fis, output);
                    }
                }

                output.flush();

                if (isTemp(f)) {
                    System.out.println("Deleting temp file: " + f.getAbsolutePath());
                    f.delete();
                }
            }
        };
        Response.ResponseBuilder rb = Response.ok(so, getMediaType(fileName));
        rb.header("Access-Control-Expose-Headers", "content-disposition,x-suggested-file-name");
        rb.header("content-disposition", "attachment; filename = " + fileName);
        rb.header("x-suggested-file-name", fileName);
        return rb.build();
    }

    public Response createResponse(Object rta, String fileName) throws IOException, Exception {
        if (rta instanceof List) {
            File tmp = File.createTempFile(fileName, "zip");
            try (FileOutputStream fos = new FileOutputStream(tmp); GZIPOutputStream goz = new GZIPOutputStream(fos)) {
                JSONEncoder.encode(rta, goz, true);
            } catch (Exception e) {
                throw e;
            }
            return createResponse(tmp, fileName, false);
        } else {
            throw new Exception("No implementado");
        }
    }

    private boolean isTemp(File f) {
        String p2 = f.getAbsolutePath();
        String p1 = System.getProperty("java.io.tmpdir");

        for (int i = 0; i < Math.min(p1.length(), p2.length()); i++) {
            if (p1.charAt(i) != p2.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public final static Map<String, String> MIME_TYPES;

    static {
        MIME_TYPES = new HashMap<>();
        MIME_TYPES.put("pdf", "application/pdf");
        MIME_TYPES.put("jpg", "image/jpeg");
        MIME_TYPES.put("jpeg", "image/jpeg");
        MIME_TYPES.put("png", "image/png");
        MIME_TYPES.put("xls", "application/vnd.ms-excel");
        MIME_TYPES.put("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        MIME_TYPES.put("doc", "application/msword");
        MIME_TYPES.put("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        MIME_TYPES.put("csv", "text/csv");
        MIME_TYPES.put("txt", "text/plain");
        MIME_TYPES.put("zip", "application/zip");
        MIME_TYPES.put("rar", "application/x-rar");
        MIME_TYPES.put("ppt", "application/vnd.ms-powerpoint");
        MIME_TYPES.put("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
    }

    private static String getMediaType(String fName) {
        if (fName == null || fName.isEmpty()) {
            return "application/octet-stream";
        }
        int li = fName.lastIndexOf(".");
        if (li < 0) {
            return "application/octet-stream";
        }
        String fExt = fName.substring(li + 1, fName.length()).toLowerCase();
        String type = MIME_TYPES.get(fExt);
        if (type == null) {
            return "application/octet-stream";
        }
        return type;
    }

    /**
     * Now() de la base de datos sin tener en cuenta la zona horaria, si son las
     * 4PM en Colombia el retorna como si fueran las 4pm en greenwich, o en la
     * zona donde esté el server
     *
     * @param conn
     * @return
     */
    public Date now(Connection conn) throws Exception {
        return MySQLQuery.now(conn);
    }

    private static Response errorResponse(Exception ex, Status st) {
        String m = ex.getMessage() != null && !ex.getMessage().isEmpty() ? ex.getMessage() : ex.getClass().toString();
        return Response.serverError().encoding("UTF-8").entity(m).status(st).build();
    }

}
