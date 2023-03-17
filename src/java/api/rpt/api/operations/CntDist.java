package api.rpt.api.operations;

import api.rpt.api.dataTypes.DataType;

public class CntDist extends Operation {

    @Override
    public String getName() {
        return "cnt_dist";
    }

    @Override
    public String getLabel() {
        return "Conteo Dif";
    }

    @Override
    public DataType getResultType(DataType dt) {
        return DataType.TYPE_INT;
    }
}
