package api.smb.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import utilities.MySQLQuery;

public class Establish extends BaseModel<Establish> {
//inicio zona de reemplazo

    public String name;
    public Integer establishId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "establish_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, establishId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        establishId = MySQLQuery.getAsInteger(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "establish";
    }

    public static String getSelFlds(String alias) {
        return new Establish().getSelFldsForAlias(alias);
    }

    public static List<Establish> getList(MySQLQuery q, Connection conn) throws Exception {
        return new Establish().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new Establish().deleteById(id, conn);
    }

    public static List<Establish> getAll(Connection conn) throws Exception {
        return new Establish().getAllList(conn);
    }
    
    public static List<Establish> getAllSuperEstablishs(Connection conn) throws Exception {
        MySQLQuery mq = new MySQLQuery("SELECT o.id, o.name, o.establish_id FROM establish as o WHERE o.establish_id IS NULL ORDER BY o.name ASC");

        Object[][] data = mq.getRecords(conn);
        List<Establish> lista = new ArrayList<>();
        if (data != null) {
            for (Object[] obj : data) {
                Establish opt = new Establish();
                opt.id = MySQLQuery.getAsInteger(obj[0]);
                opt.name = MySQLQuery.getAsString(obj[1]);
                opt.establishId = MySQLQuery.getAsInteger(obj[2]);
                lista.add(opt);
            }
        }
        return lista;
    }
    
    public static List<Establish> getAllSubEstablishs(Connection conn, int establishId) throws Exception {
        MySQLQuery mq = new MySQLQuery("SELECT o.id, o.name, o.establish_id FROM establish as o WHERE o.establish_id = ?1 ORDER BY o.name ASC").setParam(1, establishId);

        Object[][] data = mq.getRecords(conn);
        List<Establish> lista = new ArrayList<>();
        if (data != null) {
            for (Object[] obj : data) {
                Establish opt = new Establish();
                opt.id = MySQLQuery.getAsInteger(obj[0]);
                opt.name = MySQLQuery.getAsString(obj[1]);
                opt.establishId = MySQLQuery.getAsInteger(obj[2]);
                lista.add(opt);
            }
        }
        return lista;
    }
    
    public static Establish findEstablish(Connection conn, int establishId) throws Exception {
        return new Establish().select(establishId, conn);
    }

//fin zona de reemplazo
}