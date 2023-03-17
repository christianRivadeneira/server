package api.per.rpt;

import api.per.model.PerExtra;
import api.per.model.PerSurcharge;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
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

public class PersonalReport {

    public static MySQLReport getDetailsExtras(Integer employeeId, Date payMonth, String authOffices, boolean invertNames, boolean rdbDet, boolean rdbTot, int type, int part,
            Integer sbArea, Integer pos, Integer cityId, Integer officeId, String nameEmployee, int saraRoundExtrasMinutes, boolean manualOnly, Connection conn) throws Exception {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(payMonth);
        int year = gc.get(GregorianCalendar.YEAR);
        int month = gc.get(GregorianCalendar.MONTH) + 1;
        boolean active = true;
        String qStr = "SELECT DISTINCT e.id, "
                + "CONCAT(" + (invertNames ? "e.last_name,' ',e.first_name" : "e.first_name,' ',e.last_name") + ") AS nm,"
                + "e.document "
                + ""
                + "FROM per_extra "
                + "INNER JOIN per_employee e ON e.id = per_extra.emp_id AND e.active =1 "
                + "INNER JOIN per_employeer em ON em.id = per_extra.employeer_id "
                + "INNER JOIN per_pos pos ON pos.id = per_extra.pos_id "
                + "INNER JOIN per_contract ctr ON ctr.emp_id = e.id "
                + "WHERE "
                + (authOffices != null ? "ctr.office_id IN (" + authOffices + ") AND " : "")
                + "ctr.leave_date IS NULL "
                + "AND ctr.active = 1 ";
        if (manualOnly) {
            qStr += "AND input_type = 'man' AND per_extra.active = " + (active ? "1" : "0") + " ";
        } else {
            if (rdbDet) {
                qStr += "AND reg_type <> 'total' AND per_extra.active = " + (active ? "1" : "0") + " ";
            } else {
                qStr += "AND reg_type = 'total' AND per_extra.active = " + (active ? "1" : "0") + " ";
            }
            if (type == 1) {
                qStr += "AND input_type = 'man' ";
            } else if (type == 2) {
                qStr += "AND input_type = 'gate' ";
            }
        }
        if (employeeId != null) {
            qStr += "AND e.id = " + employeeId + " ";
        }
        qStr += "AND EXTRACT(YEAR FROM pay_month) = " + year + " "
                + "AND EXTRACT(MONTH FROM pay_month) = " + month + " ";
        if (part == 1) {
            qStr += "AND EXTRACT(DAY FROM pay_month) = 16 ";
        } else {
            qStr += "AND EXTRACT(DAY FROM pay_month) = 1 ";
        }
        if (sbArea != null) {
            qStr += " AND pos.sarea_id = " + sbArea + " ";
        } else {
            qStr += " AND pos.id != 24 "; //24 cargo especial para los vendedores
        }
        if (pos != null) {
            qStr += " AND pos.id = " + pos + " ";
        }
        if (cityId != null) {
            qStr += " AND ctr.city_id = " + cityId + " ";
        }
        if (officeId != null) {
            qStr += " AND ctr.office_id = " + officeId + " ";
        }
        qStr += "ORDER BY nm ASC ";
        MySQLQuery q = new MySQLQuery(qStr);
        Object[][] data = q.getRecords(conn);

        String date = "'" + year + "-" + month + "-" + (part == 1 ? "16" : "01") + "'";

        String title;
        if (rdbTot) {
            title = "Detalle Horas Extras Totales";
        } else {
            title = "Detalle Horas Extras";
        }
        MySQLReport rep = new MySQLReport(title, "", "hrs_extras_det", MySQLQuery.now(conn));
        if (nameEmployee != null) {
            rep.getSubTitles().add("Empleado : " + nameEmployee);
        } else {
            rep.getSubTitles().add("Empleados : [Todos] ");
        }
        rep.setShowNumbers(true);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().get(0).setWrap(true);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "###,##0.0#"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, " HH:mm:ss a"));//3
        rep.getFormats().add(new CellFormat(MySQLReportWriter.ENUM, MySQLReportWriter.RIGHT, new PerExtra().getEnumOptions("ev_type")));//4
        rep.setZoomFactor(85);
        List<Column> cols = new ArrayList<>();
        cols.add(new Column("Empleado", 35, 0));
        cols.add(new Column("Cédula", 15, 0));
        cols.add(new Column("Zona", 15, 0));
        cols.add(new Column("Cargo", 35, 0));
        cols.add(new Column("Proceso", 35, 0));
        cols.add(new Column("Boleta", 10, 0));
        cols.add(new Column("Fec. Evento", 12, 2));
        cols.add(new Column("Hora Inicio", 13, 3));
        cols.add(new Column("Hora Fin", 13, 3));
        cols.add(new Column("Duración", 10, 1));
        cols.add(new Column("Tipo.", 10, 4));
        cols.add(new Column("Persona Reportó", 35, 0));
        cols.add(new Column("Autorizado", 12, 0));
        if (type == 0 || type == 1) {
            cols.add(new Column("Motivo", 45, 0));
        }
        Table model = new Table(title);
        model.setColumns(cols);
        model.setData(data);
        BigDecimal roundMin = new BigDecimal(saraRoundExtrasMinutes).divide(new BigDecimal(60), 8, RoundingMode.HALF_EVEN);
        for (int i = 0; i < data.length; i++) {
            String qMul = "SELECT  "
                    + "CONCAT(" + (invertNames ? "e.last_name,' ',e.first_name" : "e.first_name,' ',e.last_name") + "), "
                    + "e.document, zo.name, po.name, a.name, "
                    + "IF(reg_type = 'bill', 'Si', 'No'), ev_date, beg_time, end_time, approved_time/3600, ev_type, "
                    + "IF(input_type = 'gate', 'Portería', CONCAT(" + (invertNames ? "ep.last_name,' ',ep.first_name" : "ep.first_name,' ',ep.last_name") + ")), "
                    + "IF(checked, 'Si', 'No') "
                    + (type == 0 || type == 1 ? ", per_extra.notes " : "")
                    + "FROM per_extra "
                    + "INNER JOIN per_employee e ON e.id = per_extra.emp_id AND e.active = 1 "
                    + "INNER JOIN employee ep ON ep.id = per_extra.reg_by_id "
                    + "INNER JOIN per_contract c ON c.emp_id = e.id AND c.`last` = 1 "
                    + "INNER JOIN per_pos po ON po.id = c.pos_id "
                    + "LEFT JOIN per_sbarea sa ON sa.id = po.sarea_id "
                    + "LEFT JOIN per_area a ON a.id = sa.area_id "
                    + "LEFT JOIN per_office of ON of.id = c.office_id "
                    + "LEFT JOIN city ci ON ci.id = of.city_id "
                    + "LEFT JOIN zone zo ON zo.id = ci.zone_id ";
            if (rdbDet) {
                qMul += "WHERE reg_type <> 'total' AND per_extra.active = " + (active ? "1" : "0") + " ";
            } else {
                qMul += "WHERE reg_type = 'total' AND per_extra.active = " + (active ? "1" : "0") + " ";
            }
            if (type == 1) {
                qMul += "AND input_type = 'man' ";
            } else if (type == 2) {
                qMul += "AND input_type = 'gate' ";
            }
            qMul += "AND e.id = " + data[i][0] + " ";
            qMul += "AND  pay_month = " + date + " ";
            qMul += "ORDER BY ev_date ASC";

            Object[][] dataTable = new MySQLQuery(qMul).getRecords(conn);

            if (dataTable != null) {
                Object[] objects = data[i];
                Table tb = new Table(model);
                tb.setTitle("Horas Extras - " + objects[1].toString());

                for (Object[] dataTableRow : dataTable) {
                    dataTableRow[9] = getRoundValue(MySQLQuery.getAsBigDecimal(dataTableRow[9], true), roundMin);
                }

                tb.setData(dataTable);
                rep.getTables().add(tb);
            }
        }
        return rep;
    }

    public static MySQLReport getDetailsSurcharges(Integer employeeId, Date payMonth, String authOffices, boolean invertNames, boolean rdbDet, boolean rdbTot, int part,
            Integer sbArea, Integer pos, Integer cityId, Integer officeId, String nameEmployee, int saraRoundExtrasMinutes, boolean manualOnly, Connection conn) throws Exception {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(payMonth);
        int year = gc.get(GregorianCalendar.YEAR);
        int month = gc.get(GregorianCalendar.MONTH) + 1;
        boolean active = true;
        String qStr = "SELECT DISTINCT "
                + "e.id, "
                + "CONCAT(" + (invertNames ? "e.last_name,' ',e.first_name" : "e.first_name,' ',e.last_name") + ") AS nm "
                + "FROM per_surcharge AS ps "
                + "INNER JOIN per_employee e ON e.id = ps.emp_id AND e.active =1 "
                + "INNER JOIN per_employeer em ON em.id = ps.employeer_id "
                + "INNER JOIN per_pos pos ON pos.id = ps.pos_id "
                + "INNER JOIN per_contract ctr ON ctr.emp_id = e.id "
                + "WHERE "
                + (authOffices != null ? "ctr.office_id IN (" + authOffices + ") AND " : "")
                + "ctr.leave_date IS NULL "
                + "AND ctr.active = 1 ";
        if (manualOnly) {
            qStr += "AND (ps.reg_type = 'total' OR ps.reg_type = 'det') ";
        } else {
            if (rdbDet) {
                qStr += "AND ps.reg_type <> 'total' ";
            } else {
                qStr += "AND ps.reg_type = 'total' ";
            }
        }
        if (employeeId != null) {
            qStr += "AND e.id = " + employeeId + " ";
        }
        qStr += "AND YEAR(ps.pay_month) = " + year + " "
                + "AND MONTH(ps.pay_month) = " + month + " ";
        if (part == 1) {
            qStr += "AND DAY(ps.pay_month) = 16 ";
        } else {
            qStr += "AND DAY(ps.pay_month) = 1 ";
        }
        if (sbArea != null) {
            qStr += " AND pos.sarea_id = " + sbArea + " ";
        } else {
            qStr += " AND pos.id != 24 "; //24 cargo especial para los vendedores
        }
        if (pos != null) {
            qStr += " AND pos.id = " + pos + " ";
        }
        if (cityId != null) {
            qStr += " AND ctr.city_id = " + cityId + " ";
        }
        if (officeId != null) {
            qStr += " AND ctr.office_id = " + officeId + " ";
        }
        qStr += "ORDER BY nm ASC ";
        MySQLQuery q = new MySQLQuery(qStr);
        Object[][] data = q.getRecords(conn);

        MySQLReport rep = new MySQLReport("Reporte de Recargos", "", "surcharges_det", MySQLQuery.now(conn));
        if (nameEmployee != null) {
            rep.getSubTitles().add("Empleado : " + nameEmployee);
        } else {
            rep.getSubTitles().add("Empleados : [Todos] ");
        }
        rep.setShowNumbers(true);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().get(0).setWrap(true);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "###,##0.0#"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.ENUM, MySQLReportWriter.LEFT, new PerSurcharge().getEnumOptions("ev_type")));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, "dd/MM/yyyy"));//3
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.RIGHT, " HH:mm:ss a"));//4

        rep.setZoomFactor(85);
        List<Column> cols = new ArrayList<>();
        cols.add(new Column("Registró", 35, 0));
        cols.add(new Column("Duración", 10, 1));
        cols.add(new Column("Tipo", 30, 2));
        cols.add(new Column("Fec. Evento", 12, 3));
        cols.add(new Column("Inicio", 13, 4));
        cols.add(new Column("Fin", 13, 4));
        cols.add(new Column("Ingreso", 35, 0));
        if (rdbDet) {
            cols.add(new Column("Motivo", 45, 0));
        }

        Table model = new Table("Reporte de Recargos");
        model.setColumns(cols);
        model.setData(data);

        BigDecimal roundMin = new BigDecimal(saraRoundExtrasMinutes).divide(new BigDecimal(60), 8, RoundingMode.HALF_EVEN);
        for (int i = 0; i < data.length; i++) {
            String qMul = "SELECT  "
                    + "IF(ps.emp_id <> 1, CONCAT(" + (invertNames ? "e.last_name,' ',e.first_name" : "e.first_name,' ',e.last_name") + "), 'Portería'),"
                    + "approved_time/3600, "
                    + "ev_type, "
                    + "ps.reg_date,"
                    + "ps.beg_time,"
                    + "ps.end_time, "
                    + "CONCAT(emp.first_name,' ',emp.last_name), "
                    + "ps.notes "
                    + "FROM per_surcharge AS ps "
                    + "INNER JOIN per_employee e ON e.id = ps.emp_id AND e.active =1 "
                    + "LEFT JOIN employee emp ON ps.reg_by_id = emp.id  "
                    + "WHERE ps.active = " + (active ? "1" : "0") + " "
                    + "AND ps.emp_id = " + data[i][0] + " "
                    + "AND YEAR(pay_month) = " + year + " "
                    + "AND MONTH(pay_month) = " + month + " ";
            if (rdbDet) {
                qMul += "AND ps.reg_type <> 'total' ";
            } else {
                qMul += "AND ps.reg_type = 'total' ";
            }
            if (part == 1) {
                qMul += "AND DAY(pay_month) = 16 ";
            } else {
                qMul += "AND DAY(pay_month) = 1 ";
            }
            qMul += "ORDER BY approved_time ASC";

            Object[][] dataTable = new MySQLQuery(qMul).getRecords(conn);

            if (dataTable != null) {
                Object[] objects = data[i];
                Table tb = new Table(model);
                tb.setTitle("Recargos - " + objects[1].toString());

                for (Object[] dataTableRow : dataTable) {
                    dataTableRow[1] = getRoundValue(MySQLQuery.getAsBigDecimal(dataTableRow[1], true), roundMin);
                }

                tb.setData(dataTable);
                rep.getTables().add(tb);
            }
        }
        return rep;
    }

    private static BigDecimal getRoundValue(BigDecimal val, BigDecimal roundMin) {
        BigDecimal rVal = BigDecimal.ZERO;
        if (val.compareTo(BigDecimal.ZERO) != 0) {
            if (roundMin.compareTo(BigDecimal.ZERO) != 0) {
                rVal = val.divide(roundMin, 0, RoundingMode.FLOOR).multiply(roundMin).setScale(4, RoundingMode.HALF_EVEN);
            } else {
                rVal = val;
            }
        }
        return rVal;
    }

    public static MySQLReport getRepVacations(Date begDt, Date endDt, boolean invertNames, String authOffices, Connection conn) throws Exception {
        String str = "SELECT "
                + "e.document, "
                + "CONCAT(" + (invertNames ? "e.last_name,' ',e.first_name" : "e.first_name,' ',e.last_name") + "), "//2
                + "c.name, pe.name, pp.name, ps.name, v.date_beg, v.date_end, v.observation "
                + "FROM per_vacation AS v "
                + "INNER JOIN per_employee AS e ON v.employee_id = e.id "
                + "INNER JOIN per_contract ctr ON ctr.emp_id = e.id "
                + "LEFT JOIN city c ON c.id = ctr.city_id "
                + "LEFT JOIN per_employeer pe ON pe.id = ctr.employeer_id "
                + "LEFT JOIN per_pos pp ON pp.id = ctr.pos_id "
                + "LEFT JOIN per_sbarea ps ON ps.id = pp.sarea_id "
                + "WHERE "
                + "v.date_beg BETWEEN ?1 AND ?2 AND "
                + "v.date_end BETWEEN ?1 AND ?2 "
                + (authOffices != null ? " AND ctr.office_id IN (" + authOffices + ") " : "")
                + "AND ctr.`last`";
        MySQLQuery q = new MySQLQuery(str);
        q.setParam(1, begDt);
        q.setParam(2, endDt);
        Object[][] data = q.getRecords(conn);

        MySQLReport rep = new MySQLReport("Reporte de Vacaciones", "", "vacaciones", MySQLQuery.now(conn));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        rep.getSubTitles().add("Periodo : " + sdf.format(begDt)
                + " -  " + sdf.format(endDt));
        rep.setShowNumbers(true);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().get(0).setWrap(true);
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.LEFT, "dd/MM/yyyy"));//1
        rep.setZoomFactor(85);
        rep.setVerticalFreeze(5);
        Table tbl = new Table("Reporte de Vacaciones");
        tbl.getColumns().add(new Column("Documento", 20, 0));
        tbl.getColumns().add(new Column("Empleado", 50, 0));
        tbl.getColumns().add(new Column("Ciudad", 30, 0));
        tbl.getColumns().add(new Column("Empleador", 35, 0));
        tbl.getColumns().add(new Column("Cargo", 35, 0));
        tbl.getColumns().add(new Column("Proceso", 35, 0));
        tbl.getColumns().add(new Column("Inicio", 20, 1));
        tbl.getColumns().add(new Column("Fin", 20, 1));
        tbl.getColumns().add(new Column("Descripción", 60, 0));
        tbl.setData(data);
        if (tbl.getData().length > 0) {
            rep.getTables().add(tbl);
        }
        return rep;
    }

    public static MySQLReport getEmpPayment(Date payMonth, boolean qna1, boolean invertNames, String authOffices, Integer enterpriseId, Integer cityId, Integer employeerId,
            Integer sbareaId, boolean salesman, Integer areaId, Integer posId, Integer officeId, String employeerName, String enterpriseName, String cityName,
            String areaName, String sbAreaName, String titleArea, String titleSbArea, Connection conn) throws Exception {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(payMonth);
        GregorianCalendar gcBeg = new GregorianCalendar();
        gcBeg.setTime(payMonth);
        GregorianCalendar gcEnd = new GregorianCalendar();
        gcEnd.setTime(payMonth);
        if (qna1) {
            gcBeg.set(GregorianCalendar.DAY_OF_MONTH, 1);
            gcEnd.set(GregorianCalendar.DAY_OF_MONTH, 15);
            gc.set(GregorianCalendar.DAY_OF_MONTH, 1);
        } else {
            gcBeg.set(GregorianCalendar.DAY_OF_MONTH, 16);
            gcEnd.set(GregorianCalendar.DAY_OF_MONTH, gc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
            gc.set(GregorianCalendar.DAY_OF_MONTH, 16);
        }
        MySQLQuery cq = new MySQLQuery("SELECT COUNT(*)=0 FROM `per_emp_pay` WHERE pay_date = ?1");
        cq.setParam(1, Dates.trimDate(gc.getTime()));
        if (cq.getAsBoolean(conn)) {
            throw new Exception("No se han calculado las horas extras para el periodo.");
        }
        StringBuilder str = new StringBuilder("SELECT "
                + "emp.id, "//0
                + "ctr.id, "//1
                + "emp.document, "//2
                + "CONCAT(" + (invertNames ? "emp.last_name,' ',emp.first_name" : "emp.first_name,' ',emp.last_name") + ") AS nm, "//3
                + "en.`name`, "//4
                + "ci.`name`, "//5
                + "of.`name`, "//6
                + "empl.`name`, "//7
                + "a.`name`,"
                + "sba.`name`, "//8
                + "pos.`name`, "//9
                + "pay.base, "//10
                + "ctr.beg_date, "//11
                + "ctr.leave_date, "//12
                + "ctr.employeer_id, "//13
                + "pay.id, "//14
                + "days_acc, "//15
                + "days_lea, "//16
                + "days_pen, "//17
                + "days_lic, "//18
                + "total_days, "//19
                + "0 "//20
                + "FROM "
                + "per_employee AS emp "
                + "INNER JOIN per_contract AS ctr ON ctr.emp_id = emp.id AND ctr.active = 1  "
                + "INNER JOIN per_emp_pay AS pay ON pay.employee_id = emp.id AND pay.employeer_id = ctr.employeer_id AND pay.pay_date = ?3 "
                + "INNER JOIN per_pos AS pos ON ctr.pos_id = pos.id "
                + "INNER JOIN per_sbarea AS sba ON pos.sarea_id = sba.id "
                + "INNER JOIN per_area a ON a.id = sba.area_id "
                + "INNER JOIN enterprise AS en ON en.id = ctr.enterprise_id "
                + "INNER JOIN city AS ci ON ci.id = ctr.city_id "
                + "INNER JOIN per_employeer AS empl ON ctr.employeer_id = empl.id "
                + "INNER JOIN per_office AS of ON of.id = ctr.office_id "
                + "WHERE "
                + (authOffices != null ? "ctr.office_id IN (" + authOffices + ") AND " : "")
                + "emp.active = 1 AND "
                + "IF(ctr.leave_date IS NULL, TRUE,?1 <= ctr.leave_date) AND ctr.beg_date <= ?2 ");
        if (enterpriseId != null) {
            str.append(" AND ctr.enterprise_id = ").append(enterpriseId).append(" ");
        }
        if (cityId != null) {
            str.append(" AND ctr.city_id = ").append(cityId).append(" ");
        }
        if (employeerId != null) {
            if (employeerId < 0) {
                str.append(" AND ctr.employeer_id is null ");
            } else {
                str.append(" AND ctr.employeer_id = ").append(employeerId).append(" ");
            }
        }
        if (sbareaId != null) {
            str.append(" AND pos.sarea_id = ").append(sbareaId).append(" ");
        } else if (!salesman) { //No incluye los vendedores 
            str.append(" AND pos.id != 24 ");
        }
        if (areaId != null) {
            str.append(" AND a.id = ").append(areaId).append(" ");
        }
        if (posId != null) {
            str.append(" AND pos.id = ").append(posId).append(" ");
        }
        if (officeId != null) {
            str.append(" AND ctr.office_id = ").append(officeId).append(" ");
        }
        str.append(" ORDER BY nm ASC ");

        Object[][] dataEmp = new MySQLQuery(str.toString()).setParam(1, Dates.trimDate(gcBeg.getTime())).setParam(2, Dates.trimDate(gcEnd.getTime())).setParam(3, Dates.trimDate(gc.getTime())).getRecords(conn);
        Object[][] dataNovs = new MySQLQuery("SELECT `name`, id, type FROM per_nov_type").getRecords(conn);
        List<Empl> emps = Empl.getEmployees(dataEmp);
        int month = gc.get(GregorianCalendar.MONTH) + 1;
        int year = gc.get(GregorianCalendar.YEAR);

        Object[][] data = new Object[emps.size()][39 + dataNovs.length];
        for (int i = 0; i < emps.size(); i++) {
            Empl e = emps.get(i);
            MySQLQuery mqNovs = new MySQLQuery("SELECT "
                    + "nt.id, "
                    + "IF(nt.type = 'pos', sum(nv.amount), sum(nv.amount) * -1) "//1
                    + "FROM "
                    + "per_emp_nov AS nv "
                    + "INNER JOIN per_nov_type AS nt ON nv.nov_type_id = nt.id "
                    + "WHERE "
                    + "MONTH(nov_date) = " + month + " AND "
                    + "YEAR(nov_date) = " + year + " AND "
                    + "nv.employee_id  = " + e.empId + " AND "
                    + "nv.employeer_id = " + e.employeerId + " AND "
                    + "nv.active = 1 AND "
                    + "DAY(nov_date) = " + (qna1 ? "1" : "16") + " "
                    + " GROUP BY nt.id ");
            MySQLQuery mqExtras = new MySQLQuery("SELECT diu_ord_hrs, diu_ord_mon, diu_dom_hrs, diu_dom_mon, noc_ord_hrs, noc_ord_mon, noc_dom_hrs, noc_dom_mon FROM sigma.per_emp_pay WHERE id = " + e.payId);
            MySQLQuery mqSurchs = new MySQLQuery("SELECT sur_noc_sem_hrs, sur_noc_sem_mon, sur_diu_dom_hrs, sur_diu_dom_mon, sur_noc_dom_hrs, sur_noc_dom_mon FROM sigma.per_emp_pay WHERE id = " + e.payId);

            data[i][0] = e.empDocument;
            data[i][1] = e.empName;
            data[i][2] = e.entName;
            data[i][3] = e.cityName;
            data[i][4] = e.officeName;
            data[i][5] = e.employeerName;
            data[i][6] = e.area;
            data[i][7] = e.subArea;
            data[i][8] = e.posName;
            data[i][9] = e.begDate;
            data[i][10] = e.leaveDate;
            data[i][11] = (e.totalDays + e.daysAcc + e.daysLea + e.daysPen + e.daysLic);
            data[i][12] = e.daysAcc;
            data[i][13] = e.daysLea;
            data[i][14] = e.daysPen;
            data[i][15] = e.daysLic;
            data[i][16] = e.totalDays;//total de los dias
            data[i][17] = e.payBase;
            data[i][18] = (e.payBase.divide(new BigDecimal(30), 2, RoundingMode.HALF_UP)).multiply(getAsBD(e.totalDays));
            //HORAS EXTRAS
            BigDecimal totexs = BigDecimal.ZERO;
            BigDecimal hrsexs = BigDecimal.ZERO;
            BigDecimal totSurchs = BigDecimal.ZERO;
            BigDecimal hrSurchs = BigDecimal.ZERO;
            //Hora extra diurna ordinaria
            data[i][19] = getAsBD(mqExtras, conn, 0, 0);
            data[i][20] = getAsBD(mqExtras, conn, 0, 1);
            //Hora extra diurna dominical
            data[i][23] = getAsBD(mqExtras, conn, 0, 2);
            data[i][24] = getAsBD(mqExtras, conn, 0, 3);
            //Hora extra nocturna ordinaria
            data[i][21] = getAsBD(mqExtras, conn, 0, 4);
            data[i][22] = getAsBD(mqExtras, conn, 0, 5);
            //Hora extra nocturna dominical
            data[i][25] = getAsBD(mqExtras, conn, 0, 6);
            data[i][26] = getAsBD(mqExtras, conn, 0, 7);
            hrsexs = hrsexs.add((BigDecimal) data[i][19]).add((BigDecimal) data[i][21]).add((BigDecimal) data[i][23]).add((BigDecimal) data[i][25]);
            data[i][27] = hrsexs;
            totexs = totexs.add((BigDecimal) data[i][20]).add((BigDecimal) data[i][22]).add((BigDecimal) data[i][24]).add((BigDecimal) data[i][26]);
            data[i][28] = totexs;
            //Recargo nocturno semanal
            data[i][29] = getAsBD(mqSurchs, conn, 0, 0);
            data[i][30] = getAsBD(mqSurchs, conn, 0, 1);
            //Recargo diurno dominical
            data[i][31] = getAsBD(mqSurchs, conn, 0, 2);
            data[i][32] = getAsBD(mqSurchs, conn, 0, 3);
            //Recargo nocturno dominical
            data[i][33] = getAsBD(mqSurchs, conn, 0, 4);
            data[i][34] = getAsBD(mqSurchs, conn, 0, 5);
            hrSurchs = hrSurchs.add((BigDecimal) data[i][29]).add((BigDecimal) data[i][31]).add((BigDecimal) data[i][33]);
            data[i][35] = hrSurchs;
            totSurchs = totSurchs.add((BigDecimal) data[i][30]).add((BigDecimal) data[i][32]).add((BigDecimal) data[i][34]);
            data[i][36] = totSurchs;
            BigDecimal totnov = BigDecimal.ZERO;
            Object dataNovsEmp[][] = mqNovs.getRecords(conn);
            for (int j = 0; j < dataNovs.length; j++) { //ciclo que permite ingresar los valores de novedades por empleado
                for (Object[] obj : dataNovsEmp) {
                    if (obj[0].equals(dataNovs[j][1])) {
                        data[i][37 + j] = obj[1];
                        totnov = totnov.add((BigDecimal) data[i][37 + j]);
                    }
                }
            }
            data[i][37 + dataNovs.length] = totnov;
            //total de todo los calculos
            BigDecimal vlrdia = e.payBase.divide(new BigDecimal(30), 2, RoundingMode.HALF_UP);
            data[i][37 + dataNovs.length + 1] = totexs.add(totnov).add(vlrdia.multiply(getAsBD(e.totalDays))).add(totSurchs);
        }
        String[] months = new String[]{"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        String qna;
        if (qna1) {
            qna = "Primera Quincena de " + months[gc.get(GregorianCalendar.MONTH)];
        } else {
            qna = "Segunda Quincena de " + months[gc.get(GregorianCalendar.MONTH)];
        }
        String title = "Empleador: " + rem(employeerName) + ", Empresa: " + rem(enterpriseName) + ", Ciudad: " + rem(cityName);
        MySQLReport rep = new MySQLReport("Planilla Quincenal - " + qna, title, "planilla", MySQLQuery.now(conn));
        rep.setShowNumbers(true);
        rep.getSubTitles().add("Área: " + rem(areaName));
        rep.getSubTitles().add("Súb Área: " + rem(sbAreaName));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.LEFT, "dd/MM/yyyy"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "###,##0.0#"));//3
        rep.setZoomFactor(80);
        rep.getFormats().get(0).setWrap(true);
        rep.setHorizontalFreeze(4);
        rep.setVerticalFreeze(6);
        List<Column> cols = new ArrayList<>();
        cols.add(new Column("Documento", 15, 0));//0
        cols.add(new Column("Empleado", 30, 0));//1
        cols.add(new Column("Empresa", 12, 0));//2
        cols.add(new Column("Ciudad", 12, 0));//3
        cols.add(new Column("Oficina", 30, 0));//4
        cols.add(new Column("Empleador", 15, 0));//5
        cols.add(new Column(titleArea, 25, 0));//6
        cols.add(new Column(titleSbArea, 25, 0));//6
        cols.add(new Column("Cargo", 22, 0));//7
        cols.add(new Column("Inicio", 12, 1));//8
        cols.add(new Column("Retiro", 12, 1));//9
        cols.add(new Column("Días Trabajados", 12, 2));//10
        cols.add(new Column("Accidentes", 12, 3));//11
        cols.add(new Column("Incapacidad", 13, 3));//12
        cols.add(new Column("Sanciones", 12, 3));//13
        cols.add(new Column("Permisos", 12, 3));//14
        cols.add(new Column("Total Días", 12, 2));//15
        cols.add(new Column("Básico", 15, 3));//16
        cols.add(new Column("Quincena", 15, 3));//17
        cols.add(new Column("Diu. Ord.", 13, 3)); //horas extras
        cols.add(new Column("Vlr Diu. Ord.", 15, 3));
        cols.add(new Column("Noct. Ord.", 13, 3)); //horas extras
        cols.add(new Column("Vlr Noct. Ord.", 15, 3));
        cols.add(new Column("Diu. Dom.", 13, 3)); //horas extras
        cols.add(new Column("Vlr Diu. Dom.", 15, 3));
        cols.add(new Column("Noct. Dom.", 13, 3)); //horas extras
        cols.add(new Column("Vlr Noct. Dom.", 15, 3));
        cols.add(new Column("Tot Exs", 15, 3));
        cols.add(new Column("Vlr Tot Exs", 15, 3));
        cols.add(new Column("Noct. Ord.", 13, 3)); //recargos 
        cols.add(new Column("Vlr Noct. Ord.", 15, 3));
        cols.add(new Column("Diu. Dom.", 13, 3)); //recargos 
        cols.add(new Column("Vlr Diu. Dom.", 15, 3));
        cols.add(new Column("Noct. Dom.", 13, 3)); //recargos 
        cols.add(new Column("Vlr Noct. Dom.", 15, 3));
        cols.add(new Column("Tot Recs.", 15, 3));
        cols.add(new Column("Vlr Tot Recs", 15, 3));
        for (Object[] nov : dataNovs) {
            cols.add(new Column((String) nov[0], 15, 3));
        }
        cols.add(new Column("Total", 15, 3));
        cols.add(new Column("Vlr Total ", 15, 3));
        Table tb = new Table("Planilla Quincenal - " + qna);
        tb.setColumns(cols);
        TableHeader header = new TableHeader();
        tb.getHeaders().add(header);
        header.getColums().add(new HeaderColumn("Datos Empleados", 11, 1));
        header.getColums().add(new HeaderColumn("Días Trabajados", 1, 2));
        header.getColums().add(new HeaderColumn("Deducciones", 4, 1));
        header.getColums().add(new HeaderColumn("Total Días", 1, 2));
        header.getColums().add(new HeaderColumn("Salario", 2, 1));
        header.getColums().add(new HeaderColumn("Horas Extras", 10, 1));
        header.getColums().add(new HeaderColumn("Recargos", 8, 1));
        header.getColums().add(new HeaderColumn("Novedades de Nomina", dataNovs.length + 1, 1));
        header.getColums().add(new HeaderColumn("Vlr Total", 1, 2));
        tb.setSummaryRow(new SummaryRow("Totales", 12));
        tb.setData(data);
        if (tb.getData().length > 0) {
            rep.getTables().add(tb);
        }
        rep.setMultiRowTitles(true);
        return rep;
    }

    public static MySQLReport getEmpPaymentClc(Date payMonth, boolean qna1, boolean invertNames, String authOffices, Integer enterpriseId, Integer cityId, Integer employeerId,
            Integer sbareaId, boolean salesman, Integer areaId, Integer posId, Integer officeId, String employeerName, String enterpriseName, String cityName,
            String areaName, String sbAreaName, String titleArea, String titleSbArea, Connection conn) throws Exception {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(payMonth);
        GregorianCalendar gcBeg = new GregorianCalendar();
        gcBeg.setTime(payMonth);
        GregorianCalendar gcEnd = new GregorianCalendar();
        gcEnd.setTime(payMonth);
        if (qna1) {
            gcBeg.set(GregorianCalendar.DAY_OF_MONTH, 1);
            gcEnd.set(GregorianCalendar.DAY_OF_MONTH, 15);
            gc.set(GregorianCalendar.DAY_OF_MONTH, 1);
        } else {
            gcBeg.set(GregorianCalendar.DAY_OF_MONTH, 16);
            gcEnd.set(GregorianCalendar.DAY_OF_MONTH, gc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
            gc.set(GregorianCalendar.DAY_OF_MONTH, 16);
        }
        MySQLQuery cq = new MySQLQuery("SELECT COUNT(*)=0 FROM `per_emp_pay` WHERE pay_date = ?1");
        cq.setParam(1, Dates.trimDate(gc.getTime()));
        if (cq.getAsBoolean(conn)) {
            throw new Exception("No se han calculado las horas extras para el periodo.");
        }
        StringBuilder str = new StringBuilder("SELECT "
                + "emp.id, "//0
                + "ctr.id, "//1
                + "emp.document, "//2
                + "CONCAT(" + (invertNames ? "emp.last_name,' ',emp.first_name" : "emp.first_name,' ',emp.last_name") + ") AS nm, "//3
                + "en.`name`, "//4
                + "ci.`name`, "//5
                + "of.`name`, "//6
                + "empl.`name`, "//7
                + "a.`name`,"
                + "sba.`name`, "//8
                + "pos.`name`, "//9
                + "pay.base, "//10
                + "ctr.beg_date, "//11
                + "ctr.leave_date, "//12
                + "ctr.employeer_id, "//13
                + "pay.id, "//14
                + "days_acc, "//15
                + "days_lea, "//16
                + "days_pen, "//17
                + "days_lic, "//18
                + "total_days, "//19
                + "days_vac "//20
                + "FROM "
                + "per_employee AS emp "
                + "INNER JOIN per_contract AS ctr ON ctr.emp_id = emp.id AND ctr.active = 1  "
                + "INNER JOIN per_emp_pay AS pay ON pay.employee_id = emp.id AND pay.employeer_id = ctr.employeer_id AND pay.pay_date = ?3 "
                + "INNER JOIN per_pos AS pos ON ctr.pos_id = pos.id "
                + "INNER JOIN per_sbarea AS sba ON pos.sarea_id = sba.id "
                + "INNER JOIN per_area a ON a.id = sba.area_id "
                + "INNER JOIN enterprise AS en ON en.id = ctr.enterprise_id "
                + "INNER JOIN city AS ci ON ci.id = ctr.city_id "
                + "INNER JOIN per_employeer AS empl ON ctr.employeer_id = empl.id "
                + "INNER JOIN per_office AS of ON of.id = ctr.office_id "
                + "WHERE "
                + (authOffices != null ? "ctr.office_id IN (" + authOffices + ") AND " : "")
                + "emp.active = 1 AND "
                + "IF(ctr.leave_date IS NULL, TRUE,?1 <= ctr.leave_date) AND ctr.beg_date <= ?2 ");
        if (enterpriseId != null) {
            str.append(" AND ctr.enterprise_id = ").append(enterpriseId).append(" ");
        }
        if (cityId != null) {
            str.append(" AND ctr.city_id = ").append(cityId).append(" ");
        }
        if (employeerId != null) {
            if (employeerId < 0) {
                str.append(" AND ctr.employeer_id is null ");
            } else {
                str.append(" AND ctr.employeer_id = ").append(employeerId).append(" ");
            }
        }
        if (sbareaId != null) {
            str.append(" AND pos.sarea_id = ").append(sbareaId).append(" ");
        } else if (!salesman) { //No incluye los vendedores 
            str.append(" AND pos.id != 24 ");
        }
        if (areaId != null) {
            str.append(" AND a.id = ").append(areaId).append(" ");
        }
        if (posId != null) {
            str.append(" AND pos.id = ").append(posId).append(" ");
        }
        if (officeId != null) {
            str.append(" AND ctr.office_id = ").append(officeId).append(" ");
        }
        str.append(" ORDER BY nm ASC ");

        Object[][] dataEmp = new MySQLQuery(str.toString()).setParam(1, Dates.trimDate(gcBeg.getTime())).setParam(2, Dates.trimDate(gcEnd.getTime())).setParam(3, Dates.trimDate(gc.getTime())).getRecords(conn);
        Object[][] dataNovs = new MySQLQuery("SELECT `name`, id, type FROM per_nov_type").getRecords(conn);
        List<Empl> emps = Empl.getEmployees(dataEmp);
        int month = gc.get(GregorianCalendar.MONTH) + 1;
        int year = gc.get(GregorianCalendar.YEAR);

        Object[][] data = new Object[emps.size()][40 + dataNovs.length];
        for (int i = 0; i < emps.size(); i++) {
            Empl e = emps.get(i);
            MySQLQuery mqNovs = new MySQLQuery("SELECT "
                    + "nt.id, "
                    + "IF(nt.type = 'pos', sum(nv.amount), sum(nv.amount) * -1) "//1
                    + "FROM "
                    + "per_emp_nov AS nv "
                    + "INNER JOIN per_nov_type AS nt ON nv.nov_type_id = nt.id "
                    + "WHERE "
                    + "MONTH(nov_date) = " + month + " AND "
                    + "YEAR(nov_date) = " + year + " AND "
                    + "nv.employee_id  = " + e.empId + " AND "
                    + "nv.employeer_id = " + e.employeerId + " AND "
                    + "nv.active = 1 AND "
                    + "DAY(nov_date) = " + (qna1 ? "1" : "16") + " "
                    + " GROUP BY nt.id ");
            MySQLQuery mqExtras = new MySQLQuery("SELECT diu_ord_hrs, diu_ord_mon, diu_dom_hrs, diu_dom_mon, noc_ord_hrs, noc_ord_mon, noc_dom_hrs, noc_dom_mon FROM sigma.per_emp_pay WHERE id = " + e.payId);
            MySQLQuery mqSurchs = new MySQLQuery("SELECT sur_noc_sem_hrs, sur_noc_sem_mon, sur_diu_dom_hrs, sur_diu_dom_mon, sur_noc_dom_hrs, sur_noc_dom_mon FROM sigma.per_emp_pay WHERE id = " + e.payId);

            data[i][0] = e.empDocument;
            data[i][1] = e.empName;
            data[i][2] = e.entName;
            data[i][3] = e.cityName;
            data[i][4] = e.officeName;
            data[i][5] = e.employeerName;
            data[i][6] = e.area;
            data[i][7] = e.subArea;
            data[i][8] = e.posName;
            data[i][9] = e.begDate;
            data[i][10] = e.leaveDate;
            data[i][11] = (e.totalDays + e.daysAcc + e.daysLea + e.daysPen + e.daysLic + e.daysVac);
            data[i][12] = e.daysAcc;
            data[i][13] = e.daysLea;
            data[i][14] = e.daysPen;
            data[i][15] = e.daysLic;
            data[i][16] = e.daysVac;
            data[i][17] = e.totalDays;//total de los dias
            data[i][18] = e.payBase;
            data[i][19] = (e.payBase.divide(new BigDecimal(30), 2, RoundingMode.HALF_UP)).multiply(getAsBD(e.totalDays));
            //HORAS EXTRAS
            BigDecimal totexs = BigDecimal.ZERO;
            BigDecimal hrsexs = BigDecimal.ZERO;
            BigDecimal totSurchs = BigDecimal.ZERO;
            BigDecimal hrSurchs = BigDecimal.ZERO;
            //Hora extra diurna ordinaria
            data[i][20] = getAsBD(mqExtras, conn, 0, 0);
            data[i][21] = getAsBD(mqExtras, conn, 0, 1);
            //Hora extra diurna dominical
            data[i][22] = getAsBD(mqExtras, conn, 0, 2);
            data[i][23] = getAsBD(mqExtras, conn, 0, 3);
            //Hora extra nocturna ordinaria
            data[i][24] = getAsBD(mqExtras, conn, 0, 4);
            data[i][25] = getAsBD(mqExtras, conn, 0, 5);
            //Hora extra nocturna dominical
            data[i][26] = getAsBD(mqExtras, conn, 0, 6);
            data[i][27] = getAsBD(mqExtras, conn, 0, 7);
            hrsexs = hrsexs.add((BigDecimal) data[i][20]).add((BigDecimal) data[i][22]).add((BigDecimal) data[i][24]).add((BigDecimal) data[i][26]);
            data[i][28] = hrsexs;
            totexs = totexs.add((BigDecimal) data[i][21]).add((BigDecimal) data[i][23]).add((BigDecimal) data[i][25]).add((BigDecimal) data[i][27]);
            data[i][29] = totexs;
            //Recargo nocturno semanal
            data[i][30] = getAsBD(mqSurchs, conn, 0, 0);
            data[i][31] = getAsBD(mqSurchs, conn, 0, 1);
            //Recargo diurno dominical
            data[i][32] = getAsBD(mqSurchs, conn, 0, 2);
            data[i][33] = getAsBD(mqSurchs, conn, 0, 3);
            //Recargo nocturno dominical
            data[i][34] = getAsBD(mqSurchs, conn, 0, 4);
            data[i][35] = getAsBD(mqSurchs, conn, 0, 5);
            hrSurchs = hrSurchs.add((BigDecimal) data[i][29]).add((BigDecimal) data[i][31]).add((BigDecimal) data[i][33]);
            data[i][36] = hrSurchs;
            totSurchs = totSurchs.add((BigDecimal) data[i][30]).add((BigDecimal) data[i][32]).add((BigDecimal) data[i][34]);
            data[i][37] = totSurchs;
            BigDecimal totnov = BigDecimal.ZERO;
            Object dataNovsEmp[][] = mqNovs.getRecords(conn);
            for (int j = 0; j < dataNovs.length; j++) { //ciclo que permite ingresar los valores de novedades por empleado
                for (Object[] obj : dataNovsEmp) {
                    if (obj[0].equals(dataNovs[j][1])) {
                        data[i][38 + j] = obj[1];
                        totnov = totnov.add((BigDecimal) data[i][38 + j]);
                    }
                }
            }
            data[i][38 + dataNovs.length] = totnov;
            //total de todo los calculos
            BigDecimal vlrdia = e.payBase.divide(new BigDecimal(30), 2, RoundingMode.HALF_UP);
            data[i][38 + dataNovs.length + 1] = totexs.add(totnov).add(vlrdia.multiply(getAsBD(e.totalDays))).add(totSurchs);
        }
        String[] months = new String[]{"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        String qna;
        if (qna1) {
            qna = "Primera Quincena de " + months[gc.get(GregorianCalendar.MONTH)];
        } else {
            qna = "Segunda Quincena de " + months[gc.get(GregorianCalendar.MONTH)];
        }
        String title = "Empleador: " + rem(employeerName) + ", Empresa: " + rem(enterpriseName) + ", Ciudad: " + rem(cityName);
        MySQLReport rep = new MySQLReport("Planilla Quincenal - " + qna, title, "planilla", MySQLQuery.now(conn));
        rep.setShowNumbers(true);
        rep.getSubTitles().add("Área: " + rem(areaName));
        rep.getSubTitles().add("Súb Área: " + rem(sbAreaName));
        rep.getFormats().add(new CellFormat(MySQLReportWriter.LABEL, MySQLReportWriter.LEFT));//0
        rep.getFormats().add(new CellFormat(MySQLReportWriter.DATE, MySQLReportWriter.LEFT, "dd/MM/yyyy"));//1
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "#"));//2
        rep.getFormats().add(new CellFormat(MySQLReportWriter.NUMBER, MySQLReportWriter.RIGHT, "###,##0.0#"));//3
        rep.setZoomFactor(80);
        rep.getFormats().get(0).setWrap(true);
        rep.setHorizontalFreeze(4);
        rep.setVerticalFreeze(6);
        List<Column> cols = new ArrayList<>();
        cols.add(new Column("Documento", 15, 0));//0
        cols.add(new Column("Empleado", 30, 0));//1
        cols.add(new Column("Empresa", 12, 0));//2
        cols.add(new Column("Ciudad", 12, 0));//3
        cols.add(new Column("Oficina", 30, 0));//4
        cols.add(new Column("Empleador", 15, 0));//5
        cols.add(new Column(titleArea, 25, 0));//6
        cols.add(new Column(titleSbArea, 25, 0));//6
        cols.add(new Column("Cargo", 22, 0));//7
        cols.add(new Column("Inicio", 12, 1));//8
        cols.add(new Column("Retiro", 12, 1));//9
        cols.add(new Column("Días Trabajados", 12, 2));//10
        cols.add(new Column("Accidentes", 12, 3));//11
        cols.add(new Column("Incapacidad", 13, 3));//12
        cols.add(new Column("Sanciones", 12, 3));//13
        cols.add(new Column("Permisos", 12, 3));//14
        cols.add(new Column("Vacaciones", 13, 3));//15
        cols.add(new Column("Total Días", 12, 2));//16
        cols.add(new Column("Básico", 15, 3));//17
        cols.add(new Column("Quincena", 15, 3));//18
        cols.add(new Column("Diu. Ord.", 13, 3)); //horas extras
        cols.add(new Column("Vlr Diu. Ord.", 15, 3));
        cols.add(new Column("Noct. Ord.", 13, 3)); //horas extras
        cols.add(new Column("Vlr Noct. Ord.", 15, 3));
        cols.add(new Column("Diu. Dom.", 13, 3)); //horas extras
        cols.add(new Column("Vlr Diu. Dom.", 15, 3));
        cols.add(new Column("Noct. Dom.", 13, 3)); //horas extras
        cols.add(new Column("Vlr Noct. Dom.", 15, 3));
        cols.add(new Column("Tot Exs", 15, 3));
        cols.add(new Column("Vlr Tot Exs", 15, 3));
        cols.add(new Column("Noct. Ord.", 13, 3)); //recargos 
        cols.add(new Column("Vlr Noct. Ord.", 15, 3));
        cols.add(new Column("Diu. Dom.", 13, 3)); //recargos 
        cols.add(new Column("Vlr Diu. Dom.", 15, 3));
        cols.add(new Column("Noct. Dom.", 13, 3)); //recargos 
        cols.add(new Column("Vlr Noct. Dom.", 15, 3));
        cols.add(new Column("Tot Recs.", 15, 3));
        cols.add(new Column("Vlr Tot Recs", 15, 3));
        for (Object[] nov : dataNovs) {
            cols.add(new Column((String) nov[0], 15, 3));
        }
        cols.add(new Column("Total", 15, 3));
        cols.add(new Column("Vlr Total ", 15, 3));
        Table tb = new Table("Planilla Quincenal - " + qna);
        tb.setColumns(cols);
        TableHeader header = new TableHeader();
        tb.getHeaders().add(header);
        header.getColums().add(new HeaderColumn("Datos Empleados", 11, 1));
        header.getColums().add(new HeaderColumn("Días Trabajados", 1, 2));
        header.getColums().add(new HeaderColumn("Deducciones", 5, 1));
        header.getColums().add(new HeaderColumn("Total Días", 1, 2));
        header.getColums().add(new HeaderColumn("Salario", 2, 1));
        header.getColums().add(new HeaderColumn("Horas Extras", 10, 1));
        header.getColums().add(new HeaderColumn("Recargos", 8, 1));
        header.getColums().add(new HeaderColumn("Novedades de Nomina", dataNovs.length + 1, 1));
        header.getColums().add(new HeaderColumn("Vlr Total", 1, 2));
        tb.setSummaryRow(new SummaryRow("Totales", 12));
        tb.setData(data);
        if (tb.getData().length > 0) {
            rep.getTables().add(tb);
        }
        rep.setMultiRowTitles(true);
        return rep;
    }

    private static String rem(String str) {
        return str.replaceAll("\\[", "").replaceAll("]", "");
    }

    private static BigDecimal getAsBD(MySQLQuery mq, Connection conn, int row, int col) throws Exception {
        Object[][] data = mq.getRecords(conn);
        if (row < data.length) {
            Object[] drow = data[row];
            if (col < drow.length) {
                return getAsBD(drow[col]);
            } else {
                return BigDecimal.ZERO;
            }
        } else {
            return BigDecimal.ZERO;
        }
    }

    private static BigDecimal getAsBD(Object o) {
        return MySQLQuery.getAsBigDecimal(o, true);
    }

    private static class Empl {

        Integer empId;
        Integer ctrId;
        String empDocument;
        String empName;
        String entName;
        String cityName;
        String officeName;
        String employeerName;
        String area;
        String subArea;
        String posName;
        BigDecimal payBase;
        Date begDate;
        Date leaveDate;
        Integer employeerId;
        Integer payId;
        Integer daysAcc;
        Integer daysLea;
        Integer daysPen;
        Integer daysLic;
        Integer daysVac;
        Integer totalDays;

        public static List<Empl> getEmployees(Object[][] data) throws Exception {
            List<Empl> emps = new ArrayList<>();
            for (Object[] row : data) {
                Empl e = new Empl();
                e.empId = MySQLQuery.getAsInteger(row[0]);
                e.ctrId = MySQLQuery.getAsInteger(row[1]);
                e.empDocument = MySQLQuery.getAsString(row[2]);
                e.empName = MySQLQuery.getAsString(row[3]);
                e.entName = MySQLQuery.getAsString(row[4]);
                e.cityName = MySQLQuery.getAsString(row[5]);
                e.officeName = MySQLQuery.getAsString(row[6]);
                e.employeerName = MySQLQuery.getAsString(row[7]);
                e.area = MySQLQuery.getAsString(row[8]);
                e.subArea = MySQLQuery.getAsString(row[9]);
                e.posName = MySQLQuery.getAsString(row[10]);
                e.payBase = MySQLQuery.getAsBigDecimal(row[11], true);
                e.begDate = MySQLQuery.getAsDate(row[12]);
                e.leaveDate = MySQLQuery.getAsDate(row[13]);
                e.employeerId = MySQLQuery.getAsInteger(row[14]);
                e.payId = MySQLQuery.getAsInteger(row[15]);
                e.daysAcc = MySQLQuery.getAsInteger(row[16]);
                e.daysLea = MySQLQuery.getAsInteger(row[17]);
                e.daysPen = MySQLQuery.getAsInteger(row[18]);
                e.daysLic = MySQLQuery.getAsInteger(row[19]);
                e.totalDays = MySQLQuery.getAsInteger(row[20]);
                e.daysVac = MySQLQuery.getAsInteger(row[21]);
                emps.add(e);
            }
            return emps;
        }
    }
}
