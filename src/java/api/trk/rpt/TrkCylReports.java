package api.trk.rpt;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TrkCylReports {

    public static File getNoSaleCyls(Date beginDate, Connection conn) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        File tmp = File.createTempFile("tmp", ".csv");
        try (PrintWriter out = new PrintWriter(tmp); Statement st = conn.createStatement()) {
            out.write("Nif,Fabricado,Ingresado,Fábrica,Capacidad,Tara,Estado" + System.lineSeparator());//
            ResultSet rs = st.executeQuery("SELECT CONCAT(LPAD(c.nif_y, 2, 0), LPAD(c.nif_f, 4, 0), LPAD(c.nif_s, 6, 0)) AS Nif, "
                    + "DATE_FORMAT(c.fab_date, '%d/%m/%Y') AS Fabricacion, "
                    + "DATE_FORMAT(c.create_date, '%d/%m/%Y') AS Ingresado, "
                    + "f.name AS Fabrica, "
                    + "t.name AS Capacidad, "
                    + "c.tara AS Tara, "
                    + "IF(c.ok, 'Apto', 'No Apto') AS Estado "
                    + "FROM trk_cyl c "
                    + "INNER JOIN cylinder_type t ON c.cyl_type_id = t.id "
                    + "INNER JOIN inv_factory f ON c.factory_id = f.id "
                    + "WHERE "
                    + "c.active AND c.last_verify < '" + sdf.format(beginDate) + " 00:00:00' ");

            int cols = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                for (int j = 1; j <= cols; j++) {
                    if(rs!= null && rs.getString(j)!= null){
                    out.write(rs.getString(j));
                    }
                    out.write(",");
                }
                out.write(System.lineSeparator());
            }
            return tmp;
        }
    }
}
