package api.rpt.api.dataTypes;

import java.util.Objects;
import jxl.format.BorderLineStyle;

public class CellBorder {

    int top;
    int right;
    int bottom;
    int left;
    int tickness;
    String color;

    public static final int HAIR = 1;
    public static final int THIN = 2;
    public static final int MEDIUM = 3;
    public static final int THICK = 4;
    public static final int NONE = 0;

    public CellBorder() {
        this(NONE, NONE, NONE, NONE, DataType.COL_WHITE);
    }

    public CellBorder(int top, int right, int bottom, int left, String color) {
        this.top = top;
        this.right = right;
        this.bottom = bottom;
        this.left = left;
        this.color = color;
    }

    public static BorderLineStyle getBorderStyle(int b) {
        switch (b) {
            case HAIR:
                return BorderLineStyle.HAIR;
            case THIN:
                return BorderLineStyle.THIN;
            case MEDIUM:
                return BorderLineStyle.MEDIUM;
            case THICK:
                return BorderLineStyle.THICK;
            case NONE:
                return BorderLineStyle.NONE;
            default:
                throw new RuntimeException();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CellBorder)) {
            return false;
        }
        return obj.hashCode() == hashCode();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + this.top;
        hash = 59 * hash + this.right;
        hash = 59 * hash + this.bottom;
        hash = 59 * hash + this.left;
        hash = 59 * hash + this.tickness;
        hash = 59 * hash + Objects.hashCode(this.color);
        return hash;
    }
}
