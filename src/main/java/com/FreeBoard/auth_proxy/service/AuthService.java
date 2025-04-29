package com.FreeBoard.auth_proxy.service;

import com.FreeBoard.auth_proxy.exception.ExceptionClass.SagaFailException;
import com.FreeBoard.auth_proxy.model.DTO.AuthRequestDto;
import com.FreeBoard.auth_proxy.model.DTO.NewUserEventDTO;
import com.FreeBoard.auth_proxy.model.DTO.NewUserRequestDto;
import com.FreeBoard.auth_proxy.model.entity.AuthSagaEntity;
import lombok.AllArgsConstructor;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthService {

    private final KeyCloakClient keyCloakClient;
    private final KafkaService kafkaService;
    private final AuthSagaService authSagaService;

    public AccessTokenResponse registerUser(NewUserRequestDto userRequest) {
        AuthSagaEntity saga = authSagaService.initAuthSaga();
        UUID sagaId = saga.getId();

        try {
            String userId = keyCloakClient.createUserInKeycloak(userRequest);

            authSagaService.markAsUserCreated(sagaId, UUID.fromString(userId));

            NewUserEventDTO event = new NewUserEventDTO(
                    sagaId,
                    UUID.fromString(userId),
                    userRequest.getUsername(),
                    userRequest.getEmail()
            );

            kafkaService.sendNewUserEvent(event);

        } catch (Exception e) {
            authSagaService.markAsFailed(sagaId, e.getMessage());
            throw new SagaFailException("Registration process failed", e);
        }
        AuthRequestDto authRequestDto = new AuthRequestDto(userRequest.getUsername(), userRequest.getCredentials().getFirst().getValue());
        return authenticateUser(authRequestDto);
    }

    public AccessTokenResponse authenticateUser(AuthRequestDto authRequestDto) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("username", authRequestDto.getUsername());
        parameters.add("password", authRequestDto.getPassword());
        parameters.add("grant_type", "password");

        return keyCloakClient.authenticate(parameters);
    }

    public AccessTokenResponse refreshToken(String refreshToken) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("grant_type", "refresh_token");
        parameters.add("refresh_token", refreshToken);

        return keyCloakClient.authenticate(parameters);
    }
}