package utilities.dataTableHelper;

import java.util.Date;

@Deprecated
public class DataColumn {

    
    private String name;
    private String mask;
    private Class columnClass;
    private int width;
    private boolean resizable;

    public DataColumn() {
    }

    public DataColumn(String name, String mask, Class colClass, int width, boolean resizable) {
        this.name = name;
        this.columnClass = colClass;
        this.width = width;
        this.mask = mask;
        this.resizable = resizable;
    }

    public DataColumn(String name, String mask, Class colClass, int width) {
        this(name, mask, colClass, width, true);
    }

    public DataColumn(String name, Class colClass, int width) {
        this(name, null, colClass, width, true);
        if (colClass.equals(Date.class)) {
            throw new RuntimeException("Las columnas tipo fecha necesitan una máscara.");
        }
    }

    public DataColumn(String name, Class colClass, int width, boolean resizable) {
        this(name, null, colClass, width, resizable);
        if (colClass.equals(Date.class)) {
            throw new RuntimeException("Las columnas tipo fecha necesitan una máscara.");
        }
    }

    public Class getColumnClass() {
        return columnClass;
    }

    public void setColumnClass(Class columnClass) {
        this.columnClass = columnClass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMask() {
        return mask;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public boolean isResizable() {
        return resizable;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
    }
}
