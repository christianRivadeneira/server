package web.personal;

import java.io.IOException;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import utilities.MySQLQuery;

@WebServlet(name = "GenerateEvents", urlPatterns = {"/GenerateEvents"})
public class CallGateEventGenerator extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Map<String, String> args = MySQLQuery.scapedParams(request);

        if (args.containsKey("pwd") && args.get("pwd").equals("EzTQFvZ5z")) {
            new GateAndExtrasCalculus().processRequest();
        } else {
            response.getWriter().print("No tiene permiso para usar este elemento");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Eventos portería";
    }
}
