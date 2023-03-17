package utilities.pdf;

import java.sql.Connection;
import java.util.Date;
import model.quality.CalCfg;
import utilities.MySQLQuery;

public class PDFQualityHeader extends ModelPDFQualityHeader {

    public PDFQualityHeader() throws Exception {
        super();
    }

    public PDFQualityHeader(Connection ep, String tittle) throws Exception {
        super(ep, tittle);
    }

    public PDFQualityHeader(Connection ep, int formatId, String subTitle) throws Exception {
        super(ep);
        Object[] row = new MySQLQuery("SELECT title, effect, version, code FROM cal_format WHERE id = " + formatId).getRecord(ep);
        CalCfg cfg = CalCfg.getCfg(ep);
        this.subTitle = subTitle;
        this.title = row[0].toString();
        this.valid = (Date) row[1];
        this.version = (Integer) row[2];
        this.code = row[3].toString();
        this.justTitle = false;
        this.showFooter = cfg.showFooter;
    }

    public PDFQualityHeader(Connection ep, int formatId) throws Exception {
        this(ep, formatId, null);
    }
}
