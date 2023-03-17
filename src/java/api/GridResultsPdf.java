package api;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import printout.PrintCootranarPNC;
import utilities.MySQLQuery;
import utilities.pdf.PDFCellStyle;
import utilities.pdf.PDFFontsHelper;
import web.fileManager;
import web.fileManager.PathInfo;

public class GridResultsPdf {

    private final Document document;
    private final PDFCellStyle titleStyle;
    private final PDFCellStyle titleStyleCenter;
    private final PDFCellStyle titleDocStyleCenter;
    private final PDFCellStyle titleDocStyleCenter2;
    private final PDFCellStyle cellStyle;
    private final Color colorBackground;
    private final Color colorBorder;

    public GridResultsPdf(File file, Color colorBackground, Color colorBorder) throws Exception {
        this.colorBackground = colorBackground;
        this.colorBorder = colorBorder;
        document = new Document(new Rectangle(8.5f * 72f, 13f * 72f), 36f, 36f, 36f, 36f);

        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        HeaderFooterHV event = new HeaderFooterHV();
        writer.setBoxSize("art", new Rectangle(36, 54, 559, 788));
        writer.setPageEvent(event);

        titleStyle = new PDFCellStyle();
        titleStyle.setAppearance(true, 5, PDFCellStyle.WHITE, colorBorder);
        titleStyle.sethAlignment(PDFCellStyle.ALIGN_LEFT);
        titleStyle.setFontInfo(true, PDFCellStyle.BLACK, PDFCellStyle.DEFAULT_FONT_SIZE);
        titleStyle.setBorder(false);
        titleStyleCenter = titleStyle.copy();
        titleStyleCenter.sethAlignment(PDFCellStyle.ALIGN_CENTER);
        titleDocStyleCenter = titleStyleCenter.copy();
        titleDocStyleCenter.setFontSize(12f);
        titleDocStyleCenter.setBorder(false);
        titleDocStyleCenter2 = titleDocStyleCenter.copy();
        titleDocStyleCenter2.setFontSize(14f);
        cellStyle = new PDFCellStyle();
        cellStyle.setAppearance(true, 5, PDFCellStyle.WHITE, colorBorder);
        cellStyle.sethAlignment(PDFCellStyle.ALIGN_LEFT);
        cellStyle.setFontSize(PDFCellStyle.DEFAULT_FONT_SIZE - 1);
        cellStyle.setBorders(true, true, false, false);
        document.open();

    }

    public void addDocumentTitle(String title) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(MySQLCol.getFormat(MySQLCol.TYPE_DD_MM_YYYY_HH12_MM_SS_A));
        PdfPTable table = new PdfPTable(1);
        table.setWidths(new float[]{100});
        table.setWidthPercentage(100);

        table.addCell(titleDocStyleCenter2.getCell(title));
        titleStyleCenter.setBorder(false);
        titleStyleCenter.setBold(false);
        table.addCell(titleStyleCenter.getCell("Consultado el " + sdf.format(Calendar.getInstance().getTime())));
        titleStyleCenter.setBold(true);
        document.add(table);
    }

    public void addGrid(String title, GridResult r) throws Exception {
        int totalCol = r.cols.length;

        if ((r.cols == null || r.cols.length == 0) || (r.data == null || r.data.length == 0)) {

            PdfPTable table = new PdfPTable(1);
            table.setWidthPercentage(100);

            //titulo tabla
            table.addCell(titleDocStyleCenter.getCell(title, totalCol, 1));
            table.addCell(titleStyle.getCell("", totalCol, 1));

            cellStyle.setBackgroundColor(PDFCellStyle.WHITE);
            table.addCell(cellStyle.getCell("No se hallaron datos", PDFCellStyle.ALIGN_CENTER));
            table.setSpacingBefore(20f);
            document.add(table);
        } else {
            if (r.cols.length != r.data[0].length) {
                throw new Exception("El número de columnas no coincide con la data");
            }

            float[] widthCol = new float[totalCol];

            for (int i = 0; i < totalCol; i++) {
                widthCol[i] = r.cols[i].width;
            }

            PdfPTable table = new PdfPTable(totalCol);
            table.setWidths(widthCol);
            table.setWidthPercentage(100);

            //titulo tabla
            table.addCell(titleDocStyleCenter.getCell(title, totalCol, 1));
            table.addCell(titleStyle.getCell("", totalCol, 1));

            //titulos columnas
            for (MySQLCol row : r.cols) {
                table.addCell(titleStyle.getCell(row.name));
            }

            for (int i = 0; i < r.data.length; i++) {
                Object[] row = r.data[i];
                if (i % 2 == 0) {
                    cellStyle.setBackgroundColor(colorBackground);
                } else {
                    cellStyle.setBackgroundColor(PDFCellStyle.WHITE);
                }

                for (int j = 0; j < row.length; j++) {
                    Object rowCell = row[j];
                    MySQLCol rowCol = r.cols[j];
                    SimpleDateFormat sdf;
                    DecimalFormat df;
                    cellStyle.sethAlignment(PDFCellStyle.ALIGN_LEFT);

                    switch (rowCol.type) {
                        case MySQLCol.TYPE_DECIMAL_1:
                        case MySQLCol.TYPE_DECIMAL_2:
                        case MySQLCol.TYPE_DECIMAL_3:
                        case MySQLCol.TYPE_DECIMAL_4:
                            cellStyle.sethAlignment(PDFCellStyle.ALIGN_RIGHT);
                            df = new DecimalFormat(MySQLCol.getFormat(rowCol.type));
                            table.addCell(cellStyle.getCell(rowCell != null ? df.format(MySQLQuery.getAsBigDecimal(rowCell, false)) : null));
                            break;
                        case MySQLCol.TYPE_DD_MM_YYYY_HH12_MM_A:
                        case MySQLCol.TYPE_DD_MM_YYYY_HH12_MM_SS_A:
                        case MySQLCol.TYPE_DD_MM_YYYY:
                            cellStyle.sethAlignment(PDFCellStyle.ALIGN_RIGHT);
                            sdf = new SimpleDateFormat(MySQLCol.getFormat(rowCol.type));
                            table.addCell(cellStyle.getCell(rowCell != null ? sdf.format(MySQLQuery.getAsDate(rowCell)) : null));
                            break;
                        case MySQLCol.TYPE_ENUM:
                            table.addCell(cellStyle.getCell(rowCell != null ? new EnumCellPdf(rowCol.enumOpts).map.get(rowCell) : null));
                            break;
                        case MySQLCol.TYPE_BOOLEAN:
                            table.addCell(cellStyle.getCell(rowCell != null ? (MySQLQuery.getAsBoolean(rowCell) ? "Si" : "No") : null));
                            break;
                        default:
                            table.addCell(cellStyle.getCell(rowCell != null ? rowCell.toString() : null));
                            break;
                    }
                }
            }
            table.setSpacingBefore(20f);
            document.add(table);
        }
    }

    public void addVerticalGrid(String title, GridResult r) throws Exception {
        int totalCol = r.cols.length;

        if ((r.cols == null || r.cols.length == 0) || (r.data == null || r.data.length == 0)) {
            return;
        }

        if (r.cols.length != r.data[0].length) {
            throw new Exception("El número de columnas no coincide con la data");
        }

        /*   float[] widthCol = new float[totalCol];

        for (int i = 0; i < totalCol; i++) {
            widthCol[i] = r.cols[i].width;
        }*/
        PdfPTable table = new PdfPTable(r.data.length + 1);
        //  table.setWidths(widthCol);
        table.setWidthPercentage(100);

        //titulo tabla
        table.addCell(titleDocStyleCenter.getCell(title, totalCol, 1));
        table.addCell(titleStyle.getCell("", totalCol, 1));

        for (int i = 0; i < r.cols.length; i++) {
            if (i % 2 == 0) {
                cellStyle.setBackgroundColor(colorBackground);
            } else {
                cellStyle.setBackgroundColor(PDFCellStyle.WHITE);
            }
            cellStyle.setBold(true);
            table.addCell(cellStyle.getCell(r.cols[i].name));
            cellStyle.setBold(false);
            MySQLCol rowCol = r.cols[i];

            SimpleDateFormat sdf = null;
            Map<String, String> enumOpts = null;
            switch (rowCol.type) {
                case MySQLCol.TYPE_DD_MM_YYYY_HH12_MM_SS_A:
                case MySQLCol.TYPE_DD_MM_YYYY:
                    sdf = new SimpleDateFormat(MySQLCol.getFormat(rowCol.type));
                    break;
                case MySQLCol.TYPE_ENUM:
                    enumOpts = new EnumCellPdf(rowCol.enumOpts).map;
                    break;
            }

            for (Object[] data : r.data) {
                Object rowCell = data[i];

                switch (rowCol.type) {
                    case MySQLCol.TYPE_DD_MM_YYYY_HH12_MM_SS_A:
                        table.addCell(cellStyle.getCell(rowCell != null ? sdf.format(MySQLQuery.getAsDate(rowCell)) : null));
                        break;
                    case MySQLCol.TYPE_DD_MM_YYYY:
                        table.addCell(cellStyle.getCell(rowCell != null ? sdf.format(MySQLQuery.getAsDate(rowCell)) : null));
                        break;
                    case MySQLCol.TYPE_ENUM:
                        String label = enumOpts.get(rowCell);
                        table.addCell(cellStyle.getCell(rowCell != null ? label == null ? rowCell : label : null));
                        break;
                    case MySQLCol.TYPE_BOOLEAN:
                        String boolLabel = "";
                        if (rowCell != null) {
                            boolLabel = MySQLQuery.getAsBoolean(rowCell) ? "Si" : "No";
                        }
                        table.addCell(cellStyle.getCell(rowCell != null ? boolLabel : null));
                        break;
                    default:
                        table.addCell(cellStyle.getCell(rowCell != null ? rowCell.toString() : null));
                        break;
                }
            }
        }
        table.setSpacingBefore(20f);
        document.add(table);
    }

    static class EnumCellPdf {

        private final Map<String, String> map;

        public EnumCellPdf(String[][] map) {
            this.map = new HashMap<>();
            for (String[] opt : map) {
                this.map.put(opt[0], opt[1]);
            }
        }
    }

    public void addPhothos(Connection conn, Integer[] ownerId, Integer[] ownerType) throws Exception {
        PathInfo pInfo = new fileManager.PathInfo(conn);

        String ids = "";

        for (int i = 0; i < ownerId.length; i++) {
            String idsOwner = new MySQLQuery("SELECT GROUP_CONCAT(id) FROM bfile WHERE owner_id = " + ownerId[i]
                    + " AND owner_type = " + ownerType[i] + " AND (file_name LIKE '%.png' OR file_name LIKE '%.jpg' OR file_name LIKE '%.jpge' OR file_name LIKE '%.tif') ORDER BY created ASC").getAsString(conn);

            if (idsOwner != null) {
                ids += (ids.length() > 0 ? ("," + idsOwner) : idsOwner);
            }
        }

        PdfPTable tabAdj = new PdfPTable(2);
        tabAdj.setWidthPercentage(100);
        cellStyle.setBackgroundColor(colorBackground);
        cellStyle.setBorders(true, true, true, true);
        PDFCellStyle noBorderStyle = cellStyle.copy();
        noBorderStyle.setAppearance(false, 0, Color.WHITE, Color.WHITE);
        noBorderStyle.sethAlignment(Element.ALIGN_CENTER);
        noBorderStyle.setvAlignment(Element.ALIGN_TOP);

        noBorderStyle.setPaddings(0, 5, 5, 5);

        if (!ids.isEmpty()) {
            document.newPage();
            String[] photosId = ids.split(",");
            for (int i = 0; i < photosId.length; i++) {
                int photoId = Integer.valueOf(photosId[i]);
                Image img = Image.getInstance(getFile(photoId, pInfo));
                img.scalePercent(20);
                img.setWidthPercentage(100);

                PdfPCell imgCell = new PdfPCell(img, false);
                imgCell.setPadding(5);
                img.scaleToFit(80, 80);
                imgCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                imgCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                imgCell.setBorderColor(cellStyle.getBorderColor());

                PdfPTable child = new PdfPTable(1);
                child.addCell(cellStyle.getCell("Evidencia " + (i + 1)));
                child.addCell(imgCell);
                tabAdj.addCell(noBorderStyle.getCell(child, 1, 1));
            }

            if (photosId.length % 2 != 0) {
                tabAdj.addCell(noBorderStyle.getCell("", 1, 1));
            }
            tabAdj.setSpacingBefore(20f);
            document.add(tabAdj);
        }
    }

    public void addPhotho(Connection conn, Integer ownerId, Integer ownerType) throws Exception {
        PathInfo pInfo = new fileManager.PathInfo(conn);

        Integer id = new MySQLQuery("SELECT id FROM bfile WHERE owner_id = " + ownerId
                + " AND owner_type = " + ownerType + " ORDER BY created ASC").getAsInteger(conn);
        PdfPTable tabAdj = new PdfPTable(1);
        tabAdj.setWidthPercentage(100);

        if (id != null) {
            Image img = Image.getInstance(getFile(id, pInfo));
            img.scalePercent(80);
            img.setWidthPercentage(100);

            PdfPCell imgCell = new PdfPCell(img, false);
            img.scaleToFit(80, 80);
            imgCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            imgCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            imgCell.setBorderWidth(0f);

            tabAdj.addCell(imgCell);
            tabAdj.setSpacingAfter(-20f);
            document.add(tabAdj);
        }
    }

    public void close() {
        document.close();
    }

    private byte[] getFile(Integer id, PathInfo pInfo) throws Exception {
        File f = pInfo.getExistingFile(id);
        FileInputStream fis = new FileInputStream(f);
        return IOUtils.toByteArray(fis);

    }
}

class HeaderFooterHV extends PdfPageEventHelper {

    PdfTemplate total;

    @Override
    public void onOpenDocument(PdfWriter writer, Document dcmnt) {
        total = writer.getDirectContent().createTemplate(100, 100);
        total.setBoundingBox(new Rectangle(-20, -20, 100, 100));
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        try {
            PdfContentByte cb = writer.getDirectContent();
            cb.saveState();
            String text = "Página " + writer.getPageNumber() + " de ";
            cb.saveState();
            cb.setFontAndSize(PDFFontsHelper.getRegular(), PDFCellStyle.DEFAULT_FONT_SIZE - 2);
            cb.beginText();
            float y = document.getPageSize().getLeft() + 27;
            cb.moveText(15, y);
            cb.showText(text);
            cb.endText();
            cb.restoreState();
            cb.addTemplate(total, 15 + PDFFontsHelper.getRegular().getWidthPoint(text, PDFCellStyle.DEFAULT_FONT_SIZE - 2), y);
            cb.restoreState();
        } catch (Exception ex) {
        }
    }

    @Override
    public void onCloseDocument(PdfWriter writer, Document document) {
        try {
            float fontSize = PDFCellStyle.DEFAULT_FONT_SIZE - 2;
            total.beginText();
            total.setFontAndSize(PDFFontsHelper.getRegular(), fontSize);
            total.setTextMatrix(0, 0);
            total.showText(String.valueOf(writer.getPageNumber() - 1));
            total.endText();
        } catch (Exception ex) {
            Logger.getLogger(PrintCootranarPNC.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
