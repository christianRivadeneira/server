package web.polling;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Question {

    public int id;
    public String txt;
    public int score;

    public Question(ResultSet rs) throws Exception {
        id = rs.getInt(1);
        txt = rs.getString(2);
        score = rs.getInt(3);
    }

    public static Question[] getQuestions(Connection conn, String type, int questionId) throws Exception {
        Statement st = conn.createStatement();
        List<Question> rta = new ArrayList<Question>();
        ResultSet rs = st.executeQuery("SELECT id, txt, score FROM cal_poll_option WHERE type = '" + type + "' AND question_id = " + questionId + " ORDER BY place ASC");
        while (rs.next()) {
            rta.add(new Question(rs));
        }
        return rta.toArray(new Question[0]);
    }
}
