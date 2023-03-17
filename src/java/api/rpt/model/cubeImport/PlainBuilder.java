package api.rpt.model.cubeImport;

public class PlainBuilder implements Builder {

    private final StringBuilder sb = new StringBuilder("");

    @Override
    public PlainBuilder sp() {
        sb.append(" ");
        return this;
    }

    @Override
    public PlainBuilder br() {
        return sp();
    }

    @Override
    public PlainBuilder black(String text) {
        sb.append(text);
        return this;
    }

    @Override
    public PlainBuilder gray(String text) {
        sb.append(text);
        return this;
    }

    @Override
    public PlainBuilder blue(String text) {
        sb.append(text);
        return this;
    }

    @Override
    public PlainBuilder blueReg(String text) {
        sb.append(text);
        return this;
    }

    @Override
    public PlainBuilder fucsia(String text) {
        sb.append(text);
        return this;
    }

    @Override
    public PlainBuilder olive(String text) {
        sb.append(text);
        return this;
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
