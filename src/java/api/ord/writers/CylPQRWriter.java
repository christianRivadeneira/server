package api.ord.writers;

import api.ord.model.OrdCfg;
import api.ord.model.OrdContractIndex;
import api.ord.model.OrdPqrCyl;
import api.ord.model.OrdPqrReason;
import api.ord.model.OrdTechnician;
import api.sys.model.Enterprise;
import api.sys.model.Neigh;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.AcroFields;
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
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilities.Reports;
import utilities.pdf.PDFFontsHelper;

public class CylPQRWriter {

    private BaseFont bf;
    private BaseFont bfb;
    private PdfContentByte cb;
    private Map<String, float[]> pos;
    private Document document;
    private PdfWriter writer;
    private File fin;
    private static PdfImportedPage paper;
    private static PdfLayer not_printed;
    private final Connection conn;
    private OrdCfg cfg;

    public CylPQRWriter(Connection conn) throws Exception {
        this.conn = conn;
    }

    public void beginDocument(OrdPqrCyl bill, OrdCfg cfg) throws Exception {
        //create fonts
        this.cfg = cfg;
        fin = File.createTempFile("pqr_fugas", ".pdf");
        bf = PDFFontsHelper.getRegular();
        bfb = PDFFontsHelper.getBold();

        pos = getPositions();

        document = new Document(new Rectangle(792, 612));
        writer = PdfWriter.getInstance(document, new FileOutputStream(fin));
        writer.setPageEvent(new PdfPageEventBill());
        document.open();
        preparePage();
        drawFact(bill);
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
        PdfReader reader = new PdfReader(Reports.getPatternStream("cylPQR.pdf", this));
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

    private void drawFact(OrdPqrCyl bill) throws Exception {
        cb.beginLayer(not_printed);
        cb.addTemplate(paper, 0, 0);
        cb.setColorStroke(Color.GRAY);
        drawLine(5.5f, 0f, 5.5f, 8.5f);//hlarga //mitad de la hoja
        cb.endLayer();
        cb.setColorStroke(Color.BLACK);
        String pqrNum = String.valueOf(bill.serial);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat df1 = new SimpleDateFormat("HH:mm");
        String pqrFec = df.format(bill.creationDate);
        String pqrCap = df1.format(bill.registHour);
        OrdContractIndex index = new OrdContractIndex().select(bill.indexId, conn);
        String clientName = index.firstName + " " + index.lastName;
        OrdPqrReason reason = new OrdPqrReason().select(bill.pqrReason, conn);
        OrdTechnician tec = new OrdTechnician().select(bill.technicianId, conn);
        String tecName = tec.firstName + " " + tec.lastName;
        String neighName = "";
        if (index.neighId != null) {
            neighName = new Neigh().select(index.neighId, conn).name;
        }
        String addr = index.address + " " + neighName;
        String type = (index.type.equals("brand") ? "Marca" : (index.type.equals("univ") ? "Provisional" : "App"));
        String ent = new Enterprise().select(bill.enterpriseId, conn).name;

        cb.setFontAndSize(bf, 6);
        addText("num1", pqrNum);
        addText("num2", pqrNum);

        cb.setFontAndSize(bf, 9);
        addText("fec1", pqrFec);
        addText("fec2", pqrFec);

        addText("cap1", pqrCap);
        addText("cap2", pqrCap);

        addText("type1", type);
        addText("type2", type);

        addText("client1", clientName);
        addText("client2", clientName);

        addText("phones1", index.phones);
        addText("phones2", index.phones);

        addText("addr1", addr);
        addText("addr2", addr);

        addText("bill1", bill.billNum);
        addText("bill2", bill.billNum);

        addText("rea1", reason.description);
        addText("rea2", reason.description);

        addText("tec1", tecName);
        addText("tec2", tecName);

        addText("ent1", ent);
        addText("ent2", ent);

        List<String> lstParts = new ArrayList<>();
        String str = cfg.pqrCylNotes;
        long pages = (long) Math.ceil(str.length() / 86d); //86 caracteres como máximo por fila en el pdf
        int lastIndex = 0;
        for (int i = 0; i < pages; i++) {
            int lastSpace = 0;
            int end = ((i * 86) + 86 < str.length() ? (i * 86) + 86 : str.length());
            for (int j = i * 86; j < end; j++) {
                if (str.charAt(j) == ' ' && i < (pages - 1)) {
                    lastSpace = j;
                } else if (i == (pages - 1)) {
                    lastSpace = str.length();
                }
            }
            lstParts.add(str.substring(lastIndex, lastSpace));
            lastIndex = lastSpace + 1;
        }

        for (int i = 0; i < lstParts.size(); i++) {
            addText("note1-" + (i + 1), lstParts.get(i));
            addText("note2-" + (i + 1), lstParts.get(i));
        }

    }

    private void addText(String name, String text) {
        cb.saveState();
        cb.beginText();
        float pm = (pos.get(name)[2] + pos.get(name)[4]) / 2.0f;
        cb.moveText(pos.get(name)[1], pm - 2.5f);
        cb.showText(text);
        cb.endText();
        cb.restoreState();
    }

    private class PdfPageEventBill implements PdfPageEvent {

        @Override
        public void onOpenDocument(PdfWriter writer, Document dcmnt) {
            try {
                PdfReader reader = new PdfReader(Reports.getPatternStream("cylPQR.pdf", this));
                paper = writer.getImportedPage(reader, 1);
                not_printed = new PdfLayer("template", writer);
                not_printed.setOnPanel(false);
                not_printed.setPrint("Print", true);
            } catch (Exception ex) {
                Logger.getLogger(CylPQRWriter.class.getName()).log(Level.SEVERE, null, ex);
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
