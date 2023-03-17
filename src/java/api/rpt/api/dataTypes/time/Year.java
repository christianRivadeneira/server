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

public class Year extends DataType {

    private static final String STR_FORMAT = "#";
    private static final DecimalFormat FORMAT = new DecimalFormat(STR_FORMAT);

    @Override
    public String getName() {
        return "year";
    }

    @Override
    public String getLabel() {
        return "Año";
    }

    @Override
    public String getAsXlsString(Object content) {
        if (content == null) {
            return "";
        }
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
        return new MySQLCol(MySQLCol.TYPE_INTEGER, 100, "Año");
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
        return "YEAR(" + field + ")";
    }
}
