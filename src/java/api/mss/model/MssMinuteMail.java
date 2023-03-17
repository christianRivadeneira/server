package api.mss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class MssMinuteMail extends BaseModel<MssMinuteMail> {
//inicio zona de reemplazo

    public String email;
    public int incidentTypeId;
    public int clientId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "email",
            "incident_type_id",
            "client_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, email);
        q.setParam(2, incidentTypeId);
        q.setParam(3, clientId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        email = MySQLQuery.getAsString(row[0]);
        incidentTypeId = MySQLQuery.getAsInteger(row[1]);
        clientId = MySQLQuery.getAsInteger(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_minute_mail";
    }

    public static String getSelFlds(String alias) {
        return new MssMinuteMail().getSelFldsForAlias(alias);
    }

    public static List<MssMinuteMail> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssMinuteMail().getListFromQuery(q, conn);
    }

    public static List<MssMinuteMail> getList(Params p, Connection conn) throws Exception {
        return new MssMinuteMail().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssMinuteMail().deleteById(id, conn);
    }

    public static List<MssMinuteMail> getAll(Connection conn) throws Exception {
        return new MssMinuteMail().getAllList(conn);
    }

//fin zona de reemplazo
    public static List<MssMinuteMail> getByIncidentType(int typeId, Connection conn) throws Exception {
        Params pars = new Params("incident_type_id", typeId);
        return new MssMinuteMail().getListFromParams(pars, conn);
    }
}
