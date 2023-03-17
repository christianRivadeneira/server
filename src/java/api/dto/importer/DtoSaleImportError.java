package api.dto.importer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DtoSaleImportError {

    public String label;
    public int count;

    public DtoSaleImportError() {
    }

    public DtoSaleImportError(String label, int count) {
        this.label = label;
        this.count = count;
    }

    public static List<DtoSaleImportError> fromMap(Map<String, Integer> map) {
        List<DtoSaleImportError> rta = new ArrayList<>();
        Iterator<Map.Entry<String, Integer>> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> e = it.next();
            rta.add(new DtoSaleImportError(e.getKey(), e.getValue()));
        }
        return rta;
    }

}
