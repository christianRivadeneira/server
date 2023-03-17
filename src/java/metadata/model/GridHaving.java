package metadata.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GridHaving {

    public static void main(String[] args) {
        String[] parts = "el general no tiene quien le escriba".split(" ");
        List<String[]> data = new ArrayList<>();

        for (int i = 0; i < parts.length - 1; i++) {
            data.add(new String[]{parts[i], parts[i+1]});
        }
        
        Collections.shuffle(data);

        for (int i = 0; i < data.size(); i++) {
            System.out.println("\""+data.get(i)[0]+"\", \""+data.get(i)[1]+"\"");
             
        }
    }
}
