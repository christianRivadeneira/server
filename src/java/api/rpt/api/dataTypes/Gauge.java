package api.rpt.api.dataTypes;

import api.rpt.api.operations.Operation;
import java.text.Format;
import javax.swing.JSpinner;
import jxl.biff.DisplayFormat;
import jxl.write.WritableSheet;
import api.MySQLCol;

public class Gauge extends DataType {

    @Override
    public String getName() {
        return "gauge";
    }

    @Override
    public String getLabel() {
        return "Indicador";
    }

    @Override
    public String getAsXlsString(Object content) {
        if (content.equals("ok")) {
            return "Correcto";
        } else if (content.equals("error")) {
            return "Error";
        } else if (content.equals("warn")) {
            return "Advertencia";
        } else {
            throw new UnsupportedOperationException();
        }
//        return MySQLQuery.getAsString(content);
        //throw new UnsupportedOperationException("El tipo no soporta spinners");
    }

    @Override
    public Format getJavaFormat() {
//        return null;
        throw new UnsupportedOperationException("El tipo no soporta spinners");
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
        throw new UnsupportedOperationException("El tipo no soporta spinners");
//        if (((String) content).trim().isEmpty()) {
//            return "NULL";
//        } else {
//            return Matcher.quoteReplacement("\"" + NotesTextField.scape((String) content) + "\"");
//        }
    }

    @Override
    public Operation[] getOpers() {
        throw new UnsupportedOperationException("El tipo no soporta spinners");
        //return new Operation[]{Operation.CNT, Operation.CNT_DIST};
    }

    @Override
    public MySQLCol getMySQLCol() {
        throw new UnsupportedOperationException("El tipo no soporta spinners");
        //return new MySQLCol(MySQLCol.TYPE_TEXT, 100, "Valor");
    }

    @Override
    public boolean hasMaxMin() {
        throw new UnsupportedOperationException("El tipo no soporta spinners");
        //return false;
    }

    @Override
    public void prepareSpinner(JSpinner spn) {
        throw new UnsupportedOperationException("El tipo no soporta spinners");
    }
}
