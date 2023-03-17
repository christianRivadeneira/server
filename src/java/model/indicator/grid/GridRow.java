package model.indicator.grid;

import java.util.ArrayList;

public class GridRow {
    private String cityName;
    private String employeeName;
    private ArrayList<GridResultGroup> groups;
    private int agencyId;

    public GridRow(){
        
    }

    public GridRow(String cityName, String employeeName){
        this.cityName = cityName;
        this.employeeName = employeeName;
        this.groups = new ArrayList<GridResultGroup>();
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public ArrayList<GridResultGroup> getGroups() {
        return groups;
    }

    public void setGroups(ArrayList<GridResultGroup> groups) {
        this.groups = groups;
    }

    public int getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(int agencyId) {
        this.agencyId = agencyId;
    }
}
