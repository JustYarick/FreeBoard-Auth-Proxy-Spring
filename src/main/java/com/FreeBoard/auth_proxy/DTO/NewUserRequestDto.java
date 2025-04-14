package com.FreeBoard.auth_proxy.DTO;

import lombok.Data;

import java.util.List;

@Data
public class NewUserRequestDto {
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private boolean enabled = true;
    private List<Credentials> credentials;

    @Data
    public static class Credentials {
        private String type = "password";
        private String value;
        private boolean temporary = false;
    }
}
