package com.FreeBoard.auth_proxy.service;

import com.FreeBoard.auth_proxy.exception.ExceptionClass.KeycalokException;
import com.FreeBoard.auth_proxy.exception.ExceptionClass.UserAlreadyExistException;
import com.FreeBoard.auth_proxy.exception.ExceptionClass.UserNotFoundException;
import com.FreeBoard.auth_proxy.model.DTO.NewUserRequestDto;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@Setter
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


    public KeyCloakClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public UserRepresentation getUserById(String userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAdminToken());

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String url = String.format("%s/admin/realms/%s/users/%s", keyCloakUrl, realm, userId);

        ResponseEntity<UserRepresentation> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                UserRepresentation.class
        );

        if (response.getStatusCode().is2xxSuccessful()) {
            return response.getBody();
        } else if (response.getStatusCode().is4xxClientError()) {
            throw  new UserNotFoundException("User not found");
        }
        else {
            throw new KeycalokException("Error getting user: " + userId);
        }
    }


    public AccessTokenResponse authenticate(MultiValueMap<String, String> parameters) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        parameters.add("client_id", clientId);
        parameters.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(parameters, headers);

        ResponseEntity<AccessTokenResponse> response = restTemplate.exchange(
                getAuthUrl(),
                HttpMethod.POST,
                entity,
                AccessTokenResponse.class
        );
        return response.getBody();
    }

    public String createUserInKeycloak(NewUserRequestDto userRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getAdminToken());

        HttpEntity<NewUserRequestDto> entity = new HttpEntity<>(userRequest, headers);
        String url = String.format("%s/admin/realms/%s/users", keyCloakUrl, realm);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);

            if (response.getStatusCode() == HttpStatus.CREATED) {
                URI location = response.getHeaders().getLocation();
                if (location != null) {
                    String path = location.getPath();
                    return path.substring(path.lastIndexOf('/') + 1);
                } else {
                    throw new IllegalStateException("Location header not found");
                }
            } else {
                throw new IllegalStateException("Keycloak registration failed: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error creating user in Keycloak: {}", e.getMessage());
            throw new KeycalokException("Failed to create user in Keycloak", e);
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
            return response.getBody().getToken();
        } else {
            log.error("Failed to obtain admin token. Status: {}, Body: {}",
                    response.getStatusCode(), response.getBody());
            throw new KeycalokException("Could not obtain admin token from Keycloak.");
        }
    }

    private String getAuthUrl() {
        return UriComponentsBuilder.fromUri(
                URI.create(keyCloakUrl)
                )
                .pathSegment("realms", realm, "protocol", "openid-connect", "token")
                .toUriString();
    }


    public boolean isPasswordCorrect(String userId, String password) {
        try {
            UserRepresentation user = getUserById(userId);
            String username = user.getUsername();

            if (username == null || username.isBlank()) {
                log.warn("Username not found for userId: {}", userId);
                return false;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "password");
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("username", username);
            params.add("password", password);

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    getAuthUrl(),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            return response.getStatusCode() == HttpStatus.OK;

        } catch (HttpClientErrorException.Unauthorized e) {
            return false;
        } catch (Exception e) {
            log.error("Error checking password for userId {}: {}", userId, e.getMessage());
            return false;
        }
    }


    public void updateUserEmail(String userId, String newEmail) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(getAdminToken());

        Map<String, Object> updatePayload = new HashMap<>();
        updatePayload.put("email", newEmail);
        updatePayload.put("emailVerified", false);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(updatePayload, headers);

        String url = String.format("%s/admin/realms/%s/users/%s", keyCloakUrl, realm, userId);

        try {
            restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new UserAlreadyExistException("User already exists");
            }
            throw new KeycalokException("Email update failed", e);
        } catch (Exception e) {
            log.error("Failed to update email for user {}: {}", userId, e.getMessage());
            throw new KeycalokException("Email update failed", e);
        }

    }


    public void updateUserPassword(String userId, Map<String, Object> passwordData) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getAdminToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(passwordData, headers);
        String url = String.format("%s/admin/realms/%s/users/%s/reset-password", keyCloakUrl, realm, userId);

        try {
            restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        } catch (Exception e) {
            log.error("Failed to change password for user {}: {}", userId, e.getMessage());
            throw new KeycalokException("Failed to change password");
        }
    }

    public void updateUsername(String userId, String newUsername) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + getAdminToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> usernameData = new HashMap<>();
        usernameData.put("username", newUsername);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(usernameData, headers);
        String url = String.format("%s/admin/realms/%s/users/%s", keyCloakUrl, realm, userId);

        try {
            restTemplate.exchange(url, HttpMethod.PUT, entity, Void.class);
        } catch (Exception e) {
            log.error("Failed to change username for user {}: {}", userId, e.getMessage());
            throw new KeycalokException("Failed to change username");
        }
    }
}