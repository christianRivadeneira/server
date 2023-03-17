package api.crm.model;

import api.BaseModel;
import java.sql.Connection;
import java.util.List;
import utilities.MySQLQuery;
import java.util.Date;

public class CrmTaskRev extends BaseModel<CrmTaskRev> {
//inicio zona de reemplazo

    public int taskId;
    public Date modifyDate;

    @Override
    protected String[] getFlds() {
        return new String[]{
            "task_id",
            "modify_date"
        };
    }

    @Override
    protected void prepareQuery(MySQLQuery q) {
        q.setParam(1, taskId);
        q.setParam(2, modifyDate);
    }

    @Override
    public void setRow(Object[] row) throws Exception {
        taskId = MySQLQuery.getAsInteger(row[0]);
        modifyDate = MySQLQuery.getAsDate(row[1]);
        id = MySQLQuery.getAsInteger(row[row.length - 1]);
    }

    @Override
    protected String getTblName() {
        return "crm_task_rev";
    }

    public static String getSelFlds(String alias) {
        return new CrmTaskRev().getSelFldsForAlias(alias);
    }

    public static List<CrmTaskRev> getList(MySQLQuery q, Connection conn) throws Exception {
        return new CrmTaskRev().getListFromQuery(q, conn);
    }

    public static void delete(int id, Connection conn) throws Exception {
        new CrmTaskRev().deleteById(id, conn);
    }

    public static List<CrmTaskRev> getAll(Connection conn) throws Exception {
        return new CrmTaskRev().getAllList(conn);
    }

//fin zona de reemplazo
    
    public static void updateDateByTask(Integer taskId, Connection conn) throws Exception {
        new MySQLQuery("UPDATE crm_task_rev SET modify_date = NOW() "
                + "WHERE task_id = " + taskId).executeUpdate(conn);        
    }
    
    public static void deleteByTask(Integer taskId, Connection conn) throws Exception {
        new MySQLQuery("DELETE FROM crm_task_rev WHERE task_id = " + taskId).executeUpdate(conn);        
    }
}