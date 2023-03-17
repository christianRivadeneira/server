package web.marketing.smsClaro;

import javax.xml.ws.BindingProvider;
import net.tiaxa.ws.tggdatasoapservice.SendMessageRequest;
import net.tiaxa.ws.tggdatasoapservice.SendMessageResponse;

public class ClaroSmsSender {

    //TUTORIAL PARA CREAR EL VINCULO CON EL WSDL DE CLARO *************
    //*****************************************************************
    //*****************************************************************
    
    // 1) En el proyecto servidor buscar la carpeta "Wen Service References"
    // 2) Click derecho -> new -> Web Service Client...
    // 3) Seleccionar WSDL URL Y Escribir la siguiente URL:  https://www.gestormensajeriaadmin.com/RA/tggDataSoap?wsdl
    // 4) Click en finish y aceptar las 3 ssh keys(dialogos)
    
    public String sendMsg(String msg, String reqId, String phone) {
        SendMessageRequest smr = new SendMessageRequest();
        smr.setDataCoding("GSM-7");
        smr.setMessage(msg);
        smr.setReceiptRequest("0");
        smr.setRequestId(reqId);
        smr.setSender("85440");
        smr.setSubscriber("57" + phone);

        net.tiaxa.ws.tggdatasoapservice.TggDataService service = new net.tiaxa.ws.tggdatasoapservice.TggDataService();
        net.tiaxa.ws.tggdatasoapservice.TggDataPortType port = service.getTggDataPort();

        BindingProvider prov = (BindingProvider) port;
        prov.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, "https://www.gestormensajeriaadmin.com/RA/tggDataSoap?wsdl");
        prov.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, "w355");
        prov.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, "M0nt4g4s");

        SendMessageResponse response = port.sendMessage(smr);
        return getCode(response.getResultCode());
    }

    private String getCode(String code) {
        String result = null;
        switch (code) {
            case "0":
                result = code + " Envío exitoso";
                break;
            case "5":
                result = code + " Parámetro canal no es válido. Debe ser WS, WEB o SMPP";
                break;
            case "100":
                result = code + " Error de validación en el servicio";
                break;
            case "101":
                result = code + " Mensaje enviado fuera del tiempo permitido";
                break;
            case "102":
                result = code + " Teléfono en lista negra";
                break;
            case "103":
                result = code + " Error en el parámetro teléfono";
                break;
            case "104":
                result = code + " El número de teléfono no está en la lista";
                break;
            case "105":
                result = code + " Se alcanzó el límite de mensajes por mes";
                break;
            case "106":
                result = code + " Se alcanzó el límite de mensajes permitidos por mes";
                break;
            case "107":
                result = code + " Se alcanzó el límite de mensajes de invitación diarios al mismo número";
                break;
            case "108":
                result = code + " El número no ha pasado la validación del operador";
                break;
            case "113":
                result = code + " La compañía no se encuentra activa.";
                break;
            case "114":
                result = code + " Error en el parámetro de entrada Company. Éste es inválido o nulo";
                break;
            case "115":
                result = code + " El atributo ask_for_ack (Solicitud de acknowledge) del servicio no coincide con lo enviado en el request (receiptRequest).";
                break;
            case "116":
                result = code + " Error en el parámetro de entrada Sender. Éste es inválido o nulo.";
                break;
            case "117":
                result = code + " Error en el parámetro de entrada delivery_receipt. Éste debe ser 0 o 1.";
                break;
            case "118":
                result = code + " El número ya está asociado a la lista especificada. Doble Alta.";
                break;
            case "119":
                result = code + " La cantidad de móviles permitidos para la lista ha sido alcanzada.";
                break;
            case "120":
                result = code + " El número corresponde a un país no soportado.";
                break;
            case "121":
                result = code + " No fue posible adicionar el msisdn a la lista blanca.";
                break;
            case "122":
                result = code + " No fue posible eliminar el número de la lista blanca.";
                break;
            case "999":
                result = code + " Error de procesamiento interno";
                break;
            default:
                result = code + " Opción desconocida";
                break;
        }
        return result;
    }
}
