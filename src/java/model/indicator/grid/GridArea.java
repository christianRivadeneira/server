package model.indicator.grid;

import java.util.ArrayList;
import model.indicator.IndArea;

public class GridArea {
    private IndArea area;
    private ArrayList<GridSubArea> subAreas;
    private int indCount = 0;

    public GridArea(){

    }

    public GridArea(IndArea area){
        this.area = area;
        subAreas = new ArrayList<GridSubArea>();
    }

    public void incCount(int amount){
        indCount+=amount;
    }

    public IndArea getArea() {
        return area;
    }
    
    public void setArea(IndArea area) {
        this.area = area;
    }

    public ArrayList<GridSubArea> getSubAreas() {
        return subAreas;
    }

    public void setSubAreas(ArrayList<GridSubArea> subAreas) {
        this.subAreas = subAreas;
    }

    public int getIndCount() {
        return indCount;
    }

    public void setIndCount(int indCount) {
        this.indCount = indCount;
    }
}
