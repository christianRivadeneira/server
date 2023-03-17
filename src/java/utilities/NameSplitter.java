package utilities;

import java.util.ArrayList;
import java.util.List;

public class NameSplitter {

    public String cad1;
    public String cad2;

    public NameSplitter(String cad1, String cad2) {
        this.cad1 = cad1;
        this.cad2 = cad2;
    }

    private static String[] getWords(String s) {
        String[] parts = s.replaceAll("[\\s]+", " ").toUpperCase().split(" ");
        String curWord = "";
        List<String> words = new ArrayList<>();
        for (String part : parts) {
            curWord += part + " ";
            if (!isShortWord(part)) {
                words.add(curWord);
                curWord = "";
            }
        }
        return words.toArray(new String[words.size()]);
    }

    public static boolean isShortWord(String s) {
        return s.equals("DE") || s.equals("DEL") || s.equals("LA") || s.equals("LAS") || s.equals("LOS") || s.equals("E");
    }

    public static NameSplitter split(String cadGral) {
        String[] parts = getWords(cadGral);
        String cad1 = "";
        String cad2 = "";

        if (parts.length % 2 == 0) {
            for (int k = 0; k < parts.length / 2; k++) {
                cad1 += " " + parts[k];
            }
            for (int k = parts.length / 2; k < parts.length; k++) {
                cad2 += " " + parts[k];
            }
        } else {
            for (int k = 0; k < (parts.length - 1) / 2; k++) {
                cad1 += " " + parts[k];
            }
            for (int k = (parts.length - 1) / 2; k < parts.length; k++) {
                cad2 += " " + parts[k];
            }

        }

        cad1 = Strings.toTitleType(cad1);
        cad2 = Strings.toTitleType(cad2);

        if (cad1.equals("")) {
            cad1 = cad2;
            cad2 = "";
        }
        return new NameSplitter(cad1, cad2);
    }
}
