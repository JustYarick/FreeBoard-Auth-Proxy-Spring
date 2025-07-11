package com.FreeBoard.auth_proxy.service;

import com.FreeBoard.auth_proxy.model.SagaStatus;
import com.FreeBoard.auth_proxy.model.entity.AuthSagaEntity;
import com.FreeBoard.auth_proxy.repository.SagaRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@AllArgsConstructor
public class AuthSagaService {

    private final SagaRepository sagaRepository;

    public AuthSagaEntity initAuthSaga() {
        AuthSagaEntity saga = AuthSagaEntity.builder()
                .userId("TEMP")
                .status(SagaStatus.INIT)
                .createdAt(LocalDateTime.now())
                .build();
        return sagaRepository.save(saga);
    }

    public AuthSagaEntity getAuthSaga(UUID sagaId) {
        return sagaRepository.findById(sagaId)
                .orElseThrow(() -> new RuntimeException("Saga not found"));
    }

    public AuthSagaEntity updateStatuses(UUID sagaId, SagaStatus status) {
        AuthSagaEntity saga = getAuthSaga(sagaId);

        saga.setStatus(status);
        saga.setUpdatedAt(LocalDateTime.now());
        return sagaRepository.save(saga);
    }

    public AuthSagaEntity updateUserId(UUID sagaId, UUID userId) {
        AuthSagaEntity saga = getAuthSaga(sagaId);

        saga.setUserId(userId.toString());
        saga.setUpdatedAt(LocalDateTime.now());
        return sagaRepository.save(saga);
    }

    public AuthSagaEntity markAsUserCreated(UUID sagaId, UUID userId) {
        AuthSagaEntity saga = getAuthSaga(sagaId);

        log.info("User for saga:{} successfully registered", sagaId);
        saga.setUserId(userId.toString());
        saga.setStatus(SagaStatus.USER_CREATED);
        saga.setCreatedAt(LocalDateTime.now());
        return sagaRepository.save(saga);
    }

    public AuthSagaEntity markAsProfileCreated(UUID sagaId) {
        AuthSagaEntity saga = getAuthSaga(sagaId);

        log.info("Profile for saga:{} successfully created", sagaId);
        saga.setStatus(SagaStatus.PROFILE_CREATED);
        saga.setUpdatedAt(LocalDateTime.now());
        return sagaRepository.save(saga);
    }

    public AuthSagaEntity markAsFailed(UUID sagaId, String errorMessage) {
        AuthSagaEntity saga = getAuthSaga(sagaId);

        log.error("Saga: {} failed error: {}", sagaId, errorMessage);
        saga.setStatus(SagaStatus.FAILED);
        saga.setErrorMessage(errorMessage);
        saga.setUpdatedAt(LocalDateTime.now());
        return sagaRepository.save(saga);
    }
}
