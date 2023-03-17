package api.sys.api;

import api.BaseAPI;
import api.GridResult;
import api.sys.model.grid.GridActionDto;
import api.sys.model.grid.GridFilter;
import api.sys.model.grid.GridFilterComboDto;
import api.sys.model.grid.GridFilterEnumDto;
import api.sys.model.grid.GridGroupDto;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import metadata.model.Field;
import metadata.model.Grid;
import metadata.model.GridAction;
import metadata.model.GridCond;
import metadata.model.GridFld;
import metadata.model.GridGroup;
import metadata.model.GridRequest;
import metadata.model.Table;
import model.system.SessionLogin;
import utilities.mysqlReport.MySQLReport;

@Path("/sysGrid")
public class SysGridApi extends BaseAPI {

    @POST
    @Path("/getData")
    public Response get(GridRequest req) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            if (getBillInstId() != null) {
                useBillInstance(conn);
            }
            GridResult t = Grid.getApiTable(req, sl.employeeId, conn);
            return createResponse(t);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getGrp")
    public Response getGrp(@QueryParam("table") String table, @QueryParam("group") String group) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            Table tbl = Table.getByName(table);
            GridGroup grp = tbl.getGroupByName(group);
            if (grp == null) {
                throw new Exception("La tabla " + tbl.name + " no tiene un grupo " + group);
            }
            GridGroupDto rta = new GridGroupDto();
            rta.label = grp.label;
            rta.name = grp.name;
            rta.grids = new ArrayList<>();

            //getDto(tbl, grid)            
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getInfo")
    public Response getInfo(@QueryParam("table") String table, @QueryParam("grid") String grid) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Table tbl = Table.getByName(table);
            if (tbl == null) {
                throw new Exception("La tabla:" + table + " no existe.");
            }
            return createResponse(getDto(tbl, grid));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    private static GridDto getDto(Table tbl, String grid) throws Exception {
        Grid g = tbl.getGridByName(grid);
        if (g == null) {
            throw new Exception("La tabla " + tbl.name + " no tiene un grid " + grid);
        }
        GridDto dto = new GridDto();
        dto.plural = tbl.plural;
        dto.singular = tbl.singular;
        dto.dataPath = g.dataPath;
        dto.deletePath = g.deletePath;
        dto.customTitle = g.label;

        for (int i = 0; i < g.conds.size(); i++) {
            GridCond c = g.conds.get(i);
            if (c.readFromParentId != null && c.readFromParentId) {
                dto.parentIdSlot = c.slot;
            }
            if (c.readFromTextFilter != null && c.readFromTextFilter) {
                dto.textFilterSlot = c.slot;
            }
        }

        for (int i = 0; i < g.conds.size(); i++) {
            GridCond c = g.conds.get(i);
            if (c.readFromFilter != null && c.readFromFilter) {
                GridFilter flt = new GridFilter();
                if (dto.filters == null) {
                    dto.filters = new ArrayList<>();
                }
                Field fld = Field.getById(c.fldId);
                flt.name = fld.name;
                if (fld.label != null) {
                    flt.label = fld.label;
                } else if (fld.fkTblName != null && !fld.fkTblName.isEmpty()) {
                    flt.label = Table.getByName(fld.fkTblName).singular;
                }
                flt.slot = c.slot;

                if (fld.type.equals(Field.BOOLEAN) && c.comparison.equals(GridCond.EQUALS)) {
                    flt.type = GridFilter.BOOLEAN;
                } else if (fld.type.equals(Field.INTEGER) && fld.fk && c.comparison.equals(GridCond.EQUALS)) {
                    flt.type = GridFilter.CMB;

                    flt.cmb = new GridFilterComboDto();
                    flt.cmb.gridName = c.cmbGridName;
                    flt.cmb.male = Table.getByName(fld.fkTblName).male;
                    flt.cmb.tableName = fld.fkTblName;

                } else if (fld.type.equals(Field.DATE) || fld.type.equals(Field.DATE_TIME)) {
                    switch (c.comparison) {
                        case GridCond.ON_DAY:
                            flt.type = GridFilter.DAY;
                            break;
                        case GridCond.ON_MONTH:
                            flt.type = GridFilter.MONTH;
                            break;
                        case GridCond.ON_YEAR:
                            flt.type = GridFilter.YEAR;
                            break;
                        default:
                            throw new RuntimeException("El filtro no soporta la comparación " + c.comparison);
                    }
                } else if (fld.type.equals(Field.ENUM) && c.comparison.equals(GridCond.EQUALS)) {
                    flt.type = GridFilter.ENUM;
                    flt.enumOpts = new GridFilterEnumDto();
                    flt.enumOpts.male = Table.getByName(fld.tblName).male;
                    flt.enumOpts.data = fld.emunOpts;

                } else {
                    throw new RuntimeException("");
                }
                dto.filters.add(flt);
            }
        }

        dto.actions = new ArrayList<>();
        for (int i = 0; i < g.accs.size(); i++) {
            GridAction a = g.accs.get(i);
            dto.actions.add(getDto(tbl, g, a));

        }
        return dto;
    }

    private static void addEnumMenu(Grid g, GridAction a, GridActionDto ad) throws Exception {
        if (a.enumRouteFldId != null) {
            ad.type = GridAction.MENU;
            String[][] opts = Field.getById(a.enumRouteFldId).emunOpts;
            ad.opts = new ArrayList<>();
            for (String[] enumOpt : opts) {
                String key = enumOpt[0];
                String val = enumOpt[1];
                GridActionDto opt = new GridActionDto();
                ad.opts.add(opt);
                opt.global = true;
                opt.label = val;
                opt.newFormName = a.newFormName + "_" + key;
                opt.tableName = a.tableName != null && !a.tableName.isEmpty() ? a.tableName : g.tblName;
                opt.type = GridAction.ADD;
            }
        } else {
            ad.type = a.type;
        }
    }

    private static GridActionDto getDto(Table tbl, Grid g, GridAction a) throws Exception {
        GridActionDto ad = new GridActionDto();
        if (a.type.equals(GridAction.ADD)) {
            ad.global = true;
            ad.label = "Agregar";
            ad.deskIcon = "/icons/plus.png";
            ad.webIcon = "fa-plus";
            if (a.tableName == null || a.tableName.isEmpty()) {
                ad.tableName = tbl.name;
            }
            if (a.newFormName == null || a.newFormName.isEmpty()) {
                ad.newFormName = "main";
            }
            addEnumMenu(g, a, ad);
        } else if (a.type.equals(GridAction.EDIT)) {
            ad.type = a.type;
            ad.global = false;
            ad.label = "Editar";
            ad.deskIcon = "/icons/edit.png";
            ad.webIcon = "fa-edit";

            if (a.enumRouteFldId != null) {
                for (int j = 0; j < g.flds.size(); j++) {
                    GridFld f = g.flds.get(j);
                    if (f.isKey && f.fldId.equals(a.enumRouteFldId)) {
                        ad.enumRouteKeyPos = j;
                        break;
                    }
                }
                if (a.enumRouteFldId == null) {
                    throw new Exception("Field " + Field.getById(a.enumRouteFldId).name + " must be present as a key on columns list");
                }
            }

            if (a.tableName == null || a.tableName.isEmpty()) {
                ad.tableName = tbl.name;
            }
            if (a.editFormName == null || a.editFormName.isEmpty()) {
                ad.editFormName = "main";
            }
            ad.apiRowIndex = a.apiRowIndex;
        } else if (a.type.equals(GridAction.REMOVE)) {
            ad.type = a.type;
            ad.global = false;
            ad.label = "Eliminar";
            ad.deskIcon = "/icons/trash.png";
            ad.webIcon = "fa-trash-alt";
            ad.apiRowIndex = a.apiRowIndex;
        } else if (a.type.equals(GridAction.SPACE)) {
            ad.type = a.type;
            ad.global = false;
        } else if (a.type.equals(GridAction.EXPORT)) {
            ad.type = a.type;
            ad.global = true;
            ad.deskIcon = "/icons/export.png";
            ad.webIcon = "fa-table";
            ad.label = "Exportar";
        } else if (a.type.equals(GridAction.LOGS)) {
            ad.type = a.type;
            ad.global = false;
            ad.deskIcon = "/icons/history.png";
            ad.webIcon = "fa-binoculars";
            ad.label = "Logs";
            ad.apiRowIndex = a.apiRowIndex;
            ad.tableName = a.tableName;
        } else if (a.type.equals(GridAction.ATTACHMENTS)) {
            ad.type = a.type;
            ad.global = false;
            ad.deskIcon = "/icons/clip.png";
            ad.label = "Adjuntos";
            ad.webIcon = "fa-clipboard";
            ad.apiRowIndex = a.apiRowIndex;
        } else if (a.type.equals(GridAction.MENU)) {
        } else if (a.type.equals(GridAction.GENERATED)) {
            ad.type = a.type;
            ad.global = a.global;
//            ad.gridGroupName = a.gridGroupName;
            ad.editFormName = a.editFormName;
            ad.newFormName = a.newFormName;
            ad.gridName = a.gridName;
            ad.tableName = a.tableName;
            ad.label = a.label;
            ad.enumRouteKeyPos = a.enumRouteKeyPos;
            if (a.deskIcon == null || a.deskIcon.isEmpty()) {
                ad.deskIcon = "/icons/list.png";
            } else {
                ad.deskIcon = a.deskIcon;
            }
            if (a.webIcon == null || a.webIcon.isEmpty()) {
                ad.webIcon = "/icons/list.png";
            } else {
                ad.webIcon = a.webIcon;
            }
            addEnumMenu(g, a, ad);
        } else if (a.type.equals(GridAction.CUSTOM)) {
            ad.label = a.label;
            ad.name = a.name;
            ad.global = a.global;
            if (a.deskIcon == null || a.deskIcon.isEmpty()) {
                ad.deskIcon = "/icons/list.png";
            } else {
                ad.deskIcon = a.deskIcon;
            }
            if (a.webIcon == null || a.webIcon.isEmpty()) {
                ad.webIcon = "/icons/list.png";
            } else {
                ad.webIcon = a.webIcon;
            }
            addEnumMenu(g, a, ad);
        } else if (a.type.equals(GridAction.API_CALL)) {
            ad.type = GridAction.API_CALL;
            ad.apiResponse = a.apiResponse;
            ad.global = a.global;
            ad.label = a.label;
            ad.name = null;
            if (a.deskIcon == null || a.deskIcon.isEmpty()) {
                ad.deskIcon = "/icons/list.png";
            } else {
                ad.deskIcon = a.deskIcon;
            }
            if (a.webIcon == null || a.webIcon.isEmpty()) {
                ad.webIcon = "/icons/list.png";
            } else {
                ad.webIcon = a.webIcon;
            }

            ad.apiEndPoint = a.apiEndPoint;
            ad.apiConfirmDialog = a.apiConfirmDialog;
            ad.apiReload = a.apiReload;
            ad.apiResponse = a.apiResponse;
            ad.apiMethod = a.apiMethod;
            ad.apiRowIndex = a.apiRowIndex;
        }
        return ad;
    }

    @POST
    @Produces(value = "application/vnd.ms-excel")
    @Path("/exportExcel")
    public Response exportExcel(GridRequest req) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            GridResult gridResult = Grid.getApiTable(req, sl.employeeId, conn);
            Table tbl = Table.getByName(req.tableName);
            if (tbl == null) {
                throw new Exception("La tabla:" + req.tableName + " no existe.");
            }            

            String title = tbl.plural;
            MySQLReport rep = new MySQLReport(title, null, "Hoja 1", now(conn));
            rep.setZoomFactor(85);
            rep.setHorizontalFreeze(0);
            rep.setVerticalFreeze(0);
            rep.setTitle(title);
            rep.setCreation(new Date());
            rep.setShowNumbers(true);//por el indicador de registros
            if (gridResult.data != null && gridResult.data.length == 0) {
                throw new Exception("No hay datos que mostrar.");
            }
            rep.getSubTitles().add("Hay " + gridResult.data.length + " Registros");
            rep.getTables().add(new utilities.mysqlReport.Table(title));
            rep = MySQLReport.getReport(rep, gridResult.cols, gridResult.data);
            return createResponse(rep.write(conn), title + ".xls");
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
