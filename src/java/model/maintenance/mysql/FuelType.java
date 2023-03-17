package model.maintenance.mysql;

import java.sql.Connection;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

public class FuelType {

    private Integer id;
    private String name;
    private String pucCode;

    public FuelType() {
    }

    public FuelType(Integer id) {
        this.id = id;
    }

    public FuelType(Object[] row) {
        this.id = MySQLQuery.getAsInteger(row[0]);
        this.name = MySQLQuery.getAsString(row[1]);
        this.pucCode = MySQLQuery.getAsString(row[2]);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPucCode() {
        return pucCode;
    }

    public void setPucCode(String pucCode) {
        this.pucCode = pucCode;
    }

    public static FuelType[] getAllFuelTypes(Connection con) throws Exception {
        String q = "SELECT id, name, puc_code FROM fuel_type ORDER BY name";
        Object[][] data = new MySQLQuery(q).getRecords(con);
        FuelType[] rta = new FuelType[data.length];
        for (int i = 0; i < data.length; i++) {
            rta[i] = new FuelType(data[i]);
        }
        return rta;
    }

    public static FuelType[] getAllFuelTypes() throws Exception {
        Connection con = null;
        try {
            con = MySQLCommon.getConnection("sigmads", null);
            return getAllFuelTypes(con);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception ex) {
                }
            }
        }
    }

}
