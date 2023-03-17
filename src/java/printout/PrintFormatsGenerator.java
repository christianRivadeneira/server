package printout;

import com.lowagie.text.Document;
import java.io.File;
import java.io.OutputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;

public abstract class PrintFormatsGenerator {

    public final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");    
    public Connection ep;
    public Document document;
    public File fin;    
    public OutputStream os = null;

    public abstract File initFormat(Connection ep, Integer registId) throws Exception;

}
