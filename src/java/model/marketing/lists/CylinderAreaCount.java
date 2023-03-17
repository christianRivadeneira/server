package model.marketing.lists;

import java.util.ArrayList;

public class CylinderAreaCount {
    private Integer idArea;
    private String name;
    private ArrayList<CylinderTypeCount> counters = new ArrayList<CylinderTypeCount>();

    public Integer getIdArea() {
        return idArea;
    }

    public void setIdArea(Integer idArea) {
        this.idArea = idArea;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<CylinderTypeCount> getCounters() {
        return counters;
    }

    public void setCounters(ArrayList<CylinderTypeCount> counters) {
        this.counters = counters;
    }

}
