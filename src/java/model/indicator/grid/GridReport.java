package model.indicator.grid;

import java.util.ArrayList;
import model.indicator.IndSpan;

public class GridReport {

    private IndSpan span;
    private ArrayList<GridArea> areas;
    private ArrayList<GridRow> rows;

    public GridReport() {
    }

    public GridReport(IndSpan span) {
        areas = new ArrayList<GridArea>();
        rows = new ArrayList<GridRow>();
    }

    public IndSpan getSpan() {
        return span;
    }

    public void setSpan(IndSpan span) {
        this.span = span;
    }

    public ArrayList<GridArea> getAreas() {
        return areas;
    }

    public void setAreas(ArrayList<GridArea> areas) {
        this.areas = areas;
    }

    public ArrayList<GridRow> getRows() {
        return rows;
    }

    public void setRows(ArrayList<GridRow> rows) {
        this.rows = rows;
    }
}
