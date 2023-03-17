package api.rpt.model.rptTbls;

import api.rpt.model.RptInfo;
import api.rpt.api.dataTypes.DataType;

import java.util.ArrayList;
import java.util.List;

public class PieTable {

    public final Object[][] data;
    public List<Key> secs = new ArrayList<>();

    public PieTable(RptInfo info, Object[][] rawData) throws Exception {
        this.data = new Object[rawData.length][2];
        DataType[] types = new DataType[info.dims.size()];
        for (int j = 0; j < info.dims.size(); j++) {
            types[j] = DataType.getType(info.dims.get(j).getCubeFld().dataType);
        }

        for (int i = 0; i < rawData.length; i++) {
            Object[] rawRow = rawData[i];
            String rowName = "";
            for (int j = 0; j < info.dims.size(); j++) {
                rowName += (rawRow[j] != null ? types[j].getAsXlsString(rawRow[j]) : "No definido") + " ";
            }
            
            
            if (!Key.contains(secs, rowName)) {
                Object[] keys = new Object[info.dims.size()];
                System.arraycopy(rawRow, 0, keys, 0, keys.length);
                secs.add(new Key(rowName, keys));
            }
            data[i][0] = rowName;
            data[i][1] = rawRow[info.dims.size()];
        }
    }
}
