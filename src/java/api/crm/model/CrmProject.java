package api.crm.model;

import api.BaseModel;
import api.Params;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class CrmProject extends BaseModel<CrmProject> {
//inicio zona de reemplazo

    public int clientId;
    public String name;
    public String state;
    public String notes;
    public Date dtInitial;
    public Date dtFinal;
    public Integer hours;
    public Date dtEstimated;
    public Integer respId;
    public boolean monthlyCtrl;
    public BigDecimal valueHour;
    public BigDecimal entry;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "client_id",
            "name",
            "state",
            "notes",
            "dt_initial",
            "dt_final",
            "hours",
            "dt_estimated",
            "resp_id",
            "monthly_ctrl",
            "value_hour",
            "entry"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, clientId);
        q.setParam(2, name);
        q.setParam(3, state);
        q.setParam(4, notes);
        q.setParam(5, dtInitial);
        q.setParam(6, dtFinal);
        q.setParam(7, hours);
        q.setParam(8, dtEstimated);
        q.setParam(9, respId);
        q.setParam(10, monthlyCtrl);
        q.setParam(11, valueHour);
        q.setParam(12, entry);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        clientId = MySQLQuery.getAsInteger(row[0]);
        name = MySQLQuery.getAsString(row[1]);
        state = MySQLQuery.getAsString(row[2]);
        notes = MySQLQuery.getAsString(row[3]);
        dtInitial = MySQLQuery.getAsDate(row[4]);
        dtFinal = MySQLQuery.getAsDate(row[5]);
        hours = MySQLQuery.getAsInteger(row[6]);
        dtEstimated = MySQLQuery.getAsDate(row[7]);
        respId = MySQLQuery.getAsInteger(row[8]);
        monthlyCtrl = MySQLQuery.getAsBoolean(row[9]);
        valueHour = MySQLQuery.getAsBigDecimal(row[10], false);
        entry = MySQLQuery.getAsBigDecimal(row[11], false);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "crm_project";
    }

    public static String getSelFlds(String alias) {
        return new CrmProject().getSelFldsForAlias(alias);
    }

    public static List<CrmProject> getList(MySQLQuery q, Connection conn) throws Exception {
        return new CrmProject().getListFromQuery(q, conn);
    }

    public static List<CrmProject> getList(Params p, Connection conn) throws Exception {
        return new CrmProject().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new CrmProject().deleteById(id, conn);
    }

    public static List<CrmProject> getAll(Connection conn) throws Exception {
        return new CrmProject().getAllList(conn);
    }

//fin zona de reemplazo
    
    public String[][] getEnumOptionsAsMatrix(String fieldName) {
        if (fieldName.equals("state")) {
            return getEnumStrAsMatrix("wait=Propuesta&active=Desarrollo&ejec=Ejecutado&fail=Cancelado");
        } else if (fieldName.equals("exectedStates")) {
            return getEnumStrAsMatrix("wait=Propuesta&active=Desarrollo");
        } else if (fieldName.equals("finishedStates")) {
            return getEnumStrAsMatrix("ejec=Ejecutado&fail=Cancelado");
        } 
        return null;
    }
    
    /*
    public static List<CrmProject> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}
