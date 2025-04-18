package com.FreeBoard.auth_proxy.exception.ExceptionClass;

public class InvalidCredential extends RuntimeException {
    public InvalidCredential(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidCredential(String message) {
        super(message);
    }
}
