package api.rpt.api.operations;

import api.rpt.api.dataTypes.DataType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Operation {

    public static final Operation SUM = new Sum();
    public static final Operation CNT = new Cnt();
    public static final Operation CNT_DIST = new CntDist();
    public static final Operation MAX = new Max();
    public static final Operation MIN = new Min();
    public static final Operation AVG = new Avg();
    public static final Operation DESV = new Desv();

    public abstract String getName();

    public abstract String getLabel();

    public abstract DataType getResultType(DataType dataType);

    private static final Operation[] opers;

    static {
        List<Operation> lTypes = new ArrayList<>();
        Field[] flds = Operation.class.getDeclaredFields();
        for (Field fld : flds) {
            if (fld.getType().equals(Operation.class)) {
                try {
                    lTypes.add((Operation) fld.get(null));
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(Operation.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        opers = lTypes.toArray(new Operation[lTypes.size()]);
    }

    public static Operation[] getAll() {
        return opers;
    }
    
        public static Operation getOper(String oper) {
        for (Operation type1 : opers) {
            if (type1.getName().equals(oper)) {
                return type1;
            }
        }
        throw new RuntimeException("Tipo no reconocido: " + oper);
    }
            
}
