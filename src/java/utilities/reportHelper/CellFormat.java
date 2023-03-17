package utilities.reportHelper;

public class CellFormat {

    private String format;
    private int align;
    private int type;
    private boolean wrap = false;

    public static int LABEL = 1;
    public static int NUMBER = 2;
    public static int DATE = 3;

    public static int LEFT = 4;
    public static int RIGHT = 5;
    public static int CENTER = 6;
    
    public CellFormat() {
    }

    public CellFormat(int type, int align) {
        this.format = null;
        this.align = align;
        this.type = type;
    }

    public CellFormat(int type, int align, String format) {
        this.format = format;
        this.align = align;
        this.type = type;
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
