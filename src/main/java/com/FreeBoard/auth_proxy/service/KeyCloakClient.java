package com.FreeBoard.auth_proxy.service;

import com.FreeBoard.auth_proxy.model.DTO.AccessTokenResponse;
import com.FreeBoard.auth_proxy.model.DTO.AuthRequestDto;
import com.FreeBoard.auth_proxy.model.DTO.NewUserEventDTO;
import com.FreeBoard.auth_proxy.model.DTO.NewUserRequestDto;
import com.FreeBoard.auth_proxy.model.entity.AuthSagaEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

import static com.FreeBoard.auth_proxy.model.SagaStatus.USER_CREATED;

@Component
@Slf4j
public class KeyCloakClient {

    @Value("${keycloak.auth-server-url}")
    private String keyCloakUrl;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate;
    private final KafkaService kafkaService;
    private final AuthSagaService authSagaService;

    public KeyCloakClient(RestTemplate restTemplate,
                          KafkaService kafkaService,
                          AuthSagaService authSagaService) {

        this.restTemplate = restTemplate;
        this.kafkaService = kafkaService;
        this.authSagaService = authSagaService;
    }

    public AccessTokenResponse authenticate(AuthRequestDto request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("grant_type", "password");
        parameters.add("client_id", clientId);
        parameters.add("client_secret", clientSecret);
        parameters.add("username", request.getUsername());
        parameters.add("password", request.getPassword());

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(parameters, headers);

        ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(
                getAuthUrl(),
                HttpMethod.POST,
                entity,
                AccessTokenResponse.class
        );
        return response.getBody();
    }

    public AccessTokenResponse refreshToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("grant_type", "refresh_token");
        parameters.add("client_id", clientId);
        parameters.add("client_secret", clientSecret);
        parameters.add("refresh_token", refreshToken);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(parameters, headers);

        return restTemplate.exchange(getAuthUrl(),
                HttpMethod.POST,
                entity,
                AccessTokenResponse.class).getBody();
    }

    public void registerUser(NewUserRequestDto userRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + getAdminToken());

        HttpEntity<NewUserRequestDto> entity = new HttpEntity<>(userRequest, headers);
        String url = String.format("%s/admin/realms/%s/users", keyCloakUrl, realm);

        System.out.println(entity.getBody().toString());
        AuthSagaEntity saga = authSagaService.initAuthSaga();
        UUID sagaId = saga.getId();

        try {
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                URI location = response.getHeaders().getLocation();
                if (location != null) {
                    String path = location.getPath();
                    String userId = path.substring(path.lastIndexOf('/') + 1);

                    authSagaService.markAsUserCreated(sagaId, UUID.fromString(userId));

                    NewUserEventDTO event = new NewUserEventDTO(
                            sagaId,
                            UUID.fromString(userId),
                            userRequest.getUsername(),
                            userRequest.getEmail()
                    );

                    kafkaService.sendNewUserEvent(event);
                } else {
                    authSagaService.markAsFailed(sagaId, "Location header not found");
                    throw new IllegalStateException("Location header not found");
                }
            } else {
                authSagaService.markAsFailed(sagaId, "Keycloak registration failed: " + response.getStatusCode());
                throw new IllegalStateException("Keycloak registration failed");
            }
        } catch (Exception ex) {
            authSagaService.markAsFailed(sagaId, ex.getMessage());
            throw new RuntimeException("Saga failed", ex);
        }
    }

    private String getAdminToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("client_id", clientId);
        parameters.add("client_secret", clientSecret);
        parameters.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(parameters, headers);

        ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(
                getAuthUrl(),
                HttpMethod.POST,
                entity,
                AccessTokenResponse.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return response.getBody().getAccessToken();
        } else {
            log.error("Failed to obtain admin token. Status: {}, Body: {}",
                    response.getStatusCode(), response.getBody());
            throw new RuntimeException("Could not obtain admin token from Keycloak.");
        }
    }

    private String getAuthUrl() {
        return UriComponentsBuilder.fromHttpUrl(keyCloakUrl)
                .pathSegment("realms")
                .pathSegment(realm)
                .pathSegment("protocol")
                .pathSegment("openid-connect")
                .pathSegment("token")
                .toUriString();
    }
}
