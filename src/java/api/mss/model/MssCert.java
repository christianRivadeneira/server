package api.mss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class MssCert extends BaseModel<MssCert> {

    public List<MssCertElement> listCertElement;
    public List<MssCertElement> listCertElementDelete;

//inicio zona de reemplazo
    public Date regDt;
    public String type;
    public String delivery;
    public Integer postId;
    public int superId;
    public String notes;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "reg_dt",
            "type",
            "delivery",
            "post_id",
            "super_id",
            "notes"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, regDt);
        q.setParam(2, type);
        q.setParam(3, delivery);
        q.setParam(4, postId);
        q.setParam(5, superId);
        q.setParam(6, notes);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        regDt = MySQLQuery.getAsDate(row[0]);
        type = MySQLQuery.getAsString(row[1]);
        delivery = MySQLQuery.getAsString(row[2]);
        postId = MySQLQuery.getAsInteger(row[3]);
        superId = MySQLQuery.getAsInteger(row[4]);
        notes = MySQLQuery.getAsString(row[5]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_cert";
    }

    public static String getSelFlds(String alias) {
        return new MssCert().getSelFldsForAlias(alias);
    }

    public static List<MssCert> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssCert().getListFromQuery(q, conn);
    }

    public static List<MssCert> getList(Params p, Connection conn) throws Exception {
        return new MssCert().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssCert().deleteById(id, conn);
    }

    public static List<MssCert> getAll(Connection conn) throws Exception {
        return new MssCert().getAllList(conn);
    }

//fin zona de reemplazo
}
