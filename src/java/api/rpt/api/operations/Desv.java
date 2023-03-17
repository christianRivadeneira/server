package api.rpt.api.operations;

import api.rpt.api.dataTypes.DataType;

public class Desv extends Operation {

    @Override
    public String getName() {
        return "desv";
    }

    @Override
    public String getLabel() {
        return "Desviación";
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
