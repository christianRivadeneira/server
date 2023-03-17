package api.rpt.api.operations;

import api.rpt.api.dataTypes.DataType;

public class Cnt extends Operation {

    @Override
    public String getName() {
        return "cnt";
    }

    @Override
    public String getLabel() {
        return "Conteo";
    }

    @Override
    public DataType getResultType(DataType dt) {
        return DataType.TYPE_INT;
    }

}
