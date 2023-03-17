package api.sys.model;

public class LoginResponse {

    public String sessionId;
    private Integer daysLeftPasswordExpiration;
    private String token;
    private String projectNum;
    public Employee employee;
    public boolean documentAsPassword;
}
