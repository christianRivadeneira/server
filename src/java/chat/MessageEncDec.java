package chat;

import java.io.StringReader;
import javax.json.Json;
import javax.json.JsonException;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

public class MessageEncDec implements Encoder.Text<Message>, Decoder.Text<Message> {

    @Override
    public String encode(Message msg) throws EncodeException {
        return msg.toJsonString();
    }

    @Override
    public void init(EndpointConfig ec) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public Message decode(String string) throws DecodeException {
        return new Message(string);
    }

    @Override
    public boolean willDecode(String string) {
        try {
            Json.createReader(new StringReader(string)).readObject();
            return true;
        } catch (JsonException ex) {
            return false;
        }

    }
}
