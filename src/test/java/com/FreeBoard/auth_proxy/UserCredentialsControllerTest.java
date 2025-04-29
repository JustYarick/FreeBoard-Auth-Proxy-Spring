package com.FreeBoard.auth_proxy;

import com.FreeBoard.auth_proxy.model.DTO.ChangeEmailRequest;
import com.FreeBoard.auth_proxy.model.DTO.ChangePasswordRequest;
import com.FreeBoard.auth_proxy.model.DTO.CredentialResponse;
import com.FreeBoard.auth_proxy.service.UserCredentialsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserCredentialsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserCredentialsService userCredentialsService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public UserCredentialsService userCredentialsService() {
            return Mockito.mock(UserCredentialsService.class);
        }
    }

    @Test
    @WithMockUser
    void getEmail_ShouldReturnEmail_WhenAuthenticated() throws Exception {
        CredentialResponse mockResponse = new CredentialResponse("test@example.com", "email");
        when(userCredentialsService.getUserEmail()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/auth/user/email"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("test@example.com"))
                .andExpect(jsonPath("$.type").value("email"));
    }

    @Test
    @WithMockUser
    void getUsername_ShouldReturnUsername_WhenAuthenticated() throws Exception {
        CredentialResponse mockResponse = new CredentialResponse("testuser", "username");
        when(userCredentialsService.getUsername()).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/auth/user/username"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("testuser"))
                .andExpect(jsonPath("$.type").value("username"));
    }

    @Test
    @WithMockUser
    void changeUsername_ShouldReturnOk_WhenUsernameIsValid() throws Exception {
        String validUsername = "validUsername";

        mockMvc.perform(patch("/api/v1/auth/user/username")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUsername)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void changeUsername_ShouldReturnBadRequest_WhenUsernameIsInvalid() throws Exception {
        String invalidUsername = "";

        mockMvc.perform(patch("/api/v1/auth/user/username")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUsername)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void changePassword_ShouldReturnOk_WhenPasswordIsValid() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword("StrongPassword123!");

        mockMvc.perform(patch("/api/v1/auth/user/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void changePassword_ShouldReturnBadRequest_WhenPasswordIsInvalid() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setNewPassword("");

        mockMvc.perform(patch("/api/v1/auth/user/password")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void changeEmail_ShouldReturnOk_WhenEmailAndPasswordAreValid() throws Exception {
        ChangeEmailRequest request = new ChangeEmailRequest();
        request.setNewEmail("newemail@example.com");
        request.setPassword("ValidPassword123!");

        mockMvc.perform(patch("/api/v1/auth/user/email")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void changeEmail_ShouldReturnBadRequest_WhenEmailIsInvalid() throws Exception {
        ChangeEmailRequest request = new ChangeEmailRequest();
        request.setNewEmail("");
        request.setPassword("ValidPassword123!");

        mockMvc.perform(patch("/api/v1/auth/user/email")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void changeEmail_ShouldReturnBadRequest_WhenPasswordIsInvalid() throws Exception {
        ChangeEmailRequest request = new ChangeEmailRequest();
        request.setNewEmail("newemail@example.com");
        request.setPassword("");

        mockMvc.perform(patch("/api/v1/auth/user/email")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
