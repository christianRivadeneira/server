package api.dto.importer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class CSVReader {

    public static Object[][] read(File f, String fieldSeparator, Class[] cols) throws Exception {
        return read(f, Charset.defaultCharset(), fieldSeparator, cols);
    }

    public static Object[][] read(File f, Charset charset, String fieldSeparator, Class[] cols) throws Exception {
        List<Object[]> rows = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f), charset))) {
            String line;
            int nrow = 0; // numero de fila
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(fieldSeparator);
                Object[] row = new Object[parts.length];
                if (parts.length > cols.length) {
                    throw new Exception("Número de columnas inesperado. Fila  " + nrow);
                }
                rows.add(row);
                for (int i = 0; i < parts.length; i++) {
                    String part = parts[i];
                    if (cols[i].equals(String.class)) {
                        row[i] = part;
                    } else {
                        throw new Exception("Clase no soportada. " + cols[i].getName());
                    }
                }
                nrow++;
            }
        }
        return rows.toArray(new Object[0][]);
    }

}
