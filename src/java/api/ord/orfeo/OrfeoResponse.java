package api.ord.orfeo;

public class OrfeoResponse {
    public boolean status;
    public String data;

    public String tryGetOkValue() throws Exception {
        if(this.status){
            return this.data;
        } else if(this.data == null || this.data.length() == 0) {
            throw new Exception("Problemas en Orfeo, no se pudo procesar la solicitud");
        } else {
            throw new Exception("Problemas en Orfeo: " + this.data);
        }
    }
}
