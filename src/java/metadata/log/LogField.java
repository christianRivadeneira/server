package metadata.log;

public class LogField {

    public static final int ADDED = 1;
    public static final int CHANGED = 2;
    public static final int REMOVED = 3;

    public String fieldName;
    public String fieldId;
    public int operation;
    public Object oldValue;
}
