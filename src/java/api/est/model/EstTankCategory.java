package api.est.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

public class EstTankCategory extends BaseModel<EstTankCategory> {
//inicio zona de reemplazo

    public String description;
    public int typeId;
    public boolean rotacion;
    public boolean timelyPaymentBonus;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "description",
            "type_id",
            "rotacion",
            "timely_payment_bonus"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, description);
        q.setParam(2, typeId);
        q.setParam(3, rotacion);
        q.setParam(4, timelyPaymentBonus);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        description = MySQLQuery.getAsString(row[0]);
        typeId = MySQLQuery.getAsInteger(row[1]);
        rotacion = MySQLQuery.getAsBoolean(row[2]);
        timelyPaymentBonus = MySQLQuery.getAsBoolean(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "est_tank_category";
    }

    public static String getSelFlds(String alias) {
        return new EstTankCategory().getSelFldsForAlias(alias);
    }

    public static List<EstTankCategory> getList(MySQLQuery q, Connection conn) throws Exception {
        return new EstTankCategory().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new EstTankCategory().deleteById(id, conn);
    }

    public static List<EstTankCategory> getAll(Connection conn) throws Exception {
        return new EstTankCategory().getAllList(conn);
    }

//fin zona de reemplazo
}
