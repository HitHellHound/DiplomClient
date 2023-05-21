package org.diplom.client;

import org.diplom.client.connection.communicator.FaceRecognitionCommunicator;
import org.diplom.client.connection.communicator.SessionCommunicator;
import org.diplom.client.crypto.CryptoManager;
import org.diplom.client.dto.ScriptMessage;
import org.diplom.client.manager.FaceRecognitionManager;
import org.diplom.client.manager.LocalEnvManager;
import org.diplom.client.manager.SessionManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        FaceRecognitionManager faceRecognitionManager = SPRING_CONTEXT.getBean("faceRecognitionManager", FaceRecognitionManager.class);

        //System.out.println(localEnvManager.createAuthToken());
        long start = System.currentTimeMillis();
        List<String> res = faceRecognitionManager.faceRecognition();
        res.forEach(System.out::println);
        System.out.println(1.0 * (System.currentTimeMillis() - start) / 1000);


        /*if (!sessionManager.loadAPI()) {
            localEnvManager.saveAPI(sessionCommunicator.downloadServerConfigs("http://localhost:8080/start").getMessage());
            if (!sessionManager.loadAPI()) {
                System.exit(10);
            }
        }

        sessionCommunicator.createSession();
        System.out.println(sessionCommunicator.loginByAuthToken("AuthTokenMotherBoardToken"));
        ScriptMessage scriptMessage = faceRecognitionCommunicator.startFaceRegistration("MyABOBA");*/
        //localEnvManager.saveFaceRegistrationFile(scriptMessage.getMessage());
    }

    public static List<String> readProcessOutput(InputStream inputStream) {
        try (BufferedReader output = new BufferedReader(new InputStreamReader(inputStream))) {
            return output.lines().collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String resolvePythonScriptPath(String projectWay) {
        File file = new File(projectWay);
        return file.getAbsolutePath();
    }
}
