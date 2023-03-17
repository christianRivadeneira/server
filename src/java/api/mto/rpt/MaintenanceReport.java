package api.mto.rpt;

import api.GridResult;
import api.MySQLCol;
import api.mto.model.MtoCfg;
import api.sys.model.Agency;
import api.sys.model.Enterprise;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import javax.persistence.NoResultException;
import model.maintenance.IndicatorPar;
import model.maintenance.list.AreaCostListItem;
import model.maintenance.list.CostReport;
import model.maintenance.list.FuelCostListItem;
import model.maintenance.mysql.Area;
import model.maintenance.mysql.FuelType;
import model.maintenance.mysql.MySQLAgency;
import utilities.Dates;
import utilities.MySQLQuery;
import utilities.mysqlReport.CellFormat;
import utilities.mysqlReport.Column;
import utilities.mysqlReport.HeaderColumn;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.mysqlReport.SummaryRow;
import utilities.mysqlReport.Table;
import utilities.mysqlReport.TableHeader;

public class MaintenanceReport {

    private static final String fuelStrQuery = " "
            + "SELECT "
            + "sum(fl.cost),"
            + "CASE km_src "
            + "WHEN 'fueload' THEN COALESCE(SUM(IF(fl.mileage_cur > fl.mileage_last, fl.mileage_cur - fl.mileage_last, 0)), 0) "
            + "WHEN 'chk'     THEN COALESCE((SELECT SUM(l.mileage     - COALESCE(l.last_mileage, l.mileage)) FROM mto_chk_lst l WHERE l.vh_id = v.id AND l.dt BETWEEN ?1 AND ?2), 0) "
            + "WHEN 'route'   THEN COALESCE((SELECT SUM(len) FROM mto_trip t WHERE t.veh_id = v.id AND t.trip_date BETWEEN ?1 AND ?2),0) "
            + "WHEN 'gps'     THEN COALESCE((SELECT SUM(km) FROM mto_gps_km r WHERE r.vh_id = v.id AND r.dt BETWEEN ?1 AND ?2),0) "
            + "WHEN 'chk'     THEN COALESCE((SELECT SUM(l.mileage_cur     - COALESCE(l.mileage_last, l.mileage_cur)) FROM mto_kms_manual l WHERE l.vh_id = v.id AND l.date BETWEEN ?1 AND ?2), 0) "
            + "WHEN 'none'    THEN NULL "
            + "END, "
            + "sum(fl.amount) "
            + "FROM "
            + "vehicle AS v "
            + "INNER JOIN fuel_load AS fl ON v.id = fl.vehicle_id "
            + "WHERE "
            + "v.id = ?1 AND "
            + "fl.work_id is null AND "
            + "fl.days >= ?2 AND "
            + "fl.days <= ?3 ";

    public static MySQLReport getAnnualCostReport(int cityId, int enterId, int year, Boolean contract, Integer contractor, boolean storeMovs, Connection con) throws Exception {

        String cName = null;
        String eName = null;
        String[] months = new String[]{"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};

        cName = new MySQLQuery("SELECT name FROM city WHERE id = " + cityId).getAsString(con);
        eName = new MySQLQuery("SELECT name FROM enterprise WHERE id = " + enterId).getAsString(con);

        Object[] cfg = new MySQLQuery("SELECT income, trips, store, work_order_flow  FROM mto_cfg").getRecord(con);
        boolean income = MySQLQuery.getAsBoolean(cfg[0]);
        boolean trips = MySQLQuery.getAsBoolean(cfg[1]);
        boolean store = MySQLQuery.getAsBoolean(cfg[2]);

        String rName;
        if (eName == null && cName == null) {
            rName = "Reporte de Gastos de la Flota ";
        } else if (eName != null && cName == null) {
            rName = "Reporte de Gastos de la Flota Agencias " + eName;
        } else if (eName == null && cName != null) {
            rName = "Reporte de Gastos de la Flota Agencias " + cName;
        } else {
            rName = "Reporte de Gastos de la Flota Agencia " + eName + " - " + cName;
        }

        MySQLReport rep1 = new MySQLReport(rName, "Año " + year, "Hoja 1", MySQLQuery.now(con));
        rep1.setVerticalFreeze(0);
        rep1.setHorizontalFreeze(3);
        rep1.setZoomFactor(80);
        //Formatos
        rep1.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        rep1.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$#,##0.00"));

        //DATOS DE GASTOS DES LAS AREAS*********************************************************************************************************
        CostReport rep = findAnnualCostReport("mto", cityId, enterId, year, contract, contractor, storeMovs, con);
        List<Column> cols = new ArrayList<>();
        cols.add(new Column("Código", 15, 0));
        cols.add(new Column("Área", 30, 0));
        for (String month : months) {
            cols.add(new Column(month, 17, 1));
        }
        cols.add(new Column("Total", 22, 1));

        Table tblModArea = new Table("Gastos por Área");
        tblModArea.setColumns(cols);
        tblModArea.setSummaryRow(new SummaryRow("Total", 2));
        Table tblByAreas = new Table(tblModArea);
        for (int j = 0; j < rep.areaCosts.size(); j++) {
            AreaCostListItem ar = rep.areaCosts.get(j);
            Object[] row = new Object[15];
            row[0] = ar.areaPUC;
            row[1] = ar.areaName;
            List<BigDecimal> vals = ar.values;
            BigDecimal total = BigDecimal.ZERO;
            for (int i = 0; i < vals.size(); i++) {
                row[i + 2] = vals.get(i);
                total = total.add(vals.get(i));
            }
            row[14] = total;
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                tblByAreas.addRow(row);
            }

        }
        if (tblByAreas.getData() != null && tblByAreas.getData().length > 0) {
            rep1.getTables().add(tblByAreas);
        }

        // GASTOS POR RUBRO *********************************************************************************************************
        CostReport repRub = findAnnualCostReport("rub", cityId, enterId, year, contract, contractor, storeMovs, con);
        List<Column> colsr = new ArrayList<>();
        colsr.add(new Column("Código", 15, 0));
        colsr.add(new Column("Rubro", 30, 0));
        for (String month : months) {
            colsr.add(new Column(month, 17, 1));
        }
        colsr.add(new Column("Total", 22, 1));
        Table tblModRubros = new Table("Gastos por Rubro");
        tblModRubros.setColumns(colsr);
        tblModRubros.setSummaryRow(new SummaryRow("Total", 2));

        Table tblByRubros = new Table(tblModRubros);
        for (int j = 0; j < repRub.areaCosts.size(); j++) {
            AreaCostListItem ar = repRub.areaCosts.get(j);
            Object[] row = new Object[15];

            row[0] = ar.areaPUC;
            row[1] = ar.areaName;
            List<BigDecimal> vals = ar.values;
            BigDecimal total = BigDecimal.ZERO;
            for (int i = 0; i < vals.size(); i++) {
                row[i + 2] = vals.get(i);
                total = total.add(vals.get(i));
            }
            row[14] = total;
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                tblByRubros.addRow(row);
            }

        }
        if (tblByRubros.getData() != null && tblByRubros.getData().length > 0) {
            rep1.getTables().add(tblByRubros);
        }

        //GASTOS COMBUSTIBLES ******************************************************************************************************************************************************************************************************************
        // reportar la información de Combsutibles
        colsr = new ArrayList<>();
        colsr.add(new Column("Código", 15, 0));
        colsr.add(new Column("Combustible", 30, 0));
        for (String month : months) {
            colsr.add(new Column(month, 17, 1));
        }
        colsr.add(new Column("Total", 22, 1));
        tblModRubros.setColumns(colsr);
        Table tblByComb = new Table(tblModRubros); // el mismo modelo de la tablade rubros
        tblByComb.setTitle("Gastos por Combustibles");
        for (int j = 0; j < rep.fuelCosts.size(); j++) {
            FuelCostListItem ar = rep.fuelCosts.get(j);
            Object[] row = new Object[15];
            row[0] = ar.fuelTypeCode;
            row[1] = ar.fuelTypeName;
            List<BigDecimal> vals = ar.values;
            BigDecimal total = BigDecimal.ZERO;
            for (int i = 0; i < vals.size(); i++) {
                row[i + 2] = vals.get(i);
                total = total.add(vals.get(i));
            }
            row[14] = total;
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                tblByComb.addRow(row);
            }
        }
        if (tblByComb.getData() != null && tblByComb.getData().length > 0) {
            rep1.getTables().add(tblByComb);
        }

        //TABLA DE INGRESOS ******************************************************************************************************************************************************************************************************************
        CostReport repIng = findAnnualCostReport("ing", cityId, enterId, year, contract, contractor, storeMovs, con);

        //la información de rubros
        List<Column> coling = new ArrayList<>();
        coling.add(new Column("Código", 15, 0));
        coling.add(new Column("Rubro", 30, 0));
        for (String month : months) {
            coling.add(new Column(month, 17, 1));
        }
        coling.add(new Column("Total", 22, 1));
        Table tblModIng = new Table("Ingresos por Rubro");
        tblModIng.setColumns(coling);
        tblModIng.setSummaryRow(new SummaryRow("Total", 2));
        Table tblByIngs = new Table(tblModIng);
        if (income) {
            for (int j = 0; j < repIng.areaCosts.size(); j++) {
                AreaCostListItem ar = repIng.areaCosts.get(j);
                Object[] row = new Object[15];

                row[0] = ar.areaPUC;
                row[1] = ar.areaName;
                List<BigDecimal> vals = ar.values;
                BigDecimal total = BigDecimal.ZERO;
                for (int i = 0; i < vals.size(); i++) {
                    row[i + 2] = vals.get(i);
                    total = total.add(vals.get(i));
                }
                row[14] = total;
                if (total.compareTo(BigDecimal.ZERO) > 0) {
                    tblByIngs.addRow(row);
                }

            }
            if (tblByIngs.getData() != null && tblByIngs.getData().length > 0) {
                rep1.getTables().add(tblByIngs);
            }
        }
        //INFORMACION DE ALMACENES ******************************************************************************************************************************************************************************************************************
        Table tblStore = new Table(tblModRubros); // el mismo modelo de la tablade rubros
        if (store && !storeMovs) {
            Object[][] data = new MySQLQuery("SELECT "
                    + "sup.id,MONTH(mv.dt),SUM(mv.cost) "//0
                    + "FROM mto_store_mv AS mv "//1
                    + "INNER JOIN mto_store_mv_type AS tp ON tp.id = mv.type_id "//2
                    + "INNER JOIN mto_store AS st ON mv.store_id = st.id "//3
                    + "INNER JOIN mto_store_ref AS ref ON ref.id = mv.ref_id "//4
                    + " "//5
                    + "INNER JOIN area AS sub ON sub.id = ref.suba_id "//6
                    + "INNER JOIN area AS sup ON sup.id = sub.area_id "//7
                    + " "//8
                    + "LEFT JOIN mto_store AS dest ON dest.id = mv.dest_store_id "//9
                    + "LEFT JOIN prov_provider AS pr ON pr.id = mv.provider_id "//10
                    + "LEFT JOIN mto_store_terc AS tr ON tr.id = mv.terc_id "//11
                    + "LEFT JOIN item AS i ON i.id = mv.item_id "//12
                    + "LEFT JOIN work_order AS wo ON wo.id = i.work_id "//13
                    + "LEFT JOIN vehicle AS v ON v.id = wo.vehicle_id "//14
                    + "WHERE YEAR(mv.dt) = " + year + " AND !mv.cancel GROUP BY sup.id, MONTH(mv.dt)").getRecords(con);
            tblStore.setTitle("Abastecimiento de Almacenes");
            Area[] areas = Area.getSuperAreas("mto", con);
            for (Area area : areas) {
                Object[] row = new Object[15];
                row[0] = area.getPuc();
                row[1] = area.getName();
                BigDecimal totalR = BigDecimal.ZERO;
                for (Object[] rowData : data) {
                    Integer supaId = MySQLQuery.getAsInteger(rowData[0]);
                    Integer month = MySQLQuery.getAsInteger(rowData[1]);
                    BigDecimal cost = MySQLQuery.getAsBigDecimal(rowData[2], true);
                    for (int j = 0; j < months.length; j++) {
                        if (supaId.equals(area.getId()) && month == ((j + 1))) {
                            row[j + 2] = MySQLQuery.getAsBigDecimal(row[j + 2], true).add(cost);
                            totalR = totalR.add(MySQLQuery.getAsBigDecimal(cost, true));
                        }
                    }
                }
                row[14] = totalR;
                if (totalR.compareTo(BigDecimal.ZERO) > 0) {
                    tblStore.addRow(row);
                }
            }
            if (tblStore.getData() != null && tblStore.getData().length > 0) {
                rep1.getTables().add(tblStore);
            }
        }
        //INFORMACION DE FLETES ******************************************************************************************************************************************************************************************************************
        Table tblByFletes = new Table(tblModIng); // usamos el modelo de tabla de los ingresos
        if (trips) // reportar la informacion de ingresos de la flota
        {
            tblByFletes.setTitle("Ingresos por Fletes ");
            Object[] rowIng = new Object[15];
            rowIng[0] = "";
            rowIng[1] = "Fletes";
            BigDecimal totIng = BigDecimal.ZERO;
            String str = "SELECT "
                    + "SUM(t.price) "//0
                    + "FROM "
                    + "mto_trip AS t "
                    + "INNER JOIN vehicle AS vh ON vh.id= t.veh_id "
                    + "INNER JOIN agency AS a ON a.id = vh.agency_id "
                    + "WHERE ";
            if (cityId > 0) {
                str += "a.city_id = " + cityId + " AND ";

            }
            if (enterId > 0) {
                str += "a.enterprise_id = " + enterId + " AND ";
            }
            str += "YEAR(t.trip_date) = " + year + " AND "
                    + "MONTH(t.trip_date )= ?1 ";
            for (int j = 0; j < 12; j++) {
                MySQLQuery q = new MySQLQuery(str);
                q.setParam(1, j + 1);
                try {
                    rowIng[j + 2] = q.getAsBigDecimal(con, true);
                    totIng = totIng.add((BigDecimal) rowIng[j + 2]);
                } catch (NoResultException nr) {
                    rowIng[j + 2] = BigDecimal.ZERO;
                    totIng = totIng.add(BigDecimal.ZERO);
                }
            }
            rowIng[14] = totIng;
            tblByFletes.addRow(rowIng);
            if (tblByFletes.getData() != null && tblByFletes.getData().length > 0) {
                rep1.getTables().add(tblByFletes);
            }
        }

        //REPORTAR LOS TOTALES ******************************************************************************************************************************************************************************************************************
        if (income || trips) {
            List<Column> colsTot = new ArrayList<>();
            colsTot.add(new Column("", 15, 0));
            colsTot.add(new Column("Total", 30, 0));
            for (String month : months) {
                colsTot.add(new Column(month, 17, 1));
            }
            colsTot.add(new Column("Total", 22, 1));
            Table tblsResult = new Table("Totales por Columna");
            tblsResult.setColumns(colsTot);

            Table tblByResult = new Table(tblsResult);
            Object[][] rowTot = new Object[3][15];
            for (int i = 0; i < 15; i++) {//inicializar el vector
                rowTot[0][i] = BigDecimal.ZERO;
                rowTot[1][i] = BigDecimal.ZERO;
                rowTot[2][i] = BigDecimal.ZERO;
            }
            rowTot[0][0] = "";
            rowTot[0][1] = "Gastos";

            rowTot[1][0] = "";
            rowTot[1][1] = "Ingresos";

            rowTot[2][0] = "";
            rowTot[2][1] = "Resultado";

            BigDecimal tout = BigDecimal.ZERO;
            BigDecimal tin = BigDecimal.ZERO;
            BigDecimal tresult = BigDecimal.ZERO;

            for (int i = 0; i < tblByAreas.getData().length; i++) { // recorreindo la tabla de los vlores de las areas
                Object[] row = tblByAreas.getData()[i];
                for (int j = 2; j < row.length; j++) {
                    rowTot[2][j] = ((BigDecimal) rowTot[2][j]).subtract((BigDecimal) row[j]);
                    rowTot[0][j] = ((BigDecimal) rowTot[0][j]).add((BigDecimal) row[j]);
                    tresult = ((BigDecimal) tresult).subtract((BigDecimal) row[j]);
                    tout = ((BigDecimal) tout).add((BigDecimal) row[j]);
                }
            }
            if (tblByRubros.getData() != null) {
                for (int i = 0; i < tblByRubros.getData().length; i++) { // recorreindo la tabla de los valores por rubro
                    Object[] row = tblByRubros.getData()[i];
                    for (int j = 2; j < row.length; j++) {
                        rowTot[2][j] = ((BigDecimal) rowTot[2][j]).subtract((BigDecimal) row[j]);
                        rowTot[0][j] = ((BigDecimal) rowTot[0][j]).add((BigDecimal) row[j]);
                        tresult = ((BigDecimal) tresult).subtract((BigDecimal) row[j]);
                        tout = ((BigDecimal) tout).add((BigDecimal) row[j]);
                    }
                }
            }
            for (int i = 0; i < tblByComb.getData().length; i++) { // recorreindo la tabla de los vlores por combustibles
                Object[] row = tblByComb.getData()[i];
                for (int j = 2; j < row.length; j++) {
                    rowTot[2][j] = ((BigDecimal) rowTot[2][j]).subtract((BigDecimal) row[j]);
                    rowTot[0][j] = ((BigDecimal) rowTot[0][j]).add((BigDecimal) row[j]);
                    tresult = ((BigDecimal) tresult).subtract((BigDecimal) row[j]);
                    tout = ((BigDecimal) tout).add((BigDecimal) row[j]);
                }
            }

            if (tblStore.getData() != null) { // el 
                for (int i = 0; i < tblStore.getData().length; i++) { // recorreindo la tabla de los valores de almacenes
                    Object[] row = tblStore.getData()[i];
                    for (int j = 2; j < row.length; j++) {
                        rowTot[2][j] = MySQLQuery.getAsBigDecimal(rowTot[2][j], true).add(MySQLQuery.getAsBigDecimal(row[j], true));
                        rowTot[0][j] = MySQLQuery.getAsBigDecimal(rowTot[0][j], true).add(MySQLQuery.getAsBigDecimal(row[j], true));
                        tresult = ((BigDecimal) tresult).subtract(MySQLQuery.getAsBigDecimal(row[j], true));
                        tout = ((BigDecimal) tout).add(MySQLQuery.getAsBigDecimal(row[j], true));
                    }
                }
            }

            if (tblByFletes.getData() != null) {
                for (int i = 0; i < tblByFletes.getData().length; i++) { // recorreindo la tabla de los vlores por combustibles
                    Object[] row = tblByFletes.getData()[i];
                    for (int j = 2; j < row.length; j++) {
                        rowTot[2][j] = ((BigDecimal) rowTot[2][j]).add((BigDecimal) row[j]);
                        rowTot[1][j] = ((BigDecimal) rowTot[1][j]).add((BigDecimal) row[j]);
                        tresult = ((BigDecimal) tresult).add((BigDecimal) row[j]);
                        tin = ((BigDecimal) tin).add((BigDecimal) row[j]);
                    }
                }
            }

            if (tblByIngs.getData() != null) {
                for (int i = 0; i < tblByIngs.getData().length; i++) { // recorriendo valores de ingresos
                    Object[] row = tblByIngs.getData()[i];
                    for (int j = 2; j < row.length; j++) {
                        rowTot[2][j] = ((BigDecimal) rowTot[2][j]).add((BigDecimal) row[j]);
                        rowTot[1][j] = ((BigDecimal) rowTot[1][j]).add((BigDecimal) row[j]);
                        tresult = ((BigDecimal) tresult).add((BigDecimal) row[j]);
                        tin = ((BigDecimal) tin).add((BigDecimal) row[j]);
                    }
                }
            }

            rowTot[0][14] = tout;
            rowTot[1][14] = tin;
            rowTot[2][14] = tresult;
            tblByResult.setData(Arrays.asList(rowTot));
            if (tblByResult.getData() != null && tblByResult.getData().length > 0) {
                rep1.getTables().add(tblByResult);
            }
        }
        return rep1;

    }

    public static MySQLReport getCostReportByDates(int cityId, int entId, int idVeh, Date fBegin, Date fEnd, Boolean contract, Integer contractor, boolean storeMovs, boolean percCosts, Connection conn) throws Exception {

        String cName = new MySQLQuery("SELECT name FROM city WHERE id = " + cityId).getAsString(conn);
        String eName = new MySQLQuery("SELECT name FROM enterprise WHERE id = " + entId).getAsString(conn);

        Object[] cfg = new MySQLQuery("SELECT income, trips, store FROM mto_cfg").getRecord(conn);
        boolean income = MySQLQuery.getAsBoolean(cfg[0]);
        boolean trips = MySQLQuery.getAsBoolean(cfg[1]);
        boolean store = MySQLQuery.getAsBoolean(cfg[2]);

        String rName;
        if (eName == null && cName == null) {
            rName = "Reporte de Gastos por Periodo ";
        } else if (eName != null && cName == null) {
            rName = "Reporte de Gastos por Periodo Agencias " + eName;
        } else if (eName == null && cName != null) {
            rName = "Reporte de Gastos por Periodo Agencias " + cName;
        } else {
            rName = "Reporte de Gastos por Periodo Agencia " + eName + " - " + cName;
        }

        String time = "Periodo " + Dates.getBackupFormat().format(fBegin) + " - " + Dates.getBackupFormat().format(fEnd);
        MySQLReport rep1 = new MySQLReport(rName, time, "Hoja 1", MySQLQuery.now(conn));
        if (idVeh > 0) {
            Object[] vhRow = new MySQLQuery("SELECT plate, contract FROM vehicle WHERE id = " + idVeh).getRecord(conn);
            String vhPlate = vhRow[0].toString();
            rep1.getSubTitles().add("Vehículo :" + vhPlate);
        }
        rep1.setVerticalFreeze(0);
        rep1.setHorizontalFreeze(0);
        rep1.setZoomFactor(80);
        //Formatos
        rep1.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        rep1.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$#,##0.00"));
        rep1.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "0.00%"));

        //GASTOS POR AREAS *******************************************************************************************************************************
        CostReport rep = findCostReport("mto", cityId, entId, idVeh, fBegin, fEnd, contract, contractor, storeMovs, conn);
        List<Column> cols = new ArrayList<>();
        cols.add(new Column("Código", 15, 0));
        cols.add(new Column("Área", 35, 0));
        cols.add(new Column("Porcentaje", 15, 2));
        cols.add(new Column("Valor", 25, 1));
        if (percCosts && (income || trips)) {
            cols.add(new Column("% vs Ingresos", 15, 2));
            cols.add(new Column("% vs Gastos", 15, 2));
        }

        Table tblModArea = new Table("Gastos por Áreas");
        tblModArea.setColumns(cols);
        tblModArea.setSummaryRow(new SummaryRow("Total", 2));
        //Fin config
        Table tblByAreas = new Table(tblModArea);
        BigDecimal totars = BigDecimal.ZERO;
        for (int j = 0; j < rep.areaCosts.size(); j++) { //lo recorro para optener el total de las areas
            AreaCostListItem ar = rep.areaCosts.get(j);
            totars = totars.add((BigDecimal) ar.values.get(0));
        }
        for (int j = 0; j < rep.areaCosts.size(); j++) {
            AreaCostListItem ar = rep.areaCosts.get(j);
            Object[] row = new Object[(percCosts && (income || trips)) ? 6 : 4];
            if (ar.values.get(0) != null && ((BigDecimal) ar.values.get(0)).compareTo(BigDecimal.ZERO) > 0) { //para evitar listar las areas en ceros
                tblByAreas.addRow(row);
                row[0] = ar.areaPUC;
                row[1] = ar.areaName;
                row[2] = (((BigDecimal) ar.values.get(0)).divide((BigDecimal) totars, 5, RoundingMode.UP));
                row[3] = ar.values.get(0);
            }
        }

        // GASTOS POR RUBRO *********************************************************************************************************************
        CostReport repRub = findCostReport("rub", cityId, entId, idVeh, fBegin, fEnd, contract, contractor, storeMovs, conn);
        List<Column> colsRub = new ArrayList<>();
        colsRub.add(new Column("Código", 15, 0));
        colsRub.add(new Column("Rubro", 35, 0));
        colsRub.add(new Column("Porcentaje", 15, 2));
        colsRub.add(new Column("Valor", 25, 1));
        if (percCosts && (income || trips)) {
            colsRub.add(new Column("% vs Ingresos", 15, 2));
            colsRub.add(new Column("% vs Gastos", 15, 2));
        }

        Table tblModRub = new Table("Gastos por Rubro");
        tblModRub.setColumns(colsRub);
        tblModRub.setSummaryRow(new SummaryRow("Total", 2));
        //Fin config
        Table tblByRubro = new Table(tblModRub);
        BigDecimal totrub = BigDecimal.ZERO;
        for (int j = 0; j < repRub.areaCosts.size(); j++) { //lo recorro para optener el total de las areas
            AreaCostListItem ar = repRub.areaCosts.get(j);
            totrub = totrub.add((BigDecimal) ar.values.get(0));
        }
        for (int j = 0; j < repRub.areaCosts.size(); j++) {
            AreaCostListItem ar = repRub.areaCosts.get(j);
            Object[] row = new Object[(percCosts && (income || trips)) ? 6 : 4];
            if (ar.values.get(0) != null && ((BigDecimal) ar.values.get(0)).compareTo(BigDecimal.ZERO) > 0) { //para evitar listar las areas en ceros
                tblByRubro.addRow(row);
                row[0] = ar.areaPUC;
                row[1] = ar.areaName;
                row[2] = (((BigDecimal) ar.values.get(0)).divide((BigDecimal) totrub, 5, RoundingMode.UP));
                row[3] = ar.values.get(0);
            }
        }

        // GASTOS POR COMBUSTIBLES ********************************************************************************************************************
        Table tblByCombs = new Table(tblModRub); // usamos el mismo modelo de tabla anterior
        tblByCombs.setTitle("Gastos por Combustibles");
        BigDecimal totcomb = BigDecimal.ZERO;
        for (int j = 0; j < rep.fuelCosts.size(); j++) { //lo recorro para optener el total de las areas
            FuelCostListItem ar = rep.fuelCosts.get(j);
            totcomb = totcomb.add((BigDecimal) ar.values.get(0));
        }
        for (int j = 0; j < rep.fuelCosts.size(); j++) {
            FuelCostListItem ar = rep.fuelCosts.get(j);
            Object[] row = new Object[(percCosts && (income || trips)) ? 6 : 4];
            if (ar.values.get(0) != null && ((BigDecimal) ar.values.get(0)).compareTo(BigDecimal.ZERO) > 0) { //para evitar listar las areas en ceros
                tblByCombs.addRow(row);
                row[0] = ar.fuelTypeCode;
                row[1] = ar.fuelTypeName;
                row[2] = (((BigDecimal) ar.values.get(0)).divide((BigDecimal) totcomb, 5, RoundingMode.UP));
                row[3] = ar.values.get(0);
            }
        }

        //INFORMACION DE ALMACENES ******************************************************************************************************************************************************************************************************************
        Table tblStore = new Table(tblByRubro); // el mismo modelo de la tablade rubros
        if (store && !storeMovs) {
            MySQLQuery q = new MySQLQuery("SELECT "
                    + "sup.id,SUM(mv.cost) "//0
                    + "FROM mto_store_mv AS mv "//1
                    + "INNER JOIN mto_store_mv_type AS tp ON tp.id = mv.type_id "//2
                    + "INNER JOIN mto_store AS st ON mv.store_id = st.id "//3
                    + "INNER JOIN mto_store_ref AS ref ON ref.id = mv.ref_id "//4
                    + " "//5
                    + "INNER JOIN area AS sub ON sub.id = ref.suba_id "//6
                    + "INNER JOIN area AS sup ON sup.id = sub.area_id "//7
                    + " "//8
                    + "LEFT JOIN mto_store AS dest ON dest.id = mv.dest_store_id "//9
                    + "LEFT JOIN prov_provider AS pr ON pr.id = mv.provider_id "//10
                    + "LEFT JOIN mto_store_terc AS tr ON tr.id = mv.terc_id "//11
                    + "LEFT JOIN item AS i ON i.id = mv.item_id "//12
                    + "LEFT JOIN work_order AS wo ON wo.id = i.work_id "//13
                    + "LEFT JOIN vehicle AS v ON v.id = wo.vehicle_id "//14
                    + "WHERE mv.dt BETWEEN ?1 AND ?2 AND !mv.cancel GROUP BY sup.id");
            q.setParam(1, fBegin);
            q.setParam(2, fEnd);
            Object[][] data = q.getRecords(conn);
            tblStore.setTitle("Gastos en Almacenes");
            Area[] areas = Area.getSuperAreas("mto", conn);
            BigDecimal total = BigDecimal.ZERO;
            for (Object[] rowData : data) {
                total = total.add(MySQLQuery.getAsBigDecimal(rowData[1], true));
            }
            for (Area area : areas) {
                Object[] row = new Object[(percCosts && (income || trips)) ? 6 : 4];
                row[0] = area.getPuc();
                row[1] = area.getName();
                for (Object[] rowData : data) {
                    Integer supaId = MySQLQuery.getAsInteger(rowData[0]);
                    BigDecimal cost = MySQLQuery.getAsBigDecimal(rowData[1], true);
                    if (supaId.equals(area.getId())) {
                        row[3] = MySQLQuery.getAsBigDecimal(row[3], true).add(cost);
                    }
                }
                if (row[3] != null && MySQLQuery.getAsBigDecimal(row[3], true).compareTo(BigDecimal.ZERO) > 0) {
                    row[2] = (MySQLQuery.getAsBigDecimal(row[3], true).divide(total, 5, RoundingMode.UP));
                    tblStore.addRow(row);
                }
            }
        }

        //INGRESOS POR FLETES *****************************************************************************************************************
        CostReport repIng = findCostReport("ing", cityId, entId, idVeh, fBegin, fEnd, contract, contractor, storeMovs, conn);
        List<Column> colsIng = new ArrayList<>();
        colsIng.add(new Column("Código", 15, 0));
        colsIng.add(new Column("Rubro", 35, 0));
        colsIng.add(new Column("Porcentaje", 15, 2));
        colsIng.add(new Column("Valor", 25, 1));

        Table tblModIng = new Table("Ingresos por Rubro");
        tblModIng.setColumns(colsIng);
        tblModIng.setSummaryRow(new SummaryRow("Total", 2));
        //Fin config
        Table tblByIngs = new Table(tblModIng);
        if (income) {
            BigDecimal toting = BigDecimal.ZERO;
            for (int j = 0; j < repIng.areaCosts.size(); j++) { //lo recorro para optener el total de las areas
                AreaCostListItem ar = repIng.areaCosts.get(j);
                toting = toting.add((BigDecimal) ar.values.get(0));
            }
            for (int j = 0; j < repIng.areaCosts.size(); j++) {
                AreaCostListItem ar = repIng.areaCosts.get(j);
                Object[] row = new Object[4];
                if (ar.values.get(0) != null && ((BigDecimal) ar.values.get(0)).compareTo(BigDecimal.ZERO) > 0) { //para evitar listar las areas en ceros
                    tblByIngs.addRow(row);
                    row[0] = ar.areaPUC;
                    row[1] = ar.areaName;
                    row[2] = (((BigDecimal) ar.values.get(0)).divide((BigDecimal) toting, 5, RoundingMode.UP));
                    row[3] = ar.values.get(0);
                }
            }
        }
        // INGRESOS POR FLETES *****************************************************************************************************************
        Table tblByFletes = new Table(tblModIng); // Usamos el modelo de tabla anterior
        if (trips) {
            // reportar la información de los ingresos de la flota

            tblByFletes.setTitle("Ingresos por Fletes");
            Object[] rowIng = new Object[4];
            rowIng[0] = "";
            rowIng[1] = "Fletes";
            rowIng[2] = BigDecimal.ONE;//es un valor fijo
            String str = "SELECT "
                    + "SUM(t.price) "//0
                    + "FROM "
                    + "mto_trip AS t "
                    + "INNER JOIN vehicle AS vh ON vh.id= t.veh_id "
                    + "INNER JOIN agency AS a ON a.id = vh.agency_id "
                    + "WHERE ";
            if (cityId > 0) {
                str += "a.city_id = " + cityId + " AND ";

            }
            if (entId > 0) {
                str += "a.enterprise_id = " + entId + " AND ";
            }
            str += "t.trip_date BETWEEN ?1 AND ?2 ";
            if (idVeh > 0) {
                str += "AND t.veh_id =" + idVeh + " ";
            }

            MySQLQuery q = new MySQLQuery(str);
            q.setParam(1, fBegin);
            q.setParam(2, fEnd);
            try {
                rowIng[3] = q.getAsBigDecimal(conn, true);
            } catch (NoResultException nr) {
                rowIng[3] = BigDecimal.ZERO;
            }
            tblByFletes.addRow(rowIng);
        }
        //LA TABLA DE LOS TOTALES **************************************************************************************************************
        List<Column> colsTot = new ArrayList<>();
        colsTot.add(new Column("", 15, 0));
        colsTot.add(new Column("Item", 30, 0));
        colsTot.add(new Column("", 20, 0));
        colsTot.add(new Column("Valor", 30, 1));
        if (percCosts && (income || trips)) {
            colsTot.add(new Column("% vs Ingresos", 15, 2));
        }

        Table tblsResult = new Table("Totales por Columna");
        tblsResult.setColumns(colsTot);
        Table tblByResult = new Table(tblsResult);

        if (income || trips) {
            Object[][] rowTot = new Object[3][percCosts ? 5 : 4];

            for (int i = 0; i < (percCosts ? 5 : 4); i++) {//inicializar la matriz
                rowTot[0][i] = BigDecimal.ZERO;
                rowTot[1][i] = BigDecimal.ZERO;
                rowTot[2][i] = BigDecimal.ZERO;
            }
            // Ingreso los valores constantes
            rowTot[0][0] = "";
            rowTot[0][1] = "Gastos";
            rowTot[0][2] = "";

            rowTot[1][0] = "";
            rowTot[1][1] = "Ingresos";
            rowTot[1][2] = "";

            rowTot[2][0] = "";
            rowTot[2][1] = "Resultado";
            rowTot[2][2] = "";

            if (tblByAreas.getData() != null) {
                for (int i = 0; i < tblByAreas.getData().length; i++) { // recorriendo la tabla de los vlores de las areas
                    Object[] row = tblByAreas.getData()[i];
                    rowTot[0][3] = ((BigDecimal) rowTot[0][3]).add((BigDecimal) row[3]);//Egresos
                    rowTot[2][3] = ((BigDecimal) rowTot[2][3]).subtract((BigDecimal) row[3]); //Resultados
                }
            }
            if (tblByRubro.getData() != null) {
                for (int i = 0; i < tblByRubro.getData().length; i++) { // valores de los rubros
                    Object[] row = tblByRubro.getData()[i];
                    rowTot[0][3] = ((BigDecimal) rowTot[0][3]).add((BigDecimal) row[3]);//Egresos
                    rowTot[2][3] = ((BigDecimal) rowTot[2][3]).subtract((BigDecimal) row[3]); //Resultados
                }
            }
            if (tblByCombs.getData() != null) {
                for (int i = 0; i < tblByCombs.getData().length; i++) { // valores de los combustibles
                    Object[] row = tblByCombs.getData()[i];
                    rowTot[0][3] = ((BigDecimal) rowTot[0][3]).add((BigDecimal) row[3]);//Egresos
                    rowTot[2][3] = ((BigDecimal) rowTot[2][3]).subtract((BigDecimal) row[3]); //Resultados
                }
            }
            if (tblStore.getData() != null) {
                for (int i = 0; i < tblStore.getData().length; i++) { // valores de los almacenes
                    Object[] row = tblStore.getData()[i];
                    rowTot[0][3] = ((BigDecimal) rowTot[0][3]).add((BigDecimal) row[3]);//Egresos
                    rowTot[2][3] = ((BigDecimal) rowTot[2][3]).subtract((BigDecimal) row[3]); //Resultados
                }
            }
            if (tblByFletes.getData() != null) {
                for (int i = 0; i < tblByFletes.getData().length; i++) { // valores de los ingresos
                    Object[] row = tblByFletes.getData()[i];
                    rowTot[1][3] = ((BigDecimal) rowTot[1][3]).add((BigDecimal) row[3]);//Ingresos
                    rowTot[2][3] = ((BigDecimal) rowTot[2][3]).add((BigDecimal) row[3]);//Egresos
                }
            }
            if (tblByIngs.getData() != null) {
                for (int i = 0; i < tblByIngs.getData().length; i++) { // valores de los ingresos
                    Object[] row = tblByIngs.getData()[i];
                    rowTot[1][3] = ((BigDecimal) rowTot[1][3]).add((BigDecimal) row[3]);//Ingresos
                    rowTot[2][3] = ((BigDecimal) rowTot[2][3]).add((BigDecimal) row[3]);//Egresos
                }
            }

            if (percCosts) {
                rowTot[0][4] = (MySQLQuery.getAsBigDecimal(rowTot[1][3], true).intValue() == 0 ? 0 : (MySQLQuery.getAsBigDecimal(rowTot[0][3], true)).divide(MySQLQuery.getAsBigDecimal(rowTot[1][3], true), 5, RoundingMode.UP));
                rowTot[1][4] = (MySQLQuery.getAsBigDecimal(rowTot[1][3], true).intValue() == 0 ? 0 : (MySQLQuery.getAsBigDecimal(rowTot[1][3], true)).divide(MySQLQuery.getAsBigDecimal(rowTot[1][3], true), 5, RoundingMode.UP));
                rowTot[2][4] = (MySQLQuery.getAsBigDecimal(rowTot[1][3], true).intValue() == 0 ? 0 : (MySQLQuery.getAsBigDecimal(rowTot[2][3], true)).divide(MySQLQuery.getAsBigDecimal(rowTot[1][3], true), 5, RoundingMode.UP));
            }
            //Recorro los datos que he ingreso y los asigno a la tabla
            for (int i = 0; i < rowTot.length; i++) {
                Object[] row = rowTot[i];
                tblByResult.addRow(row);
            }
        }
        if (percCosts && (income || trips)) {
            BigDecimal totCosts = MySQLQuery.getAsBigDecimal(tblByResult.getData()[0][3], true);
            BigDecimal totIngs = MySQLQuery.getAsBigDecimal(tblByResult.getData()[1][3], true);

            if (tblByAreas.getData() != null) {
                for (Object[] row : tblByAreas.getData()) { // recorriendo la tabla de los valores de las areas
                    row[4] = (totIngs.intValue() == 0 ? 0 : (MySQLQuery.getAsBigDecimal(row[3], true)).divide(totIngs, 5, RoundingMode.UP));
                    row[5] = (totCosts.intValue() == 0 ? 0 : (MySQLQuery.getAsBigDecimal(row[3], true)).divide(totCosts, 5, RoundingMode.UP));
                }
            }

            if (tblByRubro.getData() != null) {
                for (Object[] row : tblByRubro.getData()) { // valores de los rubros
                    row[4] = (totIngs.intValue() == 0 ? 0 : (MySQLQuery.getAsBigDecimal(row[3], true)).divide(totIngs, 5, RoundingMode.UP));
                    row[5] = (totCosts.intValue() == 0 ? 0 : (MySQLQuery.getAsBigDecimal(row[3], true)).divide(totCosts, 5, RoundingMode.UP));
                }
            }

            if (tblByCombs.getData() != null) {
                for (Object[] row : tblByCombs.getData()) { // valores de los combustibles
                    row[4] = (totIngs.intValue() == 0 ? 0 : (MySQLQuery.getAsBigDecimal(row[3], true)).divide(totIngs, 5, RoundingMode.UP));
                    row[5] = (totCosts.intValue() == 0 ? 0 : (MySQLQuery.getAsBigDecimal(row[3], true)).divide(totCosts, 5, RoundingMode.UP));
                }
            }

            if (tblStore.getData() != null) {
                for (Object[] row : tblStore.getData()) { // valores de los almacenes
                    row[4] = (totIngs.intValue() == 0 ? 0 : (MySQLQuery.getAsBigDecimal(row[3], true)).divide(totIngs, 5, RoundingMode.UP));
                    row[5] = (totCosts.intValue() == 0 ? 0 : (MySQLQuery.getAsBigDecimal(row[3], true)).divide(totCosts, 5, RoundingMode.UP));
                }
            }

        }
        if (tblByAreas.getData() != null && tblByAreas.getData().length > 0) {
            rep1.getTables().add(tblByAreas);
        }
        if (tblByRubro.getData() != null && tblByRubro.getData().length > 0) {
            rep1.getTables().add(tblByRubro);
        }
        if (tblByCombs.getData() != null && tblByCombs.getData().length > 0) {
            rep1.getTables().add(tblByCombs);
        }
        if (tblStore.getData() != null && tblStore.getData().length > 0) {
            rep1.getTables().add(tblStore);
        }
        if (tblByFletes.getData() != null && tblByFletes.getData().length > 0) {
            rep1.getTables().add(tblByFletes);
        }
        if (tblByIngs.getData() != null && tblByIngs.getData().length > 0) {
            rep1.getTables().add(tblByIngs);
        }
        if (tblByResult.getData() != null && tblByResult.getData().length > 0) {
            rep1.getTables().add(tblByResult);
        }
        return rep1;
    }

    public static CostReport findAnnualCostReport(String type, int idCity, int idEnt, int year, Boolean contract, Integer contractor, boolean storeMovs, Connection conn) throws Exception {
        int[] months = new int[]{Calendar.JANUARY, Calendar.FEBRUARY, Calendar.MARCH, Calendar.APRIL, Calendar.MAY, Calendar.JUNE, Calendar.JULY, Calendar.AUGUST, Calendar.SEPTEMBER, Calendar.OCTOBER, Calendar.NOVEMBER, Calendar.DECEMBER};
        CostReport rep = null;

        for (int i = 0; i < 12; i++) {
            int month = months[i];
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, 1);
            int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

            Date fBegin = new GregorianCalendar(year, month, 1, 0, 0, 0).getTime();
            Date fEnd = new GregorianCalendar(year, month, maxDay, 0, 0, 0).getTime();

            if (i == 0) {
                rep = findCostReport(type, idCity, idEnt, 0, fBegin, fEnd, contract, contractor, storeMovs, conn);
            } else {
                CostReport tmp = findCostReport(type, idCity, idEnt, 0, fBegin, fEnd, contract, contractor, storeMovs, conn);
                for (int j = 0; j < rep.areaCosts.size(); j++) {
                    rep.areaCosts.get(j).values.add(tmp.areaCosts.get(j).values.get(0));
                }
                if (type == null || type.equals("mto")) {//unicamente en las areas de mantenimiento
                    for (int j = 0; j < tmp.fuelCosts.size(); j++) {
                        rep.fuelCosts.get(j).values.add(tmp.fuelCosts.get(j).values.get(0));
                    }
                }
            }
        }
        return rep;
    }

    //COSTOS ENTRE FECHAS
    public static CostReport findCostReport(String type, int cityId, int entId, int vehId, Date fBegin, Date fEnd, Boolean contract, Integer contractor, boolean storeMovs, Connection con) throws Exception {
        //preparando cost report
        CostReport rep = new CostReport();
        rep.single = (true);
        Object[] cfg = new MySQLQuery("SELECT income, trips, store, work_order_flow, filter_order_approv  FROM mto_cfg").getRecord(con);
        boolean flow = MySQLQuery.getAsBoolean(cfg[3]);
        boolean filterApprov = MySQLQuery.getAsBoolean(cfg[4]);
        String filter;
        if (flow) {
            filter = " (wo.flow_status ='done' OR wo.flow_status='approved') AND ";
            if (filterApprov) {
                filter += "(  "
                        + "SELECT MAX(f.checked)   "
                        + "FROM mto_flow_check AS f   "
                        + "WHERE f.work_order_id = wo.id  "
                        + "AND f.flow_step_id =   "
                        + "(SELECT st.id  "
                        + "FROM mto_flow_step st  "
                        + "WHERE st.flow_id = wo.flow_id  "
                        + "ORDER BY st.place DESC  "
                        + "LIMIT 1  "
                        + ") AND f.`status` = 'ok' ) BETWEEN ?1 AND ?2 ";
            } else {
                filter += "wo.create_date BETWEEN ?1 AND ?2  ";
            }
        } else {
            filter = " wo.`begin` BETWEEN ?1 AND ?2  "
                    + "AND wo.flow_status ='done'  ";
        }
        int idAg = 0;
        if (cityId > 0 && entId > 0) {
            try {
                MySQLAgency ag = MySQLAgency.getAgency(cityId, entId, con);
                idAg = ag.getId();
            } catch (Exception ex) {
                throw new Exception("No existe una agencia con esos datos");
            }
        }

        //costos de items
        String qs1 = "SELECT supa.id, SUM(i.value) "
                + "FROM work_order wo "
                + "INNER JOIN item i ON wo.id = i.work_id "
                + "INNER JOIN area suba ON suba.id = i.area_id "
                + "INNER JOIN area supa ON supa.id = suba.area_id "
                + "INNER JOIN vehicle v ON wo.vehicle_id = v.id  "//hacer parametrizable????????? v.active = 1 
                + "WHERE  wo.canceled = 0  AND "
                + filter
                + (!storeMovs ? " AND wo.store_id IS NULL " : " ")
                + (type != null ? " AND supa.type = '" + type + "' " : " ")
                + (contractor != null ? " AND v.contractor_id = " + contractor + " " : " ")
                + (contract != null ? " AND v.contract = " + contract + " " : " ");

        if (vehId <= 0) {
            if (cityId > 0 && entId > 0) {
                qs1 += "AND wo.agency_id = " + idAg + " ";
            } else if (cityId > 0 && entId == 0) {
                qs1 += "AND wo.agency_id IN (SELECT ag.id FROM agency ag WHERE ag.city_id = " + cityId + ") ";
            } else if (cityId == 0 && entId > 0) {
                qs1 += "AND wo.agency_id IN (SELECT ag.id FROM agency as ag WHERE ag.enterprise_id = " + entId + ") ";
            }
        } else {
            qs1 += "AND wo.vehicle_id = " + vehId + " ";
        }
        qs1 += " GROUP BY supa.id ";

        MySQLQuery q1 = new MySQLQuery(qs1);
        q1.setParam(1, fBegin);
        q1.setParam(2, fEnd);
        Object[][] areaCostData = q1.getRecords(con);

        //listar areas
        Area[] areas = Area.getSuperAreas(type, con);
        List<AreaCostListItem> larea = new ArrayList<>(areas.length);
        rep.areaCosts = (larea);

        //llenando los valores de las areas de mantenimeinto
        for (Area area : areas) {
            AreaCostListItem ac = new AreaCostListItem();
            larea.add(ac);
            ac.setArea(area.getId(), area.getName(), area.getPuc());
            ac.values = (new ArrayList<BigDecimal>());
            BigDecimal val = BigDecimal.ZERO;
            for (Object[] areaCostRow : areaCostData) {
                int rowId = MySQLQuery.getAsInteger(areaCostRow[0]);
                if (ac.areaId == rowId) {
                    val = MySQLQuery.getAsBigDecimal(areaCostRow[1], true);
                    break;
                }
            }
            ac.values.add(val);
        }

        if (type == null || type.equals("mto")) { //este if me permite no repetir los valores de combustible segun las areas
            FuelType[] fuelTypes = FuelType.getAllFuelTypes(con);
            List<FuelCostListItem> lstf = new ArrayList<>(fuelTypes.length);
            rep.fuelCosts = (lstf);
            //costo por tipo de combustible
            String qs3
                    = "SELECT fl.fuel_type_id, SUM(fl.cost) "
                    + "FROM fuel_load  fl "
                    + "INNER JOIN vehicle v ON fl.vehicle_id = v.id "
                    + "WHERE fl.days BETWEEN ?1 AND ?2 "
                    + (contractor != null ? " AND v.contractor_id = " + contractor + " " : " ")
                    + (contract != null ? " AND v.contract = " + contract + " " : " ");

            if (vehId <= 0) {
                if (cityId > 0 && entId > 0) {
                    qs3 += "AND fl.agency_id = " + idAg + " ";
                } else if (cityId > 0 && entId == 0) {
                    qs3 += "AND fl.agency_id IN (SELECT ag.id FROM agency ag WHERE ag.city_id = " + cityId + ") ";
                } else if (cityId == 0 && entId > 0) {
                    qs3 += "AND fl.agency_id IN (SELECT ag.id FROM agency as ag WHERE ag.enterprise_id = " + entId + ") ";
                }
            } else {
                qs3 += "AND fl.vehicle_id = " + vehId + " ";
            }
            qs3 += " GROUP BY fl.fuel_type_id";

            MySQLQuery fuelQuery = new MySQLQuery(qs3);
            fuelQuery.setParam(1, fBegin);
            fuelQuery.setParam(2, fEnd);
            Object[][] fuelData = fuelQuery.getRecords(con);

            for (FuelType fuelType : fuelTypes) {
                FuelCostListItem fc = new FuelCostListItem();
                lstf.add(fc);
                fc.setFuelType(fuelType.getId(), fuelType.getName());
                fc.values = (new ArrayList<BigDecimal>());
                BigDecimal val = BigDecimal.ZERO;
                for (Object[] fuelRow : fuelData) {
                    int rowId = MySQLQuery.getAsInteger(fuelRow[0]);
                    if (fc.fuelTypeId == rowId) {
                        val = MySQLQuery.getAsBigDecimal(fuelRow[1], true);
                        break;
                    }
                }
                fc.values.add(val);
            }
        }
        return rep;

    }

    //COSTOS X VEHÍCULO
    public static CostReport findCostReportVeh(String type, int idCity, int idEnt, int idVeh, int year, Boolean contract, Integer contractor, boolean storeMovs, Connection con) throws Exception {
        int[] months = new int[]{Calendar.JANUARY, Calendar.FEBRUARY, Calendar.MARCH, Calendar.APRIL, Calendar.MAY, Calendar.JUNE, Calendar.JULY, Calendar.AUGUST, Calendar.SEPTEMBER, Calendar.OCTOBER, Calendar.NOVEMBER, Calendar.DECEMBER};
        CostReport rep = null;
        CostReport tmp;

        for (int i = 0; i < 12; i++) {
            int month = months[i];
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, 1);
            int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

            Date fBegin = new GregorianCalendar(year, month, 1, 0, 0, 0).getTime();
            Date fEnd = new GregorianCalendar(year, month, maxDay, 0, 0, 0).getTime();

            if (i == 0) {
                rep = findCostReport(type, idCity, idEnt, idVeh, fBegin, fEnd, contract, contractor, storeMovs, con);
            } else {
                tmp = findCostReport(type, idCity, idEnt, idVeh, fBegin, fEnd, contract, contractor, storeMovs, con);
                for (int j = 0; j < rep.areaCosts.size(); j++) {
                    rep.areaCosts.get(j).values.add(tmp.areaCosts.get(j).values.get(0));
                }

                if (type == null || type.equals("mto")) {
                    for (int j = 0; j < tmp.fuelCosts.size(); j++) {
                        rep.fuelCosts.get(j).values.add(tmp.fuelCosts.get(j).values.get(0));
                    }
                }
            }
        }
        return rep;
    }

    //PROYECCIÓN
    public static CostReport findForecastReport(String type, int cityId, int enterId, int vehId, int year, Boolean contract, Integer contractor, Connection con) throws Exception {
        CostReport rep = new CostReport();
        rep.areaCosts = (new ArrayList<>());
        rep.fuelCosts = (new ArrayList<>());

        Area[] ars = Area.getSuperAreas(type, con);
        FuelType[] ftypes = FuelType.getAllFuelTypes(con);
        MySQLAgency[] ags = MySQLAgency.getAgencies(cityId, enterId, con);

        if (ags.length == 0) {
            throw new Exception("No hay agencias con los parámetros solicitados.");
        }

        Object[][] lareas;
        Object[][] lfuels = new Object[0][];

        if (vehId <= 0) {
            String agIds = String.valueOf(ags[0].getId());
            for (int i = 1; i < ags.length; i++) {
                agIds += ("," + ags[i].getId());
            }

            String qs1
                    = "SELECT fc.area_id, Sum(fc.val) "
                    + "FROM forecast fc "
                    + "INNER JOIN vehicle v ON fc.vec_id = v.id "
                    + "INNER JOIN area a ON fc.area_id = a.id "
                    + "WHERE  "
                    + "a.type = '" + type + "' AND "
                    + "fc.anio =" + year + " ";
            if (contract != null) {
                qs1 += " AND v.contract =" + contract + " ";
            }
            if (contractor != null) {
                qs1 += " AND v.contractor_id =" + contractor + " ";
            }
            qs1 += " AND fc.fuel_type_id IS NULL "
                    + " AND v.agency_id IN (" + agIds + ") GROUP BY fc.area_id";
            MySQLQuery q1 = new MySQLQuery(qs1);
            lareas = q1.getRecords(con);

            if (type.equals("mto")) {
                String qs2
                        = "SELECT fc.fuel_type_id, Sum(fc.val) "
                        + " FROM forecast fc, vehicle v "
                        + " WHERE fc.vec_id = v.id  "
                        + " AND fc.anio =" + year + " "
                        + " AND fc.area_id IS NULL";
                if (contract != null) {
                    qs2 += " AND v.contract =" + contract + " ";
                }
                if (contractor != null) {
                    qs2 += " AND v.contractor_id =" + contractor + " ";
                }
                qs2 += " AND v.agency_id IN (" + agIds + ") GROUP BY fc.fuel_type_id";
                MySQLQuery q2 = new MySQLQuery(qs2);
                lfuels = q2.getRecords(con);
            }
        } else {
            String qs1
                    = "SELECT fc.area_id, fc.val "
                    + "FROM forecast fc "
                    + "INNER JOIN vehicle v ON fc.vec_id = v.id "
                    + "INNER JOIN area a ON fc.area_id = a.id "
                    + "WHERE  "
                    + "fc.vec_id = " + vehId + " AND "
                    + "a.type = '" + type + "' ";

            if (contract != null) {
                qs1 += " AND v.contract =" + contract + " ";

            }
            if (contractor != null) {
                qs1 += " AND v.contractor_id =" + contractor + " ";

            }
            qs1 += " AND fc.anio =" + year + " "
                    + " AND fc.fuel_type_id IS NULL";

            MySQLQuery q1 = new MySQLQuery(qs1);
            lareas = q1.getRecords(con);

            if (type.equals("mto")) {
                String qs2
                        = "SELECT fc.fuel_type_id, fc.val "
                        + "FROM forecast fc, vehicle v "
                        + "WHERE fc.vec_id = " + vehId + " ";
                if (contract != null) {
                    qs2 += " AND v.contract =" + contract + " ";

                }
                if (contractor != null) {
                    qs2 += " AND v.contractor_id =" + contractor + " ";

                }
                qs2 += " AND fc.vec_id=v.id "
                        + " AND fc.anio =" + year + " "
                        + " AND fc.area_id IS NULL";
                lfuels = new MySQLQuery(qs2).getRecords(con);
            }
        }

        for (Area ar : ars) {
            AreaCostListItem li = new AreaCostListItem();
            li.setArea(ar.getId(), ar.getName(), ar.getPuc());
            li.values = (new ArrayList<BigDecimal>());
            boolean find = false;

            for (Object[] larea : lareas) {
                if (ar.getId() == ((Integer) larea[0]).intValue()) {
                    BigDecimal val = new BigDecimal(larea[1].toString());
                    li.values.add(val);
                    find = true;
                }
            }
            if (!find) {
                li.values.add(BigDecimal.ZERO);
            }
            rep.areaCosts.add(li);
        }

        if (type.equals("mto")) {
            for (FuelType ft : ftypes) {
                FuelCostListItem li = new FuelCostListItem();
                li.setFuelType(ft.getId(), ft.getName());
                li.values = (new ArrayList<BigDecimal>());
                boolean find = false;

                for (Object[] lfuel : lfuels) {
                    if (ft.getId() == ((Integer) lfuel[0]).intValue()) {
                        BigDecimal val = new BigDecimal(lfuel[1].toString());
                        li.values.add(val);
                        find = true;
                    }
                }
                if (!find) {
                    li.values.add(BigDecimal.ZERO);
                }
                rep.fuelCosts.add(li);
            }
        }
        rep.single = (Boolean.TRUE);
        return rep;
    }

    public static MySQLReport getAnnualCostReportVeh(int idVeh, int year, Integer contractor, boolean storeMovs, Connection conn) throws Exception {
        Object[] vhRow = new MySQLQuery("SELECT plate, contract FROM vehicle WHERE id = " + idVeh).getRecord(conn);
        String vhPlate = vhRow[0].toString();
        boolean vhContract = MySQLQuery.getAsBoolean(vhRow[1]);

        //Vehicle vec = VehicleController.findVehicle(idVeh, em);
        String[] months = new String[]{"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        MySQLReport rep1 = new MySQLReport("Reporte de Gastos", "Año " + year, "Hoja 1", MySQLQuery.now(conn));
        rep1.getSubTitles().add("Vehículo : " + vhPlate);
        rep1.setVerticalFreeze(0);
        rep1.setHorizontalFreeze(3);
        rep1.setZoomFactor(80);
        //Formatos
        rep1.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        rep1.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$#,##0.00"));

        //GASTOS POR AREAS*************************************************************************************************
        //Columnas
        List<Column> cols = new ArrayList<>();
        cols.add(new Column("Código", 15, 0));
        cols.add(new Column("Área", 30, 0));

        for (String month : months) {
            cols.add(new Column(month, 17, 1));
        }
        cols.add(new Column("Total", 22, 1));

        Table tblModAreas = new Table("Gastos por Área");
        tblModAreas.setColumns(cols);
        tblModAreas.setSummaryRow(new SummaryRow("Total", 2));
        CostReport rep = findCostReportVeh("mto", 0, 0, idVeh, year, vhContract, contractor, storeMovs, conn);
        Table tblByAreas = new Table(tblModAreas);
        for (int j = 0; j < rep.areaCosts.size(); j++) {
            AreaCostListItem ar = rep.areaCosts.get(j);
            Object[] row = new Object[15];
            row[0] = ar.areaPUC;
            row[1] = ar.areaName;
            List<BigDecimal> vals = ar.values;
            BigDecimal total = BigDecimal.ZERO;
            for (int i = 0; i < vals.size(); i++) {
                row[i + 2] = vals.get(i);
                total = total.add(vals.get(i));
            }
            row[14] = total;
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                tblByAreas.addRow(row);
            }
        }
        if (tblByAreas.getData() != null && tblByAreas.getData().length > 0) {
            rep1.getTables().add(tblByAreas);
        }

        //GASTOS POR RUBROS*************************************************************************************************
        CostReport repRub = findCostReportVeh("rub", 0, 0, idVeh, year, vhContract, contractor, storeMovs, conn);
        //Columnas
        List<Column> colsRubro = new ArrayList<>();
        colsRubro.add(new Column("Código", 15, 0));
        colsRubro.add(new Column("Rubro", 30, 0));
        for (String month : months) {
            colsRubro.add(new Column(month, 17, 1));
        }
        colsRubro.add(new Column("Total", 25, 1));

        Table tblModRubro = new Table("Gastos por Rubro");
        tblModRubro.setColumns(colsRubro);
        tblModRubro.setSummaryRow(new SummaryRow("Total", 2));
        //Fin config
        Table tblRubros = new Table(tblModRubro);
        for (int j = 0; j < repRub.areaCosts.size(); j++) {
            AreaCostListItem ar = repRub.areaCosts.get(j);
            Object[] row = new Object[15];
            row[0] = ar.areaPUC;
            row[1] = ar.areaName;
            List<BigDecimal> vals = ar.values;
            BigDecimal total = BigDecimal.ZERO;
            for (int i = 0; i < vals.size(); i++) {
                row[i + 2] = vals.get(i);
                total = total.add(vals.get(i));
            }
            row[14] = total;
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                tblRubros.addRow(row);
            }
        }
        if (tblRubros.getData() != null && tblRubros.getData().length > 0) {
            rep1.getTables().add(tblRubros);
        }
        //GASTOS POR COMBUSTIBLES*************************************************************************************************

        Table tblByComb = new Table(tblModRubro);
        tblByComb.setTitle("Gastos por Combustibles");
        for (int j = 0; j < rep.fuelCosts.size(); j++) {
            FuelCostListItem ar = rep.fuelCosts.get(j);
            Object[] row = new Object[15];
            row[0] = ar.fuelTypeCode;
            row[1] = ar.fuelTypeName;
            List<BigDecimal> vals = ar.values;
            BigDecimal total = BigDecimal.ZERO;
            for (int i = 0; i < vals.size(); i++) {
                row[i + 2] = vals.get(i);
                total = total.add(vals.get(i));
            }
            row[14] = total;
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                tblByComb.addRow(row);
            }
        }
        if (tblByComb.getData() != null && tblByComb.getData().length > 0) {
            rep1.getTables().add(tblByComb);
        }
        boolean income = new MySQLQuery("SELECT income FROM mto_cfg").getAsBoolean(conn);
        boolean trips = new MySQLQuery("SELECT trips FROM mto_cfg").getAsBoolean(conn);
        //INGRESOS POR RUBRO    *************************************************************************************************
        CostReport repIng = findCostReportVeh("ing", 0, 0, idVeh, year, vhContract, contractor, storeMovs, conn);
        List<Column> colsIng = new ArrayList<>();
        colsIng.add(new Column("Código", 15, 0));
        colsIng.add(new Column("Rubro", 30, 0));
        for (String month : months) {
            colsIng.add(new Column(month, 17, 1));
        }
        colsIng.add(new Column("Total", 25, 1));

        Table tblModIng = new Table("Ingresos por Rubro");
        tblModIng.setColumns(colsRubro);
        tblModIng.setSummaryRow(new SummaryRow("Total", 2));

        Table tblByIngs = new Table(tblModIng);

        if (income) {
            for (int j = 0; j < repIng.areaCosts.size(); j++) {
                AreaCostListItem ar = repIng.areaCosts.get(j);
                Object[] row = new Object[15];
                row[0] = ar.areaPUC;
                row[1] = ar.areaName;
                List<BigDecimal> vals = ar.values;
                BigDecimal total = BigDecimal.ZERO;
                for (int i = 0; i < vals.size(); i++) {
                    row[i + 2] = vals.get(i);
                    total = total.add(vals.get(i));
                }
                row[14] = total;
                if (total.compareTo(BigDecimal.ZERO) > 0) {
                    tblByIngs.addRow(row);
                }
            }
            if (tblByIngs.getData() != null && tblByIngs.getData().length > 0) {
                rep1.getTables().add(tblByIngs);
            }
        }
        //FLETES**************************************************************************************************************************************************************************************************
        Table tblByFletes = new Table(tblByIngs);
        if (trips) {
            // reportar la informacion de ingresos de la flota
            tblByFletes.setTitle("Ingresos del Vehículo");
            Object[] rowIng = new Object[15];
            rowIng[0] = "";
            rowIng[1] = "Fletes";
            BigDecimal totIng = BigDecimal.ZERO;
            String str = "SELECT "
                    + "SUM(t.price) "//0
                    + "FROM "
                    + "mto_trip AS t "
                    + "WHERE "
                    + "YEAR(t.trip_date) = " + year + " AND "
                    + "MONTH(t.trip_date )= ?1 AND "
                    + "t.veh_id = " + idVeh + " ";

            for (int j = 0; j < 12; j++) {
                MySQLQuery q = new MySQLQuery(str);
                q.setParam(1, j + 1);
                try {
                    rowIng[j + 2] = q.getAsBigDecimal(conn, true);
                    totIng = totIng.add((BigDecimal) rowIng[j + 2]);
                } catch (NoResultException nr) {
                    rowIng[j + 2] = BigDecimal.ZERO;
                    totIng = totIng.add(BigDecimal.ZERO);
                }
            }
            rowIng[14] = totIng;
            tblByFletes.addRow(rowIng);
            if (tblByFletes.getData() != null && tblByFletes.getData().length > 0) {
                rep1.getTables().add(tblByFletes);
            }
        }
        //TOTALES **************************************************************************************************************************************************************************************************
        if (income || trips) {
            List<Column> colsResult = new ArrayList<>();
            colsResult.add(new Column("", 15, 0));
            colsResult.add(new Column("Total", 30, 0));
            for (String month : months) {
                colsResult.add(new Column(month, 17, 1));
            }
            colsResult.add(new Column("Total", 22, 1));
            Table tblResults = new Table("Totales por Columna");
            tblResults.setColumns(colsResult);

            Table tblByResults = new Table(tblResults);
            Object[][] rowTot = new Object[3][15];
            for (int i = 0; i < 15; i++) {//inicializar el vector
                rowTot[0][i] = BigDecimal.ZERO;
                rowTot[1][i] = BigDecimal.ZERO;
                rowTot[2][i] = BigDecimal.ZERO;
            }
            rowTot[0][0] = "";
            rowTot[0][1] = "Gastos";

            rowTot[1][0] = "";
            rowTot[1][1] = "Ingresos";

            rowTot[2][0] = "";
            rowTot[2][1] = "Resultado";

            BigDecimal tout = BigDecimal.ZERO;
            BigDecimal tin = BigDecimal.ZERO;
            BigDecimal tresult = BigDecimal.ZERO;

            if (tblByAreas.getData() != null) {
                for (int i = 0; i < tblByAreas.getData().length; i++) { // recorreindo la tabla de los vlores de las areas
                    Object[] row = tblByAreas.getData()[i];
                    for (int j = 2; j < row.length; j++) {
                        rowTot[2][j] = ((BigDecimal) rowTot[2][j]).subtract((BigDecimal) row[j]);
                        rowTot[0][j] = ((BigDecimal) rowTot[0][j]).add((BigDecimal) row[j]);
                        tresult = ((BigDecimal) tresult).subtract((BigDecimal) row[j]);
                        tout = ((BigDecimal) tout).add((BigDecimal) row[j]);
                    }
                }
            }
            if (tblRubros.getData() != null) {
                for (int i = 0; i < tblRubros.getData().length; i++) { // recorreindo la tabla de los valores por rubro
                    Object[] row = tblRubros.getData()[i];
                    for (int j = 2; j < row.length; j++) {
                        rowTot[2][j] = ((BigDecimal) rowTot[2][j]).subtract((BigDecimal) row[j]);
                        rowTot[0][j] = ((BigDecimal) rowTot[0][j]).add((BigDecimal) row[j]);
                        tresult = ((BigDecimal) tresult).subtract((BigDecimal) row[j]);
                        tout = ((BigDecimal) tout).add((BigDecimal) row[j]);
                    }
                }
            }

            if (tblByComb.getData() != null) {
                for (int i = 0; i < tblByComb.getData().length; i++) { // recorreindo la tabla de los vlores por combustibles
                    Object[] row = tblByComb.getData()[i];
                    for (int j = 2; j < row.length; j++) {
                        rowTot[2][j] = ((BigDecimal) rowTot[2][j]).subtract((BigDecimal) row[j]);
                        rowTot[0][j] = ((BigDecimal) rowTot[0][j]).add((BigDecimal) row[j]);
                        tresult = ((BigDecimal) tresult).subtract((BigDecimal) row[j]);
                        tout = ((BigDecimal) tout).add((BigDecimal) row[j]);
                    }
                }
            }

            if (tblByIngs.getData() != null) {
                for (int i = 0; i < tblByIngs.getData().length; i++) { // recorriendo los ingresos
                    Object[] row = tblByIngs.getData()[i];
                    for (int j = 2; j < row.length; j++) {
                        rowTot[2][j] = ((BigDecimal) rowTot[2][j]).add((BigDecimal) row[j]);
                        rowTot[1][j] = ((BigDecimal) rowTot[1][j]).add((BigDecimal) row[j]);
                        tresult = ((BigDecimal) tresult).add((BigDecimal) row[j]);
                        tin = ((BigDecimal) tin).add((BigDecimal) row[j]);
                    }
                }
            }
            if (tblByFletes.getData() != null) {
                for (int i = 0; i < tblByFletes.getData().length; i++) { // recorreindo la tabla de los vlores por combustibles
                    Object[] row = tblByFletes.getData()[i];
                    for (int j = 2; j < row.length; j++) {
                        rowTot[2][j] = ((BigDecimal) rowTot[2][j]).add((BigDecimal) row[j]);
                        rowTot[1][j] = ((BigDecimal) rowTot[1][j]).add((BigDecimal) row[j]);
                        tresult = ((BigDecimal) tresult).add((BigDecimal) row[j]);
                        tin = ((BigDecimal) tin).add((BigDecimal) row[j]);
                    }
                }
            }
            rowTot[0][14] = tout;
            rowTot[1][14] = tin;
            rowTot[2][14] = tresult;

            for (int i = 0; i < rowTot.length; i++) {
                Object[] row = rowTot[i];
                tblByResults.addRow(row);
            }
            if (tblByResults.getData() != null && tblByResults.getData().length > 0) {
                rep1.getTables().add(tblByResults);
            }
        }
        return rep1;
    }

    public static void findForecast(int year, double porc, Integer contractor, Connection con) throws Exception {
        new MySQLQuery("DELETE FROM forecast WHERE anio = " + year).executeDelete(con);

        Date fBegin = new GregorianCalendar(year - 1, GregorianCalendar.JANUARY, 1, 0, 0, 0).getTime();
        Date fEnd = new GregorianCalendar(year - 1, GregorianCalendar.DECEMBER, 31, 0, 0, 0).getTime();
        BigDecimal fac = new BigDecimal(((porc / 100.0d) + 1) / 12);

        Area[] ars = Area.getSuperAreas(null);
        FuelType[] ftypes = FuelType.getAllFuelTypes();

        Object[][] vecs = new MySQLQuery("SELECT id FROM vehicle WHERE active = 1").getRecords(con);

        for (int i = 0; i < vecs.length; i++) {
            Object[] veh = vecs[i];
            int vehId = MySQLQuery.getAsInteger(veh[0]);
            CostReport cr = findCostReport(null, 0, 0, vehId, fBegin, fEnd, false, contractor, true, con);
            int months = findRegisteredMonths(vehId, year - 1, con);

            for (Area ar : ars) {
                BigDecimal val = BigDecimal.ZERO;
                for (int k = 0; k < cr.areaCosts.size(); k++) {
                    AreaCostListItem ac = cr.areaCosts.get(k);
                    if ((ac.areaId == ar.getId()) && months > 0) {
                        val = (ac.values.get(0).multiply(fac).divide(new BigDecimal(months), 5, RoundingMode.UP).multiply(new BigDecimal(12)));
                        break;
                    }
                }
                MySQLQuery ins = new MySQLQuery("INSERT INTO forecast SET area_id = " + ar.getId() + ", val = ?1, vec_id = " + vehId + ", anio = " + year);
                ins.setParam(1, val);
                ins.executeInsert(con);
            }

            for (FuelType ftype : ftypes) {
                BigDecimal val = BigDecimal.ZERO;
                for (int k = 0; k < cr.fuelCosts.size(); k++) {
                    FuelCostListItem fci = cr.fuelCosts.get(k);
                    if (fci.fuelTypeId == ftype.getId() && months > 0) {
                        val = fci.values.get(0).multiply(fac).divide(new BigDecimal(months), 5, RoundingMode.UP).multiply(new BigDecimal(12));
                        break;
                    }
                }
                MySQLQuery ins = new MySQLQuery("INSERT INTO forecast SET fuel_type_id = " + ftype.getId() + ", val = ?1, vec_id = " + vehId + ", anio = " + year);
                ins.setParam(1, val);
                ins.executeInsert(con);
            }
        }
    }

    public static int findRegisteredMonths(int idVeh, int year, Connection con) throws Exception {

        String qs1 = "select count(*) from "
                + "(SELECT DISTINCT EXTRACT(MONTH FROM begin) "
                + "FROM work_order "
                + "WHERE "
                + "vehicle_id = ?1 AND EXTRACT(YEAR FROM begin) = ?2) d";

        String qs2 = "select count(*) from "
                + "(SELECT DISTINCT EXTRACT(MONTH FROM days) "
                + "FROM fuel_load "
                + "WHERE "
                + "vehicle_id = ?1 AND EXTRACT(YEAR FROM days) = ?2) d";

        MySQLQuery q1 = new MySQLQuery(qs1);
        q1.setParam(1, idVeh);
        q1.setParam(2, year);
        Integer r1 = q1.getAsInteger(con);

        MySQLQuery q2 = new MySQLQuery(qs2);
        q2.setParam(1, idVeh);
        q2.setParam(2, year);
        Integer r2 = q2.getAsInteger(con);

        int n1 = (r1 != null ? r1 : 0);
        int n2 = (r2 != null ? r2 : 0);

        return Math.max(n2, n1);

    }

    //DOTACION
    public static MySQLReport getElementsTotal(int cityId, int entId, Connection conn) throws Exception {
        MySQLReport rep = new MySQLReport("Resumen Total de Dotación", "", "resumen_dotación ", MySQLQuery.now(conn));
        rep.setZoomFactor(80);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));//1

        Table tb = new Table("Resumen Total de Dotación");
        TableHeader header = new TableHeader();

        HashMap<Enterprise, List<MySQLAgency>> agencies = new HashMap<Enterprise, List<MySQLAgency>>();
        int colNum = 0;

        Object[][] ents = new MySQLQuery("SELECT id, name, short_name FROM enterprise ORDER BY name").getRecords(conn);

        for (int i = 0; i < ents.length; i++) {
            Enterprise enterprise = new Enterprise();
            enterprise.id = MySQLQuery.getAsInteger(ents[i][0]);
            enterprise.name = ents[i][1].toString();
            enterprise.shortName = ents[i][2].toString();
            if (entId == 0 || (entId > 0 && enterprise.id == entId)) {

                Object[][] cities = new MySQLQuery("SELECT c.id FROM city as c, agency as a WHERE a.enterprise_id = " + enterprise.id + " AND a.city_id = c.id ORDER BY c.name ASC").getRecords(conn);

                List<MySQLAgency> ags = new ArrayList<MySQLAgency>();
                for (int j = 0; j < cities.length; j++) {
                    Object[] city = cities[j];
                    if (cityId == 0 || (cityId > 0 && MySQLQuery.getAsInteger(city[0]) == cityId)) {
                        try {
                            MySQLAgency agency = MySQLAgency.getAgency(MySQLQuery.getAsInteger(city[0]), enterprise.id);
                            ags.add(agency);
                        } catch (Exception ex) {
                        }
                    }
                }
                if (ags.size() > 0) {
                    agencies.put(enterprise, ags);
                    colNum += ags.size();
                }
            }
        }

        //////cabeceras//////
        Iterator<Entry<Enterprise, List<MySQLAgency>>> it = agencies.entrySet().iterator();
        tb.getColumns().add(new Column("Elemento", 24, 0));
        header.getColums().add(new HeaderColumn("Elemento", 1, 2));
        while (it.hasNext()) {
            Entry<Enterprise, List<MySQLAgency>> entry = it.next();
            List<MySQLAgency> enAgs = entry.getValue();
            header.getColums().add(new HeaderColumn(entry.getKey().shortName, enAgs.size(), 1));
            for (int i = 0; i < enAgs.size(); i++) {
                MySQLAgency agency = enAgs.get(i);
                String cName = new MySQLQuery("SELECT name FROM city WHERE id = " + agency.getCityId()).getAsString(conn);
                tb.getColumns().add(new Column(cName, 20, 1));
            }
        }
        tb.getColumns().add(new Column("Total", 10, 1));
        header.getColums().add(new HeaderColumn("Total", 1, 2));
        tb.getHeaders().add(header);
        //datos
        String str = "SELECT "
                + "count(me.id) "
                + "FROM "
                + "mto_element AS me "
                + "INNER JOIN mto_veh_element AS mve ON me.id = mve.element_id AND me.id = ?1 "
                + "INNER JOIN vehicle AS v ON mve.vehicle_id = v.id AND v.active = 1 AND v.visible = 1 AND v.agency_id = ?2 "
                + "GROUP BY "
                + "me.id";

        Object[][] elems = new MySQLQuery("SELECT id, description FROM mto_element ORDER BY description").getRecords(conn);
        for (int j = 0; j < elems.length; j++) {
            Object[] elem = elems[j];
            Object[] row = new Object[colNum + 2];
            row[0] = elem[1];
            it = agencies.entrySet().iterator();
            int sum = 0;
            int col = 1;
            while (it.hasNext()) {
                Entry<Enterprise, List<MySQLAgency>> entry = it.next();
                List<MySQLAgency> enAgs = entry.getValue();
                for (int i = 0; i < enAgs.size(); i++, col++) {
                    MySQLAgency agency = enAgs.get(i);
                    MySQLQuery q = new MySQLQuery(str);
                    q.setParam(1, elem[0]);
                    q.setParam(2, agency.getId());
                    try {
                        Long num = q.getAsLong(conn);
                        num = (num != null ? num : 0);
                        row[col] = num;
                        sum = sum + num.intValue();
                    } catch (NoResultException e) {
                    }

                }
                row[1 + colNum] = sum;
            }
            tb.addRow(row);
        }

        if (tb.getData() != null && tb.getData().length > 0) {
            tb.setSummaryRow(new SummaryRow("Totales", 1));
            rep.getTables().add(tb);
        }
        return rep;
    }

    public static MySQLReport getElementsAgency(int cityId, int entId, int elementId, Connection conn) throws Exception {
        int total = 0; //cantidad un elemento en la empresa
        MySQLReport rep;
        if (elementId == 0) {
            rep = new MySQLReport("Reporte General de Dotación", "Lista de Elementos", "dotación", MySQLQuery.now(conn));
        } else {
            String desc = new MySQLQuery("SELECT description FROM mto_element WHERE id = " + elementId).getAsString(conn);
            rep = new MySQLReport("Reporte General de Dotación", "Elemento: " + desc, "dotación", MySQLQuery.now(conn));
        }

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "dd/MM/yyyy"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//3
        rep.getFormats().get(3).setWrap(true);
        rep.setZoomFactor(85);

        Table tb = new Table("model");
        tb.getColumns().add(new Column("Int.", 11, 0));
        tb.getColumns().add(new Column("Placa", 10, 0));
        tb.getColumns().add(new Column("Tipo", 24, 0));
        tb.getColumns().add(new Column("Modelo", 9, 1));
        if (elementId == 0) {
            tb.getColumns().add(new Column("Elemento", 24, 0));
        }
        tb.getColumns().add(new Column("Serial", 15, 0));
        tb.getColumns().add(new Column("Entrega", 15, 2));
        tb.getColumns().add(new Column("Devolución", 15, 2));
        tb.getColumns().add(new Column("Vencimiento", 15, 2));
        tb.getColumns().add(new Column("Observación", 30, 3));

        MySQLAgency[] ags = MySQLAgency.getAgencies(cityId, entId);

        //listado de vehiculos por agencia
        MySQLQuery vehQuery = new MySQLQuery("SELECT "
                + "v.id, " //0
                + "v.internal, " //1
                + "v.plate, " //2
                + "vt.`name`, "//3
                + "v.model "//4
                + "FROM "
                + "vehicle AS v "
                + "INNER JOIN vehicle_type AS vt ON v.vehicle_type_id = vt.id AND v.active = 1 AND v.visible = 1 AND v.agency_id = ?1");

        //elementos por veh
        String qs = "SELECT "
                + "me.description, "//0
                + "mve.serial, "//1
                + "mve.begin_date, "//2
                + "mve.end_date, "//3
                + "mve.dt_rev_date, "//
                + "mve.observation "//
                + "FROM "
                + "mto_element AS me "
                + "INNER JOIN mto_veh_element AS mve ON mve.element_id = me.id "
                + "WHERE "
                + "mve.vehicle_id = ?1 ";
        if (elementId > 0) {
            qs += "And me.id= " + elementId + " ";
        }

        MySQLQuery elementQuery = new MySQLQuery(qs);

        for (int i = 0; i < ags.length; i++) {
            MySQLAgency ag = ags[i];
            vehQuery.setParam(1, ag.getId());
            Object[][] vecs = vehQuery.getRecords(conn);

            Table bTable = new Table("Dotación");
            boolean ban = true;
            int celem = 0; //cuenta el nuemro de lemento por agencia,
            for (int j = 0; j < vecs.length; j++) {
                elementQuery.setParam(1, vecs[j][0]);
                Object[][] elements = elementQuery.getRecords(conn);
                for (int k = 0; k < elements.length; k++) {
                    celem++;
                    total++;
                    if (ban) {
                        if (elementId == 0) {
                            String cName = new MySQLQuery("SELECT name FROM city WHERE id = " + ag.getCityId()).getAsString(conn);
                            String eName = new MySQLQuery("SELECT name FROM enterprise WHERE id = " + ag.getEnterpriseId()).getAsString(conn);
                            bTable = new Table(eName + " - " + cName);
                        } else {
                            bTable = new Table("Dotación");
                        }
                        rep.getTables().add(bTable);
                        bTable.setColumns(tb.getColumns());
                        ban = false;
                    }
                    Object[] row;
                    if (elementId == 0) {
                        row = new Object[10];
                    } else {
                        row = new Object[9];
                    }
                    bTable.addRow(row);
                    row[0] = vecs[j][1];
                    row[1] = vecs[j][2];
                    row[2] = vecs[j][3];
                    row[3] = vecs[j][4];
                    if (elementId == 0) {
                        row[4] = elements[k][0];
                        row[5] = elements[k][1];
                        row[6] = elements[k][2];
                        row[7] = elements[k][3];
                        row[8] = elements[k][4];
                        row[9] = elements[k][5];

                    } else {
                        row[4] = elements[k][1];
                        row[5] = elements[k][2];
                        row[6] = elements[k][3];
                        row[7] = elements[k][4];
                        row[8] = elements[k][5];
                    }
                }
                if (elementId > 0) {
                    String cName = new MySQLQuery("SELECT name FROM city WHERE id = " + ag.getCityId()).getAsString(conn);
                    String eName = new MySQLQuery("SELECT name FROM enterprise WHERE id = " + ag.getEnterpriseId()).getAsString(conn);
                    bTable.setTitle(eName + " - " + cName + " Cantidad: " + celem);
                }
            }
        }
        if (elementId > 0) {
            rep.getSubTitles().add("Cantidad: " + total);
        }

        return rep;
    }

    public static MySQLReport getElementsVeh(int vehId, Connection conn) throws Exception {
        Object[] vhRow = new MySQLQuery("SELECT plate, internal FROM vehicle WHERE id = " + vehId).getRecord(conn);

        MySQLReport rep = new MySQLReport("Reporte de Elementos de Dotación por Vehículo", "Placa:" + vhRow[0] + "  Interno:" + vhRow[1], "dotación_veh", MySQLQuery.now(conn));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().get(0).setWrap(true);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "dd/MM/yyyy"));//1
        rep.setVerticalFreeze(7);
        rep.setZoomFactor(85);

        Table tb = new Table("Elementos de Dotación");
        tb.getColumns().add(new Column("Elemento", 25, 0));
        tb.getColumns().add(new Column("Serial", 15, 0));
        tb.getColumns().add(new Column("Desde", 12, 1));
        tb.getColumns().add(new Column("Hasta", 12, 1));
        tb.getColumns().add(new Column("Observación", 30, 0));

        MySQLQuery q = new MySQLQuery("SELECT "
                + "me.description, "//0
                + "mve.serial, " //1
                + "mve.begin_date, " //2
                + "mve.end_date,"//3
                + "mve.observation " //4
                + "FROM "
                + "mto_element AS me "
                + "INNER JOIN mto_veh_element AS mve ON mve.element_id = me.id "
                + "WHERE "
                + "mve.vehicle_id = ?1");
        q.setParam(1, vehId);
        Object[][] res = q.getRecords(conn);

        for (int i = 0; i < res.length; i++) {

            Object[] row;
            row = new Object[5];
            tb.addRow(row);

            row[0] = res[i][0]; // elemento
            row[1] = res[i][1];
            row[2] = res[i][2];
            row[3] = res[i][3];
            row[4] = res[i][4];
        }
        if (tb.getData() != null && tb.getData().length > 0) {
            rep.getTables().add(tb);
        }
        return rep;
    }

    public static MySQLReport getDriversVeh(int vehId, Connection conn) throws Exception {
        Object[] vhRow = new MySQLQuery("SELECT plate, internal FROM vehicle WHERE id = " + vehId).getRecord(conn);
        MySQLReport rep = new MySQLReport("Historico de Conductores Por Vehículo", "Vehículo Placa:" + vhRow[0] + " " + (vhRow[1] != null ? " Interno:" + vhRow[1] : ""), "conductores", MySQLQuery.now(conn));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().get(0).setWrap(true);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "dd/MM/yyyy"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$ #,##0.0##"));//3
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.0##"));//4
        rep.setZoomFactor(80);

        Table tb = new Table("Conductores");

        tb.getColumns().add(new Column("Conductor", 30, 0));
        tb.getColumns().add(new Column("Inicio", 12, 1));
        tb.getColumns().add(new Column("Fin", 12, 1));
        tb.getColumns().add(new Column("km", 12, 2));
        tb.getColumns().add(new Column("Combustible", 17, 3));
        tb.getColumns().add(new Column("gal", 12, 4));
        tb.getColumns().add(new Column("vlr/km", 14, 3));
        tb.getColumns().add(new Column("km/gal", 10, 4));

        MySQLQuery driversQuery = new MySQLQuery("SELECT "
                + "e.first_name, " //0
                + "e.last_name, " //1
                + "dv.`begin`, " //2
                + "dv.`end` " //3
                + "FROM "
                + "driver_vehicle AS dv "
                + "INNER JOIN employee AS e ON e.id = dv.driver_id "
                + "WHERE "
                + "dv.vehicle_id = ?1");
        driversQuery.setParam(1, vehId);
        Object[][] drivers = driversQuery.getRecords(conn);

        for (int i = 0; i < drivers.length; i++) {

            Object[] row;
            row = new Object[8];
            tb.addRow(row);

            row[0] = drivers[i][0] + " " + drivers[i][1];
            row[1] = drivers[i][2];
            row[2] = drivers[i][3];
            MySQLQuery fuelQuery = new MySQLQuery(fuelStrQuery);
            fuelQuery.setParam(1, vehId);
            fuelQuery.setParam(2, drivers[i][2]);
            if (drivers[i][3] == null) {
                fuelQuery.setParam(3, new Date());
            } else {
                fuelQuery.setParam(3, drivers[i][3]);
            }
            BigDecimal totalFuel = BigDecimal.ZERO;
            BigDecimal km = BigDecimal.ZERO;
            BigDecimal gls = BigDecimal.ZERO;

            Object[][] fuel = fuelQuery.getRecords(conn);

            totalFuel = (fuel.length > 0 && fuel[0][0] != null) ? (BigDecimal) fuel[0][0] : BigDecimal.ZERO;
            km = (fuel.length > 0 && fuel[0][1] != null) ? (BigDecimal) fuel[0][1] : BigDecimal.ZERO;
            row[3] = km.compareTo(BigDecimal.ZERO) > 0 ? km : null;
            row[4] = totalFuel;
            gls = (fuel.length > 0 && fuel[0][2] != null) ? ((BigDecimal) fuel[0][2]) : BigDecimal.ZERO;
            row[5] = gls.compareTo(BigDecimal.ZERO) > 0 ? gls : null;

            if (km != null) {
                if (km.compareTo(BigDecimal.ZERO) > 0) {
                    row[6] = totalFuel.divide(km, RoundingMode.CEILING);
                }
            }

            if (gls != null) {
                if (km.compareTo(BigDecimal.ZERO) > 0) {
                    row[7] = km.divide(gls, RoundingMode.CEILING);
                }
            }
        }
        if (tb.getData() != null && tb.getData().length > 0) {
            tb.setSummaryRow(new SummaryRow("Totales", 3));
            rep.getTables().add(tb);
        }
        return rep;
    }

    public static MySQLReport getVehsDriver(int driverId, Boolean active, Connection conn) throws Exception {
        Object[] empRow = new MySQLQuery("SELECT first_name, last_name, document FROM employee WHERE id = " + driverId).getRecord(conn);
        String firstName = empRow[0].toString();
        String lastName = empRow[1].toString();
        String document = empRow[2].toString();

        MySQLReport rep = new MySQLReport("Reporte de Vehículos por Conductor ", "Conductor: " + firstName + " " + lastName + " Documento:" + document, "vehs_conductor", MySQLQuery.now(conn));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().get(0).setWrap(true);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "dd/MM/yyyy"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "$ #,##0.0##"));//3
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0.0##"));//4
        rep.setZoomFactor(80);

        Table tb = new Table("Conductor: " + firstName + " " + lastName);
        rep.getTables().add(tb);
        tb.getColumns().add(new Column("Int.", 11, 0));
        tb.getColumns().add(new Column("Placa", 10, 0));
        tb.getColumns().add(new Column("Inicio", 12, 1));
        tb.getColumns().add(new Column("Fin", 12, 1));
        tb.getColumns().add(new Column("km", 12, 2));
        tb.getColumns().add(new Column("Combustible", 17, 3));
        tb.getColumns().add(new Column("gal", 12, 4));
        tb.getColumns().add(new Column("vlr/km", 14, 3));
        tb.getColumns().add(new Column("km/gal", 10, 4));

        MySQLQuery vehQuery = new MySQLQuery("SELECT "
                + "v.id, " //0
                + "v.internal, " //1
                + "v.plate, "//2
                + "dv.`begin`, " //3
                + "dv.`end`, "//4
                + "vt.`name` "//5
                + "FROM "
                + "driver_vehicle AS dv "
                + "INNER JOIN vehicle AS v ON dv.vehicle_id = v.id AND dv.driver_id = ?1 "
                + "INNER JOIN vehicle_type AS vt ON v.vehicle_type_id = vt.id "
                + "INNER JOIN employee AS emp ON emp.id = dv.driver_id "
                + "WHERE "
                + "emp.active = " + active + " ");

        vehQuery.setParam(1, driverId);
        Object[][] vecs = vehQuery.getRecords(conn);

        for (int i = 0; i < vecs.length; i++) {

            Object[] row;
            row = new Object[9];
            tb.addRow(row);

            row[0] = vecs[i][1];
            row[1] = vecs[i][2];
            row[2] = vecs[i][3];
            row[3] = vecs[i][4];
            MySQLQuery fuelQuery = new MySQLQuery(fuelStrQuery);
            fuelQuery.setParam(1, vecs[i][0]);
            fuelQuery.setParam(2, vecs[i][3]);
            if (vecs[i][4] == null) {
                fuelQuery.setParam(3, new Date());
            } else {
                fuelQuery.setParam(3, vecs[i][4]);
            }
            BigDecimal totalFuel = BigDecimal.ZERO;
            Object[][] fuel = fuelQuery.getRecords(conn);
            totalFuel = (fuel.length > 0 && fuel[0][0] != null ? (BigDecimal) fuel[0][0] : BigDecimal.ZERO);
            BigDecimal km = (fuel.length > 0 && fuel[0][1] != null ? (BigDecimal) fuel[0][1] : BigDecimal.ZERO);
            row[4] = km.compareTo(BigDecimal.ZERO) > 0 ? km : null;
            row[5] = totalFuel;
            BigDecimal gls = (fuel.length > 0 && fuel[0][2] != null ? ((BigDecimal) fuel[0][2]) : BigDecimal.ZERO);
            row[6] = gls.compareTo(BigDecimal.ZERO) > 0 ? gls : null;

            if (km != null) {
                if (km.compareTo(BigDecimal.ZERO) > 0) {
                    row[7] = totalFuel.divide(km, RoundingMode.CEILING);
                }
            }

            if (gls != null) {
                if (km.compareTo(BigDecimal.ZERO) > 0) {
                    row[8] = km.divide(gls, RoundingMode.CEILING);
                }
            }
        }
        if (tb.getData() != null && tb.getData().length > 0) {
            tb.setSummaryRow(new SummaryRow("Totales", 4));
        }
        return rep;
    }

    public static MySQLReport getComparativeCosts(Integer year, Integer vehId, String vhPlate, int cityId, int entId, String cName, String eName, Boolean type, Connection ep) throws Exception {
        MtoCfg mtoCfg = new MtoCfg().select(1, ep);
        int[] monthsIds = new int[]{Calendar.JANUARY, Calendar.FEBRUARY, Calendar.MARCH, Calendar.APRIL, Calendar.MAY, Calendar.JUNE, Calendar.JULY, Calendar.AUGUST, Calendar.SEPTEMBER, Calendar.OCTOBER, Calendar.NOVEMBER, Calendar.DECEMBER};
        String[] months = new String[]{"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        int[] years = new int[]{year, year - 1, year - 2};

        MySQLReport rep = new MySQLReport("Reporte Comparativo por Años", "", "comp_años", MySQLQuery.now(ep));
        rep.setVerticalFreeze(0);
        rep.setHorizontalFreeze(0);
        rep.setZoomFactor(80);
        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));
        //Columnas
        List<Column> cols = new ArrayList<>();
        cols.add(new Column("Año", 11, 0));
        for (String month : months) {
            cols.add(new Column(month, 15, 1));
        }
        String filter;
        if (mtoCfg.workOrderFlow) {
            filter = " (wo.flow_status ='done' OR wo.flow_status='approved') AND ";
            if (mtoCfg.filterOrderApprov) {
                filter += "(  "
                        + "SELECT MAX(f.checked)   "
                        + "FROM mto_flow_check AS f   "
                        + "WHERE f.work_order_id = wo.id  "
                        + "AND f.flow_step_id =   "
                        + "(SELECT st.id  "
                        + "FROM mto_flow_step st  "
                        + "WHERE st.flow_id = wo.flow_id  "
                        + "ORDER BY st.place DESC  "
                        + "LIMIT 1  "
                        + ") AND f.`status` = 'ok' ) BETWEEN ?1 AND ?2 ";
            } else {
                filter += "wo.create_date BETWEEN ?1 AND ?2  ";
            }
        } else {
            filter = " wo.`begin` BETWEEN ?1 AND ?2 "
                    + "AND wo.flow_status ='done'  ";
        }
        Table tblMod = new Table("");
        tblMod.setColumns(cols);
        //Cabecera
        TableHeader header = new TableHeader();
        tblMod.getHeaders().add(header);
        header.getColums().add(new HeaderColumn("Año", 1, 2));
        header.getColums().add(new HeaderColumn("Meses", 12, 1));

        //combustible
        String sfuels = "SELECT SUM(fl.cost) "
                + "FROM fuel_load  fl "
                + "INNER JOIN vehicle AS v ON fl.vehicle_id = v.id "
                + "WHERE "
                + "?1 <= fl.days AND "
                + "fl.days <= ?2 ";

        //combustible en galones
        String sfuelsGals = "SELECT SUM(fl.amount) "
                + "FROM fuel_load  fl "
                + "INNER JOIN vehicle AS v ON fl.vehicle_id = v.id "
                + "WHERE "
                + "?1 <= fl.days AND "
                + "fl.days <= ?2 ";

        //GASTOS POR RUBROS ****************************************************************************************************************************
        String smto = "SELECT "
                + "Sum(i.`value`) "
                + "FROM "
                + "work_order AS wo "
                + "INNER JOIN item AS i ON i.work_id = wo.id "
                + "INNER JOIN vehicle AS v ON v.id = wo.vehicle_id "
                + "WHERE "
                + "wo.canceled = 0 AND"
                + filter + " AND "
                + "wo.kind <> 'ing' AND "
                + "i.maint_task_id IS NULL ";

        //INGRESOS POR RUBROS ********************************************************************************************************************
        String sings = "SELECT "
                + "Sum(i.`value`) "
                + "FROM "
                + "work_order AS wo "
                + "INNER JOIN item AS i ON i.work_id = wo.id "
                + "INNER JOIN vehicle AS v ON v.id = wo.vehicle_id "
                + "WHERE "
                + "wo.canceled = 0 AND "
                + filter + " AND "
                + "wo.kind = 'ing' AND "
                + "i.maint_task_id IS NULL ";

        //costos de preventivo
        String spremto = "SELECT "
                + "Sum(i.`value`) "
                + "FROM "
                + "work_order AS wo "
                + "INNER JOIN item AS i ON i.work_id = wo.id "
                + "INNER JOIN vehicle AS v ON v.id = wo.vehicle_id "
                + "WHERE "
                + "wo.canceled = 0 AND "
                + filter + " AND "
                + "wo.kind <> 'ing' AND "
                + "i.maint_task_id IS NOT NULL ";

        //datos para los ingresos por fletes
        String sfletes = "SELECT "
                + "SUM(t.price) "//0
                + "FROM "
                + "mto_trip AS t "
                + "INNER JOIN vehicle AS vh ON vh.id= t.veh_id "
                + "INNER JOIN agency AS a ON a.id = vh.agency_id "
                + "WHERE ";
        if (cityId > 0) {
            sfletes += "a.city_id = " + cityId + " AND ";

        }
        if (entId > 0) {
            sfletes += "a.enterprise_id = " + entId + " AND ";
        }

        sfletes += "t.trip_date BETWEEN ?1 AND ?2 ";
        if (vehId > 0) {
            sfletes += "AND t.veh_id =" + vehId + " ";
        }

        String title;

        if (vehId <= 0) {
            if (cityId > 0 && entId > 0) {
                Agency ag = Agency.getAgencyByParams(cityId, entId, ep);
                if (ag == null) {
                    throw new Exception("No existe una agencia con esos datos");
                }
                smto += "AND wo.agency_id = " + ag.id + " ";
                sings += "AND wo.agency_id = " + ag.id + " ";
                spremto += "AND wo.agency_id = " + ag.id + " ";
                sfuels += "AND fl.agency_id = " + ag.id + " ";
                sfuelsGals += "AND fl.agency_id = " + ag.id + " ";
                title = "Agencia " + cName + " - " + eName;
            } else if (cityId > 0 && entId == 0) {
                smto += "AND wo.agency_id IN (SELECT ag.id FROM agency ag WHERE ag.city_id = " + cityId + ") ";
                sings += "AND wo.agency_id IN (SELECT ag.id FROM agency ag WHERE ag.city_id = " + cityId + ") ";
                spremto += "AND wo.agency_id IN (SELECT ag.id FROM agency ag WHERE ag.city_id = " + cityId + ") ";
                sfuels += "AND fl.agency_id IN (SELECT ag.id FROM agency ag WHERE ag.city_id = " + cityId + ") ";
                sfuelsGals += "AND fl.agency_id IN (SELECT ag.id FROM agency ag WHERE ag.city_id = " + cityId + ") ";
                title = "Agencias de " + cName;
            } else if (cityId == 0 && entId > 0) {
                smto += "AND wo.agency_id IN (SELECT ag.id FROM agency as ag WHERE ag.enterprise_id = " + entId + ") ";
                sings += "AND wo.agency_id IN (SELECT ag.id FROM agency as ag WHERE ag.enterprise_id = " + entId + ") ";
                spremto += "AND wo.agency_id IN (SELECT ag.id FROM agency as ag WHERE ag.enterprise_id = " + entId + ") ";
                sfuels += "AND fl.agency_id IN (SELECT ag.id FROM agency as ag WHERE ag.enterprise_id = " + entId + ") ";
                sfuelsGals += "AND fl.agency_id IN (SELECT ag.id FROM agency as ag WHERE ag.enterprise_id = " + entId + ") ";
                title = "Agencias de " + eName;
            } else {
                title = "Todas las agencias";
            }
            if (type != null) {
                smto += " AND v.contract = " + type + "  ";
                sings += " AND v.contract = " + type + "  ";
                spremto += " AND v.contract = " + type + "  ";
                sfuels += " AND v.contract = " + type + " ";
                sfuelsGals += " AND v.contract = " + type + " ";
                title += (type ? " Contratista" : " No contratista");
            }
        } else {
            smto += "AND wo.vehicle_id = " + vehId + " ";
            sings += "AND wo.vehicle_id = " + vehId + " ";
            spremto += "AND wo.vehicle_id = " + vehId + " ";
            sfuels += "AND fl.vehicle_id = " + vehId + " ";
            sfuelsGals += "AND fl.vehicle_id = " + vehId + " ";
            title = "Vehículo " + vhPlate;
        }

        rep.getSubTitles().add(title);
        Table tbMant = new Table(tblMod);
        tbMant.setTitle("Gastos Áreas y Rubros");
        Table tbFuel = new Table(tblMod);
        tbFuel.setTitle("Gastos Combustibles");
        Table tbFuelGals = new Table(tblMod);
        tbFuelGals.setTitle("Consumo Combustibles por Galones");
        Table tbIng = new Table(tblMod);
        tbIng.setTitle("Ingresos por Rubros");
        Table tbFlete = new Table(tblMod);
        tbFlete.setTitle("Ingresos por Fletes");
        Table tbTot = new Table(tblMod);
        tbTot.setTitle("Resultados");

        for (int i = 0; i < years.length; i++) {

            int curYear = years[i];
            Object[] rowMant = new Object[13];
            Object[] rowFuel = new Object[13];
            Object[] rowFuelGals = new Object[13];
            Object[] rowIngs = new Object[13];
            Object[] rowFlete = new Object[13];
            Object[] rowTot = new Object[13];
            rowMant[0] = String.valueOf(curYear);
            rowFuel[0] = String.valueOf(curYear);
            rowFuelGals[0] = String.valueOf(curYear);
            rowIngs[0] = String.valueOf(curYear);
            rowFlete[0] = String.valueOf(curYear);
            rowTot[0] = String.valueOf(curYear);

            for (int j = 0; j < monthsIds.length; j++) {
                int curMonth = monthsIds[j];
                Calendar calendar = Calendar.getInstance();
                calendar.set(curYear, curMonth, 1);
                int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                Date fBegin = new GregorianCalendar(curYear, curMonth, 1, 0, 0, 0).getTime();
                Date fEnd = new GregorianCalendar(curYear, curMonth, maxDay, 0, 0, 0).getTime();

                MySQLQuery mq;
                mq = new MySQLQuery(smto);
                mq.setParam(1, fBegin);
                mq.setParam(2, fEnd);
                BigDecimal mtoQ = mq.getAsBigDecimal(ep, true);

                mq = new MySQLQuery(spremto);
                mq.setParam(1, fBegin);
                mq.setParam(2, fEnd);
                BigDecimal premtoQ = mq.getAsBigDecimal(ep, true);

                mq = new MySQLQuery(sfuels);
                mq.setParam(1, fBegin);
                mq.setParam(2, fEnd);
                rowMant[j + 1] = mtoQ.add(premtoQ);
                rowFuel[j + 1] = mq.getAsBigDecimal(ep, true);

                mq = new MySQLQuery(sfuelsGals);
                mq.setParam(1, fBegin);
                mq.setParam(2, fEnd);
                rowFuelGals[j + 1] = mq.getAsBigDecimal(ep, true);

                if (mtoCfg.income) {
                    mq = new MySQLQuery(sings);
                    mq.setParam(1, fBegin);
                    mq.setParam(2, fEnd);
                    rowIngs[j + 1] = mq.getAsBigDecimal(ep, true);
                }

                if (mtoCfg.trips) {
                    mq = new MySQLQuery(sfletes);
                    mq.setParam(1, fBegin);
                    mq.setParam(2, fEnd);
                    rowFlete[j + 1] = mq.getAsBigDecimal(ep, true);
                }

                rowTot[j + 1] = BigDecimal.ZERO;
                if (mtoCfg.income || mtoCfg.trips) {

                    if (rowIngs[j + 1] != null) {
                        rowTot[j + 1] = ((BigDecimal) rowTot[j + 1]).add((BigDecimal) rowIngs[j + 1]);
                    }
                    if (rowFlete[j + 1] != null) {
                        rowTot[j + 1] = ((BigDecimal) rowTot[j + 1]).add((BigDecimal) rowFlete[j + 1]);
                    }
                    rowTot[j + 1] = ((BigDecimal) rowTot[j + 1]).subtract((BigDecimal) rowMant[j + 1]).subtract((BigDecimal) rowFuel[j + 1]);
                }
            }

            tbMant.addRow(rowMant);
            tbFuel.addRow(rowFuel);
            tbFuelGals.addRow(rowFuelGals);
            if (mtoCfg.income) {
                tbIng.addRow(rowIngs);
            }
            if (mtoCfg.trips) {
                tbFlete.addRow(rowFlete);
            }
            if (mtoCfg.income || mtoCfg.trips) {
                tbTot.addRow(rowTot);
            }
        }

        rep.getTables().add(tbMant);
        rep.getTables().add(tbFuel);
        rep.getTables().add(tbFuelGals);
        if (mtoCfg.income) {
            rep.getTables().add(tbIng);
        }
        if (mtoCfg.trips) {
            rep.getTables().add(tbFlete);
        }
        if (mtoCfg.income || mtoCfg.trips) {
            rep.getTables().add(tbTot);
        }

        return rep;
    }

    public static MySQLReport getComparativeAreRubr(int areaId, String nameArea, int year, Connection con) throws Exception {
        int[] monthsIds = new int[]{Calendar.JANUARY, Calendar.FEBRUARY, Calendar.MARCH, Calendar.APRIL, Calendar.MAY, Calendar.JUNE, Calendar.JULY, Calendar.AUGUST, Calendar.SEPTEMBER, Calendar.OCTOBER, Calendar.NOVEMBER, Calendar.DECEMBER};
        String[] months = new String[]{"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        int[] years = new int[]{year, year - 1, year - 2};

        MySQLReport rep = new MySQLReport("Reporte Comparativo por Años", "", "comp_años", MySQLQuery.now(con));
        rep.setVerticalFreeze(0);
        rep.setHorizontalFreeze(0);
        rep.setZoomFactor(80);
        //Formatos
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#,##0"));
        //Columnas
        List<Column> cols = new ArrayList<>();
        cols.add(new Column("Año", 11, 0));
        for (String month : months) {
            cols.add(new Column(month, 15, 1));
        }

        Object[] cfg = new MySQLQuery("SELECT income, trips, store, work_order_flow, filter_order_approv  FROM mto_cfg").getRecord(con);
        boolean flow = MySQLQuery.getAsBoolean(cfg[3]);
        boolean filterOrderApprov = MySQLQuery.getAsBoolean(cfg[4]);
        String filter;
        if (flow) {
            filter = " (wo.flow_status ='done' OR wo.flow_status='approved') AND ";
            if (filterOrderApprov) {
                filter += "(  "
                        + "SELECT MAX(f.checked)   "
                        + "FROM mto_flow_check AS f   "
                        + "WHERE f.work_order_id = wo.id  "
                        + "AND f.flow_step_id =   "
                        + "(SELECT st.id  "
                        + "FROM mto_flow_step st  "
                        + "WHERE st.flow_id = wo.flow_id  "
                        + "ORDER BY st.place DESC  "
                        + "LIMIT 1  "
                        + ") AND f.`status` = 'ok' ) BETWEEN ?1 AND ?2 ";
            } else {
                filter += "wo.create_date BETWEEN ?1 AND ?2 ";
            }
        } else {
            filter = " wo.`begin` BETWEEN ?1 AND ?2 AND wo.flow_status ='done'  ";
        }
        Table tblMod = new Table("");
        tblMod.setColumns(cols);
        //Cabecera
        TableHeader header = new TableHeader();
        tblMod.getHeaders().add(header);
        header.getColums().add(new HeaderColumn("Año", 1, 2));
        header.getColums().add(new HeaderColumn("Meses", 12, 1));

        //areas y subareas
        String sAreaSub
                = "SELECT SUM(i.value)"
                + "FROM item AS i "
                + "INNER JOIN area AS a ON a.id = i.area_id "
                + "INNER JOIN work_order AS wo ON wo.id = i.work_id "
                + "WHERE "
                + "a.area_id = " + areaId + " AND "
                + "wo.canceled = 0 AND "
                + filter + " AND "
                + "i.maint_task_id IS NULL ";

        Table tbAreasb = new Table(tblMod);
        tbAreasb.setTitle("Comparativo Áreas y Rubros [" + nameArea + "]");

        for (int i = 0; i < years.length; i++) {
            int curYear = years[i];
            Object[] rowAreasb = new Object[13];
            rowAreasb[0] = String.valueOf(curYear);

            for (int j = 0; j < monthsIds.length; j++) {
                int curMonth = monthsIds[j];
                Calendar calendar = Calendar.getInstance();
                calendar.set(curYear, curMonth, 1);
                int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                Date fBegin = new GregorianCalendar(curYear, curMonth, 1, 0, 0, 0).getTime();
                Date fEnd = new GregorianCalendar(curYear, curMonth, maxDay, 0, 0, 0).getTime();

                MySQLQuery areasQ = new MySQLQuery(sAreaSub);
                areasQ.setParam(1, fBegin);
                areasQ.setParam(2, fEnd);

                rowAreasb[j + 1] = areasQ.getAsBigDecimal(con, true);
            }
            tbAreasb.addRow(rowAreasb);
        }

        rep.getTables().add(tbAreasb);

        return rep;
    }

    public static GridResult getAreasValues(String type, int vehId, int anio, Connection con) throws Exception {
        String typeM = "";
        if (type.equals("mto")) {
            typeM = "Área";
        } else {
            typeM = "Rubro";
        }

        String str = "SELECT "
                + "a.`name`, "//0
                + "f.val, "//1
                + "f.id, "//2
                + "a.id "//3
                + "FROM "
                + "area AS a "
                + "LEft JOIN forecast AS f ON a.id = f.area_id AND f.anio = ?1 AND f.vec_id = ?2 "
                + "WHERE "
                + "a.area_id IS NULL ";
        if (type != null) {
            str += " AND a.type = '" + type + "' ";
        }
        str += "Order by a.name ";

        MySQLQuery q = new MySQLQuery(str);
        q.setParam(1, anio);
        q.setParam(2, vehId);
        GridResult grid = new GridResult();
        grid.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_TEXT, 25, typeM),
            new MySQLCol(MySQLCol.TYPE_DECIMAL_1, 15, "Valor"),
            new MySQLCol(MySQLCol.TYPE_KEY),
            new MySQLCol(MySQLCol.TYPE_KEY),};

        grid.data = q.getRecords(con);
        return grid;
    }

    public static GridResult getFuelsValues(int vehId, int anio, Connection con) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT "
                + "ft.`name`, "//0
                + "f.val, "//1
                + "f.id, " //2
                + "ft.id "//3
                + "FROM "
                + "fuel_type AS ft "
                + "LEft JOIN forecast AS f ON f.fuel_type_id = ft.id AND f.anio=?1 AND f.vec_id= ?2 "
                + "Order by ft.name");
        q.setParam(1, anio);
        q.setParam(2, vehId);

        GridResult grid = new GridResult();
        grid.cols = new MySQLCol[]{
            new MySQLCol(MySQLCol.TYPE_TEXT, 25, "Combustible"),
            new MySQLCol(MySQLCol.TYPE_DECIMAL_1, 15, "Valor"),
            new MySQLCol(MySQLCol.TYPE_KEY),
            new MySQLCol(MySQLCol.TYPE_KEY),};

        grid.data = q.getRecords(con);
        return grid;
    }

    public static List<IndicatorPar> findAgreementEntities(Connection con) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT id, name, desc1, desc2, type, bound, sql1, sql2 "
                + "FROM indicator_par As o ORDER BY o.id ASC");
        Object[][] data = q.getRecords(con);
        List<IndicatorPar> lst = new ArrayList<>();

        if (data != null && data.length > 0) {
            for (Object[] row : data) {
                IndicatorPar ind = new IndicatorPar();
                ind.id = (MySQLQuery.getAsInteger(row[0]));
                ind.name = (MySQLQuery.getAsString(row[1]));
                ind.desc1 = (MySQLQuery.getAsString(row[2]));
                ind.desc2 = (MySQLQuery.getAsString(row[3]));
                ind.type = (MySQLQuery.getAsString(row[4]));
                ind.bound = (MySQLQuery.getAsInteger(row[5]));
                ind.sql1 = (MySQLQuery.getAsString(row[6]));
                ind.sql2 = (MySQLQuery.getAsString(row[7]));
                lst.add(ind);
            }
        }

        return lst;
    }

    //PROYECCION GL0BAL POR AREA
    public static CostReport findForecastBudgetReport(String type, int cityId, int enterId, int year, Connection con) throws Exception {
        CostReport rep = new CostReport();
        rep.areaCosts = (new ArrayList<>());
        rep.fuelCosts = (new ArrayList<>());

        Area[] ars = Area.getSuperAreas(type, con);
        FuelType[] ftypes = FuelType.getAllFuelTypes(con);
        MySQLAgency[] ags = MySQLAgency.getAgencies(cityId, enterId, con);

        if (ags.length == 0) {
            throw new Exception("No hay agencias con los parámetros solicitados.");
        }

        Object[][] lareas;
        Object[][] lfuels = new Object[0][];

        String agIds = String.valueOf(ags[0].getId());
        for (int i = 1; i < ags.length; i++) {
            agIds += ("," + ags[i].getId());
        }

        String qs1 = "SELECT fc.area_id, fc.val "
                + "FROM forecast fc "
                + "INNER JOIN area a ON fc.area_id = a.id "
                + "WHERE  "
                + "a.type = '" + type + "' AND "
                + "fc.anio =" + year + " "
                + "AND fc.fuel_type_id IS NULL AND fc.vec_id IS NULL ";

        MySQLQuery q1 = new MySQLQuery(qs1);
        lareas = q1.getRecords(con);

        String qs2 = "SELECT fc.fuel_type_id, fc.val "
                + " FROM forecast fc "
                + " WHERE fc.anio = " + year + " AND "
                + " (fc.area_id IS NULL AND fc.vec_id IS NULL ) "
                + " AND fc.fuel_type_id IS NOT NULL ";

        lfuels = new MySQLQuery(qs2).getRecords(con);

        //-----------------------------------------------------------------------
        for (Area ar : ars) {
            AreaCostListItem li = new AreaCostListItem();
            li.setArea(ar.getId(), ar.getName(), ar.getPuc());
            li.values = (new ArrayList<BigDecimal>());
            boolean find = false;

            for (Object[] larea : lareas) {
                if (ar.getId() == ((Integer) larea[0]).intValue()) {
                    BigDecimal val = new BigDecimal(larea[1].toString());
                    li.values.add(val);
                    find = true;
                }
            }
            if (!find) {
                li.values.add(BigDecimal.ZERO);
            }
            rep.areaCosts.add(li);
        }

        if (type.equals("mto")) {
            for (FuelType ft : ftypes) {
                FuelCostListItem li = new FuelCostListItem();
                li.setFuelType(ft.getId(), ft.getName());
                li.values = (new ArrayList<BigDecimal>());
                boolean find = false;

                for (Object[] lfuel : lfuels) {
                    if (ft.getId() == ((Integer) lfuel[0]).intValue()) {
                        BigDecimal val = new BigDecimal(lfuel[1].toString());
                        li.values.add(val);
                        find = true;
                    }
                }
                if (!find) {
                    li.values.add(BigDecimal.ZERO);
                }
                rep.fuelCosts.add(li);
            }
        }

        rep.single = (Boolean.TRUE);
        return rep;
    }

    //COSTOS POR UN MES DEL AÑO
    public static CostReport findMonthCostReport(String type, int idCity, int idEnt, int year, int month, Boolean contract, Integer contractor, boolean storeMovs, Connection con) throws Exception {

        CostReport rep = null;
        CostReport tmp = null;

        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, 1);
        int maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        Date fBegin = new GregorianCalendar(year, month, 1, 0, 0, 0).getTime();
        Date fEnd = new GregorianCalendar(year, month, maxDay, 0, 0, 0).getTime();

        rep = findCostReport(type, idCity, idEnt, 0, fBegin, fEnd, contract, contractor, storeMovs, con);
        tmp = findCostReport(type, idCity, idEnt, 0, fBegin, fEnd, contract, contractor, storeMovs, con);
        for (int j = 0; j < rep.areaCosts.size(); j++) {
            rep.areaCosts.get(j).values.add(tmp.areaCosts.get(j).values.get(0));
        }
        if (type == null || type.equals("mto")) {//unicamente en las areas de mantenimiento
            for (int j = 0; j < tmp.fuelCosts.size(); j++) {
                rep.fuelCosts.get(j).values.add(tmp.fuelCosts.get(j).values.get(0));
            }
        }

        return rep;
    }

    public static void copyForecast(int vehSrc, int vehDes, int anio, Connection con) throws Exception {
        int c = new MySQLQuery(String.format("SELECT COUNT(*) "
                + "FROM forecast f WHERE f.vec_id=%d AND f.anio=%d", vehDes, anio)).getAsInteger(con);
        if (c > 0) {
            throw new Exception("Ya existen proyecciones de costos para este vehículo");
        }

        String qs1 = "INSERT INTO forecast (vec_id, area_id, val, anio, fuel_type_id) "
                + "(SELECT ?1, area_id, val, anio, fuel_type_id "
                + "FROM forecast "
                + "WHERE vec_id = " + vehSrc + " AND anio = ?2)";
        MySQLQuery q1 = new MySQLQuery(qs1);
        q1.setParam(1, vehDes);
        q1.setParam(2, anio);
        q1.executeUpdate(con);
    }

    public static MySQLReport getRptLubrication(Integer cityId, Integer sbAreaId, Integer month, Integer year, String type, Connection conn) throws Exception {
        MySQLReport rep = new MySQLReport("Matriz de Lubricación", "", "matriz_lubr", MySQLQuery.now(conn));
        MtoCfg cfg = new MtoCfg().select(1, conn);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().get(0).setWrap(true);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.DATE, "dd/MM/yyyy"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//2
        rep.setMultiRowTitles(true);
        rep.setVerticalFreeze(5);
        rep.setZoomFactor(80);

        Table tb = new Table("Vehículos");
        tb.getColumns().add(new Column("Área", 35, 0));
        tb.getColumns().add(new Column("Ubicación", 35, 0));
        tb.getColumns().add(new Column("Placa", 10, 0));
        tb.getColumns().add(new Column("Conductor", 35, 0));
        tb.getColumns().add(new Column("Tipo", 15, 0));
        tb.getColumns().add(new Column("Marca", 25, 0));
        tb.getColumns().add(new Column("Qt", 10, 2));
        tb.getColumns().add(new Column("MOD", 10, 2));
        tb.getColumns().add(new Column("Operación", 12, 0));
        tb.getColumns().add(new Column("Frecuencia", 12, 2));
        tb.getColumns().add(new Column("km Actual", 12, 2));
        tb.getColumns().add(new Column("km recorridos en el mes", 12, 2));
        tb.getColumns().add(new Column("Fecha Ejecucion MTTO Preventivo", 12, 1));
        tb.getColumns().add(new Column("km Ultimo Mtto Preventivo", 12, 2));
        tb.getColumns().add(new Column("km del Proximo  Mtto Preventivo", 12, 2));
        tb.getColumns().add(new Column("km Prom  Diario", 12, 2));
        tb.getColumns().add(new Column("Back Log", 12, 2));
        tb.getColumns().add(new Column("Dias Restantes", 12, 2));
        tb.getColumns().add(new Column("Fecha Proxima de Ejecución", 12, 1));
        tb.getColumns().add(new Column("Mes Próxima Ejecución", 12, 0));

        new MySQLQuery("SET lc_time_names = 'es_ES';").executeUpdate(conn);
        MySQLQuery q = new MySQLQuery(" "
                + "SELECT DISTINCT "
                + "CONCAT(area.name,' - ',sa.name), "
                + "ct.name, "
                + "vh.plate, "
                + "IF((SELECT COUNT(*)>0 FROM mto_out_service ms WHERE ms.vehicle_id = vh.id AND date(ms.since) <= CURDATE() AND  ms.end IS NULL) OR (SELECT COUNT(*) > 0 FROM mto_vh_note n WHERE n.vehicle_id = vh.id AND DATE(n.note_date) <= CURDATE() AND n.rev_date IS NULL), CONCAT((SELECT COALESCE(GROUP_CONCAT(CONCAT(e.first_name, ' ', e.last_name) ORDER BY e.first_name, e.last_name SEPARATOR ', '),'') FROM employee AS e INNER JOIN driver_vehicle AS dv ON dv.driver_id = e.id WHERE dv.vehicle_id = vh.id AND dv.`end` IS NULL), (SELECT COALESCE(CONCAT(' -- ', GROUP_CONCAT(mt.name)), '') FROM mto_out_service ms INNER JOIN mto_out_type mt ON ms.out_id = mt.id WHERE ms.vehicle_id = vh.id AND ms.end IS NULL),(SELECT COALESCE(CONCAT(' -- ', GROUP_CONCAT(t.name)), '') FROM mto_vh_note n INNER JOIN mto_note_type t ON t.id = n.type_id WHERE n.vehicle_id = vh.id AND DATE(n.note_date) <= CURDATE() AND n.rev_date IS NULL)), (SELECT GROUP_CONCAT(CONCAT(e.first_name, ' ', e.last_name) ORDER BY e.first_name, e.last_name SEPARATOR ', ') FROM employee AS e INNER JOIN driver_vehicle AS dv ON dv.driver_id = e.id WHERE dv.vehicle_id = vh.id AND dv.`end` IS NULL)) AS conductor, "
                + "vc.name, "
                + "CONCAT(vc.`name`,' - ',vt.`name`), "
                + "mt.quart, "
                + "vh.model, "
                + "(SELECT v.`data` FROM sys_frm_field s INNER JOIN sys_frm_value v ON v.field_id = s.id WHERE s.type_id = 1 AND s.id = 19 AND v.owner_id = vh.id LIMIT 1) oper, "
                + "IF(vmt.mileage IS NOT NULL, vmt.mileage, mt.mileage) AS frec, "
                + "(SELECT m.mileage_cur FROM mto_kms_manual m WHERE m.vh_id = vh.id "
                + (type.equals("dt") ? "AND ?1 = MONTH(m.date) AND ?2 = YEAR(m.date) " : "")
                + " ORDER BY m.date DESC LIMIT 1) AS km_actual, "
                + "(SELECT SUM(m.mileage_cur - m.mileage_last) FROM mto_kms_manual m WHERE m.vh_id = vh.id "
                + (type.equals("dt") ? "AND ?1 = MONTH(m.date) AND ?2 = YEAR(m.date)) AS km_mes, " : "AND MONTH(CURDATE()) = MONTH(m.date) AND YEAR(CURDATE()) = YEAR(m.date)) AS km_mes, ")
                + "IF(pr.id IS NOT NULL, pr.dt, (SELECT p.dt FROM mto_pend_task pe INNER JOIN mto_task_prog p ON p.id = pe.task_prog_id INNER JOIN maint_task AS mt ON pe.maint_task_id = mt.id INNER JOIN area AS child ON mt.area_id = child.id WHERE pe.vehicle_id = vh.id AND child.id = sa.id AND child.area_id = area.id ORDER BY p.dt DESC LIMIT 1)) fec_mto, "
                + "IF(pr.id IS NOT NULL, pr.kms, (SELECT p.kms FROM mto_pend_task pe INNER JOIN mto_task_prog p ON p.id = pe.task_prog_id INNER JOIN maint_task AS mt ON pe.maint_task_id = mt.id INNER JOIN area AS child ON mt.area_id = child.id WHERE pe.vehicle_id = vh.id AND child.id = sa.id AND child.area_id = area.id ORDER BY p.dt DESC LIMIT 1)) kms, "
                + "pr.id "
                + "FROM mto_pend_task pe "
                + "LEFT JOIN mto_task_prog pr ON pr.id = pe.task_prog_id "
                + "INNER JOIN vehicle vh ON vh.id = pe.vehicle_id "
                + "INNER JOIN vehicle_type AS vt ON vt.id = vh.vehicle_type_id "
                + "INNER JOIN maint_task mt ON mt.id = pe.maint_task_id "
                + "INNER JOIN task_type AS t ON t.id = mt.task_type_id "
                + "INNER JOIN area AS sa ON sa.id = mt.area_id "
                + "INNER JOIN area AS area ON area.id = sa.area_id "
                + "LEFT JOIN vehicle_maint_task vmt ON vmt.vehicle_id = vh.id AND vmt.maint_task_id = mt.id "
                + "LEFT JOIN vehicle_class AS vc ON vt.vehicle_class_id = vc.id "
                + "LEFT JOIN fuel_type_vehicle AS ftv ON ftv.vehicle_id = vh.id "
                + "LEFT JOIN fuel_type AS ft ON ftv.fuel_type_id = ft.id "
                + "LEFT JOIN agency ag ON vh.agency_id = ag.id "
                + "LEFT JOIN city ct ON ag.city_id = ct.id "
                + "LEFT JOIN enterprise et ON ag.enterprise_id = et.id "
                + "LEFT JOIN mto_contractor ctor ON ctor.id = vh.contractor_id "
                + "WHERE vh.active = true AND vh.visible = 1 and sa.active = 1 and mt.active = 1 "
                + (cityId != null ? "AND ?3 = ct.id " : "")
                + (sbAreaId != null ? "AND ?4 = sa.id " : "")
                + (type.equals("dt") ? "AND pr.id IS NOT NULL AND MONTH(pr.dt) = ?1 AND ?2 = YEAR(pr.dt) " : "AND pr.id IS NULL AND "
                + "IF(`km_left` IS NOT NULL, `km_left` <= " + getValue(cfg.mileage, false) + ", IF(`hrs_left` IS NOT NULL, CAST(`hrs_left` AS SIGNED) <= " + getValue(cfg.hours, false) + ", IF(`days_limit` IS NOT NULL, CURDATE() >= DATE_SUB(`days_limit`, INTERVAL " + getValue(cfg.weeks, false) + " WEEK) , FALSE))) ")
                + " ");

        q.setParam(1, month);
        q.setParam(2, year);
        if (cityId != null) {
            q.setParam(3, cityId);
        }
        if (sbAreaId != null) {
            q.setParam(4, sbAreaId);
        }
        Object[][] data = q.getRecords(conn);
        List<Object[]> lstData = new ArrayList<>();

        for (Object[] row : data) {
            Object[] newRow = new Object[20];
            System.arraycopy(row, 0, newRow, 0, 14);

            BigDecimal frec = MySQLQuery.getAsBigDecimal(row[9], true);
            BigDecimal kmLast = MySQLQuery.getAsBigDecimal(row[13], true);
            BigDecimal kmAct = MySQLQuery.getAsBigDecimal(row[10], true);
            BigDecimal kmRec = MySQLQuery.getAsBigDecimal(row[11], true);
            Integer progId = MySQLQuery.getAsInteger(row[14]);
            BigDecimal kmProx = frec.add(kmLast);
            BigDecimal kmPromDia = BigDecimal.ZERO;
            int daysF = 0;

            if (kmAct != null && kmAct.intValue() > 0 && kmRec != null && kmRec.intValue() > 0) {
                newRow[14] = kmProx;
                kmPromDia = kmRec.divide(new BigDecimal(30), 2, RoundingMode.HALF_UP);
                newRow[15] = kmPromDia;
                newRow[16] = (kmProx).subtract(kmAct);
                daysF = ((kmProx).subtract(kmAct)).divide(kmPromDia, 2, RoundingMode.HALF_UP).intValue();
                newRow[17] = daysF;
                Date dtProx = Dates.sumDaysDate(new Date(), daysF);
                newRow[18] = dtProx;
                newRow[19] = new SimpleDateFormat("MMMM").format(dtProx);

                if (type.equals("dt_prox")) {
                    if (daysF < 0) {
                        lstData.add(newRow);
                    } else {
                        Calendar date = Calendar.getInstance();
                        date.setTime(dtProx);
                        int y = date.get(Calendar.YEAR);
                        int m = date.get(Calendar.MONTH) + 1;
                        if (y == year && m == month) {
                            lstData.add(newRow);
                        }
                    }
                } else {
                    lstData.add(newRow);
                }
            }
        }
        tb.setData(lstData);

        if (tb.getData() != null && tb.getData().length > 0) {
            rep.getTables().add(tb);
        }
        return rep;
    }

    private static Integer getValue(Integer value, boolean sum) {
        int num = 0;
        if (value == null) {
            return null;
        } else if (sum) {
            num = value + 3000000;
        } else {
            num = value;
        }
        return num;
    }

    public static MySQLReport getRptTaskProg(Date begin, Date end, Connection conn) throws Exception {

        SimpleDateFormat dt = new SimpleDateFormat("dd/MM/yyyy");

        MySQLQuery q = new MySQLQuery("SELECT vh.plate, vh.internal, pr.dt, area.name, sa.name, t.name, pr.notes, pr.kms, CONCAT(e.first_name, ' ', e.last_name)\n"
                + "FROM mto_pend_task AS pe\n"
                + "INNER JOIN mto_task_prog pr ON pe.task_prog_id = pr.id\n"
                + "INNER JOIN employee e ON e.id = pr.emp_id\n"
                + "INNER JOIN vehicle vh ON vh.id = pe.vehicle_id\n"
                + "INNER JOIN vehicle_type AS vt ON vt.id = vh.vehicle_type_id\n"
                + "INNER JOIN maint_task mt ON mt.id = pe.maint_task_id\n"
                + "INNER JOIN task_type AS t ON t.id = mt.task_type_id\n"
                + "INNER JOIN area AS sa ON sa.id = mt.area_id \n"
                + "INNER JOIN area AS area ON area.id = sa.area_id "
                + "WHERE pr.dt BETWEEN ?1 AND ?2 ");
        q.setParam(1, Dates.getMinHours(begin));
        q.setParam(2, Dates.getMaxHours(end));
        Object[][] data = q.getRecords(conn);

        MySQLReport rep = new MySQLReport("Reporte Tareas Preventivas", "", "Tareas Preventivas", MySQLQuery.now(conn));
        rep.getSubTitles().add("Periodo " + dt.format(begin) + " - " + dt.format(end));

        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//1

        rep.setZoomFactor(85);
        rep.setShowNumbers(true);
        rep.getFormats().get(0).setWrap(true);
        rep.setVerticalFreeze(5);
        Table tb = new Table("Tareas Preventivas");
        tb.getColumns().add(new Column("Placa", 15, 0));
        tb.getColumns().add(new Column("Interno", 15, 0));
        tb.getColumns().add(new Column("Fecha", 20, 1));
        tb.getColumns().add(new Column("Área", 30, 0));
        tb.getColumns().add(new Column("Subarea", 30, 0));
        tb.getColumns().add(new Column("Tipo", 30, 0));
        tb.getColumns().add(new Column("Notas", 45, 0));
        tb.getColumns().add(new Column("Kms", 15, 0));
        tb.getColumns().add(new Column("Ejecutó", 30, 0));
        tb.setData(data);
        if (tb.getData().length > 0) {
            rep.getTables().add(tb);
        }
        return rep;
    }
}
