package controller.system;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import utilities.DBSettings;

public class BackupController {

    public static byte[] executeSQLFile(Connection conn, File f, String dbPar) throws Exception {
        int BUFFER = 10485760;
        DBSettings db = new DBSettings(conn);
        Process run = Runtime.getRuntime().exec(String.format("mysql -u%s -p%s -h%s -P%s %s", db.user, db.pass, db.host, db.port, (dbPar == null ? db.db : dbPar)));
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(run.getOutputStream())); BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)))) {
            int count;
            char[] cbuf = new char[BUFFER];
            while ((count = br.read(cbuf, 0, BUFFER)) != -1) {
                bw.write(cbuf, 0, count);
            }
        }
        run.getOutputStream().close();
        byte[] rtaData = toByteArray(run.getInputStream());
        run.getInputStream().close();
        run.waitFor();
        if (run.exitValue() != 0) {
            throw new Exception("El proceso terminó con código " + run.exitValue() + "\n" + getString(run.getErrorStream()));
        } else {
            return rtaData;
        }
    }
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    public static String toString(byte[] input, String encoding) throws IOException {
        StringWriter sw = new StringWriter();
        copy(input, sw, encoding);
        return sw.toString();
    }

    private static String getString(InputStream is) throws Exception {
        return toString(toByteArray(is), "UTF-8");
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    private static void copy(byte[] input, Writer output, String encoding) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(input);
        copy(in, output, encoding);
    }

    private static void copy(InputStream input, Writer output, String encoding) throws IOException {
        InputStreamReader in = new InputStreamReader(input, encoding);
        copy(in, output);
    }

    private static int copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    private static int copy(Reader input, Writer output) throws IOException {
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        int count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}
