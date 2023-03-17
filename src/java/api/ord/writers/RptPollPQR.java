package api.ord.writers;

import api.ord.model.OrdCfg;
import api.ord.model.OrdContractIndex;
import api.ord.model.OrdPoll;
import api.ord.model.OrdPollOption;
import api.ord.model.OrdPollQuestion;
import api.ord.model.OrdPqrClientTank;
import api.ord.model.OrdPqrCyl;
import api.ord.model.OrdPqrReason;
import api.ord.model.OrdPqrTank;
import api.ord.model.OrdRepairs;
import api.ord.model.OrdTankClient;
import api.ord.model.OrdTechnician;
import api.ord.model.OrdTextPoll;
import api.sys.model.Enterprise;
import api.sys.model.Neigh;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilities.Dates;
import utilities.MySQLQuery;
import utilities.pdf.PDFCellStyle;
import web.enterpriseLogo;
import web.fileManager;

public class RptPollPQR {

    public static final Integer POLL_TYPE_CYL = 3;
    public static final Integer POLL_TYPE_CYL_APP = 15;

    public static final Integer POLL_TYPE_TANK = 5;
    public static final Integer POLL_TYPE_TANK_APP = 16;

    public static final Integer POLL_TYPE_REPAIR = 11;
    public static final Integer POLL_TYPE_REPAIR_APP = 14;

    protected final SimpleDateFormat sdf = Dates.getDefaultFormat();
    protected final SimpleDateFormat sdfHour = new SimpleDateFormat("HH:mm");
    protected int pollVersionId;
    protected int pollTypeId;

    protected Document document;
    protected File fin;
    protected PdfWriter writer;
    protected Image signClient;
    protected Image signTech;
    protected Image check;
    protected Image check2;

    protected String neigt;
    protected List<OrdTextPoll> lstText;
    protected PdfPCell checkCell;
    protected PdfPCell check2Cell;
    protected PDFCellStyle cellStyleTitle;
    protected PDFCellStyle cellStyleSmall;
    protected PDFCellStyle cellStyleLogo1;
    protected PDFCellStyle cellStyleLogo2;
    protected PDFCellStyle cellStyleLogo3;
    protected PDFCellStyle cellStyleLogo;
    protected PDFCellStyle cellStyleTextBold;
    protected PDFCellStyle cellStyleText;
    protected OrdTankClient build;
    protected OrdTankClient buildCli;
    protected OrdPqrClientTank client;
    protected OrdPoll poll;
    protected OrdPqrCyl pqrCyl;
    protected OrdPqrTank pqrTank;
    protected OrdRepairs pqrRepair;
    protected List<OrdPollQuestion> pollQuestions;
    protected List<OrdPollOption> pollOptions;
    protected OrdTextPoll[] textPolls;
    protected OrdContractIndex index;
    protected OrdTechnician tech;
    protected Neigh neigh;
    protected OrdPqrReason reason;
    protected Enterprise enterprise;
    protected OrdCfg cfg;
    protected Connection ep;

    protected void begin(Connection ep, Integer pollTypeId, Integer pqrId) throws Exception {
        this.ep = ep;
        this.pollTypeId = pollTypeId;

        if (Objects.equals(POLL_TYPE_CYL_APP, pollTypeId)) {
            pqrCyl = new OrdPqrCyl().select(pqrId, ep);
            poll = new OrdPoll().select(pqrCyl.pqrPollId, ep);
            index = new OrdContractIndex().select(pqrCyl.indexId, ep);
            reason = new OrdPqrReason().select(pqrCyl.pqrReason, ep);
            tech = new OrdTechnician().select(pqrCyl.technicianId, ep);
            if (index.neighId != null) {
                neigh = new Neigh().select(index.neighId, ep);
            }
            enterprise = new Enterprise().select(pqrCyl.enterpriseId, ep);
            lstText = OrdTextPoll.getAllByPollId(ep, pqrCyl.pqrPollId);
        } else if (Objects.equals(POLL_TYPE_TANK_APP, pollTypeId)) {
            pqrTank = new OrdPqrTank().select(pqrId, ep);
            poll = new OrdPoll().select(pqrTank.pqrPollId, ep);
            if (pqrTank.buildId != null) {
                build = new OrdTankClient().select(pqrTank.buildId, ep);
            } else {
                client = new OrdPqrClientTank().select(pqrTank.clientId, ep);
                if (client.buildOrdId != null) {
                    buildCli = new OrdTankClient().select(client.buildOrdId, ep);
                } else {
                    neigt = new MySQLQuery("SELECT name FROM neigh WHERE id = " + client.neighId).getAsString(ep);
                }
            }
            reason = new OrdPqrReason().select(pqrTank.reasonId, ep);
            tech = new OrdTechnician().select(pqrTank.technicianId, ep);
            lstText = OrdTextPoll.getAllByPollId(ep, pqrTank.pqrPollId);
        } else if (Objects.equals(POLL_TYPE_REPAIR_APP, pollTypeId)) {
            pqrRepair = new OrdRepairs().select(pqrId, ep);
            poll = new OrdPoll().select(pqrRepair.pqrPollId, ep);
            if (pqrRepair.indexId != null) {
                index = new OrdContractIndex().select(pqrRepair.indexId, ep);
                if (index.neighId != null) {
                    neigh = new Neigh().select(index.neighId, ep);
                }
            } else if (pqrRepair.buildId != null) {
                buildCli = new OrdTankClient().select(pqrRepair.buildId, ep);
                neigt = new MySQLQuery("SELECT name FROM neigh WHERE id = " + buildCli.neighId).getAsString(ep);
            } else if (pqrRepair.clientId != null) {
                client = new OrdPqrClientTank().select(pqrRepair.clientId, ep);
                if (client.buildOrdId != null) {
                    buildCli = new OrdTankClient().select(client.buildOrdId, ep);
                } else {
                    neigt = new MySQLQuery("SELECT name FROM neigh WHERE id = " + client.neighId).getAsString(ep);
                }
            }
            reason = new OrdPqrReason().select(pqrRepair.reasonId, ep);
            tech = new OrdTechnician().select(pqrRepair.technicianId, ep);
            lstText = OrdTextPoll.getAllByPollId(ep, pqrRepair.pqrPollId);
        } else {
            throw new Exception("Tipo no reconocido");
        }

        pollQuestions = OrdPollQuestion.getByTypeQuery(ep, pollTypeId, poll.pollVersionId);
        pollVersionId = poll.pollVersionId;
        String questionsId = "";
        for (int i = 0; i < pollQuestions.size(); i++) {
            questionsId += pollQuestions.get(i).id + (i >= pollQuestions.size() - 1 ? "" : ",");
        }

        MySQLQuery qPollOptions = new MySQLQuery("SELECT " + OrdPollOption.getSelFlds("opo")
                + "FROM ord_poll_option opo "
                + "WHERE opo.poll_question_id IN ( " + questionsId + ") "
                + "ORDER BY opo.ordinal ");
        pollOptions = OrdPollOption.getList(qPollOptions, ep);

        cfg = new OrdCfg().select(1, ep);

        //Consulta Firmas               
        Integer bfClientId = new MySQLQuery("SELECT id FROM bfile WHERE owner_id = " + poll.id + " AND owner_type = " + FrmAttachments.ORD_POLL_SIGN + " ORDER BY updated DESC LIMIT 1").getAsInteger(ep);
        Integer bfTechId = new MySQLQuery("SELECT id FROM bfile WHERE owner_id = " + tech.id + " AND owner_type = " + FrmAttachments.ORD_POLL_TECH + " ORDER BY updated DESC LIMIT 1").getAsInteger(ep);

        signClient = (bfClientId != null ? setSignatures(bfClientId, ep) : null);
        signTech = (bfTechId != null ? setSignatures(bfTechId, ep) : null);

        check = Image.getInstance(getClass().getResource("/api/ord/writers/chk_off.wmf"));
        check.scaleAbsolute(11, 11);
        check2 = Image.getInstance(getClass().getResource("/api/ord/writers/chk_on.wmf"));
        check2.scaleAbsolute(11, 11);

        checkCell = new PdfPCell(check);
        checkCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        checkCell.setBorder(0);
        checkCell.setPaddingTop(3);

        check2Cell = new PdfPCell(check2);
        check2Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        check2Cell.setBorder(0);
        check2Cell.setPaddingTop(3);

        cellStyleLogo = new PDFCellStyle();
        cellStyleLogo.setFontSize(11);
        cellStyleLogo.setBorderColor(PDFCellStyle.BLACK);
        cellStyleLogo.sethAlignment(Element.ALIGN_CENTER);
        cellStyleLogo.setBold(true);

        cellStyleLogo1 = new PDFCellStyle();
        cellStyleLogo1.setFontSize(11);
        cellStyleLogo1.setBorderColor(PDFCellStyle.BLACK);
        cellStyleLogo1.sethAlignment(Element.ALIGN_CENTER);
        cellStyleLogo1.setBorders(false, true, true, true);
        cellStyleLogo1.setBold(true);

        cellStyleLogo2 = new PDFCellStyle();
        cellStyleLogo2.setFontSize(8);
        cellStyleLogo2.setBorderColor(PDFCellStyle.BLACK);
        cellStyleLogo2.sethAlignment(Element.ALIGN_LEFT);
        cellStyleLogo2.setBorders(true, false, true, true);
        cellStyleLogo2.setBold(false);

        cellStyleLogo3 = new PDFCellStyle();
        cellStyleLogo3.setFontSize(8);
        cellStyleLogo3.setBorderColor(PDFCellStyle.BLACK);
        cellStyleLogo3.sethAlignment(Element.ALIGN_CENTER);
        cellStyleLogo3.setBold(false);

        cellStyleTextBold = new PDFCellStyle();
        cellStyleTextBold.setFontSize(11);
        cellStyleTextBold.setBorderColor(PDFCellStyle.WHITE);
        cellStyleTextBold.sethAlignment(Element.ALIGN_LEFT);
        cellStyleTextBold.setBold(true);

        cellStyleText = new PDFCellStyle();
        cellStyleText.setFontSize(11);
        cellStyleText.setBorderColor(PDFCellStyle.WHITE);
        cellStyleText.sethAlignment(Element.ALIGN_LEFT);

        cellStyleSmall = new PDFCellStyle();
        cellStyleSmall.setFontSize(6);
        cellStyleSmall.setBorderColor(PDFCellStyle.BLACK);
        cellStyleSmall.setBorders(false, true, true, true);
        cellStyleSmall.sethAlignment(Element.ALIGN_CENTER);

        cellStyleTitle = new PDFCellStyle();
        cellStyleTitle.setFontSize(11);
        cellStyleTitle.setBorderColor(PDFCellStyle.WHITE);
        cellStyleTitle.sethAlignment(Element.ALIGN_CENTER);
        cellStyleTitle.setBold(true);
        cellStyleTitle.setPaddings(5, 5, 0, 0);
    }

    protected void loadHead() throws Exception {
        PdfPCell imgCell;
        try {
            Image logo;
            try {
                File file = enterpriseLogo.getEnterpriseLogo("4", ep);
                byte[] readAllBytes = Files.readAllBytes(file.toPath());
                logo = Image.getInstance(readAllBytes);
            } catch (Exception ex) {
                Logger.getLogger(RptPollPQR.class.getName()).log(Level.SEVERE, null, ex);
                logo = null;
            }
            if (logo != null) {
                logo.setAlignment(Element.ALIGN_CENTER);
                logo.scaleToFit(100, 100);
                imgCell = new PdfPCell(logo);
            } else {
                imgCell = new PdfPCell();
            }
        } catch (Exception ex) {
            Logger.getLogger(RptPollPQR.class.getName()).log(Level.SEVERE, null, ex);
            imgCell = new PdfPCell();
        }
        imgCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        imgCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        imgCell.setRowspan(4);
        PdfPTable logoTab = new PdfPTable(6);
        logoTab.setWidths(new float[]{20, 32, 12, 12, 12, 12});
        logoTab.setWidthPercentage(100);
        logoTab.addCell(imgCell);

        String cod = "", title = "", vig = "", cons = "";
        if (Objects.equals(POLL_TYPE_CYL_APP, pollTypeId)) {
            title = "FORMATO DE PQR FUGAS (CILINDROS)";
            vig = "VIGENCIA \n22-sep-22";
            cod = "CÓDIGO \n40-045.2-0010";
            cons = "CONSECUTIVO \n" + pqrCyl.serial;
        } else if (Objects.equals(POLL_TYPE_TANK_APP, pollTypeId)) {
            title = "FORMATO DE PQR FUGAS (ESTACIONARIOS)";
            vig = "VIGENCIA \n22-sep-22";
            cod = "CÓDIGO \n40.045.2-0011";
            cons = "CONSECUTIVO \n" + pqrTank.serial;
        } else if (Objects.equals(POLL_TYPE_REPAIR_APP, pollTypeId)) {
            title = "SOLICITUD DE VISITA DE ASISTENCIA TÉCNICA";
            vig = "VIGENCIA \n22-sep-22";
            cod = "CÓDIGO \n40.045.2-0012";
            cons = "CONSECUTIVO \n" + pqrRepair.serial;
        } else {
            throw new Exception("Tipo no reconocido");
        }
        logoTab.addCell(cellStyleLogo.getCell("MONTAGAS S.A E.S.P\nNIT 891202203-9", 5, 1));
        if (Objects.equals(POLL_TYPE_REPAIR_APP, pollTypeId)) {
            logoTab.addCell(cellStyleLogo2.getCell("NOMBRE DEL FORMATO:", 5, 1));
        } else {
            logoTab.addCell(cellStyleLogo2.getCell("NOMBRE DEL PROCEDIMIENTO:", 5, 1));
        }
        logoTab.addCell(cellStyleLogo1.getCell(title, 5, 1));
        logoTab.addCell(cellStyleLogo.getCell("SUBPROCESO DE SERVICIO Y ATENCIÓN AL CLIENTE"));
        logoTab.addCell(cellStyleLogo3.getCell(vig));
        logoTab.addCell(cellStyleLogo3.getCell("VERSIÓN \n3"));
        logoTab.addCell(cellStyleLogo3.getCell(cod));
        logoTab.addCell(cellStyleLogo3.getCell(cons));

        logoTab.setSpacingAfter(10);
        document.add(logoTab);
    }

    protected void underlined(PDFCellStyle cellStyle, boolean visible) {
        cellStyle.setBorderColor(Color.LIGHT_GRAY);
        cellStyle.setBorders(false, visible, false, false);
    }

    protected void loadSignatures() throws Exception {
        PdfPTable signTab = new PdfPTable(5);
        signTab.setWidths(new float[]{10, 35, 10, 35, 10});
        signTab.setWidthPercentage(100);

        PdfPCell cellClient = null;
        if (signClient != null) {
            signClient.setAlignment(PDFCellStyle.ALIGN_CENTER);
            signClient.scaleAbsolute(140, 60);
            signClient.setBorderColorBottom(Color.BLACK);
            signClient.setBorderWidthBottom(1);
            cellClient = new PdfPCell(signClient);
            cellClient.setBorder(0);
        }

        PdfPCell cellTech = null;
        if (signTech != null) {
            signTech.setAlignment(PDFCellStyle.ALIGN_CENTER);
            signTech.scaleAbsolute(140, 60);
            signTech.setBorderColorBottom(Color.BLACK);
            signTech.setBorderWidthBottom(1);
            cellTech = new PdfPCell(signTech);
            cellTech.setBorder(0);
        }
        cellStyleText.setBorderColor(Color.WHITE);
        cellStyleText.setvAlignment(Element.ALIGN_BOTTOM);
        signTab.addCell(cellStyleText.getCell(""));

        if (signClient != null) {
            signTab.addCell(cellClient);
        } else {
            signTab.addCell(cellStyleText.getCell("_________________________________"));
        }
        signTab.addCell(cellStyleText.getCell(""));
        if (signTech != null) {
            signTab.addCell(cellTech);
        } else {
            signTab.addCell(cellStyleText.getCell("_________________________________"));
        }
        signTab.addCell(cellStyleText.getCell(""));

        signTab.addCell(cellStyleText.getCell(""));
        signTab.addCell(cellStyleText.getCell("Firma de Cliente"));
        signTab.addCell(cellStyleText.getCell(""));
        signTab.addCell(cellStyleText.getCell("Atendido por"));
        signTab.addCell(cellStyleText.getCell(""));

        if (signClient == null && signTech == null) {
            signTab.setSpacingBefore(30);
        }
        document.add(signTab);
    }

    protected void longText(PdfPTable table, PDFCellStyle cellStyle, String title, String text, int colSpan, int capTitle, boolean extras) throws Exception {
        underlined(cellStyle, true);
        if (title != null) {
            table.addCell(cellStyleTextBold.getCell(title));
        }

        if (text != null) {
            String[] split = text.split(" ");
            if (split.length > 0) {
                text = split[0];
                for (int i = 1; i < split.length; i++) {
                    if (text.length() + split[i].length() + 1 > (title != null ? (108 - capTitle) : 108)) {
                        if (title != null) {
                            table.addCell(cellStyle.getCell(text, colSpan - 1, 1));
                            title = null;
                        } else {
                            table.addCell(cellStyle.getCell(text, colSpan, 1));
                        }
                        text = "";
                    } else if (i >= split.length - 1) {
                        table.addCell(cellStyle.getCell(text + " " + split[i], colSpan, 1));
                    }
                    text += (!text.isEmpty() ? " " : "") + split[i];
                }
                if (split.length == 1 && !text.isEmpty()) {
                    table.addCell(cellStyle.getCell(text, colSpan - 1, 1));
                }
                if (extras) {
                    table.addCell(cellStyle.getCell(" ", colSpan, 1));
                }
            }
        }
        underlined(cellStyle, false);
    }

    protected Paragraph separator() {
        Font font = new Font(BaseFont.FONT_TYPE_DOCUMENT, 1, 100, Color.LIGHT_GRAY);
        String str = "";
        for (int i = 0; i < 165; i++) {
            str += "____";
        }
        Paragraph sep = new Paragraph(str, font);
        return sep;
    }

    protected OrdPollOption[] returnOptionsQuestion(Integer questionId) {
        List<OrdPollOption> lstOptions = new ArrayList<>();
        for (OrdPollOption row : pollOptions) {
            if (row.pollQuestionId == questionId) {
                lstOptions.add(row);
            }
        }

        if (lstOptions.size() > 0) {
            OrdPollOption[] options = new OrdPollOption[lstOptions.size()];
            for (int i = 0; i < lstOptions.size(); i++) {
                options[i] = lstOptions.get(i);
            }
            return options;
        } else {
            return null;
        }
    }

    protected File endDocument() throws Exception {
        document.close();
        return fin;
    }

    protected void beginDocument() throws Exception {
        document = new Document(new Rectangle(8.5f * 70f, 11f * 72f), 25f, 25f, 20f, 20f);
        //fin = OpenFile.getConsecutiveFile("encuesta.pdf"); TODO
        fin = File.createTempFile("encuesta", ".pdf");
        writer = PdfWriter.getInstance(document, new FileOutputStream(fin));
        writer.setBoxSize("art", new Rectangle(36, 54, 559, 788));
        document.open();
    }

    protected String getText(int ordinal) {
        if (lstText != null && lstText.size() > 0) {
            for (OrdTextPoll text : lstText) {
                if (text.ordinal == ordinal) {
                    return text.text != null ? text.text : " ";
                }
            }
        }
        return null;
    }

    protected Integer getBoolean(int index) {
        if (poll.answer != null && !poll.answer.isEmpty()) {
            char charAt = poll.answer.charAt(index);
            return Integer.parseInt(Character.toString(charAt));
        }
        return null;
    }

    protected Image setSignatures(Integer bfileId, Connection con) throws Exception {

        File f = new fileManager.PathInfo(con).getExistingFile(bfileId);
        InputStream is = new FileInputStream(f);

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
