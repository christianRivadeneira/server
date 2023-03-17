package api.crm.rpt;

import api.ord.model.OrdPollMisc;
import api.ord.model.OrdPollOption;
import api.ord.model.OrdPollQuestion;
import api.ord.model.OrdTextPoll;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import utilities.MySQLQuery;
import utilities.pdf.PDFCellStyle;
import web.fileManager;

public class FormatReport {

    public static final int CRM_POLL_CLIENT = 136;

    private static Document document;
    private static PDFCellStyle titleStyle;
    private static PDFCellStyle titleStyleCenter;
    private static PDFCellStyle titleDocStyleCenter;
    private static PDFCellStyle titleDocStyleCenter2;
    private static PDFCellStyle cellStyle;
    private static PDFCellStyle cellStyleTitle;

    public static File generateReport(Connection conn, int pollId, int pollClientId) throws Exception {
        File file = File.createTempFile("reporteFormato", ".pdf");
        document = new Document(new Rectangle(8.5f * 72f, 13f * 72f), 36f, 36f, 36f, 36f);

        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
        writer.setBoxSize("art", new Rectangle(36, 54, 559, 788));

        titleStyle = new PDFCellStyle();
        titleStyle.setAppearance(true, 4, PDFCellStyle.WHITE, PDFCellStyle.WHITE);
        titleStyle.sethAlignment(PDFCellStyle.ALIGN_LEFT);
        titleStyle.setFontInfo(true, PDFCellStyle.BLACK, (PDFCellStyle.DEFAULT_FONT_SIZE + 2));
        titleStyleCenter = titleStyle.copy();
        titleStyleCenter.sethAlignment(PDFCellStyle.ALIGN_CENTER);
        titleDocStyleCenter = titleStyleCenter.copy();
        titleDocStyleCenter.setFontSize(12f);
        titleDocStyleCenter.setBorder(false);
        titleDocStyleCenter2 = titleDocStyleCenter.copy();
        titleDocStyleCenter2.setFontSize(14f);
        cellStyle = new PDFCellStyle();
        cellStyle.setAppearance(true, 4, PDFCellStyle.WHITE, PDFCellStyle.WHITE);
        cellStyle.sethAlignment(PDFCellStyle.ALIGN_LEFT);
        cellStyle.setFontSize(PDFCellStyle.DEFAULT_FONT_SIZE + 2);
        cellStyle.setBorders(true, true, false, false);
        cellStyleTitle = cellStyle.copy();
        cellStyleTitle.setFontInfo(true, PDFCellStyle.BLACK, (PDFCellStyle.DEFAULT_FONT_SIZE + 2));

        document.open();

        Object[] dataFormat = new MySQLQuery("SELECT p.answer, cp.create_date, c.name, ty.name, c.document, c.address, c.main_contact FROM crm_poll_client cp INNER JOIN ord_poll p ON p.id = cp.poll_id INNER JOIN ord_poll_version v ON v.id = p.poll_version_id INNER JOIN ord_poll_type ty On ty.id = v.ord_poll_type_id INNER JOIN crm_client c ON c.id = cp.client_id WHERE cp.id = " + pollClientId).getRecord(conn);
        Integer versionId = new MySQLQuery("SELECT poll_version_id FROM ord_poll WHERE id = " + pollId + "").getAsInteger(conn);
        Integer pollTypeId = new MySQLQuery("SELECT `ord_poll_type_id` FROM ord_poll_version WHERE id = " + versionId).getAsInteger(conn);
        List<OrdTextPoll> lstText = OrdTextPoll.getListPollId(pollId, conn);
        List<OrdPollMisc> lstMisc = OrdPollMisc.getListPollId(pollId, conn);
        List<OrdPollQuestion> questions = OrdPollQuestion.getListPollTypeId(versionId, conn);
        String pollString = dataFormat[0].toString();

        String questionIds = "";

        for (OrdPollQuestion row : questions) {
            questionIds += row.id + ",";
        }

        if (questionIds.isEmpty()) {
            throw new Exception("La encuesta no tiene preguntas asignadas.");
        }

        List<OrdPollOption> options = OrdPollOption.getListQuestionsId(questionIds.substring(0, questionIds.length() - 1), conn);

        addTitleDocument("FORMATO: " + dataFormat[3].toString());

        addTitleTextDocument("", "", false);
        addTitleTextDocument("Cliente", dataFormat[2].toString(), true);
        addTitleTextDocument("Documento", dataFormat[4].toString(), true);
        addTitleTextDocument("Dirección", dataFormat[5].toString(), true);
        addTitleTextDocument("Contacto", dataFormat[6].toString(), true);
        addTitleTextDocument("", "", false);

        int index = 0;
        for (int i = 0; i < questions.size(); i++) {
            OrdPollQuestion q = questions.get(i);

            OrdPollOption[] opts = getOpcs(q.id, options);
            if (q.misc) {
                addTextTitleDocument(q.text + ": ", true);
                String[] titles = q.miscTitle.split(",");

                for (int j = 0; j < titles.length; j++) {
                    String title = titles[j];

                    String content = getMisc(q.id, j, lstMisc);

                    addTextDocument(title + ": " + content, true);
                }
                index++;
            } else if (q.shortText || q.dateTime || q.longText) {
                String content = getLstText(q.ordinal, lstText);
                addTitleTextDocument(q.text + ": ", content, true);
                index++;
            } else if (q.multiple) {
                String content = "";
                for (int j = 0; j < opts.length; j++) {
                    Integer sel = getBoolean(index, pollString);
                    OrdPollOption op = opts[j];

                    content += op.text + ": ";
                    if (sel != null && sel > 0) {
                        content += "X   ";
                    } else {
                        content += "    ";
                    }
                    index++;
                }
                addTitleTextDocument(q.text + ": ", content, true);
            } else {
                String content = "";
                Integer sel = getBoolean(index, pollString);
                for (int j = 0; j < opts.length; j++) {
                    OrdPollOption op = opts[j];

                    content += op.text + ": ";
                    if (sel != null && sel == (j + 1)) {
                        content += "X   ";
                    } else {
                        content += "    ";
                    }
                }
                index++;
                addTitleTextDocument(q.text + ": ", content, true);
            }
        }

        addTextDocument("", false);
        addTextTitleDocument("Firma:", false);
        addFirm(conn, pollClientId, CRM_POLL_CLIENT);
        addTextDocument("Nit.No.:" + dataFormat[4] + "", false);

        document.close();
        return file;
    }

    public static void addFirm(Connection conn, Integer ownerId, Integer ownerType) throws Exception {
        fileManager.PathInfo pInfo = new fileManager.PathInfo(conn);

        Integer id = new MySQLQuery("SELECT id FROM bfile WHERE owner_id = " + ownerId
                + " AND owner_type = " + ownerType + " AND description LIKE '%Firma%' ORDER BY created ASC LIMIT 1").getAsInteger(conn);

        PdfPTable tabAdj = new PdfPTable(2);
        tabAdj.setWidths(new float[]{30, 70});
        tabAdj.setWidthPercentage(100);
        cellStyle.setBackgroundColor(Color.WHITE);
        cellStyle.setBorders(true, true, true, true);
        PDFCellStyle noBorderStyle = cellStyle.copy();
        noBorderStyle.setAppearance(false, 0, Color.WHITE, Color.WHITE);
        noBorderStyle.sethAlignment(Element.ALIGN_CENTER);
        noBorderStyle.setvAlignment(Element.ALIGN_TOP);

        noBorderStyle.setPaddings(0, 5, 5, 5);

        if (id != null) {
            Image img = Image.getInstance(getFile(id, pInfo));
            img.scalePercent(20);
            img.setWidthPercentage(100);

            PdfPCell imgCell = new PdfPCell(img, false);
            imgCell.setPadding(5);
            img.scaleToFit(10, 10);
            imgCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            imgCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            imgCell.setBorderColor(Color.BLACK);
            imgCell.setBorderWidthBottom(1f);
            imgCell.setBorderWidthTop(0);
            imgCell.setBorderWidthLeft(0);
            imgCell.setBorderWidthRight(0);

            PdfPTable child = new PdfPTable(1);
            child.addCell(imgCell);
            tabAdj.addCell(noBorderStyle.getCell(child, 1, 1));
            tabAdj.addCell(titleDocStyleCenter.getCell(""));
            tabAdj.setSpacingBefore(2f);
            document.add(tabAdj);
        } else {
            addTextDocument("", false);
            addTextDocument("", false);
            addTextDocument("_______________________", false);
        }
    }

    private static byte[] getFile(Integer id, fileManager.PathInfo pInfo) throws Exception {
        File f = pInfo.getExistingFile(id);
        FileInputStream fis = new FileInputStream(f);
        return IOUtils.toByteArray(fis);
    }

    private static void addTitleDocument(String text) throws Exception {
        PdfPTable table = new PdfPTable(1);
        table.setWidths(new float[]{100});
        table.setWidthPercentage(100);

        table.addCell(titleDocStyleCenter.getCell(text));
        table.setSpacingAfter(10);

        document.add(table);
    }

    private static void addTitleTextDocument(String title, String text, boolean border) throws Exception {
        PdfPTable table = new PdfPTable(2);
        table.setWidths(new float[]{20, 80});
        table.setWidthPercentage(100);

        if (border) {
            titleStyle.setBorderWidth(5f);
            titleStyle.setBorderColor(Color.BLACK);
            titleStyle.setBackgroundColor(Color.LIGHT_GRAY);
            titleStyle.setBorders(true, true, true, true);

            cellStyle.setBorderWidth(5f);
            cellStyle.setBorderColor(Color.BLACK);
            cellStyle.setBorders(true, true, true, true);
        } else {
            titleStyle.setBorder(false);
            titleStyle.setBackgroundColor(Color.WHITE);
            cellStyle.setBorder(false);
        }

        table.addCell(titleStyle.getCell(title));
        table.addCell(cellStyle.getCell(text));
        table.setSpacingAfter(0);

        document.add(table);
    }

    private static void addTextDocument(String text, boolean border) throws Exception {
        PdfPTable table = new PdfPTable(1);
        table.setWidths(new float[]{100});
        table.setWidthPercentage(100);

        if (border) {
            cellStyle.setBorderWidth(5f);
            cellStyle.setBorderColor(Color.BLACK);
            cellStyle.setBorders(true, true, true, true);
        } else {
            cellStyle.setBorder(false);
        }
        
        table.addCell(cellStyle.getCell(text));

        document.add(table);
    }

    private static void addTextTitleDocument(String text, boolean border) throws Exception {
        PdfPTable table = new PdfPTable(1);
        table.setWidths(new float[]{100});
        table.setWidthPercentage(100);
        
        if (border) {
            cellStyleTitle.setBorderWidth(5f);
            cellStyleTitle.setBorderColor(Color.BLACK);
            cellStyleTitle.setBackgroundColor(Color.LIGHT_GRAY);
            cellStyleTitle.setBorders(true, true, true, true);
        } else {
            cellStyleTitle.setBorder(false);
            cellStyleTitle.setBackgroundColor(Color.WHITE);
        }

        table.addCell(cellStyleTitle.getCell(text));
        document.add(table);
    }

    private static OrdPollOption[] getOpcs(int questionId, List<OrdPollOption> options) {
        List<OrdPollOption> lst = new ArrayList<>();
        OrdPollOption[] data;

        for (OrdPollOption row : options) {
            if (row.pollQuestionId == questionId) {
                lst.add(row);
            }
        }

        if (lst.size() > 0) {
            data = new OrdPollOption[lst.size()];
            for (int i = 0; i < lst.size(); i++) {
                data[i] = lst.get(i);
            }
        } else {
            data = new OrdPollOption[0];
        }

        return data;
    }

    private static String getMisc(int questionId, int j, List<OrdPollMisc> lstMisc) {
        String misc = "";
        if (lstMisc != null && lstMisc.size() > 0) {
            for (OrdPollMisc row : lstMisc) {
                if (row.questionId == questionId) {
                    switch (j) {
                        case 0:
                            misc += row.text1 + ", ";
                            break;
                        case 1:
                            misc += row.text2 + ", ";
                            break;
                        case 2:
                            misc += row.text3 + ", ";
                            break;
                        case 3:
                            misc += row.text4 + ", ";
                            break;
                        case 4:
                            misc += row.text5 + ", ";
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        return misc;
    }

    private static String getLstText(int ordinal, List<OrdTextPoll> lstText) {
        if (lstText != null && lstText.size() > 0) {
            for (OrdTextPoll text : lstText) {
                if (text.ordinal == ordinal) {
                    return text.text != null ? text.text : " ";
                }
            }
        }
        return null;
    }

    private static Integer getBoolean(int index, String pollString) {
        if (pollString != null && !pollString.isEmpty() && pollString.length() > index) {
            char charAt = pollString.charAt(index);
            return Integer.parseInt(Character.toString(charAt));
        }
        return null;
    }

}
