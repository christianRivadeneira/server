package utilities.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
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
import utilities.Reports;
import web.enterpriseLogo;

public class ModelPDFQualityHeader extends PdfPageEventHelper {

    private PdfTemplate total;
    private File logo;
    private Font fHead;
    private Font fTit;
    private BaseFont bf;
    private ICC_Profile icc;
    private Connection ep;
    ///

    protected String title;
    protected String subTitle;

    protected boolean justTitle;

    protected Date valid;
    protected int version;
    protected String code;
    protected boolean showFooter;

    public ModelPDFQualityHeader() throws Exception {
    }

    public ModelPDFQualityHeader(Connection ep) throws Exception {
        this(ep, null);
    }

    public ModelPDFQualityHeader(Connection ep, String title, Date valid, int version, String code) throws Exception {
        this(ep, title, null, valid, version, code);
    }

    public ModelPDFQualityHeader(Connection ep, String title, String subTitle, Date valid, int version, String code) throws Exception {
        justTitle = false;
        this.title = title;
        this.subTitle = subTitle;
        this.valid = valid;
        this.version = version;
        this.code = code;
        this.ep = ep;
        begin();
    }

    public ModelPDFQualityHeader(Connection ep, String title, String subTitle) throws Exception {
        this.title = title;
        this.subTitle = subTitle;
        justTitle = true;
        this.ep = ep;
        begin();
    }

    public ModelPDFQualityHeader(Connection ep, String title) throws Exception {
        this(ep, title, null);
    }

    private void begin() throws Exception {
        logo = File.createTempFile("logo", ".png");
        File file = enterpriseLogo.getEnterpriseLogo("4", ep);
        byte[] readAllBytes = Files.readAllBytes(file.toPath());
        FileOutputStream fos = new FileOutputStream(logo);
        fos.write(readAllBytes);
        fos.close();

        File f = File.createTempFile("sRGB", ".profile");
        fos = new FileOutputStream(f);
        fos.write(Reports.readInputStreamAsBytes(PDFFontsHelper.class.getResourceAsStream("/utilities/pdf/sRGB.profile")));
        fos.close();
        icc = ICC_Profile.getInstance(new FileInputStream(f));

        bf = PDFFontsHelper.getRegular();
        BaseFont bfb = PDFFontsHelper.getBold();

        fHead = new Font(bf, 7f, Font.NORMAL, Color.BLACK);
        fTit = new Font(bfb, 12f, Font.NORMAL, Color.BLACK);

    }

    @Override
    public void onOpenDocument(PdfWriter writer, Document dcmnt) {
        total = writer.getDirectContent().createTemplate(100, 100);
        total.setBoundingBox(new Rectangle(-20, -20, 100, 100));
    }

    @Override
    public void onStartPage(PdfWriter writer, Document dcmnt) {
        try {
            if (!justTitle) {
                PdfPTable conv = new PdfPTable(showFooter ? 2 : 3);
                conv.setSpacingBefore(15f);
                conv.setSpacingAfter(30f);
                conv.setWidthPercentage(100f);
                int[] cols1 = new int[]{25, 50, 25};
                int[] cols2 = new int[]{25, 75};
                conv.setWidths(showFooter ? cols2 : cols1);

                try {
                    Image img1 = Image.getInstance(logo.getAbsolutePath());

                    img1.scaleAbsoluteWidth((img1.getWidth() / img1.getHeight()) * 40);
                    img1.scaleAbsoluteHeight(40);

                    PdfPCell c = new PdfPCell(img1);
                    c.setBorderColor(getLineColor());
                    c.setHorizontalAlignment(Element.ALIGN_CENTER);
                    c.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    if (!showFooter) {
                        c.setRowspan(4);
                    } else {
                        c.setPadding(5f);
                    }
                    conv.addCell(c);
                } catch (IOException ex) {
                    addCell(conv, "Logo", 1, 1, fHead, Element.ALIGN_CENTER, true, -1, -1);
                }

                addCell(conv, title + (subTitle != null ? "\n" + subTitle : ""), 1, showFooter ? 1 : 4, fTit, Element.ALIGN_CENTER, true, -1, 10);
                if (!showFooter) {
                    addCell(conv, " ", 1, 1, fHead, Element.ALIGN_LEFT, true, 15, -1);
                    addCell(conv, "VIGENCIA: " + new SimpleDateFormat("dd/MM/yyyy").format(valid), 1, 1, fHead, Element.ALIGN_LEFT, true, 15, -1);
                    addCell(conv, "VERSIÓN: " + version, 1, 1, fHead, Element.ALIGN_LEFT, true, 15, -1);
                    addCell(conv, "CÓDIGO: " + code, 1, 1, fHead, Element.ALIGN_LEFT, true, 15, -1);
                }
                dcmnt.add(conv);
            } else {

                PdfPTable conv = new PdfPTable(3);
                conv.setSpacingBefore(15f);
                conv.setSpacingAfter(15f);
                conv.setWidthPercentage(100f);
                conv.setWidths(new int[]{25, 50, 25});

                try {
                    Image img1 = Image.getInstance(logo.getAbsolutePath());

                    img1.scaleAbsoluteWidth((img1.getWidth() / img1.getHeight()) * 50);
                    img1.scaleAbsoluteHeight(50);

                    PdfPCell c = new PdfPCell(img1);
                    c.setBorderColor(getLineColor());
                    c.setHorizontalAlignment(Element.ALIGN_LEFT);
                    c.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    c.setFixedHeight(52);
                    c.setBorder(0);
                    conv.addCell(c);
                } catch (IOException ex) {
                    addCell(conv, "Logo", 1, 1, fHead, Element.ALIGN_CENTER, false, -1, -1);
                }
                addCell(conv, title + (subTitle != null ? "\n" + subTitle : ""), 1, 1, fTit, Element.ALIGN_CENTER, false, -1, 10);
                addCell(conv, "", 1, 1, fTit, Element.ALIGN_CENTER, false, -1, 10);
                dcmnt.add(conv);
            }
        } catch (DocumentException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onEndPage(PdfWriter writer, Document dcmnt) {
        float x;
        float y = 20;
        PdfContentByte cb = writer.getDirectContent();
        cb.saveState();
        String text = "PÁGINA " + writer.getPageNumber() + " DE ";
        cb.saveState();
        cb.setFontAndSize(bf, 7);
        if (showFooter) {
            String cod = code + " V. " + version;
            x = dcmnt.getPageSize().getWidth() - bf.getWidthPoint(cod, 7) - dcmnt.rightMargin() - 5;
            cb.beginText();
            cb.moveText(x, y);
            cb.showText(cod);
            cb.endText();
        }

        cb.beginText();
        if (!showFooter) {
            if (!justTitle) {
                if (dcmnt.getPageSize().getWidth() < dcmnt.getPageSize().getHeight()) {
                    x = 444f;
                    y = 746.3f;
                } else {
                    x = 686f;
                    y = 565f;
                }
            } else {
                x = dcmnt.getPageSize().getWidth() - bf.getWidthPoint(text, 7) - dcmnt.rightMargin() - 5;
                if (dcmnt.getPageSize().getWidth() < dcmnt.getPageSize().getHeight()) {
                    y = 746.3f + 12;
                } else {
                    y = 565f + 12;
                }
            }
        } else {
            x = 35;
            y = 20;
        }

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

    private void addCell(PdfPTable table, String text, int colSpan, int rowSpan, Font font, int align, boolean borders, int rowHeight, int hPadding) {
        Phrase par = new Phrase(text, font);
        PdfPCell cell = new PdfPCell(par);
        cell.setBorderColor(getLineColor());
        if (!borders) {
            cell.setBorder(0);
        }
        if (hPadding > 0) {
            cell.setPaddingLeft(hPadding);
            cell.setPaddingRight(hPadding);
        }
        cell.setColspan(colSpan);
        cell.setRowspan(rowSpan);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setHorizontalAlignment(align);
        if (rowHeight > 0) {
            cell.setFixedHeight(rowHeight);
        }
        table.addCell(cell);
    }

    private static Color getLineColor() {
        return new Color(191, 191, 191);
    }
}
