package org.diplom.client.manager;

import org.diplom.client.exception.CameraNotFoundException;
import org.diplom.client.exception.ScriptExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FaceRecognitionManager {
    @Autowired
    private LocalEnvManager localEnvManager;

    public String faceRegistration() throws CameraNotFoundException, ScriptExecutionException {
        List<String> results = new ArrayList<>();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("python", resolvePythonScriptPath(LocalEnvManager.FACE_REGISTRATION_SCRIPT_PATH));

            Process process = processBuilder.start();
            results = readProcessOutput(process.getInputStream());

            int exitCode = process.waitFor();

            if (exitCode == 1) {
                throw new CameraNotFoundException("Connect camera!");
            } else if (exitCode != 0) {
                throw new ScriptExecutionException("Something went wrong while FaceRegistration script execution");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return results.stream().findFirst().orElseThrow(() -> new ScriptExecutionException("There no face vector"));
    }

    public List<String> faceRecognition() throws CameraNotFoundException, ScriptExecutionException {
        List<String> results = new ArrayList<>();
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("python", resolvePythonScriptPath(LocalEnvManager.FACE_RECOGNITION_SCRIPT_PATH));

            Process process = processBuilder.start();
            results = readProcessOutput(process.getInputStream());

            int exitCode = process.waitFor();

            if (exitCode == 1) {
                throw new CameraNotFoundException("Connect camera!");
            } else if (exitCode != 0) {
                throw new ScriptExecutionException("Something went wrong while FaceRecognition script execution");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return results.stream().filter(result -> result.startsWith("["))
                .flatMap(result -> {
                    if (StringUtils.countOccurrencesOf(result, "]") > 1)
                        return Arrays.stream(result.split("]"))
                                .filter(str -> str.startsWith("["))
                                .map(str -> str + "]");
                    else
                        return Arrays.stream(new String[]{result});
                }).collect(Collectors.toList());
    }

    private String resolvePythonScriptPath(String projectWay) {
        File file = new File(projectWay);
        return file.getAbsolutePath();
    }

    private List<String> readProcessOutput(InputStream inputStream) {
        try (BufferedReader output = new BufferedReader(new InputStreamReader(inputStream))) {
            return output.lines().collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
