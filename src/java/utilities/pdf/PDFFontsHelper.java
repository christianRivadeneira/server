package utilities.pdf;

import com.lowagie.text.pdf.BaseFont;
import java.io.File;
import java.io.FileOutputStream;
import utilities.Reports;

public class PDFFontsHelper {

    private static File normFile;
    private static File boldFile;

    private static BaseFont normFont;
    private static BaseFont boldFont;

    public static BaseFont getRegular() throws Exception {
        if ((normFile == null || !normFile.exists()) || normFont == null) {
            normFile = File.createTempFile("MyriadWebPro", ".ttf");
            try (FileOutputStream fos = new FileOutputStream(normFile)) {
                fos.write(Reports.readInputStreamAsBytes(PDFFontsHelper.class.getResourceAsStream("./MyriadWebPro.ttf")));
                normFont = BaseFont.createFont(normFile.getAbsolutePath(), BaseFont.CP1252, BaseFont.EMBEDDED);
            }
        }
        return normFont;
    }

    public static BaseFont getBold() throws Exception {
        if ((boldFile == null || !boldFile.exists()) || boldFont == null) {
            boldFile = File.createTempFile("MyriadWebPro-Bold", ".ttf");
            try (FileOutputStream fos = new FileOutputStream(boldFile)) {
                fos.write(Reports.readInputStreamAsBytes(PDFFontsHelper.class.getResourceAsStream("./MyriadWebPro-Bold.ttf")));
                boldFont = BaseFont.createFont(boldFile.getAbsolutePath(), BaseFont.CP1252, BaseFont.EMBEDDED);
            }
        }
        return boldFont;
    }
}
