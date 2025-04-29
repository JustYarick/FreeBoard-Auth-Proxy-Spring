package com.FreeBoard.auth_proxy;
import com.FreeBoard.auth_proxy.controller.AuthController;
import com.FreeBoard.auth_proxy.exception.ExceptionClass.KeycalokException;
import com.FreeBoard.auth_proxy.exception.ExceptionClass.UserAlreadyExistException;
import com.FreeBoard.auth_proxy.model.DTO.AuthRequestDto;
import com.FreeBoard.auth_proxy.model.DTO.NewUserRequestDto;
import com.FreeBoard.auth_proxy.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.AccessTokenResponse;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    // login tests

    @Test
    void login_ShouldReturnTokenResponse() {
        AuthRequestDto request = new AuthRequestDto("testUser", "password");
        AccessTokenResponse mockResponse = new AccessTokenResponse();
        mockResponse.setToken("test-token");

        when(authService.authenticateUser(any(AuthRequestDto.class))).thenReturn(mockResponse);

        ResponseEntity<AccessTokenResponse> response = authController.login(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("test-token", response.getBody().getToken());
    }

    @Test
    void login_ShouldThrowException_WhenInvalidCredentials() {
        AuthRequestDto request = new AuthRequestDto("invalidUser", "wrongPassword");

        when(authService.authenticateUser(any(AuthRequestDto.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        assertThrows(HttpClientErrorException.class, () -> {
            authController.login(request);
        });
    }

    @Test
    void login_ShouldReturn400_WhenUsernameIsBlank() throws Exception {
        AuthRequestDto invalidRequest = new AuthRequestDto("", "validPassword");

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_ShouldReturn400_WhenPasswordIsBlank() throws Exception {
        AuthRequestDto request = new AuthRequestDto("testUser", "");  // Пустой пароль

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // refresh tests

    @Test
    void refresh_ShouldReturnNewToken() {
        String refreshToken = "test-refresh-token";
        AccessTokenResponse mockResponse = new AccessTokenResponse();
        mockResponse.setToken("new-token");

        when(authService.refreshToken(any(String.class))).thenReturn(mockResponse);

        ResponseEntity<AccessTokenResponse> response = authController.refresh(refreshToken);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("new-token", response.getBody().getToken());
    }

    @Test
    void refresh_ShouldThrowException_WhenInvalidRefreshToken() {
        String invalidRefreshToken = "invalid-token";

        when(authService.refreshToken(any(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThrows(HttpClientErrorException.class, () -> {
            authController.refresh(invalidRefreshToken);
        });
    }

    @Test
    void refresh_ShouldReturn400_WhenRefreshTokenIsBlank() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .param("refreshToken", ""))
                .andExpect(status().isBadRequest());
    }

    // register tests

    @Test
    void register_ShouldReturnTokenResponse() {
        NewUserRequestDto.Credentials credentials = new NewUserRequestDto.Credentials();
        credentials.setValue("password");

        NewUserRequestDto request = new NewUserRequestDto();
        request.setUsername("newUser");
        request.setEmail("new@user.com");
        request.setCredentials(List.of(credentials));

        AccessTokenResponse mockResponse = new AccessTokenResponse();
        mockResponse.setToken("new-user-token");

        when(authService.registerUser(any(NewUserRequestDto.class))).thenReturn(mockResponse);

        ResponseEntity<AccessTokenResponse> response = authController.register(request);

        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("new-user-token", response.getBody().getToken());
    }

    @Test
    void register_ShouldThrowException_WhenUsernameAlreadyExists() {
        NewUserRequestDto.Credentials credentials = new NewUserRequestDto.Credentials();
        credentials.setValue("password");

        NewUserRequestDto request = new NewUserRequestDto();
        request.setUsername("existingUser");
        request.setEmail("existing@user.com");
        request.setCredentials(List.of(credentials));

        when(authService.registerUser(any(NewUserRequestDto.class)))
                .thenThrow(new UserAlreadyExistException("User already exists"));

        assertThrows(UserAlreadyExistException.class, () -> {
            authController.register(request);
        });
    }

    @Test
    void register_ShouldReturn400_WhenEmailIsInvalid() throws Exception {
        NewUserRequestDto.Credentials credentials = new NewUserRequestDto.Credentials();
        credentials.setValue("password");

        NewUserRequestDto request = new NewUserRequestDto();
        request.setUsername("newUser");
        request.setEmail("invalid-email"); // Невалидный email
        request.setCredentials(List.of(credentials));

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_ShouldReturn400_WhenPasswordIsMissing() throws Exception {
        NewUserRequestDto request = new NewUserRequestDto();
        request.setUsername("newUser");
        request.setEmail("new@user.com");
        request.setCredentials(Collections.emptyList());

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(result -> System.out.println("Request Body: " + result.getRequest().getContentAsString()))
                .andExpect(status().isBadRequest());
    }


    @Test
    void register_ShouldThrowException_WhenKeycloakFails() {
        NewUserRequestDto.Credentials credentials = new NewUserRequestDto.Credentials();
        credentials.setValue("password");

        NewUserRequestDto request = new NewUserRequestDto();
        request.setUsername("newUser");
        request.setEmail("new@user.com");
        request.setCredentials(List.of(credentials));

        when(authService.registerUser(any(NewUserRequestDto.class)))
                .thenThrow(new KeycalokException("Keycloak unavailable"));

        assertThrows(KeycalokException.class, () -> {
            authController.register(request);
        });
    }
}