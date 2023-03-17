package api.bill.writers.note;

import api.bill.model.BillBuilding;
import api.bill.model.BillClientTank;
import api.bill.model.BillFinanceNote;
import api.bill.model.BillFinanceNoteFee;
import api.bill.model.BillInstance;
import api.bill.model.BillSpan;
import api.bill.model.BillTransaction;
import api.sys.model.City;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.billing.BillBank;
import model.billing.constants.Accounts;
import utilities.FileUtils;
import utilities.pdf.PDFFontsHelper;

public class FinanNoteWriter {

    private BaseFont bf;
    private BaseFont bfb;
    private PdfContentByte cb;
    private Map<String, float[]> pos;
    private final SimpleDateFormat shortDateFormat = new SimpleDateFormat("d MMMM yyyy");
    private final DecimalFormat moneyFormat = new DecimalFormat("$###,###,###,##0.00");
    private Document document;
    private PdfWriter writer;
    private File fin;
    private static PdfImportedPage paper;
    private static PdfLayer not_printed;
    //private static float offSet = 468f;
    private final static float offSet = 72.f * 5.5f;
    private int currBill = 0;
    private final BillInstance inst;
    private byte[] logoBytes;

    public FinanNoteWriter(BillInstance inst, Connection conn) throws Exception {
        this.inst = inst;
        logoBytes = FileUtils.getEnterpriseLogo(conn, FileUtils.LogoType.ICON_CAL);
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

    public void addNote(BillFinanceNote note, BillClientTank client, BillBuilding build, BillSpan span, BillBank bank, City city, List<BillFinanceNoteFee> lst) throws Exception {
        if (currBill > 1) {
            document.newPage();
            preparePage();
            currBill = 0;
        }
        drawNote(note, client, build, span, bank, city, lst, currBill);
        currBill++;
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
            reader = new PdfReader(FinanNoteWriter.class.getResourceAsStream("billNoteTemplateNet.pdf"));
        } else {
            reader = new PdfReader(FinanNoteWriter.class.getResourceAsStream("billNoteTemplateTank.pdf"));
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

    public void drawNote(BillFinanceNote note, BillClientTank client, BillBuilding build, BillSpan span, BillBank bank, City city, List<BillFinanceNoteFee> lst, int j) throws Exception {
        cb.beginLayer(not_printed);
        if (j == 0) {
            cb.addTemplate(paper, 0, 0);
        } else {
            cb.addTemplate(paper, 0, -offSet);
        }
        drawLine(0.3f, 5.5f, 8.2f, 5.5f);//hlarga //mitad de la hoja
        cb.endLayer();

        if (logoBytes != null) {
            Image img = Image.getInstance(logoBytes);
            float w = 55f * (img.getWidth() / img.getHeight());
            float xp = ((paper.getBoundingBox().getWidth() * 0.55f) - w) / 2;

            img.setAlignment(Element.ALIGN_CENTER);
            cb.addImage(img, w, 0, 0, 55, xp, paper.getHeight() - 100);
        }

        Date beginDate = span.beginDate;
        Date endDate = span.endDate;

        cb.setFontAndSize(bfb, 9);
     /*   switch (note.typeNotes) {
            case "n_cred":
                addText("title", "Nota Crédito sin Afectación a Bancos", j);
                break;
            case "n_deb":
                addText("title", "Nota Débito sin Afectación a Bancos", j);
                break;
            case "aj_cred":
                addText("title", "Nota Crédito con Afectación a Bancos", j);
                break;
            case "aj_deb":
                addText("title", "Nota Débito con Afectación a Bancos", j);
                break;
            default:
                addText("title", "Otro tipo de nota", j);
                break;
        }*/
        cb.setFontAndSize(bf, 9);
        addText("names", client.firstName + " " + client.lastName, j);
        addText("instNum", inst.isNetInstance() ? client.code : client.numInstall + " - " + inst.name, j);
        addText("notNum", note.id + "", j);
        //addText("date", shortDateFormat.format(note.whenNotes), j);
        addText("address", (build != null ? build.address : client.address), j);
        addText("building", inst.isTankInstance() ? build.name + " " + city.name : city.name, j);
        addText("span", shortDateFormat.format(beginDate) + " - " + shortDateFormat.format(endDate), j);

        BigDecimal total = BigDecimal.ZERO;

        Map<Integer, String> cts = Accounts.accNames;

        for (int i = 0; i < lst.size(); i++) {
            BillFinanceNoteFee trans = lst.get(i);
            String acc;
         /*   switch (note.typeNotes) {
                case "n_cred":
                case "aj_cred":
                    acc = cts.get(trans.accountCredId);
                    break;
                case "n_deb":
                case "aj_deb":
                    acc = cts.get(trans.accountDebId);
                    break;
                default:
                    throw new Exception("Tipo de nota no soportado " + note.typeNotes);
            }*/
            cb.setFontAndSize(bfb, 9);
         //   addText("car" + (i + 1), acc, j);
            cb.setFontAndSize(bf, 9);
            addTextRight("car" + (i + 1), moneyFormat.format(trans.capital), j);
            total = total.add(trans.capital);
        }

        //////////////////////////////////////////////////////////
        addTextRight("total", moneyFormat.format(total), j);

        Paragraph p = new Paragraph();
        if (bank != null) {
            if (bank.numAccount.isEmpty()) {
                p.add(bank.name);
            } else {
                p.add(bank.name + " - " + bank.numAccount);
            }
            p.add("\n\n");
        }
        p.add(note.description);
        ColumnText ct = new ColumnText(cb);
        p.getFont().setSize(9f);
        ct.addElement(p);
        ct.setSimpleColumn(pos.get("desc")[1], pos.get("desc")[2], pos.get("desc")[3], pos.get("desc")[4]);
        ct.go();
    }

    private void addText(String name, String text, int section) {
        cb.saveState();
        cb.beginText();
        //cb.moveText(pos.get(name)[1], pos.get(name)[2] - 2 - (section * offSet));
        cb.moveText(pos.get(name)[1], pos.get(name)[2] + 3 - (section * offSet));
        cb.showText(text);
        cb.endText();
        cb.restoreState();
    }

    private void addTextRight(String name, String text, int section) {
        cb.saveState();
        cb.beginText();
        cb.moveText(pos.get(name)[3] - bf.getWidthPoint(text, 9), pos.get(name)[2] + 3 - (section * offSet));
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
                    reader = new PdfReader(FinanNoteWriter.class.getResourceAsStream("billNoteTemplateNet.pdf"));
                } else {
                    reader = new PdfReader(FinanNoteWriter.class.getResourceAsStream("billNoteTemplateTank.pdf"));
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
