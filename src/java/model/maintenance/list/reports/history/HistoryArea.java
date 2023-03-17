package model.maintenance.list.reports.history;

import java.util.ArrayList;
import java.util.List;


public class HistoryArea {
    private int areaId;
    private String areaName;
    private String areaCode;
    private String areaType;
    private List<HistoryListItem> items;

    public HistoryArea(){
        items = new ArrayList<HistoryListItem>();
    }

    public void setArea(int areaId, String areaName, String areaCode, String areaType) {
        this.areaId = areaId;
        this.areaName = areaName;
        this.areaCode = areaCode;
        this.setAreaType(areaType);
    }
    
    public List<HistoryListItem> getItems() {
        return items;
    }

    public void setItems(List<HistoryListItem> items) {
        this.items = items;
    }

    public int getAreaId() {
        return areaId;
    }

    public void setAreaId(int areaId) {
        this.areaId = areaId;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    /**
     * @return the areaType
     */
    public String getAreaType() {
        return areaType;
    }

    /**
     * @param areaType the areaType to set
     */
    public void setAreaType(String areaType) {
        this.areaType = areaType;
    }
}
