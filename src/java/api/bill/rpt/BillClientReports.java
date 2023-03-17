package api.bill.rpt;

import api.bill.model.BillBuildFactor;
import api.bill.model.BillBuilding;
import api.bill.model.BillClientFactor;
import api.bill.model.BillClientTank;
import api.bill.model.BillInstance;
import api.bill.model.BillSpan;
import java.math.BigDecimal;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import model.billing.constants.Accounts;
import model.billing.constants.Transactions;
import utilities.Dates;
import utilities.MySQLPreparedQuery;
import utilities.MySQLQuery;
import utilities.cast;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.SummaryRow;
import utilities.mysqlReport.Table;

public class BillClientReports {

    // LISTADO DE CLIENTES TANQUES
    public static MySQLReport getClientsTank(Integer buildId, boolean active, Connection conn) throws Exception {
        BillBuilding build = null;
        if (buildId != null) {
            build = new BillBuilding().select(buildId, conn);
        }

        MySQLReport rep = new MySQLReport("Listado de Clientes", "Edificio: " + (build != null ? build.name : "Todos"), "clientes", MySQLQuery.now(conn));
        rep.getSubTitles().add("Activo: " + (active ? "Si" : "No"));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().get(0).setWrap(true);
        rep.setVerticalFreeze(5);
        rep.setZoomFactor(80);
        rep.setShowNumbers(true);
        rep.setMultiRowTitles(true);
        Table tbl = new Table("Listado de Clientes");
        tbl.getColumns().add(new Column("Edificio", 40, 0));
        tbl.getColumns().add(new Column("Nombre", 25, 0));
        tbl.getColumns().add(new Column("Apellido", 20, 0));
        tbl.getColumns().add(new Column("Documento", 18, 0));
        tbl.getColumns().add(new Column("Apartamento", 15, 0));
        tbl.getColumns().add(new Column("Instalación", 20, 0));
        tbl.getColumns().add(new Column("Medidor", 20, 0));
        tbl.getColumns().add(new Column("Teléfono", 20, 0));
        tbl.getColumns().add(new Column("Lista de Precios", 20, 0));
        tbl.getColumns().add(new Column("Revisión de Instalación", 15, 0));
        tbl.getColumns().add(new Column("Fecha Revisión de Instalación", 15, 0));
        tbl.getColumns().add(new Column("Fecha Mínima", 15, 0));
        tbl.getColumns().add(new Column("Fecha de Vencimiento", 15, 0));
        MySQLQuery mq = new MySQLQuery("SELECT "
                + "b.`name`, "
                + "bt.first_name,"
                + "bt.last_name, "
                + "bt.doc, "
                + "bt.apartment, "
                + "bt.num_install, "
                + "(SELECT `number` FROM bill_meter WHERE client_id = bt.id ORDER BY start_span_id DESC LIMIT 1), "
                + "bt.phones,"
                + "(SELECT name FROM bill_price_list WHERE id = (SELECT list_id FROM bill_client_list WHERE span_id = (SELECT MAX(span_id) FROM bill_client_list WHERE client_id = bt.id) AND client_id = bt.id)),"
                + "(SELECT MAX(sbict.code) FROM bill_inst_check bic INNER JOIN sigma.bill_inst_check_type sbict ON bic.type_id = sbict.id INNER JOIN sigma.bill_inst_inspector bii ON bic.inspector_id = bii.id WHERE bic.client_id = bt.id LIMIT 1) AS 'Tipo Revisión Instalación Interna', "
                + "(SELECT bic.chk_date FROM bill_inst_check bic INNER JOIN sigma.bill_inst_check_type sbict2 ON bic.type_id = sbict2.id INNER JOIN sigma.bill_inst_inspector bii ON bic.inspector_id = bii.id WHERE bic.client_id = bt.id AND sbict2.code=(SELECT MAX(sbict.code) FROM bill_inst_check bic INNER JOIN sigma.bill_inst_check_type sbict ON bic.type_id = sbict.id INNER JOIN sigma.bill_inst_inspector bii ON bic.inspector_id = bii.id WHERE bic.client_id = bt.id LIMIT 1) LIMIT 1) AS 'Fecha Revisión', "
                + "NULL AS 'Fecha Minima', "
                + "(SELECT DATE_ADD(bic.chk_date, INTERVAL 5 YEAR) FROM bill_inst_check bic INNER JOIN sigma.bill_inst_check_type sbict2 ON bic.type_id = sbict2.id INNER JOIN sigma.bill_inst_inspector bii ON bic.inspector_id = bii.id WHERE bic.client_id = bt.id AND sbict2.code=(SELECT MAX(sbict.code) FROM bill_inst_check bic INNER JOIN sigma.bill_inst_check_type sbict ON bic.type_id = sbict.id INNER JOIN sigma.bill_inst_inspector bii ON bic.inspector_id = bii.id WHERE bic.client_id = bt.id LIMIT 1) LIMIT 1) AS 'Fecha Vencimiento' "
                + "FROM bill_client_tank AS bt "
                + "INNER JOIN bill_building AS b ON b.id = bt.building_id "
                + "WHERE bt.active = " + active + " "
                + (buildId != null ? "AND b.id = " + buildId + " " : "")
                + "ORDER BY b.name,CONCAT(bt.first_name,' ',bt.last_name) ");

        Object[][] data = mq.getRecords(conn);
        tbl.setData(data);
        String review=null;
        for(Object[] i:data){
            if(i[9]!=null){
                if(i[9].toString().equalsIgnoreCase("1")){
                    review="Inicial";
                }
                else if(i[9].toString().equalsIgnoreCase("2")){
                    review="Periódica";
                }
                else if(i[9].toString().equalsIgnoreCase("3")){
                    review="Periódica fuera del plazo máximo de certificación";
                }
                else if(i[9].toString().equalsIgnoreCase("4")){
                    review="Reforma";
                }
                else if(i[9].toString().equalsIgnoreCase("5")){
                    review="Solicitud por parte del usuario";
                }
                i[9]=review;
            }
            if(i[12]!=null){
                SimpleDateFormat normalDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date dateMin=getMinDate((Date) i[12]);
                i[11]=normalDateFormat.format(dateMin);
            }
        }
        if (data != null && data.length > 0) {
            tbl.setData(data);
            rep.getTables().add(tbl);
        }
        return rep;
    }
    public static Date getMinDate(Date maxDate){
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(maxDate);
        gc.add(GregorianCalendar.MONTH, -5);
        Date minDate = Dates.trimDate(gc.getTime());
        return minDate;
    }
    // LISTADO DE TODOS LOS CLIENTES TANQUES
    public static MySQLReport getAllClientsTank(Connection conn) throws Exception {
        MySQLReport rep;
        rep = new MySQLReport("Listado de Clientes", "", "Clientes", MySQLQuery.now(conn));
        String tanques;
        String dbName = "SELECT db, c.name FROM bill_instance i " 
                + "INNER JOIN city c ON c.id = i.city_id " 
                + "WHERE `type` = 'tank'";
        MySQLQuery mq = new MySQLQuery(dbName);
        Object[][] dataBase = mq.getRecords(conn);
        List<DataBase> dataBaseList = new DataBase().getData(dataBase);
        Object[][] miTankData = new Object[0][0];
        List<Item> items = new ArrayList<>();
        for(int i = 0; i < dataBaseList.size(); i++){
            DataBase db = dataBaseList.get(i);
            int cantidadClientes = new MySQLQuery("SELECT COUNT(*) FROM " + db.nameDataBase + ".bill_client_tank").getAsInteger(conn);
            if(cantidadClientes > 0){
                tanques = "SELECT otc.active, bct.id AS IdCliente, " 
                    + "otc.folder_name CodigoInterno, " 
                    + "et.serial AS SerialTanque, " 
                    + "bct.doc AS NroDocumento, " 
                    + "bct.first_name AS NombresCliente, " 
                    + "bct.last_name AS ApellidosCliente, " 
                    + "bct.phones AS TelefonosCliente, " 
                    + "bct.mail AS CorreoCliente, " 
                    + "bct.num_install AS NumeroInstalacion, " 
                    + "bct.apartment AS Apartamento, " 
                    + "bb.id AS IdEdificio, " 
                    + "bb.name AS NombreEdificio, " 
                    + "(CASE WHEN bct.active = 1 THEN 'Si' WHEN bct.active = 0 THEN 'No' END) AS Activo, " 
                    + "bct.creation_date AS FechaCreación, " 
                    + "bct.notes AS Notas " 
                    + "FROM " + db.nameDataBase + ".bill_client_tank AS bct " 
                    + "INNER JOIN " + db.nameDataBase + ".bill_building AS bb ON bb.id = bct.building_id " 
                    + "LEFT JOIN sigma.ord_tank_client AS otc ON otc.mirror_id = bb.id " 
                    + "LEFT JOIN sigma.est_tank AS et ON et.client_id = otc.id GROUP BY bct.num_install";
                MySQLQuery tank = new MySQLQuery(tanques);
                Object[][] tanks = tank.getRecords(conn);
                List<Tanks> tanksList = new Tanks().getData(tanks);
                Object[][] tanksData = new Object[tanksList.size()][16];
                
                for(int t = 0; t < tanksList.size(); t++){
                    Tanks myT = tanksList.get(t);
                    tanksData[t][0] = myT.instance != null ? db.name : "";
                    tanksData[t][1] = myT.idClient;
                    tanksData[t][2] = myT.codInt;
                    tanksData[t][3] = myT.serialTank;
                    tanksData[t][4] = myT.doc;
                    tanksData[t][5] = myT.firstName;
                    tanksData[t][6] = myT.lastName;
                    tanksData[t][7] = myT.phones;
                    tanksData[t][8] = myT.mail;
                    tanksData[t][9] = myT.numInstall;
                    tanksData[t][10] = myT.apartment;
                    tanksData[t][11] = myT.idBuilding;
                    tanksData[t][12] = myT.nameBuilding;
                    tanksData[t][13] = myT.active;
                    tanksData[t][14] = myT.creationDate;
                    tanksData[t][15] = myT.notes;
                    miTankData = tanksData;
                }
                for (Object[] row : miTankData) {
                    items.add(myGetData(row));
                }
            }
        }
        
        Object[][] defData = new Object[items.size()][16];
        for (int i = 0; i < items.size(); i++) {
            defData[i][0] = items.get(i).instance;
            defData[i][1] = items.get(i).idClient;
            defData[i][2] = items.get(i).codInt;
            defData[i][3] = items.get(i).serialTank;
            defData[i][4] = items.get(i).doc;
            defData[i][5] = items.get(i).firstName;
            defData[i][6] = items.get(i).lastName;
            defData[i][7] = items.get(i).phones;
            defData[i][8] = items.get(i).mail;
            defData[i][9] = items.get(i).numInstall;
            defData[i][10] = items.get(i).apartment;
            defData[i][11] = items.get(i).idBuilding;
            defData[i][12] = items.get(i).nameBuilding;
            defData[i][13] = items.get(i).active;
            defData[i][14] = items.get(i).creationDate;
            defData[i][15] = items.get(i).notes;
        }
        
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#.00"));
        rep.getFormats().get(0).setWrap(true);
        rep.setVerticalFreeze(5);
        rep.setZoomFactor(80);
        rep.setShowNumbers(true);
        
        Table tbl = new Table("Listado de Clientes");
        tbl.getColumns().add(new Column("Instancia", 12, 0));
        tbl.getColumns().add(new Column("ID Cliente", 15, 0));
        tbl.getColumns().add(new Column("Código Interno", 18, 0));
        tbl.getColumns().add(new Column("Serial Tanque", 15, 0));
        tbl.getColumns().add(new Column("Documento", 18, 0));
        tbl.getColumns().add(new Column("Nombre", 25, 0));
        tbl.getColumns().add(new Column("Apellido", 25, 0));
        tbl.getColumns().add(new Column("Teléfono", 20, 0));
        tbl.getColumns().add(new Column("Correo", 25, 0));
        tbl.getColumns().add(new Column("Instalación", 20, 0));
        tbl.getColumns().add(new Column("Apartamento", 15, 0));
        tbl.getColumns().add(new Column("ID Edificio", 15, 0));
        tbl.getColumns().add(new Column("Edificio", 40, 0));
        tbl.getColumns().add(new Column("Activo", 10, 0));
        tbl.getColumns().add(new Column("Fecha Creación", 20, 0));
        tbl.getColumns().add(new Column("Notas", 40, 0));
        
        tbl.setData(defData);
        
        if (tbl.getData().length > 0 ) {
            rep.getTables().add(tbl);
        }
        return rep;
    }

    // LISTADO DE CLIENTES REDES
    public static MySQLReport getClientsNet(Integer neighId, boolean active, Connection conn) throws Exception {

        String neighName = new MySQLQuery("SELECT name FROM sigma.neigh WHERE id = " + neighId).getAsString(conn);

        MySQLReport rep = new MySQLReport("Listado de Clientes", "Barrio: " + (neighName != null ? neighName : "Todos"), "clientes", MySQLQuery.now(conn));
        rep.getSubTitles().add("Activo: " + (active ? "Si" : "No"));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().get(0).setWrap(true);
        rep.setVerticalFreeze(5);
        rep.setZoomFactor(80);
        rep.setShowNumbers(true);
        rep.setMultiRowTitles(true);
        Table tbl = new Table("Listado de Clientes");
        tbl.getColumns().add(new Column("Barrio", 40, 0));
        tbl.getColumns().add(new Column("Nombre", 25, 0));
        tbl.getColumns().add(new Column("Apellido", 20, 0));
        tbl.getColumns().add(new Column("Documento", 18, 0));
        tbl.getColumns().add(new Column("Direccion", 15, 0));
        tbl.getColumns().add(new Column("Instalación", 20, 0));
        tbl.getColumns().add(new Column("Medidor", 20, 0));
        tbl.getColumns().add(new Column("Teléfono", 20, 0));
        MySQLQuery mq = new MySQLQuery("SELECT "
                + "b.`name`, "
                + "bt.first_name,"
                + "bt.last_name, "
                + "bt.doc, "
                + "bt.address, "
                + "bt.num_install, "
                + "(SELECT `number` FROM bill_meter WHERE client_id = bt.id ORDER BY start_span_id DESC LIMIT 1), "
                + "bt.phones "
                + "FROM bill_client_tank AS bt "
                + "LEFT JOIN sigma.neigh AS b ON b.id = bt.neigh_id "
                + "WHERE bt.active = " + active + " "
                + (neighId != null ? "AND b.id = " + neighId + " " : "")
                + "ORDER BY b.name,CONCAT(bt.first_name,' ',bt.last_name) ");

        Object[][] data = mq.getRecords(conn);
        tbl.setData(data);
        if (data != null && data.length > 0) {
            tbl.setData(data);
            rep.getTables().add(tbl);
        }
        return rep;

    }

    // LECTURAS DE CLIENTES TANQUES EN UN PERIODO
    public static MySQLReport getReadingsTankByClient(BillInstance bi, int spanId, Integer buildingId, boolean notRead, Connection conn) throws Exception {
        BillSpan sp = new BillSpan().select(spanId, conn);
        SimpleDateFormat shortDateFormat = new SimpleDateFormat("d MMMM yyyy");
        String periodo = "Periodo de Consumo: " + shortDateFormat.format(sp.beginDate) + "  -  " + shortDateFormat.format(sp.endDate);
        MySQLReport rep = new MySQLReport("Listado de Consumos - " + bi.name, periodo, "Consumos", MySQLQuery.now(conn));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.000"));//1
        rep.setZoomFactor(85);

        Table tb = new Table("model");
        tb.getColumns().add(new Column("Num Medidor", 20, 0));
        tb.getColumns().add(new Column("Nombres", 30, 0));
        tb.getColumns().add(new Column("Num Instalación", 17, 0));
        tb.getColumns().add(new Column("Lec Actual", 15, 1));
        tb.getColumns().add(new Column("Lec Anterior", 15, 1));
        tb.getColumns().add(new Column("Cons. m3", 15, 1));
        tb.getColumns().add(new Column("Cons. gal", 15, 1));
        tb.getColumns().add(new Column("Cons. kg", 15, 1));
        tb.getColumns().add(new Column("fact", 10, 1));
        tb.getColumns().add(new Column("gal * fact", 15, 1));
        tb.getColumns().add(new Column("kg * fact", 15, 1));

        MySQLPreparedQuery factorQ = BillBuildFactor.getFactorQuery(conn);
        MySQLPreparedQuery clientFactorQ = BillClientFactor.getFactorQuery(conn);
        List<BillBuilding> buildings = new ArrayList<>();

        if (buildingId == null) {
            buildings = BillBuilding.getAllBuildings(conn);
        } else {
            BillBuilding building = new BillBuilding().select(buildingId, conn);
            buildings.add(building);
        }

        int curSpan = new MySQLQuery("SELECT count(*) FROM bill_reading where span_id = ?1").setParam(1, spanId).getAsInteger(conn);
        int bkSpan = new MySQLQuery("SELECT count(*) FROM bill_reading_bk where span_id = ?1").setParam(1, spanId).getAsInteger(conn);
        int curPrevSpan = new MySQLQuery("SELECT count(*) FROM bill_reading where span_id = ?1").setParam(1, spanId - 1).getAsInteger(conn);
        int bkPrevSpan = new MySQLQuery("SELECT count(*) FROM bill_reading_bk where span_id = ?1").setParam(1, spanId - 1).getAsInteger(conn);

        String curTable;
        String prevTable;

        if (curSpan > 0 && bkSpan == 0) {
            curTable = "bill_reading";
        } else if (curSpan == 0 && bkSpan > 0) {
            curTable = "bill_reading_bk";
        } else {
            throw new Exception("No se hallaron datos");
        }

        if (curPrevSpan > 0 && bkPrevSpan == 0) {
            prevTable = "bill_reading";
        } else if (curPrevSpan == 0 && bkPrevSpan > 0) {
            prevTable = "bill_reading_bk";
        }
        else if(curSpan > 0 && bkSpan == 0 && curPrevSpan == 0 && bkPrevSpan == 0){
            prevTable = "bill_reading";
        }
        else {
            throw new Exception("No se hallaron datos");
        }

        String qs = "SELECT IFNULL((SELECT `number` FROM bill_meter WHERE client_id = c.id ORDER BY start_span_id DESC LIMIT 1), ''), num_install, first_name, COALESCE(c.last_name,''), apartment, r1.reading, r2.reading, r1.last_reading, c.id "
                + "FROM bill_client_tank c "
                + "left join " + curTable + " r1 on r1.client_tank_id =c.id and r1.span_id=" + spanId + " "
                + "left join " + prevTable + " r2 on r2.client_tank_id =c.id and r2.span_id=" + (spanId - 1) + "   "
                + "where c.building_id = ?1 AND "
                + "c.active = 1 ";
        if (notRead) {
            qs += " and r1.reading is null ";
        }
        qs += "order by num_install ASC ";
        MySQLPreparedQuery q = new MySQLPreparedQuery(qs, conn);

        for (BillBuilding bl : buildings) {
            BigDecimal buildFac = BillBuildFactor.getFactor(spanId, bl.id, factorQ);
            q.setParameter(1, bl.id);
            Object[][] data = q.getRecords();
            if (data.length > 0) {
                Table bTable = new Table(bl.oldId + " " + bl.name + " " + bl.address + " <Lec:" + data.length + ">");
                rep.getTables().add(bTable);
                bTable.setColumns(tb.getColumns());

                for (Object[] dataRow : data) {
                    Integer clientId = MySQLQuery.getAsInteger(dataRow[8]);
                    BigDecimal clientFac = BillClientFactor.getFactor(spanId, clientId, clientFactorQ);
                    Object[] row = new Object[11];
                    bTable.addRow(row);
                    row[0] = dataRow[0] != null ? (String) dataRow[0] : "";//medidor
                    row[1] = (dataRow[2] != null ? (String) dataRow[2] : "") + " " + (dataRow[3] != null ? (String) dataRow[3] : "");//nombres
                    row[2] = dataRow[1] != null ? (String) dataRow[1] : "";//instalación
                    row[3] = dataRow[5] != null ? (BigDecimal) dataRow[5] : null;//lec1
                    if (dataRow[5] != null) {
                        row[4] = dataRow[7] != null ? (BigDecimal) dataRow[7] : null;//lec2
                    } else {
                        row[4] = dataRow[6] != null ? (BigDecimal) dataRow[6] : null;//lec2
                    }
                    if (row[3] != null && row[4] != null) {
                        BigDecimal m3Cons = ((BigDecimal) row[3]).subtract((BigDecimal) row[4]);
                        row[5] = m3Cons;
                        row[6] = m3Cons.multiply(sp.getM3ToGalKte());
                        row[7] = m3Cons.multiply(sp.getM3ToGalKte()).multiply(sp.galToKgKte);
                        row[8] = (clientFac == BigDecimal.ZERO ? buildFac : clientFac);
                        row[9] = m3Cons.multiply(sp.getM3ToGalKte()).multiply((clientFac == BigDecimal.ZERO ? buildFac : clientFac));
                        row[10] = m3Cons.multiply(sp.getM3ToGalKte()).multiply(sp.galToKgKte).multiply((clientFac == BigDecimal.ZERO ? buildFac : clientFac));
                    } else {
                        row[5] = null;
                        row[6] = null;
                        row[7] = null;
                        row[8] = (clientFac == BigDecimal.ZERO ? buildFac : clientFac);
                        row[9] = null;
                        row[10] = null;
                    }
                }
            }
        }
        return rep;

    }

    // LECTURAS DE CLIENTES REDES EN UN PERIODO
    public static MySQLReport getReadingsNetByClient(BillInstance bi, int spanId, boolean notRead, Connection conn) throws Exception {
        BillSpan sp = new BillSpan().select(spanId, conn);
        SimpleDateFormat shortDateFormat = new SimpleDateFormat("d MMMM yyyy");
        String periodo = "Periodo de Consumo: " + shortDateFormat.format(sp.beginDate) + "  -  " + shortDateFormat.format(sp.endDate);
        MySQLReport rep = new MySQLReport("Listado de Consumos - " + bi.name, periodo, "Consumos", MySQLQuery.now(conn));

        if (!sp.readingsClosed) {
            rep.getSubTitles().add("INFORMACIÓN PROVISIONAL, CAPTURA DE LECTURAS EN CURSO");
        }

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.000"));//1
        rep.setZoomFactor(85);
        rep.setMultiRowTitles(2);

        Table tb = new Table("model");
        tb.getColumns().add(new Column("Código", 17, 0));
        tb.getColumns().add(new Column("Medidor", 20, 0));
        tb.getColumns().add(new Column("Sector", 15, 0));
        tb.getColumns().add(new Column("Estrato", 15, 0));
        tb.getColumns().add(new Column("Nombres", 30, 0));
        tb.getColumns().add(new Column("Dirección", 40, 0));
        tb.getColumns().add(new Column("Lectura Anterior m3", 15, 1));
        tb.getColumns().add(new Column("Lectura Actual m3", 15, 1));
        tb.getColumns().add(new Column("Consumo Medido m3", 15, 1));
        tb.getColumns().add(new Column("Factor Corrección", 15, 1));
        tb.getColumns().add(new Column("Consumo Corregido m3", 15, 1));
        tb.getColumns().add(new Column("Menor o Igual a " + new DecimalFormat("#,##0.000").format(sp.vitalCons), 15, 1));
        tb.getColumns().add(new Column("Sobre " + new DecimalFormat("#,##0.000").format(sp.vitalCons), 15, 1));
        tb.getColumns().add(new Column("Novedad", 40, 0));

        int curSpan = new MySQLQuery("SELECT count(*) FROM bill_reading where span_id = ?1").setParam(1, spanId).getAsInteger(conn);
        int bkSpan = new MySQLQuery("SELECT count(*) FROM bill_reading_bk where span_id = ?1").setParam(1, spanId).getAsInteger(conn);
        int curPrevSpan = new MySQLQuery("SELECT count(*) FROM bill_reading where span_id = ?1").setParam(1, spanId - 1).getAsInteger(conn);
        int bkPrevSpan = new MySQLQuery("SELECT count(*) FROM bill_reading_bk where span_id = ?1").setParam(1, spanId - 1).getAsInteger(conn);

        String curTable;
        String prevTable;

        if (curSpan > 0 && bkSpan == 0) {
            curTable = "bill_reading";
        } else if (curSpan == 0 && bkSpan > 0) {
            curTable = "bill_reading_bk";
        } else {
            throw new Exception("No se hallaron datos");
        }

        if (curPrevSpan > 0 && bkPrevSpan == 0) {
            prevTable = "bill_reading";
        } else if (curPrevSpan == 0 && bkPrevSpan > 0) {
            prevTable = "bill_reading_bk";
        } else {
            //para el primer periodo no van a haber datos el periodo anterior ni el la normal ni en la bk
            prevTable = "bill_reading";
        }

        MySQLPreparedQuery rebillQ = new MySQLPreparedQuery("SELECT orig_beg_read, orig_end_read FROM bill_clie_rebill WHERE active AND client_id = ?1 AND error_span_id = " + spanId, conn);

        String qs = "SELECT "
                + "code, "
                + "IFNULL((SELECT `number` FROM bill_meter WHERE client_id = c.id ORDER BY start_span_id DESC LIMIT 1), ''), "
                + "CONCAT(first_name, IF(c.last_name IS NOT NULL, CONCAT(' ', c.last_name), '')), "
                + "CONCAT(address, ' ' , IFNULL(n.name, '')), "
                + "IFNULL(r1.last_reading, r2.reading), "
                + "r1.reading, "
                + "cau.meter_factor,"
                + "IF(r1.critical_reading IS NOT NULL, 'Desviación Crítica', f.name),"
                + "UPPER(cau.sector),"
                + "cau.stratum, "
                + "c.id "
                + "FROM bill_client_tank c "
                + "INNER join " + curTable + " r1 on r1.client_tank_id = c.id and r1.span_id = " + spanId + " "
                + "LEFT join " + prevTable + " r2 on r2.client_tank_id = c.id and r2.span_id = " + (spanId - 1) + "  "
                + "INNER JOIN bill_clie_cau cau ON cau.client_id = c.id AND cau.span_id = " + spanId + " "
                + "LEFT JOIN sigma.neigh n ON n.id = c.neigh_id "
                + "LEFT JOIN sigma.bill_reading_fault f ON f.id = r1.fault_id "
                + "where c.active = 1 ";
        if (notRead) {
            qs += " and r1.reading is null ";
        }
        qs += "order by code ASC ";
        MySQLQuery q = new MySQLQuery(qs);
        Object[][] data = q.getRecords(conn);
        if (data.length > 0) {
            Table bTable = new Table(bi.name + " <" + data.length + " Lecturas>");
            rep.getTables().add(bTable);
            bTable.setColumns(tb.getColumns());

            for (Object[] dataRow : data) {

                Integer clientId = cast.asInt(dataRow, 10);
                rebillQ.setParameter(1, clientId);
                Object[] rebillRow = rebillQ.getRecord();
                BigDecimal last;
                BigDecimal cur;

                if (rebillRow == null) {
                    last = cast.asBigDecimal(dataRow, 4, false);
                    cur = cast.asBigDecimal(dataRow, 5, false);
                } else {
                    last = cast.asBigDecimal(rebillRow, 0, false);
                    cur = cast.asBigDecimal(rebillRow, 1, false);
                }

                BigDecimal meterFactor = cast.asBigDecimal(dataRow, 6);
                BigDecimal factor = meterFactor.compareTo(BigDecimal.ONE) != 0 ? meterFactor : sp.fadj;

                BigDecimal m3Cons = null;
                BigDecimal corrCons = null;
                if (last != null && cur != null) {
                    m3Cons = cur.subtract(last);
                    corrCons = m3Cons.multiply(factor);
                }

                Object[] row = new Object[14];
                bTable.addRow(row);
                row[0] = cast.asString(dataRow, 0);
                row[1] = cast.asString(dataRow, 1);

                row[2] = cast.asString(dataRow, 8);
                row[3] = cast.asString(dataRow, 9);

                row[4] = cast.asString(dataRow, 2);
                row[5] = cast.asString(dataRow, 3);
                row[6] = last;
                row[7] = cur;
                row[8] = m3Cons;
                row[9] = factor;
                if (m3Cons != null && corrCons != null) {
                    row[10] = corrCons;
                    if (corrCons.compareTo(sp.vitalCons) <= 0) {
                        row[11] = corrCons;
                        row[12] = BigDecimal.ZERO;
                    } else {
                        row[11] = sp.vitalCons;
                        row[12] = corrCons.subtract(sp.vitalCons);
                    }
                }
                row[13] = cast.asString(dataRow, 7);
            }
        }
        return rep;

    }

    //reporte de debitos y créditos
    public static MySQLReport getAccountsBalance(BillInstance inst, int spanId, Connection conn) throws Exception {

        SimpleDateFormat longDateFormat = new SimpleDateFormat("d MMMM yyyy");
        SimpleDateFormat shortDateFormat = new SimpleDateFormat("MMMM yyyy");
        MySQLReport rep = new MySQLReport("Débitos y Créditos - " + inst.name, "", "Hoja 1", MySQLQuery.now(conn));
        BillSpan span = new BillSpan().select(spanId, conn);
        rep.getSubTitles().add("Recaudado entre: Entrega de Cupones -  " + longDateFormat.format(span.limitDate));
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(span.limitDate);
        rep.setZoomFactor(80);
        gc.set(GregorianCalendar.DAY_OF_MONTH, 5);
        rep.getSubTitles().add("Causación de: " + shortDateFormat.format(gc.getTime()));
        rep.getSubTitles().add("Consumos realizados entre: " + longDateFormat.format(span.beginDate) + "  -  " + longDateFormat.format(span.endDate));

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$ #,##0.00"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//2

        Table tb = new Table("Balance de Cuentas");
        tb.getColumns().add(new Column("Concepto", 30, 0));//0
        tb.getColumns().add(new Column("Saldo Anterior", 27, 1));//1
        tb.getColumns().add(new Column("Débito", 27, 1));//2
        tb.getColumns().add(new Column("Crédito", 27, 1));//3
        tb.getColumns().add(new Column("Pendiente", 27, 1));//4

        MySQLPreparedQuery debAntQ = new MySQLPreparedQuery(" SELECT SUM(t.value) FROM bill_transaction t WHERE t.account_deb_id = ?1 AND t.bill_span_id < " + spanId, conn);
        MySQLPreparedQuery credAntQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction t WHERE t.account_cred_id = ?1 AND t.bill_span_id < " + spanId, conn);

        MySQLPreparedQuery debTotalQ = new MySQLPreparedQuery(" SELECT SUM(t.value) FROM bill_transaction t WHERE t.account_deb_id = ?1 AND t.bill_span_id <= " + spanId, conn);
        MySQLPreparedQuery credTotalQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction t WHERE t.account_cred_id = ?1 AND t.bill_span_id <= " + spanId, conn);

        MySQLPreparedQuery debActQ = new MySQLPreparedQuery(" SELECT SUM(t.value) FROM bill_transaction t WHERE t.account_deb_id = ?1 AND t.bill_span_id = " + spanId, conn);
        MySQLPreparedQuery credActQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction t WHERE t.account_cred_id = ?1 AND t.bill_span_id = " + spanId + " and t.account_deb_id <> " + Accounts.C_CAR_GLP, conn);

        MySQLPreparedQuery cartAdjQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction t WHERE t.account_cred_id = " + Accounts.E_AJUST + " AND t.bill_span_id = " + spanId + " and t.account_deb_id = " + Accounts.C_CAR_GLP, conn);
        MySQLPreparedQuery billAdjQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction t WHERE t.account_cred_id = " + Accounts.E_AJUST + " AND t.bill_span_id = " + spanId + " and t.account_deb_id = " + Accounts.BANCOS, conn);

        MySQLPreparedQuery transQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction t WHERE t.trans_type_id  = ?1 AND t.bill_span_id = " + spanId, conn);

        ///cuentas de mes
        a(tb, debActQ, credActQ, Accounts.C_CONS);
        BigDecimal contrib = BigDecimal.ZERO;
        if (inst.isNetInstance()) {
            a(tb, debActQ, credActQ, Accounts.C_CONS_SUBS);
            contrib = a(tb, debActQ, credActQ, Accounts.C_CONTRIB);
            a(tb, debActQ, credActQ, Accounts.C_REBILL);
        }
        a(tb, debActQ, credActQ, Accounts.C_BASI);
        a(tb, debActQ, credActQ, Accounts.C_CUOTA_SER_CLI_GLP);
        a(tb, debActQ, credActQ, Accounts.C_CUOTA_SER_CLI_SRV);
        a(tb, debActQ, credActQ, Accounts.C_CUOTA_INT_CRE);
        a(tb, debActQ, credActQ, Accounts.C_CUOTA_SER_EDI);
        a(tb, debActQ, credActQ, Accounts.C_CUOTA_FINAN_DEU);
        a(tb, debActQ, credActQ, Accounts.C_RECON);

        Object[] row = new Object[5];
        row[0] = "Ajuste a la Decena Cartera";
        row[1] = BigDecimal.ZERO;
        row[2] = cartAdjQ.getAsBigDecimal(true);
        row[3] = BigDecimal.ZERO;
        row[4] = row[2];
        tb.addRow(row);

        ////////////cartera
        b(tb, credAntQ, debAntQ, credActQ, debActQ, Accounts.C_CAR_GLP);
        b(tb, credAntQ, debAntQ, credActQ, debActQ, Accounts.C_CAR_SRV);
        b(tb, credAntQ, debAntQ, credActQ, debActQ, Accounts.C_CAR_FINAN_DEU);
        b(tb, credAntQ, debAntQ, credActQ, debActQ, Accounts.C_CAR_CONTRIB);
        b(tb, credAntQ, debAntQ, credActQ, debActQ, Accounts.C_CAR_INTE_CRE);
        b(tb, credAntQ, debAntQ, credActQ, debActQ, Accounts.C_CAR_OLD);
        b(tb, credAntQ, debAntQ, credActQ, debActQ, Accounts.C_FINAN_DEU_POR_COBRAR);

        ////////////intereses
        b(tb, credAntQ, debAntQ, credActQ, debActQ, Accounts.C_INT_GLP);
        b(tb, credAntQ, debAntQ, credActQ, debActQ, Accounts.C_INT_SRV);
        b(tb, credAntQ, debAntQ, credActQ, debActQ, Accounts.C_INT_FINAN_DEU);
        b(tb, credAntQ, debAntQ, credActQ, debActQ, Accounts.C_INT_CONTRIB);
        b(tb, credAntQ, debAntQ, credActQ, debActQ, Accounts.C_INT_OLD);

        row = new Object[5];
        row[0] = "Ajuste a la Decena";
        row[1] = BigDecimal.ZERO;
        row[2] = BigDecimal.ZERO;
        row[3] = billAdjQ.getAsBigDecimal(true);
        row[4] = BigDecimal.ZERO;
        tb.addRow(row);

        tb.setSummaryRow(new SummaryRow("Totales", 1));
        rep.getTables().add(tb);

        ///CAUSACIÓN DE INTERESES
        MySQLPreparedQuery cauIntAntQ = new MySQLPreparedQuery(" SELECT SUM(t.value) FROM bill_transaction t WHERE t.account_deb_id = ?1 AND t.bill_span_id = " + (spanId - 1) + " AND t.trans_type_id = " + Transactions.CAUSA_INTE_GLP, conn);
//
        Table tb1 = new Table("Interés Causados Periodo Anterior");
        tb1.getColumns().add(new Column("Concepto", 30, 0));//0
        tb1.getColumns().add(new Column("Valor", 27, 1));//1
        rep.getTables().add(tb1);

        //////////////////////////////////////
        cauIntAntQ.setParameter(1, Accounts.C_INT_GLP);
        row = new Object[2];
        row[0] = Accounts.accNames.get(Accounts.C_INT_GLP);
        row[1] = cauIntAntQ.getAsBigDecimal(true);
        tb1.addRow(row);

        cauIntAntQ.setParameter(1, Accounts.C_INT_SRV);
        row = new Object[2];
        row[0] = Accounts.accNames.get(Accounts.C_INT_SRV);
        row[1] = cauIntAntQ.getAsBigDecimal(true);
        tb1.addRow(row);

        cauIntAntQ.setParameter(1, Accounts.C_INT_OLD);
        row = new Object[2];
        row[0] = Accounts.accNames.get(Accounts.C_INT_OLD);
        row[1] = cauIntAntQ.getAsBigDecimal(true);
        tb1.addRow(row);

        ///PROCEDENCIA DE LOS CREDITOS
        transQ.setParameter(1, Transactions.PAGO_BANCO);
        BigDecimal paidInBank = transQ.getAsBigDecimal(true);

        transQ.setParameter(1, Transactions.CAUSA_SUBSIDY);
        BigDecimal subsidy = transQ.getAsBigDecimal(true);

        transQ.setParameter(1, Transactions.N_CREDIT);
        BigDecimal paidWithNotes = transQ.getAsBigDecimal(true);

        transQ.setParameter(1, Transactions.N_AJ_CREDIT);
        paidWithNotes.add(transQ.getAsBigDecimal(true));

        transQ.setParameter(1, Transactions.PAGO_ANTICIP);
        BigDecimal paidAntic = transQ.getAsBigDecimal(true);

        transQ.setParameter(1, Transactions.DTO_EDIF);
        BigDecimal dto = transQ.getAsBigDecimal(true);

        Table tb2 = new Table("Procedencia de los Créditos");
        tb2.getColumns().add(new Column("Concepto", 30, 0));//0
        tb2.getColumns().add(new Column("Valor", 27, 1));//1
        rep.getTables().add(tb2);

        row = new Object[2];
        row[0] = "Bancos";
        row[1] = paidInBank;
        tb2.addRow(row);

        row = new Object[2];
        row[0] = "Subsidios";
        row[1] = subsidy;
        tb2.addRow(row);

        row = new Object[2];
        row[0] = "Notas Crédito";
        row[1] = paidWithNotes;
        tb2.addRow(row);

        row = new Object[2];
        row[0] = "Saldos a Favor";
        row[1] = paidAntic;
        tb2.addRow(row);

        row = new Object[2];
        row[0] = "Descuentos";
        row[1] = dto;
        tb2.addRow(row);

        //SALDOS A FAVOR
        Table tb3 = new Table("Saldos a Favor");
        tb3.getColumns().add(new Column("Concepto", 30, 0));//0
        tb3.getColumns().add(new Column("Valor", 27, 1));//1
        rep.getTables().add(tb3);

        row = new Object[2];
        credActQ.setParameter(1, Accounts.C_ANTICIP);
        row[0] = "Saldos a Favor Creados del Periodo";
        BigDecimal totalNewAnticExp = credActQ.getAsBigDecimal(true);
        row[1] = totalNewAnticExp;
        tb3.addRow(row);

        row = new Object[2];
        credTotalQ.setParameter(1, Accounts.C_ANTICIP);
        debTotalQ.setParameter(1, Accounts.C_ANTICIP);
        row[0] = "Saldos a Favor Disponible";
        row[1] = debTotalQ.getAsBigDecimal(true).subtract(credTotalQ.getAsBigDecimal(true)).negate();
        tb3.addRow(row);

        Table tb4 = new Table("Detalle de los Saldos a Favor del Periodo");
        tb4.getColumns().add(new Column("Tipo", 30, 0));//0
        tb4.getColumns().add(new Column("Valor", 27, 1));//1
        rep.getTables().add(tb4);

        tb4.setData(new MySQLQuery("SELECT t.name, SUM(tr.value) FROM bill_antic_note n "
                + "INNER JOIN sigma.bill_antic_note_type t ON t.id = n.type_id "
                + "INNER JOIN bill_transaction tr ON tr.doc_id = n.id AND tr.doc_type = 'pag_antic' "
                + "WHERE n.active AND n.bill_span_id = ?1 "
                + "GROUP BY t.id ORDER BY t.name").setParam(1, spanId).getRecords(conn));
        tb4.setSummaryRow(new SummaryRow("Total", 1));
        tb4.getData();

        BigDecimal totalNewAntic = BigDecimal.ZERO;
        for (Object[] data : tb4.getData()) {
            totalNewAntic = totalNewAntic.add(MySQLQuery.getAsBigDecimal(data[1], true));
        }

        if (totalNewAntic.compareTo(totalNewAnticExp) != 0) {
            throw new Exception("El detalle de los saldos a favor no coincide.");
        }

        if (inst.isNetInstance()) {
            //PAGOS ANTICIPADOS
            Table tbNet = new Table("Subsidios y Contribuciones");
            tbNet.getColumns().add(new Column("Concepto", 30, 0));//0
            tbNet.getColumns().add(new Column("Valor", 27, 1));//1
            rep.getTables().add(tbNet);

            tbNet.addRow(new Object[]{"Subsidios", subsidy});
            tbNet.addRow(new Object[]{"Contribuciones", contrib});
            tbNet.addRow(new Object[]{"Diferencia", subsidy.subtract(contrib)});

        }
        return rep;

    }

    private static BigDecimal a(Table tb, MySQLPreparedQuery debActQ, MySQLPreparedQuery credActQ, int accId) throws Exception {
        debActQ.setParameter(1, accId);
        credActQ.setParameter(1, accId);
        Object[] row = new Object[5];
        row[0] = Accounts.accNames.get(accId);
        row[1] = BigDecimal.ZERO;
        row[2] = debActQ.getAsBigDecimal(true);
        row[3] = credActQ.getAsBigDecimal(true);
        row[4] = ((BigDecimal) row[2]).subtract((BigDecimal) row[3]);
        tb.addRow(row);
        return (BigDecimal) row[4];
    }

    private static void b(Table tb, MySQLPreparedQuery credAntQ, MySQLPreparedQuery debAntQ, MySQLPreparedQuery credActQ, MySQLPreparedQuery debActQ, int accId) throws Exception {
        credAntQ.setParameter(1, accId);
        debAntQ.setParameter(1, accId);
        credActQ.setParameter(1, accId);
        debActQ.setParameter(1, accId);
        Object[] row = new Object[5];
        row[0] = Accounts.accNames.get(accId);
        row[1] = debAntQ.getAsBigDecimal(true).subtract(credAntQ.getAsBigDecimal(true));
        row[2] = debActQ.getAsBigDecimal(true);
        row[3] = credActQ.getAsBigDecimal(true);
        row[4] = ((BigDecimal) row[1]).add((BigDecimal) row[2]).subtract((BigDecimal) row[3]);
        tb.addRow(row);
    }

    public static MySQLReport getClientsBalance(int spanId, BillInstance inst, Connection conn) throws Exception {
        String cityName = inst.name;

        MySQLReport rep = new MySQLReport("Balance de Clientes - " + cityName, "", "Hoja 1", MySQLQuery.now(conn));

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$ #,##0.00"));//1

        List<Integer> accIds = new ArrayList<>();
        accIds.add(Accounts.C_CONS);
        if (inst.isNetInstance()) {
            accIds.add(Accounts.C_CONS_SUBS);
            accIds.add(Accounts.C_CONTRIB);
            accIds.add(Accounts.C_REBILL);
        }
        accIds.add(Accounts.C_BASI);
        accIds.add(Accounts.C_RECON);
        accIds.add(Accounts.C_CUOTA_SER_CLI_GLP);
        accIds.add(Accounts.C_CUOTA_SER_CLI_SRV);
        accIds.add(Accounts.C_CUOTA_SER_EDI);
        accIds.add(Accounts.C_CUOTA_FINAN_DEU);
        accIds.add(Accounts.C_CUOTA_INT_CRE);
        accIds.add(Accounts.C_CAR_GLP);
        accIds.add(Accounts.C_CAR_SRV);
        accIds.add(Accounts.C_CAR_FINAN_DEU);
        accIds.add(Accounts.C_CAR_CONTRIB);
        accIds.add(Accounts.C_CAR_INTE_CRE);
        accIds.add(Accounts.C_CAR_OLD);
        accIds.add(Accounts.C_INT_GLP);
        accIds.add(Accounts.C_INT_SRV);
        accIds.add(Accounts.C_INT_FINAN_DEU);
        accIds.add(Accounts.C_INT_CONTRIB);
        accIds.add(Accounts.C_INT_OLD);
        accIds.add(Accounts.C_ANTICIP);
        accIds.add(Accounts.C_FINAN_DEU_POR_COBRAR);

        int colw = 20;
        Table tb = new Table("Balance");
        tb.getColumns().add(new Column("Num. Inst", 15, 0));//0
        for (int i = 0; i < accIds.size(); i++) {
            tb.getColumns().add(new Column(Accounts.accNames.get(accIds.get(i)), colw, 1));//1    
        }
        tb.getColumns().add(new Column(Accounts.accNames.get(Accounts.BANCOS), colw, 1));//11
        rep.setZoomFactor(80);

        String noCausa = " t.trans_type_id <> " + Transactions.CAUSA_CART + " AND t.trans_type_id <> " + Transactions.CAUSA_INTE_GLP;
        MySQLPreparedQuery credQ = new MySQLPreparedQuery("select sum(t.value) from bill_transaction as t where t.account_cred_id = ?1 and t.cli_tank_id = ?2 and (t.bill_span_id < " + spanId + " or (t.bill_span_id = " + spanId + " and " + noCausa + "))", conn);
        MySQLPreparedQuery debQ = new MySQLPreparedQuery(" select sum(t.value) from bill_transaction as t where t.account_deb_id  = ?1 and t.cli_tank_id = ?2 and (t.bill_span_id < " + spanId + " or (t.bill_span_id = " + spanId + " and " + noCausa + "))", conn);

        MySQLPreparedQuery credActQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_cred_id = ?1 AND t.cli_tank_id = ?2 AND t.bill_span_id = " + spanId, conn);
        MySQLPreparedQuery debActQ = new MySQLPreparedQuery("SELECT SUM(t.value) FROM bill_transaction as t WHERE t.account_deb_id = ?1 AND t.cli_tank_id = ?2 AND t.bill_span_id = " + spanId, conn);

        if (inst.isTankInstance()) {
            List<BillBuilding> buildings = BillBuilding.getAll(conn);
            for (BillBuilding bl : buildings) {
                BillClientTank[] clients = BillClientTank.getByBuildId(bl.id, false, conn);
                Table bTable = new Table(bl.oldId + " " + bl.name + " " + bl.address + " " + clients.length + " Clientes");
                rep.getTables().add(bTable);
                bTable.setColumns(tb.getColumns());
                Object[][] data = new Object[clients.length][];
                bTable.setData(data);

                for (int i = 0; i < clients.length; i++) {
                    BillClientTank c = clients[i];
                    data[i] = getClientBalanceRow(c, accIds, credQ, debQ, credActQ, debActQ);
                }
                bTable.setSummaryRow(new SummaryRow("Totales", 1));
            }
        } else {
            BillClientTank[] clients = BillClientTank.getAll(true, conn);
            Table bTable = new Table(tb);
            rep.getTables().add(bTable);
            bTable.setData(new Object[clients.length][]);
            for (int i = 0; i < clients.length; i++) {
                BillClientTank c = clients[i];
                bTable.getData()[i] = getClientBalanceRow(c, accIds, credQ, debQ, credActQ, debActQ);
            }
            bTable.setSummaryRow(new SummaryRow("Totales", 1));
        }
        return rep;
    }

    public static MySQLReport getBalanceTest(Connection conn) throws Exception {

        MySQLReport rep = new MySQLReport("Balance de Clientes", "", "Hoja 1", MySQLQuery.now(conn));

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$ #,##0.00"));//1

        List<Integer> accIds = new ArrayList<>();
        accIds.add(Accounts.C_CONS);
        accIds.add(Accounts.C_CONS_SUBS);
        accIds.add(Accounts.C_BASI);
        accIds.add(Accounts.C_CONTRIB);
        accIds.add(Accounts.C_REBILL);
        accIds.add(Accounts.C_RECON);
        accIds.add(Accounts.C_CUOTA_SER_CLI_GLP);
        accIds.add(Accounts.C_CUOTA_SER_CLI_SRV);
        accIds.add(Accounts.C_CUOTA_SER_EDI);
        accIds.add(Accounts.C_CUOTA_FINAN_DEU);
        accIds.add(Accounts.C_CUOTA_INT_CRE);
        accIds.add(Accounts.C_CAR_GLP);
        accIds.add(Accounts.C_CAR_SRV);
        accIds.add(Accounts.C_CAR_FINAN_DEU);
        accIds.add(Accounts.C_CAR_CONTRIB);
        accIds.add(Accounts.C_CAR_INTE_CRE);
        accIds.add(Accounts.C_CAR_OLD);
        accIds.add(Accounts.C_INT_GLP);
        accIds.add(Accounts.C_INT_SRV);
        accIds.add(Accounts.C_INT_FINAN_DEU);
        accIds.add(Accounts.C_INT_CONTRIB);
        accIds.add(Accounts.C_INT_OLD);
        accIds.add(Accounts.C_ANTICIP);
        accIds.add(Accounts.C_FINAN_DEU_POR_COBRAR);

        int colw = 20;
        Table tb = new Table("Balance");
        tb.getColumns().add(new Column("Num. Inst", 15, 0));//0
        for (int i = 0; i < accIds.size(); i++) {
            tb.getColumns().add(new Column(Accounts.accNames.get(accIds.get(i)), colw, 1));//1    
        }
        rep.setZoomFactor(80);
        MySQLPreparedQuery credQ = new MySQLPreparedQuery("select sum(t.value) from bill_transaction as t where t.account_cred_id = ?1 and t.cli_tank_id = ?2", conn);
        MySQLPreparedQuery debQ = new MySQLPreparedQuery(" select sum(t.value) from bill_transaction as t where t.account_deb_id  = ?1 and t.cli_tank_id = ?2", conn);

        List<BillInstance> instances = BillInstance.getAll(conn);
        for (int i = 0; i < instances.size(); i++) {
            BillInstance inst = instances.get(i);
            inst.useInstance(conn);
            BillClientTank[] clients = BillClientTank.getAll(true, conn);
            Table bTable = new Table(tb);

            for (BillClientTank cl : clients) {
                Object[] row = new Object[accIds.size() + 1];
                row[0] = cl.numInstall;
                boolean add = false;
                for (int k = 0; k < accIds.size(); k++) {
                    int accountId = accIds.get(k);
                    credQ.setParameter(1, accountId);
                    credQ.setParameter(2, cl.id);

                    debQ.setParameter(1, accountId);
                    debQ.setParameter(2, cl.id);
                    BigDecimal balance = debQ.getAsBigDecimal(true).subtract(credQ.getAsBigDecimal(true));
                    row[k + 1] = balance;

                    switch (accountId) {
                        case Accounts.C_ANTICIP:
                            if (balance.compareTo(BigDecimal.ZERO) > 0) {
                                add = true;
                            }
                            break;
                        case Accounts.C_FINAN_DEU_POR_COBRAR:
                            if (balance.compareTo(new BigDecimal(-1)) < 0) {
                                add = true;
                            }
                            break;
                        default:
                            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                                add = true;
                            }
                            break;
                    }
                }

                if (add) {
                    bTable.addRow(row);
                }
            }
            if (!bTable.isEmpty()) {
                bTable.setTitle(inst.name);
                bTable.setSummaryRow(new SummaryRow("Totales", 1));
                rep.getTables().add(bTable);
            }
        }
        if (rep.getTables().isEmpty()) {
            throw new Exception("No se hallaron datos");
        }
        return rep;
    }
    
    
    // LISTADO DE TODOS LOS CLIENTES TANQUES
    public static MySQLReport getTransactionalClients(String date, String period, Connection conn) throws Exception {
        MySQLReport rep;
        rep = new MySQLReport("Transaccional Facturacion Tanques", "", "Transaccional", MySQLQuery.now(conn));
        rep.getSubTitles().add("Periodo de: " + period);
        String transactional;
        String dbName = "SELECT db, c.name FROM bill_instance i " 
                + "INNER JOIN city c ON c.id = i.city_id " 
                + "WHERE `type` = 'tank'";
        MySQLQuery mq = new MySQLQuery(dbName);
        Object[][] dataBase = mq.getRecords(conn);
        List<DataBase> dataBaseList = new DataBase().getData(dataBase);
        Object[][] myData = new Object[0][0];
        List<Item2> myItems = new ArrayList<>();
        for(int i = 0; i < dataBaseList.size(); i++){
            DataBase db = dataBaseList.get(i);
            int cantidadClientes = new MySQLQuery("SELECT COUNT(*) FROM " + db.nameDataBase + ".bill_client_tank").getAsInteger(conn);
            if(cantidadClientes > 0){
                transactional = "SELECT bt.active, " 
                        + "otc.folder_name AS CodigoInterno, " 
                        + "b.`name` NombreEdificio, " 
                        + "b.address AS DireccionEdificio, " 
                        + "bt.apartment AS ApartamentoCliente, " 
                        + "bt.num_install AS NumeroInstalacionCliente, " 
                        + "(SELECT `number` FROM " + db.nameDataBase + ".bill_meter WHERE client_id = bt.id ORDER BY start_span_id DESC LIMIT 1) AS MedidorCliente, " 
                        + "ne.name AS BarrioEdificio, " 
                        + "se.name AS SectorEdificio, " 
                        + "otc.lat AS LatitudEdificio, " 
                        + "otc.lon AS LongitudEdificio, " 
                        + "otc.contact_name AS ContactoEdificio, " 
                        + "otc.contact_mail AS EmailContactoEdificio, " 
                        + "tk.serial AS SerieTanque, " 
                        + "tk.id_code AS CodigoTanque, " 
                        + "tk.capacity AS CapacidadTanque, " 
                        + "tk.par_beg AS InicioParcialTanque, " 
                        + "tk.last_par AS UltParcialTanque, " 
                        + "tk.tot_beg AS InicioTotalTanque, " 
                        + "tk.last_tot AS UltTotalTanque, " 
                        + "loc.description AS UbicacionTanque, " 
                        + "tk.certificate AS CertificadoTanque, " 
                        + "tk.certificate_date AS FechaCertificadoTanque, " 
                        + "tk.factory AS FabricanteTanque, " 
                        + "tk.num_users AS NroUsuariosTanque, " 
                        + "tk.description AS NotasTanque, " 
                        + "(CASE WHEN tk.ctr_type = 'emp' THEN 'Empresa' WHEN tk.ctr_type = 'cli' THEN 'Cliente' END) AS PropiedadTanque, " 
                        + "bt.first_name AS NombreCliente, " 
                        + "bt.last_name AS ApellidoCliente, " 
                        + "IFNULL(CONCAT(bt.first_name,' ',bt.last_name),bt.first_name) AS NombresCLiente, " 
                        + "bt.doc AS DocumentoCliente, " 
                        + "bt.phones AS TelefonoCliente, " 
                        + "bt.mail AS CorreoCliente, " 
                        + "(SELECT name FROM " + db.nameDataBase + ".bill_price_list WHERE id = (SELECT list_id FROM " + db.nameDataBase + ".bill_client_list WHERE span_id = (SELECT MAX(span_id) FROM " + db.nameDataBase + ".bill_client_list WHERE client_id = bt.id) AND client_id = bt.id)) AS ListaDePrecios, " 
                        + "(SELECT br.reading FROM " + db.nameDataBase + ".bill_reading AS br INNER JOIN " + db.nameDataBase + ".bill_span AS bs ON bs.id = br.span_id WHERE br.client_tank_id = bt.id AND bs.cons_month = '" + date + "') AS LecturaActualCliente, " 
                        + "(SELECT br.last_reading FROM " + db.nameDataBase + ".bill_reading AS br INNER JOIN " + db.nameDataBase + ".bill_span AS bs ON bs.id = br.span_id WHERE br.client_tank_id = bt.id AND bs.cons_month = '" + date + "') AS UltimaLecturaCliente, " 
                        + "(SELECT (br.reading - br.last_reading) FROM " + db.nameDataBase + ".bill_reading AS br INNER JOIN " + db.nameDataBase + ".bill_span AS bs ON bs.id = br.span_id WHERE br.client_tank_id = bt.id AND bs.cons_month = '" + date + "') AS Consumo, " 
                        + "(SELECT bbp.value FROM " + db.nameDataBase + ".bill_bill_pres AS bbp WHERE bbp.bill_id = (SELECT bb.id FROM " + db.nameDataBase + ".bill_bill AS bb INNER JOIN " + db.nameDataBase + ".bill_span AS bs ON bs.id = bb.bill_span_id WHERE bb.client_tank_id = bt.id AND bs.cons_month = '" + date + "' ORDER BY bb.id DESC LIMIT 1) AND bbp.label = 'Consumo') AS ValorConsumo " 
                        + "FROM " + db.nameDataBase + ".bill_client_tank AS bt " 
                        + "LEFT JOIN " + db.nameDataBase + ".bill_building AS b ON b.id = bt.building_id " 
                        + "LEFT JOIN sigma.ord_tank_client AS otc ON otc.mirror_id = b.id " 
                        + "LEFT JOIN sigma.neigh AS ne ON ne.id = otc.neigh_id " 
                        + "LEFT JOIN sigma.sector AS se ON se.id = ne.sector_id " 
                        + "LEFT JOIN " + db.nameDataBase + ".bill_reading AS br ON br.client_tank_id = bt.id " 
                        + "LEFT JOIN sigma.est_tank AS tk ON tk.client_id = otc.id " 
                        + "LEFT JOIN sigma.est_tank_location AS loc ON loc.id = tk.location_id " 
                        + "WHERE bt.active = 1 " 
                        + "GROUP BY MedidorCliente " 
                        + "ORDER BY b.name, CONCAT(bt.first_name,' ',bt.last_name)";
                MySQLQuery myTransactional = new MySQLQuery(transactional);
                Object[][] myTransactionals = myTransactional.getRecords(conn);
                List<Transactional> transactionalList = new Transactional().getData(myTransactionals);
                Object[][] transactionalData = new Object[transactionalList.size()][38];
                for(int t = 0; t < transactionalList.size(); t++){
                    Transactional myT = transactionalList.get(t);
                    transactionalData[t][0] = myT.instance != null ? db.name : "";
                    transactionalData[t][1] = myT.codInt;
                    transactionalData[t][2] = myT.nameBuilding;
                    transactionalData[t][3] = myT.addressBuilding;
                    transactionalData[t][4] = myT.apartment;
                    transactionalData[t][5] = myT.numInstall;
                    transactionalData[t][6] = myT.meterClient;
                    transactionalData[t][7] = myT.neighBuilding;
                    transactionalData[t][8] = myT.sectorBuilding;
                    transactionalData[t][9] = myT.latBuilding;
                    transactionalData[t][10] = myT.lonBuilding;
                    transactionalData[t][11] = myT.contactBuilding;
                    transactionalData[t][12] = myT.mailBuilding;
                    transactionalData[t][13] = myT.serieTank;
                    transactionalData[t][14] = myT.codTank;
                    transactionalData[t][15] = myT.capacityTank;
                    transactionalData[t][16] = myT.parBegTank;
                    transactionalData[t][17] = myT.lastParTank;
                    transactionalData[t][18] = myT.totBegTank;
                    transactionalData[t][19] = myT.lastTotTank;
                    transactionalData[t][20] = myT.locationTank;
                    transactionalData[t][21] = myT.certificateTank;
                    transactionalData[t][22] = myT.certDateTank;
                    transactionalData[t][23] = myT.factoryTank;
                    transactionalData[t][24] = myT.numUsers;
                    transactionalData[t][25] = myT.notesTank;
                    transactionalData[t][26] = myT.propertyTank;
                    transactionalData[t][27] = myT.firstNameClient;
                    transactionalData[t][28] = myT.lastNameClient;
                    transactionalData[t][29] = myT.nameClient;
                    transactionalData[t][30] = myT.docClient;
                    transactionalData[t][31] = myT.phonesClient;
                    transactionalData[t][32] = myT.mailClient;
                    transactionalData[t][33] = myT.listPrices;
                    transactionalData[t][34] = myT.actualReading;
                    transactionalData[t][35] = myT.lastReading;
                    transactionalData[t][36] = myT.consum;
                    transactionalData[t][37] = myT.consumPrice;
                    myData = transactionalData;
                }
                for (Object[] row : myData) {
                    myItems.add(myGetData2(row));
                }
            }    
        }
        Object[][] defData = new Object[myItems.size()][38];
        for (int i = 0; i < myItems.size(); i++) {
            defData[i][0] = myItems.get(i).instance;
            defData[i][1] = myItems.get(i).codInt;
            defData[i][2] = myItems.get(i).nameBuilding;
            defData[i][3] = myItems.get(i).addressBuilding;
            defData[i][4] = myItems.get(i).apartment;
            defData[i][5] = myItems.get(i).numInstall;
            defData[i][6] = myItems.get(i).meterClient;
            defData[i][7] = myItems.get(i).neighBuilding;
            defData[i][8] = myItems.get(i).sectorBuilding;
            defData[i][9] = myItems.get(i).latBuilding;
            defData[i][10] = myItems.get(i).lonBuilding;
            defData[i][11] = myItems.get(i).contactBuilding;
            defData[i][12] = myItems.get(i).mailBuilding;
            defData[i][13] = myItems.get(i).serieTank;
            defData[i][14] = myItems.get(i).codTank;
            defData[i][15] = myItems.get(i).capacityTank;
            defData[i][16] = myItems.get(i).parBegTank;
            defData[i][17] = myItems.get(i).lastParTank;
            defData[i][18] = myItems.get(i).totBegTank;
            defData[i][19] = myItems.get(i).lastTotTank;
            defData[i][20] = myItems.get(i).locationTank;
            defData[i][21] = myItems.get(i).certificateTank;
            defData[i][22] = myItems.get(i).certDateTank;
            defData[i][23] = myItems.get(i).factoryTank;
            defData[i][24] = myItems.get(i).numUsers;
            defData[i][25] = myItems.get(i).notesTank;
            defData[i][26] = myItems.get(i).propertyTank;
            defData[i][27] = myItems.get(i).firstNameClient;
            defData[i][28] = myItems.get(i).lastNameClient;
            defData[i][29] = myItems.get(i).nameClient;
            defData[i][30] = myItems.get(i).docClient;
            defData[i][31] = myItems.get(i).phonesClient;
            defData[i][32] = myItems.get(i).mailClient;
            defData[i][33] = myItems.get(i).listPrices;
            defData[i][34] = myItems.get(i).actualReading;
            defData[i][35] = myItems.get(i).lastReading;
            defData[i][36] = myItems.get(i).consum;
            defData[i][37] = myItems.get(i).consumPrice;    
        }
        
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#.000"));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.00"));//2
        rep.getFormats().get(0).setWrap(true);
        rep.setVerticalFreeze(5);
        rep.setZoomFactor(80);
        rep.setShowNumbers(true);
        
        Table tbl = new Table("Transaccional de Clientes");
        tbl.getColumns().add(new Column("Instancia", 12, 0));
        tbl.getColumns().add(new Column("Código Interno", 18, 0));
        tbl.getColumns().add(new Column("Edificio", 40, 0));
        tbl.getColumns().add(new Column("Dirección Edificio", 25, 0));
        tbl.getColumns().add(new Column("Apartamento", 15, 0));
        tbl.getColumns().add(new Column("Instalación", 20, 0));
        tbl.getColumns().add(new Column("Medidor", 15, 0));
        tbl.getColumns().add(new Column("Barrio Edificio", 25, 0));
        tbl.getColumns().add(new Column("Sector Edificio", 25, 0));
        tbl.getColumns().add(new Column("Latitud", 15, 0));
        tbl.getColumns().add(new Column("Longitud", 15, 0));
        tbl.getColumns().add(new Column("Contacto Edificio", 25, 0));
        tbl.getColumns().add(new Column("Correo Edificio", 25, 0));
        tbl.getColumns().add(new Column("Serie Tanque", 15, 0));
        tbl.getColumns().add(new Column("Código Tanque", 18, 0));
        tbl.getColumns().add(new Column("Cap. Tanque", 15, 0));
        tbl.getColumns().add(new Column("Ini. Par. Tanque", 20, 0));
        tbl.getColumns().add(new Column("Ult. Par. Tanque", 20, 0));
        tbl.getColumns().add(new Column("Ini. Tot. Tanque", 20, 0));
        tbl.getColumns().add(new Column("Ult. Tot. Tanque", 20, 0));
        tbl.getColumns().add(new Column("Ubicación Tanque", 20, 0));
        tbl.getColumns().add(new Column("Cert. Tanque", 20, 0));
        tbl.getColumns().add(new Column("Fecha Cert. Tanque", 22, 0));
        tbl.getColumns().add(new Column("Fab. Tanque", 20, 0));
        tbl.getColumns().add(new Column("Nro. Usu. Tanque", 20, 0));
        tbl.getColumns().add(new Column("Notas Tanque", 40, 0));
        tbl.getColumns().add(new Column("Propiedad Tanque", 20, 0));
        tbl.getColumns().add(new Column("Nombre Cliente", 25, 0));
        tbl.getColumns().add(new Column("Apellido Cliente", 25, 0));
        tbl.getColumns().add(new Column("Nombres Cliente", 40, 0));
        tbl.getColumns().add(new Column("Documento Cliente", 20, 0));
        tbl.getColumns().add(new Column("Teléfono Cliente", 20, 0));
        tbl.getColumns().add(new Column("Correo Cliente", 25, 0));
        tbl.getColumns().add(new Column("Lista de Precios", 20, 0));
        tbl.getColumns().add(new Column("Lec. Act. Cliente", 20, 1));
        tbl.getColumns().add(new Column("Ult. Lec. Cliente", 20, 1));
        tbl.getColumns().add(new Column("Consumo", 15, 1));
        tbl.getColumns().add(new Column("Valor Consumo", 20, 2));
        
        tbl.setData(defData);
        
        if (tbl.getData().length > 0 ) {
            rep.getTables().add(tbl);
        }
        return rep;
    }

    private static Object[] getClientBalanceRow(BillClientTank cl, List<Integer> accIds, MySQLPreparedQuery credQ, MySQLPreparedQuery debQ, MySQLPreparedQuery credActQ, MySQLPreparedQuery debActQ) throws Exception {
        Object[] row = new Object[accIds.size() + 2];
        row[0] = cl.numInstall;
        for (int k = 0; k < accIds.size(); k++) {
            int accountId = accIds.get(k);
            credQ.setParameter(1, accountId);
            credQ.setParameter(2, cl.id);

            debQ.setParameter(1, accountId);
            debQ.setParameter(2, cl.id);
            BigDecimal balance = debQ.getAsBigDecimal(true).subtract(credQ.getAsBigDecimal(true));
            row[k + 1] = balance;
        }

        int accountId = Accounts.BANCOS;
        credActQ.setParameter(1, accountId);
        credActQ.setParameter(2, cl.id);

        debActQ.setParameter(1, accountId);
        debActQ.setParameter(2, cl.id);
        BigDecimal balance = debActQ.getAsBigDecimal(true).subtract(credActQ.getAsBigDecimal(true));
        row[row.length - 1] = balance;
        return row;
    }

    private static class DataBase {
        String nameDataBase;
        String name;
        
        public List<DataBase> getData(Object[][] data){
            List<DataBase> dataBaseData = new ArrayList<DataBase>();
            for(Object[] row : data){
                DataBase d = new DataBase();
                d.nameDataBase = MySQLQuery.getAsString(row[0]);
                d.name = MySQLQuery.getAsString(row[1]);
                dataBaseData.add(d);
            }
            return dataBaseData;
        }  
    }
    
    private static class Tanks {
        String instance;
        Integer idClient;
        String codInt;
        String serialTank;
        String doc;
        String firstName;
        String lastName;
        String phones;
        String mail;
        String numInstall;
        String apartment;
        Integer idBuilding;
        String nameBuilding;
        String active;
        Date creationDate;
        String notes;
        
        public List<Tanks> getData(Object[][] data){
            List<Tanks> tankData = new ArrayList<Tanks>();
            for(Object[] row : data){
                Tanks t = new Tanks();
                t.instance = MySQLQuery.getAsString(row[0]);
                t.idClient = MySQLQuery.getAsInteger(row[1]);
                t.codInt = MySQLQuery.getAsString(row[2]);
                t.serialTank = MySQLQuery.getAsString(row[3]);
                t.doc = MySQLQuery.getAsString(row[4]);
                t.firstName = MySQLQuery.getAsString(row[5]);
                t.lastName = MySQLQuery.getAsString(row[6]);
                t.phones = MySQLQuery.getAsString(row[7]);
                t.mail = MySQLQuery.getAsString(row[8]);
                t.numInstall = MySQLQuery.getAsString(row[9]);
                t.apartment = MySQLQuery.getAsString(row[10]);
                t.idBuilding = MySQLQuery.getAsInteger(row[11]);
                t.nameBuilding = MySQLQuery.getAsString(row[12]);
                t.active = MySQLQuery.getAsString(row[13]);
                t.creationDate = MySQLQuery.getAsDate(row[14]);
                t.notes = MySQLQuery.getAsString(row[15]);
                tankData.add(t);
            }
            return tankData;
        }
    }
    
    private static class Item {
        String instance;
        Integer idClient;
        String codInt;
        String serialTank;
        String doc;
        String firstName;
        String lastName;
        String phones;
        String mail;
        String numInstall;
        String apartment;
        Integer idBuilding;
        String nameBuilding;
        String active;
        Date creationDate;
        String notes;
    }
    
    private static Item myGetData(Object[] row) {
        Item item = new Item();
        item.instance = MySQLQuery.getAsString(row[0]);
        item.idClient = MySQLQuery.getAsInteger(row[1]);
        item.codInt = MySQLQuery.getAsString(row[2]);
        item.serialTank = MySQLQuery.getAsString(row[3]);
        item.doc = MySQLQuery.getAsString(row[4]);
        item.firstName = MySQLQuery.getAsString(row[5]);
        item.lastName = MySQLQuery.getAsString(row[6]);
        item.phones = MySQLQuery.getAsString(row[7]);
        item.mail = MySQLQuery.getAsString(row[8]);
        item.numInstall = MySQLQuery.getAsString(row[9]);
        item.apartment = MySQLQuery.getAsString(row[10]);
        item.idBuilding = MySQLQuery.getAsInteger(row[11]);
        item.nameBuilding = MySQLQuery.getAsString(row[12]);
        item.active = MySQLQuery.getAsString(row[13]);
        item.creationDate = MySQLQuery.getAsDate(row[14]);
        item.notes = MySQLQuery.getAsString(row[15]);
        return item;
    }

    private static class Item2 {
        String instance;
        String codInt;
        String nameBuilding;
        String addressBuilding;
        String apartment;
        String numInstall;
        String meterClient;
        String neighBuilding;
        String sectorBuilding;
        String latBuilding;
        String lonBuilding;
        String contactBuilding;
        String mailBuilding;
        String serieTank;
        String codTank;
        Integer capacityTank;
        Date parBegTank;
        Date lastParTank;
        Date totBegTank;
        Date lastTotTank;
        String locationTank;
        String certificateTank;
        Date certDateTank;
        String factoryTank;
        Integer numUsers;
        String notesTank;
        String propertyTank;
        String firstNameClient;
        String lastNameClient;
        String nameClient;
        String docClient;
        String phonesClient;
        String mailClient;
        String listPrices;
        BigDecimal actualReading;
        BigDecimal lastReading;
        BigDecimal consum;
        BigDecimal consumPrice;
    }
    
    private static Item2 myGetData2(Object[] row) {
        Item2 item = new Item2();
        item.instance = MySQLQuery.getAsString(row[0]);
        item.codInt = MySQLQuery.getAsString(row[1]);
        item.nameBuilding = MySQLQuery.getAsString(row[2]);
        item.addressBuilding = MySQLQuery.getAsString(row[3]);
        item.apartment = MySQLQuery.getAsString(row[4]);
        item.numInstall = MySQLQuery.getAsString(row[5]);
        item.meterClient = MySQLQuery.getAsString(row[6]);
        item.neighBuilding = MySQLQuery.getAsString(row[7]);
        item.sectorBuilding = MySQLQuery.getAsString(row[8]);
        item.latBuilding = MySQLQuery.getAsString(row[9]);
        item.lonBuilding = MySQLQuery.getAsString(row[10]);
        item.contactBuilding = MySQLQuery.getAsString(row[11]);
        item.mailBuilding = MySQLQuery.getAsString(row[12]);
        item.serieTank = MySQLQuery.getAsString(row[13]);
        item.codTank = MySQLQuery.getAsString(row[14]);
        item.capacityTank = MySQLQuery.getAsInteger(row[15]);
        item.parBegTank = MySQLQuery.getAsDate(row[16]);
        item.lastParTank = MySQLQuery.getAsDate(row[17]);
        item.totBegTank = MySQLQuery.getAsDate(row[18]);
        item.lastTotTank = MySQLQuery.getAsDate(row[19]);
        item.locationTank = MySQLQuery.getAsString(row[20]);
        item.certificateTank = MySQLQuery.getAsString(row[21]);
        item.certDateTank = MySQLQuery.getAsDate(row[22]);
        item.factoryTank = MySQLQuery.getAsString(row[23]);
        item.numUsers = MySQLQuery.getAsInteger(row[24]);
        item.notesTank = MySQLQuery.getAsString(row[25]);
        item.propertyTank = MySQLQuery.getAsString(row[26]);
        item.firstNameClient = MySQLQuery.getAsString(row[27]);
        item.lastNameClient = MySQLQuery.getAsString(row[28]);
        item.nameClient = MySQLQuery.getAsString(row[29]);
        item.docClient = MySQLQuery.getAsString(row[30]);
        item.phonesClient = MySQLQuery.getAsString(row[31]);
        item.mailClient = MySQLQuery.getAsString(row[32]);
        item.listPrices = MySQLQuery.getAsString(row[33]);
        item.actualReading = MySQLQuery.getAsBigDecimal(row[34], false);
        item.lastReading = MySQLQuery.getAsBigDecimal(row[35], false);
        item.consum = MySQLQuery.getAsBigDecimal(row[36], false);
        item.consumPrice = MySQLQuery.getAsBigDecimal(row[37], false);
        return item;  
    }

    private static class Transactional {
        String instance;
        String codInt;
        String nameBuilding;
        String addressBuilding;
        String apartment;
        String numInstall;
        String meterClient;
        String neighBuilding;
        String sectorBuilding;
        String latBuilding;
        String lonBuilding;
        String contactBuilding;
        String mailBuilding;
        String serieTank;
        String codTank;
        Integer capacityTank;
        Date parBegTank;
        Date lastParTank;
        Date totBegTank;
        Date lastTotTank;
        String locationTank;
        String certificateTank;
        Date certDateTank;
        String factoryTank;
        Integer numUsers;
        String notesTank;
        String propertyTank;
        String firstNameClient;
        String lastNameClient;
        String nameClient;
        String docClient;
        String phonesClient;
        String mailClient;
        String listPrices;
        BigDecimal actualReading;
        BigDecimal lastReading;
        BigDecimal consum;
        BigDecimal consumPrice;
        
        public List<Transactional> getData(Object[][] data){
            List<Transactional> transactionalData = new ArrayList<Transactional>();
            for(Object[] row : data){
                Transactional t = new Transactional();
                t.instance = MySQLQuery.getAsString(row[0]);
                t.codInt = MySQLQuery.getAsString(row[1]);
                t.nameBuilding = MySQLQuery.getAsString(row[2]);
                t.addressBuilding = MySQLQuery.getAsString(row[3]);
                t.apartment = MySQLQuery.getAsString(row[4]);
                t.numInstall = MySQLQuery.getAsString(row[5]);
                t.meterClient = MySQLQuery.getAsString(row[6]);
                t.neighBuilding = MySQLQuery.getAsString(row[7]);
                t.sectorBuilding = MySQLQuery.getAsString(row[8]);
                t.latBuilding = MySQLQuery.getAsString(row[9]);
                t.lonBuilding = MySQLQuery.getAsString(row[10]);
                t.contactBuilding = MySQLQuery.getAsString(row[11]);
                t.mailBuilding = MySQLQuery.getAsString(row[12]);
                t.serieTank = MySQLQuery.getAsString(row[13]);
                t.codTank = MySQLQuery.getAsString(row[14]);
                t.capacityTank = MySQLQuery.getAsInteger(row[15]);
                t.parBegTank = MySQLQuery.getAsDate(row[16]);
                t.lastParTank = MySQLQuery.getAsDate(row[17]);
                t.totBegTank = MySQLQuery.getAsDate(row[18]);
                t.lastTotTank = MySQLQuery.getAsDate(row[19]);
                t.locationTank = MySQLQuery.getAsString(row[20]);
                t.certificateTank = MySQLQuery.getAsString(row[21]);
                t.certDateTank = MySQLQuery.getAsDate(row[22]);
                t.factoryTank = MySQLQuery.getAsString(row[23]);
                t.numUsers = MySQLQuery.getAsInteger(row[24]);
                t.notesTank = MySQLQuery.getAsString(row[25]);
                t.propertyTank = MySQLQuery.getAsString(row[26]);
                t.firstNameClient = MySQLQuery.getAsString(row[27]);
                t.lastNameClient = MySQLQuery.getAsString(row[28]);
                t.nameClient = MySQLQuery.getAsString(row[29]);
                t.docClient = MySQLQuery.getAsString(row[30]);
                t.phonesClient = MySQLQuery.getAsString(row[31]);
                t.mailClient = MySQLQuery.getAsString(row[32]);
                t.listPrices = MySQLQuery.getAsString(row[33]);
                t.actualReading = MySQLQuery.getAsBigDecimal(row[34], false);
                t.lastReading = MySQLQuery.getAsBigDecimal(row[35], false);
                t.consum = MySQLQuery.getAsBigDecimal(row[36], false);
                t.consumPrice = MySQLQuery.getAsBigDecimal(row[37], false);
                transactionalData.add(t);
            }
            return transactionalData;
        }
        
        
    }
    
    

}
