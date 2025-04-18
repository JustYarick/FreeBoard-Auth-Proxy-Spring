package com.FreeBoard.auth_proxy.model.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangeEmailRequest {

    @NotBlank(message = "New email is required")
    @JsonProperty("new_email")
    @Email(message = "Invalid email format")
    private String newEmail;

    @NotBlank(message = "Password is required")
    private String password;
}
