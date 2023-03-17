package api.rpt.api.dataTypes;

import api.rpt.api.operations.Operation;
import java.text.Format;
import javax.swing.JSpinner;
import jxl.biff.DisplayFormat;
import jxl.write.WritableSheet;
import api.MySQLCol;
import utilities.MySQLQuery;

public class Bool extends DataType {

    @Override
    public String getName() {
        return "bool";
    }

    @Override
    public String getLabel() {
        return "Booleano";
    }

    @Override
    public String getAsXlsString(Object content) {
        return MySQLQuery.getAsBoolean(content) ? "Si" : "No";
    }

    @Override
    public Format getJavaFormat() {
        return null;
    }

    @Override
    public DisplayFormat getJxlFormat() {
        return null;
    }

    @Override
    public void addCell(WritableSheet sheet, int fontStyle, String bgColor, CellBorder border, int col, int row, Object val) throws Exception {
        sheet.addCell(new jxl.write.Label(col, row, getAsXlsString(val), getFormat(fontStyle, bgColor, border)));
    }

    @Override
    public boolean isAbleToAdd() {
        return false;
    }

    @Override
    public String getAsSQLString(Object content) {
        if (((Boolean) content)) {
            return "1";
        } else {
            return "0";
        }
    }

    @Override
    public Operation[] getOpers() {
        return new Operation[]{Operation.CNT, Operation.CNT_DIST};
    }

    @Override
    public MySQLCol getMySQLCol() {
        return new MySQLCol(MySQLCol.TYPE_BOOLEAN, 100, "Valor");
    }

    @Override
    public boolean hasMaxMin() {
        return false;
    }

    @Override
    public void prepareSpinner(JSpinner spn) {
        throw new UnsupportedOperationException("El tipo no soporta spinners");
    }
}
