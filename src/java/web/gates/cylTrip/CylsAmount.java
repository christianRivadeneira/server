package web.gates.cylTrip;

import java.util.ArrayList;
import java.util.List;
import utilities.MySQLQuery;

public class CylsAmount {

    public static final int OPER_UPDATE = 1;
    public static final int OPER_CREATE = 2;
    public static final int OPER_DELETE = 3;
    public static final int OPER_NONE = 0;

    public int capa;
    public int typeId;//Marca, Universal, Ecuatoriano
    public int amount;
    public String state;//Llenos, Vacíos, Fugas
    public String name;
    public int operation;

    public CylsAmount() {
    }

    public CylsAmount(Object[] row) {
        this.amount = MySQLQuery.getAsInteger(row[0]);
        this.capa = MySQLQuery.getAsInteger(row[1]);
        this.state = MySQLQuery.getAsString(row[2]);
        this.typeId = MySQLQuery.getAsInteger(row[3]);
        if (row.length > 4) {
            this.name = MySQLQuery.getAsString(row[4]);
        }
    }

    public static List<CylsAmount> getListCylsAmount(Object[][] rows) {
        List<CylsAmount> lstCylsAmounts = new ArrayList<>();
        if (rows != null && rows.length > 0) {
            for (Object[] row : rows) {
                CylsAmount obj = new CylsAmount(row);
                lstCylsAmounts.add(obj);
            }
        }
        return lstCylsAmounts;
    }

    public static CylsAmount getByCapa(List<CylsAmount> lst, int capa) {
        return getByCapa(lst, capa, null);
    }

    public static CylsAmount getByCapa(List<CylsAmount> lst, int capa, Integer typeId) {
        for (CylsAmount obj : lst) {
            if (typeId != null) {
                if (obj.capa == capa && obj.typeId == typeId) {
                    return obj;
                }
            } else {
                if (obj.capa == capa) {
                    return obj;
                }
            }
        }
        return null;
    }

}
