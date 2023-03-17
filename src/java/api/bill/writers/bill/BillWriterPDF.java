package api.bill.writers.bill;

import api.bill.model.BillCfg;
import api.bill.model.BillInstance;
import api.bill.model.BillSpan;
import api.sys.model.SysCfg;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.Barcode128;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfLayer;
import com.lowagie.text.pdf.PdfPageEvent;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilities.Reports;
import utilities.pdf.PDFFontsHelper;

public abstract class BillWriterPDF extends BillWriter {

    private PdfContentByte cb;
    protected BaseFont bf;
    protected BaseFont bfb;
    protected Map<String, float[]> pos;
    private Document document;
    private PdfWriter writer;
    private int currBill = 0;
    protected AcroFields form;

    protected static PdfImportedPage paper;
    protected static PdfLayer not_printed;

    private String[] bankData;

    @Override
    public void begin(BillInstance inst, SysCfg sysCfg, BillCfg cfg, Connection billConn, Boolean printLogo, File fout) throws Exception {
        super.begin(inst, sysCfg, cfg, billConn, printLogo, fout);
        bankData = new String[rawBankData.length + 1];
        bankData[0] = "Páguese en:";
        for (int i = 0; i < rawBankData.length; i++) {
            bankData[i + 1] = rawBankData[i][0].toString();
        }

        this.fout = (fout == null ? Reports.createReportFile("facturas", "pdf") : fout);
        bf = PDFFontsHelper.getRegular();
        bfb = PDFFontsHelper.getBold();
        document = createDocument();
        pos = getPositions();
        writer = PdfWriter.getInstance(document, new FileOutputStream(this.fout));
        writer.setPageEvent(new PdfPageEventBill());
        document.open();
        preparePage();
        prepare(billConn);
    }

    @Override
    public abstract void prepare(Connection ep) throws Exception;

    /**
     *
     * @param j 0 para la primera factura de la página, 1 para la segunda
     */
    protected void prepareNewBill(int j) {
        cb.beginLayer(not_printed);
        if (j == 0) {
            cb.addTemplate(paper, 0, 0);
        } else {
            cb.addTemplate(paper, 0, -getOffset());
        }
        cb.endLayer();
    }

    protected void setFont(BaseFont fb, float size) {
        cb.setFontAndSize(fb, size);
    }
    protected void setColorRed(){
        cb.setColorFill(Color.red);
    }
    protected void setColorBlack(){
        cb.setColorFill(Color.black);
    }

    protected void setFont(BaseFont fb, float size, Color c) {
        setFont(fb, size);
        cb.setColorStroke(c);
        cb.setColorFill(c);
    }

    @Override
    public File endDocument() {
        document.close();
        return fout;
    }

    @Override
    public void addBill(BillForPrint bill) throws Exception {
        super.addBill(bill);
        if (currBill > 1) {
            document.newPage();
            preparePage();
            currBill = 0;
        }
        drawFact(inst, bill, span, currBill);
        currBill++;
    }

    protected abstract PdfReader getReader(boolean printLogo) throws Exception;

    protected float getOffset() {
        return document.getPageSize().getHeight() / 2;
    }

    protected abstract Document createDocument();

    protected Map<String, float[]> getPositions() throws Exception {
        Map<String, float[]> res = new HashMap<>();
        PdfStamper stamper = new PdfStamper(getReader(printLogo), new ByteArrayOutputStream());
        AcroFields form = stamper.getAcroFields();
        HashMap fields = form.getFields();
        String key;
        for (Iterator i = fields.keySet().iterator(); i.hasNext();) {
            key = (String) i.next();
            String key1 = key.substring(key.lastIndexOf(".") + 1);
            key1 = key1.substring(0, key1.indexOf("["));
            res.put(key1, form.getFieldPositions(key));
        }
        //page, llx, lly, urx, ury
        stamper.close();
        return res;
    }

    protected void addText(float x, float y, String text, int section) {
        cb.saveState();
        cb.beginText();
        cb.moveText(x, y - 2f - (section * getOffset()));
        cb.showText(text);
        cb.endText();
        cb.restoreState();
    }

    protected void addTextRot(String name, String text, int section) {
        cb.saveState();
        cb.beginText();
        float pm = (pos.get(name)[1] + pos.get(name)[3]) / 2.0f;
        cb.moveText(pm - 2f, pos.get(name)[2] - (section * getOffset()));
        cb.showTextAligned(Element.ALIGN_LEFT, text, pm + 2f, pos.get(name)[2] - (section * getOffset()), 90f);
        //cb.showText(text);
        cb.endText();
        cb.restoreState();
    }

    protected void addText(String name, String text, int section) {
        cb.saveState();
        cb.beginText();
        if (!pos.containsKey(name)) {
            throw new RuntimeException("No se encuentra la etiqueta: " + name);
        }
        float pm = (pos.get(name)[2] + pos.get(name)[4]) / 2.0f;
        cb.moveText(pos.get(name)[1], pm - 2f - (section * getOffset()));
        cb.showText(text);
        cb.endText();
        cb.restoreState();
    }

    protected void addTextRight(String name, String text, BaseFont font, int size, int section) {
        setFont(font, size);
        cb.saveState();
        cb.beginText();
        if (!pos.containsKey(name)) {
            throw new RuntimeException("No se encuentra la etiqueta: " + name);
        }
        float pm = (pos.get(name)[2] + pos.get(name)[4]) / 2.0f;
        cb.moveText(pos.get(name)[3] - font.getWidthPoint(text, size), pm - 2f - (section * getOffset()));
        cb.showText(text);
        cb.endText();
        cb.restoreState();
    }

    protected void addTextRight(BaseFont font, float size, float x, float y, String text, int section) {
        cb.saveState();
        cb.beginText();
        cb.moveText(x - font.getWidthPoint(text, size), y - 2f - (section * getOffset()));
        cb.showText(text);
        cb.endText();
        cb.restoreState();
    }

    protected void addTextCenter(String name, String text, int section) {
        cb.saveState();
        cb.beginText();
        if (!pos.containsKey(name)) {
            throw new RuntimeException("No se encuentra la etiqueta: " + name);
        }
        float pm = (pos.get(name)[2] + pos.get(name)[4]) / 2.0f;
        cb.moveText((pos.get(name)[1] + pos.get(name)[3] - bf.getWidthPoint(text, 9)) / 2, pm - 2.0f - (section * getOffset()));
        cb.showText(text);
        cb.endText();
        cb.restoreState();
    }

    protected void addImage(String name, Map<String, float[]> positions, Image barCode, int section) throws Exception {
        barCode.setAbsolutePosition(positions.get(name)[1] - 3, positions.get(name)[2] - 5 - (getOffset() * section));
        cb.addImage(barCode);
    }

    private void preparePage() {
        cb = writer.getDirectContent();
        cb.setColorStroke(new Color(230, 230, 230));
        cb.setFontAndSize(bf, 9);
    }

    protected Image getBarCode(String billNum, BigDecimal amount, Date date, float heigth) throws Exception {
        String barCode = BillWriter.getStringBarcode(billNum, amount, date, billCfg.codEnt, false);
        Barcode128 bCode = new Barcode128();
        bCode.setSize(9);
        bCode.setBaseline(11);
        bCode.setCodeType(Barcode128.CODE128_UCC);
        bCode.setCode(barCode);
        bCode.setBarHeight(heigth);
        Image img = bCode.createImageWithBarcode(cb, null, null);
        //img.setRotationDegrees(90f);
        return img;
    }

    protected class TextPos {

        float x1;
        float x2;
        float y;
    }

    /**
     * Coloca una marca de agua en uno de los recuadros de
     *
     * @param text texto de la marca de agua
     * @param section 1 para la primera fra, 2 para la segunda
     * @param fieldName
     * @throws java.lang.Exception
     */
    protected void addWatermark(String text, int section, String fieldName) throws Exception {
        float x1 = pos.get(fieldName)[1] + 3;
        float x2 = pos.get(fieldName)[3] - 3;
        float y1 = pos.get(fieldName)[2] + 3;
        float y2 = pos.get(fieldName)[4] - 3;

        cb.saveState();
        cb.beginText();
        cb.setColorFill(new Color(210, 210, 210));
        cb.setFontAndSize(bf, 24);
        cb.moveText(x1, ((y1 + y2) / 2) - (section * getOffset()) - 8);
        cb.showText(text);
        cb.endText();
        cb.restoreState();
        cb.setFontAndSize(bf, 9);
        cb.setColorFill(Color.BLACK);
    }

    protected TextPos getTextPos(int nLines, int currLine) {
        TextPos rta = new TextPos();
        if (currLine >= nLines) {
            rta.x1 = pos.get("car2")[1] + 3;
            rta.x2 = pos.get("car2")[3] - 3;
            currLine = currLine - nLines;
        } else {
            rta.x1 = pos.get("car1")[1] + 3;
            rta.x2 = pos.get("car1")[3] - 3;
        }
        float y1 = pos.get("car1")[2] + 3;
        float y2 = pos.get("car1")[4] - 3;
        float rowHeight = (y2 - y1) / nLines;

        rta.y = (float) (y2 - ((currLine + 0.5) * rowHeight));
        return rta;
    }

    protected TextPos getBankPos(String name, int nLines, int currLine) {
        TextPos rta = new TextPos();
        rta.x1 = pos.get(name)[1];
        rta.x2 = pos.get(name)[3];
        float y1 = pos.get(name)[2];
        float y2 = pos.get(name)[4];
        float rowHeight = (y2 - y1) / nLines;
        rta.y = (float) (y2 - ((currLine + 0.5) * rowHeight));
        return rta;
    }

    protected void drawBanks(String name, int j) throws Exception {
        float fSize = 8;
        setFont(bf, fSize);
        int currLine = 0;
        for (String bank : bankData) {
            TextPos textPos = getBankPos(name, Math.max(bankData.length, 10), currLine++);
            addText((float) textPos.x1, (float) textPos.y, bank, j);
        }
    }
    
    protected void drawRevision(String name, int j, String nameReview, String dateReview, String statusReview, String maxDate,String suspDate)throws Exception {
        float fSize=(float) 7.7;
        setFont(bf,fSize);
        if(statusReview.equalsIgnoreCase("Vigente")){
            addText((float)428.185,(float) 651.0, "Última Rev. Revisión "+nameReview+" "+ dateReview, j);
            addText((float)428.185,(float) 645.0, "Reporte su certificado de Rev. periódica antes de", j);
            addText((float)428.185,(float) 639.0, maxDate+" para evitar suspensiones.", j);
            addText((float)428.185,(float) 633.0, "Fecha de suspensión: "+suspDate, j);
        }
        else if(statusReview.equalsIgnoreCase("Próx. Vencimiento")){
            addText((float)428.185,(float) 651.0, "Evite la suspensión del servicio, adjunte copia de", j);
            addText((float)428.185,(float) 645.0, "esta factura con el certificado de su revisión", j);
            addText((float)428.185,(float) 639.0, "periódica obligatoria de instalación interna de gas", j);
            addText((float)428.185,(float) 633.0, "antes de "+maxDate+".", j);
            addText((float)428.185,(float) 627.0, "Última Rev. Revisión "+nameReview+" "+ dateReview, j);
            addText((float)428.185,(float) 621.0, "Fecha de suspensión: "+suspDate, j);
        }
        else if(statusReview.equalsIgnoreCase("Vencida")){
            addText((float)428.185,(float) 651.0, "Última Rev. Revisión "+nameReview+" "+ dateReview, j);
            addText((float)428.185,(float) 645.0, "Fecha de vencimiento Rev. "+maxDate, j);
        }
        
        //System.out.println("Esta es x1 "+textPos.x1+" Esta es y "+textPos.y);       
        
    }
    
    protected void drawTitleRevision(String name, int j, String statusReview) throws Exception {
        float fSize=(float) 9;
        setFont(bf,fSize);
        addText((float)428.185,(float) 660.0, "Revisión de Instalación - "+statusReview, j);
    }

    private void drawLine(float x1, float y1, float x2, float y2) {
        cb.moveTo(72 * x1, 72 * y1);
        cb.lineTo(72 * x2, 72 * y2);
        cb.stroke();
    }

    protected void fillRect(float x, float y, float w, float h) {
        fillRect(x, y, w, h, new Color(220, 220, 220));
    }

    protected void fillRect(float x, float y, float w, float h, Color color) {
        cb.rectangle(x, y, w, h);
        cb.setColorFill(color);
        cb.fill();
        cb.setColorFill(Color.BLACK);
    }

    protected void drawBars(List<Double> cons, int j, SimpleDateFormat month, DecimalFormat reading, DecimalFormat readingShort, Date endDate, boolean skipZero, Color color, int height) {
        drawBars(cons, j, month, reading, readingShort, endDate, skipZero, color, height, true);
    }

    protected void drawBars(List<Double> cons, int j, SimpleDateFormat month, DecimalFormat reading, DecimalFormat readingShort, Date endDate, boolean skipZero, Color color, int height, boolean drawBars) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(endDate);
        if (cons.size() > 0) {
            setFont(bf, 7);
            for (int i = 0; i < 6; i++) {
                gc.add(GregorianCalendar.MONTH, -1);
                addText("lbl" + i, month.format(gc.getTime()), j);
            }
            addText("lblAvg", "Prom", j);
            double sum = 0;
            int zeros = 0;
            double max = Double.MIN_VALUE;
            for (int k = 0; k < Math.min(6, cons.size()); k++) {
                Double curCons = cons.get(k);
                if (skipZero && curCons == 0) {
                    zeros++;
                }
                sum += curCons;
                max = Math.max(max, curCons);
                if (curCons < 99) {
                    addText("cons" + k, reading.format(curCons), j);
                } else {
                    addText("cons" + k, readingShort.format(curCons), j);
                }
            }

            double avg = sum / (cons.size() - zeros);
            if (avg < 99) {
                addText("consAvg", reading.format(avg), j);
            } else {
                addText("consAvg", readingShort.format(avg), j);
            }

            if (drawBars) {
                for (int i = 0; i < Math.min(6, cons.size()); i++) {
                    Double curCons = cons.get(i);
                    String lblName = "lbl" + i;
                    fillRect(pos.get(lblName)[1], pos.get(lblName)[2] - (j * getOffset()) + 28, 16, (float) (curCons / max * height), color);
                }
                fillRect(pos.get("lblAvg")[1], pos.get("lblAvg")[2] - (j * getOffset()) + 28, 16, (float) (avg / max * height), color);
            }
        }
    }

    protected void drawMainBody(LineForPrint[] lines, int j, DecimalFormat moneyFormat, String blockName, float fontSize, float border) throws Exception {

        float x1 = pos.get(blockName)[1] + border;
        float x2 = pos.get(blockName)[3] - border;
        float y1 = pos.get(blockName)[2] + border;
        float y2 = pos.get(blockName)[4] - border;

        float lineH = (y2 - y1) / lines.length;
        lineH = lineH > 15 ? 15 : lineH;
        float y = y2 - (fontSize / 2);
        setFont(bf, fontSize);
        for (LineForPrint line : lines) {
            BaseFont f = line.bold ? bfb : bf;
            setFont(f, fontSize);
            if (line.value != null) {
                addTextRight(f, fontSize, x2, y, moneyFormat.format(line.value), j);
            }
            addText(x1, y, line.label, j);
            y -= lineH;
        }
    }

    protected void drawMainBody(LineForPrint[] lines, int j, DecimalFormat moneyFormat, BigDecimal total, int nLines) throws Exception {
        float fSize = 8;
        setFont(bf, fSize);
        int currLine = 0;
        for (LineForPrint line : lines) {
            //comentar eso
            //if (line.value != null && line.value.compareTo(BigDecimal.ZERO) != 0) {
            //     if (line.value != null ) {
            BaseFont f = line.bold ? bfb : bf;
            setFont(f, fSize);
            TextPos textPos = getTextPos(nLines, currLine++);
            if (line.value != null) {
                addTextRight(f, fSize, (float) textPos.x2, (float) textPos.y, moneyFormat.format(line.value), j);
            }
            addText((float) textPos.x1, (float) textPos.y, line.label, j);
            //comentar eso
            //}
        }

        if (currLine >= nLines * 2) {
            throw new Exception("No hay espacio para las filas.");
        }
        setFont(bfb, fSize);
        //comentar eso
//        TextPos textPos = getTextPos(nLines, (nLines * (currLine >= nLines ? 2 : 1)) - 1);
//        addTextRight(bfb, fSize, (float) textPos.x2, (float) textPos.y, moneyFormat.format(total), j);
//        addText((float) textPos.x1, (float) textPos.y, "TOTAL A PAGAR", j);
    }

    protected abstract void drawFact(BillInstance inst, BillForPrint bill, BillSpan span, int j) throws Exception;

    private class PdfPageEventBill implements PdfPageEvent {

        @Override
        public void onOpenDocument(PdfWriter writer, Document dcmnt) {
            try {
                paper = writer.getImportedPage(getReader(printLogo), 1);
                not_printed = new PdfLayer("template", writer);
                not_printed.setOnPanel(false);
                not_printed.setPrint("Print", true);
            } catch (Exception ex) {
                Logger.getLogger(PdfPageEventBill.class.getName()).log(Level.INFO, "message", ex);
            }
        }

        @Override
        public void onStartPage(PdfWriter writer, Document dcmnt) {
        }

        @Override
        public void onEndPage(PdfWriter writer, Document dcmnt) {
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document dcmnt) {
        }

        @Override
        public void onParagraph(PdfWriter writer, Document dcmnt, float f) {
        }

        @Override
        public void onParagraphEnd(PdfWriter writer, Document dcmnt, float f) {
        }

        @Override
        public void onChapter(PdfWriter writer, Document dcmnt, float f, Paragraph prgrph) {
        }

        @Override
        public void onChapterEnd(PdfWriter writer, Document dcmnt, float f) {
        }

        @Override
        public void onSection(PdfWriter writer, Document dcmnt, float f, int i, Paragraph prgrph) {
        }

        @Override
        public void onSectionEnd(PdfWriter writer, Document dcmnt, float f) {
        }

        @Override
        public void onGenericTag(PdfWriter writer, Document dcmnt, Rectangle rctngl, String string) {
        }
    }
}
