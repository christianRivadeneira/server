package utilities.reportHelper;


public class HeaderColumn {
private String colName = null;
    private int colSpan = 1;
    private int rowSpan = 1;

    public HeaderColumn(){

    }

    public HeaderColumn(int colSpan, int rowSpan) {
        this.colName = null;
        this.colSpan = colSpan;
        this.rowSpan = rowSpan;
    }

    public HeaderColumn(String colName, int colSpan, int rowSpan) {
        this.colName = colName;
        this.colSpan = colSpan;
        this.rowSpan = rowSpan;
    }

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }

    public int getColSpan() {
        return colSpan;
    }

    public void setColSpan(int colSpan) {
        this.colSpan = colSpan;
    }

    public int getRowSpan() {
        return rowSpan;
    }

    public void setRowSpan(int rowSpan) {
        this.rowSpan = rowSpan;
    }
}
