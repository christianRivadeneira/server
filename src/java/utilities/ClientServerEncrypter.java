package utilities;

public class ClientServerEncrypter extends DesEncrypter {

    public ClientServerEncrypter(String remoteAddress) throws Exception {
        super(remoteAddress.equals("127.0.0.1") || remoteAddress.equals("0:0:0:0:0:0:0:1") ? "-1" : "-443713");
    }
}
