package api.dto.importer;

public class Header {

    public int datePos = -1;
    public int hourPos = -1;
    public int docPos = -1;
    public int capaPos = -1;
    public int valPos = -1;
    public int subsPos = -1;
    public int salesmanPos = -1;
    public int nifPos = -1;
    public int stratumPos = -1;
    public int nAprov = -1;
    public int bill = -1;
    public int anulNotesPos = -1;
    public int latPos = -1;
    public int lonPos = -1;
    public int munPos = -1;
    public int deptoPos = -1;

    public static boolean isHeader(Object[] row) {
        boolean dateFound = false;
        boolean hourFound = false;
        boolean docFound = false;

        for (Object cell : row) {
            String s = Header.normalize(cell);
            if (Header.in(s, "FECHA")) {
                dateFound = true;
            } else if (Header.in(s, "APROBACION")) {
                hourFound = true;
            }
//            else if (Header.in(s, "HORA")) {
//                hourFound = true;
//            } 
            else if (Header.in(s, "DOCUMENTO", "DOCUM", "DOC", "BENEFICIARIO")) {
                docFound = true;
            }
        }
        return dateFound && docFound && hourFound;
    }

    public Header(Object[] headerRow) throws Exception {
        //FECHA HORA	DOCUMENTO   CAP         VALOR   SUBSIDIO    ID VENDEDOR	NIF	ESTRATO	DEPTO   MUNICIPIO   No. APROBACIÓN      FACTURA
        for (int i = 0; i < headerRow.length; i++) {
            if (in(headerRow[i], "FECHA")) {
                datePos = i;
            } 
//            else if (in(headerRow[i], "HORA")) {
//                hourPos = i;
//            } 
            else if (in(headerRow[i], "DOCUMENTO", "DOCUM", "BENEFICIARIO")) {
                docPos = i;
            } else if (in(headerRow[i], "CAPACIDAD", "CAP")) {
                capaPos = i;
            } else if (in(headerRow[i], "VALOR")) {
                valPos = i;
            } else if (in(headerRow[i], "SUBSIDIO", "VR SUBSIDIO", "V/SUBSIDIO")) {
                subsPos = i;
            } else if (in(headerRow[i], "ID VENDEDOR", "VENDEDOR")) {
                salesmanPos = i;
            } else if (in(headerRow[i], "NIF")) {
                nifPos = i;
            } else if (in(headerRow[i], "ESTRATO")) {
                stratumPos = i;
            } else if (in(headerRow[i], "No. APROBACIÓN", "APROBACION", "APROBACIÓN")) {
                nAprov = i;
            } else if (in(headerRow[i], "FACTURA")) {
                bill = i;
            } else if (in(headerRow[i], "ANULADA", "ANULACION")) {
                anulNotesPos = i;
            } else if (in(headerRow[i], "LATITUD")) {
                latPos = i;
            } else if (in(headerRow[i], "LONGITUD")) {
                lonPos = i;
            } else if (in(headerRow[i], "MUNICIPIO")) {
                munPos = i;
            } else if (in(headerRow[i], "DEPARTAMENTO")) {
                deptoPos = i;
            }
        }

        if (datePos == -1) {
            throw new Exception("Falta la columna de fecha");
        }
//        if (hourPos == -1) {
//            throw new Exception("Falta la columna de hora");
//        }
        if (docPos == -1) {
            throw new Exception("Falta la columna de documento");
        }
        if (capaPos == -1) {
            throw new Exception("Falta la columna de capacidad");
        }
        if (valPos == -1) {
            throw new Exception("Falta la columna de valor");
        }
        if (subsPos == -1) {
            throw new Exception("Falta la columna de subsidios");
        }
        if (salesmanPos == -1) {
            throw new Exception("Falta la columna de vendedor");
        }
        if (nifPos == -1) {
            throw new Exception("Falta la columna de nif");
        }
        if (stratumPos == -1) {
            throw new Exception("Falta la columna de estrato");
        }
        if (nAprov == -1) {
            throw new Exception("Falta la columna número de aprobación");
        }
        if (bill == -1) {
            throw new Exception("Falta la columna de factura");
        }
        if (anulNotesPos == -1) {
            throw new Exception("Falta la columna de anulada");
        }
        if (latPos == -1) {
            throw new Exception("Falta la columna de latitud");
        }
        if (lonPos == -1) {
            throw new Exception("Falta la columna de longitud");
        }
        if (munPos == -1) {
            throw new Exception("Falta la columna de municipio");
        }
        if (deptoPos == -1) {
            throw new Exception("Falta la columna de departamento");
        }
    }

    public static boolean in(Object header, Object... lst) {
        String headerStr = normalize(header);
        for (Object e : lst) {
            if (headerStr.equals(normalize(e))) {
                return true;
            }
        }
        return false;
    }

    public static String normalize(Object o) {
        return o != null ? o.toString().toUpperCase().replaceAll("[^A-Z]", " ").replaceAll("\\s+", " ").trim() : "";
    }
}
