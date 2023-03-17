package api.dto.importer;

import api.dto.api.DtoSaleApi;
import static api.dto.api.DtoSaleApi.DTO_IMPORT_LOG;
import api.dto.model.DtoCylPrice;
import api.dto.model.DtoCylType;
import api.dto.model.DtoSalesman;
import api.trk.model.CylinderType;
import api.trk.model.TrkCyl;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import utilities.Dates;
import utilities.MySQLQuery;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.Table;
import web.fileManager;
import web.marketing.cylSales.SyncDtoSales;

/**
 *
 * @author alder
 */
public class Importer {

    public static class Content {

        public List<MinMinas> allRows = new ArrayList<>();
        public Map<String, Integer> errors = new HashMap<>();
        public boolean otherDates;
    }

    public static File fromZip(File f) throws Exception {
        File tmp;
        try (ZipFile zipFile = new ZipFile(f)) {
            ZipEntry e = zipFile.entries().nextElement();
            tmp = File.createTempFile("csv", ".csv");
            try (FileOutputStream fout = new FileOutputStream(tmp); InputStream in = zipFile.getInputStream(e)) {
                fileManager.copy(in, fout);
            }
        }
        return tmp;
    }

    public static Content getContent(File f, Connection conn) throws Exception {
        Content rta = new Content();
        List<DtoCylType> dtoCylTypes = DtoCylType.getAll(conn);
        List<DtoCylPrice> cylPrices = DtoCylPrice.getAll(conn);
        List<DtoSalesman> salesmen = DtoSalesman.getActive(conn);
        List<CylinderType> cylTypes = CylinderType.getAll(conn);

        Class[] colsCsv = new Class[]{
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class,
            String.class};

        Object[][] data = CSVReader.read(f, Charset.forName("UTF-8"), ",", colsCsv);
        int headRow = -1;
        for (int i = 0; i < data.length; i++) {
            if (Header.isHeader(data[i])) {
                headRow = i;
                break;
            }
        }
        if (headRow == -1) {
            throw new Exception("No se encontró la fila de encabezados.");
        }

        Header head = new Header(data[headRow]);

        for (int j = headRow + 1; j < data.length; j++) {
            try {
                Object[] row = data[j];
                if (!isAllWhite(row)) {
                    MinMinas m = new MinMinas();
                    SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    SimpleDateFormat myFormat = new SimpleDateFormat("yyyy-MM-dd");
                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                    Date fecha = formato.parse((String) row[head.datePos]);
                    String miFecha = myFormat.format(fecha);
                    String hora = formatter.format(fecha);
                    m.dt =  MinMinas.forgeDate(miFecha, hora);
                    m.clieDoc = MySQLQuery.getAsLong(row[head.docPos]);
                    m.origCapa = getAsInt(row[head.capaPos]);
                    m.valueTotal = MySQLQuery.getAsBigDecimal(row[head.valPos], true).intValue();
                    m.subsidy = MySQLQuery.getAsBigDecimal(row[head.subsPos], true).intValue();
                    m.salesmanDoc = row[head.salesmanPos].toString();
                    m.nif = MySQLQuery.getAsString(row[head.nifPos]);
                    m.stratum = getAsInt(row[head.stratumPos]);
                    m.aprovNumber = MySQLQuery.getAsInteger(row[head.nAprov]);
                    m.bill = MySQLQuery.getAsBigInteger(row[head.bill]);
                    m.salesman = DtoSalesman.findSalesman(salesmen, m.salesmanDoc);
                    m.cylPrice = DtoCylPrice.findCylPrice(cylPrices, (m.salesman != null ? m.salesman.centerId : null), (m.cyl != null ? m.cyl.cylTypeId : null));
                    m.subsidy = (m.subsidy != null && m.subsidy == 0 ? null : m.subsidy);
                    m.valueTotal = (m.valueTotal != null && m.valueTotal == 0 ? null : m.valueTotal);
                    m.lat = MySQLQuery.getAsBigDecimal(row[head.latPos], false);
                    m.lon = MySQLQuery.getAsBigDecimal(row[head.lonPos], false);
                    m.depto = MySQLQuery.getAsString(row[head.deptoPos]);
                    m.mun = MySQLQuery.getAsString(row[head.munPos]);

                    if (row.length > 15) {
                        m.anulNotes = MySQLQuery.getAsString(row[head.anulNotesPos]);
                    } else {
                        m.anulNotes = null;
                    }

                    if (m.subsidy != null) {

                        StringBuilder sbImport = new StringBuilder();
                        StringBuilder sbNoPayment = new StringBuilder();

                        if (m.aprovNumber == null) {
                            sbImport.append("Falta aprobación");
                            m.state = MinMinas.STATE_WARN;
                        }
                        if (m.bill == null) {
                            sbImport.append("Falta factura");
                            m.state = MinMinas.STATE_WARN;
                        }

                        if (m.valueTotal == null) {
                            sbNoPayment.append("Falta valor");
                            m.payment = false;
                        }

                        if (m.cylPrice != null && m.valueTotal != null) {
                            if (m.cylPrice.priceFrom > m.valueTotal || m.valueTotal > m.cylPrice.priceTo) {
                                if (m.cylPrice.priceFrom > m.valueTotal) {
                                    sbNoPayment.append("Total menor que ").append(m.cylPrice.priceFrom);
                                } else {
                                    sbNoPayment.append("Total mayor que ").append(m.cylPrice.priceTo);
                                }
                                m.payment = false;
                            }
                        }
                        if (m.cylPrice != null && m.subsidy > m.valueTotal) {
                            sbNoPayment.append("Subsidio mayor al valor.");
                            m.payment = false;
                        }

                        if (m.salesman == null) {
                            String e = "Vendedor desconocido: " + m.salesmanDoc + ". ";
                            addError(rta.errors, e);
                            sbImport.append(e);
                            m.state = MinMinas.STATE_ERROR;
                        }

                        m.cyl = TrkCyl.selectByNif(m.nif, conn);
                        if (m.cyl != null) {
                            m.cylType = CylinderType.find(cylTypes, m.cyl.cylTypeId);
                            DtoCylType minasType = DtoCylType.findCylType(dtoCylTypes, m.origCapa);
                            if (minasType == null) {
                                sbImport.append("Capacidad desconocida: ").append(m.origCapa).append(". ");
                                m.state = MinMinas.STATE_WARN;
                            } else if (minasType.cylinderTypeId != m.cyl.cylTypeId) {
                                String e = "Capacidad no Coincide, mme " + minasType.minasName + " kg vs mg " + m.cylType.kg + " kg ";
                                addError(rta.errors, e);
                                sbImport.append(e);
                                m.state = MinMinas.STATE_ERROR;
                            }
                        } else {
                            String e = "Cilindro Desconocido: " + m.nif + ". ";
                            addError(rta.errors, e);
                            sbImport.append(e);
                            m.state = MinMinas.STATE_ERROR;
                        }

                        rta.allRows.add(m);
                        m.origRowIndex = j + 1;
                        m.importNotes = sbImport.toString();
                        m.noPaymentNotes = sbNoPayment.toString();

                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(DtoSaleApi.class.getName()).log(Level.INFO, "message", ex);
                throw new Exception("Error en la fila " + (j + 1) + ".\n" + ex.getMessage());
            }
        }

        //verifica si hay registros de otras fechas
        Date today = Dates.trimDate(new Date());
        for (MinMinas allRow : rta.allRows) {
            if (Dates.trimDate(allRow.dt).compareTo(today) != 0) {
                rta.otherDates = true;
                break;
            }
        }
        return rta;
    }

    public synchronized static DtoSaleImportResult importCsv(int importId, Connection conn) throws Exception {
        long t = System.currentTimeMillis();
        fileManager.PathInfo pi = new fileManager.PathInfo(conn);
        Integer fileId = new MySQLQuery("SELECT id FROM bfile WHERE owner_id = ?1 AND owner_type = ?2").setParam(1, importId).setParam(2, DTO_IMPORT_LOG).getAsInteger(conn);

        File f = Importer.fromZip(pi.getExistingFile(fileId));
        Content c;
        try {
            c = getContent(f, conn);
        } finally {
            f.delete();
        }

        Updates updates = Updates.processData(c.allRows, importId, c.otherDates, conn);

        StringBuilder sb = new StringBuilder();
        switch (updates.inserts) {
            case 0:
                sb.append("No se hallaron nuevos registros.");
                break;
            case 1:
                sb.append("Se halló un nuevo registro.");
                break;
            default:
                sb.append("Se hallaron ").append(updates.inserts).append(" nuevos registros.");
                break;
        }
        sb.append(System.lineSeparator());

        switch (updates.overwrites) {
            case 0:
                sb.append("No se reemplazaron registros.");
                break;
            case 1:
                sb.append("Se reemplazó un registro.");
                break;
            default:
                sb.append("Se reemplazon ").append(updates.overwrites).append(" registros.");
                break;
        }
        sb.append(System.lineSeparator());

        switch (updates.misses) {
            case 0:
                sb.append("No se omitió ningún registro.");
                break;
            case 1:
                sb.append("Se omitió un registro.");
                break;
            default:
                sb.append("Se omitieron ").append(updates.misses).append(" registros.");
                break;
        }
        sb.append(System.lineSeparator());

        switch (updates.error) {
            case 0:
                sb.append("No hay registros con errores.");
                break;
            case 1:
                sb.append("Se ingresó un registro con errores.");
                break;
            default:
                sb.append("Se ingresaron ").append(updates.error).append(" registros con errores.");
                break;
        }
        //warn
        sb.append(System.lineSeparator());
        switch (updates.warns) {
            case 0:
                sb.append("No hay registros para revisión.");
                break;
            case 1:
                sb.append("Se ingresó un registro para revisión.");
                break;
            default:
                sb.append("Se ingresaron ").append(updates.warns).append(" registros para revisión.");
                break;
        }
        //no payment
        sb.append(System.lineSeparator());
        switch (updates.noPayment) {
            case 0:
                sb.append("Todos los registros podrán ser pagados.");
                break;
            case 1:
                sb.append("Hay un registro que no será pagado.");
                break;
            default:
                sb.append("Se ingresaron ").append(updates.noPayment).append(" registros que no serán pagados.");
                break;
        }
        sb.append(System.lineSeparator());
        t = System.currentTimeMillis() - t;
        sb.append("Duro: ").append(t).append("ms");
        sb.append(System.lineSeparator());
        new MySQLQuery("UPDATE dto_import_log SET dt_import = now(), notes = CONCAT(COALESCE(notes, ''), '" + sb.toString() + "') WHERE id = " + importId).executeUpdate(conn);
        SyncDtoSales.syncDtoTrk(updates.inserts + updates.overwrites, conn);
        return new DtoSaleImportResult(sb.toString());
    }

    public static MySQLReport importCsvAnul(int importId, Connection conn) throws Exception {
        List<Object[]> lstAnul = new ArrayList<>();
        long t = System.currentTimeMillis();
        fileManager.PathInfo pi = new fileManager.PathInfo(conn);
        Integer fileId = new MySQLQuery("SELECT id FROM bfile WHERE owner_id = ?1 AND owner_type = ?2").setParam(1, importId).setParam(2, DTO_IMPORT_LOG).getAsInteger(conn);

        File f = Importer.fromZip(pi.getExistingFile(fileId));
        Content c;
        try {
            c = getContent(f, conn);
        } finally {
            f.delete();
        }

        if (c.allRows != null) {
            for (MinMinas row : c.allRows) {
                if (row.anulNotes != null && row.anulNotes.equals("ANULADO")) {
                    Integer anuLiq = new MySQLQuery("SELECT COUNT(*) "
                            + "FROM trk_anul_sale a "
                            + "INNER JOIN dto_sale s ON s.id = a.dto_sale_id "
                            + "WHERE s.aprov_number = ?1").setParam(1, row.aprovNumber).getAsInteger(conn);

                    if (anuLiq == null || anuLiq == 0) {
                        Integer numLiq = new MySQLQuery("SELECT COUNT(*) FROM dto_sale s WHERE s.aprov_number = ?1 AND s.dto_liq_id IS NOT NULL").setParam(1, row.aprovNumber).getAsInteger(conn);
                        Object[] rowAnul = new Object[17];
                         rowAnul[0] = row.aprovNumber;
                            rowAnul[1] = row.clieDoc;
                            rowAnul[2] = row.stratum;
                            rowAnul[3] = row.mun;
                            rowAnul[4] = row.depto;
                            rowAnul[5] = Dates.getExcelFormat().format(row.dt);
                            rowAnul[6] = Dates.getSQLTimeFormat().format(row.dt);
                            rowAnul[7] = row.valueTotal;
                            rowAnul[8] = row.subsidy;
                            rowAnul[9] = row.salesmanDoc;
                            rowAnul[10] = row.origCapa;
                            rowAnul[11] = row.nif;
                            rowAnul[12] = row.lat;
                            rowAnul[13] = row.lon;
                            rowAnul[14] = row.bill;
                        if (numLiq != null && numLiq>0) {
                            rowAnul[15] = row.anulNotes;
                            rowAnul[16] = (numLiq > 0 ? "Si" : "No");
                        } else {
                            rowAnul[15] = "No importada";
                            rowAnul[16] = " ";
                        }
                        lstAnul.add(rowAnul);
                    }
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(System.lineSeparator());

        switch (c.allRows.size()) {
            case 0:
                sb.append("No se encontró ningún registro anulado.");
                break;
            case 1:
                sb.append("Se encontró un registro anulado.");
                break;
            default:
                sb.append("Se encontró ").append(c.allRows.size()).append(" registros anulados.");
                break;
        }
        sb.append(System.lineSeparator());

        t = System.currentTimeMillis() - t;
        sb.append("Duro: ").append(t).append("ms");
        sb.append(System.lineSeparator());
        new MySQLQuery("UPDATE dto_import_log SET dt_import = now(), notes = CONCAT(COALESCE(notes, ''), '" + sb.toString() + "') WHERE id = " + importId).executeUpdate(conn);

        MySQLReport rep = new MySQLReport("Reporte Ministerio Ventas Anuladas", "", "ministerio", MySQLQuery.now(conn));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().get(0).setWrap(true);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "dd/MM/yyyy"));//1
        rep.setVerticalFreeze(5);
        rep.setZoomFactor(80);

        Table tb = new Table("Ventas Anuladas");
        tb.getColumns().add(new Column("Aprobación", 15, 0));
        tb.getColumns().add(new Column("Beneficiario", 15, 0));
        tb.getColumns().add(new Column("Estrato", 15, 0));
        tb.getColumns().add(new Column("Municipio", 15, 0));
        tb.getColumns().add(new Column("Departamento", 15, 0));
        tb.getColumns().add(new Column("Fecha", 15, 0));
        tb.getColumns().add(new Column("Hora", 15, 0));
        tb.getColumns().add(new Column("Valor", 15, 0));
        tb.getColumns().add(new Column("Subsidio", 15, 0));
        tb.getColumns().add(new Column("Vendedor", 15, 0));
        tb.getColumns().add(new Column("Capacidad", 15, 0));
        tb.getColumns().add(new Column("NIF", 15, 0));
        tb.getColumns().add(new Column("Latitud", 15, 0));
        tb.getColumns().add(new Column("Longitud", 15, 0));
        tb.getColumns().add(new Column("Factura", 15, 0));
        tb.getColumns().add(new Column("Anulada", 15, 0));
        tb.getColumns().add(new Column("Liquidada", 15, 0));

        Object[][] res = new Object[lstAnul.size()][17];
        res = lstAnul.toArray(res);
        tb.setData(res);
        if (tb.getData() != null && tb.getData().length > 0) {
            rep.getTables().add(tb);
        }
        return rep;
    }

    private static void addError(Map<String, Integer> errors, String e) {
        if (errors.containsKey(e)) {
            int i = errors.get(e) + 1;
            errors.remove(e);
            errors.put(e, i);
        } else {
            errors.put(e, 1);
        }
    }

    private static Integer getAsInt(Object o) throws Exception {
        if (o == null) {
            return null;
        } else if (o instanceof Double) {
            return ((Double) o).intValue();
        } else if (o instanceof String) {
            String s = o.toString().trim();
            return Integer.valueOf(s);
        }
        throw new Exception("No se puede convertir " + o + " en entero.");
    }

    private static boolean isAllWhite(Object[] row) {
        for (Object cell : row) {
            if (cell != null && !cell.toString().isEmpty()) {
                return false;
            }
        }
        return true;
    }

}
