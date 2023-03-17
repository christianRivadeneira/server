package web.marketing.cylSales;

import web.ShortException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class Cylinder {

    public int trkCylId;
    public String cylNif;
    public int cylTypeId;
    public String minasName;
    public String mgName;
    public boolean salable;
    public Integer respId;
    public int year;
    public int factory;
    public int serial;

    public Cylinder(CylValidations val, String nif) throws Exception {
        trkCylId = val.cylId;
        cylTypeId = val.typeId;
        minasName = val.kgName;
        mgName = val.typeName;
        salable = val.salable;
        respId = val.respId;
        cylNif = nif;
        year = val.year;
        factory = val.factory;
        serial = val.serial;
    }

    public static Cylinder getCylinder(String nif, boolean delivered, boolean isSubSale, boolean offLine, WarningList sale, int empId, Connection conn) throws Exception {
        CylValidations val = CylValidations.getValidations(nif, empId, false, CylValidations.SALES, null, conn);
        if (val.cylError != null) {
            manageException(val.cylError, offLine, sale);
        }
        if (delivered) {
            if (val.saleError != null) {
                manageException(val.saleError, offLine, sale);
            }
            if (isSubSale) {
                if (val.subsidyError != null) {
                    manageException(val.subsidyError, offLine, sale);
                }
            }
        }
        return new Cylinder(val, nif);
    }

    public static List<Cylinder> getCylFromList(Connection conn, List<String> nifs, boolean offLine, boolean delivered, int empId, WarningList sale) throws ShortException, Exception {
        List<Cylinder> valNifs = new ArrayList<>();
        if (nifs != null && !nifs.isEmpty()) {
            for (int i = 0; i < nifs.size(); i++) {
                valNifs.add(getCylinder(nifs.get(i), delivered, false, offLine, sale, empId, conn));
            }
        }
        return valNifs;
    }

    private static void manageException(String ex, boolean offLine, WarningList s) throws Exception {
        if (!offLine) {
            throw new ShortException(ex);
        } else {
            s.addWarn(ex);
        }
    }

}
