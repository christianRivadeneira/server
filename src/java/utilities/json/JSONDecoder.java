package utilities.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class JSONDecoder {

    private BufferedReader is;
    private char cur;

    public JSONDecoder() throws Exception {

    }

    public <T> T getObject(InputStream is, String charset, Class<T> cs) throws Exception {
        if (charset != null) {
            this.is = new BufferedReader(new InputStreamReader(is, charset));
        } else {
            this.is = new BufferedReader(new InputStreamReader(is));
        }

        next();
        return readObject(cs);
    }

    public <T> T getObject(InputStream is, Class<T> cs) throws Exception {
        return getObject(is, null, cs);
    }

    public <T> List<T> getList(InputStream is, String charset, Class<T> cs) throws Exception {
        if (charset != null) {
            this.is = new BufferedReader(new InputStreamReader(is, charset));
        } else {
            this.is = new BufferedReader(new InputStreamReader(is));
        }
        next();
        return readList(cs);
    }

    public <T> List<T> getList(InputStream is, Class<T> cs) throws Exception {
        return getList(is, null, cs);
    }

    public <T> T[] toArray(List<T> lst, Class<T> c) {
        T[] rta = (T[]) Array.newInstance(c, lst.size());
        return lst.toArray(rta);
    }

    public <T> List<T> readList(Class<T> c) throws Exception {
        List<T> res = new ArrayList<>();
        if (cur != '[') {
            throw new UnexpectedException('[', cur);
        }
        next();
        readWhiteSpace();
        if (cur == ']') {
            next();
            return res;
        } else {
            while (true) {
                res.add((T) readValue(c));
                switch (cur) {
                    case ',':
                        next();
                        break;
                    case ']':
                        next();
                        return res;
                    default:
                        throw new Exception(", or ] was expected but " + cur + " was found");
                }
            }
        }
    }

    private char next() throws IOException {
        cur = (char) is.read();
        return cur;
    }

    private void readWhiteSpace() throws IOException {
        char c = cur;
        if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
            next();
            readWhiteSpace();
        }
    }

    private <T> T readObject(Class<T> cs) throws Exception {
        T obj = cs.newInstance();
        char c = cur;
        readWhiteSpace();
        if (c == '{') {
            next();
            while (true) {
                readWhiteSpace();
                if (cur == '}') {
                    next();
                    return obj;
                } else {
                    String key = readString();
                    readWhiteSpace();
                    if (cur != ':') {
                        throw new Exception(": was expected");
                    } else {
                        next();
                        try {
                            Field fld = cs.getField(key);
                            fld.set(obj, readValue(fld.getGenericType()));
                        } catch (NoSuchFieldException ex) {
                            Logger.getLogger(JSONDecoder.class.getName()).log(Level.SEVERE, "La clase {0} no tiene un campo llamado ''{1}''", new Object[]{cs.toString(), key});
                            ignoreValue();
                        }
                        if (cur == ',') {
                            next();
                        }
                    }
                }
            }
        } else {
            throw new UnexpectedException('{', cur);
        }
    }

    private Object readValue(Type type) throws Exception {
        readWhiteSpace();
        Object o = null;
        char c = cur;
        if (c == '"') {
            if (type.equals(Date.class)) {
                o = decodeDate(readString());
            } else if (type.equals(BigDecimal.class)) {
                o = new BigDecimal(readString());
            } else if (type.equals(String.class)) {
                o = readString();
            } else {
                String s = readString();
                if (isDate(s)) {
                    o = decodeDate(s);
                } else {
                    o = s;
                }
            }
        } else if ((48 <= c && c <= 57) || c == '-') {
            o = readNumber((Class) type);
        } else if (c == '{') {
            o = readObject((Class) type);
        } else if (c == '[') {
            if ((type instanceof Class && ((Class) type).isArray())) {
                Class et = ((Class) type).getComponentType();
                List l = readList(et);
                o = toArray(l, et);
            } else if (type instanceof ParameterizedType) {
                Class t = (Class) ((ParameterizedType) type).getActualTypeArguments()[0];
                o = readList(t);
            } else {
                o = readList(Object.class);
            }
        } else if (c == 't') {
            if (cur == 't' && next() == 'r' && next() == 'u' && next() == 'e') {
                o = true;
                next();
            } else {
                throw new Exception("'true' was expected, '" + cur + "' was found.");
            }
        } else if (c == 'f') {
            if (cur == 'f' && next() == 'a' && next() == 'l' && next() == 's' && next() == 'e') {
                o = false;
                next();
            } else {
                throw new Exception("'false' was expected, '" + cur + "' was found.");
            }
        } else if (c == 'n') {
            if (cur == 'n' && next() == 'u' && next() == 'l' && next() == 'l') {
                o = null;
                next();
            } else {
                throw new Exception("'null' was expected, '" + cur + "' was found.");
            }
        } else {
            throw new Exception("'" + c + "' was not expected");
        }
        readWhiteSpace();
        return o;
    }

    private void ignoreObject() throws Exception {
        char c = cur;
        readWhiteSpace();
        if (c == '{') {
            next();
            while (true) {
                readWhiteSpace();
                if (cur == '}') {
                    next();
                    return;
                } else {
                    readString();
                    readWhiteSpace();
                    if (cur != ':') {
                        throw new Exception(": was expected");
                    } else {
                        next();
                        ignoreValue();
                        if (cur == ',') {
                            next();
                        }
                    }
                }
            }
        } else {
            throw new UnexpectedException('{', cur);
        }
    }

    private void ignoreValue() throws Exception {
        readWhiteSpace();
        char c = cur;
        if (c == '"') {
            readString();
        } else if ((48 <= c && c <= 57) || c == '-') {
            ignoreNumber();
        } else if (c == '{') {
            ignoreObject();
        } else if (c == '[') {
            ignoreList();
        } else if (c == 't') {
            if (cur == 't' && next() == 'r' && next() == 'u' && next() == 'e') {
                next();
            } else {
                throw new Exception("'true' was expected, '" + cur + "' was found.");
            }
        } else if (c == 'f') {
            if (cur == 'f' && next() == 'a' && next() == 'l' && next() == 's' && next() == 'e') {
                next();
            } else {
                throw new Exception("'false' was expected, '" + cur + "' was found.");
            }
        } else if (c == 'n') {
            if (cur == 'n' && next() == 'u' && next() == 'l' && next() == 'l') {
                next();
            } else {
                throw new Exception("'null' was expected, '" + cur + "' was found.");
            }
        } else {
            throw new Exception("'" + c + "' was not expected");
        }
        readWhiteSpace();
    }

    private void ignoreNumber() throws Exception {
        StringBuilder sb = new StringBuilder();
        readInteger(sb);
        readFraction(sb);
        readExp(sb);
    }

    public void ignoreList() throws Exception {
        if (cur != '[') {
            throw new UnexpectedException('[', cur);
        }
        next();
        readWhiteSpace();
        if (cur == ']') {
            next();
        } else {
            while (true) {
                ignoreValue();
                if (cur == ',') {
                    next();
                } else if (cur == ']') {
                    next();
                    return;
                }
            }
        }
    }

    private Object readNumber(Class cs) throws Exception {
        StringBuilder sb = new StringBuilder();
        readInteger(sb);
        boolean frac = readFraction(sb);
        boolean exp = readExp(sb);

        if (cs.equals(Object.class) || cs.equals(Number.class)) {
            if (frac && exp) {
                return new Double(sb.toString());
            } else if (!frac && exp) {
                return new Double(sb.toString());
            } else if (frac && !exp) {
                return new BigDecimal(sb.toString());
            } else if (!frac && !exp) {
                return new Integer(sb.toString());
            }
        }

        if (cs.equals(Integer.class) || cs.equals(int.class)) {
            return new Integer(sb.toString());
        }

        if (cs.equals(Double.class) || cs.equals(double.class)) {
            return new Double(sb.toString());
        }

        if (cs.equals(Float.class) || cs.equals(float.class)) {
            return new Float(sb.toString());
        }

        if (cs.equals(BigDecimal.class)) {
            return new BigDecimal(sb.toString());
        }

        if (cs.equals(Long.class) || cs.equals(long.class)) {
            return new Long(sb.toString());
        }
        throw new Exception("Unsupported class: " + cs.toString());
    }

    private void readInteger(StringBuilder sb) throws Exception {
        if (cur == '-') {
            sb.append(cur);
            next();
        }
        if (cur == '0') {
            sb.append(cur);
            next();
            if (48 <= cur && cur <= 57) {
                throw new Exception("No digit was expected after initial 0");
            }
        } else if (49 <= cur && cur <= 57) {//1-9
            sb.append(cur);
            next();
            readDigits(sb);
        } else {
            throw new Exception("A digit was expected '" + cur + "' found.");
        }
    }

    private boolean readFraction(StringBuilder sb) throws Exception {
        if (cur == '.') {
            sb.append('.');
            next();
            if (!readDigits(sb)) {
                throw new Exception("At least a digit was expected '" + cur + "' found.");
            }
            return true;
        }
        return false;
    }

    private boolean readExp(StringBuilder sb) throws Exception {
        if (cur == 'E' || cur == 'e') {
            sb.append(cur);
            next();
            if (cur == '+' || cur == '-') {
                sb.append(cur);
                next();
            }
            if (!readDigits(sb)) {
                throw new Exception("At least a digit was expected '" + cur + "' found.");
            }
            return true;
        }
        return false;
    }

    private boolean readDigits(StringBuilder sb) throws IOException {
        boolean atLeastOne = (48 <= cur && cur <= 57);
        while (48 <= cur && cur <= 57) {
            sb.append(cur);
            next();
        }
        return atLeastOne;
    }

    private String readString() throws Exception {
        char c = cur;
        if (c == '\"') {
            StringBuilder s = new StringBuilder();
            while (true) {
                c = next();
                if (c != '\"') {
                    if (c == '\\') {
                        c = next();
                        switch (c) {
                            case '"':
                                s.append('"');
                                break;
                            case '\\':
                                s.append('\\');
                                break;
                            case '/':
                                s.append('/');
                                break;
                            case 'b':
                                s.append((char) 8);
                                break;
                            case 'f':
                                s.append((char) 12);
                                break;
                            case 'n':
                                s.append((char) 10);
                                break;
                            case 'r':
                                s.append((char) 13);
                                break;
                            case 't':
                                s.append((char) 9);
                                break;
                            case 'u':
                                int hexVal = Integer.parseInt(new String(new char[]{next(), next(), next(), next()}), 16);
                                s.append((char) hexVal);
                                //     i += 4;
                                break;
                            default:
                                break;
                        }
                    } else {
                        s.append(c);
                    }
                } else {
                    next();
                    return s.toString();
                }
            }
        } else {
            throw new UnexpectedException('"', cur);
        }
    }

    class UnexpectedException extends IOException {

        public UnexpectedException(char expected, char cur) {
            super("'" + expected + "' was expected but '" + cur + "' was found.");
        }
    }

    private static final Pattern P = Pattern.compile("([0-9]{4,4})-([0-9]{2,2})-([0-9]{2,2})T([0-9]{2,2}):([0-9]{2,2}):([0-9]{2,2}).([0-9]{3,3})(([-+][0-9]{2,2}:[0-9]{2,2})|Z)");

    public static boolean isDate(String str) throws Exception {
        return P.matcher(str).matches();
    }

    public static Date decodeDate(String str) throws Exception {
        if (!P.matcher(str).matches()) {
            throw new Exception("Date doesn't match the required format");
        }

        int y = Integer.valueOf(str.substring(0, 4));
        int m = Integer.valueOf(str.substring(5, 7)) - 1;
        int d = Integer.valueOf(str.substring(8, 10));
        int h = Integer.valueOf(str.substring(11, 13));
        int mn = Integer.valueOf(str.substring(14, 16));
        int s = Integer.valueOf(str.substring(17, 19));
        int ms = Integer.valueOf(str.substring(20, 23));

        GregorianCalendar gc = new GregorianCalendar(y, m, d, h, mn, s);
        gc.set(GregorianCalendar.MILLISECOND, ms);

//estas líneas harían que se transformen las horas cuando front y back están en zonas horarias diferentes
        /*String tz = str.substring(23, str.length());
        TimeZone t;
        if (tz.equals("Z")) {
            t = TimeZone.getTimeZone("GMT");
        } else {
            t = TimeZone.getTimeZone("GMT" + tz);
        }
        gc.setTimeZone(t);*/
        return gc.getTime();
    }

    public static String encodeDate(Date d) {
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(d);
        int year = c.get(GregorianCalendar.YEAR);
        int month = c.get(GregorianCalendar.MONTH) + 1;
        int day = c.get(GregorianCalendar.DAY_OF_MONTH);
        int h = c.get(GregorianCalendar.HOUR_OF_DAY);
        int m = c.get(GregorianCalendar.MINUTE);
        int s = c.get(GregorianCalendar.SECOND);
        int ms = c.get(GregorianCalendar.MILLISECOND);

        int off = c.getTimeZone().getOffset(d.getTime());
        int htz = (int) Math.abs(Math.floor(off / 3600000d));
        int mtz = (int) Math.abs((off % 3600000d) / 60000d);

        String tz;
        if (off == 0) {
            tz = "Z";
        } else {
            tz = String.format("%s%02d:%02d", (off < 0 ? "-" : "+"), htz, mtz);
        }
        return String.format("%04d-%02d-%02dT%02d:%02d:%02d.%03d%s", year, month, day, h, m, s, ms, tz);
    }
}
