/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Mario
 */
public abstract class MySQLParametrizable {

    private static final Pattern PAT = Pattern.compile("\\?(?<n>[0-9]+)");
    private Map<Integer, List<Integer>> params;
    private final String query;

    public MySQLParametrizable(String query) {
        this.query = query;
        generateParamsMap();

    }

    public abstract PreparedStatement getPs();

    /**
     * Convierte el query con parámetros sigma que son ?1 .... ?n en parámetros
     * JDBC que son ? ? ? ?, dónde cada incognita se llama por su posición en
     * que query empezando en 1
     *
     * @return
     */
    public final String getNormalizeQuery() {
        return query.replaceAll("\\?(?<n>[0-9]+)", "?");
    }

    /**
     * Un mapa que permite convertir parámetros sigma que son ?1 .... ?n en
     * parámetros JDBC que son ? ? ? ?, dónde cada incognita se llama por su
     * posición en que query empezando en 1
     *
     * @param query
     * @return
     */
    private void generateParamsMap() {
        params = new HashMap<>();
        Matcher matcher = PAT.matcher(query);
        for (int i = 1; matcher.find(); i++) {
            int sigmaName = Integer.valueOf(matcher.group().substring(1));
            int jdbcName = i;
            if (params.containsKey(sigmaName)) {
                params.get(sigmaName).add(jdbcName);
            } else {
                List<Integer> l = new ArrayList<>();
                params.put(sigmaName, l);
                l.add(jdbcName);
            }
        }
    }

    public void setParameter(int sigmaName, Object param) throws SQLException {
        List<Integer> l = params.get(sigmaName);
        for (Integer i : l) {
            getPs().setObject(i, param);
        }
    }
}
