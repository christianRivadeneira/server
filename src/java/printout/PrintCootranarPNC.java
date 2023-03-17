package printout;

import printout.basics.MtoChkType;
import printout.basics.MtoChkVersion;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.BaseFont;
import java.sql.Connection;
import printout.basics.rows.MtoChkRow;
import printout.basics.groups.MtoChkGrp;
import printout.basics.cols.MtoChkCol;
import utilities.pdf.PDFCellStyle;
import printout.basics.MtoChkLst;
import printout.basics.MtoChkVal;
import java.io.File;
import java.io.FileOutputStream;
import utilities.MySQLQuery;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import java.awt.Color;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilities.pdf.PDFFontsHelper;
import web.enterpriseLogo;

public class PrintCootranarPNC extends PrintFormatsGenerator {

    private File fin;
    private HeaderFooter event;
    private MtoChkLst lst;
    private MtoChkVersion ver;
    private MtoChkType type;
    private MtoChkGrp[] grps;
    private MtoChkCol[][] cols;
    private MtoChkRow[][] rows;
    private PDFCellStyle titleStyle;
    private PDFCellStyle cellStyle;
    private MtoChkVal[][] ans;

    private static final int MAX_LINES_DESCRIPTION = 20;
    private static final int MAX_LINE_DESCRIPTION = 170;

    private final Ansswer QUIEN_DETECTO_EL_PNC = new Ansswer(131, 862, 316);

    private final Ansswer CORREGIDO_ANTES_DE_SALIR = new Ansswer(132, 863, 317);

    private final Ansswer DATOS_DE_CORRECCION = new Ansswer(null, 861, 316);

    @Override
    public File initFormat(Connection ep, Integer registId) throws Exception {
        this.ep = ep;
        lst = new MtoChkLst().select(registId, ep);
        ver = new MtoChkVersion().select(lst.versionId, ep);
        type = new MtoChkType().select(ver.typeId, ep);
        grps = MtoChkGrp.getGrpsSubQ(lst.versionId, lst.vhId, ep);
        cols = new MtoChkCol[grps.length][];
        rows = new MtoChkRow[grps.length][];
        for (int i = 0; i < grps.length; i++) {
            cols[i] = MtoChkCol.getColsByGrp(new MySQLQuery(MtoChkCol.getQueryColByGrp(grps[i].id, ep)).getRecords(ep));
            rows[i] = MtoChkRow.getRowsByGrp(new MySQLQuery(MtoChkRow.getQueryRowByGrp(grps[i].id, ep)).getRecords(ep));
        }
        titleStyle = new PDFCellStyle();
        titleStyle.setAppearance(true, PDFCellStyle.DEFAULT_PADDING, PDFCellStyle.GRAY_BACKGROUND, PDFCellStyle.GRAY_BORDER);
        titleStyle.sethAlignment(PDFCellStyle.ALIGN_LEFT);
        titleStyle.setFontInfo(false, PDFCellStyle.BLACK, PDFCellStyle.DEFAULT_FONT_SIZE);
        cellStyle = new PDFCellStyle();
        cellStyle.setAppearance(true, PDFCellStyle.DEFAULT_PADDING, PDFCellStyle.WHITE, PDFCellStyle.GRAY_BORDER);
        cellStyle.sethAlignment(PDFCellStyle.ALIGN_LEFT);
        cellStyle.setFontSize(PDFCellStyle.DEFAULT_FONT_SIZE - 1);
        ans = MtoChkVal.getAnswers(rows, lst.id, ep);

        return generateReport();
    }

    private void beginDocument() throws Exception {
        document = new Document(PageSize.A4, 30f, 30f, 40f, 30f);
        fin = File.createTempFile("formatos", ".pdf");
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fin));
        event = new HeaderFooter();
        writer.setBoxSize("art", new Rectangle(36, 54, 559, 788));
        writer.setPageEvent(event);
        document.open();
    }

    private File endDocument() throws Exception {
        document.close();
        return fin;
    }

    public File generateReport() throws Exception {
        try {
            beginDocument();
            Boolean entIcon = new MySQLQuery("SELECT e.alternative FROM vehicle AS veh INNER JOIN agency AS a ON a.id = veh.agency_id INNER JOIN enterprise AS e ON e.id = a.enterprise_id WHERE veh.id = " + lst.vhId).getAsBoolean(ep);
            PdfPCell imgCell;
            try {
                Image img = entIcon ? Image.getInstance(Files.readAllBytes(enterpriseLogo.getEnterpriseLogo(6 + "", ep).toPath())) : Image.getInstance(Files.readAllBytes(enterpriseLogo.getEnterpriseLogo(4 + "", ep).toPath()));
                img.setAlignment(Element.ALIGN_CENTER);
                img.scaleToFit(80, 80);
                imgCell = new PdfPCell(img);
            } catch (Exception ex) {
                imgCell = new PdfPCell();
            }
            imgCell.setPadding(5);
            imgCell.setBorderColor(cellStyle.getBorderColor());
            imgCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            PdfPTable tab = new PdfPTable(2);
            tab.setWidths(new float[]{30, 70});
            tab.setWidthPercentage(100);
            tab.addCell(imgCell);
            PDFCellStyle nobackgroundCell = titleStyle.copy();
            nobackgroundCell.setBackgroundColor(cellStyle.getBackgrounColor());
            tab.addCell(nobackgroundCell.getCell(type.name.toUpperCase(), PDFCellStyle.ALIGN_CENTER));
            tab.setSpacingAfter(10);
            document.add(tab);

            tab = new PdfPTable(4);
            tab.setWidthPercentage(100);
            tab.setWidths(new int[]{20, 20, 30, 30});
            tab.addCell(titleStyle.getCell("FECHA:", Element.ALIGN_LEFT));
            tab.addCell(cellStyle.getCell(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss a").format(lst.dt), Element.ALIGN_LEFT));
            tab.addCell(titleStyle.getCell("NOMBRE QUIEN DETECTO EL PNC:", Element.ALIGN_LEFT));
            String detect = (String) getAnsswer(QUIEN_DETECTO_EL_PNC);
            detect = (detect != null ? detect : "") + "\n ";
            tab.addCell(cellStyle.getCell(detect.toUpperCase(), Element.ALIGN_LEFT));
            tab.setSpacingAfter(10);
            document.add(tab);

            tab = new PdfPTable(4);
            tab.setWidthPercentage(100);
            tab.setWidths(new int[]{40, 30, 10, 20});
            tab.addCell(titleStyle.getCell("PERSONA DESIGNADA PARA EL TRATAMIENTO DEL PRODUCTO NO CONFORME", Element.ALIGN_LEFT));
            tab.addCell(cellStyle.getCell(lst.responName != null ? lst.responName.toUpperCase() : ""));
            tab.addCell(titleStyle.getCell("CARGO:", Element.ALIGN_LEFT));
            tab.addCell(cellStyle.getCell(lst.responJob != null ? lst.responJob.toUpperCase() : ""));
            tab.setSpacingAfter(10);
            document.add(tab);

            tab = new PdfPTable(5);
            tab.setWidthPercentage(100);
            tab.setWidths(new int[]{60, 10, 10, 10, 10});
            tab.addCell(titleStyle.getCell("DESCRIPCIÓN DEL PRODUCTO NO CONFORME\n ", 5, 1, Element.ALIGN_CENTER));
            addRowsText(tab, 5);
            tab.addCell(titleStyle.getCell("EL PRODUCTO NO CONFORME SE CORRIGIó ANTES QUE EL VEHÍCULO SALGA EN LA RUTA PROGRAMADA", 1, 1, Element.ALIGN_LEFT));
            tab.addCell(titleStyle.getCell("SI", 1, 1, Element.ALIGN_LEFT));
            Boolean cads = (Boolean) getAnsswer(CORREGIDO_ANTES_DE_SALIR);
            cads = (cads == null ? false : cads);
            tab.addCell(cads ? getOptOn(this) : getOptOff(this));
            tab.addCell(titleStyle.getCell("NO", 1, 1, Element.ALIGN_LEFT));
            tab.addCell(!cads ? getOptOn(this) : getOptOff(this));

            PDFCellStyle stS = cellStyle.copy();
            stS.setFontSize(PDFCellStyle.DEFAULT_FONT_SIZE - 2);
            tab.addCell(stS.getCell("Si la respuesta anterior es negativa, por ningún motivo el vehículo "
                    + "programado deberá despacharse y se deberá verificar que el mismo se corrija antes "
                    + "que el vehículo cubra la siguiente ruta para lo cual se deberá diligenciar la "
                    + "siguiente parte del presente formato."
                    + "\n\n"
                    + "Si la respuestas anteriror es positiva no diligencie la continuaciónj del siguiente formato.\n ", 5, 1, Element.ALIGN_LEFT));

            Object[][] datosCor = new MySQLQuery("SELECT v.corr_date,CONCAT(e.first_name,' ',e.last_name) "
                    + "FROM mto_chk_val AS v "
                    + "INNER JOIN employee AS e ON e.id = v.corr_employee_id "
                    + "WHERE v.lst_id = " + lst.id + " AND v.col_id = " + DATOS_DE_CORRECCION.colId + " AND v.row_id = " + DATOS_DE_CORRECCION.rowId + "").getRecords(ep);

            Date dtCorr = null;
            String corrector = "";
            if (datosCor != null && datosCor.length > 0) {
                dtCorr = (datosCor != null && datosCor[0][0] != null) ? MySQLQuery.getAsDate(datosCor[0][0]) : null;
                corrector = (datosCor != null && datosCor[0][1] != null) ? MySQLQuery.getAsString(datosCor[0][1]) : "";
            }
            tab.addCell(titleStyle.getCell("VERIFICACIÓN DE LA SOLUCIÓN DEL PRODUCTO NO CONFORME" + "\n ", 5, 1, Element.ALIGN_CENTER));
            tab.addCell(titleStyle.getCell("RESPONSABLE DE LA VERIFICACIÓN", 1, 1, Element.ALIGN_LEFT));
            tab.addCell(cellStyle.getCell(corrector.toUpperCase() + " \n ", 4, 1, Element.ALIGN_LEFT));
            tab.addCell(titleStyle.getCell("FECHA DE VERIFICACIÓN", 1, 1, Element.ALIGN_LEFT));
            tab.addCell(cellStyle.getCell((dtCorr != null ? new SimpleDateFormat("dd/MM/yyyy").format(dtCorr) : "") + "\n ", 4, 1, Element.ALIGN_LEFT));
            tab.addCell(titleStyle.getCell("EL PRODUCTO NO CONFORME SE CORRIGIÓ\n ", 1, 1, Element.ALIGN_LEFT));
            tab.addCell(titleStyle.getCell("SI", 1, 1, Element.ALIGN_LEFT));
            tab.addCell(dtCorr != null ? getOptOn(this) : getOptOff(this));
            tab.addCell(titleStyle.getCell("NO", 1, 1, Element.ALIGN_LEFT));
            tab.addCell(dtCorr == null ? getOptOn(this) : getOptOff(this));
            document.add(tab);
            return endDocument();
        } catch (Exception ex) {
            Logger.getLogger(PrintCootranarPNC.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    class HeaderFooter extends PdfPageEventHelper {

        PdfTemplate total;
        PdfTemplate sgc;

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

            sgc = writer.getDirectContent().createTemplate(100, 100);
            sgc.setBoundingBox(new Rectangle(-20, -20, 100, 100));
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            try {
                BaseFont f = PDFFontsHelper.getRegular();
                float fontSize = PDFCellStyle.DEFAULT_FONT_SIZE - 2;
                total.beginText();
                total.setFontAndSize(f, fontSize);
                total.setTextMatrix(0, 0);
                total.showText(String.valueOf(writer.getPageNumber() - 1));
                total.endText();

                sgc.beginText();
                sgc.setFontAndSize(f, fontSize);
                sgc.setTextMatrix(0, 0);
                sgc.showText(type.sgcCode != null ? type.sgcCode : "");
                sgc.endText();
            } catch (Exception ex) {
                Logger.getLogger(PrintCootranarPNC.class.getName()).log(Level.SEVERE, null, ex);
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
        c.setBorderColor(Color.LIGHT_GRAY);
        c.setPadding(3);
        return c;
    }

    private void addRowsText(PdfPTable tab, int colSpan) throws Exception {
        char[] text = lst.notes != null ? lst.notes.toCharArray() : new char[0];
        if (text.length > MAX_LINE_DESCRIPTION) {
            int indexText = 0;
            for (int i = 0; i < MAX_LINES_DESCRIPTION; i++) {
                String nText = "";
                for (int j = 0; j < MAX_LINE_DESCRIPTION; j++) {
                    if (indexText < text.length) {
                        nText += text[indexText];
                        indexText++;
                    } else {
                        nText += " ";
                    }
                }
                tab.addCell(cellStyle.getCell(nText + "\n ", colSpan, 1));
            }
        } else {
            tab.addCell(cellStyle.getCell((lst.notes != null && !lst.notes.isEmpty() ? lst.notes : " "), colSpan, 1));
            for (int i = 0; i < MAX_LINES_DESCRIPTION; i++) {
                tab.addCell(cellStyle.getCell(" ", colSpan, 1));
            }
        }
    }

    private Object getAnsswer(Ansswer findAns) throws Exception {
        for (int i = 0; i < grps.length; i++) {
            MtoChkGrp grp = grps[i];
            MtoChkCol[] gCols = cols[i];
            MtoChkRow[] gRows = rows[i];
            if (grp.id.equals(findAns.grpId)) {
                for (int j = 0; j < gRows.length; j++) {
                    MtoChkRow gRow = gRows[j];
                    MtoChkVal val = ans[i][j];
                    if (gRow.id.equals(findAns.rowId)) {
                        if (gRow.type.equals("nor")) {
                            for (MtoChkCol gCol : gCols) {
                                if (gCol.id.equals(findAns.colId)) {
                                    return (val != null && val.colId.equals(gCol.id));
                                }
                            }
                        } else if (gRow.type.equals("num") || gRow.type.equals("txt")) {
                            return (val != null && val.val != null ? val.val : "");
                        }
                    }
                }
            }
        }
        return null;
    }

    private class Ansswer {

        Integer grpId;
        Integer rowId;
        Integer colId;

        public Ansswer(Integer grpId, Integer rowId, Integer colId) {
            this.grpId = grpId;
            this.rowId = rowId;
            this.colId = colId;
        }

    }

}
