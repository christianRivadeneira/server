package api.sys.model;

import controller.system.LoginController;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import model.menu.Credential;

public class LoginRequest {

    public String login;
    public String pass;
    public String type;
    public String extras;
    public String phone;
    public String pack;
    public boolean returnToken;
    public String poolName;
    public String tz;

    public Credential toCredential(ServletContext c, HttpServletRequest sr) throws Exception {
        return LoginController.getByCredentials(c, login, pass, type, extras, phone, pack, sr, returnToken, null, poolName, tz, false);
    }

}
