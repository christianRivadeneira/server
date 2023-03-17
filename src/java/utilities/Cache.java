package utilities;

public class Cache {

    public static String getFilter(String txtSearch) {
        return getFilter("cache", txtSearch);
    }
    
    public static String getFilter(String fieldName, String txtSearch) {

        String[] parts = txtSearch.replaceAll("[\\s]+", " ").split("[\\s]");
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < parts.length; i++) {
            sb.append(fieldName).append(" like '%").append(parts[i]).append("%'");
            if (i < parts.length - 1) {
                sb.append(" AND ");
            }
        }
        sb.append(")");

        return sb.toString();
    }
}
