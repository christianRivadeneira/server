package api.mto.dto;

import static api.mto.api.MtoTripApi.getExpected;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PointItem {

    public String hexColor;
    public String title;
    public String subtitle;
    public String notes;
    public boolean reg;

    public PointItem() {

    }

    public PointItem(String name, String type, Date expFull, Date expPart, Date regTime, Date now, int tolerance) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm aa");
        title = name;
        reg = regTime != null;

        Date expTime = getExpected(expPart, expFull);

        String lblExpTime = expTime != null ? sdf.format(expTime) : "";

        if (regTime != null && expTime != null) {
            String lblRegTime = sdf.format(regTime);
            notes = "Esperado: " + lblExpTime + " - llegada: " + lblRegTime;

            int difMinutes = (int) (((expTime.getTime() - regTime.getTime()) / 1000) / 60);

            if (difMinutes < 0) {
                if (Math.abs(difMinutes) < tolerance) {
                    hexColor = "#ff9800";//naranja
                } else {
                    hexColor = "#f44336";//rojo
                }

                subtitle = ((type.equals("going_start")) ? "Partida " : "Llegada ")
                        + Math.abs(difMinutes) + " Minutos Tarde";

            } else {
                hexColor = "#4caf50";//verde
                subtitle = ((!type.equals("going_start")) ? "Llegada a Tiempo" : "Salida a Tiempo");
            }
        } else {
            if (expTime != null) {
                int difMinutes = (int) (((expTime.getTime() - now.getTime()) / 1000) / 60);
                if (difMinutes < 0) {
                    if (Math.abs(difMinutes) < tolerance) {
                        hexColor = "#ff9800";//naranja        
                    } else {
                        hexColor = "#f44336";//rojo                                
                    }
                    subtitle = "Esperado: " + lblExpTime;
                } else {
                    hexColor = "#9e9e9e";//gris
                    subtitle = "Esperado: " + lblExpTime;
                }
            } else {
                hexColor = "#9e9e9e";//gris
                subtitle = "Pendiente";
            }
        }
    }
}
