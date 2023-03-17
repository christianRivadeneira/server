package api.mto.api;

import api.BaseAPI;
import api.mto.dto.HistoryDTO;
import api.mto.dto.VehicleList;
import api.mto.model.MtoCfg;
import api.mto.model.Vehicle;
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
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import model.maintenance.list.reports.history.HistoryArea;
import model.maintenance.list.reports.history.HistoryListItem;
import model.maintenance.list.reports.history.HistoryReport;
import model.maintenance.mysql.Area;
import printout.PrintCootranarPNC;
import utilities.Dates;
import utilities.MySQLQuery;
import utilities.Reports;
import utilities.mysqlReport.*;
import utilities.pdf.PDFCellStyle;
import utilities.pdf.PDFFontsHelper;
import web.enterpriseLogo;
import web.fileManager;

@Path("/HistoryApi")
public class HistoryApi extends BaseAPI {

    public static final int MTO_VEH_PHOTO = 37;
    private static final SimpleDateFormat SDF = new SimpleDateFormat("dd/MM/yyyy");
    private static final DecimalFormat DF = new DecimalFormat("#.###");

    @POST
    @Path("/PDFOrder")
    public Response PDFOrder(HistoryDTO historyDTO) {
        try (Connection conn = getConnection()) {
            URL resourceImg = this.getClass().getResource("/icons/tipial/camera2.png");
            VehicleList vehicleList = VehicleList.getVehicleListById(historyDTO.vehId, conn);
            Vehicle vh = new Vehicle().select(vehicleList.id, conn);
            HistoryReport data = findHistoryReport(conn, historyDTO.vehId, historyDTO.areaId, historyDTO.subAreaId, historyDTO.rdbFull, historyDTO.begDate, historyDTO.endDate, historyDTO.filterTask);
            Object[][] docsVh = new MySQLQuery("SELECT "
                    + "d.description, "
                    + "dv.fecha "
                    + "FROM document AS d "
                    + "INNER JOIN document_vehicle AS dv ON dv.doc_id = d.id "
                    + "WHERE dv.vehicle_id = " + vehicleList.id + " "
                    + "AND dv.apply = 1 "
                    + "GROUP BY dv.id ").getRecords(conn);

            boolean hasDynamic = new MySQLQuery("SELECT COUNT(*)> 0 FROM mto_hist_field").getAsBoolean(conn);
            Object[][] dataDynamic = new MySQLQuery("SELECT  f.name, v.`data`,  f.`type` FROM vehicle vh LEFT JOIN sys_frm_value v ON v.owner_id = vh.id AND v.field_id IN (SELECT id FROM sys_frm_field f WHERE f.type_id = 1) INNER JOIN sys_frm_field f ON f.id = v.field_id INNER JOIN mto_hist_field hf ON hf.field_id = f.id WHERE vh.id = " + vh.id).getRecords(conn);
            if (hasDynamic) {
                dataFormat(dataDynamic);
            }

            Document document = new Document(new Rectangle(8.5f * 72f, 13f * 72f), 36f, 36f, 36f, 36f);
            File fin = File.createTempFile("hoja de vida", ".pdf");
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fin));
            HeaderFooter event = new HeaderFooter();
            writer.setBoxSize("art", new Rectangle(36, 54, 559, 788));
            writer.setPageEvent(event);
            document.open();
            PDFCellStyle titleStyle = new PDFCellStyle();
            titleStyle.setAppearance(true, PDFCellStyle.DEFAULT_PADDING, PDFCellStyle.GRAY_BACKGROUND, PDFCellStyle.GRAY_BORDER);
            titleStyle.sethAlignment(PDFCellStyle.ALIGN_LEFT);
            titleStyle.setFontInfo(true, PDFCellStyle.BLACK, PDFCellStyle.DEFAULT_FONT_SIZE - 1);
            PDFCellStyle titleStyleCenter = titleStyle.copy();
            titleStyleCenter.sethAlignment(PDFCellStyle.ALIGN_CENTER);
            PDFCellStyle cellStyle = new PDFCellStyle();
            cellStyle.setAppearance(true, PDFCellStyle.DEFAULT_PADDING, PDFCellStyle.WHITE, PDFCellStyle.GRAY_BORDER);
            cellStyle.sethAlignment(PDFCellStyle.ALIGN_LEFT);
            cellStyle.setFontSize(PDFCellStyle.DEFAULT_FONT_SIZE - 1);
            document.add(getTabHeader(conn, vehicleList.id, cellStyle, titleStyle));

            PdfPTable tbl = new PdfPTable(3);
            tbl.setWidths(new float[]{30, 35, 35});
            tbl.setWidthPercentage(100);
            tbl.addCell(getAdjImg(resourceImg, conn, vehicleList.id, cellStyle));
            PdfPCell celInt = cellStyle.getCell("");
            celInt.setHorizontalAlignment(Element.ALIGN_CENTER);
            celInt.setVerticalAlignment(Element.ALIGN_TOP);
            celInt.addElement(getInternalTab(titleStyle, cellStyle, titleStyleCenter, vehicleList, vh));
            tbl.addCell(celInt);

            tbl.addCell(getCellDocs(cellStyle, titleStyleCenter, titleStyle, docsVh));
            tbl.setSpacingAfter(10);
            document.add(tbl);

            PdfPTable tblDynamic = new PdfPTable(3);
            tblDynamic.setWidthPercentage(100);
            tblDynamic.setSpacingAfter(10);
            if (hasDynamic) {
                document.add(getDynamicTab(titleStyle, cellStyle, dataDynamic, tblDynamic));
            }

            MtoCfg cfg = new MtoCfg().select(1, conn);

            int[] withsChk = new int[11];
            withsChk[0] = 12;//tipo
            withsChk[1] = 28;//rubro
            withsChk[2] = 15;//fecha
            withsChk[3] = 12;//km
            withsChk[4] = 12;//factura
            withsChk[5] = 12;//orden
            withsChk[6] = 12;//O.Trabajo
            withsChk[7] = 40;//descripcion
            withsChk[8] = 35;//proveedor / cliente
            withsChk[9] = 9;//cantidad
            withsChk[10] = 17;//valor
            //-------------------------------------
            int[] withs = new int[10];
            withs[0] = 12;//tipo
            withs[1] = 28;//rubro
            withs[2] = 15;//fecha
            withs[3] = 12;//km
            withs[4] = 12;//factura
            withs[5] = 12;//orden
            withs[6] = 40;//descripcion
            withs[7] = 35;//proveedor / cliente
            withs[8] = 13;//cantidad
            withs[9] = 17;//valor

            Boolean cabecera = true;
            for (int i = 0; i < data.getAreas().size(); i++) {
                HistoryArea harea = data.getAreas().get(i);
                int tblAreaId = harea.getAreaId();
                boolean ing = harea.getAreaType().equals("ing");
                PdfPTable tblIn = new PdfPTable(cfg.mtoChkOrder && tblAreaId > 0 ? 11 : 10);
                tblIn.setWidthPercentage(100);
                tblIn.setWidths(cfg.mtoChkOrder && tblAreaId > 0 ? withsChk : withs);

                tblIn.addCell(titleStyleCenter.getCell((tblAreaId <= 0) ? "Boletas de Combustible" : (harea.getAreaType().equals("ing")) ? "Novedades en el rubro " + harea.getAreaName() : "Novedades en el área / rubro " + harea.getAreaName(), (cfg.mtoChkOrder ? 11 : 10), 1));
                tblIn.addCell(titleStyle.getCell("Tipo"));
                tblIn.addCell(titleStyle.getCell(ing ? "Rubro" : "Súb Área / Rubro"));
                tblIn.addCell(titleStyle.getCell("Fecha"));
                tblIn.addCell(titleStyle.getCell("km"));
                tblIn.addCell(titleStyle.getCell("Factura"));
                tblIn.addCell(titleStyle.getCell("Orden"));
                if (cfg.mtoChkOrder && tblAreaId > 0) {
                    tblIn.addCell(titleStyle.getCell("Trabajo"));
                }
                tblIn.addCell(titleStyle.getCell("Descripción"));
                tblIn.addCell(titleStyle.getCell(ing ? "Proveedor" : "Cliente"));
                tblIn.addCell(titleStyle.getCell((cfg.mtoChkOrder ? "Cant" : "Cantidad")));
                tblIn.addCell(titleStyle.getCell("Valor"));
                BigDecimal totalCantidad = BigDecimal.ZERO;
                BigDecimal totalValor = BigDecimal.ZERO;
                boolean add = harea.getItems().size() > 0;
                for (int j = 0; j < harea.getItems().size(); j++) {
                    HistoryListItem it = harea.getItems().get(j);
                    tblIn.addCell(cellStyle.getCell(it.getType()));
                    tblIn.addCell(cellStyle.getCell(it.getSubArea()));
                    tblIn.addCell(cellStyle.getCell(it.getBegin() != null ? SDF.format(it.getBegin()) + "" : ""));
                    tblIn.addCell(cellStyle.getCell((it.getMileageCur()) + ""));
                    tblIn.addCell(cellStyle.getCell(it.getBillNum()));
                    tblIn.addCell(cellStyle.getCell(it.getOrderNum()));
                    if (cfg.mtoChkOrder && tblAreaId > 0) {
                        tblIn.addCell(cellStyle.getCell(it.getChkOrderNum()));
                    }
                    tblIn.addCell(cellStyle.getCell(it.getDescription() != null ? it.getDescription() : " "));
                    tblIn.addCell(cellStyle.getCell(it.getProvider()));
                    BigDecimal cantidad = (it.getAmount() != null ? it.getAmount() : BigDecimal.ZERO);
                    totalCantidad = totalCantidad.add(cantidad);
                    tblIn.addCell(cellStyle.getCell(DF.format(cantidad)));

                    if (!historyDTO.historyWithValues) {
                        tblIn.addCell(cellStyle.getCell(""));
                    } else {
                        BigDecimal val = (it.getValue() != null ? it.getValue() : BigDecimal.ZERO);
                        totalValor = totalValor.add(val);
                        tblIn.addCell(cellStyle.getCell(DF.format(val)));
                    }

                }
                if (add) {
                    if (cabecera) {
                        cabecera = false;
                    }
                    tblIn.addCell(titleStyle.getCell("Total", cfg.mtoChkOrder && tblAreaId > 0 ? 9 : 8, 1));
                    tblIn.addCell(titleStyle.getCell(new DecimalFormat("#").format(totalCantidad) + "", 1, 1));
                    if (!historyDTO.historyWithValues) {
                        tblIn.addCell(titleStyle.getCell("", 1, 1));
                    } else {
                        tblIn.addCell(titleStyle.getCell(DF.format(totalValor) + "", 1, 1));
                    }
                    tblIn.setSpacingAfter(10);
                    document.add(tblIn);
                }
            }
            document.close();

            return createResponse(fin, fin.getName());
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/PDFHistoryTask")
    public Response PDFHistoryTask(HistoryDTO historyDTO) {
        try (Connection conn = getConnection()) {
            URL resourceImg = this.getClass().getResource("/icons/tipial/camera2.png");
            VehicleList vehicleList = VehicleList.getVehicleListById(historyDTO.vehId, conn);
            Vehicle vh = new Vehicle().select(vehicleList.id, conn);

            MySQLQuery mq = new MySQLQuery("SELECT sb.area_id, a.name, p.dt, sb.name, p.kms, p.notes "
                    + "FROM mto_pend_task t "
                    + "INNER JOIN mto_task_prog p ON t.task_prog_id = p.id "
                    + "INNER JOIN maint_task mt ON mt.id = t.maint_task_id "
                    + "INNER JOIN area sb ON sb.id = mt.area_id "
                    + "INNER JOIN area a ON a.id = sb.area_id "
                    + "WHERE t.vehicle_id = ?1 "
                    + (historyDTO.areaId != null ? "AND a.id = ?2 " : "")
                    + (historyDTO.subAreaId != null ? "AND sb.id = ?3 " : "")
                    + (!historyDTO.rdbFull ? "AND p.dt BETWEEN ?4 AND ?5 " : ""));

            mq.setParam(1, vh.id);
            if (historyDTO.areaId != null) {
                mq.setParam(2, historyDTO.areaId);
            }
            if (historyDTO.subAreaId != null) {
                mq.setParam(3, historyDTO.subAreaId);
            }
            if (!historyDTO.rdbFull) {
                mq.setParam(4, historyDTO.begDate);
                mq.setParam(5, historyDTO.endDate);
            }

            Object[][] data = mq.getRecords(conn);

            Object[][] docsVh = new MySQLQuery("SELECT "
                    + "d.description, "
                    + "dv.fecha "
                    + "FROM document AS d "
                    + "INNER JOIN document_vehicle AS dv ON dv.doc_id = d.id "
                    + "WHERE dv.vehicle_id = " + vehicleList.id + " "
                    + "AND dv.apply = 1 "
                    + "GROUP BY dv.id ").getRecords(conn);

            Document document = new Document(new Rectangle(8.5f * 72f, 13f * 72f), 36f, 36f, 36f, 36f);
            File fin = File.createTempFile("hoja de vida", ".pdf");
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fin));
            HeaderFooter event = new HeaderFooter();
            writer.setBoxSize("art", new Rectangle(36, 54, 559, 788));
            writer.setPageEvent(event);
            document.open();
            PDFCellStyle titleStyle = new PDFCellStyle();
            titleStyle.setAppearance(true, PDFCellStyle.DEFAULT_PADDING, PDFCellStyle.GRAY_BACKGROUND, PDFCellStyle.GRAY_BORDER);
            titleStyle.sethAlignment(PDFCellStyle.ALIGN_LEFT);
            titleStyle.setFontInfo(true, PDFCellStyle.BLACK, PDFCellStyle.DEFAULT_FONT_SIZE - 1);
            PDFCellStyle titleStyleCenter = titleStyle.copy();
            titleStyleCenter.sethAlignment(PDFCellStyle.ALIGN_CENTER);
            PDFCellStyle cellStyle = new PDFCellStyle();
            cellStyle.setAppearance(true, PDFCellStyle.DEFAULT_PADDING, PDFCellStyle.WHITE, PDFCellStyle.GRAY_BORDER);
            cellStyle.sethAlignment(PDFCellStyle.ALIGN_LEFT);
            cellStyle.setFontSize(PDFCellStyle.DEFAULT_FONT_SIZE - 1);
            document.add(getTabHeader(conn, vehicleList.id, cellStyle, titleStyle));

            PdfPTable tbl = new PdfPTable(3);
            tbl.setWidths(new float[]{30, 35, 35});
            tbl.setWidthPercentage(100);
            tbl.addCell(getAdjImg(resourceImg, conn, vehicleList.id, cellStyle));
            PdfPCell celInt = cellStyle.getCell("");
            celInt.setHorizontalAlignment(Element.ALIGN_CENTER);
            celInt.setVerticalAlignment(Element.ALIGN_TOP);
            celInt.addElement(getInternalTab(titleStyle, cellStyle, titleStyleCenter, vehicleList, vh));
            tbl.addCell(celInt);

            tbl.addCell(getCellDocs(cellStyle, titleStyleCenter, titleStyle, docsVh));
            tbl.setSpacingAfter(10);
            document.add(tbl);

            int[] withs = new int[4];
            withs[0] = 10;//fecha
            withs[1] = 30;//subarea
            withs[2] = 15;//kms
            withs[3] = 45;//notas

            List<Integer> areas = getAreas(data);

            for (Integer areaId : areas) {
                PdfPTable tblIn = new PdfPTable(4);
                tblIn.setWidthPercentage(100);
                tblIn.setWidths(withs);

                tblIn.addCell(titleStyleCenter.getCell(getNameArea(data, areaId), 4, 1));
                tblIn.addCell(titleStyle.getCell("Fecha"));
                tblIn.addCell(titleStyle.getCell("Sub Área"));
                tblIn.addCell(titleStyle.getCell("kms"));
                tblIn.addCell(titleStyle.getCell("Notas"));
                for (Object[] row : data) {
                    Integer id = MySQLQuery.getAsInteger(row[0]);

                    if (Objects.equals(id, areaId)) {
                        tblIn.addCell(cellStyle.getCell(row[2] != null ? SDF.format(MySQLQuery.getAsDate(row[2])) + "" : ""));
                        tblIn.addCell(cellStyle.getCell(MySQLQuery.getAsString(row[3])));
                        tblIn.addCell(cellStyle.getCell(row[4] != null ? DF.format(MySQLQuery.getAsBigDecimal(row[4], false)) : ""));
                        tblIn.addCell(cellStyle.getCell(MySQLQuery.getAsString(row[5])));
                    }

                }
                document.add(tblIn);
            }

            document.close();

            return createResponse(fin, fin.getName());
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private List<Integer> getAreas(Object[][] data) {
        List<Integer> lst = new ArrayList<>();

        for (Object[] row : data) {
            Integer id = MySQLQuery.getAsInteger(row[0]);
            if (!lst.contains(id)) {
                lst.add(id);
            }
        }
        return lst;
    }

    private String getNameArea(Object[][] data, Integer areaId) {
        for (Object[] row : data) {
            Integer id = MySQLQuery.getAsInteger(row[0]);
            if (Objects.equals(id, areaId)) {
                return MySQLQuery.getAsString(row[1]);
            }
        }
        return "";
    }

    @POST
    @Path("/ExcelOrder")
    public Response ExcelOrder(HistoryDTO historyDTO) {
        try (Connection conn = getConnection()) {
            VehicleList vehicleList = VehicleList.getVehicleListById(historyDTO.vehId, conn);
            Vehicle vh = new Vehicle().select(vehicleList.id, conn);
            HistoryReport data = findHistoryReport(conn, historyDTO.vehId, historyDTO.areaId, historyDTO.subAreaId, historyDTO.rdbFull, historyDTO.begDate, historyDTO.endDate, historyDTO.filterTask);
            Object[][] docsVh = new MySQLQuery("SELECT "
                    + "d.description, "
                    + "dv.fecha "
                    + "FROM document AS d "
                    + "INNER JOIN document_vehicle AS dv ON dv.doc_id = d.id "
                    + "WHERE dv.vehicle_id = " + vehicleList.id + " "
                    + "AND dv.apply = 1 "
                    + "GROUP BY dv.id ").getRecords(conn);

            boolean hasDynamic = new MySQLQuery("SELECT COUNT(*)> 0 FROM mto_hist_field").getAsBoolean(conn);
            Object[][] dataDynamic = new MySQLQuery("SELECT  f.name, v.`data`,  f.`type` FROM vehicle vh LEFT JOIN sys_frm_value v ON v.owner_id = vh.id AND v.field_id IN (SELECT id FROM sys_frm_field f WHERE f.type_id = 1) INNER JOIN sys_frm_field f ON f.id = v.field_id INNER JOIN mto_hist_field hf ON hf.field_id = f.id WHERE vh.id = " + vh.id).getRecords(conn);
            if (hasDynamic) {
                dataFormat(dataDynamic);
            }

            int taskId = createTask(conn, historyDTO.empId, "HistoryApi.ExcelOrder");
            MySQLReport rep = new MySQLReport("Hoja de Vida " + vehicleList.plate, "", "hoja_vida", now(conn));
            rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0//0
            rep.getFormats().get(0).setWrap(true);
            rep.setMultiRowTitles(true);
            rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.LEFT, "dd/MM/yyyy"));//1
            rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "###,###"));//2
            rep.getFormats().add(new CellFormat(MySQLReportWriter.ENUM, MySQLReportWriter.LEFT, getKmSrcOptions("km_src", true, true, true, true)));//3
            rep.setZoomFactor(80);
            List<Column> colsVeh = new ArrayList();
            colsVeh.add(new Column("Interno", 15, 0));
            colsVeh.add(new Column("Clase", 20, 0));
            colsVeh.add(new Column("Tipo", 20, 0));
            colsVeh.add(new Column("Color", 13, 0));
            colsVeh.add(new Column("Modelo", 13, 0));
            colsVeh.add(new Column("Placa", 13, 0));
            colsVeh.add(new Column("Motor", 35, 0));
            colsVeh.add(new Column("Chasis", 35, 0));
            colsVeh.add(new Column("Cilindraje", 15, 0));
            colsVeh.add(new Column("Mto Preventivo", 15, 3));
            Table tblVeh = new Table("Datos del Vehículo " + vehicleList.plate);
            tblVeh.setColumns(colsVeh);

            Object[][] dataVeh = new Object[1][10];
            dataVeh[0][0] = vehicleList.internal;
            dataVeh[0][1] = vehicleList.vhClass;
            dataVeh[0][2] = vehicleList.type;
            dataVeh[0][3] = vh.color;
            dataVeh[0][4] = vh.model;
            dataVeh[0][5] = vehicleList.plate;
            dataVeh[0][6] = vh.engine;
            dataVeh[0][7] = vh.chasis;
            dataVeh[0][8] = vh.cylinderCap;
            dataVeh[0][9] = vh.prevMto ? "Si" : "No";
            tblVeh.setData(dataVeh);

            //COLUMNAS PARA LOS GASTOS
            List<Column> colsArs = new ArrayList();
            colsArs.add(new Column("Tipo.", 12, 0));//0
            colsArs.add(new Column("Súb Área / Rubro", 30, 0));
            colsArs.add(new Column("Fecha", 12, 1));
            colsArs.add(new Column("km", 12, 2));
            colsArs.add(new Column("Factura", 12, 0));
            colsArs.add(new Column("Orden", 12, 0));
            colsArs.add(new Column("Descripción", 40, 0));
            colsArs.add(new Column("Proveedor", 35, 0));
            colsArs.add(new Column("Cantidad", 13, 2));
            colsArs.add(new Column("Valor", 15, 2));
            Table tblArsModel = new Table("Novedades");
            tblArsModel.setColumns(colsArs);
            tblArsModel.setSummaryRow(new SummaryRow("Total", 8));

            //COLUMNAS PARA LOS INGRESOS
            List<Column> colsIng = new ArrayList();
            colsIng.add(new Column("Tipo.", 12, 0));//0
            colsIng.add(new Column("Rubro", 30, 0));
            colsIng.add(new Column("Fecha", 12, 1));
            colsIng.add(new Column("km", 12, 2));
            colsIng.add(new Column("Factura", 12, 0));
            colsIng.add(new Column("Orden", 12, 0));
            colsIng.add(new Column("Descripción", 40, 0));
            colsIng.add(new Column("Cliente", 35, 0));
            colsIng.add(new Column("Cantidad", 13, 2));
            colsIng.add(new Column("Valor", 15, 2));
            Table tblIngModel = new Table("Novedades");
            tblIngModel.setColumns(colsIng);
            tblIngModel.setSummaryRow(new SummaryRow("Total", 8));

            Boolean cabecera = true;
            for (int i = 0; i < data.getAreas().size(); i++) {
                HistoryArea harea = data.getAreas().get(i);
                Table tbls = new Table(harea.getAreaType().equals("ing") ? tblIngModel : tblArsModel);
                if (harea.getItems().size() > 0) {
                    tbls.setTitle((harea.getAreaId() <= 0) ? "Boletas de Combustible" : (harea.getAreaType().equals("ing")) ? "Novedades en el rubro " + harea.getAreaName() : "Novedades en el área / rubro " + harea.getAreaName());
                    Object[][] dataArs = new Object[harea.getItems().size()][10];
                    for (int j = 0; j < harea.getItems().size(); j++) {
                        HistoryListItem it = harea.getItems().get(j);
                        Object[] row = new Object[10];
                        row[0] = it.getType();
                        row[1] = it.getSubArea();
                        row[2] = it.getBegin();
                        row[3] = it.getMileageCur();
                        row[4] = it.getBillNum();
                        row[5] = it.getOrderNum();
                        row[6] = it.getDescription() != null ? it.getDescription() : " ";
                        row[7] = it.getProvider();
                        row[8] = it.getAmount() != null ? it.getAmount() : 0; //cantidad
                        row[9] = !historyDTO.historyWithValues ? 0 : it.getValue() != null ? it.getValue() : 0;
                        dataArs[j] = row;
                    }
                    tbls.setData(dataArs);
                    if (tbls.getData().length > 0) {
                        if (cabecera && tblVeh.getData().length > 0) {
                            rep.getTables().add(tblVeh);
                            cabecera = false;
                        }
                        rep.getTables().add(tbls);
                    }
                }
            }
            endTask(conn, taskId);

            File file = Reports.createReportFile("ExcelOrder", "xls");
            useDefault(conn);
            MySQLReportWriter.write(rep, file, conn);
            return createResponse(file, file.getName());
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/PDFEvents")
    public Response PDFEvents(HistoryDTO historyDTO) {
        try (Connection conn = getConnection()) {
            URL resourceImg = this.getClass().getResource("/icons/tipial/camera2.png");
            VehicleList vehicleList = VehicleList.getVehicleListById(historyDTO.vehId, conn);
            Vehicle vh = new Vehicle().select(vehicleList.id, conn);
            Object[][] docsVh = new MySQLQuery("SELECT "
                    + "d.description, "
                    + "dv.fecha "
                    + "FROM document AS d "
                    + "INNER JOIN document_vehicle AS dv ON dv.doc_id = d.id "
                    + "WHERE dv.vehicle_id = " + vehicleList.id + " "
                    + "AND dv.apply = 1 "
                    + "GROUP BY dv.id ").getRecords(conn);
            Object[][] dataNote = new MySQLQuery("SELECT "
                    + "t.name, "//0
                    + "DATE(n.note_date), "
                    + "n.note, "
                    + "DATE(n.rev_date) "
                    + "FROM mto_vh_note AS n "//3
                    + "INNER JOIN mto_note_type AS t ON n.type_id = t.id "//4
                    + "WHERE "
                    + (historyDTO.rdbEvDate ? "n.note_date BETWEEN ?1 AND ?2 AND " : " ")
                    + "n.vehicle_id = " + vehicleList.id).setParam(1, historyDTO.begDate2).setParam(2, historyDTO.endDate2).getRecords(conn);
            Object[][] dataAcciden = new MySQLQuery("SELECT "
                    + "if(a.accident_incident=1,'Accidente','Incidente'), "//0
                    + "DATE(a.accident_date), "//1
                    + "a.damage_description,"
                    + (!historyDTO.historyWithValues ? " 0 " : "a.total_value ")//2
                    + "FROM mto_accident a "//3
                    + "WHERE "
                    + (historyDTO.rdbEvDate ? "a.accident_date BETWEEN ?1 AND ?2 AND " : " ")
                    + "a.vehicle_id = " + vehicleList.id).setParam(1, historyDTO.begDate2).setParam(2, historyDTO.endDate2).getRecords(conn);
            Object[][] dataFine = new MySQLQuery("SELECT "
                    + "if(f.paid=1,'Sí','No'), "//0
                    + "DATE (f.fine_date), "//1
                    + "f.cause, "
                    + (!historyDTO.historyWithValues ? " 0 " : "f.value ")
                    + "FROM mto_fine AS f "//3
                    + "WHERE "
                    + (historyDTO.rdbEvDate ? "f.fine_date BETWEEN ?1 AND ?2 AND " : " ")
                    + "f.vehicle_id = " + vehicleList.id).setParam(1, historyDTO.begDate2).setParam(2, historyDTO.endDate2).getRecords(conn);
            Object[][] dataOut = new MySQLQuery("SELECT "
                    + "t.name, "//0
                    + "s.since, "
                    + "s.comments, "//
                    + "s.end "
                    + "FROM mto_out_service AS s "//3
                    + "INNER JOIN mto_out_type AS t ON s.out_id = t.id "//4
                    + "WHERE "
                    + (historyDTO.rdbEvDate ? "s.since BETWEEN ?1 AND ?2 AND " : " ")
                    + "s.vehicle_id = " + vehicleList.id).setParam(1, historyDTO.begDate2).setParam(2, historyDTO.endDate2).getRecords(conn);

            boolean hasDynamic = new MySQLQuery("SELECT COUNT(*)> 0 FROM mto_hist_field").getAsBoolean(conn);
            Object[][] dataDynamic = new MySQLQuery("SELECT  f.name, v.`data`,  f.`type` FROM vehicle vh LEFT JOIN sys_frm_value v ON v.owner_id = vh.id AND v.field_id IN (SELECT id FROM sys_frm_field f WHERE f.type_id = 1) INNER JOIN sys_frm_field f ON f.id = v.field_id INNER JOIN mto_hist_field hf ON hf.field_id = f.id WHERE vh.id = " + vh.id).getRecords(conn);
            if (hasDynamic) {
                dataFormat(dataDynamic);
            }

            Document document = new Document(new Rectangle(8.5f * 72f, 13f * 72f), 36f, 36f, 36f, 36f);
            File fin = File.createTempFile("hoja de vida", ".pdf");
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fin));
            HeaderFooter event = new HeaderFooter();
            writer.setBoxSize("art", new Rectangle(36, 54, 559, 788));
            writer.setPageEvent(event);
            document.open();
            PDFCellStyle titleStyle = new PDFCellStyle();
            titleStyle.setAppearance(true, PDFCellStyle.DEFAULT_PADDING, PDFCellStyle.GRAY_BACKGROUND, PDFCellStyle.GRAY_BORDER);
            titleStyle.sethAlignment(PDFCellStyle.ALIGN_LEFT);
            titleStyle.setFontInfo(true, PDFCellStyle.BLACK, PDFCellStyle.DEFAULT_FONT_SIZE - 1);
            PDFCellStyle titleStyleCenter = titleStyle.copy();
            titleStyleCenter.sethAlignment(PDFCellStyle.ALIGN_CENTER);
            PDFCellStyle cellStyle = new PDFCellStyle();
            cellStyle.setAppearance(true, PDFCellStyle.DEFAULT_PADDING, PDFCellStyle.WHITE, PDFCellStyle.GRAY_BORDER);
            cellStyle.sethAlignment(PDFCellStyle.ALIGN_LEFT);
            cellStyle.setFontSize(PDFCellStyle.DEFAULT_FONT_SIZE - 1);
            document.add(getTabHeader(conn, vehicleList.id, cellStyle, titleStyle));
            PdfPTable tbl = new PdfPTable(3);
            tbl.setWidths(new float[]{30, 35, 35});
            tbl.setWidthPercentage(100);
            tbl.addCell(getAdjImg(resourceImg, conn, vehicleList.id, cellStyle));
            PdfPCell celInt = cellStyle.getCell("");
            celInt.setHorizontalAlignment(Element.ALIGN_CENTER);
            celInt.setVerticalAlignment(Element.ALIGN_TOP);
            celInt.addElement(getInternalTab(titleStyle, cellStyle, titleStyleCenter, vehicleList, vh));
            tbl.addCell(celInt);
            tbl.addCell(getCellDocs(cellStyle, titleStyleCenter, titleStyle, docsVh));
            tbl.setSpacingAfter(10);
            document.add(tbl);

            PdfPTable tblDynamic = new PdfPTable(3);
            tblDynamic.setWidthPercentage(100);
            tblDynamic.setSpacingAfter(10);
            if (hasDynamic) {
                document.add(getDynamicTab(titleStyle, cellStyle, dataDynamic, tblDynamic));
            }

            PdfPTable notes = new PdfPTable(4);
            notes.setWidthPercentage(100);
            notes.setSpacingBefore(10);
            notes.setWidths(new int[]{30, 20, 50, 20});
            notes.addCell(titleStyleCenter.getCell("Novedades", 4, 1));
            notes.addCell(titleStyle.getCell("Tipo"));
            notes.addCell(titleStyle.getCell("Fecha"));
            notes.addCell(titleStyle.getCell("Descripción"));
            notes.addCell(titleStyle.getCell("Revisión"));
            for (Object[] row : dataNote) {
                notes.addCell(cellStyle.getCell((row[0] != null ? row[0] : "") + ""));
                notes.addCell(cellStyle.getCell((row[1] != null ? SDF.format(MySQLQuery.getAsDate(row[1])) : "")));
                notes.addCell(cellStyle.getCell((row[2] != null ? row[2] : "") + ""));
                notes.addCell(cellStyle.getCell((row[3] != null ? SDF.format(MySQLQuery.getAsDate(row[3])) : "")));
            }
            PdfPTable accident = new PdfPTable(4);
            accident.setWidthPercentage(100);
            accident.setSpacingBefore(10);
            accident.setWidths(new int[]{30, 20, 50, 20});
            accident.addCell(titleStyleCenter.getCell("Accidentes - Incidentes", 4, 1));
            accident.addCell(titleStyle.getCell("Tipo"));
            accident.addCell(titleStyle.getCell("Fecha"));
            accident.addCell(titleStyle.getCell("Observaciónes"));
            accident.addCell(titleStyle.getCell("Valor"));
            for (Object[] row : dataAcciden) {
                accident.addCell(cellStyle.getCell((row[0] != null ? row[0] : "") + ""));
                accident.addCell(cellStyle.getCell((row[1] != null ? SDF.format(MySQLQuery.getAsDate(row[1])) : "")));
                accident.addCell(cellStyle.getCell((row[2] != null ? row[2] : "") + ""));
                if (!historyDTO.historyWithValues) {
                    accident.addCell(cellStyle.getCell(""));
                } else {
                    accident.addCell(cellStyle.getCell(DF.format(MySQLQuery.getAsBigDecimal(row[3], true)) + ""));
                }
            }
            PdfPTable fine = new PdfPTable(4);
            fine.setWidthPercentage(100);
            fine.setSpacingBefore(10);
            fine.setWidths(new int[]{30, 20, 50, 20});
            fine.addCell(titleStyleCenter.getCell("Comparendos", 4, 1));
            fine.addCell(titleStyle.getCell("Pagado"));
            fine.addCell(titleStyle.getCell("Fecha"));
            fine.addCell(titleStyle.getCell("Causa"));
            fine.addCell(titleStyle.getCell("Valor"));
            for (Object[] row : dataFine) {
                fine.addCell(cellStyle.getCell((row[0] != null ? row[0] : "") + ""));
                fine.addCell(cellStyle.getCell((row[1] != null ? SDF.format(MySQLQuery.getAsDate(row[1])) : "")));
                fine.addCell(cellStyle.getCell((row[2] != null ? row[2] : "") + ""));
                if (!historyDTO.historyWithValues) {
                    fine.addCell(cellStyle.getCell(""));
                } else {
                    fine.addCell(cellStyle.getCell(DF.format(MySQLQuery.getAsBigDecimal(row[3], true)) + ""));
                }

            }
            PdfPTable out = new PdfPTable(4);
            out.setWidthPercentage(100);
            out.setSpacingBefore(10);
            out.setWidths(new int[]{30, 20, 50, 20});
            out.addCell(titleStyleCenter.getCell("Fuera de Servicio", 4, 1));
            out.addCell(titleStyle.getCell("Tipo"));
            out.addCell(titleStyle.getCell("Inicio"));
            out.addCell(titleStyle.getCell("Descripción"));
            out.addCell(titleStyle.getCell("Fin"));
            for (Object[] row : dataOut) {
                out.addCell(cellStyle.getCell((row[0] != null ? row[0] : "") + ""));
                out.addCell(cellStyle.getCell((row[1] != null ? SDF.format(MySQLQuery.getAsDate(row[1])) : "")));
                out.addCell(cellStyle.getCell((row[2] != null ? row[2] : "") + ""));
                out.addCell(cellStyle.getCell((row[3] != null ? SDF.format(MySQLQuery.getAsDate(row[3])) : "")));
            }
            if (historyDTO.cmbFilterIndex == 0) {
                if (dataNote.length > 0) {
                    document.add(notes);
                }
                if (dataAcciden.length > 0) {
                    document.add(accident);
                }
                if (dataFine.length > 0) {
                    document.add(fine);
                }
                if (dataOut.length > 0) {
                    document.add(out);
                }
            } else if (historyDTO.cmbFilterIndex == 1) {
                if (dataAcciden.length > 0) {
                    document.add(accident);
                }
            } else if (historyDTO.cmbFilterIndex == 2) {
                if (dataFine.length > 0) {
                    document.add(fine);
                }
            } else if (historyDTO.cmbFilterIndex == 3) {
                if (dataOut.length > 0) {
                    document.add(out);
                }
            } else if (historyDTO.cmbFilterIndex == 4) {
                if (dataNote.length > 0) {
                    document.add(notes);
                }
            }
            document.close();

            return createResponse(fin, fin.getName());
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/ExcelEvents")
    public Response ExcelEvents(HistoryDTO historyDTO) {
        try (Connection conn = getConnection()) {
            VehicleList vehicleList = VehicleList.getVehicleListById(historyDTO.vehId, conn);
            Vehicle vh = new Vehicle().select(vehicleList.id, conn);
            Object[][] docsVh = new MySQLQuery("SELECT "
                    + "d.description, "
                    + "dv.fecha "
                    + "FROM document AS d "
                    + "INNER JOIN document_vehicle AS dv ON dv.doc_id = d.id "
                    + "WHERE dv.vehicle_id = " + vehicleList.id + " "
                    + "AND dv.apply = 1 "
                    + "GROUP BY dv.id ").getRecords(conn);
            Object[][] dataNote = new MySQLQuery("SELECT "
                    + "t.name, "//0
                    + "DATE(n.note_date), "
                    + "n.note, "
                    + "DATE(n.rev_date) "
                    + "FROM mto_vh_note AS n "//3
                    + "INNER JOIN mto_note_type AS t ON n.type_id = t.id "//4
                    + "WHERE "
                    + (historyDTO.rdbEvDate ? "n.note_date BETWEEN ?1 AND ?2 AND " : " ")
                    + "n.vehicle_id = " + vehicleList.id).setParam(1, historyDTO.begDate2).setParam(2, historyDTO.endDate2).getRecords(conn);
            Object[][] dataAcciden = new MySQLQuery("SELECT "
                    + "if(a.accident_incident=1,'Accidente','Incidente'), "//0
                    + "DATE(a.accident_date), "//1
                    + "a.damage_description,"
                    + (!historyDTO.historyWithValues ? " 0 " : "a.total_value ")//2
                    + "FROM mto_accident a "//3
                    + "WHERE "
                    + (historyDTO.rdbEvDate ? "a.accident_date BETWEEN ?1 AND ?2 AND " : " ")
                    + "a.vehicle_id = " + vehicleList.id).setParam(1, historyDTO.begDate2).setParam(2, historyDTO.endDate2).getRecords(conn);
            Object[][] dataFine = new MySQLQuery("SELECT "
                    + "if(f.paid=1,'Sí','No'), "//0
                    + "DATE (f.fine_date), "//1
                    + "f.cause, "
                    + (!historyDTO.historyWithValues ? " 0 " : "f.value ")
                    + "FROM mto_fine AS f "//3
                    + "WHERE "
                    + (historyDTO.rdbEvDate ? "f.fine_date BETWEEN ?1 AND ?2 AND " : " ")
                    + "f.vehicle_id = " + vehicleList.id).setParam(1, historyDTO.begDate2).setParam(2, historyDTO.endDate2).getRecords(conn);
            Object[][] dataOut = new MySQLQuery("SELECT "
                    + "t.name, "//0
                    + "s.since, "
                    + "s.comments, "//
                    + "s.end "
                    + "FROM mto_out_service AS s "//3
                    + "INNER JOIN mto_out_type AS t ON s.out_id = t.id "//4
                    + "WHERE "
                    + (historyDTO.rdbEvDate ? "s.since BETWEEN ?1 AND ?2 AND " : " ")
                    + "s.vehicle_id = " + vehicleList.id).setParam(1, historyDTO.begDate2).setParam(2, historyDTO.endDate2).getRecords(conn);

            boolean hasDynamic = new MySQLQuery("SELECT COUNT(*)> 0 FROM mto_hist_field").getAsBoolean(conn);
            Object[][] dataDynamic = new MySQLQuery("SELECT  f.name, v.`data`,  f.`type` FROM vehicle vh LEFT JOIN sys_frm_value v ON v.owner_id = vh.id AND v.field_id IN (SELECT id FROM sys_frm_field f WHERE f.type_id = 1) INNER JOIN sys_frm_field f ON f.id = v.field_id INNER JOIN mto_hist_field hf ON hf.field_id = f.id WHERE vh.id = " + vh.id).getRecords(conn);
            if (hasDynamic) {
                dataFormat(dataDynamic);
            }

            MySQLReport rep = new MySQLReport("Hoja de Vida " + vehicleList.plate, "", "hoja_vida", now(conn));
            rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
            rep.getFormats().get(0).setWrap(true);
            rep.setMultiRowTitles(true);
            rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.LEFT, "dd/MM/yyyy"));//1
            rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "###,###"));//2
            rep.setZoomFactor(85);

            List<Column> colNote = new ArrayList();
            colNote.add(new Column("Tipo", 30, 0));
            colNote.add(new Column("Fecha", 20, 1));
            colNote.add(new Column("Descripción", 50, 0));
            colNote.add(new Column("Revisión", 20, 1));
            Table tblNote = new Table("Novedades");
            tblNote.setColumns(colNote);
            tblNote.setData(dataNote);

            List<Column> colsAccident = new ArrayList();
            colsAccident.add(new Column("Tipo", 30, 0));
            colsAccident.add(new Column("Fecha", 20, 1));
            colsAccident.add(new Column("Observaciónes", 50, 0));
            colsAccident.add(new Column("Valor", 20, 2));
            Table tblAccident = new Table("Accidentes - Incidentes");
            tblAccident.setColumns(colsAccident);
            tblAccident.setData(dataAcciden);

            List<Column> colsFine = new ArrayList();
            colsFine.add(new Column("Pagado", 30, 0));
            colsFine.add(new Column("Fecha", 20, 1));
            colsFine.add(new Column("Causa", 50, 0));
            colsFine.add(new Column("Valor", 20, 2));
            Table tblFine = new Table("Comparendos");
            tblFine.setColumns(colsFine);
            tblFine.setData(dataFine);

            List<Column> colsOut = new ArrayList();
            colsOut.add(new Column("Tipo", 30, 0));
            colsOut.add(new Column("Inicio", 20, 1));
            colsOut.add(new Column("Descripción", 50, 0));
            colsOut.add(new Column("Fin", 20, 1));
            Table tblOut = new Table("Fuera de Servicio");
            tblOut.setColumns(colsOut);
            tblOut.setData(dataOut);

            if (historyDTO.cmbFilterIndex == 0) {
                if (tblNote.getData().length > 0) {
                    rep.getTables().add(tblNote);
                }
                if (tblAccident.getData().length > 0) {
                    rep.getTables().add(tblAccident);
                }
                if (tblFine.getData().length > 0) {
                    rep.getTables().add(tblFine);
                }
                if (tblOut.getData().length > 0) {
                    rep.getTables().add(tblOut);
                }
            } else if (historyDTO.cmbFilterIndex == 1) {
                if (tblAccident.getData().length > 0) {
                    rep.getTables().add(tblAccident);
                }
            } else if (historyDTO.cmbFilterIndex == 2) {
                if (tblFine.getData().length > 0) {
                    rep.getTables().add(tblFine);
                }
            } else if (historyDTO.cmbFilterIndex == 3) {
                if (tblOut.getData().length > 0) {
                    rep.getTables().add(tblOut);
                }
            } else if (historyDTO.cmbFilterIndex == 4) {
                if (tblNote.getData().length > 0) {
                    rep.getTables().add(tblNote);
                }
            }

            File file = Reports.createReportFile("ExcelEvents", "xls");
            useDefault(conn);
            MySQLReportWriter.write(rep, file, conn);
            return createResponse(file, file.getName());
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    public String getKmSrcOptions(String fieldName, boolean trips, boolean chkList, boolean gps, boolean showKmManual) {
        if (fieldName.equals("km_src")) {
            return (trips ? "route=Recorrido Ruta&" : "")
                    + "fueload=km - Tanqueos&"
                    + (chkList ? "chk=km - Registros&" : "")
                    + (gps ? "gps=km - GPS&" : "")
                    + (showKmManual ? "manual=Manual&" : "")
                    + "none=Ninguno";
        }
        return null;
    }

    private static PdfPTable getDynamicTab(PDFCellStyle titleStyle, PDFCellStyle cellStyle, Object[][] dataDynamic, PdfPTable tblDynamic) throws Exception {
        double fields = dataDynamic.length;
        double numTables = 3;
        double interval = Math.ceil(fields / numTables);
        for (int i = 0; i < numTables; i++) {
            PdfPCell c2 = cellStyle.getCell("");
            c2.setHorizontalAlignment(Element.ALIGN_CENTER);
            c2.setVerticalAlignment(Element.ALIGN_TOP);

            PdfPTable dynamicTab = new PdfPTable(2);
            dynamicTab.setWidths(new float[]{50, 50});
            dynamicTab.setWidthPercentage(100);
            dynamicTab.setWidths(new float[]{50, 50});

            int beg = (int) (interval * i);
            int end = (int) (beg + interval);
            if (i == 2) {//ultima columna
                end = dataDynamic.length;
            }
            for (int j = beg; j < end && fields >= end; j++) {
                Object[] row = dataDynamic[j];
                dynamicTab.addCell(titleStyle.getCell(row[0].toString()));
                dynamicTab.addCell(cellStyle.getCell(row[1].toString()));
            }
            c2.addElement(dynamicTab);
            tblDynamic.addCell(c2);
        }
        return tblDynamic;
    }

    private static PdfPTable getInternalTab(PDFCellStyle titleStyle, PDFCellStyle cellStyle, PDFCellStyle titleStyleCenter, VehicleList vehicleList, Vehicle vh) throws Exception {
        PdfPTable internalTab = new PdfPTable(2);
        internalTab.setWidths(new float[]{50, 50});
        internalTab.setWidthPercentage(100);
        internalTab.addCell(titleStyleCenter.getCell("Basicos", 2, 1));
        internalTab.setWidths(new float[]{50, 50});
        internalTab.addCell(titleStyle.getCell("Interno"));
        internalTab.addCell(cellStyle.getCell(vehicleList.internal));
        internalTab.addCell(titleStyle.getCell("Clase"));
        internalTab.addCell(cellStyle.getCell(vehicleList.vhClass));
        internalTab.addCell(titleStyle.getCell("Tipo"));
        internalTab.addCell(cellStyle.getCell(vehicleList.type));
        internalTab.addCell(titleStyle.getCell("Color"));
        internalTab.addCell(cellStyle.getCell(vh.color));
        internalTab.addCell(titleStyle.getCell("Modelo"));
        internalTab.addCell(cellStyle.getCell(vh.model != null ? vh.model + "" : ""));
        internalTab.addCell(titleStyle.getCell("Placa"));
        internalTab.addCell(cellStyle.getCell(vehicleList.plate));
        internalTab.addCell(titleStyle.getCell("Motor"));
        internalTab.addCell(cellStyle.getCell(vh.engine));
        internalTab.addCell(titleStyle.getCell("Chasis"));
        internalTab.addCell(cellStyle.getCell(vh.chasis));
        internalTab.addCell(titleStyle.getCell("Cilindraje"));
        internalTab.addCell(cellStyle.getCell(vh.cylinderCap != null ? vh.cylinderCap + "" : ""));
        internalTab.addCell(titleStyle.getCell("Mto Preventivo"));
        internalTab.addCell(cellStyle.getCell(vh.prevMto ? "Si" : "No"));
        return internalTab;
    }

    private static PdfPCell getCellDocs(PDFCellStyle cellStyle, PDFCellStyle titleStyleCenter, PDFCellStyle titleStyle, Object[][] docsVh) throws Exception {
        PdfPCell c2 = cellStyle.getCell("");
        PdfPTable tbl2 = new PdfPTable(2);
        tbl2.setWidths(new float[]{50, 50});
        tbl2.setWidthPercentage(100);
        tbl2.addCell(titleStyleCenter.getCell("Documentos", 2, 1));
        for (Object[] rowDoc : docsVh) {
            tbl2.addCell(titleStyle.getCell(rowDoc[0].toString()));
            tbl2.addCell(cellStyle.getCell(rowDoc[1] != null ? SDF.format(rowDoc[1]) : ""));
        }
        c2.addElement(tbl2);
        c2.setHorizontalAlignment(Element.ALIGN_CENTER);
        c2.setVerticalAlignment(Element.ALIGN_TOP);
        return c2;
    }

    private static PdfPCell getAdjImg(URL resourceImg, Connection ep, Integer vhId, PDFCellStyle cellStyle) throws Exception {
        Integer photoId = new MySQLQuery("SELECT id FROM bfile WHERE owner_id = " + vhId + " AND owner_type = " + MTO_VEH_PHOTO + " ORDER BY created DESC LIMIT 0,1").getAsInteger(ep);
        Image img = Image.getInstance(resourceImg);
        try {
            img = photoId != null ? Image.getInstance(getFile(ep, photoId)) : Image.getInstance(resourceImg);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (photoId == null) {
            img.scalePercent(40);
        }
        img.setAlignment(Element.ALIGN_CENTER);
        img.setWidthPercentage(100);
        PdfPCell imgCell = new PdfPCell(img, (photoId != null));
        imgCell.setPadding(5);
        imgCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        imgCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        imgCell.setBorderColor(cellStyle.getBorderColor());
        return imgCell;
    }

    public static byte[] getFile(Connection conn, int fileId) throws Exception {
        fileManager.PathInfo pInfo = new fileManager.PathInfo(conn);

        File f = pInfo.getExistingFile(fileId);
        if (f == null) {
            throw new Exception("El archivo no exíste");
        }
        return Files.readAllBytes(f.toPath());
    }

    private static PdfPTable getTabHeader(Connection ep, Integer vhId, PDFCellStyle cellStyle, PDFCellStyle titleStyle) throws Exception {
        PdfPTable tab = new PdfPTable(2);
        tab.setWidths(new float[]{40, 60});
        tab.setWidthPercentage(100);
        Boolean entIcon = new MySQLQuery("SELECT e.alternative "
                + "FROM vehicle AS v "
                + "INNER JOIN agency AS a ON a.id = v.agency_id "
                + "INNER JOIN enterprise AS e ON e.id = a.enterprise_id "
                + "WHERE v.id = " + vhId).getAsBoolean(ep);
        PdfPCell imgCell;
        try {
            File file = entIcon ? enterpriseLogo.getEnterpriseLogo("6", ep) : enterpriseLogo.getEnterpriseLogo("4", ep);
            byte[] readAllBytes = Files.readAllBytes(file.toPath());
            Image img = Image.getInstance(readAllBytes);
            img.setAlignment(Element.ALIGN_CENTER);
            img.scaleToFit(80, 80);
            imgCell = new PdfPCell(img);
        } catch (Exception ex) {
            imgCell = new PdfPCell();
        }
        imgCell.setPadding(5);
        imgCell.setBorderColor(cellStyle.getBorderColor());
        imgCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        tab.addCell(imgCell);
        PDFCellStyle nobackgroundCell = titleStyle.copy();
        nobackgroundCell.setBackgroundColor(cellStyle.getBackgrounColor());
        tab.addCell(nobackgroundCell.getCell("HOJA DE VIDA", PDFCellStyle.ALIGN_CENTER));
        tab.setSpacingAfter(10);
        return tab;
    }

    public static HistoryReport findHistoryReport(Connection con, int idVeh, Integer areaId, Integer subaId, boolean full, String fBeginStr, String fEndStr, String filterTask) throws Exception {
        Date fBegin = Dates.getSQLDateTimeFormat().parse(fBeginStr);
        Date fEnd = Dates.getSQLDateTimeFormat().parse(fEndStr);

        Area[] areas;
        if (areaId == null) {
            areas = Area.getSuperAreas(null, con);

        } else {
            areas = new Area[1];
            areas[0] = Area.getAreaById(areaId, con);
        }

        HistoryReport report = new HistoryReport();

        for (Area area : areas) {
            HistoryArea ha = new HistoryArea();
            report.getAreas().add(ha);
            ha.setArea(area.getId(), area.getName(), area.getPuc(), area.getType());

            boolean includeDetail = new MySQLQuery("SELECT work_order_item_detail FROM mto_cfg").getAsBoolean(con);
            boolean includeChkOrder = new MySQLQuery("SELECT mto_chk_order FROM mto_cfg").getAsBoolean(con);

            MySQLQuery q1 = new MySQLQuery("SELECT "
                    + "IF(mt.id IS NOT NULL, 'Programada', IF(o.kind='store','Almacén',IF(o.kind = 'sum','Suministro',IF(o.kind = 'tra','Trabajo',IF(o.kind='com','Compra',IF(o.kind='men','Caja Menor',IF(o.kind='car','Combustible',o.kind))))))) , "
                    + "child.name, "
                    + "o.begin, "
                    + "i.amount, "
                    + (includeDetail
                            ? "if(i.description IS NOT NULL, CONCAT(i.description, ' ', det.`name`), CONCAT(mt.description, ' ', det.`name`)), "
                            : "if(i.description IS NOT NULL, i.description, mt.description) , ")
                    + "o.order_num, "
                    + "IF(p.name IS NOT NULL,p.name, c.name) , "
                    + "o.bill_num, "
                    + "i.value, "
                    + "o.mileage_cur, "
                    + (includeChkOrder ? " CAST(o.chk_order_id AS CHAR) " : " '' ")
                    + "FROM work_order o "
                    + "INNER JOIN item i on i.work_id = o.id "
                    + "INNER JOIN area child on child.id = i.area_id "
                    + "LEFT JOIN prov_provider p on p.id = o.provider_id "
                    + "LEFT JOIN crm_client c on c.id = o.client_id "
                    + "LEFT JOIN maint_task mt on i.maint_task_id = mt.id "
                    + (includeDetail ? "LEFT JOIN mto_detail det ON det.id = i.detail_id " : "")
                    + (includeChkOrder ? "LEFT JOIN mto_chk_order ch ON ch.id = o.id " : "")
                    + "WHERE "
                    + (!full ? " o.begin BETWEEN ?1 AND ?2 AND " : "")
                    + (!filterTask.equals("none") ? " i.type = '" + filterTask + "' AND " : "")
                    + "o.canceled = 0 AND o.flow_status = 'done' AND "
                    + "o.vehicle_id = " + idVeh + " AND "
                    + "child.area_id = " + area.getId() + " "
                    + (subaId != null ? "and child.id = " + subaId + " " : "")
                    + " ORDER BY begin DESC ");
            q1.setParam(1, fBegin);
            q1.setParam(2, fEnd);
            Object[][] l1 = q1.getRecords(con);
            for (Object[] l11 : l1) {
                HistoryListItem hi = new HistoryListItem();
                ha.getItems().add(hi);
                hi.setType(l11[0].toString());
                hi.setSubArea((String) l11[1]);
                hi.setBegin((Date) l11[2]);
                hi.setAmount(l11[3] != null ? new BigDecimal(MySQLQuery.getAsLong(l11[3])) : null);
                hi.setDescription((String) l11[4]);
                hi.setOrderNum((String) l11[5]);
                hi.setProvider((String) l11[6]);
                hi.setBillNum((String) l11[7]);
                hi.setValue(l11[8] != null ? new BigDecimal(l11[8].toString()) : null);
                hi.setMileageCur(l11[9] != null ? (Integer) l11[9] : 0);
                hi.setChkOrderNum((String) l11[10]);
            }
        }

        String qs2
                = "SELECT "
                + "fl.days, fl.amount,w.description,ft.name,w.order_num,p.name,fl.cost,fl.bill_num "
                + "FROM "
                + "fuel_load fl "
                + "inner JOIN work_order w on w.id=fl.work_id "
                + "inner JOIN prov_provider p on p.id=fl.provider_id "
                + "inner JOIN fuel_type ft on ft.id=fl.fuel_type_id "
                + "where "
                + (!full ? " w.begin BETWEEN ?1 AND ?2 AND " : "")
                + "fl.vehicle_id=" + idVeh + " "
                + "ORDER BY w.begin DESC";

        MySQLQuery q2 = new MySQLQuery(qs2);
        q2.setParam(1, fBegin);
        q2.setParam(2, fEnd);
        Object[][] l2 = q2.getRecords(con);

        if (areaId == null && l2.length > 0) {
            Area cArea = new Area(-1, "Boletas de Combustible", "", null, "mto");
            HistoryArea comb = new HistoryArea();
            comb.setArea(cArea.getId(), cArea.getName(), cArea.getPuc(), cArea.getType());
            report.getAreas().add(comb);
            for (Object[] l21 : l2) {
                HistoryListItem hi = new HistoryListItem();
                comb.getItems().add(hi);
                hi.setType("comb");
                hi.setSubArea(" ");
                hi.setBegin((Date) l21[0]);
                hi.setAmount(l21[1] != null ? new BigDecimal(l21[1].toString()) : null);
                String des = (l21[2] != null ? l21[2] + " " : "") + (l21[3] != null ? l21[3] : "");
                hi.setDescription((String) des);
                hi.setOrderNum((String) l21[4]);
                hi.setProvider((String) l21[5]);
                hi.setValue(l21[6] != null ? new BigDecimal(l21[6].toString()) : null);
                hi.setBillNum(l21[7] != null ? (l21[7]).toString() : null);
            }
        }
        return report;
    }

    public static HistoryReport findHistoryOrderReport(Connection con, int idVeh, Integer areaId, Integer subaId, boolean full, String fBeginStr, String fEndStr, String filterTask) throws Exception {
        Date fBegin = Dates.getSQLDateTimeFormat().parse(fBeginStr);
        Date fEnd = Dates.getSQLDateTimeFormat().parse(fEndStr);

        Area[] areas;
        if (areaId == null) {
            areas = Area.getSuperAreas(null, con);

        } else {
            areas = new Area[1];
            areas[0] = Area.getAreaById(areaId, con);
        }

        HistoryReport report = new HistoryReport();

        for (Area area : areas) {
            HistoryArea ha = new HistoryArea();
            report.getAreas().add(ha);
            ha.setArea(area.getId(), area.getName(), area.getPuc(), area.getType());

            boolean includeDetail = new MySQLQuery("SELECT work_order_item_detail FROM mto_cfg").getAsBoolean(con);
            boolean includeChkOrder = new MySQLQuery("SELECT mto_chk_order FROM mto_cfg").getAsBoolean(con);

            MySQLQuery q1 = new MySQLQuery("SELECT "
                    + "IF(mt.id IS NOT NULL, 'Programada', IF(o.kind='store','Almacén',IF(o.kind = 'sum','Suministro',IF(o.kind = 'tra','Trabajo',IF(o.kind='com','Compra',IF(o.kind='men','Caja Menor',IF(o.kind='car','Combustible',o.kind))))))) , "
                    + "child.name, "
                    + "o.begin, "
                    + "i.amount, "
                    + (includeDetail
                            ? "if(i.description IS NOT NULL, CONCAT(i.description, ' ', det.`name`), CONCAT(mt.description, ' ', det.`name`)), "
                            : "if(i.description IS NOT NULL, i.description, mt.description) , ")
                    + "o.order_num, "
                    + "IF(p.name IS NOT NULL,p.name, c.name) , "
                    + "o.bill_num, "
                    + "i.value, "
                    + "o.mileage_cur, "
                    + (includeChkOrder ? " CAST(o.chk_order_id AS CHAR) " : " '' ")
                    + "FROM work_order o "
                    + "INNER JOIN item i on i.work_id = o.id "
                    + "INNER JOIN area child on child.id = i.area_id "
                    + "LEFT JOIN prov_provider p on p.id = o.provider_id "
                    + "LEFT JOIN crm_client c on c.id = o.client_id "
                    + "LEFT JOIN maint_task mt on i.maint_task_id = mt.id "
                    + (includeDetail ? "LEFT JOIN mto_detail det ON det.id = i.detail_id " : "")
                    + (includeChkOrder ? "LEFT JOIN mto_chk_order ch ON ch.id = o.id " : "")
                    + "WHERE "
                    + (!full ? " o.begin BETWEEN ?1 AND ?2 AND " : "")
                    + (!filterTask.equals("none") ? " i.type = '" + filterTask + "' AND " : "")
                    + "o.canceled = 0 AND o.flow_status = 'done' AND "
                    + "o.vehicle_id = " + idVeh + " AND "
                    + "child.area_id = " + area.getId() + " "
                    + (subaId != null ? "and child.id = " + subaId + " " : "")
                    + " ORDER BY begin DESC ");
            q1.setParam(1, fBegin);
            q1.setParam(2, fEnd);
            Object[][] l1 = q1.getRecords(con);
            for (Object[] l11 : l1) {
                HistoryListItem hi = new HistoryListItem();
                ha.getItems().add(hi);
                hi.setType(l11[0].toString());
                hi.setSubArea((String) l11[1]);
                hi.setBegin((Date) l11[2]);
                hi.setAmount(l11[3] != null ? new BigDecimal(MySQLQuery.getAsLong(l11[3])) : null);
                hi.setDescription((String) l11[4]);
                hi.setOrderNum((String) l11[5]);
                hi.setProvider((String) l11[6]);
                hi.setBillNum((String) l11[7]);
                hi.setValue(l11[8] != null ? new BigDecimal(l11[8].toString()) : null);
                hi.setMileageCur(l11[9] != null ? (Integer) l11[9] : 0);
                hi.setChkOrderNum((String) l11[10]);
            }
        }
        return report;
    }

    public void dataFormat(Object[][] data) throws Exception {
        DateFormat sdfPdf = new SimpleDateFormat("dd/MM/yyyy");
        DateFormat sdfConvert = new SimpleDateFormat("yyyy/MM/dd");

        for (Object[] row : data) {
            switch (MySQLQuery.getAsString(row[2])) {
                case "bool":
                    row[1] = row[1].equals("1") ? "SI" : "NO";
                    break;
                case "date":
                    Date date = sdfConvert.parse(row[1].toString().replaceAll("-", "/"));
                    row[1] = sdfPdf.format(date);
                    break;
                default:
                    break;
            }
        }
    }

    public static int createTask(Connection conn, Integer empId, String className) throws Exception {
        MySQLQuery q = new MySQLQuery("INSERT INTO sys_task SET employee_id = ?1, class = ?2, begin = NOW()");
        q.setParam(1, empId);
        q.setParam(2, className);
        return q.executeInsert(conn);
    }

    public static void endTask(Connection conn, int taskId) throws Exception {
        endTask(conn, taskId, null);
    }

    public static void endTask(Connection conn, int taskId, Exception ex) throws Exception {
        MySQLQuery q = new MySQLQuery("UPDATE sys_task SET end = NOW(), ex_class = ?2, ex_msg = ?3 WHERE id = ?1");
        q.setParam(1, taskId);
        q.setParam(2, ex != null ? ex.getClass().getName() : null);
        q.setParam(3, ex != null && ex.getMessage() != null ? ex.getMessage() : null);
        q.executeUpdate(conn);
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
            cb.setFontAndSize(PDFFontsHelper.getRegular(), PDFCellStyle.DEFAULT_FONT_SIZE - 2);
            cb.beginText();
            float y = document.getPageSize().getLeft() + 27;
            cb.moveText(15, y);
            cb.showText(text);
            cb.endText();
            cb.restoreState();
            cb.addTemplate(total, 15 + PDFFontsHelper.getRegular().getWidthPoint(text, PDFCellStyle.DEFAULT_FONT_SIZE - 2), y);
            cb.restoreState();
        } catch (Exception ex) {
        }
    }

    @Override
    public void onCloseDocument(PdfWriter writer, Document document) {
        try {
            float fontSize = PDFCellStyle.DEFAULT_FONT_SIZE - 2;
            total.beginText();
            total.setFontAndSize(PDFFontsHelper.getRegular(), fontSize);
            total.setTextMatrix(0, 0);
            total.showText(String.valueOf(writer.getPageNumber() - 1));
            total.endText();
        } catch (Exception ex) {
            Logger.getLogger(PrintCootranarPNC.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
