package utilities.mysqlReport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Table {

    private String title;
    private List<Column> columns = new ArrayList<>();
    private List<TableHeader> headers = new ArrayList<>();
    private SummaryRow summaryRow;
    private Object[][] data;
    private boolean rotateTitleCols = false;

    public Table(Table model) {

        this.title = model.title;
        this.columns = model.columns;
        this.headers = model.headers;
        this.summaryRow = model.summaryRow;
        this.rotateTitleCols = model.rotateTitleCols;
    }

    public Table(String title) {

        this.title = title;
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

    public Object[][] getData() {
        return data;
    }

    public void setData(List<Object[]> data) {
        this.data = data.toArray(new Object[data.size()][]);
    }
    
    public void setData(Object[][] data) {
        this.data = data;
    }

    public boolean isEmpty() {
        return getData() == null || getData().length == 0;
    }

    Map<String, Integer> coloredCells = new HashMap<String, Integer>();

    public void setColor(int row, int col, int Color) {
        coloredCells.put(row + "-" + col, Color);
    }

    public void setRowColor(int row, int color) {
        for (int i = 0; i < columns.size(); i++) {
            coloredCells.put(row + "-" + i, color);
        }
    }

    public boolean getRotateTitleCols() {
        return rotateTitleCols;
    }

    public void setRotateTitleCols(boolean rotateTitleCols) {
        this.rotateTitleCols = rotateTitleCols;
    }

    public void addRow(Object[] row) {
        Object[][] origData = this.getData();
        Object[][] newData = new Object[origData != null ? origData.length + 1 : 1][];
        if (origData != null) {
            System.arraycopy(origData, 0, newData, 0, origData.length);
        }
        newData[newData.length - 1] = row;
        this.setData(newData);
    }
}
