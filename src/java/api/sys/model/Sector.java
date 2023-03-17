package api.sys.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class Sector extends BaseModel<Sector> {
//inicio zona de reemplazo

    public String name;
    public int cityId;
    public String codigo;
    public Integer polyColor;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "city_id",
            "codigo",
            "poly_color"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, cityId);
        q.setParam(3, codigo);
        q.setParam(4, polyColor);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        cityId = MySQLQuery.getAsInteger(row[1]);
        codigo = MySQLQuery.getAsString(row[2]);
        polyColor = MySQLQuery.getAsInteger(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "sector";
    }

    public static String getSelFlds(String alias) {
        return new Sector().getSelFldsForAlias(alias);
    }

    public static List<Sector> getList(MySQLQuery q, Connection conn) throws Exception {
        return new Sector().getListFromQuery(q, conn);
    }

    public static List<Sector> getList(Params p, Connection conn) throws Exception {
        return new Sector().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new Sector().deleteById(id, conn);
    }

    public static List<Sector> getAll(Connection conn) throws Exception {
        return new Sector().getAllList(conn);
    }

//fin zona de reemplazo
    
    public static List<Sector> getSectorsByOffice(int officeId, Connection em) throws Exception {
        MySQLQuery mq = new MySQLQuery("select concat(c.`name`, ' - ', s.`name`), s.`city_id`, s.`codigo`, s.id from "
                + "ord_office o "
                + "inner join ord_office_city oc on oc.office_id = o.id "
                + "inner join city c on c.id = oc.city_id "
                + "inner join sector s on s.city_id = c.id "
                + "where o.id = " + officeId + " "
                + "order by c.name, s.name");

        List<Sector> list = getList(mq, em);
        return list;
    }

    public static List<Sector> getSectorsByCity(int cityId, Connection ep) throws Exception {
        MySQLQuery mq = new MySQLQuery("SELECT " + getSelFlds("") + " FROM  "
                + "sector "
                + "where city_id = " + cityId + " order by name");

        List<Sector> list = getList(mq, ep);
        return list;
    }

}
