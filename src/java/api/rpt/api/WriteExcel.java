package api.rpt.api;

import api.rpt.api.dataTypes.CellBorder;
import api.rpt.api.dataTypes.DataType;
import api.rpt.model.RptInfo;
import api.rpt.model.rptTbls.PivotTable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import jxl.CellView;
import jxl.format.Colour;
import jxl.write.Blank;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import utilities.Reports;

public class WriteExcel {

    private static final String SUM_FX_NAME;

    static {
        if (Locale.getDefault().getLanguage().toLowerCase().equals("en")) {
            SUM_FX_NAME = "SUM";
        } else {
            SUM_FX_NAME = "SUMA";
        }
    }

    public static File writeTable(PivotTable p, RptInfo info) throws Exception {
        File f = File.createTempFile(info.rpt.name, ".xls");
        WritableWorkbook workbook = Reports.getModernBaseAsStream(f, 1);
        workbook.removeSheet(0);
        WritableSheet sheet = workbook.createSheet(info.rpt.name, 0);

        sheet.getSettings().setZoomFactor(80);
        //sheet.getSettings().setProtected(true);
        //sheet.getSettings().setHorizontalFreeze(p.tblHeaderCols + 1);
        sheet.getSettings().setVerticalFreeze(p.tblHeaderRows + 1);
        sheet.getSettings().setShowGridLines(false);
        sheet.getSettings().setPrintGridLines(false);

        CellView cv = new CellView();
        cv.setSize(5 * 256);
        sheet.setColumnView(0, cv);

        if (info.joins.size() > 0) {
            //fusionar títulos de filas
            for (int i = 0; i < info.rows.size(); i++) {
                int groupStart = 0;
                for (int j = 0; j < p.data.length - 1; j++) {
                    if ((i > 0 ? !Objects.equals(p.data[j + 1][i - 1], p.data[j][i - 1]) : false) || !Objects.equals(p.data[j + 1][i], p.data[j][i])) {
                        sheet.mergeCells(i + 1, groupStart + 1, i + 1, j + 1);
                        groupStart = j + 1;
                    }
                }
                sheet.mergeCells(i + 1, groupStart + 1, i + 1, p.data.length);
            }

            //fusionar títulos de columnas
            for (int i = 0; i < info.cols.size(); i++) {
                int groupStart = 0;
                for (int j = 0; j < p.data[0].length - 1; j++) {
                    if ((i > 0 ? !Objects.equals(p.data[i - 1][j + 1], p.data[i - 1][j]) : false) || !Objects.equals(p.data[i][j + 1], p.data[i][j])) {
                        sheet.mergeCells(groupStart + 1, i + 1, j + 1, i + 1);
                        groupStart = j + 1;
                    }
                }
                sheet.mergeCells(groupStart + 1, i + 1, p.data[0].length, i + 1);
            }
        }

        for (int i = 0; i < p.data.length; i++) {
            Object[] row = p.data[i];
            for (int j = 0; j < row.length; j++) {
                Object val = row[j];
                int right = CellBorder.NONE;
                int bottom = CellBorder.NONE;
                if (j < p.tblHeaderCols || i < p.tblHeaderRows) {
                    if (j == p.tblHeaderCols - 1) {
                        right = CellBorder.MEDIUM;
                    }
                    if (i == p.tblHeaderRows - 1) {
                        bottom = CellBorder.MEDIUM;
                    }
                }

                CellBorder b = new CellBorder(CellBorder.NONE, right, bottom, CellBorder.NONE, info.rpt.color);
                //boolean title = (j < p.tblHeaderCols || i < p.tblHeaderRows);
                String color = ((p.tblHeaderRows % 2 == 0 ? i % 2 == 0 : i % 2 != 0) && i >= p.tblHeaderRows ? DataType.COL_LIGHT_GRAY : DataType.COL_WHITE);

                if (val != null) {
                    DataType.getType(p.types[i][j]).addCell(sheet, DataType.FONT_REGULAR, color, b, j + 1, i + 1, val);
                } else {
                    DataType.TYPE_STR.addCell(sheet, DataType.FONT_REGULAR, color, b, j + 1, i + 1, null);
                }
            }
        }

        int maxColW = 0;
        for (int i = 0; i < p.data[0].length; i++) {
            int cs = autosizeColumn(sheet, i, DataType.getBoldFont(), p);
            if (i >= p.tblHeaderCols) {
                maxColW = Math.max(maxColW, cs);
            }
        }

        addTotalRow(sheet, p);
        addTotalCol(sheet, p, maxColW);
        //showColors(sheet);

        for (int i = 0; i < p.data.length; i++) {
            cv = new CellView();
            cv.setSize(350);
            sheet.setRowView(i + 1, cv);
        }

        DataType.clearFormats();
        workbook.write();
        workbook.close();
        return f;
    }

    public static void showColors(WritableSheet sheet) throws Exception {
        Colour[] colors = Colour.getAllColours();
        for (int i = 0; i < colors.length; i++) {
            Colour color = colors[i];
            WritableCellFormat cf = new WritableCellFormat();
            cf.setBackground(color);
            sheet.addCell(new Label(0, i, color.getDescription(), cf));
        }
    }

    private static void addTotalRow(WritableSheet sheet, PivotTable p) throws Exception {
        //TOTALES POR COLUMNAS
        CellBorder b = new CellBorder();
        boolean added = false;
        for (int i = p.tblHeaderCols; i < p.data[0].length; i++) {
            List<String> colTypes = new ArrayList<>();
            for (int j = p.tblHeaderRows; j < p.data.length; j++) {
                if (p.types[j][i] != null && !colTypes.contains(p.types[j][i])) {
                    colTypes.add(p.types[j][i]);
                    if (colTypes.size() > 1) {
                        break;
                    }
                }
            }

            DataType dt = DataType.getType((colTypes.get(0)));
            if (colTypes.size() == 1 && dt.isAbleToAdd()) {
                added = true;
                String colName = Reports.getColName(i + 1);
                String fx = SUM_FX_NAME + "(" + colName + (p.tblHeaderRows + 2) + " : " + colName + (p.data.length + 1) + ")";
                sheet.addCell(new Formula(i + 1, p.data.length + 1, fx, dt.getFormat(DataType.FONT_BOLD, DataType.COL_LIGHT_GRAY, b)));
            } else {
                sheet.addCell(new Blank(i + 1, p.data.length + 1, DataType.getFormat(DataType.FONT_REGULAR, DataType.COL_LIGHT_GRAY, b, null)));
            }
        }
        if (added) {
            if (p.tblHeaderCols > 0) {
                sheet.addCell(new Label(1, p.data.length + 1, "Total", DataType.getFormat(DataType.FONT_BOLD, DataType.COL_LIGHT_GRAY, b, null)));
                sheet.mergeCells(1, p.data.length + 1, p.tblHeaderCols, p.data.length + 1);
            }
        } else {
            sheet.removeRow(p.data.length + 1);
        }
    }

    private static void addTotalCol(WritableSheet sheet, PivotTable p, int maxColW) throws Exception {
        CellBorder b = new CellBorder();
        boolean added = false;
        if (p.data.length == 0 || (p.tblHeaderCols - p.data[0].length == 0)) {
            return;
        }

        for (int i = p.tblHeaderRows; i < p.data.length; i++) {
            List<String> rowTypes = new ArrayList<>();
            for (int j = p.tblHeaderCols; j < p.data[0].length; j++) {
                if (p.types[i][j] != null && !rowTypes.contains(p.types[i][j])) {
                    rowTypes.add(p.types[i][j]);
                    if (rowTypes.size() > 1) {
                        break;
                    }
                }
            }

            DataType dt = DataType.getType((rowTypes.get(0)));

            if (rowTypes.size() == 1 && dt.isAbleToAdd()) {
                added = true;
                String colName1 = Reports.getColName(p.tblHeaderCols + 1);
                String colName2 = Reports.getColName(p.data[0].length);
                int row = i + 2;
                sheet.addCell(new Formula(p.data[0].length + 1, i + 1, SUM_FX_NAME+"(" + colName1 + (row) + ":" + colName2 + (row) + ")", dt.getFormat(DataType.FONT_BOLD, DataType.COL_LIGHT_GRAY, b)));
            } else {
                sheet.addCell(new Blank(p.data[0].length + 1, i + 1, DataType.getFormat(DataType.FONT_REGULAR, DataType.COL_LIGHT_GRAY, b, null)));
            }
        }

        if (added) {
            sheet.addCell(new Label(p.data[0].length + 1, 1, "Total", DataType.getFormat(DataType.FONT_BOLD, DataType.COL_LIGHT_GRAY, b, null)));
            sheet.mergeCells(p.data[0].length + 1, 1, p.data[0].length + 1, p.tblHeaderRows);
            sheet.addCell(new Blank(p.data[0].length + 1, p.data.length + 1, DataType.getFormat(DataType.FONT_BOLD, DataType.COL_LIGHT_GRAY, b, null)));

            CellView cv = new CellView();
            cv.setSize(maxColW);
            sheet.setColumnView(p.data[0].length + 1, cv);
        } else {
            sheet.removeColumn(p.data[0].length + 1);
        }
    }

    private static int autosizeColumn(WritableSheet sheet, int col, WritableFont font, PivotTable p) {
        int maxWidth = 0;
        int skipRows = p.tblHeaderRows - 1;
        for (int i = skipRows; i < p.data.length; i++) {

            if (p.data[i][col] != null) {
                String contents = DataType.getType(p.types[i][col]).getAsXlsString(p.data[i][col]);

                int pointSize = font.getPointSize();
                int numChars = contents.length();

                if (font.isItalic() || font.getBoldWeight() > 400) {
                    numChars += 2;
                }
                int curWidth = numChars * pointSize * 256;
                maxWidth = Math.max(maxWidth, curWidth);
            }
        }
        CellView cv = new CellView();
        cv.setSize((int) (maxWidth / font.getPointSize()));
        sheet.setColumnView(col + 1, cv);
        return (int) (maxWidth / font.getPointSize());
    }
}
