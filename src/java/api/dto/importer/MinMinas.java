package api.dto.importer;

import api.dto.model.DtoCylPrice;
import api.dto.model.DtoSalesman;
import api.trk.model.CylinderType;
import api.trk.model.TrkCyl;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import utilities.MySQLQuery;

public class MinMinas {

    public static final int STATE_OK = 1;
    public static final int STATE_WARN = 2;
    public static final int STATE_ERROR = 3;

    public static Integer noPaymentCausalId = null;

    public int origRowIndex;
    public Date dt;
    public long clieDoc;
    public Integer stratum;
    public Integer subsidy;
    public String nif;
    public String salesmanDoc;
    public String importNotes;
    public Integer aprovNumber;
    public BigInteger bill;
    public String notes;
    public Integer origCapa;
    public Integer valueTotal;
    public DtoCylPrice cylPrice;
    public DtoSalesman salesman;
    public String noPaymentNotes;
    public String anulNotes;
    public BigDecimal lat;
    public BigDecimal lon;
    public TrkCyl cyl;
    public CylinderType cylType;
    public String mun;
    public String depto;    

    public int state = STATE_OK;
    public boolean payment = true;

    private static final SimpleDateFormat DTF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private Integer getNoPaymentCausalId(Connection ep) throws Exception {
        if (noPaymentCausalId == null) {
            noPaymentCausalId = new MySQLQuery("SELECT id FROM dto_causal WHERE used_import = 1").getAsInteger(ep);
        }
        return noPaymentCausalId;
    }

    public String getInsert(int logId, Connection ep) throws Exception {
        StringBuilder sb = new StringBuilder("");
        sb.append("INSERT INTO dto_sale SET ");
        sb.append("import_id = ").append(logId).append(", ");
        sb.append("dt = '").append(DTF.format(dt)).append("', ");
        sb.append("clie_doc = ").append(clieDoc).append(", ");
        sb.append("stratum = ").append(stratum).append(", ");
        sb.append("value_total = ").append(valueTotal).append(", ");
        sb.append("subsidy = ").append(subsidy).append(", ");
        sb.append("aprov_number = ").append(aprovNumber).append(", ");
        sb.append("bill = ").append(bill).append(", ");
        sb.append("nif = '").append(nif != null ? nif : "").append("', ");
        if (cyl != null) {
            sb.append("cyl_type_id = ").append(cyl.cylTypeId).append(", ");
        }

        if (salesman != null) {
            sb.append("salesman_id = ").append(salesman.id).append(", ");
            sb.append("center_id = ").append(salesman.centerId).append(", ");

        }
        sb.append("import_notes = '").append(importNotes).append("', ");

        if (!payment) {
            sb.append("causal_id = ").append(getNoPaymentCausalId(ep)).append(", ");
            sb.append("cau_notes = '").append(noPaymentNotes).append("', ");
        }

        switch (state) {
            case STATE_OK:
                sb.append("state = 'ok'");
                break;
            case STATE_ERROR:
                sb.append("state = 'error'");
                break;
            case STATE_WARN:
                sb.append("state = 'warn'");
                break;
            default:
                break;
        }
        return sb.toString();
    }

    public static Date forgeDate(Object date, Object hour) throws Exception {
        Date d = null;
        Date h = null;

        if (date == null) {
            throw new Exception("Debe indicar una fecha.");
        } else if (date instanceof Date) {
            d = (Date) date;
        } else if (date instanceof String) {
            int l = date.toString().length();
            switch (l) {
                case 10:
                    d = new SimpleDateFormat("yyyy-MM-dd").parse(date.toString());
                    break;
                case 19:
                    d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date.toString());
                    break;
                default:
                    throw new Exception("No se reconoce el formato de fecha.");
            }
        }

        if (hour != null && hour instanceof Date) {
            h = (Date) hour;
        } else if (hour != null && hour instanceof String) {
            int l = hour.toString().length();
            switch (l) {
                case 8:
                    h = new SimpleDateFormat("HH:mm:ss").parse(hour.toString());
                    break;
                case 19:
                    h = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(hour.toString());
                    break;
                default:
                    throw new Exception("No se reconoce el formato de hora en " + hour);
            }
        }

        if (h == null) {
            return d;
        } else {
            GregorianCalendar gcd = new GregorianCalendar();
            gcd.setTime(d);
            GregorianCalendar gch = new GregorianCalendar();
            gch.setTime(h);

            int y = gcd.get(GregorianCalendar.YEAR);
            int mon = gcd.get(GregorianCalendar.MONTH);
            int day = gcd.get(GregorianCalendar.DAY_OF_MONTH);

            int hr = gch.get(GregorianCalendar.HOUR_OF_DAY);
            int mn = gch.get(GregorianCalendar.MINUTE);
            int sec = gch.get(GregorianCalendar.SECOND);

            GregorianCalendar gcf = new GregorianCalendar(y, mon, day, hr, mn, sec);
            return gcf.getTime();
        }
    }

}
