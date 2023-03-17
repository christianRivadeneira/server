package model.maintenance.mysql;

import java.sql.Connection;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

public class MySQLAgency {

    private Integer id;
    private int cityId;
    private int enterpriseId;

    public MySQLAgency() {
    }

    public MySQLAgency(Integer id) {
        this.id = id;
    }

    public MySQLAgency(Integer id, int cityId, int enterpriseId) {
        this.id = id;
        this.cityId = cityId;
        this.enterpriseId = enterpriseId;
    }

    public MySQLAgency(Object[] row) {
        this.id = MySQLQuery.getAsInteger(row[0]);
        this.cityId = MySQLQuery.getAsInteger(row[1]);
        this.enterpriseId = MySQLQuery.getAsInteger(row[2]);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public int getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(int enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public static MySQLAgency getAgency(int cityId, int entId, Connection con) throws Exception {
        MySQLAgency[] ags = getAgencies(cityId, entId, con);
        if (ags.length > 0) {
            return ags[0];
        }
        throw new Exception("La agencia no éxiste");
    }

    public static MySQLAgency[] getAgencies(int cityId, int entId) throws Exception {
        Connection con = null;
        try {
            con = MySQLCommon.getConnection("sigmads", null);
            return getAgencies(cityId, entId, con);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    public static MySQLAgency getAgency(int cityId, int entId) throws Exception {
        Connection con = null;
        try {
            con = MySQLCommon.getConnection("sigmads", null);
            return getAgency(cityId, entId, con);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    public static MySQLAgency getAgencyById(int id) throws Exception {
        Connection con = null;
        try {
            con = MySQLCommon.getConnection("sigmads", null);
            return getAgencyById(id, con);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    public static MySQLAgency[] getAgencies(int cityId, int entId, Connection con) throws Exception {
        String q;
        if (cityId > 0 && entId > 0) {
            q = "SELECT id, city_id, enterprise_id FROM agency WHERE city_id = " + cityId + " AND enterprise_id = " + entId + " ORDER BY agency.id";
        } else if (cityId > 0 && entId == 0) {
            q = "SELECT id, city_id, enterprise_id FROM agency WHERE city_id = " + cityId + " ORDER BY agency.id";
        } else if (cityId == 0 && entId > 0) {
            q = "SELECT id, city_id, enterprise_id FROM agency WHERE enterprise_id = " + entId + " ORDER BY agency.id";
        } else {
            q = "SELECT id, city_id, enterprise_id FROM agency ORDER BY agency.id";
        }
        Object[][] data = new MySQLQuery(q).getRecords(con);
        MySQLAgency[] rta = new MySQLAgency[data.length];
        for (int i = 0; i < data.length; i++) {
            rta[i] = new MySQLAgency(data[i]);
        }
        return rta;
    }

    public static MySQLAgency getAgencyById(int id, Connection con) throws Exception {
        Object[][] data = new MySQLQuery("SELECT id, city_id, enterprise_id FROM agency WHERE id = " + id).getRecords(con);
        if (data.length <= 0) {
            throw new Exception("El área no éxiste.");
        }
        return new MySQLAgency(data[0]);
    }
}
