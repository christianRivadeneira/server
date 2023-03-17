package utilities.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.awt.color.ICC_Profile;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import utilities.MySQLQuery;
import utilities.Reports;
import web.enterpriseLogo;

public class PDFHeaderMg extends PdfPageEventHelper {

    private PDFCellStyle cellStyleLogo1;
    private PDFCellStyle cellStyleLogo2;
    private PDFCellStyle cellStyleLogo3;
    private PDFCellStyle cellStyleLogo;
    private PdfTemplate total;
    private Image logo;
    private BaseFont bf;
    private ICC_Profile icc;

    private Connection ep;
    private int formatId;
    private String subTitle;
    private String title;
    private Date valid;
    private int version;
    private String code;
    private String serial;

    public PDFHeaderMg() {
    }

    public PDFHeaderMg(Connection ep, int formatId, String subtitle, String serial) throws Exception {
        this.ep = ep;
        this.formatId = formatId;
        this.subTitle = subtitle;
        this.serial = serial;
        begin();
    }

    private void begin() throws Exception {
        cellStyleLogo = new PDFCellStyle();
        cellStyleLogo.setFontSize(9);
        cellStyleLogo.setBorderColor(PDFCellStyle.GRAY_BORDER);
        cellStyleLogo.sethAlignment(Element.ALIGN_CENTER);
        cellStyleLogo.setBold(true);
        cellStyleLogo.setPaddings(5, 5, 5, 5);

        cellStyleLogo1 = cellStyleLogo.copy();
        cellStyleLogo1.setFontSize(10);
        cellStyleLogo1.setBorders(false, true, true, true);
        cellStyleLogo1.setPaddings(0, 7, 0, 0);

        cellStyleLogo2 = new PDFCellStyle();
        cellStyleLogo2.setFontSize(8);
        cellStyleLogo2.setBorderColor(PDFCellStyle.GRAY_BORDER);
        cellStyleLogo2.sethAlignment(Element.ALIGN_LEFT);
        cellStyleLogo2.setBorders(true, false, true, true);
        cellStyleLogo2.setBold(false);

        cellStyleLogo3 = new PDFCellStyle();
        cellStyleLogo3.setFontSize(8);
        cellStyleLogo3.setBorderColor(PDFCellStyle.GRAY_BORDER);
        cellStyleLogo3.sethAlignment(Element.ALIGN_CENTER);
        cellStyleLogo3.setBold(false);

        Object[] row = new MySQLQuery("SELECT f.title, f.effect, f.version, f.code, p.name FROM cal_format f LEFT JOIN cal_proc p ON f.proc_id = p.id WHERE f.id = " + formatId).getRecord(ep);
        this.title = row[0].toString();
        this.valid = (Date) row[1];
        this.version = (Integer) row[2];
        this.code = row[3].toString();
        this.subTitle = (subTitle != null ? subTitle : (row[4] != null ? row[4].toString() : null));

        try {
            File file = enterpriseLogo.getEnterpriseLogo("4", ep);
            byte[] readAllBytes = Files.readAllBytes(file.toPath());
            logo = Image.getInstance(readAllBytes);
        } catch (Exception ex) {
            logo = null;
        }

        File f = File.createTempFile("sRGB", ".profile");
        FileOutputStream fos = new FileOutputStream(f);
        fos.write(Reports.readInputStreamAsBytes(PDFFontsHelper.class.getResourceAsStream("/utilities/pdf/sRGB.profile")));
        fos.close();
        icc = ICC_Profile.getInstance(new FileInputStream(f));
        bf = PDFFontsHelper.getRegular();
    }

    @Override
    public void onOpenDocument(PdfWriter writer, Document dcmnt) {
        total = writer.getDirectContent().createTemplate(100, 100);
        total.setBoundingBox(new Rectangle(-20, -20, 100, 100));
    }

    @Override
    public void onStartPage(PdfWriter writer, Document document) {
        try {
            PdfPCell imgCell;
            if (logo != null) {
                logo.setAlignment(Element.ALIGN_CENTER);
                logo.scaleToFit(100, 100);
                imgCell = new PdfPCell(logo);
            } else {
                imgCell = new PdfPCell();
            }

            imgCell.setBorderColor(PDFCellStyle.GRAY_BORDER);
            imgCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            imgCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            imgCell.setRowspan(4);
            PdfPTable logoTab;
            if (subTitle != null) {
                logoTab = new PdfPTable(6);
                logoTab.setWidths(new float[]{20, 32, 12, 12, 12, 12});
            } else {
                logoTab = new PdfPTable(5);
                logoTab.setWidths(new float[]{20, 20, 20, 20, 20});
            }
            logoTab.setWidthPercentage(100);
            logoTab.addCell(imgCell);

            logoTab.addCell(cellStyleLogo.getCell("MONTAGAS S.A E.S.P\nNIT 891202203-9", (subTitle != null ? 5 : 4), 1));
            logoTab.addCell(cellStyleLogo2.getCell("NOMBRE DEL FORMATO:", (subTitle != null ? 5 : 4), 1));
            logoTab.addCell(cellStyleLogo1.getCell(title, (subTitle != null ? 5 : 4), 1));
            if (subTitle != null) {
                logoTab.addCell(cellStyleLogo.getCell(subTitle));
            }
            logoTab.addCell(cellStyleLogo3.getCell("VIGENCIA \n" + new SimpleDateFormat("dd-MMM-yyyy").format(valid)));
            logoTab.addCell(cellStyleLogo3.getCell("VERSIÓN \n" + version));
            logoTab.addCell(cellStyleLogo3.getCell("CÓDIGO \n" + code));
            logoTab.addCell(cellStyleLogo3.getCell("CONSECUTIVO \n " + (serial != null ? serial : "")));

            logoTab.setSpacingAfter(10);
            document.add(logoTab);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onEndPage(PdfWriter writer, Document dcmnt) {
        float x = 35;
        float y = 20;
        PdfContentByte cb = writer.getDirectContent();
        cb.saveState();
        String text = "PÁGINA " + writer.getPageNumber() + " DE ";
        cb.saveState();
        cb.setFontAndSize(bf, 7);
        cb.beginText();

        cb.moveText(x, y);
        cb.showText(text);
        cb.endText();
        cb.restoreState();
        cb.addTemplate(total, x + bf.getWidthPoint(text, 7), y);

        cb.restoreState();
    }

    @Override
    public void onCloseDocument(PdfWriter writer, Document dcmnt) {
        try {
            writer.setOutputIntents("Custom", "", "http://www.color.org", "sRGB IEC61966-2.1", icc);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        total.beginText();
        total.setFontAndSize(bf, 7);
        total.setTextMatrix(0, 0);
        total.showText(String.valueOf(writer.getPageNumber() - 1));
        total.endText();
    }

    @Override
    public void onGenericTag(PdfWriter writer, Document dcmnt, Rectangle rect, String string) {
        PdfContentByte cb = writer.getDirectContent();
        cb.setLineWidth(0.5f);
        cb.setColorStroke(Color.LIGHT_GRAY);
        cb.rectangle(rect.getLeft() - 4, rect.getBottom() - 2, rect.getWidth() + 8, rect.getHeight() + 2);
        cb.stroke();
        cb.resetRGBColorStroke();
        cb.rectangle(rect);
    }
}
