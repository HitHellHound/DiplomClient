package org.diplom.client.connection.communicator;

import org.diplom.client.crypto.CryptoManager;
import org.diplom.client.crypto.GOST28147_89;
import org.diplom.client.dto.AuthMessage;
import org.diplom.client.dto.Message;
import org.diplom.client.dto.ScriptMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.Base64;

@Component
public class SessionCommunicator extends DefaultCommutator {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private CryptoManager cryptoManager;

    public Message downloadServerConfigs(String url) {
        ResponseEntity<Message> response = restTemplate.exchange(url, HttpMethod.GET,
                null, Message.class);
        return response.getBody();
    }

    public void createSession() {
        Message message = new Message();

        KeyPair keyPair = cryptoManager.generateSessionKeys();

        sessionManager.setClientPublicKey(keyPair.getPublic().getEncoded());
        sessionManager.setClientPrivateKey(keyPair.getPrivate().getEncoded());
        message.setMessage(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));

        ResponseEntity<Message> response = restTemplate.postForEntity(
                sessionManager.getURLByKey("session"), message, Message.class);

        sessionManager.setCookies(response.getHeaders().get("Set-Cookie"));
        sessionManager.setServerPublicKey(Base64.getDecoder().decode(response.getBody().getMessage()));

        cryptoManager.initSessionCiphers();
    }

    public ScriptMessage loginByCreditionals(String identifier, String password) {
        String encryptedIdentifier = cryptoManager.sessionEncryption(identifier.getBytes(StandardCharsets.UTF_8));
        String encryptedPassword = cryptoManager.sessionEncryption(password.getBytes(StandardCharsets.UTF_8));

        AuthMessage message = new AuthMessage(encryptedIdentifier, encryptedPassword);

        ResponseEntity<ScriptMessage> response = restTemplate.exchange(sessionManager.getURLByKey("login"), HttpMethod.POST,
                new HttpEntity<>(message, createHeaders()), ScriptMessage.class);

        if (response.getBody().getCode() == 200)
            createSessionGostCipher(response.getBody());

        return response.getBody();
    }

    public ScriptMessage loginByAuthToken(String authToken) {
        Message message = new Message(cryptoManager.sessionEncryption(authToken.getBytes(StandardCharsets.UTF_8)));

        ResponseEntity<ScriptMessage> response = restTemplate.exchange(sessionManager.getURLByKey("login"), HttpMethod.PUT,
                new HttpEntity<>(message, createHeaders()), ScriptMessage.class);

        if (response.getBody().getCode() == 200)
            createSessionGostCipher(response.getBody());

        return response.getBody();
    }

    private void createSessionGostCipher(ScriptMessage message) {
        byte[] key = cryptoManager.sessionDecryption(message.getScriptKey());
        byte[][] table = new byte[8][16];
        for (int i = 0; i < 8; i++) {
            table[i] = cryptoManager.sessionDecryption(message.getScriptTable().get(i));
        }
        byte[] syncMessage = cryptoManager.sessionDecryption(message.getScriptSyncMessage());

        sessionManager.setSessionGostCipher(new GOST28147_89(key, table, syncMessage));
    }
}
