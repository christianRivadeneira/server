package api.sys.model;

import java.util.Date;
import java.util.GregorianCalendar;
import utilities.MySQLQuery;

public class TokenThPoll {

    public static final String KEY = "RsH3uT9b";

    public String p;
    public String t;
    public int ipe;
    public Date ed;
    public int ld;

    public String serialize() {
        return p + "@" + t + "@" + ipe + "@" + ed + "@" + ld;
    }

    public TokenThPoll deserialize(String s) {
        String[] parts = s.split("@");
        p = parts[0];
        t = parts[1];
        ipe = MySQLQuery.getAsInteger(parts[2]);
        ed = MySQLQuery.getAsDate(parts[3]);
        ld = MySQLQuery.getAsInteger(parts[4]);
        return this;
    }

    public boolean validateToken() {
        int limitDays = ld;
        Date expeditionDt = ed;

        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(expeditionDt);
        gc.add(GregorianCalendar.DAY_OF_MONTH, limitDays);

        GregorianCalendar curDate = new GregorianCalendar();
        curDate.setTime(new Date());        
        return curDate.before(gc);
    }

}
