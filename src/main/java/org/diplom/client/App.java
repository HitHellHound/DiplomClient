package org.diplom.client;

import org.diplom.client.connection.*;
import org.diplom.client.connection.communicator.FaceRecognitionCommunicator;
import org.diplom.client.connection.communicator.SessionCommunicator;
import org.springframework.context.support.ClassPathXmlApplicationContext;

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

        if (!sessionManager.loadAPI()) {
            localEnvManager.saveAPI(sessionCommunicator.downloadServerConfigs("http://localhost:8080/start").getMessage());
            if (!sessionManager.loadAPI()) {
                System.exit(10);
            }
        }

        sessionCommunicator.createSession();
        System.out.println(sessionCommunicator.loginByAuthToken("AuthToken"));
        System.out.println(sessionCommunicator.loginByCreditionals("testUser", "password"));
    }
}
