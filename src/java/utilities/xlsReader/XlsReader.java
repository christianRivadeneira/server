package utilities.xlsReader;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import jxl.CellType;
import jxl.DateCell;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import utilities.shrinkfiles.FileTypes;

public class XlsReader {

    public static XlsData readExcel(String path, int sheet) throws Exception {
        TimeZone def = TimeZone.getDefault();
        WorkbookSettings ws = new WorkbookSettings();
        ws.setLocale(new Locale("es", "es"));
        ws.setEncoding("windows-1252");
        if (FileTypes.getType(new File(path)) != FileTypes.OLD_MS_OFFICE && (new File(path)).isFile()) {
            throw new Exception("Tipo de archivo incorrecto, se esparaba:\n- Libro de Excel 97-2003 (*.xls)");
        }

        Workbook wb = Workbook.getWorkbook(new File(path), ws);
        Sheet sh = wb.getSheet(sheet);
        List<Object[]> res = new ArrayList<Object[]>();
        List<Integer> rows = new ArrayList<Integer>();
        //recorriendo
        for (int i = 0; i < sh.getRows(); i++) {

            List<Object> row = new ArrayList<Object>();
            for (int j = 0; j < sh.getRow(i).length; j++) {
                CellType type = sh.getRow(i)[j].getType();
                if (type.equals(CellType.LABEL)) {
                    row.add(sh.getRow(i)[j].getContents());
                } else if (type == CellType.NUMBER) {
                    String number = sh.getRow(i)[j].getContents();

                    if (number.length() >= 19) {
                        throw new Exception("Longitud de celda no soportada \n " + number);
                    }

                    Double dNum = ((NumberCell) sh.getRow(i)[j]).getValue();
                    long l = dNum.longValue();
                    int in = dNum.intValue();
                    if (dNum == in) {
                        row.add(in);
                    } else if (dNum == l) {
                        row.add(l);
                    } else {
                        row.add(dNum);
                    }
                } else if (type == CellType.DATE) {
                    DateCell dc = (DateCell) sh.getRow(i)[j];
                    row.add(new Date(dc.getDate().getTime() - def.getRawOffset() - def.getDSTSavings()));
                } else if (type == CellType.EMPTY) {
                    row.add(null);
                } else if (type == CellType.NUMBER_FORMULA) {
                    row.add(((NumberCell) sh.getRow(i)[j]).getValue());
                } else {
                    throw new Exception("Tipo no soportado en [" + (i + 1) + "," + (j + 1) + "] de la Hoja " + (sheet + 1) + ": " + type);
                }
            }
            rows.add(i + 1);
            res.add(row.toArray());
        }
        wb.close();
        XlsData xls = new XlsData();
        xls.setData(res.toArray(new Object[0][]));
        xls.setRows(rows.toArray(new Integer[0]));
        return xls;
    }

    public static XlsData readExcel(String path, Class[] classes, boolean ignoreFirst) throws Exception {
        WorkbookSettings ws = new WorkbookSettings();
        ws.setLocale(new Locale("es", "es"));
        ws.setEncoding("windows-1252");

        if (FileTypes.getType(new File(path)) != FileTypes.OLD_MS_OFFICE && (new File(path)).isFile()) {
            throw new Exception("Tipo de archivo incorrecto, se esparaba:\n- Libro de Excel 97-2003 (*.xls)");
        }

        Workbook wb = Workbook.getWorkbook(new File(path), ws);
        if (wb.getSheets().length != 1) {
            throw new Exception("El libro debe tener una sola hoja.");
        }
        int cols = classes.length;
        Sheet sh = wb.getSheet(0);

        //identificar columnas visibles
        boolean[] visCols = new boolean[sh.getColumns()];
        for (int i = 0; i < sh.getColumns(); i++) {
            if (!sh.getColumnView(i).isHidden() && sh.getColumnView(i).getSize() > 0) {
                boolean empty = true;
                for (int j = 0; j < sh.getColumn(i).length && empty; j++) {
                    if ((!sh.getColumn(i)[j].getContents().trim().isEmpty()) && (!sh.getColumn(i)[j].isHidden())) {
                        empty = false;
                    }
                }
                visCols[i] = !empty;
            } else {
                visCols[i] = false;
            }
        }

        int visColCount = 0;
        for (int i = 0; i < visCols.length; i++) {
            if (visCols[i]) {
                visColCount++;
            }
        }
        if (visColCount != cols) {
            throw new Exception("Se esperaban " + cols + " columnas visibles, pero hay " + visColCount + ".");
        }

        //identificar filas visibles
        boolean[] visRows = new boolean[sh.getRows()];
        for (int i = 0; i < sh.getRows(); i++) {
            boolean empty = true;
            for (int j = 0; j < sh.getRow(i).length && empty; j++) {
                if (visCols[j]) {
                    if ((!sh.getRow(i)[j].getContents().trim().isEmpty()) && (!sh.getRow(i)[j].isHidden())) {
                        empty = false;
                    }
                }
            }
            visRows[i] = !empty;
        }

        int visRowCount = 0;
        for (int i = 0; i < visRows.length; i++) {
            if (visRows[i]) {
                visRowCount++;
            }
        }
        if (visRowCount == 0) {
            throw new Exception("La hoja no contiene filas visibles.");
        }
        //buscando la primera fila completa
        int firstRow = -1;
        for (int i = 0; i < sh.getRows() && firstRow < 0; i++) {
            if (visRows[i]) {
                int rowLength = 0;
                for (int j = 0; j < sh.getRow(i).length; j++) {
                    if (visCols[j]) {
                        if (!sh.getRow(i)[j].getContents().trim().isEmpty()) {
                            rowLength++;
                        }
                    }
                }
                if (rowLength == cols) {
                    firstRow = i;
                }
            }
        }

        List<Object[]> res = new ArrayList<Object[]>();
        List<Integer> rows = new ArrayList<Integer>();
        //recorriendo
        if (ignoreFirst) {
            firstRow++;
        }
        for (int i = firstRow; i < sh.getRows(); i++) {
            if (visRows[i]) {
                List<Object> row = new ArrayList<Object>();
                for (int j = 0, k = 0; j < sh.getRow(i).length; j++) {
                    if (visCols[j]) {
                        CellType type = sh.getRow(i)[j].getType();
                        if (classes[k].equals(String.class)) {
                            if (type == CellType.LABEL) {
                                row.add(sh.getRow(i)[j].getContents());
                            } else if (type == CellType.NUMBER) {
                                row.add(sh.getRow(i)[j].getContents());
                            } else if (type == CellType.DATE) {
                                row.add(sh.getRow(i)[j].getContents());
                            } else if (type == CellType.EMPTY) {
                                row.add("");
                            } else {
                                throw new RuntimeException("Tipo no soportado " + type.toString() + ", fila " + (i + 1) + ", columna " + (j + 1));
                            }
                        } else if (classes[k].equals(Double.class)) {
                            if (type == CellType.LABEL) {
                                try {
                                    row.add(Double.valueOf(sh.getRow(i)[j].getContents()));
                                } catch (NumberFormatException ex) {
                                    throw new Exception("No se pudo convertir '" + sh.getRow(i)[j].getContents() + "' en número, fila " + (i + 1) + ", columna " + (j + 1));
                                }
                            } else if (type == CellType.NUMBER) {
                                row.add(((NumberCell) sh.getRow(i)[j]).getValue());
                            } else if (type == CellType.DATE) {
                                throw new RuntimeException("No puede convertir una fecha en número, fila " + (i + 1) + ", columna " + (j + 1));
                            } else if (type == CellType.EMPTY) {
                                row.add(0d);
                            } else {
                                throw new RuntimeException("Tipo no soportado " + type.toString() + ", fila " + (i + 1) + ", columna " + (j + 1));
                            }
                        } else if (classes[k].equals(Date.class)) {
                            if (type == CellType.LABEL) {
                                throw new RuntimeException("Tipo no puede convertir un texto en fecha, fila " + (i + 1) + ", columna " + (j + 1));
                            } else if (type == CellType.NUMBER) {
                                throw new RuntimeException("Tipo no puede convertir un número en fecha, fila " + (i + 1) + ", columna " + (j + 1));
                            } else if (type == CellType.DATE) {
                                row.add(((DateCell) sh.getRow(i)[j]).getDate());
                            } else if (type == CellType.EMPTY) {
                                row.add(null);
                            } else {
                                throw new RuntimeException("Tipo no soportado " + type.toString());
                            }
                        }
                        k++;
                    }
                }
                if (row.size() != cols) {
                    throw new Exception("Se esperaban " + cols + " registros en la fila " + (i + 1) + ", hay " + row.size() + ".");
                }
                for (int j = 0; j < classes.length; j++) {
                    if (!classes[j].isInstance(row.get(j))) {
                        throw new Exception("La columna " + (j + 1) + " debería ser " + getClassDesc(classes[j]) + " y es " + getClassDesc(row.get(j).getClass()) + " en la fila " + (i + 1) + ".");
                    }
                }
                rows.add(i + 1);
                res.add(row.toArray());
            }
        }
        wb.close();
        XlsData xls = new XlsData();
        xls.setData(res.toArray(new Object[0][]));
        xls.setRows(rows.toArray(new Integer[0]));
        return xls;
    }

    private static String getClassDesc(Class c) {
        if (c.equals(String.class)) {
            return "Texto";
        }
        if (c.equals(Double.class)) {
            return "Número";
        }
        if (c.equals(Date.class)) {
            return "Fecha";
        }
        return c.getName();
    }

    public static Sheet[] gethSheets(String path) throws Exception {
        WorkbookSettings ws = new WorkbookSettings();
        ws.setLocale(new Locale("es", "es"));
        ws.setEncoding("windows-1252");
        Workbook wb = Workbook.getWorkbook(new File(path), ws);
        Sheet[] r = wb.getSheets();
        wb.close();
        return r;
    }

}
