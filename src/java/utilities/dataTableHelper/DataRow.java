package utilities.dataTableHelper;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

@Deprecated
public class DataRow {

    private ArrayList<Object> data;
    private ArrayList<Object> keys;
    private byte[] bdata;
    
    public DataRow() {
    }

    public DataRow(int dataSize, int keysSize) {
        data = new ArrayList<Object>(dataSize);
        keys = new ArrayList<Object>(keysSize);
    }

    public ArrayList<Object> getData() {
        return data;
    }

    public void setData(Object[] objs, int beginIndex, int endIndex) {
        if (beginIndex == 0 && endIndex == objs.length - 1) {
            getData().addAll(Arrays.asList(objs));
        } else {
            Object[] tmp = new Object[endIndex - beginIndex + 1];
            System.arraycopy(objs, beginIndex, tmp, 0, tmp.length);
            getData().addAll(Arrays.asList(tmp));
        }
    }

    public void setData(ArrayList<Object> data) {
        this.setData(data);
    }

    public ArrayList<Object> getKeys() {
        return keys;
    }

    public void setKeys(ArrayList<Object> keys) {
        this.setKeys(keys);
    }

    public byte[] getBdata() {
        return bdata;
    }
    
    public void setBdata(byte[] bdata) {
        this.bdata = bdata;
    }
    
    public void generateBData() throws Exception{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        for (int i = 0; i < data.size(); i++) {
            oos.writeObject(data.get(i));                        
        }
        oos.close();
        bdata = baos.toByteArray();
        data.clear();
    }
}
