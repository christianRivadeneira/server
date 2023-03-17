package utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.WritableWorkbook;

public class Reports {

    public static String getPatternXml(String name, Object caller) throws Exception {
        return readInputStreamAsString(caller.getClass().getResourceAsStream(String.format("/reports/%s.xml", name)));
    }

    public static WritableWorkbook getModernBaseAsStream(File path, int sheets) throws Exception {
        return getBaseAsStream(path, "/utilities/mysqlReport/modern_base.xls", sheets);
    }

    public static WritableWorkbook getBaseAsStream(File path, int sheets) throws Exception {
        return getBaseAsStream(path, "/utilities/mysqlReport/base.xls", sheets);
    }

    public static WritableWorkbook getWorkbook(File f, Class caller, String basePath) throws Exception {
        WorkbookSettings in = new WorkbookSettings();
        in.setLocale(new Locale("es"));
        in.setEncoding("windows-1252");
        Workbook template = Workbook.getWorkbook(caller.getResourceAsStream(basePath), in);

        WorkbookSettings out = new WorkbookSettings();
        out.setLocale(new Locale("es"));
        out.setEncoding("windows-1252");
        out.setWriteAccess("sigma");
        WritableWorkbook workbook = Workbook.createWorkbook(f, template, out);
        return workbook;
    }

    private static WritableWorkbook getBaseAsStream(File path, String basePath, int sheets) throws Exception {
        WorkbookSettings ws = new WorkbookSettings();
        ws.setLocale(new Locale("es"));
        ws.setEncoding("windows-1252");
        Workbook base = Workbook.getWorkbook(Reports.class.getResourceAsStream(basePath), ws);
        WritableWorkbook workbook = Workbook.createWorkbook(path, base);
        if (sheets == 0) {
            throw new Exception("No se hallaron datos.");
        }
        for (int i = 1; i < sheets; i++) {
            workbook.copySheet(0, "Hoja " + (i + 1), i);
        }
        return workbook;
    }

    public static InputStream getGridAsStream(Class caller) {
        return caller.getResourceAsStream("/reports/indicators/base.xls");
    }

    public static InputStream getPatternStream(String name, Object caller) throws Exception {
        return caller.getClass().getResourceAsStream(name);
    }

    public static File createReportFile(String name, String ext) throws Exception {
        String date = Dates.getBackupFormat().format(new Date());
        return File.createTempFile(name + "_" + date, "." + ext);
    }

    public static String readInputStreamAsString(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[1024 * 4];
        InputStreamReader bin = new InputStreamReader(in, StandardCharsets.UTF_8);
        int n = 0;
        while (-1 != (n = bin.read(buffer))) {
            sb.append(buffer, 0, n);
        }
        return sb.toString();
    }

    public static byte[] readInputStreamAsBytes(InputStream in) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(in);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = bis.read();
        while (result != -1) {
            byte b = (byte) result;
            buf.write(b);
            result = bis.read();
        }
        in.close();
        return buf.toByteArray();
    }

    public static void setContents(File aFile, String aContents) throws FileNotFoundException, IOException {
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(aFile), "UTF8");
        Writer output = new BufferedWriter(osw);
        try {
            output.write(aContents);
        } finally {
            output.close();
        }
    }

    public static byte[] getFileBytes(File f) throws Exception {
        int BUFFER = 10485760;
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int count;
        byte[] cbuf = new byte[BUFFER];
        while ((count = bis.read(cbuf, 0, BUFFER)) != -1) {
            baos.write(cbuf, 0, count);
        }
        bis.close();
        baos.close();
        return baos.toByteArray();
    }

    public static String getColName(int colIndex) throws Exception {
        if (colIndex > 256) {
            //   throw new Exception("El reporte excede la capacidad del formato xls.");
        }
        String[] colNames = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
        int block = (int) Math.floor(colIndex / (double) colNames.length);
        if (block == 0) {
            return colNames[colIndex];
        } else {
            return colNames[block - 1] + colNames[colIndex - (block * colNames.length)];
        }
    }

    /**
     * Copy y cierra los flujos
     *
     * @param input
     * @param output
     * @return
     * @throws IOException
     */
    public static int copy(InputStream input, OutputStream output) throws IOException {
        return copy(input, output, false, false);
    }

    public static int copy(InputStream input, OutputStream output, boolean closeInput, boolean closeOutput) throws IOException {
        byte[] buffer = new byte[4096];
        int count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        if (closeInput) {
            try {
                input.close();
            } catch (Exception ex) {
            }
        }
        if (closeOutput) {
            try {
                output.close();
            } catch (Exception ex) {
            }
        }
        return count;
    }

    public static void copy(File fromFile, File toFile) throws Exception {
        FileInputStream from = null;
        FileOutputStream to = null;
        try {
            from = new FileInputStream(fromFile);
            to = new FileOutputStream(toFile);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = from.read(buffer)) != -1) {
                to.write(buffer, 0, bytesRead); // write
            }
        } finally {
            if (from != null) {
                try {
                    from.close();
                } catch (IOException e) {
                }
            }
            if (to != null) {
                try {
                    to.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public static void downloadFile(String path, URL address) throws Exception {
        HttpURLConnection con = (HttpURLConnection) address.openConnection();
        String content = con.getHeaderField("Content-Disposition");
        String fileName = content.substring(content.indexOf("=") + 1, content.length());
        path += (path.endsWith(File.separator) ? fileName : (File.separator + fileName));
        FileOutputStream fos = new FileOutputStream(path);
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        copy(con.getInputStream(), bos);
        bos.close();
        fos.close();
        con.getInputStream().close();
    }
}
