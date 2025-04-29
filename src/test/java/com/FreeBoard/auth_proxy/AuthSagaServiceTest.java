package com.FreeBoard.auth_proxy;

import com.FreeBoard.auth_proxy.service.AuthSagaService;
import org.springframework.boot.test.context.SpringBootTest;
import com.FreeBoard.auth_proxy.model.SagaStatus;
import com.FreeBoard.auth_proxy.model.entity.AuthSagaEntity;
import com.FreeBoard.auth_proxy.repository.SagaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
class AuthSagaServiceTest {

    private SagaRepository sagaRepository;
    private AuthSagaService authSagaService;

    @BeforeEach
    void setUp() {
        sagaRepository = mock(SagaRepository.class);
        authSagaService = new AuthSagaService(sagaRepository);
    }

    @Test
    void initAuthSaga_shouldSaveAndReturnSaga() {
        AuthSagaEntity expectedSaga = AuthSagaEntity.builder()
                .userId("TEMP")
                .status(SagaStatus.INIT)
                .createdAt(LocalDateTime.now())
                .build();

        when(sagaRepository.save(Mockito.any(AuthSagaEntity.class))).thenReturn(expectedSaga);

        AuthSagaEntity result = authSagaService.initAuthSaga();

        assertThat(result.getUserId()).isEqualTo("TEMP");
        assertThat(result.getStatus()).isEqualTo(SagaStatus.INIT);
        verify(sagaRepository).save(Mockito.any(AuthSagaEntity.class));
    }

    @Test
    void getAuthSaga_shouldReturnSaga() {
        UUID sagaId = UUID.randomUUID();
        AuthSagaEntity saga = new AuthSagaEntity();
        when(sagaRepository.findById(sagaId)).thenReturn(Optional.of(saga));

        AuthSagaEntity result = authSagaService.getAuthSaga(sagaId);

        assertThat(result).isEqualTo(saga);
        verify(sagaRepository).findById(sagaId);
    }

    @Test
    void getAuthSaga_shouldThrowException_whenNotFound() {
        UUID sagaId = UUID.randomUUID();
        when(sagaRepository.findById(sagaId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> authSagaService.getAuthSaga(sagaId));
    }

    @Test
    void updateStatuses_shouldUpdateStatus() {
        UUID sagaId = UUID.randomUUID();
        AuthSagaEntity saga = new AuthSagaEntity();
        when(sagaRepository.findById(sagaId)).thenReturn(Optional.of(saga));
        when(sagaRepository.save(any(AuthSagaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthSagaEntity result = authSagaService.updateStatuses(sagaId, SagaStatus.PROFILE_CREATED);

        assertThat(result.getStatus()).isEqualTo(SagaStatus.PROFILE_CREATED);
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(sagaRepository).save(saga);
    }

    @Test
    void updateUserId_shouldUpdateUserId() {
        UUID sagaId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AuthSagaEntity saga = new AuthSagaEntity();
        when(sagaRepository.findById(sagaId)).thenReturn(Optional.of(saga));
        when(sagaRepository.save(any(AuthSagaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthSagaEntity result = authSagaService.updateUserId(sagaId, userId);

        assertThat(result.getUserId()).isEqualTo(userId.toString());
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(sagaRepository).save(saga);
    }

    @Test
    void markAsUserCreated_shouldUpdateUserIdAndStatus() {
        UUID sagaId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AuthSagaEntity saga = new AuthSagaEntity();
        when(sagaRepository.findById(sagaId)).thenReturn(Optional.of(saga));
        when(sagaRepository.save(any(AuthSagaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthSagaEntity result = authSagaService.markAsUserCreated(sagaId, userId);

        assertThat(result.getUserId()).isEqualTo(userId.toString());
        assertThat(result.getStatus()).isEqualTo(SagaStatus.USER_CREATED);
        assertThat(result.getCreatedAt()).isNotNull();
        verify(sagaRepository).save(saga);
    }

    @Test
    void markAsProfileCreated_shouldUpdateStatus() {
        UUID sagaId = UUID.randomUUID();
        AuthSagaEntity saga = new AuthSagaEntity();
        when(sagaRepository.findById(sagaId)).thenReturn(Optional.of(saga));
        when(sagaRepository.save(any(AuthSagaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthSagaEntity result = authSagaService.markAsProfileCreated(sagaId);

        assertThat(result.getStatus()).isEqualTo(SagaStatus.PROFILE_CREATED);
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(sagaRepository).save(saga);
    }

    @Test
    void markAsFailed_shouldUpdateStatusAndErrorMessage() {
        UUID sagaId = UUID.randomUUID();
        String errorMessage = "Something went wrong";
        AuthSagaEntity saga = new AuthSagaEntity();
        when(sagaRepository.findById(sagaId)).thenReturn(Optional.of(saga));
        when(sagaRepository.save(any(AuthSagaEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthSagaEntity result = authSagaService.markAsFailed(sagaId, errorMessage);

        assertThat(result.getStatus()).isEqualTo(SagaStatus.FAILED);
        assertThat(result.getErrorMessage()).isEqualTo(errorMessage);
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(sagaRepository).save(saga);
    }
}