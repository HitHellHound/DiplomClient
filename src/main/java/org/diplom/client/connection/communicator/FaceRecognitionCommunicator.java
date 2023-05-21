package org.diplom.client.connection.communicator;

import org.diplom.client.crypto.CryptoManager;
import org.diplom.client.crypto.GOST28147_89_Mode;
import org.diplom.client.dto.Message;
import org.diplom.client.dto.RecognitionMessage;
import org.diplom.client.dto.ScriptMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Component
public class FaceRecognitionCommunicator extends DefaultCommutator {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private CryptoManager cryptoManager;

    public ScriptMessage startFaceRegistration(String hardwareSerialNumber) {
        Message message = new Message(
                cryptoManager.sessionEncryption(hardwareSerialNumber.getBytes(StandardCharsets.UTF_8)));

        ResponseEntity<ScriptMessage> response = restTemplate.exchange(
                sessionManager.getURLByKey("registration"),
                HttpMethod.POST, new HttpEntity<>(message, createHeaders()), ScriptMessage.class);

        return response.getBody();
    }

    public Message finishFaceRegistration(String hardwareSerialNumber, String faceVector) {
        RecognitionMessage message = new RecognitionMessage();

        String encryptedFaceVector = Base64.getEncoder().encodeToString(
                sessionManager.getSessionGostCipher().cipher(
                        faceVector.getBytes(StandardCharsets.UTF_8), GOST28147_89_Mode.GAMMA));
        message.setEmbeddedFaceVectors(List.of(encryptedFaceVector));

        message.setMessage(cryptoManager.sessionEncryption(hardwareSerialNumber.getBytes(StandardCharsets.UTF_8)));

        ResponseEntity<Message> response = restTemplate.exchange(
                sessionManager.getURLByKey("registration"),
                HttpMethod.PUT, new HttpEntity<>(message, createHeaders()), Message.class);

        return response.getBody();
    }

    public ScriptMessage notifyServer() {
        ResponseEntity<ScriptMessage> response = restTemplate.exchange(
                sessionManager.getURLByKey("notify"), HttpMethod.GET,
                new HttpEntity<String>(createHeaders()), ScriptMessage.class);
        return response.getBody();
    }

    public ScriptMessage askForScriptAndNotify(String hardwareSerialNumber) {
        ResponseEntity<ScriptMessage> response = restTemplate.exchange(
                sessionManager.getURLByKey("notify") + "?needScriptHWSN=" + hardwareSerialNumber,
                HttpMethod.GET, new HttpEntity<String>(createHeaders()), ScriptMessage.class);
        return response.getBody();
    }

    public Message sendVectorsToVerify(List<String> embeddedFaceVectors) {
        RecognitionMessage message = new RecognitionMessage();
        message.setEmbeddedFaceVectors(encryptFaceVectors(embeddedFaceVectors));

        ResponseEntity<Message> response = restTemplate.exchange(
                sessionManager.getURLByKey("recognition"),
                HttpMethod.POST, new HttpEntity<>(message, createHeaders()), Message.class);

        return response.getBody();
    }

    private List<String> encryptFaceVectors(List<String> faceVectors) {
        List<String> vectors = new ArrayList<>();

        for (String faceVector : faceVectors) {
            byte[] vector = sessionManager.getSessionGostCipher().cipher(faceVector.getBytes(StandardCharsets.UTF_8),
                    GOST28147_89_Mode.GAMMA);
            vectors.add(Base64.getEncoder().encodeToString(vector));
        }

        return vectors;
    }
}
