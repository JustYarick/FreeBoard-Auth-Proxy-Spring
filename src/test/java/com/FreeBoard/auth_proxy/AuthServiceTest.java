package com.FreeBoard.auth_proxy;

import com.FreeBoard.auth_proxy.exception.ExceptionClass.SagaFailException;
import com.FreeBoard.auth_proxy.model.DTO.AuthRequestDto;
import com.FreeBoard.auth_proxy.model.DTO.NewUserEventDTO;
import com.FreeBoard.auth_proxy.model.DTO.NewUserRequestDto;
import com.FreeBoard.auth_proxy.service.AuthSagaService;
import com.FreeBoard.auth_proxy.service.AuthService;
import com.FreeBoard.auth_proxy.service.KafkaService;
import com.FreeBoard.auth_proxy.service.KeyCloakClient;
import org.springframework.boot.test.context.SpringBootTest;
import com.FreeBoard.auth_proxy.model.SagaStatus;
import com.FreeBoard.auth_proxy.model.entity.AuthSagaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.util.MultiValueMap;
import org.keycloak.representations.AccessTokenResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
class AuthServiceTest {

    private KeyCloakClient keyCloakClient;
    private KafkaService kafkaService;
    private AuthSagaService authSagaService;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        keyCloakClient = mock(KeyCloakClient.class);
        kafkaService = mock(KafkaService.class);
        authSagaService = mock(AuthSagaService.class);
        authService = new AuthService(keyCloakClient, kafkaService, authSagaService);
    }

    @Test
    void registerUser_successfulFlow() {
        // Arrange
        NewUserRequestDto.Credentials credentials = new NewUserRequestDto.Credentials();
        credentials.setValue("password123");

        NewUserRequestDto request = new NewUserRequestDto(
                "username",
                "email@example.com",
                true,
                List.of(credentials)
        );

        UUID sagaId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        AuthSagaEntity saga = AuthSagaEntity.builder()
                .id(sagaId)
                .status(SagaStatus.INIT)
                .createdAt(LocalDateTime.now())
                .build();

        when(authSagaService.initAuthSaga()).thenReturn(saga);
        when(keyCloakClient.createUserInKeycloak(request)).thenReturn(userId.toString());

        authService.registerUser(request);

        verify(authSagaService).markAsUserCreated(sagaId, userId);

        ArgumentCaptor<NewUserEventDTO> eventCaptor = ArgumentCaptor.forClass(NewUserEventDTO.class);
        verify(kafkaService).sendNewUserEvent(eventCaptor.capture());

        NewUserEventDTO capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getSagaId()).isEqualTo(sagaId);
        assertThat(capturedEvent.getUserId()).isEqualTo(userId);
        assertThat(capturedEvent.getUsername()).isEqualTo(request.getUsername());
        assertThat(capturedEvent.getEmail()).isEqualTo(request.getEmail());
    }

    @Test
    void registerUser_shouldHandleFailureAndMarkSagaAsFailed() {
        NewUserRequestDto.Credentials credentials = new NewUserRequestDto.Credentials();
        credentials.setValue("password123");

        NewUserRequestDto request = new NewUserRequestDto(
                "username",
                "email@example.com",
                true,
                List.of(credentials)
        );

        UUID sagaId = UUID.randomUUID();
        AuthSagaEntity saga = AuthSagaEntity.builder()
                .id(sagaId)
                .status(SagaStatus.INIT)
                .createdAt(LocalDateTime.now())
                .build();

        when(authSagaService.initAuthSaga()).thenReturn(saga);
        when(keyCloakClient.createUserInKeycloak(request)).thenThrow(new RuntimeException("Keycloak error"));

        SagaFailException exception = assertThrows(SagaFailException.class, () -> authService.registerUser(request));
        assertThat(exception.getMessage()).isEqualTo("Registration process failed");

        verify(authSagaService).markAsFailed(eq(sagaId), contains("Keycloak error"));
        verifyNoInteractions(kafkaService);
    }

    @Test
    void authenticateUser_shouldReturnAccessTokenResponse() {
        AuthRequestDto authRequest = new AuthRequestDto("username", "password");
        AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
        accessTokenResponse.setToken("some-token");

        when(keyCloakClient.authenticate(any(MultiValueMap.class))).thenReturn(accessTokenResponse);

        AccessTokenResponse result = authService.authenticateUser(authRequest);

        assertThat(result.getToken()).isEqualTo("some-token");

        ArgumentCaptor<MultiValueMap<String, String>> paramsCaptor = ArgumentCaptor.forClass(MultiValueMap.class);
        verify(keyCloakClient).authenticate(paramsCaptor.capture());

        MultiValueMap<String, String> capturedParams = paramsCaptor.getValue();
        assertThat(capturedParams.getFirst("username")).isEqualTo("username");
        assertThat(capturedParams.getFirst("password")).isEqualTo("password");
        assertThat(capturedParams.getFirst("grant_type")).isEqualTo("password");
    }

    @Test
    void refreshToken_shouldReturnAccessTokenResponse() {
        String refreshToken = "refresh-token";
        AccessTokenResponse accessTokenResponse = new AccessTokenResponse();
        accessTokenResponse.setToken("new-token");

        when(keyCloakClient.authenticate(any(MultiValueMap.class))).thenReturn(accessTokenResponse);

        AccessTokenResponse result = authService.refreshToken(refreshToken);

        assertThat(result.getToken()).isEqualTo("new-token");

        ArgumentCaptor<MultiValueMap<String, String>> paramsCaptor = ArgumentCaptor.forClass(MultiValueMap.class);
        verify(keyCloakClient).authenticate(paramsCaptor.capture());

        MultiValueMap<String, String> capturedParams = paramsCaptor.getValue();
        assertThat(capturedParams.getFirst("grant_type")).isEqualTo("refresh_token");
        assertThat(capturedParams.getFirst("refresh_token")).isEqualTo(refreshToken);
    }
}