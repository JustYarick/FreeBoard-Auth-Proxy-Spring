package com.FreeBoard.auth_proxy.service;

import com.FreeBoard.auth_proxy.exception.ExceptionClass.InvalidCredential;
import com.FreeBoard.auth_proxy.exception.ExceptionClass.KeycalokException;
import com.FreeBoard.auth_proxy.exception.ExceptionClass.UserNotFoundException;
import com.FreeBoard.auth_proxy.model.DTO.ChangeEmailRequest;
import com.FreeBoard.auth_proxy.model.DTO.ChangePasswordRequest;
import com.FreeBoard.auth_proxy.model.DTO.CredentialResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class UserCredentialsService {

    private final KeyCloakClient keyCloakClient;

    public CredentialResponse getUserEmail() {
        String userId = SecurityContextService.getCurrentUser();
        if(userId == null) throw new UserNotFoundException("User not found");

        return new CredentialResponse(keyCloakClient.getUserById(userId).getEmail(), "email");
    }

    public CredentialResponse getUsername() {
        String userId = SecurityContextService.getCurrentUser();
        if(userId == null) throw new UserNotFoundException("User not found");

        return new CredentialResponse(keyCloakClient.getUserById(userId).getUsername(), "username");
    }

    public Void changePassword(ChangePasswordRequest request) {
        String userId = SecurityContextService.getCurrentUser();
        if(userId == null) throw new UserNotFoundException("User not found");

        Map<String, Object> passwordData = new HashMap<>();
        passwordData.put("type", "password");
        passwordData.put("value", request.getNewPassword());
        passwordData.put("temporary", false);
        try {
            keyCloakClient.updateUserPassword(userId, passwordData);
        } catch (Exception e) {
            throw new KeycalokException("Failed to change password for user: " + userId, e);
        }
        return null;
    }

    public Void changeEmail(ChangeEmailRequest changeEmailRequest){
        String userId = SecurityContextService.getCurrentUser();
        if(userId == null) throw new UserNotFoundException("User not found");

        if(keyCloakClient.isPasswordCorrect(userId, changeEmailRequest.getPassword())){
            keyCloakClient.updateUserEmail(userId, changeEmailRequest.getNewEmail());
        } else {
            throw new InvalidCredential("Incorrect data");
        }
        return null;
    }

    public Void changeUsername(String username) {
        keyCloakClient.updateUsername(
                SecurityContextService.getCurrentUser(),
                username);
        return null;
    }
}
