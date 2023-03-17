package api.crm.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class CrmClient extends BaseModel<CrmClient> {
//inicio zona de reemplazo

    public Integer salesEmployeeId;
    public String name;
    public String document;
    public String address;
    public String phone;
    public String email;
    public boolean active;
    public Date beginDate;
    public Integer cityId;
    public boolean person;
    public String description;
    public String type;
    public Boolean noMoreMails;
    public Integer chanelId;
    public Date assignedDate;
    public String shortName;
    public String state;
    public Integer grpsId;
    public String mainContact;
    public Integer prospectId;
    public String cache;
    public Integer createdBy;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "sales_employee_id",
            "name",
            "document",
            "address",
            "phone",
            "email",
            "active",
            "begin_date",
            "city_id",
            "person",
            "description",
            "type",
            "no_more_mails",
            "chanel_id",
            "assigned_date",
            "short_name",
            "state",
            "grps_id",
            "main_contact",
            "prospect_id",
            "cache",
            "created_by"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, salesEmployeeId);
        q.setParam(2, name);
        q.setParam(3, document);
        q.setParam(4, address);
        q.setParam(5, phone);
        q.setParam(6, email);
        q.setParam(7, active);
        q.setParam(8, beginDate);
        q.setParam(9, cityId);
        q.setParam(10, person);
        q.setParam(11, description);
        q.setParam(12, type);
        q.setParam(13, noMoreMails);
        q.setParam(14, chanelId);
        q.setParam(15, assignedDate);
        q.setParam(16, shortName);
        q.setParam(17, state);
        q.setParam(18, grpsId);
        q.setParam(19, mainContact);
        q.setParam(20, prospectId);
        q.setParam(21, cache);
        q.setParam(22, createdBy);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        salesEmployeeId = MySQLQuery.getAsInteger(row[0]);
        name = MySQLQuery.getAsString(row[1]);
        document = MySQLQuery.getAsString(row[2]);
        address = MySQLQuery.getAsString(row[3]);
        phone = MySQLQuery.getAsString(row[4]);
        email = MySQLQuery.getAsString(row[5]);
        active = MySQLQuery.getAsBoolean(row[6]);
        beginDate = MySQLQuery.getAsDate(row[7]);
        cityId = MySQLQuery.getAsInteger(row[8]);
        person = MySQLQuery.getAsBoolean(row[9]);
        description = MySQLQuery.getAsString(row[10]);
        type = MySQLQuery.getAsString(row[11]);
        noMoreMails = MySQLQuery.getAsBoolean(row[12]);
        chanelId = MySQLQuery.getAsInteger(row[13]);
        assignedDate = MySQLQuery.getAsDate(row[14]);
        shortName = MySQLQuery.getAsString(row[15]);
        state = MySQLQuery.getAsString(row[16]);
        grpsId = MySQLQuery.getAsInteger(row[17]);
        mainContact = MySQLQuery.getAsString(row[18]);
        prospectId = MySQLQuery.getAsInteger(row[19]);
        cache = MySQLQuery.getAsString(row[20]);
        createdBy = MySQLQuery.getAsInteger(row[21]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "crm_client";
    }

    public static String getSelFlds(String alias) {
        return new CrmClient().getSelFldsForAlias(alias);
    }

    public static List<CrmClient> getList(MySQLQuery q, Connection conn) throws Exception {
        return new CrmClient().getListFromQuery(q, conn);
    }

    public static List<CrmClient> getList(Params p, Connection conn) throws Exception {
        return new CrmClient().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new CrmClient().deleteById(id, conn);
    }

    public static List<CrmClient> getAll(Connection conn) throws Exception {
        return new CrmClient().getAllList(conn);
    }

//fin zona de reemplazo
    
    public String[][] getEnumOptionsAsMatrix(String fieldName) {
        if (fieldName.equals("type")) {
            return getEnumStrAsMatrix("client=Cliente&prospect=Prospecto");
        } else if (fieldName.equals("statesClient")) {
            return getEnumStrAsMatrix("opor_cli=Oportunidad&no_cont=Evaluación&desc=Descalificado");
        } else if (fieldName.equals("statesProsp")) {
            return getEnumStrAsMatrix("cont=Candidato&eval=Evaluación&opor=Oportunidad&desc=Descalificado");
        } else if (fieldName.equals("state")) {
            return getEnumStrAsMatrix("cont=Candidato&eval=Evaluación&opor=Oportunidad&desc=Descalificado&no_cont=Evaluación&opor_cli=Oportunidad");
        }
        return null;
    }
    
    
    /*
    public static List<CrmClient> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}
