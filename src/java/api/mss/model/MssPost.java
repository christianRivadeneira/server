package api.mss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class MssPost extends BaseModel<MssPost> {
//inicio zona de reemplazo

    public int clientId;
    public Integer danePobladoId;
    public int sysCenterId;
    public String address;
    public String name;
    public String code;
    public String notes;
    public String motto;
    public boolean active;
    public Date beginDt;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "client_id",
            "dane_poblado_id",
            "sys_center_id",
            "address",
            "name",
            "code",
            "notes",
            "motto",
            "active",
            "begin_dt"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, clientId);
        q.setParam(2, danePobladoId);
        q.setParam(3, sysCenterId);
        q.setParam(4, address);
        q.setParam(5, name);
        q.setParam(6, code);
        q.setParam(7, notes);
        q.setParam(8, motto);
        q.setParam(9, active);
        q.setParam(10, beginDt);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        clientId = MySQLQuery.getAsInteger(row[0]);
        danePobladoId = MySQLQuery.getAsInteger(row[1]);
        sysCenterId = MySQLQuery.getAsInteger(row[2]);
        address = MySQLQuery.getAsString(row[3]);
        name = MySQLQuery.getAsString(row[4]);
        code = MySQLQuery.getAsString(row[5]);
        notes = MySQLQuery.getAsString(row[6]);
        motto = MySQLQuery.getAsString(row[7]);
        active = MySQLQuery.getAsBoolean(row[8]);
        beginDt = MySQLQuery.getAsDate(row[9]);
        id = MySQLQuery.getAsInteger(row[10]);
    }

    @Override
    protected String getTblName() {
        return "mss_post";
    }

    public static String getSelFlds(String alias) {
        return new MssPost().getSelFldsForAlias(alias);
    }

    public static List<MssPost> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssPost().getListFromQuery(q, conn);
    }

    public static List<MssPost> getList(Params p, Connection conn) throws Exception {
        return new MssPost().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssPost().deleteById(id, conn);
    }

    public static List<MssPost> getAll(Connection conn) throws Exception {
        return new MssPost().getAllList(conn);
    }

//fin zona de reemplazo
    public static MssPost getByCode(String postCode, Connection conn) throws Exception {
        return new MssPost().select(new Params("code", postCode), conn);
    }    
    
    public static List<MssPost> getList(Object[][] data) throws Exception {
        List<MssPost> lst = new ArrayList<>();
        
        if (data != null && data.length > 0) {
            for (Object[] row : data) {
                MssPost post = new MssPost();
                post.setRow(row);
                lst.add(post);
            }
        } else {
            return null;
        }
        return lst;
    }
    
    public static int getIntValues() {
        MssPost post = new MssPost();
        return post.getFlds().length;
    }
}
