package model.maintenance.list.reports.history;

import java.math.BigDecimal;
import java.util.Date;

public class HistoryListItem {

    private String type;
    private String subArea;
    private Date begin;
    private BigDecimal amount;
    private String description;
    private String orderNum;
    private String provider;
    private String billNum;
    private BigDecimal value;
    private int mileageCur;
    private String chkOrderNum;

    public String getSubArea() {
        return subArea;
    }

    public void setSubArea(String subArea) {
        this.subArea = subArea;
    }

    public Date getBegin() {
        return begin;
    }

    public void setBegin(Date begin) {
        this.begin = begin;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(String orderNum) {
        this.orderNum = orderNum;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getBillNum() {
        return billNum;
    }

    public void setBillNum(String billNum) {
        this.billNum = billNum;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public int getMileageCur() {
        return mileageCur;
    }

    public void setMileageCur(int mileageCur) {
        this.mileageCur = mileageCur;
    }

    public String getChkOrderNum() {
        return chkOrderNum;
    }

    public void setChkOrderNum(String chkOrderNum) {
        this.chkOrderNum = chkOrderNum;
    }
        
}
