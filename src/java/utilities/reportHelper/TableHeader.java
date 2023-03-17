package utilities.reportHelper;

import java.util.ArrayList;

public class TableHeader {

    private ArrayList<HeaderColumn> colums = new ArrayList<HeaderColumn>();
    
    public ArrayList<HeaderColumn> getColums() {
        return colums;
    }

    public void setColums(ArrayList<HeaderColumn> colums) {
        this.colums = colums;
    }
}
