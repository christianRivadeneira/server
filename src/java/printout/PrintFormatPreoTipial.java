package printout;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
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
import java.nio.file.Files;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Employee;
import model.MtoContractor;
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
import web.enterpriseLogo;

public class PrintFormatPreoTipial extends PrintFormatsGenerator {

    private ColumnText ct = null;
    private HeaderFooter event;
    private File fin;

    private PDFCellStyle titleSt;
    private PDFCellStyle cellSt;
    private PDFCellStyle notesSt;
    private PDFCellStyle whiteBorderSt;

    private MtoChkLst lst;
    private MtoChkVersion ver;
    private MtoChkType type;
    private MtoChkGrp[] grps;
    private MtoChkCol[][] cols;
    private MtoChkRow[][] rows;
    private Employee driver;

    private Object[][] auxDriverD;
    private Employee auxDriver;
    private MtoContractor contractor;

    private Object[][] vehicleD;
    private Object[][] driverD;
    private MtoChkVal[][] ans;

    private Image signDriv;
    private Image signResp;

    @Override
    public File initFormat(Connection ep, Integer registId) throws Exception {
        this.ep = ep;
        lst = new MtoChkLst().select(registId, ep);
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

        if (lst.contractorId != null) {
            contractor = new MtoContractor().select(lst.contractorId, ep);
        }
        titleSt = new PDFCellStyle();
        titleSt.setAppearance(true, PDFCellStyle.DEFAULT_PADDING, PDFCellStyle.GRAY_BACKGROUND, PDFCellStyle.GRAY_BORDER);
        titleSt.sethAlignment(PDFCellStyle.ALIGN_LEFT);
        titleSt.setFontInfo(true, PDFCellStyle.BLACK, 5f);

        notesSt = new PDFCellStyle();
        notesSt.setAppearance(true, PDFCellStyle.DEFAULT_PADDING, PDFCellStyle.GRAY_BORDER, PDFCellStyle.GRAY_BORDER);
        notesSt.sethAlignment(PDFCellStyle.ALIGN_LEFT);
        notesSt.setFontInfo(true, PDFCellStyle.BLACK, 5f);

        cellSt = new PDFCellStyle();
        cellSt.setAppearance(true, PDFCellStyle.DEFAULT_PADDING, PDFCellStyle.WHITE, PDFCellStyle.GRAY_BORDER);
        cellSt.sethAlignment(PDFCellStyle.ALIGN_LEFT);
        cellSt.setFontInfo(true, PDFCellStyle.BLACK, 5f);
        cellSt.setBold(false);

        whiteBorderSt = cellSt.copy();
        whiteBorderSt.setBorderColor(Color.WHITE);

        ans = MtoChkVal.getAnswers(rows, lst.id, ep);
        return generateReport();
    }

    private void beginDocument() throws Exception {
        document = new Document(new Rectangle(8.5f * 70f, 11f * 72f), 25f, 25f, 20f, 5f);
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

            PdfPTable tabHer = getHeader();
            tabHer.setWidthPercentage(100);
            document.add(tabHer);

            float tblH = getTableDriversData();
            for (int i = 0; i < grps.length; i++) {
                MtoChkGrp grp = grps[i];
                MtoChkCol[] gCols = cols[i];
                MtoChkRow[] gRows = rows[i];
                PdfPTable tab = new PdfPTable(1 + gCols.length);
                tab.setHeaderRows(2);
                float[] widths = new float[1 + gCols.length];
                tab.setWidthPercentage(100);
                tab.addCell(titleSt.getCell(grp.name, 1 + gCols.length, 1, PDFCellStyle.ALIGN_CENTER));

                if (grp.notes != null && !grp.notes.isEmpty()) {
                    tab.addCell(notesSt.getCell(grp.notes, 1 + gCols.length, 1, PDFCellStyle.ALIGN_LEFT));
                }
                tab.addCell(titleSt.getCell("Descripción"));
                widths[0] = 15;
                for (int j = 0; j < gCols.length; j++) {
                    MtoChkCol gCol = gCols[j];
                    tab.addCell(titleSt.getCell(gCol.shortName, PDFCellStyle.ALIGN_CENTER));
                    widths[j + 1] = 1;
                }
                tab.setWidths(widths);
                for (int j = 0; j < gRows.length; j++) {
                    MtoChkRow gRow = gRows[j];
                    MtoChkVal val = ans[i][j];
                    if (gRow.type.equals("nor")) {
                        tab.addCell(cellSt.getCell(gRow.name.toUpperCase()));
                        for (MtoChkCol gCol : gCols) {
                            tab.addCell((val != null && val.colId != null) && val.colId.equals(gCol.id) ? getOptOn(this) : getOptOff(this));
                        }
                    } else if (gRow.type.equals("tit")) {
                        tab.addCell(cellSt.getCell(gRow.name.toUpperCase(), gCols.length + 1, 1, PDFCellStyle.ALIGN_CENTER));
                    } else if (gRow.type.equals("num") || gRow.type.equals("txt")) {
                        tab.addCell(cellSt.getCell(gRow.name.toUpperCase()));
                        tab.addCell(cellSt.getCell((val != null && val.val != null ? val.val : ""), gCols.length, 1));
                    }
                }
                ct.addElement(tab);
            }
            float[][] pPageCols = new float[3][4];//ES PARA LA PRIMERA HOJA QUE YA TIENE TABLAS
            pPageCols[0][0] = 25;
            pPageCols[0][1] = 200;
            pPageCols[0][2] = 206;
            pPageCols[0][3] = document.getPageSize().getHeight() - (tblH);

            pPageCols[1][0] = 207;
            pPageCols[1][1] = 200;
            pPageCols[1][2] = 387;
            pPageCols[1][3] = document.getPageSize().getHeight() - (tblH);

            pPageCols[2][0] = 388;
            pPageCols[2][1] = 200;
            pPageCols[2][2] = 570;
            pPageCols[2][3] = document.getPageSize().getHeight() - (tblH);

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
            ct.go();

            Integer bfIdDriv = new MySQLQuery("SELECT id FROM bfile WHERE owner_id = " + lst.id + " AND owner_type = " + MtoChkLst.MTO_SIGN_DRIVER + " ORDER BY updated DESC LIMIT 1").getAsInteger(ep);
            Integer bfIdResp = new MySQLQuery("SELECT id FROM bfile WHERE owner_id = " + lst.creatorId + " AND owner_type = " + MtoChkLst.MTO_SIGN_FORMATS + " ORDER BY updated DESC LIMIT 1").getAsInteger(ep);

            signDriv = (bfIdDriv != null ? setSignatures(bfIdDriv) : null);
            signResp = (bfIdResp != null ? setSignatures(bfIdResp) : null);
            ct.setSimpleColumn(570, 25, 25, 195);

            PdfPTable tblSignature = new PdfPTable(2);
            tblSignature.setWidthPercentage(100);
            PDFCellStyle styleSignature = cellSt.copy();
            styleSignature.setFontSize(6);
            styleSignature.setvAlignment(PDFCellStyle.ALIGN_BOTTOM);
            styleSignature.sethAlignment(PDFCellStyle.ALIGN_CENTER);
            styleSignature.setBold(true);
            styleSignature.setBorder(false);

            PdfPCell cellDriv = styleSignature.getCell("");
            if (signDriv != null) {
                signDriv.setAlignment(PDFCellStyle.ALIGN_CENTER);
                signDriv.scaleAbsolute(50, 50);
                signDriv.setBorderColorBottom(Color.darkGray);
                signDriv.setBorderWidthBottom(3);
                cellDriv.addElement(signDriv);
            } else {
                cellDriv = styleSignature.getCell(" ");
            }

            PdfPCell cellResp = styleSignature.getCell("");
            if (signResp != null) {
                signResp.setAlignment(PDFCellStyle.ALIGN_CENTER);
                signResp.scaleAbsolute(50, 50);
                signResp.setBorderColorBottom(Color.darkGray);
                signResp.setBorderWidthBottom(3);
                cellResp.addElement(signResp);
            } else {
                cellResp = styleSignature.getCell(" ");
            }
            tblSignature.setSpacingBefore((signResp != null || signDriv != null) ? 5 : 35);
            tblSignature.addCell(cellDriv);
            tblSignature.addCell(cellResp);
            tblSignature.addCell(styleSignature.getCell("NOMBRE Y FIRMA DEL CONDUCTOR"));
            tblSignature.addCell(styleSignature.getCell("FIRMA DE QUIEN DESARROLLA LA INSPECCION"));

            ct.addElement(getTableNotes());
            if (type.showSign) {
                ct.addElement(tblSignature);
            }

            PdfPTable tblLine = new PdfPTable(1);
            tblLine.setSpacingBefore(5);
            tblLine.setWidthPercentage(100);

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 214; i++) {
                sb.append("_");
            }
            tblLine.addCell(whiteBorderSt.getCell(sb.toString(), 1, 1));
            ct.addElement(tblLine);
            ct.addElement(getFooter());
            ct.go();
            return endDocument();
        } catch (Exception ex) {
            Logger.getLogger(PrintFormatPreoTipial.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
    }

    private PdfPCell getLogo(PDFCellStyle style) {
        PdfPCell imgCell;
        try {
            Image img = Image.getInstance(Image.getInstance(Files.readAllBytes(enterpriseLogo.getEnterpriseLogo("5", ep).toPath())));
            img.setAlignment(Element.ALIGN_CENTER);
            img.scaleToFit(60, 55);
            imgCell = new PdfPCell(img);
            imgCell.setRowspan(1);
        } catch (Exception ex) {
            imgCell = new PdfPCell();
        }
        imgCell.setPadding(5);
        imgCell.setBorderColor(style.getBorderColor());
        imgCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        return imgCell;
    }

    class HeaderFooter extends PdfPageEventHelper {

        PdfTemplate total;

        @Override
        public void onStartPage(PdfWriter writer, Document document) {
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
                Logger.getLogger(PrintFormatPreoTipial.class.getName()).log(Level.SEVERE, null, ex);
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
        optImg.scaleAbsolute(3, 3);
        PdfPCell c = new PdfPCell(optImg);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setBorderColor(Color.LIGHT_GRAY);
        c.setPadding(3);
        return c;
    }

    private float getTableDriversData() throws Exception {
        PdfPTable titTable = new PdfPTable(2);
        titTable.setWidthPercentage(100);
        PDFCellStyle styleT = titleSt.copy();
        styleT.sethAlignment(PDFCellStyle.ALIGN_CENTER);
        titTable.addCell(styleT.getCell("DATOS GENERALES", 2, 1));
        PDFCellStyle cStyle = cellSt.copy();
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
        tbl2.addCell(cStyle.getCell((vehicleD[0][0] != null ? vehicleD[0][0].toString() : ""), 1, 1));
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

        c2.addElement(tbl2);
        c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        c2.setVerticalAlignment(Element.ALIGN_TOP);
        titTable.addCell(c2);
        document.add(titTable);
        return titTable.getTotalHeight() + 65;
    }

    private PdfPTable getTableNotes() throws Exception {
        PdfPTable tabHeader = new PdfPTable(5);
        tabHeader.setWidthPercentage(100);
        tabHeader.addCell(titleSt.getCell("Notas", 1, 1, Element.ALIGN_CENTER));
        tabHeader.addCell(cellSt.getCell(lst.notes != null ? lst.notes : "", 5, 1));
        tabHeader.addCell(cellSt.getCell("APROBACIÓN", 1, 1));
        tabHeader.addCell(getTableResp());
        tabHeader.addCell(cellSt.getCell("", 4, 1));

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

    private PdfPTable getHeader() {
        try {
            PDFCellStyle headerSt = cellSt.copy();
            PdfPTable header = new PdfPTable(3);
            header.addCell(getLogo(headerSt));
            headerSt.sethAlignment(PDFCellStyle.ALIGN_CENTER);
            header.addCell(headerSt.getCell(type.name.toUpperCase(), 1, 1));
            header.addCell(headerSt.getCell("Código: " + (type.sgcCode != null ? type.sgcCode : ""), 1, 1));
            header.addCell(headerSt.getCell("Sistema de Gestión Integral", 1, 1));
            header.addCell(getTableNo(whiteBorderSt));
            header.addCell(headerSt.getCell("Vigencia: " + dateFormat.format(ver.since), 1, 1));
            return header;
        } catch (Exception e) {
            Logger.getLogger(PrintFormatPreoTipial.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    private PdfPTable getFooter() {
        try {
            PdfPTable footer = new PdfPTable(6);

            PDFCellStyle footerSt = cellSt.copy();
            footer.setSpacingBefore(5);
            footer.setWidthPercentage(100);
            footer.addCell(getLogo(footerSt));

            footerSt.sethAlignment(PDFCellStyle.ALIGN_CENTER);
            footer.addCell(footerSt.getCell(type.name.toUpperCase(), 4, 1));
            footer.addCell(getTableNo(whiteBorderSt));
            footerSt.sethAlignment(PDFCellStyle.ALIGN_LEFT);

            footer.addCell(footerSt.getCell("C. Placa: " + vehicleD[0][3].toString(), 1, 1));
            footer.addCell(footerSt.getCell("D. Interno: " + vehicleD[0][0].toString(), 1, 1));
            footer.addCell(footerSt.getCell("E. Fecha", 1, 1));
            footer.addCell(footerSt.getCell(dateFormat.format(lst.dt), 1, 1));
            footer.addCell(footerSt.getCell("F. Ciudad: ", 2, 1));

            footer.addCell(footerSt.getCell("", 2, 1));//Firma A
            footer.addCell(footerSt.getCell("", 2, 1));//Firma B
            footer.addCell(footerSt.getCell("APROBACIÓN", 1, 1));
            footer.addCell(getTableResp());
            footer.addCell(footerSt.getCell("23. FIRMA DE QUIEN DESARROLLA LA INSPECCION", 2, 1));
            footer.addCell(footerSt.getCell("24. NOMBRE Y FIRMA DEL CONDUCTOR", 2, 1));
            footer.addCell(footerSt.getCell("DESPRENDIBLE PARA CONDUCTOR", 2, 1));

            return footer;
        } catch (Exception e) {
            Logger.getLogger(PrintFormatPreoTipial.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    private PdfPTable getTableNo(PDFCellStyle style) throws Exception {
        PdfPTable tblNo = new PdfPTable(6);
        tblNo.addCell(style.getCell("", 1, 1));
        tblNo.addCell(style.getCell("NO.", 1, 1));
        tblNo.addCell(style.getCell("", 4, 1));
        return tblNo;
    }

    private PdfPTable getTableResp() throws Exception {
        PdfPTable tblResp = new PdfPTable(4);
        tblResp.addCell(cellSt.getCell("SI", 1, 1));
        tblResp.addCell(titleSt.getCell("", 1, 1));
        tblResp.addCell(cellSt.getCell("No", 1, 1));
        tblResp.addCell(titleSt.getCell("", 1, 1));
        return tblResp;
    }
}
