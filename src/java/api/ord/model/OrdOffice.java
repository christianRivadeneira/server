package api.ord.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.math.BigDecimal;

public class OrdOffice extends BaseModel<OrdOffice> {
//inicio zona de reemplazo

    public String description;
    public String sname;
    public Integer managerId;
    public int seqCyl;
    public int seqBill;
    public int seqTank;
    public int seqOther;
    public int seqRepairs;
    public boolean draw;
    public BigDecimal lat;
    public BigDecimal lon;
    public boolean active;
    public boolean salesApp;
    public boolean clientsApp;
    public boolean pqrsApp;
    public boolean virtualApp;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "description",
            "sname",
            "manager_id",
            "seq_cyl",
            "seq_bill",
            "seq_tank",
            "seq_other",
            "seq_repairs",
            "draw",
            "lat",
            "lon",
            "active",
            "sales_app",
            "clients_app",
            "pqrs_app",
            "virtual_app"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, description);
        q.setParam(2, sname);
        q.setParam(3, managerId);
        q.setParam(4, seqCyl);
        q.setParam(5, seqBill);
        q.setParam(6, seqTank);
        q.setParam(7, seqOther);
        q.setParam(8, seqRepairs);
        q.setParam(9, draw);
        q.setParam(10, lat);
        q.setParam(11, lon);
        q.setParam(12, active);
        q.setParam(13, salesApp);
        q.setParam(14, clientsApp);
        q.setParam(15, pqrsApp);
        q.setParam(16, virtualApp);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        description = MySQLQuery.getAsString(row[0]);
        sname = MySQLQuery.getAsString(row[1]);
        managerId = MySQLQuery.getAsInteger(row[2]);
        seqCyl = MySQLQuery.getAsInteger(row[3]);
        seqBill = MySQLQuery.getAsInteger(row[4]);
        seqTank = MySQLQuery.getAsInteger(row[5]);
        seqOther = MySQLQuery.getAsInteger(row[6]);
        seqRepairs = MySQLQuery.getAsInteger(row[7]);
        draw = MySQLQuery.getAsBoolean(row[8]);
        lat = MySQLQuery.getAsBigDecimal(row[9], false);
        lon = MySQLQuery.getAsBigDecimal(row[10], false);
        active = MySQLQuery.getAsBoolean(row[11]);
        salesApp = MySQLQuery.getAsBoolean(row[12]);
        clientsApp = MySQLQuery.getAsBoolean(row[13]);
        pqrsApp = MySQLQuery.getAsBoolean(row[14]);
        virtualApp = MySQLQuery.getAsBoolean(row[15]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "ord_office";
    }

    public static String getSelFlds(String alias) {
        return new OrdOffice().getSelFldsForAlias(alias);
    }

    public static List<OrdOffice> getList(MySQLQuery q, Connection conn) throws Exception {
        return new OrdOffice().getListFromQuery(q, conn);
    }

    public static List<OrdOffice> getList(Params p, Connection conn) throws Exception {
        return new OrdOffice().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new OrdOffice().deleteById(id, conn);
    }

    public static List<OrdOffice> getAll(Connection conn) throws Exception {
        return new OrdOffice().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<OrdOffice> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}