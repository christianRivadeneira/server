package web.quality.project;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.system.SessionLogin;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import service.MySQL.MySQLCommon;
import utilities.MySQLQuery;

@MultipartConfig
@WebServlet(name = "importProject", urlPatterns = {"/importProject"})
public class importProject extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> pars = MySQLQuery.scapedParams(request);
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
            SAXBuilder builder = new SAXBuilder();
            Integer type = new Integer(pars.get("type"));
            Document doc = (Document) builder.build(request.getPart("file").getInputStream());
            Element root = doc.getRootElement();
            Element elTasks = root.getChild("Tasks", root.getNamespace());
            List<Section> sections = new ArrayList<>();
            List<Element> tasks = elTasks.getChildren("Task", root.getNamespace());
            List<SubSection> subSections = new ArrayList<>();
            List<Task> secTasks = new ArrayList<>();
            for (int i = 0; i < tasks.size(); i++) {
                Element task = tasks.get(i);
                Element level = task.getChild("OutlineLevel", root.getNamespace());
                if (level != null) {
                    switch (new Integer(level.getValue())) {
                        case 2: {
                            Section section = new Section();
                            section.setName(task.getChild("Name", root.getNamespace()).getValue().replace("'", ""));
                            Date begin = df.parse(task.getChild("Start", root.getNamespace()).getValue());
                            section.setBegin(begin);
                            Date end = df.parse(task.getChild("Finish", root.getNamespace()).getValue());
                            section.setEnd(end);
                            section.setOutLineNumber(task.getChild("OutlineNumber", root.getNamespace()).getValue());
                            if (secTasks.size() > 0) {
                                SubSection subsec = subSections.get(subSections.size() - 1);
                                subsec.setTaks(secTasks);
                                subSections.set(subSections.size() - 1, subsec);
                                secTasks = new ArrayList<>();
                            }
                            if (subSections.size() > 0) {
                                Section sec = sections.get(sections.size() - 1);
                                sec.setSubs(subSections);
                                sections.set(sections.size() - 1, sec);
                                subSections = new ArrayList<>();
                            }
                            sections.add(section);
                            break;
                        }
                        case 3: {
                            SubSection sub = new SubSection();
                            sub.setName(task.getChild("Name", root.getNamespace()).getValue().replace("'", ""));
                            Date begin = df.parse(task.getChild("Start", root.getNamespace()).getValue());
                            sub.setBegin(begin);
                            Date end = df.parse(task.getChild("Finish", root.getNamespace()).getValue());
                            sub.setEnd(end);
                            sub.setOutLineNumber(task.getChild("OutlineNumber", root.getNamespace()).getValue());
                            if (secTasks.size() > 0) {
                                SubSection subsec = subSections.get(subSections.size() - 1);
                                subsec.setTaks(secTasks);
                                subSections.set(subSections.size() - 1, subsec);
                                secTasks = new ArrayList<>();
                            }
                            subSections.add(sub);
                            break;
                        }
                        case 4: {
                            Task tk = new Task();
                            tk.setName(task.getChild("Name", root.getNamespace()).getValue().replace("'", ""));
                            Date begin = df.parse(task.getChild("Start", root.getNamespace()).getValue());
                            tk.setBegin(begin);
                            Date end = df.parse(task.getChild("Finish", root.getNamespace()).getValue());
                            tk.setEnd(end);
                            tk.setOutLineNumber(task.getChild("OutlineNumber", root.getNamespace()).getValue());
                            secTasks.add(tk);
                            break;
                        }
                        default:
                            break;
                    }

                }

            }
            //Ingresamos las ultimas subsecciones
            if (subSections.size() > 0) {
                Section sec = sections.get(sections.size() - 1);
                sec.setSubs(subSections);
                sections.set(sections.size() - 1, sec);
            }
            //Ingresando las ultimas tareas
            if (secTasks.size() > 0) {
                SubSection subsec = subSections.get(subSections.size() - 1);
                subsec.setTaks(secTasks);
                subSections.set(subSections.size() - 1, subsec);
            }
            try {
                Connection con = null;
                try {
                    con = MySQLCommon.getConnection(pars.get("poolName"), pars.get("tz"));
                    SessionLogin.validate(pars.get("sessionId"), con);
                    Statement st = con.createStatement();
                    SimpleDateFormat mdf = new SimpleDateFormat("yyyy-MM-dd");
                    ResultSet rs = st.executeQuery("SELECT e.id "
                            + "FROM employee e "
                            + "INNER JOIN login AS l ON l.employee_id = e.id "
                            + "INNER JOIN profile AS p ON p.id = l.profile_id "
                            + "WHERE e.id != 1 AND e.active = 1 AND ( p.id = 58 OR p.id= 36 )");
                    List<Integer> ids = new ArrayList<>();
                    while (rs.next()) {
                        ids.add(rs.getInt(1));
                    }
                    st.executeUpdate("UPDATE cal_meet SET last = 0");
                    for (int i = 0; i < sections.size(); i++) {
                        Section sec = sections.get(i);
                        st.executeUpdate("INSERT INTO cal_meet "
                                + "SET "
                                + "`type_id` = " + type + ", " //El tipo se debe seleccionar
                                + "`office_id` = NULL, "
                                + "`beg_date` = '" + mdf.format(sec.getBegin()) + "', "
                                + "`place` = '" + sec.getName() + "', "
                                + "`sched_dates` = NULL, "
                                + "`execution_dates` = NULL, "
                                + "`reason` = NULL, "
                                + "`notes` = '" + sec.getName() + "', "
                                + "`last` = 1 , "
                                + "`done` = 0 ", Statement.RETURN_GENERATED_KEYS);

                        ResultSet rs1 = st.getGeneratedKeys();
                        int meetId;
                        if (rs1.next()) { //Codigo para capturar el id de la ultima reunion ingresada
                            meetId = rs1.getInt(1);
                            rs1.close();
                        } else {
                            throw new Exception("No se pudo recuperar la llave.");
                        }

                        st.executeUpdate("INSERT INTO cal_plan "
                                + "SET "
                                + "`crea_date` = '" + mdf.format(sec.getBegin()) + "', "
                                + "`short_desc` = \"Proyecto\", "
                                + "`description` = NULL, "
                                + "`goal_id` = NULL, "
                                + "`action_id` = NULL, "
                                + "`meet_id` = " + meetId + ", "
                                + "`proc_id` = NULL, "
                                + "`emp_id` = NULL, "
                                + "`checker_id` = NULL, "
                                + "`state` = \"ejec\" ", Statement.RETURN_GENERATED_KEYS);
                        ResultSet rs2 = st.getGeneratedKeys();
                        int planId;
                        if (rs2.next()) { //Codigo para capturar el id de la ultima reunion ingresada
                            planId = rs2.getInt(1);
                            rs2.close();
                        } else {
                            throw new Exception("No se pudo recuperar la llave.");
                        }
                        List<SubSection> sub = sec.getSubs();

                        for (int j = 0; j < ids.size(); j++) {
                            st.executeUpdate("INSERT INTO "
                                    + "cal_emp_meeting (id, meet_id, employee_id, rol) "
                                    + "VALUES (null, " + meetId + ", " + ids.get(j) + ", '') ");

                        }

                        if (sub != null) {
                            for (int j = 0; j < sub.size(); j++) {
                                SubSection subSection = sub.get(j);
                                if (subSection.getTaks() != null && subSection.getTaks().size() > 0) {
                                    List<Task> sectask = subSection.getTaks();

                                    for (int k = 0; k < sectask.size(); k++) {
                                        Task task = sectask.get(k);
                                        st.executeUpdate("INSERT INTO cal_activity "
                                                + "SET "
                                                + "`plan_id` = " + planId + ", "
                                                + "`description` = '" + task.getName() + "', "
                                                + "`begin_date` = '" + mdf.format(task.getBegin()) + "', "
                                                + "`end_date` = '" + mdf.format(task.getEnd()) + "', "
                                                + "`cost` = 0, "
                                                + "`done_date` = NULL, "
                                                + "`check_notes` = NULL, "
                                                + "`checked_by_id` = NULL, "
                                                + "`notes` = NULL, "
                                                + "`done_notes` = NULL, "
                                                + "`done_by_id` = NULL, "
                                                + "`check_date` = NULL ", Statement.RETURN_GENERATED_KEYS);
                                        ResultSet rs3 = st.getGeneratedKeys();
                                        int taskId;
                                        if (rs3.next()) { //Codigo para capturar el id de la ultima tarea ingresada
                                            taskId = rs3.getInt(1);
                                            rs3.close();
                                        } else {
                                            throw new Exception("No se pudo recuperar la llave.");
                                        }
                                        for (int m = 0; m < ids.size(); m++) {
                                            st.executeUpdate("INSERT INTO "
                                                    + "cal_emp_act (act_id, emp_id) "
                                                    + "VALUES (" + taskId + ", " + ids.get(m) + ") ");

                                        }
                                    }
                                }
                            }
                        }
                    }
                    //st.exe
                } catch (Exception ex) {
                    response.reset();
                    response.sendError(500, ex.getMessage());
                    ex.printStackTrace();
                } finally {
                    MySQLCommon.closeConnection(con);
                }

                out.write("\nOK");
            } catch (IOException ex) {
                response.reset();
                response.sendError(500, ex.getMessage());
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            response.getOutputStream().write(ex.getMessage().getBytes());
            ex.printStackTrace();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
