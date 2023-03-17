package model.maintenance.list.reports;

import java.math.BigDecimal;
import java.util.ArrayList;
import model.maintenance.IndicatorPar;

public class IndicatorListItem {
    public IndicatorPar indicator;
    public ArrayList<BigDecimal> upper = new ArrayList(12);
    public ArrayList<BigDecimal> lower = new ArrayList(12);

    public IndicatorListItem(){
        
    }

    public IndicatorListItem(IndicatorPar indicator){
        this.indicator = indicator;
    }  
}
