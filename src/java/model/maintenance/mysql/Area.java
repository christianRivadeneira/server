package model.maintenance.mysql;

import java.awt.geom.Path2D;
import java.sql.Connection;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

public class Area {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String name;
    private String puc;
    private Integer areaId;
    private String type;

    public Area() {
    }

    public Area(Integer id, String name, String puc, Integer areaId, String type) {
        this.id = id;
        this.name = name;
        this.puc = puc;
        this.areaId = areaId;
        this.type = type;
    }

    public Area(Object[] row) {
        this.id = MySQLQuery.getAsInteger(row[0]);
        this.name = MySQLQuery.getAsString(row[1]);
        this.puc = MySQLQuery.getAsString(row[2]);
        this.areaId = MySQLQuery.getAsInteger(row[3]);
        this.type = MySQLQuery.getAsString(row[4]);
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

    public String getPuc() {
        return puc;
    }

    public void setPuc(String puc) {
        this.puc = puc;
    }

    public Integer getAreaId() {
        return areaId;
    }

    public void setAreaId(Integer areaId) {
        this.areaId = areaId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static Area[] getSuperAreas(String type) throws Exception {
        Connection con = null;
        try {
            con = MySQLCommon.getConnection("sigmads", null);
            return getSuperAreas(type, con);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    public static Area[] getSuperAreas(String type, Connection con) throws Exception {
        Object[][] data = new MySQLQuery("SELECT id, name, puc, area_id, type FROM area WHERE area_id IS NULL " + (type != null ? "AND type = '" + type + "'" : "") + " ORDER BY name").getRecords(con);
        Area[] rta = new Area[data.length];
        for (int i = 0; i < data.length; i++) {
            rta[i] = new Area(data[i]);
        }
        return rta;
    }

    public static Area getAreaById(int id, Connection con) throws Exception {
        Object[][] data = new MySQLQuery("SELECT id, name, puc, area_id, type FROM area WHERE id = " + id).getRecords(con);
        if (data.length <= 0) {
            throw new Exception("El área no éxiste.");
        }
        return new Area(data[0]);
    }
}
