package utilities.shrinkfiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Este archivo está en cliente y servidor, mantener sincronizado.
 *
 * @author Mario
 */
public class FileTypes {

    //si llega un patrón con más de 8 elementos se debe actualizar head = new byte[8]; en getTypeType
    private static final byte[] PDF_PATTERN = new byte[]{37, 80, 68, 70, 45};
    private static final byte[] JPG_PATTERN = new byte[]{-1, -40, -1};
    private static final byte[] PNG_PATTERN = new byte[]{-119, 80, 78, 71, 13};
    private static final byte[] TIF1_PATTERN = new byte[]{0x4d, 0x4d, 0x00, 0x2a};
    private static final byte[] TIF2_PATTERN = new byte[]{0x49, 0x49, 0x2a, 0x00};
    private static final byte[] ZIP_PATTERN = new byte[]{0x50, 0x4B, 0x03, 0x04};
    private static final byte[] BMP_PATTERN = new byte[]{0x42, 0x4d};
    private static final byte[] GIF_PATTERN = new byte[]{0x47, 0x49, 0x46, 0x38};
    private static final byte[] RAR_PATTERN = new byte[]{0x52, 0x61, 0x72, 0x21, 0x1A, 0x07};
    private static final byte[] OLD_MS_OFFICE_PATTERN = new byte[]{-48, -49, 17, -32, -95, -79, 26, -31};

    public static void main(String[] args) {
        int[] bytes = new int[]{0xD0, 0xCF, 0x11, 0xE0, 0xA1, 0xB1, 0x1A, 0xE1};
        for (int i = 0; i < bytes.length; i++) {
            System.out.print((byte) bytes[i]);
            System.out.print(", ");
        }
    }

    public static final int UNK = -1;
    public static final int PDF = 1;
    public static final int JPG = 2;
    public static final int PNG = 3;
    public static final int TIF = 4;
    public static final int ZIP = 5;
    public static final int DOCX = 6;
    public static final int BMP = 7;
    public static final int GIF = 8;
    public static final int RAR = 9;
    public static final int OLD_MS_OFFICE = 10;
    public static final int XLSX = 11;

    /**
     * Retorna el tipo del archivo según la lista de constantes públicas de la
     * clase
     *
     * @param f
     * @return tipo, cómo UNK, PDF, JPG, etc.
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static int getType(File f) throws FileNotFoundException, IOException {
        byte[] head;
        try (FileInputStream fis = new FileInputStream(f)) {
            head = new byte[8];
            fis.read(head);
        }

        if (match(PDF_PATTERN, head)) {
            return PDF;
        } else if (match(JPG_PATTERN, head)) {
            return JPG;
        } else if (match(PNG_PATTERN, head)) {
            return PNG;
        } else if (match(BMP_PATTERN, head)) {
            return BMP;
        } else if (match(GIF_PATTERN, head)) {
            return GIF;
        } else if (match(TIF1_PATTERN, head)) {
            return TIF;
        } else if (match(TIF2_PATTERN, head)) {
            return TIF;
        } else if (match(OLD_MS_OFFICE_PATTERN, head)) {
            return OLD_MS_OFFICE;
        } else if (match(RAR_PATTERN, head)) {
            return RAR;
        } else if (match(ZIP_PATTERN, head)) {
            List<String> ze = getZipEntries(f);
            if (ze.contains("word/document.xml")) {
                return DOCX;
            } else if (ze.contains("xl/workbook.xml")) {
                return XLSX;
            }
            return ZIP;
        } else {
            return UNK;
        }
    }

    private static List<String> getZipEntries(File f) throws IOException {
        List<String> rta = new ArrayList<>();
        try (ZipFile zipFile = new ZipFile(f)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                rta.add(entry.getName());
            }
        }
        return rta;
    }

    private static byte[] extract(File f) throws FileNotFoundException, IOException {
        byte[] head;
        try (FileInputStream fis = new FileInputStream(f)) {
            head = new byte[5];
            fis.read(head);
        }
        return head;
    }

    private static void open(File f, List<File> list) {
        if (f.isDirectory()) {
            File[] files = f.listFiles();
            if (files != null) {
                for (File file : files) {
                    open(file, list);
                }
            }
        } else if (f.isFile()) {
            if (!list.contains(f) && !f.isHidden()) {
                if (f.getAbsolutePath().toLowerCase().endsWith(".docx")) {
                    list.add(f);
                }
            }
        }
    }

    private static boolean match(byte[] pattern, byte[] file) {
        int l = Math.min(pattern.length, file.length);
        if (l == 0) {
            return false;
        }
        for (int i = 0; i < l; i++) {
            if (pattern[i] != file[i]) {
                return false;
            }
        }
        return true;
    }
}
