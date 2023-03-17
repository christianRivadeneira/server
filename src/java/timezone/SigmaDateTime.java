package timezone;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SigmaDateTime implements Serializable {

    private static final SimpleDateFormat dateTimeFormatNs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private String str = "";

    static final long serialVersionUID = -3604981652949132723L;

    public SigmaDateTime(Date d) {
        str = (d != null ? dateTimeFormatNs.format(d) : "");
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeUTF(str);
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        str = ois.readUTF();
    }

    public Date getAsDate() throws Exception {
        return (str.isEmpty() ? null : dateTimeFormatNs.parse(str));
    }
}
