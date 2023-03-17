package web;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ShortException extends Exception {

    private String msg;

    public ShortException() {
    }

    public ShortException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public ShortException(Exception ex) {
        super(ex);
        this.msg = ex.getMessage();
        this.msg = (msg != null && !msg.isEmpty() ? msg : ex.getClass().getSimpleName());
    }

    @Override
    public String getMessage() {
        return msg;
    }

    public void simplePrint() {
        StackTraceElement[] stack = getStackTrace();
        StringBuilder sb = new StringBuilder();
        if (stack != null && stack.length > 0) {
            sb.append(getMessage());
            sb.append(" (");
            int lines = Math.min(stack.length, 3);
            for (int i = 0; i < lines; i++) {
                StackTraceElement e = stack[i];
                sb.append(e.getFileName()).append(": ").append(e.getLineNumber());
                if (i < lines - 1) {
                    sb.append(", ");
                }
            }
            sb.append(")");
        }
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, sb.toString());
    }

    public String getSimpleStack() {
        StackTraceElement[] stack = getStackTrace();
        StringBuilder sb = new StringBuilder();
        if (stack != null && stack.length > 0) {
            sb.append(getMessage());
            sb.append(" (");
            int lines = Math.min(stack.length, 6);
            for (int i = 0; i < lines; i++) {
                StackTraceElement e = stack[i];
                sb.append(e.getFileName()).append(": ").append(e.getLineNumber());
                if (i < lines - 1) {
                    sb.append(", ");
                }
            }
            sb.append(")");
        }
        return sb.toString();
    }
}
