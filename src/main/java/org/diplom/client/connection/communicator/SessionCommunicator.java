package org.diplom.client.connection.communicator;

import org.diplom.client.dto.AuthMessage;
import org.diplom.client.dto.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SessionCommunicator extends DefaultCommutator {
    @Autowired
    private RestTemplate restTemplate;

    public Message downloadServerConfigs(String url) {
        ResponseEntity<Message> response = restTemplate.exchange(url, HttpMethod.GET,
                null, Message.class);
        return response.getBody();
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
        ResponseEntity<Message> response = restTemplate.exchange(getSessionManager().getURLByKey("login"), HttpMethod.POST,
                new HttpEntity<>(message, createHeaders()), Message.class);
        return response.getBody();
    }

    public Message loginByAuthToken(String authToken) {
        Message message = new Message(authToken);
        ResponseEntity<Message> response = restTemplate.exchange(getSessionManager().getURLByKey("login"), HttpMethod.PUT,
                new HttpEntity<>(message, createHeaders()), Message.class);
        return response.getBody();
    }
}
