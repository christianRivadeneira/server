package utilities.reportHelper;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class NativeTable {

    private String title;
    private List<Column> columns = new ArrayList<Column>();
    private List<TableHeader> headers = new ArrayList<TableHeader>();
    private SummaryRow summaryRow;
    private byte[] bdata;
    private ByteArrayOutputStream dataBaos;
    private ZipOutputStream zos;
    private ObjectOutputStream dataOos;
    private int rows;
    private int cells;

    public NativeTable() {
    }

    public NativeTable(int rows) {
        this.rows = rows;
    }

    public NativeTable(NativeTable model, int rows) {
        this(rows);
        this.title = model.title;
        this.columns = model.columns;
        this.headers = model.headers;
        this.summaryRow = model.summaryRow;
    }

    public NativeTable(String title, int rows) {
        this(rows);
        this.title = title;        
    }

    public void begin() throws Exception {
        if (columns.isEmpty()) {
            throw new Exception("Debe agregar alguna columna.");
        }
        
        dataBaos = new ByteArrayOutputStream();
        zos = new ZipOutputStream(dataBaos);
        zos.putNextEntry(new ZipEntry("data"));
        dataOos = new ObjectOutputStream(zos);
    }

    public void end() throws Exception {
        if (rows == 0) {
            throw new Exception("El número de filas debe ser mayor que 0.");
        }

        if (rows * columns.size() != cells) {
            throw new Exception("El número de celdas agregadas no es correcto.");
        }
        dataOos.flush();
        zos.closeEntry();
        zos.close();
        bdata = dataBaos.toByteArray();
        dataOos.close();
    }

    public void addCell(Object o) throws Exception {
        dataOos.writeObject(o);
        cells++;
    }

    public void addCells(Object[] obs) throws Exception {
        for (int i = 0; i < obs.length; i++) {
            dataOos.writeObject(obs[i]);
        }
        cells += obs.length;
    }

    public void addCells(Object[] obs, int beginIndex, int endIndex) throws Exception {
        for (int i = beginIndex; i <= endIndex; i++) {
            dataOos.writeObject(obs[i]);
        }
        cells += (endIndex - beginIndex + 1);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }

    public SummaryRow getSummaryRow() {
        return summaryRow;
    }

    public void setSummaryRow(SummaryRow summaryRow) {
        this.summaryRow = summaryRow;
    }

    public List<TableHeader> getHeaders() {
        return headers;
    }

    public void setHeaders(List<TableHeader> headers) {
        this.headers = headers;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public byte[] getBdata() {
        return bdata;
    }

    public void setBdata(byte[] bdata) {
        this.bdata = bdata;
    }

    public boolean isEmpty() {
        return rows <= 0;
    }
}
