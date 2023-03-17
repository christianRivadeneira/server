package model.indicator.grid;

import model.indicator.IndIndicator;
import model.indicator.IndScale;
import model.indicator.IndType;

public class GridIndicator {

    private IndScale scale;
    private IndIndicator indicator;
    private IndType type;

    public GridIndicator() {
    }

    public GridIndicator(Object[] row) {
        if (row.length == 3) {
            scale = (IndScale) row[0];
            indicator = (IndIndicator) row[1];
            type = (IndType) row[2];
        } else if (row.length == 2) {
            indicator = (IndIndicator) row[0];
            type = (IndType) row[1];
        }
    }

    public IndScale getScale() {
        return scale;
    }

    public void setScale(IndScale scale) {
        this.scale = scale;
    }

    public IndIndicator getIndicator() {
        return indicator;
    }

    public void setIndicator(IndIndicator indicator) {
        this.indicator = indicator;
    }

    public IndType getType() {
        return type;
    }

    public void setType(IndType type) {
        this.type = type;
    }
}
