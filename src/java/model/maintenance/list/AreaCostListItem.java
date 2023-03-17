package model.maintenance.list;

import java.math.BigDecimal;
import java.util.List;

public class AreaCostListItem {

    public String areaPUC;
    public String areaName;
    public int areaId;
    public List<BigDecimal> values;

    public void setArea(int id, String name, String puc) {
        this.areaId = (id);
        this.areaName = (name);
        this.areaPUC = (puc);
    }
}
