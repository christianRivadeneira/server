package api.mss.api;

import api.BaseAPI;
import api.Params;
import api.mss.model.MssCert;
import api.mss.model.MssCertElement;
import api.mss.model.MssClient;
import api.mss.model.MssGuard;
import static api.mss.model.MssGuard.getSuperIdFromEmployee;
import api.mss.model.MssPost;
import api.sys.model.DanePoblado;
import api.sys.model.SysCrudLog;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import utilities.ServerNow;
import utilities.pdf.PDFCellStyle;
import web.enterpriseLogo;

@Path("/mssCert")
public class MssCertApi extends BaseAPI {

    @POST
    public Response insert(MssCert obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            obj.insert(conn);
            SysCrudLog.created(this, obj, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(MssCert obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssCert old = new MssCert().select(obj.id, conn);
            obj.update(conn);
            SysCrudLog.updated(this, obj, old, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssCert obj = new MssCert().select(id, conn);
            Object[][] inProgData = null;

            inProgData = new MySQLQuery("SELECT c.id, c.name, c.status,c.cert_id"
                    + " from mss_cert_element c"
                    + " WHERE "
                    + " c.cert_id = ?1"
            ).setParam(1, id).getRecords(conn);

            List<MssCertElement> shifts = new ArrayList<>();

            for (Object[] row : inProgData) {
                MssCertElement objCertElement = new MssCertElement();

                objCertElement.id = MySQLQuery.getAsInteger(row[0]);
                objCertElement.name = MySQLQuery.getAsString(row[1]);
                objCertElement.status = MySQLQuery.getAsString(row[2]);
                objCertElement.certId = MySQLQuery.getAsInteger(row[3]);

                shifts.add(objCertElement);
            }
            obj.listCertElement = shifts;

            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            MssCert.delete(id, conn);
            SysCrudLog.deleted(this, MssCert.class, id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/byPost")
    public Response getAllSuper(@QueryParam("postId") int postId) {
        try (Connection conn = getConnection()) {
            getSession(conn);            
            List<MssCert> listCert = new MssCert().getListFromParams(new Params("post_id",postId), conn);           
            return createResponse(listCert);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/setElementCert")
    public Response insertCertElement(MssCert obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            Integer superdId = getSuperIdFromEmployee(sl.employeeId, conn);

            obj.regDt = new ServerNow();
            obj.superId = superdId;
            if (obj.id == 0) {
                int idCert = obj.insert(conn);
                SysCrudLog.created(this, obj, conn);
                if (obj.listCertElement != null) {
                    for (int i = 0; i < obj.listCertElement.size(); i++) {

                        MssCertElement mssValue = new MssCertElement();
                        mssValue.name = obj.listCertElement.get(i).name;
                        mssValue.certId = idCert;
                        mssValue.status = obj.listCertElement.get(i).status;
                        mssValue.insert(conn);
                    }
                }

            } else {
                obj.update(conn);
                SysCrudLog.created(this, obj, conn);
                if (obj.listCertElement != null) {
                    for (int i = 0; i < obj.listCertElement.size(); i++) {
                        MssCertElement mssValue = new MssCertElement();
                        if (obj.listCertElement.get(i).id == 0) {
                            mssValue.name = obj.listCertElement.get(i).name;
                            mssValue.certId = obj.id;
                            mssValue.status = obj.listCertElement.get(i).status;
                            mssValue.insert(conn);
                        } else {
                            mssValue.name = obj.listCertElement.get(i).name;
                            mssValue.certId = obj.id;
                            mssValue.status = obj.listCertElement.get(i).status;
                            mssValue.id = obj.listCertElement.get(i).id;
                            mssValue.update(conn);
                        }
                    }
                }
                if (obj.listCertElementDelete != null) {
                    for (int i = 0; i < obj.listCertElementDelete.size(); i++) {
                        if (obj.listCertElementDelete.get(i).id != 0) {
                            MssCertElement mssValue = new MssCertElement();
                            mssValue.delete(obj.listCertElementDelete.get(i).id, conn);
                        }

                    }
                }

            }

            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/pdf")
    public Response getPdf(@QueryParam("id") int certId) {
        try (Connection conn = getConnection()) {
            getSession(conn);

            MssCert cert = new MssCert().select(certId, conn);
            List<MssCertElement> elems = MssCertElement.getByCert(cert.id, conn);

            MssGuard superv = new MssGuard().select(cert.superId, conn);
            MssPost post = new MssPost().select(cert.postId, conn);
            MssClient cli = new MssClient().select(post.clientId, conn);

            Document document = new Document(new Rectangle(8.5f * 72f, 11f * 72f), 50f, 50f, 50f, 50f);
            File fin = File.createTempFile("hoja de vida", ".pdf");
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(fin));
            //  HeaderFooter event = new HeaderFooter();
            writer.setBoxSize("art", new Rectangle(36, 54, 559, 788));
            // writer.setPageEvent(event);
            document.open();
            
            PDFCellStyle tblTitleStyle = new PDFCellStyle();
            tblTitleStyle.setAppearance(true, PDFCellStyle.DEFAULT_PADDING, PDFCellStyle.WHITE, PDFCellStyle.GRAY_BORDER);
            tblTitleStyle.sethAlignment(PDFCellStyle.ALIGN_CENTER);
            tblTitleStyle.setFontSize(PDFCellStyle.DEFAULT_FONT_SIZE);
            tblTitleStyle.setBold(true);

            PDFCellStyle tblStyle = new PDFCellStyle();
            tblStyle.setAppearance(true, PDFCellStyle.DEFAULT_PADDING, PDFCellStyle.WHITE, PDFCellStyle.GRAY_BORDER);
            tblStyle.sethAlignment(PDFCellStyle.ALIGN_CENTER);
            tblStyle.setFontSize(PDFCellStyle.DEFAULT_FONT_SIZE);
            tblStyle.setBold(false);

            //document.add(getTabHeader(conn, cellStyle, titleStyle));
            Paragraph tp = new PDFCellStyle(false, 0, true, null, null, Color.BLACK, Element.ALIGN_CENTER, Element.ALIGN_CENTER, 12).getParagraph("ACTA DE INSTALACIÓN O LEVANTAMIENTO DE PUESTO DE TRABAJO", Element.ALIGN_CENTER);
            document.add(tp);

            PDFCellStyle bodyStyle = new PDFCellStyle(false, 0, false, null, null, Color.BLACK, Element.ALIGN_CENTER, Element.ALIGN_CENTER, 10);
            String s = "INSTALACIÓN [" + (cert.type.equals("in") ? "X" : " ") + "]                LEVANTAMIENTO [" + (cert.type.equals("out") ? "X" : " ") + "]";
            Paragraph p = bodyStyle.getParagraph(s, Element.ALIGN_CENTER);
            p.setSpacingAfter(15);
            p.setSpacingBefore(15);
            document.add(p);

            String noData = " __________________ ";
            String cityName;

            if (post.danePobladoId != null) {
                cityName = new DanePoblado().select(post.danePobladoId, conn).name;
            } else {
                cityName = noData;
            }

            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(cert.regDt);

            String s1 = "En " + cityName + ", el día " + gc.get(GregorianCalendar.DAY_OF_MONTH) + " del mes " + (gc.get(GregorianCalendar.MONTH) + 1) + " del año " + gc.get(GregorianCalendar.YEAR) + ", siendo las " + gc.get(GregorianCalendar.HOUR_OF_DAY) + ":" + gc.get(GregorianCalendar.MINUTE) + ", ";
            s1 += "se reunieron en el puesto de trabajo No " + post.code + " ubicado en " + (post.address != null && !post.address.isEmpty() ? post.address : noData) + ", " + "el señor " + superv.firstName + " " + superv.lastName + ", con cédula de ciudadanía " + superv.document + " expedida en " + (superv.expDocument != null && !superv.expDocument.isEmpty() ? superv.expDocument : noData) + " por parte de SEGURIDAD DEL SUR LIMITADA ";
            s1 += "y el señor " + noData + " con cédula de ciudadanía " + noData + " expedida en " + noData + "por parte de " + cli.name + ". Con el fin de " + (cert.type.equals("in") ? "recibir" : "entregar") + ", según presentación efectuada, los siguientes elementos: ";
            p = bodyStyle.getParagraph(s1, Element.ALIGN_CENTER);
            p.setSpacingAfter(15);
            p.setSpacingBefore(15);
            document.add(p);

            PdfPTable tbl = new PdfPTable(4);
            tbl.setWidths(new float[]{90, 30, 30, 30});
            tbl.setWidthPercentage(100);

            tbl.addCell(tblTitleStyle.getCell("ELEMENTOS EN CUSTODIA", 1, 2));
            tbl.addCell(tblTitleStyle.getCell("ESTADO", 3, 1));

            tbl.addCell(tblTitleStyle.getCell("BUENO"));
            tbl.addCell(tblTitleStyle.getCell("REGULAR"));
            tbl.addCell(tblTitleStyle.getCell("MALO"));

            tbl.setSpacingAfter(10);

            for (int i = 0; i < elems.size(); i++) {
                MssCertElement e = elems.get(i);

                tbl.addCell(tblStyle.getCell(e.name));

                switch (e.status) {
                    case "b":
                        tbl.addCell(tblStyle.getCell("X"));
                        tbl.addCell(tblStyle.getCell(""));
                        tbl.addCell(tblStyle.getCell(""));
                        break;
                    case "r":
                        tbl.addCell(tblStyle.getCell(""));
                        tbl.addCell(tblStyle.getCell("X"));
                        tbl.addCell(tblStyle.getCell(""));
                        break;
                    case "m":
                        tbl.addCell(tblStyle.getCell(""));
                        tbl.addCell(tblStyle.getCell(""));
                        tbl.addCell(tblStyle.getCell("X"));
                        break;
                    default:
                        tbl.addCell(tblStyle.getCell(""));
                        tbl.addCell(tblStyle.getCell(""));
                        tbl.addCell(tblStyle.getCell(""));
                        break;
                }
            }

            document.add(tbl);

            String s3 = "ENTREGA TOTAL [" + (cert.delivery.equals("total") ? "X" : " ") + "]                ENTREGA PARCIAL [" + (cert.delivery.equals("parcial") ? "X" : " ") + "]";
            p = bodyStyle.getParagraph(s3, Element.ALIGN_CENTER);
            p.setSpacingAfter(15);
            p.setSpacingBefore(15);
            document.add(p);

            String s4 = "Observaciones: " + cert.notes;
            p = bodyStyle.getParagraph(s4, Element.ALIGN_JUSTIFIED);
            p.setSpacingAfter(15);
            p.setSpacingBefore(15);
            document.add(p);

            String s5 = "Una vez terminada la diligencia siendo las " + gc.get(GregorianCalendar.HOUR_OF_DAY) + ":" + gc.get(GregorianCalendar.MINUTE) + ", el representate de la empresa que " + (cert.type.equals("in") ? "entrega" : "recibe") + " LO HACE A SATISFACCIÓN";
            p = bodyStyle.getParagraph(s5, Element.ALIGN_CENTER);
            p.setSpacingAfter(15);
            p.setSpacingBefore(15);
            document.add(p);

            String s6 = "_________________                             _________________";
            p = bodyStyle.getParagraph(s6, Element.ALIGN_CENTER);            
            p.setSpacingBefore(50);
            document.add(p);
            
            String s7 = "RECIBE                                              ENTREGA";
            p = bodyStyle.getParagraph(s7, Element.ALIGN_CENTER);            
            document.add(p);

            document.close();
            return createResponse(fin, "acta.pdf");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private static PdfPTable getTabHeader(Connection ep, PDFCellStyle cellStyle, PDFCellStyle titleStyle) throws Exception {
        PdfPTable tab = new PdfPTable(2);
        tab.setWidths(new float[]{40, 60});
        tab.setWidthPercentage(100);

        PdfPCell imgCell;
        try {
            byte[] readAllBytes = Files.readAllBytes(enterpriseLogo.getEnterpriseLogo("4", ep).toPath());
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
        tab.addCell(nobackgroundCell.getCell("ACTA DE INSTALACIÓN O LEVANTAMIENTO DE PUESTO DE TRABAJO", PDFCellStyle.ALIGN_CENTER));
        tab.setSpacingAfter(10);
        return tab;
    }

}
