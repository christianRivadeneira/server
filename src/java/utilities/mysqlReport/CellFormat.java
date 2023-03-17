package utilities.mysqlReport;

public class CellFormat {

    private String format;
    private int align;
    private int type;
    private boolean wrap = false;

    public CellFormat() {
    }

    public CellFormat(int type, int align) {
        this(type, align, null, false);
    }

    public CellFormat(int type, int align, String format) {
        this(type, align, format, false);
    }

    public CellFormat(int type, int align, boolean wrap) {
        this(type, align, null, wrap);
    }

    public CellFormat(int type, int align, String format, boolean wrap) {
        this.type = type;
        this.align = align;
        this.format = format;
        this.wrap = wrap;
        if (type == MySQLReportWriter.ENUM && format == null) {
            throw new RuntimeException("El tipo ENUM requiere formato");
        }
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public int getAlign() {
        return align;
    }

    public void setAlign(int align) {
        this.align = align;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isWrap() {
        return wrap;
    }

    public void setWrap(boolean wrap) {
        this.wrap = wrap;
    }
}
