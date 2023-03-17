package utilities.logs;

import java.awt.Window;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Table;
import api.MySQLCol;
import utilities.MySQLQuery;
import utilities.SysTask;
import utilities.mysqlReport.MySQLReport;
import utilities.mysqlReport.MySQLReportWriter;
import utilities.reportHelper.CellFormat;

public class LogUtils {

    public static final String LINE_BREAK = "\\n";

    private static Comparable isNull(Comparable c) {
        if (c instanceof String) {
            return ((String) c).isEmpty() ? null : c;
        }
        return c;
    }

    public static void getLogLine(StringBuilder sb, String varName, Comparable val, int type) throws Exception {
        val = isNull(val);
        if (val != null) {
            sb.append(LogUtils.LINE_BREAK).append(varName).append(": ").append(LogUtils.toString(val, type));
        }
    }

    /**
     *
     * @param ep
     * @param sb
     * @param varName
     * @param oldVal
     * @param newVal
     * @param labelQuery
     * @return String de un query deseado si hay cambios
     * @throws Exception
     */
    @SuppressWarnings("null")
    public static int getLogLine(Connection ep, StringBuilder sb, String varName, Comparable oldVal, Comparable newVal, String labelQuery) throws Exception {
        oldVal = isNull(oldVal);
        newVal = isNull(newVal);
        boolean isOldVal = (oldVal != null);
        boolean isNewVal = (newVal != null);
        varName = varName.replaceAll("[\\.]", "").trim();

        if (isOldVal && isNewVal) {
            if (oldVal.compareTo(newVal) != 0) {
                sb.append(LINE_BREAK).append(varName).append(": Se cambió ");
                sb.append(getString(ep, labelQuery, oldVal));
                return 1;
            }
        } else if (!isOldVal && isNewVal) {
            sb.append(LINE_BREAK).append(varName).append(": Se adicionó.");
            return 1;
        } else if (isOldVal && !isNewVal) {
            sb.append(LINE_BREAK).append(varName).append(": Se removió ");
            sb.append(getString(ep, labelQuery, oldVal));
            return 1;
        }
        return 0;
    }

    /**
     * Busca diferencias entre 2 objectos comparables, teniendo en cuenta que
     * pueden ser nulos.
     *
     * @param val1
     * @param val2
     * @return verdadero si ninguno es nulo y son diferentes, o si uno es nulo y
     * el otro no.
     */
    @SuppressWarnings("null")
    public static boolean different(Comparable val1, Comparable val2) {

        val1 = isNull(val1);
        val2 = isNull(val2);
        boolean isOldVal = (val1 != null);
        boolean isNewVal = (val2 != null);

        if (isOldVal && isNewVal) {
            if (val1.compareTo(val2) != 0) {
                return true;
            }
        } else if (!isOldVal && isNewVal) {
            return true;
        } else if (isOldVal && !isNewVal) {
            return true;
        }
        return false;
    }

    /**
     *
     * @param sb
     * @param type
     * @param varName
     * @param oldVal
     * @param newVal
     * @return Dato viejo si hay cambios
     * @throws Exception
     */
    @SuppressWarnings("null")
    public static int getLogLine(StringBuilder sb, int type, String varName, Comparable oldVal, Comparable newVal) throws Exception {
        varName = varName.replaceAll("[\\.]", "").trim();

        oldVal = isNull(oldVal);
        newVal = isNull(newVal);

        if (!Objects.equals(oldVal, newVal)) {
            boolean isOldVal = (oldVal != null);
            boolean isNewVal = (newVal != null);

            if (isOldVal && isNewVal) {
                if (oldVal.compareTo(newVal) != 0) {
                    sb.append(LINE_BREAK).append(varName).append(": Se cambió ").append(toString(oldVal, type));
                    return 1;
                }
            } else if (!isOldVal && isNewVal) {
                sb.append(LINE_BREAK).append(varName).append(": Se adicionó.");
                return 1;
            } else if (isOldVal && !isNewVal) {
                sb.append(LINE_BREAK).append(varName).append(": Se removió ").append(toString(oldVal, type));
                return 1;
            }
        }
        return 0;
    }

    /**
     *
     * @param sb
     * @param varName
     * @param oldVal
     * @param newVal
     * @param enumReturn
     * @return Conpara dos valores y retorna el valor del ultimo parametro que
     * corresponde al texto del valor viejo
     * @throws Exception
     */
    @SuppressWarnings("null")
    public static int getLogLine(StringBuilder sb, String varName, Comparable oldVal, Comparable newVal, String enumReturn) throws Exception {
        oldVal = isNull(oldVal);
        newVal = isNull(newVal);
        boolean isOldVal = (oldVal != null);
        boolean isNewVal = (newVal != null);
        varName = varName.replaceAll("[\\.]", "").trim();
        if (isOldVal && isNewVal) {
            if (oldVal.compareTo(newVal) != 0) {
                sb.append(LINE_BREAK).append(varName).append(": Se cambió ").append(enumReturn);
                return 1;
            }
        } else if (!isOldVal && isNewVal) {
            sb.append(LINE_BREAK).append(varName).append(": Se adicionó.");
            return 1;
        } else if (isOldVal && !isNewVal) {
            sb.append(LINE_BREAK).append(varName).append(": Se removió ").append(enumReturn);
            return 1;
        }
        return 0;
    }

    /**
     *
     * @param sb
     * @param varName
     * @param oldVal
     * @param newVal
     * @param enumOpts
     * @return Conpara dos valores y retorna el valor del ultimo parametro que
     * corresponde al texto del valor viejo
     * @throws Exception
     */
    @SuppressWarnings("null")
    public static int getLogLine(StringBuilder sb, String varName, Comparable oldVal, Comparable newVal, Map<String, String> enumOpts) throws Exception {
        oldVal = isNull(oldVal);
        newVal = isNull(newVal);
        boolean isOldVal = (oldVal != null);
        boolean isNewVal = (newVal != null);
        varName = varName.replaceAll("[\\.]", "").trim();
        if (isOldVal && isNewVal) {
            if (oldVal.compareTo(newVal) != 0) {
                sb.append(LINE_BREAK).append(varName).append(": Se cambió ").append(enumOpts.get(oldVal.toString()));
                return 1;
            }
        } else if (!isOldVal && isNewVal) {
            sb.append(LINE_BREAK).append(varName).append(": Se adicionó.");
            return 1;
        } else if (isOldVal && !isNewVal) {
            sb.append(LINE_BREAK).append(varName).append(": Se removió ").append(enumOpts.get(oldVal.toString()));
            return 1;
        }
        return 0;
    }

    public static String toString(Object val, int type) throws Exception {
        if (MySQLCol.isDate(type)) {
            return new SimpleDateFormat(MySQLCol.getFormat(type)).format(MySQLQuery.getAsDate(val));
        } else if (MySQLCol.isTime(type)) {
            Date date;
            if (val instanceof Integer) {
                int minutes = (Integer) val;
                GregorianCalendar gc = new GregorianCalendar();
                gc.set(1990, 01, 01, (int) Math.floor(minutes / 60), minutes % 60);
                date = gc.getTime();
            } else {
                date = MySQLQuery.getAsDate(val);
            }
            return new SimpleDateFormat(MySQLCol.getFormat(type)).format(date);
        } else if (MySQLCol.isDecimal(type) || MySQLCol.isInteger(type)) {
            return new DecimalFormat(MySQLCol.getFormat(type)).format(MySQLQuery.getAsBigDecimal(val, true));
        } else if (MySQLCol.TYPE_TEXT == type) {
            return ": " + val.toString();
        } else if (MySQLCol.TYPE_BOOLEAN == type) {
            return (MySQLQuery.getAsBoolean(val) ? "Si" : "No");
        } else if (MySQLCol.TYPE_ENUM == type || MySQLCol.TYPE_ICON == type || MySQLCol.TYPE_COLOR == type || MySQLCol.TYPE_COLOR_ICON == type) {
            throw new Exception("Unsupported: " + type);
        } else {
            throw new Exception("Unrecognized: " + type);
        }
    }

    private static String getString(Connection ep, String labelQuery, Object oldId) throws Exception {
        return new MySQLQuery(labelQuery.replaceAll("\\?", oldId.toString())).getAsString(ep);
    }
}
