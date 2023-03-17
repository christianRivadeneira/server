package api.gt.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class GtCenter extends BaseModel<GtCenter> {
//inicio zona de reemplazo

    public String name;
    public boolean active;
    public boolean saveCylTrip;
    public boolean saveTankTrip;
    public String simmerCode;
    public Integer sysCenterId;
    public boolean visible;
    public String platformCod;
    public String autosaleCod;
    public String pulmCod;
    public boolean onlyBarcode;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "active",
            "save_cyl_trip",
            "save_tank_trip",
            "simmer_code",
            "sys_center_id",
            "visible",
            "platform_cod",
            "autosale_cod",
            "pulm_cod",
            "only_barcode"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, active);
        q.setParam(3, saveCylTrip);
        q.setParam(4, saveTankTrip);
        q.setParam(5, simmerCode);
        q.setParam(6, sysCenterId);
        q.setParam(7, visible);
        q.setParam(8, platformCod);
        q.setParam(9, autosaleCod);
        q.setParam(10, pulmCod);
        q.setParam(11, onlyBarcode);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        active = MySQLQuery.getAsBoolean(row[1]);
        saveCylTrip = MySQLQuery.getAsBoolean(row[2]);
        saveTankTrip = MySQLQuery.getAsBoolean(row[3]);
        simmerCode = MySQLQuery.getAsString(row[4]);
        sysCenterId = MySQLQuery.getAsInteger(row[5]);
        visible = MySQLQuery.getAsBoolean(row[6]);
        platformCod = MySQLQuery.getAsString(row[7]);
        autosaleCod = MySQLQuery.getAsString(row[8]);
        pulmCod = MySQLQuery.getAsString(row[9]);
        onlyBarcode = MySQLQuery.getAsBoolean(row[10]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "gt_center";
    }

    public static String getSelFlds(String alias) {
        return new GtCenter().getSelFldsForAlias(alias);
    }

    public static List<GtCenter> getList(MySQLQuery q, Connection conn) throws Exception {
        return new GtCenter().getListFromQuery(q, conn);
    }

    public static List<GtCenter> getList(Params p, Connection conn) throws Exception {
        return new GtCenter().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new GtCenter().deleteById(id, conn);
    }

    public static List<GtCenter> getAll(Connection conn) throws Exception {
        return new GtCenter().getAllList(conn);
    }

//fin zona de reemplazo
}
