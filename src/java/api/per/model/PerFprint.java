package api.per.model;

import java.util.ArrayList;
import java.util.List;
import utilities.AES;
import utilities.MySQLQuery;

public class PerFprint {

    //Dejar por fuera de la zona de reemplazo
    public Integer officeId;
    public String blobToStr;
    public String empName;
    public String empDoc;
    //Dejar por fuera de la zona de reemplazo
    
    
    public int id;
    public int empId;
    public byte[] blob;

    public PerFprint() {
    }

    public PerFprint(Object[] row) {
        this.id = MySQLQuery.getAsInteger(row[0]);
        this.empId = MySQLQuery.getAsInteger(row[1]);
        this.blobToStr = AES.bytesToHex((byte[]) row[2]);
        this.officeId = MySQLQuery.getAsInteger(row[3]);
        this.empName = MySQLQuery.getAsString(row[4]);
        this.empDoc = MySQLQuery.getAsString(row[5]);
    }

    public List<PerFprint> getList(Object[][] data) {
        List<PerFprint> lst = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            lst.add(new PerFprint(data[i]));
        }
        return lst;
    }
}
