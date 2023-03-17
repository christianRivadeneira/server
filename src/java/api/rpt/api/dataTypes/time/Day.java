package api.rpt.api.dataTypes.time;

import api.rpt.api.dataTypes.CellBorder;
import api.rpt.api.dataTypes.DataType;
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

public class Day extends DataType {

    private static final String STR_FORMAT = "#";
    private static final DecimalFormat FORMAT = new DecimalFormat(STR_FORMAT);

    @Override
    public String getName() {
        return "day";
    }

    @Override
    public String getLabel() {
        return "Día";
    }

    @Override
    public String getAsXlsString(Object content) {
        return FORMAT.format(MySQLQuery.getAsInteger(content));
    }

    @Override
    public String getAsSQLString(Object content) {
        return MySQLQuery.getAsString(content);
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
        sheet.addCell(new jxl.write.Number(col, row, MySQLQuery.getAsInteger(val), getFormat(fontStyle, bgColor, border)));
    }

    @Override
    public boolean isAbleToAdd() {
        return true;
    }

    @Override
    public Operation[] getOpers() {
        return Operation.getAll();
    }

    @Override
    public MySQLCol getMySQLCol() {
        return new MySQLCol(MySQLCol.TYPE_INTEGER, 100, "Día");
    }

    @Override
    public boolean hasMaxMin() {
        return true;
    }

    @Override
    public void prepareSpinner(JSpinner spn) {
        spn.setModel(new SpinnerNumberModel());
        spn.setEditor(new JSpinner.NumberEditor(spn, STR_FORMAT));
    }

    @Override
    public String getFunction(String field) {
        return "DAYOFMONTH(" + field + ")";
    }
}
