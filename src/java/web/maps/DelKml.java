package web.maps;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.servlet.http.HttpServlet;

@Singleton
@Startup
public class DelKml extends HttpServlet {

    @Schedule(hour = "23", minute = "5")
    protected void processRequest() {
        try {
            System.out.println("Inicio de tarea de borrado de kml");
            Process p = Runtime.getRuntime().exec("find / -name \"*.kml\" -type f -delete");
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            System.out.println("Finaliza borrado");
        } catch (Exception e) {
            Logger.getLogger(DelKml.class.getName()).log(Level.SEVERE, null, e);
        }
    }
}
