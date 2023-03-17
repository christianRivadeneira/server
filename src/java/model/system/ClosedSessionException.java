package model.system;

import web.ShortException;

public class ClosedSessionException extends ShortException {

    public ClosedSessionException() {
        super("Sesión finalizada por inactividad.\nReinicie el aplicativo.");
    }
}
