package api.dto.model;

import java.sql.Connection;
import utilities.MySQLQuery;

public class DtoSalesmanLog {

    public Integer salesmanId;
    public String type;
    public String notes;

    public int insert(DtoSalesmanLog obj, int employeeId, Connection ep) throws Exception {
        MySQLQuery q = new MySQLQuery("INSERT INTO dto_salesman_log SET "
                + "`employee_id` = " + employeeId + ", "
                + "`log_date` = NOW(), "
                + "`salesman_id` = ?1,"
                + "`type` = ?2, "
                + "`notes` = '" + obj.notes + "' ");
        q.setParam(1, obj.salesmanId);
        q.setParam(2, obj.type);
        return q.executeInsert(ep);
    }
}
