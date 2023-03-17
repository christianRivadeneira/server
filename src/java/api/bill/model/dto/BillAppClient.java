package api.bill.model.dto;


import java.io.PrintWriter;
import java.lang.reflect.Field;

/**
 * Clase modelo para datos de clientes en el app de lecturas
 * La clase en la App se llama Client
 */
public class BillAppClient {
    //no cambiar el orden ********************************* IMPORTANTE
    //si se cambia el orden, tambien cambiar en el app

    public Integer id;//0
    public Integer buildId;//1
    public Integer neighId;//2
    public String ownerName;//3
    public String address;//4
    public String numMeter;//5
    public String numInstall;//6

    public Double lastRead;//7
    public Double currRead;//8
    public Double c1;//9
    public Double c2;//10
    public Double c3;//11
    public Double c4;//12
    public Double c5;//13
    public Double c6;//14
    public int months;//15
    public String buildName;//16
    public boolean spanClosed;//17
    public String document;//18
    public Integer faultId;//19
    public boolean residential;//20
    
    //el último campo debe ser algo que no pueda ser nulo, de otro modo el trim lo borra y hay problemas en cliente
    
    /*IMPORTANTE el orden de estos parametors corresponde al metodo 
     "toClientObject" en el app de lecturas*/
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
