package utilities;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import web.fileManager;

public class FileUtils {

    public enum LogoType {
        ICON_WEB(1),
        ICON_DESK(2),
        ICON_PANEL(3),
        ICON_CAL(4),
        ICON_REPORT(5),
        ICON_ALTERNATIVE(6);

        private final int value;

        private LogoType(int value) {
            this.value = value;
        }
    };

    public static byte[] getEnterpriseLogo(Connection conn, LogoType logoType) throws Exception {
        //en esta consulta no se debe colocar sigma. hardcoded porque al hacerlo se quitan los logos en linode
        Integer bfileId = new MySQLQuery("SELECT id FROM bfile WHERE "
                + "owner_type = " + 29 + " AND owner_id = " + logoType.value).getAsInteger(conn);

        if (bfileId != null) {
            File file = new fileManager.PathInfo(conn).getExistingFile(MySQLQuery.getAsInteger(bfileId));
            if (file != null) {
                InputStream is = new FileInputStream(file);
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                int nRead;
                byte[] dataImage = new byte[16384];

                while ((nRead = is.read(dataImage, 0, dataImage.length)) != -1) {
                    buffer.write(dataImage, 0, nRead);
                }
                buffer.flush();
                return buffer.toByteArray();
            } else {
                return Reports.readInputStreamAsBytes(getEmptyImage());
            }
        } else {
            return Reports.readInputStreamAsBytes(getEmptyImage());
        }
    }

    private static InputStream getEmptyImage() throws IOException {
        return FileUtils.class.getResourceAsStream("./empty.png");
    }

}
