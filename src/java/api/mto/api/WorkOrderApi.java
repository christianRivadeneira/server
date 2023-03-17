package api.mto.api;

import api.BaseAPI;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import utilities.MySQLQuery;
import utilities.apiClient.StringResponse;
import utilities.pdf.*;
import web.enterpriseLogo;

@Path("/WorkOrderApi")
public class WorkOrderApi extends BaseAPI {

    public static final String FLOW_PLANNING = "planning";
    public static final String FLOW_CHECKING = "checking";
    public static final String FLOW_APPROVED = "approved";
    public static final String FLOW_CANCELLED = "cancelled";
    public static final String FLOW_DONE = "done";

    private final int CAL_FORMAT_WORK_ORDER = 18;
    private final int DT_FORMAT_WORK_ORDER_ID = 10;
    private final DecimalFormat decimalFormat = new DecimalFormat("###,###.##");
    private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy");
    private PdfContentByte cb;
    private Document document;
    private File fin;
    private HeaderFooter event;
    private PdfWriter writer;
    private OutputStream os = null;
    private boolean locked = false;
    private boolean letPrintOrder;
    private String codeFormat;

    @GET
    @Path("/PDFOrder")
    public Response PDFOrder(@QueryParam("orderId") Integer orderId) {
        try (Connection conn = getConnection()) {

            if (new MySQLQuery("SELECT check_date FROM work_order WHERE id = " + orderId).getAsDate(conn) == null) {
                new MySQLQuery("UPDATE work_order SET check_date = NOW() WHERE id = " + orderId).executeUpdate(conn);
            }

            codeFormat = new MySQLQuery("SELECT f.code FROM cal_format f LEFT JOIN cal_proc p ON f.proc_id = p.id WHERE f.id = " + CAL_FORMAT_WORK_ORDER).getAsString(conn);

            Date serverDate;
            Object[] headRow;
            Object[] vehicleRow;
            Object[][] items;

            MySQLQuery m1 = new MySQLQuery("SELECT "
                    + "w.`create_date`, "//0
                    + "IF(ms.id IS NOT NULL,ms.`name`,prov.`name`), "//1
                    + "IF(ms.id IS NOT NULL,'',prov.nit),  "//2
                    + "IF(ms.id IS NOT NULL,'',prov.represent),  "//3
                    + "IF(ms.id IS NOT NULL,msc.`name`,ct.`name`), "//4
                    + "IF(ms.id IS NOT NULL,'',prov.address), "//5
                    + "veh.plate, "//6
                    + "prov.telephone, "//7
                    + "w.credit, "//8
                    + "w.order_num, "//9
                    + "e.alternative,  "//10
                    + "e.address,  "//11
                    + "e.phones,  "//12
                    + "w.kind, "//13
                    + "w.flow_status, "//14
                    + "prov.mail, "//15
                    + "w.description, "//16
                    + "w.creator_id,  "//17
                    + "(SELECT IF(GROUP_CONCAT(DISTINCT i.type) = 'corr', 'Correctiva','Preventiva')  " //genera el tipo de orden
                    + "FROM work_order  AS wo "
                    + "INNER JOIN item AS i ON i.work_id = wo.id "
                    + "WHERE "
                    + "wo.id = " + orderId + "), "//18
                    + "w.agency_id "//19
                    + "FROM work_order AS w "
                    + "LEFT JOIN prov_provider AS prov ON prov.id = w.provider_id "
                    + "LEFT JOIN mto_store AS ms ON ms.id = w.store_id "
                    + "LEFT JOIN agency AS msa ON msa.id = ms.agency_id "
                    + "LEFT JOIN city AS msc ON msc.id = msa.city_id "
                    + "LEFT JOIN dane_poblado AS ct ON prov.pob_id = ct.id "
                    + "INNER JOIN vehicle AS veh ON w.vehicle_id = veh.id "
                    + "INNER JOIN agency AS a ON a.id = veh.agency_id "
                    + "INNER JOIN enterprise AS e ON e.id = a.enterprise_id "
                    + "WHERE w.id = " + orderId);//1

            /**
             * los redondeos que aparecen aquí deben hacerse iguales que en el
             * exportador a CGUno en FrmExportCGUno.java
             */
            MySQLQuery m2 = new MySQLQuery("SELECT "
                    + "COALESCE(it.description, mt.description, CONCAT(a.`name`, ' - ',t.`name`)), "//0
                    + "it.amount, "//cantidad 1
                    + "@fac:=ROUND(it.`value`), "//valor facturado 2
                    + "@fac / it.amount, "//valor unitario incluye iva 3
                    + "@base:=ROUND(@fac /(1 + (it.`iva` / 100))), "//base facturado ya sin iva 4
                    + "@net:=ROUND(@base /(1 - (it.dto / 100))), "//bruto valor total sin descuentos ni iva, formula cambiada 5 
                    + "@net - @base, "//dto formula cambiada 6 
                    + "@fac - @base, "//iva 7 
                    + "CONCAT(supera.`name`, ' - ',suba.`name`), " //area/rubro 8 
                    + "it.iva " //iva 9
                    + "FROM item AS it "
                    + "INNER JOIN area AS a ON it.area_id = a.id "
                    + "INNER JOIN area AS suba ON it.area_id = suba.id "
                    + "INNER JOIN area AS supera ON suba.area_id = supera.id "
                    + "INNER JOIN task_type AS t ON it.task_type_id = t.id "
                    + "LEFT JOIN maint_task AS mt ON it.maint_task_id = mt.id "
                    + "WHERE it.work_id = " + orderId);//2
            MySQLQuery m3 = new MySQLQuery("SELECT NOW()");//3
            MySQLQuery m4 = new MySQLQuery("SELECT "
                    + "v.id, "
                    + "v.plate, "
                    + "v.model, "
                    + "vt.name AS marca, "
                    + "ft.name AS tipoCombustible, "
                    + "vc.name AS clase, "
                    + "IF(v.cylinder_cap IS NULL,'',v.cylinder_cap) AS cilindraje "
                    + "FROM vehicle AS v   "
                    + "INNER JOIN vehicle_type AS vt ON v.vehicle_type_id = vt.id "
                    + "INNER JOIN vehicle_class AS vc ON vc.id = vt.vehicle_class_id "
                    + "LEFT JOIN fuel_type_vehicle AS ftv ON ftv.vehicle_id = v.id "
                    + "LEFT JOIN fuel_type AS ft ON ft.id = ftv.fuel_type_id "
                    + "WHERE v.id = (SELECT vehicle_id FROM work_order WHERE id = " + orderId + " )");//4

            boolean flow = new MySQLQuery("SELECT c.work_order_flow FROM mto_cfg c").getAsBoolean(conn);
            headRow = m1.getRecord(conn);
            items = m2.getRecords(conn);
            serverDate = m3.getAsDate(conn);
            vehicleRow = m4.getRecord(conn);
            letPrintOrder = headRow[14].equals(FLOW_APPROVED);
            if (flow) {
                if (headRow[14] == null || (!headRow[14].equals(FLOW_APPROVED) && !headRow[14].equals(FLOW_DONE))) {
                    locked = true;
                }
            }

            document = new Document(new Rectangle(8.5f * 70f, 11f * 72f), 50f, 50f, 20f, 20f);
            fin = File.createTempFile("orden", ".pdf");
            writer = PdfWriter.getInstance(document, (os != null ? os : new FileOutputStream(fin)));
            event = new HeaderFooter();
            writer.setBoxSize("art", new Rectangle(36, 54, 559, 788));
            writer.setPageEvent(event);
            document.open();
            cb = writer.getDirectContent();
            PDFCellStyle printed = new PDFCellStyle();
            printed.setAppearance(false, 0, PDFCellStyle.WHITE, PDFCellStyle.WHITE);

            document.add(printed.getParagraph("Impreso: " + dateTimeFormat.format(serverDate).toUpperCase(), Element.ALIGN_RIGHT));

            PDFCellStyle titleStyle = printed.copy();
            titleStyle.setBold(true);
            titleStyle.setPadding(1);

            PDFCellStyle cellStyleRight = new PDFCellStyle();
            cellStyleRight.setBorderColor(PDFCellStyle.GRAY_BORDER);
            cellStyleRight.sethAlignment(Element.ALIGN_RIGHT);
            cellStyleRight.setBold(false);

            PDFCellStyle cellStyleLeft = cellStyleRight.copy();
            cellStyleLeft.setBorderColor(PDFCellStyle.GRAY_BORDER);
            cellStyleLeft.sethAlignment(Element.ALIGN_LEFT);

            PDFCellStyle colTitleStyle = new PDFCellStyle();
            colTitleStyle.setAppearance(true, 5, PDFCellStyle.GRAY_BACKGROUND, PDFCellStyle.GRAY_BORDER);
            colTitleStyle.setBold(true);

            PDFCellStyle colInfoStyle = new PDFCellStyle();
            colInfoStyle.setAppearance(true, 5, PDFCellStyle.WHITE, PDFCellStyle.GRAY_BORDER);
            colInfoStyle.setBold(true);
            colInfoStyle.setBorder(false);
            colInfoStyle.setFontSize(15);
            colInfoStyle.sethAlignment(Element.ALIGN_LEFT);

            PDFCellStyle colTitleStyleRight = new PDFCellStyle();
            colTitleStyleRight.setAppearance(true, 5, PDFCellStyle.GRAY_BACKGROUND, PDFCellStyle.GRAY_BORDER);
            colTitleStyleRight.setBold(false);
            colTitleStyleRight.sethAlignment(Element.ALIGN_RIGHT);

            PDFCellStyle cellStyleCenter = cellStyleRight.copy();
            cellStyleCenter.setBorderColor(PDFCellStyle.GRAY_BORDER);
            cellStyleCenter.sethAlignment(Element.ALIGN_CENTER);

            PDFCellStyle infoTitleStyle = new PDFCellStyle();
            infoTitleStyle.setBold(true);
            infoTitleStyle.setBorder(false);
            infoTitleStyle.sethAlignment(Element.ALIGN_LEFT);

            PDFCellStyle infoTitleStyleDto = new PDFCellStyle();
            infoTitleStyleDto.setBold(true);
            infoTitleStyleDto.setTextColor(Color.GRAY);
            infoTitleStyleDto.setBorder(false);
            infoTitleStyleDto.sethAlignment(Element.ALIGN_LEFT);

            PDFCellStyle infoTitleStyleSoft = infoTitleStyle.copy();
            infoTitleStyleSoft.setFontInfo(false, Color.LIGHT_GRAY, 10);

            PDFCellStyle infoTitleStyleCenter = cellStyleRight.copy();
            infoTitleStyleCenter.setBold(false);
            infoTitleStyleCenter.setBorder(false);
            infoTitleStyleCenter.sethAlignment(Element.ALIGN_CENTER);

            PDFCellStyle infoTitleStyleCenterBorder = cellStyleRight.copy();
            infoTitleStyleCenterBorder.setBold(false);
            infoTitleStyleCenterBorder.setBorders(false, true, false, false);
            infoTitleStyleCenterBorder.sethAlignment(Element.ALIGN_CENTER);

            PDFCellStyle infoTitleStyleCenterBold = cellStyleRight.copy();
            infoTitleStyleCenterBold.setBold(true);
            infoTitleStyleCenterBold.setBorder(false);

            PDFCellStyle infoStyle = infoTitleStyle.copy();
            infoStyle.setBold(false);

            PDFCellStyle linear = infoTitleStyle.copy();
            linear.setBorder(false);
            linear.setBackgroundColor(PDFCellStyle.GRAY_BACKGROUND);

            PdfPTable logoTabTit = new PdfPTable(3);
            logoTabTit.setWidths(new float[]{25, 50, 25});
            logoTabTit.setWidthPercentage(100);

            PdfPCell imgLogo;
            PdfPCell imgLogo2;

            try {
                File file = enterpriseLogo.getEnterpriseLogo("4", conn);
                byte[] readAllBytes = Files.readAllBytes(file.toPath());
                Image img = Image.getInstance(readAllBytes);
                img.setAlignment(Element.ALIGN_CENTER);
                img.scaleToFit(60, 60);
                imgLogo = new PdfPCell(img);

                Image img2 = Image.getInstance(readAllBytes);
                img2.setAlignment(Element.ALIGN_CENTER);
                img2.scaleToFit(60, 60);
                imgLogo2 = new PdfPCell(img2);
            } catch (Exception ex) {
                imgLogo = new PdfPCell();
                imgLogo2 = new PdfPCell();
            }

            imgLogo.setBorder(0);
            imgLogo2.setBorder(0);
            logoTabTit.addCell(imgLogo);
            logoTabTit.addCell(infoTitleStyleCenter.getCell("COMBUSTIBLES LIQUIDOS DE COLOMBIA S.A ESP \n ORDEN DE TRABAJO"));
            logoTabTit.addCell(imgLogo2);
            logoTabTit.setSpacingBefore(5);
            document.add(logoTabTit);

            PdfPCell imgCell;

            try {
                Image img;
                if (MySQLQuery.getAsBoolean(headRow[10])) {
                    File file = enterpriseLogo.getEnterpriseLogo("4", conn);
                    byte[] readAllBytes = Files.readAllBytes(file.toPath());
                    img = Image.getInstance(readAllBytes);
                    img.setAlignment(Element.ALIGN_CENTER);
                    img.scaleToFit(60, 60);
                } else {
                    img = Image.getInstance(this.getClass().getResource("/icons/tipial/camera2.png"));
                    img.scalePercent(10);
                }
                img.setAlignment(Element.ALIGN_CENTER);
                img.scaleToFit(60, 60);
                imgCell = new PdfPCell(img);
            } catch (Exception ex) {
                imgCell = new PdfPCell();
            }

            imgCell.setBorder(0);
            imgCell.setHorizontalAlignment(Element.ALIGN_CENTER);

            PdfPTable logoTab = new PdfPTable(3);
            logoTab.setWidths(new float[]{50, 25, 25});
            logoTab.setWidthPercentage(100);

            logoTab.addCell(imgCell);
            infoTitleStyleCenterBold.sethAlignment(Element.ALIGN_RIGHT);
            logoTab.addCell(infoTitleStyleCenterBold.getCell("ORDEN DE TRABAJO"));
            infoTitleStyleCenterBold.sethAlignment(Element.ALIGN_CENTER);
            logoTab.addCell(colInfoStyle.getCell("No.  " + (headRow[9] != null ? headRow[9].toString() : " ")));
            logoTab.setSpacingAfter(10);
            document.add(logoTab);

            PdfPTable tabWorkOrder = new PdfPTable(4);
            tabWorkOrder.setWidthPercentage(100);
            tabWorkOrder.addCell(linear.getCell("", 6, 1));//linea
            tabWorkOrder.addCell(infoTitleStyleDto.getCell("INFORMACIÓN ORDEN TRABAJO", 4, 1));
            tabWorkOrder.addCell(infoTitleStyle.getCell("Fecha de Solicitud"));
            tabWorkOrder.addCell(infoStyle.getCell(dateTimeFormat.format(headRow[0])));
            tabWorkOrder.addCell(infoTitleStyle.getCell("Área Solicitante"));
            tabWorkOrder.addCell(infoStyle.getCell("Mantenimiento"));
            tabWorkOrder.addCell(infoTitleStyle.getCell("Justificacion"));
            tabWorkOrder.addCell(infoStyle.getCell((headRow[16] != null ? headRow[16].toString() : "")));
            tabWorkOrder.addCell(infoTitleStyle.getCell("Tipo"));
            tabWorkOrder.addCell(infoStyle.getCell((headRow[18] != null ? headRow[18].toString() : "")));
            tabWorkOrder.addCell(infoStyle.getCell("", 4, 1));//espacio
            tabWorkOrder.addCell(linear.getCell("", 4, 1));//linea
            tabWorkOrder.setSpacingAfter(10);
            document.add(tabWorkOrder);

            PdfPTable tabVehicle = new PdfPTable(6);
            tabVehicle.setWidthPercentage(100);
            tabVehicle.addCell(linear.getCell("", 6, 1));//linea
            tabVehicle.addCell(infoTitleStyleDto.getCell("INFORMACIÓN VEHICULO", 6, 1));
            tabVehicle.addCell(infoTitleStyle.getCell("Placa"));
            tabVehicle.addCell(infoStyle.getCell((vehicleRow[1] != null ? vehicleRow[1].toString() : " ")));
            tabVehicle.addCell(infoTitleStyle.getCell("Modelo"));
            tabVehicle.addCell(infoStyle.getCell((vehicleRow[2] != null ? vehicleRow[2].toString() : " ")));
            tabVehicle.addCell(infoTitleStyle.getCell("Marca"));
            tabVehicle.addCell(infoStyle.getCell((vehicleRow[3] != null ? vehicleRow[3].toString() : " ")));
            tabVehicle.addCell(infoTitleStyle.getCell("Tipo de combustible"));
            tabVehicle.addCell(infoStyle.getCell((vehicleRow[4] != null ? vehicleRow[4].toString() : " ")));
            tabVehicle.addCell(infoTitleStyle.getCell("Clase"));
            tabVehicle.addCell(infoStyle.getCell((vehicleRow[5] != null ? vehicleRow[5].toString() : " ")));
            tabVehicle.addCell(infoTitleStyle.getCell("Cilindraje"));
            tabVehicle.addCell(infoStyle.getCell((vehicleRow[6] != null ? vehicleRow[6].toString() : " ")));
            tabVehicle.addCell(infoTitleStyle.getCell("Ubicación Vehículo"));
            tabVehicle.addCell(infoStyle.getCell(getLocationVehicle(orderId, conn), 5, 1));

            tabVehicle.addCell(infoStyle.getCell("", 6, 1));//espacio
            tabVehicle.addCell(linear.getCell("", 6, 1));//linea
            tabVehicle.setSpacingAfter(10);
            document.add(tabVehicle);

            PdfPTable tabProvider = new PdfPTable(6);
            tabProvider.setWidthPercentage(100);
            tabProvider.addCell(linear.getCell("", 6, 1));//linea
            tabProvider.addCell(infoTitleStyleDto.getCell("INFORMACIÓN PROVEEDOR", 6, 1));
            tabProvider.addCell(infoTitleStyle.getCell("Razón Social"));
            tabProvider.addCell(infoStyle.getCell(headRow[1].toString()));
            tabProvider.addCell(infoTitleStyle.getCell("CC./NIT."));
            tabProvider.addCell(infoStyle.getCell((headRow[2] != null ? headRow[2].toString() : " ")));
            tabProvider.addCell(infoTitleStyle.getCell("Dirección"));
            tabProvider.addCell(infoStyle.getCell((headRow[5] != null ? headRow[5].toString() : " ")));
            tabProvider.addCell(infoTitleStyle.getCell("Contacto"));
            tabProvider.addCell(infoStyle.getCell((headRow[3] != null ? headRow[3].toString() : " ")));
            tabProvider.addCell(infoTitleStyle.getCell("Teléfono"));
            tabProvider.addCell(infoStyle.getCell((headRow[7] != null ? headRow[7].toString() : " ")));
            tabProvider.addCell(infoTitleStyle.getCell("Ciudad"));
            tabProvider.addCell(infoStyle.getCell((headRow[4] != null ? headRow[4].toString() : " ")));
            tabProvider.addCell(infoTitleStyle.getCell("Forma de Pago"));
            if (headRow[8] != null) {
                tabProvider.addCell(infoStyle.getCell((MySQLQuery.getAsInteger(headRow[8]) == 0 ? "Contado" : "Crédito")));
            } else {
                tabProvider.addCell(infoStyle.getCell("No establecido"));
            }

            tabProvider.addCell(infoTitleStyle.getCell("Email"));
            tabProvider.addCell(infoStyle.getCell((headRow[15] != null ? headRow[15].toString() : ""), 3, 1));

            tabProvider.addCell(infoStyle.getCell("", 6, 1));//espacio
            tabProvider.addCell(linear.getCell("", 6, 1));//linea
            tabProvider.setSpacingAfter(10);

            document.add(tabProvider);

            PdfPTable tab = new PdfPTable(8);

            tab.setWidthPercentage(100);
            tab.addCell(cellStyleRight.getCell("", 6, 1));
            tab.addCell(colTitleStyle.getCell("Cumple a Satisfacción", 2, 1));
            tab.addCell(colTitleStyle.getCell("Área / Rubro"));
            tab.addCell(colTitleStyle.getCell("Descripción"));
            tab.addCell(colTitleStyle.getCell("Cantidad"));
            tab.addCell(colTitleStyle.getCell("Valor Unitario"));
            tab.addCell(colTitleStyle.getCell("Valor Iva"));
            tab.addCell(colTitleStyle.getCell("Valor Total"));
            tab.addCell(colTitleStyle.getCell("Si"));
            tab.addCell(colTitleStyle.getCell("No"));

            tab.setWidths(new int[]{20, 28, 9, 10, 10, 10, 5, 5});
            BigDecimal net = BigDecimal.ZERO, dto = BigDecimal.ZERO, iva = BigDecimal.ZERO, sub = BigDecimal.ZERO, total;
            for (Object[] item : items) {
                tab.addCell(cellStyleLeft.getCell(item[8] != null ? MySQLQuery.getAsString(item[8]) : " "));
                tab.addCell(cellStyleLeft.getCell(item[0] != null ? MySQLQuery.getAsString(item[0]) : " "));
                tab.addCell(cellStyleRight.getCell(item[1] + ""));
                tab.addCell(cellStyleRight.getCell(decimalFormat.format(MySQLQuery.getAsBigDecimal(item[4], true).floatValue() / MySQLQuery.getAsInteger(item[1]))));
                tab.addCell(cellStyleRight.getCell(decimalFormat.format(MySQLQuery.getAsBigDecimal(item[7], true))));
                tab.addCell(colTitleStyleRight.getCell(decimalFormat.format((MySQLQuery.getAsDouble(MySQLQuery.getAsBigDecimal(item[3], true)) * MySQLQuery.getAsInteger(item[1])))));
                net = net.add(MySQLQuery.getAsBigDecimal(item[5], true));
                dto = dto.add(MySQLQuery.getAsBigDecimal(item[6], true));
                iva = iva.add(MySQLQuery.getAsBigDecimal(item[7], true));
                sub = sub.add(MySQLQuery.getAsBigDecimal(item[4], true));
                tab.addCell(cellStyleRight.getCell(""));
                tab.addCell(cellStyleRight.getCell(""));
            }
            total = net.subtract(dto).add(iva);

            document.add(tab);

            PdfPTable calcTab = new PdfPTable(5);
            calcTab.setWidthPercentage(100);
            calcTab.setWidths(new int[]{10, 10, 10, 10, 10});
            calcTab.setSpacingBefore(10);
            calcTab.addCell(colTitleStyle.getCell("Total Bruto\n" + decimalFormat.format(net)));
            calcTab.addCell(colTitleStyle.getCell("Descuento\n" + decimalFormat.format(dto)));
            calcTab.addCell(colTitleStyle.getCell("Subtotal\n" + decimalFormat.format(sub)));
            calcTab.addCell(colTitleStyle.getCell("Valor IVA\n" + decimalFormat.format(iva)));
            calcTab.addCell(colTitleStyle.getCell("Total\n" + decimalFormat.format(total)));
            calcTab.addCell(cellStyleLeft.getCell("Observaciones: ", 5, 1));
            calcTab.addCell(cellStyleLeft.getCell("\n", 5, 1));
            calcTab.setSpacingAfter(10);
            document.add(calcTab);

            PdfPTable tabCondiciones = new PdfPTable(4);
            tabCondiciones.setWidthPercentage(100);
            tabCondiciones.addCell(colTitleStyle.getCell("Condiciones y observaciones", 4, 1));
            tabCondiciones.addCell(colTitleStyle.getCell("Plazo de entrega "));
            tabCondiciones.addCell(cellStyleLeft.getCell(getPlazo(orderId, conn)));
            tabCondiciones.addCell(colTitleStyle.getCell("Días de garantía "));
            tabCondiciones.addCell(cellStyleLeft.getCell(getGarantia(orderId, conn)));
            document.add(tabCondiciones);

            PdfPTable tabP = new PdfPTable(5);
            tabP.setWidths(new float[]{10, 35, 10, 35, 10});
            tabP.setWidthPercentage(100);

            if (items.length > 12) {
                document.newPage();
            }
            infoTitleStyle.setvAlignment(Element.ALIGN_BOTTOM);

            tabP.addCell(infoTitleStyle.getCell(""));
            tabP.addCell(infoTitleStyle.getCell("Elaboró"));
            tabP.addCell(infoTitleStyle.getCell(""));
            tabP.addCell(infoTitleStyle.getCell("Aprobó"));
            tabP.addCell(infoTitleStyle.getCell(""));

            for (int i = 0; i < 5; i++) {
                tabP.addCell(infoTitleStyle.getCell("", 5, 1));
            }

            tabP.addCell(infoTitleStyle.getCell(""));
            tabP.addCell(infoTitleStyle.getCell("___________________________________"));
            tabP.addCell(infoTitleStyle.getCell(""));
            tabP.addCell(infoTitleStyle.getCell("___________________________________"));
            tabP.addCell(infoTitleStyle.getCell(""));

            for (int i = 0; i < 2; i++) {
                tabP.addCell(infoTitleStyle.getCell("", 5, 1));
            }

            tabP.addCell(infoTitleStyle.getCell(""));
            tabP.addCell(infoTitleStyle.getCell("Entrego"));
            tabP.addCell(infoTitleStyle.getCell(""));
            tabP.addCell(infoTitleStyle.getCell("Recibió"));
            tabP.addCell(infoTitleStyle.getCell(""));

            for (int i = 0; i < 5; i++) {
                tabP.addCell(infoTitleStyle.getCell("", 5, 1));
            }

            tabP.addCell(infoTitleStyle.getCell(""));
            tabP.addCell(infoTitleStyle.getCell("___________________________________"));
            tabP.addCell(infoTitleStyle.getCell(""));
            tabP.addCell(infoTitleStyle.getCell("___________________________________"));
            tabP.addCell(infoTitleStyle.getCell(""));

            tabP.setSpacingAfter(10);
            tabP.setSpacingBefore(10);
            document.add(tabP);

            PdfPTable tabNotes = new PdfPTable(1);
            tabNotes.setWidthPercentage(100);
            if (items.length > 3 && items.length <= 8) {
                document.newPage();
                tabNotes.addCell(infoTitleStyle.getCell(""));
                tabNotes.addCell(infoTitleStyle.getCell(""));
                tabNotes.addCell(infoTitleStyle.getCell(""));
            }

            String[] notes = new String[]{"Copia de la orden de compra.", "Remisión o prueba de entrega del producto o servicio prestado. (Con nombre claro y completo e identificación de quien recibe)", "Para los casos que se solicite certificación de calidad del producto / lote.", "Radicar facturas con copia de la orden de compra en Bogotá - Puente Aranda Carrera 50 No. 18 A - 75 Piso 2.", "Fecha máxima de radicación de facturas día 23 de cada mes o día hábil anterior, cuando es domingo o festivo."};

            infoStyle.setFontSize(6);
            infoTitleStyle.setFontSize(6);
            tabNotes.addCell(infoTitleStyle.getCell("Nota. Para la radiación de la factura adjuntar:"));

            for (int i = 0; i < notes.length; i++) {
                tabNotes.addCell(infoStyle.getCell((i + 1) + ".  " + notes[i]));
            }

            tabNotes.addCell(infoTitleStyle.getCell("Horario radicación Lunes - Viernes 8 am a 12 pm y 2 pm a 4:30 pm Sábados 8:30 am a 11:30 am"));
            tabNotes.addCell(infoTitleStyle.getCell("NO SE RECIBIRÁ FACTURA QUE NO TRAIGA ADJUNTO"));
            document.add(tabNotes);
            document.close();

            return createResponse(fin, fin.getName());
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getPathOrder")
    public Response getPathOrder(@Context HttpServletRequest request) {
        try (Connection conn = getConnection()) {
            String url = request.getScheme() + "://" + request.getRemoteAddr() + ":"
                    + request.getLocalPort() + request.getContextPath() + "/api/WorkOrderApi/PDFOrder";

            StringResponse rta = new StringResponse(url);
            return createResponse(rta);
        } catch (Exception ex) {
            return createResponse(ex);
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
        public void onStartPage(PdfWriter writer, Document dcmnt) {
            String text = getWaterMark();
            if (text != null && !text.isEmpty() && dcmnt.isOpen()) {
                try {
                    if (document.isOpen()) {
                        PDFCellStyle s = new PDFCellStyle();
                        s.setFontInfo(true, new Color(210, 210, 210), 42);
                        PdfContentByte canvas = writer.getDirectContent();
                        PdfTemplate textTemplate = canvas.createTemplate(400, 300);
                        ColumnText columnText = new ColumnText(textTemplate);
                        columnText.setSimpleColumn(0, 0, 400, 300);
                        Paragraph par = s.getParagraph(text, Element.ALIGN_CENTER);
                        columnText.addElement(par);
                        columnText.go();
                        Image textImg = Image.getInstance(textTemplate);
                        textImg.setInterpolation(true);
                        textImg.scaleAbsolute(400, 300);
                        textImg.setRotationDegrees((float) 45);
                        textImg.setAbsolutePosition(100, 120);
                        document.add(textImg);
                    }
                } catch (Exception ex) {
                }
            }
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
                float y = document.getPageSize().getHeight() - 25;
                if (codeFormat != null) {
                    cb.moveText(460, 15);
                    cb.showText(codeFormat);
                } else {
                    cb.moveText(15, y);
                    cb.showText(text);
                }
                cb.endText();
                cb.restoreState();
                if (codeFormat == null) {
                    cb.addTemplate(total, 15 + PDFFontsHelper.getRegular().getWidthPoint(text, 9), y);
                }
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
            }
        }
    }

    public String getWaterMark() {
        try {
            return (locked ? "ORDEN EN TRÁMITE, NO VÁLIDA PARA EJECUTAR" : (!letPrintOrder ? "DOCUMENTO INFORMATIVO, NO VÁLIDO PARA EJECUTAR" : null));
        } catch (Exception ex) {
            return null;
        }
    }

    private String getLocationVehicle(Integer orderId, Connection conn) throws Exception {

        String ubicacion = new MySQLQuery("SELECT "
                + "ct.name "
                + "FROM work_order AS w "
                + "INNER JOIN vehicle AS vh ON vh.id = w.vehicle_id "
                + "INNER JOIN agency AS ag ON vh.agency_id = ag.id "
                + "INNER JOIN city AS ct ON ag.city_id = ct.id "
                + "WHERE w.id = " + orderId).getAsString(conn);

        return (ubicacion != null ? ubicacion : "");
    }

    private String getPlazo(Integer orderId, Connection conn) throws Exception {
        String plazo = new MySQLQuery("SELECT "
                + " sfv.`data` "
                + "FROM sys_frm_field AS f "
                + "INNER JOIN sys_frm_value as sfv ON sfv.field_id = f.id "
                + "WHERE f.type_id = " + DT_FORMAT_WORK_ORDER_ID + " AND "
                + "f.id = 38 AND "
                + "sfv.owner_id = " + orderId).getAsString(conn);

        return (plazo != null ? plazo : "");
    }

    private String getGarantia(Integer orderId, Connection conn) throws Exception {
        String garantia = new MySQLQuery("SELECT "
                + " sfv.`data` "
                + "FROM sys_frm_field AS f "
                + "INNER JOIN sys_frm_value as sfv ON sfv.field_id = f.id "
                + "WHERE f.type_id = " + DT_FORMAT_WORK_ORDER_ID + " AND "
                + "f.id = 37 AND "
                + "sfv.owner_id = " + orderId).getAsString(conn);
        return (garantia != null ? garantia : "");
    }

}
