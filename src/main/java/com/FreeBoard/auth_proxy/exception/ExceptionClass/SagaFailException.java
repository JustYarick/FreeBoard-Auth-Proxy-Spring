package com.FreeBoard.auth_proxy.exception.ExceptionClass;

public class SagaFailException extends RuntimeException {
    public SagaFailException(String message, Throwable cause) {
        super(message, cause);
    }

    public SagaFailException(String message) {
        super(message);
    }
}
