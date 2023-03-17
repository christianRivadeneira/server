package api.tanks.model;

import utilities.MySQLQuery;

public class ZeroSale {

    public String document;
    public String name;
    public String address;
    public String phones;

    public ZeroSale(Object[] row) {
        document = MySQLQuery.getAsString(row[0]);
        name = MySQLQuery.getAsString(row[1]);
        address = MySQLQuery.getAsString(row[2]);
        phones = MySQLQuery.getAsString(row[3]);
    }

}
