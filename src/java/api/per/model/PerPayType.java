package api.per.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.math.BigDecimal;

public class PerPayType extends BaseModel<PerPayType> {
//inicio zona de reemplazo

    public BigDecimal value;
    public String name;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "value",
            "name"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, value);
        q.setParam(2, name);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        value = MySQLQuery.getAsBigDecimal(row[0], false);
        name = MySQLQuery.getAsString(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "per_pay_type";
    }

    public static String getSelFlds(String alias) {
        return new PerPayType().getSelFldsForAlias(alias);
    }

    public static List<PerPayType> getList(MySQLQuery q, Connection conn) throws Exception {
        return new PerPayType().getListFromQuery(q, conn);
    }

//fin zona de reemplazo
    
}