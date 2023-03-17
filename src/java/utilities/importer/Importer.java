package utilities.importer;

import java.util.List;

public class Importer {

    public List<ImporterCol> cols;
    public int headRow = -1;
    public Object[] row;

    public Importer(Object[][] data, List<ImporterCol> cols) throws Exception {
        this.cols = cols;

        for (int i = 0; i < data.length; i++) {
            if (isHeader(data[i], cols.get(0).name)) {
                headRow = i;
                break;
            }
        }
        if (headRow == -1) {
            throw new Exception("El archivos seleccionado no contiene las columnas esperadas.");
        }

        setHeadPosition(data[headRow], cols);

    }

    private boolean isHeader(Object[] row, String firstCol) {
        for (Object cell : row) {
            String s = normalize(cell);
            if (in(s, firstCol)) {
                return true;
            }
        }
        return false;
    }

    private boolean in(Object header, Object... lst) {
        String headerStr = normalize(header);
        for (Object e : lst) {
            if (headerStr.equals(normalize(e))) {
                return true;
            }
        }
        return false;
    }

    private String normalize(Object o) {
        return o != null ? o.toString().toUpperCase().replaceAll("[^A-Z]", " ").replaceAll("\\s+", " ").trim() : "";
    }

    public void validateValues(Object[] row, int rowPos) throws Exception {
        for (int i = 0; i < cols.size(); i++) {
            if (row[i] == null && !cols.get(i).isNullable) {
                throw new Exception("No se encontró contenido de \""
                        + ImporterCol.getColTypeName(cols.get(i).type)
                        + "\" en la Fila: " + rowPos + " Columna: " + i);
            }
        }
    }

    private void setHeadPosition(Object[] headerRow, List<ImporterCol> cols) throws Exception {

        int rowCol = 0;
        for (int i = 0; i < headerRow.length; i++) {
            if (rowCol < cols.size() && in(headerRow[i], cols.get(rowCol).name) && cols.get(rowCol).pos == -1) {
                cols.get(rowCol).pos = i;
                rowCol++;
            }
        }

        for (int i = 0; i < cols.size(); i++) {
            if (cols.get(i).pos == -1) {
                throw new Exception("Falta la columna " + cols.get(i).name);
            }
        }
    }

    public boolean isAllWhite() {
        for (Object cell : row) {
            if (cell != null && !cell.toString().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public Object get(int i) {
        int pos = cols.get(i).pos;
        if (pos < row.length) {
            return row[pos];
        } else {
            return null;
        }
    }

}
