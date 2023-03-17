package utilities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonString;
import javax.json.JsonValue;
import static javax.json.JsonValue.ValueType.ARRAY;
import static javax.json.JsonValue.ValueType.OBJECT;
import static javax.json.JsonValue.ValueType.STRING;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import metadata.model.Table;
import service.MySQL.MySQLUpdate;
import web.ShortException;
import web.quality.SendMail;

public class MySQLQuery {

    public static boolean PRINT_QUERIES = false;

    private static final int INSERT = 1;
    private static final int UPDATE = 2;
    private static final int DELETE = 3;
    private static final int INSERT_IGNORE = 4;

    public static Date now(Connection conn) throws Exception {
        return new MySQLQuery("SELECT NOW()").getAsDate(conn);
    }

    private final String query;
    public static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("''yyyy-MM-dd HH:mm:ss''");
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("''yyyy-MM-dd''");

    private static final SimpleDateFormat sdtf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final Pattern dtPat = Pattern.compile("[\\d]{4}-[\\d]{2}-[\\d]{2} [\\d]{2}:[\\d]{2}:[\\d]{2}");

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private static final Pattern dPat = Pattern.compile("[\\d]{4}-[\\d]{2}-[\\d]{2}");

    private final Map<Integer, Object> params = new HashMap<>();

    public MySQLQuery(String query) {
        this.query = clean(query);
    }

    public static String formatDate(Date date) {
        return dateFormat.format(date);
    }

    public static String formatDateTime(Date date) {
        return dateTimeFormat.format(date);
    }

    public MySQLQuery() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public MySQLQuery setParam(int par, Object val) {
        if (params.containsKey(par)) {
            params.remove(par);
        }
        params.put(par, val);
        return this;
    }

    public MySQLQuery print() {
        System.out.println(query);
        return this;
    }

    public String getParametrizedQuery() {
        Iterator<Map.Entry<Integer, Object>> it = params.entrySet().iterator();
        String rta = query;
        while (it.hasNext()) {
            Map.Entry<Integer, Object> next = it.next();
            rta = setParam(rta, next.getKey(), next.getValue());
        }

        if (Table.DEVEL_MODE || PRINT_QUERIES) {
            Logger.getLogger(MySQLQuery.class.getName()).log(Level.INFO, rta);
        }
        return rta;
    }

    public static String setParam(String query, int par, Object val) {
        String v = null;
        if (val != null) {
            if (val instanceof Integer) {
                v = val.toString();
            } else if (val instanceof Long) {
                v = val.toString();
            } else if (val instanceof String) {
                if (((String) val).trim().isEmpty()) {
                    v = "NULL";
                } else {
                    v = "\"" + scape((String) val) + "\"";
                }
                //para las comillas escapadas en el replace all
                v = Matcher.quoteReplacement(v);
            } else if (val instanceof Boolean) {
                if (((Boolean) val)) {
                    v = "1";
                } else {
                    v = "0";
                }
            } else if (val instanceof ServerNow) {
                v = "NOW()";
            } else if (val instanceof ServerCurdate) {
                v = "CURDATE()";
            } else if (val instanceof Date) {
                v = MySQLQuery.dateTimeFormat.format((Date) val);
            } else if (val instanceof BigDecimal) {
                v = ((BigDecimal) val).toPlainString();
            } else if (val instanceof Double) {
                v = ((Double) val).toString();
            } else if (val instanceof byte[]) {
                byte[] hChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
                byte[] b = (byte[]) val;
                StringBuilder s = new StringBuilder("0x");
                for (int i = 0; i < b.length; i++) {
                    int v1 = b[i] & 0xff;
                    s.append((char) hChars[v1 >> 4]);
                    s.append((char) hChars[v1 & 0xf]);
                }
                v = s.toString();
            } else if (val instanceof BigInteger) {
                v = val.toString();
            } else {
                throw new RuntimeException("tipo no soportado " + val.getClass().toString());
            }
        } else {
            v = "NULL";
        }
        query = query.replaceAll("\\?" + par + ",", v + ",");
        query = query.replaceAll("\\?" + par + "[\\s]", v + " ");
        query = query.replaceAll("\\?" + par + "[)]", v + ")");
        query = query.replaceAll("\\?" + par + "[;]", v + ";");
        query = query.replaceAll("\\?" + par + "\\z", v + " ");
        return query;
    }

    public Double getAsDouble(Connection ep) throws Exception {
        return getAsDouble(getSingleValue(ep));
    }

    public Float getAsFloat(Connection ep) throws Exception {
        return getAsFloat(getSingleValue(ep));
    }

    public Integer getAsInteger(Connection ep, boolean nullAsZero) throws Exception {
        Integer i = getAsInteger(getSingleValue(ep));
        return i == null && nullAsZero ? 0 : i;
    }

    public Integer getAsInteger(Connection ep) throws Exception {
        return getAsInteger(getSingleValue(ep));
    }

    public Long getAsLong(Connection ep) throws Exception {
        return getAsLong(getSingleValue(ep));
    }

    public Boolean getAsBoolean(Connection ep) throws Exception {
        return getAsBoolean(getSingleValue(ep));
    }

    public BigDecimal getAsBigDecimal(Connection ep, boolean nullAsZero) throws Exception {
        return getAsBigDecimal(getSingleValue(ep), nullAsZero);
    }

    public String getAsString(Connection ep) throws Exception {
        return getAsString(getSingleValue(ep));
    }

    public Date getAsDate(Connection ep) throws Exception {
        return getAsDate(getSingleValue(ep));
    }

    public Object getSingleValue(Connection ep) throws Exception {
        Object[] rec = getRecord(ep);
        if (rec != null) {
            switch (rec.length) {
                case 0:
                    return null;
                case 1:
                    return rec[0];
                default:
                    throw new Exception("La consulta retorna más de una columna");
            }
        } else {
            return null;
        }
    }

    /**
     * Devuelve la primera fila del resultado
     *
     * @param ep
     * @return la primera fila del resultado, o null si no hubo resultados
     * @throws Exception
     */
    public Object[] getRecord(Connection ep) throws Exception {
        Object[][] data = getRecords(ep);
        if (data != null) {
            switch (data.length) {
                case 0:
                    return null;
                case 1:
                    return data[0];
                default:
                    if (Table.DEVEL_MODE) {
                        throw new Exception("La consulta retorna más de una fila");
                    } else {
                        //throw new Exception("La consulta retorna más de una fila");
                        StringBuilder sb = new StringBuilder(getParametrizedQuery());
                        sb.append("<br>");
                        for (Object[] row : data) {
                            sb.append("<br>");
                            for (Object c : row) {
                                sb.append(c);
                                sb.append("|");
                            }
                        }
                        SendMail.sendMail(ep, "karol.mendoza@montagas.com.co", "more than one row", sb.toString(), sb.toString());
                        return data[0];
                    }
            }
        } else {
            return null;
        }
    }

    public List<List<Object>> getRecordsAsList(Connection conn) throws Exception {
        Statement st = null;
        try {
            st = conn.createStatement();
            ResultSet rs = st.executeQuery(getParametrizedQuery());
            int cols = rs.getMetaData().getColumnCount();
            List<List<Object>> res = new ArrayList<>();

            while (rs.next()) {
                List<Object> row = new ArrayList<>(cols);
                res.add(row);
                for (int i = 0; i < cols; i++) {
                    row.add(rs.getObject(i + 1));
                }
            }
            return detectEmptyResult(res);
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    public static Object[][] getRecords(ResultSet rs) throws SQLException {
        int cols = rs.getMetaData().getColumnCount();
        List<Object[]> res = new ArrayList<>();

        while (rs.next()) {
            Object[] row = new Object[cols];
            res.add(row);
            for (int i = 0; i < cols; i++) {
                Object obj = rs.getObject(i + 1);
                row[i] = obj;
            }
        }
        return detectEmptyResult(res.toArray(new Object[0][]));
    }

    public Object[][] getRecords(Connection conn) throws Exception {

        Statement st = null;

        try {
            st = conn.createStatement();
            String ps = getParametrizedQuery();
            ResultSet rs = st.executeQuery(ps);
            return getRecords(rs);
        } catch (Exception e) {
            MySQLQuery.insertFailedQuery(getParametrizedQuery(), e, conn);
            throw e;
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    public static List<List<Object>> detectEmptyResult(List<List<Object>> data) {
        if (data != null && data.size() == 1) {
            List<Object> row = data.get(0);
            boolean allNull = true;
            for (int i = 0; i < row.size() && allNull; i++) {
                allNull = (row.get(i) == null);
            }
            if (allNull) {
                return new ArrayList<>(0);
            } else {
                return data;
            }
        } else {
            return data;
        }
    }

    public static Object[][] detectEmptyResult(Object[][] data) {
        if (data != null && data.length == 1) {
            Object[] row = data[0];
            boolean allNull = true;
            for (int i = 0; i < row.length && allNull; i++) {
                allNull = (row[i] == null);
            }
            if (allNull) {
                return new Object[0][row.length];
            } else {
                return data;
            }
        } else {
            return data;
        }
    }

    public int executeUpdate(Connection ep) throws Exception {
        return sendUpdate(ep, UPDATE);
    }

    public int executeInsert(Connection ep) throws Exception {
        return sendUpdate(ep, INSERT);
    }

    public int executeDelete(Connection ep) throws Exception {
        return sendUpdate(ep, DELETE);
    }
    
    public int executeInsertIgnore(Connection ep) throws Exception {
        return sendUpdate(ep, INSERT_IGNORE);
    }

    private int sendUpdate(Connection con, int type) throws Exception {
        String q = getParametrizedQuery();
        try (Statement st = con.createStatement()) {
            int res = -1;
            if (type == INSERT) {
                if (st.executeUpdate(q, Statement.RETURN_GENERATED_KEYS) == 0) {
                    throw new Exception("Error al insertar.");
                } else {
                    ResultSet rs1 = st.getGeneratedKeys();
                    if (rs1.next()) {
                        res = rs1.getInt(1);
                        rs1.close();
                    } else {
                        throw new Exception("No se pudo recuperar la llave.");
                    }
                }
            } else {
                res = st.executeUpdate(q);
            }
            return res;
        } catch (Exception ex) {
            Logger.getLogger(MySQLQuery.class.getName()).log(Level.SEVERE, null, ex);
            insertFailedQuery(q, ex, con);
            throw MySQLUpdate.maskSQLException(q, 0, ex);
        }
    }

    public static void insertFailedQuery(String query, Exception ex, Connection con) {
        try (Statement st = con.createStatement()) {
            if (!con.getAutoCommit()) {
                con.rollback();
            }
            st.executeUpdate("INSERT INTO sigma.sys_failed_query SET dt = NOW(), query = '" + query.replace("'", "\\'").replaceAll("\"", "\\\\\"") + "', stack_trace = '" + ex.getMessage().replace("'", "\\'").replaceAll("\"", "\\\\\"") + " - " + new ShortException().getSimpleStack().replace("'", "\\'").replaceAll("\"", "\\\\\"") + "'");
            if (!con.getAutoCommit()) {
                con.commit();
            }
        } catch (Exception e) {
            Logger.getLogger(MySQLQuery.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public static String clean(String q) {
        return q.replaceAll("\\s", " ").replaceAll("[\\s]{1,}", " ");
    }

    public static BigDecimal getAsBigDecimal(Object o, boolean nullAsZero) {
        if (o == null && nullAsZero) {
            return BigDecimal.ZERO;
        }
        if (o == null && !nullAsZero) {
            return null;
        }
        if (o instanceof BigDecimal) {
            return (BigDecimal) o;
        }
        if (o instanceof String) {
            return new BigDecimal((String) o);
        }
        if (o instanceof Long) {
            return new BigDecimal((Long) o);
        }
        if (o instanceof Integer) {
            return new BigDecimal((Integer) o);
        }
        if (o instanceof Double) {
            return new BigDecimal((Double) o);
        }
        if (o instanceof Float) {
            return new BigDecimal((Float) o);
        }
        if (o instanceof Boolean) {
            return ((Boolean) o) ? new BigDecimal(1) : new BigDecimal(0);
        }
        throw new RuntimeException("No se puede convertir " + (o != null ? o.getClass().getName() : "NULL") + " a BigDecimal.");
    }

    public static Float getAsFloat(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof BigDecimal) {
            return ((BigDecimal) o).floatValue();
        }
        if (o instanceof String) {
            return new Float((String) o);
        }
        if (o instanceof Long) {
            return new Float((Long) o);
        }
        if (o instanceof Integer) {
            return new Float((Integer) o);
        }
        if (o instanceof Double) {
            return new Float((Double) o);
        }
        if (o instanceof Float) {
            return (Float) o;
        }
        if (o instanceof Boolean) {
            return ((Boolean) o) ? new Float(1) : new Float(0);
        }
        throw new RuntimeException("No se puede convertir " + (o != null ? o.getClass().getName() : "NULL") + " a BigDecimal.");
    }

    public static Double getAsDouble(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Double) {
            return ((Double) o);
        }
        if (o instanceof Long) {
            return ((Long) o).doubleValue();
        }
        if (o instanceof Integer) {
            return ((Integer) o).doubleValue();
        }
        if (o instanceof BigDecimal) {
            return ((BigDecimal) o).doubleValue();
        }
        if (o instanceof Boolean) {
            return ((Boolean) o) ? 1d : 0d;
        }
        if (o instanceof Float) {
            return ((Float) o).doubleValue();
        }
        throw new RuntimeException("No se puede convertir " + o.getClass().getName() + " a Double.");
    }

    public static Long getAsLong(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Long) {
            return (Long) o;
        }
        if (o instanceof Integer) {
            return ((Integer) o).longValue();
        }
        if (o instanceof BigDecimal) {
            return ((BigDecimal) o).longValue();
        }
        if (o instanceof Boolean) {
            return ((Boolean) o) ? 1l : 0l;
        }
        if (o instanceof Date) {
            return ((Date) o).getTime();
        }
        if (o instanceof String) {
            return Long.valueOf((String) o);
        }
        throw new RuntimeException("No se puede convertir " + o.getClass().getName() + " a Long.");
    }

    public static Integer getAsInteger(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Integer) {
            return (Integer) o;
        }
        if (o instanceof Long) {
            return ((Long) o).intValue();
        }
        if (o instanceof BigDecimal) {
            return ((BigDecimal) o).intValue();
        }
        if (o instanceof Boolean) {
            return ((Boolean) o) ? 1 : 0;
        }
        if (o instanceof BigInteger) {
            return ((BigInteger) o).intValue();
        }
        if (o instanceof Double) {
            return ((Double) o).intValue();
        }
        if (o instanceof String) {
            return Integer.valueOf((String) o);
        }
        throw new RuntimeException("No se puede convertir " + o.getClass().getName() + " a Integer.");
    }

    public static Boolean getAsBoolean(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Boolean) {
            return ((Boolean) o);
        }
        if (o instanceof Integer) {
            return ((Integer) o) == 1;
        }
        if (o instanceof Long) {
            return ((Long) o) == 1;
        }
        if (o instanceof BigDecimal) {
            return ((BigDecimal) o).compareTo(BigDecimal.ONE) == 0;
        }
        if (o instanceof String) {
            String str = ((String) o).toLowerCase().trim();
            if (str.matches("[01]{1}")) {
                return str.equals("1");
            } else if (str.matches("[a-z]+")) {
                switch (str) {
                    case "true":
                        return true;
                    case "false":
                        return false;
                    default:
                        throw new RuntimeException(str + "No se puede convertir a Boolean");
                }
            }
        }
        throw new RuntimeException("No se puede convertir " + o.getClass().getName() + " a Boolean.");
    }

    public static BigInteger getAsBigInteger(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof BigInteger) {
            return (BigInteger) o;
        }
        if (o instanceof Long) {
            return BigInteger.valueOf((Long) o);
        }
        if (o instanceof BigDecimal) {
            return ((BigDecimal) o).toBigInteger();
        }
        if (o instanceof Boolean) {
            return BigInteger.valueOf(((Boolean) o) ? 1l : 0l);
        }
        if (o instanceof Integer) {
            return BigInteger.valueOf(((Integer) o).longValue());
        }
        if (o instanceof Double) {
            return BigInteger.valueOf(((Double) o).longValue());
        }
        if (o instanceof String) {
            return BigInteger.valueOf(Long.valueOf(o.toString()));
        }
        throw new RuntimeException("No se puede convertir " + o.getClass().getName() + " a BigInteger.");
    }

    /**
     * Convirte en String
     *
     * @param obj
     * @return null, si obj es null
     */
    public static String getAsString(Object obj) {
        if (obj == null) {
            return null;
        } else if (obj instanceof byte[]) {
            return new String((byte[]) obj);
        }
        return obj.toString();
    }

    public static Date getAsDate(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Date) {
            return (Date) o;
        }
        if (o instanceof String) {
            try {
                if (dtPat.matcher((String) o).matches()) {
                    return sdtf.parse((String) o);
                } else if (dPat.matcher((String) o).matches()) {
                    return sdf.parse((String) o);
                } else {
                    throw new RuntimeException(o + " No tiene el formato MySQL");
                }
            } catch (ParseException ex) {
                throw new RuntimeException(o + " No es una fecha valida");
            }
        }
        throw new RuntimeException("Se esperaba Date, se halló " + o.getClass().toString());
    }

    public String getQuery() {
        return query;
    }

    //hay una copia de esto en el cliente en NotesTextField
    public static String scape(String str) {
        String rta = "";
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '\'':
                case '\"':
                    rta += "\\" + c;
                    break;
                case '\\':
                    if (i < str.length() - 1) {
                        char c1 = str.charAt(i + 1);
                        switch (c1) {
                            case '\'':
                            case '\"':
                            case '\\':
                                rta += c;
                                rta += c1;
                                i++;
                                break;
                            default:
                                rta += "\\\\";
                                break;
                        }
                    } else {
                        rta += "\\\\";
                    }
                    break;
                default:
                    rta += c;
                    break;
            }
        }
        return rta;
    }

    public static JsonObject scapeJsonObjStream(HttpServletRequest request) throws IOException, ServletException {
        return (JsonObject) scapeJsonObj(Json.createReader(request.getInputStream()).readObject());
    }

    public static JsonObject scapeJsonObj(HttpServletRequest request) throws IOException, ServletException {
        return scapeJsonObj(request, "data");
    }

    public static JsonObject scapeJsonObj(HttpServletRequest request, String partName) throws IOException, ServletException {
        return (JsonObject) scapeJsonObj(Json.createReader(request.getPart(partName).getInputStream()).readObject());
    }

    public static JsonArray scapeJsonArray(HttpServletRequest request) throws IOException, ServletException {
        return scapeJsonArray(request, "data");
    }

    public static JsonArray scapeJsonArray(HttpServletRequest request, String partName) throws IOException, ServletException {
        return (JsonArray) scapeJsonObj(Json.createReader(request.getPart(partName).getInputStream()).readArray());
    }

    public static JsonValue scapeJsonObj(final JsonValue v) {
        switch (v.getValueType()) {
            case STRING:
                return new JsonString() {

                    private final String s = MySQLQuery.scape(((JsonString) v).getString());

                    @Override
                    public String getString() {
                        return s;
                    }

                    @Override
                    public CharSequence getChars() {
                        return s;
                    }

                    @Override
                    public JsonValue.ValueType getValueType() {
                        return JsonValue.ValueType.STRING;
                    }
                };
            case OBJECT:
                JsonObjectBuilder job = Json.createObjectBuilder();
                JsonObject o = ((JsonObject) v);
                Iterator<Map.Entry<String, JsonValue>> it = o.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<String, JsonValue> e = it.next();
                    job.add(e.getKey(), scapeJsonObj(e.getValue()));
                }
                return job.build();
            case ARRAY:
                JsonArray arr = (JsonArray) v;
                JsonArrayBuilder b = Json.createArrayBuilder();
                for (int i = 0; i < arr.size(); i++) {
                    b.add(scapeJsonObj(arr.get(i)));
                }
                return b.build();
            default:
                return v;
        }
    }

    public static Map<String, String> scapedParams(HttpServletRequest request) throws UnsupportedEncodingException {
        request.setCharacterEncoding("UTF-8");
        Map<String, String> rta = new HashMap<>();
        Enumeration<String> en = request.getParameterNames();
        while (en.hasMoreElements()) {
            String name = en.nextElement();
            rta.put(name, MySQLQuery.scape(request.getParameter(name)));
        }
        return rta;
    }

    public static String[][] getEnumOptionsAsMatrix(String options) {
        String[] parts = options.split("&");
        String[][] res = new String[parts.length][];
        for (int i = 0; i < parts.length; i++) {
            String[] sbParts = parts[i].split("=");
            res[i] = new String[]{sbParts[0].trim(), sbParts[1].trim()};
        }
        return res;
    }

    public static Map<String, String> getEnumOptionsAsMap(String options) {
        HashMap<String, String> map = new HashMap();
        String[] parts = options.split("&");
        for (String part : parts) {
            String[] sbParts = part.split("=");
            map.put(sbParts[0].trim(), sbParts[1].trim());
        }
        return map;
    }

    /**
     * Retorna true cuando obj es nulo, un String vacío o un List vacío
     *
     * @param obj
     * @return
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof String) {
            return ((String) obj).isEmpty();
        } else if (obj instanceof List) {
            return ((List) obj).isEmpty();
        }
        return false;
    }

    public static Map<String, String> getEnumOptAsMap(String[][] enumOpts) {
        HashMap rta = new HashMap<>();
        for (String[] opt : enumOpts) {
            rta.put(opt[0], opt[1]);
        }
        return rta;
    }
}
