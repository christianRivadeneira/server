package api.est.model;

import api.BaseModel;
import api.Params;
import java.io.Serializable;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import utilities.MySQLQuery;

public class EstMto extends BaseModel<EstMto> implements Serializable {

//inicio zona de reemplazo

    public Date progDate;
    public Date execDate;
    public String type;
    public String notes;
    public int tankId;
    public String certificate;
    public Integer importId;
    public Date lastPar;
    public Date lastTot;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "prog_date",
            "exec_date",
            "type",
            "notes",
            "tank_id",
            "certificate",
            "import_id",
            "last_par",
            "last_tot"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, progDate);
        q.setParam(2, execDate);
        q.setParam(3, type);
        q.setParam(4, notes);
        q.setParam(5, tankId);
        q.setParam(6, certificate);
        q.setParam(7, importId);
        q.setParam(8, lastPar);
        q.setParam(9, lastTot);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        progDate = MySQLQuery.getAsDate(row[0]);
        execDate = MySQLQuery.getAsDate(row[1]);
        type = MySQLQuery.getAsString(row[2]);
        notes = MySQLQuery.getAsString(row[3]);
        tankId = MySQLQuery.getAsInteger(row[4]);
        certificate = MySQLQuery.getAsString(row[5]);
        importId = MySQLQuery.getAsInteger(row[6]);
        lastPar = MySQLQuery.getAsDate(row[7]);
        lastTot = MySQLQuery.getAsDate(row[8]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "est_mto";
    }

    public static String getSelFlds(String alias) {
        return new EstMto().getSelFldsForAlias(alias);
    }

    public static List<EstMto> getList(MySQLQuery q, Connection conn) throws Exception {
        return new EstMto().getListFromQuery(q, conn);
    }

    public static List<EstMto> getList(Params p, Connection conn) throws Exception {
        return new EstMto().getListFromParams(p, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new EstMto().deleteById(id, conn);
    }

    public static List<EstMto> getAll(Connection conn) throws Exception {
        return new EstMto().getAllList(conn);
    }

//fin zona de reemplazo
    public String getEnumOptions(String fieldName) {
        if (fieldName.equals("type")) {
            return "tot=Total&par=Parcial&other=Otro";
        }
        return null;
    }

    public String[][] getEnumOptionsAsMatrix(String fieldName) {
        if (fieldName.equals("type")) {
            return getEnumStrAsMatrix("tot=Total&par=Parcial&other=Otro");
        }
        return null;
    }

}
