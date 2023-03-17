package utilities.reportHelper;

import java.util.ArrayList;
import java.util.List;

public class Table {
    private String title;
    private List<Column> columns = new ArrayList<Column>();
    private List<TableHeader> headers = new ArrayList<TableHeader>();
    private List<Object[]> data = new ArrayList<Object[]>();
    private SummaryRow summaryRow;

    public Table(){

    }

    public Table(Table model){
        this.title = model.title;
        this.columns = model.columns;
        this.headers = model.headers;
        this.summaryRow = model.summaryRow;
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

    public List<Object[]> getData() {
        return data;
    }

    public void setData(List<Object[]> data) {
        this.data = data;
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
}
