package api.ord.orfeo;

import utilities.MySQLQuery;
import utilities.apiClient.ApiClient;

public class OrfeoClient {

    private final String orfeoPath = "http://172.16.1.14/orfeo-api/api/web/version1/sigma/";
    private final String registerPqrPath = orfeoPath + "create";
    private final String technicalAssistancePqrPath = orfeoPath + "assistance";
    private final String closePqrPath = orfeoPath + "closure";

    public OrfeoResponse registerPqr(OrfeoCreatePqrCommand command) throws Exception {
        ApiClient client = new ApiClient(ApiClient.POST, registerPqrPath);
        client.setRequestBody(command);
        OrfeoResponse response = client.getObject(OrfeoResponse.class);

        return response;
    }

    public OrfeoResponse closePqr(OrfeoClosePqrCommand command) throws Exception {
        ApiClient client = new ApiClient(ApiClient.POST, closePqrPath);
        String radicado = command.radNumber;
        if (MySQLQuery.isEmpty(radicado)) {
            throw new Exception("No se encontro contenido en el radicado");
        }
        command.radNumber = radicado.substring(radicado.indexOf(":") + 1, radicado.length()).trim();
        client.setRequestBody(command);
        OrfeoResponse response = client.getObject(OrfeoResponse.class);

        return response;
    }

    public OrfeoResponse registerAssistance(OrfeoTechnicalAssistanceCommand command) throws Exception {

        ApiClient client = new ApiClient(ApiClient.POST, technicalAssistancePqrPath, ApiClient.MULTIPART_REQUEST);
        String radicado = command.radNumber;
        if (MySQLQuery.isEmpty(radicado)) {
            throw new Exception("No se encontro contenido en el radicado");
        }
        String rad = radicado.substring(radicado.indexOf(":") + 1, radicado.length()).trim();
        System.out.println("radicado " + rad);
        client.setParam("radNumber", rad);
        client.setParam("notes", command.notes);
        client.setParam("date", command.date);
        client.setParam("file", command.file);

        OrfeoResponse response = client.getObject(OrfeoResponse.class);

        return response;
    }
}
