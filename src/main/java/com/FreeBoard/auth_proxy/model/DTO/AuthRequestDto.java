package com.FreeBoard.auth_proxy.model.DTO;

import lombok.Data;

@Data
public class AuthRequestDto {
    private String username;
    private String password;
}
