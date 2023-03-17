package api.sys.api;

import api.BaseAPI;
import api.sys.model.form.FormDto;
import api.sys.model.form.FormFieldComboDto;
import api.sys.model.form.FormFieldDto;
import api.sys.model.form.FormFieldEnumDto;
import api.sys.model.form.FormTabDto;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import metadata.model.Field;
import metadata.model.Form;
import metadata.model.FormField;
import metadata.model.FormTab;
import metadata.model.GridCond;
import metadata.model.Table;
import metadata.model.Validation;
import utilities.MySQLQuery;

@Path("/sysFrm")
public class SysFrmApi extends BaseAPI {

    @GET
    @Path("/ids/")
    public Response getIds(@QueryParam("table") String table, @QueryParam("form") String formName, @QueryParam("fldName") String fldName, @QueryParam("id") int id) throws Exception {
        try (Connection conn = getConnection()) {
            List<Field> tree = new ArrayList<>();
            List<FormTab> tabs = Table.getByName(table).getFormByName(formName).tabs;
            for (int i = 0; i < tabs.size(); i++) {
                FormTab tab = tabs.get(i);
                for (int j = 0; j < tab.flds.size(); j++) {
                    FormField ff = tab.flds.get(j);
                    Field f = Field.getById(ff.fldId);
                    if (f.name.equals(fldName)) {
                        while (ff.cmbParentFldId != null) {
                            ff = tab.flds.get(--j);
                            Field fff = Field.getById(ff.fldId);
                            //si el parent sigue en la misma tabla signfica que está subiendo por el arbol
                            if (fff.tblName.equals(table)) {
                                break;
                            }
                            tree.add(fff);
                        }
                        break;
                    }
                }
            }

            Object[][] rta = new Object[tree.size()][2];
            for (int i = 0; i < tree.size(); i++) {
                Field f = tree.get(i);
                MySQLQuery q = new MySQLQuery("SELECT " + f.name + " FROM " + f.tblName + " WHERE id = ?1");
                q.setParam(1, id);
                //System.out.println(q.getParametrizedQuery());
                id = q.getAsInteger(conn);
                rta[i][0] = f.name;
                rta[i][1] = id;
            }
            return createResponse(rta);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/enumOpts")
    public Response getEnumOpts(@QueryParam("table") String table, @QueryParam("field") String field) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Table t = Table.getByName(table);
            Field f = t.getFieldByName(field);
            return createResponse(f.emunOpts);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    
    @GET
    public Response getAll(@QueryParam("table") String table, @QueryParam("form") String formName) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Table tbl = Table.getByName(table);
            Form form = tbl.getFormByName(formName);
            if (form == null) {
                throw new Exception("no se encontró el frm: " + formName);
            }
            FormDto rta = new FormDto();
            rta.name = form.name;
            rta.tblName = form.tblName;
            rta.customNewTitle = form.customNewTitle;
            rta.customEditTitle = form.customEditTitle;
            rta.male = tbl.male;
            rta.singular = tbl.singular;
            rta.plural = tbl.singular;
            rta.activeFldName = tbl.activeFldId != null ? Field.getById(tbl.activeFldId).name : null;

            rta.selectPath = form.selectPath;
            rta.insertPath = form.insertPath;
            rta.updatePath = form.updatePath;

            for (int i = 0; i < form.tabs.size(); i++) {
                FormTab tab = form.tabs.get(i);
                FormTabDto tabDto = new FormTabDto();
                tabDto.label = tab.label;
                tabDto.name = tab.name;
                rta.tabs.add(tabDto);

                for (int j = 0; j < tab.flds.size(); j++) {
                    FormField ff = tab.flds.get(j);
                    Field f = Field.getById(ff.fldId);
                    if (f == null) {
                        throw new Exception("Field " + ff.fldId + " not found");
                    }

                    boolean render = (ff.readFromParent == null || !ff.readFromParent) && ff.slotNum == null && ff.fixedBoolean == null && ff.fixedEnum == null;

                    if (ff.readFromParent != null && ff.readFromParent) {
                        rta.fromParentFldName = f.name;
                    }

                    FormFieldDto ffDto = new FormFieldDto();
                    ffDto.fixedBoolean = ff.fixedBoolean;
                    ffDto.fixedEnum = ff.fixedEnum;

                    if (ff.slotNum != null) {
                        ffDto.slotNum = ff.slotNum;
                        ffDto.slot = GridCond.getSlotType(f);
                    }
                    ffDto.editable = ff.editable;
                    ffDto.titleCase = ff.titleCase;
                    ffDto.validations = f.validations;
                    tabDto.flds.add(ffDto);
                    ffDto.label = f.label != null && !f.label.isEmpty() ? f.label : (f.fkTblName != null && !f.fkTblName.isEmpty() ? Table.getByName(f.fkTblName).singular : "");
                    ffDto.name = f.name;

                    if ((ff.required || !f.nullable) && render) {
                        ffDto.validations.add(new Validation(Validation.REQUIRED));
                    }

                    if (f.emunOpts != null && render) {
                        FormFieldEnumDto e = new FormFieldEnumDto();
                        ffDto.enumOpts = e;
                        e.data = f.emunOpts;
                        e.male = tbl.male;
                    }

                    ffDto.type = f.type;
                    if (f.fk && render) {
                        FormFieldComboDto cmb = new FormFieldComboDto();
                        ffDto.cmb = cmb;
                        cmb.male = Table.getByName(f.fkTblName).male;
                        cmb.billing = Table.getByName(f.fkTblName).billing;
                        cmb.isLeaf = tbl.name.equals(f.tblName);
                        cmb.gridName = ff.cmbGrid != null && !ff.cmbGrid.isEmpty() ? ff.cmbGrid : "combo";
                        if (ff.cmbParentFldId != null) {
                            Field parF = Field.getById(ff.cmbParentFldId);
                            cmb.parentName = parF.name;
                        }
                        cmb.tableName = f.fkTblName;
                    }
                }
            }
            return createResponse(rta);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

}
