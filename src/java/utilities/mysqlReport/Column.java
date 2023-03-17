package utilities.mysqlReport;

public class Column {

    private String name;
    private int width;
    private int format;

    public Column() {
    }

    public Column(String name, int width, int format) {
        this.name = name;
        this.width = width;
        this.format = format;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }
}
