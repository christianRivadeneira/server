package api.mss.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class MssCertElement extends BaseModel<MssCertElement> {
//inicio zona de reemplazo

    public String name;
    public String status;
    public int certId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "status",
            "cert_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, status);
        q.setParam(3, certId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        status = MySQLQuery.getAsString(row[1]);
        certId = MySQLQuery.getAsInteger(row[2]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "mss_cert_element";
    }

    public static String getSelFlds(String alias) {
        return new MssCertElement().getSelFldsForAlias(alias);
    }

    public static List<MssCertElement> getList(MySQLQuery q, Connection conn) throws Exception {
        return new MssCertElement().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new MssCertElement().deleteById(id, conn);
    }

    public static List<MssCertElement> getAll(Connection conn) throws Exception {
        return new MssCertElement().getAllList(conn);
    }

//fin zona de reemplazo
    public static List<MssCertElement> getByCert(int certId, Connection conn) throws Exception {
        Params p = new Params("cert_id", certId);
        p.sort("name");        
        return new MssCertElement().getListFromParams(p, conn);
    }

}
