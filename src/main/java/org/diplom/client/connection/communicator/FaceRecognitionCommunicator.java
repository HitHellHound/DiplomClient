package org.diplom.client.connection.communicator;

import org.diplom.client.dto.Message;
import org.diplom.client.dto.RecognitionMessage;
import org.diplom.client.dto.ScriptMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class FaceRecognitionCommunicator extends DefaultCommutator {
    @Autowired
    private RestTemplate restTemplate;

    public ScriptMessage startFaceRegistration(String hardwareSerialNumber) {
        ResponseEntity<ScriptMessage> response = restTemplate.exchange(
                getSessionManager().getURLByKey("registration") + "?hardwareSerialNumber=" + hardwareSerialNumber,
                HttpMethod.GET, new HttpEntity<String>(createHeaders()), ScriptMessage.class);
        return response.getBody();
    }

    public Message finishFaceRegistration(String hardwareSerialNumber, String faceVector) {
        Message message = new Message();
        message.setMessage(faceVector);
        ResponseEntity<Message> response = restTemplate.exchange(
                getSessionManager().getURLByKey("registration") + "?hardwareSerialNumber=" + hardwareSerialNumber,
                HttpMethod.POST, new HttpEntity<>(message, createHeaders()), Message.class);
        return response.getBody();
    }

    public ScriptMessage notifyServer() {
        ResponseEntity<ScriptMessage> response = restTemplate.exchange(
                getSessionManager().getURLByKey("notify"), HttpMethod.GET,
                new HttpEntity<String>(createHeaders()), ScriptMessage.class);
        return response.getBody();
    }

    public ScriptMessage askForScriptAndNotify(String hardwareSerialNumber) {
        ResponseEntity<ScriptMessage> response = restTemplate.exchange(
                getSessionManager().getURLByKey("notify") + "?hardwareSerialNumber=" + hardwareSerialNumber,
                HttpMethod.GET, new HttpEntity<String>(createHeaders()), ScriptMessage.class);
        return response.getBody();
    }

    public Message sendVectorsToVerify(List<String> embeddedFaceVectors) {
        RecognitionMessage message = new RecognitionMessage();
        message.setEmbeddedFaceVectors(embeddedFaceVectors);
        ResponseEntity<Message> response = restTemplate.exchange(
                getSessionManager().getURLByKey("recognition"),
                HttpMethod.POST, new HttpEntity<>(message, createHeaders()), Message.class);
        return response.getBody();
    }
}
