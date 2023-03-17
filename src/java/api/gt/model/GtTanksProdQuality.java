/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.gt.model;

import api.BaseModel;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import utilities.MySQLQuery;

/**
 *
 * @author Programador Sistemas
 */
public class GtTanksProdQuality extends BaseModel<GtTanksProdQuality>{
    
    public BigDecimal c3Propano;
    public BigDecimal c4Butano;
    public BigDecimal c5Olefinas;
    public BigDecimal agua;
    public Date fechaCalidad;
    public Integer idTank;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "c_3_propano",
            "c_4_butano",
            "c_5_olefinas",
            "agua",
            "fecha_calidad",
            "id_tank"
        };
    }
    
    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, c3Propano);
        q.setParam(2, c4Butano);
        q.setParam(3, c5Olefinas);
        q.setParam(4, agua);
        q.setParam(5, fechaCalidad);
        q.setParam(6, idTank);
    }

    @Override
    protected void setRow(Object[] row) throws Exception {
        c3Propano=MySQLQuery.getAsBigDecimal(row[0], false);
        c4Butano=MySQLQuery.getAsBigDecimal(row[1], false);
        c5Olefinas=MySQLQuery.getAsBigDecimal(row[2], false);
        agua=MySQLQuery.getAsBigDecimal(row[3], false);
        fechaCalidad=MySQLQuery.getAsDate(row[4]);
        idTank=MySQLQuery.getAsInteger(row[5]);
        id=MySQLQuery.getAsInteger(row[row.length-1]);
        
    }
    
    @Override
    protected String getTblName() {
        return "gt_tanks_prod_quality";
    }

}
