package api.dto.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class DtoCenter extends BaseModel<DtoCenter> {
//inicio zona de reemplazo

    public String name;
    public Integer initialBalancePrev;
    public Integer initialBalanceMonth;
    public Date dDay;
    public Integer sysCenterId;
    public boolean visible;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "initial_balance_prev",
            "initial_balance_month",
            "d_day",
            "sys_center_id",
            "visible"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, initialBalancePrev);
        q.setParam(3, initialBalanceMonth);
        q.setParam(4, dDay);
        q.setParam(5, sysCenterId);
        q.setParam(6, visible);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        initialBalancePrev = MySQLQuery.getAsInteger(row[1]);
        initialBalanceMonth = MySQLQuery.getAsInteger(row[2]);
        dDay = MySQLQuery.getAsDate(row[3]);
        sysCenterId = MySQLQuery.getAsInteger(row[4]);
        visible = MySQLQuery.getAsBoolean(row[5]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "dto_center";
    }

    public static String getSelFlds(String alias) {
        return new DtoCenter().getSelFldsForAlias(alias);
    }

    public static List<DtoCenter> getList(MySQLQuery q, Connection conn) throws Exception {
        return new DtoCenter().getListFromQuery(q, conn);
    }

    public static List<DtoCenter> getList(Params p, Connection conn) throws Exception {
        return new DtoCenter().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new DtoCenter().deleteById(id, conn);
    }

    public static List<DtoCenter> getAll(Connection conn) throws Exception {
        return new DtoCenter().getAllList(conn);
    }

//fin zona de reemplazo
    /*
    public static List<DtoCenter> getBy(Connection conn) throws Exception {
        Params p = new Params("", );
        p.sort("");
        return new getList(p, conn);
    }*/    

}