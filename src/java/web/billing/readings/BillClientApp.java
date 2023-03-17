package web.billing.readings;

//Clase modelo para datos de clientes en el app de lecturas
import java.io.PrintWriter;
import java.lang.reflect.Field;

public class BillClientApp {
//no cambiar el orden

    public Integer id;
    public Integer buildId;
    public Integer neighId;
    public String ownerName;
    public String address;
    public String numMeter;
    public String numInstall;

    public Double lastRead;
    public Double currRead;
    public Double c1;
    public Double c2;
    public Double c3;
    public Double c4;

    public int months;
    public String name;
    public boolean spanClosed;

    //Un nuevo campo debe ser añadido al final
    public void writeObject(PrintWriter w) throws Exception {
        Class billClientClass = this.getClass();
        Field fields[] = billClientClass.getFields();
        for (Field field : fields) {
            String value = field.get(this) != null ? field.get(this).toString() : "";
            w.write(value);
            w.write(9);
        }
    }

}
