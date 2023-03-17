package api.com.model;

import java.text.SimpleDateFormat;
import utilities.MySQLQuery;

public class PvSaleSummary {

    public Integer salePos;
    public String date;
    public String doc;
    public String storeName;
    public String bill;
    public boolean isCredit;
    public String amounts;

    public PvSaleSummary() {
    }

    public PvSaleSummary(Object[] row) {
        this.date = new SimpleDateFormat("dd/MM/yyyy hh:mm a").format(MySQLQuery.getAsDate(row[1]));
        this.doc = MySQLQuery.getAsString(row[2]);
        this.storeName = MySQLQuery.getAsString(row[3]);
        this.bill = MySQLQuery.getAsString(row[4]);
        this.isCredit = MySQLQuery.getAsBoolean(row[5]);
    }
}
