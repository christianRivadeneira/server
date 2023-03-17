package api.mss.model;

import api.BaseModel;
import api.Params;
import static api.mss.api.MssShiftIncidentApi.TZ;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;
import web.quality.MailCfg;
import web.quality.SendMail;

public class MssSuperReview extends BaseModel<MssSuperReview> {
//inicio zona de reemplazo

    public Date regDate;
    public String notes;
    public int superId;
    public int guardId;
    public int postId;
    public String feedback;
    public boolean signed;
    public Integer progId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "reg_date",
            "notes",
            "super_id",
            "guard_id",
            "post_id",
            "feedback",
            "signed",
            "prog_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, regDate);
        q.setParam(2, notes);
        q.setParam(3, superId);
        q.setParam(4, guardId);
        q.setParam(5, postId);
        q.setParam(6, feedback);
        q.setParam(7, signed);
        q.setParam(8, progId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        regDate = MySQLQuery.getAsDate(row[0]);
        notes = MySQLQuery.getAsString(row[1]);
        superId = MySQLQuery.getAsInteger(row[2]);
        guardId = MySQLQuery.getAsInteger(row[3]);
        postId = MySQLQuery.getAsInteger(row[4]);
        feedback = MySQLQuery.getAsString(row[5]);
        signed = MySQLQuery.getAsBoolean(row[6]);
        progId = MySQLQuery.getAsInteger(row[7]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_super_review";
    }

    public static String getSelFlds(String alias) {
        return new MssSuperReview().getSelFldsForAlias(alias);
    }

    public static List<MssSuperReview> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssSuperReview().getListFromQuery(q, conn);
    }

    public static List<MssSuperReview> getList(Params p, Connection conn) throws Exception {
        return new MssSuperReview().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssSuperReview().deleteById(id, conn);
    }

    public static List<MssSuperReview> getAll(Connection conn) throws Exception {
        return new MssSuperReview().getAllList(conn);
    }

//fin zona de reemplazo
    public static void sendMailSuperReview(int id, Connection conn) throws Exception {
        Object[][] data = new MySQLQuery("SELECT DATE_FORMAT(r.reg_date, '%d/%c/%Y %H:%i:%s'), CONCAT(g.first_name, ' ', g.last_name), p.name, rf.name, r.notes "
                + "FROM mss_super_review r "
                + "INNER JOIN mss_post p ON p.id = r.post_id "
                + "INNER JOIN mss_guard g ON g.id = r.guard_id "
                + "INNER JOIN mss_super_review_checklist ck ON ck.review_id = r.id "
                + "INNER JOIN mss_super_review_finding rf ON rf.id = ck.finding_id "
                + "WHERE ck.review_id = ?1").setParam(1, id).getRecords(conn);
        if (data != null && data.length > 0) {
            MailCfg cfg = MailCfg.select(conn);
            String msg = "Se ha creado una novedad de supervisión:<br/>"
                    + "<br/><b>Fecha de Registro:</b> " + MySQLQuery.getAsString(data[0][0])
                    + "<br/><b>Guarda:</b> " + MySQLQuery.getAsString(data[0][1])
                    + "<br/><b>Puesto:</b> " + MySQLQuery.getAsString(data[0][2])
                    + "<br/><b>Evaluación:</b> ";

            for (int i = 0; i < data.length; i++) {
                msg += "<br/> " + (i + 1) + ". " + MySQLQuery.getAsString(data[i][3]);
            }
            if (data[0][4] != null) {
                msg += "<br/><b>Observaciones:</b> " + MySQLQuery.getAsString(data[0][4]);
            }

            String html = SendMail.getHtmlMsg(conn, "Novedad de Supervisión", msg);
            SendMail.sendMail(cfg, new MySQLQuery("SELECT admin_mail FROM mss_cfg WHERE id = 1").getAsString(conn), "Novedad de Supervisión", html, TZ, null, null, null, null);
        }
    }
}
