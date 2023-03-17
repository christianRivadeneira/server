package model.marketing.lists;

import java.util.ArrayList;

public class CylinderTypeCount {

    private Integer cilinderType;
    private String typeName;
    private ArrayList<Integer> count = new ArrayList<>();
    private ArrayList<Integer> months = new ArrayList<>();

    public Integer getCilinderType() {
        return cilinderType;
    }

    public void setCilinderType(Integer cilinderType) {
        this.cilinderType = cilinderType;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public ArrayList<Integer> getCount() {
        return count;
    }

    public void setCount(ArrayList<Integer> count) {
        this.count = count;
    }

    public ArrayList<Integer> getMonths() {
        return months;
    }

    public void setMonths(ArrayList<Integer> months) {
        this.months = months;
    }
}
