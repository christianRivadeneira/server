package api.ord.model;

import api.BaseModel;
import java.io.File;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class OrdTextPoll extends BaseModel<OrdTextPoll> {

    public static void getList(String query, Connection con) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
//inicio zona de reemplazo

    public Integer pollId;
    public Integer ordinal;
    public String text;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "poll_id",
            "ordinal",
            "text"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, pollId);
        q.setParam(2, ordinal);
        q.setParam(3, text);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        pollId = MySQLQuery.getAsInteger(row[0]);
        ordinal = MySQLQuery.getAsInteger(row[1]);
        text = MySQLQuery.getAsString(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_text_poll";
    }

    public static String getSelFlds(String alias) {
        return new OrdTextPoll().getSelFldsForAlias(alias);
    }

    public static List<OrdTextPoll> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdTextPoll().getListFromQuery(q, conn);
    }

    public static List<OrdTextPoll> getListPollId(int pollId, Connection conn) throws Exception {
        MySQLQuery q = new MySQLQuery("SELECT " + getSelFlds("") + " FROM ord_text_poll WHERE poll_id = " + pollId + " ORDER BY ordinal");
        return new OrdTextPoll().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdTextPoll().deleteById(id, conn);
    }

    public static List<OrdTextPoll> getAll(Connection conn) throws Exception {
        return new OrdTextPoll().getAllList(conn);
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

    public static void deletePreviousPollAnswers(Connection con, int pollId) throws Exception {
        new MySQLQuery("DELETE FROM ord_text_poll WHERE poll_id = ?1 ").setParam(1, pollId).executeDelete(con);
    }

    public static void registerPollAnswers(Connection con, OrdPoll poll, List<OrdTextPoll> ordTextPollList) throws Exception {
        for (OrdTextPoll textPoll : ordTextPollList) {
            textPoll.pollId = poll.id;
            textPoll.insert(con);
        }
    }

    public static List<OrdTextPoll> getAllByPollId(Connection con, int pollId) throws Exception {

        MySQLQuery q = new MySQLQuery("SELECT " + getSelFlds("") + " "
                + "FROM ord_text_poll "
                + "WHERE poll_id = ?1 "
                + "ORDER BY ordinal ").setParam(1, pollId);

        return OrdTextPoll.getList(q, con);
    }
}
