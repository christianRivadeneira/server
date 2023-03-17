package metadata.log;

public class Diff {

    public static String getTableName(Object o) {
        String[] parts = o.getClass().getName().split("\\.");
        return toDbName(parts[parts.length - 1]);
    }
    
    public static String toDbName(String name) {
        name = (name.substring(0, 1).toLowerCase() + name.substring(1, name.length()));
        String s = "";
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c >= 65 && c <= 90) {
                s += "_" + name.substring(i, i + 1).toLowerCase();
            } else if (c >= 48 && c <= 57) {
                boolean under = i == 0 ? true : name.charAt(i - 1) != '_';
                s += (under ? "_" : "") + name.substring(i, i + 1);
            } else {
                s += name.substring(i, i + 1);
            }
        }
        return s;
    }
}
