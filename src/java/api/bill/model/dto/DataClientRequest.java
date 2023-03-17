package api.bill.model.dto;

import utilities.Cache;

public class DataClientRequest {

    public String type;
    public String strFind;
    public boolean findByNumInstall;
    public boolean findByName;
    public boolean findByDocument;
    public boolean findByMeter;
    public boolean findComplete;
    public boolean findByRef;
    public int page;

    public String getFiltQuery() throws Exception {
        if (findByNumInstall) {
            strFind = strFind.trim().replaceAll("\\s+", "");
            return "c.num_install like '%" + strFind + "%'";
        } else if (findByName) {
            return "CONCAT(c.first_name, ' ', c.last_name) like '%" + strFind.replaceAll("\\s+", "%") + "%'";
        } else if (findByDocument) {
            return "c.doc like '%" + strFind + "%'";
        } else if (findByMeter) {
            return "(SELECT `number` FROM bill_meter WHERE client_id = c.id ORDER BY start_span_id DESC LIMIT 1) like '%" + strFind + "%'";
        } else if (findComplete) {
            return Cache.getFilter(strFind);
        } else if (findByRef) {
            return "c.code like '%" + strFind + "%'";
        } else {
            throw new Exception("Parametro desconocido");
        }
    }
}
