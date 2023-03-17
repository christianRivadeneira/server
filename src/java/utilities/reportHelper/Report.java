package utilities.reportHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Report {

    private String title;
    private List<String> subTitles = new ArrayList<>();
    private String sheetName;
    private int rowHeigth;
    private List<Table> tables = new ArrayList<>();
    private List<NativeTable> nativeTables = new ArrayList<>();
    private List<CellFormat> formats = new ArrayList<>();
    private Date creation;
    private int horizontalFreeze = 0;
    private int verticalFreeze = 0;
    private int zoomFactor = 90;
    private long begTime;
    private long endTime;

    public Report() {
    }

    public Report(String title, String subtitle, String sheetName, int rowHeight) {
        this.title = title;
        begTime = System.currentTimeMillis();
        if (subtitle != null) {
            if (!subtitle.isEmpty()) {
                this.subTitles.add(subtitle);
            }
        }
        this.sheetName = sheetName;
        this.rowHeigth = rowHeight;
        this.creation = new Date();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public int getRowHeigth() {
        return rowHeigth;
    }

    public void setRowHeigth(int rowHeigth) {
        this.rowHeigth = rowHeigth;
    }

    public List<Table> getTables() {
        setEndTime(System.currentTimeMillis());
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public List<NativeTable> getNativeTables() {
        setEndTime(System.currentTimeMillis());
        return nativeTables;
    }

    public void setNativeTables(List<NativeTable> nativeTables) {
        this.nativeTables = nativeTables;
    }

    public List<CellFormat> getFormats() {
        return formats;
    }

    public void setFormats(List<CellFormat> formats) {
        this.formats = formats;
    }

    public List<String> getSubTitles() {
        return subTitles;
    }

    public void setSubTitles(List<String> subTitles) {
        this.subTitles = subTitles;
    }

    public Date getCreation() {
        return creation;
    }

    public void setCreation(Date creation) {
        this.creation = creation;
    }

    public int getHorizontalFreeze() {
        return horizontalFreeze;
    }

    public void setHorizontalFreeze(int horizontalFreeze) {
        this.horizontalFreeze = horizontalFreeze;
    }

    public int getVerticalFreeze() {
        return verticalFreeze;
    }

    public void setVerticalFreeze(int verticalFreeze) {
        this.verticalFreeze = verticalFreeze;
    }

    public int getZoomFactor() {
        return zoomFactor;
    }

    public void setZoomFactor(int zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

    public long getBegTime() {
        return begTime;
    }

    public void setBegTime(long begTime) {
        this.begTime = begTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }
}
