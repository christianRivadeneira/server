package api.mss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import utilities.MySQLQuery;
import utilities.ServerNow;
import web.push.GCMUtils;

public class MssChallenge extends BaseModel<MssChallenge> {
//inicio zona de reemplazo

    public int shiftId;
    public String oper;
    public int num1;
    public int num2;
    public int expResult;
    public Integer regResult;
    public Date questionDt;
    public Date answerDt;
    public boolean checked;
    public String checkNotes;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "shift_id",
            "oper",
            "num1",
            "num2",
            "exp_result",
            "reg_result",
            "question_dt",
            "answer_dt",
            "checked",
            "check_notes"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, shiftId);
        q.setParam(2, oper);
        q.setParam(3, num1);
        q.setParam(4, num2);
        q.setParam(5, expResult);
        q.setParam(6, regResult);
        q.setParam(7, questionDt);
        q.setParam(8, answerDt);
        q.setParam(9, checked);
        q.setParam(10, checkNotes);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        shiftId = MySQLQuery.getAsInteger(row[0]);
        oper = MySQLQuery.getAsString(row[1]);
        num1 = MySQLQuery.getAsInteger(row[2]);
        num2 = MySQLQuery.getAsInteger(row[3]);
        expResult = MySQLQuery.getAsInteger(row[4]);
        regResult = MySQLQuery.getAsInteger(row[5]);
        questionDt = MySQLQuery.getAsDate(row[6]);
        answerDt = MySQLQuery.getAsDate(row[7]);
        checked = MySQLQuery.getAsBoolean(row[8]);
        checkNotes = MySQLQuery.getAsString(row[9]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_challenge";
    }

    public static String getSelFlds(String alias) {
        return new MssChallenge().getSelFldsForAlias(alias);
    }

    public static List<MssChallenge> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssChallenge().getListFromQuery(q, conn);
    }

    public static List<MssChallenge> getList(Params p, Connection conn) throws Exception {
        return new MssChallenge().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssChallenge().deleteById(id, conn);
    }

    public static List<MssChallenge> getAll(Connection conn) throws Exception {
        return new MssChallenge().getAllList(conn);
    }

//fin zona de reemplazo
    public static void sendChallenge(int appId, int shiftId, int empId, Connection conn) throws Exception {
        MssChallenge c = new MssChallenge();
        c.shiftId = shiftId;
        double r = Math.random();
        if (r < 1 / 3d) {
            c.num1 = (int) (Math.ceil(Math.random() * 10) );
            c.num2 = (int) (Math.ceil(Math.random() * 10) );
            c.oper = "add";
            c.expResult = c.num1 + c.num2;
        } else if (r < 2 / 3d) {
            c.oper = "sub";
            c.num1 = (int) (Math.ceil(Math.random() * 10) );
            c.num2 = (int) (Math.ceil(Math.random() * 10) );
            if (c.num2 > c.num1) {
                int aux = c.num1;
                c.num1 = c.num2;
                c.num2 = aux;
            }
            c.expResult = c.num1 - c.num2;
        } else {
            c.num1 = (int) (Math.ceil(Math.random() * 10) );
            c.num2 = (int) (Math.ceil(Math.random() * 10) );
            c.oper = "mul";
            c.expResult = c.num1 * c.num2;
        }
        c.questionDt = new ServerNow();
        c.checked = false;
        c.insert(conn);
        JsonObjectBuilder ob = Json.createObjectBuilder();
        ob.add("challengeId", c.id);
        ob.add("type", "challenge");
        GCMUtils.sendToApp(appId, ob.build(), empId + "", conn);
    }
}
