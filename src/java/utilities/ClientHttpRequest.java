package utilities;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import javax.swing.JProgressBar;

public class ClientHttpRequest {

    private final HttpURLConnection connection;
    private OutputStream os = null;

    public ClientHttpRequest(String url) throws IOException {
        this(new URL(url).openConnection());
    }

    public ClientHttpRequest(URLConnection conn) throws IOException {
        this.connection = (HttpURLConnection) conn;
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
    }

    protected void connect() throws IOException {
        if (os == null) {
            os = connection.getOutputStream();
        }
    }

    protected void write(char c) throws IOException {
        connect();
        os.write(c);
    }

    protected void write(String s) throws IOException {
        connect();
        if (s != null && (!s.isEmpty())) {
            os.write(s.getBytes("UTF-8"));
        }
    }

    protected void newline() throws IOException {
        connect();
        write("\r\n");
    }

    protected void writeln(String s) throws IOException {
        connect();
        write(s);
        newline();
    }
    private final static Random random = new Random();

    protected static String randomString() {
        return Long.toString(random.nextLong(), 36);
    }
    String boundary = "---------------------------" + randomString() + randomString() + randomString();

    private void boundary() throws IOException {
        write("--");
        write(boundary);
    }

    /**
     * Creates a new multipart POST HTTP request on a freshly opened
     * URLConnection
     *
     * @param connection an already open URL connection
     * @throws IOException
     */
    private void writeName(String name) throws IOException {
        newline();
        write("Content-Disposition: form-data; name=\"");
        write(name);
        write('"');
    }

    /**
     * adds a string parameter to the request
     *
     * @param name parameter name
     * @param value parameter value
     * @throws IOException
     */
    public void setParameter(String name, String value) throws IOException {
        boundary();
        writeName(name);
        newline();
        newline();
        writeln(value);
    }

    private void pipe(InputStream in, OutputStream out) throws IOException {
        this.pipe(in, out, 0, null);
    }

    private void pipe(InputStream in, OutputStream out, long len, JProgressBar bar) throws IOException {
        copy(in, out, (int) len, bar);
    }

    /**
     * adds a file parameter to the request
     *
     * @param name parameter name
     * @param filename the name of the file
     * @param is input stream to read the contents of the file from
     * @throws IOException
     */
    public void setParameter(String name, String filename, InputStream is) throws IOException {
        this.setParameter(name, filename, is, 0, null);
    }

    public void setParameter(String name, String filename, InputStream is, long len) throws IOException {
        setParameter(name, filename, is, len, null);
    }

    public void setParameter(String name, String filename, InputStream is, long len, JProgressBar bar) throws IOException {
        boundary();
        writeName(name);
        write("; filename=\"");
        write(filename);
        write('"');
        newline();
        write("Content-Type: ");
        String type = URLConnection.guessContentTypeFromName(filename);
        if (type == null) {
            type = "application/octet-stream";
        }
        writeln(type);
        newline();
        if (bar != null) {
            pipe(is, os, len, bar);
        } else {
            pipe(is, os);
        }
        newline();
    }

    /**
     * posts the requests to the server, with all the cookies and parameters
     * that were added
     *
     * @return input stream with the server response
     * @throws IOException
     */
    public InputStream post() throws IOException {
        boundary();
        writeln("--");
        os.close();
        try {
            String enc = connection.getHeaderField("Content-Encoding");
            if (enc != null && enc.equals("gzip")) {
                return new GZIPInputStream(connection.getInputStream());
            } else {
                return connection.getInputStream();
            }
        } catch (IOException ex) {
            if (connection.getResponseMessage() != null && !connection.getResponseMessage().isEmpty()) {
                throw new IOException(connection.getResponseMessage());
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if (connection.getErrorStream() != null) {
                copy(connection.getErrorStream(), baos);
                throw new IOException(new String(baos.toByteArray(), Charset.forName("UTF-8")));
            } else {
                throw ex;
            }
        }
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        return ClientHttpRequest.copy(input, output, 0, null);
    }

    public static int copy(InputStream input, OutputStream output, int len, JProgressBar bar) throws IOException {
        byte[] buffer = new byte[128 * 1024];
        int count = 0;
        int n = 0;
        if (bar != null && len > 0) {
            boolean origIndeterm = bar.isIndeterminate();
            bar.setIndeterminate(false);
            bar.setMaximum(len);
            bar.setValue(0);
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
                count += n;
                bar.setValue(count);
            }
            bar.setValue(0);
            bar.setIndeterminate(origIndeterm);
        } else {
            while (-1 != (n = input.read(buffer))) {
                output.write(buffer, 0, n);
                count += n;
            }
        }
        return count;
    }

    public static String convertStreamToString(InputStream is) throws IOException {
        if (is != null) {
            Writer writer = new StringWriter();
            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }

    public void setConnectTimeout(int connectTimeOut) {
        connection.setConnectTimeout(connectTimeOut);
    }

    public void setReadTimeout(int readTimeOut) {
        connection.setReadTimeout(readTimeOut);
    }

    public void close() {
        connection.disconnect();
    }
}
