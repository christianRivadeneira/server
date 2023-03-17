package api.com.model;

import java.text.SimpleDateFormat;
import utilities.MySQLQuery;

public class PvOrderState {

    public String orderDate;
    public String progDate;
    public String pvSalesman;
    public String vehicle;
    public String inventory;
    public String state;

    public PvOrderState() {
    }

    public PvOrderState(Object[] row) {
        this.orderDate = new SimpleDateFormat("dd/MM/yyyy hh:mm a").format(MySQLQuery.getAsDate(row[0]));
        this.progDate = new SimpleDateFormat("dd/MM/yyyy").format(MySQLQuery.getAsDate(row[1]));
        this.pvSalesman = MySQLQuery.getAsString(row[2]);
        this.vehicle = MySQLQuery.getAsString(row[3]);
        this.inventory = MySQLQuery.getAsString(row[4]);
        this.state = MySQLQuery.getAsString(row[5]);
    }
}
