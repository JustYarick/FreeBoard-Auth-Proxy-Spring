package com.FreeBoard.auth_proxy.controller;

import com.FreeBoard.auth_proxy.model.DTO.ChangeEmailRequest;
import com.FreeBoard.auth_proxy.model.DTO.ChangePasswordRequest;
import com.FreeBoard.auth_proxy.model.DTO.CredentialResponse;
import com.FreeBoard.auth_proxy.service.UserCredentialsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/user")
@AllArgsConstructor
@Validated
public class UserCredentialsController {

    private final UserCredentialsService userCredentialsService;

    @GetMapping("/email")
    public ResponseEntity<CredentialResponse> getEmail() {
        return ResponseEntity.ok(userCredentialsService.getUserEmail());
    }

    @GetMapping("/username")
    public ResponseEntity<CredentialResponse> getUsername() {
        return ResponseEntity.ok(userCredentialsService.getUsername());
    }

    @PatchMapping("/username")
    public ResponseEntity<Void> changeUsername(@RequestBody @Valid @NotBlank @Size(min = 4) String username) {
        return ResponseEntity.ok(userCredentialsService.changeUsername(username));
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequest changePasswordRequest) {
        return ResponseEntity.ok(userCredentialsService.changePassword(changePasswordRequest));
    }

    @PatchMapping("/email")
    public ResponseEntity<Void> changeEmail(@RequestBody @Valid ChangeEmailRequest changeEmailRequest) {
        return ResponseEntity.ok(userCredentialsService.changeEmail(changeEmailRequest));
    }
}