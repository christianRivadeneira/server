package api.bill.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class BillSchedServiceFail extends BaseModel<BillSchedServiceFail> {
//inicio zona de reemplazo

    public String location;
    public String src;
    public String media;
    public Date schedStart;
    public Date schedEnd;
    public int users;
    public String tipoSusp;
    public Date dateMedia;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "location",
            "src",
            "media",
            "sched_start",
            "sched_end",
            "users",
            "tipo_susp",
            "date_media"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, location);
        q.setParam(2, src);
        q.setParam(3, media);
        q.setParam(4, schedStart);
        q.setParam(5, schedEnd);
        q.setParam(6, users);
        q.setParam(7, tipoSusp);
        q.setParam(8, dateMedia);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        location = MySQLQuery.getAsString(row[0]);
        src = MySQLQuery.getAsString(row[1]);
        media = MySQLQuery.getAsString(row[2]);
        schedStart = MySQLQuery.getAsDate(row[3]);
        schedEnd = MySQLQuery.getAsDate(row[4]);
        users = MySQLQuery.getAsInteger(row[5]);
        tipoSusp = MySQLQuery.getAsString(row[6]);
        dateMedia = MySQLQuery.getAsDate(row[7]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "bill_sched_service_fail";
    }

    public static String getSelFlds(String alias) {
        return new BillSchedServiceFail().getSelFldsForAlias(alias);
    }

    public static List<BillSchedServiceFail> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillSchedServiceFail().getListFromQuery(q, conn);
    }

    public static List<BillSchedServiceFail> getList(Params p, Connection conn) throws Exception {
        return new BillSchedServiceFail().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new BillSchedServiceFail().deleteById(id, conn);
    }

    public static List<BillSchedServiceFail> getAll(Connection conn) throws Exception {
        return new BillSchedServiceFail().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<BillSchedServiceFail> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}