/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package api.gt.model;

import api.BaseModel;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;

/**
 *
 * @author Programador Sistemas
 */
public class GtTanks extends BaseModel<GtTanks>{
    
    public String name;
    public String serial;
    public BigDecimal capacity;
    public Integer centerId;
    public boolean qualityCyl;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "name",
            "serial",
            "capacity",
            "center_id",
            "quality_cyl"
        };
    }
    
    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, name);
        q.setParam(2, serial);
        q.setParam(3, capacity);
        q.setParam(4, centerId);
        q.setParam(5, qualityCyl);
    }

    @Override
    protected void setRow(Object[] row) throws Exception {
        name=MySQLQuery.getAsString(row[0]);
        serial=MySQLQuery.getAsString(row[1]);
        capacity=MySQLQuery.getAsBigDecimal(row[2], true);
        centerId=MySQLQuery.getAsInteger(row[3]);
        qualityCyl=MySQLQuery.getAsBoolean(row[4]);
        id=MySQLQuery.getAsInteger(row[row.length-1]);
        
    }
    
    @Override
    protected String getTblName() {
        return "gt_glp_tank";
    }
    
    public static List<GtTanks>getByCenterId(int id, Connection conn)throws Exception{
        return new GtTanks().getAllListByCenter(conn,id);
    }
    
    protected List<GtTanks> getAllListByCenter(Connection conn,int id) throws Exception {
        return getListFromQuery(new MySQLQuery("SELECT "
                + "t.name,t.serial,t.capacity,t.center_id,t.id "
                + "FROM gt_glp_tank t "
                + "INNER JOIN gt_center c on c.id =t.center_id "
                + "INNER JOIN sys_center sc on sc.id = c.sys_center_id "
                + "INNER JOIN inv_center i on i.id = sc.inv_center_id "
                + "WHERE i.id ="+id), conn);
    }
}
