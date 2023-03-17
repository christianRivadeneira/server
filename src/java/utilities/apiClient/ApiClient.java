package utilities.apiClient;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import javax.json.JsonObjectBuilder;
import java.io.File;
import javax.json.Json;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import utilities.ClientHttpRequest;
import utilities.json.JSONDecoder;
import java.util.List;
import javax.json.JsonArrayBuilder;
import javax.json.JsonWriter;
import utilities.json.JSONEncoder;

public class ApiClient {

    private String url = "";
    private String args = "";
    private HttpURLConnection con = null;
    private JsonObjectBuilder ob = null;
    private ClientHttpRequest mpartReq = null;
    private int readTimeOut;
    private int connectTimeOut;
    private int mode;
    private int method;
    private Object requestBody;

    public static final int JSON_REQUEST = 1;
    public static final int QUERY_STR = 2;
    public static final int MULTIPART_REQUEST = 3;

    public static final int GET = 1;
    public static final int POST = 2;
    public static final int PUT = 3;
    public static final int DELETE = 4;

    private static final int DEFAULT_READ_TIMEOUT = 99999;
    private static final int DEFAULT_CONNECT_TIMEOUT = 9999;

    public ApiClient(int method, String url, int connectTimeOut, int readTimeOut) throws IOException {
        this(method, url, QUERY_STR, connectTimeOut, readTimeOut);
    }

    public ApiClient(int method, String url) throws IOException {
        this(method, url, QUERY_STR, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    public ApiClient(int method, String url, int mode) throws IOException {
        this(method, url, mode, DEFAULT_CONNECT_TIMEOUT, DEFAULT_READ_TIMEOUT);
    }

    public ApiClient(int method, String url, int mode, int connectTimeOut, int readTimeOut) throws IOException {
        if (url.startsWith("http")) {
            this.url = url;
        } else {
            if (url.startsWith("/")) {
                url = url.substring(1);
            }
            if (!url.startsWith("api/")) {
                url = "api/" + url;
            }
            this.url = url;
        }
        this.readTimeOut = readTimeOut;
        this.connectTimeOut = connectTimeOut;
        this.mode = mode;
        this.method = method;
        if (mode == JSON_REQUEST) {
            ob = Json.createObjectBuilder();
        } else if (mode == MULTIPART_REQUEST) {
            createConnection();
            mpartReq = new ClientHttpRequest(con);
        }
    }

    public void setRequestBody(Object obj) {
        this.requestBody = obj;
    }

    private void createConnection() throws IOException {
        con = (HttpURLConnection) new URL(url + (mode == QUERY_STR ? args : "")).openConnection();

        switch (method) {
            case GET:
                con.setRequestMethod("GET");
                break;
            case POST:
                con.setRequestMethod("POST");
                break;
            case PUT:
                con.setRequestMethod("PUT");
                break;
            case DELETE:
                con.setRequestMethod("DELETE");
                break;
            default:
                throw new RuntimeException("Unrecognized method: " + method);
        }

        con.setReadTimeout(readTimeOut);
        con.setConnectTimeout(connectTimeOut);
        con.setRequestProperty("Content-Type", "application/json");
    }

    private void sendRequest() throws Exception {
        if (mode == MULTIPART_REQUEST && requestBody != null) {
            throw new RuntimeException("Can't use MULTIPART_REQUEST mode along with request body");
        }

        if (mode != MULTIPART_REQUEST) {
            createConnection();
        }

        if (mode == JSON_REQUEST && requestBody != null) {
            throw new RuntimeException("Can't use JSON REQUEST mode along with request body");
        } else if (mode != JSON_REQUEST && requestBody != null) {
            con.setDoOutput(true);
            JSONEncoder.encode(requestBody, con.getOutputStream(), false);
            con.getOutputStream().close();
        } else if (mode == JSON_REQUEST && requestBody == null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ByteArrayInputStream bais;
                JsonWriter w = Json.createWriter(baos);
                w.writeObject(ob.build());
                w.close();
                bais = new ByteArrayInputStream(baos.toByteArray());
                ClientHttpRequest.copy(bais, con.getOutputStream());
            } finally {
                baos.close();
            }
            con.getOutputStream().close();
        } else if (mode == MULTIPART_REQUEST) {
            mpartReq.post();
        } else if (mode != JSON_REQUEST && requestBody == null) {

        }

        if (con.getResponseCode() != 200) {
            if (con.getErrorStream() != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ClientHttpRequest.copy(con.getErrorStream(), baos);
                throw new IOException(new String(baos.toByteArray(), Charset.forName("UTF-8")));
            } else {
                if (con.getResponseMessage() != null && !con.getResponseMessage().isEmpty()) {
                    throw new IOException(con.getResponseMessage());
                }
                throw new IOException("Error desconocido");
            }
        }

    }

    public void setParam(String key, Object value) throws IOException {
        switch (mode) {
            case JSON_REQUEST:
                if (value == null) {
                    ob.addNull(key);
                } else if (value instanceof Integer) {
                    ob.add(key, (int) value);
                } else if (value instanceof BigInteger) {
                    ob.add(key, (BigInteger) value);
                } else if (value instanceof Double) {
                    ob.add(key, (double) value);
                } else if (value instanceof BigDecimal) {
                    ob.add(key, (BigDecimal) value);
                } else if (value instanceof Boolean) {
                    ob.add(key, (Boolean) value);
                } else if (value instanceof Long) {
                    ob.add(key, (Long) value);
                } else if (value instanceof String) {
                    ob.add(key, (String) value);
                } else if (value instanceof JsonArrayBuilder) {
                    ob.add(key, (JsonArrayBuilder) value);
                } else {
                    throw new RuntimeException("Unexpected type: " + value.getClass().getSimpleName());
                }
                break;
            case MULTIPART_REQUEST:
                if (value instanceof File) {
                    File file = (File) value;
                    mpartReq.setParameter(key, file.getName());
                    InputStream is = new BufferedInputStream(new FileInputStream(file));
                    mpartReq.setParameter(key, file.getName(), is, file.length());
                } else {
                    mpartReq.setParameter(key, value.toString());
                }
                break;
            default:
                args += ((args.isEmpty() ? "?" : "&") + key + "=" + (value == null ? "" : value));
                break;
        }
    }

    public <T> T getObject(Class<T> cs) throws Exception {
        return new JSONDecoder().getObject(getResponseStream(), cs);
    }

    public <T> List<T> getList(Class<T> cs) throws Exception {
        return new JSONDecoder().getList(getResponseStream(), cs);
    }

    /**
     * Cuando el API no envía un response body
     *
     * @throws Exception Cuando status != 200
     */
    public void getEmpty() throws Exception {
        sendRequest();
    }

    public InputStream getResponseStream() throws Exception {
        sendRequest();
        return con.getInputStream();
    }
}
