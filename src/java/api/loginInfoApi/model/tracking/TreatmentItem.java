package api.loginInfoApi.model.tracking;

import utilities.MySQLQuery;

public class TreatmentItem {

    public int id;
    public String name;
    public boolean initialClasify;
    public boolean onProcess;
    public boolean finalProduct;

    public TreatmentItem(Object[] row) {
        id = MySQLQuery.getAsInteger(row[0]);
        name = MySQLQuery.getAsString(row[1]);
        initialClasify = MySQLQuery.getAsBoolean(row[2]);
        onProcess = MySQLQuery.getAsBoolean(row[3]);
        finalProduct = MySQLQuery.getAsBoolean(row[4]);
    }
    
}
