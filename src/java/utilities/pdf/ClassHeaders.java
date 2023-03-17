package utilities.pdf;

import com.lowagie.text.pdf.PdfPageEventHelper;
import java.sql.Connection;
import api.sys.model.SysCfg;

public class ClassHeaders {

    public static PdfPageEventHelper returnPageEvent(Connection ep, Integer formatId, String subtitle, boolean hasSub) throws Exception {
        return returnPageEvent(ep, formatId, null, subtitle, hasSub);
    }

    public static PdfPageEventHelper returnPageEvent(Connection ep, Integer formatId, String serial, String subtitle, boolean hasSub) throws Exception {
        SysCfg cfg = SysCfg.select(ep);
        String className = cfg.classPdfHeaderBack;

        if (className == null) {
            className = PDFQualityHeader.class.getCanonicalName();
        }
        Object obj = Class.forName(className).newInstance();
        if (obj instanceof PDFQualityHeader) {
            return new PDFQualityHeader(ep, formatId, (hasSub ? subtitle : null));
        } else if (obj instanceof PDFHeaderMg) {
            return new PDFHeaderMg(ep, formatId, subtitle, serial);
        } else {
            throw new Exception("La clase espeficicada " + className + ", no extiende de PdfPageEventHelper");
        }
    }
}
