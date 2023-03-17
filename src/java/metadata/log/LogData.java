package metadata.log;

import api.MySQLCol;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Objects;
import static metadata.log.Diff.getTableName;
import metadata.model.Field;
import metadata.model.Table;
import utilities.MySQLQuery;

public class LogData {

    public List<LogField> flds = new ArrayList<>();

    private static Object standarizeEmptyStr(Object s) {
        if (s != null) {
            return s.toString().trim().equals("") ? null : s;
        } else {
            return null;
        }
    }

    public static LogData getFromObjs(Object old, Object updated) throws Exception {
        LogData rta = new LogData();
        String tblName = getTableName(old);
        Table tbl = Table.getByName(tblName);
        if (tbl == null) {
            throw new Exception("No hay metadatos de la tabla '" + tblName + "'");
        }
        java.lang.reflect.Field[] fields = old.getClass().getFields();
        for (java.lang.reflect.Field fld : fields) {
            Object oldV = fld.get(old);
            Object updatedV = fld.get(updated);
            boolean equals;
            if (fld.getType().equals(java.util.Date.class)) {
                equals = false;
                if (oldV != null && updatedV != null) {
                    equals = ((java.util.Date) oldV).getTime() == ((java.util.Date) updatedV).getTime();
                } else if (oldV == null && updatedV == null) {
                    equals = true;
                }
            } else if (fld.getType().equals(String.class)) {
                equals = Objects.deepEquals(standarizeEmptyStr(oldV), standarizeEmptyStr(updatedV));
            } else {
                equals = Objects.deepEquals(oldV, updatedV);
            }

            if (!equals) {
                Field f = tbl.getFieldByName(fld.getName());
                if (f == null) {
                    throw new Exception("No hay datos del campo '" + fld.getName() + "' en " + tbl.name);
                }
                LogField lFld = new LogField();
                rta.flds.add(lFld);
                lFld.fieldId = f.id;
                lFld.fieldName = f.name;

                if (oldV == null && updatedV != null) {
                    lFld.operation = LogField.ADDED;
                } else if (oldV != null && updatedV == null) {
                    lFld.operation = LogField.REMOVED;
                } else {
                    lFld.operation = LogField.CHANGED;
                }

                if (oldV != null) {
                    lFld.oldValue = oldV;
                }
            }
        }
        return rta;
    }

    public String getAsString(String tblName, Connection conn) throws Exception {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < flds.size(); i++) {
            LogField lf = flds.get(i);
            Object oldV = lf.oldValue;
            Field f = Field.getById(lf.fieldId);
            if (f != null) {
                switch (lf.operation) {
                    case LogField.ADDED:
                        sb.append("Se agregó ");
                        break;
                    case LogField.REMOVED:
                        sb.append("Se removió ");
                        break;
                    case LogField.CHANGED:
                        sb.append("Se cambió ");
                        break;
                    default:
                        throw new RuntimeException();
                }
                if (f.label != null) {
                    sb.append("el campo ");
                    sb.append(f.label.toLowerCase());
                } else {
                    sb.append(f.name);
                }

                if (oldV != null) {
                    sb.append(", era ");
                    if (f.fk) {
                        Descriptor.getDescription(f.fkTblName, MySQLQuery.getAsInteger(oldV), conn);
                    } else {
                        sb.append(toString(oldV, f));
                    }
                }
                sb.append(". ");
            }
        }
        return sb.toString();
    }

    private static String toString(Object val, Field fld) throws Exception {
        String format = null;
        int type = MySQLCol.getConstFromStr(fld.format);

        if (MySQLCol.hasFormat(type)) {
            format = MySQLCol.getFormat(type);
        }
        if (MySQLCol.isDate(type)) {
            return new SimpleDateFormat(format).format(MySQLQuery.getAsDate(val));
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
            return new SimpleDateFormat(format).format(date);
        } else if (MySQLCol.isDecimal(type) || MySQLCol.isInteger(type)) {
            return new DecimalFormat(format).format(MySQLQuery.getAsBigDecimal(val, true));
        } else if (MySQLCol.TYPE_TEXT == type) {
            return val.toString();
        } else if (MySQLCol.TYPE_BOOLEAN == type) {
            return (MySQLQuery.getAsBoolean(val) ? "Si" : "No");
        } else if (MySQLCol.TYPE_ENUM == type || MySQLCol.TYPE_ICON == type || MySQLCol.TYPE_COLOR == type || MySQLCol.TYPE_COLOR_ICON == type) {
            throw new Exception("Unsupported: " + type);
        } else {
            throw new Exception("Unrecognized: " + type);
        }
    }
}
