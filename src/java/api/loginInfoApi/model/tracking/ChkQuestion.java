package api.loginInfoApi.model.tracking;

import utilities.MySQLQuery;

public class ChkQuestion {
    
    public int questId;
    public String questText;
    public boolean checked;
    public boolean initialClasify;
    public boolean onProcess;
    public boolean finalProduct;
    
       public ChkQuestion(Object[] obj) {
        questId = MySQLQuery.getAsInteger(obj[0]);
        questText = MySQLQuery.getAsString(obj[1]);
        initialClasify = MySQLQuery.getAsBoolean(obj[2]);
        onProcess = MySQLQuery.getAsBoolean(obj[3]);
        finalProduct = MySQLQuery.getAsBoolean(obj[4]);
        checked = false;
    }
    
}
