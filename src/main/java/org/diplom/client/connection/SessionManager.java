package org.diplom.client.connection;

import org.diplom.client.crypto.GOST28147_89;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SessionManager {
    @Autowired
    private LocalEnvManager fileManager;
    private List<String> cookies;
    private byte[] clientPrivateKey;
    private byte[] clientPublicKey;
    private byte[] serverPublicKey;
    private GOST28147_89 sessionGostCipher;
    private GOST28147_89 scriptGostCipher;
    private Map<String, String> API;

    public boolean loadAPI() {
        API = fileManager.loadAPI();
        if (API != null)
            return true;
        else
            return false;
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

    public byte[] getClientPrivateKey() {
        return clientPrivateKey;
    }

    public void setClientPrivateKey(byte[] clientPrivateKey) {
        this.clientPrivateKey = clientPrivateKey;
    }

    public byte[] getClientPublicKey() {
        return clientPublicKey;
    }

    public void setClientPublicKey(byte[] clientPublicKey) {
        this.clientPublicKey = clientPublicKey;
    }

    public byte[] getServerPublicKey() {
        return serverPublicKey;
    }

    public void setServerPublicKey(byte[] serverPublicKey) {
        this.serverPublicKey = serverPublicKey;
    }

    public GOST28147_89 getSessionGostCipher() {
        return sessionGostCipher;
    }

    public void setSessionGostCipher(GOST28147_89 sessionGostCipher) {
        this.sessionGostCipher = sessionGostCipher;
    }

    public GOST28147_89 getScriptGostCipher() {
        return scriptGostCipher;
    }

    public void setScriptGostCipher(GOST28147_89 scriptGostCipher) {
        this.scriptGostCipher = scriptGostCipher;
    }
}
