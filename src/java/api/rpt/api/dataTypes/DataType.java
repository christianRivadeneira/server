package api.rpt.api.dataTypes;

import api.rpt.api.charts.Colors;
import api.rpt.api.dataTypes.time.Day;
import api.rpt.api.dataTypes.time.Month;
import api.rpt.api.dataTypes.time.Quarter;
import api.rpt.api.dataTypes.time.Week;
import api.rpt.api.dataTypes.time.Year;
import api.rpt.api.operations.Operation;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.Format;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JSpinner;
import jxl.biff.DisplayFormat;
import jxl.format.Border;
import jxl.format.Colour;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import api.MySQLCol;

public abstract class DataType implements Serializable {

    public static final DataType TYPE_INT = new Int();
    public static final DataType TYPE_DBL = new Dbl();
    public static final DataType TYPE_DECIMAL = new Dec();
    public static final DataType TYPE_BOOL = new Bool();
    public static final DataType TYPE_STR = new Str();
    public static final DataType TYPE_ENUM = new Enum();
    public static final DataType TYPE_DATE = new Date();
    public static final DataType TYPE_DT = new Dt();
    public static final DataType TYPE_TIME = new Time();
    public static final DataType TYPE_YEAR = new Year();
    public static final DataType TYPE_QUARTER = new Quarter();
    public static final DataType TYPE_MONTH = new Month();
    public static final DataType TYPE_WEEK = new Week();
    public static final DataType TYPE_DAY = new Day();
    public static final DataType TYPE_GAUGE = new Gauge();

    public static final int FONT_BOLD = 1;
    public static final int FONT_REGULAR = 2;

    public static final String COL_LIGHT_GRAY = "l_gray";
    public static final String COL_AMBAR = Colors.AMBER;
    public static final String COL_BLUE = Colors.BLUE;
    public static final String COL_BROWN = Colors.BROWN;
    public static final String COL_CYAN = Colors.CYAN;
    public static final String COL_ORANGE = Colors.DEEP_ORANGE;
    public static final String COL_INDIGO = Colors.INDIGO;
    public static final String COL_GREEN = Colors.LIGHT_GREEN;
    public static final String COL_PURPLE = Colors.PURPLE;
    public static final String COL_RED = Colors.RED;
    public static final String COL_AQUA = Colors.TEAL;
    public static final String COL_WHITE = "white";
    public static final String COL_ALL = Colors.ALL;

    /**
     * Excel	Android black	black Brown all Olive Green ambar Dark Green azul
     * Dark Teal café Dark Blue cian Indigo naraja Grey 80 indigo Dark Red verde
     * Orange purpura Dark Yellow rojo Green aguamarina Teal gris_claro white
     * white
     *
     * @return
     */
    public abstract String getName();

    public abstract String getAsXlsString(Object content);

    public abstract String getAsSQLString(Object content);

    public abstract boolean isAbleToAdd();

    public abstract boolean hasMaxMin();

    public abstract Format getJavaFormat();

    public abstract DisplayFormat getJxlFormat();

    public abstract String getLabel();

    public abstract Operation[] getOpers();

    public abstract MySQLCol getMySQLCol();

    public abstract void prepareSpinner(JSpinner spn);

    public abstract void addCell(WritableSheet sheet, int fontStyle, String bgColor, CellBorder border, int col, int row, Object val) throws Exception;

    @Override
    public String toString() {
        return getName();
    }

    public static String getEnumOptions(DataType[] types) throws Exception {
        String str = "";
        for (DataType dt : types) {
            str += dt.getName() + "=" + dt.getLabel() + "&";
        }
        return str;
    }

    public static String getEnumOptions() throws Exception {
        return getEnumOptions(types);
    }

    public static DataType[] types;

    static {
        List<DataType> lTypes = new ArrayList<>();
        Field[] flds = DataType.class.getDeclaredFields();
        for (Field fld : flds) {
            if (fld.getType().equals(DataType.class)) {
                try {
                    lTypes.add((DataType) fld.get(null));
                } catch (IllegalArgumentException | IllegalAccessException ex) {
                    Logger.getLogger(DataType.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        types = lTypes.toArray(new DataType[lTypes.size()]);
    }

    public static WritableFont getBoldFont() {
        return new WritableFont(WritableFont.ARIAL, 10, WritableFont.BOLD);
    }

    public static DataType getType(String type) {
        for (DataType type1 : types) {
            if (type1.getName().equals(type)) {
                return type1;
            }
        }
        throw new RuntimeException("Tipo no reconocido: " + type);
    }

    private static class FormatParams {

        public int style;
        public String color;
        public DisplayFormat format;
        public CellBorder border;

        public FormatParams(int style, String color, DisplayFormat format, CellBorder border) {
            this.style = style;
            this.color = color;
            this.format = format;
            this.border = border;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof FormatParams)) {
                return false;
            }
            return obj.hashCode() == hashCode();
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 83 * hash + this.style;
            hash = 83 * hash + Objects.hashCode(this.color);
            hash = 83 * hash + Objects.hashCode(this.format);
            hash = 83 * hash + Objects.hashCode(this.border);
            return hash;
        }

    }

    private static Map<FormatParams, WritableCellFormat> formats = new HashMap<>();

    public static void clearFormats() {
        formats.clear();
    }

    public WritableCellFormat getFormat(int style, String bgColor, CellBorder border) throws Exception {
        return getFormat(style, bgColor, border, getJxlFormat());
    }

    public static Colour getColor(String color) {
        switch (color) {
            case COL_LIGHT_GRAY:
                return Colour.TEAL;
            case COL_AMBAR:
                return Colour.OLIVE_GREEN;
            case COL_BLUE:
                return Colour.DARK_GREEN;
            case COL_BROWN:
                return Colour.DARK_TEAL;
            case COL_CYAN:
                return Colour.DARK_BLUE;
            case COL_ORANGE:
                return Colour.INDIGO;
            case COL_INDIGO:
                return Colour.GRAY_80;
            case COL_GREEN:
                return Colour.DARK_RED;
            case COL_PURPLE:
                return Colour.ORANGE;
            case COL_AQUA:
                return Colour.GREEN;
            case COL_WHITE:
                return Colour.WHITE;
            case COL_RED:
                return Colour.DARK_YELLOW;
            case COL_ALL:
                return Colour.BROWN;
            default:
                throw new RuntimeException("Color no reconocido: " + color);
        }
    }

    public static WritableCellFormat getFormat(int style, String bgColor, CellBorder border, DisplayFormat format) throws Exception {

        FormatParams fp = new FormatParams(style, bgColor, format, border);
        if (formats.containsKey(fp)) {
            return formats.get(fp);
        } else {
            WritableCellFormat f;
            if (format != null) {
                switch (style) {
                    case FONT_BOLD:
                        f = new WritableCellFormat(getBoldFont(), format);
                        break;
                    case FONT_REGULAR:
                        f = new WritableCellFormat(format);
                        break;
                    default:
                        throw new RuntimeException("Estilo no reconocido: " + style);
                }
            } else {
                switch (style) {
                    case FONT_BOLD:
                        f = new WritableCellFormat(getBoldFont());
                        break;
                    case FONT_REGULAR:
                        f = new WritableCellFormat();
                        break;
                    default:
                        throw new RuntimeException("Estilo no reconocido: " + style);
                }
            }

            f.setBorder(Border.BOTTOM, CellBorder.getBorderStyle(border.bottom), getColor(border.color));
            f.setBorder(Border.LEFT, CellBorder.getBorderStyle(border.left), getColor(border.color));
            f.setBorder(Border.RIGHT, CellBorder.getBorderStyle(border.right), getColor(border.color));
            f.setBorder(Border.TOP, CellBorder.getBorderStyle(border.top), getColor(border.color));

            f.setBackground(getColor(bgColor));
            formats.put(fp, f);
            return f;
        }
    }

    public static DataType getFromMySQLType(String mysqlType) {
        String type = mysqlType.toLowerCase().split("\\(")[0];
        switch (type) {
            case "int":
                return TYPE_INT;
            case "bigint":
                return TYPE_INT;
            case "smallint":
                return TYPE_INT;
            case "mediumint":
                return TYPE_INT;
            case "decimal":
                return TYPE_DECIMAL;
            case "double":
                return TYPE_DBL;
            case "float":
                return TYPE_DBL;
            case "date":
                return TYPE_DATE;
            case "datetime":
                return TYPE_DT;
            case "time":
                return TYPE_TIME;
            case "varchar":
                return TYPE_STR;
            case "char":
                return TYPE_STR;
            case "text":
                return TYPE_STR;
            case "tinyint":
                return TYPE_BOOL;
            case "enum":
                return TYPE_ENUM;
            default:
                throw new RuntimeException("El tipo " + mysqlType + " no está soportado");
        }
    }

    public static DataType getFromJavaClass(Class cs) {
        if (cs.equals(Integer.class) || cs.equals(BigInteger.class) || cs.equals(java.lang.Long.class)) {
            return TYPE_INT;
        } else if (cs.equals(BigDecimal.class)) {
            return TYPE_DECIMAL;
        } else if (cs.equals(Double.class) || cs.equals(Float.class)) {
            return TYPE_DBL;
        } else if (cs.equals(java.util.Date.class) || cs.equals(java.sql.Date.class) || cs.getName().equals("java.sql.Timestamp")) {
            return TYPE_DT;
        } else if (cs.equals(String.class)) {
            return TYPE_STR;
        } else if (cs.equals(Boolean.class)) {
            return TYPE_BOOL;
        } else if (cs.equals(java.sql.Time.class)) {
            return TYPE_TIME;
        } else {
            throw new RuntimeException("El tipo " + cs.getName() + " no está soportado");
        }
    }

    public String getFunction(String field) {
        return field;
    }
}
