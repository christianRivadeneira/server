package api;

import java.util.HashMap;
import java.util.Map;
import metadata.log.Diff;
import static metadata.model.Table.hasUpperOrNum;

public class Params {

    public static final int ASC = 1;
    public static final int DESC = 2;

    protected final Map<String, Object> paramsMap = new HashMap<>();
    protected final Map<String, Integer> sortMap = new HashMap<>();

    public Params() {

    }

    public Params(String fld, Object val) {
        super();
        if (hasUpperOrNum(fld)) {
            fld = Diff.toDbName(fld);
        }
        paramsMap.put(fld, val);
    }

    public Params param(String fld, Object val) {
        if (hasUpperOrNum(fld)) {
            fld = Diff.toDbName(fld);
        }
        paramsMap.put(fld, val);
        return this;
    }

    public Params sort(String fld, int type) {
        sortMap.put(fld, type);
        return this;
    }

    public Params sort(String fld) {
        sortMap.put(fld, ASC);
        return this;
    }

    public Params orderBy(String fld, int type) {
        return sort(fld, type);
    }

    public Params orderBy(String fld) {
        return sort(fld);
    }

}
