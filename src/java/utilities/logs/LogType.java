package utilities.logs;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class LogType {

    public int id;
    public String nameQuery;
    public String nameType = "";

    public LogType(int id) {
        this.id = id;
    }

    public LogType(int id, String nameQuery) {
        this.id = id;
        this.nameQuery = nameQuery;
    }

    public LogType(int id, String nameQuery, String nameType) {
        this.id = id;
        this.nameQuery = nameQuery;
        this.nameType = nameType;
    }
    
    public static LogType[] getTypes(Class cs) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        List<LogType> rta = new ArrayList<>();
        Field[] flds = cs.getDeclaredFields();
        for (Field fld : flds) {
            if (fld.getType().equals(LogType.class)) {
                rta.add((LogType) fld.get(null));
            }
        }
        return rta.toArray(new LogType[rta.size()]);
    }  
    
}
