package api.rpt.api.dataTypes;

import api.rpt.api.operations.Operation;
import java.text.Format;
import java.text.SimpleDateFormat;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import jxl.biff.DisplayFormat;
import jxl.write.DateFormat;
import jxl.write.WritableSheet;
import api.MySQLCol;
import utilities.MySQLQuery;

public class Date extends DataType {

    private static final String STR_FORMAT = "dd/MM/yyyy";
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat(STR_FORMAT);

    @Override
    public String getName() {
        return "date";
    }

    @Override
    public String getLabel() {
        return "Fecha";
    }

    @Override
    public String getAsXlsString(Object content) {
        return FORMAT.format(MySQLQuery.getAsDate(content));
    }

    @Override
    public Format getJavaFormat() {
        return FORMAT;
    }

    @Override
    public DisplayFormat getJxlFormat() {
        return new DateFormat(STR_FORMAT);
    }

    @Override
    public void addCell(WritableSheet sheet, int fontStyle, String bgColor, CellBorder border, int col, int row, Object val) throws Exception {
        sheet.addCell(new jxl.write.DateTime(col, row, MySQLQuery.getAsDate(val), getFormat(fontStyle, bgColor, border)));
    }

    @Override
    public boolean isAbleToAdd() {
        return false;
    }

    @Override
    public String getAsSQLString(Object content) {
        return MySQLQuery.dateFormat.format((java.util.Date) content);
    }

    @Override
    public Operation[] getOpers() {
        return new Operation[]{Operation.CNT, Operation.CNT_DIST, Operation.MAX, Operation.MIN};
    }

    @Override
    public MySQLCol getMySQLCol() {
        return new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 100, "Valor");
    }

    @Override
    public boolean hasMaxMin() {
        return true;
    }

    @Override
    public void prepareSpinner(JSpinner spn) {
        spn.setModel(new SpinnerDateModel());
        spn.setEditor(new JSpinner.DateEditor(spn, STR_FORMAT));
    }
}
