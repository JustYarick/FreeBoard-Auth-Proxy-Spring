package com.FreeBoard.auth_proxy;

import com.FreeBoard.auth_proxy.exception.ExceptionClass.KeycalokException;
import com.FreeBoard.auth_proxy.exception.ExceptionClass.UserAlreadyExistException;
import com.FreeBoard.auth_proxy.model.DTO.NewUserRequestDto;
import com.FreeBoard.auth_proxy.service.KeyCloakClient;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
class KeyCloakClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private KeyCloakClient keyCloakClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        keyCloakClient = new KeyCloakClient(restTemplate);

        // Устанавливаем значения вручную, чтобы @Value не мешал
        keyCloakClient.setKeyCloakUrl("http://localhost:8080");
        keyCloakClient.setClientId("test-client");
        keyCloakClient.setRealm("test-realm");
        keyCloakClient.setClientSecret("secret");
    }

    @Test
    void shouldAuthenticateUserSuccessfully() {
        // given
        AccessTokenResponse mockToken = new AccessTokenResponse();
        mockToken.setToken("access-token");

        ResponseEntity<AccessTokenResponse> response = new ResponseEntity<>(mockToken, HttpStatus.OK);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(AccessTokenResponse.class)))
                .thenReturn(response);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("username", "user");
        params.add("password", "pass");

        // when
        AccessTokenResponse result = keyCloakClient.authenticate(params);

        // then
        assertNotNull(result);
        assertEquals("access-token", result.getToken());
    }

    @Test
    void shouldCreateUserInKeycloakSuccessfully() {
        // given
        NewUserRequestDto request = new NewUserRequestDto();
        request.setUsername("newuser");

        URI location = URI.create("http://localhost:8080/admin/realms/test-realm/users/1234");
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(location);

        ResponseEntity<Void> response = new ResponseEntity<>(headers, HttpStatus.CREATED);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(response);
        mockAdminToken();

        // when
        String userId = keyCloakClient.createUserInKeycloak(request);

        // then
        assertEquals("1234", userId);
    }

    @Test
    void shouldThrowWhenCreateUserFails() {
        // given
        NewUserRequestDto request = new NewUserRequestDto();
        request.setUsername("newuser");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Void.class)))
                .thenThrow(new RuntimeException("error"));
        mockAdminToken();

        // then
        assertThrows(KeycalokException.class, () -> keyCloakClient.createUserInKeycloak(request));
    }

    @Test
    void shouldUpdateUserEmailSuccessfully() {
        // given
        mockAdminToken();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        // when
        assertDoesNotThrow(() -> keyCloakClient.updateUserEmail("userId", "newemail@test.com"));
    }

    @Test
    void shouldHandleConflictWhenUpdatingEmail() {
        // given
        mockAdminToken();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Void.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.CONFLICT));

        // then
        assertThrows(UserAlreadyExistException.class, () -> keyCloakClient.updateUserEmail("userId", "newemail@test.com"));
    }

    @Test
    void shouldCheckPasswordSuccessfully() {
        // given
        UserRepresentation user = new UserRepresentation();
        user.setUsername("testuser");

        when(restTemplate.exchange(contains("/users/"), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserRepresentation.class)))
                .thenReturn(new ResponseEntity<>(user, HttpStatus.OK));
        when(restTemplate.exchange(contains("/token"), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("{}", HttpStatus.OK));
        mockAdminToken();

        // when
        boolean result = keyCloakClient.isPasswordCorrect("userId", "password");

        // then
        assertTrue(result);
    }

    @Test
    void shouldReturnFalseIfPasswordIncorrect() {
        // given
        UserRepresentation user = new UserRepresentation();
        user.setUsername("testuser");

        when(restTemplate.exchange(contains("/users/"), eq(HttpMethod.GET), any(HttpEntity.class), eq(UserRepresentation.class)))
                .thenReturn(new ResponseEntity<>(user, HttpStatus.OK));
        when(restTemplate.exchange(contains("/token"), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));
        mockAdminToken();

        // when
        boolean result = keyCloakClient.isPasswordCorrect("userId", "wrongpassword");

        // then
        assertFalse(result);
    }

    @Test
    void shouldUpdateUserPasswordSuccessfully() {
        // given
        mockAdminToken();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        Map<String, Object> passwordData = new HashMap<>();
        passwordData.put("value", "newpass");

        // when
        assertDoesNotThrow(() -> keyCloakClient.updateUserPassword("userId", passwordData));
    }

    @Test
    void shouldUpdateUsernameSuccessfully() {
        // given
        mockAdminToken();
        when(restTemplate.exchange(anyString(), eq(HttpMethod.PUT), any(HttpEntity.class), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        // when
        assertDoesNotThrow(() -> keyCloakClient.updateUsername("userId", "newUsername"));
    }

    private void mockAdminToken() {
        AccessTokenResponse token = new AccessTokenResponse();
        token.setToken("admin-token");

        when(restTemplate.exchange(contains("/token"), eq(HttpMethod.POST), any(HttpEntity.class), eq(AccessTokenResponse.class)))
                .thenReturn(new ResponseEntity<>(token, HttpStatus.OK));
    }
}
