package com.FreeBoard.auth_proxy.controller;

import com.FreeBoard.auth_proxy.model.DTO.AuthRequestDto;
import com.FreeBoard.auth_proxy.model.DTO.NewUserRequestDto;
import com.FreeBoard.auth_proxy.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/authenticate")
    public ResponseEntity<AccessTokenResponse> login(@Valid @RequestBody AuthRequestDto request) {
        AccessTokenResponse token = authService.authenticateUser(request);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refresh(@Valid @RequestParam @NotBlank String refreshToken) {
        AccessTokenResponse token = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity<AccessTokenResponse> register(@Valid @RequestBody NewUserRequestDto userRequest) {
        return ResponseEntity.ok(authService.registerUser(userRequest));
    }
}
