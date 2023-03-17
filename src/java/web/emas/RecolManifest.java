package web.emas;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import utilities.MySQLQuery;
import utilities.pdf.PDFCellStyle;
import utilities.pdf.PDFFontsHelper;
import web.fileManager;

public class RecolManifest {

    private final Connection conn;

    private Document document;
    private File fin;

    private final Object[] sedeData;
    private final Object[][] amountData;
    Object[][] resType;
    private Image recolSignature, clientSignature;

    private HeaderFooter event;

    public static final int EMAS_RECOL_SIGNATURE = 83;//ids de FrmAttachments
    public static final int EMAS_CLIENT_SIGNATURE = 84;

    public RecolManifest(Connection conn, int recolVisitId, Integer signClientOnwerId) throws Exception {
        this.conn = conn;
        this.sedeData = new MySQLQuery("SELECT "
                + "d.`name`,"
                + "s.address,"
                + "v.dt,"
                + "TIME(v.end_date),"
                + "s.`name`, "
                + "s.phone, "
                + "CONCAT(e.first_name,' ',e.last_name), "
                + "ve.internal,"
                + "ve.plate,"
                + "v.notes,"
                + "e.document,"
                + "c.nit,"
                + "rc.name,"
                + "v.man_num "
                + "FROM emas_recol_visit v "
                + "INNER JOIN emas_clie_sede s ON v.clie_sede_id=s.id "
                + "INNER JOIN emas_client c ON s.client_id = c.id "
                + "INNER JOIN employee e ON e.id=v.emp_recol_id "
                + "INNER JOIN dane_poblado d ON d.id=s.dane_pob_id "
                + "INNER JOIN emas_vehicle ve ON v.vehicle_id=ve.id "
                + "INNER JOIN emas_res_class rc ON rc.id=ve.class_id "
                + "WHERE v.id = ?1").setParam(1, recolVisitId).getRecord(conn);

        this.amountData = new MySQLQuery("SELECT "
                + "rt.`name`,"
                + "a.amount,"
                + "ct.`name`,"
                + "a.num_container,"
                + "a.color "
                + "FROM emas_amount a "
                + "INNER JOIN emas_recol_visit v ON a.recol_visit_id=v.id "
                + "INNER JOIN emas_res_type rt ON rt.id=a.res_type_id "
                + "INNER JOIN emas_container_type ct ON ct.id=a.container_id "
                + "WHERE v.id = ?1").setParam(1, recolVisitId).getRecords(conn);

        resType = new MySQLQuery("SELECT "
                + "rt.`name` "
                + "FROM emas_res_type rt "
                + "INNER JOIN emas_res_class rc ON rt.class_id=rc.id "
                + "INNER JOIN emas_vehicle v ON v.class_id = rc.id "
                + "WHERE v.id = (SELECT vehicle_id FROM emas_recol_visit WHERE id = ?1)").setParam(1, recolVisitId).getRecords(conn);

        try {
            Integer recolFileId = new MySQLQuery("SELECT id "
                    + "FROM bfile "
                    + "WHERE owner_id = (SELECT emp_recol_id FROM emas_recol_visit WHERE id = ?1) "
                    + "AND owner_type = ?2").setParam(1, recolVisitId).setParam(2, EMAS_RECOL_SIGNATURE).getAsInteger(conn);

            Integer clientFileId = new MySQLQuery("SELECT id "
                    + "FROM bfile "
                    + "WHERE owner_id = ?1 "
                    + "AND owner_type = ?2").setParam(1, signClientOnwerId).setParam(2, EMAS_CLIENT_SIGNATURE).getAsInteger(conn);
            
            if (recolFileId != null) {
                recolSignature = setSignatures(recolFileId);
            }

            if (clientFileId != null) {
                clientSignature = setSignatures(clientFileId);
            }

        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
        }

    }

    private void beginDocument() throws Exception {
        document = new Document(new Rectangle(11f * 72f, 10f * 72f), 15f, 15f, 20f, 20f);
        fin = File.createTempFile("manifiesto_recoleccion", ".pdf");
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fin));
        event = new RecolManifest.HeaderFooter();
        writer.setBoxSize("art", new Rectangle(36, 54, 559, 788));
        writer.setPageEvent(event);
        document.open();
    }

    private Image setSignatures(Integer bfileId) throws Exception {
        fileManager.PathInfo pInfo = new fileManager.PathInfo(conn);        
        InputStream is = new FileInputStream(pInfo.getExistingFile(bfileId));

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] dataImage = new byte[16384];

        while ((nRead = is.read(dataImage, 0, dataImage.length)) != -1) {
            buffer.write(dataImage, 0, nRead);
        }

        buffer.flush();
        return Image.getInstance(buffer.toByteArray());

    }

    public File generateReport() throws Exception {
        try {
            beginDocument();
            PDFCellStyle cellStyleCenter = new PDFCellStyle();
            cellStyleCenter.setBorderColor(PDFCellStyle.GRAY_BORDER);
            cellStyleCenter.sethAlignment(Element.ALIGN_CENTER);

            PDFCellStyle cellStyleLeft = cellStyleCenter.copy();
            cellStyleLeft.sethAlignment(Element.ALIGN_LEFT);

            Map<Integer, String> colors = new HashMap<Integer, String>();
            colors.put(0, "Blanco");
            colors.put(1, "Rojo");
            colors.put(2, "Café");
            colors.put(3, "Negro");

            PdfPTable tab = getTableData();
            double totalKg = 0;

            if (amountData != null && amountData.length > 0) {

                for (int i = 0; i < resType.length; i++) {

                    boolean found = false;
                    tab.addCell(cellStyleLeft.getCell(resType[i][0].toString().toUpperCase()));
                    for (int j = 0; j < amountData.length; j++) {

                        if (amountData[j][0].equals(resType[i][0])) {
                            tab.addCell(cellStyleCenter.getCell("" + MySQLQuery.getAsDouble(amountData[j][1])));
                            tab.addCell(cellStyleCenter.getCell(amountData[j][2].toString()));
                            tab.addCell(cellStyleCenter.getCell(amountData[j][3].toString()));
                            tab.addCell(cellStyleCenter.getCell(colors.get(MySQLQuery.getAsInteger(amountData[j][4]))));
                            totalKg += MySQLQuery.getAsDouble(amountData[j][1]);
                            System.out.println("amountData: " + MySQLQuery.getAsDouble(amountData[j][1]));
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        tab.addCell(cellStyleCenter.getCell("0.0"));
                        tab.addCell(cellStyleCenter.getCell("-"));
                        tab.addCell(cellStyleCenter.getCell("0"));
                        tab.addCell(cellStyleCenter.getCell("-"));
                    }

                }

                PDFCellStyle cellTotal = cellStyleLeft.copy();
                cellTotal.setFontSize(10f);
                cellTotal.setBold(true);

                tab.addCell(cellTotal.getCell("Total", 1, 1));
                cellTotal.sethAlignment(Element.ALIGN_CENTER);
                tab.addCell(cellTotal.getCell(String.format("%.1f", totalKg), 1, 1));
                tab.addCell(cellTotal.getCell("", 3, 1));

                PDFCellStyle cellObs = cellTotal.copy();
                cellObs.sethAlignment(Element.ALIGN_LEFT);
                cellObs.setBorders(true, false, true, true);

                tab.addCell(cellObs.getCell("Observaciones:", 6, 1));

                cellObs.setBorders(false, true, true, true);
                cellObs.setBold(false);
                tab.addCell(cellObs.getCell((sedeData[9] == null ? "" : sedeData[9].toString()), 6, 1));

            } else if ((amountData == null || amountData.length == 0) && clientSignature != null) {

                PDFCellStyle noResidues = cellStyleCenter.copy();
                noResidues.setBold(true);
                noResidues.setFontSize(10);
                noResidues.setPadding(10);
                tab.addCell(noResidues.getCell("NO SE RECOLECTARON RESIDUOS", 5, 1));

            } else if ((amountData == null || amountData.length == 0) && clientSignature == null) {

                PDFCellStyle noResidues = cellStyleCenter.copy();
                noResidues.setBold(true);
                noResidues.setFontSize(10);
                noResidues.setPadding(10);
                tab.addCell(noResidues.getCell("EL CLIENTE ESTUVO AUSENTE", 5, 1));

            }

            //==============
            PDFCellStyle cellDeliverReceive = cellStyleCenter.copy();
            cellDeliverReceive.setFontSize(10f);
            cellDeliverReceive.setBold(true);

            PdfPTable infoTab = new PdfPTable(new float[]{20, 20});

            infoTab.addCell(cellDeliverReceive.getCell("ENTREGA", 1, 1));
            infoTab.addCell(cellDeliverReceive.getCell("RECIBE", 1, 1));

            PdfPCell signatureCell;
            if (recolSignature != null) {
                recolSignature.scaleToFit(150, 132);
                signatureCell = new PdfPCell(recolSignature);
                signatureCell.setBorderColor(PDFCellStyle.GRAY_BORDER);
                signatureCell.setColspan(1);
                signatureCell.setPaddingTop(5);
                signatureCell.setPaddingBottom(5);
                signatureCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                infoTab.addCell(signatureCell);
            } else {
                infoTab.addCell(cellDeliverReceive.getCell("", 1, 1));
            }

            if (clientSignature != null) {
                clientSignature.scaleToFit(150, 132);
                signatureCell = new PdfPCell(clientSignature);
                signatureCell.setBorderColor(PDFCellStyle.GRAY_BORDER);
                signatureCell.setColspan(1);
                signatureCell.setPaddingTop(5);
                signatureCell.setPaddingBottom(5);
                signatureCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                infoTab.addCell(signatureCell);
            } else {
                infoTab.addCell(cellDeliverReceive.getCell("", 1, 1));
            }

            PDFCellStyle cellSigns = cellDeliverReceive.copy();
            cellSigns.sethAlignment(Element.ALIGN_LEFT);
            cellSigns.setBold(false);

            infoTab.addCell(cellSigns.getCell("Firma", 1, 1));
            infoTab.addCell(cellSigns.getCell("Firma", 1, 1));

            infoTab.addCell(cellSigns.getCell("C.C.    " + sedeData[10].toString(), 1, 1));
            infoTab.addCell(cellSigns.getCell("NIT    " + (sedeData[11] != null ? sedeData[11] : ""), 1, 1));

            PdfPCell infoCell = new PdfPCell(infoTab);
            infoCell.setBorder(0);
            infoCell.setColspan(5);
            tab.addCell(infoCell);

            PDFCellStyle cellFooter = cellSigns.copy();
            cellFooter.setFontSize(7f);

            tab.addCell(cellFooter.getCell("EMAS CRA 24 No. 23-51 Línea de Atención al cliente 018000950096 línea 110 ", 6, 1));

            //===========        
            document.add(tab);
            document.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return fin;
    }

    class HeaderFooter extends PdfPageEventHelper {

        PdfTemplate total;

        @Override
        public void onOpenDocument(PdfWriter writer, Document dcmnt) {
            total = writer.getDirectContent().createTemplate(100, 100);
            total.setBoundingBox(new Rectangle(-20, -20, 100, 100));
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                PdfContentByte cb = writer.getDirectContent();
                cb.saveState();
                String text = "Página " + writer.getPageNumber() + " de ";
                cb.saveState();
                cb.setFontAndSize(PDFFontsHelper.getRegular(), 9);
                cb.beginText();
                float y = document.getPageSize().getHeight() - 27;
                cb.moveText(15, y);
                cb.showText(text);
                cb.endText();
                cb.restoreState();
                cb.addTemplate(total, 15 + PDFFontsHelper.getRegular().getWidthPoint(text, 9), y);
                cb.restoreState();
            } catch (Exception ex) {
            }
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            try {
                total.beginText();
                total.setFontAndSize(PDFFontsHelper.getRegular(), 9);
                total.setTextMatrix(0, 0);
                total.showText(String.valueOf(writer.getPageNumber() - 1));
                total.endText();
            } catch (Exception ex) {
                Logger.getLogger(RecolManifest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private PdfPTable getTableData() throws Exception {

        PDFCellStyle printed = new PDFCellStyle();
        printed.setAppearance(false, 0, PDFCellStyle.WHITE, PDFCellStyle.GRAY_BORDER);

        PDFCellStyle titleStyle = printed.copy();
        titleStyle.setBold(true);
        titleStyle.setFontSize(10f);
        titleStyle.setPadding(1);

        PDFCellStyle colTitleStyle = new PDFCellStyle();
        colTitleStyle.setAppearance(false, 5, PDFCellStyle.GRAY_BACKGROUND, PDFCellStyle.GRAY_BORDER);
        colTitleStyle.setFontSize(10f);
        colTitleStyle.setBold(true);

        PDFCellStyle infoTitleStyle = new PDFCellStyle();
        infoTitleStyle.setBold(true);
        infoTitleStyle.setBorder(false);
        infoTitleStyle.sethAlignment(Element.ALIGN_LEFT);

        PDFCellStyle infoStyle = infoTitleStyle.copy();
        infoStyle.setBold(false);

        PdfPTable tab = new PdfPTable(5);
        tab.setHeaderRows(9);
        tab.setWidthPercentage(100);
        int[] widths = new int[5];
        widths[0] = 9;//Tipo de Residuo
        widths[1] = 7;//Cantidad (kg)
        widths[2] = 9;//Tipo de Recipiente
        widths[3] = 6;//No. de Recipientes
        widths[4] = 6;//Color

        Date date = new Date();
        SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd");

        PdfPCell printCell = printed.getCell("Impreso: " + d.format(date), 6, 1);
        printCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tab.addCell(printCell);

        printCell = titleStyle.getCell("No. " + sedeData[13], 6, 1);
        printCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tab.addCell(printCell);

        Image img = Image.getInstance(this.getClass().getResource("/icons/emas/emas.png"));
        img.setAlignment(Element.ALIGN_CENTER);
        img.scaleToFit(150, 132);

        PdfPCell imgCell = new PdfPCell(img);
        imgCell.setBorder(0);
        imgCell.setColspan(6);
        imgCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tab.addCell(imgCell);

        PdfPCell c1 = titleStyle.getCell("MANIFIESTO SERVICIO\nRESIDUOS " + sedeData[12].toString().toUpperCase(), 6, 1);
        c1.setPaddingTop(0);
        c1.setPaddingBottom(10);
        tab.addCell(c1);

        tab.addCell(colTitleStyle.getCell("DATOS DEL GENERADOR", 6, 1));

        PdfPTable infoTab = new PdfPTable(new float[]{10, 10, 10, 10});

        infoTab.addCell(infoTitleStyle.getCell("CIUDAD:", 1, 1));
        infoTab.addCell(infoStyle.getCell("" + sedeData[0].toString().toUpperCase(), 1, 1));
        infoTab.addCell(infoTitleStyle.getCell("DIRECCIÓN:", 1, 1));
        infoTab.addCell(infoStyle.getCell(sedeData[1] != null ? sedeData[1].toString().toUpperCase() : "", 1, 1));

        infoTab.addCell(infoTitleStyle.getCell("FECHA DE RECOLECCIÓN:", 1, 1));
        infoTab.addCell(infoStyle.getCell(sedeData[2].toString().toUpperCase(), 1, 1));
        infoTab.addCell(infoTitleStyle.getCell("HORA:", 1, 1));
        infoTab.addCell(infoStyle.getCell(sedeData[3] != null ? new SimpleDateFormat("hh:mm:ss a").format(sedeData[3]) : "", 1, 1));

        infoTab.addCell(infoTitleStyle.getCell("ESTABLECIMIENTO:", 1, 1));
        infoTab.addCell(infoStyle.getCell(sedeData[4].toString().toUpperCase(), 3, 1));

        infoTab.addCell(infoTitleStyle.getCell("CÓDIGO:", 1, 1));
        infoTab.addCell(infoStyle.getCell("", 1, 1));
        infoTab.addCell(infoTitleStyle.getCell("TELÉFONO:", 1, 1));
        infoTab.addCell(infoStyle.getCell(sedeData[5] != null ? sedeData[5].toString().toUpperCase() : "", 1, 1));

        PdfPCell infoCell = new PdfPCell(infoTab);
        infoCell.setBorder(0);
        infoCell.setColspan(11);
        infoCell.setPaddingTop(5);
        infoCell.setPaddingBottom(5);
        tab.addCell(infoCell);

        tab.addCell(colTitleStyle.getCell("DATOS DEL SERVICIO", 6, 1));

        infoTab = new PdfPTable(new float[]{10, 10, 10, 10});

        infoTab.addCell(infoTitleStyle.getCell("RECOLECTOR:", 1, 1));
        infoTab.addCell(infoStyle.getCell(sedeData[6].toString().toUpperCase(), 3, 1));

        infoTab.addCell(infoTitleStyle.getCell("VEHÍCULO:", 1, 1));
        infoTab.addCell(infoStyle.getCell("" + sedeData[7].toString().toUpperCase(), 1, 1));
        infoTab.addCell(infoTitleStyle.getCell("PLACA:", 1, 1));
        infoTab.addCell(infoStyle.getCell(sedeData[8].toString().toUpperCase(), 1, 1));

        infoCell = new PdfPCell(infoTab);
        infoCell.setBorder(0);
        infoCell.setColspan(11);
        infoCell.setPaddingTop(5);
        infoCell.setPaddingBottom(5);
        tab.addCell(infoCell);

        tab.addCell(colTitleStyle.getCell("Tipo de Residuo"));//0
        tab.addCell(colTitleStyle.getCell("Cantidad (KG)"));//1
        tab.addCell(colTitleStyle.getCell("Tipo de Recipiente"));//2
        tab.addCell(colTitleStyle.getCell("No. de Recipientes"));//3
        tab.addCell(colTitleStyle.getCell("Color"));//4
        tab.setWidths(widths);
        return tab;
    }

}
