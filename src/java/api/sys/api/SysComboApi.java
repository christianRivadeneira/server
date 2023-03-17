package api.sys.api;

import api.BaseAPI;
import api.sys.model.SimpleComboData;
import api.sys.model.SimpleComboRow;
import java.sql.Connection;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import metadata.model.Grid;
import metadata.model.GridCond;
import metadata.model.GridRequest;
import metadata.model.GridTable;
import metadata.model.GridTableCond;
import metadata.model.Table;
import model.system.SessionLogin;
import utilities.MySQLQuery;

@Path("/sysCombo")
public class SysComboApi extends BaseAPI {

    /*@GET
    @Path("/description")
    public Response get(@QueryParam("table") String tableName, @QueryParam("grkd\") String combo, @QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            return createResponse(SimpleComboRow.getById(tableName, id, conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }*/
    @GET
    @Path("/data")
    public Response getData(@QueryParam("table") String table, @QueryParam("grid") String gridName, @QueryParam("filter") String filter, @QueryParam("parentId") Integer parentId, @QueryParam("profileId") Integer profileId, @QueryParam("instanceId") Integer instanceId) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            Table t = Table.getByName(table);
            if (t == null) {
                throw new Exception("table " + table + " doesn't exist");
            }
            Grid grid = t.getGridByName(gridName);

            if (grid == null) {
                throw new Exception("grid " + gridName + " doesn't exist on table " + table);

            }

            GridRequest req = new GridRequest();
            req.tableName = table;
            req.gridName = gridName;
            req.profileId = profileId;
            if (parentId != null) {
                List<GridCond> conds = grid.conds;
                boolean found = false;
                for (int i = 0; i < conds.size(); i++) {
                    GridCond c = conds.get(i);
                    if (c.slotType != null && c.slotType.equals(GridCond.INT)) {
                        found = true;
                        break;
                    }
                }
                List<GridTable> tbls = grid.tbls;
                for (int i = 0; i < tbls.size(); i++) {
                    GridTable gt = tbls.get(i);
                    if (gt.fldId != null) {
                        if (gt.conds != null && !gt.conds.isEmpty()) {
                            for (int j = 0; j < gt.conds.size(); j++) {
                                GridTableCond c = gt.conds.get(j);
                                if (c.cond.equals(GridTableCond.FIXED)) {
                                    found = true;
                                    break;
                                }
                            }
                        }

                    }
                }
                if (found) {
                    req.ints.add(parentId);
                }
            }
            if (filter != null) {
                req.strings.add(filter);
            }
            if (instanceId != null) {
                useBillInstance(instanceId, conn);
            }
            Object[][] data = new MySQLQuery(grid.getQuery(req, sl.employeeId, null, conn)).getRecords(conn);

            SimpleComboData rta = new SimpleComboData();

            for (Object[] row : data) {
                SimpleComboRow r = new SimpleComboRow();
                rta.data.add(r);
                r.id = MySQLQuery.getAsInteger(row[0]);
                StringBuilder sb = new StringBuilder();
                for (int j = 1; j < grid.flds.size(); j++) {
                    if (grid.flds.get(j).toString) {
                        sb.append(row[j]);
                    }
                    if (j < grid.flds.size() - 1) {
                        sb.append(" - ");
                    }
                }
                r.label = sb.toString();
                grid.flds.get(0);
            }
            return createResponse(rta);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
