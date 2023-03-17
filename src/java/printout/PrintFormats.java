package printout;

import java.io.File;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PrintFormats {

    public static File Print(Connection ep, Integer registId, String className) throws Exception {
        File f = null;
        if (className == null) {
            className = PrintFormatGral.class.getCanonicalName();
        }

        try {
            Object obj = Class.forName(className).newInstance();
            if (obj instanceof PrintFormatsGenerator) {
                PrintFormatsGenerator typeFormat = (PrintFormatsGenerator) obj;
                f = typeFormat.initFormat(ep, registId);
            } else {
                throw new Exception("La clase espeficicada " + className + ", no extiende de PrintFormats");
            }
        } catch (Exception e) {
            Logger.getLogger(PrintFormats.class.getName()).log(Level.SEVERE, null, e);
        }

        return f;
    }
}
