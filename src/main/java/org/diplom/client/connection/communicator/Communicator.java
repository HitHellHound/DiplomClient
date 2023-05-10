package org.diplom.client.connection.communicator;

import org.springframework.http.HttpHeaders;

public interface Communicator {
    HttpHeaders createHeaders();
}
