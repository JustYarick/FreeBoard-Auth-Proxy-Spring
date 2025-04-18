package com.FreeBoard.auth_proxy.controller;

import com.FreeBoard.auth_proxy.model.DTO.AccessTokenResponse;
import com.FreeBoard.auth_proxy.model.DTO.AuthRequestDto;
import com.FreeBoard.auth_proxy.model.DTO.NewUserRequestDto;
import com.FreeBoard.auth_proxy.service.AuthService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/authenticate")
    public ResponseEntity<AccessTokenResponse> login(@RequestBody AuthRequestDto request) {
        AccessTokenResponse token = authService.authenticateUser(request);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponse> refresh(@RequestParam String refreshToken) {
        AccessTokenResponse token = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody NewUserRequestDto userRequest) {
        authService.registerUser(userRequest);
        return ResponseEntity.ok("User registered successfully");
    }
}
