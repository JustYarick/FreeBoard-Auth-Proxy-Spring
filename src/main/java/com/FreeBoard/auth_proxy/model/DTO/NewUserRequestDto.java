package com.FreeBoard.auth_proxy.model.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewUserRequestDto {

    @NotBlank(message = "Username cannot be blank")
    private String username;

    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @NotNull(message = "Enabled flag cannot be null")
    @Builder.Default
    private boolean enabled = true;

    @NotNull(message = "Credentials cannot be null")
    @Size(min = 1, message = "Credentials must not be empty")
    private List<Credentials> credentials;

    @Data
    public static class Credentials {

        @NotBlank(message = "Type cannot be blank")
        private String type = "password";

        @NotBlank(message = "Value cannot be blank")
        private String value;

        private boolean temporary = false;
    }
}