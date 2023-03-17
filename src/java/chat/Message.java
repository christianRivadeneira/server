package chat;

import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

public class Message {

    public static final short INT = 1;
    public static final short UTF = 2;
    public static final short BOOLEAN = 3;
    public static final short DATE = 4;
    public static final short LONG = 5;

    private List<Object> data;
    private int c;

    public Message() {
        data = new ArrayList<>();
    }

    public Message(String jsonStr) {
        c = -1;
        data = new ArrayList<>();
        JsonReader reader = Json.createReader(new StringReader(jsonStr));
        JsonObject json = reader.readObject();
        int size = json.getInt("s");
        JsonArray array = json.getJsonArray("d");
        for (int i = 0; i < size; i++) {
            JsonObject val = (JsonObject) array.get(i);
            int type = val.getInt("t");
            switch (type) {
                case INT:
                    data.add(val.getInt("v"));
                    break;
                case LONG:
                    data.add(Long.valueOf(val.getString("v")));
                    break;
                case UTF:
                    data.add(val.getString("v"));
                    break;
                case BOOLEAN:
                    data.add(val.getBoolean("v"));
                    break;
                case DATE:
                    data.add(new Date(Long.valueOf(val.getString("v"))));
                    break;
                default:
                    break;
            }
        }
    }

    public String toJsonString() {
        JsonObjectBuilder ob = Json.createObjectBuilder();
        JsonArrayBuilder ab = Json.createArrayBuilder();
        ob.add("s", data.size());

        for (int i = 0; i < data.size(); i++) {
            Object d = data.get(i);
            JsonObjectBuilder dob = Json.createObjectBuilder();
            if (d instanceof Integer) {
                dob.add("t", INT);
                dob.add("v", ((Integer) d));
            } else if (d instanceof Long) {
                dob.add("t", LONG);
                dob.add("v", String.valueOf((Long) d));
            } else if (d instanceof String) {
                dob.add("t", UTF);
                dob.add("v", ((String) d));
            } else if (d instanceof Boolean) {
                dob.add("t", BOOLEAN);
                dob.add("v", ((Boolean) d));
            } else if (d instanceof Date) {
                dob.add("t", DATE);
                dob.add("v", String.valueOf(((Date) d).getTime()));
            }
            ab.add(dob);
        }
        ob.add("d", ab);

        return ob.build().toString();
    }

    public void addInt(int i) {
        data.add(i);
    }

    public void addLong(long l) {
        data.add(l);
    }

    public void addUTF(String s) {
        data.add(s);
    }

    public void addBoolean(boolean b) {
        data.add(b);
    }

    public void addDate(Date d) {
        data.add(d);
    }

    public int getInt() {
        c++;
        return getAsInteger(data.get(c));
    }

    public long getLong() {
        c++;
        return getAsLong(data.get(c));
    }

    public String getUTF() {
        c++;
        return getAsString(data.get(c));
    }

    public boolean getBoolean() {
        c++;
        return getAsBoolean(data.get(c));
    }

    public Date getDate() {
        c++;
        return getAsDate(data.get(c));
    }

    private static Long getAsLong(Object o) {
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
        throw new RuntimeException("No se puede convertir " + o.getClass().getName() + " a Long.");
    }

    private static Integer getAsInteger(Object o) {
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
        throw new RuntimeException("No se puede convertir " + o.getClass().getName() + " a Integer.");
    }

    private static Boolean getAsBoolean(Object o) {
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
        throw new RuntimeException("No se puede convertir " + o.getClass().getName() + " a Boolean.");
    }

    private static String getAsString(Object obj) {
        if (obj == null) {
            return null;
        } else if (obj instanceof byte[]) {
            return new String((byte[]) obj);
        }
        return obj.toString();
    }

    private static Date getAsDate(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof Date) {
            return (Date) o;
        }
        throw new RuntimeException("Se esperaba Date, se halló " + o.getClass().toString());
    }
}
