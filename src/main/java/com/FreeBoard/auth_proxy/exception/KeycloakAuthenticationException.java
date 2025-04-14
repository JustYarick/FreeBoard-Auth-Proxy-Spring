package com.FreeBoard.auth_proxy.exception;

import org.springframework.http.HttpStatusCode;

public class KeycloakAuthenticationException extends RuntimeException {
    private final HttpStatusCode status;
    private final String responseBody;

    public KeycloakAuthenticationException(HttpStatusCode status, String responseBody) {
        super(responseBody);
        this.status = status;
        this.responseBody = responseBody;
    }

    public HttpStatusCode getStatus() {
        return status;
    }

    public String getResponseBody() {
        return responseBody;
    }
}
