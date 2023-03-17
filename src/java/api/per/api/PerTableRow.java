package api.per.api;

import api.BaseModel;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;


public class PerTableRow extends BaseModel<PerTableRow> {
    public String rowTitle;
    public BigDecimal vlrRad;
    public BigDecimal vlrAprob;
    public BigDecimal vlrPend;
    public String type = "";

    @Override
    protected void prepareQuery(MySQLQuery q) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void setRow(Object[] row) throws Exception {
        rowTitle = MySQLQuery.getAsString(row[0]);
        vlrRad = MySQLQuery.getAsBigDecimal(row[1], true);
        vlrAprob = MySQLQuery.getAsBigDecimal(row[2], true);
        vlrPend =  MySQLQuery.getAsBigDecimal(row[3], true);
        if(row.length > 4)
            type = MySQLQuery.getAsString(row[4]);
    }

    @Override
    protected String[] getFlds() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected String getTblName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public static List<PerTableRow> getList(MySQLQuery q, Connection con) throws Exception {
        return new PerTableRow().getListFromQuery(q, con);
    }

}