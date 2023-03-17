package api.crm.model;

import api.BaseModel;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import api.MySQLCol;
import utilities.MySQLQuery;
import utilities.logs.LogUtils;

public class CrmTask extends BaseModel<CrmTask> implements Serializable{

    public String clientName;
    public String typeName;
    public String projectName;
    public String createName;
    public String typeRepeat;
    public Date newProgDate;

//inicio zona de reemplazo

    public Integer clientId;
    public Integer projectId;
    public String descShort;
    public Date progDate;
    public Date ejecDate;
    public String description;
    public Integer typeTaskId;
    public String priority;
    public boolean satisfactory;
    public Date remDate;
    public boolean emailSent;
    public BigDecimal lat;
    public BigDecimal lon;
    public Integer createdBy;
    public Integer creatorId;
    public Integer respId;
    public Integer vehicleId;
    public Integer storeId;
    public Integer indexId;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "client_id",
            "project_id",
            "desc_short",
            "prog_date",
            "ejec_date",
            "description",
            "type_task_id",
            "priority",
            "satisfactory",
            "rem_date",
            "email_sent",
            "lat",
            "lon",
            "created_by",
            "creator_id",
            "resp_id",
            "vehicle_id",
            "store_id",
            "index_id",
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, clientId);
        q.setParam(2, projectId);
        q.setParam(3, descShort);
        q.setParam(4, progDate);
        q.setParam(5, ejecDate);
        q.setParam(6, description);
        q.setParam(7, typeTaskId);
        q.setParam(8, priority);
        q.setParam(9, satisfactory);
        q.setParam(10, remDate);
        q.setParam(11, emailSent);
        q.setParam(12, lat);
        q.setParam(13, lon);
        q.setParam(14, createdBy);
        q.setParam(15, creatorId);
        q.setParam(16, respId);
        q.setParam(17, vehicleId);
        q.setParam(18, storeId);
        q.setParam(19, indexId);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        clientId = MySQLQuery.getAsInteger(row[0]);
        projectId = MySQLQuery.getAsInteger(row[1]);
        descShort = MySQLQuery.getAsString(row[2]);
        progDate = MySQLQuery.getAsDate(row[3]);
        ejecDate = MySQLQuery.getAsDate(row[4]);
        description = MySQLQuery.getAsString(row[5]);
        typeTaskId = MySQLQuery.getAsInteger(row[6]);
        priority = MySQLQuery.getAsString(row[7]);
        satisfactory = MySQLQuery.getAsBoolean(row[8]);
        remDate = MySQLQuery.getAsDate(row[9]);
        emailSent = MySQLQuery.getAsBoolean(row[10]);
        lat = MySQLQuery.getAsBigDecimal(row[11], false);
        lon = MySQLQuery.getAsBigDecimal(row[12], false);
        createdBy = MySQLQuery.getAsInteger(row[13]);
        creatorId = MySQLQuery.getAsInteger(row[14]);
        respId = MySQLQuery.getAsInteger(row[15]);
        vehicleId = MySQLQuery.getAsInteger(row[16]);
        storeId = MySQLQuery.getAsInteger(row[17]);
        indexId = MySQLQuery.getAsInteger(row[18]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "crm_task";
    }

    public static String getSelFlds(String alias) {
        return new CrmTask().getSelFldsForAlias(alias);
    }

    public static List<CrmTask> getList(MySQLQuery q, Connection conn) throws Exception {
        return new CrmTask().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new CrmTask().deleteById(id, conn);
    }

    public static List<CrmTask> getAll(Connection conn) throws Exception {
        return new CrmTask().getAllList(conn);
    }

//fin zona de reemplazo
    public void afterSelect(Connection con) throws Exception {
        clientName = new MySQLQuery("SELECT name FROM crm_client WHERE id = " + clientId).getAsString(con);
        typeName = new MySQLQuery("SELECT name FROM crm_type_task WHERE id = " + typeTaskId).getAsString(con);
        projectName = new MySQLQuery("SELECT name FROM crm_project WHERE id = " + projectId).getAsString(con);
        createName = new MySQLQuery("SELECT CONCAT(last_name, ' ',first_name) FROM employee WHERE id = " + createdBy).getAsString(con);
        typeRepeat = new MySQLQuery("SELECT t.repeat FROM crm_type_task t WHERE id = " + typeTaskId).getAsString(con);
    }

    public String getEnumOptions(String fieldName) {
        if (fieldName.equals("priority")) {
            return "l=Baja&m=Normal&h=Alta&u=Urgente";
        }
        return null;
    }
    
     public String[][] getEnumOptionsAsMatrix(String fieldName) {
        if (fieldName.equals("priority")) {
            return getEnumStrAsMatrix("l=Baja&m=Normal&h=Alta&u=Urgente");
        }
        return null;
    }

    public String getLogs(CrmTask orig, CrmTask obj, Connection conn) throws Exception {
        StringBuilder sb = new StringBuilder();

        if (orig != null) {
            sb.append("Se editó la actividad");
            int nov = 0;

            nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_TEXT, "Actividad", orig.descShort, obj.descShort);
            nov += LogUtils.getLogLine(conn, sb, "Proyecto", orig.projectId, obj.projectId, "SELECT `name` FROM crm_project WHERE id = " + orig.projectId);
            nov += LogUtils.getLogLine(conn, sb, "Tipo", orig.typeTaskId, obj.typeTaskId, "SELECT `name` FROM crm_type_task WHERE id = " + orig.typeTaskId);
            nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_DD_MM_YYYY_HH12_MM_A, "Programada", orig.progDate, obj.progDate);
            nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_DD_MM_YYYY_HH12_MM_A, "Ejecución", orig.ejecDate, obj.ejecDate);
            nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_TEXT, "Descripción", orig.description, obj.description);
            nov += LogUtils.getLogLine(sb, "Prioridad", orig.priority, obj.priority, MySQLQuery.getEnumOptionsAsMap(getEnumOptions("priority")).get(orig.priority));
            nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_BOOLEAN, "Satisfactorio", orig.satisfactory, obj.satisfactory);
            nov += LogUtils.getLogLine(sb, MySQLCol.TYPE_DD_MM_YYYY_HH12_MM_A, "Recordar", orig.remDate, obj.remDate);

            if (nov > 0) {
                return sb.toString();
            } else {
                return null;
            }
        } else {
            sb.append("Se creó la actividad");
            return sb.toString();
        }
    }

}
