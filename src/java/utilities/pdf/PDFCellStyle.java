package utilities.pdf;

import com.lowagie.text.Chunk;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public final class PDFCellStyle implements Serializable {

    public static final int DEFAULT_PADDING = 2;
    public static final float DEFAULT_FONT_SIZE = 8f;

    public static final int ALIGN_CENTER = Element.ALIGN_CENTER;
    public static final int ALIGN_LEFT = Element.ALIGN_LEFT;
    public static final int ALIGN_RIGHT = Element.ALIGN_RIGHT;

    public static final int ALIGN_MIDDLE = Element.ALIGN_MIDDLE;
    public static final int ALIGN_TOP = Element.ALIGN_TOP;
    public static final int ALIGN_BOTTOM = Element.ALIGN_BOTTOM;
    public static final int ALIGN_JUSTIFIED = Element.ALIGN_JUSTIFIED;

    private boolean borderTop = true;
    private boolean borderBottom = true;
    private boolean borderLeft = true;
    private boolean borderRight = true;

    private int paddingTop = DEFAULT_PADDING;
    private int paddingRight = DEFAULT_PADDING;
    private int paddingLeft = DEFAULT_PADDING;
    private int paddingBottom = DEFAULT_PADDING;

    private float borderWidth = 1f;

    private boolean bold;

    private Color backgroundColor = new Color(255, 255, 255);
    private Color borderColor = new Color(0, 0, 0);
    private Color textColor = new Color(0, 0, 0);

    private int hAlignment = ALIGN_CENTER;
    private int vAlignment = ALIGN_MIDDLE;
    private transient Font font = null;
    private float fontSize = DEFAULT_FONT_SIZE;

    public static final Color GRAY_BORDER = new Color(191, 191, 191);
    public static final Color GRAY_BACKGROUND = new Color(221, 221, 221);
    public static final Color BLACK = new Color(0, 0, 0);
    public static final Color WHITE = new Color(255, 255, 255);

    public PDFCellStyle(boolean border, int padding, boolean bold, Color bgColor, Color borderColor, Color textColor, int hAlign, int vAlign, float fontSize) throws Exception {
        this();
        setBorder(border);
        setPadding(padding);
        setBold(bold);
        setBackgroundColor(bgColor);
        setBorderColor(borderColor);
        setTextColor(textColor);
        sethAlignment(hAlign);
        setvAlignment(vAlign);
        setFontSize(fontSize);
    }

    public void setFontInfo(boolean bold, Color textColor, float fontSize) throws Exception {
        setBold(bold);
        setTextColor(textColor);
        setFontSize(fontSize);
        updateFont();
    }

    public void setAppearance(boolean border, int padding, Color bgColor, Color borderColor) throws Exception {
        setBorder(border);
        setPadding(padding);
        setBackgroundColor(bgColor);
        setBorderColor(borderColor);
    }

    public PDFCellStyle() throws Exception {
        updateFont();
    }

    private void updateFont() throws Exception {
        font = new Font(bold ? PDFFontsHelper.getBold() : PDFFontsHelper.getRegular(), fontSize, Font.NORMAL, textColor);
    }

    public void setBorders(boolean top, boolean bottom, boolean left, boolean right) {
        borderTop = top;
        borderBottom = bottom;
        borderLeft = left;
        borderRight = right;
    }

    public Color getBackgrounColor() {
        return backgroundColor;
    }

    public void setBorder(boolean border) {
        setBorders(border, border, border, border);
    }

    public void setPaddings(int top, int bottom, int left, int right) {
        paddingTop = top;
        paddingBottom = bottom;
        paddingLeft = left;
        paddingRight = right;
    }

    public void setPadding(int padding) {
        setPaddings(padding, padding, padding, padding);
    }

    public void setBold(boolean bold) throws Exception {
        this.bold = bold;
        updateFont();
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    public void setTextColor(Color textColor) throws Exception {
        this.textColor = textColor;
        updateFont();
    }

    public void sethAlignment(int hAlignment) {
        if (!isValidAlign(hAlignment)) {
            throw new RuntimeException("Alineación inválida.");
        }
        this.hAlignment = hAlignment;
    }

    public void setvAlignment(int vAlignment) {
        if (!isValidAlign(vAlignment)) {
            throw new RuntimeException("Alineación inválida.");
        }
        this.vAlignment = vAlignment;
    }

    public void setFontSize(float fontSize) throws Exception {
        this.fontSize = fontSize;
        updateFont();
    }

    public Color getBorderColor() {
        return borderColor;
    }
    
    public PdfPCell getCell(Object text) throws Exception {
        return getCell(text != null ? (text instanceof String ? (String) text : text.toString()) : "");
    }

    public PdfPCell getCell(String text) throws Exception {
        return getCell(text != null ? text : "", 1, 1);
    }

    public PdfPCell getCell(String text, int customHAlign) throws Exception {
        return getCell(text, 1, 1, customHAlign);
    }

    public PdfPCell getCell(String text, int colSpan, int rowSpan) throws Exception {
        return getCell(text, colSpan, rowSpan, -1);
    }

    public PdfPCell getCell(String text, int colSpan, int rowSpan, int customHAlign) throws Exception {
        if (font == null) {
            updateFont();
        }
        return getCell(new Paragraph(new Chunk(text, font)), null, colSpan, rowSpan, customHAlign);
    }

    /**
     * Crea una nueva celda y pone tbl en su interior
     *
     * @param tbl
     * @param colSpan
     * @param rowSpan
     * @return
     * @throws Exception
     */
    public PdfPCell getCell(PdfPTable tbl, int colSpan, int rowSpan) throws Exception {
        return getCell(null, tbl, colSpan, rowSpan, -1);
    }

    private PdfPCell getCell(Paragraph par, PdfPTable tab, int colSpan, int rowSpan, int customHAlign) throws Exception {
        if (!isValidAlign(customHAlign)) {
            throw new Exception("Alineación inválida.");
        }

        PdfPCell c = null;
        if (par != null) {
            c = new PdfPCell(par);
        } else if (tab != null) {
            c = new PdfPCell(tab);
        }

        if (c == null) {
            throw new RuntimeException("Debe agregar una tabla o un parrafo");
        }
        if (!backgroundColor.equals(Color.WHITE)) {
            c.setBackgroundColor(backgroundColor);
        }
        c.setBorderColor(borderColor);
        c.setPaddingTop(paddingTop);
        c.setPaddingBottom(paddingBottom);
        c.setPaddingLeft(paddingLeft);
        c.setPaddingLeft(paddingRight);

        c.setColspan(colSpan);
        c.setRowspan(rowSpan);
        c.setVerticalAlignment(vAlignment);
        c.setHorizontalAlignment(customHAlign != -1 ? customHAlign : hAlignment);

        c.setBorder((borderTop ? Rectangle.TOP : 0) + (borderBottom ? Rectangle.BOTTOM : 0) + (borderLeft ? Rectangle.LEFT : 0) + (borderRight ? Rectangle.RIGHT : 0));
        //c.setBorderWidth(borderWidth);

        return c;
    }

    public Paragraph getParagraph(String text, int customHAlign) throws Exception {
        if (!isValidAlign(customHAlign)) {
            throw new Exception("Alineación inválida.");
        }
        Paragraph par = new Paragraph(new Chunk(text, font));
        par.setAlignment(customHAlign != -1 ? customHAlign : hAlignment);
        par.setSpacingAfter(paddingTop);
        par.setSpacingBefore(paddingBottom);
        return par;
    }

    private boolean isValidAlign(int align) {
        return align == -1 || align == ALIGN_BOTTOM || align == ALIGN_CENTER || align == ALIGN_LEFT || align == ALIGN_MIDDLE || align == ALIGN_RIGHT || align == ALIGN_TOP || align == ALIGN_JUSTIFIED;
    }

    public Font getFont() throws Exception {
        if (font == null) {
            updateFont();
        }
        return font;
    }

    public float getBorderWidth() {
        return borderWidth;
    }

    public void setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth;
    }

    public PDFCellStyle copy() throws Exception {
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;

        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            oos.writeObject(this);
            oos.close();
            baos.close();
            byte[] data = baos.toByteArray();

            bais = new ByteArrayInputStream(data);
            ois = new ObjectInputStream(bais);
            Object obj = ois.readObject();
            bais.close();
            return (PDFCellStyle) obj;
        } finally {
            baos.close();
            oos.close();
            bais.close();
            ois.close();
        }
    }
}
