package api.sys.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.sys.dto.AppProfileRequest;
import api.sys.dto.Module;
import api.sys.dto.Option;
import api.sys.model.Menu;
import java.sql.Connection;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import api.sys.model.Profile;
import java.util.ArrayList;
import java.util.List;
import metadata.model.GridRequest;
import model.system.SessionLogin;
import utilities.MySQLQuery;

@Path("/profile")
public class ProfileApi extends BaseAPI {

    @POST
    public Response insert(Profile obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(Profile obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            obj.update(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    public Response get(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Profile obj = new Profile().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Profile.delete(id, conn);
            return createResponse();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getAll")
    public Response getAll() {
        try (Connection conn = getConnection()) {
            getSession(conn);
            return createResponse(Profile.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getProfiles")
    public Response getProfiles() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);

            MySQLQuery mqProfs = new MySQLQuery(Profile.getProfilesByEmployeeQuery(sl.employeeId));
            MySQLQuery mqMods = new MySQLQuery(Menu.getModsByUsrQuery(sl.employeeId));

            List<Menu> mods = Menu.getList(mqMods, conn);
            List<Profile> profs = Profile.getList(mqProfs, conn);

            List<Module> modules = new ArrayList<>();
            if (profs.isEmpty()) {
                throw new Exception("Ud no tiene autorización para ningún perfíl. Debe comunicarse con el encargado.");
            } else {
                for (int i = 0; i < mods.size(); i++) {
                    Menu menu = mods.get(i);
                    Module mod = new Module();
                    mod.name = menu.label;
                    mod.color = menu.color;
                    mod.webIcon = menu.webIcon;
                    mod.place = menu.place;
                    mod.webPath = menu.webPath;
                    mod.moduleId = menu.id;

                    List<Option> profiles = new ArrayList<>();
                    for (Profile prof : profs) {
                        if (menu.id == prof.menuId) {
                            Option opt = new Option();
                            opt.id = prof.id;
                            opt.name = prof.name;
                            opt.type = "prof";
                            opt.showBi = prof.showBi;
                            profiles.add(opt);
                        }
                    }
                    mod.options = profiles;
                    modules.add(mod);
                }
            }
            return createResponse(modules);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/modulesAllow")
    public Response modulesAllow(GridRequest req) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            GridResult r = new GridResult();
            r.cols = new MySQLCol[]{
                new MySQLCol(MySQLCol.TYPE_KEY),
                new MySQLCol(MySQLCol.TYPE_TEXT, 75, "Nombre")};

            r.data = new MySQLQuery("SELECT "
                    + " m.id, "
                    + " m.label ,"
                    + " COUNT(p.menu_id) AS count_prof ,"
                    + " (SELECT COUNT(*) FROM profile p WHERE p.menu_id = m.id AND p.active) AS active_prof "
                    + " FROM menu AS m "
                    + " LEFT JOIN `profile` AS p ON p.menu_id = m.id AND p.is_mobile = false "
                    + " WHERE m.reg_type = 'mod' "
                    + " GROUP BY m.id "
                    + " HAVING count_prof > 0 AND active_prof > 0 ORDER BY m.label ASC"
            ).getRecords(conn);

            r.sortType = GridResult.SORT_ASC;
            r.sortColIndex = 1;

            return createResponse(r);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @POST
    @Path("/profileCfgApp")
    public Response getprofileCfgApp(AppProfileRequest obj) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);
            List<List<Object>> profilesApp = Profile.getProfilesApp(sl.employeeId, obj.tableName, obj.fields, conn);                       
            return Response.ok(profilesApp).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }
}
