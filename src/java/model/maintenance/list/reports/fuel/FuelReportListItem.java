package model.maintenance.list.reports.fuel;

import java.math.BigDecimal;

public class FuelReportListItem {

    private String Internal;
    private String Clase;
    private String Type;
    private String Plate;
    private String City;
    private String Enterprise;
    private BigDecimal kBegin;
    private BigDecimal kEnd;
    private BigDecimal mileage;
    private BigDecimal Amount;
    private BigDecimal Cost;
    private BigDecimal expected;

    public String getInternal() {
        return Internal;
    }

    public void setInternal(String Internal) {
        this.Internal = Internal;
    }

    public String getClase() {
        return Clase;
    }

    public void setClase(String Clase) {
        this.Clase = Clase;
    }

    public String getType() {
        return Type;
    }

    public void setType(String Type) {
        this.Type = Type;
    }

    public String getPlate() {
        return Plate;
    }

    public void setPlate(String Plate) {
        this.Plate = Plate;
    }

    public String getCity() {
        return City;
    }

    public void setCity(String City) {
        this.City = City;
    }

    public String getEnterprise() {
        return Enterprise;
    }

    public void setEnterprise(String Enterprise) {
        this.Enterprise = Enterprise;
    }

    public BigDecimal getkBegin() {
        return kBegin;
    }

    public void setkBegin(BigDecimal kBegin) {
        this.kBegin = kBegin;
    }

    public BigDecimal getkEnd() {
        return kEnd;
    }

    public void setkEnd(BigDecimal kEnd) {
        this.kEnd = kEnd;
    }

    public BigDecimal getAmount() {
        return Amount;
    }

    public void setAmount(BigDecimal Amount) {
        this.Amount = Amount;
    }

    public BigDecimal getCost() {
        return Cost;
    }

    public void setCost(BigDecimal Cost) {
        this.Cost = Cost;
    }

    public BigDecimal getMileage() {
        return mileage;
    }

    public void setMileage(BigDecimal mileage) {
        this.mileage = mileage;
    }

    public BigDecimal getExpected() {
        return expected;
    }

    public void setExpected(BigDecimal expected) {
        this.expected = expected;
    }
}
