package utilities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class Dates {

    public static int getMonths(Date from, Date to) {
        GregorianCalendar g = new GregorianCalendar();
        g.setTime(from);
        int y1 = g.get(GregorianCalendar.YEAR);
        int m1 = g.get(GregorianCalendar.MONTH) + 1;
        g.setTime(to);
        int y2 = g.get(GregorianCalendar.YEAR);
        int m2 = g.get(GregorianCalendar.MONTH) + 1;
        return ((y2 * 12) + m2) - ((y1 * 12) + m1);
    }
    
    //meses en un rango
    public static Date[][] getDateList(int begYear, int endYear, int begMonth, int endMonth) throws Exception {
        if (begYear > endYear) {
            throw new Exception("Verifique la coherencia de los años suministrados.");
        } else if (begYear == endYear) {
            if (endMonth < begMonth) {
                throw new Exception("Verifique la coherencia de los años suministrados.");
            }
        }

        ArrayList<Date[]> dates = new ArrayList<Date[]>();
        for (int y = begYear; y <= endYear; y++) {
            int bMonth = 0;
            int eMonth = 11;
            if (begYear == endYear) {
                bMonth = begMonth;
                eMonth = endMonth;
            } else if (y == begYear) {
                bMonth = begMonth;
                eMonth = 11;
            } else if (y == endYear) {
                bMonth = 0;
                eMonth = endMonth;
            }

            for (int m = bMonth; m <= eMonth; m++) {
                GregorianCalendar gBeg = new GregorianCalendar(y, m, 1);
                GregorianCalendar gEnd = new GregorianCalendar(y, m, gBeg.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
                Date[] mDates = new Date[2];
                mDates[0] = trimDate(gBeg.getTime());
                mDates[1] = trimDate(gEnd.getTime());
                dates.add(mDates);
            }
        }

        if (dates.isEmpty()) {
            throw new Exception("Verifique la coherencia de las fechas suministradas.");
        }
        return dates.toArray(new Date[0][0]);

    }

    /**
     * Compara dos fechas cuando las horas se guardan por separado
     *
     * @param d1 Componente dia de la fecha 1
     * @param t1 Componente hora de la fecha 2
     * @param d2 Componente dia de la fecha 2
     * @param t2 Componente hora de la fecha 2
     * @return
     */
    public static int compare(Date d1, Date t1, Date d2, Date t2) {
        long ms1 = Dates.trimDate(d1).getTime() + (t1.getTime() % 86400000);
        long ms2 = Dates.trimDate(d2).getTime() + (t2.getTime() % 86400000);

        if (ms1 == ms2) {
            return 0;
        } else if (ms1 < ms2) {
            return -1;
        } else {
            return 1;
        }
    }

    //mes actual, mes anterior, mismo mes año anterior
    public static Date[][] getDateList(Date date) {
        //calculos con fechas
        GregorianCalendar curEndGc = new GregorianCalendar();
        curEndGc.setTime(date);
        GregorianCalendar curBegGc = new GregorianCalendar();
        curBegGc.setTime(date);
        curBegGc.set(GregorianCalendar.DAY_OF_MONTH, 1);

        int year = curEndGc.get(GregorianCalendar.YEAR);
        int month = curEndGc.get(GregorianCalendar.MONTH);

        GregorianCalendar lmBegGc = new GregorianCalendar(month > 0 ? year : year - 1, month > 0 ? month - 1 : 11, 1);
        GregorianCalendar lmEndGc = new GregorianCalendar(month > 0 ? year : year - 1, month > 0 ? month - 1 : 11, lmBegGc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));

        GregorianCalendar lyBegGc = new GregorianCalendar(year - 1, month, 1);
        GregorianCalendar lyEndGc = new GregorianCalendar(year - 1, month, lyBegGc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));

        Date[][] dates = new Date[3][];
        dates[0] = new Date[2];
        dates[0][0] = Dates.trimDate(curBegGc.getTime());//curBeg
        dates[0][1] = Dates.trimDate(curEndGc.getTime());//curEnd
        dates[1] = new Date[2];
        dates[1][0] = Dates.trimDate(lmBegGc.getTime());//lmBegGc
        dates[1][1] = Dates.trimDate(lmEndGc.getTime());//lmEndGc
        dates[2] = new Date[2];
        dates[2][0] = Dates.trimDate(lyBegGc.getTime());//lyBegGc
        dates[2][1] = Dates.trimDate(lyEndGc.getTime());//lyEndGc
        return dates;
    }

    public static int getDayOfMonth(Date date) {
        GregorianCalendar c1 = new GregorianCalendar();
        c1.setTime(date);
        return c1.get(GregorianCalendar.DAY_OF_MONTH);
    }

    public static int getMaxDayOfMonth(Date date) {
        GregorianCalendar c1 = new GregorianCalendar();
        c1.setTime(date);
        return c1.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
    }

    public static Date trimDate(Date date) {
        GregorianCalendar c1 = new GregorianCalendar();
        c1.setTime(date);
        GregorianCalendar c2 = new GregorianCalendar();
        c2.set(c1.get(GregorianCalendar.YEAR), c1.get(GregorianCalendar.MONTH), c1.get(GregorianCalendar.DAY_OF_MONTH), 0, 0, 0);
        c2.set(GregorianCalendar.MILLISECOND, 0);
        return c2.getTime();
    }

    //recorta la hora, deja solo horas y minutos, redondea los segundos, año mes y dia quedan en 1970-01-01
    public static Date trimToMinute(Date dt) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(dt);
        gc.set(GregorianCalendar.YEAR, 1970);
        gc.set(GregorianCalendar.MONTH, 0);
        gc.set(GregorianCalendar.DAY_OF_MONTH, 1);
        gc.set(GregorianCalendar.MILLISECOND, 0);
        if (gc.get(GregorianCalendar.SECOND) > 30 || (gc.get(GregorianCalendar.SECOND) == 30 && gc.get(GregorianCalendar.MINUTE) % 2 == 0)) {
            gc.add(GregorianCalendar.MINUTE, 1);
        }
        gc.set(GregorianCalendar.SECOND, 0);
        return gc.getTime();
    }

    //devuelve la fecha con horas minutos y segundos minimos (ceros) : 00:00:00
    public static Date getMinHours(Date dt) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(dt);
        gc.set(GregorianCalendar.HOUR, 0);
        gc.add(GregorianCalendar.MINUTE, 0);
        gc.set(GregorianCalendar.SECOND, 0);
        return gc.getTime();
    }

    //devuelve la fecha con horas minutos y segundos maximos (23) : 23:59:59
    public static Date getMaxHours(Date dt) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(dt);
        gc.set(GregorianCalendar.HOUR, 23);
        gc.add(GregorianCalendar.MINUTE, 59);
        gc.set(GregorianCalendar.SECOND, 59);
        return gc.getTime();
    }

    //para los formularios
    public static SimpleDateFormat getDefaultFormat() {
        return new SimpleDateFormat("dd/MM/yyyy");
    }

    //nombrar archivos
    public static SimpleDateFormat getFilesFormat() {
        return new SimpleDateFormat("yyyy-MM-dd [H-m-s]");
    }

    //para usar dentro de excel
    public static SimpleDateFormat getExcelFormat() {
        return new SimpleDateFormat("yyyy-MM-dd");
    }

    //para nombrar los backups
    public static SimpleDateFormat getBackupFormat() {
        return new SimpleDateFormat("yyyy-MM-dd_H-m-s");
    }

    /**
     * para fechas de MySQL yyyy-MM-dd HH:mm:ss 2015-03-25 15:48:30
     *
     * @return
     */
    public static SimpleDateFormat getSQLDateTimeFormat() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    //para usar dentro de excel
    public static SimpleDateFormat getSQLDateFormat() {
        return new SimpleDateFormat("yyyy-MM-dd");
    }

    //para corrovorar con el usuario
    public static SimpleDateFormat getCheckFormat() {
        Locale es = new Locale("es", "ES");
        return new SimpleDateFormat("EEEE dd 'de' MMMM 'de' yyyy", es);
    }

    //para facturación
    public static SimpleDateFormat getShortFormat() {
        return new SimpleDateFormat("d MMMM yyyy");
    }

    public static SimpleDateFormat getSQLTimeFormat() {
        return new SimpleDateFormat("HH:mm:ss");
    }

    /**
     * para facturación dd/MM/yyyy hh:mm aa por ejemplo 25/Marzo/2015 08:44 AM
     *
     * @return
     */
    public static SimpleDateFormat getDateTimeFormat() {
        return new SimpleDateFormat("dd/MM/yyyy hh:mm aa");
    }

    public static SimpleDateFormat getHourFormat() {
        return new SimpleDateFormat("hh:mm aa");
    }

    public static XMLGregorianCalendar Date2XMLDate(Date d) throws DatatypeConfigurationException {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(d);
        XMLGregorianCalendar x = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        return x;
    }

    public static Date XMLDateToDate(XMLGregorianCalendar d) throws DatatypeConfigurationException {
        if (d == null) {
            return null;
        }
        return d.toGregorianCalendar().getTime();
    }

    public static void validateOrder(Date begDate, Date endDate) throws Exception {
        if (begDate.compareTo(endDate) > 0) {
            throw new Exception("La fecha inicial debe ser menor o igual a la final.");
        }
    }

    /**
     * Retorna yyyy-MM-dd 00:00:00 Y yyyy-MM-dd 23:59:59 para su uso en queries
     * con between
     *
     * @param beginDate
     * @param endDate
     * @return
     */
    public static Date[] getForBetween(Date beginDate, Date endDate) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(beginDate);
        gc.set(GregorianCalendar.HOUR, 0);
        gc.set(GregorianCalendar.MINUTE, 0);
        gc.set(GregorianCalendar.SECOND, 0);
        gc.set(GregorianCalendar.MILLISECOND, 0);
        Date d1 = gc.getTime();

        gc.setTime(endDate);
        gc.set(GregorianCalendar.HOUR, 23);
        gc.set(GregorianCalendar.MINUTE, 59);
        gc.set(GregorianCalendar.SECOND, 59);
        gc.set(GregorianCalendar.MILLISECOND, 0);
        Date d2 = gc.getTime();
        return new Date[]{d1, d2};
    }

    public static int curWeekOfYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.setMinimalDaysInFirstWeek(2);
        calendar.setTime(new Date());
        return calendar.get(Calendar.WEEK_OF_YEAR);
    }

    public static int weekMonth(int curWeek) {
        int month = 0;
        for (int i = 0; i <= 13; i++) {
            for (int y = 1; y < 5; y++) {
                month = month + 1;
                if (month == curWeek) {
                    return y;
                }
            }
        }
        return 0;
    }

    public static Date sumDaysDate(Date date, int days) {
        if (days == 0) {
            return date;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTime();
    }

    public static Date[] getDatesBegEnd(Date date) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(date);
        gc.set(GregorianCalendar.DAY_OF_MONTH, 1);
        Date beg = gc.getTime();
        gc.set(GregorianCalendar.DAY_OF_MONTH, gc.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));
        Date end = gc.getTime();
        return new Date[]{beg, end};
    }

    public static Date[] getDatesBetween(int begYear, int begMonth, int begDay, int endYear, int endMonth, int endDay) {
        return getDatesBetween(createDate(begYear, begMonth, begDay), createDate(endYear, endMonth, endDay));
    }

    public static Date[] getDatesBetween(Date begDate, Date endDate) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(begDate);
        List<Date> dates = new ArrayList<>();
        dates.add(begDate);
        Date curDate = begDate;
        while (!curDate.equals(endDate)) {
            gc.setTime(curDate);
            gc.add(GregorianCalendar.DAY_OF_MONTH, 1);
            curDate = gc.getTime();
            dates.add(curDate);
        }
        return dates.toArray(new Date[dates.size()]);
    }

    public static Date createDate(int year, int month, int day) {
        return Dates.trimDate(new GregorianCalendar(year, month, day).getTime());
    }

    public static long compareMinutes(Date d1, Date d2) {
        long diff = d2.getTime() - d1.getTime();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);   
        return minutes;
    }

}
