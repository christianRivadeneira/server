package api.rpt.api.dataTypes;

import api.rpt.api.operations.Operation;
import java.text.DecimalFormat;
import java.text.Format;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import jxl.biff.DisplayFormat;
import jxl.write.NumberFormat;
import jxl.write.WritableSheet;
import api.MySQLCol;
import utilities.MySQLQuery;

public class Dec extends DataType {

    private static final String STR_FORMAT = "###,##0.00";
    private static final DecimalFormat FORMAT = new DecimalFormat(STR_FORMAT);

    @Override
    public String getName() {
        return "dec";
    }

    @Override
    public String getLabel() {
        return "Decimal";
    }

    @Override
    public String getAsXlsString(Object content) {
        if (content == null) {
            return "";
        }
        return FORMAT.format(MySQLQuery.getAsBigDecimal(content, false));
    }

    @Override
    public Format getJavaFormat() {
        return FORMAT;
    }

    @Override
    public DisplayFormat getJxlFormat() {
        return new NumberFormat(STR_FORMAT);
    }

    @Override
    public void addCell(WritableSheet sheet, int fontStyle, String bgColor, CellBorder border, int col, int row, Object val) throws Exception {
        sheet.addCell(new jxl.write.Number(col, row, MySQLQuery.getAsBigDecimal(val, false).doubleValue(), getFormat(fontStyle, bgColor, border)));
    }

    @Override
    public boolean isAbleToAdd() {
        return true;
    }

    @Override
    public String getAsSQLString(Object content) {
        return MySQLQuery.getAsBigDecimal(content, true).toPlainString();
    }

    @Override
    public Operation[] getOpers() {
        return Operation.getAll();
    }

    @Override
    public MySQLCol getMySQLCol() {
        return new MySQLCol(MySQLCol.TYPE_DECIMAL_2, 100, "Valor");
    }

    @Override
    public boolean hasMaxMin() {
        return true;
    }

    @Override
    public void prepareSpinner(JSpinner spn) {
        spn.setModel(new SpinnerNumberModel(0, null, null, 0.01d));
        spn.setEditor(new JSpinner.NumberEditor(spn, STR_FORMAT));
    }
}
