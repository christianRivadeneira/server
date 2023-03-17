package model.system;

public class AuthenticationException extends Exception {

    public AuthenticationException(String msg) {
        super(msg);
    }
    
    public AuthenticationException(Exception ex) {
        super(ex);
    }
}
