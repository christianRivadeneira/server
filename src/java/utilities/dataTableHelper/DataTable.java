package utilities.dataTableHelper;

import java.util.ArrayList;

@Deprecated
public class DataTable {
    private ArrayList<DataColumn> columns = new ArrayList<DataColumn>();
    private ArrayList<DataRow> data = new ArrayList<DataRow>();
    private Integer sortAscBy;
    private Integer sortDescBy;

    public DataTable(){};
    
    public ArrayList<DataColumn> getColumns() {
        return columns;
    }

    public void setColumns(ArrayList<DataColumn> columns) {
        this.columns = columns;
    }

    public ArrayList<DataRow> getData() {
        return data;
    }

    public void setData(ArrayList<DataRow> data) {
        this.data = data;
    }

    public Integer getSortAscBy() {
        return sortAscBy;
    }

    public void setSortAscBy(Integer sortAscBy) {
        this.sortDescBy = null;
        this.sortAscBy = sortAscBy;
    }

    public Integer getSortDescBy() {
        return sortDescBy;
    }

    public void setSortDescBy(Integer sortDescBy) {
        this.sortAscBy = null;
        this.sortDescBy = sortDescBy;
    }
}
