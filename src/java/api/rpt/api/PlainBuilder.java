package api.rpt.api;

public class PlainBuilder {

    private static final int BR_LEN = System.lineSeparator().length();
    
    private final StringBuilder sb = new StringBuilder("");

    public PlainBuilder sp() {
        sb.append(" ");
        return this;
    }

    public PlainBuilder br() {
        sb.append(System.lineSeparator());
        return this;
    }
    
    public PlainBuilder removeBr(){
        remove(BR_LEN);
        return this;
    }

    public PlainBuilder remove(int a) {
        sb.delete(sb.length() - a, sb.length());
        return this;
    }

    public PlainBuilder add(String text) {
        sb.append(text);
        return this;
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
