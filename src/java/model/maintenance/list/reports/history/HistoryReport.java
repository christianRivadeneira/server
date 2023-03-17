package model.maintenance.list.reports.history;

import java.util.ArrayList;
import java.util.List;

public class HistoryReport {
    private List<HistoryArea> areas;

    public HistoryReport(){
        areas = new ArrayList<HistoryArea>();
    }

    public List<HistoryArea> getAreas() {
        return areas;
    }

    public void setAreas(List<HistoryArea> areas) {
        this.areas = areas;
    }
}
