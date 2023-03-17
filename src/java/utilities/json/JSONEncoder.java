package utilities.json;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class JSONEncoder {

    public static void encode(Object o, OutputStream os, boolean format) throws Exception {
        PrintStream sb = new PrintStream(os);
        writeObject(o, sb, format, 0);
        sb.flush();
    }

    private static void lb(PrintStream sb, boolean format, int level) {
        if (format) {
            sb.append(System.lineSeparator());
            for (int j = 0; j < level; j++) {
                sb.append("    ");
            }
        }
    }

    private static void writeObject(Object o, PrintStream sb, boolean format, Integer level) throws IllegalArgumentException, IllegalAccessException, Exception {
        level++;
        if (o == null) {
            sb.append("null");
            return;
        }
        if (o instanceof List<?>) {
            writeList(sb, (List) o, format, level);
            return;
        }

        if (o.getClass().isArray()) {
            writeList(sb, (Object[]) o, format, level);
            return;
        }
        sb.append("{");

        Class<?> c = o.getClass();
        String[] hidden;
        try {
            Field hiddenFld = c.getField("HIDDEN");
            hidden = (String[]) hiddenFld.get(o);
        } catch (NoSuchFieldException ex) {
            hidden = new String[]{};
        }

        Field[] declaredFields = c.getFields();
        List<Field> flds = new ArrayList<>();
        for (Field field : declaredFields) {
            boolean hide = false;
            for (String hid : hidden) {
                if (hid.equals(field.getName())) {
                    hide = true;
                    break;
                }
            }

            if (!hide && !java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                flds.add(field);
            }
        }

        for (int i = 0; i < flds.size(); i++) {
            lb(sb, format, level);
            Field fld = flds.get(i);
            Object v = fld.get(o);
            sb.append("\"");
            sb.append(fld.getName());
            sb.append("\":");
            if (format) {
                sb.append(" ");
            }
            writeValue(v, sb, format, level);
            if (i < flds.size() - 1) {
                sb.append(",");
            }
        }
        level--;
        lb(sb, format, level);
        sb.append("}");
    }

    private static void writeValue(Object v, PrintStream sb, boolean format, Integer level) throws Exception {
        if (v == null) {
            sb.append("null");
            return;
        }
        Class fc = v.getClass();
        if (fc.equals(String.class)) {
            sb.append("\"");
            jsonEnconde(sb, (String) v);
            sb.append("\"");
        } else if (java.util.Date.class.isAssignableFrom(fc)) {
            sb.append("\"");
            jsonEnconde(sb, JSONDecoder.encodeDate((java.util.Date) v));
            sb.append("\"");
        } else if (fc.equals(Integer.class)) {
            sb.append(v.toString());
        } else if (fc.equals(Boolean.class)) {
            sb.append((Boolean) v ? "true" : "false");
        } else if (fc.equals(Double.class)) {
            sb.append(v.toString());
        } else if (fc.equals(Long.class)) {
            sb.append(v.toString());
        } else if (fc.equals(BigDecimal.class)) {
            sb.append(v.toString());
        } else if (fc.equals(List.class) || fc.equals(ArrayList.class)) {
            List lst = (List) v;
            writeList(sb, lst, format, level);
        } else {
            if (fc.getName().startsWith("java.")) {
                throw new Exception("Unsupported class: " + fc.getName() + " v=" + v);
            }
            writeObject(v, sb, format, level);
        }
    }

    private static void writeList(PrintStream sb, Object[] lst, boolean format, Integer level) throws Exception {
        level++;
        sb.append("[");
        for (int j = 0; j < lst.length; j++) {
            Object obj = lst[j];
            writeValue(obj, sb, format, level);
            if (j < lst.length - 1) {
                sb.append(",");
                if (format) {
                    sb.append(" ");
                }
            }
        }
        sb.append("]");
        level--;
    }

    private static void writeList(PrintStream sb, List lst, boolean format, Integer level) throws Exception {
        level++;
        sb.append("[");
        for (int j = 0; j < lst.size(); j++) {
            Object obj = lst.get(j);
            writeValue(obj, sb, format, level);
            if (j < lst.size() - 1) {
                sb.append(",");
                if (format) {
                    sb.append(" ");
                }
            }
        }
        sb.append("]");
        level--;
    }

    private static void jsonEnconde(PrintStream sb, String str) {
        char[] chArr = str.toCharArray();
        for (char ch : chArr) {
            if (ch == '"') {
                sb.append("\\\"");
            } else if (ch == '/') {
                sb.append("\\/");
            } else if (ch == 8) {
                sb.append("\\b");
            } else if (ch == '\\') {
                sb.append("\\\\");
            } else if (ch == 12) {
                sb.append("\\f");
            } else if (ch == 10) {
                sb.append("\\n");
            } else if (ch == 13) {
                sb.append("\\r");
            } else if (ch == 9) {
                sb.append("\\t");
            } else if (ch >= 128) {
                sb.append("\\u").append(String.format("%04X", (int) ch));
            } else {
                sb.append(ch);
            }
        }
    }

}
