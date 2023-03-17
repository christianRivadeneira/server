package utilities;

public class Strings {

    public static String toTitleType(String str) {
        str = str.trim().toLowerCase();
        if (str.length() == 0) {
            return "";
        }
        String[] parts = str.split(" ");
        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].matches("[ivxlcdm]{1,}")) {
                sb.append(parts[i].toUpperCase());
            } else {
                if (parts[i].length() <= 3 && i > 0) {
                    sb.append(parts[i]);
                } else {
                    String s = parts[i];
                    sb.append(s.substring(0, 1).toUpperCase());
                    sb.append(s.substring(1));
                }
            }
            if (i < parts.length - 1) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    public static String toHtmlLetters(String str) {
        str = str.replaceAll("\\¿", "&iquest");
        str = str.replaceAll("Á", "&Aacute");
        str = str.replaceAll("É", "&Eacute");
        str = str.replaceAll("Í", "&Iacute");
        str = str.replaceAll("Ó", "&Oacute");
        str = str.replaceAll("Ú", "&Uacute");
        str = str.replaceAll("Ñ", "&Ntilde");
        str = str.replaceAll("á", "&aacute");
        str = str.replaceAll("é", "&eacute");
        str = str.replaceAll("í", "&iacute");
        str = str.replaceAll("ó", "&oacute");
        str = str.replaceAll("ú", "&uacute");
        str = str.replaceAll("ñ", "&ntilde");
        return str;
    }

    public static String toUTFLetters(String str) {
        str = str.replaceAll("&amp;", "");
        str = str.replaceAll("iquest", "\\¿");
        str = str.replaceAll("Aacute", "Á");
        str = str.replaceAll("Eacute", "É");
        str = str.replaceAll("Iacute", "Í");
        str = str.replaceAll("Oacute", "Ó");
        str = str.replaceAll("Uacute", "Ú");
        str = str.replaceAll("Ntilde", "Ñ");
        str = str.replaceAll("aacute", "á");
        str = str.replaceAll("eacute", "é");
        str = str.replaceAll("iacute", "í");
        str = str.replaceAll("oacute", "ó");
        str = str.replaceAll("uacute", "ú");
        str = str.replaceAll("ntilde", "ñ");
        return str;
    }
}
