package org.diplom.client;

import org.diplom.client.connection.*;
import org.diplom.client.connection.communicator.FaceRecognitionCommunicator;
import org.diplom.client.connection.communicator.SessionCommunicator;
import org.diplom.client.crypto.CryptoManager;
import org.diplom.client.crypto.GOST28147_89;
import org.diplom.client.crypto.GOST28147_89_Mode;
import org.diplom.client.dto.Message;
import org.diplom.client.dto.ScriptMessage;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;

/**
 * Hello world!
 */
public class App {
    private static ClassPathXmlApplicationContext SPRING_CONTEXT;

    public static void main(String[] args) throws Exception {
        SPRING_CONTEXT = new ClassPathXmlApplicationContext("applicationContext.xml");

        SessionCommunicator sessionCommunicator = SPRING_CONTEXT.getBean("sessionCommunicator", SessionCommunicator.class);
        FaceRecognitionCommunicator faceRecognitionCommunicator = SPRING_CONTEXT.getBean("faceRecognitionCommunicator", FaceRecognitionCommunicator.class);
        SessionManager sessionManager = SPRING_CONTEXT.getBean("sessionManager", SessionManager.class);
        LocalEnvManager localEnvManager = SPRING_CONTEXT.getBean("localEnvManager", LocalEnvManager.class);

        CryptoManager cryptoManager = SPRING_CONTEXT.getBean("cryptoManager", CryptoManager.class);

        if (!sessionManager.loadAPI()) {
            localEnvManager.saveAPI(sessionCommunicator.downloadServerConfigs("http://localhost:8080/start").getMessage());
            if (!sessionManager.loadAPI()) {
                System.exit(10);
            }
        }


        sessionCommunicator.createSession();
        System.out.println(sessionCommunicator.loginByAuthToken("AuthTokenMotherBoardToken"));
        ScriptMessage scriptMessage = faceRecognitionCommunicator.startFaceRegistration("MyABOBA");
        localEnvManager.saveFaceRegistrationFile(scriptMessage.getMessage());
    }
}
