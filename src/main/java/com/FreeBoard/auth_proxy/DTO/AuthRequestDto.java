package com.FreeBoard.auth_proxy.DTO;

import lombok.Data;

@Data
public class AuthRequestDto {
    private String username;
    private String password;
}
