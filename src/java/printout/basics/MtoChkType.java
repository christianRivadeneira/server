package printout.basics;

import java.sql.Connection;
import utilities.MySQLQuery;

public class MtoChkType {
//inicio zona de reemplazo

    public int id;
    public String name;
    public String shortName;
    public String notes;
    public boolean elements;
    public boolean drivers;
    public boolean drv;
    public boolean work;
    public boolean insp;
    public boolean hasProgram;
    public boolean hasMileage;
    public boolean hasCda;
    public boolean hasVerify;
    public String sgcCode;
    public boolean showSign;
    public boolean signDriv;
    public String nameClass;

    private static final String selFlds = "`name`, "
            + "`short_name`, "
            + "`notes`, "
            + "`elements`, "
            + "`drivers`, "
            + "`drv`, "
            + "`work`, "
            + "`insp`, "
            + "`has_program`, "
            + "`has_mileage`, "
            + "`has_cda`, "            
            + "`has_verify`, "
            + "`sgc_code`, "
            + "`show_sign`, "
            + "`sign_driv`, "
            + "`name_class`";

    private static final String setFlds = "mto_chk_type SET "
            + "`name` = ?1, "
            + "`short_name` = ?2, "
            + "`notes` = ?3, "
            + "`elements` = ?4, "
            + "`drivers` = ?5, "
            + "`drv` = ?6, "
            + "`work` = ?7, "
            + "`insp` = ?8, "
            + "`has_program` = ?9, "
            + "`has_mileage` = ?10, "
            + "`has_cda` = ?11, "            
            + "`has_verify` = ?12, "
            + "`sgc_code` = ?13, "
            + "`show_sign` = ?14, "
            + "`sign_driv` = ?15, "
            + "`name_class` = ?16";

    private void setFields(MtoChkType obj, MySQLQuery q) {
        q.setParam(1, obj.name);
        q.setParam(2, obj.shortName);
        q.setParam(3, obj.notes);
        q.setParam(4, obj.elements);
        q.setParam(5, obj.drivers);
        q.setParam(6, obj.drv);
        q.setParam(7, obj.work);
        q.setParam(8, obj.insp);
        q.setParam(9, obj.hasProgram);
        q.setParam(10, obj.hasMileage);
        q.setParam(11, obj.hasCda);
        q.setParam(12, obj.hasVerify);
        q.setParam(13, obj.sgcCode);
        q.setParam(14, obj.showSign);
        q.setParam(15, obj.signDriv);
        q.setParam(16, obj.nameClass);

    }

    public MtoChkType select(int id, Connection ep) throws Exception {
        MtoChkType obj = new MtoChkType();
        MySQLQuery q = new MySQLQuery("SELECT " + selFlds + " FROM mto_chk_type WHERE id = " + id);
        Object[] row = q.getRecord(ep);
        obj.name = MySQLQuery.getAsString(row[0]);
        obj.shortName = MySQLQuery.getAsString(row[1]);
        obj.notes = MySQLQuery.getAsString(row[2]);
        obj.elements = MySQLQuery.getAsBoolean(row[3]);
        obj.drivers = MySQLQuery.getAsBoolean(row[4]);
        obj.drv = MySQLQuery.getAsBoolean(row[5]);
        obj.work = MySQLQuery.getAsBoolean(row[6]);
        obj.insp = MySQLQuery.getAsBoolean(row[7]);
        obj.hasProgram = MySQLQuery.getAsBoolean(row[8]);
        obj.hasMileage = MySQLQuery.getAsBoolean(row[9]);
        obj.hasCda = MySQLQuery.getAsBoolean(row[10]);
        obj.hasVerify = MySQLQuery.getAsBoolean(row[11]);
        obj.sgcCode = MySQLQuery.getAsString(row[12]);
        obj.showSign = MySQLQuery.getAsBoolean(row[13]);
        obj.signDriv = MySQLQuery.getAsBoolean(row[14]);
        obj.nameClass = MySQLQuery.getAsString(row[15]);
        obj.id = id;
        return obj;
    }

//fin zona de reemplazo
    public int insert(MtoChkType obj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + setFlds);
        setFields(obj, q);
        return q.executeInsert(ep);
    }

    public void update(MtoChkType obj, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("UPDATE " + setFlds + " WHERE id = " + obj.id);
        setFields(obj, q);
        q.executeUpdate(ep);
    }

    public void delete(int id, Connection ep) throws Exception {
        new MySQLQuery("DELETE FROM mto_chk_type WHERE id = " + id).executeDelete(ep);
    }

    public String getEnumOptions(String fieldName) {
        if (fieldName.equals("type")) {
            return "gral=General&prof=Proforma 3";
        }
        return null;
    }

}
