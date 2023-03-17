package model.maintenance.list;

import java.math.BigDecimal;
import java.util.List;

public class FuelCostListItem {

    public int fuelTypeId;
    public String fuelTypeName;
    public String fuelTypeCode;
    public List<BigDecimal> values;

    public FuelCostListItem() {
    }

    public void setFuelType(int fuelTypeId, String fuelTypeName) {
        this.fuelTypeId = fuelTypeId;
        this.fuelTypeName = fuelTypeName;
    }
}
