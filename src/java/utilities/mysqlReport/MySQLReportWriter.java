package utilities.mysqlReport;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.xml.datatype.XMLGregorianCalendar;
import jxl.Cell;
import jxl.CellView;
import jxl.biff.EmptyCell;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.Orientation;
import jxl.format.VerticalAlignment;
import jxl.write.Blank;
import jxl.write.DateFormat;
import jxl.write.DateTime;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableImage;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import service.MySQL.MySQLCommon;
import utilities.Dates;
import utilities.FileUtils;
import utilities.FileUtils.LogoType;
import utilities.MySQLQuery;
import utilities.Reports;

public class MySQLReportWriter {

    private static final String SUM_FX_NAME;

    static {
        if (Locale.getDefault().getLanguage().toLowerCase().equals("en")) {
            SUM_FX_NAME = "SUM";
        } else {
            SUM_FX_NAME = "SUMA";
        }
    }

    public static int LABEL = 1;
    public static int NUMBER = 2;
    public static int DATE = 3;
    public static int ENUM = 7;
    public static int LEFT = 4;
    public static int RIGHT = 5;
    public static int CENTER = 6;

    public static final int COLOR_RED = 1;
    public static final int COLOR_ORANGE = 2;
    public static final int COLOR_YELLOW = 3;
    public static final int COLOR_GREEN = 4;
    public static final int COLOR_WHITE = 5;
    public static final int COLOR_GRAY = 6;
    public static final int COLOR_BLUE = 7;

    protected static int getColumnsToPixels(MySQLReport rep) throws Exception {
        List<Column> cols = rep.getTables().get(0).getColumns();
        int pxWidth = 0;
        for (int i = 0; i < cols.size(); i++) {
            pxWidth += (cols.get(i).getWidth() * 7);
        }
        return pxWidth;
    }

    public static Image reduceImage(byte[] raw, int height) throws Exception {
        BufferedImage logo = ImageIO.read(new ByteArrayInputStream(raw));
        if (logo != null) {
            int width = (int) ((logo.getWidth(null) / ((double) logo.getHeight(null))) * height);
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            g2.drawImage(logo.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);
            return img;
        } else {
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }
    }

    protected static void addHeaderImage(MySQLReport rep, WritableSheet sheet, Map<Integer, Integer> widths, Connection conn) throws Exception {
        //List<Column> cols = rep.getTables().get(0).getColumns();
        Image logo = reduceImage(FileUtils.getEnterpriseLogo(conn, LogoType.ICON_REPORT), 50);
        int COLS = 10;
        int pxWidth = 0;

        for (int i = 0; i < Math.min(COLS, widths.size()); i++) {
            pxWidth += (widths.get(i + 1) * 7);
        }
        pxWidth += (Math.max(COLS - widths.size(), 0) * 81);

        int imgHeight = Math.max(50 + (20 * rep.getSubTitles().size()), logo.getHeight(null));
        BufferedImage img = new BufferedImage(pxWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g.setColor(Color.white);
        g.fillRect(0, 0, (int) pxWidth, imgHeight);
        g.drawImage(logo, 0, imgHeight > logo.getHeight(null) ? (imgHeight - logo.getHeight(null)) / 2 : 0, null);
        g.setColor(Color.black);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        g.drawString(rep.getTitle(), logo.getWidth(null) + 15, 20);

        g.setColor(Color.gray);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        g.drawString("Generado el " + new SimpleDateFormat("dd/MM/yyyy hh:mm a").format(rep.getCreation()), logo.getWidth(null) + 15, 40);
        for (int i = 0; i < rep.getSubTitles().size(); i++) {
            g.drawString(rep.getSubTitles().get(i), logo.getWidth(null) + 15, (20 * i) + 60);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", bos);
        WritableImage wi = new WritableImage(1, 1, COLS, 1, bos.toByteArray());
        sheet.addImage(wi);
        CellView cv = new CellView();
        cv.setSize((int) (imgHeight * 0.756d * 20));
        sheet.setRowView(1, cv);
    }

    protected static WritableCellFormat getTotal() throws Exception {
        WritableFont font = new WritableFont(WritableFont.ARIAL, 10);
        font.setBoldStyle(WritableFont.BOLD);
        WritableCellFormat format = new WritableCellFormat(font);
        format.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.BLACK);
        format.setBackground(Colour.ORANGE);
        format.setAlignment(Alignment.CENTRE);
        format.setVerticalAlignment(VerticalAlignment.CENTRE);
        return format;
    }

    protected static List<WritableCellFormat> getFormats(MySQLReport rep, Colour backGround, Colour border) throws Exception {
        List<WritableCellFormat> exFormats = new ArrayList<>();
        Iterator<CellFormat> itFormats = rep.getFormats().iterator();
        while (itFormats.hasNext()) {
            CellFormat cellFormat = itFormats.next();
            WritableCellFormat format = null;
            if (cellFormat.getType() == NUMBER) {
                format = new WritableCellFormat(new NumberFormat(cellFormat.getFormat()));
            } else if (cellFormat.getType() == LABEL) {
                format = new WritableCellFormat();
            } else if (cellFormat.getType() == DATE) {
                format = new WritableCellFormat(new DateFormat(cellFormat.getFormat()));
            } else if (cellFormat.getType() == ENUM) {
                format = new EnumCellFormat(cellFormat.getFormat());
            } else {
                throw new Exception("No se reconoce el tipo " + cellFormat.getType());
            }

            if (cellFormat.getAlign() == RIGHT) {
                format.setAlignment(Alignment.RIGHT);
            } else if (cellFormat.getAlign() == LEFT) {
                format.setAlignment(Alignment.LEFT);
            } else if (cellFormat.getAlign() == CENTER) {
                format.setAlignment(Alignment.CENTRE);
            }
            format.setVerticalAlignment(VerticalAlignment.CENTRE);
            format.setBorder(Border.ALL, BorderLineStyle.THIN, border);
            format.setBackground(backGround);
            format.setWrap(cellFormat.isWrap());
            exFormats.add(format);
        }
        return exFormats;
    }

    private static int addTotalRow(MySQLReport rep, Table table, WritableSheet sheet, int row, List<WritableCellFormat> exFormatsSummary) throws Exception {
        if (table.getSummaryRow() != null) {
            row++;
            jxl.format.CellFormat total = getTotal();
            int merge = table.getSummaryRow().getMerge();
            sheet.addCell(new Label(1, row, table.getSummaryRow().getName(), total));
            for (int j = 1; j < merge; j++) {
                sheet.addCell(new Blank(j + 1, row, total));
            }
            sheet.mergeCells(1, row, merge, row);

            for (int j = merge + 1; j <= table.getColumns().size(); j++) {
                String colName = Reports.getColName(j);
                if (!table.getSummaryRow().isColumnDisabled(j - 1) && rep.getFormats().get(table.getColumns().get(j - 1).getFormat()).getType() == NUMBER) {
                    sheet.addCell(new Formula(j, row, SUM_FX_NAME + "(" + colName + "" + (row - table.getData().length + 1) + ":" + colName + "" + row + ")", exFormatsSummary.get(table.getColumns().get(j - 1).getFormat())));
                } else {
                    sheet.addCell(new Blank(j, row, total));
                }
            }
            CellView cv = new CellView();
            cv.setSize((15 + 2) * 20);
            sheet.setRowView(row, cv);
        }
        return row;
    }

    protected static void addNumberCol(MySQLReport rep) {
        //agregar columna de contadores.
        if (rep.isShowNumbers()) {
            Iterator<Table> tablesIt = rep.getTables().iterator();
            for (int i = 0; tablesIt.hasNext(); i++) {
                Table table = tablesIt.next();
                List<TableHeader> headers = table.getHeaders();
                for (int j = 0; j < headers.size(); j++) {
                    TableHeader header = headers.get(j);
                    if (!(header.getColums().get(0) instanceof NumHeaderColumn)) {
                        if (j == 0) {
                            header.getColums().add(0, new NumHeaderColumn(headers.size() + 1));
                        } else {
                            header.getColums().add(0, new NumHeaderColumn(1));
                        }
                    }
                }
                if (!(table.getColumns().get(0) instanceof NumColumn)) {
                    table.getColumns().add(0, new NumColumn());
                }

                Object[][] newData = new Object[table.getData().length][table.getColumns().size()];
                for (int j = 0; j < table.getData().length; j++) {
                    System.arraycopy(table.getData()[j], 0, newData[j], 1, table.getData()[j].length);
                    newData[j][0] = j + 1;
                    table.getData()[j] = null;
                }
                table.setData(newData);
            }
        }
    }

    protected static int addTable(MySQLReport rep, int tableI, int curRow, WritableSheet sheet, Map<Integer, List<WritableCellFormat>> colFormats, List<CellFormat> formats, jxl.format.CellFormat tableTitle, jxl.format.CellFormat columnTitleLeft, WritableCellFormat columnTitleCent, jxl.format.CellFormat columnTitleRot, List<WritableCellFormat> exFormatsSummary) throws Exception {
        Table table = rep.getTables().get(tableI);
        CellView cv;
        //titulo de la tabla;
        if (table.getTitle() != null ? (!table.getTitle().isEmpty()) : false) {
            curRow++;
            cv = new CellView();
            cv.setSize((15 + 3) * 20);
            sheet.setRowView(curRow, cv);
            sheet.addCell(new Label(1, curRow, table.getTitle(), tableTitle));
            for (int j = 1; j < table.getColumns().size(); j++) {
                sheet.addCell(new Blank(j + 1, curRow, tableTitle));
            }
            sheet.mergeCells(1, curRow, table.getColumns().size(), curRow);
        }
        //cabeceras de las tablas
        List<TableHeader> headers = table.getHeaders();
        for (int j = 0; j < headers.size(); j++) {
            curRow++;
            cv = new CellView();
            cv.setSize((15 + 2) * 20);
            sheet.setRowView(curRow, cv);
            TableHeader header = headers.get(j);
            int xp = 1;
            for (int k = 0; k < header.getColums().size(); k++) {
                HeaderColumn col = header.getColums().get(k);
                if (col.getColName() != null) {
                    Label label;
                    if (col.getColSpan() > 1) {
                        label = new Label(xp, curRow, col.getColName(), columnTitleCent);
                        for (int l = 1; l < col.getColSpan(); l++) {
                            sheet.addCell(new Blank(xp + l, curRow, columnTitleLeft));
                        }
                        sheet.mergeCells(xp, curRow, xp + col.getColSpan() - 1, curRow);
                    } else {
                        label = new Label(xp, curRow, col.getColName(), columnTitleLeft);
                    }

                    if (sheet.getCell(xp, curRow) instanceof EmptyCell) {
                        sheet.addCell(label);
                    }

                    if (col.getRowSpan() > 1) {
                        for (int l = 1; l < col.getRowSpan(); l++) {
                            sheet.addCell(new Blank(xp, curRow + l, columnTitleLeft));
                        }
                        sheet.mergeCells(xp, curRow, xp, curRow + col.getRowSpan() - 1);
                    }
                }
                xp += col.getColSpan();
            }
        }

        //columnas titulos
        curRow++;
        Iterator<Column> colIt = table.getColumns().iterator();

        if (!rep.isMultiRowTitles()) {
            cv = new CellView();
            cv.setSize((15 + 2) * 20);
            sheet.setRowView(curRow, cv);
        } else if (rep.getMultiRowTitlesRows() != null) {
            cv = new CellView();
            cv.setSize(((15 + 2) * 20) * rep.getMultiRowTitlesRows());
            sheet.setRowView(curRow, cv);
        }

        for (int j = 1; colIt.hasNext(); j++) {
            Column column = colIt.next();
            Cell cell = sheet.getCell(j, curRow);
            if (cell instanceof EmptyCell) {
                sheet.addCell(new Label(j, curRow, column.getName(), table.getRotateTitleCols() ? columnTitleRot : columnTitleLeft));
            }
        }
        for (int i = 0; i < table.getData().length; i++) {
            Object[] dRow = table.getData()[i];
            curRow++;
            for (int k = 0; k < dRow.length; k++) {
                Column col = table.getColumns().get(k);
                Integer mapColor = table.coloredCells.get(i + "-" + k);
                int color = mapColor != null ? mapColor : COLOR_WHITE;
                List<WritableCellFormat> exFormats = colFormats.get(color);
                if (formats.get(col.getFormat()).getType() == LABEL) {
                    Object cur = dRow[k];
                    if (cur != null) {
                        if (cur instanceof byte[]) {
                            sheet.addCell(new Label(k + 1, curRow, new String((byte[]) cur, "ISO-8859-1"), exFormats.get(col.getFormat())));
                        } else {
                            sheet.addCell(new Label(k + 1, curRow, cur.toString(), exFormats.get(col.getFormat())));
                        }
                    } else {
                        sheet.addCell(new Blank(k + 1, curRow, exFormats.get(col.getFormat())));
                    }
                } else if (formats.get(col.getFormat()).getType() == NUMBER) {
                    Object cur = dRow[k];
                    if (cur != null) {
                        sheet.addCell(new jxl.write.Number(k + 1, curRow, Double.parseDouble(cur.toString()), exFormats.get(col.getFormat())));
                    } else {
                        sheet.addCell(new Blank(k + 1, curRow, exFormats.get(col.getFormat())));
                    }
                } else if (formats.get(col.getFormat()).getType() == DATE) {
                    Object cur = dRow[k];
                    if (cur != null) {
                        Date date;
                        if (cur instanceof XMLGregorianCalendar) {
                            date = Dates.XMLDateToDate((XMLGregorianCalendar) cur);
                        } else {
                            date = (Date) cur;
                        }
                        sheet.addCell(new DateTime(k + 1, curRow, date, exFormats.get(col.getFormat())));
                    } else {
                        sheet.addCell(new Blank(k + 1, curRow, exFormats.get(col.getFormat())));
                    }
                } else if (formats.get(col.getFormat()).getType() == ENUM) {
                    Object cur = dRow[k];
                    if (cur != null) {
                        sheet.addCell(new Label(k + 1, curRow, ((EnumCellFormat) exFormats.get(col.getFormat())).opts.get(cur.toString()), exFormats.get(col.getFormat())));
                    } else {
                        sheet.addCell(new Blank(k + 1, curRow, exFormats.get(col.getFormat())));
                    }
                }
            }
        }

        curRow = MySQLReportWriter.addTotalRow(rep, table, sheet, curRow, exFormatsSummary);
        return curRow++;
    }

    public static Map<Integer, List<WritableCellFormat>> getColoredFormats(MySQLReport rep) throws Exception {
        Map<Integer, List<WritableCellFormat>> colorFormats = new HashMap<>();
        colorFormats.put(COLOR_WHITE, getFormats(rep, Colour.WHITE, Colour.GRAY_50));
        colorFormats.put(COLOR_GREEN, getFormats(rep, Colour.BRIGHT_GREEN, Colour.GRAY_50));
        colorFormats.put(COLOR_RED, getFormats(rep, Colour.PINK2, Colour.GRAY_50));
        colorFormats.put(COLOR_YELLOW, getFormats(rep, Colour.YELLOW, Colour.GRAY_50));
        colorFormats.put(COLOR_GRAY, getFormats(rep, Colour.ORANGE, Colour.GRAY_50));
        colorFormats.put(COLOR_BLUE, getFormats(rep, Colour.RED, Colour.GRAY_50));
        return colorFormats;
    }

    public static void main(String[] args) throws Exception {
        Connection conn = MySQLCommon.getConnection("sigmads", null);

        MySQLReport rep1 = new MySQLReport("Empleados por Perfil", null, "Hoja1", MySQLQuery.now(conn));
        rep1.setMultiRowTitles(true);
        rep1.getFormats().add(new CellFormat(LABEL, LEFT));
        rep1.getFormats().add(new CellFormat(ENUM, LEFT, "h=Masculino&m=Femenino"));
        rep1.getFormats().add(new CellFormat(NUMBER, LEFT, "#0"));
        rep1.setShowNumbers(true);
        Object[][] profs = new MySQLQuery("SELECT id, name FROM profile ORDER BY name ASC").getRecords(conn);

        Table model = new Table("");
        model.getColumns().add(new Column("Documento", 30, 0));
        model.getColumns().add(new Column("Nombre", 30, 0));
        model.getColumns().add(new Column("Apellido", 30, 0));
        model.getColumns().add(new Column("Género", 20, 1));
        model.getColumns().add(new Column("Número Número Número Número Número", 20, 2));

        for (int i = 0; i < profs.length; i++) {
            Object[] objects = profs[i];
            Table tb = new Table(model);
            if (i == 0) {
                tb.getColumns().get(tb.getColumns().size() - 1).setName("Número");
            } else {
                tb.getColumns().get(tb.getColumns().size() - 1).setName("Número Número Número Número Número");
            }
            tb.setTitle(objects[1].toString());
            tb.setData(new MySQLQuery("SELECT document, first_name, last_name, IF(RAND() > 0.5, 'h', 'm'), IF(RAND() > 0.5, 0, 1) FROM employee INNER JOIN login ON login.employee_id = employee.id AND login.profile_id = " + objects[0]).getRecords(conn));
            rep1.getTables().add(tb);
        }
        //write(rep1, "vrc", conn);
    }

    static class EnumCellFormat extends WritableCellFormat {

        public HashMap<String, String> opts = new HashMap<>();

        public EnumCellFormat(String str) {
            super();
            String[] parts = str.split("&");
            for (String part : parts) {
                String[] mParts = part.split("=");
                opts.put(mParts[0].trim(), mParts[1].trim());
            }
        }
    }

    @Deprecated
    public static void write(MySQLReport rep, File file, Connection conn) throws Exception {
        MySQLReportWriter.write(new MySQLReport[]{rep}, file, conn);
    }

    @Deprecated
    public static void write(MySQLReport[] reports, String name, Connection conn) throws Exception {
        File file = Reports.createReportFile(name, "xls");
        MySQLReportWriter.write(reports, file, conn);
    }

    public static void write(MySQLReport[] reps, File path, Connection conn) throws Exception {
        List<MySQLReport> lReps = new ArrayList<>();
        for (MySQLReport rep : reps) {
            if (!rep.getTables().isEmpty()) {
                lReps.add(rep);
            }
        }

        if (lReps.isEmpty()) {
            throw new Exception("No se hallaron datos.");
        }

        WritableWorkbook workbook = Reports.getBaseAsStream(path, lReps.size());

        for (int r = 0; r < lReps.size(); r++) {
            MySQLReport rep = lReps.get(r);
            addNumberCol(rep);
            //quitar subitulos en blanco
            String[] subs = rep.getSubTitles().toArray(new String[0]);
            rep.getSubTitles().clear();
            for (String sub : subs) {
                if (!sub.trim().isEmpty()) {
                    rep.getSubTitles().add(sub);
                }
            }

            List<CellFormat> formats = rep.getFormats();
            //List<WritableCellFormat> exFormats = getFormats(rep, Colour.BRIGHT_GREEN, Colour.GRAY_50);
            Map<Integer, List<WritableCellFormat>> colorFormats = getColoredFormats(rep);

            WritableSheet sheet = workbook.getSheet(r);
            jxl.format.CellFormat tableTitle = sheet.getCell(0, 2).getCellFormat();
            jxl.format.CellFormat columnTitle = sheet.getCell(0, 1).getCellFormat();
            WritableCellFormat columnTitleLeft = new WritableCellFormat(columnTitle);
            WritableCellFormat columnTitleCent = new WritableCellFormat(columnTitle);
            WritableCellFormat columnTitleRot = new WritableCellFormat(columnTitle);
            columnTitleCent.setAlignment(Alignment.CENTRE);
            columnTitleCent.setWrap(true);
            columnTitleLeft.setWrap(true);
            columnTitleRot.setWrap(true);
            columnTitleRot.setOrientation(Orientation.PLUS_90);
            columnTitleRot.setVerticalAlignment(VerticalAlignment.BOTTOM);

            sheet.setName(rep.getSheetName());
            sheet.getSettings().setZoomFactor(rep.getZoomFactor());
            sheet.removeColumn(0);
            sheet.removeColumn(0);

            sheet.getSettings().setProtected(true);
            if (rep.getHorizontalFreeze() > 0) {
                sheet.getSettings().setHorizontalFreeze(rep.getHorizontalFreeze());
            }
            if (rep.getVerticalFreeze() > 0) {
                sheet.getSettings().setVerticalFreeze(rep.getVerticalFreeze());
            }

            CellView cv = new CellView();
            cv.setSize(5 * 256);
            sheet.setColumnView(0, cv);

            //determinar anchos de columnas
            Map<Integer, Integer> widths = new HashMap<>();
            for (int i = 0; i < rep.getTables().size(); i++) {
                List<Column> cols = rep.getTables().get(i).getColumns();
                for (int j = 0; j < cols.size(); j++) {
                    Column col = cols.get(j);
                    if (!widths.containsKey(j + 1)) {
                        widths.put(j + 1, col.getWidth());
                    }
                }
            }

            Iterator<Map.Entry<Integer, Integer>> it = widths.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, Integer> e = it.next();
                cv = new CellView();
                cv.setSize(e.getValue() * 256);
                sheet.setColumnView(e.getKey(), cv);
            }

            int row = 1;
            //logo
            addHeaderImage(rep, sheet, widths, conn);
            row++;
            //fin logo
            List<WritableCellFormat> exFormatsSummary = null;
            for (int i = 0; i < rep.getTables().size(); i++) {
                if (rep.getTables().get(i).getSummaryRow() != null) {
                    exFormatsSummary = getFormats(rep, Colour.ORANGE, Colour.BLACK);
                    break;
                }
            }

            for (int i = 0; i < rep.getTables().size(); i++) {
                row = addTable(rep, i, row, sheet, colorFormats, formats, tableTitle, columnTitleLeft, columnTitleCent, columnTitleRot, exFormatsSummary);
                row++;
            }
        }
        workbook.write();
        workbook.close();
    }
}
