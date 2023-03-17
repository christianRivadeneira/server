package printout;

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
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import printout.basics.MtoChkLst;
import printout.basics.MtoChkType;
import printout.basics.MtoChkVal;
import printout.basics.MtoChkVersion;
import printout.basics.cols.MtoChkCol;
import printout.basics.groups.MtoChkGrp;
import printout.basics.rows.MtoChkRow;
import utilities.MySQLQuery;
import utilities.pdf.PDFCellStyle;
import utilities.pdf.PDFFontsHelper;

public class PrintPreoFurgTipial extends PrintFormatsGenerator {

    private MtoChkLst lst;
    private MtoChkVersion ver;
    private MtoChkType type;
    private MtoChkGrp[] grps;
    private MtoChkCol[][] cols;
    private MtoChkRow[][] rows;
    private Object[][] vhData;
    private PDFCellStyle emptyStyle;
    private PDFCellStyle titleStyle;
    private PDFCellStyle cellStyle;
    private PDFCellStyle lefStyle;

    private final SimpleDateFormat dayF = new SimpleDateFormat("dd");
    private final SimpleDateFormat yearF = new SimpleDateFormat("yyyy");
    private final SimpleDateFormat MonthF = new SimpleDateFormat("MMMMM");
    private final SimpleDateFormat hourF = new SimpleDateFormat("hh:mm aa");
    private PDFCellStyle fontWithoutBorder;

    @Override
    public File initFormat(Connection ep, Integer registId) throws Exception {
        this.ep = ep;
        lst = new MtoChkLst().select(registId, ep);
        vhData = new MySQLQuery("SELECT "
                + "v.plate, "//0
                + "vt.`name`, "//1
                + "vc.`name`, "//2
                + "CONCAT(con.first_name, ' ', con.last_name), "//3
                + "e.`name`, "//4
                + "v.model "//5
                + "FROM vehicle AS v "
                + "INNER JOIN vehicle_type AS vt ON vt.id = v.vehicle_type_id "
                + "INNER JOIN vehicle_class AS vc ON vc.id = vt.vehicle_class_id "
                + "INNER JOIN agency AS a ON a.id = v.agency_id "
                + "INNER JOIN enterprise AS e ON e.id = a.enterprise_id "
                + "LEFT JOIN mto_contractor AS con ON con.id = v.contractor_id "
                + "WHERE v.id = " + lst.vhId).getRecords(ep);
        ver = new MtoChkVersion().select(lst.versionId, ep);
        type = new MtoChkType().select(ver.typeId, ep);
        grps = MtoChkGrp.getGrpsSubQ(lst.versionId, lst.vhId, ep);
        cols = new MtoChkCol[grps.length][];
        rows = new MtoChkRow[grps.length][];

        for (int i = 0; i < grps.length; i++) {
            cols[i] = MtoChkCol.getColsByGrp(new MySQLQuery(MtoChkCol.getQueryColByGrp(grps[i].id, ep)).getRecords(ep));
            rows[i] = MtoChkRow.getRowsByGrp(new MySQLQuery(MtoChkRow.getQueryRowByGrp(grps[i].id, ep)).getRecords(ep));
        }

        emptyStyle = new PDFCellStyle();
        emptyStyle.setBorders(false, false, false, false);
        emptyStyle.setBold(true);

        titleStyle = new PDFCellStyle();
        titleStyle.setAppearance(true, PDFCellStyle.DEFAULT_PADDING + 2, PDFCellStyle.WHITE, PDFCellStyle.BLACK);
        titleStyle.sethAlignment(PDFCellStyle.ALIGN_CENTER);
        titleStyle.setFontInfo(true, PDFCellStyle.BLACK, PDFCellStyle.DEFAULT_FONT_SIZE);

        lefStyle = titleStyle.copy();
        lefStyle.sethAlignment(PDFCellStyle.ALIGN_LEFT);

        cellStyle = new PDFCellStyle();
        cellStyle.setBorderColor(PDFCellStyle.BLACK);
        cellStyle.sethAlignment(PDFCellStyle.ALIGN_LEFT);
        cellStyle.setBackgroundColor(Color.WHITE);

        fontWithoutBorder = new PDFCellStyle();
        fontWithoutBorder.setFontSize(PDFCellStyle.DEFAULT_FONT_SIZE + 2);
        fontWithoutBorder.setBorder(false);

        return generateReport();
    }

    private void beginDocument(OutputStream os) throws Exception {
        document = new Document(new Rectangle(8.5f * 70f, 11f * 72f), 25f, 25f, 40f, 20f);
        fin = File.createTempFile("rpt_chklst", ".pdf");
        PdfWriter writer = PdfWriter.getInstance(document, (os != null ? os : new FileOutputStream(fin)));
        writer.setBoxSize("art", new Rectangle(36, 54, 559, 788));
        writer.setPageEvent(new HeaderFooters());
        document.open();
    }

    private File endDocument() throws Exception {
        document.close();
        return fin;
    }

    public File generateReport() throws Exception {
        try {
            beginDocument(os);
            document.add(getHeader());
            document.add(getParagraph());

            MtoChkVal[][] ans = MtoChkVal.getAnswers(rows, lst.id, ep);
            for (int i = 0; i < grps.length; i++) {
                MtoChkGrp grp = grps[i];
                MtoChkCol[] gCols = cols[i];
                MtoChkRow[] gRows = rows[i];
                float[] widths = new float[1 + gCols.length];
                widths[0] = 10;
                PdfPTable tabHeader = new PdfPTable(1 + gCols.length);
                tabHeader.setWidthPercentage(90);

                tabHeader.addCell(titleStyle.getCell("Verificación del estado".toUpperCase()));
                tabHeader.addCell(titleStyle.getCell("estado".toUpperCase(), gCols.length, 1));

                tabHeader.addCell(titleStyle.getCell(grp.name.toUpperCase()));
                for (int j = 0; j < gCols.length; j++) {
                    tabHeader.addCell(titleStyle.getCell(gCols[j].shortName, PDFCellStyle.ALIGN_CENTER));
                    widths[j + 1] = 3;
                }
                tabHeader.setWidths(widths);
                tabHeader.setSpacingBefore(15);
                document.add(tabHeader);

                PdfPTable tab = new PdfPTable(1 + gCols.length);
                tab.setWidths(widths);
                tab.setWidthPercentage(90);
                for (int j = 0; j < gRows.length; j++) {
                    MtoChkRow gRow = gRows[j];
                    MtoChkVal val = ans[i][j];
                    if (gRow.type.equals("nor")) {
                        tab.addCell(cellStyle.getCell(gRow.name.toUpperCase()));
                        for (MtoChkCol gCol : gCols) {
                            if (val != null && val.colId != null) {
                                tab.addCell(val.colId.equals(gCol.id) ? getOptOn(this) : getOptOff(this));
                            } else {
                                tab.addCell(getOptOff(this));
                            }
                        }
                    } else if (gRow.type.equals("tit")) {
                        tab.addCell(lefStyle.getCell(gRow.name.toUpperCase(), gCols.length + 1, 1, PDFCellStyle.ALIGN_CENTER));
                    } else if (gRow.type.equals("num") || gRow.type.equals("txt")) {
                        tab.addCell(cellStyle.getCell(gRow.name.toUpperCase()));
                        tab.addCell(cellStyle.getCell((val != null && val.val != null ? val.val : ""), gCols.length, 1));
                    }
                }
                document.add(tab);
            }
            getTblFoot();
            return endDocument();
        } catch (Exception ex) {
            Logger.getLogger(PrintFormatPreoTipial.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    class HeaderFooters extends PdfPageEventHelper {

        PdfTemplate total;

        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            try {
                PdfContentByte cb = writer.getDirectContent();

                //Encabezado
                cb.beginText();
                String text = "Página " + writer.getPageNumber() + " de ";
                cb.setFontAndSize(PDFFontsHelper.getRegular(), 9);
                float y = document.getPageSize().getHeight() - 27;
                cb.moveText(15, y);
                cb.showText(text);
                cb.endText();
                cb.addTemplate(total, 15 + PDFFontsHelper.getRegular().getWidthPoint(text, 9), y);

                if (type != null && type.sgcCode != null) {
                    //Footer
                    cb.beginText();
                    String text2 = "Código SGC: " + type.sgcCode;
                    cb.setFontAndSize(PDFFontsHelper.getRegular(), 7);
                    float len = PDFFontsHelper.getRegular().getWidthPoint(text2, 7);
                    float y2 = document.getPageSize().getHeight() - (document.getPageSize().getHeight() - 20);
                    float x2 = document.getPageSize().getWidth() - 20 - len;
                    cb.showTextAligned(Element.ALIGN_RIGHT, text2, x2 + PDFFontsHelper.getRegular().getWidthPoint(text2, 7), y2, 0f);
                    cb.endText();
                }
            } catch (Exception ex) {
                Logger.getLogger(PrintFormatGral.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void onOpenDocument(PdfWriter writer, Document dcmnt) {
            total = writer.getDirectContent().createTemplate(100, 100);
            total.setBoundingBox(new Rectangle(-20, -20, 100, 100));
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            try {
                document.add(getHeader());
                total.beginText();
                total.setFontAndSize(PDFFontsHelper.getRegular(), 9);
                total.setTextMatrix(0, 0);
                total.showText(String.valueOf(writer.getPageNumber() - 1));
                total.endText();
            } catch (Exception ex) {
                Logger.getLogger(PrintPreoFurgTipial.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    protected static PdfPCell getOptOn(Object caller) throws Exception {
        return getImgCell("chks", caller);
    }

    protected static PdfPCell getOptOff(Object caller) throws Exception {
        return getImgCell("blank", caller);
    }

    private static PdfPCell getImgCell(String img, Object caller) throws Exception {
        Image optImg = Image.getInstance(caller.getClass().getResource("/forms/maintenance/panels/checklist/" + img + ".wmf"));
        optImg.scaleAbsolute(6, 6);
        PdfPCell c = new PdfPCell(optImg);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setBorderColor(Color.BLACK);
        c.setPadding(3);
        return c;
    }

    private PdfPTable getHeader() throws Exception {
        PdfPTable tbl = new PdfPTable(1);
        tbl.setWidthPercentage(90);
        PdfPCell imgCell = cellStyle.getCell("");
        Image img = Image.getInstance(PrintPreoFurgTipial.class.getResource("/icons/tipial/logo_min.jpg"));
        img.setAlignment(PDFCellStyle.ALIGN_CENTER);
        img.scaleToFit(390, 80);
        imgCell.addElement(img);
        tbl.addCell(imgCell);
        tbl.addCell(titleStyle.getCell("RESOLUCIÓN 315 DE 2013"));
        tbl.addCell(titleStyle.getCell("(FEBRERO 6)"));

        return tbl;
    }

    private PdfPTable getParagraph() throws Exception {
        PdfPTable tbl = new PdfPTable(1);
        tbl.setSpacingBefore(15);
        tbl.setWidthPercentage(90);

        tbl.addCell(fontWithoutBorder.getCell("En _______________________ "
                + "a los " + dayF.format(lst.dt) + " dias "
                + "del mes de " + MonthF.format(lst.dt) + " "
                + "del año " + yearF.format(lst.dt) + " "
                + "siendo las " + hourF.format(lst.dt) + " "
                + "se procede a realizar el PROTOCOLO DE ALISTAMIENTO al Vehículo "
                + "Clase: " + (vhData[0][2] != null ? vhData[0][2].toString() : "_______________________") + ", "
                + "Marca: " + (vhData[0][1] != null ? vhData[0][1].toString() : "_______________________") + ", "
                + "Placa: " + (vhData[0][0] != null ? vhData[0][0].toString() : "_______________________") + ", "
                + "Modelo: " + (vhData[0][5] != null ? vhData[0][5].toString() : "_______________________") + ", "
                + "Empresa a la que pertenece _______________________", Element.ALIGN_JUSTIFIED));

        return tbl;
    }

    private void getTblFoot() throws Exception {
        String note = "El incumplimiento a la RESOLUCIÓN 315 DE FEBRERO 6 DEL 2013 ARTICULO 4 PROTOCOLO DE ALISTAMIENTO con su parágrafo. El alistamiento lo realizara la empresa con personal diferente de sus conductores pero con la participación del conductor del vehículo a ser despachado. Del proceso de alistamiento y de las personas que participaron en el mismo, así como de su relación con la empresa, se dejara constancia en la planilla de viaje ocasional, planilla de despacho o extracto de contrato según el caso";
        PdfPTable tbl = new PdfPTable(1);
        tbl.setSpacingBefore(15);
        tbl.setWidthPercentage(90);
        tbl.addCell(fontWithoutBorder.getCell(note, Element.ALIGN_JUSTIFIED));

        PdfPTable tblFirma = new PdfPTable(2);
        tblFirma.setWidthPercentage(90);
        tblFirma.setSpacingBefore(55);

        fontWithoutBorder.setBold(true);
        tblFirma.addCell(fontWithoutBorder.getCell("Firma del Conductor", PDFCellStyle.ALIGN_CENTER));
        tblFirma.addCell(fontWithoutBorder.getCell("Firma quien realiza el protocolo de alistamiento", PDFCellStyle.ALIGN_CENTER));

        document.add(tbl);
        document.add(tblFirma);
    }
}
