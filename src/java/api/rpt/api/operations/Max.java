package api.rpt.api.operations;

import api.rpt.api.dataTypes.DataType;

public class Max extends Operation {

    @Override
    public String getName() {
        return "max";
    }

    @Override
    public String getLabel() {
        return "Máximo";
    }

    @Override
    public DataType getResultType(DataType dt) {
        if (dt.hasMaxMin()) {
            return dt;
        } else {
            throw new RuntimeException("La operación no soporta el tipo: " + dt.getName());
        }
    }

}
