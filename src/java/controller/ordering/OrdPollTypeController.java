package controller.ordering;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import model.ordering.OrdPollOption;
import model.ordering.OrdPollQuestion;

public class OrdPollTypeController {

    public static OrdPollQuestion[] getPollQuestions(int pollTypeId, EntityManager em) {
        String str = "SELECT "
                + "Object(q) "//0
                + "FROM "
                + "OrdPollVersion AS v, "
                + "OrdPollQuestion AS q "
                + "WHERE "
                + "q.ordPollVersionId = v.id AND "
                + "v.last = 1 AND "
                + "v.ordPollTypeId = " + pollTypeId + " "
                + "ORDER BY "
                + "q.ordinal ASC ";

        Query q = em.createQuery(str);
        List<OrdPollQuestion> res = q.getResultList();
        OrdPollQuestion[] lst = new OrdPollQuestion[res.size()];
        return res.toArray(lst);
    }

    public static OrdPollQuestion[] getPollQuestionsByVersion(int pollVersionId, EntityManager em) {
        String str = "SELECT "
                + "Object(q) "//0
                + "FROM "
                + "OrdPollQuestion AS q "
                + "WHERE "
                + "q.ordPollVersionId =" + pollVersionId + " "
                + "ORDER BY "
                + "q.ordinal ASC ";

        Query q = em.createQuery(str);
        List<OrdPollQuestion> res = q.getResultList();
        OrdPollQuestion[] lst = new OrdPollQuestion[res.size()];
        return res.toArray(lst);
    }

    public static OrdPollOption[] getPollQuestionOptions(int questionId, EntityManager em) {

        String str = "SELECT "
                + "Object(o) "//0
                + "FROM "
                + "OrdPollOption AS o "
                + "WHERE "
                + "o.pollQuestionId = " + questionId + " "
                + "ORDER BY "
                + "o.ordinal ASC";
        Query q = em.createQuery(str);
        List<OrdPollOption> res = q.getResultList();
        OrdPollOption[] lst = new OrdPollOption[res.size()];
        return res.toArray(lst);
    }
}
