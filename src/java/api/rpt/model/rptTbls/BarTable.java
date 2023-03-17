package api.rpt.model.rptTbls;

import api.rpt.model.RptInfo;
import api.rpt.api.dataTypes.DataType;
import api.rpt.model.RptRptFld;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarTable {
    
    public List<Value> ds = new ArrayList<>();
    public List<Key> cats = new ArrayList<>();
    public List<Key> series = new ArrayList<>();

    public double max = Double.MIN_VALUE;
    public double min = Double.MAX_VALUE;

    private final Map<String, Integer> catIndexes = new HashMap<>();
    private final Map<String, Integer> seriesIndexes = new HashMap<>();

    public BarTable(RptInfo info, Object[][] rawData) throws Exception {
        boolean sorted = false;
        List<RptRptFld> joins = info.joins;
        for (int i = 0; i < joins.size() && !sorted; i++) {
            if (joins.get(i).sort != null) {
                sorted = true;
            }
        }
        if (!sorted) {
            sortMat(rawData, info.rows.size());
        }

        for (Object[] row : rawData) {
            Object[] rowsKey = new Object[info.rows.size()];
            Object[] colsKey = new Object[info.cols.size()];
            Object[] joinKey = new Object[info.joins.size()];

            System.arraycopy(row, 0, rowsKey, 0, rowsKey.length);
            System.arraycopy(row, rowsKey.length, colsKey, 0, colsKey.length);
            System.arraycopy(row, rowsKey.length + colsKey.length, joinKey, 0, joinKey.length);

            Object val = joinKey[0];

            String seriesName = "";
            for (int i = 0; i < rowsKey.length; i++) {
                if (rowsKey[i] != null) {
                    seriesName += DataType.getType(info.rows.get(i).getCubeFld().dataType).getAsXlsString(rowsKey[i]);
                } else {
                    seriesName += "No definido";
                }
                seriesName += " ";
            }
            seriesName = seriesName.trim();

            String catName = "";
            for (int i = 0; i < colsKey.length; i++) {
                if (colsKey[i] != null) {
                    catName += DataType.getType(info.cols.get(i).getCubeFld().dataType).getAsXlsString(colsKey[i]);
                } else {
                    catName += "No definido";
                }
                catName += " ";
            }
            catName = catName.trim();
            
            if (!catIndexes.containsKey(catName)) {
                catIndexes.put(catName, cats.size());
                cats.add(new Key(catName, colsKey));
            }

            if (!seriesIndexes.containsKey(seriesName)) {
                seriesIndexes.put(seriesName, series.size());
                series.add(new Key(seriesName, rowsKey));
            }
            if (val != null) {
                double d = ((Number) val).doubleValue();
                if (d < min) {
                    min = d;
                }
                if (d > max) {
                    max = d;
                }
                try {
                    ds.add(new Value((Number) val, seriesIndexes.get(seriesName), catIndexes.get(catName)));
                }catch(Exception ex){
                    int a = 1;
                }
            }
        }
    }

    public static void sortMat(Object[][] mat, final int skip) {
        Arrays.sort(mat, new Comparator<Object[]>() {
            @Override
            public int compare(Object[] l1, Object[] l2) {
                for (int i = skip; i < l1.length; i++) {
                    int c;
                    if (l1[i] == null && l2[i] == null) {
                        c = 0;
                    } else if (l1[i] != null && l2[i] == null) {
                        c = -1;
                    } else if (l1[i] == null && l2[i] != null) {
                        c = 1;
                    } else if (l1[i] != null && l2[i] != null) {
                        c = ((Comparable) l1[i]).compareTo((Comparable) l2[i]);
                    } else {
                        throw new RuntimeException();
                    }
                    if (c != 0) {
                        return c;
                    }
                }
                return 0;
            }
        });
    }
}
