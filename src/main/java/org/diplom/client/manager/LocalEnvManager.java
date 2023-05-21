package org.diplom.client.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.diplom.client.crypto.GOST28147_89;
import org.diplom.client.crypto.GOST28147_89_Mode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class LocalEnvManager {
    @Autowired
    private SessionManager sessionManager;
    public static final String RESOURCE_PATH = "src/main/resources";
    public static final String API_FILE_PATH = RESOURCE_PATH + "/api";
    public static final String FACE_RECOGNITION_SCRIPT_PATH = RESOURCE_PATH + "/faceRecognition.py";
    public static final String FACE_RECOGNITION_ENCRYPTED_SCRIPT_PATH = RESOURCE_PATH + "/faceRecognition";
    public static final String FACE_REGISTRATION_SCRIPT_PATH = RESOURCE_PATH + "/faceRegistration.py";
    public static final int LENGTH_OF_AUTH_TOKEN = 245;

    public void saveAPI(String api) {
        try(FileWriter writer = new FileWriter(API_FILE_PATH, false)) {
            writer.append(api);
        }
        catch (Exception exception) {

        }
    }

    public Map<String, String> loadAPI() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(Path.of(API_FILE_PATH).toFile(), Map.class);
        }
        catch (Exception exception) {
            return null;
        }
    }

    public void saveScript(String encryptedScript, String path) {
        GOST28147_89 gostCipher = sessionManager.getSessionGostCipher();
        byte[] encryptedScriptBytes = Base64.getDecoder().decode(encryptedScript);
        byte[] script = gostCipher.cipher(encryptedScriptBytes, GOST28147_89_Mode.GAMMA);
        try (BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(path))) {
            writer.write(script);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void decryptFaceRecognitionScript() {
        GOST28147_89 gostCipher = sessionManager.getScriptGostCipher();
        try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream(FACE_RECOGNITION_ENCRYPTED_SCRIPT_PATH))) {
            byte[] encryptedScript = reader.readAllBytes();
            byte[] script = gostCipher.cipher(encryptedScript, GOST28147_89_Mode.GAMMA);

            BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(FACE_REGISTRATION_SCRIPT_PATH));
            writer.write(script);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getHardwareSerialNumber() {
        String hardwareSerialNumber = "";
        try {
            Process process = Runtime.getRuntime().exec("wmic diskdrive get serialnumber");
            List<String> results = readProcessOutput(process.getInputStream());
            results = results.stream()
                    .filter(result -> result != null && !result.isEmpty())
                    .map(String::strip)
                    .collect(Collectors.toList());

            hardwareSerialNumber += results.get(1);

            process = Runtime.getRuntime().exec("wmic baseboard get serialnumber");
            results = readProcessOutput(process.getInputStream());
            results = results.stream()
                    .filter(result -> result != null && !result.isEmpty())
                    .map(String::strip)
                    .collect(Collectors.toList());

            hardwareSerialNumber += results.get(1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return hardwareSerialNumber;
    }

    public String createAuthToken() {
        byte[] hardwareSerialNumber = getHardwareSerialNumber().getBytes(StandardCharsets.UTF_8);
        byte[] result;
        try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream(FACE_RECOGNITION_ENCRYPTED_SCRIPT_PATH))) {
            result = reader.readNBytes(LENGTH_OF_AUTH_TOKEN);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < hardwareSerialNumber.length; i++) {
            result[i] = hardwareSerialNumber[i];
        }
        return new String(result, StandardCharsets.UTF_8);
    }

    private List<String> readProcessOutput(InputStream inputStream) {
        try (BufferedReader output = new BufferedReader(new InputStreamReader(inputStream))) {
            return output.lines().collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
