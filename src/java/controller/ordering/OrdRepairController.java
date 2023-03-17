package controller.ordering;

import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import utilities.dataTableHelper.DataColumn;
import utilities.dataTableHelper.DataRow;
import utilities.dataTableHelper.DataTable;

public class OrdRepairController {

    public static DataTable getMissPolledCylPQRs(int officeId, Date date, EntityManager em) throws Exception {
//        throw new Exception("Disabled by Developer");
        String str = "SELECT "
                + "p.serial, "//0
                + "p.creation_date, "//2
                + "p.regist_hour, "//3
                + "CONCAT(i.first_name, \" \", i.last_name), "//5
                + "CONCAT(i.address, IF(neigh.`name` IS NOT NULL, CONCAT(' ', neigh.`name`), '')), "//6
                + "IF(i.type = 'brand', 'Afiliado', IF(i.type = 'univ', 'Provisional', IF(i.type = 'app', 'App', null))), "//7
                + "p.id "//10
                + "FROM "
                + "ord_pqr_cyl AS p "
                + "INNER JOIN ord_technician AS t ON t.id = p.technician_id "
                + "INNER JOIN ord_contract_index AS i ON i.id = p.index_id "
                + "INNER JOIN ord_pqr_reason AS r ON r.id = p.pqr_reason "
                + "LEFT JOIN neigh ON neigh.id = i.neigh_id "
                + "WHERE "
                + "p.office_id = " + officeId + " AND "
                + "p.pqr_poll_id IS NOT NULL AND "
                + "p.pqr_anul_cause_id IS NULL AND "
                + "p.satis_poll_id IS NULL AND "
                + "p.attention_date = ?1";

        DataTable table = new DataTable();
        table.getColumns().add(new DataColumn("Serial", Integer.class, 6));//0
        table.getColumns().add(new DataColumn("Capturado", "dd/MM/yyyy", Date.class, 6));//2
        table.getColumns().add(new DataColumn("Hora", "HH:mm", Date.class, 6));//3
        table.getColumns().add(new DataColumn("Cliente", String.class, 12));//5
        table.getColumns().add(new DataColumn("Dirección", String.class, 16));//6
        Query q = em.createNativeQuery(str);
        q.setParameter(1, date);
        List<Object[]> result = q.getResultList();
        for (int i = 0; i < result.size(); i++) {
            DataRow row = new DataRow(6, 1);
            Object[] obs = result.get(i);
            row.setData(obs, 0, 5);
            row.getKeys().add(obs[6]);
            table.getData().add(row);
        }
        return table;
    }
}
