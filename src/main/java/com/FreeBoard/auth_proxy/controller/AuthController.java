package com.FreeBoard.auth_proxy.controller;

import com.FreeBoard.auth_proxy.DTO.AccessTokenResponse;
import com.FreeBoard.auth_proxy.DTO.AuthRequestDto;
import com.FreeBoard.auth_proxy.DTO.NewUserRequestDto;
import com.FreeBoard.auth_proxy.client.KeyCloakClient;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final KeyCloakClient keyCloakClient;

    @PostMapping("/authenticate")
    public ResponseEntity<AccessTokenResponse> login(@RequestBody AuthRequestDto request) {
        AccessTokenResponse token = keyCloakClient.authenticate(request);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refresh(@RequestParam String refreshToken) {
        AccessTokenResponse token = keyCloakClient.refreshToken(refreshToken);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody NewUserRequestDto userRequest) {
        keyCloakClient.registerUser(userRequest);
        return ResponseEntity.ok("User registered successfully");
    }
}
