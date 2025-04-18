package com.FreeBoard.auth_proxy.controller;

import com.FreeBoard.auth_proxy.model.DTO.ChangeEmailRequest;
import com.FreeBoard.auth_proxy.model.DTO.ChangePasswordRequest;
import com.FreeBoard.auth_proxy.service.UserCredentialsService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/user")
@AllArgsConstructor
public class UserCredentialsController {

    private final UserCredentialsService userCredentialsService;

    @GetMapping("/email")
    public ResponseEntity<String> getEmail() {
        return ResponseEntity.ok(userCredentialsService.getUserEmail());
    }

    @GetMapping("/username")
    public ResponseEntity<String> getUsername() {
        return ResponseEntity.ok(userCredentialsService.getUsername());
    }

    @PatchMapping("/username")
    public ResponseEntity<Void> changeUsername(@RequestBody String username) {
        return ResponseEntity.ok(userCredentialsService.changeUsername(username));
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        return ResponseEntity.ok(userCredentialsService.changePassword(changePasswordRequest));
    }

    @PatchMapping("/email")
    public ResponseEntity<Void> changeEmail(@RequestBody ChangeEmailRequest changeEmailRequest) {
        return ResponseEntity.ok(userCredentialsService.changeEmail(changeEmailRequest));
    }
}