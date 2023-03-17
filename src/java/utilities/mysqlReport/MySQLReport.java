package utilities.mysqlReport;

import api.MySQLCol;
import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import utilities.MySQLQuery;

public class MySQLReport {

    private String title;
    private List<String> subTitles = new ArrayList<>();
    private String sheetName;
    private List<Table> tables = new ArrayList<>();
    private List<CellFormat> formats = new ArrayList<>();
    private Date creation;
    private int horizontalFreeze = 0;
    private int verticalFreeze = 0;
    private int zoomFactor = 90;
    private boolean showNumbers = false;
    private boolean multiRowTitles = false;
    private Integer multiRowTitlesRows;

    public MySQLReport(String title, String subtitle, String sheetName, Date created) {
        this.title = title;
        if (subtitle != null) {
            if (!subtitle.isEmpty()) {
                this.subTitles.add(subtitle);
            }
        }

        if (sheetName != null && (!sheetName.trim().isEmpty())) {
            this.sheetName = sheetName.trim();
        } else {
            this.sheetName = "Hoja 1";
        }
        this.creation = created;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> nativeTables) {
        this.tables = nativeTables;
    }

    public List<CellFormat> getFormats() {
        return formats;
    }

    public void setFormats(List<CellFormat> formats) {
        this.formats = formats;
    }

    public List<String> getSubTitles() {
        return subTitles;
    }

    
    public void setSubTitles(List<String> subTitles) {
        this.subTitles = subTitles;
    }

    public Date getCreation() {
        return creation;
    }

    public void setCreation(Date creation) {
        this.creation = creation;
    }

    public int getHorizontalFreeze() {
        return horizontalFreeze;
    }

    public void setHorizontalFreeze(int horizontalFreeze) {
        this.horizontalFreeze = horizontalFreeze;
    }

    public int getVerticalFreeze() {
        return verticalFreeze;
    }

    public void setVerticalFreeze(int verticalFreeze) {
        this.verticalFreeze = verticalFreeze;
    }

    public int getZoomFactor() {
        return zoomFactor;
    }

    public void setZoomFactor(int zoomFactor) {
        this.zoomFactor = zoomFactor;
    }

    public boolean isShowNumbers() {
        return showNumbers;
    }

    public void setShowNumbers(boolean showNumbers) {
        this.showNumbers = showNumbers;
    }

    public boolean isMultiRowTitles() {
        return multiRowTitles;
    }

    public void setMultiRowTitles(boolean titleAutoHeigh) {
        this.multiRowTitles = titleAutoHeigh;
        if (!titleAutoHeigh) {
            this.multiRowTitlesRows = null;
        }
    }

    public void setMultiRowTitles(int rows) {
        this.multiRowTitles = true;
        this.multiRowTitlesRows = rows;
    }
    
     public void setMultiRowTitles3(int rows) {
        this.multiRowTitles = true;
        this.multiRowTitlesRows = rows;
    }

    public static MySQLReport getReport(MySQLReport rep, MySQLCol[] cols, Object[][] data) throws Exception {

        if (data != null && data.length == 0) {
            throw new Exception("No hay datos que mostrar.");
        }

        int totalWidth = 0;
        List<MySQLCol> visCols = new ArrayList<>();
        List<Integer> removeRows = new ArrayList<>();

        for (int i = 0; i < cols.length; i++) {
            MySQLCol col = cols[i];
            if (col.getType() != MySQLCol.TYPE_ICON && col.getType() != MySQLCol.TYPE_KEY) {
                visCols.add(col);
                totalWidth += col.width;
            } else {
                removeRows.add(i);
            }
        }

        Object[][] newData = removeRows(data, removeRows);
        Table tbl = rep.getTables().get(0);

        CellFormat cf = new CellFormat();

        for (int i = 0; i < visCols.size(); i++) {
            MySQLCol col = visCols.get(i);
            cf = new CellFormat();
            cf.setWrap(false);

            if (MySQLCol.isDate(col.getType()) || MySQLCol.isTime(col.getType())) {
                cf.setFormat(MySQLCol.getFormat(col.type));
                cf.setAlign(4);
                cf.setType(MySQLReportWriter.DATE);
            } else if (MySQLCol.isDecimal(col.getType())) {
                cf.setFormat(MySQLCol.getFormat(col.type));
                cf.setAlign(5);
                cf.setType(MySQLReportWriter.NUMBER);
            } else if (MySQLCol.isInteger(col.getType())) {
                cf.setFormat(MySQLCol.getFormat(col.type));
                cf.setAlign(5);
                cf.setType(MySQLReportWriter.NUMBER);
            } else if (MySQLCol.TYPE_TEXT == col.getType()) {
                cf.setAlign(4);
                cf.setType(MySQLReportWriter.LABEL);
                cf.setWrap(true);
            } else if (MySQLCol.TYPE_BOOLEAN == col.getType()) {
                cf.setAlign(4);
                cf.setType(MySQLReportWriter.LABEL);
            } else if (MySQLCol.TYPE_ENUM == col.getType()) {
                cf.setAlign(4);
                cf.setType(MySQLReportWriter.ENUM);
            } else {
                throw new Exception("Unsupported: " + col.getType());
            }
            rep.getFormats().add(cf);

            Column nc = new Column();
            nc.setFormat(cf.getType());
            nc.setWidth((int) (col.width / ((double) totalWidth) * (190 - 10)));
            nc.setName(col.name);
            tbl.getColumns().add(nc);
        }

        for (int i = 0; i < visCols.size(); i++) {
            for (Object[] dataRow : newData) {
                Object obj = dataRow[i];
                switch (visCols.get(i).getType()) {
                    case MySQLCol.TYPE_BOOLEAN:
                        if (obj == null) {
                            dataRow[i] = ("No");
                        } else if (obj instanceof Boolean) {
                            dataRow[i] = (((Boolean) obj) ? "Si" : "No");
                        } else if (obj instanceof Long) {
                            dataRow[i] = (((Long) obj) != 0 ? "Si" : "No");
                        } else if (obj instanceof Integer) {
                            dataRow[i] = (((Integer) obj) != 0 ? "Si" : "No");
                        } else {
                            throw new RuntimeException("Se esperaba Boolean o Long pero se encontró " + obj.getClass().toString());
                        }
                        break;
                    case MySQLCol.TYPE_ENUM:
                        Map<String, String> hm = MySQLQuery.getEnumOptAsMap(visCols.get(i).enumOpts);
                        dataRow[i] = ((HashMap) hm).get(obj);
                        break;
                    default:
                        dataRow[i] = obj;
                        break;
                }
            }
        }
        tbl.setData(newData);
        return rep;
    }

    public static Object[][] removeRows(Object[][] origData, List<Integer> rowsIdx) {
        int newSize = origData[0].length - rowsIdx.size();
        Object[][] newData = new Object[origData.length][newSize];
        int p = 0;
        for (int i = 0; i < origData.length; ++i) {
            int q = 0;
            for (int j = 0; j < origData[0].length; ++j) {
                if (rowsIdx.contains(j)) {
                    continue;
                }
                newData[p][q] = origData[i][j];
                ++q;
            }
            ++p;
        }
        return newData;
    }

    public File write(Connection conn) throws Exception {
        File f = File.createTempFile("rpt", ".xls");
        MySQLReportWriter.write(new MySQLReport[]{this}, f, conn);
        return f;
    }

    /**
     * @return the multiRowTitlesRows
     */
    public Integer getMultiRowTitlesRows() {
        return multiRowTitlesRows;
    }
}
