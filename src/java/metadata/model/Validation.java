package metadata.model;

public class Validation {

    public static final String ALPHA_SPACES = "ALPHA_SPACES";
    public static final String DIGITS = "DIGITS";
    public static final String EMAIL = "EMAIL";
    public static final String MAX = "MAX";
    public static final String MIN = "MIN";
    public static final String REGEX = "REGEX";
    public static final String REQUIRED = "REQUIRED";
    public static final String UNIQUE = "UNIQUE";

    public String type;
    public Integer len;
    public String mask;

    public Validation() {
    }

    public Validation(String type) {
        this.type = type;
    }

    public Validation(String type, Integer len) {
        this.type = type;
        this.len = len;
    }

    public Validation(String type, String mask) {
        this.type = type;
        this.mask = mask;
    }
}
