package printout;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
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

public class PrintInvTvs extends PrintFormatsGenerator {

    private File fin;
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

    private Object[][] vehicleD;
    private Object[][] docsVhD;
    private Object[][] driverD;
    private Object[][] auxDriverD;
    private MtoChkVal[][] ans;
    private ColumnText ct = null;

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
        cellStyle = new PDFCellStyle();
        cellStyle.setAppearance(true, PDFCellStyle.DEFAULT_PADDING, PDFCellStyle.WHITE, PDFCellStyle.GRAY_BORDER);
        cellStyle.sethAlignment(PDFCellStyle.ALIGN_LEFT);
        PDFCellStyle boldStyle = cellStyle.copy();
        boldStyle.setBold(true);
        ans = MtoChkVal.getAnswers(rows, lst.id, ep);

        return generateReport();
    }

    private void beginDocument() throws Exception {
        document = new Document(new Rectangle(8.5f * 70f, 11f * 72f), 25f, 25f, 40f, 20f);
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

            document.add(cellStyle.getParagraph(new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a").format(lst.dt), PDFCellStyle.ALIGN_CENTER));

            PdfPTable header = getTableHeader();
            document.add(header);
            float tblH = getTableDriversData() + header.getTotalHeight();
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
                tab.setWidths(new float[]{30, 5, 5});
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
                tab.addCell(titleStyle.getCell("Descripción"));
                widths[0] = 15;
                for (int j = 0; j < gCols.length; j++) {
                    MtoChkCol gCol = gCols[j];
                    tab.addCell(titleStyle.getCell(gCol.shortName, PDFCellStyle.ALIGN_CENTER));
                    widths[j + 1] = 3;
                }
                tab.setWidths(widths);
                for (int j = 0; j < gRows.length; j++) {
                    MtoChkRow gRow = gRows[j];
                    MtoChkVal val = ans[i][j];
                    switch (gRow.type) {
                        case "nor":
                            tab.addCell(cellStyle.getCell(gRow.name.toUpperCase()));
                            for (MtoChkCol gCol : gCols) {
                                tab.addCell((val != null && val.colId != null) && val.colId.equals(gCol.id) ? getOptOn(this) : getOptOff(this));
                            }
                            break;
                        case "tit":
                            tab.addCell(cellStyle.getCell(gRow.name.toUpperCase(), gCols.length + 1, 1, PDFCellStyle.ALIGN_CENTER));
                            break;
                        case "num":
                        case "txt":
                            tab.addCell(cellStyle.getCell(gRow.name.toUpperCase()));
                            tab.addCell(cellStyle.getCell((val != null && val.val != null ? val.val : ""), gCols.length, 1));
                            break;
                        default:
                            break;
                    }
                }
                ct.addElement(tab);
            }
            float[][] pPageCols = new float[2][4];//ES PARA LA PRIMERA HOJA QUE YA TIENE TABLAS
            pPageCols[0][0] = 25;
            pPageCols[0][1] = 25;
            pPageCols[0][2] = 296;
            pPageCols[0][3] = document.getPageSize().getHeight() - (tblH + 72);

            pPageCols[1][0] = 299;
            pPageCols[1][1] = 25;
            pPageCols[1][2] = 570;
            pPageCols[1][3] = document.getPageSize().getHeight() - (tblH + 72);

            float[][] nPageCols = new float[2][4];//NO CAMBIA SOLO ES PARA HOJAS NUEVAS
            nPageCols[0][0] = 25;
            nPageCols[0][1] = 25;
            nPageCols[0][2] = 296;
            nPageCols[0][3] = 750;

            nPageCols[1][0] = 299;
            nPageCols[1][1] = 25;
            nPageCols[1][2] = 570;
            nPageCols[1][3] = 750;

            int column = 0;
            boolean primari = true;
            int status = ColumnText.START_COLUMN;
            boolean newPage = false;
            while (ColumnText.hasMoreText(status)) {
                if (primari) {
                    ct.setSimpleColumn(pPageCols[column][0], pPageCols[column][1], pPageCols[column][2], pPageCols[column][3]);
                    primari = false;
                } else if (newPage) {
                    ct.setSimpleColumn(nPageCols[column][0], nPageCols[column][1], nPageCols[column][2], nPageCols[column][3]);
                } else {
                    ct.setSimpleColumn(pPageCols[column][0], pPageCols[column][1], pPageCols[column][2], pPageCols[column][3]);
                }
                status = ct.go();
                column++;
                if (column > 1) {
                    column = 0;
                    document.newPage();
                    newPage = true;
                }
            }

            ct.addElement(getTableObs());
            if (column > 1) {
                document.newPage();
                ct.setSimpleColumn(nPageCols[column][0], nPageCols[column][1], nPageCols[column][2], nPageCols[column][3]);
            } else if (newPage) {
                ct.setSimpleColumn(nPageCols[column][0], nPageCols[column][1], document.getPageSize().getWidth() - 25, nPageCols[column][3]);
            } else {
                ct.setSimpleColumn(25, 25, document.getPageSize().getWidth() - 25, ct.getYLine() - 10);
            }

            ct.addElement(getTableNovs());
            ct.addElement(getTblSignatures());
            ct.go();
            return endDocument();
        } catch (Exception ex) {
            Logger.getLogger(PrintInvTvs.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;
        }
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
                Logger.getLogger(PrintInvTvs.class.getName()).log(Level.SEVERE, null, ex);
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
        titTable.addCell(styleT.getCell("DOCUMENTOS Y CARACTERÍSTICAS DEL VEHÍCULO", 2, 1));
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

        tbl2.addCell(styleT.getCell("Documentos", 4, 1));
        for (Object[] docsVhD1 : docsVhD) {
            tbl2.addCell(cStyle.getCell(docsVhD1[0].toString(), 2, 1));
            tbl2.addCell(cStyle.getCell(docsVhD1[1] != null ? dateFormat.format(docsVhD1[1]) : "", 2, 1));
        }
        c2.addElement(tbl2);
        c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        c2.setVerticalAlignment(Element.ALIGN_TOP);
        titTable.addCell(c2);
        document.add(titTable);
        return titTable.getTotalHeight() + 30;
    }

    private PdfPTable getTableHeader() throws Exception {
        PdfPTable tblHeader = new PdfPTable(2);
        tblHeader.setWidthPercentage(100);
        tblHeader.setSpacingBefore(10);
        tblHeader.setWidths(new int[]{10, 70});

        PdfPCell imgCell = cellStyle.getCell("");
        imgCell.setRowspan(2);
        Image img = Image.getInstance(PrintInvTvs.class.getResource("/icons/tvs/tvs_logo.png"));
        img.setAlignment(PDFCellStyle.ALIGN_CENTER);
        img.scalePercent(40);
        imgCell.addElement(img);
        tblHeader.addCell(imgCell);

        PDFCellStyle tx = titleStyle.copy();
        tx.setBackgroundColor(Color.WHITE);
        tx.sethAlignment(PDFCellStyle.ALIGN_CENTER);
        tblHeader.addCell(tx.getCell("TRANSPORTADORA DE VALORES DEL SUR LTDA.\n" + type.name.toUpperCase(), 1, 2));
        tx.sethAlignment(PDFCellStyle.ALIGN_CENTER);
        tblHeader.addCell(tx.getCell(""));
        return tblHeader;

    }

    private PdfPTable getTableNovs() throws Exception {
        PdfPTable tblNovs = new PdfPTable(2);
        tblNovs.setWidthPercentage(100);
        tblNovs.setSpacingBefore(10);

        PDFCellStyle ts = titleStyle.copy();
        ts.sethAlignment(PDFCellStyle.ALIGN_CENTER);
        tblNovs.addCell(ts.getCell("NOVEDADES REGISTRADAS (Señale daños encontrados)", 2, 1));

        PdfPCell imgCell = cellStyle.getCell("");
        Image img = Image.getInstance(PrintInvTvs.class.getResource("/icons/tvs/carro_normal.png"));
        img.setAlignment(PDFCellStyle.ALIGN_CENTER);
        img.scalePercent(20);
        imgCell.addElement(img);
        tblNovs.addCell(imgCell);

        PdfPTable tblRows = new PdfPTable(1);
        tblRows.setWidthPercentage(100);
        tblRows.addCell(ts.getCell("Descripciòn Novedad"));
        tblRows.addCell(cellStyle.getCell(" "));
        for (int i = 0; i < 10; i++) {
            tblRows.addCell(cellStyle.getCell(" "));
        }
        tblNovs.addCell(cellStyle.getCell(tblRows, 0, 0));
        return tblNovs;
    }

    private PdfPTable getTblSignatures() throws Exception {
        PdfPTable tblSign = new PdfPTable(3);
        tblSign.setWidthPercentage(100);
        tblSign.setSpacingBefore(30);

        PDFCellStyle signStyle = cellStyle.copy();
        signStyle.setBorders(false, true, false, false);
        PdfPCell cell = signStyle.getCell("");
        cell.setBorderWidthRight(20);
        cell.setBorderWidthLeft(20);
        cell.setBorderColorLeft(Color.WHITE);
        cell.setBorderColorRight(Color.WHITE);

        tblSign.addCell(cell);
        tblSign.addCell(cell);
        tblSign.addCell(cell);

        PDFCellStyle sStyle = cellStyle.copy();
        sStyle.setBorders(false, false, false, false);
        sStyle.sethAlignment(PDFCellStyle.ALIGN_CENTER);
        sStyle.setvAlignment(PDFCellStyle.ALIGN_TOP);

        tblSign.addCell(sStyle.getCell("Nombre y Firma De Quien Entrega"));
        tblSign.addCell(sStyle.getCell("Nombre y Firma De Quien Recibe"));
        tblSign.addCell(sStyle.getCell("Nombre Jefe Taller y/o Coordinador"));

        return tblSign;
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
}
