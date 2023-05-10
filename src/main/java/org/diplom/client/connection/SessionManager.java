package org.diplom.client.connection;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.diplom.client.connection.LocalPathHelper.API_FILE_PATH;

@Component
public class SessionManager {
    private List<String> cookies;
    private String clientPrivateKey;
    private String serverPublicKey;
    private static Map<String, String> API;

    public boolean loadAPI() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            API = mapper.readValue(Path.of(API_FILE_PATH).toFile(), Map.class);
        }
        catch (Exception exception) {
            return false;
        }
        return true;
    }

    public boolean downloadAPI(String apiString) {
        try(FileWriter writer = new FileWriter(API_FILE_PATH, false)) {
            writer.append(apiString);
        }
        catch (Exception exception) {
            return false;
        }
        return loadAPI();
    }

    public String getBaseURI() {
        return API.get("base");
    }

    public String getURLByKey(String key) {
        return getBaseURI() + API.get(key);
    }

    public String getCookiesAsString() {
        StringBuilder stringBuilder = new StringBuilder();
        cookies.stream().forEach(cookie -> stringBuilder.append(cookie + "; "));
        return stringBuilder.toString();
    }
    public List<String> getCookies() {
        return cookies;
    }

    public void setCookies(List<String> cookies) {
        this.cookies = cookies;
    }

    public String getClientPrivateKey() {
        return clientPrivateKey;
    }

    public void setClientPrivateKey(String clientPrivateKey) {
        this.clientPrivateKey = clientPrivateKey;
    }

    public String getServerPublicKey() {
        return serverPublicKey;
    }

    public void setServerPublicKey(String serverPublicKey) {
        this.serverPublicKey = serverPublicKey;
    }
}
