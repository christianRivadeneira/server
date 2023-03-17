package model.indicator.list;

import model.indicator.IndIndicator;


public class IndicatorList {

    private IndIndicator indicator;
    private String indicatorType;

    public String getIndicatorType() {
        return indicatorType;
    }

    public void setIndicatorType(String indicatorType) {
        this.indicatorType = indicatorType;
    }

    public IndIndicator getIndicator() {
        return indicator;
    }

    public void setIndicator(IndIndicator indicator) {
        this.indicator = indicator;
    }
}
