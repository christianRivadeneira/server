/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package web.tanks;

import java.math.BigDecimal;
import java.sql.Connection;
import utilities.MySQLQuery;

/**
 *
 * @author Programador Sistemas
 */
public class EstSaleQuality {
    
    public Integer id;
    public BigDecimal c3Propano;
    public BigDecimal c4Butano;
    public BigDecimal c5Olefinas;
    public BigDecimal agua;
    
    private static final String SEL_FLDS = "`est_sale_quality`, "
            + "`c_3_propano`, "
            + "`c_4_butano`, "
            + "`c_5_olefinas`, "
            + "`agua`";
    
    private static final String SET_FLDS = "est_sale_quality SET "
            + "`c_3_propano` = ?1, "
            + "`c_4_butano` = ?2, "
            + "`c_5_olefinas` = ?3, "
            + "`agua` = ?4";
    
     private static void setFields(EstSaleQuality obj, MySQLQuery q) {
        q.setParam(1, obj.c3Propano);
        q.setParam(2, obj.c4Butano);
        q.setParam(3, obj.c5Olefinas);
        q.setParam(4, obj.agua);
     }
     
     public static EstSaleQuality getFromRow(Object[] row) throws Exception {
        if (row == null || row.length == 0) {
            return null;
        }
        EstSaleQuality obj = new EstSaleQuality();
        obj.c3Propano = MySQLQuery.getAsBigDecimal(row[0],false);
        obj.c4Butano = MySQLQuery.getAsBigDecimal(row[1],false);
        obj.c5Olefinas = MySQLQuery.getAsBigDecimal(row[2],false);
        obj.agua = MySQLQuery.getAsBigDecimal(row[3], false);
        
        obj.id = MySQLQuery.getAsInteger(row[row.length - 1]);
        return obj;
     }
     
     //zona api
     public int insert(EstSaleQuality obj, Connection ep)throws Exception{
         int nId = new MySQLQuery(EstSaleQuality.getInsertQuery(obj)).executeInsert(ep);
        obj.id = nId;
        return nId;
     }
     public static String getInsertQuery(EstSaleQuality obj) {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + SET_FLDS);
        setFields(obj, q);
        return q.getParametrizedQuery();
    }
    
}
