package org.diplom.client.connection.communicator;

import org.diplom.client.dto.AuthMessage;
import org.diplom.client.dto.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class SessionCommunicator extends DefaultCommutator {
    @Autowired
    private RestTemplate restTemplate;

    public boolean downloadServerConfigs(String url) {
        ResponseEntity<Message> response  = restTemplate.exchange(url, HttpMethod.GET,
                null, Message.class);
        return getSessionManager().downloadAPI(response.getBody().getMessage());
    }

    public void createSession() {
        Message clientPublicKey = new Message();
        clientPublicKey.setMessage("ClientPublicKey");
        ResponseEntity<Message> response = restTemplate.postForEntity(
                getSessionManager().getURLByKey("session"), clientPublicKey, Message.class);
        getSessionManager().setCookies(response.getHeaders().get("Set-Cookie"));
        getSessionManager().setServerPublicKey(response.getBody().getMessage());
    }

    public Message loginByCreditionals(String identifier, String password) {
        AuthMessage message = new AuthMessage(identifier, password);
        Message responseMessage = restTemplate.postForObject(getSessionManager().getURLByKey("login"),
                message, Message.class);
        return responseMessage;
    }

    public Message loginByAuthToken(String authToken) {
        Message responseMessage = restTemplate.getForObject(getSessionManager().getURLByKey("login") +
                        "?authToken=" + authToken, Message.class);
        return responseMessage;
    }
}
