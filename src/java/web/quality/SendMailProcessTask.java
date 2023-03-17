package web.quality;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import service.MySQL.MySQLCommon;
import utilities.Dates;
import utilities.MySQLQuery;
import utilities.SysTask;

@Singleton
public class SendMailProcessTask {

    @Schedule(hour = "9", minute = "15")
    protected void processRequest() {
        System.out.println("EJECUNTADO TAREA CORREOS AUTOMATICOS");
        try {
            try (Connection con = MySQLCommon.getConnection("sigmads", null)) {

                SysTask t = new SysTask(SendMailProcessTask.class, System.getProperty("user.name"), 1, con);
                try {
                    SysMailProcess[] process = SysMailProcess.getAllActiveAndAuto(con);

                    if (process == null || process.length == 0) {
                        return;
                    }
                    System.out.println("procesos automaticos: " + process.length);
                    for (SysMailProcess proc : process) {
                        boolean startProcess = false;
                        boolean fixProcess = false;
                        GregorianCalendar gc = new GregorianCalendar();
                        SimpleDateFormat sdf = Dates.getSQLDateFormat();
                        Date now = new Date();
                        Date progAux = proc.startTime;
                        now = sdf.parse(sdf.format(now));
                        progAux = sdf.parse(sdf.format(progAux));

                        GregorianCalendar prog = new GregorianCalendar();
                        prog.setTime(progAux);
                        prog.set(gc.HOUR_OF_DAY, 0);
                        prog.set(gc.MINUTE, 0);
                        prog.set(gc.SECOND, 0);
                        prog.set(gc.MILLISECOND, 0);
                        prog.set(gc.HOUR, 0);

                        GregorianCalendar cur = new GregorianCalendar();
                        cur.setTime(now);
                        cur.set(gc.HOUR_OF_DAY, 0);
                        cur.set(gc.MINUTE, 0);
                        cur.set(gc.SECOND, 0);
                        cur.set(gc.MILLISECOND, 0);
                        cur.set(gc.HOUR, 0);

                        switch (proc.periodType) {
                            case "week":
                                prog.add(GregorianCalendar.WEEK_OF_YEAR, proc.period);
                                break;
                            case "month":
                                prog.add(GregorianCalendar.MONTH, proc.period);
                                break;
                            case "day":
                                prog.add(GregorianCalendar.DAY_OF_YEAR, proc.period);
                                break;
                            default:
                                return;
                        }

                        progAux = sdf.parse(sdf.format(prog.getTime()));
                        now = sdf.parse(sdf.format(cur.getTime()));

                        System.out.println("Proceso: " + proc.name);
                        System.out.println("programado para: " + progAux + " -  Fecha actual: " + now );
                        
                        if (progAux.equals(now)) {
                            System.out.println("Proceso programado ....");
                            startProcess = true;
                        } else if (progAux.after(now)) {
                            System.out.println("Proceso atrasado ....");
                            proc.startTime = prog.getTime();
                            new SysMailProcess().update(proc, con);//restaurando fecha

                            startProcess = true;
                            fixProcess = true;
                        }                        

                        if (startProcess) {
                            if (proc.columns != null && proc.queryContent != null) {
                                String tableHtml = "";
                                Object[][] data = new MySQLQuery(proc.queryContent).print().getRecords(con);
                                if (data != null && data.length > 0) {
                                    tableHtml = getHtmlTable(proc.columns, data);
                                    proc.message += tableHtml;
                                    new SysMailUtil().sendMail(proc.id, proc.subject, proc.message, con, true);
                                    if (proc.queryAction != null) {
                                        new MySQLQuery(proc.queryAction).executeUpdate(con);
                                    }
                                }
                            } else {
                                new SysMailUtil().sendMail(proc.id, proc.subject, proc.message, con, true);
                            }

                            if (!fixProcess) {
                                proc.startTime = new Date();
                                new SysMailProcess().update(proc, con);
                            }
                        }
                    }
                    t.success(con);
                } catch (Exception ex) {
                    t.error(ex, con);
                    Logger.getLogger(SendMailProcessTask.class.getName()).log(Level.SEVERE, null, ex);
                    try {
                        SendMail.sendMail(con, "soporte@qualisys.com.co", "Correo de Procesos Auto", "Error en envio correo automaticos " + ex.getMessage(), "Erroren envio correo automaticos " + ex.getMessage());
                    } catch (Exception ex1) {
                        Logger.getLogger(SendMailProcessTask.class.getName()).log(Level.SEVERE, null, ex1);
                    }
                }
            }
        } catch (Exception ex) {
            // para errores de cx a bd
            Logger.getLogger(SendMailProcessTask.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String getHtmlTable(String columns, Object[][] data) {
        String[] cols = columns.split(",");
        StringBuilder buf = new StringBuilder();
        buf.append("<table style=\"width:100%\"><tr>");

        for (String col : cols) {
            buf.append("<th>");
            buf.append(col != null ? MySQLQuery.getAsString(col) : "");
            buf.append("</th>");
        }

        for (int i = 0; i < data.length; i++) {
            buf.append("<tr>");
            for (int j = 0; j < cols.length; j++) {
                buf.append("<td>");
                buf.append(data[i][j] != null ? MySQLQuery.getAsString(data[i][j]) : "");
                buf.append("</td>");
            }
            buf.append("</tr>");
        }
        buf.append("</table>");
        return buf.toString();
    }

}
