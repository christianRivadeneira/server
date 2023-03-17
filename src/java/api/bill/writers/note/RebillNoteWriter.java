package api.bill.writers.note;

import api.bill.model.BillClieRebill;
import api.bill.model.BillClientTank;
import api.bill.model.BillInstance;
import api.bill.model.BillSpan;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilities.FileUtils;
import utilities.MySQLQuery;
import utilities.pdf.PDFFontsHelper;

public class RebillNoteWriter {

    private BaseFont bf;
    private BaseFont bfb;
    private PdfContentByte cb;
    private Map<String, float[]> pos;
    private final SimpleDateFormat shortDateFormat = new SimpleDateFormat("dd/MMMM/yyyy");
    private final DecimalFormat nf = new DecimalFormat("###,###.0000");
    private Document document;
    private PdfWriter writer;
    private File fin;
    private static PdfImportedPage paper;
    private static PdfLayer not_printed;
    private final BillInstance inst;
    private final byte[] logoBytes;
    private final Connection conn;

    public RebillNoteWriter(BillInstance inst, Connection conn) throws Exception {
        this.inst = inst;
        logoBytes = FileUtils.getEnterpriseLogo(conn, FileUtils.LogoType.ICON_CAL);
        this.conn = conn;
    }

    public void beginDocument() throws Exception {
        //create fonts
        bf = PDFFontsHelper.getRegular();
        bfb = PDFFontsHelper.getBold();
        pos = getPositions();
        document = new Document(new Rectangle(8.5f * 72f, 11f * 72f));
        fin = File.createTempFile("nota", ".pdf");
        writer = PdfWriter.getInstance(document, new FileOutputStream(fin));
        writer.setPageEvent(new PdfPageEventBill());
        document.open();
        preparePage();
    }

    public void addNote(BillClieRebill rb, BillClientTank client, BillSpan span) throws Exception {
        //document.newPage();
        preparePage();

        cb.beginLayer(not_printed);
        cb.addTemplate(paper, 0, 0);
        drawLine(0.3f, 5.5f, 8.2f, 5.5f);//hlarga //mitad de la hoja
        cb.endLayer();

        if (logoBytes != null) {
            Image img = Image.getInstance(logoBytes);
            float w = 55f * (img.getWidth() / img.getHeight());
            float xp = ((paper.getBoundingBox().getWidth() * 0.55f) - w) / 2;

            img.setAlignment(Element.ALIGN_CENTER);
            cb.addImage(img, w, 0, 0, 55, xp, paper.getHeight() - 100);
        }

        cb.setFontAndSize(bfb, 9);
        addText("title", "Refacturación");
        cb.setFontAndSize(bf, 9);
        addText("names", client.firstName + " " + client.lastName);
        addText("code", client.code);
        addText("city", new MySQLQuery("SELECT name FROM sigma.city WHERE id = ?1").setParam(1, inst.cityId).getAsString(conn));
        String neigh = new MySQLQuery("SELECT name FROM sigma.neigh WHERE id = ?1").setParam(1, client.neighId).getAsString(conn);
        addText("address", client.address + " " + neigh);

        BigDecimal total = BigDecimal.ZERO;
        cb.setFontAndSize(bf, 9);

        List<String> labels = new ArrayList<>();
        List<BigDecimal> vals = new ArrayList<>();

        if (rb.diffValConsNoSubs.compareTo(BigDecimal.ZERO) != 0) {
            labels.add("Diferencia Consumo sin Subsidio " + nf.format(rb.diffM3NoSubs) + "m3");
            vals.add(null);
            labels.add("Valor Unitario Consumo sin Subsidio " + nf.format(rb.newSector.equals("r") ? span.cuvR : span.cuvNr) + "$/m3");
            vals.add(null);
            labels.add("Total Refacturación Consumo sin Subsidio");
            vals.add(rb.diffValConsNoSubs);
        }
        if (rb.diffValConsSubs.compareTo(BigDecimal.ZERO) != 0) {
            labels.add("Diferencia Consumo Subsidiado " + nf.format(rb.diffM3Subs) + "m3");
            vals.add(null);
            labels.add("Valor Unitario Consumo Subsidiado " + nf.format(rb.newStratum == 1 ? span.cEq1 : span.cEq2) + "$/m3");
            vals.add(null);
            labels.add("Total Refacturación Consumo Subsidiado");
            vals.add(rb.diffValConsSubs);
        }
        if (rb.diffValSubs.compareTo(BigDecimal.ZERO) != 0) {
            labels.add("Porcentaje de Subsidio " + nf.format(rb.newStratum == 1 ? span.subPerc1 : span.subPerc2) + "%");
            vals.add(null);
            labels.add("Total Refacturación Subsidio");
            vals.add(rb.diffValSubs.negate());
        }
        if (rb.diffValContrib.compareTo(BigDecimal.ZERO) != 0) {
            labels.add("Porcentaje de Contribución " + nf.format(rb.newSector.equals("r") ? span.contribR : span.contribNr) + "%");
            vals.add(null);
            labels.add("Total Refacturación Contribución");
            vals.add(rb.diffValContrib);
        }
        if (rb.diffValExcContrib.compareTo(BigDecimal.ZERO) != 0) {
            labels.add("Contribución Exenta");
            vals.add(rb.diffValExcContrib);
        }

        for (int i = 0; i < vals.size(); i++) {
            addText("car" + (i + 1), labels.get(i));
            if (vals.get(i) != null) {
                addTextRight("car" + (i + 1), "$" + nf.format(vals.get(i)));
                total = total.add(vals.get(i));
            }
        }

        addTextRight("total", "$" + nf.format(total));

        addText("date", shortDateFormat.format(rb.created));
        addText("creator", new MySQLQuery("SELECT CONCAT(first_name, ' ', last_name) FROM sigma.employee WHERE id = ?1").setParam(1, rb.creatorId).getAsString(conn));

        BillSpan rebillSpan = new BillSpan().select(rb.rebillSpanId, conn);
        BillSpan errorSpan = new BillSpan().select(rb.errorSpanId, conn);

        addText("rebill_span", rebillSpan.getConsLabel());
        addText("rebilled_span", errorSpan.getConsLabel());

        BigDecimal origCons = new MySQLQuery("SELECT (m3_subs+m3_no_subs) FROM bill_clie_cau WHERE client_id = ?1 AND span_id = ?2").setParam(1, rb.clientId).setParam(2, errorSpan.id).getAsBigDecimal(conn, true);
        BigDecimal newCons = origCons.add(rb.diffM3NoSubs.add(rb.diffM3Subs));

        addText("orig_cons", nf.format(origCons) + "m3");
        addText("new_cons", nf.format(newCons) + "m3");

        Paragraph p = new Paragraph();
        p.add(rb.reason);
        ColumnText ct = new ColumnText(cb);
        p.getFont().setSize(9f);
        ct.addElement(p);
        ct.setSimpleColumn(pos.get("desc")[1], pos.get("desc")[2], pos.get("desc")[3], pos.get("desc")[4]);
        ct.go();
    }

    public File endDocument() {
        document.close();
        return fin;
    }

    private void preparePage() {
        cb = writer.getDirectContent();
        cb.setColorStroke(new Color(230, 230, 230));
        cb.setFontAndSize(bf, 9);
    }

    private Map<String, float[]> getPositions() throws Exception {
        Map<String, float[]> res = new HashMap<>();
        PdfReader reader;
        if (inst.isNetInstance()) {
            reader = new PdfReader(RebillNoteWriter.class.getResourceAsStream("rebillTemplate.pdf"));
        } else {
            throw new RuntimeException("La facturación tanques no tiene notas de refacturación");
        }
        PdfStamper stamper = new PdfStamper(reader, new ByteArrayOutputStream());
        AcroFields form = stamper.getAcroFields();
        HashMap fields = form.getFields();
        String key;
        for (Iterator i = fields.keySet().iterator(); i.hasNext();) {
            key = (String) i.next();
            String key1 = key.substring(key.lastIndexOf(".") + 1);
            key1 = key1.substring(0, key1.indexOf("["));

            res.put(key1, form.getFieldPositions(key));
        }
        stamper.close();
        return res;
    }

    private void drawLine(float x1, float y1, float x2, float y2) {
        cb.moveTo(72 * x1, 72 * y1);
        cb.lineTo(72 * x2, 72 * y2);
        cb.stroke();
    }

    private void addText(String name, String text) {
        cb.saveState();
        cb.beginText();
        //cb.moveText(pos.get(name)[1], pos.get(name)[2] - 2 - (section * offSet));
        cb.moveText(pos.get(name)[1], pos.get(name)[2] + 3);
        cb.showText(text);
        cb.endText();
        cb.restoreState();
    }

    private void addTextRight(String name, String text) {
        cb.saveState();
        cb.beginText();
        cb.moveText(pos.get(name)[3] - bf.getWidthPoint(text, 9), pos.get(name)[2] + 3);
        cb.showText(text);
        cb.endText();
        cb.restoreState();
    }

    private class PdfPageEventBill implements PdfPageEvent {

        @Override
        public void onOpenDocument(PdfWriter writer, Document dcmnt) {
            try {
                PdfReader reader;
                if (inst.isNetInstance()) {
                    reader = new PdfReader(RebillNoteWriter.class.getResourceAsStream("rebillTemplate.pdf"));
                } else {
                    throw new RuntimeException("La facturación tanques no tiene notas de refacturación");
                }
                paper = writer.getImportedPage(reader, 1);
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
