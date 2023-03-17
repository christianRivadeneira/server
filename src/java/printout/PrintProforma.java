package printout;

import api.mto.model.MtoCfg;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
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

public class PrintProforma extends PrintFormatsGenerator {

    private File fin;
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
    private Image respSign;

    @Override
    public File initFormat(Connection ep, Integer registId) throws Exception {
        this.ep = ep;
        lst = new MtoChkLst().select(registId, ep);
        vhData = new MySQLQuery("SELECT "
                + "v.plate, "
                + "vt.`name`, "
                + "vc.`name`, "
                + "CONCAT(con.first_name, ' ', con.last_name), "
                + "e.`name` "
                + "FROM vehicle AS v "
                + "INNER JOIN vehicle_type AS vt ON vt.id = v.vehicle_type_id "
                + "INNER JOIN vehicle_class AS vc ON vc.id = vt.vehicle_class_id "
                + "INNER JOIN agency AS a ON a.id = v.agency_id "
                + "INNER JOIN enterprise AS e ON e.id = a.enterprise_id "
                + "LEFT JOIN mto_contractor AS con ON con.id = v.contractor_id "
                + "WHERE v.id = " + lst.vhId).getRecords(ep);
        ver = new MtoChkVersion().select(lst.versionId, ep);
        type = new MtoChkType().select(ver.typeId, ep);
        grps = MtoChkGrp.getGrpsByVersion(ver.id, ep);
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
        PDFCellStyle boldStyle = cellStyle.copy();
        boldStyle.setBold(true);

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
        return generateReport(null);
    }

    public File generateReport(OutputStream os) throws Exception {
        try {
            beginDocument(os);
            document.add(getHeader());
            document.add(getTblBasics());
            MtoChkVal[][] ans = MtoChkVal.getAnswers(rows, lst.id, ep);

            for (int i = 0; i < grps.length; i++) {
                MtoChkGrp grp = grps[i];
                MtoChkCol[] gCols = cols[i];
                MtoChkRow[] gRows = rows[i];
                float[] widths = new float[4 + gCols.length];
                widths[0] = 10;
                PdfPTable tabHeader = new PdfPTable(4 + gCols.length);
                tabHeader.setWidthPercentage(100);
                tabHeader.addCell(emptyStyle.getCell(grp.name.toUpperCase(), 4 + gCols.length, 1, PDFCellStyle.ALIGN_CENTER));
                tabHeader.addCell(titleStyle.getCell("DESCRIPCIÓN"));
                for (int j = 0; j < gCols.length; j++) {
                    tabHeader.addCell(titleStyle.getCell(gCols[j].shortName, PDFCellStyle.ALIGN_CENTER));
                    widths[j + 1] = 3;
                }
                tabHeader.addCell(titleStyle.getCell("CORRECTIVO"));
                widths[gCols.length + 1] = 10;
                tabHeader.addCell(titleStyle.getCell("FECHA"));
                widths[gCols.length + 2] = 10;
                tabHeader.addCell(titleStyle.getCell("TALLER"));
                widths[gCols.length + 3] = 10;
                tabHeader.setWidths(widths);
                tabHeader.setSpacingBefore(10);
                document.add(tabHeader);

                PdfPTable tab = new PdfPTable(4 + gCols.length);
                tab.setWidths(widths);
                tab.setWidthPercentage(100);
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
                    if (val != null) {
                        if (val.workOrderId != null) {
                            Object[][] corrData = new MySQLQuery("SELECT wo.description, wo.`begin`, pro.`name` "
                                    + "FROM work_order AS wo "
                                    + "INNER JOIN prov_provider AS pro ON pro.id = wo.provider_id "
                                    + "WHERE wo.id = " + val.workOrderId).getRecords(ep);
                            if (corrData.length > 0) {
                                Object[] corrRow = corrData[0];
                                tab.addCell(cellStyle.getCell(corrRow[0] != null ? corrRow[0].toString() : ""));
                                tab.addCell(cellStyle.getCell(corrRow[1] != null ? dateFormat.format(corrRow[1]) : ""));
                                tab.addCell(cellStyle.getCell(corrRow[2] != null ? corrRow[2].toString() : ""));
                            } else {
                                tab.addCell(cellStyle.getCell(""));
                                tab.addCell(cellStyle.getCell(""));
                                tab.addCell(cellStyle.getCell(""));
                            }
                        } else {
                            tab.addCell(cellStyle.getCell(val.correction != null ? val.correction : ""));
                            tab.addCell(cellStyle.getCell(val.corrDate != null ? dateFormat.format(val.corrDate) : ""));
                            tab.addCell(cellStyle.getCell(val.corrProv != null ? val.corrProv : ""));
                        }
                    } else {
                        tab.addCell(cellStyle.getCell(""));
                        tab.addCell(cellStyle.getCell(""));
                        tab.addCell(cellStyle.getCell(""));
                    }
                }
                tab.setSpacingBefore(10);
                document.add(tab);
            }
            getTblFoot();
            return endDocument();
        } catch (Exception ex) {
            Logger.getLogger(PrintProforma.class.getName()).log(Level.SEVERE, null, ex);
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
                Logger.getLogger(PrintProforma.class.getName()).log(Level.SEVERE, null, ex);
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
        tbl.setSpacingBefore(15);
        tbl.setWidthPercentage(100);

        PDFCellStyle font = new PDFCellStyle();
        font.setBold(true);
        font.setFontSize(PDFCellStyle.DEFAULT_FONT_SIZE + 2);

        PDFCellStyle sFont = font.copy();
        sFont.setFontSize(PDFCellStyle.DEFAULT_FONT_SIZE);
        font.setBorder(false);
        sFont.setBorder(false);

        tbl.addCell(font.getCell("PROFORMA 3\nMINISTERIO DE TRANSPORTE", PDFCellStyle.ALIGN_CENTER));
        tbl.addCell(sFont.getCell("DIRECCIÓN TERRITORIAL DE NARIÑO\nFICHA TÉCNICA DE REVISIÓN Y MANTENIMIENTO VEHÍCULOS DE PASAJEROS", PDFCellStyle.ALIGN_CENTER));
        tbl.setSpacingBefore(10);
        return tbl;
    }

    private PdfPTable getTblBasics() throws Exception {
        PdfPTable tbl = new PdfPTable(6);
        tbl.setSpacingBefore(15);
        tbl.setWidthPercentage(100);
        tbl.setHeaderRows(2);

        tbl.addCell(lefStyle.getCell("PLACA: ", 1, 1));
        tbl.addCell(cellStyle.getCell((vhData[0][0] != null ? vhData[0][0].toString() : ""), 1, 1));

        tbl.addCell(lefStyle.getCell("MARCA: ", 1, 1));
        tbl.addCell(cellStyle.getCell((vhData[0][1] != null ? vhData[0][1].toString() : ""), 1, 1));

        tbl.addCell(lefStyle.getCell("CLASE: ", 1, 1));
        tbl.addCell(cellStyle.getCell((vhData[0][2] != null ? vhData[0][2].toString() : ""), 1, 1));

        tbl.addCell(lefStyle.getCell("PROPIETARIO: ", 1, 1));
        tbl.addCell(cellStyle.getCell((vhData[0][3] != null ? vhData[0][3].toString() : ""), 2, 1));

        tbl.addCell(lefStyle.getCell("EMPRESA: ", 1, 1));
        tbl.addCell(cellStyle.getCell((vhData[0][4] != null ? vhData[0][4].toString() : ""), 2, 1));

        tbl.addCell(lefStyle.getCell("FECHA: ", 1, 1));
        tbl.addCell(cellStyle.getCell(dateFormat.format(lst.dt), 2, 1));

        tbl.addCell(lefStyle.getCell("KILOMETRAJE: ", 1, 1));
        tbl.addCell(cellStyle.getCell((lst.mileage != null ? lst.mileage : "") + "", 2, 1));
        return tbl;
    }

    private void getTblFoot() throws Exception {
        Integer bfileId = new MySQLQuery("SELECT id FROM bfile WHERE owner_id = 8 AND owner_type = " + MtoChkLst.SYS_ICONS + " ORDER BY updated DESC LIMIT 1").getAsInteger(ep);

        if (bfileId != null) {
            respSign = setSignatures(bfileId);
        }

        Paragraph p = new Paragraph();
        p.setFont(emptyStyle.getFont());
        p.add(Chunk.NEWLINE);
        p.add("RESPONSABLE DEL ANÁLISIS Y CORRECTIVOS");
        document.add(p);
        MtoCfg cfg = new MtoCfg().select(1, ep);
        PdfPTable tbl = new PdfPTable(1);
        tbl.setSpacingBefore(8);
        tbl.setWidthPercentage(80);
        tbl.setHorizontalAlignment(Element.ALIGN_LEFT);
        tbl.setWidths(new float[]{25});
        PDFCellStyle emptStyle = emptyStyle.copy();
        PDFCellStyle empCellStyle = cellStyle.copy();
        empCellStyle.setBorder(false);
        emptStyle.sethAlignment(PDFCellStyle.ALIGN_LEFT);

        PdfPCell signatureCell;
        if (respSign != null && type.showSign) {
            respSign.scaleAbsolute(140, 60);
            signatureCell = new PdfPCell(respSign);
            signatureCell.setBorderColor(Color.WHITE);
            signatureCell.setPaddingBottom(-10);
            signatureCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            tbl.addCell(signatureCell);
        } else {
            for (int i = 0; i < 15; i++) {
                tbl.addCell(emptStyle.getCell("", 1, 1));
            }
        }
        //tbl.addCell(emptStyle.getCell("", 1, 1));
        tbl.addCell(emptStyle.getCell("___________________________________", 1, 1));
        //tbl.addCell(emptStyle.getCell("NOMBRE:", 1, 1));
        tbl.addCell(empCellStyle.getCell((cfg.bossLstName != null ? cfg.bossLstName : "").toUpperCase(), 1, 1));
        //tbl.addCell(emptStyle.getCell("CARGO :", 1, 1));
        tbl.addCell(empCellStyle.getCell((cfg.bossLstWork != null ? cfg.bossLstWork : "").toUpperCase(), 1, 1));
        //tbl.addCell(emptStyle.getCell("MATRÍCULA PROFESIONAL: ", 1, 1));
        tbl.addCell(empCellStyle.getCell((cfg.bossLstNumberTp != null ? cfg.bossLstNumberTp : ""), 1, 1));
        //tbl.addCell(emptStyle.getCell("FIRMA: ", 2, 1));
        document.add(tbl);
    }

    private Image setSignatures(Integer bfileId) throws Exception {
        InputStream is = MtoChkLst.readFileDirect(bfileId, ep);
        if (is != null) {
            try {
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] dataImage = new byte[16384];

                while ((nRead = is.read(dataImage, 0, dataImage.length)) != -1) {
                    buffer.write(dataImage, 0, nRead);
                }

                buffer.flush();
                return Image.getInstance(buffer.toByteArray());
            } catch (Exception ex) {
                return null;
            }
        } else {
            return null;
        }
    }
}
