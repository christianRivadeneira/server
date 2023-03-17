package utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddressTextField  {
    public static String clearAddress(String dir) {
        if (dir == null) {
            return "";
        }
        dir = dir.toUpperCase();
        dir = Pattern.compile("[\\s]{2,}", Pattern.CASE_INSENSITIVE).matcher(dir).replaceAll(".").trim();
        Pattern p = Pattern.compile("[A-Z][0-9]|[0-9][A-Z]");
        Matcher mt = p.matcher(dir);

        while (mt.find()) {
            String p1 = dir.substring(0, mt.start() + 1);
            String p2 = dir.substring(mt.start() + 1);
            dir = p1 + " " + p2;
            mt = p.matcher(dir);
        }

        String[][] replacements = new String[][]{
            {"VEREDA|VDA|VR", "VD"},
            {"CASA|CAS", "CS"},
            {"MANZANA|MZA|MAN", "MZ"},
            {"DIAGONAL|DIAG|DGL", "DG"},
            {"CALLE|CLE|CLLE|CAL|CLL", "CL"},
            {"KRA|KRRA|KRR|KRRA", "KR"},
            {"CR|CRA|CARRERA|CRRA|CRR|CRRA", "KR"},

            {"N0|#|NO|NUM|NUMERO|NRO", " "},
            {"APARTAMENTO|APTO|APT", "AP"},
            {"AVENIDA|AVN", "AV"},
            {"BARRIO", "BR"},
            {"BLOQUE|BL|BLQ", "BQ"},
            {"CC", "CE"},
            {"ETAPA|ETP", "ET"},
            /////////////////////////////////

            {"ADMINISTRACIÓN", "AD"},
            {"AEROPUERTO", "AE"},
            {"AGRUPACIÓN", "AG"},
            {"ALTILLO", "AL"},
            {"APARTAMENTO", "AP"},
            {"AUTOPISTA", "AU"},
            {"AVENIDA", "AV"},
            //{"AVENIDA CALLE", "AC"},
            //{"AVENIDA CARRERA", "AK"},
            {"BARRIO", "BR"},
            {"BIS", "BIS"},
            {"BLOQUE", "BQ"},
            {"BODEGA", "BG"},
            //{"BULEVAR", "BL"},

            {"CARRETERA", "CT"},
            {"CASA", "CS"},
            {"CELULA", "CU"},
            {"CENTRO COMERCIAL", "CE"},
            {"CIRCULAR", "CQ"},
            {"CIRCUNVALAR", "CV"},
            {"CIUDADELA", "CD"},
            {"CONJUNTO RESIDENCIAL", "CO"},
            {"CONSULTORIO", "CN"},
            //{"CUENTAS CORRIDAS", "CC"},
            //{"DEPOSITO", "DP"},
            //{"DEPOSITO SÓTANO", "DS"},
            {"DIAGONAL", "DG"},
            {"EDIFICIO", "ED"},
            {"ENTRADA", "EN"},
            {"ESQUINA", "EQ"},
            {"ESTACION", "ES"},
            {"ESTE", "ESTE"},
            {"ETAPA", "ET"},
            {"EXTERIOR", "EX"},
            {"FINCA", "FI"},
            {"GARAJE", "GA"},
            //{"GARAJE SÓTANO", "GS"},
            {"INTERIOR", "IN"},
            {"KILOMETRO", "KM"},
            {"LOCAL", "LC"},
            //{"LOCAL MEZZANINE", "LM"},
            {"LOTE", "LT"},
            {"MANZANA", "MZ"},
            //{"MEZZANINE", "MN"},
            //{"MODULO", "MD"},
            {"NORTE", "NORTE"},
            {"OESTE", "OESTE"},
            {"OFICINA", "OF"},
            {"PARQUE", "PQ"},
            {"PARQUEADERO", "PA"},
            {"PASAJE", "PJ"},
            {"PASEO", "PS"},
            {"PEATONAL", "PT"},
            //{"PENT-HOUSE", "PN"},
            {"PISO", "PI"},
            {"PLANTA", "PL"},
            {"PORTERÍA", "PR"},
            {"PREDIO", "PD"},
            {"PUESTO", "PU"},
            //{"ROUND POINT (GLORIETA)", "RP"},
            {"SECTOR", "SC"},
            //{"SEMISÓTANO", "SS"},
            {"SOTANO", "SO"},
            //{"SUITE", "ST"},
            //{"SUPERMANZANA", "SM"},
            {"SUR", "SUR"},
            {"TERRAZA", "TZ"},
            {"TORRE", "TO"},
            {"TRANSVERSAL", "TV"},
            {"TRONCAL", "TC"},
            {"UNIDAD", "UN"},
            //{"UNIDAD RESIDENCIAL", "UL"},
            {"URBANIZACION", "UR"},
            //{"VARIANTE", "VT"},
            {"VIA", "VI"},
            {"ZONA", "ZN"},};


        dir = Pattern.compile("[^A-Z0-9Ñ]", Pattern.CASE_INSENSITIVE).matcher(dir).replaceAll(" ");
        for (int i = 0; i < replacements.length; i++) {
            dir = Pattern.compile("\\b" + replacements[i][0] + "\\b", Pattern.CASE_INSENSITIVE).matcher(dir).replaceAll(replacements[i][1]);
        }
        dir = Pattern.compile("[\\s]{2,}", Pattern.CASE_INSENSITIVE).matcher(dir).replaceAll(" ").trim();

        Matcher matcher = Pattern.compile("\\b[0]+(\\d{1,})\\b", Pattern.CASE_INSENSITIVE).matcher(dir);

        while (matcher.find()) {
            dir = matcher.replaceAll(matcher.group(1));
            matcher = Pattern.compile("\\b[0]+(\\d)\\b", Pattern.CASE_INSENSITIVE).matcher(dir);
        }

        return dir;
    }

}
