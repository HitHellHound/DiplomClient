package org.diplom.client.connection;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.nio.file.Path;
import java.util.Map;

@Component
public class LocalEnvManager {
    public static final String RESOURCE_PATH = "src/main/resources";
    public static final String API_FILE_PATH = RESOURCE_PATH + "/api";
    public static final String FACE_RECOGNITION_SCRIPT_PATH = RESOURCE_PATH + "/faceRecognition.py";
    public static final String FACE_REGISTRATION_SCRIPT_PATH = RESOURCE_PATH + "/faceRegistration.py";

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
}
