package api.dto.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class DtoCylPrice extends BaseModel<DtoCylPrice> {
//inicio zona de reemplazo

    public int priceFrom;
    public int priceTo;
    public int cylinderTypeId;
    public int centerId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "price_from",
            "price_to",
            "cylinder_type_id",
            "center_id"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, priceFrom);
        q.setParam(2, priceTo);
        q.setParam(3, cylinderTypeId);
        q.setParam(4, centerId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        priceFrom = MySQLQuery.getAsInteger(row[0]);
        priceTo = MySQLQuery.getAsInteger(row[1]);
        cylinderTypeId = MySQLQuery.getAsInteger(row[2]);
        centerId = MySQLQuery.getAsInteger(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "dto_cyl_price";
    }

    public static String getSelFlds(String alias) {
        return new DtoCylPrice().getSelFldsForAlias(alias);
    }

    public static List<DtoCylPrice> getList(MySQLQuery q, Connection conn) throws Exception {
        return new DtoCylPrice().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new DtoCylPrice().deleteById(id, conn);
    }

    public static List<DtoCylPrice> getAll(Connection conn) throws Exception {
        return new DtoCylPrice().getAllList(conn);
    }

//fin zona de reemplazo
    
    public static DtoCylPrice findCylPrice(List<DtoCylPrice> prices, Integer centerId, Integer cylTypeId) {
        if (centerId == null || cylTypeId == null) {
            return null;
        }
        for (DtoCylPrice price : prices) {
            if (price.centerId == centerId && price.cylinderTypeId == cylTypeId) {
                return price;
            }
        }
        return null;
    }
    
}