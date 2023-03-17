package web.vicky.model;

import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import utilities.Dates;
import utilities.MySQLQuery;

public class ScheduleCheck {

    public boolean open;
    public String title;
    public String subtitle;
    public String msg;

    public ScheduleCheck(boolean open, String title, String subtitle, String msg) {
        this.open = open;
        this.title = title;
        this.subtitle = subtitle;
        this.msg = msg;
    }

    public ScheduleCheck(boolean open) {
        this.open = open;
    }

    private static String getWeekDay(int gd) {
        switch (gd) {
            case GregorianCalendar.MONDAY:
                return "mo";
            case GregorianCalendar.TUESDAY:
                return "tue";
            case GregorianCalendar.WEDNESDAY:
                return "wed";
            case GregorianCalendar.THURSDAY:
                return "thu";
            case GregorianCalendar.FRIDAY:
                return "fri";
            case GregorianCalendar.SATURDAY:
                return "sat";
            case GregorianCalendar.SUNDAY:
                return "sun";
            default:
                throw new RuntimeException();
        }
    }

    private static int getTimeAsSeconds(Date d) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(d);
        return (gc.get(GregorianCalendar.HOUR_OF_DAY) * 3600) + (gc.get(GregorianCalendar.MINUTE) * 60) + gc.get(GregorianCalendar.SECOND);
    }

    private static String getSecondsAsString(int secs) {
        int hours = (int) secs / 3600;
        GregorianCalendar gc = new GregorianCalendar();
        gc.set(GregorianCalendar.HOUR_OF_DAY, hours);
        gc.set(GregorianCalendar.MINUTE, (secs % 3600) / 60);
        return new SimpleDateFormat("hh:mm aa").format(gc.getTime());
    }

    private static int[] getShedule(int officeId, Date d, Connection conn) throws Exception {
        String day;
        d = Dates.trimDate(d);
        if (new MySQLQuery("SELECT COUNT(*) > 0 FROM per_holiday WHERE holi_date = ?1").setParam(1, d).getAsBoolean(conn)) {
            day = "hol";
        } else {
            GregorianCalendar gc = new GregorianCalendar();
            gc.setTime(d);
            day = getWeekDay(gc.get(GregorianCalendar.DAY_OF_WEEK));
        }
        Object[] dayRow = new MySQLQuery("SELECT beg_time, end_time FROM ord_off_schedule WHERE type = ?1 AND office_id = " + officeId + ";").setParam(1, day).getRecord(conn);
        if (dayRow == null) {
            return null;
        }
        return new int[]{getTimeAsSeconds(MySQLQuery.getAsDate(dayRow[0])), getTimeAsSeconds(MySQLQuery.getAsDate(dayRow[1]))};
    }

    private static Date getTomorrow() {
        GregorianCalendar gc = new GregorianCalendar();
        gc.add(GregorianCalendar.DAY_OF_MONTH, 1);
        return gc.getTime();
    }

    public static ScheduleCheck validateSchedule(int officeId, Connection conn) throws Exception {
        Object[] outRecord = new MySQLQuery("SELECT description, end_date FROM ord_off_out_service WHERE office_id = " + officeId + " AND beg_date < now() and (end_date is null OR (end_date IS NOT NULL AND now() < end_date))").getRecord(conn);
        if (outRecord != null) {
            String desc = MySQLQuery.getAsString(outRecord[0]);
            Date endDate = MySQLQuery.getAsDate(outRecord[1]);
            SimpleDateFormat ff = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss aa");
            return new ScheduleCheck(false, "Lo sentimos", "El servicio no está disponible", "Por motivo de:\n" + desc + "." + (endDate != null ? "\nInténtentelo a partir de " + ff.format(endDate) : "\nInténtelo más adelante"));
        }

        Date today = new Date();
        int[] todayRow = getShedule(officeId, today, conn);

        boolean tryTomorrow = false;
        if (todayRow != null) {
            int todayBeg = todayRow[0];
            int todayEnd = todayRow[1];
            int now = getTimeAsSeconds(today);
            if (now < todayBeg) {
                return new ScheduleCheck(false, "Lo sentimos", "El servicio no está disponible", "Inténtelo hoy a partir de las " + getSecondsAsString(todayBeg));
            } else if (now > todayEnd) {
                tryTomorrow = true;
            }
        } else {
            tryTomorrow = true;
        }

        if (tryTomorrow) {
            int[] tomorrow = getShedule(officeId, getTomorrow(), conn);
            if (tomorrow != null) {
                return new ScheduleCheck(false, "Lo sentimos", "El servicio no está disponible", "Inténtelo mañana a partir de las " + getSecondsAsString(tomorrow[0]));
            } else {
                return new ScheduleCheck(false, "Lo sentimos", "El servicio no está disponible", "Inténtelo más adelante");
            }
        }
        return new ScheduleCheck(true);
    }
}
