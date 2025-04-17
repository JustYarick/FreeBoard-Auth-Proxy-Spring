package com.FreeBoard.auth_proxy.model;

public enum SagaStatus {
    INIT,
    USER_CREATED,
    PROFILE_CREATED,
    COMPLETED,
    COMPENSATING,
    FAILED
}
