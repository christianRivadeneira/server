package printout;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Employee;
import model.MtoContractor;
import printout.basics.MtoChkElement;
import printout.basics.MtoChkLst;
import printout.basics.MtoChkType;
import printout.basics.MtoChkVal;
import printout.basics.MtoChkVersion;
import printout.basics.cols.MtoChkCol;
import printout.basics.groups.MtoChkGrp;
import printout.basics.rows.MtoChkRow;
import utilities.MySQLQuery;
import utilities.pdf.PDFCellStyle;
import utilities.pdf.PDFFontsHelper;

public class PrintFormatInvPas extends PrintFormatsGenerator {

    private HeaderFooter event;
    private MtoChkLst lst;
    private MtoChkVersion ver;
    private MtoChkType type;
    private MtoChkGrp[] grps;
    private MtoChkCol[][] cols;
    private MtoChkRow[][] rows;
    private MtoChkElement[] vhElement = new MtoChkElement[0];
    private Employee driver;
    private Employee auxDriver;
    private MtoContractor contractor;
    private PDFCellStyle titleStyle;
    private PDFCellStyle cellStyle;
    private PDFCellStyle notesStyle;

    private Employee creator;
    private Object[][] vehicleD;
    private Object[][] docsVhD;
    private Object[][] driverD;
    private Object[][] auxDriverD;
    private MtoChkVal[][] ans;
    private ColumnText ct = null;

    private Image signDriv;
    private Image signResp;

    @Override
    public File initFormat(Connection ep, Integer registId) throws Exception {
        this.ep = ep;
        lst = new MtoChkLst().select(registId, ep);
        creator = new Employee().select(lst != null ? lst.creatorId : null, ep);
        vehicleD = new MySQLQuery("SELECT "//vehicle
                + "COALESCE(v.internal,''),"
                + "vc.`name`, "
                + "vt.`name`, "
                + "v.plate, "
                + "e.`name` "
                + "FROM vehicle AS v "
                + "INNER JOIN vehicle_type AS vt ON vt.id = v.vehicle_type_id "
                + "INNER JOIN vehicle_class AS vc ON vc.id = vt.vehicle_class_id "
                + "INNER JOIN agency AS a ON a.id = v.agency_id "
                + "INNER JOIN enterprise AS e ON e.id = a.enterprise_id "
                + "WHERE v.id = " + lst.vhId).getRecords(ep);
        docsVhD = new MySQLQuery("SELECT "//docsVh
                + "d.description, "
                + "dv.doc_date "
                + "FROM document AS d "
                + "INNER JOIN mto_chk_doc AS dv ON dv.vehicle_doc_type_id = d.id "
                + "WHERE dv.lst_id = " + lst.id).getRecords(ep);
        String qDocsDrivers = "SELECT "
                + "d.description, "
                + "dd.doc_date "
                + "FROM mto_driver_doc_type AS d "
                + "INNER JOIN mto_chk_doc AS dd ON dd.driver_doc_type_id = d.id "
                + "WHERE dd.driver_id = ?1 AND dd.lst_id = " + lst.id;
        driverD = new MySQLQuery(qDocsDrivers).setParam(1, lst.driverId).getRecords(ep);
        if (lst.auxDriverId != null) {
            auxDriverD = new MySQLQuery(qDocsDrivers).setParam(1, lst.auxDriverId).getRecords(ep);
        }
        driver = new Employee().select(lst.driverId, ep);
        if (lst.auxDriverId != null) {
            auxDriver = new Employee().select(lst.auxDriverId, ep);
        }
        ver = new MtoChkVersion().select(lst.versionId, ep);
        type = new MtoChkType().select(ver.typeId, ep);
        grps = MtoChkGrp.getGrpsSubQ(lst.versionId, lst.vhId, ep);
        cols = new MtoChkCol[grps.length][];
        rows = new MtoChkRow[grps.length][];

        for (int i = 0; i < grps.length; i++) {
            cols[i] = MtoChkCol.getColsByGrp(new MySQLQuery(MtoChkCol.getQueryColByGrp(grps[i].id, ep)).getRecords(ep));
            rows[i] = MtoChkRow.getRowsByGrp(new MySQLQuery(MtoChkRow.getQueryRowByGrp(grps[i].id, ep)).getRecords(ep));
        }
        if (type.elements) {
            vhElement = MtoChkElement.getAllData(ep, lst.id);
        }
        if (lst.contractorId != null) {
            contractor = new MtoContractor().select(lst.contractorId, ep);
        }
        titleStyle = new PDFCellStyle();
        titleStyle.setAppearance(true, PDFCellStyle.DEFAULT_PADDING, PDFCellStyle.GRAY_BACKGROUND, PDFCellStyle.GRAY_BORDER);
        titleStyle.sethAlignment(PDFCellStyle.ALIGN_LEFT);
        titleStyle.setFontInfo(true, PDFCellStyle.BLACK, PDFCellStyle.DEFAULT_FONT_SIZE);

        notesStyle = new PDFCellStyle();
        notesStyle.setAppearance(true, PDFCellStyle.DEFAULT_PADDING, PDFCellStyle.GRAY_BORDER, PDFCellStyle.GRAY_BORDER);
        notesStyle.sethAlignment(PDFCellStyle.ALIGN_LEFT);
        notesStyle.setFontInfo(true, PDFCellStyle.BLACK, PDFCellStyle.DEFAULT_FONT_SIZE);

        cellStyle = new PDFCellStyle();
        cellStyle.setAppearance(true, PDFCellStyle.DEFAULT_PADDING, PDFCellStyle.WHITE, PDFCellStyle.GRAY_BORDER);
        cellStyle.sethAlignment(PDFCellStyle.ALIGN_LEFT);
        PDFCellStyle boldStyle = cellStyle.copy();
        boldStyle.setBold(true);
        ans = MtoChkVal.getAnswers(rows, lst.id, ep);

        return generateReport();
    }

    private void beginDocument() throws Exception {
        document = new Document(new Rectangle(8.5f * 70f, 11f * 72f), 25f, 25f, 10f, 20f);
        fin = File.createTempFile("rpt_chklst", ".pdf");
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fin));
        event = new HeaderFooter();
        writer.setBoxSize("art", new Rectangle(36, 54, 559, 788));
        writer.setPageEvent(event);
        document.open();
        ct = new ColumnText(writer.getDirectContent());

        ct.setAlignment(Element.ALIGN_JUSTIFIED);
        ct.setExtraParagraphSpace(6);
        ct.setLeading(0, 1.2f);
        ct.setFollowingIndent(27);
    }

    private File endDocument() throws Exception {
        document.close();
        return fin;
    }

    public File generateReport() throws Exception {
        try {
            beginDocument();
            PDFCellStyle font = new PDFCellStyle();
            font.setBold(true);
            font.setFontSize(PDFCellStyle.DEFAULT_FONT_SIZE + 4);

            Paragraph p = font.getParagraph(type.name.toUpperCase(), PDFCellStyle.ALIGN_CENTER);
            document.add(p);
            document.add(cellStyle.getParagraph(new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(lst.dt), PDFCellStyle.ALIGN_CENTER));
            float tblH = getTableDriversData();
            if (type.elements) {
                PdfPTable tab = new PdfPTable(3);
                tab.setWidthPercentage(100);
                tab.addCell(titleStyle.getCell("Elementos de dotación", 3, 1, PDFCellStyle.ALIGN_CENTER));
                tab.addCell(titleStyle.getCell("Descripción"));
                tab.addCell(titleStyle.getCell("Si"));
                tab.addCell(titleStyle.getCell("No"));
                for (MtoChkElement rowElement : vhElement) {
                    tab.addCell(cellStyle.getCell(rowElement.name.toUpperCase()));
                    if (!rowElement.needReview) {
                        tab.addCell(rowElement.checked ? getOptOn(this) : getOptOff(this));
                        tab.addCell(!rowElement.checked ? getOptOn(this) : getOptOff(this));
                    } else {
                        tab.addCell(cellStyle.getCell(rowElement.revDate != null ? dateFormat.format(rowElement.revDate) : "", 2, 1, PDFCellStyle.ALIGN_CENTER));
                    }
                }
                tab.setWidths(new float[]{30, 2, 2});
                ct.addElement(tab);
            }
            for (int i = 0; i < grps.length; i++) {
                MtoChkGrp grp = grps[i];
                MtoChkCol[] gCols = cols[i];
                MtoChkRow[] gRows = rows[i];
                PdfPTable tab = new PdfPTable(1 + gCols.length);
                tab.setHeaderRows(2);
                float[] widths = new float[1 + gCols.length];
                tab.setWidthPercentage(100);
                tab.addCell(titleStyle.getCell(grp.name, 1 + gCols.length, 1, PDFCellStyle.ALIGN_CENTER));

                if ((grp.notes != null) && !grp.notes.isEmpty()) {
                    tab.addCell(notesStyle.getCell(grp.notes, 1 + gCols.length, 1, PDFCellStyle.ALIGN_LEFT));
                }
                tab.addCell(titleStyle.getCell("Descripción"));
                widths[0] = 15;
                for (int j = 0; j < gCols.length; j++) {
                    MtoChkCol gCol = gCols[j];
                    tab.addCell(titleStyle.getCell(gCol.shortName, PDFCellStyle.ALIGN_CENTER));
                    widths[j + 1] = 1;
                }
                tab.setWidths(widths);
                for (int j = 0; j < gRows.length; j++) {
                    MtoChkRow gRow = gRows[j];
                    MtoChkVal val = ans[i][j];
                    if (gRow.type.equals("nor")) {
                        tab.addCell(cellStyle.getCell(gRow.name.toUpperCase()));
                        for (MtoChkCol gCol : gCols) {
                            tab.addCell((val != null && val.colId != null) && val.colId.equals(gCol.id) ? getOptOn(this) : getOptOff(this));
                        }
                    } else if (gRow.type.equals("tit")) {
                        tab.addCell(cellStyle.getCell(gRow.name.toUpperCase(), gCols.length + 1, 1, PDFCellStyle.ALIGN_CENTER));
                    } else if (gRow.type.equals("num") || gRow.type.equals("txt")) {
                        tab.addCell(cellStyle.getCell(gRow.name.toUpperCase()));
                        tab.addCell(cellStyle.getCell((val != null && val.val != null ? val.val : ""), gCols.length, 1));
                    }
                }
                ct.addElement(tab);
            }
            float[][] pPageCols = new float[2][4];//ES PARA LA PRIMERA HOJA QUE YA TIENE TABLAS
            pPageCols[0][0] = 25;//x ini
            pPageCols[0][1] = 105;//y ini
            pPageCols[0][2] = 296;//x fin
            pPageCols[0][3] = document.getPageSize().getHeight() - (tblH);//y fin

            pPageCols[1][0] = 299;
            pPageCols[1][1] = 105;
            pPageCols[1][2] = 570;
            pPageCols[1][3] = document.getPageSize().getHeight() - (tblH);

            int column = 0;
            boolean primari = true;
            int status = ColumnText.START_COLUMN;

            while (ColumnText.hasMoreText(status)) {
                if (primari) {
                    ct.setSimpleColumn(pPageCols[column][0], pPageCols[column][1], pPageCols[column][2], pPageCols[column][3]);
                    primari = false;
                } else {
                    ct.setSimpleColumn(pPageCols[column][0], pPageCols[column][1], pPageCols[column][2], pPageCols[column][3]);
                }
                status = ct.go();
                column++;
            }
            ct.addElement(getTableObs());
            ct.go();

            if (type.showSign) {
                Integer bfIdDriv = new MySQLQuery("SELECT id FROM bfile WHERE owner_id = " + lst.id + " AND owner_type = " + MtoChkLst.MTO_SIGN_DRIVER + " ORDER BY updated DESC LIMIT 1").getAsInteger(ep);
                Integer bfIdResp = new MySQLQuery("SELECT id FROM bfile WHERE owner_id = " + lst.creatorId + " AND owner_type = " + MtoChkLst.MTO_SIGN_FORMATS + " ORDER BY updated DESC LIMIT 1").getAsInteger(ep);

                signDriv = (bfIdDriv != null ? setSignatures(bfIdDriv) : null);
                signResp = (bfIdResp != null ? setSignatures(bfIdResp) : null);

                ct.setSimpleColumn(570, 25, 25, 110);

                PdfPTable tblSignature = new PdfPTable(2);
                tblSignature.setWidthPercentage(100);
                tblSignature.setSpacingBefore(10);

                PDFCellStyle styleSignature = cellStyle.copy();
                styleSignature.setFontSize(6);
                styleSignature.setvAlignment(PDFCellStyle.ALIGN_BOTTOM);
                styleSignature.sethAlignment(PDFCellStyle.ALIGN_CENTER);
                styleSignature.setBold(true);
                styleSignature.setBorder(false);

                PdfPCell cellDriv = styleSignature.getCell("");
                if (signDriv != null) {
                    signDriv.setAlignment(PDFCellStyle.ALIGN_CENTER);
                    signDriv.scaleAbsolute(140, 60);
                    signDriv.setBorderColorBottom(Color.darkGray);
                    signDriv.setBorderWidthBottom(3);
                    cellDriv.addElement(signDriv);
                } else {
                    cellDriv = styleSignature.getCell(" ");
                }

                PdfPCell cellResp = styleSignature.getCell("");
                if (signResp != null) {
                    signResp.setAlignment(PDFCellStyle.ALIGN_CENTER);
                    signResp.scaleAbsolute(140, 60);
                    signResp.setBorderColorBottom(Color.darkGray);
                    signResp.setBorderWidthBottom(2);
                    cellResp.addElement(signResp);
                } else {
                    cellResp = styleSignature.getCell(" ");
                }

                tblSignature.addCell(cellDriv);
                tblSignature.addCell(cellResp);

                tblSignature.addCell(styleSignature.getCell("Conductor Entrega/Recibe el Vehículo"));
                tblSignature.addCell(styleSignature.getCell(creator != null ? creator.firstName + " " + creator.lastName : " "));

                tblSignature.addCell(styleSignature.getCell(" "));
                tblSignature.addCell(styleSignature.getCell("Elaborado Por"));
                ct.addElement(tblSignature);
            }
            ct.go();
            return endDocument();
        } catch (Exception ex) {
            Logger.getLogger(PrintFormatGral.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    class HeaderFooter extends PdfPageEventHelper {

        PdfTemplate total;

        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            try {
                PdfContentByte cb = writer.getDirectContent();

                //Encabezado
                cb.beginText();
                String text = "Página " + writer.getPageNumber() + " de ";
                cb.setFontAndSize(PDFFontsHelper.getRegular(), 9);
                float y = document.getPageSize().getHeight() - 17;
                cb.moveText(15, y);
                cb.showText(text);
                cb.endText();
                cb.addTemplate(total, 15 + PDFFontsHelper.getRegular().getWidthPoint(text, 9), y);

                if (type != null && type.sgcCode != null) {
                    //Footer
                    cb.beginText();
                    String text2 = "Código SGC: " + type.sgcCode;
                    cb.setFontAndSize(PDFFontsHelper.getRegular(), 7);
                    float len = PDFFontsHelper.getRegular().getWidthPoint(text2, 7);
                    float y2 = document.getPageSize().getHeight() - (document.getPageSize().getHeight() - 20);
                    float x2 = document.getPageSize().getWidth() - 20 - len;
                    cb.showTextAligned(Element.ALIGN_RIGHT, text2, x2 + PDFFontsHelper.getRegular().getWidthPoint(text2, 7), y2, 0f);
                    cb.endText();
                }
            } catch (Exception ex) {
                Logger.getLogger(PrintFormatInvPas.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public void onOpenDocument(PdfWriter writer, Document dcmnt) {
            total = writer.getDirectContent().createTemplate(100, 100);
            total.setBoundingBox(new Rectangle(-20, -20, 100, 100));
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
                Logger.getLogger(PrintFormatInvPas.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    protected static PdfPCell getOptOn(Object caller) throws Exception {
        return getImgCell("chks", caller);
    }

    protected static PdfPCell getOptOff(Object caller) throws Exception {
        return getImgCell("blank", caller);
    }

    private static PdfPCell getImgCell(String img, Object caller) throws Exception {
        Image optImg = Image.getInstance(caller.getClass().getResource("/forms/maintenance/panels/checklist/" + img + ".wmf"));
        optImg.scaleAbsolute(6, 6);
        PdfPCell c = new PdfPCell(optImg);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setBorderColor(Color.LIGHT_GRAY);
        c.setPadding(3);
        return c;
    }

    private float getTableDriversData() throws Exception {
        PdfPTable titTable = new PdfPTable(2);
        titTable.setSpacingBefore(15);
        titTable.setWidthPercentage(100);
        PDFCellStyle styleT = titleStyle.copy();
        styleT.sethAlignment(PDFCellStyle.ALIGN_CENTER);
        titTable.addCell(styleT.getCell("DATOS GENERALES", 2, 1));
        PDFCellStyle cStyle = cellStyle.copy();
        PdfPCell c1 = cStyle.getCell("");
        PdfPTable tbl1 = new PdfPTable(4);
        tbl1.setWidthPercentage(100);
        tbl1.addCell(styleT.getCell("Propietario Y Conductores", 4, 1));
        tbl1.addCell(cStyle.getCell("Propietario: ", 1, 1));
        tbl1.addCell(cStyle.getCell("" + (contractor != null ? contractor.firstName + " " + contractor.lastName + (contractor.document != null ? " - " + contractor.document : "") : ""), 3, 1));
        tbl1.addCell(cStyle.getCell("Conductor 1: ", 1, 1));
        tbl1.addCell(cStyle.getCell(driver.firstName + " " + driver.lastName, 3, 1));
        for (Object[] driverD1 : driverD) {
            tbl1.addCell(cStyle.getCell(driverD1[0] + ": ", 1, 1));
            tbl1.addCell(cStyle.getCell(driverD1[1] != null ? dateFormat.format(driverD1[1]) + "" : "", 1, 1));
        }
        if (driverD.length % 2 != 0) {
            tbl1.addCell(cStyle.getCell(" ", 2, 1));
        }
        tbl1.addCell(cStyle.getCell("Conductor 2: ", 1, 1));
        tbl1.addCell(cStyle.getCell((auxDriver != null ? (auxDriver.firstName + " " + auxDriver.lastName) : ""), 3, 1));
        if (lst.auxDriverId != null) {
            for (Object[] auxDriverD1 : auxDriverD) {
                tbl1.addCell(cStyle.getCell(auxDriverD1[0] + ": ", 1, 1));
                tbl1.addCell(cStyle.getCell(auxDriverD1[1] != null ? dateFormat.format(auxDriverD1[1]) + "" : "", 1, 1));
            }
            if (auxDriverD.length % 2 != 0) {
                tbl1.addCell(cStyle.getCell(" ", 2, 1));
            }
        }

        c1.addElement(tbl1);
        c1.setHorizontalAlignment(Element.ALIGN_CENTER);
        c1.setVerticalAlignment(Element.ALIGN_TOP);
        titTable.addCell(c1);

        PdfPCell c2 = cStyle.getCell("");
        PdfPTable tbl2 = new PdfPTable(4);
        tbl2.setWidthPercentage(100);
        tbl2.addCell(styleT.getCell("Vehículo", 4, 1));
        tbl2.addCell(cStyle.getCell("No Interno: ", 1, 1));
        tbl2.addCell(cStyle.getCell(vehicleD[0][0].toString(), 1, 1));
        tbl2.addCell(cStyle.getCell("Clase: ", 1, 1));
        tbl2.addCell(cStyle.getCell(vehicleD[0][1].toString(), 1, 1));
        tbl2.addCell(cStyle.getCell("Tipo: ", 1, 1));
        tbl2.addCell(cStyle.getCell(vehicleD[0][2].toString(), 1, 1));
        tbl2.addCell(cStyle.getCell("Placa: ", 1, 1));
        tbl2.addCell(cStyle.getCell(vehicleD[0][3].toString(), 1, 1));
        tbl2.addCell(cStyle.getCell("Empresa: ", 1, 1));
        tbl2.addCell(cStyle.getCell(vehicleD[0][4].toString(), 1, 1));
        tbl2.addCell(cStyle.getCell("Kilometraje", 1, 1));
        tbl2.addCell(cStyle.getCell("" + (lst.mileage != null ? lst.mileage : ""), 1, 1));

        if (docsVhD != null && docsVhD.length > 0) {
            tbl2.addCell(styleT.getCell("Documentos", 4, 1));
        }
        for (Object[] docsVhD1 : docsVhD) {
            tbl2.addCell(cStyle.getCell(docsVhD1[0].toString(), 2, 1));
            tbl2.addCell(cStyle.getCell(docsVhD1[1] != null ? dateFormat.format(docsVhD1[1]) : "", 2, 1));
        }
        c2.addElement(tbl2);
        c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        c2.setVerticalAlignment(Element.ALIGN_TOP);
        titTable.addCell(c2);
        document.add(titTable);
        return titTable.getTotalHeight() + 65;
    }

    private PdfPTable getTableObs() throws Exception {

        float[] widths = new float[5];
        widths[0] = 10;
        widths[1] = 10;
        widths[2] = 10;
        widths[3] = 10;
        widths[4] = 10;
        PdfPTable tabHeader = new PdfPTable(5);
        tabHeader.setWidthPercentage(100);
        tabHeader.addCell(titleStyle.getCell("OBSERVACIONES", 5, 1, Element.ALIGN_CENTER));
        tabHeader.addCell(titleStyle.getCell("GRUPO"));
        tabHeader.addCell(titleStyle.getCell("ELEMENTO"));
        tabHeader.addCell(titleStyle.getCell("CORRECTIVO"));
        tabHeader.addCell(titleStyle.getCell("FECHA"));
        tabHeader.addCell(titleStyle.getCell("TALLER"));
        tabHeader.setWidths(widths);
        tabHeader.setSpacingBefore(10);
        for (int i = 0; i < grps.length; i++) {
            MtoChkGrp grp = grps[i];
            MtoChkRow[] gRows = rows[i];
            for (int j = 0; j < gRows.length; j++) {
                MtoChkRow gRow = gRows[j];
                MtoChkVal val = ans[i][j];
                if (val != null) {
                    if (val.workOrderId != null) {
                        Object[][] corrData = new MySQLQuery("SELECT "
                                + "wo.description, wo.`begin`, pro.`name` "
                                + "FROM work_order AS wo "
                                + "INNER JOIN prov_provider AS pro ON pro.id = wo.provider_id "
                                + "WHERE wo.id = " + val.workOrderId).getRecords(ep);
                        if (corrData.length > 0) {
                            Object[] corrRow = corrData[0];
                            tabHeader.addCell(titleStyle.getCell(grp.name));
                            tabHeader.addCell(titleStyle.getCell(gRow.name));
                            tabHeader.addCell(cellStyle.getCell(corrRow[0] != null ? corrRow[0].toString() : ""));
                            tabHeader.addCell(cellStyle.getCell(corrRow[1] != null ? dateFormat.format(corrRow[1]) : ""));
                            tabHeader.addCell(cellStyle.getCell(corrRow[2] != null ? corrRow[2].toString() : ""));
                        }
                    } else if (val.corrDate != null) {
                        tabHeader.addCell(titleStyle.getCell(grp.name));
                        tabHeader.addCell(titleStyle.getCell(gRow.name));
                        tabHeader.addCell(cellStyle.getCell(val.correction != null ? val.correction : ""));
                        tabHeader.addCell(cellStyle.getCell(val.corrDate != null ? dateFormat.format(val.corrDate) : ""));
                        tabHeader.addCell(cellStyle.getCell(val.corrProv != null ? val.corrProv : ""));
                    }
                }
            }
        }
        if (type.elements) {
            for (MtoChkElement rowElements : vhElement) {
                if (rowElements.workOrderId != null) {
                    Object[][] corrData = new MySQLQuery("SELECT "
                            + "wo.description, wo.`begin`, pro.`name` "
                            + "FROM work_order AS wo "
                            + "INNER JOIN prov_provider AS pro ON pro.id = wo.provider_id "
                            + "WHERE wo.id = " + rowElements.workOrderId).getRecords(ep);
                    if (corrData.length > 0) {
                        Object[] corrRow = corrData[0];
                        tabHeader.addCell(titleStyle.getCell("Elementos de Dotación"));
                        tabHeader.addCell(titleStyle.getCell(rowElements.name));
                        tabHeader.addCell(cellStyle.getCell(corrRow[0] != null ? corrRow[0].toString() : ""));
                        tabHeader.addCell(cellStyle.getCell(corrRow[1] != null ? dateFormat.format(corrRow[1]) : ""));
                        tabHeader.addCell(cellStyle.getCell(corrRow[2] != null ? corrRow[2].toString() : ""));
                    }
                } else if (rowElements.correction != null) {
                    tabHeader.addCell(titleStyle.getCell("Elementos de Dotación"));
                    tabHeader.addCell(titleStyle.getCell(rowElements.name));
                    tabHeader.addCell(cellStyle.getCell(rowElements.correction != null ? rowElements.correction : ""));
                    tabHeader.addCell(cellStyle.getCell(rowElements.corrDate != null ? dateFormat.format(rowElements.corrDate) : ""));
                    tabHeader.addCell(cellStyle.getCell(rowElements.corrProv));
                }
            }
        }
        tabHeader.addCell(titleStyle.getCell("Notas", 1, 1, Element.ALIGN_CENTER));
        tabHeader.addCell(cellStyle.getCell(lst.notes != null ? lst.notes : "", 5, 1));
        return tabHeader;
    }

    private Image setSignatures(Integer bfileId) throws Exception {
        InputStream is = MtoChkLst.readFileDirect(bfileId, ep);
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
