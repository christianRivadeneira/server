package api.sys.api;

import api.BaseAPI;
import api.GridResult;
import api.MySQLCol;
import api.sys.dto.Module;
import api.sys.dto.Option;
import api.sys.model.Menu;
import api.sys.model.Profile;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import metadata.model.GridRequest;
import model.system.SessionLogin;
import utilities.MySQLQuery;

@Path("/menu")
public class MenuApi extends BaseAPI {

    @POST
    public Response insert(Menu obj) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            obj.insert(conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @PUT
    public Response update(Menu obj) {
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
            Menu obj = new Menu().select(id, conn);
            return Response.ok(obj).build();
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @DELETE
    public Response delete(@QueryParam("id") int id) {
        try (Connection conn = getConnection()) {
            getSession(conn);
            Menu.delete(id, conn);
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
            return createResponse(Menu.getAll(conn));
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getMenu")
    public Response getMenu() {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);

            MySQLQuery mqMods = new MySQLQuery("SELECT " + Menu.getSelFlds("m") + ", m.id "
                    + " FROM login l "
                    + " INNER JOIN profile p ON p.id = l.profile_id "
                    + " AND p.is_mobile = false AND p.active "
                    + " INNER JOIN menu m ON m.id = p.menu_id "
                    + " WHERE l.employee_id = " + sl.employeeId
                    + " GROUP BY p.menu_id ORDER BY m.label");

            MySQLQuery mqProfs = new MySQLQuery("SELECT " + Profile.getSelFlds("p") + ", p.id "
                    + " FROM login l "
                    + " INNER JOIN profile p ON p.id = l.profile_id "
                    + " AND p.is_mobile = false AND p.active "
                    + " INNER JOIN menu m ON m.id = p.menu_id "
                    + " WHERE l.employee_id = " + sl.employeeId
                    + " GROUP BY p.menu_id ORDER BY m.label");

            List<Menu> mods = Menu.getList(mqMods, conn);
            List<Profile> profs = Profile.getList(mqProfs, conn);

            Map<Integer, DefaultProfile> defultProfs = new HashMap<>();

            if (profs.isEmpty()) {
                new MySQLQuery("UPDATE employee SET last_profile = null WHERE id = " + sl.employeeId).executeUpdate(conn);
                throw new Exception("Ud no tiene autorización para ningún perfíl. Debe comunicarse con el encargado.");
            } else {
                new MySQLQuery("UPDATE employee SET last_profile = ?1 WHERE id = " + sl.employeeId).setParam(1, profs.get(0).id).executeUpdate(conn);
                for (int i = 0; i < profs.size(); i++) {
                    Profile obj = profs.get(i);
                    defultProfs.put(obj.menuId, new DefaultProfile(obj.id, obj.name));
                }
            }

            List<Module> modules = new ArrayList<>();
            for (int i = 0; i < mods.size(); i++) {
                Menu menu = mods.get(i);
                Module mod = new Module();
                mod.name = menu.label;
                mod.color = menu.color;
                mod.webIcon = menu.webIcon;
                mod.place = menu.place;
                mod.webPath = menu.webPath;
                mod.crudTblName = menu.crudTblName;
                mod.crudGridName = menu.crudGridName;
                DefaultProfile objProfile = defultProfs.get(menu.id);
                mod.profileId = objProfile.profId;
                mod.profileName = objProfile.profName;

                List<Option> barOption = Menu.getBarOptionsByProfile(mod.profileId, conn);
                for (Option bar : barOption) {
                    bar.options = Menu.getOptionsByBar(mod.profileId, bar.id, conn);
                    if (bar.options != null && !bar.options.isEmpty()) {
                        for (Option opt : bar.options) {
                            if (opt.type.equals("sub")) {
                                opt.options = Menu.getSubOptions(mod.profileId, opt.id, conn);
                            }
                        }
                    }
                }
                mod.options = barOption;
                modules.add(mod);

            }

            return createResponse(modules);
        } catch (Exception ex) {
            return createResponse(ex);
        }
    }

    @GET
    @Path("/getMenuByProfile")
    public Response getMenuByProfile(@QueryParam("id") Integer profileId) {
        try (Connection conn = getConnection()) {
            SessionLogin sl = getSession(conn);

            if (profileId == null && sl.employeeId != 1) {
                profileId = new MySQLQuery("SELECT last_profile "
                        + "FROM employee WHERE id = " + sl.employeeId).getAsInteger(conn);
            } else {
                new MySQLQuery("UPDATE employee SET last_profile = " + profileId + " "
                        + "WHERE id = " + sl.employeeId).executeUpdate(conn);
            }

            if (profileId == null) {
                profileId = Profile.getRandomProfile(sl.employeeId, conn);
            }

            List<Option> barOption = Menu.getBarOptionsByProfile(profileId, conn);
            for (Option bar : barOption) {
                bar.options = Menu.getOptionsByBar(profileId, bar.id, conn);
                if (bar.options != null && !bar.options.isEmpty()) {
                    for (Option opt : bar.options) {
                        if (opt.type.equals("sub")) {
                            opt.options = Menu.getSubOptions(profileId, opt.id, conn);
                        }
                    }
                }
            }

            if (profileId != null) {
                Profile p = new Profile().select(profileId, conn);
                if (p.showBi) {
                    Option opt = new Option();
                    opt.id = 0;
                    opt.name = "Dashboard";
                    opt.webIcon = "fa-chart-line";
                    opt.webPath = "123asd";
                    opt.place = -1;
                    opt.type = "dash";
                    opt.crudTblName = "";
                    opt.crudGridName = "";
                    barOption.add(0, opt);
                }
            }

            return createResponse(barOption);
        } catch (Exception ex) {
            return createResponse(ex);
        }

    }

    public class DefaultProfile {

        Integer profId;
        String profName;

        public DefaultProfile(Integer profId, String profName) {
            this.profId = profId;
            this.profName = profName;
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
}
