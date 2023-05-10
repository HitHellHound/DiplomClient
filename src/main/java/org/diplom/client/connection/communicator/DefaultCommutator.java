package org.diplom.client.connection.communicator;

import org.diplom.client.connection.SessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class DefaultCommutator implements Communicator {
    @Autowired
    private SessionManager sessionManager;

    @Override
    public HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cookie", getSessionManager().getCookiesAsString());
        return headers;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
}
