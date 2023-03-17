package api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import metadata.model.Field;
import metadata.model.Table;
import utilities.MySQLQuery;
import utilities.json.JSONDecoder;
import utilities.json.JSONEncoder;
import web.ShortException;

public abstract class BaseModel<T extends BaseModel> {

    public int id;

    public T newInstance() throws Exception {
        return (T) getClass().newInstance();
    }

    protected abstract void prepareQuery(MySQLQuery q);

    protected abstract void setRow(Object[] row) throws Exception;

    protected abstract String[] getFlds();

    protected abstract String getTblName();

    protected String getSelFlds() {
        return getSelFldsForAlias(null);
    }

    public String getSetFlds() throws Exception {
        StringBuilder sb = new StringBuilder();
        String[] flds = getFlds();
        for (int i = 0; i < flds.length; i++) {
            String fld = flds[i];
            sb.append("`").append(fld).append("` = ?").append(i + 1).append("");
            if (i < flds.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }

    public T select(int id, Connection conn) throws Exception {
        Object[] row = new MySQLQuery("SELECT " + getSelFlds() + ", id FROM " + getTblName() + " WHERE id = " + id).getRecord(conn);
        if (row != null) {
            setRow(row);
            afterSelect(conn);
            return (T) this;
        } else {
            return null;
        }
    }

    public T select(MySQLQuery q, Connection conn) throws Exception {
        Object[] row = q.getRecord(conn);
        if (row != null) {
            setRow(row);
            afterSelect(conn);
            return (T) this;
        } else {
            return null;
        }
    }

    private MySQLQuery getQuery(Params pars) throws Exception {
        String str = "SELECT " + getSelFlds() + " FROM " + getTblName() + " WHERE ";

        String[] paramFlds = pars.paramsMap.keySet().toArray(new String[pars.paramsMap.size()]);
        for (int i = 0; i < paramFlds.length; i++) {
            if (i > 0) {
                str += " AND ";
            }
            str += "`" + paramFlds[i] + "` = ?" + i + " ";
        }

        if (!pars.sortMap.isEmpty()) {
            str += "ORDER BY ";
            String[] sortFlds = pars.sortMap.keySet().toArray(new String[pars.sortMap.size()]);
            for (int i = 0; i < sortFlds.length; i++) {
                if (i > 0) {
                    str += " , ";
                }
                str += "`" + sortFlds[i] + "` " + (pars.sortMap.get(sortFlds[i]) == Params.ASC ? "ASC" : "DESC") + " ";
            }
        }

        MySQLQuery q = new MySQLQuery(str);
        for (int i = 0; i < paramFlds.length; i++) {
            q.setParam(i, pars.paramsMap.get(paramFlds[i]));
        }
        return q;
    }

    public T select(Params pars, Connection conn) throws Exception {
        return select(getQuery(pars), conn);
    }

    public int insert(Connection conn) throws Exception {
        MySQLQuery q = new MySQLQuery("INSERT INTO " + getTblName() + " SET " + getSetFlds());
        prepareQuery(q);
        int nId = q.executeInsert(conn);
        this.id = nId;
        return nId;
    }

    public void update(Connection conn) throws Exception {
        MySQLQuery q = new MySQLQuery("UPDATE " + getTblName() + " SET " + getSetFlds() + " WHERE id = " + id);
        prepareQuery(q);
        int d = q.executeUpdate(conn);
        if (d <= 0) {
            throw new ShortException("El registro no existe");
        }
    }

    protected void deleteById(int delId, Connection conn) throws Exception {
        Table tbl = Table.getByName(getTblName());
        int d;
        if (tbl != null && tbl.activeFldId != null) {
            Field fld = tbl.getFieldById(tbl.activeFldId);
            d = new MySQLQuery("UPDATE " + getTblName() + " SET " + fld.name + " = 0 WHERE id = " + delId).executeDelete(conn);
        } else {
            d = new MySQLQuery("DELETE FROM " + getTblName() + " WHERE id = " + delId).executeDelete(conn);
        }
        if (d <= 0) {
            throw new ShortException("El registro no existe");
        }
    }

    protected void disableById(int delId, Connection conn) throws Exception {
        int d = new MySQLQuery("UPDATE " + getTblName() + " SET active = 0 WHERE id = " + delId).executeDelete(conn);
        if (d <= 0) {
            throw new ShortException("El registro no existe");
        }
    }

    protected List<T> getListFromQuery(MySQLQuery q, Connection conn) throws Exception {
        Object[][] data = q.getRecords(conn);
        List<T> rta = new ArrayList<>();
        for (Object[] row : data) {
            T o = (T) newInstance();
            o.setRow(row);
            o.afterSelect(conn);
            rta.add(o);
        }
        return rta;
    }

    public List<T> getListFromParams(Params pars, Connection conn) throws Exception {
        return getListFromQuery(getQuery(pars), conn);
    }

    protected List<T> getAllList(Connection conn) throws Exception {
        return getListFromQuery(new MySQLQuery("SELECT " + getSelFlds() + " FROM " + getTblName()), conn);
    }

    protected String getSelFldsForAlias(String alias) {
        StringBuilder sb = new StringBuilder();
        String[] flds = getFlds();
        for (String fld : flds) {
            if (alias != null && !alias.isEmpty()) {
                sb.append(alias).append(".");
            }
            sb.append("`").append(fld).append("`").append(",");
        }
        if (alias != null && !alias.isEmpty()) {
            sb.append(alias).append(".");
        }
        sb.append("id ");
        return sb.toString();
    }

    public void afterSelect(Connection con) throws Exception {
    }

    public static String[][] getEnumStrAsMatrix(String options) {
        String[] parts = options.split("&");
        String[][] res = new String[parts.length][];
        for (int i = 0; i < parts.length; i++) {
            String[] sbParts = parts[i].split("=");
            res[i] = new String[]{sbParts[0].trim(), sbParts[1].trim()};
        }
        return res;
    }

    public String[][] getEnumOpts(String fld) throws Exception {
        return Table.getByName(getTblName()).getFieldByName(fld).emunOpts;
    }

    public T duplicate() throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JSONEncoder.encode(this, baos, false);
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        JSONDecoder dec = new JSONDecoder();
        return (T) dec.getObject(bais, this.getClass());
    }    
}
