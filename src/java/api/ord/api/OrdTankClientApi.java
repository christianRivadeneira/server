package api.ord.api;

import api.BaseAPI;
import api.GridResult;
import api.GridResultsPdf;
import api.MySQLCol;
import api.bill.model.BillBuilding;
import api.bill.model.BillClientTank;
import api.bill.model.BillInstance;
import api.crm.model.CrmTask;
import api.est.model.EstCfg;
import api.est.model.EstMto;
import api.ord.model.OrdTankClient;
import static api.sys.api.SysHVApi.CRM_CLIENT;
import static api.sys.api.SysHVApi.ORD_TANK_CLIENT;
import api.sys.model.SysCrudLog;
import java.awt.Color;
import java.io.File;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import model.system.SessionLogin;
import utilities.MySQLQuery;
import web.billing.BillingServlet;

@Path("/ordTankClient")
public class OrdTankClientApi extends BaseAPI {

    private BillBuilding createBuilding(OrdTankClient obj, Connection conn) throws Exception {
        if (new BillInstance().select(obj.billInstanceId, conn) == null) {
            throw new Exception("No se puede crear un edificio de facturación en este poblado.");
        }
        useBillInstance(obj.billInstanceId, conn);
        BillBuilding build = new BillBuilding();
        setFields(build, obj);
        build.oldId = new MySQLQuery("(SELECT ifnull((SELECT MAX(b.old_id) from bill_building b), 0) + 1)").getAsInteger(conn);
        build.active = true;
        build.insert(conn);
        useDefault(conn);
        SysCrudLog.created(this, build, conn);
        return build;
    }

    private BillBuilding updateBuilding(OrdTankClient obj, Connection conn) throws Exception {
        if (new BillInstance().select(obj.billInstanceId, conn) == null) {
            throw new Exception("No se puede crear un edificio de facturación en este poblado.");
        }
        useBillInstance(obj.billInstanceId, conn);
        BillBuilding orig = new BillBuilding().select(obj.mirrorId, conn);
        BillBuilding build = new BillBuilding().select(obj.mirrorId, conn);
        setFields(build, obj);
        build.update(conn);
        useDefault(conn);
        SysCrudLog.updated(this, build, orig, conn);
        return build;
    }

    private void setFields(BillBuilding build, OrdTankClient obj) {
        build.address = obj.address;
        build.name = obj.name;
        build.phones = obj.phones;
        build.tankClientTypeId = obj.categId;
    }

    @POST
    public Response insert(OrdTankClient obj) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                getSession(conn);
                if (obj.type.equals("build")) {
                    BillBuilding build = createBuilding(obj, conn);
                    obj.mirrorId = build.id;
                } else {
                    obj.mirrorId = 0;
                }
                useDefault(conn);
                obj.insert(conn);
                new MySQLQuery(OrdTankClient.getCacheQuery(obj.id)).executeUpdate(conn);
                conn.commit();
                return Response.ok(obj).build();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(OrdTankClient obj) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                getSession(conn);
                OrdTankClient orig = new OrdTankClient().select(obj.id, conn);

                if (!obj.active && "build".equals(obj.type)) {
                    useBillInstance(obj.billInstanceId, conn);
                    if (BillClientTank.getCountClients(obj.mirrorId, conn) > 0) {
                        throw new Exception("El cliente esta activo en facturación.");
                    }
                }
                if (orig.active && !obj.active) {
                    if (new MySQLQuery("SELECT COUNT(*) > 0 FROM sigma.est_tank WHERE active = 1 AND client_id = " + obj.id).getAsBoolean(conn)) {
                        throw new Exception("No se puede desactivar si tiene tanques activos.");
                    }
                }

                useDefault(conn);
                EstCfg modCfg = new EstCfg().select(1, conn);
                if (orig.type.equals("norm") && obj.type.equals("build")) {
                    BillBuilding build = createBuilding(obj, conn);
                    obj.mirrorId = build.id;
                } else if (orig.type.equals("build") && obj.type.equals("build")) {
                    if (modCfg.showUsersNumber && obj.createdUsers != null && obj.expectedUsers != null
                            && obj.expectedUsers < obj.createdUsers) {
                        throw new Exception("El cliente no puede tener menos medidores de los ya creados.");
                    }
                    updateBuilding(obj, conn);
                } else if (orig.type.equals("build") && obj.type.equals("norm")) {
                    useBillInstance(orig.billInstanceId, conn);
                    if (BillClientTank.getCountClients(obj.mirrorId, conn) > 0) {
                        throw new Exception("El cliente esta activo en facturación.");
                    }
                    BillBuilding.delete(obj.mirrorId, conn);
                    obj.mirrorId = 0;
                }
                useDefault(conn);
                obj.update(conn);
                new MySQLQuery(OrdTankClient.getCacheQuery(obj.id)).executeUpdate(conn);
                conn.commit();
                return Response.ok(obj).build();
            } catch (Exception ex) {
                conn.rollback();
                throw ex;
            }
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            OrdTankClient obj = new OrdTankClient().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            OrdTankClient.delete(id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getAll")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            return createResponse(OrdTankClient.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/hv")
    public Response getHv(@QueryParam("clientId") int clientId, @QueryParam("limit") boolean limit, @QueryParam("pictures") boolean pictures) {
        try (Connection conn = getConnection()) {
            return createResponse(getHv(clientId, limit, pictures, conn), "hv.pdf");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    public static File getHv(int clientId, boolean limit, boolean pictures, Connection conn) throws Exception {

        Boolean workWithEst = new MySQLQuery("SELECT work_with_est FROM crm_cfg WHERE id = 1").getAsBoolean(conn);
        Integer cmrId = new MySQLQuery("SELECT c.id "
                + "FROM ord_tank_client t  "
                + "INNER JOIN est_prospect p ON p.client_id = t.id "
                + "INNER JOIN crm_client c ON c.prospect_id = p.id "
                + "WHERE t.id = " + clientId).getAsInteger(conn);

        OrdTankClient client = new OrdTankClient().select(clientId, conn);
        File f = File.createTempFile("hojaDeVida", ".pdf");

        Color colorBackground = new Color(255, 248, 248);
        Color colorBorder = new Color(250, 69, 30);

        GridResultsPdf pdf = new GridResultsPdf(f, colorBackground, colorBorder);

        pdf.addDocumentTitle("Hoja de Vida");

        GridResult r = new GridResult();

        r.data = new MySQLQuery("SELECT "
                + "ep.reg_dt, "
                + "ep.afil_dt, "
                + "ep.capacity, "
                + "ep.cons_planned "
                + "FROM est_prospect ep "
                + "WHERE ep.client_id = " + clientId + " ").getRecords(conn);

        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY_HH12_MM_SS_A, 30, "Registro"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY_HH12_MM_SS_A, 30, "Afiliación"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Capacidad"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Consumo")};

        pdf.addVerticalGrid("Datos Prospecto", r);

        r = new GridResult();
        r.data = new MySQLQuery("SELECT "
                + "otc.folder_name, "
                + "otc.name, "
                + "CONCAT(otc.document, IFNULL(CONCAT('-', otc.dv), '')), "
                + "otc.type, "
                + "otc.phones, "
                + "cat.description, "
                + "otc.address, "
                + "COALESCE(otc.neigh, n.name), "
                + "c.name, "
                + "t.name "
                + "FROM ord_tank_client otc "
                + "INNER JOIN est_tank_category cat ON otc.categ_id = cat.id "
                + "LEFT JOIN neigh n ON otc.neigh_id = n.id "
                + "LEFT JOIN city c ON otc.city_id = c.id "
                + "LEFT JOIN est_price_type t ON otc.price_type_id = t.id "
                + "WHERE otc.id = " + clientId + " ").getRecords(conn);
        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_TEXT, 15, "Interno"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Cliente"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Documento"),
            new MySQLCol(MySQLCol.TYPE_ENUM, 15, "Tipo", new OrdTankClient().getEnumOptionsAsMatrix("type")),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Teléfonos"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Categoría"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Dirección"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Barrio"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Ciudad"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 15, "Lista")};

        pdf.addVerticalGrid("Datos Básicos", r);

        if (workWithEst != null && workWithEst) {
            boolean hasDynamic = new MySQLQuery("SELECT COUNT(*)> 0 FROM crm_hist_field").getAsBoolean(conn);
            Object[][] dataDynamic = new MySQLQuery("SELECT  f.name, v.`data`,  f.`type` FROM crm_client cl LEFT JOIN sys_frm_value v ON v.owner_id = cl.id AND v.field_id IN (SELECT id FROM sys_frm_field f WHERE f.type_id = 27) INNER JOIN sys_frm_field f ON f.id = v.field_id INNER JOIN crm_hist_field hf ON hf.field_id = f.id WHERE cl.id = " + cmrId).getRecords(conn);

            if (hasDynamic) {
                dataFormat(dataDynamic);

                r = new GridResult();
                r.data = new Object[1][dataDynamic.length];
                r.cols = new MySQLCol[dataDynamic.length];

                for (int i = 0; i < dataDynamic.length; i++) {
                    r.data[0][i] = MySQLQuery.getAsString(dataDynamic[i][1]);
                    r.cols[i] = new MySQLCol(MySQLCol.TYPE_TEXT, 10, MySQLQuery.getAsString(dataDynamic[i][0]));
                }

                pdf.addVerticalGrid("Datos Personalizados", r);
            }
        }

        Object[][] otherData = new MySQLQuery(
                "SELECT f.name, v.`data`, f.`type` FROM "
                + "sys_frm_value v "
                + "INNER JOIN sys_frm_field f ON f.id = v.field_id "
                + "WHERE f.type_id = 13 AND v.owner_id = ?1 "
                + "ORDER BY f.name ASC ").setParam(1, client.id).getRecords(conn);

        if (otherData.length > 0) {
            dataFormat(otherData);
            r = new GridResult();
            r.data = new Object[1][otherData.length];
            r.cols = new MySQLCol[otherData.length];
            for (int i = 0; i < otherData.length; i++) {
                r.data[0][i] = MySQLQuery.getAsString(otherData[i][1]);
                r.cols[i] = new MySQLCol(MySQLCol.TYPE_TEXT, 10, MySQLQuery.getAsString(otherData[i][0]));
            }
            pdf.addVerticalGrid("Datos Adicionales", r);
        }

        r = new GridResult();

        r.data = new MySQLQuery("SELECT "
                + "otc.represen_name, "
                + "otc.network, "
                + "CONCAT(pe.first_name,' ',pe.last_name),"
                + "otc.contact_name, "
                + "otc.contact_phone, "
                + "otc.contact_mail, "
                + "cta.name, "
                + "otc.stratum "
                + "FROM ord_tank_client otc "
                + "LEFT JOIN per_employee  pe ON pe.id = otc.exec_reg_id "
                + "LEFT JOIN est_contractor cta ON cta.id = otc.contractor_id "
                + "WHERE otc.id = " + clientId + " ").getRecords(conn);

        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Representante"),
            new MySQLCol(MySQLCol.TYPE_ENUM, 15, "Red", new OrdTankClient().getEnumOptionsAsMatrix("network")),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Ejecutivo"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Contacto"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Teléfono de Contacto"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Correo de Contacto"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Contratista"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Estrato")};

        pdf.addVerticalGrid("Datos Comerciales", r);

        r = new GridResult();
        r.data = new MySQLQuery("SELECT "
                + "t.serial,"
                + "t.capacity,"
                + "t.ctr_type, "
                + "t.factory, "
                + "otc2.name "
                + "FROM ord_tank_client otc "
                + "LEFT JOIN est_tank t ON t.client_id = otc.id "
                + "LEFT JOIN ord_tank_client otc2 ON otc2.id = t.last_client_id "
                + "WHERE "
                + "otc.id = " + clientId).getRecords(conn);

        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Serial"),
            new MySQLCol(MySQLCol.TYPE_INTEGER, 15, "Capacidad"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Propiedad"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Fabricante"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Ult Cliente")};

        pdf.addGrid("Tanques", r);

        r = new GridResult();
        r.data = new MySQLQuery("SELECT "
                + "ext.lbs, "
                + "ext.enterprise, "
                + "ext.expiration_date "
                + "FROM est_ext_client ext "
                + "WHERE ext.active "
                + "AND ext.client_id = " + clientId
                + (limit ? " LIMIT 15" : "")).getRecords(conn);

        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Libras"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Empresa"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Expiración")};

        pdf.addGrid("Extintores", r);

        r = new GridResult();
        r.data = new MySQLQuery("SELECT   "
                + "s.sale_date,   "
                + "s.bill_num,   "
                + "v.plate ,   "
                + "CONCAT(e.first_name,' ',e.last_name),   "
                + "s.kgs,"
                + "s.total "
                + "FROM   "
                + "est_sale s    "
                + "LEFT JOIN vehicle v ON v.id = s.vh_id   "
                + "LEFT JOIN employee  e ON e.id = s.driver_id   "
                + "WHERE   "
                + "s.client_id =" + clientId + " "
                + "ORDER BY s.sale_date DESC "
                + (limit ? " LIMIT 15" : "")).getRecords(conn);

        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 30, "Venta"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Fact /Rem"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Vehículo"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Vendedor"),
            new MySQLCol(MySQLCol.TYPE_DECIMAL_1, 15, "kgs"),
            new MySQLCol(MySQLCol.TYPE_DECIMAL_1, 15, "Total")};

        pdf.addGrid("Facturas / Remisiones", r);

        if (client.type.equals("build")) {
            String db = BillingServlet.getInst(client.billInstanceId).db;
            r = new GridResult();
            r.data = new MySQLQuery("SELECT s.cons_month, SUM(r.reading - r.last_reading) FROM "
                    + db + ".bill_reading r "
                    + "INNER JOIN " + db + ".bill_span s ON s.id = r.span_id "
                    + "INNER JOIN " + db + ".bill_client_tank c ON c.id = r.client_tank_id "
                    + "WHERE c.building_id = ?1 AND r.reading IS NOT null "
                    + "GROUP BY r.span_id "
                    + "ORDER BY s.cons_month DESC").setParam(1, client.mirrorId).getRecords(conn);
            r.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_DD_MM, 30, "Mes Consumo"),
                new MySQLCol(MySQLCol.TYPE_DECIMAL_4, 15, "Consumo Medido m3")};

            pdf.addGrid("Consumos por Medidores", r);
        }

        r = new GridResult();
        r.data = new MySQLQuery("SELECT   "
                + "t.desc_short,  "
                + "t.prog_date,  "
                + "t.ejec_date,  "
                + "t.description,  "
                + "t.priority    "
                + "FROM crm_task t  "
                + "LEFT JOIN crm_client c ON c.id = t.client_id   "
                + "LEFT JOIN est_prospect p ON p.id = c.prospect_id  "
                + "WHERE   "
                + "p.client_id = " + clientId
                + (limit ? " LIMIT 15" : "")).getRecords(conn);

        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Actividad"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 15, "Prog"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 15, "Eject"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Detalle"),
            new MySQLCol(MySQLCol.TYPE_ENUM, 15, "Prioridades", new CrmTask().getEnumOptionsAsMatrix("priority"))
        };

        pdf.addGrid("Actividades", r);

        r = new GridResult();
        r.data = new MySQLQuery("SELECT p.serial, p.regist_date, p.regist_hour, r.description, "
                + "CONCAT(t.first_name, ' ', t.last_name), IF(p.satis_poll_id IS NULL, 'Capturado', IF(p.anul_cause_id IS NULL, 'Confirmado', 'Cancelado')) "
                + "FROM ord_pqr_tank p "
                + "INNER JOIN ord_technician AS t ON t.id = p.technician_id "
                + "INNER JOIN ord_pqr_reason AS r ON r.id = p.reason_id "
                + "WHERE p.build_id = " + clientId
                + (limit ? " LIMIT 15" : "")).getRecords(conn);

        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_TEXT, 15, "Serial"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 20, "Capturado"),
            new MySQLCol(MySQLCol.TYPE_HH12_MM_SS_A, 15, "Hora"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Motivo"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Técnico"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 15, "Estado")
        };

        pdf.addGrid("PQRs Estacionarios", r);

        r = new GridResult();
        r.data = new MySQLQuery("SELECT p.serial, p.regist_date, p.regist_hour, "
                + "r.description, p.subject_reason, IF(p.pqr_poll_id IS NULL, 'Capturado', IF(p.anul_cause_id IS NULL, 'Confirmado', 'Cancelado')) "
                + "FROM ord_pqr_other AS p "
                + "INNER JOIN ord_pqr_reason AS r ON r.id = p.reason_id "
                + "WHERE p.build_id = " + clientId
                + (limit ? " LIMIT 15" : "")).getRecords(conn);

        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_TEXT, 15, "Serial"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 20, "Capturado"),
            new MySQLCol(MySQLCol.TYPE_HH12_MM_SS_A, 15, "Hora"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Motivo"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Asunto"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 15, "Estado")
        };

        pdf.addGrid("PQRs Reclamantes", r);

        r = new GridResult();
        r.data = new MySQLQuery("SELECT p.serial, p.regist_date, p.regist_hour, "
                + "r.description, CONCAT(t.first_name, ' ', t.last_name), "
                + "IF(p.pqr_poll_id IS NULL, 'Capturado', IF(p.anul_cause_id IS NULL, 'Confirmado', 'Cancelado')) "
                + "FROM ord_repairs AS p "
                + "INNER JOIN ord_technician AS t ON t.id = p.technician_id "
                + "INNER JOIN ord_pqr_reason AS r ON r.id = p.reason_id "
                + "WHERE p.build_id = " + clientId
                + (limit ? " LIMIT 15" : "")).getRecords(conn);

        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_TEXT, 15, "Serial"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 20, "Capturado"),
            new MySQLCol(MySQLCol.TYPE_HH12_MM_SS_A, 15, "Hora"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Motivo"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 30, "Técnico"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 15, "Estado")
        };

        pdf.addGrid("Asistencias Técnicas", r);

        r = new GridResult();
        r.data = new MySQLQuery("SELECT t.serial, t.capacity, mto.prog_date, mto.exec_date, mto.type "
                + "FROM est_tank t "
                + "INNER JOIN est_mto mto ON mto.tank_id = t.id "
                + "WHERE t.client_id = " + clientId
                + (limit ? " LIMIT 15" : "")).getRecords(conn);

        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Tanque"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 20, "Capacidad"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 20, "Programada"),
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 20, "Ejecutada"),
            new MySQLCol(MySQLCol.TYPE_ENUM, 20, "Tipo", new EstMto().getEnumOptionsAsMatrix("type"))
        };

        pdf.addGrid("Mantenimientos", r);

        r = new GridResult();
        r.data = new MySQLQuery("SELECT "
                + "type.name, "
                + "IF(bfile.id is null, false, true), "
                + "IF(doc.state IS NOT NULL, doc.state, 'pend') "
                + "FROM "
                + "est_doc_type AS type "
                + "LEFT JOIN est_client_doc AS doc ON type.id = doc.type_id AND doc.client_id = ?1 "
                + "LEFT JOIN bfile ON doc.id = bfile.owner_id AND bfile.owner_type = 24 "
                + "WHERE (type.mandatory AND type.active) AND (type.type = 'third_network' || type.type = 'all')"
                + (limit ? " LIMIT 15" : "")).setParam(1, clientId).getRecords(conn);

        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_TEXT, 65, "Documento"),
            new MySQLCol(MySQLCol.TYPE_BOOLEAN, 35, "Adjunto"),
            new MySQLCol(MySQLCol.TYPE_ENUM, 40, "Estado", MySQLQuery.getEnumOptionsAsMatrix("ok=Al Día&na=No Aplica&act=Actualizar&pend=Pendiente"))
        };

        pdf.addGrid("Requisitos", r);

        //novedades
        r = new GridResult();
        r.data = new MySQLQuery("SELECT "
                + "dt, "
                + "novs, "
                + "num_visit, "
                + "active "
                + "FROM est_sede_nov "
                + "WHERE clie_tank_id = " + clientId + " "
                + "ORDER BY dt DESC"
                + (limit ? " LIMIT 15" : "")
        ).getRecords(conn);

        r.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_DD_MM_YYYY, 12, "Fecha"),
            new MySQLCol(MySQLCol.TYPE_TEXT, 50, "Detalle"),
            new MySQLCol(MySQLCol.TYPE_INTEGER, 8, "Mostrar"),
            new MySQLCol(MySQLCol.TYPE_BOOLEAN, 8, "Activo")
        };
        pdf.addGrid("Novedades", r);

        if (pictures) {
            if (workWithEst != null && workWithEst) {
                pdf.addPhothos(conn,
                        new Integer[]{clientId, cmrId},
                        new Integer[]{ORD_TANK_CLIENT, CRM_CLIENT});
            } else {
                pdf.addPhothos(conn,
                        new Integer[]{clientId},
                        new Integer[]{ORD_TANK_CLIENT});
            }
        }

        pdf.close();
        return f;
    }

    private static void dataFormat(Object[][] data) throws Exception {
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

}
