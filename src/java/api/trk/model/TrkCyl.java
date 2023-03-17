package api.trk.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.math.BigDecimal;
import java.util.Date;

// Salable puede entenderse como !chk_reserve
//resp null	Salable 0	Vendido
//resp null	Salable 1	Libre, debloqueado por plataforma
//resp no null	Salable 0	Reservado para alguien
//resp no null	Salable 1	Si se presenta, es un Bug


public class TrkCyl extends BaseModel<TrkCyl> {

    //Fuera de la zona de reemplazo
    public String typeName; //Nombre de la capacidad
    //---------------------------------------------------

    //inicio zona de reemplazo
    public int nifY;
    public int nifF;
    public int nifS;
    public boolean ok;
    public boolean active;
    public String notes;
    public Date fabDate;
    public int cylTypeId;
    public Integer factoryId;
    public boolean imported;
    public int facLen;
    public Date usedDate;
    public String typeLabel;
    public boolean hasLabel;
    public Date createDate;
    public Date lastVerify;
    public int empVerifier;
    public BigDecimal tara;
    public boolean salable;
    public Integer respId;
    public boolean minasReported;
    public Integer invCenterId;
    public boolean suiReported;
    public BigDecimal c3;
    public BigDecimal c4;
    public BigDecimal c5;
    public BigDecimal agua;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "nif_y",
            "nif_f",
            "nif_s",
            "ok",
            "active",
            "notes",
            "fab_date",
            "cyl_type_id",
            "factory_id",
            "imported",
            "fac_len",
            "used_date",
            "type_label",
            "has_label",
            "create_date",
            "last_verify",
            "emp_verifier",
            "tara",
            "salable",
            "resp_id",
            "minas_reported",
            "inv_center_id",
            "sui_reported",
            "c_3",
            "c_4",
            "c_5",
            "agua"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, nifY);
        q.setParam(2, nifF);
        q.setParam(3, nifS);
        q.setParam(4, ok);
        q.setParam(5, active);
        q.setParam(6, notes);
        q.setParam(7, fabDate);
        q.setParam(8, cylTypeId);
        q.setParam(9, factoryId);
        q.setParam(10, imported);
        q.setParam(11, facLen);
        q.setParam(12, usedDate);
        q.setParam(13, typeLabel);
        q.setParam(14, hasLabel);
        q.setParam(15, createDate);
        q.setParam(16, lastVerify);
        q.setParam(17, empVerifier);
        q.setParam(18, tara);
        q.setParam(19, salable);
        q.setParam(20, respId);
        q.setParam(21, minasReported);
        q.setParam(22, invCenterId);
        q.setParam(23, suiReported);
        q.setParam(24, c3);
        q.setParam(25, c4);
        q.setParam(26, c5);
        q.setParam(27, agua);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        nifY = MySQLQuery.getAsInteger(row[0]);
        nifF = MySQLQuery.getAsInteger(row[1]);
        nifS = MySQLQuery.getAsInteger(row[2]);
        ok = MySQLQuery.getAsBoolean(row[3]);
        active = MySQLQuery.getAsBoolean(row[4]);
        notes = MySQLQuery.getAsString(row[5]);
        fabDate = MySQLQuery.getAsDate(row[6]);
        cylTypeId = MySQLQuery.getAsInteger(row[7]);
        factoryId = MySQLQuery.getAsInteger(row[8]);
        imported = MySQLQuery.getAsBoolean(row[9]);
        facLen = MySQLQuery.getAsInteger(row[10]);
        usedDate = MySQLQuery.getAsDate(row[11]);
        typeLabel = MySQLQuery.getAsString(row[12]);
        hasLabel = MySQLQuery.getAsBoolean(row[13]);
        createDate = MySQLQuery.getAsDate(row[14]);
        lastVerify = MySQLQuery.getAsDate(row[15]);
        empVerifier = MySQLQuery.getAsInteger(row[16]);
        tara = MySQLQuery.getAsBigDecimal(row[17], false);
        salable = MySQLQuery.getAsBoolean(row[18]);
        respId = MySQLQuery.getAsInteger(row[19]);
        minasReported = MySQLQuery.getAsBoolean(row[20]);
        invCenterId = MySQLQuery.getAsInteger(row[21]);
        suiReported = MySQLQuery.getAsBoolean(row[22]);
        c3 = MySQLQuery.getAsBigDecimal(row[23], false);
        c4 = MySQLQuery.getAsBigDecimal(row[24], false);
        c5 = MySQLQuery.getAsBigDecimal(row[25], false);
        agua = MySQLQuery.getAsBigDecimal(row[26], false);
        
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "trk_cyl";
    }

    public static String getSelFlds(String alias) {
        return new TrkCyl().getSelFldsForAlias(alias);
    }

    public static List<TrkCyl> getList(MySQLQuery q, Connection conn) throws Exception {
        return new TrkCyl().getListFromQuery(q, conn);
    }

//fin zona de reemplazo
    
    @Override
    public String toString() {
        return String.format("%02d-%04d-%06d", nifY, nifF, nifS);
    }

    public static TrkCyl selectByNif(String nif, Connection conn) throws Exception {
        Integer[] parts = getNifParts(nif);
        String q = "SELECT " + TrkCyl.getSelFlds("") + " FROM trk_cyl WHERE nif_y = " + parts[0] + " AND nif_f = " + parts[1] + " AND nif_s = " + parts[2];
        return new TrkCyl().select(new MySQLQuery(q), conn);
    }

    public static Integer[] getNifParts(String nif) throws Exception {
        Integer y = null;
        Integer f = null;
        Integer s = null;
        if (nif.matches("[0-9]+-[0-9]+")) {
            nif = nif.replaceAll("-", "");
            if (nif.length() < 9) {
                throw new Exception("La etiqueta no cumple con el formato");
            }
            String fs = nif.substring(2, nif.length() - 6);
            y = Integer.valueOf(nif.substring(0, 2));
            f = Integer.valueOf(fs);
            s = Integer.valueOf(nif.substring(nif.length() - 6));
        } else if (nif.matches("[0-9]+")) {
            if (nif.length() < 9) {
                throw new Exception("La etiqueta no cumple con el formato");
            }
            String fs = nif.substring(2, nif.length() - 6);
            y = Integer.valueOf(nif.substring(0, 2));
            f = Integer.valueOf(fs);
            s = Integer.valueOf(nif.substring(nif.length() - 6));
        } else if (nif.matches("[0-9][0-9]-[0-9]+-[0-9]+")) {
            String[] parts = nif.split("-");
            y = Integer.valueOf(parts[0]);
            f = Integer.valueOf(parts[1]);
            s = Integer.valueOf(parts[2]);
        } else {
            throw new Exception("La etiqueta no cumple con el formato");
        }
        return new Integer[]{y, f, s};
    }

}
