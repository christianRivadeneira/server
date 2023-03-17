package model.indicator.grid;

import java.math.BigDecimal;
import java.util.ArrayList;
import model.indicator.IndArea;

public class GridSubArea {

    private IndArea subArea;
    private int indCount = 0;
    private ArrayList<GridIndicator> indicators;
    private BigDecimal score;

    public GridSubArea() {
    }

    public GridSubArea(IndArea subArea, GridArea parentArea) {
        this.subArea = subArea;
        indicators = new ArrayList<GridIndicator>();
    }

    public void incCount(int amount) {
        indCount += amount;
    }

    public IndArea getSubArea() {
        return subArea;
    }

    public void setSubArea(IndArea subArea) {
        this.subArea = subArea;
    }

    public int getIndCount() {
        return indCount;
    }

    public void setIndCount(int indCount) {
        this.indCount = indCount;
    }

    public ArrayList<GridIndicator> getIndicators() {
        return indicators;
    }

    public void setIndicators(ArrayList<GridIndicator> indicators) {
        this.indicators = indicators;
    }

    public BigDecimal getScore() {
        return score;
    }
    
    public void setScore(BigDecimal score) {
        this.score = score;
    }

    
}
