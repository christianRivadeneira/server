package api.rpt.api.dataTypes;

import api.rpt.api.operations.Operation;
import java.text.Format;
import java.util.regex.Matcher;
import javax.swing.JSpinner;
import jxl.biff.DisplayFormat;
import jxl.write.WritableSheet;
import api.MySQLCol;
import utilities.MySQLQuery;

public class Str extends DataType {

    private static final String NAME = "str";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getLabel() {
        return "Cadena";
    }

    @Override
    public String getAsXlsString(Object content) {
        return MySQLQuery.getAsString(content);
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
        if (val != null) {
            sheet.addCell(new jxl.write.Label(col, row, MySQLQuery.getAsString(val), getFormat(fontStyle, bgColor, border)));
        } else {
            sheet.addCell(new jxl.write.Blank(col, row, getFormat(fontStyle, bgColor, border)));
        }
    }

    @Override
    public boolean isAbleToAdd() {
        return false;
    }

    @Override
    public String getAsSQLString(Object content) {
        if (content == null || ((String) content).trim().isEmpty()) {
            return "NULL";
        } else {
            return Matcher.quoteReplacement("\"" + MySQLQuery.scape((String) content) + "\"");
        }
    }

    @Override
    public Operation[] getOpers() {
        return new Operation[]{Operation.CNT, Operation.CNT_DIST};
    }

    @Override
    public MySQLCol getMySQLCol() {
        return new MySQLCol(MySQLCol.TYPE_TEXT, 100, "Valor");
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
