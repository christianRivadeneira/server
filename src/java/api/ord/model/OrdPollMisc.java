package api.ord.model;

import api.BaseModel;
import java.io.File;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class OrdPollMisc extends BaseModel<OrdPollMisc> {

    public static void getList(String query, Connection con) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
//inicio zona de reemplazo

    public Integer pollId;
    public Integer questionId;
    public Integer ordinal;
    public String text1;
    public String text2;
    public String text3;
    public String text4;
    public String text5;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "poll_id",
            "question_id",
            "ordinal",
            "text1",
            "text2",
            "text3",
            "text4",
            "text5"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, pollId);
        q.setParam(2, questionId);
        q.setParam(3, ordinal);
        q.setParam(4, text1);
        q.setParam(5, text2);
        q.setParam(6, text3);
        q.setParam(7, text4);
        q.setParam(8, text5);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        pollId = MySQLQuery.getAsInteger(row[0]);
        questionId = MySQLQuery.getAsInteger(row[1]);
        ordinal = MySQLQuery.getAsInteger(row[2]);
        text1 = MySQLQuery.getAsString(row[3]);
        text2 = MySQLQuery.getAsString(row[4]);
        text3 = MySQLQuery.getAsString(row[5]);
        text4 = MySQLQuery.getAsString(row[6]);
        text5 = MySQLQuery.getAsString(row[7]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_poll_misc";
    }

    public static String getSelFlds(String alias) {
        return new OrdPollMisc().getSelFldsForAlias(alias);
    }

    public static List<OrdPollMisc> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdPollMisc().getListFromQuery(q, conn);
    }
    
    public static List<OrdPollMisc> getListPollId(int pollId, Connection conn) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT " + getSelFlds("") + " FROM ord_poll_misc  WHERE poll_id = " + pollId + " ORDER BY ordinal");
        return new OrdPollMisc().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdPollMisc().deleteById(id, conn);
    }

    public static List<OrdPollMisc> getAll(Connection conn) throws Exception {
        return new OrdPollMisc().getAllList(conn);
    }

//fin zona de reemplazo
    public static String geFilesHome() throws Exception {
        String home = System.getProperty("user.home");
        if (!home.endsWith(File.separator)) {
            home += File.separator;
        }
        home = home + "Qualisys" + File.separator + "tmp" + File.separator;
        File f = new File(home);
        if (!f.exists() && !f.mkdirs()) {
            throw new Exception("No se pudo crear la carpeta de logs.\n" + home);
        }
        return home;
    }
}
