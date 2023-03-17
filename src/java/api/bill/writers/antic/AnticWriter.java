package api.bill.writers.antic;

import api.bill.model.BillAnticNote;
import api.bill.model.BillBuilding;
import api.bill.model.BillClientTank;
import api.bill.model.BillInstance;
import api.bill.model.BillSpan;
import api.bill.model.BillTransaction;
import api.bill.writers.note.NoteWriter;
import api.sys.model.City;
import api.sys.model.Employee;
import com.lowagie.text.Document;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilities.MySQLQuery;
import utilities.pdf.PDFFontsHelper;

public class AnticWriter {

    private BaseFont bf;
    private BaseFont bfb;
    private PdfContentByte cb;
    private Map<String, float[]> pos;
    private final SimpleDateFormat shortDateFormat = new SimpleDateFormat("d MMMM yyyy");
    private final DecimalFormat moneyFormat = new DecimalFormat("$###,###,###,##0.00");
    private Document document;
    private PdfWriter writer;
    private File fout;
    private static PdfImportedPage paper;
    private static PdfLayer not_printed;
//    private final Connection conn;
    private final BillInstance inst;

    public AnticWriter(BillInstance inst) throws Exception {
        this.inst = inst;
        beginDocument();
    }

    private void beginDocument() throws Exception {
        //create fonts
        bf = PDFFontsHelper.getRegular();
        bfb = PDFFontsHelper.getBold();

        pos = getPositions();

        document = new Document(new Rectangle(8.5f * 72f, 11f * 72f));
        fout = File.createTempFile("nota", ".pdf");
        writer = PdfWriter.getInstance(document, new FileOutputStream(fout));
        writer.setPageEvent(new PdfPageEventBill());
        document.open();
        preparePage();
    }

    public File endDocument() {
        document.close();
        return fout;
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
            reader = new PdfReader(NoteWriter.class.getResourceAsStream("billNoteTemplateNet.pdf"));
        } else {
            reader = new PdfReader(NoteWriter.class.getResourceAsStream("billNoteTemplateTank.pdf"));
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

    public void drawNote(BillAnticNote note, BillClientTank client, BillBuilding build, BillSpan span, Employee e, City city, List<BillTransaction> deta, Connection conn) throws Exception {
        cb.addTemplate(paper, 0, 0);
        cb.beginLayer(not_printed);
        drawLine(0.3f, 5.5f, 8.2f, 5.5f);//hlarga //mitad de la hoja
        cb.endLayer();

        Date beginDate = span.beginDate;
        Date endDate = span.endDate;

        cb.setFontAndSize(bfb, 9);
        addText("title", "NOTA DE SALDO A FAVOR");

        cb.setFontAndSize(bf, 9);
        addText("names", client.firstName + " " + client.lastName);
        addText("instNum", inst.isNetInstance() ? client.code : client.numInstall + " - " + inst.name);
        addText("notNum", note.serial + "");
        addText("date", shortDateFormat.format(note.whenNotes));
        if (inst.isTankInstance()) {
            addText("address", build.address);
        } else {
            String neigh = new MySQLQuery("SELECT name FROM sigma.neigh WHERE id = ?1").setParam(1, client.neighId).getAsString(conn);
            addText("address", client.address + " " + neigh);
        }
        addText("building", inst.isTankInstance() ? build.name + " " + city.name : city.name);
        addText("span", shortDateFormat.format(beginDate) + " - " + shortDateFormat.format(endDate));
//        addText("billNum", "-");

        cb.setFontAndSize(bfb, 9);
        cb.setFontAndSize(bf, 9);
        BigDecimal total = BigDecimal.ZERO;

        for (int k = 0; k < deta.size(); k++) {
            addText("car" + (k + 1), "Valor a devolver");
            addTextRight("car" + (k + 1), moneyFormat.format(deta.get(k).value));
            total = total.add(deta.get(k).value);
        }

        addTextRight("total", moneyFormat.format(total));

        Paragraph p = new Paragraph();
        p.add("Creada por " + e.firstName + " " + e.lastName + "\n");
        p.add("Tipo: " + new MySQLQuery("SELECT name FROM bill_antic_note_type t WHERE t.id = ?1").setParam(1, note.typeId).getAsString(conn) + "\n");
        p.add("Etiqueta: " + note.label + "\n");
        p.add("Observaciones: " + note.descNotes + "\n");
        ColumnText ct = new ColumnText(cb);
        p.getFont().setSize(9f);
        ct.addElement(p);
        ct.setSimpleColumn(pos.get("desc")[1], pos.get("desc")[2], pos.get("desc")[3], pos.get("desc")[4]);
        ct.go();
    }

    private void addText(String name, String text) {
        cb.saveState();
        cb.beginText();
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
                    reader = new PdfReader(NoteWriter.class.getResourceAsStream("billNoteTemplateNet.pdf"));
                } else {
                    reader = new PdfReader(NoteWriter.class.getResourceAsStream("billNoteTemplateTank.pdf"));
                }
                paper = writer.getImportedPage(reader, 1);
                not_printed = new PdfLayer("template", writer);
                not_printed.setOnPanel(false);
                not_printed.setPrint("Print", true);
            } catch (Exception ex) {
                Logger.getLogger(PdfPageEventBill.class.getName()).log(Level.SEVERE, "message", ex);
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
