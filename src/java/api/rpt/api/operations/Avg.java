package api.rpt.api.operations;

import api.rpt.api.dataTypes.DataType;

public class Avg extends Operation {

    @Override
    public String getName() {
        return "avg";
    }

    @Override
    public String getLabel() {
        return "Promedio";
    }

    @Override
    public DataType getResultType(DataType dt) {
        if (dt.isAbleToAdd()) {
            return dt;
        } else {
            throw new RuntimeException("La operación no soporta el tipo: " + dt.getName());
        }
    }
}
