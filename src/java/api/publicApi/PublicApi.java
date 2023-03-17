package api.publicApi;

import api.BaseAPI;
import api.Params;
import api.ord.api.OrfeoApi;
import api.ord.model.OrdActivityPqr;
import api.ord.model.OrdCfg;
import api.ord.model.OrdPqrCyl;
import api.ord.model.OrdPqrOther;
import api.ord.model.OrdPqrTank;
import api.ord.model.OrdRepairs;
import api.ord.orfeo.OrfeoResponse;
import api.ord.orfeo.PqrResponseFromOrfeo;
import api.prov.dto.ProvItem;
import api.sys.model.Employee;
import api.sys.model.SysMailProcess;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import utilities.MySQLQuery;
import utilities.SysTask;
import utilities.pdf.*;
import web.quality.SendMail;
import static web.quality.SendMail.getHtmlMsg;
import web.quality.SysMailUtil;

@Path("/public")
public class PublicApi extends BaseAPI {

    @GET
    @Path("/ProvPdfOrder")
    public Response PDFOrder(@QueryParam("extId") Integer extId) {
        try (Connection conn = getConnection()) {

            if (new MySQLQuery("SELECT received_date FROM prov_request WHERE id = " + extId).getAsDate(conn) == null) {
                new MySQLQuery("UPDATE prov_request SET received_date = NOW() WHERE id = " + extId).executeUpdate(conn);
            }

            DecimalFormat df = new DecimalFormat("###,###.##");
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy");
            File fin;
            Date serverDate = new MySQLQuery("SELECT NOW()").getAsDate(conn);
            Object[] headRow = new MySQLQuery("SELECT "
                    + "purch_serial, " //0
                    + "r.`beg_date`, " //1
                    + "CONCAT(re.first_name, ' ', re.last_name), " //2
                    + "pa.name, " //3
                    + "'', " //4
                    + "pr.name, " //5
                    + "pr.nit, " //6
                    + "pr.represent, " //7
                    + "pr.address, " //8
                    + "c.name, " //9
                    + "pr.telephone, "//10
                    + "CONCAT(acc.name,' - ',acc.acc_code), "//11
                    + "pm.name,"//12
                    + "coin.name,"//13
                    + "req.rev1_date, "//14
                    + "req.req_serial, "//15
                    + "req.notes, " //16
                    + "term.level, " //17
                    + "term.exp_days, "//18
                    + "concat(rev1.first_name, ' ', rev1.last_name), " //19
                    + "concat(rev2.first_name, ' ', rev2.last_name), " //20
                    + "concat(rev3.first_name, ' ', rev3.last_name), " //21
                    + "a1.code, "//22
                    + "a1.description, "//23
                    + "a2.code, "//24
                    + "a2.description, "//25
                    + "a3.code, "//26
                    + "a3.description "//27
                    + "FROM prov_request req "
                    + "INNER JOIN sys_flow_req r ON req.sys_req_id = r.id "
                    + "INNER JOIN employee re ON re.id = r.employee_id "
                    + "INNER JOIN per_area pa ON pa.id = r.per_area_id "
                    + "LEFT JOIN prov_provider pr ON pr.id = req.provider_id "
                    + "LEFT JOIN dane_poblado c ON pr.pob_id = c.id "
                    + "LEFT JOIN prov_pay_method AS pm ON req.pay_method_id = pm.id "
                    + "LEFT JOIN prov_coin AS coin ON req.coin_id = coin.id "
                    + "LEFT JOIN prov_art_term AS term ON req.term_id = term.id "
                    + "LEFT JOIN employee AS rev1 ON req.rev_1 = rev1.id "
                    + "LEFT JOIN employee AS rev2 ON req.rev_2 = rev2.id "
                    + "LEFT JOIN employee AS rev3 ON req.rev_3 = rev3.id "
                    + "LEFT JOIN prov_ciiu_activity a1 ON a1.id = pr.ciiu1_id "
                    + "LEFT JOIN prov_ciiu_activity a2 ON a2.id = pr.ciiu2_id "
                    + "LEFT JOIN prov_ciiu_activity a3 ON a3.id = pr.ciiu3_id "
                    + "LEFT JOIN prov_article_type art ON art.id= req.art_type_id "
                    + "LEFT JOIN prov_acc_account acc ON art.account_id=acc.id  "
                    + "WHERE req.id = " + extId).getRecords(conn)[0];
            ProvItem[] items = ProvItem.getFromData(new MySQLQuery(ProvItem.getQuery(extId)).getRecords(conn));
            Object[] entRow = new MySQLQuery("SELECT e.name, e.nit FROM enterprise e WHERE e.alternative = 0 LIMIT 1").getRecord(conn);

            Document document = new Document(new Rectangle(8.5f * 72f, 13f * 72f), 36f, 36f, 36f, 36f);
            String serial = headRow[0] != null ? headRow[0].toString() : " ";
            fin = File.createTempFile("orden_" + serial + "_", ".pdf");
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fin));
            writer.setPageEvent((ClassHeaders.returnPageEvent(conn, 18, serial, null, false)));
            writer.setBoxSize("art", new Rectangle(36, 54, 559, 788));

            PDFCellStyle red = new PDFCellStyle();
            red.setAppearance(true, 5, Color.WHITE, PDFCellStyle.GRAY_BORDER);
            red.setTextColor(Color.red);
            red.sethAlignment(Element.ALIGN_LEFT);

            PDFCellStyle printed = new PDFCellStyle();
            printed.setAppearance(false, 0, PDFCellStyle.WHITE, PDFCellStyle.WHITE);

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
            colInfoStyle.setFontSize(15);
            colInfoStyle.sethAlignment(Element.ALIGN_LEFT);

            PDFCellStyle colTitleStyleRight = new PDFCellStyle();
            colTitleStyleRight.setAppearance(true, 5, PDFCellStyle.GRAY_BACKGROUND, PDFCellStyle.GRAY_BORDER);
            colTitleStyleRight.setBold(false);
            colTitleStyleRight.sethAlignment(Element.ALIGN_RIGHT);

            PDFCellStyle cellStyleCenter = cellStyleRight.copy();
            cellStyleCenter.setBorderColor(PDFCellStyle.GRAY_BORDER);
            cellStyleCenter.sethAlignment(Element.ALIGN_CENTER);

            PDFCellStyle nameStyle = cellStyleCenter.copy();
            nameStyle.setPaddings(35, 5, 5, 5);

            PDFCellStyle infoTitleStyle = new PDFCellStyle();
            infoTitleStyle.setBold(true);
            infoTitleStyle.setBorder(false);
            infoTitleStyle.sethAlignment(Element.ALIGN_LEFT);

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
            infoTitleStyleCenterBold.sethAlignment(Element.ALIGN_CENTER);

            PDFCellStyle infoStyle = infoTitleStyle.copy();
            infoStyle.setBold(false);

            PDFCellStyle columTitleStyle = new PDFCellStyle();
            columTitleStyle.setAppearance(true, 5, PDFCellStyle.WHITE, PDFCellStyle.WHITE);
            columTitleStyle.setBold(true);
            columTitleStyle.setFontSize(12);
            columTitleStyle.sethAlignment(Element.ALIGN_CENTER);
            columTitleStyle.setPaddings(0, 0, 10, 10);

            document.open();
            document.add(printed.getParagraph("Impreso: " + dateTimeFormat.format(serverDate).toUpperCase(), Element.ALIGN_RIGHT));
            document.add(printed.getParagraph("Fecha: " + (headRow[14] != null ? dateTimeFormat.format(headRow[14]) : "Pendiente"), Element.ALIGN_RIGHT));

            //---------------------------------
            PdfPTable infoTab = new PdfPTable(4);
            int[] widths = new int[4];
            widths[0] = 5;
            widths[1] = 12;
            widths[2] = 5;
            widths[3] = 12;
            infoTab.setWidthPercentage(100);
            infoTab.setWidths(widths);

            infoTab.addCell(infoTitleStyle.getCell("Proveedor:"));
            infoTab.addCell(infoStyle.getCell(headRow[5]));

            infoTab.addCell(infoTitleStyle.getCell("Comprador:"));
            infoTab.addCell(infoStyle.getCell(entRow[0]));

            infoTab.addCell(infoTitleStyle.getCell("Nit:"));
            infoTab.addCell(infoStyle.getCell(headRow[6]));

            infoTab.addCell(infoTitleStyle.getCell("Nit:"));
            infoTab.addCell(infoStyle.getCell(entRow[1]));

            infoTab.addCell(infoTitleStyle.getCell("Contacto:"));
            infoTab.addCell(infoStyle.getCell(headRow[7]));

            infoTab.addCell(infoTitleStyle.getCell("Cuenta:"));
            infoTab.addCell(infoStyle.getCell(headRow[11]));

            infoTab.addCell(infoTitleStyle.getCell("Dirección:"));
            infoTab.addCell(infoStyle.getCell(headRow[8]));

            infoTab.addCell(infoTitleStyle.getCell("Forma de Pago:"));
            infoTab.addCell(infoStyle.getCell(headRow[12]));

            infoTab.addCell(infoTitleStyle.getCell("Ciudad:"));
            infoTab.addCell(infoStyle.getCell(headRow[9]));

            infoTab.addCell(infoTitleStyle.getCell("Moneda:"));
            infoTab.addCell(infoStyle.getCell((headRow[13] != null ? headRow[13] : "Peso Colombiano (COP)")));

            infoTab.addCell(infoTitleStyle.getCell("Teléfono:"));
            infoTab.addCell(infoStyle.getCell(headRow[10]));

            infoTab.addCell(infoTitleStyle.getCell(""));
            infoTab.addCell(infoStyle.getCell(headRow[4]));

            if (headRow[22] != null || headRow[24] != null || headRow[26] != null) {
                infoTab.addCell(infoTitleStyle.getCell("Actividad:"));
                infoTab.addCell(infoStyle.getCell((headRow[22] != null ? headRow[22] + (headRow[24] != null ? ", " : "") : "") + (headRow[24] != null ? headRow[24] + (headRow[26] != null ? ", " : "") : "") + (headRow[26] != null ? headRow[26].toString() : "")));

                infoTab.addCell(infoTitleStyle.getCell(""));
                infoTab.addCell(infoStyle.getCell(""));
            }

            infoTab.setSpacingAfter(20);
            document.add(infoTab);

            int[] withs = new int[7];
            withs[0] = 50;//PRODUCTO Y/O SERVICIO SOLICITADO
            withs[1] = 12;//CANTIDAD
            withs[2] = 10;//UND MEDIDA
            withs[3] = 15;//PRECIO UNIT.
            withs[4] = 8;//IVA
            withs[5] = 8;//desc
            withs[6] = 15;//SUBTOTAL

            PdfPTable tblContent = new PdfPTable(7);
            tblContent.setWidths(withs);
            tblContent.setWidthPercentage(100);

            tblContent.addCell(colTitleStyle.getCell("Descripción Detallada Producto y/o Servicio"));
            tblContent.addCell(colTitleStyle.getCell("Cantidad"));
            tblContent.addCell(colTitleStyle.getCell("Und. Medida"));
            tblContent.addCell(colTitleStyle.getCell("Precio Unitario."));
            tblContent.addCell(colTitleStyle.getCell("IVA %"));
            tblContent.addCell(colTitleStyle.getCell("Desc % "));
            tblContent.addCell(colTitleStyle.getCell("Subtotal"));

            BigDecimal net = BigDecimal.ZERO, dto = BigDecimal.ZERO, iva = BigDecimal.ZERO, sub = BigDecimal.ZERO, total;
            if (items != null && items.length > 0) {
                for (ProvItem item : items) {
                    tblContent.addCell(cellStyleLeft.getCell(item.name + " " + (item.notes != null ? item.notes : "")));
                    tblContent.addCell(cellStyleCenter.getCell(item.amount + ""));
                    tblContent.addCell(cellStyleCenter.getCell(item.provU + ""));
                    tblContent.addCell(cellStyleCenter.getCell(df.format(item.vlunit) + ""));
                    tblContent.addCell(cellStyleCenter.getCell(item.ivaRate + ""));
                    tblContent.addCell(cellStyleCenter.getCell(item.dtoRate + ""));
                    tblContent.addCell(cellStyleCenter.getCell(df.format(item.vltotal) + ""));

                    net = net.add(item.net);
                    dto = dto.add(item.dto);
                    iva = iva.add(item.iva);
                    sub = sub.add(item.base);
                }
            }
            total = net.subtract(dto).add(iva);
            document.add(tblContent);

            String obs = (headRow[16] != null ? headRow[16] + "" : "");
            if (headRow[22] != null) {
                obs += "\n\n" + headRow[22] + " " + headRow[23];
            }
            if (headRow[24] != null) {
                obs += "\n" + headRow[24] + " " + headRow[25];
            }
            if (headRow[26] != null) {
                obs += "\n" + headRow[26] + " " + headRow[27];
            }

            PdfPTable calcTab = new PdfPTable(5);
            calcTab.setWidthPercentage(100);
            calcTab.setWidths(new int[]{10, 10, 10, 10, 10});
            calcTab.setSpacingBefore(20);
            calcTab.addCell(colTitleStyle.getCell("Total Bruto\n" + df.format(net)));
            calcTab.addCell(colTitleStyle.getCell("Descuento\n" + df.format(dto)));
            calcTab.addCell(colTitleStyle.getCell("Subtotal\n" + df.format(sub)));
            calcTab.addCell(colTitleStyle.getCell("Valor IVA\n" + df.format(iva)));
            calcTab.addCell(colTitleStyle.getCell("Total\n" + df.format(total)));
            calcTab.addCell(cellStyleLeft.getCell("Observaciones: " + obs, 5, 1));
            calcTab.addCell(colTitleStyle.getCell("Área Solicitante:", 2, 1));
            calcTab.addCell(cellStyleLeft.getCell(headRow[3].toString(), 3, 1));

            calcTab.addCell(colTitleStyle.getCell("Solicitud:", 2, 1));
            calcTab.addCell(cellStyleLeft.getCell(dateTimeFormat.format(headRow[1]), 1, 1));

            calcTab.addCell(colTitleStyle.getCell("Nivel de Compra:", 1, 1));
            calcTab.addCell(cellStyleLeft.getCell(headRow[17].toString(), 1, 1));

            calcTab.addCell(colTitleStyle.getCell("Número de Solicitud:", 2, 1));
            calcTab.addCell(cellStyleLeft.getCell(headRow[15].toString(), 1, 1));

            calcTab.addCell(colTitleStyle.getCell("Entrega:", 1, 1));
            calcTab.addCell(cellStyleLeft.getCell(dateTimeFormat.format(add(MySQLQuery.getAsDate(headRow[1]), MySQLQuery.getAsInteger(headRow[18]))), 1, 1));

            document.add(calcTab);

            PdfPTable signTab = new PdfPTable(3);
            signTab.setWidthPercentage(100);
            signTab.setWidths(new int[]{10, 10, 10});
            signTab.addCell(red.getCell("Estimado Proveedor:\nPor favor tener en cuenta que la recepción de mercancía se realiza unicamente en Almacén de la planta Daza en Montagas S.A E.S.P en los siguientes horarios: De lunes a viernes de 9:00 a.m. a 11:00 a.m. y de 2:00 p.m. a 4:30 p.m. Y los días sábados de 9 am a 11 am.\nANEXAR ORDEN DE COMPRA A SU FACTURA", 3, 1));

            signTab.addCell(nameStyle.getCell(headRow[19]));
            signTab.addCell(nameStyle.getCell(headRow[20]));
            signTab.addCell(nameStyle.getCell(headRow[21]));

            signTab.addCell(colTitleStyle.getCell("Elaborado"));
            signTab.addCell(colTitleStyle.getCell("Verificado"));
            signTab.addCell(colTitleStyle.getCell("Aprobado"));

            document.add(signTab);
            document.close();

            return createResponse(fin, "Orden.pdf");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    public static Date add(Date d, int expDays) {
        int days = 0;
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(d);
        while (expDays - 1 != days) {
            gc.add(GregorianCalendar.DAY_OF_MONTH, 1);
            int day = gc.get(GregorianCalendar.DAY_OF_WEEK);
            if (day != GregorianCalendar.SATURDAY && day != GregorianCalendar.SUNDAY) {
                days++;
            }
        }
        return gc.getTime();
    }

    @POST
    @Path("/pqrResponse")
    public Response pqrResponse(PqrResponseFromOrfeo orfeoResponse, @Context HttpServletRequest request) {

        String sigmaPassword = request.getHeader("X-Sigma-Password");
        try (Connection con = getConnection()) {
            if (MySQLQuery.isEmpty(sigmaPassword) || !sigmaPassword.equals("9YxsO5Eo0tH04U8b")) {
                throw new Exception("La peticion no contiene la constraseña de seguridad");
            }
            SysTask t = new SysTask(OrfeoApi.class, "Orfeo", 1, con);
            try {
                OrdPqrCyl pqrCyl = new OrdPqrCyl().select(new Params("rad_orfeo", orfeoResponse.radNumber), con);
                OrdPqrTank pqrTank = new OrdPqrTank().select(new Params("rad_orfeo", orfeoResponse.radNumber), con);
                OrdPqrOther pqrOther = new OrdPqrOther().select(new Params("rad_orfeo", orfeoResponse.radNumber), con);
                OrdRepairs repair = new OrdRepairs().select(new Params("rad_orfeo", orfeoResponse.radNumber), con);
                Employee employee = null;
                OrdActivityPqr ordActivityPqr = new OrdActivityPqr();

                String mailSubject = "Se requiere su atención en la ";
                if (pqrCyl != null) {
                    employee = new Employee().select(pqrCyl.registBy, con);
                    ordActivityPqr.pqrCylId = pqrCyl.id;
                    mailSubject += "PQR Cilindros No. " + pqrCyl.serial;
                } else if (pqrTank != null) {
                    employee = new Employee().select(pqrTank.registBy, con);
                    ordActivityPqr.pqrTankId = pqrTank.id;
                    mailSubject += "PQR Estacionarios No. " + pqrTank.serial;
                } else if (pqrOther != null) {
                    employee = new Employee().select(pqrOther.registBy, con);
                    ordActivityPqr.pqrOtherId = pqrOther.id;
                    mailSubject += "PQR Reclamante No. " + pqrOther.serial;
                } else if (repair != null) { // repair
                    employee = new Employee().select(repair.registBy, con);
                    ordActivityPqr.repairId = repair.id;
                    mailSubject += "PQR Asistencias No. " + repair.serial;
                } else {
                    OrfeoResponse response = new OrfeoResponse();
                    response.status = false;
                    response.data = "No se encontro un pqr con ese número de radicado.";
                    return createResponse(response);
                }

                Date now = now(con);
                ordActivityPqr.actDate = now;
                ordActivityPqr.activity = "Traza Orfeo";
                ordActivityPqr.actDeveloper = employee.firstName + " " + employee.lastName; // TODO - Nombre empleado
                ordActivityPqr.creationDate = orfeoResponse.date;
                ordActivityPqr.createId = employee.id;
                ordActivityPqr.modDate = now;
                ordActivityPqr.modId = employee.id;
                ordActivityPqr.radOrfeo = orfeoResponse.responseRadNumber;
                ordActivityPqr.observation = orfeoResponse.notes;
                ordActivityPqr.insert(con);

                String content = "El módulo de Orfeo reportó una nueva actividad con radicado No. " + orfeoResponse.responseRadNumber;

                try {
                    OrdCfg cfg = new OrdCfg().select(1, con);
                    if (cfg.orfeoMailUsers && !MySQLQuery.isEmpty(employee.mail)) {
                        SendMail.sendMail(con, employee.mail, mailSubject, getHtmlMsg(con, mailSubject, content), "");
                    }

                    MySQLQuery mq = new MySQLQuery("SELECT " + SysMailProcess.getSelFlds("") + " FROM sys_mail_process WHERE UPPER(constant) = 'ORFEO_MAILS' AND active");
                    SysMailProcess mailProcess = new SysMailProcess().select(mq, con);
                    if (mailProcess != null) {
                        new SysMailUtil().sendMail(mailProcess.id, mailSubject, content, con, false);
                    }
                } catch (Exception ex) {
                    t.error(ex, con);
                    Logger.getLogger(OrfeoApi.class.getName()).log(Level.SEVERE, null, ex);
                }

                OrfeoResponse response = new OrfeoResponse();
                response.status = true;
                response.data = "Ok";
                return createResponse(response);
            } catch (Exception ex) {
                t.error(ex, con);
                Logger.getLogger(OrfeoApi.class.getName()).log(Level.SEVERE, null, ex);
                OrfeoResponse response = new OrfeoResponse();
                response.status = false;
                response.data = ex.getMessage() != null && !ex.getMessage().isEmpty()
                        ? ex.getMessage()
                        : "No se pudo registrar la actividad";
                return createResponse(response);
            }
        } catch (Exception ex) {
            Logger.getLogger(OrfeoApi.class.getName()).log(Level.SEVERE, null, ex);
            OrfeoResponse response = new OrfeoResponse();
            response.status = false;
            response.data = ex.getMessage() != null && !ex.getMessage().isEmpty()
                    ? ex.getMessage()
                    : "No se pudo registrar la actividad";
            return createResponse(response);
        }
    }
}
