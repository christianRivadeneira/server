package utilities.apiClient;

public class StringResponse {

    public String status;
    public String msg;

    public StringResponse() {
    }

    public StringResponse(String status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public StringResponse(String status) {
        this.status = status;
    }    

}
