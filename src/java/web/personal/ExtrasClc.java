package web.personal;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import utilities.Dates;
import utilities.MySQLQuery;

/**
 * @author Fabián Gómez
 */
public class ExtrasClc {

    public static void generate(Connection conn) throws Exception {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");        
        Date dateServer = new MySQLQuery("SELECT DATE(NOW())").getAsDate(conn);
        
        MySQLQuery qData = new MySQLQuery("SELECT "
                + "ev.event_day, "
                + "ev.emp_id, "
                + "(SELECT e.reg_hour FROM per_gate_event e WHERE e.emp_id = ev.emp_id AND e.event_day = ?1 AND e.`type` = 'in' ORDER BY e.reg_hour ASC LIMIT 1) AS begDate, "
                + "(SELECT e.reg_hour FROM per_gate_event e WHERE e.emp_id = ev.emp_id AND e.event_day = ?1 AND e.`type` = 'out' ORDER BY e.reg_hour DESC LIMIT 1) AS endDate, "
                + "p.hour_loss - (SELECT IFNULL(SUM(TIMESTAMPDIFF(MINUTE , s.beg_hour, s.end_hour)/60), 0) FROM per_gate_span s WHERE s.event_day = ?1 AND s.emp_id = ev.emp_id), "
                + "p.hour_loss "
                + "FROM per_gate_event ev "
                + "INNER JOIN per_gate_prof p ON p.id = ev.profile_id "
                + "WHERE ev.event_day = ?1 "
                + "GROUP BY ev.emp_id");

        qData.setParam(1, sf.format(getYesterday(dateServer)));
        Object[][] data = qData.getRecords(conn);

        PerCfg cfg =  new PerCfg().select(1, conn);

        if (data != null) {
            for (Object[] row : data) {
                if (row[2] != null && row[3] != null) {
                    PerWorkday obj = new PerWorkday();
                    obj.qlik = false;
                    Date evDate = MySQLQuery.getAsDate(row[0]);

                    GregorianCalendar gcpay = new GregorianCalendar();
                    gcpay.setTime(evDate);
                    if (gcpay.get(GregorianCalendar.DAY_OF_MONTH) <= 15) {
                        gcpay.set(GregorianCalendar.DAY_OF_MONTH, 1);
                    } else {
                        gcpay.set(GregorianCalendar.DAY_OF_MONTH, 16);
                    }
                    obj.payMonth = gcpay.getTime();
                    obj.empId = MySQLQuery.getAsInteger(row[1]);
                    obj.evDate = evDate;
                    obj.begTime = Dates.trimToMinute(MySQLQuery.getAsDate(row[2]));
                    obj.endTime = Dates.trimToMinute(MySQLQuery.getAsDate(row[3]));
                    obj.hourLoss = MySQLQuery.getAsInteger(row[4]);
                    obj.notes = "Tarea Programada Huellero";
                    obj.authorizedById = null;
                    obj.regById = MySQLQuery.getAsInteger(1);

                    setTimes(obj, null, cfg, conn);

                    if (obj.exDiuSem != BigDecimal.ZERO
                            || obj.exDiuDom != BigDecimal.ZERO
                            || obj.exNocSem != BigDecimal.ZERO
                            || obj.exNocDom != BigDecimal.ZERO
                            || obj.recNocDom != BigDecimal.ZERO
                            || obj.recNocSem != BigDecimal.ZERO
                            || obj.recDiuDom != BigDecimal.ZERO) {
                        obj.hourLoss = MySQLQuery.getAsInteger(row[5]);                        
                        obj.insert(obj, conn);
                    }
                }
            }
        }
        
        //EXTRAS DE JORNADAS ADICIONALES
        MySQLQuery q = new MySQLQuery("SELECT s.event_day, s.emp_id, s.beg_hour, s.end_hour FROM per_gate_span s WHERE s.`type` = 'full' AND s.`event_day` = ?1 AND end_hour IS NOT NULL");
        q.setParam(1, getYesterday(dateServer));
        Object[][] extrs = q.getRecords(conn);

        if (extrs != null) {
            for (Object[] row : extrs) {
                if (row[2] != null && row[3] != null) {
                    PerWorkday obj = new PerWorkday();
                    obj.qlik = false;
                    Date evDate = MySQLQuery.getAsDate(row[0]);

                    GregorianCalendar gcpay = new GregorianCalendar();
                    gcpay.setTime(evDate);
                    if (gcpay.get(GregorianCalendar.DAY_OF_MONTH) <= 15) {
                        gcpay.set(GregorianCalendar.DAY_OF_MONTH, 1);
                    } else {
                        gcpay.set(GregorianCalendar.DAY_OF_MONTH, 16);
                    }
                    obj.payMonth = gcpay.getTime();
                    obj.empId = MySQLQuery.getAsInteger(row[1]);
                    obj.evDate = evDate;
                    obj.begTime = Dates.trimToMinute(MySQLQuery.getAsDate(row[2]));
                    obj.endTime = Dates.trimToMinute(MySQLQuery.getAsDate(row[3]));
                    obj.hourLoss = 0;
                    obj.notes = "Tarea Programada Huellero";
                    obj.authorizedById = null;
                    obj.regById = MySQLQuery.getAsInteger(1);

                    setTimes(obj, null, cfg, conn);

                    if (obj.exDiuSem != BigDecimal.ZERO
                            || obj.exDiuDom != BigDecimal.ZERO
                            || obj.exNocSem != BigDecimal.ZERO
                            || obj.exNocDom != BigDecimal.ZERO
                            || obj.recNocDom != BigDecimal.ZERO
                            || obj.recNocSem != BigDecimal.ZERO
                            || obj.recDiuDom != BigDecimal.ZERO) {                       
                        obj.insert(obj, conn);
                    }
                }
            }
        }
    }

    public static void setTimes(PerWorkday w, List<Date> holidays, PerCfg cfg, Connection conn) throws Exception {
        w.exDiuSem = BigDecimal.ZERO;
        w.exDiuDom = BigDecimal.ZERO;
        w.exNocSem = BigDecimal.ZERO;
        w.exNocDom = BigDecimal.ZERO;
        w.recNocDom = BigDecimal.ZERO;
        w.recNocSem = BigDecimal.ZERO;
        w.recDiuDom = BigDecimal.ZERO;

        Date today = w.evDate;
        Date tomorrow = getTomorrow(today);

        boolean todHoli = isSunday(today);
        boolean tomHoli = isSunday(tomorrow);

        if (holidays == null) {
            MySQLQuery q = new MySQLQuery("SELECT count(*) > 0 FROM per_holiday WHERE holi_date = ?1");
            q.setParam(1, today);

            MySQLQuery q2 = new MySQLQuery("SELECT count(*) > 0 FROM per_holiday WHERE holi_date = ?1");
            q2.setParam(1, tomorrow);

            todHoli = todHoli || q.getAsBoolean(conn);
            tomHoli = tomHoli || q2.getAsBoolean(conn);

        } else {
            todHoli = todHoli || holidays.contains(today);
            tomHoli = tomHoli || holidays.contains(tomorrow);
        }

        long baseToday = getYMDms(today);
        long baseTomorrow = getYMDms(tomorrow);

        Span real = new Span(baseToday + getHMSms(w.begTime), (getHMSms(w.begTime) >= getHMSms(w.endTime) ? baseTomorrow : baseToday) + getHMSms(w.endTime));
        Span exp = new Span(real.beg, real.beg + getHms(8 + w.hourLoss));

        Span prePaid = real.inte(exp);

        Span ex1 = new Span(real.beg, exp.beg);
        Span ex2 = new Span(exp.end, real.end);

        setDay(w, prePaid, ex1, ex2, new DaySpans(today, cfg), todHoli);
        setDay(w, prePaid, ex1, ex2, new DaySpans(tomorrow, cfg), tomHoli);

        if (w.recDiuDom.compareTo(BigDecimal.ZERO) != 0) {
            w.recDiuDom = w.recDiuDom.subtract(new BigDecimal(w.hourLoss));
        }
    }

    private static void setDay(PerWorkday w, Span prePaid, Span ex1, Span ex2, DaySpans day, boolean holi) {
        w.exDiuSem = add(w.exDiuSem, day.diu.commHrs(!holi, ex1, ex2));
        w.exDiuDom = add(w.exDiuDom, day.diu.commHrs(holi, ex1, ex2));
        w.exNocSem = add(w.exNocSem, day.noc1.commHrs(!holi, ex1, ex2), day.noc2.commHrs(!holi, ex1, ex2));
        w.exNocDom = add(w.exNocDom, day.noc1.commHrs(holi, ex1, ex2), day.noc2.commHrs(holi, ex1, ex2));
        w.recNocDom = add(w.recNocDom, prePaid.commHrs(holi, day.noc1, day.noc2));
        w.recNocSem = add(w.recNocSem, prePaid.commHrs(!holi, day.noc1, day.noc2));
        w.recDiuDom = add(w.recDiuDom, prePaid.commHrs(holi, day.diu));
    }

    private static long getHMSms(Date dt) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(dt);
        return gc.getTimeInMillis() - getYMDms(dt);
    }

    private static long getYMDms(Date dt) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(Dates.trimDate(dt));
        return gc.getTimeInMillis();
    }

    private static class DaySpans {

        public Span noc1;
        public Span diu;
        public Span noc2;

        public DaySpans(Date d, PerCfg cfg) {
            long baseToday = getYMDms(d);
            noc1 = new Span(baseToday, baseToday + getHMSms(cfg.diuBegin));
            diu = new Span(baseToday + getHMSms(cfg.diuBegin), baseToday + getHMSms(cfg.diuEnd));
            noc2 = new Span(baseToday + getHMSms(cfg.diuEnd), baseToday + getHms(24));
        }

    }

    private static Date getTomorrow(Date dt) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(dt);
        gc.add(GregorianCalendar.DAY_OF_MONTH, 1);
        return gc.getTime();
    }

    private static Date getYesterday(Date dt) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(dt);
        gc.add(GregorianCalendar.DAY_OF_MONTH, -1);
        return gc.getTime();
    }

    private static BigDecimal add(Object... args) {
        BigDecimal r = BigDecimal.ZERO;
        for (Object arg : args) {
            if (arg != null) {
                r = r.add((BigDecimal) arg);
            }
        }
        return r;
    }

    private static class Span {

        public long beg;
        public long end;

        public Span(long beg, long end) {
            this.beg = beg;
            this.end = end;
        }

        public Span inte(Span s) {
            return new Span(Math.max(s.beg, beg), Math.min(s.end, end));
        }

        private BigDecimal hrs() {
            return new BigDecimal((end > beg ? (end - beg) / 1000d / 60d / 60d : 0));
        }

        public BigDecimal commHrs(boolean go, Object... args) {
            BigDecimal r = BigDecimal.ZERO;
            if (go) {
                for (Object arg : args) {
                    r = r.add(this.inte((Span) arg).hrs());
                }
            }
            return r;
        }

        @Override
        public String toString() {
            return beg + " - " + end;
        }
    }

    private static long getHms(int hrs) {
        return 1000 * 60 * 60 * hrs;
    }

    private static boolean isSunday(Date dt) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(dt);
        return gc.get(GregorianCalendar.DAY_OF_WEEK) == GregorianCalendar.SUNDAY;
    }
}
