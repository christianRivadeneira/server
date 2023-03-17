package api.bill.model;

import api.BaseModel;
import api.Params;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import utilities.MySQLPreparedQuery;
import utilities.MySQLQuery;

public class BillReadingFault extends BaseModel<BillReadingFault> {
//inicio zona de reemplazo

    public String name;
    public String consType;
    public String type;
    public boolean active;
    
    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "cons_type",
            "type",
            "active"
        };
    }
    
    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, consType);
        q.setParam(3, type);
        q.setParam(4, active);
    }
    
    @Override
    public void setRow(Object[] row) throws Exception {
        name = MySQLQuery.getAsString(row[0]);
        consType = MySQLQuery.getAsString(row[1]);
        type = MySQLQuery.getAsString(row[2]);
        active = MySQLQuery.getAsBoolean(row[3]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }
    
    @Override
    protected String getTblName() {
        return "bill_reading_fault";
    }
    
    public static String getSelFlds(String alias) {
        return new BillReadingFault().getSelFldsForAlias(alias);
    }
    
    public static List<BillReadingFault> getList(MySQLQuery q, Connection conn) throws Exception {
        return new BillReadingFault().getListFromQuery(q, conn);
    }
    
    public static List<BillReadingFault> getList(Params p, Connection conn) throws Exception {
        return new BillReadingFault().getListFromParams(p, conn);
    }
    
    public static void delete(int id, Connection conn) throws Exception {
        new BillReadingFault().deleteById(id, conn);
    }
    
    public static List<BillReadingFault> getAll(Connection conn) throws Exception {
        return new BillReadingFault().getAllList(conn);
    }

//fin zona de reemplazo
    public static List<BillReadingFault> getActive(Connection conn) throws Exception {
        Params p = new Params("active", true);
        return BillReadingFault.getList(p, conn);
    }
    
    public static Map<Integer, BillReadingFault> getActiveAsMap(Connection conn) throws Exception {
        Map<Integer, BillReadingFault> rta = new HashMap<>();
        List<BillReadingFault> lst = BillReadingFault.getActive(conn);
        for (int i = 0; i < lst.size(); i++) {
            BillReadingFault f = lst.get(i);
            rta.put(f.id, f);
        }        
        return rta;
    }
    
    public static MySQLPreparedQuery getFaultDescByBillQuery(Connection billConn) throws Exception {
        return new MySQLPreparedQuery("SELECT "
                + "IF(r.critical_reading IS NOT NULL, 'Facturado por promedio: Desviación Crítica', "
                + "CONCAT( "
                + "CASE "
                + "    WHEN f.cons_type = 'zero' THEN \"No registra consumo: \" "
                + "    WHEN f.cons_type = 'avg'  THEN \"Facturado por promedio: \" "
                + "END, f.name)) "
                + "FROM bill_reading r "
                + "LEFT JOIN sigma.bill_reading_fault f ON r.fault_id = f.id "
                + "WHERE r.client_tank_id = ?2 AND r.span_id = ?1", billConn);
    }
    
}
